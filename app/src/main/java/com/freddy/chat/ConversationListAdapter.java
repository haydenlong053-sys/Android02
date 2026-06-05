package com.freddy.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.freddy.chat.utils.AvatarViews;
import com.freddy.chat.utils.WeChatUiHelper;
import com.freddy.im.http.model.im.SessionItem;

import java.util.ArrayList;
import java.util.List;
import com.cboda.intelligent.R;

/**
 * 会话列表：{@link SessionItem} + 按 sessionId 聚合的未读数。
 */
public final class ConversationListAdapter extends RecyclerView.Adapter<ConversationListAdapter.VH> {

    public interface OnSessionClickListener {
        void onSessionClick(SessionItem sessionItem);
    }

    public interface OnSessionLongClickListener {
        boolean onSessionLongClick(SessionItem sessionItem);
    }

    private final List<SessionItem> items = new ArrayList<>();
    private OnSessionClickListener onSessionClickListener;
    private OnSessionLongClickListener onSessionLongClickListener;

    public void setOnSessionClickListener(OnSessionClickListener listener) {
        this.onSessionClickListener = listener;
    }

    public void setOnSessionLongClickListener(OnSessionLongClickListener listener) {
        this.onSessionLongClickListener = listener;
    }


    public void setNewData(List<SessionItem> sessionList) {
        items.clear();
        if (sessionList != null) {
            items.addAll(sessionList);
        }
        notifyDataSetChanged();
    }

    /**
     * 从列表中移除会话（仅 UI 层内存列表，不触发网络刷新）。
     *
     * @return 是否成功移除
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


    /**
     * 合并/更新部分会话（群变更等），按 sessionId 匹配。
     */
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
                notifyItemChanged(index);
            } else {
                items.add(0, update);
                notifyItemInserted(0);
            }
        }
        sortByRecent();
    }

    private void sortByRecent() {
        items.sort((a, b) -> Long.compare(b.getLastMessageTime(), a.getLastMessageTime()));
        notifyDataSetChanged();
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
        String name = s.getSessionName() != null ? s.getSessionName() : "";
        String displayName = name.isEmpty()
                ? holder.itemView.getContext().getString(R.string.conversation_name_fallback)
                : name;
        holder.tvName.setText(displayName);
        AvatarViews.bind(holder.avatar, WeChatUiHelper.resolveAvatarUrl(s), displayName, s.getSessionId());

        holder.tvSubtitle.setText(WeChatUiHelper.formatSessionPreview(s));
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
