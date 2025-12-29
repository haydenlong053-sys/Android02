package com.cboda.baselibrary.utils;

import android.os.SystemClock;
import android.view.View;

public abstract class OnSingleClickListener implements View.OnClickListener {
    private static final long MIN_CLICK_INTERVAL = 800;
    private long lastClickTime = 0;

    @Override
    public void onClick(View v) {
        long currentTime = SystemClock.uptimeMillis();
        if (currentTime - lastClickTime > MIN_CLICK_INTERVAL) {
            lastClickTime = currentTime;
            onSingleClick(v);
        }
    }
    public abstract void onSingleClick(View v);
}
