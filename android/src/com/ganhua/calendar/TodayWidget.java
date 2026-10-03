package com.ganhua.calendar;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import java.io.BufferedReader;
import java.io.InputStreamReader;
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
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_today);
        views.setTextViewText(R.id.widget_date, (now.get(Calendar.MONTH) + 1) + "月" + now.get(Calendar.DAY_OF_MONTH) + "日");
        views.setTextViewText(R.id.widget_week, WEEK[now.get(Calendar.DAY_OF_WEEK) - 1]);
        views.setTextViewText(R.id.widget_quote, quoteFor(context, now));
        Intent open = new Intent(context, MainActivity.class);
        PendingIntent pending = PendingIntent.getActivity(
                context, 0, open, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_root, pending);
        manager.updateAppWidget(id, views);
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
