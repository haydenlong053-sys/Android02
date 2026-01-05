/*
 * Copyright (c) 2022 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversation.message.viewholder;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.List;

import com.cboda.chatui.AppServiceProvider;
import com.cboda.chatui.WfcUIKit;
import com.cboda.chatui.annotation.MessageContextMenuItem;
import com.cboda.chatui.conversation.ConversationFragment;
import com.cboda.chatui.conversation.forward.ForwardActivity;
import com.cboda.chatui.conversation.message.model.UiMessage;
import com.cboda.chatui.favorite.FavoriteItem;
import com.cboda.chatui.net.SimpleCallback;
import cn.wildfirechat.message.ArticlesMessageContent;
import cn.wildfirechat.message.CompositeMessageContent;
import cn.wildfirechat.message.FileMessageContent;
import cn.wildfirechat.message.ImageMessageContent;
import cn.wildfirechat.message.Message;
import cn.wildfirechat.message.MessageContent;
import cn.wildfirechat.message.SoundMessageContent;
import cn.wildfirechat.message.TextMessageContent;
import cn.wildfirechat.message.VideoMessageContent;
import cn.wildfirechat.model.Conversation;

public abstract class ContextableNotificationMessageContentViewHolder extends NotificationMessageContentViewHolder{
    public ContextableNotificationMessageContentViewHolder(@NonNull ConversationFragment fragment, RecyclerView.Adapter adapter, View itemView) {
        super(fragment, adapter, itemView);
    }

    @MessageContextMenuItem(tag = MessageContextMenuItemTags.TAG_DELETE, confirm = false, priority = 11)
    public void removeMessage(View itemView, UiMessage message) {

        List<String> items = new ArrayList();
        items.add("删除本地消息");
        if (message.message.conversation.type == Conversation.ConversationType.Group || message.message.conversation.type == Conversation.ConversationType.Single) {
            items.add("删除远程消息");
        } else if (message.message.conversation.type == Conversation.ConversationType.SecretChat) {
            items.add("删除自己及对方消息");
        }

        new MaterialDialog.Builder(fragment.getContext())
            .items(items)
            .itemsCallback(new MaterialDialog.ListCallback() {
                @Override
                public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                    if (position == 0) {
                        messageViewModel.deleteMessage(message.message);
                    } else {
                        messageViewModel.deleteRemoteMessage(message.message);
                    }
                }
            })
            .show();
    }

    @MessageContextMenuItem(tag = MessageContextMenuItemTags.TAG_FORWARD, priority = 11)
    public void forwardMessage(View itemView, UiMessage message) {
        Intent intent = new Intent(fragment.getContext(), ForwardActivity.class);
        intent.putExtra("message", message.message);
        fragment.startActivity(intent);
    }

    @MessageContextMenuItem(tag = MessageContextMenuItemTags.TAG_MULTI_CHECK, priority = 13)
    public void checkMessage(View itemView, UiMessage message) {
        fragment.toggleMultiMessageMode(message);
    }

    @MessageContextMenuItem(tag = MessageContextMenuItemTags.TAG_FAV, confirm = false, priority = 12)
    public void fav(View itemView, UiMessage message) {
        AppServiceProvider appServiceProvider = WfcUIKit.getWfcUIKit().getAppServiceProvider();
        FavoriteItem favoriteItem = FavoriteItem.fromMessage(message.message);

        appServiceProvider.addFavoriteItem(favoriteItem, new SimpleCallback<Void>() {
            @Override
            public void onUiSuccess(Void aVoid) {
                Toast.makeText(fragment.getContext(), "fav ok", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onUiFailure(int code, String msg) {
                Toast.makeText(fragment.getContext(), "fav error: " + code, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public String contextMenuTitle(Context context, String tag) {
        String title = "未设置";
        switch (tag) {
            case MessageContextMenuItemTags.TAG_DELETE:
                title = "删除";
                break;
            case MessageContextMenuItemTags.TAG_FORWARD:
                title = "转发";
                break;
            case MessageContextMenuItemTags.TAG_MULTI_CHECK:
                title = "多选";
                break;
            case MessageContextMenuItemTags.TAG_FAV:
                title = "收藏";
                break;
            default:
                break;
        }
        return title;
    }

    @Override
    public String contextConfirmPrompt(Context context, String tag) {
        String title = "未设置";
        switch (tag) {
            case MessageContextMenuItemTags.TAG_DELETE:
                title = "确认删除此消息";
                break;
            default:
                break;
        }
        return title;
    }

    @Override
    public boolean contextMenuItemFilter(UiMessage uiMessage, String tag) {
        Message message = uiMessage.message;

        if (message.conversation.type == Conversation.ConversationType.SecretChat) {
            if (MessageContextMenuItemTags.TAG_FORWARD.equals(tag)) {
                return true;
            }
            if (MessageContextMenuItemTags.TAG_FAV.equals(tag)) {
                return true;
            }
            return false;
        }

        // 只有部分消息支持收藏
        if (MessageContextMenuItemTags.TAG_FAV.equals(tag)) {
            MessageContent messageContent = message.content;
            if (messageContent instanceof TextMessageContent
                || messageContent instanceof FileMessageContent
                || messageContent instanceof CompositeMessageContent
                || messageContent instanceof VideoMessageContent
                || messageContent instanceof SoundMessageContent
                || messageContent instanceof ArticlesMessageContent
                || messageContent instanceof ImageMessageContent) {
                return false;
            }
            return true;
        }

        return false;
    }

}
