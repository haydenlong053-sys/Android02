package com.freddy.chat.widget;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.text.Editable;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.airbnb.lottie.LottieAnimationView;
import com.cboda.baselibrary.utils.OnSingleClickBroadListener;
import com.cboda.chatemoji.EmotionBroadLayout;
import com.cboda.chatemoji.IEmotionSelectedBroadListener;
import com.cboda.chatui.audio.AudioRecorderBroadPanel;
import com.cboda.intelligent.R;
import com.cboda.chatui.conversation.mention.MentionBroadSpan;
import com.freddy.chat.mention.AtMentionCustomInfoBuilder;
import com.freddy.chat.utils.QuoteMessageContentUtil;
import com.effective.android.panel.PanelSwitchHelper;
import com.effective.android.panel.interfaces.ContentScrollMeasurer;
import com.effective.android.panel.interfaces.PanelHeightMeasurer;
import com.effective.android.panel.interfaces.listener.OnPanelChangeListener;
import com.effective.android.panel.view.panel.IPanelView;

import java.io.File;

/**
 * 自研消息列表底部输入面板，UI 与录音交互对齐野火 {@code ConversationInputBroadPanel}，
 * 键盘/表情/更多面板切换仍由 {@link PanelSwitchHelper} 驱动。
 */
public class MessageListInputBroadPanel extends FrameLayout
        implements DefaultLifecycleObserver, IEmotionSelectedBroadListener {

    public interface Callback {
        void onSendText(@NonNull String text,
                        @Nullable String customInfo,
                        @Nullable QuoteMessageContentUtil.QuoteInfo quoteInfo);

        boolean canSendVoice();

        void onVoiceRecorded(@NonNull File audioFile, int durationSec);

        void onOpenAlbum();

        void onOpenCamera();

        void onOpenFile();

        void onOpenCollect();

        void onOpenVoiceCall();

        void onOpenBusinessCard();

        void onOpenMeetingRoom();

        void onOpenAntiFraud();

        void onKeyboardOrPanelShown();
    }

    private EditText editText;
    private ImageView extImageView;
    private ImageView emotionImageView;
    private ImageView audioImageView;
    private ImageView mIvVoiceCancel;
    private ImageView mIvVoiceSend;
    private View audioButton;
    @Nullable
    private LottieAnimationView mViewAnimateVoice;
    private Button sendButton;
    private View inputContainerLinearLayout;
    private View disableInputTipView;
    private TextView disableInputTipTextView;
    @Nullable
    private View refRelativeLayout;
    @Nullable
    private TextView refEditText;
    @Nullable
    private View clearRefImageButton;
    @Nullable
    private QuoteMessageContentUtil.QuoteInfo pendingQuote;

    @Nullable
    private Fragment fragment;
    @Nullable
    private View interceptView;
    @Nullable
    private Callback callback;
    @Nullable
    private PanelSwitchHelper panelSwitchHelper;
    @Nullable
    private AudioRecorderBroadPanel audioRecorderPanel;
    @Nullable
    private ActivityResultLauncher<String> recordPermissionLauncher;
    @Nullable
    private EmotionBroadLayout emotionBroadLayout;
    private boolean showMeetingRoomEntry = true;
    private boolean mentionEnabled;
    @Nullable
    private Runnable mentionPicker;

    public MessageListInputBroadPanel(@NonNull Context context) {
        this(context, null);
    }

    public MessageListInputBroadPanel(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MessageListInputBroadPanel(@NonNull Context context,
                                      @Nullable AttributeSet attrs,
                                      int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.message_list_input_broad_panel, this, true);
        bindViews();
    }

    public void init(@NonNull Fragment fragment,
                     @Nullable View interceptView,
                     @NonNull Callback callback) {
        if (panelSwitchHelper != null) {
            onDestroy();
        }
        this.fragment = fragment;
        this.interceptView = interceptView;
        this.callback = callback;
        fragment.getViewLifecycleOwner().getLifecycle().addObserver(this);
        registerPermissionLauncher(fragment);
        setupAudioRecorder();
        setupListeners();
        initPanelSwitchHelper();
        setupEmotionPanel();
        setupMorePanel();
    }

    /** 群聊显示「会议室」，单聊隐藏（对齐野火 {@code MeetingBroadExt#filter}）。 */
    public void configureMorePanel(boolean showMeetingRoom) {
        showMeetingRoomEntry = showMeetingRoom;
        if (fragment == null) {
            return;
        }
        View meeting = fragment.requireView().findViewById(R.id.panel_more_meeting);
        if (meeting != null) {
            meeting.setVisibility(showMeetingRoom ? VISIBLE : INVISIBLE);
        }
    }

    /** 群聊 @ 成员：输入 {@code @} 弹出选择页，长按头像由外部插入片段。 */
    public void configureMention(boolean enabled, @Nullable Runnable onPickMentionMember) {
        mentionEnabled = enabled;
        mentionPicker = onPickMentionMember;
    }

    public void insertMention(@NonNull CharSequence mentionText) {
        Editable editable = editText.getText();
        if (editable == null) {
            return;
        }
        int position = editText.getSelectionEnd();
        if (position < 0) {
            position = editable.length();
        }
        if (position > 0 && editable.charAt(position - 1) == '@') {
            editable.replace(position - 1, position, mentionText);
        } else {
            editable.insert(position, mentionText);
        }
        editText.requestFocus();
        updateSendButtonVisibility();
    }

    @NonNull
    public EditText getEditText() {
        return editText;
    }

    public void clearInput() {
        editText.setText("");
        clearQuote();
        updateSendButtonVisibility();
    }

    public void resetPanelState() {
        if (panelSwitchHelper != null) {
            panelSwitchHelper.resetState();
        }
    }

    public void disableInput(@NonNull String tip) {
        hidePanelIfShowing();
        if (inputContainerLinearLayout != null) {
            inputContainerLinearLayout.setVisibility(GONE);
        }
        if (disableInputTipView != null) {
            disableInputTipView.setVisibility(VISIBLE);
        }
        if (disableInputTipTextView != null) {
            disableInputTipTextView.setText(tip);
        }
    }

    public void enableInput() {
        if (inputContainerLinearLayout != null) {
            inputContainerLinearLayout.setVisibility(VISIBLE);
        }
        if (disableInputTipView != null) {
            disableInputTipView.setVisibility(GONE);
        }
    }

    public void hidePanelIfShowing() {
        if (isVoicePanelShowing()) {
            cancelMsgVoice(false);
        }
        if (panelSwitchHelper == null) {
            return;
        }
        if (panelSwitchHelper.isKeyboardState() || panelSwitchHelper.isPanelState()) {
            panelSwitchHelper.resetState();
        }
    }

    public boolean handleBackPressed() {
        if (isVoicePanelShowing()) {
            cancelMsgVoice(false);
            return true;
        }
        return panelSwitchHelper != null && panelSwitchHelper.hookSystemBackByPanelSwitcher();
    }

    public void onDestroy() {
        if (audioRecorderPanel != null) {
            audioRecorderPanel.finishRecord();
            audioRecorderPanel.deattch();
            audioRecorderPanel = null;
        }
        if (mViewAnimateVoice != null) {
            mViewAnimateVoice.cancelAnimation();
        }
        panelSwitchHelper = null;
        fragment = null;
        interceptView = null;
        callback = null;
        recordPermissionLauncher = null;
    }

    @Override
    public void onPause(@NonNull LifecycleOwner owner) {
        if (audioRecorderPanel != null && audioRecorderPanel.isVoiceRecording()) {
            cancelMsgVoice(false);
        }
    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        onDestroy();
    }

    private void bindViews() {
        editText = findViewById(R.id.editText);
        extImageView = findViewById(R.id.extImageView);
        emotionImageView = findViewById(R.id.emotionImageView);
        audioImageView = findViewById(R.id.audioImageView);
        mIvVoiceCancel = findViewById(R.id.mIvVoiceCancel);
        mIvVoiceSend = findViewById(R.id.mIvVoiceSend);
        audioButton = findViewById(R.id.audioButton);
        mViewAnimateVoice = findViewById(R.id.mViewAnimateVoice);
        sendButton = findViewById(R.id.sendButton);
        inputContainerLinearLayout = findViewById(R.id.inputContainerLinearLayout);
        disableInputTipView = findViewById(R.id.disableInputTipView);
        disableInputTipTextView = findViewById(R.id.disableInputTipTextView);
        refRelativeLayout = findViewById(R.id.refRelativeLayout);
        refEditText = findViewById(R.id.refEditText);
        clearRefImageButton = findViewById(R.id.clearRefImageButton);
    }

    /** 长按「引用」后展示输入区引用条。 */
    public void setQuoteMessage(@NonNull QuoteMessageContentUtil.QuoteInfo quoteInfo) {
        pendingQuote = quoteInfo;
        if (refRelativeLayout != null) {
            refRelativeLayout.setVisibility(VISIBLE);
        }
        if (refEditText != null) {
            refEditText.setText(quoteInfo.formatPreviewLine());
        }
        editText.requestFocus();
        updateSendButtonVisibility();
    }

    public void clearQuote() {
        pendingQuote = null;
        if (refRelativeLayout != null) {
            refRelativeLayout.setVisibility(GONE);
        }
        if (refEditText != null) {
            refEditText.setText("");
        }
    }

    @Nullable
    public QuoteMessageContentUtil.QuoteInfo getPendingQuote() {
        return pendingQuote;
    }

    private void registerPermissionLauncher(@NonNull Fragment fragment) {
        recordPermissionLauncher = fragment.registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (granted) {
                        enterVoiceRecordMode();
                    } else {
                        Toast.makeText(getContext(), com.cboda.baselibrary.R.string.voice_permission_denied, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupAudioRecorder() {
        audioRecorderPanel = new AudioRecorderBroadPanel(getContext());
        audioRecorderPanel.setRecordListener(new AudioRecorderBroadPanel.OnRecordListener() {
            @Override
            public void onRecordSuccess(String audioFile, int duration) {
                if (fragment == null || callback == null) {
                    return;
                }
                fragment.requireActivity().runOnUiThread(() -> {
                    hideAudioButton();
                    if (!TextUtils.isEmpty(audioFile)) {
                        callback.onVoiceRecorded(new File(audioFile), duration);
                    }
                });
            }

            @Override
            public void onRecordFail(String reason, boolean isShowSoftkey) {
                if (fragment == null) {
                    return;
                }
                fragment.requireActivity().runOnUiThread(() -> {
                    if (!TextUtils.isEmpty(reason)) {
                        Toast.makeText(getContext(), reason, Toast.LENGTH_SHORT).show();
                    }
                    if (isShowSoftkey) {
                        hideAudioButton();
                        editText.requestFocus();
                        resetPanelState();
                    } else {
                        closeRecordPanel();
                    }
                });
            }

            @Override
            public void onRecordStateChanged(AudioRecorderBroadPanel.RecordState state) {
            }
        });
    }

    private void setupListeners() {
        sendButton.setOnClickListener(v -> dispatchSend());
        if (clearRefImageButton != null) {
            clearRefImageButton.setOnClickListener(v -> clearQuote());
        }
        audioImageView.setOnClickListener(new OnSingleClickBroadListener() {
            @Override
            public void onSingleClick(View v) {
                onAudioImageViewClick();
            }
        });
        mIvVoiceCancel.setOnClickListener(new OnSingleClickBroadListener() {
            @Override
            public void onSingleClick(View v) {
                cancelMsgVoice(true);
            }
        });
        mIvVoiceSend.setOnClickListener(new OnSingleClickBroadListener() {
            @Override
            public void onSingleClick(View v) {
                sendVoiceMsg();
            }
        });
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!mentionEnabled || editText == null || !editText.isFocused()) {
                    return;
                }
                if (before == 0 && count == 1 && s.charAt(start) == '@' && mentionPicker != null) {
                    mentionPicker.run();
                }
                if (before == 1 && count == 0) {
                    handleMentionSpanDelete(start);
                }
                if (before == 0 && count > 0) {
                    handleMentionSpanInsert(start);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                updateSendButtonVisibility();
            }
        });
        updateSendButtonVisibility();
    }

    private void onAudioImageViewClick() {
        if (callback != null && !callback.canSendVoice()) {
            Toast.makeText(getContext(), com.cboda.baselibrary.R.string.voice_record_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        if (isVoicePanelShowing()) {
            hideAudioButton();
            editText.requestFocus();
            resetPanelState();
        } else {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO)
                    == PackageManager.PERMISSION_GRANTED) {
                enterVoiceRecordMode();
            } else if (recordPermissionLauncher != null) {
                recordPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
            } else {
                Toast.makeText(getContext(), com.cboda.baselibrary.R.string.voice_permission_denied, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void enterVoiceRecordMode() {
        resetPanelState();
        editText.clearFocus();
        showAudioButton();
    }

    private void showAudioButton() {
        if (interceptView != null) {
            interceptView.setVisibility(VISIBLE);
        }
        audioButton.setVisibility(VISIBLE);
        mIvVoiceCancel.setVisibility(VISIBLE);
        mIvVoiceSend.setVisibility(VISIBLE);
        audioImageView.setVisibility(GONE);
        editText.setVisibility(GONE);
        extImageView.setVisibility(GONE);
        sendButton.setVisibility(GONE);
        emotionImageView.setVisibility(GONE);
        if (mViewAnimateVoice != null) {
            mViewAnimateVoice.playAnimation();
        }
        if (fragment != null && audioRecorderPanel != null) {
            audioRecorderPanel.attach(fragment.requireView(), audioButton);
            audioRecorderPanel.startRecord();
        }
    }

    private void hideAudioButton() {
        if (interceptView != null) {
            interceptView.setVisibility(GONE);
        }
        audioButton.setVisibility(GONE);
        mIvVoiceCancel.setVisibility(GONE);
        mIvVoiceSend.setVisibility(GONE);
        audioImageView.setVisibility(VISIBLE);
        if (audioRecorderPanel != null) {
            audioRecorderPanel.deattch();
        }
        if (mViewAnimateVoice != null) {
            mViewAnimateVoice.cancelAnimation();
        }
        editText.setVisibility(VISIBLE);
        emotionImageView.setVisibility(VISIBLE);
        updateSendButtonVisibility();
    }

    private void closeRecordPanel() {
        hideAudioButton();
    }

    private void cancelMsgVoice(boolean showSoftkey) {
        if (audioRecorderPanel != null) {
            audioRecorderPanel.cancelRecord(showSoftkey);
        }
    }

    private void sendVoiceMsg() {
        if (audioRecorderPanel != null) {
            audioRecorderPanel.stopRecord();
        }
    }

    private boolean isVoicePanelShowing() {
        return audioButton != null && audioButton.getVisibility() == VISIBLE;
    }

    private void handleMentionSpanDelete(int start) {
        Editable text = editText.getText();
        if (text == null) {
            return;
        }
        MentionBroadSpan[] spans = text.getSpans(0, text.length(), MentionBroadSpan.class);
        if (spans == null) {
            return;
        }
        for (MentionBroadSpan span : spans) {
            if (text.getSpanEnd(span) == start
                    && text.getSpanFlags(span) == Spanned.SPAN_INCLUSIVE_EXCLUSIVE) {
                text.delete(text.getSpanStart(span), text.getSpanEnd(span));
                text.removeSpan(span);
                break;
            } else if (start >= text.getSpanStart(span) && start < text.getSpanEnd(span)) {
                text.removeSpan(span);
                break;
            }
        }
    }

    private void handleMentionSpanInsert(int start) {
        Editable text = editText.getText();
        if (text == null) {
            return;
        }
        MentionBroadSpan[] spans = text.getSpans(0, text.length(), MentionBroadSpan.class);
        if (spans == null) {
            return;
        }
        for (MentionBroadSpan span : spans) {
            if (start >= text.getSpanStart(span) && start < text.getSpanEnd(span)) {
                text.removeSpan(span);
                break;
            }
        }
    }

    private void updateSendButtonVisibility() {
        Editable editable = editText.getText();
        boolean hasText = editable != null && !editable.toString().trim().isEmpty();
        boolean hasMention = AtMentionCustomInfoBuilder.hasMentions(editable);
        sendButton.setVisibility(hasText || hasMention ? VISIBLE : GONE);
        extImageView.setVisibility(hasText || hasMention ? GONE : VISIBLE);
    }

    private void dispatchSend() {
        if (callback == null || editText.getText() == null) {
            return;
        }
        Editable editable = editText.getText();
        String body = AtMentionCustomInfoBuilder.buildMessageText(editable);
        String customInfo = AtMentionCustomInfoBuilder.buildCustomInfoFromSpans(editable);
        if (TextUtils.isEmpty(body) && TextUtils.isEmpty(customInfo)) {
            return;
        }
        QuoteMessageContentUtil.QuoteInfo quote = pendingQuote;
        callback.onSendText(body, customInfo, quote);
    }

    private void initPanelSwitchHelper() {
        if (panelSwitchHelper != null || fragment == null || !fragment.isAdded()) {
            return;
        }
        if (fragment.requireView().findViewById(R.id.panel_switch_layout) == null) {
            return;
        }
        int emotionPanelHeight = getResources().getDimensionPixelSize(R.dimen.chat_panel_emotion_height);
        int morePanelHeight = getResources().getDimensionPixelSize(R.dimen.chat_panel_more_height);
        panelSwitchHelper = new PanelSwitchHelper.Builder(fragment)
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
                        return emotionPanelHeight;
                    }

                    @Override
                    public int getPanelTriggerId() {
                        return R.id.emotionImageView;
                    }
                })
                .addPanelHeightMeasurer(new PanelHeightMeasurer() {
                    @Override
                    public boolean synchronizeKeyboardHeight() {
                        return false;
                    }

                    @Override
                    public int getTargetPanelDefaultHeight() {
                        return morePanelHeight;
                    }

                    @Override
                    public int getPanelTriggerId() {
                        return R.id.extImageView;
                    }
                })
                .addPanelChangeListener(new OnPanelChangeListener() {
                    @Override
                    public void onKeyboard() {
                        resetPanelTriggerIcons();
                        notifyPanelShown();
                    }

                    @Override
                    public void onNone() {
                        resetPanelTriggerIcons();
                    }

                    @Override
                    public void onPanel(IPanelView panelView) {
                        updatePanelTriggerIcons(panelView);
                        notifyPanelShown();
                    }

                    @Override
                    public void onPanelSizeChange(IPanelView panelView, boolean portrait,
                                                  int oldWidth, int oldHeight, int width, int height) {
                    }
                })
                .contentScrollOutsideEnable(true)
//                .logTrack(true)
                .build(false);
    }

    private void notifyPanelShown() {
        if (callback != null) {
            callback.onKeyboardOrPanelShown();
        }
    }

    private void setupEmotionPanel() {
        if (fragment == null) {
            return;
        }
        emotionBroadLayout = fragment.requireView().findViewById(R.id.emotionLayout);
        if (emotionBroadLayout == null) {
            return;
        }
        emotionBroadLayout.setEmotionAddVisiable(false);
        emotionBroadLayout.setEmotionSettingVisiable(false);
        emotionBroadLayout.setStickerVisible(false);
        emotionBroadLayout.setEmotionSelectedListener(this);
    }

    private void resetPanelTriggerIcons() {
        emotionImageView.setImageResource(com.cboda.chatui.R.mipmap.ic_msg_broad_emo);
        extImageView.setImageResource(com.cboda.chatui.R.mipmap.ic_msg_broad_add);
    }

    private void updatePanelTriggerIcons(@Nullable IPanelView panelView) {
        if (!(panelView instanceof View)) {
            resetPanelTriggerIcons();
            return;
        }
        int panelId = ((View) panelView).getId();
        if (panelId == R.id.panel_emotion) {
            emotionImageView.setImageResource(com.cboda.chatui.R.mipmap.ic_chat_broad_keyboard);
            extImageView.setImageResource(com.cboda.chatui.R.mipmap.ic_msg_broad_add);
        } else if (panelId == R.id.panel_more) {
            extImageView.setImageResource(com.cboda.chatui.R.mipmap.ic_msg_broad_add_off);
            emotionImageView.setImageResource(com.cboda.chatui.R.mipmap.ic_msg_broad_emo);
        } else {
            resetPanelTriggerIcons();
        }
    }

    @Override
    public void onEmojiSelected(String key) {
        if (editText.getText() == null) {
            return;
        }
        if ("/DEL".equals(key)) {
            editText.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
            return;
        }
        try {
            int code = Integer.decode(key);
            char[] chars = Character.toChars(code);
            StringBuilder value = new StringBuilder();
            for (char ch : chars) {
                value.append(ch);
            }
            int start = editText.getSelectionStart();
            if (start < 0) {
                start = editText.getText().length();
            }
            editText.getText().insert(start, value.toString());
        } catch (NumberFormatException ignored) {
        }
    }

    @Override
    public void onStickerSelected(String categoryName, String stickerName, String stickerBitmapPath) {
        // 自研 IM 暂不支持贴图消息
    }

    private void setupMorePanel() {
        if (fragment == null) {
            return;
        }
        View root = fragment.requireView();
        bindMoreItem(root.findViewById(R.id.panel_more_album),
                com.cboda.chatui.R.mipmap.ic_func_broad_pic,
                com.cboda.chatui.R.string.msg_album,
                () -> {
                    if (callback != null) {
                        callback.onOpenAlbum();
                    }
                });
        bindMoreItem(root.findViewById(R.id.panel_more_camera),
                com.cboda.chatui.R.mipmap.ic_func_broad_shot,
                com.cboda.chatui.R.string.shot,
                () -> {
                    if (callback != null) {
                        callback.onOpenCamera();
                    }
                });
        bindMoreItem(root.findViewById(R.id.panel_more_collect),
                com.cboda.chatui.R.mipmap.ic_func_broad_collect,
                com.cboda.chatui.R.string.收藏夹,
                () -> {
                    if (callback != null) {
                        callback.onOpenCollect();
                    }
                });
        bindMoreItem(root.findViewById(R.id.panel_more_voice_call),
                com.cboda.chatui.R.mipmap.ic_func_broad_video,
                com.cboda.chatui.R.string.yuyintonghua,
                () -> {
                    if (callback != null) {
                        callback.onOpenVoiceCall();
                    }
                });
        bindMoreItem(root.findViewById(R.id.panel_more_file),
                com.cboda.chatui.R.mipmap.ic_func_broad_file,
                com.cboda.chatui.R.string.file,
                () -> {
                    if (callback != null) {
                        callback.onOpenFile();
                    }
                });
        bindMoreItem(root.findViewById(R.id.panel_more_user_card),
                com.cboda.chatui.R.mipmap.ic_user_broad_card,
                com.cboda.chatui.R.string.msg_business_card,
                () -> {
                    if (callback != null) {
                        callback.onOpenBusinessCard();
                    }
                });
        bindMoreItem(root.findViewById(R.id.panel_more_meeting),
                com.cboda.chatui.R.mipmap.ic_func_broad_meeting,
                com.cboda.chatui.R.string.msg_meeting_room,
                () -> {
                    if (callback != null) {
                        callback.onOpenMeetingRoom();
                    }
                });
        bindMoreItem(root.findViewById(R.id.panel_more_anti_fraud),
                com.cboda.chatui.R.mipmap.ic_func_broad_anti_fraud,
                com.cboda.chatui.R.string.msg_anti_fraud,
                () -> {
                    if (callback != null) {
                        callback.onOpenAntiFraud();
                    }
                });
        configureMorePanel(showMeetingRoomEntry);
    }

    private void bindMoreItem(@Nullable View itemRoot,
                              int iconRes,
                              int titleRes,
                              @NonNull Runnable action) {
        if (itemRoot == null) {
            return;
        }
        ImageView icon = itemRoot.findViewById(R.id.panel_more_item_icon);
        TextView title = itemRoot.findViewById(R.id.panel_more_item_title);
        if (icon != null) {
            icon.setImageResource(iconRes);
        }
        if (title != null) {
            title.setText(titleRes);
        }
        itemRoot.setOnClickListener(new OnSingleClickBroadListener() {
            @Override
            public void onSingleClick(View v) {
                resetPanelState();
                action.run();
            }
        });
    }
}
