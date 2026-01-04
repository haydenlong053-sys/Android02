/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.kit.conversation;

import static com.cboda.baselibrary.utils.ELogUtilKt.eLogD;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProviders;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.cboda.baselibrary.bean.GlobalFriendBroadBean;
import com.cboda.baselibrary.bean.GlobalBroadInfo;
import com.cboda.baselibrary.utils.MMKVBroadUtils;
import com.blankj.utilcode.util.GsonUtils;
import com.google.gson.Gson;
import com.lqr.emoji.EmotionEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import cn.wildfire.chat.kit.ChatManagerHolder;
import cn.wildfire.chat.kit.IMServiceStatusViewModel;
import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.WfcBaseActivity;
import cn.wildfire.chat.kit.bridge.LixinCommonResult;
import cn.wildfire.chat.kit.bridge.QunXiangqing;
import cn.wildfire.chat.kit.bridge.YeHuoCommonBridge;
import cn.wildfire.chat.kit.bridge.YeHuoEvent;
import cn.wildfire.chat.kit.net.OKHttpHelper;
import cn.wildfirechat.message.BiaoQingMessageContent;
import cn.wildfirechat.message.MeetingInviteMessageContent;
import cn.wildfirechat.message.Message;
import cn.wildfirechat.message.ZhuanZhangJieShouMessageContent;
import cn.wildfirechat.message.ZhuanZhangMessageContent;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.ConversationInfo;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.remote.ChatManager;
import cn.wildfirechat.remote.SendMessageCallback;
import okhttp3.Response;

public class ConversationActivity extends WfcBaseActivity {
    private boolean isInitialized = false;
    private ConversationFragment conversationFragment;
    private Conversation conversation;
    private View page_title_left_icon;
    private View page_title_right_icon;
    private View titleGradeAndTime;
    private TextView page_title;
    private String frompage;

    @Override
    protected int contentLayout() {
        return R.layout.fragment_container_activity;
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
//            YeHuoCommonBridge.getInstance().setYeHuoQiaoJie(ConversationActivity.this,"huoququnxiangqing",conversation.target,"",null);
                YeHuoCommonBridge.getInstance().setYeHuoQiaoJie(ConversationActivity.this, "huoququnMeeting", conversation.target, "", null);
            } else if (conversation.type == Conversation.ConversationType.Single) {
                getUserData();
            }
        }
    }

    private Gson gson = GsonUtils.getGson();

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMsgReceive(YeHuoEvent yeHuoEvent) {
        if ("danliaobiaoti".equals(yeHuoEvent.type)) {
//            String params1 = yeHuoEvent.params1;
//            eLogD("onMsgReceiveClass", "params1:" + params1);
            page_title.setText(yeHuoEvent.params1);
        } else if ("reloadMsg".equals(yeHuoEvent.type)) {
            conversationFragment.reloadMessage();
        } else if ("reloadMsgZhuanFa".equals(yeHuoEvent.type)) {
            conversationFragment.reloadZhuanFaMessage();
        } else if ("qunxiangqing".equals(yeHuoEvent.type)) {
            QunXiangqing qunXiangqing = gson.fromJson(yeHuoEvent.params1, QunXiangqing.class);
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
                    conversationFragment.messageViewModel.context = ConversationActivity.this;
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

        } else if ("xiugaigongaole".equals(yeHuoEvent.type)) {
            if (conversation.target.equals(yeHuoEvent.params2)) {
                conversationFragment.qungonggao.setVisibility(View.VISIBLE);
                conversationFragment.gonggaoxinxi.setText("群公告：" + yeHuoEvent.params1);
                YeHuoCommonBridge.getInstance().setYeHuoQiaoJie(ConversationActivity.this, "gengxingonggaole", yeHuoEvent.params1, "", null);
            }

        } else if ("yonghuxinxi".equals(yeHuoEvent.type)) {
            LixinCommonResult yoinghu = gson.fromJson(yeHuoEvent.params1, LixinCommonResult.class);
            if (yoinghu != null && !TextUtils.isEmpty(yoinghu.getChatImg())) {
                conversationFragment.setConversationBackgroundImage(yoinghu.getChatImg());
            }
        } else if ("qunxiangqingshibai".equals(yeHuoEvent.type)) {
            page_title_right_icon.setVisibility(View.GONE);
        } else if ("xiugaifayanshijian".equals(yeHuoEvent.type)) {
            YeHuoCommonBridge.getInstance().setYeHuoQiaoJie(ConversationActivity.this, "huoququnxiangqing", conversation.target, "", null);
        } else if ("beifeng".equals(yeHuoEvent.type)) {
            YeHuoCommonBridge.getInstance().setYeHuoQiaoJie(ConversationActivity.this, "beifeng", conversation.target, "", null);
        } else if ("fajieshouzhuanzhangxiaoxi".equals(yeHuoEvent.type)) {
            ZhuanZhangMessageContent cardMessageContent = new ZhuanZhangMessageContent(yeHuoEvent.params1, yeHuoEvent.params2, "", "1");
            Message msg = new Message();
            msg.conversation = conversation;
            msg.content = cardMessageContent;
            msg.sender = ChatManager.Instance().getUserId();
            ChatManager.Instance().sendMessage(msg, new SendMessageCallback() {
                @Override
                public void onSuccess(long messageUid, long timestamp) {
                    Log.e("野火", "发送接受转账消息onSuccess---> ");
                }

                @Override
                public void onFail(int errorCode) {
                    Log.e("野火", "发送接受转账消息onFail---> " + errorCode);
                }

                @Override
                public void onPrepare(long messageId, long savedTime) {

                }
            });
        } else if ("bushihaoyoufaxiaoxi".equals(yeHuoEvent.type)) {
            new MaterialDialog.Builder(ConversationActivity.this)
                    .content("你还不是他（她)朋友。请先发送朋友验证请求，对方验证通过后，才能聊天")
                    .negativeText("取消")
                    .positiveText("确认")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            YeHuoCommonBridge.getInstance().setYeHuoQiaoJie(ConversationActivity.this, "tianjiahaoyou", conversation.target, "", null);
                        }
                    })
                    .build()
                    .show();
        } else if ("huiyishi".equals(yeHuoEvent.type)) {
            String json = yeHuoEvent.params1;
            MeetingInviteMessageContent inviteMessageContent =
                    new Gson().fromJson(json, MeetingInviteMessageContent.class);
            if (inviteMessageContent != null && conversationFragment != null) {
                conversationFragment.messageViewModel.sendMessage(conversation, inviteMessageContent);
            }


        } else if ("qunMeeting".equals(yeHuoEvent.type)) {
            String roomId = yeHuoEvent.params1;
            String jsonParams = yeHuoEvent.params2;
            if (conversationFragment != null) {
                conversationFragment.qunMeetingNotice.setVisibility(roomId.isEmpty() ? View.GONE : View.VISIBLE);
                conversationFragment.tvQunMeeting.setOnClickListener(v -> {
                    YeHuoCommonBridge.getInstance().setYeHuoQiaoJie(this, "enterMeeting", roomId, jsonParams, null);

                });
            }


        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMsgReceive(EmotionEvent yeHuoEvent) {
        if ("fasongbiaoqing".equals(yeHuoEvent.type)) {
            BiaoQingMessageContent biaoQingMessageContent = new BiaoQingMessageContent(yeHuoEvent.params);
            Message msg = new Message();
            msg.conversation = conversation;
            msg.content = biaoQingMessageContent;
            msg.sender = ChatManager.Instance().getUserId();
            ChatManager.Instance().sendMessage(msg, new SendMessageCallback() {
                @Override
                public void onSuccess(long messageUid, long timestamp) {
                    Log.e("野火", "发送表情-->onSuccess");
                }

                @Override
                public void onFail(int errorCode) {
                    Log.e("野火", "发送表情-->onFail:: " + errorCode);
                }

                @Override
                public void onPrepare(long messageId, long savedTime) {

                }
            });

        }
    }

    private void getUserData() {
        if (MMKVBroadUtils.Companion.getKeFuId() != null && conversation.target.contains(MMKVBroadUtils.Companion.getKeFuId())) {
//            YeHuoCommonBridge.getInstance().setYeHuoQiaoJie(ConversationActivity.this, "getUserData", conversation.target, "", null);
            titleGradeAndTime.setVisibility(View.GONE);
            page_title_right_icon.setVisibility(View.GONE);
        } else {
            YeHuoCommonBridge.getInstance().setYeHuoQiaoJie(ConversationActivity.this, "getUserData", conversation.target, "", null);
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
                        YeHuoCommonBridge.getInstance().setYeHuoQiaoJie(ConversationActivity.this, "danliaogengduo", conversation.target, "", null);
                    } else {
                        YeHuoCommonBridge.getInstance().setYeHuoQiaoJie(ConversationActivity.this, "qunliaozhuye", conversation.target, "", null);
                    }

                }


            }
        });


        page_title = findViewById(R.id.page_title);


        IMServiceStatusViewModel imServiceStatusViewModel = ViewModelProviders.of(this).get(IMServiceStatusViewModel.class);
        imServiceStatusViewModel.imServiceStatusLiveData().observe(this, aBoolean -> {
            if (!isInitialized && aBoolean) {
                init();
                isInitialized = true;
            }
        });
        conversationFragment = new ConversationFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.containerFrameLayout, conversationFragment, "content")
                .commit();

        setConversationBackground();
        if ("tousu".equals(frompage)) {
            page_title_right_icon.setVisibility(View.GONE);

            conversationFragment.fromPage = frompage;

        }


    }


    public ConversationFragment getConversationFragment() {
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
            YeHuoCommonBridge.getInstance().setYeHuoQiaoJie(ConversationActivity.this, "huoququnxiangqing", conversation.target, "", null);
            YeHuoCommonBridge.getInstance().setYeHuoQiaoJie(ConversationActivity.this, "huoququnMeeting", conversation.target, "", null);
        }


        YeHuoCommonBridge.getInstance().setYeHuoQiaoJie(ConversationActivity.this, "huoquyonghuxinxi", conversation.target, "", null);
        ChatManager.Instance().shangcishijan = 0L;
        conversationFragment.setupConversation(conversation, conversationTitle, initialFocusedMessageId, null, isPreJoinedChatRoom);

        if (conversation != null) {
            if (conversation.type == Conversation.ConversationType.Single)
                YeHuoCommonBridge.getInstance().setYeHuoQiaoJie(ConversationActivity.this, "jinyaninfo", conversation.target, "0", null);
            else
                YeHuoCommonBridge.getInstance().setYeHuoQiaoJie(ConversationActivity.this, "jinyaninfo", conversation.target, "1", null);
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
        Intent intent = new Intent(context, ConversationActivity.class);
        intent.putExtra("conversation", conversation);
        intent.putExtra("conversationTitle", title);
        intent.putExtra("isPreJoinedChatRoom", joined);
        return intent;
    }

    public static Intent buildConversationIntent(Context context, Conversation conversation, String channelPrivateChatUser, long toFocusMessageId) {
        Intent intent = new Intent(context, ConversationActivity.class);
        intent.putExtra("conversation", conversation);
        intent.putExtra("toFocusMessageId", toFocusMessageId);
        intent.putExtra("channelPrivateChatUser", channelPrivateChatUser);
        return intent;
    }
}
