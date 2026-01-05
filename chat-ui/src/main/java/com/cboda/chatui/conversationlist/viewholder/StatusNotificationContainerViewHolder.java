/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversationlist.viewholder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import com.cboda.chatui.*;
import com.cboda.chatui.conversationlist.notification.StatusNotification;
import com.cboda.chatui.conversationlist.notification.StatusNotificationManager;
import com.cboda.chatui.conversationlist.notification.viewholder.StatusNotificationViewHolder;

public class StatusNotificationContainerViewHolder extends RecyclerView.ViewHolder {
    LinearLayout containerLayout;

    public StatusNotificationContainerViewHolder(View itemView) {
        super(itemView);
        bindViews(itemView);
    }

    private void bindViews(View itemView) {
        containerLayout = itemView.findViewById(R.id.notificationContainerLayout);
    }

    public void onBind(Fragment fragment, View itemView, List<StatusNotification> statusNotifications) {
        LayoutInflater layoutInflater = LayoutInflater.from(fragment.getContext());

        containerLayout.removeAllViews();
        StatusNotificationViewHolder statusNotificationViewHolder;
        View view;
        for (StatusNotification notification : statusNotifications) {
            try {
                Class<? extends StatusNotificationViewHolder> holderClass = StatusNotificationManager.getInstance().getNotificationViewHolder(notification);
                Constructor constructor = holderClass.getConstructor(Fragment.class);
                statusNotificationViewHolder = (StatusNotificationViewHolder) constructor.newInstance(fragment);
                view = layoutInflater.inflate(StatusNotificationManager.getInstance().getNotificationViewHolderLayoutResId(notification), (ViewGroup) itemView, false);

                statusNotificationViewHolder.onBind(view, notification);
                containerLayout.addView(view);
                // TODO add divider
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }
}
