package com.example.watercontrolsystem;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Card3DView extends FrameLayout {
    private static final float MAX_ROTATION_DEGREES = 10.0f;
    private static final int ANIMATION_DURATION = 300;
    
    private float centerX;
    private float centerY;
    private Paint shadowPaint;
    private boolean isAnimating = false;
    
    public Card3DView(@NonNull Context context) {
        super(context);
        init();
    }

    public Card3DView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Card3DView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        // Enable hardware acceleration for better 3D effects
        setLayerType(View.LAYER_TYPE_HARDWARE, null);
        
        // Set up shadow paint
        shadowPaint = new Paint();
        shadowPaint.setColor(Color.BLACK);
        shadowPaint.setAlpha(50);
        
        // Make card clickable
        setClickable(true);
        
        // Set elevation for 3D effect
        setElevation(24);
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        centerX = w / 2.0f;
        centerY = h / 2.0f;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isAnimating) {
            return true;
        }
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                // Calculate rotation based on touch position
                float touchX = event.getX();
                float touchY = event.getY();
                
                float deltaX = (touchX - centerX) / centerX;
                float deltaY = (touchY - centerY) / centerY;
                
                // Apply rotation
                setRotationX(-deltaY * MAX_ROTATION_DEGREES);
                setRotationY(deltaX * MAX_ROTATION_DEGREES);
                
                // Apply shadow slightly offset
                invalidate();
                return true;
                
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                // Animate back to normal position
                isAnimating = true;
                
                ObjectAnimator animX = ObjectAnimator.ofFloat(this, "rotationX", getRotationX(), 0f);
                animX.setDuration(ANIMATION_DURATION);
                animX.setInterpolator(new AccelerateDecelerateInterpolator());
                
                ObjectAnimator animY = ObjectAnimator.ofFloat(this, "rotationY", getRotationY(), 0f);
                animY.setDuration(ANIMATION_DURATION);
                animY.setInterpolator(new AccelerateDecelerateInterpolator());
                
                animX.start();
                animY.start();
                
                postDelayed(() -> isAnimating = false, ANIMATION_DURATION);
                return true;
        }
        
        return super.onTouchEvent(event);
    }
    
    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
    }
} 