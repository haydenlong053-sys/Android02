/*
 * Copyright (c) 2022 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.kit.utils;

import android.text.TextUtils;

import com.cboda.baselibrary.bean.GlobalFriendBean;
import com.cboda.baselibrary.bean.GlobalInfo;
import com.cboda.baselibrary.utils.MMKVUtils;

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
        if (target.contains(MMKVUtils.Companion.getKeFuId())) {
            userInfo.displayName = MMKVUtils.Companion.getKeFuName();
        } else if (!TextUtils.isEmpty(userInfo.friendAlias)) {
            userInfo.displayName = userInfo.friendAlias;
        } else if (!(userInfo instanceof NullUserInfo) && !TextUtils.isEmpty(userInfo.displayName)) {
        } else {
            GlobalFriendBean friendSmailListBean = GlobalInfo.INSTANCE.queryMyFriendInfo(target);
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
