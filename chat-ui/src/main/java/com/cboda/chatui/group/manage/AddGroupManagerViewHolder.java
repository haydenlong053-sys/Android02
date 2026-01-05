/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.group.manage;

import android.view.View;

import androidx.fragment.app.Fragment;

import com.cboda.chatui.contact.UserListAdapter;
import com.cboda.chatui.contact.model.FooterValue;
import com.cboda.chatui.contact.viewholder.footer.FooterViewHolder;

public class AddGroupManagerViewHolder extends FooterViewHolder<FooterValue> {

    public AddGroupManagerViewHolder(Fragment fragment, UserListAdapter adapter, View itemView) {
        super(fragment, adapter, itemView);
    }

    @Override
    public void onBind(FooterValue footerValue) {
        // do nothing
    }
}
