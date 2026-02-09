/*
 * Copyright (c) 2022 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.utils;

import android.text.TextUtils;

import com.cboda.baselibrary.bean.GlobalFriendBroadBean;
import com.cboda.baselibrary.bean.GlobalBroadInfo;
import com.cboda.baselibrary.utils.MMKVBroadUtils;

import cn.wildfirechat.model.NullUserInfo;
import cn.wildfirechat.model.UserInfo;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class Util {
    public static int clamp(int value, int min, int max) {
        return Math.min(Math.max(value, min), max);
    }

    public static long clamp(long value, long min, long max) {
        return Math.min(Math.max(value, min), max);
    }

    public static float clamp(float value, float min, float max) {
        return Math.min(Math.max(value, min), max);
    }



}
