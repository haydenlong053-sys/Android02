package com.lxkj.aiim.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

/**
 * 简述： <功能描述>
 * Created by ysh on 2023/8/2.
 */
public class DeviceUtils {
    @SuppressLint("HardwareIds")
    public static String getDeviceId(Context context) {
        String deviceId = "";
        try {
            if (Build.VERSION.SDK_INT < 29) {
                TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                if (tm != null) {
                    if (!TextUtils.isEmpty(tm.getDeviceId()))
                        deviceId = tm.getDeviceId();
                    else
                        deviceId = Settings.Secure.getString(context.getApplicationContext().getContentResolver(),
                                Settings.Secure.ANDROID_ID);
                } else
                    deviceId = Settings.Secure.getString(context.getApplicationContext().getContentResolver(),
                            Settings.Secure.ANDROID_ID);
            } else
                deviceId = Settings.Secure.getString(context.getApplicationContext().getContentResolver(),
                        Settings.Secure.ANDROID_ID);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return deviceId;
    }

}
