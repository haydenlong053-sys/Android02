/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversation.message.viewholder;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.cboda.chatui.*;
import com.cboda.chatui.annotation.MessageContentBroadType;
import com.cboda.chatui.conversation.ConversationBroadFragment;
import com.cboda.chatui.conversation.message.model.UiBroadMessage;
import cn.wildfirechat.message.AddParticipantsMessageContent;
import cn.wildfirechat.message.notification.AddGroupMemberNotificationContent;
import cn.wildfirechat.message.notification.ChangeGroupNameNotificationContent;
import cn.wildfirechat.message.notification.ChangeGroupPortraitNotificationContent;
import cn.wildfirechat.message.notification.CreateGroupNotificationContent;
import cn.wildfirechat.message.notification.DismissGroupNotificationContent;
import cn.wildfirechat.message.notification.FriendAddedMessageContent;
import cn.wildfirechat.message.notification.FriendGreetingMessageContent;
import cn.wildfirechat.message.notification.GroupAllowMemberNotificationContent;
import cn.wildfirechat.message.notification.GroupJoinTypeNotificationContent;
import cn.wildfirechat.message.notification.GroupMuteMemberNotificationContent;
import cn.wildfirechat.message.notification.GroupMuteNotificationContent;
import cn.wildfirechat.message.notification.GroupPrivateChatNotificationContent;
import cn.wildfirechat.message.notification.GroupSetManagerNotificationContent;
import cn.wildfirechat.message.notification.KickoffGroupMemberNotificationContent;
import cn.wildfirechat.message.notification.KickoffGroupMemberVisibleNotificationContent;
import cn.wildfirechat.message.notification.ModifyGroupAliasNotificationContent;
import cn.wildfirechat.message.notification.ModifyGroupExtraNotificationContent;
import cn.wildfirechat.message.notification.ModifyGroupMemberExtraNotificationContent;
import cn.wildfirechat.message.notification.NotificationMessageContent;
import cn.wildfirechat.message.notification.QuitGroupNotificationContent;
import cn.wildfirechat.message.notification.QuitGroupVisibleNotificationContent;
import cn.wildfirechat.message.notification.StartSecretChatMessageContent;
import cn.wildfirechat.message.notification.TipNotificationContent;
import cn.wildfirechat.message.notification.TransferGroupOwnerNotificationContent;

@MessageContentBroadType(value = {
    AddGroupMemberNotificationContent.class,
    ChangeGroupNameNotificationContent.class,
    ChangeGroupPortraitNotificationContent.class,
    CreateGroupNotificationContent.class,
    DismissGroupNotificationContent.class,
    DismissGroupNotificationContent.class,
    KickoffGroupMemberNotificationContent.class,
    ModifyGroupAliasNotificationContent.class,
    ModifyGroupExtraNotificationContent.class,
    ModifyGroupMemberExtraNotificationContent.class,
    QuitGroupNotificationContent.class,
    QuitGroupVisibleNotificationContent.class,
    KickoffGroupMemberVisibleNotificationContent.class,
    TransferGroupOwnerNotificationContent.class,
    TipNotificationContent.class,
    FriendAddedMessageContent.class,
    FriendGreetingMessageContent.class,
    GroupMuteNotificationContent.class,
    GroupPrivateChatNotificationContent.class,
    GroupJoinTypeNotificationContent.class,
    GroupSetManagerNotificationContent.class,
    GroupMuteMemberNotificationContent.class,
    GroupAllowMemberNotificationContent.class,
    AddParticipantsMessageContent.class,
    StartSecretChatMessageContent.class
    // TODO add more

})
/**
 * 小灰条消息, 居中显示，且不显示发送者，用于简单通知，如果需要扩展成复杂通知，可以参考 {@link ExampleRichNotificationMessageContentViewBroadHolder}
 *
 */
public class SimpleNotificationMessageContentViewBroadHolder extends NotificationMessageContentViewBroadHolder {

    TextView notificationTextView;

    public SimpleNotificationMessageContentViewBroadHolder(ConversationBroadFragment fragment, RecyclerView.Adapter adapter, View itemView) {
        super(fragment, adapter, itemView);
        bindViews(itemView);
    }

    private void bindViews(View itemView) {
        notificationTextView =itemView.findViewById(R.id.notificationTextView);
    }

    @Override
    public void onBind(UiBroadMessage message, int position) {
        super.onBind(message, position);
        onBind(message);
    }

    @Override
    public boolean contextMenuItemFilter(UiBroadMessage uiMessage, String tag) {
        return true;
    }

    protected void onBind(UiBroadMessage message) {
        String notification;
        try {
            notification = ((NotificationMessageContent) message.message.content).formatNotification(message.message);
        } catch (Exception e) {
            e.printStackTrace();
            notification = "message is invalid";
        }
        notificationTextView.setText(notification);
    }

}
