package com.lxkj.aiim.lj.view;

import android.content.Context;
import android.util.AttributeSet;

import androidx.core.widget.NestedScrollView;

/**
 * Created by kxn on 2021/4/23 0023.
 */
public class MyScrollView extends NestedScrollView {
    private OnScrollChanged mOnScrollChanged;
    public MyScrollView(Context context) {
        this(context, null);
    }
    public MyScrollView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }
    public MyScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (mOnScrollChanged != null) {
            mOnScrollChanged.onScroll(l, t, oldl, oldt);
        }
    }
    public void setOnScrollChanged(OnScrollChanged onScrollChanged) {
        this.mOnScrollChanged = onScrollChanged;
    }
    public interface OnScrollChanged {
        void onScroll(int l, int t, int oldl, int oldt);
    }
}