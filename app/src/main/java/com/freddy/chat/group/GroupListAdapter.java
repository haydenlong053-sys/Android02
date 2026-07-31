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
import com.freddy.im.http.model.im.GroupVo;

import java.util.ArrayList;
import java.util.List;

public final class GroupListAdapter extends RecyclerView.Adapter<GroupListAdapter.Holder> {

    public interface OnGroupClickListener {
        void onGroupClick(@NonNull GroupVo group);
    }

    private final List<GroupVo> items = new ArrayList<>();
    @Nullable
    private OnGroupClickListener listener;

    public void setItems(@Nullable List<GroupVo> groups) {
        items.clear();
        if (groups != null) {
            items.addAll(groups);
        }
        notifyDataSetChanged();
    }

    public void setOnGroupClickListener(@Nullable OnGroupClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_group_list, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        GroupVo group = items.get(position);
        String name = TextUtils.isEmpty(group.getName()) ? holder.itemView.getContext()
                .getString(com.cboda.baselibrary.R.string.conversation_name_fallback) : group.getName();
        holder.tvName.setText(name);
        String meta = group.getMemCount() > 0
                ? group.getMemCount() + "人 · " + group.getId()
                : group.getId();
        holder.tvMeta.setText(meta);
        AvatarViews.bind(holder.avatarRoot, group.getIcon(), name, name);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onGroupClick(group);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final View avatarRoot;
        final TextView tvName;
        final TextView tvMeta;

        Holder(@NonNull View itemView) {
            super(itemView);
            avatarRoot = itemView.findViewById(R.id.avatar);
            tvName = itemView.findViewById(R.id.tv_group_name);
            tvMeta = itemView.findViewById(R.id.tv_group_meta);
        }
    }
}
