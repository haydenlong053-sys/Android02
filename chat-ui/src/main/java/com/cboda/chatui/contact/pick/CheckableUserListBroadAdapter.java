/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.contact.pick;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import com.cboda.chatui.R;
import com.cboda.chatui.contact.UserListBroadAdapter;
import com.cboda.chatui.contact.model.UIUserBroadInfo;
import com.cboda.chatui.contact.pick.viewholder.CheckableUserViewBroadHolder;

public class CheckableUserListBroadAdapter extends UserListBroadAdapter {
    private int maxCheckCount;

    public CheckableUserListBroadAdapter(Fragment fragment) {
        super(fragment);
    }

    public void setMaxCheckCount(int maxCheckCount) {
        this.maxCheckCount = maxCheckCount;
    }

    public int getMaxCheckCount() {
        return maxCheckCount;
    }


    public void updateUserStatus(UIUserBroadInfo userInfo) {
        if (users != null && userInfo != null) {
            for (int i = 0; i < users.size(); i++) {
                if (users.get(i).getUserInfo().uid.equals(userInfo.getUserInfo().uid)) {
                    users.set(i, userInfo);
                    notifyItemChanged(headerCount() + i);
                }
            }
        }
    }

    public List<UIUserBroadInfo> getCheckedContacts() {
        List<UIUserBroadInfo> checkedContacts = new ArrayList<>();
        if (users == null || users.isEmpty()) {
            return checkedContacts;
        }
        for (UIUserBroadInfo userInfo : users) {
            if (userInfo.isChecked()) {
                checkedContacts.add(userInfo);
            }
        }
        return checkedContacts;
    }

    @Override
    protected RecyclerView.ViewHolder onCreateContactViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView;
        itemView = LayoutInflater.from(fragment.getActivity()).inflate(R.layout.contact_item_contact, parent, false);
        CheckableUserViewBroadHolder viewHolder = new CheckableUserViewBroadHolder(fragment, this, itemView);

        itemView.setOnClickListener(v -> {
            UIUserBroadInfo userInfo = viewHolder.getBindContact();
            if (onUserClickListener != null) {
                onUserClickListener.onUserClick(userInfo);
                viewHolder.setChecked(userInfo.isChecked());
            }
        });
        return viewHolder;
    }
}
