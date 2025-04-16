package com.example.watercontrolsystem;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;

public class WaveView extends View {
    private Path wavePath;
    private Paint wavePaint;
    private int viewWidth;
    private int viewHeight;
    private float waterLevel = 0.5f; // 0.0 to 1.0
    private int waveHeight = 20;
    private int waveCount = 2;
    private float phase = 0;
    private WaveAnimation waveAnimation;

    public WaveView(Context context) {
        super(context);
        init();
    }

    public WaveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WaveView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        wavePath = new Path();
        wavePaint = new Paint();
        wavePaint.setAntiAlias(true);
        wavePaint.setStyle(Paint.Style.FILL);
        wavePaint.setColor(Color.parseColor("#6200EE"));
        
        waveAnimation = new WaveAnimation();
        waveAnimation.setRepeatCount(Animation.INFINITE);
        waveAnimation.setDuration(1500);
        waveAnimation.setInterpolator(new LinearInterpolator());
        startAnimation(waveAnimation);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewWidth = w;
        viewHeight = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // Calculate the water height based on water level
        int waterHeight = (int) (viewHeight * (1 - waterLevel));
        
        wavePath.reset();
        wavePath.moveTo(0, waterHeight);
        
        // Draw the wave
        float waveLength = viewWidth / waveCount;
        for (int i = 0; i <= waveCount; i++) {
            float x1 = i * waveLength;
            float y1 = waterHeight + waveHeight * (float) Math.sin(x1 / waveLength * 2 * Math.PI + phase);
            wavePath.lineTo(x1, y1);
        }
        
        // Close the path
        wavePath.lineTo(viewWidth, viewHeight);
        wavePath.lineTo(0, viewHeight);
        wavePath.close();
        
        canvas.drawPath(wavePath, wavePaint);
    }

    public void setWaterLevel(float level) {
        this.waterLevel = Math.max(0, Math.min(1, level));
        invalidate();
    }

    public float getWaterLevel() {
        return waterLevel;
    }

    public void setWaveColor(int color) {
        wavePaint.setColor(color);
        invalidate();
    }

    private class WaveAnimation extends Animation {
        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            phase = interpolatedTime * 2 * (float) Math.PI;
            invalidate();
        }
    }
} 