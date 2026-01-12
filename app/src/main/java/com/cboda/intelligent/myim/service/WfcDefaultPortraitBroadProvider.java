/*
 * Copyright (c) 2023 WildFireChat. All rights reserved.
 */

package com.cboda.intelligent.myim.service;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.cboda.intelligent.service.AppBroadService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import cn.wildfirechat.model.GroupInfo;
import cn.wildfirechat.model.NullGroupInfo;
import cn.wildfirechat.model.NullUserInfo;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.remote.DefaultPortraitProvider;

public class WfcDefaultPortraitBroadProvider implements DefaultPortraitProvider {
    @Override
    public String userDefaultPortrait(UserInfo userInfo) {
        if (!TextUtils.isEmpty(userInfo.portrait)) {
            return userInfo.portrait;
        } else {
            return AppBroadService.APP_SERVER_ADDRESS + "/avatar?name=" + Uri.encode(userInfo.displayName);
        }
    }

    @Override
    public String groupDefaultPortrait(GroupInfo groupInfo, List<UserInfo> userInfos) {
        Log.e("群资料",new Gson().toJson(groupInfo));
        if (groupInfo instanceof NullGroupInfo || !TextUtils.isEmpty(groupInfo.portrait)) {
            return groupInfo.portrait;
        }
        boolean pending = false;
        JSONObject request = new JSONObject();
        try {
            JSONArray reqMembers = new JSONArray();
            for (UserInfo userInfo : userInfos) {
                if (userInfo instanceof NullUserInfo) {
                    pending = true;
                }
                JSONObject obj = new JSONObject();
                if (TextUtils.isEmpty(userInfo.portrait) || userInfo.portrait.startsWith(AppBroadService.APP_SERVER_ADDRESS)) {
//                    obj.put("name", userInfo.displayName);
                } else {
                    if (!userInfo.portrait.contains("244")){

                        obj.put("avatarUrl", userInfo.portrait);
                    }
                    reqMembers.put(obj);
                }

            }
            request.put("members", reqMembers);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (pending) {
            return null;
        }
        return AppBroadService.APP_SERVER_ADDRESS + "/avatar/group?request=" + Uri.encode(request.toString());
//        return request.toString();
    }
}
