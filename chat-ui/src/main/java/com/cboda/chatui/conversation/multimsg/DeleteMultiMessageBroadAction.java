/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversation.multimsg;

import android.content.Context;
import android.view.View;

import androidx.lifecycle.ViewModelProvider;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.List;

import com.cboda.chatui.R;
import com.cboda.chatui.conversation.message.model.UiBroadMessage;
import com.cboda.chatui.viewmodel.MessageViewBroadModel;

public class DeleteMultiMessageBroadAction extends MultiMessageBroadAction {

    @Override
    public void onClick(List<UiBroadMessage> messages) {
        MessageViewBroadModel messageViewModel = new ViewModelProvider(fragment).get(MessageViewBroadModel.class);
        new MaterialDialog.Builder(fragment.getContext())
            .items("删除本地消息", "删除远程消息")
            .itemsCallback(new MaterialDialog.ListCallback() {
                @Override
                public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                    if (position == 0) {
                        for (UiBroadMessage message : messages) {
                            messageViewModel.deleteMessage(message.message);
                        }
                    } else {
                        for (UiBroadMessage message : messages) {
                            messageViewModel.deleteRemoteMessage(message.message);
                        }
                    }
                }
            })
            .show();
    }

    @Override
    public int iconResId() {
        return R.mipmap.ic_broad_delete;
    }

    @Override
    public String title(Context context) {
        return "删除";
    }

    @Override
    public boolean confirm() {
        return true;
    }

    @Override
    public String confirmPrompt() {
        return "确认删除?";
    }
}
