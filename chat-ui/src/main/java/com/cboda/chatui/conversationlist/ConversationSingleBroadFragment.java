/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversationlist;

import static com.cboda.baselibrary.utils.ELogUtilKt.APP_TAG;
import static com.cboda.baselibrary.utils.ELogUtilKt.eLogD;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.View;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.blankj.utilcode.util.ThreadUtils;
import com.cboda.baselibrary.bean.GlobalNoticeBroadBean;
import com.cboda.baselibrary.utils.MMKVBroadUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.cboda.chatui.BuildConfig;
import com.cboda.chatui.R;
import com.cboda.chatui.WfcUIBroadKit;
import com.cboda.chatui.conversation.ConversationBroadActivity;
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
import cn.wildfirechat.uistate.MessagePageUiState;
import cn.wildfirechat.viewmodel.HomeUIViewModel;
import cn.wildfirechat.viewmodel.SocialAction;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class ConversationSingleBroadFragment extends ProgressBroadFragment {
    private RecyclerView recyclerView;
    private RecyclerView mRvHead;
    private ConversationListBroadAdapter adapter;
    private static final List<Conversation.ConversationType> types = Arrays.asList(Conversation.ConversationType.Single);
    private static final List<Integer> lines = Arrays.asList(0);

    private ConversationListViewBroadModel conversationListViewModel;
    private SettingViewBroadModel settingViewModel;
    private StatusNotificationViewBroadModel statusNotificationViewModel;
    private LinearLayoutManager layoutManager;
    private OnClickConversationItemBroadListener onClickConversationItemListener;
    private List<UserInfo> lastUserInfo;

    private boolean isSyncUser = false;
    private MyHeaderBroadAdapter myHeaderAdapter;
    private GlobalNoticeBroadBean globalNoticeBean;
    private boolean isSelectMode = false;
    private HomeUIViewModel uiViewModel;


    @Override
    protected int contentLayout() {
        return R.layout.conversationlist_single_broad_frament;
    }

    @Override
    protected void afterViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        mRvHead = view.findViewById(R.id.mRvHead);
        init();
        // 直接这样写，最简洁
        uiViewModel = new ViewModelProvider(requireActivity()).get(HomeUIViewModel.class);
        uiViewModel.getUiStateLiveData().observe(getViewLifecycleOwner(), new Observer<MessagePageUiState>() {
            @Override
            public void onChanged(MessagePageUiState messagePageUiState) {

            }
        });

        // 监听 HomeUIViewModel 的 actionEvent（SharedFlow 的 LiveData 包装为 ConsumableEvent）
        uiViewModel.getActionEventLiveData().observe(getViewLifecycleOwner(), socialAction -> {
            if (socialAction == null) return;
            if (!isResumed()) return;
            if (adapter.getData().isEmpty()) return;

            if (socialAction instanceof SocialAction.SelectAll) {
                // 在此处理全选逻辑
                adapter.selectConversationByList(adapter.getData());
                uiViewModel.updateBottomBtnState(true, true, true);
            } else if (socialAction instanceof SocialAction.CancelSelectAll) {
                // 在此处理取消全选逻辑
                adapter.cancelSelectAll();
                uiViewModel.updateBottomBtnState(false, true, false);
            } else if (socialAction instanceof SocialAction.MarkAllAsRead) {
                // 在此处理全部已读逻辑
                changeOnlyThisMode(false);
                adapter.selectConversationByList(adapter.getData());
                clearUnRead();
            } else if (socialAction instanceof SocialAction.MarkSingleAsRead) {
                changeOnlyThisMode(false);
                clearUnRead();
            } else if (socialAction instanceof SocialAction.DeleteSelected) {
                // 在此处理删除已选逻辑
                changeOnlyThisMode(false);
                deleteConversations();
            } else if (socialAction instanceof SocialAction.CloseSelect) {
                adapter.closeSelect();
                uiViewModel.updateBottomBtnState(false, true, false);
            }
        });

        if (adapter != null) {
            adapter.setOnClickConversationItemListener(new OnClickConversationItemBroadListener() {
                @Override
                public void onClickConversationItem(ConversationInfo conversationInfo) {

                }

                @Override
                public void onClickConversationItemNew(ConversationInfo conversationInfo, int position) {
                    if (isSelectMode) {
                        if (adapter.getData().size() > position && position >= 0) {
                            // 传递数据项与适配器位置，避免越界并确保刷新正确的行
                            adapter.selectConversation(adapter.getData().get(position), position);
                            boolean isSelectAll = adapter.getData().size() == adapter.getSelectInfos().size();
                            boolean isAllRead = (adapter.getData().size() == adapter.getSelectInfos().size()) || adapter.getSelectInfos().isEmpty();
                            boolean enableDelete = !adapter.getSelectInfos().isEmpty();
                            uiViewModel.updateBottomBtnState(isSelectAll, isAllRead, enableDelete);
                            if (adapter.getSelectInfos().isEmpty()) {
                                //如果选择是空 则代表可以点击全已读 传全部数据过去让点击事件获取全部会话
                                uiViewModel.updateSelectConversations(adapter.getData());

                            } else {
                                //如果不是空 则传真实的
                                uiViewModel.updateSelectConversations(adapter.getSelectInfos());
                            }
                        }
                    } else {
                        // 点击会话
                        Intent intent = new Intent(getActivity(), ConversationBroadActivity.class);
                        intent.putExtra("conversation", conversationInfo.conversation);
                        startActivity(intent);
                    }

                }
            });
        }
    }

    public void setGlobalNoticeBean(GlobalNoticeBroadBean mNoticeBean) {
        this.globalNoticeBean = mNoticeBean;
        if (BuildConfig.DEBUG) {
            eLogD(APP_TAG, "--setGlobalNoticeBean---globalNoticeBean==" + globalNoticeBean.toString() + "---myHeaderAdapter==" + myHeaderAdapter);
        }
        if (myHeaderAdapter != null) {
            myHeaderAdapter.refreshHeaderData(globalNoticeBean);
        }
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
//        HashMap<String, GlobalFriendBroadBean> friendListData = MMKVBroadUtils.Companion.getFriendListData();
//        GlobalBroadInfo.INSTANCE.setMyFriendListBean(friendListData);
//        ELogUtilKt.eLogD(APP_TAG,"mList.size===="+GlobalBroadInfo.INSTANCE.getMyFriendListBean().size());
//        ELogUtilKt.eLogD(APP_TAG,"mList====="+GlobalBroadInfo.INSTANCE.getMyFriendListBean());

        UserViewBroadModel userViewModel = new ViewModelProvider(this).get(UserViewBroadModel.class);
        adapter = new ConversationListBroadAdapter(this, false);
        myHeaderAdapter = new MyHeaderBroadAdapter(globalNoticeBean, new Function1<GlobalNoticeBroadBean, Unit>() {
            @Override
            public Unit invoke(GlobalNoticeBroadBean globalNoticeBean) {
                EventBus.getDefault().post(globalNoticeBean);
                return null;
            }
        });
        if (onClickConversationItemListener != null) {
            adapter.setOnClickConversationItemListener(onClickConversationItemListener);
        }
        conversationListViewModel = new ViewModelProvider(this, new ConversationListViewModelBroadFactory(types, lines))
                .get(ConversationListViewBroadModel.class);
        conversationListViewModel.conversationListLiveData().observe(this, conversationInfos -> {
            showContent();
//            unreadSingleCount = 0;
//            unreadGroupCount = 0;
//            for (int i = 0; i < conversationInfos.size(); i++) {
//                if (conversationInfos.get(i).conversation.type == Conversation.ConversationType.Single) {
//                    unreadSingleCount += conversationInfos.get(i).unreadCount.unread;
//                }
//                if (conversationInfos.get(i).conversation.type == Conversation.ConversationType.Group) {
//                    unreadGroupCount += conversationInfos.get(i).unreadCount.unread;
//                }
//            }
//            EventBus.getDefault().post(new UnreadCountBroadEvent(null, unreadSingleCount + "", unreadGroupCount + "", null));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                List<ConversationInfo> collect = conversationInfos.stream().filter(
                        conversationInfo -> {
                            try {
                                return ChatManager.mClient.isMyFriend(conversationInfo.conversation.target) || TextUtils.equals(conversationInfo.conversation.target, MMKVBroadUtils.Companion.getKeFuId());
                            } catch (RemoteException e) {
                                return false;
                            }
                        }
                ).collect(Collectors.toList());
                adapter.setConversationInfos(collect);
            } else {
                adapter.setConversationInfos(conversationInfos);
            }
            List<String> conversationUserInfos = adapter.getConversationUserInfos();
            if (!isSyncUser) {

                userViewModel.getSyncUserInfos(conversationUserInfos, "");
            }


        });
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        mRvHead.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRvHead.setNestedScrollingEnabled(false);

        recyclerView.setAdapter(adapter);
        mRvHead.setAdapter(myHeaderAdapter);
//        recyclerView.setAdapter(new ConcatAdapter(myHeaderAdapter, adapter));
        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        userViewModel.syncUserInfoLiveData().observe(this, userInfos -> {

            if (lastUserInfo == null) {
                lastUserInfo = new ArrayList<>();
            }
            if (userInfos == null || userInfos.equals(lastUserInfo)) {
                return; // 数据没变，不执行
            }
            lastUserInfo = new ArrayList<>(userInfos);

            ChatManager.Instance().getMainHandler().postDelayed(this::reloadConversations, 200);

        });


        userViewModel.userInfoLiveData().observe(this, new Observer<List<UserInfo>>() {
            @Override
            public void onChanged(List<UserInfo> userInfos) {
                int start = layoutManager.findFirstVisibleItemPosition();
                int end = layoutManager.findLastVisibleItemPosition();
                ThreadUtils.runOnUiThread(() -> {
                    adapter.notifyItemRangeChanged(start, end - start + 1);
                });
            }
        });

        GroupViewBroadModel groupViewModel = new ViewModelProvider(this).get(GroupViewBroadModel.class);
        groupViewModel.groupInfoUpdateLiveData().observe(this, new Observer<List<GroupInfo>>() {
            @Override
            public void onChanged(List<GroupInfo> groupInfos) {
                int start = layoutManager.findFirstVisibleItemPosition();
                int end = layoutManager.findLastVisibleItemPosition();
                ThreadUtils.runOnUiThread(() -> {
                    adapter.notifyItemRangeChanged(start, end - start + 1);
                });
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
            eLogD(this.getClass().getSimpleName(), "getGroupInfos67k");
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

    private void reloadConversations() {
        if (ChatManager.Instance().getConnectionStatus() == ConnectionStatus.ConnectionStatusReceiveing) {
            return;
        }
        conversationListViewModel.reloadConversationList();
        eLogD(this.getClass().getSimpleName(), "getGroupInfos67p");
        conversationListViewModel.reloadConversationUnreadStatus();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void updateUIMode(String tag, boolean selectModel) {
        if (!tag.equals("single")) {
            return;
        }
        changeOnlyThisMode(selectModel);


    }

    private void changeOnlyThisMode(boolean selectModel) {
        this.isSelectMode = selectModel;
        adapter.updateSelectMode(selectModel);
    }

    /**
     * 标记已读
     */
    private void clearUnRead() {
        if (adapter == null) return;
        List<ConversationInfo> selectInfos = adapter.getSelectInfos();
        for (ConversationInfo selectInfo : selectInfos) {
            conversationListViewModel.clearConversationUnread(selectInfo);
        }
        ChatManager.Instance().getMainHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                uiViewModel.closeSelect();
            }
        }, 200);
    }

    private void deleteConversations() {
        if (adapter == null) return;
        List<ConversationInfo> selectInfos = adapter.getSelectInfos();
        for (ConversationInfo selectInfo : selectInfos) {
            conversationListViewModel.removeConversation(selectInfo, false);
        }
        ChatManager.Instance().getMainHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                uiViewModel.closeSelect();
            }
        }, 200);
    }
}
