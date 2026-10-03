package com.ganhua.calendar;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.widget.Toast;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
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
    private float zoomLevel = 1f;
    private float focusX = 0.5f;
    private float focusY = 0.5f;
    private float lastX;
    private float lastY;
    private float lastSpan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED);
        setContentView(R.layout.widget_config);
        widgetId = getIntent().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            int[] ids = AppWidgetManager.getInstance(this).getAppWidgetIds(new ComponentName(this, TodayWidget.class));
            if (ids.length > 0) widgetId = ids[0];
        }
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            Toast.makeText(this, "請先加小工具", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        SharedPreferences prefs = getSharedPreferences("widget", MODE_PRIVATE);
        CheckBox weather = findViewById(R.id.weather_toggle);
        SeekBar seek = findViewById(R.id.alpha_seek);
        TextView label = findViewById(R.id.alpha_label);
        preview = findViewById(R.id.bg_preview);
        preview.setOnTouchListener(this::onPreviewTouch);
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
        findViewById(R.id.pick_bg).setOnClickListener(v -> {
            Intent choose = new Intent(Intent.ACTION_GET_CONTENT);
            choose.addCategory(Intent.CATEGORY_OPENABLE);
            choose.setType("image/*");
            startActivityForResult(Intent.createChooser(choose, "轉換背景"), 21);
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
            zoomLevel = 1f;
            focusX = 0.5f;
            focusY = 0.5f;
            showCrop();
            Toast.makeText(this, "可以裁切，再撳套用", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "張圖打唔開", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean onPreviewTouch(View view, MotionEvent event) {
        if (source == null) return false;
        if (event.getPointerCount() >= 2) {
            float dx = event.getX(0) - event.getX(1);
            float dy = event.getY(0) - event.getY(1);
            float span = (float) Math.hypot(dx, dy);
            if (event.getActionMasked() == MotionEvent.ACTION_MOVE && lastSpan > 0) {
                zoomLevel = Math.max(1f, Math.min(3f, zoomLevel * span / lastSpan));
                showCrop();
            }
            lastSpan = span;
            return true;
        }
        lastSpan = 0;
        if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
            focusX = clamp(focusX - (event.getX() - lastX) / Math.max(1, view.getWidth()));
            focusY = clamp(focusY - (event.getY() - lastY) / Math.max(1, view.getHeight()));
            showCrop();
        }
        lastX = event.getX();
        lastY = event.getY();
        return true;
    }

    private float clamp(float n) { return Math.max(0f, Math.min(1f, n)); }

    private void showCrop() {
        Bitmap cropped = crop();
        if (cropped != null) preview.setImageBitmap(cropped);
    }

    private Bitmap crop() {
        if (source == null) return null;
        float scale = zoomLevel;
        int cw = Math.max(1, Math.round(source.getWidth() / scale));
        int ch = Math.max(1, Math.round(source.getHeight() / scale));
        int maxX = Math.max(0, source.getWidth() - cw);
        int maxY = Math.max(0, source.getHeight() - ch);
        int x = Math.round(maxX * focusX);
        int y = Math.round(maxY * focusY);
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
            Toast.makeText(this, "背景已套用", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "套用失敗", Toast.LENGTH_SHORT).show();
        }
    }

    private String percent(int alpha) {
        return "透明度 " + Math.round(alpha * 100f / 255f) + "%";
    }
}
