package com.cboda.intelligent.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.cboda.baselibrary.utils.DeviceBroadUtil;

/**
 * 简述： <功能描述>
 * Created by ysh on 2023/8/2.
 */
public class DeviceBroadUtils {
    @SuppressLint("HardwareIds")
    public static String getDeviceId(Context context) {
        String deviceId = "";
        try {
            deviceId = DeviceBroadUtil.INSTANCE.getAndroidId(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return deviceId;
    }

}
