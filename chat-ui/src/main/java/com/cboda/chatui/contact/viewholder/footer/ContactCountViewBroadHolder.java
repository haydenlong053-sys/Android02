/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.contact.viewholder.footer;

import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.cboda.chatui.*;
import com.cboda.chatui.contact.UserListBroadAdapter;
import com.cboda.chatui.contact.model.ContactCountFooterBroadValue;

public class ContactCountViewBroadHolder extends FooterViewBroadHolder<ContactCountFooterBroadValue> {
    TextView countTextView;
    private UserListBroadAdapter adapter;

    public ContactCountViewBroadHolder(Fragment fragment, UserListBroadAdapter adapter, View itemView) {
        super(fragment, adapter, itemView);
        this.adapter = adapter;
        bindViews(itemView);
    }

    private void bindViews(View itemView) {
        countTextView = itemView.findViewById(R.id.countTextView);
    }

    @Override
    public void onBind(ContactCountFooterBroadValue contactCountFooterValue) {
        int count = adapter.getContactCount();
        if (count == 0) {
            countTextView.setText("没有联系人");
        } else {
            countTextView.setText(count + "位联系人");
        }
    }
}
