package com.cboda.baselibrary.view.magicindicator.buildins;

import android.content.Context;

import com.cboda.baselibrary.app.UtilBroadApplication;


/**
 * 博客: http://hackware.lucode.net
 * Created by hackware on 2016/6/26.
 */
public final class UIBroadUtil {

    public static int dip2px(Context context, double dpValue) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * density + 0.5);
    }

    public static int getScreenWidth(Context context) {
        return UtilBroadApplication.cxt.getResources().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight(Context context) {
        return UtilBroadApplication.cxt.getResources().getDisplayMetrics().heightPixels;
    }
}