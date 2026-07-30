package com.freddy.chat;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.cboda.baselibrary.widge.VpGradeView;
import com.cboda.intelligent.R;
import com.freddy.chat.mention.AtMentionChecker;
import com.freddy.chat.utils.MessageAvatarBinder;
import com.freddy.chat.utils.WeChatUiHelper;
import com.freddy.im.http.model.im.SessionItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 主会话列表 {@link ConversationListFragment} 专用。
 * 转发选会话请使用 {@link com.freddy.chat.forward.ForwardConversationListAdapter}。
 */
public final class ConversationListAdapter extends RecyclerView.Adapter<ConversationListAdapter.VH> {

    public interface OnSessionClickListener {
        void onSessionClick(SessionItem sessionItem);
    }

    public interface OnSessionLongClickListener {
        boolean onSessionLongClick(SessionItem sessionItem);
    }

    private final List<SessionItem> items = new ArrayList<>();
    @Nullable
    private String currentUserId;
    @Nullable
    private OnSessionClickListener onSessionClickListener;
    @Nullable
    private OnSessionLongClickListener onSessionLongClickListener;

    public void setOnSessionClickListener(@Nullable OnSessionClickListener listener) {
        this.onSessionClickListener = listener;
    }

    public void setCurrentUserId(@Nullable String currentUserId) {
        this.currentUserId = currentUserId;
    }

    public void setOnSessionLongClickListener(@Nullable OnSessionLongClickListener listener) {
        this.onSessionLongClickListener = listener;
    }

    public void setNewData(@Nullable List<SessionItem> sessionList) {
        List<SessionItem> next = new ArrayList<>();
        if (sessionList != null) {
            next.addAll(sessionList);
        }
        sortSessions(next);
        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new SessionDiffCallback(items, next), true);
        items.clear();
        items.addAll(next);
        diff.dispatchUpdatesTo(this);
    }

    /**
     * 从列表中移除会话（仅 UI 层内存列表，不触发网络刷新）。
     */
    public boolean removeBySessionId(@Nullable String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            return false;
        }
        for (int i = 0; i < items.size(); i++) {
            SessionItem s = items.get(i);
            if (s != null && sessionId.equals(s.getSessionId())) {
                items.remove(i);
                notifyItemRemoved(i);
                return true;
            }
        }
        return false;
    }

    private static final Object PAYLOAD_UNREAD = new Object();

    /** 仅清除指定会话未读角标，避免整表刷新闪烁。 */
    public boolean clearUnread(@Nullable String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            return false;
        }
        for (int i = 0; i < items.size(); i++) {
            SessionItem s = items.get(i);
            if (s != null && sessionId.equals(s.getSessionId())) {
                if (s.getUnreadCount() <= 0) {
                    return false;
                }
                s.setUnreadCount(0);
                notifyItemChanged(i, PAYLOAD_UNREAD);
                return true;
            }
        }
        return false;
    }

    /** 合并/更新部分会话（群变更、拉取列表等），按置顶与最近消息时间排序。 */
    public void mergeSessions(@Nullable List<SessionItem> updates) {
        if (updates == null || updates.isEmpty()) {
            return;
        }
        List<SessionItem> next = new ArrayList<>(items);
        for (SessionItem update : updates) {
            if (update == null || update.getSessionId() == null) {
                continue;
            }
            int index = -1;
            for (int i = 0; i < next.size(); i++) {
                if (update.getSessionId().equals(next.get(i).getSessionId())) {
                    index = i;
                    break;
                }
            }
            if (index >= 0) {
                next.set(index, update);
            } else {
                next.add(0, update);
            }
        }
        sortSessions(next);
        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new SessionDiffCallback(items, next), true);
        items.clear();
        items.addAll(next);
        diff.dispatchUpdatesTo(this);
    }

    private static void sortSessions(@NonNull List<SessionItem> list) {
        list.sort((a, b) -> {
            int topCmp = Long.compare(b.getTopNum(), a.getTopNum());
            if (topCmp != 0) {
                return topCmp;
            }
            return Long.compare(b.getLastMessageTime(), a.getLastMessageTime());
        });
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_conversation, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position, @NonNull List<Object> payloads) {
        if (!payloads.isEmpty()) {
            boolean unreadOnly = false;
            for (Object payload : payloads) {
                if (payload == PAYLOAD_UNREAD) {
                    unreadOnly = true;
                    break;
                }
            }
            if (unreadOnly) {
                bindUnread(holder, items.get(position));
                return;
            }
        }
        onBindViewHolder(holder, position);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        SessionItem s = items.get(position);
        String name = WeChatUiHelper.resolveSessionListDisplayName(s, currentUserId);
        String displayName = name.isEmpty()
                ? holder.itemView.getContext().getString(R.string.conversation_name_fallback)
                : name;
        holder.tvName.setText(displayName);
        MessageAvatarBinder.bindSessionListAvatar(holder.avatar, s, currentUserId);
        MessageAvatarBinder.bindSessionRegisterDay(holder.tvRegisterDay, s, currentUserId);

        holder.tvSubtitle.setText(buildSubtitle(holder, s));
        if (WeChatUiHelper.isVoiceSessionPreview(s)) {
            holder.ivPreviewType.setVisibility(View.VISIBLE);
            holder.ivPreviewType.setImageResource(R.drawable.ic_msg_voice);
        } else if (WeChatUiHelper.isImageSessionPreview(s)) {
            holder.ivPreviewType.setVisibility(View.VISIBLE);
            holder.ivPreviewType.setImageResource(R.drawable.ic_msg_image);
        } else {
            holder.ivPreviewType.setVisibility(View.GONE);
        }

        bindUnread(holder, s);

        holder.itemView.setBackgroundResource(
                s.getTopNum() > 0 ? R.drawable.bg_item_pinned : R.drawable.bg_item_white);

        holder.itemView.setOnClickListener(v -> {
            if (onSessionClickListener != null) {
                onSessionClickListener.onSessionClick(s);
            }
        });
        holder.itemView.setOnLongClickListener(v -> {
            if (onSessionLongClickListener != null) {
                return onSessionLongClickListener.onSessionLongClick(s);
            }
            return false;
        });
    }

    private static void bindUnread(@NonNull VH holder, @NonNull SessionItem s) {
        int unread = s.getUnreadCount();
        boolean muted = s.isMuteNotify();
        if (unread > 0) {
            if (muted) {
                holder.tvUnread.setVisibility(View.GONE);
                holder.muteDot.setVisibility(View.VISIBLE);
            } else {
                holder.muteDot.setVisibility(View.GONE);
                holder.tvUnread.setVisibility(View.VISIBLE);
                holder.tvUnread.setText(unread > 99 ? "99+" : String.valueOf(unread));
            }
        } else {
            holder.tvUnread.setVisibility(View.GONE);
            holder.muteDot.setVisibility(View.GONE);
        }
    }

    @NonNull
    private CharSequence buildSubtitle(@NonNull VH holder, @NonNull SessionItem session) {
        String preview = WeChatUiHelper.formatSessionPreview(session, currentUserId);
        if (AtMentionChecker.isMentioningMe(session, currentUserId)) {
            String prefix = holder.itemView.getContext().getString(com.cboda.chatui.R.string.someone_mentioned_me);
            String text = prefix + preview;
            SpannableString spannable = new SpannableString(text);
            int blue = ContextCompat.getColor(holder.itemView.getContext(), R.color.color_3B47DD);
            spannable.setSpan(
                    new ForegroundColorSpan(blue),
                    0,
                    prefix.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            return spannable;
        }
        return preview;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private static final class SessionDiffCallback extends DiffUtil.Callback {
        @NonNull
        private final List<SessionItem> oldList;
        @NonNull
        private final List<SessionItem> newList;

        SessionDiffCallback(@NonNull List<SessionItem> oldList, @NonNull List<SessionItem> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            SessionItem oldItem = oldList.get(oldItemPosition);
            SessionItem newItem = newList.get(newItemPosition);
            return TextUtils.equals(oldItem.getSessionId(), newItem.getSessionId());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            SessionItem a = oldList.get(oldItemPosition);
            SessionItem b = newList.get(newItemPosition);
            return a.getUnreadCount() == b.getUnreadCount()
                    && a.getTopNum() == b.getTopNum()
                    && a.getLastMessageTime() == b.getLastMessageTime()
                    && a.getLastMessageStatus() == b.getLastMessageStatus()
                    && a.getLastMsgType() == b.getLastMsgType()
                    && a.isMuteNotify() == b.isMuteNotify()
                    && TextUtils.equals(a.getSessionName(), b.getSessionName())
                    && TextUtils.equals(a.getIcon(), b.getIcon())
                    && TextUtils.equals(a.getUsername(), b.getUsername())
                    && TextUtils.equals(a.getLastMessageId(), b.getLastMessageId())
                    && TextUtils.equals(a.getLastMessageContent(), b.getLastMessageContent())
                    && TextUtils.equals(a.getLastContentType(), b.getLastContentType())
                    && TextUtils.equals(a.getLastCustomInfo(), b.getLastCustomInfo())
                    && Objects.equals(a.getOppositeUserId(), b.getOppositeUserId());
        }

        @Nullable
        @Override
        public Object getChangePayload(int oldItemPosition, int newItemPosition) {
            SessionItem a = oldList.get(oldItemPosition);
            SessionItem b = newList.get(newItemPosition);
            boolean onlyUnread = a.getUnreadCount() != b.getUnreadCount()
                    && a.getTopNum() == b.getTopNum()
                    && a.getLastMessageTime() == b.getLastMessageTime()
                    && a.getLastMessageStatus() == b.getLastMessageStatus()
                    && a.getLastMsgType() == b.getLastMsgType()
                    && a.isMuteNotify() == b.isMuteNotify()
                    && TextUtils.equals(a.getSessionName(), b.getSessionName())
                    && TextUtils.equals(a.getIcon(), b.getIcon())
                    && TextUtils.equals(a.getUsername(), b.getUsername())
                    && TextUtils.equals(a.getLastMessageId(), b.getLastMessageId())
                    && TextUtils.equals(a.getLastMessageContent(), b.getLastMessageContent())
                    && TextUtils.equals(a.getLastContentType(), b.getLastContentType())
                    && TextUtils.equals(a.getLastCustomInfo(), b.getLastCustomInfo())
                    && Objects.equals(a.getOppositeUserId(), b.getOppositeUserId());
            return onlyUnread ? PAYLOAD_UNREAD : null;
        }
    }

    static final class VH extends RecyclerView.ViewHolder {
        final VpGradeView avatar;
        final TextView tvName;
        final TextView tvRegisterDay;
        final ImageView ivPreviewType;
        final TextView tvSubtitle;
        final TextView tvUnread;
        final View muteDot;

        VH(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.avatar);
            tvName = itemView.findViewById(R.id.tv_name);
            tvRegisterDay = itemView.findViewById(R.id.tv_register_day);
            ivPreviewType = itemView.findViewById(R.id.iv_preview_type);
            tvSubtitle = itemView.findViewById(R.id.tv_subtitle);
            tvUnread = itemView.findViewById(R.id.tv_unread);
            muteDot = itemView.findViewById(R.id.v_mute_dot);
        }
    }
}
