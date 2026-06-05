package com.freddy.chat.im;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.freddy.im.IMSConfig;

/**
 * 应用级前后台感知：进程内首个 Activity 可见视为前台，全部不可见视为后台。
 */
public final class ImAppLifecycleObserver implements DefaultLifecycleObserver {

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        IMSClientBootstrap.getInstance().updateAppStatus(IMSConfig.APP_STATUS_FOREGROUND);
    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        IMSClientBootstrap.getInstance().updateAppStatus(IMSConfig.APP_STATUS_BACKGROUND);
    }
}
