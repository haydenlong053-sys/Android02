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
import com.cboda.chatui.conversationlist.notification.StatusBroadNotification;
import com.cboda.chatui.conversationlist.notification.StatusNotificationBroadManager;
import com.cboda.chatui.conversationlist.notification.viewholder.StatusNotificationViewBroadHolder;

public class StatusNotificationContainerViewBroadHolder extends RecyclerView.ViewHolder {
    LinearLayout containerLayout;

    public StatusNotificationContainerViewBroadHolder(View itemView) {
        super(itemView);
        bindViews(itemView);
    }

    private void bindViews(View itemView) {
        containerLayout = itemView.findViewById(R.id.notificationContainerLayout);
    }

    public void onBind(Fragment fragment, View itemView, List<StatusBroadNotification> statusNotifications) {
        if (null != statusNotifications) {
            LayoutInflater layoutInflater = LayoutInflater.from(fragment.getContext());

            containerLayout.removeAllViews();
            StatusNotificationViewBroadHolder statusNotificationViewHolder;
            View view;
            for (StatusBroadNotification notification : statusNotifications) {
                try {
                    Class<? extends StatusNotificationViewBroadHolder> holderClass = StatusNotificationBroadManager.getInstance().getNotificationViewHolder(notification);
                    Constructor constructor = holderClass.getConstructor(Fragment.class);
                    statusNotificationViewHolder = (StatusNotificationViewBroadHolder) constructor.newInstance(fragment);
                    view = layoutInflater.inflate(StatusNotificationBroadManager.getInstance().getNotificationViewHolderLayoutResId(notification), (ViewGroup) itemView, false);

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
}
