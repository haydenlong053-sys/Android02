/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.contact.viewholder.header;

import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.cboda.chatui.*;
import com.cboda.chatui.contact.UserListAdapter;
import com.cboda.chatui.contact.model.FriendRequestValue;
import cn.wildfirechat.remote.ChatManager;

@SuppressWarnings("unused")
public class FriendRequestViewHolder extends HeaderViewHolder<FriendRequestValue> {
    TextView unreadRequestCountTextView;
    private FriendRequestValue value;


    public FriendRequestViewHolder(Fragment fragment, UserListAdapter adapter, View itemView) {
        super(fragment, adapter, itemView);
        bindViews(itemView);
    }

    private void bindViews(View itemView) {
        unreadRequestCountTextView = itemView.findViewById(R.id.unreadFriendRequestCountTextView);
    }

    @Override
    public void onBind(FriendRequestValue value) {
        this.value = value;
//        int count = ChatManager.Instance().getUnreadFriendRequestStatus();
        if (unreadRequestCountTextView != null){
            long count = ChatManager.Instance().haoyoushenqing;
            if (count > 0) {
                unreadRequestCountTextView.setVisibility(View.VISIBLE);
                if (count >99){
                    unreadRequestCountTextView.setText("99+");
                } else {
                    unreadRequestCountTextView.setText("" + count);
                }
            } else {
                unreadRequestCountTextView.setVisibility(View.GONE);
            }
        }

    }
}
