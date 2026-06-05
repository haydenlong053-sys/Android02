package com.freddy.chat;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.freddy.chat.utils.AvatarViews;
import com.freddy.chat.utils.WeChatUiHelper;
import com.freddy.im.http.model.im.UserInfoItem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.cboda.intelligent.R;

/**
 * 创建群：可勾选的用户列表。
 */
public class GroupMemberSelectAdapter extends RecyclerView.Adapter<GroupMemberSelectAdapter.ViewHolder> {

    private final List<UserInfoItem> items = new ArrayList<>();
    private final Set<String> selectedUserIds = new HashSet<>();

    public void setItems(@NonNull List<UserInfoItem> users) {
        items.clear();
        items.addAll(users);
        notifyDataSetChanged();
    }

    @NonNull
    public List<String> getSelectedUserIds() {
        return new ArrayList<>(selectedUserIds);
    }

    public void setSelectedUserIds(@NonNull Set<String> userIds) {
        selectedUserIds.clear();
        selectedUserIds.addAll(userIds);
        notifyDataSetChanged();
    }

    public void toggleSelection(@NonNull String userId) {
        if (selectedUserIds.contains(userId)) {
            selectedUserIds.remove(userId);
        } else {
            selectedUserIds.add(userId);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_group_member_select, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserInfoItem item = items.get(position);
        String userId = item.getUserId() != null ? item.getUserId() : "";
        String userName = item.getUserName();
        String displayName = !TextUtils.isEmpty(userName) ? userName : userId;
        holder.tvUserName.setText(displayName);
        holder.tvUserId.setText(userId);
        AvatarViews.bind(holder.avatar, WeChatUiHelper.resolveAvatarUrl(item), displayName, userId);
        holder.cbMember.setChecked(selectedUserIds.contains(userId));
        holder.itemView.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(userId)) {
                toggleSelection(userId);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final CheckBox cbMember;
        final View avatar;
        final TextView tvUserName;
        final TextView tvUserId;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cbMember = itemView.findViewById(R.id.cb_member);
            avatar = itemView.findViewById(R.id.avatar);
            tvUserName = itemView.findViewById(R.id.tv_user_name);
            tvUserId = itemView.findViewById(R.id.tv_user_id);
        }
    }
}
