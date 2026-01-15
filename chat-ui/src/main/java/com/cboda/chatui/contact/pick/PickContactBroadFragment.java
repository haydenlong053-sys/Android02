/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.contact.pick;

import androidx.lifecycle.ViewModelProvider;

import com.cboda.chatui.contact.ContactViewBroadModel;

public class PickContactBroadFragment extends PickUserBroadFragment {
    @Override
    protected void setupPickFromUsers() {
        ContactViewBroadModel contactViewModel = new ViewModelProvider(getActivity()).get(ContactViewBroadModel.class);
        contactViewModel.contactListLiveData().observe(this, userInfos -> {
            showContent();
            pickUserViewModel.setUsers(userInfos);
            userListAdapter.setUsers(userInfos);
        });
    }
}
