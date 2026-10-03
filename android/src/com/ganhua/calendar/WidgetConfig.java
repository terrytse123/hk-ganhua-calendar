package com.ganhua.calendar;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class WidgetConfig extends Activity {
    private int widgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED);
        setContentView(R.layout.widget_config);
        widgetId = getIntent().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }
        SharedPreferences prefs = getSharedPreferences("widget", MODE_PRIVATE);
        CheckBox weather = findViewById(R.id.weather_toggle);
        SeekBar seek = findViewById(R.id.alpha_seek);
        TextView label = findViewById(R.id.alpha_label);
        weather.setChecked(prefs.getBoolean(widgetId + "_weather", true));
        int alpha = prefs.getInt(widgetId + "_alpha", 180);
        seek.setProgress(alpha - 50);
        label.setText(percent(alpha));
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
                label.setText(percent(progress + 50));
            }
            public void onStartTrackingTouch(SeekBar bar) {}
            public void onStopTrackingTouch(SeekBar bar) {}
        });
        Button pick = findViewById(R.id.pick_bg);
        Button clear = findViewById(R.id.clear_bg);
        pick.setOnClickListener(v -> {
            Intent choose = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            choose.addCategory(Intent.CATEGORY_OPENABLE);
            choose.setType("image/*");
            startActivityForResult(choose, 21);
        });
        clear.setOnClickListener(v -> {
            new File(getFilesDir(), "widget-" + widgetId + ".jpg").delete();
            prefs.edit().putBoolean(widgetId + "_image", false).apply();
        });
        Button save = findViewById(R.id.save_widget);
        save.setOnClickListener(v -> {
            prefs.edit()
                    .putBoolean(widgetId + "_weather", weather.isChecked())
                    .putInt(widgetId + "_alpha", seek.getProgress() + 50)
                    .apply();
            TodayWidget.update(this, AppWidgetManager.getInstance(this), widgetId);
            Intent data = new Intent();
            data.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
            setResult(RESULT_OK, data);
            finish();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != 21 || resultCode != RESULT_OK || data == null || data.getData() == null) return;
        Uri uri = data.getData();
        try (InputStream in = getContentResolver().openInputStream(uri)) {
            Bitmap raw = BitmapFactory.decodeStream(in);
            if (raw == null) return;
            int w = 600;
            int h = Math.max(1, raw.getHeight() * w / raw.getWidth());
            Bitmap scaled = Bitmap.createScaledBitmap(raw, w, h, true);
            File out = new File(getFilesDir(), "widget-" + widgetId + ".jpg");
            try (FileOutputStream fos = new FileOutputStream(out)) {
                scaled.compress(Bitmap.CompressFormat.JPEG, 80, fos);
            }
            getSharedPreferences("widget", MODE_PRIVATE).edit().putBoolean(widgetId + "_image", true).apply();
        } catch (Exception ignored) {
        }
    }

    private String percent(int alpha) {
        return "透明度 " + Math.round(alpha * 100f / 255f) + "%";
    }
}
