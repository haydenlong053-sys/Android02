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
            userInfo=new UserInfo();
        }
        if (target.contains(MMKVBroadUtils.Companion.getKeFuId())) {
            userInfo.displayName = MMKVBroadUtils.Companion.getKeFuName();
        } else if (!TextUtils.isEmpty(userInfo.friendAlias)) {
            userInfo.displayName = userInfo.friendAlias;
        } else if (!(userInfo instanceof NullUserInfo) && !TextUtils.isEmpty(userInfo.displayName)) {
        } else {
            GlobalFriendBroadBean friendSmailListBean = GlobalBroadInfo.INSTANCE.queryMyFriendInfo(target);
            if (friendSmailListBean != null) {
                userInfo.portrait = friendSmailListBean.icon;
                if (!TextUtils.isEmpty(friendSmailListBean.remarkName)) {
                    userInfo.displayName =  friendSmailListBean.remarkName;
                } else if (!TextUtils.isEmpty(friendSmailListBean.nickName)) {
                    userInfo.displayName =  friendSmailListBean.nickName;
                }
            }else {
                userInfo.displayName =  "<" + userInfo.uid + ">";
            }
        }

        return  userInfo;
    }

}
