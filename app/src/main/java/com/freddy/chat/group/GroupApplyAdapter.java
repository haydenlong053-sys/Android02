package com.freddy.chat.group;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.cboda.intelligent.R;
import com.freddy.im.http.model.im.GroupApplyInfoRes;

import java.util.ArrayList;
import java.util.List;

public final class GroupApplyAdapter extends RecyclerView.Adapter<GroupApplyAdapter.Holder> {

    public interface OnApplyActionListener {
        void onApprove(@NonNull GroupApplyInfoRes item);

        void onReject(@NonNull GroupApplyInfoRes item);
    }

    private final List<GroupApplyInfoRes> items = new ArrayList<>();
    @Nullable
    private OnApplyActionListener listener;

    public void setItems(@Nullable List<GroupApplyInfoRes> list) {
        items.clear();
        if (list != null) {
            items.addAll(list);
        }
        notifyDataSetChanged();
    }

    public void setOnApplyActionListener(@Nullable OnApplyActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_group_apply, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        GroupApplyInfoRes item = items.get(position);
        String applicant = TextUtils.isEmpty(item.getApplicant()) ? "—" : item.getApplicant();
        holder.tvTitle.setText(holder.itemView.getContext().getString(
                R.string.group_apply_item_title, applicant));
        holder.tvMeta.setText(holder.itemView.getContext().getString(
                R.string.group_apply_item_meta, item.getGroupId(), item.getApplyId()));
        boolean pending = "0".equals(item.getStatus()) || TextUtils.isEmpty(item.getStatus());
        holder.btnApprove.setEnabled(pending);
        holder.btnReject.setEnabled(pending);
        holder.btnApprove.setOnClickListener(v -> {
            if (listener != null) {
                listener.onApprove(item);
            }
        });
        holder.btnReject.setOnClickListener(v -> {
            if (listener != null) {
                listener.onReject(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final TextView tvTitle;
        final TextView tvMeta;
        final Button btnApprove;
        final Button btnReject;

        Holder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_apply_title);
            tvMeta = itemView.findViewById(R.id.tv_apply_meta);
            btnApprove = itemView.findViewById(R.id.btn_approve);
            btnReject = itemView.findViewById(R.id.btn_reject);
        }
    }
}
