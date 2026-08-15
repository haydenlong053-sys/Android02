package com.freddy.chat;

import static com.cboda.baselibrary.utils.BaseLiveDataEventKt.imsConnectLiveData;
import static com.cboda.baselibrary.utils.ELogUtilKt.APP_IM_TAG;
import static com.cboda.baselibrary.utils.ELogUtilKt.eLogD;
import static com.cboda.baselibrary.utils.GsonUtilsKt.fromJson2;
import static com.cboda.baselibrary.utils.MMKVBroadUtils.LOGIN_USER_DATA;
import static com.cboda.baselibrary.utils.ViewExpandUtilKt.anyToInt;
import static com.cboda.baselibrary.utils.ViewExpandUtilKt.click;
import static com.cboda.baselibrary.utils.ViewExpandUtilKt.formatWithBigDecimal;
import static com.cboda.baselibrary.utils.ViewExpandUtilKt.showMeetLeaveTip;
import static com.cboda.baselibrary.utils.ViewExpandUtilKt.startOutH5Activity;
import static com.freddy.im.util.BaseLiveDataEventKt.groupDataLiveData2;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.blankj.utilcode.util.ThreadUtils;
import com.cboda.baselibrary.bean.DanmakuItem;
import com.cboda.baselibrary.bean.FriendBean;
import com.cboda.baselibrary.bean.UserInfoBean;
import com.cboda.baselibrary.common.BridgeImEnum;
import com.cboda.baselibrary.common.MyEventCodeEnum;
import com.cboda.baselibrary.toast.ToastBroadUtils;
import com.cboda.baselibrary.utils.BaseLiveDataEventKt;
import com.cboda.baselibrary.utils.E;
import com.cboda.baselibrary.utils.MMKVBroadUtils;
import com.cboda.baselibrary.utils.OnSingleClickBroadListener;
import com.cboda.baselibrary.utils.RealmUtil;
import com.cboda.baselibrary.widge.CountPillView;
import com.cboda.baselibrary.widge.HighPerformanceDanmakuView;
import com.cboda.chatui.bridge.MyBroadEvent;
import com.cboda.chatui.bridge.MyCommonBroadBridge;
import com.cboda.chatui.dialog.DanMaPop;
import com.cboda.chatui.group.GroupViewBroadModel;
import com.cboda.chatui.quickmeet.CallEventCallback;
import com.cboda.chatui.quickmeet.MeetCallManager;
import com.cboda.chatui.quickmeet.bean.CallData;
import com.cboda.chatui.quickmeet.bean.EnumerationBean;
import com.cboda.chatui.quickmeet.bean.QuickDetailMeetBean;
import com.cboda.chatui.quickmeet.bean.QuickMeetInfoBean;
import com.cboda.chatui.quickmeet.dialog.MeetSeatApplyPop;
import com.cboda.chatui.quickmeet.plugin.impl.MeetPlugin;
import com.freddy.im.http.ImNotifyHttpExample;
import com.freddy.im.group.GroupRole;
import com.cboda.intelligent.base.BaseSimple2BroadActivity;
import com.cboda.intelligent.utils.JumpBroadUtils;
import com.freddy.chat.im.IMSClientBootstrap;
import com.freddy.chat.utils.WeChatUiHelper;
import com.freddy.im.http.ImGroupHttpExample;
import com.freddy.im.group.GroupMuteEvaluator;
import com.freddy.im.db.ImLocalStore;
import com.freddy.im.http.GroupVoSessionMapper;
import com.freddy.im.http.model.im.GroupBusiExt;
import com.freddy.im.http.model.im.GroupVo;
import com.freddy.im.http.model.im.GroupNoticeRes;
import com.freddy.im.http.model.im.GroupUserVo;
import com.freddy.im.http.model.im.SessionItem;
import com.freddy.im.listener.OnHttpImListener;
import com.cboda.intelligent.R;
import com.freddy.im.signal.ImSignalProtos;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;

/**
 * 消息列表容器 Activity，内部承载 {@link MessageListFragment}。
 * 群会话会议中展示 {@link HighPerformanceDanmakuView} 弹幕。
 */
public class MessageListActivity extends BaseSimple2BroadActivity
        implements GroupNoticeHost, GroupMuteHost, CallEventCallback {
    private boolean isInitialized = false;
    private MessageListFragment messageListFragment;
    private View pageTitleLeftIcon;
    private View pageTitleRightIcon, mIvBack2, rightView, networkBanner;
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

    private HighPerformanceDanmakuView verticalDanmakuView;
    private ImageView danmakuIv;
    private View danmakuView;
    private CountPillView countPillView;
    private ImageView shopIv;
    @Nullable
    private String shopUrl;
    private GroupViewBroadModel groupViewBroadModel;

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
        int imsConType = E.INSTANCE.getImsConType();
        if (imsConType == 1 && null != networkBanner) {
            networkBanner.setVisibility(View.VISIBLE);
        }
        initListener();
    }


    private void initListener() {
        imsConnectLiveData.observe(this, type -> {
            if (null != networkBanner) {
                ThreadUtils.getMainHandler().post(() -> {
                    if (type == 1) {
                        networkBanner.setVisibility(View.VISIBLE);
                    } else if (type == 2) {
                        networkBanner.setVisibility(View.GONE);
                    }
                });
            }
        });
    }

    @Override
    public boolean isShouldHideInput(@Nullable View v, @NonNull MotionEvent event) {
        // 点发送/表情/更多等输入栏控件时不要先收键盘，否则布局抖动会导致第一次点击无效
        if (messageListFragment != null && messageListFragment.isTouchInsideInputChrome(event)) {
            return false;
        }
        return super.isShouldHideInput(v, event);
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
        networkBanner = findViewById(R.id.network_unavailable_banner);
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
        verticalDanmakuView = findViewById(R.id.verticalDanmakuView);
        danmakuIv = findViewById(R.id.danmakuIv);
        danmakuView = findViewById(R.id.danmakuView);
        countPillView = findViewById(R.id.countPillView);
        shopIv = findViewById(R.id.shopIv);
        click(shopIv, () -> {
            startOutH5Activity(shopUrl, MessageListActivity.this);
            return Unit.INSTANCE;
        });
        groupDataLiveData2.observe(this, this::applyShopEntryFromBusiExt);

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
        groupViewBroadModel = new ViewModelProvider(this).get(GroupViewBroadModel.class);
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

        MeetCallManager.getInstance().unregisterEngineEvent(this);
        MeetCallManager.getInstance().registerEngineEvent(this);
        setMeetingDanmakuVisible(isMeetingInCurrentGroup());
        bindMeetingDanmakuObservers(groupId);

        BaseLiveDataEventKt.meetEndMsgLiveData.observe(this, groupIdNo -> {
            if (messageListFragment != null
                    && messageListFragment.isAdded()
                    && TextUtils.equals(groupId, groupIdNo)) {
                messageListFragment.hideMeetJoinUi();
            }
            if (TextUtils.equals(groupId, groupIdNo)) {
                setMeetingDanmakuVisible(false);
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
            if (danmakuView != null && danmakuIv != null && danmakuIv.getVisibility() == View.VISIBLE) {
                danmakuView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void bindMeetingDanmakuObservers(@NonNull String groupId) {
        BaseLiveDataEventKt.getUpdateBarrageDataLiveData().observe(this, barrageBean -> {
            if (barrageBean == null || verticalDanmakuView == null) {
                return;
            }
            if (!MeetCallManager.getInstance().isCallingState()) {
                return;
            }
            if (!TextUtils.equals(groupId, barrageBean.getConversationId())) {
                return;
            }
            // conversationType：自研 MULTI=2；兼容仅校验 groupId
            String myId = MMKVBroadUtils.Companion.getRealUserId();
            if (TextUtils.equals(myId, barrageBean.getSender())) {
                // 本端发送时已立即展示，避免重复
                return;
            }
            if (!TextUtils.isEmpty(barrageBean.getSendName())) {
                addBarrageData(barrageBean.getSendName(), barrageBean.getGiftType(), false);
            } else {
                resolveUserDisplayName(barrageBean.getSender(), name -> {
                    addBarrageData(name, barrageBean.getGiftType(), false);
                    return Unit.INSTANCE;
                });
            }
        });

        if (danmakuIv != null) {
            click(danmakuIv, () -> {
                showMeetingDanMaPop();
                return Unit.INSTANCE;
            });
        }
    }

    private void showMeetingDanMaPop() {
        if (sessionItem == null || TextUtils.isEmpty(sessionItem.getSessionId())) {
            return;
        }
        final String groupId = sessionItem.getSessionId();
        int myRole = messageListFragment != null
                ? messageListFragment.getMyGroupRole()
                : GroupRole.MEMBER;
        boolean isMan = GroupRole.isOwnerOrAdmin(myRole);
        boolean haveApplyList = MeetCallManager.getInstance().isOwner() || isMan;
        boolean haveUnread = danmakuView != null && danmakuView.getVisibility() == View.VISIBLE;
        DanMaPop danMaPop;
        if (MeetCallManager.getInstance().isOwner()) {
            // 会议主持人始终具备禁言/解禁言；发图仍按群管理身份
            danMaPop = new DanMaPop(this, isMan, true, groupAllMute,
                    false, false, true, haveUnread);
        } else {
            danMaPop = new DanMaPop(this, isMan, isMan, groupAllMute,
                    !MeetCallManager.getInstance().isCallDirect(),
                    MeetCallManager.getInstance().isCallDirect(),
                    haveApplyList, haveUnread);
        }
        danMaPop.setClickInvoke(action -> {
            EnumerationBean.DanMaPopAction popAction = action;
            if (popAction == EnumerationBean.DanMaPopAction.FLOWER
                    || popAction == EnumerationBean.DanMaPopAction.ACCLAMATION) {
                // api/im/notify/busiPush，code=2001，giftType：1=鲜花 2=鼓掌
                UserInfoBean userInfoBean = fromJson2(MMKVBroadUtils.Companion.getSqlStrData(LOGIN_USER_DATA, ""), UserInfoBean.class);
                ImNotifyHttpExample.requestMeetingGift(popAction.getCode(), groupId, userInfoBean.getName(), null, null);
                addBarrageData(userInfoBean.getName(), popAction.getCode(), true);
            } else if (popAction == EnumerationBean.DanMaPopAction.PIC) {
                // 对齐野火：选图后发会议弹幕图 contentType=201
                if (messageListFragment != null) {
                    messageListFragment.openMeetingPictureAlbum();
                }
            } else if (popAction == EnumerationBean.DanMaPopAction.GAG
                    || popAction == EnumerationBean.DanMaPopAction.REMOVE_GAG) {
                boolean targetMute = popAction == EnumerationBean.DanMaPopAction.GAG;
                ImGroupHttpExample.requestSetAllMute(groupId, targetMute,
                        new OnHttpImListener<Boolean>() {
                            @Override
                            public void onHttpImSuccess(@NonNull Boolean result) {
                                runOnUiThread(() -> applyGroupAllMute(targetMute));
                            }
                        }, null);
            } else if (popAction == EnumerationBean.DanMaPopAction.UP_MIC) {
                if (groupViewBroadModel != null) {
                    groupViewBroadModel.goMeetUpMicApply(groupId).observe(this, ok ->
                            ToastBroadUtils.show(com.cboda.baselibrary.R.string.msg_meeting_apply_pending));
                }
            } else if (popAction == EnumerationBean.DanMaPopAction.DOWN_MIC) {
                if (groupViewBroadModel != null) {
                    groupViewBroadModel.goMeetDownMicApply(groupId).observe(this, ok ->
                            ToastBroadUtils.show(com.cboda.baselibrary.R.string.msg_meeting_off_mic_done));
                }
            } else if (popAction == EnumerationBean.DanMaPopAction.APPLICATION_LIST) {
                if (danmakuView != null) {
                    danmakuView.setVisibility(View.GONE);
                }
                new MeetSeatApplyPop(this, groupId).showPopupWindow();
            }
            return Unit.INSTANCE;
        });
        danMaPop.showPopupWindow(danmakuIv);
    }

    private void resolveUserDisplayName(@Nullable String userId,
                                        @NonNull Function1<String, Unit> callback) {
        if (TextUtils.isEmpty(userId)) {
            callback.invoke("");
            return;
        }
        RealmUtil.INSTANCE.findUserData(userId, bean -> {
            String name = userId;
            if (bean != null) {
                if (!TextUtils.isEmpty(bean.remarkName)) {
                    name = bean.remarkName;
                } else if (!TextUtils.isEmpty(bean.nickName)) {
                    name = bean.nickName;
                }
            }
            callback.invoke(name);
            return Unit.INSTANCE;
        });
    }

    private void addBarrageData(@Nullable String userName, int giftType, boolean isMeSend) {
        if (verticalDanmakuView == null) {
            return;
        }
        DanmakuItem danmakuItem = new DanmakuItem();
        danmakuItem.setContent((userName == null ? "" : userName) + ": ");
        danmakuItem.setMeSend(isMeSend);
        danmakuItem.setIconResId(giftType == 1
                ? com.cboda.baselibrary.R.mipmap.icon_danma_xh
                : com.cboda.baselibrary.R.mipmap.icon_danma_gz);
        verticalDanmakuView.addDanmaku(danmakuItem);
    }

    private boolean isMeetingInCurrentGroup() {
        if (sessionItem == null
                || sessionItem.getConversation() != ImSignalProtos.ConversationType.MULTI_VALUE
                || TextUtils.isEmpty(sessionItem.getSessionId())
                || !MeetCallManager.getInstance().isCallingState()) {
            return false;
        }
        QuickDetailMeetBean meetBean = MeetCallManager.getInstance().getMCallData().getQuickDetailMeetBean();
        String meetGroupId = meetBean != null ? meetBean.groupIdNo : null;
        if (TextUtils.isEmpty(meetGroupId)) {
            meetGroupId = MeetCallManager.getInstance().getMCallData().getChannelId();
        }
        return TextUtils.equals(sessionItem.getSessionId(), meetGroupId);
    }

    private void setMeetingDanmakuVisible(boolean visible) {
        if (danmakuIv != null) {
            danmakuIv.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
        if (verticalDanmakuView != null) {
            if (!visible) {
                verticalDanmakuView.clearAll();
            }
            verticalDanmakuView.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
        if (!visible) {
            if (danmakuView != null) {
                danmakuView.setVisibility(View.GONE);
            }
            if (countPillView != null) {
                countPillView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onMeetTotalPersonCount(@NotNull String chanelId, int personCount) {
        if (countPillView == null
                || danmakuIv == null
                || danmakuIv.getVisibility() != View.VISIBLE) {
            return;
        }
        if (sessionItem == null || !TextUtils.equals(sessionItem.getSessionId(), chanelId)) {
            return;
        }
        if (personCount > 0) {
            countPillView.setTarget(personCount, 500L);
            if (countPillView.getVisibility() != View.VISIBLE) {
                countPillView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onRtcJoinChannelSuccess() {
        setMeetingDanmakuVisible(isMeetingInCurrentGroup());
    }

    @Override
    public void onEngineDestroy(@NotNull CallData data) {
        setMeetingDanmakuVisible(false);
    }

    /**
     * 同步本地群成员角色到 MeetPlugin，加入会议时写入 CallData。
     */
    public void updateMeetPluginGroupRole(int groupMemberRole) {
        if (meetPlugin != null) {
            meetPlugin.setGroupMemberRole(groupMemberRole);
        }
    }

    /**
     * 关闭页面前检查会议中离开提示（返回键 / Fragment 关闭宿主）。
     */
    public void requestCloseHost() {
        checkMeeting();
    }

    private void initData() {
        refreshPageTitle();
    }

    /**
     * 0x0101 群变更信令入库后刷新标题（{Events#SESSION_LIST_UPDATED}）。
     */
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
            String currentUserId = IMSClientBootstrap.getInstance().getCurrentUserId();
            pageTitle.setText(WeChatUiHelper.resolvePeerDisplayName(sessionItem, currentUserId));
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
        }, () -> {
        });
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
        // 写入 GroupVo.createTime，供会话列表群天数角标展示
        SessionItem merged = GroupVoSessionMapper.toSessionItem(
                group, sessionItem, sessionItem.getSessionId(), false);
        if (merged.getCreateTime() > 0L) {
            sessionItem.setCreateTime(merged.getCreateTime());
            ImLocalStore.saveSessions(Collections.singletonList(merged));
        }
        applyShopEntryFromBusiExt(group.getBusiExt());
        fetchMutedMembersOnly(sessionItem.getSessionId());
    }

    /**
     * 会话内商城入口：对齐野火 shopIv，由 {@link GroupBusiExt#getIsshop()} 控制显隐。
     */
    private void applyShopEntryFromBusiExt(@Nullable GroupBusiExt busiExt) {
        if (shopIv == null) {
            return;
        }
        if (sessionItem == null
                || sessionItem.getConversation() != ImSignalProtos.ConversationType.MULTI_VALUE) {
            shopIv.setVisibility(View.GONE);
            return;
        }
        if (busiExt != null && !TextUtils.isEmpty(busiExt.getShopLink())) {
            shopUrl = busiExt.getShopLink();
        }
        boolean visible = busiExt != null && anyToInt(busiExt.getShowShop()) == 1;
        shopIv.setVisibility(visible ? View.VISIBLE : View.GONE);
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

    /**
     * 管理页变更禁言后刷新（可重复拉取）。
     */
    public void refreshGroupMuteFromServer() {
        groupMuteFetched = false;
        fetchGroupMuteFromServer();
    }

    /**
     * 0x0501 code=1002：信令已携带全员禁言态，直接刷新底栏。
     */
    public void applyGroupAllMute(boolean allMute) {
        groupAllMute = allMute;
        if (messageListFragment != null) {
            messageListFragment.onGroupMuteStateReady();
        }
    }

    /**
     * 0x0501 code=1003：信令已携带当前用户成员禁言态，直接刷新底栏。
     */
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
                //onResume暂时先不开启聚焦查询会议信息
//                if (meetPlugin != null && MeetCallManager.getInstance().isIdleState()) {
//                    meetPlugin.queryMeetInfo(sessionItem.getSessionId(), "active");
//                }
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
            eLogD(APP_IM_TAG, "duiinfo====" + sessionItem.getOppositeUserId());
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
        MeetCallManager.getInstance().unregisterEngineEvent(this);
        if (verticalDanmakuView != null) {
            verticalDanmakuView.clearAll();
        }
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

}
