package com.freddy.chat;

import static com.cboda.baselibrary.utils.ELogUtilKt.eLogD;
import static com.cboda.baselibrary.utils.ViewExpandUtilKt.formatWithBigDecimal;
import static com.cboda.baselibrary.utils.ViewExpandUtilKt.showMeetLeaveTip;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.cboda.baselibrary.bean.FriendBean;
import com.cboda.baselibrary.bean.UserInfoBean;
import com.cboda.baselibrary.common.BridgeImEnum;
import com.cboda.baselibrary.common.MyEventCodeEnum;
import com.cboda.baselibrary.utils.MMKVBroadUtils;
import com.cboda.baselibrary.utils.OnSingleClickBroadListener;
import com.cboda.baselibrary.utils.RealmUtil;
import com.cboda.chatui.bridge.MyBroadEvent;
import com.cboda.chatui.bridge.MyCommonBroadBridge;
import com.cboda.chatui.quickmeet.MeetCallManager;
import com.cboda.chatui.quickmeet.plugin.impl.MeetPlugin;
import com.cboda.intelligent.base.BaseSimple2BroadActivity;
import com.cboda.intelligent.utils.JumpBroadUtils;
import com.freddy.im.http.ImGroupHttpExample;
import com.freddy.im.group.GroupMuteEvaluator;
import com.freddy.im.http.model.im.GroupVo;
import com.freddy.im.http.model.im.GroupNoticeRes;
import com.freddy.im.http.model.im.SessionItem;
import com.freddy.im.listener.OnHttpImListener;
import com.cboda.intelligent.R;
import com.freddy.im.signal.ImSignalProtos;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;

/**
 * 消息列表容器 Activity，内部承载 {@link MessageListFragment}。
 */
public class MessageListActivity extends BaseSimple2BroadActivity
        implements GroupNoticeHost, GroupMuteHost {
    private boolean isInitialized = false;
    private MessageListFragment messageListFragment;
    private View pageTitleLeftIcon;
    private View pageTitleRightIcon, mIvBack2, rightView;
    private View titleGradeAndTimeBroad;
    private TextView pageTitle;
    public MeetPlugin meetPlugin;
    private String fromPageBroad;
    private SessionItem sessionItem;
    private boolean isFirst = true;
    private boolean groupMuteFetched;
    private boolean groupAllMute;
    @NonNull
    private Set<String> mutedMemberIds = Collections.emptySet();

    @Override
    public boolean showOrHideTitleLayout() {
        return false;
    }

    @Override
    public boolean showTransparentTopTitleLayout() {
        return true;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRootBgColor(com.cboda.chatui.R.color.color_F2F3F5);
        EventBus.getDefault().register(this);
        sessionItem = SessionItemIntent.getSessionItem(getIntent());
        if (sessionItem == null) {
            finish();
            return;
        }
        initView();
        if (savedInstanceState == null) {
            attachMessageListFragment(sessionItem);
        } else {
            messageListFragment = (MessageListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_container);
        }
        initData();
    }


    @Override
    protected int getLayoutId() {
        return R.layout.activity_message_list;
    }

    private void initView() {
        pageTitleLeftIcon = findViewById(com.cboda.chatui.R.id.page_title_left_icon);
        pageTitleLeftIcon.setOnClickListener(new OnSingleClickBroadListener() {
            @Override
            public void onSingleClick(View v) {
                checkMeeting();
            }
        });
        pageTitleRightIcon = findViewById(com.cboda.chatui.R.id.page_title_right_icon);
        mIvBack2 = findViewById(com.cboda.chatui.R.id.mIvBack2);
        rightView = findViewById(com.cboda.chatui.R.id.rightView);

        pageTitleRightIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sessionItem != null) {
                    if (sessionItem.getConversation() == ImSignalProtos.ConversationType.PAIR_VALUE) {
                        JumpBroadUtils.Companion.startHaoYouAddActivity(MessageListActivity.this, sessionItem.getOppositeUserId(), "2");
//                        MyCommonBroadBridge.getInstance().setMyBridgeMethod(ConversationBroadActivity.this, BridgeImEnum.SINGLE_CHAT_MORE.getValue(), conversationBroad.target, "", null);
                    } else {
                        JumpBroadUtils.Companion.startImQunInfoActivity(MessageListActivity.this, sessionItem.getSessionId());
//                        MyCommonBroadBridge.getInstance().setMyBridgeMethod(ConversationBroadActivity.this, BridgeImEnum.GROUP_CHAT_HOME.getValue(), conversationBroad.target, "", null);
                    }

                }


            }
        });

        pageTitle = findViewById(com.cboda.chatui.R.id.page_title);
        titleGradeAndTimeBroad = findViewById(com.cboda.chatui.R.id.titleGradeAndTime);


        // 使用 OnBackPressedDispatcher 适配系统返回事件（替代 onBackPressed）
        //todo 待完善
//        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
//            @Override
//            public void handleOnBackPressed() {
//                if (!messageListFragment.onBackPressed()) {
//                    if (sessionItem != null
//                            && sessionItem.getConversation() == ImSignalProtos.ConversationType.MULTI_VALUE
//                            && MeetCallManager.getInstance().isCallingState()) {
//                        showMyMeetLeaveTip();
//                    } else {
//                        finish();
//                    }
//                }
//            }
//        });
    }

    private void initData() {
        if (sessionItem != null) {
            if (sessionItem.getConversation() == ImSignalProtos.ConversationType.MULTI_VALUE) {
                String sessionName = sessionItem.getSessionName() + "(" + (sessionItem.getMemCount()) + getString(com.cboda.chatui.R.string.msg_person_unit) + ")";
                pageTitle.setText(sessionName);
            } else {
                pageTitle.setText(sessionItem.getSessionName());
            }
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        applyCachedGroupNotice();
    }

    @Override
    public void applyCachedGroupNotice() {
        if (sessionItem == null
                || sessionItem.getConversation() != ImSignalProtos.ConversationType.MULTI_VALUE) {
            return;
        }
        String groupNotice = MMKVBroadUtils.Companion.getGroupNotice(sessionItem.getSessionId());
        updateGroupNoticeUi(groupNotice, false);
    }

    @Override
    public void fetchGroupNoticeFromServer() {
        if (sessionItem == null
                || sessionItem.getConversation() != ImSignalProtos.ConversationType.MULTI_VALUE
                || TextUtils.isEmpty(sessionItem.getSessionId())) {
            return;
        }
        final String groupId = sessionItem.getSessionId();
        ImGroupHttpExample.requestGetGroupNotice(groupId, new OnHttpImListener<GroupNoticeRes>() {
            @Override
            public void onHttpImSuccess(@NonNull GroupNoticeRes result) {
                runOnUiThread(() -> {
                    if (sessionItem == null || !groupId.equals(sessionItem.getSessionId())) {
                        return;
                    }
                    String content = result.getContent() != null ? result.getContent().trim() : "";
                    updateGroupNoticeUi(content, true);
                });
            }
        }, () -> { });
    }

    @Override
    public void updateGroupNoticeUi(@Nullable String notice, boolean persist) {
        if (messageListFragment == null
                || messageListFragment.qungonggaoBroad == null
                || messageListFragment.gonggaoxinxiBroad == null
                || sessionItem == null
                || sessionItem.getConversation() != ImSignalProtos.ConversationType.MULTI_VALUE) {
            return;
        }
        if (TextUtils.isEmpty(notice)) {
            messageListFragment.qungonggaoBroad.setVisibility(View.GONE);
            messageListFragment.gonggaoxinxiBroad.setText("");
            if (persist) {
                MMKVBroadUtils.Companion.saveGroupNotice(sessionItem.getSessionId(), "");
            }
            return;
        }
        messageListFragment.qungonggaoBroad.setVisibility(View.VISIBLE);
        messageListFragment.gonggaoxinxiBroad.setText(
                getString(com.cboda.chatui.R.string.group_announcement_prefix) + notice);
        if (persist) {
            MMKVBroadUtils.Companion.saveGroupNotice(sessionItem.getSessionId(), notice);
        }
    }

    @Override
    public void openGroupNoticeDetail() {
        if (sessionItem == null || TextUtils.isEmpty(sessionItem.getSessionId())) {
            return;
        }
        JumpBroadUtils.Companion.startImQunGongGaoViewActivity(this, sessionItem.getSessionId());
    }

    @Override
    public void fetchGroupMuteFromServer() {
        if (sessionItem == null
                || sessionItem.getConversation() != ImSignalProtos.ConversationType.MULTI_VALUE
                || TextUtils.isEmpty(sessionItem.getSessionId())) {
            return;
        }
        if (groupMuteFetched) {
            return;
        }
        groupMuteFetched = true;
        final String groupId = sessionItem.getSessionId();
        final AtomicInteger pending = new AtomicInteger(2);
        final boolean[] allMuteHolder = new boolean[1];
        final Set<String> mutedHolder = new HashSet<>();

        Runnable tryApply = () -> {
            if (pending.decrementAndGet() > 0) {
                return;
            }
            runOnUiThread(() -> {
                if (sessionItem == null || !groupId.equals(sessionItem.getSessionId())) {
                    return;
                }
                groupAllMute = allMuteHolder[0];
                mutedMemberIds = mutedHolder;
                if (messageListFragment != null) {
                    messageListFragment.onGroupMuteStateReady();
                }
            });
        };

        ImGroupHttpExample.requestGetGroupDetail(groupId, new OnHttpImListener<GroupVo>() {
            @Override
            public void onHttpImSuccess(@NonNull GroupVo result) {
                // 群详情 content.allMute：0 未开启，1 开启
                allMuteHolder[0] = result.isAllMute();
                tryApply.run();
            }
        }, tryApply);

        ImGroupHttpExample.requestMutedMembers(groupId, new OnHttpImListener<List<String>>() {
            @Override
            public void onHttpImSuccess(@NonNull List<String> result) {
                mutedHolder.addAll(GroupMuteEvaluator.copyMutedIds(result));
                tryApply.run();
            }
        }, tryApply);
    }

    @Override
    public boolean isGroupAllMuted() {
        return groupAllMute;
    }

    @NonNull
    @Override
    public Set<String> getMutedMemberIds() {
        return mutedMemberIds;
    }

    @Override
    public void applyGroupMuteToInput(int myGroupRole) {
        if (messageListFragment != null) {
            messageListFragment.applyGroupMuteStatus(myGroupRole);
        }
    }

    /** 管理页变更禁言后刷新（可重复拉取）。 */
    public void refreshGroupMuteFromServer() {
        groupMuteFetched = false;
        fetchGroupMuteFromServer();
    }


    private void attachMessageListFragment(@NonNull SessionItem sessionItem) {
        String focusMsgId = SessionItemIntent.getFocusMsgId(getIntent());
        messageListFragment = MessageListFragment.newInstance(sessionItem, focusMsgId);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, messageListFragment)
                .commit();
    }


    //检查是否当前有会议存在
    private void checkMeeting() {
        if (sessionItem != null && sessionItem.getConversation() == ImSignalProtos.ConversationType.MULTI_VALUE && MeetCallManager.getInstance().isCallingState()) {
            showMyMeetLeaveTip();
        } else {
            finish();
        }
    }

    //会议离开提示
    private void showMyMeetLeaveTip() {
        showMeetLeaveTip(this, new Function0<Unit>() {
            @Override
            public Unit invoke() {
                finish();
                return null;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (null != sessionItem && null != sessionItem.getSessionId()) {
            if (sessionItem.getConversation() == ImSignalProtos.ConversationType.MULTI_VALUE) {
                titleGradeAndTimeBroad.setVisibility(View.GONE);
                if (isFirst) {
                    isFirst = false;
                    fetchGroupNoticeFromServer();
                    fetchGroupMuteFromServer();
                }
            } else if (sessionItem.getConversation() == ImSignalProtos.ConversationType.PAIR_VALUE) {
                getUserData();
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void getUserData() {
        if (MMKVBroadUtils.Companion.getKeFuId() != null && sessionItem.getOppositeUserId().contains(MMKVBroadUtils.Companion.getKeFuId())) {
//            MyCommonBroadBridge.getInstance().setMyQiaoJie(ConversationBroadActivity.this, "getUserData", conversation.target, "", null);
            titleGradeAndTimeBroad.setVisibility(View.GONE);
            pageTitleRightIcon.setVisibility(View.GONE);
            mIvBack2.setVisibility(View.GONE);
            rightView.setVisibility(View.VISIBLE);
        } else {
            MyCommonBroadBridge.getInstance().setMyBridgeMethod(MessageListActivity.this, BridgeImEnum.GET_USER_DATA.getValue(), sessionItem.getOppositeUserId(), "", null);
            RealmUtil.INSTANCE.findUserData(sessionItem.getOppositeUserId(), new Function1<FriendBean, Unit>() {
                @Override
                public Unit invoke(FriendBean bean) {
                    if (null != bean && null != bean.grade && null != bean.registerDay) {
                        titleGradeAndTimeBroad.setVisibility(View.VISIBLE);
                        TextView titleGrade = findViewById(R.id.titleGrade);
                        TextView titleTime = findViewById(R.id.titleTime);
                        titleGrade.setText(getString(com.cboda.baselibrary.R.string.credit_prefix2) + formatWithBigDecimal(bean.grade) + getString(com.cboda.chatui.R.string.msg_score_unit2));
                        titleTime.setText(getString(com.cboda.baselibrary.R.string.register_time_prefix2) + bean.registerDay + getString(com.cboda.chatui.R.string.msg_days_unit2));
                    } else {
                        titleGradeAndTimeBroad.setVisibility(View.GONE);
                    }
                    return null;
                }
            });
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMsgReceive(MyBroadEvent myImEvent) {
        if (MyEventCodeEnum.UPDATE_ANNOUNCEMENT.getValue().equals(myImEvent.type)) {
            if (sessionItem != null && sessionItem.getSessionId().equals(myImEvent.params2)) {
                updateGroupNoticeUi(myImEvent.params1, true);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }


}
