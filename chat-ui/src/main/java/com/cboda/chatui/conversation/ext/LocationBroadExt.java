/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversation.ext;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.View;

import com.cboda.chatui.R;
import com.cboda.chatui.annotation.ExtContextMenuBroadItem;
import com.cboda.chatui.bridge.YeHuoCommonBroadBridge;
import com.cboda.chatui.conversation.ext.core.ConversationBroadExt;
import com.cboda.chatui.third.location.data.LocationBroadData;
import cn.wildfirechat.message.TypingMessageContent;
import cn.wildfirechat.model.Conversation;

public class LocationBroadExt extends ConversationBroadExt {

    /**
     * @param containerView 扩展view的container
     * @param conversation
     */
    @ExtContextMenuBroadItem
    public void pickLocation(View containerView, Conversation conversation) {
        String[] permissions = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        };
        String[] notGrantedPermissions = checkPermissions(permissions);
        if (notGrantedPermissions.length > 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                activity.requestPermissions(notGrantedPermissions, 100);
                return;
            }
        }

        if (conversation.type == Conversation.ConversationType.Group){
            YeHuoCommonBroadBridge.getInstance().setYeHuoQiaoJie(activity,"weizhi","",conversation.target,null);
        } else {
            YeHuoCommonBroadBridge.getInstance().setYeHuoQiaoJie(activity,"weizhi",conversation.target,"",null);

        }

        TypingMessageContent content = new TypingMessageContent(TypingMessageContent.TYPING_LOCATION);
        messageViewModel.sendMessage(conversation, content);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            LocationBroadData locationData = (LocationBroadData) data.getSerializableExtra("location");
            messageViewModel.sendLocationMessage(conversation, locationData);
        }
    }

    @Override
    public int priority() {
        return 100;
    }

    @Override
    public int iconResId() {
        return R.mipmap.ic_func_location;
    }

    @Override
    public String title(Context context) {
        return "位置";
    }

    @Override
    public String contextMenuTitle(Context context, String tag) {
        return title(context);
    }
}
