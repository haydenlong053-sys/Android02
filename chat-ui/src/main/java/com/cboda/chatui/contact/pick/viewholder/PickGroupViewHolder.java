/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.contact.pick.viewholder;

import android.view.View;

import androidx.fragment.app.Fragment;

import com.cboda.chatui.contact.UserListAdapter;
import com.cboda.chatui.contact.model.GroupValue;
import com.cboda.chatui.contact.viewholder.header.HeaderViewHolder;

@SuppressWarnings("unused")
public class PickGroupViewHolder extends HeaderViewHolder<GroupValue> {

    public PickGroupViewHolder(Fragment fragment, UserListAdapter adapter, View itemView) {
        super(fragment, adapter, itemView);
    }

    @Override
    public void onBind(GroupValue groupValue) {

    }
}
