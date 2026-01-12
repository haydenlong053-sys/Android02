/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversation;

import static com.cboda.baselibrary.utils.ELogUtilKt.eLogD;
import static com.cboda.baselibrary.utils.GsonUtilsKt.toJson2;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.cboda.baselibrary.bean.GlobalFriendBroadBean;
import com.cboda.baselibrary.bean.GlobalBroadInfo;
import com.cboda.baselibrary.event.FanYiBroadEvent;
import com.cboda.baselibrary.utils.ELogUtilKt;
import com.cboda.baselibrary.utils.MMKVBroadUtils;
import com.bumptech.glide.Glide;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.cboda.chatui.ChatManagerBroadHolder;
import com.cboda.chatui.MarqueeTextBroadView;
import com.cboda.chatui.R;
import com.cboda.chatui.WfcUIBroadKit;
import com.cboda.chatui.bridge.TouSuJiLuBroadEvent;
import com.cboda.chatui.bridge.MyCommonBroadBridge;
import com.cboda.chatui.bridge.MyBroadEvent;
import com.cboda.chatui.channel.ChannelViewBroadModel;
import com.cboda.chatui.chatroom.ChatRoomViewBroadModel;
import com.cboda.chatui.common.OperateBroadResult;
import com.cboda.chatui.conversation.ext.core.ConversationBroadExtension;
import com.cboda.chatui.conversation.mention.MentionBroadSpan;
import com.cboda.chatui.conversation.message.model.UiBroadMessage;
import com.cboda.chatui.conversation.multimsg.MultiMessageBroadAction;
import com.cboda.chatui.conversation.multimsg.MultiMessageActionBroadManager;
import com.cboda.chatui.conversation.receipt.GroupMessageReceiptBroadActivity;
import com.cboda.chatui.group.GroupViewBroadModel;
import com.cboda.chatui.group.PickGroupMemberBroadActivity;
import com.cboda.chatui.third.utils.UIBroadUtils;
import com.cboda.chatui.user.UserViewBroadModel;
import com.cboda.chatui.utils.DownloadBroadManager;
import com.cboda.chatui.viewmodel.MessageViewBroadModel;
import com.cboda.chatui.viewmodel.SettingViewBroadModel;
import com.cboda.chatui.viewmodel.UserOnlineStateViewBroadModel;
import com.cboda.chatui.widget.InputAwareBroadLayout;
import com.cboda.chatui.widget.KeyboardAwareLinearBroadLayout;

import cn.wildfirechat.avenginekit.AVEngineKit;
import cn.wildfirechat.client.IGetMessageCallback;
import cn.wildfirechat.message.EnterChannelChatMessageContent;
import cn.wildfirechat.message.LeaveChannelChatMessageContent;
import cn.wildfirechat.message.Message;
import cn.wildfirechat.message.MessageContent;
import cn.wildfirechat.message.MultiCallOngoingMessageContent;
import cn.wildfirechat.message.SoundMessageContent;
import cn.wildfirechat.message.TypingMessageContent;
import cn.wildfirechat.message.core.MessageDirection;
import cn.wildfirechat.message.core.MessageStatus;
import cn.wildfirechat.message.notification.RecallMessageContent;
import cn.wildfirechat.message.notification.TipNotificationContent;
import cn.wildfirechat.model.BurnMessageInfo;
import cn.wildfirechat.model.ChannelInfo;
import cn.wildfirechat.model.ChatRoomInfo;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.ConversationInfo;
import cn.wildfirechat.model.GroupInfo;
import cn.wildfirechat.model.GroupMember;
import cn.wildfirechat.model.NullUserInfo;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.model.UserOnlineState;
import cn.wildfirechat.remote.ChatManager;
import cn.wildfirechat.remote.GetMessageCallback;
import cn.wildfirechat.remote.UserSettingScope;

@SuppressLint("FragmentLiveDataObserve")
public class ConversationBroadFragment extends Fragment implements
        KeyboardAwareLinearBroadLayout.OnKeyboardShownListener,
        KeyboardAwareLinearBroadLayout.OnKeyboardHiddenListener,
        ConversationMessageBroadAdapter.OnPortraitClickListener,
        ConversationMessageBroadAdapter.OnPortraitLongClickListener,
        ConversationInputBroadPanel.OnConversationInputPanelStateChangeListener,
        ConversationMessageBroadAdapter.OnMessageCheckListener, ConversationMessageBroadAdapter.OnMessageReceiptClickListener {

    private static final String TAG = "convFragment";

    public static final int REQUEST_PICK_MENTION_CONTACT = 100;
    public static final int REQUEST_CODE_GROUP_VIDEO_CHAT = 101;
    public static final int REQUEST_CODE_GROUP_AUDIO_CHAT = 102;

    private Conversation conversation;
    private boolean loadingNewMessage;
    private boolean shouldContinueLoadNewMessage = false;
    private boolean isPreJoinedChatRoom = true;

    private static final int MESSAGE_LOAD_COUNT_PER_TIME = 20;
    private static final int MESSAGE_LOAD_AROUND = 10;
    private static final long TYPING_INTERNAL = 10 * 1000;

    InputAwareBroadLayout rootLinearLayout;
    SwipeRefreshLayout swipeRefreshLayout;
    RecyclerView recyclerView;

    RecyclerView ongoingCallRecyclerView;

    public ConversationInputBroadPanel inputPanel;
    public View mViewIntercept;
    public TextView tvPhoneLogin;

    LinearLayout multiMessageActionContainerLinearLayout;

    LinearLayout unreadCountLinearLayout;
    TextView unreadCountTextView;
    TextView unreadMentionCountTextView;

    private ConversationMessageBroadAdapter adapter;
    private OngoingCallBroadAdapter ongoingCallAdapter;
    private boolean moveToBottom = true;
    private ConversationViewBroadModel conversationViewModel;
    private SettingViewBroadModel settingViewModel;
    private UserOnlineStateViewBroadModel userOnlineStateViewModel;
    public MessageViewBroadModel messageViewModel;
    private UserViewBroadModel userViewModel;
    private GroupViewBroadModel groupViewModel;
    private ChatRoomViewBroadModel chatRoomViewModel;

    private Handler handler;
    private long initialFocusedMessageId;
    private long firstUnreadMessageId;
    // 用户channel主发起，针对某个用户的会话
    private String channelPrivateChatUser;
    private String conversationTitle = "";
    private LinearLayoutManager layoutManager;

    // for group
    private GroupInfo groupInfo;
    private GroupMember groupMember;
    private boolean showGroupMemberName = false;
    private Observer<List<GroupMember>> groupMembersUpdateLiveDataObserver;
    private Observer<List<GroupInfo>> groupInfosUpdateLiveDataObserver;
    private Observer<Object> settingUpdateLiveDataObserver;
    private Map<String, Message> ongoingCalls;

    private Map<String, Message> typingMessageMap;

    private String backgroundImageUri = null;
    private int backgroundImageResId = 0;
    public View qungonggao;
    public View qunMeetingNotice;
    public MarqueeTextBroadView gonggaoxinxi;
    public MarqueeTextBroadView tvQunMeeting;

    private Observer<UiBroadMessage> messageLiveDataObserver = new Observer<UiBroadMessage>() {
        @Override
        public void onChanged(@Nullable UiBroadMessage uiMessage) {
            if (!isMessageInCurrentConversation(uiMessage)) {
                return;
            }
            MessageContent content = uiMessage.message.content;

            if (content instanceof MultiCallOngoingMessageContent) {
                MultiCallOngoingMessageContent ongoingCall = (MultiCallOngoingMessageContent) content;
                AVEngineKit.CallSession callSession = AVEngineKit.Instance().getCurrentSession();
                if (ongoingCall.getInitiator().equals(ChatManager.Instance().getUserId())
                        || ongoingCall.getTargets().contains(ChatManager.Instance().getUserId())
                        || (callSession != null && callSession.getState() != AVEngineKit.CallState.Idle)) {
                    return;
                }

                if (ongoingCalls == null) {
                    ongoingCalls = new HashMap<>();
                }
                ongoingCalls.put(ongoingCall.getCallId(), uiMessage.message);

                if (ongoingCalls.size() > 0) {
                    ongoingCallRecyclerView.setVisibility(View.VISIBLE);
                    if (ongoingCallAdapter == null) {
                        ongoingCallAdapter = new OngoingCallBroadAdapter();
                        ongoingCallRecyclerView.setAdapter(ongoingCallAdapter);
                        ongoingCallRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                    }
                    ongoingCallAdapter.setOngoingCalls(new ArrayList<>(ongoingCalls.values()));
                } else {
                    ongoingCallRecyclerView.setVisibility(View.GONE);
                    ongoingCallAdapter.setOngoingCalls(null);
                }
                cleanExpiredOngoingCalls();

                return;
            }

            if (isDisplayableMessage(uiMessage) && !(content instanceof RecallMessageContent)) {
                // 消息定位时，如果收到新消息、或者发送消息，需要重新加载消息列表
                if (shouldContinueLoadNewMessage) {
                    shouldContinueLoadNewMessage = false;
                    eLogD("ConversationFragmentClass", "reloadMessage");
                    reloadMessage();
                    return;
                }
                adapter.addNewMessage(uiMessage);
                if (moveToBottom || uiMessage.message.sender.equals(ChatManager.Instance().getUserId())) {
                    UIBroadUtils.postTaskDelay(() -> {

                                int position = adapter.getItemCount() - 1;
                                if (position < 0) {
                                    return;
                                }
                                recyclerView.scrollToPosition(position);
                            },
                            100);
                }
            }
            if (content instanceof TypingMessageContent && uiMessage.message.direction == MessageDirection.Receive) {
                if (typingMessageMap == null) {
                    typingMessageMap = new HashMap<>();
                }
                long now = System.currentTimeMillis();
                if (now - uiMessage.message.serverTime + ChatManager.Instance().getServerDeltaTime() < TYPING_INTERNAL) {
                    typingMessageMap.put(uiMessage.message.sender, uiMessage.message);
                }
                updateTypingStatusTitle();
            } else {
                if (uiMessage.message.direction == MessageDirection.Receive && typingMessageMap != null) {
                    typingMessageMap.remove(uiMessage.message.sender);
                }
                updateTypingStatusTitle();
            }

            if (getLifecycle().getCurrentState() == Lifecycle.State.RESUMED && uiMessage.message.direction == MessageDirection.Receive) {
                conversationViewModel.clearUnreadStatus(conversation);
            }
        }
    };
    private Observer<UiBroadMessage> messageUpdateLiveDatObserver = new Observer<UiBroadMessage>() {
        @Override
        public void onChanged(@Nullable UiBroadMessage uiMessage) {
            // 聊天室，对方撤回消息
            if (conversation == null) {
                return;
            }
            if (conversation.type == Conversation.ConversationType.ChatRoom && uiMessage.message.conversation == null) {
                List<UiBroadMessage> messages = adapter.getMessages();
                for (UiBroadMessage uiMsg : messages) {
                    if (uiMsg.message.messageUid == uiMessage.message.messageUid) {
                        RecallMessageContent content = new RecallMessageContent(uiMsg.message.sender, uiMsg.message.messageUid);
                        content.setOriginalSender(uiMsg.message.sender);
                        uiMsg.message.content = content;
                        adapter.updateMessage(uiMsg);
                        break;
                    }
                }
            } else {
                if (!isMessageInCurrentConversation(uiMessage)) {
                    return;
                }
                if (isDisplayableMessage(uiMessage)) {
                    adapter.updateMessage(uiMessage);
                }
                if (uiMessage.progress == 100) {
                    uiMessage.progress = 0;
                    messageViewModel.playAudioMessage(uiMessage);
                }
                if (uiMessage.audioPlayCompleted) {
                    uiMessage.audioPlayCompleted = false;
                    if (uiMessage.continuousPlayAudio) {
                        uiMessage.continuousPlayAudio = false;
                        playNextAudioMessage(uiMessage);
                    }
                }
            }
        }
    };

    private void playNextAudioMessage(UiBroadMessage uiMessage) {
        List<UiBroadMessage> messages = adapter.getMessages();
        boolean found = false;
        UiBroadMessage toPlayAudioMessage = null;
        for (int i = 0; i < messages.size(); i++) {
            UiBroadMessage uimsg = messages.get(i);
            if (found) {
                if (uimsg.message.content instanceof SoundMessageContent
                        && uimsg.message.direction == MessageDirection.Receive
                        && uimsg.message.status != MessageStatus.Played) {
                    toPlayAudioMessage = uimsg;
                    break;
                }
            } else {
                if (uimsg.message.messageUid == uiMessage.message.messageUid) {
                    found = true;
                }
            }
        }
        if (toPlayAudioMessage != null) {
            File file = DownloadBroadManager.mediaMessageContentFile(toPlayAudioMessage.message);
            if (file == null) {
                return;
            }
            toPlayAudioMessage.continuousPlayAudio = true;
            if (file.exists()) {
                messageViewModel.playAudioMessage(toPlayAudioMessage);
            } else {
                messageViewModel.downloadMedia(toPlayAudioMessage, file);
            }
        }
    }

    private Observer<UiBroadMessage> messageRemovedLiveDataObserver = new Observer<UiBroadMessage>() {
        @Override
        public void onChanged(@Nullable UiBroadMessage uiMessage) {
            // 当通过server api删除消息时，只知道消息的uid
            if (uiMessage.message.conversation != null && !isMessageInCurrentConversation(uiMessage)) {
                return;
            }
            if (uiMessage.message.messageId == 0 || isDisplayableMessage(uiMessage)) {
                adapter.removeMessage(uiMessage);
            }
        }
    };

    private boolean isDisplayableMessage(UiBroadMessage uiMessage) {
        return uiMessage.message.messageId != 0;
    }

    private Observer<Pair<String, Long>> messageStartBurnLiveDataObserver = new Observer<Pair<String, Long>>() {
        @Override
        public void onChanged(@Nullable Pair<String, Long> pair) {
            // 当通过server api删除消息时，只知道消息的uid
//            if (uiMessage.message.conversation != null && !isMessageInCurrentConversation(uiMessage)) {
//                return;
//            }
//            if (uiMessage.message.messageId == 0 || isDisplayableMessage(uiMessage)) {
//                adapter.removeMessage(uiMessage);
//            }
//            adapter.removeMessage();
            // TODO
        }
    };

    private Observer<List<Long>> messageBurnedLiveDataObserver = new Observer<List<Long>>() {
        @Override
        public void onChanged(@Nullable List<Long> messageIds) {
            for (int i = 0; i < messageIds.size(); i++) {
                adapter.removeMessageById(messageIds.get(i));
            }
        }
    };

    private Observer<Conversation> clearConversationMessageObserver = new Observer<Conversation>() {
        @Override
        public void onChanged(Conversation conversation) {
            if (conversation.equals(ConversationBroadFragment.this.conversation)) {
                adapter.setMessages(null);
                adapter.notifyDataSetChanged();
            }
        }
    };

    private Observer<List<UserInfo>> userInfoUpdateLiveDataObserver = new Observer<List<UserInfo>>() {
        @Override
        public void onChanged(@Nullable List<UserInfo> userInfos) {
            if (conversation == null) {
                return;
            }
            if (conversation.type == Conversation.ConversationType.Single) {
                conversationTitle = null;
                setTitle();
            }
            int start = layoutManager.findFirstVisibleItemPosition();
            int end = layoutManager.findLastVisibleItemPosition();
            adapter.notifyItemRangeChanged(start, end - start + 1, userInfos);
        }
    };


    private Observer<Map<String, UserOnlineState>> userOnlineStateLiveDataObserver = new Observer<Map<String, UserOnlineState>>() {
        @Override
        public void onChanged(Map<String, UserOnlineState> userOnlineStateMap) {
            if (conversation == null) {
                return;
            }
            if (conversation.type == Conversation.ConversationType.Single) {
                conversationTitle = null;
                setTitle();
            }
        }
    };

    private void initGroupObservers() {
        groupMembersUpdateLiveDataObserver = groupMembers -> {
            if (groupMembers == null || groupInfo == null) {
                return;
            }
            for (GroupMember member : groupMembers) {
                if (member.groupId.equals(groupInfo.target) && member.memberId.equals(userViewModel.getUserId())) {
                    groupMember = member;
                    updateGroupMuteStatus();
                    break;
                }
            }
        };

        groupInfosUpdateLiveDataObserver = groupInfos -> {
            if (groupInfo == null || groupInfos == null) {
                return;
            }
            for (GroupInfo info : groupInfos) {
                if (info.target.equals(groupInfo.target)) {
                    groupInfo = info;
                    updateGroupMuteStatus();
                    setTitle();
                    adapter.notifyDataSetChanged();
                    break;
                }
            }
        };


        groupViewModel.groupInfoUpdateLiveData().observeForever(groupInfosUpdateLiveDataObserver);
        groupViewModel.groupMembersUpdateLiveData().observeForever(groupMembersUpdateLiveDataObserver);
    }

    private void unInitGroupObservers() {
        if (groupViewModel == null) {
            return;
        }
        groupViewModel.groupInfoUpdateLiveData().removeObserver(groupInfosUpdateLiveDataObserver);
        groupViewModel.groupMembersUpdateLiveData().removeObserver(groupMembersUpdateLiveDataObserver);
    }

    private boolean isMessageInCurrentConversation(UiBroadMessage message) {
        if (conversation == null || message == null || message.message == null) {
            return false;
        }
        return conversation.equals(message.message.conversation);
    }

    public ConversationInputBroadPanel getConversationInputPanel() {
        return inputPanel;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        eLogD("ConversationFragmentClass", "onCreate");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.conversation_broad_activity, container, false);
        bindViews(view);
        bindEvents(view);
        initView();
        EventBus.getDefault().register(this);
        return view;
    }

    private void bindEvents(View view) {
        view.findViewById(R.id.unreadCountTextView).setOnClickListener(_v -> onUnreadCountTextViewClick());
        view.findViewById(R.id.unreadMentionCountTextView).setOnClickListener(_v -> aitonUnreadCountTextViewClick());

    }

    public String fromPage;

    private void bindViews(View view) {
        gonggaoxinxi = view.findViewById(R.id.gonggaoxinxi);
        tvQunMeeting = view.findViewById(R.id.tvQunMeeting);
        qungonggao = view.findViewById(R.id.qungonggao);
        qunMeetingNotice = view.findViewById(R.id.qunMeetingNotice);
        rootLinearLayout = view.findViewById(R.id.rootLinearLayout);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        recyclerView = view.findViewById(R.id.msgRecyclerView);
        ongoingCallRecyclerView = view.findViewById(R.id.ongoingCallRecyclerView);
        tvPhoneLogin = view.findViewById(R.id.tvPhoneLogin);
        inputPanel = view.findViewById(R.id.inputPanelFrameLayout);
        mViewIntercept = view.findViewById(R.id.mViewIntercept);
        if ("tousu".equals(fromPage)) {
            tvPhoneLogin.setVisibility(View.VISIBLE);
            inputPanel.setVisibility(View.GONE);
            tvPhoneLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (adapter != null && adapter.getMessages() != null) {
                        List<UiBroadMessage> tousuMessages = new ArrayList<>();
                        for (int i = 0; i < adapter.getMessages().size(); i++) {
                            if (adapter.getMessages().get(i).isSelectTouSu) {
                                tousuMessages.add(adapter.getMessages().get(i));
                            }
                        }

                        if (!tousuMessages.isEmpty()) {
                            EventBus.getDefault().post(new TouSuJiLuBroadEvent(tousuMessages));
                            getActivity().finish();
                        }
                    }
                }
            });
        }
        multiMessageActionContainerLinearLayout = view.findViewById(R.id.multiMessageActionContainerLinearLayout);
        unreadCountLinearLayout = view.findViewById(R.id.unreadCountLinearLayout);
        unreadCountTextView = view.findViewById(R.id.unreadCountTextView);
        unreadMentionCountTextView = view.findViewById(R.id.unreadMentionCountTextView);

        view.findViewById(R.id.contentLayout).setOnTouchListener((v, event) -> ConversationBroadFragment.this.onTouch(v, event));
        recyclerView.setOnTouchListener((v, event) -> ConversationBroadFragment.this.onTouch(v, event));
        gonggaoxinxi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (conversation != null) {
                    MyCommonBroadBridge.getInstance().setMyQiaoJie(getContext(), "chakanqungonggao", conversation.target, gonggaoxinxi.getText().toString(), null);
                }
            }
        });


    }

    @Override
    public void onResume() {
        super.onResume();
        if (conversationViewModel != null && conversation != null) {
            conversationViewModel.clearUnreadStatus(conversation);
        }
    }

    public void setupConversation(Conversation conversation, String title, long focusMessageId, String target) {
        setupConversation(conversation, title, focusMessageId, target, false);
    }

    public void setupConversation(Conversation conversation, String title, long focusMessageId, String target, boolean isJoinedChatRoom) {
        if (this.conversation != null) {
            if ((this.conversation.type == Conversation.ConversationType.Single && !ChatManager.Instance().isMyFriend(this.conversation.target))
                    || this.conversation.type == Conversation.ConversationType.Group) {
                userOnlineStateViewModel.unwatchOnlineState(this.conversation.type.getValue(), new String[]{this.conversation.target});
            }
            this.adapter = new ConversationMessageBroadAdapter(this);
            if ("tousu".equals(fromPage)) {
                this.adapter.istousu = "1";
            }
            this.recyclerView.setAdapter(this.adapter);
        }

        // TODO 要支持在线状态是才监听
        if ((conversation.type == Conversation.ConversationType.Single && !ChatManager.Instance().isMyFriend(conversation.target))
                || conversation.type == Conversation.ConversationType.Group) {
            userOnlineStateViewModel.watchUserOnlineState(conversation.type.getValue(), new String[]{conversation.target});
        }

        this.conversation = conversation;
        this.conversationTitle = title;
        this.initialFocusedMessageId = focusMessageId;
        this.channelPrivateChatUser = target;
        this.isPreJoinedChatRoom = isJoinedChatRoom;
        setupConversation(conversation);

        if (conversation != null && conversation.type == Conversation.ConversationType.Group) {
            //获取最后一次时间
            ChatManager.Instance().getMessages(conversation, 0, true, 1, ChatManager.Instance().getUserId(), new GetMessageCallback() {
                @Override
                public void onSuccess(List<Message> messages, boolean hasMore) {
                    if (messages != null && !messages.isEmpty()) {
                        Message message = messages.get(0);
                        if (!(message.content instanceof TypingMessageContent)) {
                            eLogD("野火", "获取最后一次发言时间--> " + message.serverTime);
                            ChatManager.Instance().shangcishijan = message.serverTime;
                        }

                    }
                }

                @Override
                public void onFail(int errorCode) {

                }
            });

        }

    }

    private void initView() {
        handler = new Handler();
        rootLinearLayout.addOnKeyboardShownListener(this);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (adapter.getMessages() == null || adapter.getMessages().isEmpty()) {
                swipeRefreshLayout.setRefreshing(false);
                return;
            }
            loadMoreOldMessages();
        });

        // message list
        adapter = new ConversationMessageBroadAdapter(this);
        if ("tousu".equals(fromPage)) {
            this.adapter.istousu = "1";
        }
        adapter.setOnPortraitClickListener(this);
        adapter.setOnMessageReceiptClickListener(this);
        adapter.setOnPortraitLongClickListener(this);
        adapter.setOnMessageCheckListener(this);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                // 向上滑动，不在底部，收到消息时，不滑动到底部, 发送消息时，可以强制置为true
                if (newState != RecyclerView.SCROLL_STATE_IDLE) {
                    return;
                }
                if (!recyclerView.canScrollVertically(1)) {
                    moveToBottom = true;
                    if ((initialFocusedMessageId != -1 || firstUnreadMessageId != 0) && !loadingNewMessage && shouldContinueLoadNewMessage) {
                        int lastVisibleItem = layoutManager.findLastCompletelyVisibleItemPosition();
                        if (lastVisibleItem > adapter.getItemCount() - 3) {
                            loadMoreNewMessages();
                        }
                    }
                } else {
                    moveToBottom = false;
                }
            }
        });

        inputPanel.init(this, rootLinearLayout, mViewIntercept);
        inputPanel.setOnConversationInputPanelStateChangeListener(this);

        conversationViewModel = WfcUIBroadKit.getAppScopeViewModel(ConversationViewBroadModel.class);
        conversationViewModel.clearConversationMessageLiveData().observeForever(clearConversationMessageObserver);
        conversationViewModel.secretConversationStateLiveData().observe(getViewLifecycleOwner(), new Observer<Pair<String, ChatManager.SecretChatState>>() {
            @Override
            public void onChanged(Pair<String, ChatManager.SecretChatState> stringSecretChatStatePair) {
                if (conversation != null && conversation.type == Conversation.ConversationType.SecretChat && conversation.target.equals(stringSecretChatStatePair.first)) {
                    reloadMessage();
                }
            }
        });
        messageViewModel = ViewModelProviders.of(this).get(MessageViewBroadModel.class);

        messageViewModel.messageLiveData().observeForever(messageLiveDataObserver);
        messageViewModel.messageUpdateLiveData().observeForever(messageUpdateLiveDatObserver);
        messageViewModel.messageRemovedLiveData().observeForever(messageRemovedLiveDataObserver);
        messageViewModel.messageStartBurnLiveData().observeForever(messageStartBurnLiveDataObserver);
        messageViewModel.messageBurnedLiveData().observeForever(messageBurnedLiveDataObserver);

        messageViewModel.messageReadLiveData().observe(getActivity(), readEntries -> {
            if (conversation == null) {
                return;
            }
            Map<String, Long> convReadEntities = ChatManager.Instance().getConversationRead(conversation);
            adapter.setReadEntries(convReadEntities);
        });

        userViewModel = ViewModelProviders.of(this).get(UserViewBroadModel.class);
        userViewModel.userInfoLiveData().observeForever(userInfoUpdateLiveDataObserver);

        settingUpdateLiveDataObserver = o -> {
            if (groupInfo == null) {
                return;
            }
            boolean show = "1".equals(userViewModel.getUserSetting(UserSettingScope.GroupHideNickname, groupInfo.target));
            if (showGroupMemberName != show) {
                showGroupMemberName = show;
                adapter.notifyDataSetChanged();
            }
            reloadMessage();
        };

        settingViewModel = ViewModelProviders.of(this).get(SettingViewBroadModel.class);
        settingViewModel.settingUpdatedLiveData().observeForever(settingUpdateLiveDataObserver);

        userOnlineStateViewModel = ViewModelProviders.of(this).get(UserOnlineStateViewBroadModel.class);
        userOnlineStateViewModel.getUserOnlineStateLiveData().observe(getViewLifecycleOwner(), userOnlineStateLiveDataObserver);


        if (backgroundImageUri != null) {
            setConversationBackgroundImage(backgroundImageUri);
        } else if (backgroundImageResId != 0) {
            setConversationBackgroundImage(backgroundImageResId);
        }
    }

    private void setupConversation(Conversation conversation) {
        if (conversation.type == Conversation.ConversationType.Group) {
            groupViewModel = ViewModelProviders.of(this).get(GroupViewBroadModel.class);
            initGroupObservers();
            groupViewModel.getGroupMembers(conversation.target, true);
            groupInfo = groupViewModel.getGroupInfo(conversation.target, true);
            groupMember = groupViewModel.getGroupMember(conversation.target, userViewModel.getUserId());
            showGroupMemberName = "1".equals(userViewModel.getUserSetting(UserSettingScope.GroupHideNickname, groupInfo.target));

            updateGroupMuteStatus();
        }
        userViewModel.getUserInfo(userViewModel.getUserId(), true);

        inputPanel.setupConversation(conversation);

        if (conversation.type != Conversation.ConversationType.ChatRoom) {
//            eLogD("zhanglong","setupConversation---"+conversation.target+"===="+initialFocusedMessageId);
            loadMessage(initialFocusedMessageId);
        }
        if (conversation.type == Conversation.ConversationType.ChatRoom) {
            chatRoomViewModel = ViewModelProviders.of(this).get(ChatRoomViewBroadModel.class);
            if (!isPreJoinedChatRoom) {
                joinChatRoom();
            } else {
                loadMoreOldMessages(true);
            }
        } else if (conversation.type == Conversation.ConversationType.Channel) {
            EnterChannelChatMessageContent content = new EnterChannelChatMessageContent();
            messageViewModel.sendMessage(conversation, content);
        } else if (conversation.type == Conversation.ConversationType.Group) {
            messageViewModel.getMsgDetailUserData(conversation.target);

        }

        ConversationInfo conversationInfo = ChatManager.Instance().getConversation(conversation);
        int unreadCount = conversationInfo.unreadCount.unread + conversationInfo.unreadCount.unreadMention + conversationInfo.unreadCount.unreadMentionAll;
        if (unreadCount > 10 && unreadCount < 300) {
            firstUnreadMessageId = ChatManager.Instance().getFirstUnreadMessageId(conversation);
            showUnreadMessageCountLabel(unreadCount);
        }
        int aitunreadCount = conversationInfo.unreadCount.unreadMention + conversationInfo.unreadCount.unreadMentionAll;
        if (aitunreadCount > 0) {
            showUnreadMentionCountLabel(aitunreadCount);
        } else {
            hideUnreadMentionCountLabel();
        }

        conversationViewModel.clearUnreadStatus(conversation);

        ongoingCalls = null;

        setTitle();
    }

    public void setConversationBackgroundImage(String uri) {
        if (rootLinearLayout == null) {
            this.backgroundImageUri = uri;
            return;
        }
        ScrollView scrollView = rootLinearLayout.findViewById(R.id.conversationBackgroundScrollView);
        ImageView imageView = rootLinearLayout.findViewById(R.id.conversationBackgroundImageView);
        scrollView.setVisibility(View.VISIBLE);
        Glide.with(this).load(uri).into(imageView);
    }

    public void setConversationBackgroundImage(int drawableId) {
        if (rootLinearLayout == null) {
            this.backgroundImageResId = drawableId;
            return;
        }
        ScrollView scrollView = rootLinearLayout.findViewById(R.id.conversationBackgroundScrollView);
        ImageView imageView = rootLinearLayout.findViewById(R.id.conversationBackgroundImageView);
        scrollView.setVisibility(View.VISIBLE);
        Glide.with(this).load(backgroundImageResId).into(imageView);
    }

    private void updateaita(long focusMessageId) {
        List<Message> oldMessageList = ChatManager.Instance().getMessages(conversation, focusMessageId, true, 100, channelPrivateChatUser);
        List<UiBroadMessage> oldMessages = new ArrayList<>();
        if (oldMessageList != null) {
            for (Message msg : oldMessageList) {
                if (conversation.type == Conversation.ConversationType.SecretChat) {
                    BurnMessageInfo burnMessageInfo = ChatManager.Instance().getBurnMessageInfo(msg.messageId);
                    oldMessages.add(new UiBroadMessage(msg, burnMessageInfo));
                } else {
                    oldMessages.add(new UiBroadMessage(msg));
                }
            }
        }
        Message message = ChatManager.Instance().getMessage(focusMessageId);
        List<Message> newMessageList = ChatManager.Instance().getMessages(conversation, focusMessageId, false, 100, channelPrivateChatUser);
        List<UiBroadMessage> newMessages = new ArrayList<>();
        if (newMessageList != null) {
            for (Message msg : newMessageList) {
                if (conversation.type == Conversation.ConversationType.SecretChat) {
                    BurnMessageInfo burnMessageInfo = ChatManager.Instance().getBurnMessageInfo(msg.messageId);
                    newMessages.add(new UiBroadMessage(msg, burnMessageInfo));
                } else {
                    newMessages.add(new UiBroadMessage(msg));
                }
            }
        }

        List<UiBroadMessage> messages = new ArrayList<>();
        messages.addAll(oldMessages);
        if (message != null) {
            messages.add(new UiBroadMessage(message));
        }
        messages.addAll(newMessages);
        ChatManager.Instance().getMainHandler().post(() -> {
            adapter.setMessages(messages);
            adapter.notifyDataSetChanged();

            if (adapter.getItemCount() > 1) {
                int initialMessagePosition;
                if (focusMessageId != -1) {
                    initialMessagePosition = adapter.getMessagePosition(focusMessageId);
                    if (initialMessagePosition != -1) {
                        recyclerView.scrollToPosition(initialMessagePosition);
                        adapter.highlightFocusMessage(initialMessagePosition);
                        eLogD("野火", "被aita pos--> " + initialMessagePosition);
                    }
                } else {
                    moveToBottom = true;
                    recyclerView.scrollToPosition(adapter.getItemCount() - 1);
                }
            }
        });

    }

    private void loadMessage(long focusMessageId) {

        MutableLiveData<List<UiBroadMessage>> messages;
        if (focusMessageId != -1) {
            shouldContinueLoadNewMessage = true;
            messages = conversationViewModel.loadAroundMessages(conversation, channelPrivateChatUser, focusMessageId, MESSAGE_LOAD_AROUND);
        } else {
            messages = conversationViewModel.getMessages(conversation, channelPrivateChatUser);
        }
        // load message
        swipeRefreshLayout.setRefreshing(true);
        swipeRefreshLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                //预防清空消息记录后下次进来远端拉去数据加载失败一直转圈圈
                swipeRefreshLayout.setRefreshing(false);
            }
        }, 3000L);
        adapter.setReadEntries(ChatManager.Instance().getConversationRead(conversation));
        messages.observe(this, uiMessages -> {
            swipeRefreshLayout.setRefreshing(false);

            adapter.setMessages(uiMessages);
            adapter.notifyDataSetChanged();

            if (adapter.getItemCount() > 1) {
                int initialMessagePosition;
                if (focusMessageId != -1) {
                    initialMessagePosition = adapter.getMessagePosition(focusMessageId);
                    if (initialMessagePosition != -1) {
                        recyclerView.scrollToPosition(initialMessagePosition);
                        adapter.highlightFocusMessage(initialMessagePosition);
                    }
                } else {
                    moveToBottom = true;
                    recyclerView.scrollToPosition(adapter.getItemCount() - 1);
                }
            }
        });
    }

    private void updateGroupMuteStatus() {
        if (groupInfo == null || groupMember == null) {
            return;
        }
        if (groupInfo.mute == 1
                && groupMember.type != GroupMember.GroupMemberType.Owner
                && groupMember.type != GroupMember.GroupMemberType.Manager
                && groupMember.type != GroupMember.GroupMemberType.Allowed) {
            inputPanel.disableInput("全员禁言中");
        } else if (groupMember.type == GroupMember.GroupMemberType.Muted) {
            inputPanel.disableInput("你已被禁言");
        } else {
            inputPanel.enableInput();
        }
    }

    public void openMeetingDialog(String userIdNo) {

    }

    void onUnreadCountTextViewClick() {
        hideUnreadMessageCountLabel();
        shouldContinueLoadNewMessage = true;
        loadMessage(firstUnreadMessageId);
    }

    void aitonUnreadCountTextViewClick() {
        hideUnreadMentionCountLabel();
        try {
            ChatManager.Instance().mClient.getMentionedMessagesAsync(conversation, 0, true, 100, new IGetMessageCallback.Stub() {
                @Override
                public void onSuccess(List<Message> messages, boolean hasMore) throws RemoteException {
                    if (messages != null && messages.size() > 0) {
                        shouldContinueLoadNewMessage = true;
                        updateaita(messages.get(0).messageId);
                    }
                }

                @Override
                public void onFailure(int errorCode) throws RemoteException {
                    eLogD("野火", "获取@消息--> onFailure " + errorCode);
                }
            });
        } catch (Exception e) {

        }
    }

    private void showUnreadMessageCountLabel(int count) {
        unreadCountLinearLayout.setVisibility(View.VISIBLE);
        unreadCountTextView.setVisibility(View.VISIBLE);
        unreadCountTextView.setText(count + "条消息");
    }

    private void hideUnreadMessageCountLabel() {
        unreadCountTextView.setVisibility(View.GONE);
    }

    private void showUnreadMentionCountLabel(int count) {
        unreadCountLinearLayout.setVisibility(View.VISIBLE);
        unreadMentionCountTextView.setVisibility(View.VISIBLE);
        unreadMentionCountTextView.setText(count + "条@消息");
    }

    private void hideUnreadMentionCountLabel() {
        unreadMentionCountTextView.setVisibility(View.GONE);
    }

    private void joinChatRoom() {
        chatRoomViewModel.joinChatRoom(conversation.target)
                .observe(this, new Observer<OperateBroadResult<Boolean>>() {
                    @Override
                    public void onChanged(@Nullable OperateBroadResult<Boolean> booleanOperateResult) {
                        if (booleanOperateResult.isSuccess()) {
                            String welcome = "欢迎 %s 加入聊天室";
                            TipNotificationContent content = new TipNotificationContent();
                            String userId = userViewModel.getUserId();
                            UserInfo userInfo = userViewModel.getUserInfo(userId, false);
                            if (userInfo != null) {
                                content.tip = String.format(welcome, userViewModel.getUserDisplayName(userInfo));
                            } else {
                                content.tip = String.format(welcome, "<" + userId + ">");
                            }
                            handler.postDelayed(() -> {
                                messageViewModel.sendMessage(conversation, content);
                            }, 1000);
                            setChatRoomConversationTitle();

                        } else {
                            Toast.makeText(getActivity(), "加入聊天室失败", Toast.LENGTH_SHORT).show();
                            getActivity().finish();
                        }
                    }
                });
    }

    private void quitChatRoom() {
        String welcome = "%s 离开了聊天室";
        TipNotificationContent content = new TipNotificationContent();
        String userId = userViewModel.getUserId();
        UserInfo userInfo = userViewModel.getUserInfo(userId, false);
        if (userInfo != null) {
            content.tip = String.format(welcome, userViewModel.getUserDisplayName(userInfo));
        } else {
            content.tip = String.format(welcome, "<" + userId + ">");
        }
        Message message = new Message();
        message.conversation = conversation;
        message.content = content;
        messageViewModel.sendMessageEx(message).observe(this, voidOperateResult -> chatRoomViewModel.quitChatRoom(conversation.target));
    }

    private void setChatRoomConversationTitle() {
        chatRoomViewModel.getChatRoomInfo(conversation.target, 0)
                .observe(this, chatRoomInfoOperateResult -> {
                    if (chatRoomInfoOperateResult.isSuccess()) {
                        ChatRoomInfo chatRoomInfo = chatRoomInfoOperateResult.getResult();
                        conversationTitle = chatRoomInfo.title;
                        setActivityTitle(conversationTitle);
                    }
                });
    }

    private void setTitle() {
        if (!TextUtils.isEmpty(conversationTitle)) {
            setActivityTitle(conversationTitle);
        }

        if (conversation.type == Conversation.ConversationType.Single) {
            UserInfo userInfo = ChatManagerBroadHolder.gChatManager.getUserInfo(conversation.target, false);
//            conversationTitle = userViewModel.getUserDisplayName(userInfo);
            conversationTitle = getMyUserDisplayName(conversation, userInfo);
            ELogUtilKt.eLogD("zhanglong", "conversationTitle===" + conversationTitle);
            UserOnlineState userOnlineState = ChatManager.Instance().getUserOnlineStateMap().get(userInfo.uid);
            if (userOnlineState != null) {
                String onlineDesc = userOnlineState.desc();
                if (!TextUtils.isEmpty(onlineDesc)) {
                    conversationTitle += " (" + onlineDesc + ")";
                }
            }
        } else if (conversation.type == Conversation.ConversationType.Group) {
            if (groupInfo != null) {
                conversationTitle = (!TextUtils.isEmpty(groupInfo.remark) ? groupInfo.remark : groupInfo.name) + "(" + (groupInfo.memberCount - 1) + "人)";
            }
        } else if (conversation.type == Conversation.ConversationType.Channel) {
            ChannelViewBroadModel channelViewModel = ViewModelProviders.of(this).get(ChannelViewBroadModel.class);
            ChannelInfo channelInfo = channelViewModel.getChannelInfo(conversation.target, false);
            if (channelInfo != null) {
                conversationTitle = channelInfo.name;
            }

            if (!TextUtils.isEmpty(channelPrivateChatUser)) {
                UserInfo channelPrivateChatUserInfo = userViewModel.getUserInfo(channelPrivateChatUser, false);
                if (channelPrivateChatUserInfo != null) {
                    conversationTitle += "@" + userViewModel.getUserDisplayName(channelPrivateChatUserInfo);
                } else {
                    conversationTitle += "@<" + channelPrivateChatUser + ">";
                }
            }
        } else if (conversation.type == Conversation.ConversationType.SecretChat) {
            String userId = ChatManager.Instance().getSecretChatInfo(conversation.target).getUserId();
            UserInfo userInfo = ChatManagerBroadHolder.gChatManager.getUserInfo(userId, false);
            conversationTitle = userViewModel.getUserDisplayName(userInfo);
        }

        setActivityTitle(conversationTitle);
    }

    private String getMyUserDisplayName(Conversation conversationInfo, UserInfo userInfo) {
        if (MMKVBroadUtils.Companion.getKeFuId() != null && conversation.target.contains(MMKVBroadUtils.Companion.getKeFuId())) {
            return MMKVBroadUtils.Companion.getKeFuName();
        } else if (userInfo != null && !TextUtils.isEmpty(userInfo.friendAlias)) {
            return userInfo.friendAlias;
        } else if (userInfo != null && !(userInfo instanceof NullUserInfo) && !TextUtils.isEmpty(userInfo.displayName)) {
            return userInfo.displayName;
        } else {
            GlobalFriendBroadBean friendSmailListBean = GlobalBroadInfo.INSTANCE.queryMyFriendInfo(conversationInfo.target);
            if (friendSmailListBean != null) {
                if (!TextUtils.isEmpty(friendSmailListBean.remarkName)) {
                    return friendSmailListBean.remarkName;
                } else if (!TextUtils.isEmpty(friendSmailListBean.nickName)) {
                    return friendSmailListBean.nickName;
                }
            }

            if (userInfo == null) {
                return "";
            }
            return "<" + userInfo.uid + ">";
        }
    }

    private void setActivityTitle(String title) {
        Activity activity = getActivity();
        if (activity != null) {
            activity.setTitle(title);
        }
        EventBus.getDefault().post(new MyBroadEvent("danliaobiaoti", title, ""));
    }

    boolean onTouch(View view, MotionEvent event) {
//        if (event.getAction() == MotionEvent.ACTION_DOWN && inputPanel.extension.canHideOnScroll()) {
//            inputPanel.collapse();
//        }
        inputPanel.closeConversationInputPanel();
        return false;
    }

    @Override
    public void onPortraitClick(UserInfo userInfo) {
        MyCommonBroadBridge.getInstance().setMyQiaoJie(getContext(), "danliaogengduo", userInfo.uid, "", null);
        // 去掉野火跳转用户主页
//        if (conversation.type == Conversation.ConversationType.Channel) {
//            Intent intent = new Intent(getActivity(), ChannelInfoActivity.class);
//            intent.putExtra("channelId", conversation.target);
//            startActivity(intent);
//            return;
//        }
//
//        if (groupInfo != null && groupInfo.privateChat == 1) {
//            boolean allowPrivateChat = false;
//            GroupMember groupMember = groupViewModel.getGroupMember(groupInfo.target, userViewModel.getUserId());
//            if (groupMember != null && groupMember.type == GroupMember.GroupMemberType.Normal) {
//                GroupMember targetGroupMember = groupViewModel.getGroupMember(groupInfo.target, userInfo.uid);
//                if (targetGroupMember != null && (targetGroupMember.type == GroupMember.GroupMemberType.Owner || targetGroupMember.type == GroupMember.GroupMemberType.Manager)) {
//                    allowPrivateChat = true;
//                }
//            } else if (groupMember != null && (groupMember.type == GroupMember.GroupMemberType.Owner || groupMember.type == GroupMember.GroupMemberType.Manager)) {
//                allowPrivateChat = true;
//            }
//
//            if (!allowPrivateChat) {
//                Toast.makeText(getActivity(), "禁止群成员私聊", Toast.LENGTH_SHORT).show();
//                return;
//            }
//        }
//        if (userInfo.deleted == 0) {
//            Intent intent = new Intent(getActivity(), UserInfoActivity.class);
//            intent.putExtra("userInfo", userInfo);
//            if (conversation.type == Conversation.ConversationType.Group) {
//                intent.putExtra("groupId", conversation.target);
//            }
//            startActivity(intent);
//        }
    }

    @Override
    public void onPortraitLongClick(UserInfo userInfo) {
        //不可@自己
        if (userInfo.uid.equals(ChatManager.Instance().getUserId())) {
            return;
        }
        if (conversation.type == Conversation.ConversationType.Group) {
            SpannableString spannableString = mentionSpannable(userInfo);
            int position = inputPanel.editText.getSelectionEnd();
            inputPanel.editText.getEditableText().append(" ");
            inputPanel.editText.getEditableText().replace(position, position + 1, spannableString);
        } else {
            inputPanel.editText.getEditableText().append(userViewModel.getUserDisplayName(userInfo));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode >= ConversationBroadExtension.REQUEST_CODE_MIN) {
            boolean result = inputPanel.extension.onActivityResult(requestCode, resultCode, data);
            if (result) {
                return;
            }
            eLogD(TAG, "extension can not handle " + requestCode);
        }
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_PICK_MENTION_CONTACT) {
                boolean isMentionAll = data.getBooleanExtra("mentionAll", false);
                SpannableString spannableString;
                if (isMentionAll) {
                    spannableString = mentionAllSpannable();
                } else {
                    String userId = data.getStringExtra("userId");
                    UserInfo userInfo = userViewModel.getUserInfo(userId, this.groupInfo.target, false);
                    spannableString = mentionSpannable(userInfo);
                }
                int position = inputPanel.editText.getSelectionEnd();
                position = position > 0 ? position - 1 : 0;
                inputPanel.editText.getEditableText().replace(position, position + 1, spannableString);

            } else if (requestCode == REQUEST_CODE_GROUP_AUDIO_CHAT || requestCode == REQUEST_CODE_GROUP_VIDEO_CHAT) {
                onPickGroupMemberToVoipChat(data, requestCode == REQUEST_CODE_GROUP_AUDIO_CHAT);
            }
        }
    }

    private SpannableString mentionAllSpannable() {
        String text = "@所有人 ";
        SpannableString spannableString = new SpannableString(text);
        spannableString.setSpan(new MentionBroadSpan(true), 0, spannableString.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        return spannableString;
    }

    private SpannableString mentionSpannable(UserInfo userInfo) {
        String text = "@" + ChatManager.Instance().getGroupMemberDisplayName(userInfo) + " ";
        SpannableString spannableString = new SpannableString(text);
        spannableString.setSpan(new MentionBroadSpan(userInfo.uid), 0, spannableString.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        return spannableString;
    }

    @Override
    public void onPause() {
        super.onPause();
        inputPanel.onActivityPause();
        messageViewModel.stopPlayAudio();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        if (conversation == null) {
            return;
        }

        if ((conversation.type == Conversation.ConversationType.Single && !ChatManager.Instance().isMyFriend(conversation.target))
                || conversation.type == Conversation.ConversationType.Group) {
            userOnlineStateViewModel.unwatchOnlineState(conversation.type.getValue(), new String[]{conversation.target});
        }

        if (conversation.type == Conversation.ConversationType.ChatRoom) {
            if (!isPreJoinedChatRoom) {
                quitChatRoom();
            }
        } else if (conversation.type == Conversation.ConversationType.Channel) {
            LeaveChannelChatMessageContent content = new LeaveChannelChatMessageContent();
            messageViewModel.sendMessage(conversation, content);
        }

        messageViewModel.messageLiveData().removeObserver(messageLiveDataObserver);
        messageViewModel.messageUpdateLiveData().removeObserver(messageUpdateLiveDatObserver);
        messageViewModel.messageRemovedLiveData().removeObserver(messageRemovedLiveDataObserver);
        userViewModel.userInfoLiveData().removeObserver(userInfoUpdateLiveDataObserver);
        conversationViewModel.clearConversationMessageLiveData().removeObserver(clearConversationMessageObserver);
        settingViewModel.settingUpdatedLiveData().removeObserver(settingUpdateLiveDataObserver);

        unInitGroupObservers();
        inputPanel.onDestroy();

        // 退出密聊时，清空相关临时文件
        if (conversation.type == Conversation.ConversationType.SecretChat) {
            List<UiBroadMessage> messages = adapter.getMessages();
            if (messages != null) {
                for (UiBroadMessage uiMsg : messages) {
                    File file = DownloadBroadManager.mediaMessageContentFile(uiMsg.message);
                    if (file != null && file.exists()) {
                        file.delete();
                    }
                }
            }
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMsgReceive(FanYiBroadEvent fanYiEvent) {
        adapter.notifyDataSetChanged();
    }

    boolean onBackPressed() {
        boolean consumed = true;
        if (rootLinearLayout.getCurrentInput() != null) {
            rootLinearLayout.hideAttachedInput(true);
            inputPanel.closeConversationInputPanel();
        } else if (multiMessageActionContainerLinearLayout.getVisibility() == View.VISIBLE) {
            toggleConversationMode();
        } else {
            consumed = false;
        }
        return consumed;
    }

    @Override
    public void onKeyboardShown() {
        inputPanel.onKeyboardShown();
        recyclerView.scrollToPosition(adapter.getItemCount() - 1);
    }

    @Override
    public void onKeyboardHidden() {
        inputPanel.onKeyboardHidden();
    }

    public void reloadMessage() {
        conversationViewModel.getMessages(conversation, channelPrivateChatUser).observe(this, uiMessages -> {
            adapter.setMessages(uiMessages);
            adapter.notifyDataSetChanged();
        });
    }

    public void reloadZhuanFaMessage() {
        conversationViewModel.getMessages(conversation, channelPrivateChatUser).observe(this, uiMessages -> {
            for (int i = 0; i < uiMessages.size(); i++) {
                uiMessages.get(i).message.status = MessageStatus.Sent;
            }
            adapter.setMessages(uiMessages);
            adapter.notifyDataSetChanged();
        });
    }

    private void loadMoreOldMessages() {
        loadMoreOldMessages(false);
    }


    private void loadMoreOldMessages(boolean scrollToBottom) {
//eLogD("zhanglong","channelPrivateChatUser======"+channelPrivateChatUser+"==="+adapter.oldestMessageId+"===="+adapter.oldestMessageUid);
//eLogD("zhanglong","conversation=="+conversation.target+"==="+conversation.type);

        conversationViewModel.loadOldMessages(conversation, channelPrivateChatUser, adapter.oldestMessageId, adapter.oldestMessageUid, MESSAGE_LOAD_COUNT_PER_TIME)
                .observe(this, uiMessages -> {
//                    eLogD("zhanglong","loadOldMessages===uiMessages.size==="+uiMessages.size());

                    List<UiBroadMessage> messageList = adapter.getMessages();
                    for (int i = 0; i < messageList.size(); i++) {
                        for (int j = 0; j < uiMessages.size(); j++) {
                            if (messageList.get(i).message.messageId == uiMessages.get(j).message.messageId) {
                                uiMessages.remove(j);
                                j--;
                            }
                        }
                    }

                    adapter.addMessagesAtHead(uiMessages);
                    swipeRefreshLayout.setRefreshing(false);
                    if (scrollToBottom) {
                        recyclerView.scrollToPosition(adapter.getItemCount() - 1);
                    }
                });
    }

    private void loadMoreNewMessages() {
        loadingNewMessage = true;
        adapter.showLoadingNewMessageProgressBar();
        conversationViewModel.loadNewMessages(conversation, channelPrivateChatUser, adapter.getItem(adapter.getItemCount() - 2).message.messageId, MESSAGE_LOAD_COUNT_PER_TIME)
                .observe(this, messages -> {
                    loadingNewMessage = false;
                    adapter.dismissLoadingNewMessageProgressBar();

                    if (messages == null || messages.isEmpty()) {
                        shouldContinueLoadNewMessage = false;
                    }
                    if (messages != null && !messages.isEmpty()) {
                        adapter.addMessagesAtTail(messages);
                    }
                });
    }

    private void updateTypingStatusTitle() {
        if (this.typingMessageMap == null || this.conversation.type == Conversation.ConversationType.Channel || this.conversation.type == Conversation.ConversationType.ChatRoom) {
            return;
        }
        this.checkUserTyping();
        String typingDesc = "";
        if (conversation.type == Conversation.ConversationType.Group || typingMessageMap.size() == 0) {
            this.resetConversationTitle();
            return;
        } else if (typingMessageMap.size() == 1) {
            Set<String> users = typingMessageMap.keySet();
            String userId = users.toArray(new String[1])[0];
            UserInfo userInfo;
            if (conversation.type == Conversation.ConversationType.Group) {
                userInfo = ChatManager.Instance().getUserInfo(userId, conversation.target, false);
                String userName = "有人";
                if (!TextUtils.isEmpty(userInfo.friendAlias)) {
                    userName = userInfo.friendAlias;
                } else if (!TextUtils.isEmpty(userInfo.groupAlias)) {
                    userName = userInfo.groupAlias;
                } else if (!TextUtils.isEmpty(userInfo.displayName)) {
                    userName = userInfo.displayName;
                }

                TypingMessageContent typingMessageContent = (TypingMessageContent) typingMessageMap.get(userId).content;
                switch (typingMessageContent.getTypingType()) {
                    case TypingMessageContent.TYPING_TEXT:
                        typingDesc = "正在输入";
                        break;
                    case TypingMessageContent.TYPING_VOICE:
                        typingDesc = "正在录音";
                        break;
                    case TypingMessageContent.TYPING_CAMERA:
                        typingDesc = "正在拍照";
                        break;
                    case TypingMessageContent.TYPING_FILE:
                        typingDesc = "正在发送文件";
                        break;
                    case TypingMessageContent.TYPING_LOCATION:
                        typingDesc = "正在发送位置";
                        break;
                    default:
                        typingDesc = "unknown";
                        break;
                }
                typingDesc = userName + " " + typingDesc;
            } else {
                TypingMessageContent typingMessageContent = (TypingMessageContent) typingMessageMap.get(userId).content;
                switch (typingMessageContent.getTypingType()) {
                    case TypingMessageContent.TYPING_TEXT:
                        typingDesc = "对方正在输入";
                        break;
                    case TypingMessageContent.TYPING_VOICE:
                        typingDesc = "对方正在录音";
                        break;
                    case TypingMessageContent.TYPING_CAMERA:
                        typingDesc = "对方正在拍照";
                        break;
                    case TypingMessageContent.TYPING_FILE:
                        typingDesc = "对方正在发送文件";
                        break;
                    case TypingMessageContent.TYPING_LOCATION:
                        typingDesc = "对方正在发送位置";
                        break;
                    default:
                        typingDesc = "unknown";
                        break;
                }
            }
        } else {
            typingDesc = typingMessageMap.size() + "人正在输入";
        }
        setActivityTitle(typingDesc);
        handler.postDelayed(this::updateTypingStatusTitle, 5000);
    }

    private void checkUserTyping() {
        Set<Map.Entry<String, Message>> entries = typingMessageMap.entrySet();
        Iterator<Map.Entry<String, Message>> iterator = entries.iterator();
        long now = System.currentTimeMillis();
        while (iterator.hasNext()) {
            Map.Entry<String, Message> next = iterator.next();
            Message msg = next.getValue();
            if (now - msg.serverTime + ChatManager.Instance().getServerDeltaTime() > TYPING_INTERNAL) {
                iterator.remove();
            }
        }
    }

    private void resetConversationTitle() {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }
        if (!TextUtils.equals(conversationTitle, getActivity().getTitle())) {
            setActivityTitle(conversationTitle);
        }
    }

    @Override
    public void onInputPanelExpanded() {
        recyclerView.scrollToPosition(adapter.getItemCount() - 1);
    }

    @Override
    public void onInputPanelCollapsed() {
        // do nothing
    }

    public void toggleMultiMessageMode(UiBroadMessage message) {
        inputPanel.setVisibility(View.GONE);
        message.isChecked = true;
        adapter.setMode(ConversationMessageBroadAdapter.MODE_CHECKABLE);
        adapter.notifyDataSetChanged();
        multiMessageActionContainerLinearLayout.setVisibility(View.VISIBLE);
        setupMultiMessageAction();
    }

    public void toggleConversationMode() {
        inputPanel.setVisibility(View.VISIBLE);
        multiMessageActionContainerLinearLayout.setVisibility(View.GONE);
        adapter.setMode(ConversationMessageBroadAdapter.MODE_NORMAL);
        adapter.clearMessageCheckStatus();
        adapter.notifyDataSetChanged();
    }

    public void setInputText(String text) {
        inputPanel.setInputText(text);
    }

    private void setupMultiMessageAction() {
        multiMessageActionContainerLinearLayout.removeAllViews();
        List<MultiMessageBroadAction> actions = MultiMessageActionBroadManager.getInstance().getConversationActions(conversation);
        int width = getResources().getDisplayMetrics().widthPixels;

        for (MultiMessageBroadAction action : actions) {
            action.onBind(this, conversation);
            ImageView imageView = new ImageView(getActivity());
            imageView.setImageResource(action.iconResId());


            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width / actions.size(), LinearLayout.LayoutParams.WRAP_CONTENT);
            multiMessageActionContainerLinearLayout.addView(imageView, layoutParams);
            ViewGroup.LayoutParams p = imageView.getLayoutParams();
            p.height = 70;
            imageView.requestLayout();

            imageView.setOnClickListener(v -> {
                List<UiBroadMessage> checkedMessages = adapter.getCheckedMessages();
                if (action.confirm()) {
                    new MaterialDialog.Builder(getActivity()).content(action.confirmPrompt())
                            .negativeText("取消")
                            .positiveText("确认")
                            .onPositive((dialog, which) -> {
                                action.onClick(checkedMessages);
                                toggleConversationMode();
                            })
                            .build()
                            .show();

                } else {
                    action.onClick(checkedMessages);
                    toggleConversationMode();
                }
            });
        }
    }

    @Override
    public void onMessageCheck(UiBroadMessage message, boolean checked) {
        List<UiBroadMessage> checkedMessages = adapter.getCheckedMessages();
        setAllClickableChildViewState(multiMessageActionContainerLinearLayout, checkedMessages.size() > 0);
    }

    private void setAllClickableChildViewState(View view, boolean enable) {
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                setAllClickableChildViewState(((ViewGroup) view).getChildAt(i), enable);
            }
        }
        if (view.isClickable()) {
            view.setEnabled(enable);
        }
    }

    public void pickGroupMemberToVoipChat(boolean isAudioOnly) {
        Intent intent = new Intent(getActivity(), PickGroupMemberBroadActivity.class);
        GroupInfo groupInfo = groupViewModel.getGroupInfo(conversation.target, false);
        intent.putExtra("groupInfo", groupInfo);
        int maxCount = AVEngineKit.isSupportMultiCall() ? (isAudioOnly ? AVEngineKit.MAX_AUDIO_PARTICIPANT_COUNT - 1 : AVEngineKit.MAX_VIDEO_PARTICIPANT_COUNT - 1) : 1;
        intent.putExtra("maxCount", maxCount);
        startActivityForResult(intent, isAudioOnly ? REQUEST_CODE_GROUP_AUDIO_CHAT : REQUEST_CODE_GROUP_VIDEO_CHAT);
    }

    private void onPickGroupMemberToVoipChat(Intent intent, boolean isAudioOnly) {
        List<String> memberIds = intent.getStringArrayListExtra(PickGroupMemberBroadActivity.EXTRA_RESULT);
        if (memberIds != null && memberIds.size() > 0) {
            if (AVEngineKit.isSupportMultiCall()) {
                WfcUIBroadKit.multiCall(getActivity(), conversation.target, memberIds, isAudioOnly);
            } else {
                WfcUIBroadKit.singleCall(getActivity(), memberIds.get(0), isAudioOnly);
            }
        }
    }

    @Override
    public void onMessageReceiptCLick(Message message) {
        Intent intent = new Intent(getActivity(), GroupMessageReceiptBroadActivity.class);
        intent.putExtra("message", message);
        intent.putExtra("groupInfo", groupInfo);
        startActivity(intent);
    }

    private void cleanExpiredOngoingCalls() {
        for (Iterator<Map.Entry<String, Message>> it = ongoingCalls.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, Message> entry = it.next();
            if (System.currentTimeMillis() - (entry.getValue().serverTime + ChatManager.Instance().getServerDeltaTime()) > 3000) {
                it.remove();
            }
        }
        if (ongoingCalls.size() > 0) {
            handler.postDelayed(this::cleanExpiredOngoingCalls, 1000);
        } else {
            ongoingCallAdapter.setOngoingCalls(null);
            ongoingCallRecyclerView.setVisibility(View.GONE);
        }
    }
}
