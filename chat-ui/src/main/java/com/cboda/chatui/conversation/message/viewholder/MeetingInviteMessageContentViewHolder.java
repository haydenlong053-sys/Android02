/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversation.message.viewholder;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;

import org.json.JSONObject;

import com.cboda.chatui.R;
import com.cboda.chatui.WfcBaseActivity;
import com.cboda.chatui.annotation.MessageContentType;
import com.cboda.chatui.bridge.YeHuoCommonBridge;
import com.cboda.chatui.conversation.ConversationFragment;
import com.cboda.chatui.conversation.message.model.UiMessage;
import com.cboda.chatui.voip.conference.ConferenceInfoActivity;
import cn.wildfirechat.avenginekit.AVEngineKit;
import cn.wildfirechat.message.MeetingInviteMessageContent;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.remote.ChatManager;

@MessageContentType(value = {
    MeetingInviteMessageContent.class,
})
public class MeetingInviteMessageContentViewHolder extends NormalMessageContentViewHolder {
    ImageView hostPortraitImageView;
    TextView titleTextView;
    TextView descTextView;

    private MeetingInviteMessageContent inviteMessageContent;

    public MeetingInviteMessageContentViewHolder(ConversationFragment fragment, RecyclerView.Adapter adapter, View itemView) {
        super(fragment, adapter, itemView);
        bindViews(itemView);
        bindEvents(itemView);
    }

    private void bindEvents(View itemView) {
       itemView.findViewById(R.id.contentLayout).setOnClickListener(_v -> joinConference());
    }

    private void bindViews(View itemView) {
        hostPortraitImageView =itemView.findViewById(R.id.hostPortraitImageView);
        titleTextView =itemView.findViewById(R.id.titleTextView);
        descTextView =itemView.findViewById(R.id.descTextView);
    }

    @Override
    protected void onBind(UiMessage message) {
        inviteMessageContent = (MeetingInviteMessageContent) message.message.content;
        titleTextView.setText(inviteMessageContent.getTitle());
        descTextView.setText(inviteMessageContent.getDesc());
        UserInfo userInfo = ChatManager.Instance().getUserInfo(inviteMessageContent.getIdno(), false);
        Glide
            .with(fragment)
            .load(userInfo.portrait)
            .transforms(new CenterCrop(), new RoundedCorners(10))
            .placeholder(R.mipmap.avatar_def)
            .into(hostPortraitImageView);
    }

    void joinConference() {

        String[] permissions = new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!((WfcBaseActivity) fragment.getActivity()).checkPermission(permissions)) {
                fragment.getActivity().requestPermissions(permissions, 100);
                return;
            }
        }
        JSONObject ojb =new JSONObject();
        try {
            ojb.put("ownerId",inviteMessageContent.getIdno());
            ojb.put("title",inviteMessageContent.getTitle());
            String jsonParams = ojb.toString();
            YeHuoCommonBridge.getInstance().setYeHuoQiaoJie(fragment.getActivity(),"meeting",inviteMessageContent.getMeetingId(),jsonParams,null);
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
