/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.contact.pick.viewholder;

import android.view.View;

import androidx.fragment.app.Fragment;

import com.cboda.chatui.contact.UserListBroadAdapter;
import com.cboda.chatui.contact.model.GroupBroadValue;
import com.cboda.chatui.contact.viewholder.header.HeaderViewBroadHolder;

@SuppressWarnings("unused")
public class PickGroupViewBroadHolder extends HeaderViewBroadHolder<GroupBroadValue> {

    public PickGroupViewBroadHolder(Fragment fragment, UserListBroadAdapter adapter, View itemView) {
        super(fragment, adapter, itemView);
    }

    @Override
    public void onBind(GroupBroadValue groupValue) {

    }
}
