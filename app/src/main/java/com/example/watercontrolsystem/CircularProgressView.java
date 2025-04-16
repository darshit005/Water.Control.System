package com.example.watercontrolsystem;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

public class CircularProgressView extends View {
    private Paint backgroundPaint;
    private Paint progressPaint;
    private Paint textPaint;
    private RectF arcRect;
    private float progress = 0f; // 0 to 100
    private int startAngle = 135;
    private int sweepAngle = 270;
    private String progressText = "0%";
    private int progressColor = Color.parseColor("#6200EE");

    public CircularProgressView(Context context) {
        super(context);
        init();
    }

    public CircularProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CircularProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        backgroundPaint = new Paint();
        backgroundPaint.setAntiAlias(true);
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(10f);
        backgroundPaint.setColor(Color.parseColor("#33FFFFFF"));
        backgroundPaint.setStrokeCap(Paint.Cap.ROUND);

        progressPaint = new Paint();
        progressPaint.setAntiAlias(true);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(10f);
        progressPaint.setColor(progressColor);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);

        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(40f);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextAlign(Paint.Align.CENTER);

        arcRect = new RectF();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        
        // Set the rectangle for the arc
        int padding = (int) (backgroundPaint.getStrokeWidth() * 1.5f);
        arcRect.set(padding, padding, w - padding, h - padding);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // Draw background arc
        canvas.drawArc(arcRect, startAngle, sweepAngle, false, backgroundPaint);
        
        // Draw progress arc
        float progressSweep = sweepAngle * (progress / 100f);
        canvas.drawArc(arcRect, startAngle, progressSweep, false, progressPaint);
        
        // Draw text in center
        float xPos = getWidth() / 2f;
        float yPos = getHeight() / 2f - ((textPaint.descent() + textPaint.ascent()) / 2f);
        canvas.drawText(progressText, xPos, yPos, textPaint);
    }

    public void setProgress(float progress, boolean animate) {
        if (animate) {
            ValueAnimator animator = ValueAnimator.ofFloat(this.progress, progress);
            animator.setDuration(500);
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.addUpdateListener(animation -> {
                this.progress = (float) animation.getAnimatedValue();
                this.progressText = (int) this.progress + "%";
                invalidate();
            });
            animator.start();
        } else {
            this.progress = progress;
            this.progressText = (int) progress + "%";
            invalidate();
        }
    }

    public void setProgressColor(int color) {
        this.progressColor = color;
        progressPaint.setColor(color);
        invalidate();
    }

    public void setProgressText(String text) {
        this.progressText = text;
        invalidate();
    }
} 