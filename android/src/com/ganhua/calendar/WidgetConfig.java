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
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class WidgetConfig extends Activity {
    private int widgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private Bitmap source;
    private ImageView preview;
    private SeekBar zoom;
    private SeekBar cropX;
    private SeekBar cropY;

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
        preview = findViewById(R.id.bg_preview);
        zoom = findViewById(R.id.zoom_seek);
        cropX = findViewById(R.id.crop_x);
        cropY = findViewById(R.id.crop_y);
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
        SeekBar.OnSeekBarChangeListener cropListener = new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) { showCrop(); }
            public void onStartTrackingTouch(SeekBar bar) {}
            public void onStopTrackingTouch(SeekBar bar) {}
        };
        zoom.setOnSeekBarChangeListener(cropListener);
        cropX.setOnSeekBarChangeListener(cropListener);
        cropY.setOnSeekBarChangeListener(cropListener);
        findViewById(R.id.pick_bg).setOnClickListener(v -> {
            Intent choose = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            choose.addCategory(Intent.CATEGORY_OPENABLE);
            choose.setType("image/*");
            startActivityForResult(choose, 21);
        });
        findViewById(R.id.apply_bg).setOnClickListener(v -> applyCrop());
        findViewById(R.id.clear_bg).setOnClickListener(v -> {
            source = null;
            preview.setImageDrawable(null);
            new File(getFilesDir(), "widget-" + widgetId + ".jpg").delete();
            prefs.edit().putBoolean(widgetId + "_image", false).apply();
            TodayWidget.update(this, AppWidgetManager.getInstance(this), widgetId);
        });
        findViewById(R.id.save_widget).setOnClickListener(v -> {
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
            int w = 900;
            int h = Math.max(1, raw.getHeight() * w / Math.max(1, raw.getWidth()));
            source = Bitmap.createScaledBitmap(raw, w, h, true);
            zoom.setProgress(0);
            cropX.setProgress(50);
            cropY.setProgress(50);
            showCrop();
        } catch (Exception ignored) {
        }
    }

    private void showCrop() {
        Bitmap cropped = crop();
        if (cropped != null) preview.setImageBitmap(cropped);
    }

    private Bitmap crop() {
        if (source == null) return null;
        float scale = 1f + zoom.getProgress() / 100f;
        int cw = Math.max(1, Math.round(source.getWidth() / scale));
        int ch = Math.max(1, Math.round(source.getHeight() / scale));
        int maxX = Math.max(0, source.getWidth() - cw);
        int maxY = Math.max(0, source.getHeight() - ch);
        int x = Math.round(maxX * (cropX.getProgress() / 100f));
        int y = Math.round(maxY * (cropY.getProgress() / 100f));
        return Bitmap.createBitmap(source, x, y, cw, ch);
    }

    private void applyCrop() {
        Bitmap cropped = crop();
        if (cropped == null) return;
        File out = new File(getFilesDir(), "widget-" + widgetId + ".jpg");
        try (FileOutputStream fos = new FileOutputStream(out)) {
            cropped.compress(Bitmap.CompressFormat.JPEG, 82, fos);
            getSharedPreferences("widget", MODE_PRIVATE).edit().putBoolean(widgetId + "_image", true).apply();
            TodayWidget.update(this, AppWidgetManager.getInstance(this), widgetId);
        } catch (Exception ignored) {
        }
    }

    private String percent(int alpha) {
        return "透明度 " + Math.round(alpha * 100f / 255f) + "%";
    }
}
