/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.group.manage;

import android.view.View;

import androidx.fragment.app.Fragment;

import com.cboda.chatui.contact.UserListBroadAdapter;
import com.cboda.chatui.contact.model.FooterBroadValue;
import com.cboda.chatui.contact.viewholder.footer.FooterViewBroadHolder;

public class AddGroupManagerViewBroadHolder extends FooterViewBroadHolder<FooterBroadValue> {

    public AddGroupManagerViewBroadHolder(Fragment fragment, UserListBroadAdapter adapter, View itemView) {
        super(fragment, adapter, itemView);
    }

    @Override
    public void onBind(FooterBroadValue footerValue) {
        // do nothing
    }
}
