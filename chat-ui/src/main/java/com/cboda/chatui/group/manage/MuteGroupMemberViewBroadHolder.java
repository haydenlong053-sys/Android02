/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.group.manage;

import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.cboda.chatui.R;
import com.cboda.chatui.contact.UserListBroadAdapter;
import com.cboda.chatui.contact.model.HeaderBroadValue;
import com.cboda.chatui.contact.viewholder.header.HeaderViewBroadHolder;
import cn.wildfirechat.model.GroupInfo;

public class MuteGroupMemberViewBroadHolder extends HeaderViewBroadHolder<HeaderBroadValue> {
    TextView titleTextView;

    public MuteGroupMemberViewBroadHolder(Fragment fragment, UserListBroadAdapter adapter, View itemView) {
        super(fragment, adapter, itemView);
        titleTextView = itemView.findViewById(R.id.nameTextView);
    }

    @Override
    public void onBind(HeaderBroadValue headerValue) {
        GroupInfo groupInfo = (GroupInfo) headerValue.getValue();
        titleTextView.setText(headerValue.isBoolValue() ? "发言白名单" : "群成员禁言");
    }
}
