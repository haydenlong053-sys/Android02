package com.freddy.chat;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.cboda.intelligent.R;
import com.freddy.chat.mention.AtMentionChecker;
import com.freddy.chat.utils.AvatarViews;
import com.freddy.chat.utils.WeChatUiHelper;
import com.freddy.im.http.model.im.SessionItem;

import java.util.ArrayList;
import java.util.List;

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
        items.clear();
        if (sessionList != null) {
            items.addAll(sessionList);
        }
        sortSessions();
        notifyDataSetChanged();
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

    /** 合并/更新部分会话（群变更、拉取列表等），按置顶与最近消息时间排序。 */
    public void mergeSessions(@Nullable List<SessionItem> updates) {
        if (updates == null || updates.isEmpty()) {
            return;
        }
        for (SessionItem update : updates) {
            if (update == null || update.getSessionId() == null) {
                continue;
            }
            int index = -1;
            for (int i = 0; i < items.size(); i++) {
                if (update.getSessionId().equals(items.get(i).getSessionId())) {
                    index = i;
                    break;
                }
            }
            if (index >= 0) {
                items.set(index, update);
            } else {
                items.add(0, update);
            }
        }
        sortSessions();
        notifyDataSetChanged();
    }

    private void sortSessions() {
        items.sort((a, b) -> {
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
    public void onBindViewHolder(@NonNull VH holder, int position) {
        SessionItem s = items.get(position);
        String name = WeChatUiHelper.resolveSessionListDisplayName(s, currentUserId);
        String displayName = name.isEmpty()
                ? holder.itemView.getContext().getString(R.string.conversation_name_fallback)
                : name;
        holder.tvName.setText(displayName);
        AvatarViews.bind(holder.avatar, WeChatUiHelper.resolveAvatarUrl(s), displayName, s.getSessionId());

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

        Integer unread = s.getUnreadCount();
        if (unread != null && unread > 0) {
            holder.tvUnread.setVisibility(View.VISIBLE);
            holder.tvUnread.setText(unread > 99 ? "99+" : String.valueOf(unread));
        } else {
            holder.tvUnread.setVisibility(View.GONE);
        }

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

    static final class VH extends RecyclerView.ViewHolder {
        final View avatar;
        final TextView tvName;
        final ImageView ivPreviewType;
        final TextView tvSubtitle;
        final TextView tvUnread;

        VH(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.avatar);
            tvName = itemView.findViewById(R.id.tv_name);
            ivPreviewType = itemView.findViewById(R.id.iv_preview_type);
            tvSubtitle = itemView.findViewById(R.id.tv_subtitle);
            tvUnread = itemView.findViewById(R.id.tv_unread);
        }
    }
}
