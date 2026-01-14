package com.cboda.agora.ui.ui.basic;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class AUIShadowRectBroadDrawable extends Drawable {
    private final Paint drawPaint;
    private float[] cornerRadii;
    private int shadowColor;

    private float shadowRadius = 25f, shadowOffsetX = 0f, shadowOffsetY = 2f;
    private int offsetStart, offsetTop, offsetEnd, offsetBottom;

    public AUIShadowRectBroadDrawable() {
        drawPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    public AUIShadowRectBroadDrawable setColor(int color) {
        drawPaint.setColor(color);
        return this;
    }

    public AUIShadowRectBroadDrawable setShadowRadius(float shadowRadius) {
        this.shadowRadius = shadowRadius;
        return this;
    }

    public AUIShadowRectBroadDrawable setShadowColor(int color) {
        shadowColor = color;
        return this;
    }

    public AUIShadowRectBroadDrawable setShadowOffsetX(float shadowOffsetX) {
        this.shadowOffsetX = shadowOffsetX;
        return this;
    }

    public AUIShadowRectBroadDrawable setShadowOffsetY(float shadowOffsetY) {
        this.shadowOffsetY = shadowOffsetY;
        return this;
    }

    public AUIShadowRectBroadDrawable setCornerRadii(float[] radius){
        cornerRadii = radius;
        return this;
    }

    public AUIShadowRectBroadDrawable setCornerRadius(float radius){
        cornerRadii = new float[]{
                radius, radius,
                radius, radius,
                radius, radius,
                radius, radius
        };
        return this;
    }

    public AUIShadowRectBroadDrawable setOffsetStart(int offsetStart) {
        this.offsetStart = offsetStart;
        return this;
    }

    public AUIShadowRectBroadDrawable setOffsetTop(int offsetTop) {
        this.offsetTop = offsetTop;
        return this;
    }

    public AUIShadowRectBroadDrawable setOffsetEnd(int offsetEnd) {
        this.offsetEnd = offsetEnd;
        return this;
    }

    public AUIShadowRectBroadDrawable setOffsetBottom(int offsetBottom) {
        this.offsetBottom = offsetBottom;
        return this;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        Rect bounds = getBounds();

        // 画背景
        drawPaint.setShadowLayer(shadowRadius, shadowOffsetX, shadowOffsetY, shadowColor);
        Path path = new Path();
        path.addRoundRect(new RectF(bounds.left + offsetStart - (shadowOffsetX < 0? shadowOffsetX - shadowRadius: 0),
                bounds.top  - (shadowOffsetY < 0 ? shadowOffsetY - shadowRadius: 0) + offsetTop,
                bounds.right - offsetEnd - (shadowOffsetX > 0? shadowOffsetX + shadowRadius: 0),
                bounds.bottom - offsetBottom - ((shadowOffsetY > 0? shadowOffsetY + shadowRadius: 0))), cornerRadii, Path.Direction.CW);
        canvas.drawPath(path, drawPaint);
    }

    @Override
    public void setAlpha(int alpha) {
        final int oldAlpha = drawPaint.getAlpha();
        if (alpha != oldAlpha) {
            drawPaint.setAlpha(alpha);
            invalidateSelf();
        }
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        drawPaint.setColorFilter(colorFilter);
        invalidateSelf();
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
