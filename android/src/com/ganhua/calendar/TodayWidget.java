package com.ganhua.calendar;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.widget.RemoteViews;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TodayWidget extends AppWidgetProvider {
    private static final String[] WEEK = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] ids) {
        for (int id : ids) {
            update(context, manager, id);
        }
    }

    static void update(Context context, AppWidgetManager manager, int id) {
        Calendar now = Calendar.getInstance();
        SharedPreferences prefs = context.getSharedPreferences("widget", Context.MODE_PRIVATE);
        int alpha = prefs.getInt(id + "_alpha", 180);
        boolean weather = prefs.getBoolean(id + "_weather", false);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_today);
        views.setInt(R.id.widget_root, "setBackgroundColor", Color.argb(alpha, 16, 32, 51));
        views.setTextViewText(R.id.widget_date, (now.get(Calendar.MONTH) + 1) + "月" + now.get(Calendar.DAY_OF_MONTH) + "日");
        views.setTextViewText(R.id.widget_week, WEEK[now.get(Calendar.DAY_OF_WEEK) - 1]);
        views.setTextViewText(R.id.widget_quote, quoteFor(context, now));
        views.setTextViewText(R.id.widget_weather, weather ? "天氣載入中" : "");
        Intent open = new Intent(context, MainActivity.class);
        PendingIntent pending = PendingIntent.getActivity(
                context, 0, open, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_root, pending);
        manager.updateAppWidget(id, views);
        if (weather) {
            new Thread(() -> {
                String text = fetchWeather();
                views.setTextViewText(R.id.widget_weather, text);
                manager.updateAppWidget(id, views);
            }).start();
        }
    }

    private static String fetchWeather() {
        HttpURLConnection conn = null;
        try {
            URL url = new URL("https://api.open-meteo.com/v1/forecast?latitude=22.32&longitude=114.17&current=temperature_2m,weather_code&timezone=Asia%2FHong_Kong");
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(8000);
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder raw = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) raw.append(line);
            reader.close();
            JSONObject current = new JSONObject(raw.toString()).getJSONObject("current");
            int temp = Math.round((float) current.getDouble("temperature_2m"));
            return "香港 " + weatherText(current.getInt("weather_code")) + " " + temp + "°C";
        } catch (Exception e) {
            return "天氣暫時攞唔到";
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    private static String weatherText(int code) {
        if (code == 0) return "晴";
        if (code <= 3) return "多雲";
        if (code <= 48) return "霧";
        if (code <= 57) return "毛毛雨";
        if (code <= 67) return "雨";
        if (code <= 77) return "雪";
        if (code <= 82) return "驟雨";
        return "雷雨";
    }

    private static String quoteFor(Context context, Calendar now) {
        List<String> quotes = loadQuotes(context);
        if (quotes.isEmpty()) return "今日先休息，聽日再安排。";
        int key = now.get(Calendar.YEAR) * 10000 + (now.get(Calendar.MONTH) + 1) * 100 + now.get(Calendar.DAY_OF_MONTH);
        return quotes.get(Math.floorMod(key * 33, quotes.size()));
    }

    private static List<String> loadQuotes(Context context) {
        List<String> quotes = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                context.getAssets().open("www/data.js"), StandardCharsets.UTF_8))) {
            StringBuilder all = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) all.append(line);
            String raw = all.toString();
            int start = raw.indexOf('[');
            int end = raw.indexOf(']');
            if (start < 0 || end < start) return quotes;
            String body = raw.substring(start + 1, end);
            for (String part : body.split(",")) {
                String q = part.trim();
                if (q.startsWith("\"") && q.endsWith("\"")) quotes.add(q.substring(1, q.length() - 1));
            }
        } catch (Exception ignored) {
        }
        return quotes;
    }
}
