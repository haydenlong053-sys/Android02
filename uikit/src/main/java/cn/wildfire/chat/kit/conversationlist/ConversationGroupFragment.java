/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.kit.conversationlist;

import static com.cboda.baselibrary.utils.ELogUtilKt.eLogD;
import static com.cboda.baselibrary.utils.GsonUtilsKt.toJson2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.view.View;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.cboda.baselibrary.bean.GlobalBroadInfo;
import com.cboda.baselibrary.utils.MMKVBroadUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.WfcUIKit;
import cn.wildfire.chat.kit.conversation.ConversationActivity;
import cn.wildfire.chat.kit.conversationlist.notification.ConnectionStatusNotification;
import cn.wildfire.chat.kit.conversationlist.notification.PCOnlineStatusNotification;
import cn.wildfire.chat.kit.conversationlist.notification.StatusNotificationViewModel;
import cn.wildfire.chat.kit.group.GroupViewModel;
import cn.wildfire.chat.kit.user.UserViewModel;
import cn.wildfire.chat.kit.viewmodel.SettingViewModel;
import cn.wildfire.chat.kit.widget.ProgressFragment;
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

public class ConversationGroupFragment extends ProgressFragment {
    private RecyclerView recyclerView;
    private ConversationListAdapter adapter;
    private static final List<Conversation.ConversationType> types = Arrays.asList(Conversation.ConversationType.Group);
    private static final List<Integer> lines = Arrays.asList(0);

    private ConversationListViewModel conversationListViewModel;
    private SettingViewModel settingViewModel;
    private StatusNotificationViewModel statusNotificationViewModel;
    private LinearLayoutManager layoutManager;
    private OnClickConversationItemListener onClickConversationItemListener;

    private boolean isSelectMode = false;
    private HomeUIViewModel uiViewModel;

    @Override
    protected int contentLayout() {
        return R.layout.conversationlist_frament;
    }

    @Override
    protected void afterViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
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
            adapter.setOnClickConversationItemListener(new OnClickConversationItemListener() {
                @Override
                public void onClickConversationItem(ConversationInfo conversationInfo) {

                }

                @Override
                public void onClickConversationItemNew(ConversationInfo conversationInfo, int position) {
                    if (isSelectMode) {

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


                    } else {
                        // 点击会话
                        Intent intent = new Intent(getActivity(), ConversationActivity.class);
                        intent.putExtra("conversation", conversationInfo.conversation);
                        startActivity(intent);
                    }

                }
            });
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (adapter != null && isVisibleToUser) {
            reloadConversations();
        }
    }

    public void setOnClickConversationItemListener(OnClickConversationItemListener listener) {
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

    private void init() {
        adapter = new ConversationListAdapter(this, false);
        if (onClickConversationItemListener != null) {
            adapter.setOnClickConversationItemListener(onClickConversationItemListener);
        }
        conversationListViewModel = new ViewModelProvider(this, new ConversationListViewModelFactory(types, lines))
                .get(ConversationListViewModel.class);
        conversationListViewModel.conversationListLiveData().observe(this, conversationInfos -> {
            showContent();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                List<ConversationInfo> collect = conversationInfos.stream().filter(
                        conversationInfo -> {
                            try {
                                GroupInfo groupInfo = ChatManager.Instance().getGroupInfo(conversationInfo.conversation.target, false);
                                String myUserId = ChatManager.Instance().getUserId();
                                return groupInfo != null && groupInfo.owner.equals(myUserId);
                            } catch (Exception e) {
                                return false;
                            }
                        }
                ).collect(Collectors.toList());
                adapter.setConversationInfos(collect);
            } else {
                adapter.setConversationInfos(conversationInfos);
            }
        });
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setAdapter(adapter);
        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);

        UserViewModel userViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
        userViewModel.userInfoLiveData().observe(this, new Observer<List<UserInfo>>() {
            @Override
            public void onChanged(List<UserInfo> userInfos) {
                int start = layoutManager.findFirstVisibleItemPosition();
                int end = layoutManager.findLastVisibleItemPosition();
                adapter.notifyItemRangeChanged(start, end - start + 1);
            }
        });
        GroupViewModel groupViewModel = ViewModelProviders.of(this).get(GroupViewModel.class);
        groupViewModel.groupInfoUpdateLiveData().observe(this, new Observer<List<GroupInfo>>() {
            @Override
            public void onChanged(List<GroupInfo> groupInfos) {
                int start = layoutManager.findFirstVisibleItemPosition();
                int end = layoutManager.findLastVisibleItemPosition();
                adapter.notifyItemRangeChanged(start, end - start + 1);
            }
        });

        statusNotificationViewModel = WfcUIKit.getAppScopeViewModel(StatusNotificationViewModel.class);
        statusNotificationViewModel.statusNotificationLiveData().observe(this, new Observer<Object>() {
            @Override
            public void onChanged(Object o) {
                adapter.updateStatusNotification(statusNotificationViewModel.getNotificationItems());
            }
        });
        conversationListViewModel.connectionStatusLiveData().observe(this, status -> {
            ConnectionStatusNotification connectionStatusNotification = new ConnectionStatusNotification();
            switch (status) {
                case ConnectionStatus.ConnectionStatusConnecting:
                    connectionStatusNotification.setValue("正在连接...");
                    statusNotificationViewModel.showStatusNotification(connectionStatusNotification);
                    break;
                case ConnectionStatus.ConnectionStatusReceiveing:
                    connectionStatusNotification.setValue("正在同步...");
                    statusNotificationViewModel.showStatusNotification(connectionStatusNotification);
                    break;
                case ConnectionStatus.ConnectionStatusConnected:
                    statusNotificationViewModel.hideStatusNotification(connectionStatusNotification);
                    loadAndShowPCOnlineNotification();
                    break;
                case ConnectionStatus.ConnectionStatusUnconnected:
                    connectionStatusNotification.setValue("连接失败");
                    statusNotificationViewModel.showStatusNotification(connectionStatusNotification);
                    break;
                default:
                    break;
            }
        });
        settingViewModel = new ViewModelProvider(this).get(SettingViewModel.class);
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
        statusNotificationViewModel.clearStatusNotificationByType(PCOnlineStatusNotification.class);
        if (pcOnlineInfos != null && !pcOnlineInfos.isEmpty()) {
            for (PCOnlineInfo info : pcOnlineInfos) {
                PCOnlineStatusNotification notification = new PCOnlineStatusNotification(info);
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
        conversationListViewModel.reloadConversationUnreadStatus();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void updateUIMode(String tag, boolean selectModel) {
        if (!tag.equals("myGroup")) {
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
