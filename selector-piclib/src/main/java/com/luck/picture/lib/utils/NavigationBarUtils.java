package com.luck.picture.lib.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import androidx.annotation.RequiresApi;

public class NavigationBarUtils {

    /**
     * 获取底部导航栏高度
     */
    public static int getNavigationBarHeight(Context context) {
        if (isNavigationBarAvailable(context)) {
            Resources resources = context.getResources();
            int resourceId = resources.getIdentifier(
                    "navigation_bar_height",
                    "dimen",
                    "android"
            );
            if (resourceId > 0) {
                return resources.getDimensionPixelSize(resourceId);
            }
        }
        return 0;
    }

    /**
     * 检查是否有底部导航栏
     */
    public static boolean isNavigationBarAvailable(Context context) {
        Resources resources = context.getResources();
        int id = resources.getIdentifier(
                "config_showNavigationBar",
                "bool",
                "android"
        );
        return id > 0 && resources.getBoolean(id);
    }

    /**
     * 检查当前是否显示导航栏
     */
    public static boolean isNavigationBarShowing(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Display display = activity.getWindowManager().getDefaultDisplay();
            Point realSize = new Point();
            Point displaySize = new Point();
            display.getRealSize(realSize);
            display.getSize(displaySize);
            return realSize.y != displaySize.y;
        }
        return false;
    }

    /**
     * 获取屏幕真实高度（包含导航栏）
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static int getRealHeight(Activity activity) {
        WindowManager windowManager = activity.getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        Point point = new Point();
        display.getRealSize(point);
        return point.y;
    }

    /**
     * 获取屏幕显示高度（不包含导航栏）
     */
    public static int getDisplayHeight(Activity activity) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }
}
