/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversation.mention;

import android.view.View;

import androidx.fragment.app.Fragment;

import com.cboda.chatui.contact.UserListBroadAdapter;
import com.cboda.chatui.contact.model.HeaderBroadValue;
import com.cboda.chatui.contact.viewholder.header.HeaderViewBroadHolder;

public class MentionAllHeaderViewBroadHolder extends HeaderViewBroadHolder<HeaderBroadValue> {
    public MentionAllHeaderViewBroadHolder(Fragment fragment, UserListBroadAdapter adapter, View itemView) {
        super(fragment, adapter, itemView);
    }

    @Override
    public void onBind(HeaderBroadValue value) {

    }
}
