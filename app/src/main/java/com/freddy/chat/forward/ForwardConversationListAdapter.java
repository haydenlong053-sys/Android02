package com.freddy.chat.forward;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.cboda.intelligent.R;
import com.freddy.chat.utils.AvatarViews;
import com.freddy.chat.utils.WeChatUiHelper;
import com.freddy.im.http.model.im.SessionItem;

import java.util.ArrayList;
import java.util.List;

/**
 * 转发页会话选择列表（自研专用，自 {@link com.freddy.chat.ConversationListAdapter} 复制）。
 */
public final class ForwardConversationListAdapter
        extends RecyclerView.Adapter<ForwardConversationListAdapter.VH> {

    public interface OnSessionClickListener {
        void onSessionClick(SessionItem sessionItem);
    }

    private final List<SessionItem> allItems = new ArrayList<>();
    private final List<SessionItem> items = new ArrayList<>();
    @Nullable
    private String filterKeyword = "";
    @Nullable
    private OnSessionClickListener onSessionClickListener;

    public void setOnSessionClickListener(@Nullable OnSessionClickListener listener) {
        this.onSessionClickListener = listener;
    }

    public void setNewData(@Nullable List<SessionItem> sessionList) {
        allItems.clear();
        if (sessionList != null) {
            allItems.addAll(sessionList);
        }
        applyFilter();
    }

    public void filterByKeyword(@Nullable String keyword) {
        filterKeyword = keyword != null ? keyword.trim().toLowerCase() : "";
        applyFilter();
    }

    private void applyFilter() {
        items.clear();
        if (TextUtils.isEmpty(filterKeyword)) {
            items.addAll(allItems);
        } else {
            for (SessionItem session : allItems) {
                if (session == null) {
                    continue;
                }
                String name = session.getSessionName() != null
                        ? session.getSessionName().toLowerCase() : "";
                String username = session.getUsername() != null
                        ? session.getUsername().toLowerCase() : "";
                if (name.contains(filterKeyword) || username.contains(filterKeyword)) {
                    items.add(session);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message_forward_conversation, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        SessionItem s = items.get(position);
        String name = WeChatUiHelper.resolveSessionListDisplayName(s, null);
        if (name.isEmpty()){
            holder.mLlView.setVisibility(View.GONE);
        }else {
            holder.mLlView.setVisibility(View.VISIBLE);
        }
        String displayName = name.isEmpty()
                ? holder.itemView.getContext().getString(R.string.conversation_name_fallback)
                : name;
        holder.tvName.setText(displayName);
        AvatarViews.bind(holder.avatar, WeChatUiHelper.resolveAvatarUrl(s), displayName, s.getSessionId());

        holder.itemView.setOnClickListener(v -> {
            if (onSessionClickListener != null) {
                onSessionClickListener.onSessionClick(s);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static final class VH extends RecyclerView.ViewHolder {
        final View avatar;
        final View mLlView;
        final TextView tvName;

        VH(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.avatar);
            tvName = itemView.findViewById(R.id.tv_name);
            mLlView = itemView.findViewById(R.id.mLlView);
        }
    }
}
