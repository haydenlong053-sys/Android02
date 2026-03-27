package com.cboda.baselibrary.view.magicindicator.buildins.commonnavigator.indicators;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.cboda.baselibrary.view.magicindicator.FragmentContainerBroadHelper;
import com.cboda.baselibrary.view.magicindicator.buildins.UIBroadUtil;
import com.cboda.baselibrary.view.magicindicator.buildins.commonnavigator.abs.IPagerIndicator;
import com.cboda.baselibrary.view.magicindicator.buildins.commonnavigator.model.PositionBroadData;

import java.util.List;


/**
 * 包裹住内容区域的指示器，类似天天快报的切换效果，需要和IMeasurablePagerTitleView配合使用
 * 博客: http://hackware.lucode.net
 * Created by hackware on 2016/6/26.
 */
public class WrapPager2Indicator extends View implements IPagerIndicator {
    private int mVerticalPadding;
    private int mHorizontalPadding;
    private int mFillColor;
    private float mRoundRadius;
    private Interpolator mStartInterpolator = new LinearInterpolator();
    private Interpolator mEndInterpolator = new LinearInterpolator();

    private List<PositionBroadData> mPositionDataList;
    private Paint mPaint;

    private RectF mRect = new RectF();
    private boolean mRoundRadiusSet;

    public WrapPager2Indicator(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        mVerticalPadding = UIBroadUtil.dip2px(context, 6);
        mHorizontalPadding = UIBroadUtil.dip2px(context, 10);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mPaint.setColor(mFillColor);
        float w = getWidth();
        float h = getHeight();
        if (w <= 0f || h <= 0f) {
            return;
        }
        // mRect 基于 title 区域 + padding；右侧可能超出本 View 宽度，系统会按 View 边界裁切，
        // 圆角在右缘外侧的部分被切掉，看起来像「只有一侧没圆角」。先约束到绘制区域内再画。
        float left = Math.max(0f, Math.min(mRect.left, w));
        float top = Math.max(0f, Math.min(mRect.top, h));
        float right = Math.max(0f, Math.min(mRect.right, w));
        float bottom = Math.max(0f, Math.min(mRect.bottom, h));
        if (right <= left || bottom <= top) {
            return;
        }
        float maxR = Math.min((right - left) / 2f, (bottom - top) / 2f);
        float r = Math.min(mRoundRadius, maxR);
        canvas.drawRoundRect(left, top, right, bottom, r, r, mPaint);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (mPositionDataList == null || mPositionDataList.isEmpty()) {
            return;
        }

        // 计算锚点位置
        PositionBroadData current = FragmentContainerBroadHelper.getImitativePositionData(mPositionDataList, position);
        PositionBroadData next = FragmentContainerBroadHelper.getImitativePositionData(mPositionDataList, position + 1);

        mRect.left = current.mContentLeft - mHorizontalPadding + (next.mContentLeft - current.mContentLeft) * mEndInterpolator.getInterpolation(positionOffset);
        mRect.top = current.mContentTop - mVerticalPadding;
        mRect.right = current.mContentRight + mHorizontalPadding + (next.mContentRight - current.mContentRight) * mStartInterpolator.getInterpolation(positionOffset);
        mRect.bottom = current.mContentBottom + mVerticalPadding;

        if (!mRoundRadiusSet) {
            mRoundRadius = mRect.height() / 2;
        }

        invalidate();
    }

    @Override
    public void onPageSelected(int position) {
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    public void onPositionDataProvide(List<PositionBroadData> dataList) {
        mPositionDataList = dataList;
    }

    public Paint getPaint() {
        return mPaint;
    }

    public int getVerticalPadding() {
        return mVerticalPadding;
    }

    public void setVerticalPadding(int verticalPadding) {
        mVerticalPadding = verticalPadding;
    }

    public int getHorizontalPadding() {
        return mHorizontalPadding;
    }

    public void setHorizontalPadding(int horizontalPadding) {
        mHorizontalPadding = horizontalPadding;
    }

    public int getFillColor() {
        return mFillColor;
    }

    public void setFillColor(int fillColor) {
        mFillColor = fillColor;
        invalidate();
    }

    public float getRoundRadius() {
        return mRoundRadius;
    }

    public void setRoundRadius(float roundRadius) {
        mRoundRadius = roundRadius;
        mRoundRadiusSet = true;
        invalidate();
    }

    public Interpolator getStartInterpolator() {
        return mStartInterpolator;
    }

    public void setStartInterpolator(Interpolator startInterpolator) {
        mStartInterpolator = startInterpolator;
        if (mStartInterpolator == null) {
            mStartInterpolator = new LinearInterpolator();
        }
    }

    public Interpolator getEndInterpolator() {
        return mEndInterpolator;
    }

    public void setEndInterpolator(Interpolator endInterpolator) {
        mEndInterpolator = endInterpolator;
        if (mEndInterpolator == null) {
            mEndInterpolator = new LinearInterpolator();
        }
    }
}

