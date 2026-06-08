package com.freddy.chat.widget;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.cboda.baselibrary.utils.OnSingleClickBroadListener;
import com.cboda.chatui.audio.AudioRecorderBroadPanel;
import com.cboda.intelligent.R;
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
public class MessageListInputBroadPanel extends FrameLayout implements DefaultLifecycleObserver {

    private static final String[] CHAT_EMOTIONS = {
            "😀", "😁", "😂", "🤣", "😊", "😍", "😘", "😎",
            "🙂", "😉", "😢", "😭", "😡", "👍", "👎", "🙏",
            "👏", "🎉", "❤️", "💔", "🔥", "✨", "🌹", "🎁"
    };

    public interface Callback {
        void onSendText(@NonNull String text);

        boolean canSendVoice();

        void onVoiceRecorded(@NonNull File audioFile, int durationSec);

        void onOpenAlbum();

        void onOpenCamera();

        void onOpenFile();

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

    @NonNull
    public EditText getEditText() {
        return editText;
    }

    public void clearInput() {
        editText.setText("");
        updateSendButtonVisibility();
    }

    public void resetPanelState() {
        if (panelSwitchHelper != null) {
            panelSwitchHelper.resetState();
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
    }

    private void registerPermissionLauncher(@NonNull Fragment fragment) {
        recordPermissionLauncher = fragment.registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (granted) {
                        enterVoiceRecordMode();
                    } else {
                        Toast.makeText(getContext(), R.string.voice_permission_denied, Toast.LENGTH_SHORT).show();
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
        editText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND
                    || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                    && event.getAction() == KeyEvent.ACTION_DOWN)) {
                dispatchSend();
                return true;
            }
            return false;
        });
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
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
            Toast.makeText(getContext(), R.string.voice_record_failed, Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getContext(), R.string.voice_permission_denied, Toast.LENGTH_SHORT).show();
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

    private void updateSendButtonVisibility() {
        boolean hasText = editText.getText() != null && editText.getText().toString().trim().length() > 0;
        sendButton.setVisibility(hasText ? VISIBLE : GONE);
        extImageView.setVisibility(hasText ? GONE : VISIBLE);
    }

    private void dispatchSend() {
        if (callback == null || editText.getText() == null) {
            return;
        }
        String text = editText.getText().toString().trim();
        if (TextUtils.isEmpty(text)) {
            return;
        }
        callback.onSendText(text);
    }

    private void initPanelSwitchHelper() {
        if (panelSwitchHelper != null || fragment == null) {
            return;
        }
        int panelHeight = getResources().getDimensionPixelSize(R.dimen.chat_panel_default_height);
        panelSwitchHelper = new PanelSwitchHelper.Builder(fragment.requireActivity())
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
                        return panelHeight;
                    }

                    @Override
                    public int getPanelTriggerId() {
                        return R.id.extImageView;
                    }
                })
                .addPanelChangeListener(new OnPanelChangeListener() {
                    @Override
                    public void onKeyboard() {
                        notifyPanelShown();
                    }

                    @Override
                    public void onNone() {
                    }

                    @Override
                    public void onPanel(IPanelView panelView) {
                        notifyPanelShown();
                    }

                    @Override
                    public void onPanelSizeChange(IPanelView panelView, boolean portrait,
                                                  int oldWidth, int oldHeight, int width, int height) {
                    }
                })
                .contentScrollOutsideEnable(true)
                .logTrack(true)
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
        RecyclerView rv = fragment.requireView().findViewById(R.id.rv_panel_emotion);
        if (rv == null) {
            return;
        }
        rv.setLayoutManager(new GridLayoutManager(getContext(), 8));
        rv.setAdapter(new EmotionPanelAdapter(CHAT_EMOTIONS, emoji -> {
            int start = editText.getSelectionStart();
            if (start < 0) {
                start = editText.getText().length();
            }
            editText.getText().insert(start, emoji);
        }));
    }

    private void setupMorePanel() {
        if (fragment == null) {
            return;
        }
        View root = fragment.requireView();
        View album = root.findViewById(R.id.panel_more_album);
        View camera = root.findViewById(R.id.panel_more_camera);
        View file = root.findViewById(R.id.panel_more_file);
        if (album != null) {
            album.setOnClickListener(v -> {
                if (callback != null) {
                    callback.onOpenAlbum();
                }
            });
        }
        if (camera != null) {
            camera.setOnClickListener(v -> {
                if (callback != null) {
                    callback.onOpenCamera();
                }
            });
        }
        if (file != null) {
            file.setOnClickListener(v -> {
                if (callback != null) {
                    callback.onOpenFile();
                }
            });
        }
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
        public Holder onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
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
}
