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

    public static UserInfo getMyUserDisplayName(String target, UserInfo userInfo) {
        if (userInfo == null) {
            userInfo = new UserInfo();
        }
        UserInfo finalUserInfo = userInfo;
        if (target.contains(MMKVBroadUtils.Companion.getKeFuId())) {
            finalUserInfo.displayName = MMKVBroadUtils.Companion.getKeFuName();
        } else if (!TextUtils.isEmpty(finalUserInfo.friendAlias)) {
            finalUserInfo.displayName = finalUserInfo.friendAlias;
        } else if (!(finalUserInfo instanceof NullUserInfo) && !TextUtils.isEmpty(finalUserInfo.displayName)) {
        } else {
            GlobalBroadInfo.INSTANCE.queryMyFriendInfo(target, null, friendSmailListBean -> {
                if (friendSmailListBean != null) {
                    finalUserInfo.portrait = friendSmailListBean.icon;
                    if (!TextUtils.isEmpty(friendSmailListBean.remarkName)) {
                        finalUserInfo.displayName = friendSmailListBean.remarkName;
                    } else if (!TextUtils.isEmpty(friendSmailListBean.nickName)) {
                        finalUserInfo.displayName = friendSmailListBean.nickName;
                    }
                } else {
                    finalUserInfo.displayName = "<" + finalUserInfo.uid + ">";
                }
                return null;
            });

        }
        return finalUserInfo;
    }

}
