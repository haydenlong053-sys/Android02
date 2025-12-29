package com.cboda.baselibrary.view.magicindicator;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Px;


import com.cboda.baselibrary.R;
import com.cboda.baselibrary.view.magicindicator.abs.IPagerNavigator;
import com.cboda.baselibrary.view.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter;
import com.cboda.baselibrary.view.magicindicator.buildins.commonnavigator.abs.IMeasurablePagerTitleView;
import com.cboda.baselibrary.view.magicindicator.buildins.commonnavigator.abs.IPagerIndicator;
import com.cboda.baselibrary.view.magicindicator.buildins.commonnavigator.abs.IPagerTitleView;
import com.cboda.baselibrary.view.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator;
import com.cboda.baselibrary.view.magicindicator.buildins.commonnavigator.model.PositionData;
import com.cboda.baselibrary.view.magicindicator.buildins.commonnavigator.titles.CommonPagerTitleView;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 通用的ViewPager指示器，包含PagerTitle和PagerIndicator
 * 博客: http://hackware.lucode.net
 * Created by hackware on 2016/6/26.
 */
public class CommonNavigator extends FrameLayout implements IPagerNavigator, NavigatorHelper.OnNavigatorScrollListener {
    private HorizontalScrollView mScrollView;
    private LinearLayout mTitleContainer;
    private LinearLayout mIndicatorContainer;
    private IPagerIndicator mIndicator;

    private CommonNavigatorAdapter mAdapter;
    private NavigatorHelper mNavigatorHelper;

    private boolean isHorizontalFadingEdgeEnabled;
    private boolean isVerticalFadingEdgeEnabled;
    private int fadingEdgeLength = -1;
    private int overScrollMode = -1;

    /**
     * 提供给外部的参数配置
     */
    /****************************************************/
    private boolean mAdjustMode;   // 自适应模式，适用于数目固定的、少量的title
    private boolean mEnablePivotScroll; // 启动中心点滚动
    private float mScrollPivotX = 0.5f; // 滚动中心点 0.0f - 1.0f
    private boolean mSmoothScroll = true;   // 是否平滑滚动，适用于 !mAdjustMode && !mFollowTouch
    private boolean mFollowTouch = true;    // 是否手指跟随滚动
    private int mRightPadding;
    private int mLeftPadding;
    private boolean mIndicatorOnTop;    // 指示器是否在title上层，默认为下层
    private boolean mSkimOver;  // 跨多页切换时，中间页是否显示 "掠过" 效果
    private boolean mReselectWhenLayout = true; // PositionData准备好时，是否重新选中当前页，为true可保证在极端情况下指示器状态正确

    private int mTitleMarginEnd;
    private int mTitleMarginStart;
    /****************************************************/

    // 保存每个title的位置信息，为扩展indicator提供保障
    private List<PositionData> mPositionDataList = new ArrayList<PositionData>();

    private DataSetObserver mObserver = new DataSetObserver() {

        @Override
        public void onChanged() {
            mNavigatorHelper.setTotalCount(mAdapter.getCount());    // 如果使用helper，应始终保证helper中的totalCount为最新
            init();
        }

        @Override
        public void onInvalidated() {
            // 没什么用，暂不做处理
        }
    };

    public CommonNavigator(Context context) {
        super(context);
        mNavigatorHelper = new NavigatorHelper();
        mNavigatorHelper.setNavigatorScrollListener(this);
    }

    /**
     * 设置标题右边距
     *
     * @param titleMarginEnd 右边距
     */
    public void setTitleMarginEnd(int titleMarginEnd) {
        mTitleMarginEnd = titleMarginEnd;
    }

    /**
     * 设置 标题左边距
     *
     * @param titleMarginStart 左边距
     */
    public void setTitleMarginStart(int titleMarginStart) {
        mTitleMarginStart = titleMarginStart;
    }

    @Override
    public void notifyDataSetChanged() {
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    public boolean isAdjustMode() {
        return mAdjustMode;
    }

    public void setAdjustMode(boolean is) {
        mAdjustMode = is;
    }

    public CommonNavigatorAdapter getAdapter() {
        return mAdapter;
    }

    public void setAdapter(CommonNavigatorAdapter adapter) {
        if (mAdapter == adapter) {
            return;
        }
        if (mAdapter != null) {
            mAdapter.unregisterDataSetObserver(mObserver);
        }
        mAdapter = adapter;
        if (mAdapter != null) {
            mAdapter.registerDataSetObserver(mObserver);
            mNavigatorHelper.setTotalCount(mAdapter.getCount());
            if (mTitleContainer != null) {  // adapter改变时，应该重新init，但是第一次设置adapter不用，onAttachToMagicIndicator中有init
                mAdapter.notifyDataSetChanged();
            }
        } else {
            mNavigatorHelper.setTotalCount(0);
            init();
        }
    }

    private void init() {
        removeAllViews();

        View root;
        if (mAdjustMode) {
            root = LayoutInflater.from(getContext()).inflate(R.layout.pager_navigator_layout_no_scroll, this);
        } else {
            root = LayoutInflater.from(getContext()).inflate(R.layout.pager_navigator_layout, this);
        }

        mScrollView = root.findViewById(R.id.scroll_view);   // mAdjustMode为true时，mScrollView为null
        if (mScrollView != null) {
            if (overScrollMode != -1) {
                mScrollView.setOverScrollMode(overScrollMode);
            }
            if (fadingEdgeLength != -1) {
                mScrollView.setFadingEdgeLength(fadingEdgeLength);
            }
            mScrollView.setHorizontalFadingEdgeEnabled(isHorizontalFadingEdgeEnabled);
            mScrollView.setVerticalFadingEdgeEnabled(isVerticalFadingEdgeEnabled);
        }
        mTitleContainer = root.findViewById(R.id.title_container);
        mTitleContainer.setPadding(mLeftPadding, 0, mRightPadding, 0);

        mIndicatorContainer = root.findViewById(R.id.indicator_container);
        if (mIndicatorOnTop) {
            mIndicatorContainer.getParent().bringChildToFront(mIndicatorContainer);
        }

        initTitlesAndIndicator();
    }

    /**
     * 初始化title和indicator
     */
    private void initTitlesAndIndicator() {
        for (int i = 0, j = mNavigatorHelper.getTotalCount(); i < j; i++) {
            IPagerTitleView v = mAdapter.getTitleView(getContext(), i);
            if (v instanceof View) {
                View view = (View) v;
                LinearLayout.LayoutParams lp;
                if (mAdjustMode) {
                    lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
                    lp.weight = mAdapter.getTitleWeight(getContext(), i);
                } else {
                    lp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
                }
                lp.setMarginEnd(mTitleMarginEnd);
                lp.setMarginStart(mTitleMarginStart);
                mTitleContainer.addView(view, lp);
            }
        }
        if (mAdapter != null) {
            mIndicator = mAdapter.getIndicator(getContext());
            if (mIndicator instanceof View) {
                LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
                mIndicatorContainer.addView((View) mIndicator, lp);
            }
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    public BadgeDrawable getBadgeDrawable(int index) {
        for (int i = 0, j = mNavigatorHelper.getTotalCount(); i < j; i++) {
            if (index == i) {
                View child = mTitleContainer.getChildAt(index);
                if (child instanceof CommonPagerTitleView) {
                    CommonPagerTitleView PagerTitleView = (CommonPagerTitleView) child;
                    BadgeDrawable badgeDrawable = BadgeDrawable.create(PagerTitleView.getContext());
                    BadgeUtils.attachBadgeDrawable(badgeDrawable, PagerTitleView.getContentView(), this);
                    return badgeDrawable;
                }
            }
        }
        return null;
    }

    @SuppressLint("UnsafeOptInUsageError")
    public boolean removeBadgeDrawable(int index, BadgeDrawable badgeDrawable) {
        if (badgeDrawable == null) {
            return false;
        }
        for (int i = 0, j = mNavigatorHelper.getTotalCount(); i < j; i++) {
            if (index == i) {
                View child = mTitleContainer.getChildAt(index);
                if (child instanceof CommonPagerTitleView) {
                    CommonPagerTitleView PagerTitleView = (CommonPagerTitleView) child;
                    BadgeUtils.detachBadgeDrawable(badgeDrawable, PagerTitleView.getContentView());
                    return true;
                }
            }
        }
        return false;
    }

    private boolean mIsWhite;

    public void switchColor(boolean isWhite, boolean isSlide) {
        if (mIsWhite == isWhite) {
            return;
        }
        mIsWhite = isWhite;
        if (mIndicator instanceof LinePagerIndicator) {
            LinePagerIndicator linePagerIndicator = (LinePagerIndicator) mIndicator;
            linePagerIndicator.setColors(isWhite ?
                    Color.parseColor("#ffffff") : Color.parseColor("#333333"));
            linePagerIndicator.getPaint().setColor(isWhite ?
                    Color.parseColor("#ffffff") : Color.parseColor("#333333"));
            if (!isSlide) {
                //滑动过程中，不用重绘
                linePagerIndicator.invalidate();
            }
        }
        for (int i = 0; i < mTitleContainer.getChildCount(); i++) {
            View childAt = mTitleContainer.getChildAt(i);
            final ImageView titleImg = childAt.findViewById(R.id.title_img);
            final TextView titleText = childAt.findViewById(R.id.title_text);
            if (titleImg == null) {
                break;
            }
            if (i == 0) {
//                titleImg.setImageResource(isWhite ? R.drawable.live_ta_white : R.drawable.live_ta);
            } else {
                titleText.setTextColor(isWhite ?
                        Color.parseColor("#ffffff") : Color.parseColor("#333333"));
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mAdapter != null) {
            preparePositionData();
            if (mIndicator != null) {
                mIndicator.onPositionDataProvide(mPositionDataList);
            }
            if (mReselectWhenLayout && mNavigatorHelper.getScrollState() == ScrollState.SCROLL_STATE_IDLE) {
                onPageSelected(mNavigatorHelper.getCurrentIndex());
                onPageScrolled(mNavigatorHelper.getCurrentIndex(), 0.0f, 0);
            }
        }
    }

    /**
     * 获取title的位置信息，为打造不同的指示器、各种效果提供可能
     */
    private void preparePositionData() {
        mPositionDataList.clear();
        for (int i = 0, j = mNavigatorHelper.getTotalCount(); i < j; i++) {
            PositionData data = new PositionData();
            View v = mTitleContainer.getChildAt(i);
            if (v != null) {
                data.mLeft = v.getLeft();
                data.mTop = v.getTop();
                data.mRight = v.getRight();
                data.mBottom = v.getBottom();
                if (v instanceof IMeasurablePagerTitleView) {
                    IMeasurablePagerTitleView view = (IMeasurablePagerTitleView) v;
                    data.mContentLeft = view.getContentLeft();
                    data.mContentTop = view.getContentTop();
                    data.mContentRight = view.getContentRight();
                    data.mContentBottom = view.getContentBottom();
                } else {
                    data.mContentLeft = data.mLeft;
                    data.mContentTop = data.mTop;
                    data.mContentRight = data.mRight;
                    data.mContentBottom = data.mBottom;
                }
            }
            mPositionDataList.add(data);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (mAdapter != null) {

            mNavigatorHelper.onPageScrolled(position, positionOffset, positionOffsetPixels);
            if (mIndicator != null) {
                mIndicator.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            // 手指跟随滚动
            if (mScrollView != null && mPositionDataList.size() > 0 && position >= 0 && position < mPositionDataList.size()) {
                if (mFollowTouch) {
                    int currentPosition = Math.min(mPositionDataList.size() - 1, position);
                    int nextPosition = Math.min(mPositionDataList.size() - 1, position + 1);
                    PositionData current = mPositionDataList.get(currentPosition);
                    PositionData next = mPositionDataList.get(nextPosition);
                    float scrollTo = current.horizontalCenter() - mScrollView.getWidth() * mScrollPivotX;
                    float nextScrollTo = next.horizontalCenter() - mScrollView.getWidth() * mScrollPivotX;
                    mScrollView.scrollTo((int) (scrollTo + (nextScrollTo - scrollTo) * positionOffset), 0);
                } else if (!mEnablePivotScroll) {
                    // TODO 实现待选中项完全显示出来
                }
            }
        }
    }

    public float getScrollPivotX() {
        return mScrollPivotX;
    }

    public void setScrollPivotX(float scrollPivotX) {
        mScrollPivotX = scrollPivotX;
    }

    @Override
    public void onPageSelected(int position) {
        if (mAdapter != null) {
            mNavigatorHelper.onPageSelected(position);
            if (mIndicator != null) {
                mIndicator.onPageSelected(position);
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (mAdapter != null) {
            mNavigatorHelper.onPageScrollStateChanged(state);
            if (mIndicator != null) {
                mIndicator.onPageScrollStateChanged(state);
            }
        }
    }

    @Override
    public void onAttachToMagicIndicator() {
        init(); // 将初始化延迟到这里
    }

    @Override
    public void onDetachFromMagicIndicator() {
    }

    public IPagerIndicator getPagerIndicator() {
        return mIndicator;
    }

    public boolean isEnablePivotScroll() {
        return mEnablePivotScroll;
    }

    public void setEnablePivotScroll(boolean is) {
        mEnablePivotScroll = is;
    }

    @Override
    public void onEnter(int index, int totalCount, float enterPercent, boolean leftToRight) {
        if (mTitleContainer == null) {
            return;
        }
        View v = mTitleContainer.getChildAt(index);
        if (v instanceof IPagerTitleView) {
            ((IPagerTitleView) v).onEnter(index, totalCount, enterPercent, leftToRight);
        }
    }

    @Override
    public void onLeave(int index, int totalCount, float leavePercent, boolean leftToRight) {
        if (mTitleContainer == null) {
            return;
        }
        View v = mTitleContainer.getChildAt(index);
        if (v instanceof IPagerTitleView) {
            ((IPagerTitleView) v).onLeave(index, totalCount, leavePercent, leftToRight);
        }
    }

    public boolean isSmoothScroll() {
        return mSmoothScroll;
    }

    public void setSmoothScroll(boolean smoothScroll) {
        mSmoothScroll = smoothScroll;
    }

    public boolean isFollowTouch() {
        return mFollowTouch;
    }

    public void setFollowTouch(boolean followTouch) {
        mFollowTouch = followTouch;
    }

    public boolean isSkimOver() {
        return mSkimOver;
    }

    public void setSkimOver(boolean skimOver) {
        mSkimOver = skimOver;
        mNavigatorHelper.setSkimOver(skimOver);
    }

    @Override
    public void onSelected(int index, int totalCount) {
        if (mTitleContainer == null) {
            return;
        }
        View v = mTitleContainer.getChildAt(index);
        if (v instanceof IPagerTitleView) {
            ((IPagerTitleView) v).onSelected(index, totalCount);
        }
        if (!mAdjustMode && !mFollowTouch && mScrollView != null && mPositionDataList.size() > 0) {
            int currentIndex = Math.min(mPositionDataList.size() - 1, index);
            PositionData current = mPositionDataList.get(currentIndex);
            if (mEnablePivotScroll) {
                float scrollTo = current.horizontalCenter() - mScrollView.getWidth() * mScrollPivotX;
                if (mSmoothScroll) {
                    mScrollView.smoothScrollTo((int) (scrollTo), 0);
                } else {
                    mScrollView.scrollTo((int) (scrollTo), 0);
                }
            } else {
                // 如果当前项被部分遮挡，则滚动显示完全
                if (mScrollView.getScrollX() > current.mLeft) {
                    if (mSmoothScroll) {
                        mScrollView.smoothScrollTo(current.mLeft, 0);
                    } else {
                        mScrollView.scrollTo(current.mLeft, 0);
                    }
                } else if (mScrollView.getScrollX() + getWidth() < current.mRight) {
                    if (mSmoothScroll) {
                        mScrollView.smoothScrollTo(current.mRight - getWidth(), 0);
                    } else {
                        mScrollView.scrollTo(current.mRight - getWidth(), 0);
                    }
                }
            }
        }
    }

    @Override
    public void onDeselected(int index, int totalCount) {
        if (mTitleContainer == null) {
            return;
        }
        View v = mTitleContainer.getChildAt(index);
        if (v instanceof IPagerTitleView) {
            ((IPagerTitleView) v).onDeselected(index, totalCount);
        }
    }

    public IPagerTitleView getPagerTitleView(int index) {
        if (mTitleContainer == null) {
            return null;
        }
        return (IPagerTitleView) mTitleContainer.getChildAt(index);
    }

    public LinearLayout getTitleContainer() {
        return mTitleContainer;
    }

    public int getRightPadding() {
        return mRightPadding;
    }

    public void setRightPadding(int rightPadding) {
        mRightPadding = rightPadding;
    }

    public int getLeftPadding() {
        return mLeftPadding;
    }

    public void setLeftPadding(int leftPadding) {
        mLeftPadding = leftPadding;
    }

    public boolean isIndicatorOnTop() {
        return mIndicatorOnTop;
    }

    public void setIndicatorOnTop(boolean indicatorOnTop) {
        mIndicatorOnTop = indicatorOnTop;
    }

    public boolean isReselectWhenLayout() {
        return mReselectWhenLayout;
    }

    public void setReselectWhenLayout(boolean reselectWhenLayout) {
        mReselectWhenLayout = reselectWhenLayout;
    }

    public void setFadingEdgeLength(@Px int length) {
        fadingEdgeLength = length;
    }

    public void setHorizontalFadingEdgeEnabled(boolean enabled) {
        isHorizontalFadingEdgeEnabled = enabled;
    }

    public void setVerticalFadingEdgeEnabled(boolean enabled) {
        isVerticalFadingEdgeEnabled = enabled;
    }

    public void setOverScrollMode(int overScrollMode) {
        this.overScrollMode = overScrollMode;
    }

}
