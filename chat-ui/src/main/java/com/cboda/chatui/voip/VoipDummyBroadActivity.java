/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.voip;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.cboda.chatui.voip.conference.ConferenceBroadActivity;
import cn.wildfirechat.avenginekit.AVEngineKit;
import cn.wildfirechat.model.Conversation;

public class VoipDummyBroadActivity extends FragmentActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AVEngineKit.CallSession session = AVEngineKit.Instance().getCurrentSession();
        if (session == null || session.getState() == AVEngineKit.CallState.Idle) {
            finish();
        } else if (session.isScreenSharing()){
            Toast.makeText(this, "屏幕共享中，不支持返回通话界面，请先停止屏幕共享", Toast.LENGTH_LONG).show();
            finish();
        }else if (session.isConference()) {
            Intent intent = new Intent(this, ConferenceBroadActivity.class);
            startActivity(intent);
            finish();
        } else {
            if (session.getConversation() == null) {
                finish();
                return;
            }
            Intent intent;
            if (session.getConversation().type == Conversation.ConversationType.Single) {
                intent = new Intent(this, SingleCallBroadActivity.class);
            } else {
                intent = new Intent(this, MultiCallBroadActivity.class);
            }
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }
    }
}
