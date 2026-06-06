package com.freddy.chat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.cboda.select.picture.basic.PictureSelector;
import com.cboda.select.picture.config.PictureMimeType;
import com.cboda.select.picture.config.SelectMimeType;
import com.cboda.select.picture.entity.LocalMedia;
import com.cboda.select.picture.interfaces.OnResultCallbackListener;
import com.effective.android.panel.PanelSwitchHelper;
import com.effective.android.panel.interfaces.ContentScrollMeasurer;
import com.effective.android.panel.interfaces.PanelHeightMeasurer;
import com.effective.android.panel.interfaces.listener.OnPanelChangeListener;
import com.effective.android.panel.view.panel.IPanelView;
import com.freddy.chat.event.CEventCenter;
import com.freddy.chat.event.Events;
import com.freddy.chat.event.I_CEventListener;
import com.freddy.chat.event.MessageReadStateEvent;
import com.freddy.chat.group.GroupInfoActivity;
import com.freddy.chat.group.GroupIntent;
import com.freddy.chat.im.IMSClientBootstrap;
import com.freddy.chat.im.MessageBuilder;
import com.freddy.chat.im.MessageProcessor;
import com.freddy.chat.media.PictureSelectorCompressEngine;
import com.freddy.chat.media.PictureSelectorGlideEngine;
import com.freddy.chat.media.PictureSelectorSandboxEngine;
import com.freddy.chat.utils.CThreadPoolExecutor;
import com.freddy.chat.utils.MediaUploadFileResolver;
import com.freddy.chat.utils.VideoCoverUtil;
import com.freddy.chat.utils.WeChatUiHelper;
import com.freddy.im.MessageType;
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
import com.cboda.intelligent.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 消息列表 Fragment，可嵌入宿主 Activity 或通过 {@link MessageListActivity} 展示。
 */
public class MessageListFragment extends Fragment implements I_CEventListener {

    @NonNull
    public static MessageListFragment newInstance(@NonNull SessionItem sessionItem) {
        MessageListFragment fragment = new MessageListFragment();
        fragment.setArguments(SessionItemIntent.newArguments(sessionItem));
        return fragment;
    }

    private static final String[] CHAT_EMOTIONS = {
            "😀", "😁", "😂", "🤣", "😊", "😍", "😘", "😎",
            "🙂", "😉", "😢", "😭", "😡", "👍", "👎", "🙏",
            "👏", "🎉", "❤️", "💔", "🔥", "✨", "🌹", "🎁"
    };

    private static final String[] EVENTS = {
            Events.CHAT_MESSAGE,
            Events.CHAT_MESSAGE_UPDATE,
            Events.GROUP_MEMBERS_UPDATED,
            Events.SESSION_REMOVED,
            Events.MESSAGE_READ_STATE_CHANGED
    };

    private SessionItem sessionItem;
    private String peerUserId;
    private String sessionId;

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshMessages;
    private EditText etContent;
    private MessageListAdapter adapter;
    @Nullable
    private PanelSwitchHelper panelSwitchHelper;

    private boolean isGroupChat;
    @Nullable
    private ImageButton btnVoice;
    private ActivityResultLauncher<String[]> filePickerLauncher;
    private ActivityResultLauncher<String> recordPermissionLauncher;
    @Nullable
    private MediaRecorder mediaRecorder;
    @Nullable
    private File recordingFile;
    private long recordingStartElapsedMs;

    /** 本地是否可能还有更早一页（用于下拉刷新） */
    private boolean hasMoreHistory;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                this::onFilePicked);
        recordPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (granted) {
                        toggleVoiceRecording();
                    } else {
                        Toast.makeText(requireContext(), R.string.voice_permission_denied, Toast.LENGTH_SHORT).show();
                    }
                });
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
        etContent = view.findViewById(R.id.et_content);
        Button btnSend = view.findViewById(R.id.btn_send);
        btnVoice = view.findViewById(R.id.btn_voice);

        adapter = new MessageListAdapter(
                isGroupChat,
                WeChatUiHelper.resolveAvatarUrl(sessionItem),
                IMSClientBootstrap.getInstance().getCurrentUserAvatarUrl());
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
        setupDismissPanelOnListTouch();

        swipeRefreshMessages.setOnRefreshListener(this::onPullLoadOlder);
        swipeRefreshMessages.setColorSchemeResources(R.color.wechat_green);

        btnSend.setOnClickListener(v -> sendMsg());
        if (btnVoice != null) {
            btnVoice.setOnClickListener(v -> onVoiceEntryClick());
        }
        updateVoiceButtonState(false);
        etContent.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND
                    || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                    && event.getAction() == KeyEvent.ACTION_DOWN)) {
                sendMsg();
                return true;
            }
            return false;
        });
        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        if (panelSwitchHelper != null && panelSwitchHelper.hookSystemBackByPanelSwitcher()) {
                            return;
                        }
                        setEnabled(false);
                        closeHost();
                    }
                });
        if (isGroupChat) {
            loadGroupMembers();
        }
        loadLocalMessages();
    }

    @Override
    public void onStart() {
        super.onStart();
        CEventCenter.registerEventListener(this, EVENTS);
        initPanelSwitchHelper();
    }

    @Override
    public void onStop() {
        CEventCenter.unregisterEventListener(this, EVENTS);
        super.onStop();
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
        if (panelSwitchHelper == null) {
            return;
        }
        if (panelSwitchHelper.isKeyboardState() || panelSwitchHelper.isPanelState()) {
            panelSwitchHelper.resetState();
        }
    }

    private void initPanelSwitchHelper() {
        if (panelSwitchHelper != null) {
            return;
        }
        int panelHeight = getResources().getDimensionPixelSize(R.dimen.chat_panel_default_height);
        panelSwitchHelper = new PanelSwitchHelper.Builder(requireActivity())
                .addContentScrollMeasurer(new ContentScrollMeasurer() {
                    @Override
                    public int getScrollDistance(int defaultDistance) {
                        return defaultDistance;
                    }

                    @Override
                    public int getScrollViewId() {
                        return R.id.rv_messages;
                    }
                })
                .addPanelHeightMeasurer(new PanelHeightMeasurer() {
                    @Override
                    public boolean synchronizeKeyboardHeight() {
                        return true;
                    }

                    @Override
                    public int getTargetPanelDefaultHeight() {
                        return panelHeight;
                    }

                    @Override
                    public int getPanelTriggerId() {
                        return R.id.btn_emotion;
                    }
                })
                .addPanelHeightMeasurer(new PanelHeightMeasurer() {
                    @Override
                    public boolean synchronizeKeyboardHeight() {
                        return false;
                    }

                    @Override
                    public int getTargetPanelDefaultHeight() {
                        return panelHeight;
                    }

                    @Override
                    public int getPanelTriggerId() {
                        return R.id.btn_more;
                    }
                })
                .addPanelChangeListener(new OnPanelChangeListener() {
                    @Override
                    public void onKeyboard() {
                        scrollToLatest();
                    }

                    @Override
                    public void onNone() {
                    }

                    @Override
                    public void onPanel(IPanelView panelView) {
                        scrollToLatest();
                    }

                    @Override
                    public void onPanelSizeChange(IPanelView panelView, boolean portrait,
                                                  int oldWidth, int oldHeight, int width, int height) {
                    }
                })
                .contentScrollOutsideEnable(true)
                .build(false);
        setupEmotionPanel();
        setupMorePanel();
    }

    private void setupEmotionPanel() {
        RecyclerView rv = requireView().findViewById(R.id.rv_panel_emotion);
        if (rv == null) {
            return;
        }
        rv.setLayoutManager(new GridLayoutManager(requireContext(), 8));
        rv.setAdapter(new EmotionPanelAdapter(CHAT_EMOTIONS, emoji -> {
            int start = etContent.getSelectionStart();
            if (start < 0) {
                start = etContent.getText().length();
            }
            etContent.getText().insert(start, emoji);
        }));
    }

    private void setupMorePanel() {
        View album = requireView().findViewById(R.id.panel_more_album);
        View camera = requireView().findViewById(R.id.panel_more_camera);
        View file = requireView().findViewById(R.id.panel_more_file);
        if (album != null) {
            album.setOnClickListener(v -> openPictureAlbum());
        }
        if (camera != null) {
            camera.setOnClickListener(v -> openPictureCamera());
        }
        if (file != null) {
            file.setOnClickListener(v -> openFilePicker());
        }
    }

    private void onVoiceEntryClick() {
        hideInputPanelIfShowing();
        if (TextUtils.isEmpty(peerUserId)) {
            Toast.makeText(requireContext(), R.string.voice_record_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        if (mediaRecorder != null) {
            stopVoiceRecordingAndSend();
            return;
        }
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            toggleVoiceRecording();
            return;
        }
        if (recordPermissionLauncher != null) {
            recordPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
        } else {
            Toast.makeText(requireContext(), R.string.voice_permission_denied, Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleVoiceRecording() {
        if (mediaRecorder == null) {
            startVoiceRecording();
        } else {
            stopVoiceRecordingAndSend();
        }
    }

    private void startVoiceRecording() {
        try {
            File out = new File(requireContext().getCacheDir(), "voice_" + System.currentTimeMillis() + ".m4a");
            MediaRecorder recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            recorder.setAudioEncodingBitRate(64000);
            recorder.setAudioSamplingRate(16000);
            recorder.setOutputFile(out.getAbsolutePath());
            recorder.prepare();
            recorder.start();
            mediaRecorder = recorder;
            recordingFile = out;
            recordingStartElapsedMs = SystemClock.elapsedRealtime();
            updateVoiceButtonState(true);
            Toast.makeText(requireContext(), R.string.voice_record_start, Toast.LENGTH_SHORT).show();
            if (panelSwitchHelper != null) {
                panelSwitchHelper.resetState();
            }
        } catch (Exception e) {
            releaseRecorderQuietly();
            updateVoiceButtonState(false);
            Toast.makeText(requireContext(), R.string.voice_record_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private void stopVoiceRecordingAndSend() {
        MediaRecorder recorder = mediaRecorder;
        File voiceFile = recordingFile;
        long startMs = recordingStartElapsedMs;
        mediaRecorder = null;
        recordingFile = null;
        recordingStartElapsedMs = 0L;
        updateVoiceButtonState(false);
        if (recorder == null || voiceFile == null) {
            releaseRecorderQuietly();
            return;
        }
        try {
            recorder.stop();
        } catch (Exception ignored) {
            // stop 可能因时长过短抛错，按失败处理
        }
        releaseRecorderQuietly(recorder);
        if (!voiceFile.isFile() || voiceFile.length() <= 0) {
            Toast.makeText(requireContext(), R.string.voice_record_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        int durationSec = Math.max(1, (int) ((SystemClock.elapsedRealtime() - startMs + 999) / 1000));
        Toast.makeText(requireContext(), R.string.voice_record_stop, Toast.LENGTH_SHORT).show();
        sendVoiceMessage(voiceFile, durationSec);
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
                .isDisplayCamera(true)
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
        if (panelSwitchHelper != null) {
            panelSwitchHelper.resetState();
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

  /** 原 MainActivity#sendMsg */
    private void sendMsg() {
        String text = etContent.getText().toString().trim();
        if (text.isEmpty() || TextUtils.isEmpty(peerUserId)) {
            return;
        }

        AppMessage message = MessageBuilder.buildTextMessage(sessionId,sessionItem.getConversation(), peerUserId, text);
        MessageProcessor.getInstance().sendMsg(message, () -> {
            adapter.appendMessages(Collections.singletonList(message));
            scrollToLatest();
        });
        etContent.setText("");
        if (panelSwitchHelper != null) {
            panelSwitchHelper.resetState();
        }
    }

    @Override
    public void onDestroyView() {
        releaseRecorderQuietly();
        panelSwitchHelper = null;
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

    private void runOnUiThreadSafe(@NonNull Runnable action) {
        if (!isAdded()) {
            return;
        }
        requireActivity().runOnUiThread(action);
    }

    private void releaseRecorderQuietly() {
        MediaRecorder recorder = mediaRecorder;
        mediaRecorder = null;
        releaseRecorderQuietly(recorder);
        recordingFile = null;
        recordingStartElapsedMs = 0L;
        updateVoiceButtonState(false);
    }

    private static void releaseRecorderQuietly(@Nullable MediaRecorder recorder) {
        if (recorder == null) {
            return;
        }
        try {
            recorder.reset();
        } catch (Exception ignored) {
        }
        try {
            recorder.release();
        } catch (Exception ignored) {
        }
    }

    private void updateVoiceButtonState(boolean recording) {
        if (btnVoice == null) {
            return;
        }
        btnVoice.setActivated(recording);
        btnVoice.setAlpha(recording ? 1f : 0.72f);
        if (recording) {
            btnVoice.setColorFilter(ContextCompat.getColor(requireContext(), R.color.wechat_green));
        } else {
            btnVoice.clearColorFilter();
        }
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
        if (Events.GROUP_MEMBERS_UPDATED.equals(topic) && obj instanceof String) {
            String groupId = (String) obj;
            if (isGroupChat && sessionId.equals(groupId)) {
                loadGroupMembers();
            }
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
            CThreadPoolExecutor.runOnMainThread(() -> adapter.updateMessage(message));
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

    private static final class EmotionPanelAdapter extends RecyclerView.Adapter<EmotionPanelAdapter.Holder> {

        private final String[] emotions;
        private final OnEmotionClickListener listener;

        interface OnEmotionClickListener {
            void onEmotionClick(String emoji);
        }

        EmotionPanelAdapter(String[] emotions, OnEmotionClickListener listener) {
            this.emotions = emotions;
            this.listener = listener;
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_panel_emotion, parent, false);
            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            String emoji = emotions[position];
            holder.tvEmotion.setText(emoji);
            holder.itemView.setOnClickListener(v -> listener.onEmotionClick(emoji));
        }

        @Override
        public int getItemCount() {
            return emotions.length;
        }

        static final class Holder extends RecyclerView.ViewHolder {
            final TextView tvEmotion;

            Holder(@NonNull View itemView) {
                super(itemView);
                tvEmotion = itemView.findViewById(R.id.tv_emotion);
            }
        }
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

    private boolean belongsToSession(AppMessage m) {
        if (!TextUtils.isEmpty(sessionId) && sessionId.equals(m.getConversationId())) {
            return true;
        }
        if (!TextUtils.isEmpty(peerUserId)) {
            if (peerUserId.equals(m.getFromId()) || peerUserId.equals(m.getToId())) {
                return true;
            }
        }
        if (!TextUtils.isEmpty(sessionId) && m.getMsgId() != null) {
            String anchor = ImMessageManager.extractConversationAnchor(m.getMsgId());
            if (sessionId.equals(anchor)) {
                return true;
            }
        }
        return false;
    }

}
