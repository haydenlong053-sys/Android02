package com.freddy.chat.group;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.cboda.intelligent.R;
import com.freddy.chat.utils.AvatarViews;
import com.freddy.im.http.model.im.GroupUserVo;

import java.util.ArrayList;
import java.util.List;

public final class GroupMemberAdapter extends RecyclerView.Adapter<GroupMemberAdapter.Holder> {

    public interface OnMemberActionListener {
        void onKick(@NonNull GroupUserVo member);
    }

    private final List<GroupUserVo> items = new ArrayList<>();
    @Nullable
    private String ownerId;
    @Nullable
    private String currentUserId;
    private boolean showKick;
    @Nullable
    private OnMemberActionListener actionListener;

    public void setMembers(@Nullable List<GroupUserVo> members) {
        items.clear();
        if (members != null) {
            items.addAll(members);
        }
        notifyDataSetChanged();
    }

    public void setOwnerContext(@Nullable String ownerId, @Nullable String currentUserId, boolean showKick) {
        this.ownerId = ownerId;
        this.currentUserId = currentUserId;
        this.showKick = showKick;
        notifyDataSetChanged();
    }

    public void setOnMemberActionListener(@Nullable OnMemberActionListener listener) {
        this.actionListener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_group_member, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        GroupUserVo member = items.get(position);
        String name = member.getDisplayName();
        holder.tvName.setText(name);
        boolean isOwner = !TextUtils.isEmpty(ownerId) && ownerId.equals(member.getUserId());
        holder.tvRole.setText(isOwner
                ? holder.itemView.getContext().getString(R.string.group_role_owner)
                : holder.itemView.getContext().getString(R.string.group_role_member));
        AvatarViews.bind(holder.avatarRoot, member.getHeadPortrait(), name, name);

        boolean canKick = showKick
                && !TextUtils.isEmpty(ownerId)
                && ownerId.equals(currentUserId)
                && !isOwner
                && !TextUtils.isEmpty(member.getUserId())
                && !member.getUserId().equals(currentUserId);
        if (canKick) {
            holder.btnAction.setVisibility(View.VISIBLE);
            holder.btnAction.setText(R.string.group_member_kick);
            holder.btnAction.setOnClickListener(v -> {
                if (actionListener != null) {
                    actionListener.onKick(member);
                }
            });
        } else {
            holder.btnAction.setVisibility(View.GONE);
            holder.btnAction.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final View avatarRoot;
        final TextView tvName;
        final TextView tvRole;
        final TextView btnAction;

        Holder(@NonNull View itemView) {
            super(itemView);
            avatarRoot = itemView.findViewById(R.id.avatar);
            tvName = itemView.findViewById(R.id.tv_member_name);
            tvRole = itemView.findViewById(R.id.tv_member_role);
            btnAction = itemView.findViewById(R.id.btn_member_action);
        }
    }
}
