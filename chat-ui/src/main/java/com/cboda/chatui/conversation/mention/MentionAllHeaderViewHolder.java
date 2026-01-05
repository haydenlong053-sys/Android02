/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversation.mention;

import android.view.View;

import androidx.fragment.app.Fragment;

import com.cboda.chatui.contact.UserListAdapter;
import com.cboda.chatui.contact.model.HeaderValue;
import com.cboda.chatui.contact.viewholder.header.HeaderViewHolder;

public class MentionAllHeaderViewHolder extends HeaderViewHolder<HeaderValue> {
    public MentionAllHeaderViewHolder(Fragment fragment, UserListAdapter adapter, View itemView) {
        super(fragment, adapter, itemView);
    }

    @Override
    public void onBind(HeaderValue value) {

    }
}
