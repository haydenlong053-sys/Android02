/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversationlist.notification.viewholder;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.cboda.chatui.R;
import com.cboda.chatui.annotation.StatusNotificationType;
import com.cboda.chatui.conversationlist.notification.PCOnlineStatusNotification;
import com.cboda.chatui.conversationlist.notification.StatusNotification;
import com.cboda.chatui.pc.PCSessionActivity;
import cn.wildfirechat.model.PCOnlineInfo;
import cn.wildfirechat.remote.ChatManager;

@StatusNotificationType(PCOnlineStatusNotification.class)
public class PCOnlineNotificationViewHolder extends StatusNotificationViewHolder {
    TextView statusTextView;
    PCOnlineInfo pcOnlineInfo;

    public PCOnlineNotificationViewHolder(Fragment fragment) {
        super(fragment);
    }

    @Override
    public void onBind(View view, StatusNotification notification) {
        PCOnlineStatusNotification pcOnlineStatusNotification = (PCOnlineStatusNotification) notification;
        pcOnlineInfo = pcOnlineStatusNotification.getPcOnlineInfo();
        String desc = "";
        switch (pcOnlineStatusNotification.getPcOnlineInfo().getType()) {
            case PC_Online:
                desc = "PC 在线";
                break;
            case Web_Online:
                desc = "Web 在线";
                break;
            case WX_Online:
                desc = "微信小程序 在线";
                break;
            case Pad_Online:
                desc = "Pad 在线";
                break;
            default:
                break;
        }
        if (ChatManager.Instance().isMuteNotificationWhenPcOnline()) {
            desc += "，手机通知已关闭";
        }

        statusTextView = view.findViewById(R.id.statusTextView);
        statusTextView.setText(desc);
        statusTextView.setOnClickListener(v -> showPCSessionInfo());
    }

    public void showPCSessionInfo() {
        Intent intent = new Intent(fragment.getActivity(), PCSessionActivity.class);
        intent.putExtra("pcOnlineInfo", pcOnlineInfo);
        fragment.startActivity(intent);
    }
}
