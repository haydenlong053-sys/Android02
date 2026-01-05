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
        if (content.getConnectTime() > 0 && content.getEndTime() > 0) {
            String text;
            long duration = (content.getEndTime() - content.getConnectTime()) / 1000;
            if (duration > 3600) {
                text = String.format("通话时长 %d:%02d:%02d", duration / 3600, (duration % 3600) / 60, (duration % 60));
            } else {
                text = String.format("通话时长 %02d:%02d", duration / 60, (duration % 60));
            }
            textView.setText(text);
        } else {
            String text = "未接通";
            if (message.message.content instanceof CallStartMessageContent) {
                CallStartMessageContent startMessageContent = (CallStartMessageContent) message.message.content;
                AVEngineKit.CallEndReason reason = AVEngineKit.CallEndReason.reason(startMessageContent.getStatus());
                if (reason == AVEngineKit.CallEndReason.UnKnown) {
                    text = "未接通";
                } else if (reason == AVEngineKit.CallEndReason.Busy) {
                    text = "线路忙";
                } else if (reason == AVEngineKit.CallEndReason.SignalError) {
                    text = "网络错误";
                } else if (reason == AVEngineKit.CallEndReason.Hangup) {
                    text = "已取消";
                } else if (reason == AVEngineKit.CallEndReason.MediaError) {
                    text = "网络错误";
                } else if (reason == AVEngineKit.CallEndReason.RemoteHangup) {
                    text = "对方已取消";
                } else if (reason == AVEngineKit.CallEndReason.OpenCameraFailure) {
                    text = "网络错误";
                } else if (reason == AVEngineKit.CallEndReason.Timeout) {
                    text = "未接听";
                } else if (reason == AVEngineKit.CallEndReason.AcceptByOtherClient) {
                    text = "已在其他端接听";
                } else if (reason == AVEngineKit.CallEndReason.AllLeft) {
                    text = "通话已结束";
                } else if (reason == AVEngineKit.CallEndReason.RemoteBusy) {
                    text = "对方已取消";
                } else if (reason == AVEngineKit.CallEndReason.RemoteTimeout) {
                    text = "对方未接听";
                } else if (reason == AVEngineKit.CallEndReason.RemoteNetworkError) {
                    text = "对方网络错误";
                } else if (reason == AVEngineKit.CallEndReason.RoomDestroyed) {
                    text = "通话已结束";
                } else if (reason == AVEngineKit.CallEndReason.RoomNotExist) {
                    text = "通话已结束";
                } else if (reason == AVEngineKit.CallEndReason.RoomParticipantsFull) {
                    text = "已达到最大通话人数";
                }
            }
            textView.setText(text);
        }

        if (content.isAudioOnly()) {
            callTypeImageView.setImageResource(R.mipmap.ic_msg_cell_voice_call);
        } else {
            callTypeImageView.setImageResource(R.mipmap.ic_msg_cell_video_call);
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
