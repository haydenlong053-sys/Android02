/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversation;

import static com.cboda.baselibrary.utils.ELogUtilKt.eLogD;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.cboda.baselibrary.bean.GlobalFriendBroadBean;
import com.cboda.baselibrary.bean.GlobalBroadInfo;
import com.cboda.baselibrary.utils.MMKVBroadUtils;
import com.blankj.utilcode.util.GsonUtils;
import com.google.gson.Gson;
import com.cboda.chatemoji.EmotionBroadEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import com.cboda.chatui.IMServiceStatusViewBroadModel;
import com.cboda.chatui.R;
import com.cboda.chatui.WfcBaseBroadActivity;
import com.cboda.chatui.bridge.MyCommonBroadResult;
import com.cboda.chatui.bridge.QunBroadXiangqing;
import com.cboda.chatui.bridge.MyCommonBroadBridge;
import com.cboda.chatui.bridge.MyBroadEvent;

import cn.wildfirechat.message.BiaoQingMessageContent;
import cn.wildfirechat.message.MeetingInviteMessageContent;
import cn.wildfirechat.message.Message;
import cn.wildfirechat.message.ZhuanZhangMessageContent;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.remote.ChatManager;
import cn.wildfirechat.remote.SendMessageCallback;

import com.cboda.baselibrary.common.BridgeImEnum;
import com.cboda.baselibrary.common.MyEventCodeEnum;

public class ConversationBroadActivity extends WfcBaseBroadActivity {
    private boolean isInitialized = false;
    private ConversationBroadFragment conversationBroadFragment;
    private Conversation conversationBroad;
    private View pageTitleLeftIcon;
    private View pageTitleRightIcon, mIvBack2, rightView;
    private View titleGradeAndTimeBroad;
    private TextView pageTitle;
    private String fromPageBroad;

    @Override
    protected int contentLayout() {
        return R.layout.fragment_container_broad_activity;
    }

    private void setConversationBackground() {
        // 设置聊天背景
//        conversationFragment.setConversationBackgroundImage("https://static.wildfirechat.net/web_wfc_bg2.jpeg");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (null != conversationBroad && null != conversationBroad.type && null != conversationBroad.target) {
            if (conversationBroad.type == Conversation.ConversationType.Group) {
                titleGradeAndTimeBroad.setVisibility(View.GONE);
//            MyCommonBroadBridge.getInstance().setMyQiaoJie(ConversationBroadActivity.this,"huoququnxiangqing",conversation.target,"",null);
                MyCommonBroadBridge.getInstance().setMyBridgeMethod(ConversationBroadActivity.this, BridgeImEnum.GET_GROUP_MEETING.getValue(), conversationBroad.target, "", null);
            } else if (conversationBroad.type == Conversation.ConversationType.Single) {
                getUserData();
            }
        }
    }

    private Gson gson = GsonUtils.getGson();

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMsgReceive(MyBroadEvent myImEvent) {
        if (MyEventCodeEnum.SINGLE_CHAT_TITLE.getValue().equals(myImEvent.type)) {
//            String params1 = myImEvent.params1;
//            eLogD("onMsgReceiveClass", "params1:" + params1);
            pageTitle.setText(myImEvent.params1);
        } else if (MyEventCodeEnum.RELOAD_MESSAGE.getValue().equals(myImEvent.type)) {
            conversationBroadFragment.reloadMessage();
        } else if (MyEventCodeEnum.RELOAD_FORWARD_MESSAGE.getValue().equals(myImEvent.type)) {
            conversationBroadFragment.reloadZhuanFaMessage();
        } else if (MyEventCodeEnum.GROUP_DETAILS.getValue().equals(myImEvent.type)) {
            QunBroadXiangqing qunXiangqing = gson.fromJson(myImEvent.params1, QunBroadXiangqing.class);
            if (qunXiangqing != null && !TextUtils.isEmpty(qunXiangqing.getGonggao())) {
                conversationBroadFragment.qungonggaoBroad.setVisibility(View.VISIBLE);
                conversationBroadFragment.gonggaoxinxiBroad.setText(getString(R.string.group_announcement_prefix) + qunXiangqing.getGonggao());
            } else {
                conversationBroadFragment.qungonggaoBroad.setVisibility(View.GONE);
            }

            if (conversationBroad.type == Conversation.ConversationType.Group) {
                if (qunXiangqing != null && conversationBroadFragment.messageBroadViewModel != null) {
                    if ("3".equals(qunXiangqing.getUserType())) {
                        conversationBroadFragment.messageBroadViewModel.shifouLanjie = true;
                    } else {
                        conversationBroadFragment.messageBroadViewModel.shifouLanjie = false;
                    }
                    conversationBroadFragment.messageBroadViewModel.context = ConversationBroadActivity.this;
                    conversationBroadFragment.messageBroadViewModel.jiangeshijan = qunXiangqing.getJiange();
                    if ("1".equals(qunXiangqing.getUserType())) {
                        conversationBroadFragment.messageBroadViewModel.shifoumianci = false;
                    } else {
                        if (!TextUtils.isEmpty(qunXiangqing.getMgc())) {
                            conversationBroadFragment.messageBroadViewModel.mgc = qunXiangqing.getMgc();
                            conversationBroadFragment.messageBroadViewModel.openMgc = qunXiangqing.getOpenMgc();
                            conversationBroadFragment.messageBroadViewModel.shifoumianci = true;
                        } else {
                            conversationBroadFragment.messageBroadViewModel.shifoumianci = false;
                        }
                    }
                }
            }

        } else if (MyEventCodeEnum.UPDATE_ANNOUNCEMENT.getValue().equals(myImEvent.type)) {
            if (conversationBroad.target.equals(myImEvent.params2)) {
                conversationBroadFragment.qungonggaoBroad.setVisibility(View.VISIBLE);
                conversationBroadFragment.gonggaoxinxiBroad.setText(getString(R.string.group_announcement_prefix) + myImEvent.params1);
                MyCommonBroadBridge.getInstance().setMyBridgeMethod(ConversationBroadActivity.this, BridgeImEnum.UPDATE_ANNOUNCEMENT.getValue(), myImEvent.params1, "", null);
            }

        } else if (MyEventCodeEnum.USER_INFO.getValue().equals(myImEvent.type)) {
            MyCommonBroadResult yoinghu = gson.fromJson(myImEvent.params1, MyCommonBroadResult.class);
            if (yoinghu != null && !TextUtils.isEmpty(yoinghu.getChatImg())) {
                conversationBroadFragment.setConversationBackgroundImage(yoinghu.getChatImg());
            }
        } else if (MyEventCodeEnum.GROUP_DETAILS_FAILED.getValue().equals(myImEvent.type)) {
            pageTitleRightIcon.setVisibility(View.GONE);
            mIvBack2.setVisibility(View.GONE);
            rightView.setVisibility(View.VISIBLE);
        } else if (MyEventCodeEnum.UPDATE_SEND_TIME.getValue().equals(myImEvent.type)) {
            MyCommonBroadBridge.getInstance().setMyBridgeMethod(ConversationBroadActivity.this, BridgeImEnum.GET_GROUP_DETAILS.getValue(), conversationBroad.target, "", null);
        } else if (MyEventCodeEnum.BANNED.getValue().equals(myImEvent.type)) {
            MyCommonBroadBridge.getInstance().setMyBridgeMethod(ConversationBroadActivity.this, BridgeImEnum.BANNED.getValue(), conversationBroad.target, "", null);
        } else if (MyEventCodeEnum.SEND_RECEIVE_TRANSFER_MESSAGE.getValue().equals(myImEvent.type)) {
            ZhuanZhangMessageContent cardMessageContent = new ZhuanZhangMessageContent(myImEvent.params1, myImEvent.params2, "", "1");
            Message msg = new Message();
            msg.conversation = conversationBroad;
            msg.content = cardMessageContent;
            msg.sender = ChatManager.Instance().getUserId();
            ChatManager.Instance().sendMessage(msg, new SendMessageCallback() {
                @Override
                public void onSuccess(long messageUid, long timestamp) {
                    eLogD("野火", "发送接受转账消息onSuccess---> ");
                }

                @Override
                public void onFail(int errorCode) {
                    eLogD("野火", "发送接受转账消息onFail---> " + errorCode);
                }

                @Override
                public void onPrepare(long messageId, long savedTime) {

                }
            });
        } else if (MyEventCodeEnum.NOT_FRIEND_SEND_MESSAGE.getValue().equals(myImEvent.type)) {
            new MaterialDialog.Builder(ConversationBroadActivity.this)
                    .content(getString(R.string.not_friend_send_message))
                    .negativeText(getString(R.string.quxiao))
                    .positiveText(getString(R.string.queding))
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            MyCommonBroadBridge.getInstance().setMyBridgeMethod(ConversationBroadActivity.this, BridgeImEnum.ADD_FRIEND.getValue(), conversationBroad.target, "", null);
                        }
                    })
                    .build()
                    .show();
        } else if (MyEventCodeEnum.MEETING_ROOM.getValue().equals(myImEvent.type)) {
            String json = myImEvent.params1;
            MeetingInviteMessageContent inviteMessageContent =
                    new Gson().fromJson(json, MeetingInviteMessageContent.class);
            if (inviteMessageContent != null && conversationBroadFragment != null) {
                conversationBroadFragment.messageBroadViewModel.sendMessage(conversationBroad, inviteMessageContent);
            }


        } else if (MyEventCodeEnum.GROUP_MEETING.getValue().equals(myImEvent.type)) {
            String roomId = myImEvent.params1;
            String jsonParams = myImEvent.params2;
            if (conversationBroadFragment != null) {
                conversationBroadFragment.qunMeetingNoticeBroad.setVisibility(roomId.isEmpty() ? View.GONE : View.VISIBLE);
                conversationBroadFragment.tvQunMeetingBroad.setOnClickListener(v -> {
                    MyCommonBroadBridge.getInstance().setMyBridgeMethod(this, BridgeImEnum.ENTER_MEETING.getValue(), roomId, jsonParams, null);

                });
            }


        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMsgReceive(EmotionBroadEvent myImEvent) {
        if (MyEventCodeEnum.SEND_EMOJI.getValue().equals(myImEvent.type)) {
            BiaoQingMessageContent biaoQingMessageContent = new BiaoQingMessageContent(myImEvent.params);
            Message msg = new Message();
            msg.conversation = conversationBroad;
            msg.content = biaoQingMessageContent;
            msg.sender = ChatManager.Instance().getUserId();
            ChatManager.Instance().sendMessage(msg, new SendMessageCallback() {
                @Override
                public void onSuccess(long messageUid, long timestamp) {
                    eLogD("野火", "发送表情-->onSuccess");
                }

                @Override
                public void onFail(int errorCode) {
                    eLogD("野火", "发送表情-->onFail:: " + errorCode);
                }

                @Override
                public void onPrepare(long messageId, long savedTime) {

                }
            });

        }
    }

    @SuppressLint("SetTextI18n")
    private void getUserData() {
        if (MMKVBroadUtils.Companion.getKeFuId() != null && conversationBroad.target.contains(MMKVBroadUtils.Companion.getKeFuId())) {
//            MyCommonBroadBridge.getInstance().setMyQiaoJie(ConversationBroadActivity.this, "getUserData", conversation.target, "", null);
            titleGradeAndTimeBroad.setVisibility(View.GONE);
            pageTitleRightIcon.setVisibility(View.GONE);
            mIvBack2.setVisibility(View.GONE);
            rightView.setVisibility(View.VISIBLE);
        } else {
            MyCommonBroadBridge.getInstance().setMyBridgeMethod(ConversationBroadActivity.this, BridgeImEnum.GET_USER_DATA.getValue(), conversationBroad.target, "", null);
            GlobalFriendBroadBean bean = GlobalBroadInfo.INSTANCE.queryMyFriendInfo(conversationBroad.target);
            if (null != bean && null != bean.grade && null != bean.registerDay) {
                titleGradeAndTimeBroad.setVisibility(View.VISIBLE);
                TextView titleGrade = findViewById(R.id.titleGrade);
                TextView titleTime = findViewById(R.id.titleTime);
                titleGrade.setText(getString(R.string.credit_prefix) + bean.grade + getString(R.string.msg_score_unit2));
                titleTime.setText(getString(R.string.register_time_prefix) + bean.registerDay + getString(R.string.msg_days_unit2));
            } else {
                titleGradeAndTimeBroad.setVisibility(View.GONE);
            }
        }
    }


    @Override
    protected void bindViews() {
        super.bindViews();
        titleGradeAndTimeBroad = findViewById(R.id.titleGradeAndTime);
    }

    @Override
    protected void beforeViews() {
        super.beforeViews();
        eLogD("ConversationActivityClass", "initData");
    }

    @Override
    protected void afterViews() {
        fromPageBroad = getIntent().getStringExtra("frompage");
        EventBus.getDefault().register(this);
        pageTitleLeftIcon = findViewById(R.id.page_title_left_icon);
        pageTitleLeftIcon.setOnClickListener(v -> finish());
        pageTitleRightIcon = findViewById(R.id.page_title_right_icon);
        mIvBack2 = findViewById(R.id.mIvBack2);
        rightView = findViewById(R.id.rightView);
        pageTitleRightIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (conversationBroad != null) {
                    if (conversationBroad.type == Conversation.ConversationType.Single) {
                        MyCommonBroadBridge.getInstance().setMyBridgeMethod(ConversationBroadActivity.this, BridgeImEnum.SINGLE_CHAT_MORE.getValue(), conversationBroad.target, "", null);
                    } else {
                        MyCommonBroadBridge.getInstance().setMyBridgeMethod(ConversationBroadActivity.this, BridgeImEnum.GROUP_CHAT_HOME.getValue(), conversationBroad.target, "", null);
                    }

                }


            }
        });


        pageTitle = findViewById(R.id.page_title);


        IMServiceStatusViewBroadModel imServiceStatusViewModel = new ViewModelProvider(this).get(IMServiceStatusViewBroadModel.class);
        imServiceStatusViewModel.imServiceStatusLiveData().observe(this, aBoolean -> {
            if (!isInitialized && aBoolean) {
                init();
                isInitialized = true;
            }
        });
        conversationBroadFragment = new ConversationBroadFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.containerFrameLayout, conversationBroadFragment, "content")
                .commit();

        setConversationBackground();
        if (MyEventCodeEnum.COMPLAINT.getValue().equals(fromPageBroad)) {
            pageTitleRightIcon.setVisibility(View.GONE);
            mIvBack2.setVisibility(View.GONE);
            rightView.setVisibility(View.VISIBLE);
            conversationBroadFragment.fromPage = fromPageBroad;

        }


    }


    public ConversationBroadFragment getConversationBroadFragment() {
        return conversationBroadFragment;
    }


    @Override
    public void onBackPressed() {
        if (!conversationBroadFragment.onBackPressed()) {
            super.onBackPressed();
        }
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        conversationBroad = intent.getParcelableExtra("conversation");
        if (conversationBroad == null) {
            finish();
            return;
        }
        long initialFocusedMessageId = intent.getLongExtra("toFocusMessageId", -1);
        String channelPrivateChatUser = intent.getStringExtra("channelPrivateChatUser");
        String conversationTitle = intent.getStringExtra("conversationTitle");
        boolean isPreJoinedChatRoom = intent.getBooleanExtra("isPreJoinedChatRoom", false);
        conversationBroadFragment.setupConversation(conversationBroad, conversationTitle, initialFocusedMessageId, channelPrivateChatUser, isPreJoinedChatRoom);
    }

    private void init() {
        Intent intent = getIntent();
        conversationBroad = intent.getParcelableExtra("conversation");
        String conversationTitle = intent.getStringExtra("conversationTitle");
        boolean isPreJoinedChatRoom = intent.getBooleanExtra("isPreJoinedChatRoom", false);
        //野火闪烁
        long initialFocusedMessageId = intent.getLongExtra("toFocusMessageId", -1);
        if (conversationBroad == null) {
            finish();
            return;
        }
        if (conversationBroad.type == Conversation.ConversationType.Group) {
            MyCommonBroadBridge.getInstance().setMyBridgeMethod(ConversationBroadActivity.this, BridgeImEnum.GET_GROUP_DETAILS.getValue(), conversationBroad.target, "", null);
            MyCommonBroadBridge.getInstance().setMyBridgeMethod(ConversationBroadActivity.this, BridgeImEnum.GET_GROUP_MEETING.getValue(), conversationBroad.target, "", null);
        }


        MyCommonBroadBridge.getInstance().setMyBridgeMethod(ConversationBroadActivity.this, BridgeImEnum.GET_USER_INFO.getValue(), conversationBroad.target, "", null);
        ChatManager.Instance().shangcishijan = 0L;
        conversationBroadFragment.setupConversation(conversationBroad, conversationTitle, initialFocusedMessageId, null, isPreJoinedChatRoom);

        if (conversationBroad != null) {
            if (conversationBroad.type == Conversation.ConversationType.Single)
                MyCommonBroadBridge.getInstance().setMyBridgeMethod(ConversationBroadActivity.this, BridgeImEnum.MUTE_INFO.getValue(), conversationBroad.target, "0", null);
            else
                MyCommonBroadBridge.getInstance().setMyBridgeMethod(ConversationBroadActivity.this, BridgeImEnum.MUTE_INFO.getValue(), conversationBroad.target, "1", null);
        }

    }

    public static Intent buildConversationIntent(Context context, Conversation.ConversationType type, String target, int line) {
        return buildConversationIntent(context, type, target, line, -1);
    }

    public static Intent buildConversationIntent(Context context, Conversation.ConversationType type, String target, int line, long toFocusMessageId) {
        Conversation conversation = new Conversation(type, target, line);
        return buildConversationIntent(context, conversation, null, toFocusMessageId);
    }

    public static Intent buildConversationIntent(Context context, Conversation.ConversationType type, String target, int line, String channelPrivateChatUser) {
        Conversation conversation = new Conversation(type, target, line);
        return buildConversationIntent(context, conversation, null, -1);
    }

    public static Intent buildChatRoomConversationIntent(Context context, String chatRoomId, int line, String title, boolean joined) {
        Conversation conversation = new Conversation(Conversation.ConversationType.ChatRoom, chatRoomId, line);
        Intent intent = new Intent(context, ConversationBroadActivity.class);
        intent.putExtra("conversation", conversation);
        intent.putExtra("conversationTitle", title);
        intent.putExtra("isPreJoinedChatRoom", joined);
        return intent;
    }

    public static Intent buildConversationIntent(Context context, Conversation conversation, String channelPrivateChatUser, long toFocusMessageId) {
        Intent intent = new Intent(context, ConversationBroadActivity.class);
        intent.putExtra("conversation", conversation);
        intent.putExtra("toFocusMessageId", toFocusMessageId);
        intent.putExtra("channelPrivateChatUser", channelPrivateChatUser);
        return intent;
    }
}
