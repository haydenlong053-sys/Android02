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

import com.cboda.chatui.R;
import com.cboda.chatui.contact.model.UIUserBroadInfo;
import com.cboda.chatui.contact.pick.viewholder.CheckableUserBlackViewBroadHolder;
import com.cboda.chatui.contact.pick.viewholder.CheckableUserViewBroadHolder;

public class CheckableUserListBlackBroadAdapter extends CheckableUserListBroadAdapter {

    public CheckableUserListBlackBroadAdapter(Fragment fragment) {
        super(fragment);
    }

    @Override
    protected RecyclerView.ViewHolder onCreateContactViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView;
        itemView = LayoutInflater.from(fragment.getActivity()).inflate(R.layout.contact_item_contact_black, parent, false);
        CheckableUserViewBroadHolder viewHolder = new CheckableUserBlackViewBroadHolder(fragment, this, itemView);

        itemView.setOnClickListener(v -> {
            UIUserBroadInfo userInfo = viewHolder.getBindContact();
            if (onUserClickListener != null) {
                onUserClickListener.onUserClick(userInfo);
            }
        });
        return viewHolder;
    }
}
