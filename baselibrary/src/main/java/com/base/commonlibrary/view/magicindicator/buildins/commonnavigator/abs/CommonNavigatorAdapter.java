package com.base.commonlibrary.view.magicindicator.buildins.commonnavigator.abs;

import android.content.Context;
import android.database.DataSetObservable;
import android.database.DataSetObserver;

/**
 * CommonNavigator适配器，通过它可轻松切换不同的指示器样式
 * 博客: http://hackware.lucode.net
 * Created by hackware on 2016/6/26.
 */
public abstract class CommonNavigatorAdapter {
    private OnIndicatorItemClickListener mListener;

    public void setOnIndicatorTapClickListener(OnIndicatorItemClickListener listener) {
        mListener = listener;
    }

    public interface OnIndicatorItemClickListener {
        void onTabClick(int index);
    }

    public void indicatorItemOnTabClick(int index) {
        if (null != mListener) {
            mListener.onTabClick(index);
        }
    }

    private final DataSetObservable mDataSetObservable = new DataSetObservable();

    public abstract int getCount();

    public abstract IPagerTitleView getTitleView(Context context, int index);

    public abstract IPagerIndicator getIndicator(Context context);

    public float getTitleWeight(Context context, int index) {
        return 1;
    }

    public final void registerDataSetObserver(DataSetObserver observer) {
        mDataSetObservable.registerObserver(observer);
    }

    public final void unregisterDataSetObserver(DataSetObserver observer) {
        mDataSetObservable.unregisterObserver(observer);
    }

    public final void notifyDataSetChanged() {
        mDataSetObservable.notifyChanged();
    }

    public final void notifyDataSetInvalidated() {
        mDataSetObservable.notifyInvalidated();
    }
}
