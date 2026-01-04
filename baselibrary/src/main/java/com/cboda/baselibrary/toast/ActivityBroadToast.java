package com.cboda.baselibrary.toast;

import android.app.Activity;

/**
 * author : Android 轮子哥
 * github : https://github.com/getActivity/ToastBroadUtils
 * time   : 2021/11/30
 * desc   : 利用 Activity 弹出 Toast
 */
public class ActivityBroadToast extends CustomBroadToast {

    /**
     * Toast 实现类
     */
    private final ToastBroadImpl mToastImpl;

    public ActivityBroadToast(Activity activity) {
        mToastImpl = new ToastBroadImpl(activity, this);
    }

    @Override
    public void show() {
        // 替换成 WindowManager 来显示
        mToastImpl.show();
    }

    @Override
    public void cancel() {
        // 取消 WindowManager 的显示
        mToastImpl.cancel();
    }
}