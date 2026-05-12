package com.marc.helmet.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * Simple rolling line chart for last {@link #CAPACITY} speed samples (km/h). Draws an amber trace
 * and optional threshold line.
 */
public class SpeedHistoryView extends View {

    public static final int CAPACITY = 60;

    private final float[] history = new float[CAPACITY];
    private int count;
    private int head;

    private final Paint axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint thresholdPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();

    private float thresholdKmh = 80f;
    /** Max vertical scale for drawing (always at least threshold + margin). */
    private float ymax = 220f;

    public SpeedHistoryView(Context context) {
        super(context);
        init();
    }

    public SpeedHistoryView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SpeedHistoryView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        axisPaint.setColor(0xFF444444);
        axisPaint.setStrokeWidth(dp(1));
        axisPaint.setStyle(Paint.Style.STROKE);

        gridPaint.setColor(0x22E8750A);
        gridPaint.setStrokeWidth(dp(1));

        linePaint.setColor(0xFFE8750A);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(dp(2f));
        linePaint.setStrokeJoin(Paint.Join.ROUND);
        linePaint.setStrokeCap(Paint.Cap.ROUND);

        thresholdPaint.setColor(0x88C0392B);
        thresholdPaint.setStrokeWidth(dp(1.5f));
        thresholdPaint.setStyle(Paint.Style.STROKE);

        for (int i = 0; i < CAPACITY; i++) {
            history[i] = Float.NaN;
        }
    }

    public void setThresholdKmh(float kmh) {
        thresholdKmh = Math.max(1f, kmh);
        ymax = Math.max(220f, thresholdKmh * 1.25f);
        invalidate();
    }

    /** Adds one sample (km/h). Oldest drops when full. */
    public void addSample(float speedKmh) {
        float v = Math.max(0f, speedKmh);
        history[head] = v;
        head = (head + 1) % CAPACITY;
        if (count < CAPACITY) {
            count++;
        }
        ymax = Math.max(ymax, Math.max(thresholdKmh * 1.25f, v * 1.1f));
        if (ymax < 40f) {
            ymax = 40f;
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w = getWidth();
        float h = getHeight();
        if (w <= 0 || h <= 0) {
            return;
        }

        float pad = dp(8);
        float left = pad;
        float top = pad;
        float right = w - pad;
        float bottom = h - pad;

        for (int i = 0; i <= 4; i++) {
            float y = top + (bottom - top) * i / 4f;
            canvas.drawLine(left, y, right, y, gridPaint);
        }

        float yThr = bottom - (thresholdKmh / ymax) * (bottom - top);
        yThr = clamp(yThr, top, bottom);
        canvas.drawLine(left, yThr, right, yThr, thresholdPaint);

        path.reset();
        boolean started = false;
        if (count > 0) {
            int startIndex = count < CAPACITY ? 0 : head;
            for (int i = 0; i < count; i++) {
                int idx = (startIndex + i) % CAPACITY;
                float val = history[idx];
                if (Float.isNaN(val)) {
                    continue;
                }
                float t = count <= 1 ? 1f : (float) i / (float) (count - 1);
                float x = left + t * (right - left);
                float yn = bottom - (val / ymax) * (bottom - top);
                yn = clamp(yn, top, bottom);
                if (!started) {
                    path.moveTo(x, yn);
                    started = true;
                } else {
                    path.lineTo(x, yn);
                }
            }
        }
        if (started) {
            canvas.drawPath(path, linePaint);
        }

        canvas.drawRect(left, top, right, bottom, axisPaint);
    }

    private float dp(float d) {
        return d * getResources().getDisplayMetrics().density;
    }

    private static float clamp(float v, float lo, float hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}
