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

import com.cboda.chatui.MarqueeTextBroadView;
import com.cboda.select.picture.basic.PictureSelector;
import com.cboda.select.picture.config.PictureMimeType;
import com.cboda.select.picture.config.SelectMimeType;
import com.cboda.select.picture.entity.LocalMedia;
import com.cboda.select.picture.interfaces.OnResultCallbackListener;
import com.freddy.chat.collect.MessageCollectHelper;
import com.freddy.chat.event.CEventCenter;
import com.freddy.chat.event.Events;
import com.freddy.chat.event.I_CEventListener;
import com.freddy.chat.event.MessageDeletedEvent;
import com.freddy.chat.event.MessageReadStateEvent;
import com.freddy.chat.event.MessageRepealEvent;
import com.freddy.chat.group.GroupInfoActivity;
import com.freddy.chat.group.GroupIntent;
import com.freddy.chat.im.IMSClientBootstrap;
import com.freddy.chat.im.MessageBuilder;
import com.freddy.chat.im.MessageProcessor;
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
import com.freddy.im.http.model.im.SessionItem;
import com.freddy.im.http.model.im.UploadRes;
import com.freddy.im.http.model.im.VideoBatchUploadResult;
import com.freddy.im.listener.OnHttpImListener;
import com.freddy.im.message.ImActiveChatSession;
import com.freddy.im.message.ImGroupMemberChangeManager;
import com.freddy.im.message.ImMessageManager;
import com.freddy.im.message.MessageContentFormatter;
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
        MessageListFragment fragment = new MessageListFragment();
        fragment.setArguments(SessionItemIntent.newArguments(sessionItem));
        return fragment;
    }

    private static final String[] EVENTS = {
            Events.CHAT_MESSAGE,
            Events.CHAT_MESSAGE_UPDATE,
            Events.GROUP_MEMBERS_UPDATED,
            Events.SESSION_REMOVED,
            Events.MESSAGE_READ_STATE_CHANGED,
            Events.MESSAGE_REPEAL,
            Events.MESSAGE_DELETED,
            Events.SESSION_MESSAGES_CLEARED
    };

    private SessionItem sessionItem;
    private String peerUserId;
    private String sessionId;

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshMessages;
    private MessageListInputBroadPanel inputPanel;
    private MessageListAdapter adapter;
    @Nullable
    private FrameLayout multiMessageActionContainer;

    private boolean isGroupChat;
    private ActivityResultLauncher<String[]> filePickerLauncher;

    /** 本地是否可能还有更早一页（用于下拉刷新） */
    private boolean hasMoreHistory;
    public View qungonggaoBroad;
    public MarqueeTextBroadView gonggaoxinxiBroad;
    private View mViewMeetJoin;
    private TextView mTvMeetJoin, mTvMeetPeople;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                this::onFilePicked);
        sessionItem = SessionItemIntent.getSessionItem(getArguments());
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
            Toast.makeText(requireContext(), R.string.session_item_missing, Toast.LENGTH_SHORT).show();
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

        adapter = new MessageListAdapter(
                isGroupChat,
                WeChatUiHelper.resolveAvatarUrl(sessionItem),
                IMSClientBootstrap.getInstance().getCurrentUserAvatarUrl(),
                sessionId,
                isGroupChat ? null : peerUserId,
                isGroupChat ? null : WeChatUiHelper.resolvePeerDisplayName(sessionItem),
                this::resendFailedMessage);
        adapter.setMessageActions(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
        multiMessageActionContainer = view.findViewById(R.id.multi_message_action_container);
        setupDismissPanelOnListTouch();

        swipeRefreshMessages.setOnRefreshListener(this::onPullLoadOlder);
        swipeRefreshMessages.setColorSchemeResources(R.color.wechat_green);

        inputPanel.init(this, view.findViewById(R.id.mViewIntercept), new MessageListInputBroadPanel.Callback() {
            @Override
            public void onSendText(@NonNull String text) {
                sendMsg(text);
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
                scrollToLatest();
            }
        });
        inputPanel.configureMorePanel(isGroupChat);
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
            Intent intent = new Intent(requireContext(), GroupInfoActivity.class);
            GroupIntent.put(intent, sessionId);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadGroupMembers() {
        ImLocalStore.loadGroupMembersMap(sessionId, map -> {
            adapter.setGroupMembers(map);
            if (map.isEmpty()) {
                ImGroupMemberChangeManager.getInstance().fetchAllMembersPaginated(sessionId);
            }
        });
    }

    private void loadLocalMessages() {
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

    private void showLoadedMessages(@NonNull List<AppMessage> cached) {
        hasMoreHistory = cached.size() >= ImLocalStore.MESSAGE_PAGE_SIZE;
        updateSwipeRefreshEnabled();
        if (!cached.isEmpty()) {
            adapter.appendMessages(cached);
            if (adapter.getItemCount() > 0) {
                recyclerView.scrollToPosition(adapter.getItemCount() - 1);
            }
        }
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

    private void openFilePicker() {
        hideInputPanelIfShowing();
        if (filePickerLauncher == null) {
            Toast.makeText(requireContext(), R.string.file_pick_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        filePickerLauncher.launch(new String[]{"*/*"});
    }

    private void onFilePicked(@Nullable Uri uri) {
        if (uri == null) {
            return;
        }
        if (TextUtils.isEmpty(peerUserId)) {
            Toast.makeText(requireContext(), R.string.file_pick_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        File cacheFile = MediaUploadFileResolver.resolve(requireContext(), uri);
        if (cacheFile == null || !cacheFile.isFile()) {
            Toast.makeText(requireContext(), R.string.file_pick_failed, Toast.LENGTH_SHORT).show();
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

    private void openPictureCamera() {
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
                handlePictureSelectResult(result);
            }

            @Override
            public void onCancel() {
            }
        };
    }

    private void handlePictureSelectResult(@Nullable ArrayList<LocalMedia> result) {
        if (result == null || result.isEmpty()) {
            return;
        }
        if (TextUtils.isEmpty(peerUserId)) {
            Toast.makeText(requireContext(), R.string.picture_pick_failed, Toast.LENGTH_SHORT).show();
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
            if (isVideoMedia(media)) {
                int durationSec = resolveVideoDurationSec(media);
                sendVideoMessage(file, durationSec);
            } else {
                sendImageMessage(file);
            }
            sentAny = true;
        }
        if (!sentAny) {
            Toast.makeText(requireContext(), R.string.picture_path_invalid, Toast.LENGTH_SHORT).show();
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
        if (!file.isFile()) {
            Toast.makeText(requireContext(), R.string.picture_path_invalid, Toast.LENGTH_SHORT).show();
            return;
        }
        final String localPath = file.getAbsolutePath();
        AppMessage message = MessageBuilder.buildImageMessage(
                sessionId,
                sessionItem.getConversation(),
                peerUserId,
                localPath,
                localPath,
                null,
                null);
        sendMediaMessage(message, file, (msg, upload) ->
                MessageBuilder.applyImageUpload(msg, localPath, upload), R.string.picture_upload_failed);
    }

    private void sendVideoMessage(@NonNull File file, int durationSec) {
        if (!file.isFile()) {
            Toast.makeText(requireContext(), R.string.video_path_invalid, Toast.LENGTH_SHORT).show();
            return;
        }
        final String localPath = file.getAbsolutePath();
        VideoCoverUtil.Meta meta = VideoCoverUtil.extract(localPath);
        if (meta == null || meta.coverFile == null || !meta.coverFile.isFile()) {
            Toast.makeText(requireContext(), R.string.video_cover_failed, Toast.LENGTH_SHORT).show();
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
                        onMediaUploadFailed(message.getClientMsgId(), R.string.video_upload_failed)));
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
                R.string.file_upload_failed);
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
                R.string.voice_upload_failed);
    }

    private interface UploadContentPatcher {
        void apply(@NonNull AppMessage message, @NonNull UploadRes upload);
    }

    private void sendMediaMessage(@NonNull AppMessage message,
                                  @NonNull File file,
                                  @NonNull UploadContentPatcher patcher,
                                  int uploadFailedToastRes) {
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
            adapter.appendMessages(Collections.singletonList(message));
            scrollToLatest();
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
            runOnUiThreadSafe(() -> adapter.updateMessage(toResend));
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
                MessageBuilder.applyImageUpload(msg, localPath, upload), R.string.picture_upload_failed);
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
                R.string.voice_upload_failed);
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
                R.string.file_upload_failed);
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
                        onMediaUploadFailed(message.getClientMsgId(), R.string.video_upload_failed)));
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
                R.string.message_resend_local_missing, Toast.LENGTH_SHORT).show());
        ImLocalStore.markOutgoingSendFailed(message.getClientMsgId(), updated -> {
            if (updated != null) {
                runOnUiThreadSafe(() -> adapter.updateMessage(updated));
            }
        });
    }

    private void scrollToLatest() {
        if (adapter.getItemCount() > 0) {
            recyclerView.post(() -> recyclerView.scrollToPosition(adapter.getItemCount() - 1));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ImActiveChatSession.setActive(sessionId);
        sessionItem.setUnreadCount(0);
        ImLocalStore.clearSessionUnread(sessionId);
        ImMessageManager.getInstance().fetchNewMessageListBySessionId(sessionItem,
                batch -> MessageProcessor.getInstance().receiveMsg(batch));
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
        final long boundarySeq = first.getSequenceId();
        CThreadPoolExecutor.runInBackground(() -> {
            List<AppMessage> older = ImLocalStore.loadMessagesOlderThan(
                    sessionId,
                    boundarySeq,
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

    private void sendMsg(@NonNull String text) {
        if (text.isEmpty() || TextUtils.isEmpty(peerUserId)) {
            return;
        }

        AppMessage message = MessageBuilder.buildTextMessage(
                sessionId, sessionItem.getConversation(), peerUserId, text);
        MessageProcessor.getInstance().sendMsg(message, () -> {
            adapter.appendMessages(Collections.singletonList(message));
            scrollToLatest();
        });
        if (inputPanel != null) {
            inputPanel.clearInput();
            inputPanel.resetPanelState();
        }
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

    private void closeHost() {
        if (!isAdded()) {
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
            Toast.makeText(requireContext(), R.string.message_recall_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        ImMessageHttpExample.requestRepealMessage(message.getMsgId(), success -> runOnUiThreadSafe(() ->
                applyRecallUi(message.getMsgId())), () -> runOnUiThreadSafe(() ->
                Toast.makeText(requireContext(), R.string.message_recall_failed, Toast.LENGTH_SHORT).show()));
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
            Toast.makeText(requireContext(), R.string.message_collect_failed, Toast.LENGTH_SHORT).show();
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
            Toast.makeText(requireContext(), R.string.message_forward_failed, Toast.LENGTH_SHORT).show();
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
        String peerName = isGroupChat ? null : WeChatUiHelper.resolvePeerDisplayName(sessionItem);
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
                        .setMessage(R.string.message_delete_confirm)
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
                    requireActivity(), BridgeImEnum.COLLECT_IM.getValue(), "", sessionId, sessionId,null);
        } else {
            MyCommonBroadBridge2.getInstance().setMyBridgeMethod2(
                    requireActivity(), BridgeImEnum.COLLECT_IM.getValue(), peerUserId, "", sessionId,null);
        }
    }

    private void openBusinessCardPicker() {
        if (isGroupChat) {
            MyCommonBroadBridge2.getInstance().setMyBridgeMethod2(
                    requireActivity(), BridgeImEnum.BUSINESS_CARD.getValue(), "", sessionId, sessionId,null);
        } else {
            MyCommonBroadBridge2.getInstance().setMyBridgeMethod2(
                    requireActivity(), BridgeImEnum.BUSINESS_CARD.getValue(), peerUserId, "", sessionId,null);
        }
    }

    private void openMeetingRoom() {
        if (!isGroupChat) {
            return;
        }
        MyCommonBroadBridge.getInstance().setMyBridgeMethod(
                requireActivity(), BridgeImEnum.MEETING_ROOM.getValue(), "", sessionId, null);
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
        if (Events.GROUP_MEMBERS_UPDATED.equals(topic) && obj instanceof String) {
            String groupId = (String) obj;
            if (isGroupChat && sessionId.equals(groupId)) {
                loadGroupMembers();
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
                    scrollToLatest();
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
            scrollToLatest();
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
