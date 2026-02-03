/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversationlist;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.cboda.baselibrary.event.DoubleMessageBroadEvent;
import com.cboda.baselibrary.event.UnreadCountBroadEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.cboda.chatui.R;
import com.cboda.chatui.WfcUIBroadKit;
import com.cboda.chatui.conversationlist.notification.ConnectionStatusBroadNotification;
import com.cboda.chatui.conversationlist.notification.PCOnlineStatusBroadNotification;
import com.cboda.chatui.conversationlist.notification.StatusNotificationViewBroadModel;
import com.cboda.chatui.group.GroupViewBroadModel;
import com.cboda.chatui.user.UserViewBroadModel;
import com.cboda.chatui.viewmodel.SettingViewBroadModel;
import com.cboda.chatui.widget.ProgressBroadFragment;
import cn.wildfirechat.client.ConnectionStatus;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.ConversationInfo;
import cn.wildfirechat.model.GroupInfo;
import cn.wildfirechat.model.PCOnlineInfo;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.remote.ChatManager;

public class ConversationListBroadFragment extends ProgressBroadFragment {
    private RecyclerView recyclerView;
    private ConversationListBroadAdapter adapter;
    private static final List<Conversation.ConversationType> types = Arrays.asList(Conversation.ConversationType.Single,
            Conversation.ConversationType.Group,
            Conversation.ConversationType.Channel,
            Conversation.ConversationType.SecretChat);
    private static final List<Integer> lines = Arrays.asList(0);

    private ConversationListViewBroadModel conversationListViewModel;
    private SettingViewBroadModel settingViewModel;
    private StatusNotificationViewBroadModel statusNotificationViewModel;
    private LinearLayoutManager layoutManager;
    private OnClickConversationItemBroadListener onClickConversationItemListener;

    List<ConversationInfo> mConversationList = new ArrayList<>();

    @Override
    protected int contentLayout() {
        return R.layout.conversationlist_broad_frament;
    }

    @Override
    protected void afterViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        EventBus.getDefault().register(this);
        init();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (adapter != null && isVisibleToUser) {
            reloadConversations();
        }
    }

    public void setOnClickConversationItemListener(OnClickConversationItemBroadListener listener) {
        this.onClickConversationItemListener = listener;
        if (adapter != null) {
            adapter.setOnClickConversationItemListener(listener);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
//        reloadConversations();
    }

    private int unreadSingleCount;
    private int unreadGroupCount;

    private void init() {
        adapter = new ConversationListBroadAdapter(this,false);
        if (onClickConversationItemListener != null) {
            adapter.setOnClickConversationItemListener(onClickConversationItemListener);
        }
        conversationListViewModel = new ViewModelProvider(this, new ConversationListViewModelBroadFactory(types, lines))
                .get(ConversationListViewBroadModel.class);
        conversationListViewModel.conversationListLiveData().observe(this, conversationInfos -> {
            showContent();
            unreadSingleCount = 0;
            unreadGroupCount = 0;
            for (int i = 0; i < conversationInfos.size(); i++) {
                if (conversationInfos.get(i).conversation.type == Conversation.ConversationType.Single) {
                    unreadSingleCount += conversationInfos.get(i).unreadCount.unread;
                }
                if (conversationInfos.get(i).conversation.type == Conversation.ConversationType.Group) {
                    unreadGroupCount += conversationInfos.get(i).unreadCount.unread;
                }
            }
            EventBus.getDefault().post(new UnreadCountBroadEvent(null, unreadSingleCount + "", unreadGroupCount + "", null));
            adapter.setConversationInfos(conversationInfos);

            mConversationList.clear();
            mConversationList.addAll(conversationInfos);
        });
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setAdapter(adapter);
        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);

        UserViewBroadModel userViewModel = new ViewModelProvider(this).get(UserViewBroadModel.class);
        userViewModel.userInfoLiveData().observe(this, new Observer<List<UserInfo>>() {
            @Override
            public void onChanged(List<UserInfo> userInfos) {
                int start = layoutManager.findFirstVisibleItemPosition();
                int end = layoutManager.findLastVisibleItemPosition();
                adapter.notifyItemRangeChanged(start, end - start + 1);
            }
        });
        GroupViewBroadModel groupViewModel = new ViewModelProvider(this).get(GroupViewBroadModel.class);
        groupViewModel.groupInfoUpdateLiveData().observe(this, new Observer<List<GroupInfo>>() {
            @Override
            public void onChanged(List<GroupInfo> groupInfos) {
                int start = layoutManager.findFirstVisibleItemPosition();
                int end = layoutManager.findLastVisibleItemPosition();
                adapter.notifyItemRangeChanged(start, end - start + 1);
            }
        });

        statusNotificationViewModel = WfcUIBroadKit.getAppScopeViewModel(StatusNotificationViewBroadModel.class);
        statusNotificationViewModel.statusNotificationLiveData().observe(this, new Observer<Object>() {
            @Override
            public void onChanged(Object o) {
                adapter.updateStatusNotification(statusNotificationViewModel.getNotificationItems());
            }
        });
        conversationListViewModel.connectionStatusLiveData().observe(this, status -> {
            ConnectionStatusBroadNotification connectionStatusNotification = new ConnectionStatusBroadNotification();
            switch (status) {
                case ConnectionStatus.ConnectionStatusConnecting:
                    connectionStatusNotification.setValue(getString(R.string.connection_status_connecting));
                    statusNotificationViewModel.showStatusNotification(connectionStatusNotification);
                    break;
                case ConnectionStatus.ConnectionStatusReceiveing:
                    connectionStatusNotification.setValue(getString(R.string.connection_status_syncing));
                    statusNotificationViewModel.showStatusNotification(connectionStatusNotification);
                    break;
                case ConnectionStatus.ConnectionStatusConnected:
                    statusNotificationViewModel.hideStatusNotification(connectionStatusNotification);
                    loadAndShowPCOnlineNotification();
                    break;
                case ConnectionStatus.ConnectionStatusUnconnected:
                    connectionStatusNotification.setValue(getString(R.string.connection_status_failed));
                    statusNotificationViewModel.showStatusNotification(connectionStatusNotification);
                    break;
                default:
                    break;
            }
        });
        settingViewModel = new ViewModelProvider(this).get(SettingViewBroadModel.class);
        settingViewModel.settingUpdatedLiveData().observe(this, o -> {
            if (ChatManager.Instance().getConnectionStatus() == ConnectionStatus.ConnectionStatusReceiveing) {
                return;
            }
            conversationListViewModel.reloadConversationList(true);
            conversationListViewModel.reloadConversationUnreadStatus();

            loadAndShowPCOnlineNotification();
        });

        if (ChatManager.Instance().getConnectionStatus() == ConnectionStatus.ConnectionStatusConnected) {
            loadAndShowPCOnlineNotification();
        }
    }

    private void loadAndShowPCOnlineNotification() {
        List<PCOnlineInfo> pcOnlineInfos = ChatManager.Instance().getPCOnlineInfos();
        statusNotificationViewModel.clearStatusNotificationByType(PCOnlineStatusBroadNotification.class);
        if (pcOnlineInfos != null && !pcOnlineInfos.isEmpty()) {
            for (PCOnlineInfo info : pcOnlineInfos) {
                PCOnlineStatusBroadNotification notification = new PCOnlineStatusBroadNotification(info);
                statusNotificationViewModel.showStatusNotification(notification);

                SharedPreferences sp = getActivity().getSharedPreferences("wfc_kit_config", Context.MODE_PRIVATE);
                sp.edit().putBoolean("wfc_uikit_had_pc_session", true).commit();
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(DoubleMessageBroadEvent event) {
        for (int i = 0; i < mConversationList.size(); i++) {
            if (mConversationList.get(i).unreadCount.unread > 0) {
//                recyclerView.smoothScrollToPosition(i);
                layoutManager.scrollToPositionWithOffset(i, 0);
                return;
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void reloadConversations() {
        if (ChatManager.Instance().getConnectionStatus() == ConnectionStatus.ConnectionStatusReceiveing) {
            return;
        }
        conversationListViewModel.reloadConversationList();
        conversationListViewModel.reloadConversationUnreadStatus();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

}
