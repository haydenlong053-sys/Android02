/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversation;

import static com.cboda.baselibrary.utils.ELogUtilKt.eLogD;
import static com.cboda.baselibrary.utils.GlideUtilsKt.loadImg;
import static com.cboda.baselibrary.utils.ViewExpandUtilKt.click;

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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.blankj.utilcode.util.ThreadUtils;
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

import com.cboda.baselibrary.utils.OnSingleClickBroadListener;
import com.cboda.baselibrary.widge.VerticalDanmakuView;
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
import com.cboda.chatui.dialog.DanMaPop;
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
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;

import com.cboda.baselibrary.common.BridgeImEnum;
import com.cboda.baselibrary.common.MyEventCodeEnum;

@SuppressLint("FragmentLiveDataObserve")
public class ConversationBroadFragment extends Fragment implements
        KeyboardAwareLinearBroadLayout.OnKeyboardShownListener,
        KeyboardAwareLinearBroadLayout.OnKeyboardHiddenListener,
        ConversationMessageBroadAdapter.OnPortraitClickListener,
        ConversationMessageBroadAdapter.OnPortraitLongClickListener,
        ConversationInputBroadPanel.OnConversationInputPanelStateChangeListener,
        ConversationMessageBroadAdapter.OnMessageCheckListener, ConversationMessageBroadAdapter.OnMessageReceiptClickListener {

    private static final String TAG = "convFragment";

    public static final int REQUEST_BROAD_PICK_MENTION_CONTACT = 100;
    public static final int REQUEST_BROAD_CODE_GROUP_VIDEO_CHAT = 101;
    public static final int REQUEST_BROAD_CODE_GROUP_AUDIO_CHAT = 102;

    private Conversation conversationBroad;
    private boolean loadingNewMessageBroad;
    private boolean shouldContinueLoadNewMessageBroad = false;
    private boolean isPreJoinedChatRoomBroad = true;

    private static final int MESSAGE_BROAD_LOAD_COUNT_PER_TIME = 20;
    private static final int MESSAGE_BROAD_LOAD_AROUND = 10;
    private static final long TYPING_BROAD_INTERNAL = 10 * 1000;

    InputAwareBroadLayout rootLinearLayoutBroad;
    SwipeRefreshLayout swipeRefreshLayoutBroad;
    VerticalDanmakuView verticalDanmakuView;
    ImageView danmakuIv;
    RecyclerView recyclerViewBroad;

    RecyclerView ongoingCallRecyclerViewBroad;

    public ConversationInputBroadPanel inputPanelBroad;
    public View mViewInterceptBroad;
    public TextView tvPhoneLoginBroad;

    LinearLayout multiMessageActionContainerLinearLayoutBroad;

    LinearLayout unreadCountLinearLayoutBroad;
    TextView unreadCountTextViewBroad;
    TextView unreadMentionCountTextViewBroad;

    private ConversationMessageBroadAdapter adapterBroad;
    private OngoingCallBroadAdapter ongoingCallAdapterBroad;
    private boolean moveToBottom = true;
    private ConversationViewBroadModel conversationBroadViewModel;
    private SettingViewBroadModel settingBroadViewModel;
    private UserOnlineStateViewBroadModel userOnlineStateBroadViewModel;
    public MessageViewBroadModel messageBroadViewModel;
    private UserViewBroadModel userBroadViewModel;
    private GroupViewBroadModel groupBroadViewModel;
    private ChatRoomViewBroadModel chatRoomBroadViewModel;

    private Handler handlerBroad = ThreadUtils.getMainHandler();
    ;
    private long initialFocusedBroadMessageId;
    private long firstUnreadBroadMessageId;
    // 用户channel主发起，针对某个用户的会话
    private String channelPrivateBroadChatUser;
    private String conversationBroadTitle = "";
    private LinearLayoutManager layoutBroadManager;

    // for group
    private GroupInfo groupInfoBroad;
    private GroupMember groupMemberBroad;
    private boolean showGroupMemberNameBroad = false;
    private Observer<List<GroupMember>> groupMembersUpdateBroadLiveDataObserver;
    private Observer<List<GroupInfo>> groupInfosUpdateBroadLiveDataObserver;
    private Observer<Object> settingUpdateBroadLiveDataObserver;
    private Map<String, Message> ongoingCallsBroad;

    private Map<String, Message> typingMessageBroadMap;

    private String backgroundImageBroadUri = null;
    private int backgroundImageBroadResId = 0;
    public View qungonggaoBroad;
    public View qunMeetingNoticeBroad;
    public MarqueeTextBroadView gonggaoxinxiBroad;
    public MarqueeTextBroadView tvQunMeetingBroad;

    private Observer<UiBroadMessage> messageBroadLiveDataObserver = new Observer<UiBroadMessage>() {
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

                if (ongoingCallsBroad == null) {
                    ongoingCallsBroad = new HashMap<>();
                }
                ongoingCallsBroad.put(ongoingCall.getCallId(), uiMessage.message);

                if (ongoingCallsBroad.size() > 0) {
                    ongoingCallRecyclerViewBroad.setVisibility(View.VISIBLE);
                    if (ongoingCallAdapterBroad == null) {
                        ongoingCallAdapterBroad = new OngoingCallBroadAdapter();
                        ongoingCallRecyclerViewBroad.setAdapter(ongoingCallAdapterBroad);
                        ongoingCallRecyclerViewBroad.setLayoutManager(new LinearLayoutManager(getActivity()));
                    }
                    ongoingCallAdapterBroad.setOngoingCalls(new ArrayList<>(ongoingCallsBroad.values()));
                } else {
                    ongoingCallRecyclerViewBroad.setVisibility(View.GONE);
                    ongoingCallAdapterBroad.setOngoingCalls(null);
                }
                cleanExpiredOngoingCalls();

                return;
            }

            if (isDisplayableMessage(uiMessage) && !(content instanceof RecallMessageContent)) {
                // 消息定位时，如果收到新消息、或者发送消息，需要重新加载消息列表
                if (shouldContinueLoadNewMessageBroad) {
                    shouldContinueLoadNewMessageBroad = false;
                    eLogD("ConversationFragmentClass", "reloadMessage");
                    reloadMessage();
                    return;
                }
                adapterBroad.addNewMessage(uiMessage);
                if (moveToBottom || uiMessage.message.sender.equals(ChatManager.Instance().getUserId())) {
                    UIBroadUtils.postTaskDelay(() -> {

                                int position = adapterBroad.getItemCount() - 1;
                                if (position < 0) {
                                    return;
                                }
                                recyclerViewBroad.scrollToPosition(position);
                            },
                            100);
                }
            }
            if (content instanceof TypingMessageContent && uiMessage.message.direction == MessageDirection.Receive) {
                if (typingMessageBroadMap == null) {
                    typingMessageBroadMap = new HashMap<>();
                }
                long now = System.currentTimeMillis();
                if (now - uiMessage.message.serverTime + ChatManager.Instance().getServerDeltaTime() < TYPING_BROAD_INTERNAL) {
                    typingMessageBroadMap.put(uiMessage.message.sender, uiMessage.message);
                }
                updateTypingStatusTitle();
            } else {
                if (uiMessage.message.direction == MessageDirection.Receive && typingMessageBroadMap != null) {
                    typingMessageBroadMap.remove(uiMessage.message.sender);
                }
                updateTypingStatusTitle();
            }

            if (getLifecycle().getCurrentState() == Lifecycle.State.RESUMED && uiMessage.message.direction == MessageDirection.Receive) {
                conversationBroadViewModel.clearUnreadStatus(conversationBroad);
            }
        }
    };
    private Observer<UiBroadMessage> messageUpdateBroadLiveDatObserver = new Observer<UiBroadMessage>() {
        @Override
        public void onChanged(@Nullable UiBroadMessage uiMessage) {
            // 聊天室，对方撤回消息
            if (conversationBroad == null) {
                return;
            }
            if (conversationBroad.type == Conversation.ConversationType.ChatRoom && uiMessage.message.conversation == null) {
                List<UiBroadMessage> messages = adapterBroad.getMessages();
                for (UiBroadMessage uiMsg : messages) {
                    if (uiMsg.message.messageUid == uiMessage.message.messageUid) {
                        RecallMessageContent content = new RecallMessageContent(uiMsg.message.sender, uiMsg.message.messageUid);
                        content.setOriginalSender(uiMsg.message.sender);
                        uiMsg.message.content = content;
                        adapterBroad.updateMessage(uiMsg);
                        break;
                    }
                }
            } else {
                if (!isMessageInCurrentConversation(uiMessage)) {
                    return;
                }
                if (isDisplayableMessage(uiMessage)) {
                    adapterBroad.updateMessage(uiMessage);
                }
                if (uiMessage.progress == 100 && uiMessage.message.direction == MessageDirection.Receive) {
                    uiMessage.progress = 0;
                    eLogD("zhanglong", "---接收方语音消息完成，准备播放语音气泡");
                    messageBroadViewModel.playAudioMessage(uiMessage);
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
        List<UiBroadMessage> messages = adapterBroad.getMessages();
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
                eLogD("zhanglong", "---2");
                messageBroadViewModel.playAudioMessage(toPlayAudioMessage);
            } else {
                messageBroadViewModel.downloadMedia(toPlayAudioMessage, file);
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
                adapterBroad.removeMessage(uiMessage);
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
                adapterBroad.removeMessageById(messageIds.get(i));
            }
        }
    };

    private Observer<Conversation> clearConversationMessageObserver = new Observer<Conversation>() {
        @Override
        public void onChanged(Conversation conversation) {
            if (conversation.equals(ConversationBroadFragment.this.conversationBroad)) {
                adapterBroad.setMessages(null);
                adapterBroad.notifyDataSetChanged();
            }
        }
    };

    private Observer<List<UserInfo>> userInfoUpdateLiveDataObserver = new Observer<List<UserInfo>>() {
        @Override
        public void onChanged(@Nullable List<UserInfo> userInfos) {
            if (conversationBroad == null) {
                return;
            }
            if (conversationBroad.type == Conversation.ConversationType.Single) {
                conversationBroadTitle = null;
                setTitle();
            }
            int start = layoutBroadManager.findFirstVisibleItemPosition();
            int end = layoutBroadManager.findLastVisibleItemPosition();
            adapterBroad.notifyItemRangeChanged(start, end - start + 1, userInfos);
        }
    };


    private Observer<Map<String, UserOnlineState>> userOnlineStateLiveDataObserver = new Observer<Map<String, UserOnlineState>>() {
        @Override
        public void onChanged(Map<String, UserOnlineState> userOnlineStateMap) {
            if (conversationBroad == null) {
                return;
            }
            if (conversationBroad.type == Conversation.ConversationType.Single) {
                conversationBroadTitle = null;
                setTitle();
            }
        }
    };

    private void initGroupObservers() {
        groupMembersUpdateBroadLiveDataObserver = groupMembers -> {
            if (groupMembers == null || groupInfoBroad == null) {
                return;
            }
            for (GroupMember member : groupMembers) {
                if (member.groupId.equals(groupInfoBroad.target) && member.memberId.equals(userBroadViewModel.getUserId())) {
                    groupMemberBroad = member;
                    updateGroupMuteStatus();
                    break;
                }
            }
        };

        groupInfosUpdateBroadLiveDataObserver = groupInfos -> {
            if (groupInfoBroad == null || groupInfos == null) {
                return;
            }
            for (GroupInfo info : groupInfos) {
                if (info.target.equals(groupInfoBroad.target)) {
                    groupInfoBroad = info;
                    updateGroupMuteStatus();
                    setTitle();
                    adapterBroad.notifyDataSetChanged();
                    break;
                }
            }
        };


        groupBroadViewModel.groupInfoUpdateLiveData().observeForever(groupInfosUpdateBroadLiveDataObserver);
        groupBroadViewModel.groupMembersUpdateLiveData().observeForever(groupMembersUpdateBroadLiveDataObserver);
    }

    private void unInitGroupObservers() {
        if (groupBroadViewModel == null) {
            return;
        }
        groupBroadViewModel.groupInfoUpdateLiveData().removeObserver(groupInfosUpdateBroadLiveDataObserver);
        groupBroadViewModel.groupMembersUpdateLiveData().removeObserver(groupMembersUpdateBroadLiveDataObserver);
    }

    private boolean isMessageInCurrentConversation(UiBroadMessage message) {
        if (conversationBroad == null || message == null || message.message == null) {
            return false;
        }
        return conversationBroad.equals(message.message.conversation);
    }

    public ConversationInputBroadPanel getConversationInputPanel() {
        return inputPanelBroad;
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
        gonggaoxinxiBroad = view.findViewById(R.id.gonggaoxinxi);
        tvQunMeetingBroad = view.findViewById(R.id.tvQunMeeting);
        qungonggaoBroad = view.findViewById(R.id.qungonggao);
        qunMeetingNoticeBroad = view.findViewById(R.id.qunMeetingNotice);
        rootLinearLayoutBroad = view.findViewById(R.id.rootLinearLayout);
        swipeRefreshLayoutBroad = view.findViewById(R.id.swipeRefreshLayout);
        verticalDanmakuView = view.findViewById(R.id.verticalDanmakuView);
        danmakuIv = view.findViewById(R.id.danmakuIv);
        recyclerViewBroad = view.findViewById(R.id.msgRecyclerView);
        ongoingCallRecyclerViewBroad = view.findViewById(R.id.ongoingCallRecyclerView);
        tvPhoneLoginBroad = view.findViewById(R.id.tvPhoneLogin);
        inputPanelBroad = view.findViewById(R.id.inputPanelFrameLayout);
        mViewInterceptBroad = view.findViewById(R.id.mViewIntercept);
        if ("tousu".equals(fromPage)) {
            tvPhoneLoginBroad.setVisibility(View.VISIBLE);
            inputPanelBroad.setVisibility(View.GONE);
            tvPhoneLoginBroad.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (adapterBroad != null && adapterBroad.getMessages() != null) {
                        List<UiBroadMessage> tousuMessages = new ArrayList<>();
                        for (int i = 0; i < adapterBroad.getMessages().size(); i++) {
                            if (adapterBroad.getMessages().get(i).isSelectTouSu) {
                                tousuMessages.add(adapterBroad.getMessages().get(i));
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
        multiMessageActionContainerLinearLayoutBroad = view.findViewById(R.id.multiMessageActionContainerLinearLayout);
        unreadCountLinearLayoutBroad = view.findViewById(R.id.unreadCountLinearLayout);
        unreadCountTextViewBroad = view.findViewById(R.id.unreadCountTextView);
        unreadMentionCountTextViewBroad = view.findViewById(R.id.unreadMentionCountTextView);

        view.findViewById(R.id.contentLayout).setOnTouchListener((v, event) -> ConversationBroadFragment.this.onTouch(v, event));
        recyclerViewBroad.setOnTouchListener((v, event) -> ConversationBroadFragment.this.onTouch(v, event));
        gonggaoxinxiBroad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (conversationBroad != null) {
                    MyCommonBroadBridge.getInstance().setMyBridgeMethod(getContext(), BridgeImEnum.VIEW_GROUP_ANNOUNCEMENT.getValue(), conversationBroad.target, gonggaoxinxiBroad.getText().toString(), null);
                }
            }
        });


    }

    @Override
    public void onResume() {
        super.onResume();
        if (conversationBroadViewModel != null && conversationBroad != null) {
            conversationBroadViewModel.clearUnreadStatus(conversationBroad);
        }
    }

    public void setupConversation(Conversation conversation, String title, long focusMessageId, String target) {
        setupConversation(conversation, title, focusMessageId, target, false);
    }

    public void setupConversation(Conversation conversation, String title, long focusMessageId, String target, boolean isJoinedChatRoom) {
        if (this.conversationBroad != null) {
            if ((this.conversationBroad.type == Conversation.ConversationType.Single && !ChatManager.Instance().isMyFriend(this.conversationBroad.target))
                    || this.conversationBroad.type == Conversation.ConversationType.Group) {
                userOnlineStateBroadViewModel.unwatchOnlineState(this.conversationBroad.type.getValue(), new String[]{this.conversationBroad.target});
            }
            this.adapterBroad = new ConversationMessageBroadAdapter(this);
            if ("tousu".equals(fromPage)) {
                this.adapterBroad.istousu = "1";
            }
            this.recyclerViewBroad.setAdapter(this.adapterBroad);
        }

        // TODO 要支持在线状态是才监听
        if ((conversation.type == Conversation.ConversationType.Single && !ChatManager.Instance().isMyFriend(conversation.target))
                || conversation.type == Conversation.ConversationType.Group) {
            userOnlineStateBroadViewModel.watchUserOnlineState(conversation.type.getValue(), new String[]{conversation.target});
        }

        this.conversationBroad = conversation;
        this.conversationBroadTitle = title;
        this.initialFocusedBroadMessageId = focusMessageId;
        this.channelPrivateBroadChatUser = target;
        this.isPreJoinedChatRoomBroad = isJoinedChatRoom;
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
        rootLinearLayoutBroad.addOnKeyboardShownListener(this);
//        verticalDanmakuView.setImageLoader((imageView, s) -> {
//            imageView.setVisibility(View.VISIBLE);
//            loadImg(imageView, s);
//            return null;
//        });
        click(danmakuIv, new Function0<Unit>() {
            @Override
            public Unit invoke() {
                DanMaPop danMaPop = new DanMaPop(getActivity());
                danMaPop.showPopupWindow(danmakuIv);
                return null;
            }
        });
        swipeRefreshLayoutBroad.setOnRefreshListener(() -> {
            if (adapterBroad.getMessages() == null || adapterBroad.getMessages().isEmpty()) {
                swipeRefreshLayoutBroad.setRefreshing(false);
                return;
            }
            loadMoreOldMessages();
        });

        // message list
        adapterBroad = new ConversationMessageBroadAdapter(this);
        if ("tousu".equals(fromPage)) {
            this.adapterBroad.istousu = "1";
        }
        adapterBroad.setOnPortraitClickListener(this);
        adapterBroad.setOnMessageReceiptClickListener(this);
        adapterBroad.setOnPortraitLongClickListener(this);
        adapterBroad.setOnMessageCheckListener(this);
        layoutBroadManager = new LinearLayoutManager(getActivity());
        recyclerViewBroad.setLayoutManager(layoutBroadManager);
        recyclerViewBroad.setAdapter(adapterBroad);
        ((SimpleItemAnimator) recyclerViewBroad.getItemAnimator()).setSupportsChangeAnimations(false);
        recyclerViewBroad.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                // 向上滑动，不在底部，收到消息时，不滑动到底部, 发送消息时，可以强制置为true
                if (newState != RecyclerView.SCROLL_STATE_IDLE) {
                    return;
                }
                if (!recyclerView.canScrollVertically(1)) {
                    moveToBottom = true;
                    if ((initialFocusedBroadMessageId != -1 || firstUnreadBroadMessageId != 0) && !loadingNewMessageBroad && shouldContinueLoadNewMessageBroad) {
                        int lastVisibleItem = layoutBroadManager.findLastCompletelyVisibleItemPosition();
                        if (lastVisibleItem > adapterBroad.getItemCount() - 3) {
                            loadMoreNewMessages();
                        }
                    }
                } else {
                    moveToBottom = false;
                }
            }
        });

        inputPanelBroad.init(this, rootLinearLayoutBroad, mViewInterceptBroad);
        inputPanelBroad.setOnConversationInputPanelStateChangeListener(this);

        conversationBroadViewModel = WfcUIBroadKit.getAppScopeViewModel(ConversationViewBroadModel.class);
        conversationBroadViewModel.clearConversationMessageLiveData().observeForever(clearConversationMessageObserver);
        conversationBroadViewModel.secretConversationStateLiveData().observe(getViewLifecycleOwner(), new Observer<Pair<String, ChatManager.SecretChatState>>() {
            @Override
            public void onChanged(Pair<String, ChatManager.SecretChatState> stringSecretChatStatePair) {
                if (conversationBroad != null && conversationBroad.type == Conversation.ConversationType.SecretChat && conversationBroad.target.equals(stringSecretChatStatePair.first)) {
                    reloadMessage();
                }
            }
        });
        messageBroadViewModel = new ViewModelProvider(this).get(MessageViewBroadModel.class);

        messageBroadViewModel.messageLiveData().observeForever(messageBroadLiveDataObserver);
        messageBroadViewModel.messageUpdateLiveData().observeForever(messageUpdateBroadLiveDatObserver);
        messageBroadViewModel.messageRemovedLiveData().observeForever(messageRemovedLiveDataObserver);
        messageBroadViewModel.messageStartBurnLiveData().observeForever(messageStartBurnLiveDataObserver);
        messageBroadViewModel.messageBurnedLiveData().observeForever(messageBurnedLiveDataObserver);

        messageBroadViewModel.messageReadLiveData().observe(getActivity(), readEntries -> {
            if (conversationBroad == null) {
                return;
            }
            Map<String, Long> convReadEntities = ChatManager.Instance().getConversationRead(conversationBroad);
            adapterBroad.setReadEntries(convReadEntities);
        });

        userBroadViewModel = new ViewModelProvider(this).get(UserViewBroadModel.class);
        userBroadViewModel.userInfoLiveData().observeForever(userInfoUpdateLiveDataObserver);

        settingUpdateBroadLiveDataObserver = o -> {
            if (groupInfoBroad == null) {
                return;
            }
            boolean show = "1".equals(userBroadViewModel.getUserSetting(UserSettingScope.GroupHideNickname, groupInfoBroad.target));
            if (showGroupMemberNameBroad != show) {
                showGroupMemberNameBroad = show;
                adapterBroad.notifyDataSetChanged();
            }
            reloadMessage();
        };

        settingBroadViewModel = new ViewModelProvider(this).get(SettingViewBroadModel.class);
        settingBroadViewModel.settingUpdatedLiveData().observeForever(settingUpdateBroadLiveDataObserver);

        userOnlineStateBroadViewModel = new ViewModelProvider(this).get(UserOnlineStateViewBroadModel.class);
        userOnlineStateBroadViewModel.getUserOnlineStateLiveData().observe(getViewLifecycleOwner(), userOnlineStateLiveDataObserver);


        if (backgroundImageBroadUri != null) {
            setConversationBackgroundImage(backgroundImageBroadUri);
        } else if (backgroundImageBroadResId != 0) {
            setConversationBackgroundImage(backgroundImageBroadResId);
        }
    }

    private void setupConversation(Conversation conversation) {
        if (conversation.type == Conversation.ConversationType.Group) {
            groupBroadViewModel = new ViewModelProvider(this).get(GroupViewBroadModel.class);
            initGroupObservers();
            groupBroadViewModel.getGroupMembers(conversation.target, true);
            groupInfoBroad = groupBroadViewModel.getGroupInfo(conversation.target, true);
            groupMemberBroad = groupBroadViewModel.getGroupMember(conversation.target, userBroadViewModel.getUserId());
            showGroupMemberNameBroad = "1".equals(userBroadViewModel.getUserSetting(UserSettingScope.GroupHideNickname, groupInfoBroad.target));

            updateGroupMuteStatus();
        }
        userBroadViewModel.getUserInfo(userBroadViewModel.getUserId(), true);

        inputPanelBroad.setupConversation(conversation);

        if (conversation.type != Conversation.ConversationType.ChatRoom) {
//            eLogD("zhanglong","setupConversation---"+conversation.target+"===="+initialFocusedMessageId);
            loadMessage(initialFocusedBroadMessageId);
        }
        if (conversation.type == Conversation.ConversationType.ChatRoom) {
            chatRoomBroadViewModel = new ViewModelProvider(this).get(ChatRoomViewBroadModel.class);
            if (!isPreJoinedChatRoomBroad) {
                joinChatRoom();
            } else {
                loadMoreOldMessages(true);
            }
        } else if (conversation.type == Conversation.ConversationType.Channel) {
            EnterChannelChatMessageContent content = new EnterChannelChatMessageContent();
            messageBroadViewModel.sendMessage(conversation, content);
        } else if (conversation.type == Conversation.ConversationType.Group) {
            messageBroadViewModel.getMsgDetailUserData(conversation.target);

        }

        ConversationInfo conversationInfo = ChatManager.Instance().getConversation(conversation);
        int unreadCount = conversationInfo.unreadCount.unread + conversationInfo.unreadCount.unreadMention + conversationInfo.unreadCount.unreadMentionAll;
        if (unreadCount > 10 && unreadCount < 300) {
            firstUnreadBroadMessageId = ChatManager.Instance().getFirstUnreadMessageId(conversation);
            showUnreadMessageCountLabel(unreadCount);
        }
        int aitunreadCount = conversationInfo.unreadCount.unreadMention + conversationInfo.unreadCount.unreadMentionAll;
        if (aitunreadCount > 0) {
            showUnreadMentionCountLabel(aitunreadCount);
        } else {
            hideUnreadMentionCountLabel();
        }

        conversationBroadViewModel.clearUnreadStatus(conversation);

        ongoingCallsBroad = null;

        setTitle();
    }

    public void setConversationBackgroundImage(String uri) {
        if (rootLinearLayoutBroad == null) {
            this.backgroundImageBroadUri = uri;
            return;
        }
        ScrollView scrollView = rootLinearLayoutBroad.findViewById(R.id.conversationBackgroundScrollView);
        ImageView imageView = rootLinearLayoutBroad.findViewById(R.id.conversationBackgroundImageView);
        scrollView.setVisibility(View.VISIBLE);
        Glide.with(this).load(uri).into(imageView);
    }

    public void setConversationBackgroundImage(int drawableId) {
        if (rootLinearLayoutBroad == null) {
            this.backgroundImageBroadResId = drawableId;
            return;
        }
        ScrollView scrollView = rootLinearLayoutBroad.findViewById(R.id.conversationBackgroundScrollView);
        ImageView imageView = rootLinearLayoutBroad.findViewById(R.id.conversationBackgroundImageView);
        scrollView.setVisibility(View.VISIBLE);
        Glide.with(this).load(backgroundImageBroadResId).into(imageView);
    }

    private void updateaita(long focusMessageId) {
        List<Message> oldMessageList = ChatManager.Instance().getMessages(conversationBroad, focusMessageId, true, 100, channelPrivateBroadChatUser);
        List<UiBroadMessage> oldMessages = new ArrayList<>();
        if (oldMessageList != null) {
            for (Message msg : oldMessageList) {
                if (conversationBroad.type == Conversation.ConversationType.SecretChat) {
                    BurnMessageInfo burnMessageInfo = ChatManager.Instance().getBurnMessageInfo(msg.messageId);
                    oldMessages.add(new UiBroadMessage(msg, burnMessageInfo));
                } else {
                    oldMessages.add(new UiBroadMessage(msg));
                }
            }
        }
        Message message = ChatManager.Instance().getMessage(focusMessageId);
        List<Message> newMessageList = ChatManager.Instance().getMessages(conversationBroad, focusMessageId, false, 100, channelPrivateBroadChatUser);
        List<UiBroadMessage> newMessages = new ArrayList<>();
        if (newMessageList != null) {
            for (Message msg : newMessageList) {
                if (conversationBroad.type == Conversation.ConversationType.SecretChat) {
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
            adapterBroad.setMessages(messages);
            adapterBroad.notifyDataSetChanged();

            if (adapterBroad.getItemCount() > 1) {
                int initialMessagePosition;
                if (focusMessageId != -1) {
                    initialMessagePosition = adapterBroad.getMessagePosition(focusMessageId);
                    if (initialMessagePosition != -1) {
                        recyclerViewBroad.scrollToPosition(initialMessagePosition);
                        adapterBroad.highlightFocusMessage(initialMessagePosition);
                        eLogD("野火", "被aita pos--> " + initialMessagePosition);
                    }
                } else {
                    moveToBottom = true;
                    recyclerViewBroad.scrollToPosition(adapterBroad.getItemCount() - 1);
                }
            }
        });

    }

    private void loadMessage(long focusMessageId) {

        MutableLiveData<List<UiBroadMessage>> messages;
        if (focusMessageId != -1) {
            shouldContinueLoadNewMessageBroad = true;
            messages = conversationBroadViewModel.loadAroundMessages(conversationBroad, channelPrivateBroadChatUser, focusMessageId, MESSAGE_BROAD_LOAD_AROUND);
        } else {
            messages = conversationBroadViewModel.getMessages(conversationBroad, channelPrivateBroadChatUser);
        }
        // load message
        swipeRefreshLayoutBroad.setRefreshing(true);
        swipeRefreshLayoutBroad.postDelayed(new Runnable() {
            @Override
            public void run() {
                //预防清空消息记录后下次进来远端拉去数据加载失败一直转圈圈
                swipeRefreshLayoutBroad.setRefreshing(false);
            }
        }, 3000L);
        adapterBroad.setReadEntries(ChatManager.Instance().getConversationRead(conversationBroad));
        messages.observe(this, uiMessages -> {
            swipeRefreshLayoutBroad.setRefreshing(false);

            adapterBroad.setMessages(uiMessages);
            adapterBroad.notifyDataSetChanged();

            if (adapterBroad.getItemCount() > 1) {
                int initialMessagePosition;
                if (focusMessageId != -1) {
                    initialMessagePosition = adapterBroad.getMessagePosition(focusMessageId);
                    if (initialMessagePosition != -1) {
                        recyclerViewBroad.scrollToPosition(initialMessagePosition);
                        adapterBroad.highlightFocusMessage(initialMessagePosition);
                    }
                } else {
                    moveToBottom = true;
                    recyclerViewBroad.scrollToPosition(adapterBroad.getItemCount() - 1);
                }
            }
        });
    }

    private void updateGroupMuteStatus() {
        if (groupInfoBroad == null || groupMemberBroad == null) {
            return;
        }
        if (groupInfoBroad.mute == 1
                && groupMemberBroad.type != GroupMember.GroupMemberType.Owner
                && groupMemberBroad.type != GroupMember.GroupMemberType.Manager
                && groupMemberBroad.type != GroupMember.GroupMemberType.Allowed) {
            inputPanelBroad.disableInput(getString(R.string.group_all_muted_hint));
        } else if (groupMemberBroad.type == GroupMember.GroupMemberType.Muted) {
            inputPanelBroad.disableInput(getString(R.string.group_self_muted_hint));
        } else {
            inputPanelBroad.enableInput();
        }
    }

    public void openMeetingDialog(String userIdNo) {

    }

    void onUnreadCountTextViewClick() {
        hideUnreadMessageCountLabel();
        shouldContinueLoadNewMessageBroad = true;
        loadMessage(firstUnreadBroadMessageId);
    }

    void aitonUnreadCountTextViewClick() {
        hideUnreadMentionCountLabel();
        try {
            ChatManager.Instance().mClient.getMentionedMessagesAsync(conversationBroad, 0, true, 100, new IGetMessageCallback.Stub() {
                @Override
                public void onSuccess(List<Message> messages, boolean hasMore) throws RemoteException {
                    if (messages != null && messages.size() > 0) {
                        shouldContinueLoadNewMessageBroad = true;
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
        unreadCountLinearLayoutBroad.setVisibility(View.VISIBLE);
        unreadCountTextViewBroad.setVisibility(View.VISIBLE);
        unreadCountTextViewBroad.setText(getString(R.string.unread_messages_count, count));
    }

    private void hideUnreadMessageCountLabel() {
        unreadCountTextViewBroad.setVisibility(View.GONE);
    }

    private void showUnreadMentionCountLabel(int count) {
        unreadCountLinearLayoutBroad.setVisibility(View.VISIBLE);
        unreadMentionCountTextViewBroad.setVisibility(View.VISIBLE);
        unreadMentionCountTextViewBroad.setText(getString(R.string.unread_mentions_count, count));
    }

    private void hideUnreadMentionCountLabel() {
        unreadMentionCountTextViewBroad.setVisibility(View.GONE);
    }

    private void joinChatRoom() {
        chatRoomBroadViewModel.joinChatRoom(conversationBroad.target)
                .observe(this, new Observer<OperateBroadResult<Boolean>>() {
                    @Override
                    public void onChanged(@Nullable OperateBroadResult<Boolean> booleanOperateResult) {
                        if (booleanOperateResult.isSuccess()) {
                            String welcome = getString(R.string.chatroom_welcome_format);
                            TipNotificationContent content = new TipNotificationContent();
                            String userId = userBroadViewModel.getUserId();
                            UserInfo userInfo = userBroadViewModel.getUserInfo(userId, false);
                            if (userInfo != null) {
                                content.tip = String.format(welcome, userBroadViewModel.getUserDisplayName(userInfo));
                            } else {
                                content.tip = String.format(welcome, "<" + userId + ">");
                            }
                            handlerBroad.postDelayed(() -> {
                                messageBroadViewModel.sendMessage(conversationBroad, content);
                            }, 1000);
                            setChatRoomConversationTitle();

                        } else {
                            Toast.makeText(getActivity(), R.string.chatroom_join_failed, Toast.LENGTH_SHORT).show();
                            getActivity().finish();
                        }
                    }
                });
    }

    private void quitChatRoom() {
        String welcome = getString(R.string.chatroom_leave_format);
        TipNotificationContent content = new TipNotificationContent();
        String userId = userBroadViewModel.getUserId();
        UserInfo userInfo = userBroadViewModel.getUserInfo(userId, false);
        if (userInfo != null) {
            content.tip = String.format(welcome, userBroadViewModel.getUserDisplayName(userInfo));
        } else {
            content.tip = String.format(welcome, "<" + userId + ">");
        }
        Message message = new Message();
        message.conversation = conversationBroad;
        message.content = content;
        messageBroadViewModel.sendMessageEx(message).observe(this, voidOperateResult -> chatRoomBroadViewModel.quitChatRoom(conversationBroad.target));
    }

    private void setChatRoomConversationTitle() {
        chatRoomBroadViewModel.getChatRoomInfo(conversationBroad.target, 0)
                .observe(this, chatRoomInfoOperateResult -> {
                    if (chatRoomInfoOperateResult.isSuccess()) {
                        ChatRoomInfo chatRoomInfo = chatRoomInfoOperateResult.getResult();
                        conversationBroadTitle = chatRoomInfo.title;
                        setActivityTitle(conversationBroadTitle);
                    }
                });
    }

    private void setTitle() {
        if (!TextUtils.isEmpty(conversationBroadTitle)) {
            setActivityTitle(conversationBroadTitle);
        }

        if (conversationBroad.type == Conversation.ConversationType.Single) {
            UserInfo userInfo = ChatManagerBroadHolder.gChatManager.getUserInfo(conversationBroad.target, false);
            GlobalBroadInfo.INSTANCE.queryMyFriendInfo(conversationBroad.target, null, new Function1<GlobalFriendBroadBean, Unit>() {
                @Override
                public Unit invoke(GlobalFriendBroadBean friendSmailListBean) {
                    if (MMKVBroadUtils.Companion.getKeFuId() != null && conversationBroad.target.contains(MMKVBroadUtils.Companion.getKeFuId())) {
                        conversationBroadTitle = MMKVBroadUtils.Companion.getKeFuName();
                    } else if (friendSmailListBean != null) {
                        if (!TextUtils.isEmpty(friendSmailListBean.remarkName)) {
                            conversationBroadTitle = friendSmailListBean.remarkName;
                        } else if (!TextUtils.isEmpty(friendSmailListBean.nickName)) {
                            conversationBroadTitle = friendSmailListBean.nickName;
                        } else {
                            if (userInfo == null) {
                                conversationBroadTitle = "";
                            }
                        }
                    } else if (userInfo != null && !TextUtils.isEmpty(userInfo.friendAlias)) {
                        conversationBroadTitle = userInfo.friendAlias;
                    } else if (userInfo != null && !(userInfo instanceof NullUserInfo) && !TextUtils.isEmpty(userInfo.displayName)) {
                        conversationBroadTitle = userInfo.displayName;
                    } else {
                        if (userInfo == null) {
                            conversationBroadTitle = "";
                        }
                    }
                    ELogUtilKt.eLogD("zhanglong", "conversationTitle===" + conversationBroadTitle);
                    UserOnlineState userOnlineState = ChatManager.Instance().getUserOnlineStateMap().get(userInfo.uid);
                    if (userOnlineState != null) {
                        String onlineDesc = userOnlineState.desc();
                        if (!TextUtils.isEmpty(onlineDesc)) {
                            conversationBroadTitle += " (" + onlineDesc + ")";
                        }
                    }
                    setActivityTitle(conversationBroadTitle);
                    return null;
                }
            });
        } else if (conversationBroad.type == Conversation.ConversationType.Group) {
            if (groupInfoBroad != null) {
                conversationBroadTitle = (!TextUtils.isEmpty(groupInfoBroad.remark) ? groupInfoBroad.remark : groupInfoBroad.name) + "(" + (groupInfoBroad.memberCount - 1) + getString(R.string.msg_person_unit) + ")";
            }
            setActivityTitle(conversationBroadTitle);
        } else if (conversationBroad.type == Conversation.ConversationType.Channel) {
            ChannelViewBroadModel channelViewModel = new ViewModelProvider(this).get(ChannelViewBroadModel.class);
            ChannelInfo channelInfo = channelViewModel.getChannelInfo(conversationBroad.target, false);
            if (channelInfo != null) {
                conversationBroadTitle = channelInfo.name;
            }

            if (!TextUtils.isEmpty(channelPrivateBroadChatUser)) {
                UserInfo channelPrivateChatUserInfo = userBroadViewModel.getUserInfo(channelPrivateBroadChatUser, false);
                if (channelPrivateChatUserInfo != null) {
                    conversationBroadTitle += "@" + userBroadViewModel.getUserDisplayName(channelPrivateChatUserInfo);
                } else {
                    conversationBroadTitle += "@<" + channelPrivateBroadChatUser + ">";
                }
            }
            setActivityTitle(conversationBroadTitle);
        } else if (conversationBroad.type == Conversation.ConversationType.SecretChat) {
            String userId = ChatManager.Instance().getSecretChatInfo(conversationBroad.target).getUserId();
            UserInfo userInfo = ChatManagerBroadHolder.gChatManager.getUserInfo(userId, false);
            conversationBroadTitle = userBroadViewModel.getUserDisplayName(userInfo);
            setActivityTitle(conversationBroadTitle);
        }


    }

    private void setActivityTitle(String title) {
        Activity activity = getActivity();
        if (activity != null) {
            activity.setTitle(title);
        }
        EventBus.getDefault().post(new MyBroadEvent(MyEventCodeEnum.SINGLE_CHAT_TITLE.getValue(), title, ""));
    }

    boolean onTouch(View view, MotionEvent event) {
//        if (event.getAction() == MotionEvent.ACTION_DOWN && inputPanel.extension.canHideOnScroll()) {
//            inputPanel.collapse();
//        }
        inputPanelBroad.closeConversationInputPanel();
        return false;
    }

    @Override
    public void onPortraitClick(UserInfo userInfo) {
        MyCommonBroadBridge.getInstance().setMyBridgeMethod(getContext(), BridgeImEnum.SINGLE_CHAT_MORE.getValue(), userInfo.uid, "", null);
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
        if (conversationBroad.type == Conversation.ConversationType.Group) {
            SpannableString spannableString = mentionSpannable(userInfo);
            int position = inputPanelBroad.editText.getSelectionEnd();
            inputPanelBroad.editText.getEditableText().append(" ");
            inputPanelBroad.editText.getEditableText().replace(position, position + 1, spannableString);
        } else {
            inputPanelBroad.editText.getEditableText().append(userBroadViewModel.getUserDisplayName(userInfo));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode >= ConversationBroadExtension.REQUEST_CODE_MIN) {
            boolean result = inputPanelBroad.extension.onActivityResult(requestCode, resultCode, data);
            if (result) {
                return;
            }
            eLogD(TAG, "extension can not handle " + requestCode);
        }
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_BROAD_PICK_MENTION_CONTACT) {
                boolean isMentionAll = data.getBooleanExtra("mentionAll", false);
                SpannableString spannableString;
                if (isMentionAll) {
                    spannableString = mentionAllSpannable();
                } else {
                    String userId = data.getStringExtra("userId");
                    UserInfo userInfo = userBroadViewModel.getUserInfo(userId, this.groupInfoBroad.target, false);
                    spannableString = mentionSpannable(userInfo);
                }
                int position = inputPanelBroad.editText.getSelectionEnd();
                position = position > 0 ? position - 1 : 0;
                inputPanelBroad.editText.getEditableText().replace(position, position + 1, spannableString);

            } else if (requestCode == REQUEST_BROAD_CODE_GROUP_AUDIO_CHAT || requestCode == REQUEST_BROAD_CODE_GROUP_VIDEO_CHAT) {
                onPickGroupMemberToVoipChat(data, requestCode == REQUEST_BROAD_CODE_GROUP_AUDIO_CHAT);
            }
        }
    }

    private SpannableString mentionAllSpannable() {
        String text = "@" + getString(R.string.msg_mention_all);
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
        inputPanelBroad.onActivityPause();
        messageBroadViewModel.stopPlayAudio();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        if (conversationBroad == null) {
            return;
        }

        if ((conversationBroad.type == Conversation.ConversationType.Single && !ChatManager.Instance().isMyFriend(conversationBroad.target))
                || conversationBroad.type == Conversation.ConversationType.Group) {
            userOnlineStateBroadViewModel.unwatchOnlineState(conversationBroad.type.getValue(), new String[]{conversationBroad.target});
        }

        if (conversationBroad.type == Conversation.ConversationType.ChatRoom) {
            if (!isPreJoinedChatRoomBroad) {
                quitChatRoom();
            }
        } else if (conversationBroad.type == Conversation.ConversationType.Channel) {
            LeaveChannelChatMessageContent content = new LeaveChannelChatMessageContent();
            messageBroadViewModel.sendMessage(conversationBroad, content);
        }

        messageBroadViewModel.messageLiveData().removeObserver(messageBroadLiveDataObserver);
        messageBroadViewModel.messageUpdateLiveData().removeObserver(messageUpdateBroadLiveDatObserver);
        messageBroadViewModel.messageRemovedLiveData().removeObserver(messageRemovedLiveDataObserver);
        userBroadViewModel.userInfoLiveData().removeObserver(userInfoUpdateLiveDataObserver);
        conversationBroadViewModel.clearConversationMessageLiveData().removeObserver(clearConversationMessageObserver);
        settingBroadViewModel.settingUpdatedLiveData().removeObserver(settingUpdateBroadLiveDataObserver);

        unInitGroupObservers();
        inputPanelBroad.onDestroy();

        // 退出密聊时，清空相关临时文件
        if (conversationBroad.type == Conversation.ConversationType.SecretChat) {
            List<UiBroadMessage> messages = adapterBroad.getMessages();
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
        adapterBroad.notifyDataSetChanged();
    }

    boolean onBackPressed() {
        boolean consumed = true;
        if (rootLinearLayoutBroad.getCurrentInput() != null) {
            rootLinearLayoutBroad.hideAttachedInput(true);
            inputPanelBroad.closeConversationInputPanel();
        } else if (multiMessageActionContainerLinearLayoutBroad.getVisibility() == View.VISIBLE) {
            toggleConversationMode();
        } else {
            consumed = false;
        }
        return consumed;
    }

    @Override
    public void onKeyboardShown() {
        inputPanelBroad.onKeyboardShown();
        recyclerViewBroad.scrollToPosition(adapterBroad.getItemCount() - 1);
    }

    @Override
    public void onKeyboardHidden() {
        inputPanelBroad.onKeyboardHidden();
    }

    public void reloadMessage() {
        conversationBroadViewModel.getMessages(conversationBroad, channelPrivateBroadChatUser).observe(this, uiMessages -> {
            adapterBroad.setMessages(uiMessages);
            adapterBroad.notifyDataSetChanged();
        });
    }

    public void reloadZhuanFaMessage() {
        conversationBroadViewModel.getMessages(conversationBroad, channelPrivateBroadChatUser).observe(this, uiMessages -> {
            for (int i = 0; i < uiMessages.size(); i++) {
                uiMessages.get(i).message.status = MessageStatus.Sent;
            }
            adapterBroad.setMessages(uiMessages);
            adapterBroad.notifyDataSetChanged();
        });
    }

    private void loadMoreOldMessages() {
        loadMoreOldMessages(false);
    }


    private void loadMoreOldMessages(boolean scrollToBottom) {
//eLogD("zhanglong","channelPrivateChatUser======"+channelPrivateChatUser+"==="+adapter.oldestMessageId+"===="+adapter.oldestMessageUid);
//eLogD("zhanglong","conversation=="+conversation.target+"==="+conversation.type);

        conversationBroadViewModel.loadOldMessages(conversationBroad, channelPrivateBroadChatUser, adapterBroad.oldestMessageId, adapterBroad.oldestMessageUid, MESSAGE_BROAD_LOAD_COUNT_PER_TIME)
                .observe(this, uiMessages -> {
//                    eLogD("zhanglong","loadOldMessages===uiMessages.size==="+uiMessages.size());

                    List<UiBroadMessage> messageList = adapterBroad.getMessages();
                    for (int i = 0; i < messageList.size(); i++) {
                        for (int j = 0; j < uiMessages.size(); j++) {
                            if (messageList.get(i).message.messageId == uiMessages.get(j).message.messageId) {
                                uiMessages.remove(j);
                                j--;
                            }
                        }
                    }

                    adapterBroad.addMessagesAtHead(uiMessages);
                    swipeRefreshLayoutBroad.setRefreshing(false);
                    if (scrollToBottom) {
                        recyclerViewBroad.scrollToPosition(adapterBroad.getItemCount() - 1);
                    }
                });
    }

    private void loadMoreNewMessages() {
        loadingNewMessageBroad = true;
        adapterBroad.showLoadingNewMessageProgressBar();
        conversationBroadViewModel.loadNewMessages(conversationBroad, channelPrivateBroadChatUser, adapterBroad.getItem(adapterBroad.getItemCount() - 2).message.messageId, MESSAGE_BROAD_LOAD_COUNT_PER_TIME)
                .observe(this, messages -> {
                    loadingNewMessageBroad = false;
                    adapterBroad.dismissLoadingNewMessageProgressBar();

                    if (messages == null || messages.isEmpty()) {
                        shouldContinueLoadNewMessageBroad = false;
                    }
                    if (messages != null && !messages.isEmpty()) {
                        adapterBroad.addMessagesAtTail(messages);
                    }
                });
    }

    private void updateTypingStatusTitle() {
        if (!isAdded()) {
            return;
        }
        if (this.typingMessageBroadMap == null || this.conversationBroad.type == Conversation.ConversationType.Channel || this.conversationBroad.type == Conversation.ConversationType.ChatRoom) {
            return;
        }
        this.checkUserTyping();
        String typingDesc = "";
        if (conversationBroad.type == Conversation.ConversationType.Group || typingMessageBroadMap.size() == 0) {
            this.resetConversationTitle();
            return;
        } else if (typingMessageBroadMap.size() == 1) {
            Set<String> users = typingMessageBroadMap.keySet();
            String userId = users.toArray(new String[1])[0];
            UserInfo userInfo;
            if (conversationBroad.type == Conversation.ConversationType.Group) {
                userInfo = ChatManager.Instance().getUserInfo(userId, conversationBroad.target, false);
                String userName = getString(R.string.typing_someone);
                if (!TextUtils.isEmpty(userInfo.friendAlias)) {
                    userName = userInfo.friendAlias;
                } else if (!TextUtils.isEmpty(userInfo.groupAlias)) {
                    userName = userInfo.groupAlias;
                } else if (!TextUtils.isEmpty(userInfo.displayName)) {
                    userName = userInfo.displayName;
                }

                TypingMessageContent typingMessageContent = (TypingMessageContent) typingMessageBroadMap.get(userId).content;
                switch (typingMessageContent.getTypingType()) {
                    case TypingMessageContent.TYPING_TEXT:
                        typingDesc = getString(R.string.typing_group_text, userName);
                        break;
                    case TypingMessageContent.TYPING_VOICE:
                        typingDesc = getString(R.string.typing_group_voice, userName);
                        break;
                    case TypingMessageContent.TYPING_CAMERA:
                        typingDesc = getString(R.string.typing_group_camera, userName);
                        break;
                    case TypingMessageContent.TYPING_FILE:
                        typingDesc = getString(R.string.typing_group_file, userName);
                        break;
                    case TypingMessageContent.TYPING_LOCATION:
                        typingDesc = getString(R.string.typing_group_location, userName);
                        break;
                    default:
                        typingDesc = "unknown";
                        break;
                }
            } else {
                TypingMessageContent typingMessageContent = (TypingMessageContent) typingMessageBroadMap.get(userId).content;
                switch (typingMessageContent.getTypingType()) {
                    case TypingMessageContent.TYPING_TEXT:
                        typingDesc = getString(R.string.typing_single_text);
                        break;
                    case TypingMessageContent.TYPING_VOICE:
                        typingDesc = getString(R.string.typing_single_voice);
                        break;
                    case TypingMessageContent.TYPING_CAMERA:
                        typingDesc = getString(R.string.typing_single_camera);
                        break;
                    case TypingMessageContent.TYPING_FILE:
                        typingDesc = getString(R.string.typing_single_file);
                        break;
                    case TypingMessageContent.TYPING_LOCATION:
                        typingDesc = getString(R.string.typing_single_location);
                        break;
                    default:
                        typingDesc = "unknown";
                        break;
                }
            }
        } else {
            typingDesc = getString(R.string.typing_multi_users, typingMessageBroadMap.size());
        }
        setActivityTitle(typingDesc);
        handlerBroad.postDelayed(this::updateTypingStatusTitle, 5000);
    }

    private void checkUserTyping() {
        Set<Map.Entry<String, Message>> entries = typingMessageBroadMap.entrySet();
        Iterator<Map.Entry<String, Message>> iterator = entries.iterator();
        long now = System.currentTimeMillis();
        while (iterator.hasNext()) {
            Map.Entry<String, Message> next = iterator.next();
            Message msg = next.getValue();
            if (now - msg.serverTime + ChatManager.Instance().getServerDeltaTime() > TYPING_BROAD_INTERNAL) {
                iterator.remove();
            }
        }
    }

    private void resetConversationTitle() {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }
        if (!TextUtils.equals(conversationBroadTitle, getActivity().getTitle())) {
            setActivityTitle(conversationBroadTitle);
        }
    }

    @Override
    public void onInputPanelExpanded() {
        recyclerViewBroad.scrollToPosition(adapterBroad.getItemCount() - 1);
    }

    @Override
    public void onInputPanelCollapsed() {
        // do nothing
    }

    public void toggleMultiMessageMode(UiBroadMessage message) {
        inputPanelBroad.setVisibility(View.GONE);
        message.isChecked = true;
        adapterBroad.setMode(ConversationMessageBroadAdapter.MODE_CHECKABLE);
        adapterBroad.notifyDataSetChanged();
        multiMessageActionContainerLinearLayoutBroad.setVisibility(View.VISIBLE);
        setupMultiMessageAction();
    }

    public void toggleConversationMode() {
        inputPanelBroad.setVisibility(View.VISIBLE);
        multiMessageActionContainerLinearLayoutBroad.setVisibility(View.GONE);
        adapterBroad.setMode(ConversationMessageBroadAdapter.MODE_NORMAL);
        adapterBroad.clearMessageCheckStatus();
        adapterBroad.notifyDataSetChanged();
    }

    public void setInputText(String text) {
        inputPanelBroad.setInputText(text);
    }

    private void setupMultiMessageAction() {
        multiMessageActionContainerLinearLayoutBroad.removeAllViews();
        List<MultiMessageBroadAction> actions = MultiMessageActionBroadManager.getInstance().getConversationActions(conversationBroad);
        View inflateView = getLayoutInflater().inflate(R.layout.conversation_layout_select_more, multiMessageActionContainerLinearLayoutBroad, false);
        inflateView.findViewById(R.id.mLLShare).setOnClickListener(new OnSingleClickBroadListener() {
            @Override
            public void onSingleClick(View v) {
                if (!actions.isEmpty() && actions.size() > 1) {
                    goSelectMoreAction(actions.get(1));
                }
            }
        });

        inflateView.findViewById(R.id.mLLDelete).setOnClickListener(new OnSingleClickBroadListener() {
            @Override
            public void onSingleClick(View v) {
                if (!actions.isEmpty()) {
                    goSelectMoreAction(actions.get(0));
                }
            }
        });
        multiMessageActionContainerLinearLayoutBroad.addView(inflateView);
    }

    private void goSelectMoreAction(MultiMessageBroadAction action) {
        action.onBind(ConversationBroadFragment.this, conversationBroad);
        List<UiBroadMessage> checkedMessages = adapterBroad.getCheckedMessages();
        if (checkedMessages.isEmpty()) {
            toggleConversationMode();
        } else if (action.confirm()) {
            new MaterialDialog.Builder(getActivity()).content(action.confirmPrompt())
                    .negativeText(getString(R.string.dialog_cancel))
                    .positiveText(getString(R.string.dialog_confirm))
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
    }

    @Override
    public void onMessageCheck(UiBroadMessage message, boolean checked) {
        //此处逻辑代码没什么用，会导致多选功能没选时无法点击关闭，直接注释
//        List<UiBroadMessage> checkedMessages = adapterBroad.getCheckedMessages();
//        setAllClickableChildViewState(multiMessageActionContainerLinearLayoutBroad, checkedMessages.size() > 0);
    }

//    private void setAllClickableChildViewState(View view, boolean enable) {
//        if (view instanceof ViewGroup) {
//            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
//                setAllClickableChildViewState(((ViewGroup) view).getChildAt(i), enable);
//            }
//        }
//        if (view.isClickable()) {
//            view.setEnabled(enable);
//        }
//    }

    public void pickGroupMemberToVoipChat(boolean isAudioOnly) {
        Intent intent = new Intent(getActivity(), PickGroupMemberBroadActivity.class);
        GroupInfo groupInfo = groupBroadViewModel.getGroupInfo(conversationBroad.target, false);
        intent.putExtra("groupInfo", groupInfo);
        int maxCount = AVEngineKit.isSupportMultiCall() ? (isAudioOnly ? AVEngineKit.MAX_AUDIO_PARTICIPANT_COUNT - 1 : AVEngineKit.MAX_VIDEO_PARTICIPANT_COUNT - 1) : 1;
        intent.putExtra("maxCount", maxCount);
        startActivityForResult(intent, isAudioOnly ? REQUEST_BROAD_CODE_GROUP_AUDIO_CHAT : REQUEST_BROAD_CODE_GROUP_VIDEO_CHAT);
    }

    private void onPickGroupMemberToVoipChat(Intent intent, boolean isAudioOnly) {
        List<String> memberIds = intent.getStringArrayListExtra(PickGroupMemberBroadActivity.EXTRA_RESULT);
        if (memberIds != null && memberIds.size() > 0) {
            if (AVEngineKit.isSupportMultiCall()) {
                WfcUIBroadKit.multiCall(getActivity(), conversationBroad.target, memberIds, isAudioOnly);
            } else {
                WfcUIBroadKit.singleCall(getActivity(), memberIds.get(0), isAudioOnly);
            }
        }
    }

    @Override
    public void onMessageReceiptCLick(Message message) {
        Intent intent = new Intent(getActivity(), GroupMessageReceiptBroadActivity.class);
        intent.putExtra("message", message);
        intent.putExtra("groupInfo", groupInfoBroad);
        startActivity(intent);
    }

    private void cleanExpiredOngoingCalls() {
        for (Iterator<Map.Entry<String, Message>> it = ongoingCallsBroad.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, Message> entry = it.next();
            if (System.currentTimeMillis() - (entry.getValue().serverTime + ChatManager.Instance().getServerDeltaTime()) > 3000) {
                it.remove();
            }
        }
        if (ongoingCallsBroad.size() > 0) {
            handlerBroad.postDelayed(this::cleanExpiredOngoingCalls, 1000);
        } else {
            ongoingCallAdapterBroad.setOngoingCalls(null);
            ongoingCallRecyclerViewBroad.setVisibility(View.GONE);
        }
    }
}
