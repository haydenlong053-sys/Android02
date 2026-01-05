/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversationlist.notification.viewholder;

import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.cboda.chatui.R;
import com.cboda.chatui.annotation.StatusNotificationBroadType;
import com.cboda.chatui.conversationlist.notification.ConnectionStatusBroadNotification;
import com.cboda.chatui.conversationlist.notification.StatusBroadNotification;

@StatusNotificationBroadType(ConnectionStatusBroadNotification.class)
public class ConnectionNotificationViewBroadHolder extends StatusNotificationViewBroadHolder {
    public ConnectionNotificationViewBroadHolder(Fragment fragment) {
        super(fragment);
    }

    TextView statusTextView;

    @Override
    public void onBind(View view, StatusBroadNotification notification) {
        String status = ((ConnectionStatusBroadNotification) notification).getValue();
        statusTextView = view.findViewById(R.id.statusTextView);
        statusTextView.setText(status);
        statusTextView.setOnClickListener(this::onClick);
    }

    public void onClick(View view) {
//        Toast.makeText(fragment.getContext(), "status on Click", Toast.LENGTH_SHORT).show();
    }
}
