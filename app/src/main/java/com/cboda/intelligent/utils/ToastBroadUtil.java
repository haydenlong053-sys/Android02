package com.cboda.intelligent.utils;

import android.widget.Toast;

import com.cboda.intelligent.app.LiXinBroadApp;


public final class ToastBroadUtil {

    private ToastBroadUtil() {

    }

    private static Toast mToast;

    public static void show(final String title) {
        showToast(title, Toast.LENGTH_SHORT);
    }

    public static void showLong(final String title) {
        showToast(title, Toast.LENGTH_LONG);
    }

    private static void showToast(final String title, final int dur) {
//        if (mToast == null) {
            mToast = Toast.makeText(LiXinBroadApp.Companion.getMContext(), title, dur);
//        }
        mToast.setText(title);
        mToast.show();
    }




    public static void cancel() {
        if (mToast != null) {
            mToast.cancel();
        }
    }
}
