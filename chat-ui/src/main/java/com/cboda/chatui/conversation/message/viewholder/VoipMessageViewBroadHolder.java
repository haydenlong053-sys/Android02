/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversation.message.viewholder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.cboda.chatui.R;
import com.cboda.chatui.WfcUIBroadKit;
import com.cboda.chatui.annotation.EnableContextBroadMenu;
import com.cboda.chatui.annotation.MessageContentBroadType;
import com.cboda.chatui.conversation.ConversationBroadFragment;
import com.cboda.chatui.conversation.message.model.UiBroadMessage;
import cn.wildfirechat.avenginekit.AVEngineKit;
import cn.wildfirechat.message.CallStartMessageContent;
import cn.wildfirechat.model.Conversation;

import android.content.Context;

@MessageContentBroadType(CallStartMessageContent.class)
@EnableContextBroadMenu
public class VoipMessageViewBroadHolder extends NormalMessageContentViewBroadHolder {
    TextView textView;

    ImageView callTypeImageView;

    public VoipMessageViewBroadHolder(ConversationBroadFragment fragment, RecyclerView.Adapter adapter, View itemView) {
        super(fragment, adapter, itemView);
        bindViews(itemView);
        bindEvents(itemView);
    }

    private void bindEvents(View itemView) {
        itemView.findViewById(R.id.contentTextView).setOnClickListener(this::call);
    }

    private void bindViews(View itemView) {
        textView = itemView.findViewById(R.id.contentTextView);
        callTypeImageView = itemView.findViewById(R.id.callTypeImageView);
    }

    @Override
    public void onBind(UiBroadMessage message) {
        CallStartMessageContent content = (CallStartMessageContent) message.message.content;
        Context context = fragment.getContext();
        if (content.getConnectTime() > 0 && content.getEndTime() > 0) {
            String text;
            long duration = (content.getEndTime() - content.getConnectTime()) / 1000;
            if (duration > 3600) {
                text = context.getString(R.string.call_duration_with_hours, duration / 3600, (duration % 3600) / 60, (duration % 60));
            } else {
                text = context.getString(R.string.call_duration_without_hours, duration / 60, (duration % 60));
            }
            textView.setText(text);
        } else {
            String text = context.getString(R.string.call_not_connected);
            if (message.message.content instanceof CallStartMessageContent) {
                CallStartMessageContent startMessageContent = (CallStartMessageContent) message.message.content;
                AVEngineKit.CallEndReason reason = AVEngineKit.CallEndReason.reason(startMessageContent.getStatus());
                if (reason == AVEngineKit.CallEndReason.UnKnown) {
                    text = context.getString(R.string.call_not_connected);
                } else if (reason == AVEngineKit.CallEndReason.Busy) {
                    text = context.getString(R.string.call_line_busy);
                } else if (reason == AVEngineKit.CallEndReason.SignalError) {
                    text = context.getString(R.string.call_network_error);
                } else if (reason == AVEngineKit.CallEndReason.Hangup) {
                    text = context.getString(R.string.call_cancelled);
                } else if (reason == AVEngineKit.CallEndReason.MediaError) {
                    text = context.getString(R.string.call_network_error);
                } else if (reason == AVEngineKit.CallEndReason.RemoteHangup) {
                    text = context.getString(R.string.call_remote_cancelled);
                } else if (reason == AVEngineKit.CallEndReason.OpenCameraFailure) {
                    text = context.getString(R.string.call_network_error);
                } else if (reason == AVEngineKit.CallEndReason.Timeout) {
                    text = context.getString(R.string.call_not_answered);
                } else if (reason == AVEngineKit.CallEndReason.AcceptByOtherClient) {
                    text = context.getString(R.string.call_accepted_by_other_client);
                } else if (reason == AVEngineKit.CallEndReason.AllLeft) {
                    text = context.getString(R.string.call_ended);
                } else if (reason == AVEngineKit.CallEndReason.RemoteBusy) {
                    text = context.getString(R.string.call_remote_cancelled);
                } else if (reason == AVEngineKit.CallEndReason.RemoteTimeout) {
                    text = context.getString(R.string.call_remote_not_answered);
                } else if (reason == AVEngineKit.CallEndReason.RemoteNetworkError) {
                    text = context.getString(R.string.call_remote_network_error);
                } else if (reason == AVEngineKit.CallEndReason.RoomDestroyed) {
                    text = context.getString(R.string.call_ended);
                } else if (reason == AVEngineKit.CallEndReason.RoomNotExist) {
                    text = context.getString(R.string.call_ended);
                } else if (reason == AVEngineKit.CallEndReason.RoomParticipantsFull) {
                    text = context.getString(R.string.call_max_participants_reached);
                }
            }
            textView.setText(text);
        }

        if (content.isAudioOnly()) {
            callTypeImageView.setImageResource(R.mipmap.ic_msg_cell_voice_broad_call);
        } else {
            callTypeImageView.setImageResource(R.mipmap.ic_msg_cell_video_broad_call);
        }
    }

    public void call(View view) {
        if (((CallStartMessageContent) message.message.content).getStatus() == 1) {
            return;
        }
        CallStartMessageContent callStartMessageContent = (CallStartMessageContent) message.message.content;
        if (message.message.conversation.type == Conversation.ConversationType.Single) {
            WfcUIBroadKit.singleCall(fragment.getContext(), message.message.conversation.target, callStartMessageContent.isAudioOnly());
        } else {
            fragment.pickGroupMemberToVoipChat(callStartMessageContent.isAudioOnly());
        }
    }
}
