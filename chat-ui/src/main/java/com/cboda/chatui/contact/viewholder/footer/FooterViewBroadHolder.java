/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.contact.viewholder.footer;

import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.cboda.chatui.contact.ContactViewBroadModel;
import com.cboda.chatui.contact.UserListBroadAdapter;
import com.cboda.chatui.contact.model.FooterBroadValue;

public abstract class FooterViewBroadHolder<T extends FooterBroadValue> extends RecyclerView.ViewHolder {
    protected Fragment fragment;
    protected UserListBroadAdapter adapter;
    protected ContactViewBroadModel contactViewModel;

    public FooterViewBroadHolder(Fragment fragment, UserListBroadAdapter adapter, View itemView) {
        super(itemView);
        this.fragment = fragment;
        this.adapter = adapter;
        contactViewModel = new ViewModelProvider(fragment).get(ContactViewBroadModel.class);
    }


    public abstract void onBind(T t);

}
