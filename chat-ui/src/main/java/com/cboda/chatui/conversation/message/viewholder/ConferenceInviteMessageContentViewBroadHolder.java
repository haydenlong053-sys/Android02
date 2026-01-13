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

import com.cboda.chatui.R;
import com.cboda.chatui.WfcBaseBroadActivity;
import com.cboda.chatui.annotation.MessageContentBroadType;
import com.cboda.chatui.conversation.ConversationBroadFragment;
import com.cboda.chatui.conversation.message.model.UiBroadMessage;
import com.cboda.chatui.voip.conference.ConferenceInfoBroadActivity;
import cn.wildfirechat.avenginekit.AVEngineKit;
import cn.wildfirechat.message.ConferenceInviteMessageContent;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.remote.ChatManager;

@MessageContentBroadType(value = {
    ConferenceInviteMessageContent.class,
})
public class ConferenceInviteMessageContentViewBroadHolder extends NormalMessageContentViewBroadHolder {
    ImageView hostPortraitImageView;
    TextView titleTextView;
    TextView descTextView;

    private ConferenceInviteMessageContent inviteMessageContent;

    public ConferenceInviteMessageContentViewBroadHolder(ConversationBroadFragment fragment, RecyclerView.Adapter adapter, View itemView) {
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
    protected void onBind(UiBroadMessage message) {
        inviteMessageContent = (ConferenceInviteMessageContent) message.message.content;
        titleTextView.setText(inviteMessageContent.getTitle());
        descTextView.setText(inviteMessageContent.getDesc());
        UserInfo userInfo = ChatManager.Instance().getUserInfo(inviteMessageContent.getHost(), false);
        Glide
            .with(fragment)
            .load(userInfo.portrait)
            .transforms(new CenterCrop(), new RoundedCorners(10))
            .placeholder(R.mipmap.avatar_broad_def)
            .into(hostPortraitImageView);
    }

    void joinConference() {
        if (!AVEngineKit.isSupportConference()) {
            Toast.makeText(fragment.getActivity(), "本版本不支持会议功能", Toast.LENGTH_SHORT).show();
            return;
        }
        String[] permissions = new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!((WfcBaseBroadActivity) fragment.getActivity()).checkPermission(permissions)) {
                fragment.getActivity().requestPermissions(permissions, 100);
                return;
            }
        }
//        AVEngineKit.Instance().joinConference(inviteMessageContent.getCallId(), inviteMessageContent.isAudioOnly(), inviteMessageContent.getPin(), inviteMessageContent.getHost(), inviteMessageContent.getTitle(), inviteMessageContent.getDesc(), inviteMessageContent.isAudience(), inviteMessageContent.isAdvanced(), false, false, null);
        Intent intent = new Intent(fragment.getActivity(), ConferenceInfoBroadActivity.class);
        intent.putExtra("conferenceId", inviteMessageContent.getCallId());
        intent.putExtra("password", inviteMessageContent.getPin());
        fragment.startActivity(intent);
    }
}
