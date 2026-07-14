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
import com.cboda.baselibrary.utils.BaseLiveDataEventKt;
import com.cboda.baselibrary.utils.MMKVBroadUtils;
import com.cboda.baselibrary.utils.OnSingleClickBroadListener;
import com.cboda.baselibrary.utils.RealmUtil;
import com.cboda.chatui.bridge.MyBroadEvent;
import com.cboda.chatui.bridge.MyCommonBroadBridge;
import com.cboda.chatui.quickmeet.MeetCallManager;
import com.cboda.chatui.quickmeet.bean.QuickMeetInfoBean;
import com.cboda.chatui.quickmeet.dialog.MeetSeatApplyPop;
import com.cboda.chatui.quickmeet.plugin.impl.MeetPlugin;
import com.freddy.im.group.GroupRole;
import com.cboda.intelligent.base.BaseSimple2BroadActivity;
import com.cboda.intelligent.utils.JumpBroadUtils;
import com.freddy.chat.im.IMSClientBootstrap;
import com.freddy.im.http.ImGroupHttpExample;
import com.freddy.im.group.GroupMuteEvaluator;
import com.freddy.im.http.model.im.GroupVo;
import com.freddy.im.http.model.im.GroupNoticeRes;
import com.freddy.im.http.model.im.GroupUserVo;
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
        getSupportFragmentManager().executePendingTransactions();
        initGroupMeetIfNeeded();
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

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (messageListFragment != null && messageListFragment.onBackPressed()) {
                    return;
                }
                requestCloseHost();
            }
        });
    }

    private void initGroupMeetIfNeeded() {
        if (sessionItem == null
                || sessionItem.getConversation() != ImSignalProtos.ConversationType.MULTI_VALUE
                || TextUtils.isEmpty(sessionItem.getSessionId())) {
            return;
        }
        final String groupId = sessionItem.getSessionId();
        meetPlugin = new MeetPlugin(this);
        getLifecycle().addObserver(meetPlugin);
        if (messageListFragment != null) {
            updateMeetPluginGroupRole(messageListFragment.getMyGroupRole());
        }
        meetPlugin.setHasMeetInfoInvoke(quickMeetBean -> {
            if (MeetCallManager.getInstance().isIdleState()
                    && messageListFragment != null
                    && messageListFragment.isAdded()) {
                messageListFragment.showMeetJoinUi(quickMeetBean);
            }
            return Unit.INSTANCE;
        });
        meetPlugin.queryMeetInfo(groupId, "active");

        BaseLiveDataEventKt.meetEndMsgLiveData.observe(this, groupIdNo -> {
            if (messageListFragment != null
                    && messageListFragment.isAdded()
                    && TextUtils.equals(groupId, groupIdNo)) {
                messageListFragment.hideMeetJoinUi();
            }
        });

        BaseLiveDataEventKt.getMeetUpMicMsgLiveData().observe(this, groupIdNo -> {
            if (TextUtils.isEmpty(groupIdNo) || !TextUtils.equals(groupId, groupIdNo)) {
                return;
            }
            if (!MeetCallManager.getInstance().isCallingState()) {
                return;
            }
            boolean canReviewApply = MeetCallManager.getInstance().isOwner()
                    || (messageListFragment != null
                    && GroupRole.isOwnerOrAdmin(messageListFragment.getMyGroupRole()));
            if (!canReviewApply) {
                return;
            }
            new MeetSeatApplyPop(this, groupIdNo).showPopupWindow();
        });
    }

    /** 同步本地群成员角色到 MeetPlugin，加入会议时写入 CallData。 */
    public void updateMeetPluginGroupRole(int groupMemberRole) {
        if (meetPlugin != null) {
            meetPlugin.setGroupMemberRole(groupMemberRole);
        }
    }

    /** 关闭页面前检查会议中离开提示（返回键 / Fragment 关闭宿主）。 */
    public void requestCloseHost() {
        checkMeeting();
    }

    private void initData() {
        refreshPageTitle();
    }

    /** 0x0101 群变更信令入库后刷新标题（{Events#SESSION_LIST_UPDATED}）。 */
    public void applySessionItemUpdated(@NonNull SessionItem update) {
        if (sessionItem == null || !update.getSessionId().equals(sessionItem.getSessionId())) {
            return;
        }
        if (!TextUtils.isEmpty(update.getSessionName())) {
            sessionItem.setSessionName(update.getSessionName());
        }
        if (update.getMemCount() > 0) {
            sessionItem.setMemCount(update.getMemCount());
        }
        if (!TextUtils.isEmpty(update.getIcon())) {
            sessionItem.setIcon(update.getIcon());
        }
        refreshPageTitle();
    }

    private void refreshPageTitle() {
        if (sessionItem == null || pageTitle == null) {
            return;
        }
        if (sessionItem.getConversation() == ImSignalProtos.ConversationType.MULTI_VALUE) {
            String sessionName = sessionItem.getSessionName() + "(" + (sessionItem.getMemCount())
                    + getString(com.cboda.chatui.R.string.msg_person_unit) + ")";
            pageTitle.setText(sessionName);
        } else {
            pageTitle.setText(sessionItem.getSessionName());
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
        ImGroupHttpExample.requestGetGroupDetail(groupId, new OnHttpImListener<GroupVo>() {
            @Override
            public void onHttpImSuccess(@NonNull GroupVo result) {
                applyGroupDetailForMute(result);
            }
        }, () -> fetchMutedMembersOnly(groupId));
    }

    @Override
    public void applyGroupDetailForMute(@NonNull GroupVo group) {
        if (sessionItem == null || TextUtils.isEmpty(sessionItem.getSessionId())) {
            return;
        }
        if (!sessionItem.getSessionId().equals(group.getId())) {
            return;
        }
        groupMuteFetched = true;
        groupAllMute = group.isAllMute();
        fetchMutedMembersOnly(sessionItem.getSessionId());
    }

    private void fetchMutedMembersOnly(@NonNull String groupId) {
        ImGroupHttpExample.requestMutedMembers(groupId, new OnHttpImListener<List<GroupUserVo>>() {
            @Override
            public void onHttpImSuccess(@NonNull List<GroupUserVo> result) {
                runOnUiThread(() -> {
                    if (sessionItem == null || !groupId.equals(sessionItem.getSessionId())) {
                        return;
                    }
                    mutedMemberIds = GroupMuteEvaluator.copyMutedIdsFromMembers(result);
                    if (messageListFragment != null) {
                        messageListFragment.onGroupMuteStateReady();
                    }
                });
            }
        }, () -> runOnUiThread(() -> {
            if (sessionItem != null && groupId.equals(sessionItem.getSessionId())
                    && messageListFragment != null) {
                messageListFragment.onGroupMuteStateReady();
            }
        }));
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

    /** 0x0501 code=1002：信令已携带全员禁言态，直接刷新底栏。 */
    public void applyGroupAllMute(boolean allMute) {
        groupAllMute = allMute;
        if (messageListFragment != null) {
            messageListFragment.onGroupMuteStateReady();
        }
    }

    /** 0x0501 code=1003：信令已携带当前用户成员禁言态，直接刷新底栏。 */
    public void applySelfMemberMute(boolean muted) {
        if (sessionItem == null || TextUtils.isEmpty(sessionItem.getSessionId())) {
            return;
        }
        String userId = IMSClientBootstrap.getInstance().getCurrentUserId();
        if (TextUtils.isEmpty(userId)) {
            refreshGroupMuteFromServer();
            return;
        }
        Set<String> ids = new HashSet<>(mutedMemberIds);
        if (muted) {
            ids.add(userId);
        } else {
            ids.remove(userId);
        }
        mutedMemberIds = ids;
        if (messageListFragment != null) {
            messageListFragment.onGroupMuteStateReady();
        }
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
                }
                if (meetPlugin != null && MeetCallManager.getInstance().isIdleState()) {
                    meetPlugin.queryMeetInfo(sessionItem.getSessionId(), "active");
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
