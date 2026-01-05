/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.group.manage;

import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.cboda.chatui.R;
import com.cboda.chatui.contact.UserListAdapter;
import com.cboda.chatui.contact.model.HeaderValue;
import com.cboda.chatui.contact.viewholder.header.HeaderViewHolder;
import cn.wildfirechat.model.GroupInfo;

public class MuteGroupMemberViewHolder extends HeaderViewHolder<HeaderValue> {
    TextView titleTextView;

    public MuteGroupMemberViewHolder(Fragment fragment, UserListAdapter adapter, View itemView) {
        super(fragment, adapter, itemView);
        titleTextView = itemView.findViewById(R.id.nameTextView);
    }

    @Override
    public void onBind(HeaderValue headerValue) {
        GroupInfo groupInfo = (GroupInfo) headerValue.getValue();
        titleTextView.setText(headerValue.isBoolValue() ? "发言白名单" : "群成员禁言");
    }
}
