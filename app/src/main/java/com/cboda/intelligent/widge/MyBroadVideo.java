package com.cboda.intelligent.widge;

import android.content.Context;
import android.util.AttributeSet;

import com.cboda.intelligent.R;

import cn.jzvd.JzvdStd;

/**
 * @author:李迪迦
 * @date: 2024/10/15
 * @Type:
 */
@Deprecated
public class MyBroadVideo extends JzvdStd {

    public MyBroadVideo(Context context) {
        super(context);
    }

    public MyBroadVideo(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public int getLayoutId() {
        return R.layout.layout_myvideo;
    }

}