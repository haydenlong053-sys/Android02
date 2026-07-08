package com.freddy.chat.viewholder;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.freddy.chat.im.MessageReadUiState;
import com.freddy.chat.menu.MessageListActions;
import com.freddy.chat.menu.MessageListMode;
import com.freddy.chat.utils.WeChatUiHelper;
import com.freddy.im.bean.AppMessage;
import com.freddy.im.http.model.im.GroupMemberItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MessageViewHolderContext {
    public final boolean isGroupChat;
    @Nullable
    public final String peerAvatarUrl;
    @Nullable
    public final String selfAvatarUrl;
    @Nullable
    public final String currentUserId;
    @NonNull
    public final Map<String, GroupMemberItem> membersByUserId;
    @NonNull
    public final List<AppMessage> items;
    @NonNull
    public final MessageReadUiState readUiState;
    public final int groupMemberCount;
    @Nullable
    public final String sessionId;
    @Nullable
    public final String peerUserId;
    @Nullable
    public final String peerDisplayName;
    @Nullable
    public final OnMessageRetryListener retryListener;
    @Nullable
    public final MessageListActions messageActions;
    @Nullable
    public final OnMentionMemberListener mentionMemberListener;
    @Nullable
    public final OnPortraitClickListener portraitClickListener;
    public final int listMode;

    public interface OnMentionMemberListener {
        void onMentionMember(@NonNull String userId, @NonNull String displayName);
    }

    /** 消息头像点击（对齐野火 {@code OnPortraitClickListener}）。 */
    public interface OnPortraitClickListener {
        void onPortraitClick(@NonNull String userId);
    }

    public MessageViewHolderContext(boolean isGroupChat,
                                    @Nullable String peerAvatarUrl,
                                    @Nullable String selfAvatarUrl,
                                    @Nullable String currentUserId,
                                    @NonNull Map<String, GroupMemberItem> membersByUserId,
                                    @NonNull List<AppMessage> items,
                                    @NonNull MessageReadUiState readUiState,
                                    int groupMemberCount,
                                    @Nullable String sessionId,
                                    @Nullable String peerUserId,
                                    @Nullable String peerDisplayName,
                                    @Nullable OnMessageRetryListener retryListener,
                                    @Nullable MessageListActions messageActions,
                                    @Nullable OnMentionMemberListener mentionMemberListener,
                                    @Nullable OnPortraitClickListener portraitClickListener,
                                    int listMode) {
        this.isGroupChat = isGroupChat;
        this.peerAvatarUrl = peerAvatarUrl;
        this.selfAvatarUrl = selfAvatarUrl;
        this.currentUserId = currentUserId;
        this.membersByUserId = membersByUserId;
        this.items = items;
        this.readUiState = readUiState;
        this.groupMemberCount = groupMemberCount;
        this.sessionId = sessionId;
        this.peerUserId = peerUserId;
        this.peerDisplayName = peerDisplayName;
        this.retryListener = retryListener;
        this.messageActions = messageActions;
        this.mentionMemberListener = mentionMemberListener;
        this.portraitClickListener = portraitClickListener;
        this.listMode = listMode;
    }

    public boolean isCheckableMode() {
        return listMode == MessageListMode.CHECKABLE;
    }

    public boolean isMessageChecked(@NonNull AppMessage message) {
        return messageActions != null && messageActions.isMessageChecked(message);
    }

    @Nullable
    public AppMessage getPrevMessage(int position) {
        if (position <= 0 || position > items.size() - 1) {
            return null;
        }
        return items.get(position - 1);
    }

    @Nullable
    public GroupMemberItem resolveMember(@Nullable String userId) {
        if (TextUtils.isEmpty(userId)) {
            return null;
        }
        return membersByUserId.get(userId);
    }

    @NonNull
    public Map<String, String> buildUserDisplayNameMap() {
        Map<String, String> map = new HashMap<>(membersByUserId.size() + 1);
        for (Map.Entry<String, GroupMemberItem> e : membersByUserId.entrySet()) {
            if (e.getValue() != null && !TextUtils.isEmpty(e.getValue().getNickname())) {
                map.put(e.getKey(), e.getValue().getNickname());
            }
        }
        if (!isGroupChat
                && !TextUtils.isEmpty(peerUserId)
                && !TextUtils.isEmpty(peerDisplayName)) {
            map.putIfAbsent(peerUserId, peerDisplayName);
        }
        return map;
    }

    public static boolean shouldShowGroupNickname(@Nullable AppMessage prev,
                                                  @Nullable String senderId,
                                                  @Nullable String currentUserId) {
        if (TextUtils.isEmpty(senderId)) {
            return false;
        }
        if (prev == null || WeChatUiHelper.isSystemMessage(prev)) {
            return true;
        }
        if (prev.isSender(currentUserId)) {
            return true;
        }
        return !senderId.equals(prev.getFromId());
    }
}
