package com.cboda.intelligent.widge;

import android.content.Context;


/**
 * Created by kxn on 2020/11/20 0020.
 */
public class ColorFlipPagerTitleBroadView extends ScaleTransitionPagerTitleBroadView {
    private float mChangePercent = 0.5f;

    public ColorFlipPagerTitleBroadView(Context context) {
        super(context);
    }



    @Override
    public void onSelected(int index, int totalCount) {
        setTextSize(15);
    }

    @Override
    public void onDeselected(int index, int totalCount) {
        setTextSize(15);
    }

    public float getChangePercent() {
        return mChangePercent;
    }

    public void setChangePercent(float changePercent) {
        mChangePercent = changePercent;
    }
}
