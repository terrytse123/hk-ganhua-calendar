package com.ganhua.calendar;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;

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

    private String percent(int alpha) {
        return "透明度 " + Math.round(alpha * 100f / 255f) + "%";
    }
}
