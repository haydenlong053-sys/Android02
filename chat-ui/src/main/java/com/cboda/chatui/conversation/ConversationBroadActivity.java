/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversation;

import static com.cboda.baselibrary.utils.ELogUtilKt.eLogD;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProviders;

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

public class ConversationBroadActivity extends WfcBaseBroadActivity {
    private boolean isInitialized = false;
    private ConversationBroadFragment conversationFragment;
    private Conversation conversation;
    private View page_title_left_icon;
    private View page_title_right_icon;
    private View titleGradeAndTime;
    private TextView page_title;
    private String frompage;

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
        if (null != conversation && null != conversation.type && null != conversation.target) {
            if (conversation.type == Conversation.ConversationType.Group) {
                titleGradeAndTime.setVisibility(View.GONE);
//            MyCommonBroadBridge.getInstance().setMyQiaoJie(ConversationBroadActivity.this,"huoququnxiangqing",conversation.target,"",null);
                MyCommonBroadBridge.getInstance().setMyBridgeMethod(ConversationBroadActivity.this, BridgeImEnum.GET_GROUP_MEETING.getValue(), conversation.target, "", null);
            } else if (conversation.type == Conversation.ConversationType.Single) {
                getUserData();
            }
        }
    }

    private Gson gson = GsonUtils.getGson();

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMsgReceive(MyBroadEvent myImEvent) {
        if ("danliaobiaoti".equals(myImEvent.type)) {
//            String params1 = myImEvent.params1;
//            eLogD("onMsgReceiveClass", "params1:" + params1);
            page_title.setText(myImEvent.params1);
        } else if ("reloadMsg".equals(myImEvent.type)) {
            conversationFragment.reloadMessage();
        } else if ("reloadMsgZhuanFa".equals(myImEvent.type)) {
            conversationFragment.reloadZhuanFaMessage();
        } else if ("qunxiangqing".equals(myImEvent.type)) {
            QunBroadXiangqing qunXiangqing = gson.fromJson(myImEvent.params1, QunBroadXiangqing.class);
            if (qunXiangqing != null && !TextUtils.isEmpty(qunXiangqing.getGonggao())) {
                conversationFragment.qungonggao.setVisibility(View.VISIBLE);
                conversationFragment.gonggaoxinxi.setText("群公告：" + qunXiangqing.getGonggao());
            } else {
                conversationFragment.qungonggao.setVisibility(View.GONE);
            }

            if (conversation.type == Conversation.ConversationType.Group) {
                if (qunXiangqing != null && conversationFragment.messageViewModel != null) {
                    if ("3".equals(qunXiangqing.getUserType())) {
                        conversationFragment.messageViewModel.shifouLanjie = true;
                    } else {
                        conversationFragment.messageViewModel.shifouLanjie = false;
                    }
                    conversationFragment.messageViewModel.context = ConversationBroadActivity.this;
                    conversationFragment.messageViewModel.jiangeshijan = qunXiangqing.getJiange();
                    if ("1".equals(qunXiangqing.getUserType())) {
                        conversationFragment.messageViewModel.shifoumianci = false;
                    } else {
                        if (!TextUtils.isEmpty(qunXiangqing.getMgc())) {
                            conversationFragment.messageViewModel.mgc = qunXiangqing.getMgc();
                            conversationFragment.messageViewModel.openMgc = qunXiangqing.getOpenMgc();
                            conversationFragment.messageViewModel.shifoumianci = true;
                        } else {
                            conversationFragment.messageViewModel.shifoumianci = false;
                        }
                    }
                }
            }

        } else if ("xiugaigongaole".equals(myImEvent.type)) {
            if (conversation.target.equals(myImEvent.params2)) {
                conversationFragment.qungonggao.setVisibility(View.VISIBLE);
                conversationFragment.gonggaoxinxi.setText("群公告：" + myImEvent.params1);
                MyCommonBroadBridge.getInstance().setMyBridgeMethod(ConversationBroadActivity.this, BridgeImEnum.UPDATE_ANNOUNCEMENT.getValue(), myImEvent.params1, "", null);
            }

        } else if ("yonghuxinxi".equals(myImEvent.type)) {
            MyCommonBroadResult yoinghu = gson.fromJson(myImEvent.params1, MyCommonBroadResult.class);
            if (yoinghu != null && !TextUtils.isEmpty(yoinghu.getChatImg())) {
                conversationFragment.setConversationBackgroundImage(yoinghu.getChatImg());
            }
        } else if ("qunxiangqingshibai".equals(myImEvent.type)) {
            page_title_right_icon.setVisibility(View.GONE);
        } else if ("xiugaifayanshijian".equals(myImEvent.type)) {
            MyCommonBroadBridge.getInstance().setMyBridgeMethod(ConversationBroadActivity.this, BridgeImEnum.GET_GROUP_DETAILS.getValue(), conversation.target, "", null);
        } else if ("beifeng".equals(myImEvent.type)) {
            MyCommonBroadBridge.getInstance().setMyBridgeMethod(ConversationBroadActivity.this, BridgeImEnum.BANNED.getValue(), conversation.target, "", null);
        } else if ("fajieshouzhuanzhangxiaoxi".equals(myImEvent.type)) {
            ZhuanZhangMessageContent cardMessageContent = new ZhuanZhangMessageContent(myImEvent.params1, myImEvent.params2, "", "1");
            Message msg = new Message();
            msg.conversation = conversation;
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
        } else if ("bushihaoyoufaxiaoxi".equals(myImEvent.type)) {
            new MaterialDialog.Builder(ConversationBroadActivity.this)
                    .content("你还不是他（她)朋友。请先发送朋友验证请求，对方验证通过后，才能聊天")
                    .negativeText("取消")
                    .positiveText("确认")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            MyCommonBroadBridge.getInstance().setMyBridgeMethod(ConversationBroadActivity.this, BridgeImEnum.ADD_FRIEND.getValue(), conversation.target, "", null);
                        }
                    })
                    .build()
                    .show();
        } else if ("huiyishi".equals(myImEvent.type)) {
            String json = myImEvent.params1;
            MeetingInviteMessageContent inviteMessageContent =
                    new Gson().fromJson(json, MeetingInviteMessageContent.class);
            if (inviteMessageContent != null && conversationFragment != null) {
                conversationFragment.messageViewModel.sendMessage(conversation, inviteMessageContent);
            }


        } else if ("qunMeeting".equals(myImEvent.type)) {
            String roomId = myImEvent.params1;
            String jsonParams = myImEvent.params2;
            if (conversationFragment != null) {
                conversationFragment.qunMeetingNotice.setVisibility(roomId.isEmpty() ? View.GONE : View.VISIBLE);
                conversationFragment.tvQunMeeting.setOnClickListener(v -> {
                    MyCommonBroadBridge.getInstance().setMyBridgeMethod(this, BridgeImEnum.ENTER_MEETING.getValue(), roomId, jsonParams, null);

                });
            }


        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMsgReceive(EmotionBroadEvent myImEvent) {
        if ("fasongbiaoqing".equals(myImEvent.type)) {
            BiaoQingMessageContent biaoQingMessageContent = new BiaoQingMessageContent(myImEvent.params);
            Message msg = new Message();
            msg.conversation = conversation;
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

    private void getUserData() {
        if (MMKVBroadUtils.Companion.getKeFuId() != null && conversation.target.contains(MMKVBroadUtils.Companion.getKeFuId())) {
//            MyCommonBroadBridge.getInstance().setMyQiaoJie(ConversationBroadActivity.this, "getUserData", conversation.target, "", null);
            titleGradeAndTime.setVisibility(View.GONE);
            page_title_right_icon.setVisibility(View.GONE);
        } else {
            MyCommonBroadBridge.getInstance().setMyBridgeMethod(ConversationBroadActivity.this, BridgeImEnum.GET_USER_DATA.getValue(), conversation.target, "", null);
            GlobalFriendBroadBean bean = GlobalBroadInfo.INSTANCE.queryMyFriendInfo(conversation.target);
            if (null != bean && null != bean.grade && null != bean.registerDay) {
                titleGradeAndTime.setVisibility(View.VISIBLE);
                TextView titleGrade = findViewById(R.id.titleGrade);
                TextView titleTime = findViewById(R.id.titleTime);
                titleGrade.setText("信誉：" + bean.grade + "分");
                titleTime.setText("注册时间：" + bean.registerDay + "天");
            } else {
                titleGradeAndTime.setVisibility(View.GONE);
            }
        }
    }


    @Override
    protected void bindViews() {
        super.bindViews();
        titleGradeAndTime = findViewById(R.id.titleGradeAndTime);
    }

    @Override
    protected void beforeViews() {
        super.beforeViews();
        eLogD("ConversationActivityClass", "initData");
    }

    @Override
    protected void afterViews() {
        frompage = getIntent().getStringExtra("frompage");
        EventBus.getDefault().register(this);
        page_title_left_icon = findViewById(R.id.page_title_left_icon);
        page_title_left_icon.setOnClickListener(v -> finish());
        page_title_right_icon = findViewById(R.id.page_title_right_icon);
        page_title_right_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (conversation != null) {
                    if (conversation.type == Conversation.ConversationType.Single) {
                        MyCommonBroadBridge.getInstance().setMyBridgeMethod(ConversationBroadActivity.this, BridgeImEnum.SINGLE_CHAT_MORE.getValue(), conversation.target, "", null);
                    } else {
                        MyCommonBroadBridge.getInstance().setMyBridgeMethod(ConversationBroadActivity.this, BridgeImEnum.GROUP_CHAT_HOME.getValue(), conversation.target, "", null);
                    }

                }


            }
        });


        page_title = findViewById(R.id.page_title);


        IMServiceStatusViewBroadModel imServiceStatusViewModel = ViewModelProviders.of(this).get(IMServiceStatusViewBroadModel.class);
        imServiceStatusViewModel.imServiceStatusLiveData().observe(this, aBoolean -> {
            if (!isInitialized && aBoolean) {
                init();
                isInitialized = true;
            }
        });
        conversationFragment = new ConversationBroadFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.containerFrameLayout, conversationFragment, "content")
                .commit();

        setConversationBackground();
        if ("tousu".equals(frompage)) {
            page_title_right_icon.setVisibility(View.GONE);

            conversationFragment.fromPage = frompage;

        }


    }


    public ConversationBroadFragment getConversationFragment() {
        return conversationFragment;
    }


    @Override
    public void onBackPressed() {
        if (!conversationFragment.onBackPressed()) {
            super.onBackPressed();
        }
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        conversation = intent.getParcelableExtra("conversation");
        if (conversation == null) {
            finish();
            return;
        }
        long initialFocusedMessageId = intent.getLongExtra("toFocusMessageId", -1);
        String channelPrivateChatUser = intent.getStringExtra("channelPrivateChatUser");
        String conversationTitle = intent.getStringExtra("conversationTitle");
        boolean isPreJoinedChatRoom = intent.getBooleanExtra("isPreJoinedChatRoom", false);
        conversationFragment.setupConversation(conversation, conversationTitle, initialFocusedMessageId, channelPrivateChatUser, isPreJoinedChatRoom);
    }

    private void init() {
        Intent intent = getIntent();
        conversation = intent.getParcelableExtra("conversation");
        String conversationTitle = intent.getStringExtra("conversationTitle");
        boolean isPreJoinedChatRoom = intent.getBooleanExtra("isPreJoinedChatRoom", false);
        //野火闪烁
        long initialFocusedMessageId = intent.getLongExtra("toFocusMessageId", -1);
        if (conversation == null) {
            finish();
            return;
        }
        if (conversation.type == Conversation.ConversationType.Group) {
            MyCommonBroadBridge.getInstance().setMyBridgeMethod(ConversationBroadActivity.this, BridgeImEnum.GET_GROUP_DETAILS.getValue(), conversation.target, "", null);
            MyCommonBroadBridge.getInstance().setMyBridgeMethod(ConversationBroadActivity.this, BridgeImEnum.GET_GROUP_MEETING.getValue(), conversation.target, "", null);
        }


        MyCommonBroadBridge.getInstance().setMyBridgeMethod(ConversationBroadActivity.this, BridgeImEnum.GET_USER_INFO.getValue(), conversation.target, "", null);
        ChatManager.Instance().shangcishijan = 0L;
        conversationFragment.setupConversation(conversation, conversationTitle, initialFocusedMessageId, null, isPreJoinedChatRoom);

        if (conversation != null) {
            if (conversation.type == Conversation.ConversationType.Single)
                MyCommonBroadBridge.getInstance().setMyBridgeMethod(ConversationBroadActivity.this, BridgeImEnum.MUTE_INFO.getValue(), conversation.target, "0", null);
            else
                MyCommonBroadBridge.getInstance().setMyBridgeMethod(ConversationBroadActivity.this, BridgeImEnum.MUTE_INFO.getValue(), conversation.target, "1", null);
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
