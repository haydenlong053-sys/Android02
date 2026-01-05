/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.contact.viewholder.header;

import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.cboda.chatui.R;
import com.cboda.chatui.contact.UserListBroadAdapter;
import com.cboda.chatui.contact.model.GroupBroadValue;
import cn.wildfirechat.remote.ChatManager;

@SuppressWarnings("unused")
public class GroupViewBroadHolder extends HeaderViewBroadHolder<GroupBroadValue> {

    public GroupViewBroadHolder(Fragment fragment, UserListBroadAdapter adapter, View itemView) {
        super(fragment, adapter, itemView);
        bindViews(itemView);
    }

    private void bindViews(View itemView) {
        tvNewGroupUnread = itemView.findViewById(R.id.tvNewGroupUnread);
    }
    private TextView tvNewGroupUnread;

    @Override
    public void onBind(GroupBroadValue groupValue) {
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
