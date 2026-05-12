package com.marc.helmet.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

/** Top-down sportbike schematic (CBR-inspired) rotating with instantaneous lean angle. */
public class LeanAngleView extends View {

    static final int A15 = Math.round(255 * 0.15f);
    static final int A08_GRID = Math.round(255 * 0.08f);
    static final int A40 = Math.round(255 * 0.40f);
    static final int A50 = Math.round(255 * 0.50f);
    static final int A30 = Math.round(255 * 0.30f);
    static final int A20 = Math.round(255 * 0.20f);
    static final int A03_SCAN = Math.round(255 * 0.03f);

    private float currentAngle = 0f;
    private float standingAngle = 0f;
    private float maxLeftAngle = -42f;
    private float maxRightAngle = 45f;

    private final Paint redPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dimPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint scanlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint centerLineBikePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint thresholdLeftPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint thresholdRightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint standingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint hudPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private ValueAnimator angleAnimator;

    public LeanAngleView(Context context) {
        super(context);
        initPaints(context);
    }

    public LeanAngleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initPaints(context);
    }

    private void initPaints(Context context) {
        setLayerType(LAYER_TYPE_SOFTWARE, null);

        float dm = context.getResources().getDisplayMetrics().density;

        redPaint.setColor(Color.parseColor("#FF2020"));
        redPaint.setStrokeWidth(2.5f * dm);
        redPaint.setStyle(Paint.Style.STROKE);

        dimPaint.setColor(Color.argb(A40, 255, 32, 32));
        dimPaint.setStrokeWidth(1f * dm);
        dimPaint.setStyle(Paint.Style.STROKE);

        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setColor(Color.argb(A08_GRID, 255, 32, 32));
        gridPaint.setStrokeWidth(0.5f);

        glowPaint.setStyle(Paint.Style.STROKE);
        glowPaint.setColor(Color.argb(A20, 255, 32, 32));
        glowPaint.setStrokeWidth(6f);
        glowPaint.setMaskFilter(new BlurMaskFilter(4f, BlurMaskFilter.Blur.SOLID));

        scanlinePaint.setStyle(Paint.Style.STROKE);
        scanlinePaint.setColor(Color.argb(A03_SCAN, 0, 0, 0));

        centerLineBikePaint.setStyle(Paint.Style.STROKE);
        centerLineBikePaint.setColor(Color.argb(A15, 255, 32, 32));
        centerLineBikePaint.setPathEffect(new DashPathEffect(new float[]{16f, 12f}, 0));

        thresholdLeftPaint.setStyle(Paint.Style.STROKE);
        thresholdLeftPaint.setColor(Color.argb(A30, 255, 32, 32));
        thresholdLeftPaint.setPathEffect(new DashPathEffect(new float[]{20f, 14f}, 0));
        thresholdLeftPaint.setStrokeWidth(1.5f * dm);

        thresholdRightPaint.setStyle(Paint.Style.STROKE);
        thresholdRightPaint.setColor(Color.argb(A30, 255, 32, 32));
        thresholdRightPaint.setPathEffect(new DashPathEffect(new float[]{20f, 14f}, 0));
        thresholdRightPaint.setStrokeWidth(1.5f * dm);

        standingPaint.setStyle(Paint.Style.STROKE);
        standingPaint.setColor(Color.argb(A20, 0, 255, 136));
        standingPaint.setStrokeWidth(dm);

        hudPaint.setStyle(Paint.Style.STROKE);
        hudPaint.setColor(Color.argb(A50, 255, 32, 32));
        hudPaint.setStrokeWidth(2f * dm);
    }

    private float dpToPx(Context context, float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.parseColor("#0A0A0A"));

        int w = getWidth();
        int h = getHeight();
        float cx = w / 2f;
        float cy = h / 2f;

        drawGrid(canvas, w, h);
        drawScanlines(canvas, h);

        canvas.save();
        canvas.translate(cx, cy);
        canvas.rotate(currentAngle);

        float unit = Math.min(w, h) * 0.35f;
        drawBikeSchematic(canvas, unit);
        canvas.restore();

        drawFixedThresholds(canvas, w, h, cx, cy);
        drawHudCorners(canvas, w, h);
    }

    private void drawGrid(Canvas canvas, int w, int h) {
        float step = dpToPx(getContext(), 20f);
        for (float x = 0; x <= w; x += step) {
            canvas.drawLine(x, 0, x, h, gridPaint);
        }
        for (float y = 0; y <= h; y += step) {
            canvas.drawLine(0, y, w, y, gridPaint);
        }
    }

    private void drawScanlines(Canvas canvas, int h) {
        int w = getWidth();
        for (float y = 0; y < h; y += 3f) {
            canvas.drawLine(0, y, w, y, scanlinePaint);
        }
    }

    private void drawBikeSchematic(Canvas canvas, float unit) {
        Paint p = redPaint;
        Paint d = dimPaint;

        RectF frontWheel =
                new RectF(
                        -unit * 0.6f,
                        -unit * 2.2f - unit,
                        unit * 0.6f,
                        -unit * 2.2f + unit);
        canvas.drawOval(frontWheel, p);

        float forkTop = -unit * 2.2f;
        float forkBottom = -unit * 1.35f;
        canvas.drawLine(-unit * 0.25f, forkTop + unit * 0.4f, -unit * 0.35f, forkBottom, p);
        canvas.drawLine(unit * 0.25f, forkTop + unit * 0.4f, unit * 0.35f, forkBottom, p);

        float barY = -unit * 1.2f;
        canvas.drawLine(-unit * 1.25f, barY, unit * 1.25f, barY, p);

        Path fairing = new Path();
        fairing.moveTo(-unit * 0.45f, -unit * 1.85f);
        fairing.lineTo(unit * 0.45f, -unit * 1.85f);
        fairing.lineTo(unit * 0.55f, -unit * 1.05f);
        fairing.lineTo(-unit * 0.55f, -unit * 1.05f);
        fairing.close();
        canvas.drawPath(fairing, d);

        RectF tank = new RectF(-unit * 0.85f, -unit * 0.95f, unit * 0.85f, unit * 0.15f);
        canvas.drawOval(tank, p);

        float engTop = unit * 0.2f;
        float engBottom = unit * 0.95f;
        RectF engine = new RectF(-unit * 0.7f, engTop, unit * 0.7f, engBottom);
        canvas.drawRoundRect(engine, unit * 0.08f, unit * 0.08f, p);
        for (int i = 1; i <= 4; i++) {
            float yy = engTop + (engBottom - engTop) * i / 5f;
            canvas.drawLine(-unit * 0.65f, yy, unit * 0.65f, yy, d);
        }

        RectF seat = new RectF(-unit * 0.45f, unit * 1.0f, unit * 0.45f, unit * 1.45f);
        canvas.drawOval(seat, p);

        canvas.drawLine(-unit * 0.35f, engBottom + unit * 0.05f, -unit * 0.55f, unit * 2.05f, p);
        canvas.drawLine(unit * 0.35f, engBottom + unit * 0.05f, unit * 0.55f, unit * 2.05f, p);

        float rearCy = unit * 2.2f;
        RectF rearWheel =
                new RectF(
                        -unit * 0.7f,
                        rearCy - unit * 1.05f,
                        unit * 0.7f,
                        rearCy + unit * 1.05f);
        canvas.drawOval(rearWheel, p);

        float hubY = rearCy;
        canvas.drawLine(-unit * 0.2f, engBottom + unit * 0.1f, -unit * 0.5f, hubY, d);
        canvas.drawLine(unit * 0.2f, engBottom + unit * 0.1f, unit * 0.5f, hubY, d);

        Path exhaust = new Path();
        exhaust.moveTo(unit * 0.72f, engTop + unit * 0.2f);
        exhaust.quadTo(unit * 1.1f, engBottom, unit * 0.62f, hubY + unit * 0.35f);
        Paint exh = new Paint(p);
        exh.setPathEffect(new DashPathEffect(new float[]{10f, 8f}, 0));
        canvas.drawPath(exhaust, exh);

        canvas.drawLine(0, -unit * 2.9f, 0, unit * 3.15f, centerLineBikePaint);

        float pegY = engTop + unit * 0.45f;
        float pegHalfW = unit * 0.12f;
        float pegH = unit * 0.1f;
        canvas.drawRect(-unit * 1.05f - pegHalfW, pegY - pegH / 2f, -unit * 1.05f + pegHalfW, pegY + pegH / 2f, p);
        canvas.drawRect(unit * 1.05f - pegHalfW, pegY - pegH / 2f, unit * 1.05f + pegHalfW, pegY + pegH / 2f, p);

        RectF faintGlowTank = new RectF(tank);
        faintGlowTank.inset(-unit * 0.05f, -unit * 0.05f);
        canvas.drawOval(faintGlowTank, glowPaint);
    }

    private void drawFixedThresholds(Canvas canvas, int w, int h, float cx, float cy) {
        float bottomY = h - dpToPx(getContext(), 32f);
        float lineLen = Math.min(w, h) * 0.58f;

        canvas.save();
        canvas.translate(cx, bottomY);
        canvas.rotate(maxLeftAngle);
        canvas.drawLine(0f, 0f, 0f, -lineLen, thresholdLeftPaint);
        canvas.restore();

        canvas.save();
        canvas.translate(cx, bottomY);
        canvas.rotate(maxRightAngle);
        canvas.drawLine(0f, 0f, 0f, -lineLen, thresholdRightPaint);
        canvas.restore();

        canvas.drawLine(cx, cy - lineLen * 0.9f, cx, cy + lineLen * 0.9f, standingPaint);
    }

    private void drawHudCorners(Canvas canvas, int w, int h) {
        float bracket = dpToPx(getContext(), 12f);
        float inset = dpToPx(getContext(), 6f);

        canvas.drawLine(inset, inset, inset + bracket, inset, hudPaint);
        canvas.drawLine(inset, inset, inset, inset + bracket, hudPaint);

        canvas.drawLine(w - inset, inset, w - inset - bracket, inset, hudPaint);
        canvas.drawLine(w - inset, inset, w - inset, inset + bracket, hudPaint);

        canvas.drawLine(inset, h - inset, inset + bracket, h - inset, hudPaint);
        canvas.drawLine(inset, h - inset, inset, h - inset - bracket, hudPaint);

        canvas.drawLine(w - inset, h - inset, w - inset - bracket, h - inset, hudPaint);
        canvas.drawLine(w - inset, h - inset, w - inset, h - inset - bracket, hudPaint);
    }

    public void setLeanAngle(float angle) {
        if (angleAnimator != null) {
            angleAnimator.cancel();
        }
        angleAnimator = ValueAnimator.ofFloat(currentAngle, angle);
        angleAnimator.setDuration(80);
        angleAnimator.setInterpolator(new LinearInterpolator());
        angleAnimator.addUpdateListener(
                animation -> {
                    currentAngle = (Float) animation.getAnimatedValue();
                    invalidate();
                });
        angleAnimator.start();
    }

    public void setCalibration(float standing, float maxLeft, float maxRight) {
        standingAngle = standing;
        maxLeftAngle = maxLeft;
        maxRightAngle = maxRight;
        invalidate();
    }

    public boolean isInDangerZone() {
        float delta = Math.abs(currentAngle - standingAngle);
        float limit = Math.max(Math.abs(maxLeftAngle), Math.abs(maxRightAngle)) * 0.85f;
        return delta > limit;
    }

    /** Current animated lean angle in degrees (for callers that read state). */
    public float getCurrentAngle() {
        return currentAngle;
    }
}
