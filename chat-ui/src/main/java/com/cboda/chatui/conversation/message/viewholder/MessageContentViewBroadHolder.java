/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversation.message.viewholder;

import static com.cboda.baselibrary.utils.GsonUtilsKt.fromJson2;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.cboda.baselibrary.bean.MsgBroadExtra;
import com.cboda.chatui.*;
import com.cboda.chatui.conversation.ConversationBroadFragment;
import com.cboda.chatui.conversation.message.model.UiBroadMessage;
import com.cboda.chatui.third.utils.TimeBroadBroadUtils;
import com.cboda.chatui.viewmodel.MessageViewBroadModel;

import cn.wildfirechat.message.Message;

public abstract class MessageContentViewBroadHolder extends RecyclerView.ViewHolder {
    @NonNull
    protected ConversationBroadFragment fragment;
    protected View itemView;
    protected UiBroadMessage message;
    protected int position;
    protected RecyclerView.Adapter adapter;
    protected MessageViewBroadModel messageViewModel;

    TextView timeTextView;


    public MessageContentViewBroadHolder(@NonNull ConversationBroadFragment fragment, RecyclerView.Adapter adapter, View itemView) {
        super(itemView);
        this.fragment = fragment;
        this.itemView = itemView;
        this.adapter = adapter;
        messageViewModel = new ViewModelProvider(fragment).get(MessageViewBroadModel.class);
        bindViews(itemView);
    }

    private void bindViews(View itemView) {
        timeTextView = itemView.findViewById(R.id.timeTextView);
    }

    public void onBind(UiBroadMessage message, int position) {
        this.message = message;
        this.position = position;

        setMessageTime(message.message, position);
    }

    /**
     * @param uiMessage 消息
     * @param tag       菜单项 tag
     * @return 返回true，将从context menu中排除
     */
    public abstract boolean contextMenuItemFilter(UiBroadMessage uiMessage, String tag);

    public abstract String contextMenuTitle(Context context, String tag);

    public abstract String contextConfirmPrompt(Context context, String tag);

    public void onViewRecycled() {
        // you can do some clean up here
    }

    protected void setMessageTime(Message item, int position) {
        try {
            if (!TextUtils.isEmpty(item.content.extra)) {
                MsgBroadExtra msgExtra = fromJson2(item.content.extra, MsgBroadExtra.class);
                if (msgExtra.isMeetingImage().equals("1")) {
                    timeTextView.setVisibility(View.GONE);
                    return;
                }
            }
        } catch (Exception e) {
        }
        long msgTime = item.serverTime;
        if (position > 0) {
//            Message preMsg = ((ConversationMessageBroadAdapter) adapter).getItem(position - 1).message;
//            long preMsgTime = preMsg.serverTime;
//            if (msgTime - preMsgTime > (5 * 60 * 1000)) {
            timeTextView.setVisibility(View.VISIBLE);
            timeTextView.setText(TimeBroadBroadUtils.getMsgFormatTime(msgTime));
//            } else {
//                timeTextView.setVisibility(View.GONE);
//            }
        } else {
            timeTextView.setVisibility(View.VISIBLE);
            timeTextView.setText(TimeBroadBroadUtils.getMsgFormatTime(msgTime));
        }
    }

}
