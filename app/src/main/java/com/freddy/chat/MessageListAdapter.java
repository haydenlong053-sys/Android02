package com.freddy.chat;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.freddy.chat.event.MessageReadStateEvent;
import com.freddy.chat.media.VoiceMessagePlayer;
import com.freddy.chat.im.IMSClientBootstrap;
import com.freddy.chat.im.MessageReadUiState;
import com.freddy.chat.menu.MessageListActions;
import com.freddy.chat.menu.MessageListMode;
import com.freddy.chat.utils.WeChatUiHelper;
import com.freddy.im.db.mapper.MessageRealmMapper;
import com.freddy.chat.viewholder.BaseMessageVH;
import com.freddy.chat.viewholder.CallMessageVH;
import com.freddy.chat.viewholder.CardMessageVH;
import com.freddy.chat.viewholder.FileMessageVH;
import com.freddy.chat.viewholder.HongBaoMessageVH;
//import com.freddy.chat.viewholder.HongbaoClaimMessageVH;
import com.freddy.chat.viewholder.ImageMessageVH;
import com.freddy.chat.viewholder.MapMessageVH;
import com.freddy.chat.viewholder.MeetStatusMessageVH;
import com.freddy.chat.viewholder.MergeTransMessageVH;
import com.freddy.chat.viewholder.MessageViewHolderContext;
import com.freddy.chat.viewholder.NoticeMessageVH;
import com.freddy.chat.viewholder.OnMessageRetryListener;
import com.freddy.chat.viewholder.RecallMessageVH;
import com.freddy.chat.viewholder.SystemMessageVH;
import com.freddy.chat.viewholder.TextMessageVH;
import com.freddy.chat.viewholder.VideoMessageVH;
import com.freddy.chat.viewholder.VoiceMessageVH;
import com.freddy.im.bean.AppMessage;
import com.freddy.im.http.model.im.GroupMemberItem;
import com.freddy.im.message.ImMessageManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.cboda.intelligent.R;

public final class MessageListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SYSTEM = 0;
    private static final int VIEW_TYPE_TEXT = 1;
    private static final int VIEW_TYPE_IMAGE = 2;
    private static final int VIEW_TYPE_VOICE = 3;
    private static final int VIEW_TYPE_VIDEO = 4;
    private static final int VIEW_TYPE_FILE = 5;
    private static final int VIEW_TYPE_MAP = 6;
    private static final int VIEW_TYPE_MERGE_TRANS = 7;
    private static final int VIEW_TYPE_CARD = 8;
    private static final int VIEW_TYPE_NOTICE = 9;
    private static final int VIEW_TYPE_MEET_START = 10;
    private static final int VIEW_TYPE_MEET_END = 11;
    private static final int VIEW_TYPE_CALL = 12;
    private static final int VIEW_TYPE_RECALL = 13;
    private static final int VIEW_TYPE_HONGBAO = 14;
//    private static final int VIEW_TYPE_HONGBAO_CLAIM = 15;

    private static final int VIEW_TYPE_INVITE_JOIN_GROUP = 16;
    private static final int VIEW_TYPE_UNKNOWN = 99;

    private final boolean isGroupChat;
    @Nullable
    private final String peerAvatarUrl;
    @Nullable
    private final String selfAvatarUrl;
    @Nullable
    private final String sessionId;
    @Nullable
    private final String peerUserId;
    @Nullable
    private final String peerDisplayName;
    @Nullable
    private final OnMessageRetryListener retryListener;
    @Nullable
    private MessageListActions messageActions;
    @Nullable
    private MessageViewHolderContext.OnMentionMemberListener mentionMemberListener;
    @Nullable
    private MessageViewHolderContext.OnPortraitClickListener portraitClickListener;
    @Nullable
    private MessageViewHolderContext.OnQuoteClickListener quoteClickListener;

    private final Map<String, GroupMemberItem> membersByUserId = new HashMap<>();
    private final Set<String> checkedClientMsgIds = new HashSet<>();

    private final List<AppMessage> items = new ArrayList<>();
    private int listMode = MessageListMode.NORMAL;
    @NonNull
    private final MessageReadUiState readUiState = new MessageReadUiState();
    @NonNull
    private final VoiceMessagePlayer voiceMessagePlayer = VoiceMessagePlayer.getInstance();
    @Nullable
    private String lastPlayingClientMsgId;

    public MessageListAdapter(boolean isGroupChat,
                              @Nullable String peerAvatarUrl,
                              @Nullable String selfAvatarUrl,
                              @Nullable String sessionId,
                              @Nullable String peerUserId,
                              @Nullable String peerDisplayName,
                              @Nullable OnMessageRetryListener retryListener) {
        this.isGroupChat = isGroupChat;
        this.peerAvatarUrl = peerAvatarUrl;
        this.selfAvatarUrl = selfAvatarUrl;
        this.sessionId = sessionId;
        this.peerUserId = peerUserId;
        this.peerDisplayName = peerDisplayName;
        this.retryListener = retryListener;
        voiceMessagePlayer.setListener(clientMsgId -> {
            notifyVoiceItemChanged(lastPlayingClientMsgId);
            notifyVoiceItemChanged(clientMsgId);
            lastPlayingClientMsgId = clientMsgId;
        });
    }

    public void setMessageActions(@Nullable MessageListActions messageActions) {
        this.messageActions = messageActions;
    }

    public void setMentionMemberListener(
            @Nullable MessageViewHolderContext.OnMentionMemberListener mentionMemberListener) {
        this.mentionMemberListener = mentionMemberListener;
    }

    public void setPortraitClickListener(
            @Nullable MessageViewHolderContext.OnPortraitClickListener portraitClickListener) {
        this.portraitClickListener = portraitClickListener;
    }

    public void setQuoteClickListener(
            @Nullable MessageViewHolderContext.OnQuoteClickListener quoteClickListener) {
        this.quoteClickListener = quoteClickListener;
    }

    @NonNull
    public Map<String, String> buildUserDisplayNameMap() {
        MessageViewHolderContext context = new MessageViewHolderContext(
                isGroupChat,
                peerAvatarUrl,
                selfAvatarUrl,
                IMSClientBootstrap.getInstance().getCurrentUserId(),
                membersByUserId,
                items,
                readUiState,
                resolveGroupMemberCount(),
                sessionId,
                peerUserId,
                peerDisplayName,
                retryListener,
                messageActions,
                mentionMemberListener,
                portraitClickListener,
                quoteClickListener,
                listMode);
        return context.buildUserDisplayNameMap();
    }

    public int getMode() {
        return listMode;
    }

    public void setMode(int mode) {
        if (listMode == mode) {
            return;
        }
        listMode = mode;
        if (mode == MessageListMode.NORMAL) {
            clearMessageCheckStatus();
        }
        notifyDataSetChanged();
    }

    public void clearMessageCheckStatus() {
        checkedClientMsgIds.clear();
    }

    public boolean isMessageChecked(@NonNull AppMessage message) {
        String key = MessageRealmMapper.clientMsgIdForPersist(message);
        return !TextUtils.isEmpty(key) && checkedClientMsgIds.contains(key);
    }

    public void setMessageChecked(@NonNull AppMessage message, boolean checked) {
        String key = MessageRealmMapper.clientMsgIdForPersist(message);
        if (TextUtils.isEmpty(key)) {
            return;
        }
        if (checked) {
            checkedClientMsgIds.add(key);
        } else {
            checkedClientMsgIds.remove(key);
        }
    }

    public void checkMessage(@NonNull AppMessage message) {
        setMessageChecked(message, true);
    }

    @NonNull
    public List<AppMessage> getCheckedMessages() {
        List<AppMessage> checked = new ArrayList<>();
        for (AppMessage message : items) {
            if (message != null && isMessageChecked(message)) {
                checked.add(message);
            }
        }
        return checked;
    }

    public void removeMessages(@NonNull List<AppMessage> messages) {
        if (messages.isEmpty()) {
            return;
        }
        Set<String> keys = new HashSet<>();
        for (AppMessage message : messages) {
            if (message == null) {
                continue;
            }
            String key = MessageRealmMapper.clientMsgIdForPersist(message);
            if (!TextUtils.isEmpty(key)) {
                keys.add(key);
            }
        }
        if (keys.isEmpty()) {
            return;
        }
        for (int i = items.size() - 1; i >= 0; i--) {
            AppMessage existing = items.get(i);
            if (existing == null) {
                continue;
            }
            String key = MessageRealmMapper.clientMsgIdForPersist(existing);
            if (!TextUtils.isEmpty(key) && keys.contains(key)) {
                items.remove(i);
                checkedClientMsgIds.remove(key);
                notifyItemRemoved(i);
            }
        }
    }

    public void release() {
        voiceMessagePlayer.setListener(null);
        voiceMessagePlayer.stop();
        lastPlayingClientMsgId = null;
    }

    private void notifyVoiceItemChanged(@Nullable String clientMsgId) {
        if (TextUtils.isEmpty(clientMsgId)) {
            return;
        }
        for (int i = 0; i < items.size(); i++) {
            AppMessage message = items.get(i);
            if (message == null || !WeChatUiHelper.isVoiceMessage(message.getMsgContentType())) {
                continue;
            }
            if (clientMsgId.equals(message.getClientMsgId())) {
                notifyItemChanged(i);
                return;
            }
        }
    }

    public void setGroupMembers(@Nullable Map<String, GroupMemberItem> members) {
        membersByUserId.clear();
        if (members != null) {
            membersByUserId.putAll(members);
        }
        notifyDataSetChanged();
    }

    @NonNull
    public MessageReadUiState getReadUiState() {
        return readUiState;
    }

    public void onReadStateEvent(@NonNull MessageReadStateEvent event) {
        if (event.getType() == MessageReadStateEvent.Type.PEER_READ) {
            readUiState.applyPeerRead(event.getMessageId());
        } else {
            readUiState.applyGroupReadCount(event.getMessageId(), event.getReadPersonCount());
            updateMessageReadCountInList(event.getMessageId(), event.getReadPersonCount());
        }
        notifyDataSetChanged();
    }

    private void updateMessageReadCountInList(@NonNull String messageId, int readPersonCount) {
        for (int i = 0; i < items.size(); i++) {
            AppMessage message = items.get(i);
            if (message != null && messageId.equals(message.getMsgId())) {
                message.setReadPersonCount(readPersonCount);
                items.set(i, message);
                return;
            }
        }
    }

    private int resolveGroupMemberCount() {
        return Math.max(membersByUserId.size(), 1);
    }

    public boolean containsMessageId(@Nullable String messageId) {
        if (TextUtils.isEmpty(messageId)) {
            return false;
        }
        for (AppMessage message : items) {
            if (message != null && messageId.equals(message.getMsgId())) {
                return true;
            }
        }
        return false;
    }

    public void appendMessages(List<AppMessage> batch) {
        if (batch == null || batch.isEmpty()) {
            return;
        }
        Set<String> existing = new HashSet<>(items.size() * 2);
        for (AppMessage m : items) {
            if (m == null) {
                continue;
            }
            String key = MessageRealmMapper.clientMsgIdForPersist(m);
            if (!TextUtils.isEmpty(key)) {
                existing.add(key);
            }
        }
        List<AppMessage> toAdd = new ArrayList<>(batch.size());
        for (AppMessage m : batch) {
            if (m == null) {
                continue;
            }
            String key = MessageRealmMapper.clientMsgIdForPersist(m);
            if (TextUtils.isEmpty(key) || existing.contains(key)) {
                continue;
            }
            existing.add(key);
            toAdd.add(m);
        }
        if (toAdd.isEmpty()) {
            return;
        }
        int start = items.size();
        items.addAll(toAdd);
        reportMessageRead(toAdd.get(toAdd.size() - 1));
        notifyItemRangeInserted(start, toAdd.size());
    }

    public int prependMessages(@Nullable List<AppMessage> batch) {
        if (batch == null || batch.isEmpty()) {
            return 0;
        }
        Set<String> existing = new HashSet<>(items.size() * 2);
        for (AppMessage m : items) {
            if (m == null) {
                continue;
            }
            String key = MessageRealmMapper.clientMsgIdForPersist(m);
            if (!TextUtils.isEmpty(key)) {
                existing.add(key);
            }
        }
        List<AppMessage> toAdd = new ArrayList<>(batch.size());
        for (AppMessage m : batch) {
            if (m == null) {
                continue;
            }
            String key = MessageRealmMapper.clientMsgIdForPersist(m);
            if (TextUtils.isEmpty(key) || existing.contains(key)) {
                continue;
            }
            existing.add(key);
            toAdd.add(m);
        }
        if (toAdd.isEmpty()) {
            return 0;
        }
        items.addAll(0, toAdd);
        notifyItemRangeInserted(0, toAdd.size());
        return toAdd.size();
    }

    @Nullable
    public AppMessage getFirstMessage() {
        return items.isEmpty() ? null : items.get(0);
    }

    @Nullable
    public AppMessage getMessageAt(int position) {
        if (position < 0 || position >= items.size()) {
            return null;
        }
        return items.get(position);
    }

    private void reportMessageRead(AppMessage appMessage) {
        if (appMessage == null || appMessage.getMsgId() == null) {
            return;
        }
        ImMessageManager.getInstance().reportMessageRead(appMessage.getMsgId());
    }

    public void setNewMessage(List<AppMessage> batch) {
        items.clear();
        if (batch != null && !batch.isEmpty()) {
            items.addAll(batch);
        }
        readUiState.seedFromMessages(items);
        notifyDataSetChanged();
    }

    public void clearAllMessages() {
        items.clear();
        checkedClientMsgIds.clear();
        readUiState.seedFromMessages(items);
        notifyDataSetChanged();
    }

    public void updateMessage(@NonNull AppMessage message) {
        int index = indexOfMessage(message);
        if (index < 0) {
            appendMessages(Collections.singletonList(message));
            return;
        }
        AppMessage old = items.get(index);
        boolean timeChanged = old != null && old.getTimestamp() != message.getTimestamp();
        if (!timeChanged) {
            items.set(index, message);
            notifyItemChanged(index);
            return;
        }
        // 重发刷新时间后，按 timestamp（同毫秒再比 sequenceId）重排到正确位置
        items.remove(index);
        int newIndex = findInsertIndexByTimestamp(message);
        items.add(newIndex, message);
        if (newIndex == index) {
            notifyItemChanged(newIndex);
        } else {
            notifyItemMoved(index, newIndex);
            notifyItemChanged(newIndex);
        }
    }

    /** 升序列表中按 timestamp / sequenceId 找插入下标（列表中尚无该消息）。 */
    private int findInsertIndexByTimestamp(@NonNull AppMessage message) {
        long ts = message.getTimestamp();
        long seq = message.getSequenceId();
        for (int i = 0; i < items.size(); i++) {
            AppMessage existing = items.get(i);
            if (existing == null) {
                continue;
            }
            if (existing.getTimestamp() > ts) {
                return i;
            }
            if (existing.getTimestamp() == ts && existing.getSequenceId() > seq) {
                return i;
            }
        }
        return items.size();
    }

    public void replaceOrUpdateMessage(@NonNull AppMessage message) {
        int index = indexOfMessage(message);
        if (index < 0 && !TextUtils.isEmpty(message.getMsgId())) {
            index = indexOfMsgId(message.getMsgId());
        }
        if (index < 0) {
            return;
        }
        int oldType = resolveViewType(items.get(index));
        items.set(index, message);
        int newType = resolveViewType(message);
        if (oldType != newType) {
            // viewType 变化（如气泡→撤回行）需重建 ViewHolder
            notifyItemRemoved(index);
            notifyItemInserted(index);
        } else {
            notifyItemChanged(index);
        }
    }

    public void removeByMsgId(@NonNull String msgId) {
        if (TextUtils.isEmpty(msgId)) {
            return;
        }
        for (int i = items.size() - 1; i >= 0; i--) {
            AppMessage existing = items.get(i);
            if (existing != null && msgId.equals(existing.getMsgId())) {
                String key = MessageRealmMapper.clientMsgIdForPersist(existing);
                if (!TextUtils.isEmpty(key)) {
                    checkedClientMsgIds.remove(key);
                }
                items.remove(i);
                notifyItemRemoved(i);
                return;
            }
        }
    }

    private int indexOfMessage(@NonNull AppMessage message) {
        String clientMsgId = message.getClientMsgId();
        if (!TextUtils.isEmpty(clientMsgId)) {
            for (int i = 0; i < items.size(); i++) {
                AppMessage existing = items.get(i);
                if (existing != null && clientMsgId.equals(existing.getClientMsgId())) {
                    return i;
                }
            }
        }
        return indexOfMsgId(message.getMsgId());
    }

    private int indexOfMsgId(@Nullable String msgId) {
        if (TextUtils.isEmpty(msgId)) {
            return -1;
        }
        for (int i = 0; i < items.size(); i++) {
            AppMessage existing = items.get(i);
            if (existing != null && msgId.equals(existing.getMsgId())) {
                return i;
            }
        }
        return -1;
    }

    public int findPositionByMessageId(@Nullable String messageId) {
        if (TextUtils.isEmpty(messageId)) {
            return -1;
        }
        int pos = indexOfMsgId(messageId);
        if (pos >= 0) {
            return pos;
        }
        for (int i = 0; i < items.size(); i++) {
            AppMessage existing = items.get(i);
            if (existing != null && messageId.equals(existing.getClientMsgId())) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int getItemViewType(int position) {
        AppMessage m = items.get(position);
        return resolveViewType(m);
    }

    private int resolveViewType(@Nullable AppMessage message) {
        if (message == null) {
            return VIEW_TYPE_SYSTEM;
        }
        // 撤回优先：按 status 判断，不依赖 msgType/content
        if (WeChatUiHelper.isRecalledMessage(message)) {
            return VIEW_TYPE_RECALL;
        }
        if (WeChatUiHelper.isCallMessage(message)) {
            // 群聊通话：居中提示「谁发起了语音通话」，不走 1v1 气泡样式
            return isGroupChat ? VIEW_TYPE_SYSTEM : VIEW_TYPE_CALL;
        }
        int contentType = message.getMsgContentType();
        if (WeChatUiHelper.isImageMessage(contentType)) {
            return VIEW_TYPE_IMAGE;
        }
        if (WeChatUiHelper.isVoiceMessage(contentType)) {
            return VIEW_TYPE_VOICE;
        }
        if (WeChatUiHelper.isVideoMessage(contentType)) {
            return VIEW_TYPE_VIDEO;
        }
        if (WeChatUiHelper.isFileMessage(contentType)) {
            return VIEW_TYPE_FILE;
        }
        if (WeChatUiHelper.isMapMessage(contentType)) {
            return VIEW_TYPE_MAP;
        }
        if (WeChatUiHelper.isCardMessage(contentType)) {
            return VIEW_TYPE_CARD;
        }
        if (WeChatUiHelper.isRedPacketMessage(contentType)) {
            return VIEW_TYPE_HONGBAO;
        }
//        if (WeChatUiHelper.isHongbaoClaimMessage(contentType)) {
//            return VIEW_TYPE_HONGBAO_CLAIM;
//        }
        if (WeChatUiHelper.isNoticeMessage(contentType)) {
            return VIEW_TYPE_NOTICE;
        }
        if (WeChatUiHelper.isMeetStartMessage(contentType)) {
            return VIEW_TYPE_MEET_START;
        }
        if (WeChatUiHelper.isMeetEndMessage(contentType)) {
            return VIEW_TYPE_MEET_END;
        }
        if (WeChatUiHelper.isMergeTranspondMessage(contentType)) {
            return VIEW_TYPE_MERGE_TRANS;
        }
        if (WeChatUiHelper.isSystemMessage(message)) {
            return VIEW_TYPE_SYSTEM;
        }
        return VIEW_TYPE_TEXT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_RECALL) {
            View v = inflater.inflate(R.layout.item_message_recall, parent, false);
            return new RecallMessageVH(v);
        }
        if (viewType == VIEW_TYPE_SYSTEM) {
            View v = inflater.inflate(R.layout.item_message_system, parent, false);
            return new SystemMessageVH(v);
        }
        if (viewType == VIEW_TYPE_MEET_START || viewType == VIEW_TYPE_MEET_END) {
            View v = inflater.inflate(R.layout.item_message_meet_status, parent, false);
            return new MeetStatusMessageVH(v);
        }
//        if (viewType == VIEW_TYPE_HONGBAO_CLAIM) {
//            View v = inflater.inflate(R.layout.item_message_meet_status, parent, false);
//            return new HongbaoClaimMessageVH(v);
//        }
        switch (viewType) {
            case VIEW_TYPE_IMAGE: {
                View v = inflater.inflate(R.layout.item_message, parent, false);
                return new ImageMessageVH(v);
            }
            case VIEW_TYPE_CALL:
            case VIEW_TYPE_VOICE:
            case VIEW_TYPE_VIDEO:
            case VIEW_TYPE_FILE:
            case VIEW_TYPE_MAP:
            case VIEW_TYPE_CARD:
            case VIEW_TYPE_HONGBAO:
            case VIEW_TYPE_NOTICE:
            case VIEW_TYPE_MERGE_TRANS:
            case VIEW_TYPE_TEXT:
            case VIEW_TYPE_UNKNOWN:
            default: {
                View v = inflater.inflate(R.layout.item_message, parent, false);
                if (viewType == VIEW_TYPE_CALL) {
                    return new CallMessageVH(v);
                }
                if (viewType == VIEW_TYPE_VOICE) {
                    return new VoiceMessageVH(v);
                }
                if (viewType == VIEW_TYPE_VIDEO) {
                    return new VideoMessageVH(v);
                }
                if (viewType == VIEW_TYPE_FILE) {
                    return new FileMessageVH(v);
                }
                if (viewType == VIEW_TYPE_MAP) {
                    return new MapMessageVH(v);
                }
                if (viewType == VIEW_TYPE_CARD) {
                    return new CardMessageVH(v);
                }
                if (viewType == VIEW_TYPE_HONGBAO) {
                    return new HongBaoMessageVH(v);
                }
                if (viewType == VIEW_TYPE_NOTICE) {
                    return new NoticeMessageVH(v);
                }
                if (viewType == VIEW_TYPE_MERGE_TRANS) {
                    return new MergeTransMessageVH(v);
                }
                return new TextMessageVH(v);
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        AppMessage m = items.get(position);
        MessageViewHolderContext context = new MessageViewHolderContext(
                isGroupChat,
                peerAvatarUrl,
                selfAvatarUrl,
                IMSClientBootstrap.getInstance().getCurrentUserId(),
                membersByUserId,
                items,
                readUiState,
                resolveGroupMemberCount(),
                sessionId,
                peerUserId,
                peerDisplayName,
                retryListener,
                messageActions,
                mentionMemberListener,
                portraitClickListener,
                quoteClickListener,
                listMode);
        if (holder instanceof RecallMessageVH) {
            ((RecallMessageVH) holder).bind(m, context);
            return;
        }
        if (holder instanceof SystemMessageVH) {
            ((SystemMessageVH) holder).bind(m, context);
            return;
        }
        if (holder instanceof MeetStatusMessageVH) {
            ((MeetStatusMessageVH) holder).bind(m);
            return;
        }
//        if (holder instanceof HongbaoClaimMessageVH) {
//            ((HongbaoClaimMessageVH) holder).bind(m, context);
//            return;
//        }
        if (holder instanceof CallMessageVH) {
            ((CallMessageVH) holder).bind(m, position, context);
        } else if (holder instanceof ImageMessageVH) {
            ((ImageMessageVH) holder).bind(m, position, context);
        } else if (holder instanceof VoiceMessageVH) {
            ((VoiceMessageVH) holder).bind(m, position, context, voiceMessagePlayer);
        } else if (holder instanceof VideoMessageVH) {
            ((VideoMessageVH) holder).bind(m, position, context);
        } else if (holder instanceof FileMessageVH) {
            ((FileMessageVH) holder).bind(m, position, context);
        } else if (holder instanceof MapMessageVH) {
            ((MapMessageVH) holder).bind(m, position, context);
        } else if (holder instanceof CardMessageVH) {
            ((CardMessageVH) holder).bind(m, position, context);
        } else if (holder instanceof HongBaoMessageVH) {
            ((HongBaoMessageVH) holder).bind(m, position, context);
        } else if (holder instanceof NoticeMessageVH) {
            ((NoticeMessageVH) holder).bind(m, position, context);
        } else if (holder instanceof MergeTransMessageVH) {
            ((MergeTransMessageVH) holder).bind(m, position, context);
        } else if (holder instanceof TextMessageVH) {
            ((TextMessageVH) holder).bind(m, position, context);
        } else if (holder instanceof BaseMessageVH) {
            ((BaseMessageVH) holder).bindCommon(m, position, context);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

}
