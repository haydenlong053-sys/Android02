package com.freddy.chat;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.freddy.chat.event.MessageReadStateEvent;
import com.freddy.chat.im.IMSClientBootstrap;
import com.freddy.chat.im.MessageReadUiState;
import com.freddy.chat.utils.WeChatUiHelper;
import com.freddy.chat.viewholder.BaseMessageVH;
import com.freddy.chat.viewholder.FileMessageVH;
import com.freddy.chat.viewholder.ImageMessageVH;
import com.freddy.chat.viewholder.MapMessageVH;
import com.freddy.chat.viewholder.MergeTransMessageVH;
import com.freddy.chat.viewholder.MessageViewHolderContext;
import com.freddy.chat.viewholder.SystemMessageVH;
import com.freddy.chat.viewholder.TextMessageVH;
import com.freddy.chat.viewholder.VideoMessageVH;
import com.freddy.chat.viewholder.VoiceMessageVH;
import com.freddy.im.bean.AppMessage;
import com.freddy.im.db.mapper.MessageRealmMapper;
import com.freddy.im.http.model.im.GroupMemberItem;
import com.freddy.im.message.ImMessageManager;

import java.util.ArrayList;
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
    private static final int VIEW_TYPE_UNKNOWN = 99;

    private final boolean isGroupChat;
    @Nullable
    private final String peerAvatarUrl;
    @Nullable
    private final String selfAvatarUrl;

    private final Map<String, GroupMemberItem> membersByUserId = new HashMap<>();

    private final List<AppMessage> items = new ArrayList<>();
    @NonNull
    private final MessageReadUiState readUiState = new MessageReadUiState();

    public MessageListAdapter(boolean isGroupChat,
                              @Nullable String peerAvatarUrl,
                              @Nullable String selfAvatarUrl) {
        this.isGroupChat = isGroupChat;
        this.peerAvatarUrl = peerAvatarUrl;
        this.selfAvatarUrl = selfAvatarUrl;
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

    public void updateMessage(@NonNull AppMessage message) {
        String clientMsgId = message.getClientMsgId();
        if (TextUtils.isEmpty(clientMsgId)) {
            return;
        }
        for (int i = 0; i < items.size(); i++) {
            AppMessage existing = items.get(i);
            if (clientMsgId.equals(existing.getClientMsgId())) {
                items.set(i, message);
                notifyItemChanged(i);
                return;
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        AppMessage m = items.get(position);
        return resolveViewType(m);
    }

    private int resolveViewType(@Nullable AppMessage message) {
        if (message == null || WeChatUiHelper.isSystemMessage(message)) {
            return VIEW_TYPE_SYSTEM;
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
        return VIEW_TYPE_TEXT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_SYSTEM) {
            View v = inflater.inflate(R.layout.item_message_system, parent, false);
            return new SystemMessageVH(v);
        }
        switch (viewType) {
            case VIEW_TYPE_IMAGE: {
                View v = inflater.inflate(R.layout.item_message, parent, false);
                return new ImageMessageVH(v);
            }
            case VIEW_TYPE_VOICE:
            case VIEW_TYPE_VIDEO:
            case VIEW_TYPE_FILE:
            case VIEW_TYPE_MAP:
            case VIEW_TYPE_MERGE_TRANS:
            case VIEW_TYPE_TEXT:
            case VIEW_TYPE_UNKNOWN:
            default: {
                View v = inflater.inflate(R.layout.item_message, parent, false);
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
                resolveGroupMemberCount());
        if (holder instanceof SystemMessageVH) {
            ((SystemMessageVH) holder).bind(m, context);
            return;
        }
        if (holder instanceof ImageMessageVH) {
            ((ImageMessageVH) holder).bind(m, position, context);
        } else if (holder instanceof VoiceMessageVH) {
            ((VoiceMessageVH) holder).bind(m, position, context);
        } else if (holder instanceof VideoMessageVH) {
            ((VideoMessageVH) holder).bind(m, position, context);
        } else if (holder instanceof FileMessageVH) {
            ((FileMessageVH) holder).bind(m, position, context);
        } else if (holder instanceof MapMessageVH) {
            ((MapMessageVH) holder).bind(m, position, context);
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
