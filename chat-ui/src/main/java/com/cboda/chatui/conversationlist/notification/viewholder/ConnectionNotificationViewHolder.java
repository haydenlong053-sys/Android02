/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversationlist.notification.viewholder;

import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.cboda.chatui.R;
import com.cboda.chatui.annotation.StatusNotificationType;
import com.cboda.chatui.conversationlist.notification.ConnectionStatusNotification;
import com.cboda.chatui.conversationlist.notification.StatusNotification;

@StatusNotificationType(ConnectionStatusNotification.class)
public class ConnectionNotificationViewHolder extends StatusNotificationViewHolder {
    public ConnectionNotificationViewHolder(Fragment fragment) {
        super(fragment);
    }

    TextView statusTextView;

    @Override
    public void onBind(View view, StatusNotification notification) {
        String status = ((ConnectionStatusNotification) notification).getValue();
        statusTextView = view.findViewById(R.id.statusTextView);
        statusTextView.setText(status);
        statusTextView.setOnClickListener(this::onClick);
    }

    public void onClick(View view) {
//        Toast.makeText(fragment.getContext(), "status on Click", Toast.LENGTH_SHORT).show();
    }
}
