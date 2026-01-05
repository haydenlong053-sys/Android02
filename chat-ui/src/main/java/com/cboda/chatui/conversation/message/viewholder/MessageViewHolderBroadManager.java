/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversation.message.viewholder;

import android.util.Log;
import android.util.SparseArray;

import com.cboda.chatui.R;
import com.cboda.chatui.annotation.MessageContentBroadType;
import cn.wildfirechat.message.MessageContent;
import cn.wildfirechat.message.core.ContentTag;

public class MessageViewHolderBroadManager {
    private static final String TAG = "MsgViewHolderManager";
    private static MessageViewHolderBroadManager instance = new MessageViewHolderBroadManager();

    private MessageViewHolderBroadManager() {
        init();
    }

    public static MessageViewHolderBroadManager getInstance() {
        return instance;
    }

    private void init() {
        registerMessageViewHolder(AudioMessageContentViewBroadHolder.class, R.layout.conversation_item_audio_send, R.layout.conversation_item_audio_receive);
        registerMessageViewHolder(FileMessageContentViewBroadHolder.class, R.layout.conversation_item_file_send, R.layout.conversation_item_file_receive);
        registerMessageViewHolder(ImageMessageContentViewBroadHolder.class, R.layout.conversation_item_image_send, R.layout.conversation_item_image_receive);
        registerMessageViewHolder(StickerMessageContentViewBroadHolder.class, R.layout.conversation_item_sticker_send, R.layout.conversation_item_sticker_receive);
        registerMessageViewHolder(TextMessageContentViewBroadHolder.class, R.layout.conversation_item_text_send, R.layout.conversation_item_text_receive);
        registerMessageViewHolder(VideoMessageContentViewBroadHolder.class, R.layout.conversation_item_video_send, R.layout.conversation_item_video_send);
        registerMessageViewHolder(VoipMessageViewBroadHolder.class, R.layout.conversation_item_voip_send, R.layout.conversation_item_voip_receive);
        registerMessageViewHolder(SimpleNotificationMessageContentViewBroadHolder.class, R.layout.conversation_item_notification, R.layout.conversation_item_notification);
        registerMessageViewHolder(RichNotificationMessageContentViewBroadHolder.class, R.layout.conversation_item_rich_notification, R.layout.conversation_item_rich_notification);
        registerMessageViewHolder(RecallMessageContentViewBroadHolder.class, R.layout.conversation_item_recall_notification, R.layout.conversation_item_recall_notification);
        registerMessageViewHolder(UserCardMessageContentViewBroadHolder.class, R.layout.conversation_item_user_card_send, R.layout.conversation_item_user_card_receive);
        registerMessageViewHolder(ConferenceInviteMessageContentViewBroadHolder.class, R.layout.conversation_item_conference_invite_send, R.layout.conversation_item_conference_invite_receive);
        //注册声网消息体的holder
        registerMessageViewHolder(MeetingInviteMessageContentViewBroadHolder.class, R.layout.conversation_item_meeting_invite_send, R.layout.conversation_item_meeting_invite_receive);
        //注册聊天公告消息的holder
        registerMessageViewHolder(NoticeContentViewBroadHolder.class, R.layout.conversation_item_msg_notice_send, R.layout.conversation_item_msg_notice_receive);
        registerMessageViewHolder(CompositeMessageContentViewBroadHolder.class, R.layout.conversation_item_composite_send, R.layout.conversation_item_composite_receive);
        registerMessageViewHolder(LinkMessageContentViewBroadHolder.class, R.layout.conversation_item_link_send, R.layout.conversation_item_link_receive);
        registerMessageViewHolder(ArticlesMessageContentViewBroadHolder.class, R.layout.conversation_item_articles, R.layout.conversation_item_articles);

    }

    private SparseArray<Class<? extends MessageContentViewBroadHolder>> messageViewHolders = new SparseArray<>();
    private SparseArray<Integer> messageSendLayoutRes = new SparseArray<>();
    private SparseArray<Integer> messageReceiveLayoutRes = new SparseArray<>();

    public void registerMessageViewHolder(Class<? extends MessageContentViewBroadHolder> clazz, int sendLayoutRes, int receiveLayoutRes) {
        MessageContentBroadType contentType = clazz.getAnnotation(MessageContentBroadType.class);
        if (contentType == null) {
            throw new IllegalArgumentException("the message content viewHolder must be annotated with MessageContentBroadType " + clazz.getSimpleName());
        }

        if (sendLayoutRes == 0 && receiveLayoutRes == 0) {
            throw new IllegalArgumentException("must set message content viewHolder layout ");
        }

        Class<? extends MessageContent> clazzes[] = contentType.value();
        for (Class<? extends MessageContent> notificationClazz : clazzes) {
            ContentTag contentTag = notificationClazz.getAnnotation(ContentTag.class);
            if (messageViewHolders.get(contentTag.type()) == null) {
                messageViewHolders.put(contentTag.type(), clazz);
                messageSendLayoutRes.put(contentTag.type(), sendLayoutRes);
                messageReceiveLayoutRes.put(contentTag.type(), receiveLayoutRes);
            } else {
                Log.e(MessageViewHolderBroadManager.class.getSimpleName(), "re-register message view holder " + clazz.getSimpleName());
            }
        }
    }

    public @androidx.annotation.LayoutRes
    int sendLayoutResId(int messageType) {
        Integer sendLayoutResId = messageSendLayoutRes.get(messageType);
        return sendLayoutResId == null ? R.layout.conversation_item_unknown_send : sendLayoutResId;
    }

    public @androidx.annotation.LayoutRes
    int receiveLayoutResId(int messageType) {
        Integer receiveLayoutResId = messageReceiveLayoutRes.get(messageType);
        return receiveLayoutResId == null ? R.layout.conversation_item_unknown_receive : receiveLayoutResId;
    }

    public Class<? extends MessageContentViewBroadHolder> getMessageContentViewHolder(int messageType) {
        Class clazz = messageViewHolders.get(messageType);
        if (clazz == null) {
            Log.d(TAG, "not register messageContentViewHolder for messageType " + messageType + ", fall-back to UnknownMessageContentViewHolder");
            return UnkownMessageContentViewBroadHolder.class;
        }
        return clazz;
    }
}
