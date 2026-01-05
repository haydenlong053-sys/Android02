/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.contact.viewholder.header;

import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.cboda.chatui.R;
import com.cboda.chatui.contact.UserListAdapter;
import com.cboda.chatui.contact.model.GroupValue;
import cn.wildfirechat.remote.ChatManager;

@SuppressWarnings("unused")
public class GroupViewHolder extends HeaderViewHolder<GroupValue> {

    public GroupViewHolder(Fragment fragment, UserListAdapter adapter, View itemView) {
        super(fragment, adapter, itemView);
        bindViews(itemView);
    }

    private void bindViews(View itemView) {
        tvNewGroupUnread = itemView.findViewById(R.id.tvNewGroupUnread);
    }
    private TextView tvNewGroupUnread;

    @Override
    public void onBind(GroupValue groupValue) {
        if (tvNewGroupUnread != null){
            long count = ChatManager.Instance().qunshenqing;
            if (count > 0) {
                tvNewGroupUnread.setVisibility(View.VISIBLE);
                if (count >99){
                    tvNewGroupUnread.setText("99+");
                } else {
                    tvNewGroupUnread.setText("" + count);
                }
            } else {
                tvNewGroupUnread.setVisibility(View.GONE);
            }
        }
    }
}
