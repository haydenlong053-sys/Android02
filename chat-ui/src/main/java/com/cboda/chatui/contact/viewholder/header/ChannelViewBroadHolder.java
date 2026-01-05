/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.contact.viewholder.header;

import android.view.View;

import androidx.fragment.app.Fragment;

import com.cboda.chatui.contact.UserListBroadAdapter;
import com.cboda.chatui.contact.model.HeaderBroadValue;

@SuppressWarnings("unused")
public class ChannelViewBroadHolder extends HeaderViewBroadHolder<HeaderBroadValue> {

    public ChannelViewBroadHolder(Fragment fragment, UserListBroadAdapter adapter, View itemView) {
        super(fragment, adapter, itemView);
    }

    @Override
    public void onBind(HeaderBroadValue headerValue) {

    }
}
