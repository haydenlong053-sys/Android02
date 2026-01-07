package com.cboda.intelligent.widge;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * @author:李迪迦
 * @date: 2024/9/20
 * @Type:
 */
public class BadgeBroadView extends View {

    private Paint mPaint;
    private int mCount = 0;

    public BadgeBroadView(Context context) {
        super(context);
        init();
    }

    public BadgeBroadView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setColor(Color.RED);
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(24);
    }

    public void setCount(int count) {
        mCount = count;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mCount > 0) {
            String text = String.valueOf(mCount);
            float textWidth = mPaint.measureText(text);
            float cx = getWidth() - textWidth / 2-20;
            float cy = 24;

            canvas.drawCircle(cx, cy, 20, mPaint);
            mPaint.setColor(Color.WHITE);
            canvas.drawText(text, cx - textWidth / 2, cy + 10, mPaint);
        }
    }
}