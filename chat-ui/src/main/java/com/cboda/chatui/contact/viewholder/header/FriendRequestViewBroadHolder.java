/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.contact.viewholder.header;

import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.cboda.chatui.*;
import com.cboda.chatui.contact.UserListBroadAdapter;
import com.cboda.chatui.contact.model.FriendRequestBroadValue;
import cn.wildfirechat.remote.ChatManager;

@SuppressWarnings("unused")
public class FriendRequestViewBroadHolder extends HeaderViewBroadHolder<FriendRequestBroadValue> {
    TextView unreadRequestCountTextView;
    private FriendRequestBroadValue value;


    public FriendRequestViewBroadHolder(Fragment fragment, UserListBroadAdapter adapter, View itemView) {
        super(fragment, adapter, itemView);
        bindViews(itemView);
    }

    private void bindViews(View itemView) {
        unreadRequestCountTextView = itemView.findViewById(R.id.unreadFriendRequestCountTextView);
    }

    @Override
    public void onBind(FriendRequestBroadValue value) {
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
