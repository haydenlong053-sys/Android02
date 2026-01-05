/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.contact.pick;

import androidx.lifecycle.ViewModelProviders;

import com.cboda.chatui.contact.ContactViewModel;

public class PickContactFragment extends PickUserFragment {
    @Override
    protected void setupPickFromUsers() {
        ContactViewModel contactViewModel = ViewModelProviders.of(getActivity()).get(ContactViewModel.class);
        contactViewModel.contactListLiveData().observe(this, userInfos -> {
            showContent();
            pickUserViewModel.setUsers(userInfos);
            userListAdapter.setUsers(userInfos);
        });
    }
}
