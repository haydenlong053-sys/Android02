/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.contact.viewholder.header;

import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.cboda.chatui.contact.UserListBroadAdapter;
import com.cboda.chatui.contact.model.HeaderBroadValue;

public abstract class HeaderViewBroadHolder<T extends HeaderBroadValue> extends RecyclerView.ViewHolder {
    protected Fragment fragment;
    protected UserListBroadAdapter adapter;

    public HeaderViewBroadHolder(Fragment fragment, UserListBroadAdapter adapter, View itemView) {
        super(itemView);
        this.fragment = fragment;
        this.adapter = adapter;
    }

    public abstract void onBind(T t);

}
