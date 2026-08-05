package com.freddy.chat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.cboda.baselibrary.toast.ToastBroadUtils;
import com.cboda.baselibrary.utils.ELogUtilKt;
import com.cboda.baselibrary.utils.OnSingleClickBroadListener;
import static com.cboda.baselibrary.utils.ViewExpandUtilKt.anyToInt;

import com.cboda.chatui.MarqueeTextBroadView;
import com.cboda.chatui.quickmeet.MeetCallManager;
import com.cboda.chatui.quickmeet.bean.QuickMeetInfoBean;
import com.cboda.chatui.quickmeet.plugin.impl.MeetPlugin;
import com.cboda.intelligent.activity.QuickMeetingCreateActivity;
import com.cboda.select.picture.basic.PictureSelector;
import com.cboda.select.picture.config.PictureMimeType;
import com.cboda.select.picture.config.SelectMimeType;
import com.cboda.select.picture.entity.LocalMedia;
import com.cboda.select.picture.interfaces.OnResultCallbackListener;
import com.freddy.chat.collect.MessageCollectHelper;
import com.freddy.chat.event.CEventCenter;
import com.freddy.chat.event.Events;
import com.freddy.chat.event.I_CEventListener;
import com.freddy.chat.event.GroupMuteUpdateEvent;
import com.freddy.chat.event.MessageDeletedEvent;
import com.freddy.chat.event.MessageReadStateEvent;
import com.freddy.chat.event.MessageRepealEvent;
import com.cboda.intelligent.activity.im.ImGroupIntent;
import com.cboda.intelligent.activity.im.ImGroupMemberUiMapper;
import com.cboda.intelligent.activity.im.ImQunInfo2BroadActivity;
import com.cboda.intelligent.activity.im.ImQunMemberListBroadActivity;
import com.cboda.intelligent.utils.JumpBroadUtils;
import com.freddy.chat.im.IMSClientBootstrap;
import com.freddy.chat.im.MessageBuilder;
import com.freddy.chat.im.MessageProcessor;
import com.freddy.chat.mention.ImMentionContract;
import com.freddy.chat.mention.ImMentionHelper;
import com.freddy.chat.mention.ImMentionMemberActivity;
import com.freddy.chat.media.PictureSelectorCompressEngine;
import com.freddy.chat.media.PictureSelectorGlideEngine;
import com.freddy.chat.media.PictureSelectorSandboxEngine;
import com.freddy.chat.utils.CThreadPoolExecutor;
import com.freddy.chat.utils.MediaMessageContentUtil;
import com.freddy.chat.utils.MediaUploadFileResolver;
import com.freddy.chat.utils.VideoCoverUtil;
import com.freddy.chat.utils.WeChatUiHelper;
import com.freddy.im.MessageType;
import com.freddy.im.MyCommonBroadBridge2;
import com.freddy.im.bean.AppMessage;
import com.freddy.im.db.ImLocalStore;
import com.freddy.im.http.ImMessageHttpExample;
import com.freddy.im.http.model.im.GroupBusiExt;
import com.freddy.im.http.model.im.GroupMemberItem;
import com.freddy.im.http.model.im.GroupVo;
import com.freddy.im.http.model.im.SessionItem;
import com.freddy.im.http.model.im.UploadRes;
import com.freddy.im.http.model.im.VideoBatchUploadResult;
import com.freddy.im.listener.OnHttpImListener;
import com.freddy.im.message.ImActiveChatSession;
import com.freddy.im.message.GroupMemberSyncHelper;
import com.freddy.im.message.ImMessageManager;
import com.freddy.im.message.MessageContentFormatter;
import com.freddy.webtrc.utils.BroadE;
import com.freddy.im.group.GroupMuteEvaluator;
import com.freddy.im.group.GroupRole;
import static com.freddy.im.util.BaseLiveDataEventKt.groupDataLiveData2;
import com.freddy.chat.forward.ForwardMode;
import com.freddy.chat.menu.MessageContextMenuHelper;
import com.freddy.chat.menu.MessageListActions;
import com.freddy.chat.menu.MessageListMode;
import com.freddy.chat.widget.MessageListInputBroadPanel;
import com.cboda.baselibrary.common.BridgeImEnum;
import com.cboda.baselibrary.utils.OnSingleClickBroadListener;
import com.cboda.chatui.bridge.MyCommonBroadBridge;
import com.cboda.intelligent.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 消息列表 Fragment，可嵌入宿主 Activity 或通过 {@link MessageListActivity} 展示。
 */
public class MessageListFragment extends Fragment implements I_CEventListener, MessageListActions {

    @NonNull
    public static MessageListFragment newInstance(@NonNull SessionItem sessionItem) {
        return newInstance(sessionItem, null);
    }

    @NonNull
    public static MessageListFragment newInstance(@NonNull SessionItem sessionItem,
                                                  @Nullable String focusMsgId) {
        MessageListFragment fragment = new MessageListFragment();
        fragment.setArguments(SessionItemIntent.newArguments(sessionItem, focusMsgId));
        return fragment;
    }

    private static final String[] EVENTS = {
            Events.CHAT_MESSAGE,
            Events.CHAT_MESSAGE_UPDATE,
            Events.SESSION_LIST_UPDATED,
            Events.GROUP_MEMBERS_UPDATED,
            Events.GROUP_MEMBERS_ROLE_UPDATE,
            Events.GROUP_NOTICE_UPDATED,
            Events.GROUP_MUTE_UPDATED,
            Events.SESSION_REMOVED,
            Events.MESSAGE_READ_STATE_CHANGED,
            Events.MESSAGE_REPEAL,
            Events.MESSAGE_DELETED,
            Events.SESSION_MESSAGES_CLEARED
    };

    private SessionItem sessionItem;
    private String peerUserId;
    private String sessionId;
    @Nullable
    private String focusMsgId;

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshMessages;
    private MessageListInputBroadPanel inputPanel;
    private MessageListAdapter adapter;
    @Nullable
    private FrameLayout multiMessageActionContainer;

    /** 弹幕「图片」入口：选图后按 contentType=201（会议弹幕图）发送 */
    private boolean pendingMeetingImageSend;

    private boolean isGroupChat;
    private int myGroupRole = GroupRole.MEMBER;
    private ActivityResultLauncher<String[]> filePickerLauncher;
    private ActivityResultLauncher<Intent> mentionPickerLauncher;
    private ActivityResultLauncher<Intent> groupCallMemberPickerLauncher;

    /**
     * 本地是否可能还有更早一页（用于下拉刷新）
     */
    private boolean hasMoreHistory;
    /** 是否贴在最后一屏；由滚动空闲时刷新，避免 append 后 canScrollVertically 误判。 */
    private boolean atLatestScreen = true;
    /** 距末尾 N 条以内视为底部区域，收消息仍可自动跟滚。 */
    private static final int LATEST_BOTTOM_ITEM_THRESHOLD = 3;
    public View qungonggaoBroad;
    public MarqueeTextBroadView gonggaoxinxiBroad;
    private View mViewMeetJoin;
    private TextView mTvMeetJoin, mTvMeetPeople;
    private GroupVo groupVo;
    /** 群发言间隔（秒），来自 busiExt.jiange；0 表示不限制。 */
    private int speakIntervalSec;
    /** 自己最近一次成功发出群消息的时间戳（毫秒）。 */
    private long lastSelfSendAtMs;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                this::onFilePicked);
        mentionPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() != android.app.Activity.RESULT_OK
                            || result.getData() == null
                            || inputPanel == null) {
                        return;
                    }
                    Intent data = result.getData();
                    if (data.getBooleanExtra(ImMentionContract.EXTRA_MENTION_ALL, false)) {
                        inputPanel.insertMention(ImMentionHelper.buildMentionAllSpannable(requireContext()));
                        return;
                    }
                    String userId = data.getStringExtra(ImMentionContract.EXTRA_USER_ID);
                    String displayName = data.getStringExtra(ImMentionContract.EXTRA_DISPLAY_NAME);
                    if (!TextUtils.isEmpty(userId) && !TextUtils.isEmpty(displayName)) {
                        inputPanel.insertMention(
                                ImMentionHelper.buildMentionSpannable(userId, displayName));
                    }
                });
        groupCallMemberPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() != android.app.Activity.RESULT_OK
                            || result.getData() == null) {
                        return;
                    }
                    ArrayList<String> userIds = result.getData().getStringArrayListExtra(
                            ImGroupIntent.EXTRA_SELECTED_USER_IDS);
                    if (userIds == null || userIds.isEmpty()) {
                        return;
                    }
                    BroadE.startCall(userIds, false, null, requireContext(), sessionId);
                });
        sessionItem = SessionItemIntent.getSessionItem(getArguments());
        focusMsgId = SessionItemIntent.getFocusMsgId(getArguments());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_message_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (sessionItem == null) {
            Toast.makeText(requireContext(), com.cboda.baselibrary.R.string.session_item_missing, Toast.LENGTH_SHORT).show();
            closeHost();
            return;
        }
        sessionId = sessionItem.getSessionId() != null ? sessionItem.getSessionId() : "";
        isGroupChat = sessionItem.getConversation() == MessageType.GROUP_CHAT.getMsgType();
        if (isGroupChat) {
            peerUserId = sessionItem.getSessionId();
        } else {
            peerUserId = sessionItem.getOppositeUserId() != null ? sessionItem.getOppositeUserId() : "";
        }

        recyclerView = view.findViewById(R.id.rv_messages);
        swipeRefreshMessages = view.findViewById(R.id.swipe_refresh_messages);
        inputPanel = view.findViewById(R.id.input_panel);
        mViewMeetJoin = view.findViewById(R.id.mViewMeetJoin);
        mTvMeetPeople = view.findViewById(R.id.mTvMeetPeople);
        mTvMeetJoin = view.findViewById(R.id.mTvMeetJoin);
        gonggaoxinxiBroad = view.findViewById(R.id.gonggaoxinxi);
        qungonggaoBroad = view.findViewById(R.id.qungonggao);
        if (isGroupChat) {
            setupGroupNoticeInteraction();
            setupMeetJoinInteraction();
            observeSpeakIntervalConfig();
            if (getActivity() instanceof GroupNoticeHost) {
                ((GroupNoticeHost) getActivity()).applyCachedGroupNotice();
            }
        }

        adapter = new MessageListAdapter(
                isGroupChat,
                WeChatUiHelper.resolveAvatarUrl(sessionItem),
                IMSClientBootstrap.getInstance().getCurrentUserAvatarUrl(),
                sessionId,
                isGroupChat ? null : peerUserId,
                isGroupChat ? null : WeChatUiHelper.resolvePeerDisplayName(
                        sessionItem, IMSClientBootstrap.getInstance().getCurrentUserId()),
                this::resendFailedMessage);
        adapter.setMessageActions(this);
        adapter.setPortraitClickListener(userId ->
                JumpBroadUtils.Companion.startHaoYouAddActivity(requireContext(), userId, "2"));
        if (isGroupChat) {
            adapter.setMentionMemberListener((userId, displayName) -> {
                if (inputPanel != null) {
                    inputPanel.insertMention(ImMentionHelper.buildMentionSpannable(userId, displayName));
                }
            });
        }
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView rv, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    atLatestScreen = computeAtLatestScreen();
                }
            }
        });
        multiMessageActionContainer = view.findViewById(R.id.multi_message_action_container);
        setupDismissPanelOnListTouch();

        swipeRefreshMessages.setOnRefreshListener(this::onPullLoadOlder);
        swipeRefreshMessages.setColorSchemeResources(R.color.wechat_green);

        inputPanel.init(this, view.findViewById(R.id.mViewIntercept), new MessageListInputBroadPanel.Callback() {
            @Override
            public void onSendText(@NonNull String text, @Nullable String customInfo) {
                sendMsg(text, customInfo);
            }

            @Override
            public boolean canSendVoice() {
                return !TextUtils.isEmpty(peerUserId);
            }

            @Override
            public void onVoiceRecorded(@NonNull File audioFile, int durationSec) {
                sendVoiceMessage(audioFile, durationSec);
            }

            @Override
            public void onOpenAlbum() {
                openPictureAlbum();
            }

            @Override
            public void onOpenCamera() {
                openPictureCamera();
            }

            @Override
            public void onOpenFile() {
                openFilePicker();
            }

            @Override
            public void onOpenCollect() {
                openCollectFolder();
            }

            @Override
            public void onOpenVoiceCall() {
                if (MeetCallManager.getInstance().isCallingState()) {
                    ToastBroadUtils.show(com.cboda.baselibrary.R.string.msg_meeting_func_unavailable);
                    return;
                }
                if (isGroupChat) {
                    launchGroupCallMemberPicker();
                } else {
                    BroadE.startCall(peerUserId, false, null, requireContext());
                }
            }

            @Override
            public void onOpenBusinessCard() {
                openBusinessCardPicker();
            }

            @Override
            public void onOpenMeetingRoom() {
                openMeetingRoom();
            }

            @Override
            public void onOpenAntiFraud() {
                openAntiFraud();
            }

            @Override
            public void onKeyboardOrPanelShown() {
                scrollToLatest(true);
            }
        });
        inputPanel.configureMorePanel(isGroupChat);
        if (isGroupChat) {
            inputPanel.configureMention(true, this::launchMentionPicker);
        }
        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        if (isInMultiSelectMode()) {
                            exitMultiSelectMode();
                            return;
                        }
                        if (inputPanel.handleBackPressed()) {
                            return;
                        }
                        setEnabled(false);
                        closeHost();
                    }
                });
        CEventCenter.registerEventListener(this, EVENTS);
        if (isGroupChat) {
            loadGroupMembers();
        }
        loadLocalMessages();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull android.view.MenuInflater inflater) {
        if (isGroupChat) {
            inflater.inflate(R.menu.menu_message_list, menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_group_info && isGroupChat) {
            Intent intent = new Intent(requireContext(), ImQunInfo2BroadActivity.class);
            intent.putExtra("qunid", sessionId);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadGroupMembers() {
        ImLocalStore.loadGroupMembersMap(sessionId, map -> {
            applyLocalGroupMembers(map);
            GroupMemberSyncHelper.syncOnEnterGroupChat(
                    sessionId,
                    group -> {
                        this.groupVo = group;
                        applySpeakIntervalFromGroup(group);
                        if (getActivity() instanceof GroupMuteHost) {
                            ((GroupMuteHost) getActivity()).applyGroupDetailForMute(group);
                        }
                    },
                    this::reloadGroupMembersFromLocal);
        });
    }

    private void reloadGroupMembersFromLocal() {
        ImLocalStore.loadGroupMembersMap(sessionId, this::applyLocalGroupMembers);
    }

    private void applyLocalGroupMembers(@NonNull java.util.Map<String, GroupMemberItem> map) {
        adapter.setGroupMembers(map);
        String currentUserId = IMSClientBootstrap.getInstance().getCurrentUserId();
        GroupMemberItem self = map.get(currentUserId);
        if (self != null) {
            myGroupRole = self.getRole();
        }
        if (getActivity() instanceof MessageListActivity) {
            ((MessageListActivity) getActivity()).updateMeetPluginGroupRole(myGroupRole);
        }
        if (isAdded()) {
            onGroupMuteStateReady();
        }
    }

    private void setupMeetJoinInteraction() {
        if (mTvMeetJoin == null) {
            return;
        }
        mTvMeetJoin.setOnClickListener(new OnSingleClickBroadListener() {
            @Override
            public void onSingleClick(View v) {
                if (!(getActivity() instanceof MessageListActivity)) {
                    return;
                }
                MeetPlugin plugin = ((MessageListActivity) getActivity()).meetPlugin;
                if (plugin == null) {
                    return;
                }
                plugin.joinMyMeetRoom(() -> {
                    hideMeetJoinUi();
                    return kotlin.Unit.INSTANCE;
                });
            }
        });
    }

    public void showMeetJoinUi(@NonNull QuickMeetInfoBean quickDetailMeetBean) {
        if (MeetCallManager.getInstance().isCallingState()) {
            hideMeetJoinUi();
            return;
        }
        if (mViewMeetJoin == null || mTvMeetPeople == null) {
            return;
        }
        mViewMeetJoin.setVisibility(View.VISIBLE);
        int count = anyToInt(quickDetailMeetBean.getJoinedUsers());
        if (count == 0) {
            count = 1;
        }
        mTvMeetPeople.setText(getString(
                com.cboda.baselibrary.R.string.msg_meeting_active_count,
                String.valueOf(count)));
    }

    public void hideMeetJoinUi() {
        if (mViewMeetJoin != null && mViewMeetJoin.getVisibility() == View.VISIBLE) {
            mViewMeetJoin.setVisibility(View.GONE);
        }
    }

    public int getMyGroupRole() {
        return myGroupRole;
    }

    private void setupGroupNoticeInteraction() {
        View.OnClickListener openNoticeListener = v -> {
            if (getActivity() instanceof GroupNoticeHost) {
                ((GroupNoticeHost) getActivity()).openGroupNoticeDetail();
            }
        };
        gonggaoxinxiBroad.setOnClickListener(openNoticeListener);
        qungonggaoBroad.setOnClickListener(openNoticeListener);
    }

    /**
     * 群禁言数据就绪或角色变化后刷新底栏。
     */
    public void onGroupMuteStateReady() {
        applyGroupMuteStatus(myGroupRole);
    }

    public void applyGroupMuteStatus(int role) {
        if (!isGroupChat || inputPanel == null || !(getActivity() instanceof GroupMuteHost)) {
            return;
        }
        GroupMuteHost host = (GroupMuteHost) getActivity();
        String currentUserId = IMSClientBootstrap.getInstance().getCurrentUserId();
        GroupMuteEvaluator.UiState state = GroupMuteEvaluator.evaluate(
                role, currentUserId, host.isGroupAllMuted(), host.getMutedMemberIds());
        if (state == GroupMuteEvaluator.UiState.ALL_MUTED) {
            inputPanel.disableInput(getString(com.cboda.chatui.R.string.group_all_muted_hint));
        } else if (state == GroupMuteEvaluator.UiState.SELF_MUTED) {
            inputPanel.disableInput(getString(com.cboda.chatui.R.string.group_self_muted_hint));
        } else {
            inputPanel.enableInput();
        }
    }

    private void launchMentionPicker() {
        if (!isAdded() || TextUtils.isEmpty(sessionId)) {
            return;
        }
        Intent intent = new Intent(requireContext(), ImMentionMemberActivity.class);
        intent.putExtra(ImMentionContract.EXTRA_GROUP_ID, sessionId);
        intent.putExtra(ImMentionContract.EXTRA_SHOW_MENTION_ALL, GroupRole.isOwnerOrAdmin(myGroupRole));
        mentionPickerLauncher.launch(intent);
    }

    /** 群聊语音通话：先选择群成员，再发起通话。 */
    private void launchGroupCallMemberPicker() {
        if (!isAdded() || TextUtils.isEmpty(sessionId)) {
            return;
        }
        Intent intent = new Intent(requireContext(), ImQunMemberListBroadActivity.class);
        intent.putExtra("qunid", sessionId);
        intent.putExtra("fromPage", ImGroupIntent.FROM_PAGE_SELECT_CALL_MEMBERS);
        intent.putExtra("userType", com.freddy.chat.utils.BroadE.INSTANCE.wildfireUserTypeForRole(myGroupRole));
        groupCallMemberPickerLauncher.launch(intent);
    }

    private void loadLocalMessages() {
        ELogUtilKt.eLogD(ELogUtilKt.APP_IM_TAG,"focusMsgId==="+focusMsgId);
        if (!TextUtils.isEmpty(focusMsgId)) {
            loadMessagesForFocus(focusMsgId);
            return;
        }
        ImLocalStore.loadMessages(sessionId, cached -> {
            if (!isGroupChat) {
                ImLocalStore.loadPeerReadUpToSeq(sessionId, peerSeq -> {
                    if (peerSeq > 0) {
                        adapter.getReadUiState().setPeerReadUpToSeq(peerSeq);
                    }
                    showLoadedMessages(cached);
                });
                return;
            }
            adapter.getReadUiState().seedFromMessages(cached);
            showLoadedMessages(cached);
        });
    }

    private void loadMessagesForFocus(@NonNull String msgId) {
        ImLocalStore.loadMessagesForFocus(sessionId, msgId, (cached, hasMore) -> {
            if (!isGroupChat) {
                ImLocalStore.loadPeerReadUpToSeq(sessionId, peerSeq -> {
                    if (peerSeq > 0) {
                        adapter.getReadUiState().setPeerReadUpToSeq(peerSeq);
                    }
                    showLoadedMessages(cached, hasMore, msgId);
                });
                return;
            }
            adapter.getReadUiState().seedFromMessages(cached);
            showLoadedMessages(cached, hasMore, msgId);
        });
    }

    private void showLoadedMessages(@NonNull List<AppMessage> cached) {
        showLoadedMessages(cached, cached.size() >= ImLocalStore.MESSAGE_PAGE_SIZE, null);
    }

    private void showLoadedMessages(@NonNull List<AppMessage> cached,
                                    boolean hasMore,
                                    @Nullable String scrollToMsgId) {
        hasMoreHistory = hasMore;
        updateSwipeRefreshEnabled();
        if (!cached.isEmpty()) {
            adapter.appendMessages(cached);
            refreshLastSelfSendFromMessages(cached);
            if (!TextUtils.isEmpty(scrollToMsgId)) {
                scrollToMessage(scrollToMsgId);
            } else if (adapter.getItemCount() > 0) {
                recyclerView.scrollToPosition(adapter.getItemCount() - 1);
                atLatestScreen = true;
            }
        }
    }

    private void scrollToMessage(@NonNull String msgId) {
        recyclerView.post(() -> {
            int pos = adapter.findPositionByMessageId(msgId);
            if (pos >= 0) {
                recyclerView.scrollToPosition(pos);
            }
        });
    }

    private void setupDismissPanelOnListTouch() {
        RecyclerView.OnItemTouchListener dismissOnTouch = new RecyclerView.SimpleOnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                if (e.getAction() == MotionEvent.ACTION_DOWN) {
                    hideInputPanelIfShowing();
                }
                return false;
            }
        };
        recyclerView.addOnItemTouchListener(dismissOnTouch);
        swipeRefreshMessages.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                hideInputPanelIfShowing();
            }
            return false;
        });
    }

    private void hideInputPanelIfShowing() {
        if (inputPanel != null) {
            inputPanel.hidePanelIfShowing();
        }
    }

    /**
     * 触摸点是否落在输入栏 / 表情更多面板 / 多选操作栏上。
     * 供宿主 Activity 判断：这些区域的点击不应先收起键盘，否则 adjustResize
     * 会在 DOWN→UP 之间位移导致发送按钮第一次点击失效。
     */
    public boolean isTouchInsideInputChrome(@Nullable MotionEvent event) {
        if (event == null || getView() == null) {
            return false;
        }
        View root = getView();
        return isMotionInsideView(root.findViewById(R.id.input_panel), event)
                || isMotionInsideView(root.findViewById(R.id.panel_container), event)
                || isMotionInsideView(root.findViewById(R.id.multi_message_action_container), event);
    }

    private static boolean isMotionInsideView(@Nullable View view, @NonNull MotionEvent event) {
        if (view == null || view.getVisibility() != View.VISIBLE) {
            return false;
        }
        int[] loc = new int[2];
        view.getLocationInWindow(loc);
        float x = event.getX();
        float y = event.getY();
        return x >= loc[0] && x < loc[0] + view.getWidth()
                && y >= loc[1] && y < loc[1] + view.getHeight();
    }

    private void openFilePicker() {
        hideInputPanelIfShowing();
        if (filePickerLauncher == null) {
            Toast.makeText(requireContext(), com.cboda.baselibrary.R.string.file_pick_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        filePickerLauncher.launch(new String[]{"*/*"});
    }

    private void onFilePicked(@Nullable Uri uri) {
        if (uri == null) {
            return;
        }
        if (TextUtils.isEmpty(peerUserId)) {
            Toast.makeText(requireContext(), com.cboda.baselibrary.R.string.file_pick_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        File cacheFile = MediaUploadFileResolver.resolve(requireContext(), uri);
        if (cacheFile == null || !cacheFile.isFile()) {
            Toast.makeText(requireContext(), com.cboda.baselibrary.R.string.file_pick_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        dispatchPickedFile(cacheFile, uri);
    }

    private void dispatchPickedFile(@NonNull File file, @Nullable Uri uri) {
        MediaUploadFileResolver.MediaKind kind =
                MediaUploadFileResolver.detectMediaKind(requireContext(), uri, file);
        if (kind == MediaUploadFileResolver.MediaKind.IMAGE) {
            sendImageMessage(file);
        } else if (kind == MediaUploadFileResolver.MediaKind.VIDEO) {
            sendVideoMessage(file, 1);
        } else {
            sendFileMessage(file);
        }
    }

    private void openPictureAlbum() {
        pendingMeetingImageSend = false;
        hideInputPanelIfShowing();
        PictureSelector.create(requireActivity())
                .openGallery(SelectMimeType.ofAll())
                .setImageEngine(PictureSelectorGlideEngine.createGlideEngine())
                .setSandboxFileEngine(PictureSelectorSandboxEngine.get())
                .setCompressEngine(PictureSelectorCompressEngine.get())
                .setMaxSelectNum(9)
                .setMaxVideoSelectNum(1)
                .isWithSelectVideoImage(false)
                .isDisplayCamera(false)
                .forResult(pictureResultCallback());
    }

    /**
     * 会议弹幕「图片」：走自研选图，发送 contentType={@link com.freddy.im.MessageType.MessageContentType#MEETING_IMAGE}(201)。
     */
    public void openMeetingPictureAlbum() {
        if (!isAdded()) {
            return;
        }
        pendingMeetingImageSend = true;
        hideInputPanelIfShowing();
        PictureSelector.create(requireActivity())
                .openGallery(SelectMimeType.ofImage())
                .setImageEngine(PictureSelectorGlideEngine.createGlideEngine())
                .setSandboxFileEngine(PictureSelectorSandboxEngine.get())
                .setCompressEngine(PictureSelectorCompressEngine.get())
                .setMaxSelectNum(1)
                .isDisplayCamera(false)
                .forResult(pictureResultCallback());
    }

    private void openPictureCamera() {
        pendingMeetingImageSend = false;
        hideInputPanelIfShowing();
        PictureSelector.create(requireActivity())
                .openCamera(SelectMimeType.ofImage())
                .setSandboxFileEngine(PictureSelectorSandboxEngine.get())
                .setCompressEngine(PictureSelectorCompressEngine.get())
                .forResult(pictureResultCallback());
    }

    @NonNull
    private OnResultCallbackListener<LocalMedia> pictureResultCallback() {
        return new OnResultCallbackListener<LocalMedia>() {
            @Override
            public void onResult(ArrayList<LocalMedia> result) {
                boolean meetingImage = pendingMeetingImageSend;
                pendingMeetingImageSend = false;
                handlePictureSelectResult(result, meetingImage);
            }

            @Override
            public void onCancel() {
                pendingMeetingImageSend = false;
            }
        };
    }

    private void handlePictureSelectResult(@Nullable ArrayList<LocalMedia> result) {
        handlePictureSelectResult(result, false);
    }

    private void handlePictureSelectResult(@Nullable ArrayList<LocalMedia> result, boolean meetingImage) {
        if (result == null || result.isEmpty()) {
            return;
        }
        if (TextUtils.isEmpty(peerUserId)) {
            Toast.makeText(requireContext(), com.cboda.baselibrary.R.string.picture_pick_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        boolean sentAny = false;
        for (LocalMedia media : result) {
            if (media == null) {
                continue;
            }
            String path = resolveMediaPath(media);
            File file = MediaUploadFileResolver.resolve(requireContext(), path);
            if (file == null) {
                continue;
            }
            if (!meetingImage && isVideoMedia(media)) {
                int durationSec = resolveVideoDurationSec(media);
                sendVideoMessage(file, durationSec);
            } else {
                sendImageMessage(file, meetingImage);
            }
            sentAny = true;
        }
        if (!sentAny) {
            Toast.makeText(requireContext(), com.cboda.baselibrary.R.string.picture_path_invalid, Toast.LENGTH_SHORT).show();
        }
    }

    private static boolean isVideoMedia(@NonNull LocalMedia media) {
        return PictureMimeType.isHasVideo(media.getMimeType());
    }

    private static int resolveVideoDurationSec(@NonNull LocalMedia media) {
        long ms = media.getDuration();
        if (ms <= 0) {
            return 1;
        }
        return Math.max(1, (int) ((ms + 999) / 1000));
    }

    @Nullable
    private static String resolveMediaPath(@NonNull LocalMedia media) {
        if (media.isCompressed() && !TextUtils.isEmpty(media.getCompressPath())) {
            return media.getCompressPath();
        }
        if (!TextUtils.isEmpty(media.getCompressPath())) {
            return media.getCompressPath();
        }
        if (!TextUtils.isEmpty(media.getSandboxPath())) {
            return media.getSandboxPath();
        }
        if (!TextUtils.isEmpty(media.getCutPath())) {
            return media.getCutPath();
        }
        String path = media.getAvailablePath();
        if (TextUtils.isEmpty(path)) {
            path = media.getRealPath();
        }
        if (TextUtils.isEmpty(path)) {
            path = media.getPath();
        }
        return path;
    }

    private void sendImageMessage(@NonNull File file) {
        sendImageMessage(file, false);
    }

    private void sendImageMessage(@NonNull File file, boolean meetingImage) {
        if (!file.isFile()) {
            Toast.makeText(requireContext(), com.cboda.baselibrary.R.string.picture_path_invalid, Toast.LENGTH_SHORT).show();
            return;
        }
        final String localPath = file.getAbsolutePath();
        int contentType = meetingImage
                ? MessageType.MessageContentType.MEETING_IMAGE.getMsgContentType()
                : MessageType.MessageContentType.IMAGE.getMsgContentType();
        AppMessage message = MessageBuilder.buildImageMessage(
                sessionId,
                sessionItem.getConversation(),
                peerUserId,
                localPath,
                localPath,
                null,
                null,
                contentType);
        sendMediaMessage(message, file, (msg, upload) ->
                MessageBuilder.applyImageUpload(msg, localPath, upload), com.cboda.baselibrary.R.string.picture_upload_failed);
    }

    private void sendVideoMessage(@NonNull File file, int durationSec) {
        if (!file.isFile()) {
            Toast.makeText(requireContext(), com.cboda.baselibrary.R.string.video_path_invalid, Toast.LENGTH_SHORT).show();
            return;
        }
        final String localPath = file.getAbsolutePath();
        VideoCoverUtil.Meta meta = VideoCoverUtil.extract(localPath);
        if (meta == null || meta.coverFile == null || !meta.coverFile.isFile()) {
            Toast.makeText(requireContext(), com.cboda.baselibrary.R.string.video_cover_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        final String localCoverPath = meta.coverFile.getAbsolutePath();
        final int duration = meta.durationSec > 0 ? meta.durationSec : durationSec;
        final int dw = meta.width;
        final int dh = meta.height;
        AppMessage message = MessageBuilder.buildVideoMessage(
                sessionId,
                sessionItem.getConversation(),
                peerUserId,
                duration,
                localPath,
                null,
                localCoverPath,
                null,
                dw,
                dh);
        if (!ensureSpeakIntervalAllowed()) {
            return;
        }
        publishPendingOutgoingMessage(message);
        String groupId = isGroupChat ? sessionId : null;
        ImMessageHttpExample.uploadVideoBatch(
                meta.coverFile,
                file,
                groupId,
                new OnHttpImListener<VideoBatchUploadResult>() {
                    @Override
                    public void onHttpImSuccess(@NonNull VideoBatchUploadResult result) {
                        runOnUiThreadSafe(() -> onVideoBatchUploadSuccess(
                                message, duration, localPath, localCoverPath, dw, dh, result));
                    }
                },
                () -> runOnUiThreadSafe(() ->
                        onMediaUploadFailed(message.getClientMsgId(), com.cboda.baselibrary.R.string.video_upload_failed)));
    }

    private void onVideoBatchUploadSuccess(@NonNull AppMessage message,
                                           int durationSec,
                                           @NonNull String localVideoPath,
                                           @NonNull String localCoverPath,
                                           int dw,
                                           int dh,
                                           @NonNull VideoBatchUploadResult result) {
        MessageBuilder.applyVideoUpload(
                message,
                durationSec,
                localVideoPath,
                localCoverPath,
                result.getVideoUrl(),
                result.getCoverUrl(),
                dw,
                dh);
        ImLocalStore.updateOutgoingMessage(message, updated -> {
            AppMessage toSend = updated != null ? updated : message;
            MessageProcessor.getInstance().transmitOutgoingMessage(toSend);
//            runOnUiThreadSafe(() -> adapter.updateMessage(toSend));
        });
    }

    private void sendFileMessage(@NonNull File file) {
        final String fileName = file.getName();
        final String localPath = file.getAbsolutePath();
        AppMessage message = MessageBuilder.buildFileMessage(
                sessionId,
                sessionItem.getConversation(),
                peerUserId,
                fileName,
                localPath);
        sendMediaMessage(message, file, (msg, upload) ->
                        MessageBuilder.applyFileUpload(msg, fileName, localPath, upload.getUrl()),
                com.cboda.baselibrary.R.string.file_upload_failed);
    }

    private void sendVoiceMessage(@NonNull File file, int durationSec) {
        final String localPath = file.getAbsolutePath();
        final int duration = durationSec;
        AppMessage message = MessageBuilder.buildVoiceMessage(
                sessionId,
                sessionItem.getConversation(),
                peerUserId,
                duration,
                localPath);
        sendMediaMessage(message, file, (msg, upload) ->
                        MessageBuilder.applyVoiceUpload(msg, duration, localPath, upload.getUrl()),
                com.cboda.baselibrary.R.string.voice_upload_failed);
    }

    private interface UploadContentPatcher {
        void apply(@NonNull AppMessage message, @NonNull UploadRes upload);
    }

    private void sendMediaMessage(@NonNull AppMessage message,
                                  @NonNull File file,
                                  @NonNull UploadContentPatcher patcher,
                                  int uploadFailedToastRes) {
        if (!ensureSpeakIntervalAllowed()) {
            return;
        }
        publishPendingOutgoingMessage(message);
        String groupId = isGroupChat ? sessionId : null;
        ImMessageHttpExample.uploadFile(file, groupId, new OnHttpImListener<UploadRes>() {
            @Override
            public void onHttpImSuccess(@NonNull UploadRes result) {
                runOnUiThreadSafe(() -> onMediaUploadSuccess(message, result, patcher));
            }
        }, () -> runOnUiThreadSafe(() ->
                onMediaUploadFailed(message.getClientMsgId(), uploadFailedToastRes)));
    }

    private void publishPendingOutgoingMessage(@NonNull AppMessage message) {
        MessageProcessor.getInstance().sendMsg(message, () -> {
            markSelfSendNow();
            adapter.appendMessages(Collections.singletonList(message));
            scrollToLatest(true);
        }, false);
        if (inputPanel != null) {
            inputPanel.resetPanelState();
        }
    }

    private void onMediaUploadSuccess(@NonNull AppMessage message,
                                      @NonNull UploadRes upload,
                                      @NonNull UploadContentPatcher patcher) {
        patcher.apply(message, upload);
        ImLocalStore.updateOutgoingMessage(message, updated -> {
            AppMessage toSend = updated != null ? updated : message;
            MessageProcessor.getInstance().transmitOutgoingMessage(toSend);
//            runOnUiThreadSafe(() -> adapter.updateMessage(toSend));
        });
    }

    private void onMediaUploadFailed(@Nullable String clientMsgId, int toastResId) {
        Toast.makeText(requireContext(), toastResId, Toast.LENGTH_SHORT).show();
        ImLocalStore.markOutgoingSendFailed(clientMsgId, updated -> {
            if (updated != null) {
                runOnUiThreadSafe(() -> adapter.updateMessage(updated));
            }
        });
    }

    private void resendFailedMessage(@NonNull AppMessage message) {
        String currentUserId = IMSClientBootstrap.getInstance().getCurrentUserId();
        if (!message.isSender(currentUserId)) {
            return;
        }
        ImLocalStore.markOutgoingResending(message.getClientMsgId(), updated -> {
            AppMessage toResend = updated != null ? updated : message;
            runOnUiThreadSafe(() -> {
                adapter.updateMessage(toResend);
                scrollToLatest(true);
            });
            dispatchResend(toResend);
        });
    }

    private void dispatchResend(@NonNull AppMessage message) {
        int contentType = message.getMsgContentType();
        if (WeChatUiHelper.isImageMessage(contentType)) {
            resendImageMessage(message);
        } else if (WeChatUiHelper.isVoiceMessage(contentType)) {
            resendVoiceMessage(message);
        } else if (WeChatUiHelper.isFileMessage(contentType)) {
            resendFileMessage(message);
        } else if (WeChatUiHelper.isVideoMessage(contentType)) {
            resendVideoMessage(message);
        } else {
            MessageProcessor.getInstance().transmitOutgoingMessage(message);
        }
    }

    private void resendImageMessage(@NonNull AppMessage message) {
        if (MediaMessageContentUtil.hasRemoteUrl(message.getContent())) {
            MessageProcessor.getInstance().transmitOutgoingMessage(message);
            return;
        }
        final String localPath = MediaMessageContentUtil.resolveLocalUrl(message.getContent());
        if (!MediaMessageContentUtil.isUsableLocalFile(localPath)) {
            onResendLocalFileMissing(message);
            return;
        }
        retryMediaUpload(message, new File(localPath), (msg, upload) ->
                MessageBuilder.applyImageUpload(msg, localPath, upload), com.cboda.baselibrary.R.string.picture_upload_failed);
    }

    private void resendVoiceMessage(@NonNull AppMessage message) {
        if (MediaMessageContentUtil.hasRemoteUrl(message.getContent())) {
            MessageProcessor.getInstance().transmitOutgoingMessage(message);
            return;
        }
        final String localPath = MediaMessageContentUtil.resolveLocalUrl(message.getContent());
        if (!MediaMessageContentUtil.isUsableLocalFile(localPath)) {
            onResendLocalFileMissing(message);
            return;
        }
        final int duration = MediaMessageContentUtil.resolveVoiceDurationSec(message.getContent());
        retryMediaUpload(message, new File(localPath), (msg, upload) ->
                        MessageBuilder.applyVoiceUpload(msg, duration, localPath, upload.getUrl()),
                com.cboda.baselibrary.R.string.voice_upload_failed);
    }

    private void resendFileMessage(@NonNull AppMessage message) {
        if (MediaMessageContentUtil.hasRemoteUrl(message.getContent())) {
            MessageProcessor.getInstance().transmitOutgoingMessage(message);
            return;
        }
        final String localPath = MediaMessageContentUtil.resolveLocalUrl(message.getContent());
        if (!MediaMessageContentUtil.isUsableLocalFile(localPath)) {
            onResendLocalFileMissing(message);
            return;
        }
        final String fileName = MediaMessageContentUtil.resolveFileName(message.getContent());
        retryMediaUpload(message, new File(localPath), (msg, upload) ->
                        MessageBuilder.applyFileUpload(msg, fileName, localPath, upload.getUrl()),
                com.cboda.baselibrary.R.string.file_upload_failed);
    }

    private void resendVideoMessage(@NonNull AppMessage message) {
        if (MediaMessageContentUtil.hasRemoteUrl(message.getContent())) {
            MessageProcessor.getInstance().transmitOutgoingMessage(message);
            return;
        }
        final String localVideoPath = MediaMessageContentUtil.resolveLocalUrl(message.getContent());
        if (!MediaMessageContentUtil.isUsableLocalFile(localVideoPath)) {
            onResendLocalFileMissing(message);
            return;
        }
        File videoFile = new File(localVideoPath);
        String localCoverPath = MediaMessageContentUtil.resolveLocalCoverUrl(message.getContent());
        File coverFile = MediaMessageContentUtil.isUsableLocalFile(localCoverPath)
                ? new File(localCoverPath) : null;
        if (coverFile == null || !coverFile.isFile()) {
            VideoCoverUtil.Meta meta = VideoCoverUtil.extract(localVideoPath);
            if (meta != null && meta.coverFile != null && meta.coverFile.isFile()) {
                coverFile = meta.coverFile;
                localCoverPath = coverFile.getAbsolutePath();
            }
        }
        if (coverFile == null || !coverFile.isFile()) {
            onResendLocalFileMissing(message);
            return;
        }
        final int duration = MediaMessageContentUtil.resolveVoiceDurationSec(message.getContent());
        final int dw = MediaMessageContentUtil.resolveVideoWidth(message.getContent());
        final int dh = MediaMessageContentUtil.resolveVideoHeight(message.getContent());
        final String coverPath = localCoverPath;
        String groupId = isGroupChat ? sessionId : null;
        ImMessageHttpExample.uploadVideoBatch(
                coverFile,
                videoFile,
                groupId,
                new OnHttpImListener<VideoBatchUploadResult>() {
                    @Override
                    public void onHttpImSuccess(@NonNull VideoBatchUploadResult result) {
                        runOnUiThreadSafe(() -> onVideoBatchUploadSuccess(
                                message, duration, localVideoPath, coverPath, dw, dh, result));
                    }
                },
                () -> runOnUiThreadSafe(() ->
                        onMediaUploadFailed(message.getClientMsgId(), com.cboda.baselibrary.R.string.video_upload_failed)));
    }

    private void retryMediaUpload(@NonNull AppMessage message,
                                  @NonNull File file,
                                  @NonNull UploadContentPatcher patcher,
                                  int uploadFailedToastRes) {
        String groupId = isGroupChat ? sessionId : null;
        ImMessageHttpExample.uploadFile(file, groupId, new OnHttpImListener<UploadRes>() {
            @Override
            public void onHttpImSuccess(@NonNull UploadRes result) {
                runOnUiThreadSafe(() -> onMediaUploadSuccess(message, result, patcher));
            }
        }, () -> runOnUiThreadSafe(() ->
                onMediaUploadFailed(message.getClientMsgId(), uploadFailedToastRes)));
    }

    private void onResendLocalFileMissing(@NonNull AppMessage message) {
        runOnUiThreadSafe(() -> Toast.makeText(requireContext(),
                com.cboda.baselibrary.R.string.message_resend_local_missing, Toast.LENGTH_SHORT).show());
        ImLocalStore.markOutgoingSendFailed(message.getClientMsgId(), updated -> {
            if (updated != null) {
                runOnUiThreadSafe(() -> adapter.updateMessage(updated));
            }
        });
    }

    /**
     * 滚到最新消息。
     *
     * @param force {@code true} 强制滚底（自己发消息/重发等）；
     *              {@code false} 仅在空闲且已在最后一屏时滚底（收消息等）
     */
    private void scrollToLatest(boolean force) {
        if (adapter == null || recyclerView == null || adapter.getItemCount() <= 0) {
            return;
        }
        recyclerView.post(() -> {
            if (adapter.getItemCount() <= 0) {
                return;
            }
            if (!force && !shouldStickToLatest()) {
                return;
            }
            recyclerView.scrollToPosition(adapter.getItemCount() - 1);
            atLatestScreen = true;
        });
    }

    /** 仅供 {@link #scrollToLatest(boolean)} 内部使用。 */
    private boolean shouldStickToLatest() {
        if (recyclerView.getScrollState() != RecyclerView.SCROLL_STATE_IDLE) {
            return false;
        }
        return atLatestScreen;
    }

    /**
     * 底部区域：无法再向下滚，或最后可见项落在末尾
     * {@link #LATEST_BOTTOM_ITEM_THRESHOLD} 条以内。
     */
    private boolean computeAtLatestScreen() {
        if (adapter == null || adapter.getItemCount() <= 0) {
            return true;
        }
        if (!recyclerView.canScrollVertically(1)) {
            return true;
        }
        RecyclerView.LayoutManager lm = recyclerView.getLayoutManager();
        if (!(lm instanceof LinearLayoutManager)) {
            return false;
        }
        int lastVisible = ((LinearLayoutManager) lm).findLastVisibleItemPosition();
        if (lastVisible == RecyclerView.NO_POSITION) {
            return false;
        }
        int lastIndex = adapter.getItemCount() - 1;
        return lastVisible >= lastIndex - (LATEST_BOTTOM_ITEM_THRESHOLD - 1);
    }

    @Override
    public void onResume() {
        super.onResume();
        ImActiveChatSession.setActive(sessionId);
        ImMessageManager.getInstance().fetchNewMessageListBySessionId(sessionItem,
                batch -> MessageProcessor.getInstance().receiveMsg(batch));
        sessionItem.setUnreadCount(0);
        ImLocalStore.clearSessionUnread(sessionId);
        if (isGroupChat) {
            refreshSpeakIntervalFromBus();
            if (getActivity() instanceof GroupNoticeHost) {
                ((GroupNoticeHost) getActivity()).applyCachedGroupNotice();
            }
        }
    }

    /** 从设置页返回时补读最新间隔（BusLiveData 在后台可能丢事件）。 */
    private void refreshSpeakIntervalFromBus() {
        GroupBusiExt busi = groupDataLiveData2.getValue();
        if (busi == null) {
            return;
        }
        speakIntervalSec = Math.max(0, busi.getJiange());
        if (groupVo != null) {
            groupVo.ensureBusiExt().setJiange(speakIntervalSec);
        }
    }

    @Override
    public void onPause() {
        ImActiveChatSession.clearActive();
        super.onPause();
    }

    private void updateSwipeRefreshEnabled() {
        swipeRefreshMessages.setEnabled(hasMoreHistory);
    }

    private void onPullLoadOlder() {
        AppMessage first = adapter.getFirstMessage();
        if (first == null || !hasMoreHistory) {
            swipeRefreshMessages.setRefreshing(false);
            updateSwipeRefreshEnabled();
            return;
        }
        final long boundaryTimestamp = first.getTimestamp();
        CThreadPoolExecutor.runInBackground(() -> {
            List<AppMessage> older = ImLocalStore.loadMessagesOlderThan(
                    sessionId,
                    boundaryTimestamp,
                    ImLocalStore.MESSAGE_PAGE_SIZE);
            CThreadPoolExecutor.runOnMainThread(() -> {
                LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
                int firstPos = lm != null ? lm.findFirstVisibleItemPosition() : RecyclerView.NO_POSITION;
                View child = (lm != null && firstPos != RecyclerView.NO_POSITION)
                        ? lm.findViewByPosition(firstPos)
                        : null;
                int offset = child != null ? child.getTop() - recyclerView.getPaddingTop() : 0;

                int added = adapter.prependMessages(older);
                if (older.size() < ImLocalStore.MESSAGE_PAGE_SIZE) {
                    hasMoreHistory = false;
                }
                if (added == 0) {
                    hasMoreHistory = false;
                }
                updateSwipeRefreshEnabled();
                swipeRefreshMessages.setRefreshing(false);

                if (added > 0 && lm != null && firstPos != RecyclerView.NO_POSITION) {
                    lm.scrollToPositionWithOffset(firstPos + added, offset);
                }
            });
        });
    }

    private void sendMsg(@NonNull String text, @Nullable String customInfo) {
        if (TextUtils.isEmpty(text) && TextUtils.isEmpty(customInfo)) {
            return;
        }
        if (TextUtils.isEmpty(peerUserId)) {
            return;
        }
        if (!ensureSpeakIntervalAllowed()) {
            return;
        }

        AppMessage message = MessageBuilder.buildTextMessage(
                sessionId, sessionItem.getConversation(), peerUserId, text, customInfo);
        ELogUtilKt.eLogI(ELogUtilKt.APP_IM_TAG, "message.getContent()=" + message.getContent() + "------" + "message.getCustomInfo()=" + message.getCustomInfo());
        MessageProcessor.getInstance().sendMsg(message, () -> {
            markSelfSendNow();
            adapter.appendMessages(Collections.singletonList(message));
            scrollToLatest(true);
        });
        if (inputPanel != null) {
            inputPanel.clearInput();
            inputPanel.resetPanelState();
        }
    }

    /** 监听群设置页修改发言间隔后的 LiveData。 */
    private void observeSpeakIntervalConfig() {
        groupDataLiveData2.observe(getViewLifecycleOwner(), busi -> {
            if (busi == null) {
                return;
            }
            speakIntervalSec = Math.max(0, busi.getJiange());
            if (groupVo != null) {
                groupVo.ensureBusiExt().setJiange(speakIntervalSec);
            }
        });
    }

    private void applySpeakIntervalFromGroup(@Nullable GroupVo group) {
        if (group == null || group.getBusiExt() == null) {
            speakIntervalSec = 0;
            return;
        }
        speakIntervalSec = Math.max(0, group.getBusiExt().getJiange());
    }

    /**
     * 群发言间隔：全员受控（含群主/管理员）。
     * 对齐野火间隔逻辑，但不按角色豁免。
     */
    private boolean ensureSpeakIntervalAllowed() {
        if (!isGroupChat) {
            return true;
        }
        int intervalSec = resolveSpeakIntervalSec();
        if (intervalSec <= 0) {
            return true;
        }
        if (lastSelfSendAtMs > 0L) {
            long elapsed = System.currentTimeMillis() - lastSelfSendAtMs;
            if (elapsed < intervalSec * 1000L) {
                ToastBroadUtils.show("发言过于频繁");
                return false;
            }
        }
        return true;
    }

    private int resolveSpeakIntervalSec() {
        return Math.max(0, speakIntervalSec);
    }

    private void markSelfSendNow() {
        lastSelfSendAtMs = System.currentTimeMillis();
    }

    /** 进入会话时用本地最近一条自己发的消息初始化间隔计时。 */
    private void refreshLastSelfSendFromMessages(@Nullable List<AppMessage> messages) {
        if (!isGroupChat || messages == null || messages.isEmpty()) {
            return;
        }
        String currentUserId = IMSClientBootstrap.getInstance().getCurrentUserId();
        if (TextUtils.isEmpty(currentUserId)) {
            return;
        }
        long latest = lastSelfSendAtMs;
        for (AppMessage msg : messages) {
            if (msg == null || !msg.isSender(currentUserId)) {
                continue;
            }
            long ts = WeChatUiHelper.normalizeToMillis(msg.getTimestamp());
            if (ts > latest) {
                latest = ts;
            }
        }
        lastSelfSendAtMs = latest;
    }

    @Override
    public void onDestroyView() {
        CEventCenter.unregisterEventListener(this, EVENTS);
        if (inputPanel != null) {
            inputPanel.onDestroy();
        }
        if (adapter != null) {
            adapter.release();
        }
        super.onDestroyView();
    }

    private void mergeSessionItemFromUpdate(@NonNull SessionItem update) {
        if (sessionItem == null) {
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
    }

    private void closeHost() {
        if (!isAdded()) {
            return;
        }
        if (getActivity() instanceof MessageListActivity) {
            ((MessageListActivity) getActivity()).requestCloseHost();
            return;
        }
        if (requireActivity().getSupportFragmentManager().getBackStackEntryCount() > 0) {
            requireActivity().getSupportFragmentManager().popBackStack();
        } else {
            requireActivity().finish();
        }
    }

    public boolean isInMultiSelectMode() {
        return adapter != null && adapter.getMode() == MessageListMode.CHECKABLE;
    }

    public boolean onBackPressed() {
        if (isInMultiSelectMode()) {
            exitMultiSelectMode();
            return true;
        }
        if (inputPanel != null && inputPanel.handleBackPressed()) {
            return true;
        }
        return false;
    }

    @Override
    public int getListMode() {
        return adapter != null ? adapter.getMode() : MessageListMode.NORMAL;
    }

    @Override
    public boolean isMessageChecked(@NonNull AppMessage message) {
        return adapter != null && adapter.isMessageChecked(message);
    }

    @Override
    public void onRecallMessage(@NonNull AppMessage message) {
        if (TextUtils.isEmpty(message.getMsgId())) {
            Toast.makeText(requireContext(), com.cboda.baselibrary.R.string.message_recall_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        ImMessageHttpExample.requestRepealMessage(message.getMsgId(), success -> runOnUiThreadSafe(() ->
                applyRecallUi(message.getMsgId())), () -> runOnUiThreadSafe(() ->
                Toast.makeText(requireContext(), com.cboda.baselibrary.R.string.message_recall_failed, Toast.LENGTH_SHORT).show()));
    }

    @Override
    public void onCopyMessage(@NonNull AppMessage message) {
        String text = MessageContentFormatter.extractPlainText(message.getContent());
        if (TextUtils.isEmpty(text)) {
            return;
        }
        MessageContextMenuHelper.copyTextToClipboard(requireContext(), text);
    }

    @Override
    public void onEnterMultiSelectMode(@NonNull AppMessage message) {
        if (inputPanel != null) {
            inputPanel.setVisibility(View.GONE);
        }
        adapter.checkMessage(message);
        adapter.setMode(MessageListMode.CHECKABLE);
        showMultiMessageActionBar();
    }

    @Override
    public void onDeleteMessage(@NonNull AppMessage message) {
        deleteMessagesLocally(Collections.singletonList(message));
    }

    @Override
    public void onCollectMessage(@NonNull AppMessage message) {
        String params = MessageCollectHelper.buildCollectParams(message);
        if (TextUtils.isEmpty(params)) {
            Toast.makeText(requireContext(), com.cboda.baselibrary.R.string.message_collect_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        MyCommonBroadBridge2.getInstance().setMyBridgeMethod2(
                requireContext(),
                BridgeImEnum.COLLECT.getValue(),
                message.getFromId(),
                params,
                message.getConversationId(),
                message);
    }

    @Override
    public void onForwardMessages(@NonNull List<AppMessage> messages) {
        List<String> msgIds = collectForwardableMsgIds(messages);
        if (msgIds.isEmpty()) {
            Toast.makeText(requireContext(), com.cboda.baselibrary.R.string.message_forward_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        if (messages.size() > 1) {
            showForwardModeChooser(messages, msgIds);
            return;
        }
        launchForwardActivity(messages, msgIds, ForwardMode.ONE_BY_ONE);
    }

    private void showForwardModeChooser(@NonNull List<AppMessage> messages,
                                        @NonNull List<String> msgIds) {
        new AlertDialog.Builder(requireContext())
                .setItems(new CharSequence[]{
                        getString(com.cboda.chatui.R.string.msg_forward_one_by_one),
                        getString(com.cboda.chatui.R.string.msg_forward_merge)
                }, (dialog, which) -> {
                    ForwardMode mode = which == 0 ? ForwardMode.ONE_BY_ONE : ForwardMode.MERGE;
                    launchForwardActivity(messages, msgIds, mode);
                })
                .show();
    }

    private void launchForwardActivity(@NonNull List<AppMessage> messages,
                                       @NonNull List<String> msgIds,
                                       @NonNull ForwardMode mode) {
        String peerName = isGroupChat ? null : WeChatUiHelper.resolvePeerDisplayName(
                sessionItem, IMSClientBootstrap.getInstance().getCurrentUserId());
        startActivity(MessageForwardActivity.buildIntent(
                requireContext(), msgIds, mode, sessionId, isGroupChat, peerName));
        if (isInMultiSelectMode()) {
            exitMultiSelectMode();
        }
    }

    @Override
    public void onToggleMessageChecked(@NonNull AppMessage message, boolean checked) {
        adapter.setMessageChecked(message, checked);
    }

    @Override
    public void onExitMultiSelectMode() {
        exitMultiSelectMode();
    }

    private void exitMultiSelectMode() {
        if (inputPanel != null) {
            inputPanel.setVisibility(View.VISIBLE);
        }
        if (multiMessageActionContainer != null) {
            multiMessageActionContainer.setVisibility(View.GONE);
            multiMessageActionContainer.removeAllViews();
        }
        adapter.setMode(MessageListMode.NORMAL);
    }

    private void showMultiMessageActionBar() {
        if (multiMessageActionContainer == null) {
            return;
        }
        multiMessageActionContainer.setVisibility(View.VISIBLE);
        multiMessageActionContainer.removeAllViews();
        View bar = LayoutInflater.from(requireContext())
                .inflate(com.cboda.chatui.R.layout.conversation_layout_select_more,
                        multiMessageActionContainer, false);
        bar.findViewById(com.cboda.chatui.R.id.mLLShare).setOnClickListener(new OnSingleClickBroadListener() {
            @Override
            public void onSingleClick(View v) {
                List<AppMessage> checked = adapter.getCheckedMessages();
                if (checked.isEmpty()) {
                    exitMultiSelectMode();
                    return;
                }
                onForwardMessages(checked);
            }
        });
        bar.findViewById(com.cboda.chatui.R.id.mLLDelete).setOnClickListener(new OnSingleClickBroadListener() {
            @Override
            public void onSingleClick(View v) {
                List<AppMessage> checked = adapter.getCheckedMessages();
                if (checked.isEmpty()) {
                    exitMultiSelectMode();
                    return;
                }
                new AlertDialog.Builder(requireContext())
                        .setMessage(com.cboda.baselibrary.R.string.message_delete_confirm)
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(android.R.string.ok, (d, w) ->
                                deleteMessagesLocally(checked))
                        .show();
            }
        });
        multiMessageActionContainer.addView(bar);
    }

    private void applyRecallUi(@NonNull String msgId) {
        ImLocalStore.applyMessageRepeal(msgId, recalled -> runOnUiThreadSafe(() -> {
            if (recalled != null) {
                adapter.replaceOrUpdateMessage(recalled);
            } else {
                adapter.removeByMsgId(msgId);
            }
        }));
    }

    private void deleteMessagesLocally(@NonNull List<AppMessage> messages) {
        ImLocalStore.deleteMessages(messages, deleted -> {
            if (deleted.isEmpty()) {
                return;
            }
            MessageProcessor.getInstance().notifyMessagesDeleted(deleted);
        });
    }

    @NonNull
    private List<String> collectForwardableMsgIds(@NonNull List<AppMessage> messages) {
        List<String> msgIds = new ArrayList<>();
        for (AppMessage message : messages) {
            if (message != null && !TextUtils.isEmpty(message.getMsgId())) {
                msgIds.add(message.getMsgId());
            }
        }
        return msgIds;
    }

    private void openCollectFolder() {
        if (isGroupChat) {
            MyCommonBroadBridge2.getInstance().setMyBridgeMethod2(
                    requireActivity(), BridgeImEnum.COLLECT_IM.getValue(), "", sessionId, sessionId, null);
        } else {
            MyCommonBroadBridge2.getInstance().setMyBridgeMethod2(
                    requireActivity(), BridgeImEnum.COLLECT_IM.getValue(), peerUserId, "", sessionId, null);
        }
    }

    private void openBusinessCardPicker() {
        if (isGroupChat) {
            GroupBusiExt busi = groupVo != null ? groupVo.getBusiExt() : null;
            if (busi != null
                    && "1".equals(busi.getOpenQzts())
                    && !GroupRole.isOwnerOrAdmin(myGroupRole)) {
                ToastBroadUtils.show(getString(R.string.qunzhutuisongmingp));
                return;
            }
            MyCommonBroadBridge2.getInstance().setMyBridgeMethod2(
                    requireActivity(), BridgeImEnum.BUSINESS_CARD.getValue(), "", sessionId, sessionId, null);
        } else {
            MyCommonBroadBridge2.getInstance().setMyBridgeMethod2(
                    requireActivity(), BridgeImEnum.BUSINESS_CARD.getValue(), peerUserId, "", sessionId, null);
        }
    }

    private void openMeetingRoom() {
        if (!isGroupChat || sessionId == null || sessionItem.getSessionName() == null) {
            return;
        }
        if (!MeetCallManager.getInstance().isIdleState()) {
            ToastBroadUtils.show(getString(com.cboda.baselibrary.R.string.msg_meeting_in_progress));
            return;
        }
        if (myGroupRole == GroupRole.MEMBER) {
            ToastBroadUtils.show("只有群主/管理员可以开启会议室");
        } else {
            QuickMeetingCreateActivity.Companion.launch(
                    requireContext(), sessionId, sessionItem.getSessionName(), myGroupRole);
        }
    }

    private void openAntiFraud() {
        MyCommonBroadBridge.getInstance().setMyBridgeMethod(
                requireActivity(), BridgeImEnum.AI_ANTI_FRAUD.getValue(), "", "", null);
    }

    private void runOnUiThreadSafe(@NonNull Runnable action) {
        if (!isAdded()) {
            return;
        }
        requireActivity().runOnUiThread(action);
    }

    @Override
    public void onCEvent(String topic, int msgCode, int resultCode, Object obj) {
        if (Events.SESSION_REMOVED.equals(topic) && obj instanceof String) {
            String removedId = (String) obj;
            if (!TextUtils.isEmpty(removedId) && removedId.equals(sessionId)) {
                CThreadPoolExecutor.runOnMainThread(this::closeHost);
            }
            return;
        }
        if (Events.SESSION_MESSAGES_CLEARED.equals(topic) && obj instanceof String) {
            String clearedId = (String) obj;
            if (!TextUtils.isEmpty(clearedId) && clearedId.equals(sessionId)) {
                CThreadPoolExecutor.runOnMainThread(() -> {
                    adapter.clearAllMessages();
                    hasMoreHistory = false;
                    updateSwipeRefreshEnabled();
                    if (adapter.getMode() == MessageListMode.CHECKABLE) {
                        exitMultiSelectMode();
                    }
                });
            }
            return;
        }
        if (Events.SESSION_LIST_UPDATED.equals(topic) && obj instanceof List) {
            @SuppressWarnings("unchecked")
            List<SessionItem> updates = (List<SessionItem>) obj;
            for (SessionItem update : updates) {
                if (update == null || TextUtils.isEmpty(update.getSessionId())) {
                    continue;
                }
                if (!sessionId.equals(update.getSessionId())) {
                    continue;
                }
                mergeSessionItemFromUpdate(update);
                if (getActivity() instanceof MessageListActivity) {
                    CThreadPoolExecutor.runOnMainThread(() ->
                            ((MessageListActivity) getActivity()).applySessionItemUpdated(update));
                }
                break;
            }
            return;
        }
        if ((Events.GROUP_MEMBERS_UPDATED.equals(topic)
                || Events.GROUP_MEMBERS_ROLE_UPDATE.equals(topic)) && obj instanceof String) {
            String groupId = (String) obj;
            if (isGroupChat && sessionId.equals(groupId)) {
                reloadGroupMembersFromLocal();
            }
            return;
        }
        if (Events.GROUP_MUTE_UPDATED.equals(topic) && obj instanceof GroupMuteUpdateEvent) {
            GroupMuteUpdateEvent event = (GroupMuteUpdateEvent) obj;
            if (isGroupChat && sessionId.equals(event.getGroupId())
                    && getActivity() instanceof MessageListActivity) {
                MessageListActivity activity = (MessageListActivity) getActivity();
                if (event.hasMemberMute()) {
                    activity.applySelfMemberMute(event.isMemberMuted());
                } else if (event.shouldRefreshFromServer()) {
                    activity.refreshGroupMuteFromServer();
                } else {
                    activity.applyGroupAllMute(event.isAllMuted());
                }
            }
            return;
        }
        if (Events.GROUP_NOTICE_UPDATED.equals(topic) && obj instanceof String) {
            String groupId = (String) obj;
            if (isGroupChat && sessionId.equals(groupId) && getActivity() instanceof GroupNoticeHost && isAdded()) {
                ((GroupNoticeHost) getActivity()).fetchGroupNoticeFromServer();
            }
            return;
        }
        if (Events.MESSAGE_REPEAL.equals(topic) && obj instanceof MessageRepealEvent) {
            MessageRepealEvent event = (MessageRepealEvent) obj;
            if (!matchesRepealEvent(event)) {
                return;
            }
            CThreadPoolExecutor.runOnMainThread(() -> {
                AppMessage recalled = event.getRecalledMessage();
                if (recalled != null) {
                    adapter.replaceOrUpdateMessage(recalled);
                } else {
                    adapter.removeByMsgId(event.getMessageId());
                }
            });
            return;
        }
        if (Events.MESSAGE_DELETED.equals(topic) && obj instanceof MessageDeletedEvent) {
            MessageDeletedEvent event = (MessageDeletedEvent) obj;
            if (!matchesDeletedEvent(event)) {
                return;
            }
            CThreadPoolExecutor.runOnMainThread(() -> {
                adapter.removeMessages(event.getDeletedMessages());
                if (adapter.getMode() == MessageListMode.CHECKABLE
                        && adapter.getCheckedMessages().isEmpty()) {
                    exitMultiSelectMode();
                }
            });
            return;
        }
        if (Events.MESSAGE_READ_STATE_CHANGED.equals(topic) && obj instanceof MessageReadStateEvent) {
            MessageReadStateEvent event = (MessageReadStateEvent) obj;
            if (!matchesReadStateEvent(event)) {
                return;
            }
            if (event.getType() == MessageReadStateEvent.Type.PEER_READ && isGroupChat) {
                return;
            }
            if (event.getType() == MessageReadStateEvent.Type.GROUP_READ_COUNT && !isGroupChat) {
                return;
            }
            CThreadPoolExecutor.runOnMainThread(() -> adapter.onReadStateEvent(event));
            return;
        }
        if (Events.CHAT_MESSAGE_UPDATE.equals(topic) && obj instanceof AppMessage) {
            AppMessage message = (AppMessage) obj;
            if (!belongsToSession(message)) {
                return;
            }
            CThreadPoolExecutor.runOnMainThread(() -> {
                int countBefore = adapter.getItemCount();
                adapter.updateMessage(message);
                if (adapter.getItemCount() > countBefore) {
                    scrollToLatest(false);
                }
            });
            return;
        }
        if (!Events.CHAT_MESSAGE.equals(topic) || !(obj instanceof List)) {
            return;
        }
        @SuppressWarnings("unchecked")
        List<AppMessage> batch = (List<AppMessage>) obj;
        List<AppMessage> matched = filterForSession(batch);
        if (matched.isEmpty()) {
            return;
        }
        CThreadPoolExecutor.runOnMainThread(() -> {
            adapter.appendMessages(matched);
            scrollToLatest(false);
        });
    }

    private List<AppMessage> filterForSession(List<AppMessage> batch) {
        List<AppMessage> out = new ArrayList<>();
        for (AppMessage m : batch) {
            if (m != null && belongsToSession(m)) {
                out.add(m);
            }
        }
        return out;
    }

    private boolean matchesRepealEvent(@NonNull MessageRepealEvent event) {
        if (!TextUtils.isEmpty(sessionId) && sessionId.equals(event.getSessionId())) {
            return true;
        }
        return adapter.containsMessageId(event.getMessageId());
    }

    private boolean matchesDeletedEvent(@NonNull MessageDeletedEvent event) {
        if (!TextUtils.isEmpty(sessionId) && sessionId.equals(event.getSessionId())) {
            return true;
        }
        for (AppMessage message : event.getDeletedMessages()) {
            if (message == null) {
                continue;
            }
            if (!TextUtils.isEmpty(message.getMsgId()) && adapter.containsMessageId(message.getMsgId())) {
                return true;
            }
            String clientMsgId = message.getClientMsgId();
            if (!TextUtils.isEmpty(clientMsgId) && adapter.containsMessageId(clientMsgId)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesReadStateEvent(@NonNull MessageReadStateEvent event) {
        if (!TextUtils.isEmpty(sessionId) && sessionId.equals(event.getSessionId())) {
            return true;
        }
        String anchor = ImMessageManager.extractConversationAnchor(event.getMessageId());
        if (!TextUtils.isEmpty(anchor) && sessionId.equals(anchor)) {
            return true;
        }
        return isGroupChat && adapter.containsMessageId(event.getMessageId());
    }

    private boolean belongsToSession(@Nullable AppMessage m) {
        if (m == null) {
            return false;
        }
        if (!TextUtils.isEmpty(sessionId) && sessionId.equals(m.getConversationId())) {
            return true;
        }
        if (!TextUtils.isEmpty(sessionId) && m.getMsgId() != null) {
            String anchor = ImMessageManager.extractConversationAnchor(m.getMsgId());
            if (sessionId.equals(anchor)) {
                return true;
            }
        }
        if (isGroupChat) {
            return !TextUtils.isEmpty(sessionId) && sessionId.equals(m.getToId());
        }
        // 单聊：必须同时包含「自己」和「当前会话对方」，避免对方发给第三人的消息误入本会话
        if (TextUtils.isEmpty(peerUserId)) {
            return false;
        }
        String currentUserId = IMSClientBootstrap.getInstance().getCurrentUserId();
        if (TextUtils.isEmpty(currentUserId)) {
            return false;
        }
        boolean involvesPeer = peerUserId.equals(m.getFromId()) || peerUserId.equals(m.getToId());
        boolean involvesSelf = currentUserId.equals(m.getFromId()) || currentUserId.equals(m.getToId());
        return involvesPeer && involvesSelf;
    }

}
