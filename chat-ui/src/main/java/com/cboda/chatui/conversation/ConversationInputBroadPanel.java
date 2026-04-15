/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversation;

import static com.cboda.chatui.conversation.ConversationBroadFragment.REQUEST_BROAD_PICK_MENTION_CONTACT;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;

import com.airbnb.lottie.LottieAnimationView;
import com.cboda.baselibrary.toast.ToastBroadUtils;
import com.cboda.baselibrary.utils.OnSingleClickBroadListener;
import com.cboda.chatemoji.EmotionBroadLayout;
import com.cboda.chatemoji.IEmotionExtClickBroadListener;
import com.cboda.chatemoji.IEmotionSelectedBroadListener;
import com.cboda.chatemoji.LQREmotionBroadKit;
import com.cboda.chatemoji.MoonBroadUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.cboda.chatui.BroadConfig;
import com.cboda.chatui.R;
import com.cboda.chatui.WfcWebViewBroadActivity;
import com.cboda.chatui.audio.AudioRecorderBroadPanel;
import com.cboda.chatui.audio.PttBroadPanel;
import com.cboda.chatui.bridge.MyCommonBroadBridge;
import com.cboda.chatui.conversation.ext.core.ConversationBroadExtension;
import com.cboda.chatui.conversation.mention.Mention;
import com.cboda.chatui.conversation.mention.MentionGroupMemberBroadActivity;
import com.cboda.chatui.conversation.mention.MentionBroadSpan;
import com.cboda.chatui.group.GroupViewBroadModel;
import com.cboda.chatui.quickmeet.MeetCallManager;
import com.cboda.chatui.viewmodel.MessageViewBroadModel;
import com.cboda.chatui.widget.InputAwareBroadLayout;
import com.cboda.chatui.widget.KeyboardHeightFrameBroadLayout;
import com.cboda.chatui.widget.ShapeKeyboardHeightFrameBroadLayout;
import com.cboda.chatui.widget.SimpleTextBroadWatcher;
import com.cboda.chatui.widget.ViewPagerBroadFixed;

import cn.wildfirechat.message.ChannelMenuEventMessageContent;
import cn.wildfirechat.message.Message;
import cn.wildfirechat.message.TextMessageContent;
import cn.wildfirechat.message.TypingMessageContent;
import cn.wildfirechat.model.ChannelInfo;
import cn.wildfirechat.model.ChannelMenu;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.ConversationInfo;
import cn.wildfirechat.model.GroupInfo;
import cn.wildfirechat.model.QuoteInfo;
import cn.wildfirechat.ptt.PTTClient;
import cn.wildfirechat.remote.ChatManager;

import com.cboda.baselibrary.common.BridgeImEnum;

public class ConversationInputBroadPanel extends FrameLayout implements IEmotionSelectedBroadListener, DefaultLifecycleObserver {

    LinearLayout inputContainerLinearLayout;
    TextView disableInputTipTextView;
    View disableInputTipView;

    ImageView menuImageView;
    ImageView audioImageView;
    ImageView mIvVoiceSend;
    ImageView mIvVoiceCancel;
    ImageView pttImageView;
    View audioButton;
    LottieAnimationView mViewAnimateVoice;
    EditText editText;
    ImageView emotionImageView;
    ImageView extImageView;
    Button sendButton;

    LinearLayout channelMenuContainerLinearLayout;

    ShapeKeyboardHeightFrameBroadLayout emotionContainerFrameLayout;
    EmotionBroadLayout emotionBroadLayout;
    ShapeKeyboardHeightFrameBroadLayout extContainerFrameLayout;

    ViewPagerBroadFixed extViewPager;

    RelativeLayout refRelativeLayout;
    EditText refEditText;

    ConversationBroadExtension extension;
    private Conversation conversation;
    private MessageViewBroadModel messageViewModel;
    private ConversationViewBroadModel conversationViewModel;
    private InputAwareBroadLayout rootLinearLayout;
    private View mViewIntercept;
    private Fragment fragment;
    private FragmentActivity activity;
    private AudioRecorderBroadPanel audioRecorderPanel;
    private PttBroadPanel pttPanel;

    private long lastTypingTime;
    private String draftString;
    private static final int TYPING_INTERVAL_IN_SECOND = 10;
    private static final int MAX_EMOJI_PER_MESSAGE = 50;
    private int messageEmojiCount = 0;
    private SharedPreferences sharedPreferences;

    private boolean isPttMode = false;

    private OnConversationInputPanelStateChangeListener onConversationInputPanelStateChangeListener;

    public ConversationInputBroadPanel(@NonNull Context context) {
        super(context);
    }

    public ConversationInputBroadPanel(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

    }

    public ConversationInputBroadPanel(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ConversationInputBroadPanel(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

    }

    public void setOnConversationInputPanelStateChangeListener(OnConversationInputPanelStateChangeListener onConversationInputPanelStateChangeListener) {
        this.onConversationInputPanelStateChangeListener = onConversationInputPanelStateChangeListener;
    }

    public void bind(FragmentActivity activity, InputAwareBroadLayout rootInputAwareLayout) {

    }

    public void setupConversation(Conversation conversation) {
        this.conversation = conversation;
        this.extension.bind(this.messageViewModel, conversation);

        setDraft();
        if (conversation.type == Conversation.ConversationType.Channel) {
            showChannelMenu();
        } else {
            menuImageView.setVisibility(GONE);
        }

        SharedPreferences sp = fragment.getContext().getSharedPreferences(BroadConfig.SP_CONFIG_FILE_NAME, Context.MODE_PRIVATE);
        boolean pttEnabled = sp.getBoolean("pttEnabled", true);
        if (pttEnabled && PTTClient.checkAddress(ChatManager.Instance().getHost()) && conversation.type != Conversation.ConversationType.Channel) {
            pttImageView.setVisibility(View.VISIBLE);
            pttPanel = new PttBroadPanel(getContext());
        }
    }

    private QuoteInfo quoteInfo;

    public void quoteMessage(Message message) {
        this.quoteInfo = QuoteInfo.initWithMessage(message);
        if (audioButton.getVisibility() == VISIBLE) {
            hideAudioButton();
        }
        refRelativeLayout.setVisibility(VISIBLE);
        refEditText.setText(quoteInfo.getUserDisplayName() + ": " + quoteInfo.getMessageDigest());
        editText.requestFocus();
        // FYI: https://stackoverflow.com/questions/5520085/android-show-softkeyboard-with-showsoftinput-is-not-working
        rootLinearLayout.postDelayed(() -> {
            rootLinearLayout.showSoftkey(editText);
        }, 100);
    }

    private void clearQuoteMessage() {
        if (refRelativeLayout.getVisibility() == VISIBLE) {
            refEditText.setText("");
            refRelativeLayout.setVisibility(GONE);
        }
        quoteInfo = null;
    }

    public void disableInput(String tip) {
        closeConversationInputPanel();
        inputContainerLinearLayout.setVisibility(GONE);
        disableInputTipView.setVisibility(VISIBLE);
        disableInputTipTextView.setText(tip);
    }

    public void enableInput() {
        inputContainerLinearLayout.setVisibility(VISIBLE);
        disableInputTipView.setVisibility(GONE);
    }

    public void onDestroy() {
        this.extension.onDestroy();
        if (audioRecorderPanel != null) {
            audioRecorderPanel.finishRecord();
            audioRecorderPanel.deattch();
        }
        if (pttPanel != null) {
            pttPanel.deattch();
        }
    }

    @Override
    public void onPause(@NonNull LifecycleOwner owner) {
        if (audioRecorderPanel.isVoiceRecording()) {
            cancelMsgVoice(null, false);
        }
    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        if (mViewAnimateVoice != null) {
            mViewAnimateVoice.cancelAnimation();
        }
    }

    public void init(Fragment fragment, InputAwareBroadLayout rootInputAwareLayout, View mViewIntercept) {
        LayoutInflater.from(getContext()).inflate(R.layout.conversation_input_broad_panel, this, true);
        bindViews();

        this.activity = fragment.getActivity();
        this.fragment = fragment;
        this.rootLinearLayout = rootInputAwareLayout;
        this.mViewIntercept = mViewIntercept;

        fragment.getViewLifecycleOwner().getLifecycle().addObserver(this);
        this.extension = new ConversationBroadExtension(fragment, this, extViewPager);


        sharedPreferences = getContext().getSharedPreferences("sticker", Context.MODE_PRIVATE);

        // emotion
        emotionBroadLayout.setEmotionAddVisiable(false);
        emotionBroadLayout.setEmotionSettingVisiable(false);

        // audio record panel
        audioRecorderPanel = new AudioRecorderBroadPanel(getContext());
        audioRecorderPanel.setRecordListener(new AudioRecorderBroadPanel.OnRecordListener() {
            @Override
            public void onRecordSuccess(String audioFile, int duration) {
                //发送文件
                File file = new File(audioFile);
                if (file.exists()) {
                    messageViewModel.sendAudioFile(conversation, Uri.parse(audioFile), duration);
                }
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showRecordPanel(audioButton);
                    }
                });
            }

            @Override
            public void onRecordFail(String reason, boolean isShowSoftkey) {
                if (!TextUtils.isEmpty(reason)) {
                    Toast.makeText(activity, reason, Toast.LENGTH_SHORT).show();
                }
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (isShowSoftkey) {
                            showRecordPanel(mIvVoiceCancel);
                        } else {
                            closeRecordPanel();
                        }

                    }
                });
            }

            @Override
            public void onRecordStateChanged(AudioRecorderBroadPanel.RecordState state) {
                if (state == AudioRecorderBroadPanel.RecordState.START) {
                    TypingMessageContent content = new TypingMessageContent(TypingMessageContent.TYPING_VOICE);
                    messageViewModel.sendMessage(conversation, content, false);
                }
            }
        });

        // 野火表情
        emotionBroadLayout.setEmotionSelectedListener(this);
        emotionBroadLayout.setEmotionExtClickListener(new IEmotionExtClickBroadListener() {
            @Override
            public void onEmotionAddClick(View view) {
                Toast.makeText(activity, "add", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onEmotionSettingClick(View view) {
                Toast.makeText(activity, "setting", Toast.LENGTH_SHORT).show();
            }
        });

        messageViewModel = new ViewModelProvider(fragment).get(MessageViewBroadModel.class);
        conversationViewModel = new ViewModelProvider(fragment).get(ConversationViewBroadModel.class);

        if (conversation != null) {
            if (conversation.type == Conversation.ConversationType.Channel) {
                showChannelMenu();
            } else {
                menuImageView.setVisibility(GONE);
            }
        }
    }

    private void bindViews() {
        inputContainerLinearLayout = findViewById(R.id.inputContainerLinearLayout);
        disableInputTipTextView = findViewById(R.id.disableInputTipTextView);
        disableInputTipView = findViewById(R.id.disableInputTipView);
        menuImageView = findViewById(R.id.menuImageView);
        audioImageView = findViewById(R.id.audioImageView);
        mIvVoiceSend = findViewById(R.id.mIvVoiceSend);
        mIvVoiceCancel = findViewById(R.id.mIvVoiceCancel);
        pttImageView = findViewById(R.id.pttImageView);
        audioButton = findViewById(R.id.audioButton);
        mViewAnimateVoice = findViewById(R.id.mViewAnimateVoice);
        editText = findViewById(R.id.editText);
        emotionImageView = findViewById(R.id.emotionImageView);
        extImageView = findViewById(R.id.extImageView);
        sendButton = findViewById(R.id.sendButton);
        channelMenuContainerLinearLayout = findViewById(R.id.channelMenuContainerLinearLayout);
        emotionContainerFrameLayout = findViewById(R.id.emotionContainerFrameLayout);
        emotionBroadLayout = findViewById(R.id.emotionLayout);
        extContainerFrameLayout = findViewById(R.id.extContainerContainerLayout);
        extViewPager = findViewById(R.id.conversationExtViewPager);
        refRelativeLayout = findViewById(R.id.refRelativeLayout);
        refEditText = findViewById(R.id.refEditText);

        extImageView.setOnClickListener(v -> onExtImageViewClick());
        emotionImageView.setOnClickListener(v -> onEmotionImageViewClick());
        findViewById(R.id.clearRefImageButton).setOnClickListener(v -> onClearRefImageButtonClick());
        menuImageView.setOnClickListener(v -> showChannelMenu());
        audioImageView.setOnClickListener(new OnSingleClickBroadListener() {
            @Override
            public void onSingleClick(View v) {
                if (MeetCallManager.getInstance().isCallingState()){
                    ToastBroadUtils.show(com.cboda.baselibrary.R.string.msg_meeting_func_unavailable);
                }else {
                    showRecordPanel(v);
                }

            }
        });
        pttImageView.setOnClickListener(this::showRecordPanel);

        mIvVoiceCancel.setOnClickListener(new OnSingleClickBroadListener() {
            @Override
            public void onSingleClick(View v) {
                cancelMsgVoice(v, true);
            }
        });
        mIvVoiceSend.setOnClickListener(new OnSingleClickBroadListener() {
            @Override
            public void onSingleClick(View v) {
                sendVoiceMsg(v);
            }
        });
        sendButton.setOnClickListener(v -> sendMessage());
        audioButton.setOnClickListener(v -> {
        });

        editText.addTextChangedListener(new SimpleTextBroadWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                onInputTextChanged(s, start, before, count);
            }

            @Override
            public void afterTextChanged(Editable s) {
                afterInputTextChanged(s);
            }
        });

    }

    private void sendVoiceMsg(View view) {
//        showRecordPanel(view);
        if (audioRecorderPanel != null) {
            audioRecorderPanel.stopRecord();
        }
    }

    void onExtImageViewClick() {
        if (audioButton.getTag() != null) {
            return;
        }
        if (rootLinearLayout.getCurrentInput() == extContainerFrameLayout) {
            hideConversationExtension();
            rootLinearLayout.showSoftkey(editText);
        } else {
            emotionImageView.setImageResource(R.mipmap.ic_msg_broad_emo);
            showConversationExtension();
        }
    }

    void onEmotionImageViewClick() {

        if (audioRecorderPanel.isShowingRecorder() || (pttPanel != null && pttPanel.isShowingTalking())) {
            return;
        }
        if (rootLinearLayout.getCurrentInput() == emotionContainerFrameLayout) {
            hideEmotionLayout();
            rootLinearLayout.showSoftkey(editText);
        } else {
            hideAudioButton();
            showEmotionLayout();
        }
    }

    void onClearRefImageButtonClick() {
        clearQuoteMessage();
        updateConversationDraft();
    }

    void onInputTextChanged(CharSequence s, int start, int before, int count) {
        if (activity.getCurrentFocus() == editText) {
            if (conversation.type == Conversation.ConversationType.Group) {
                if (before == 0 && count == 1 && s.charAt(start) == '@') {
//                    if (start == 0 || s.charAt(start - 1) == ' ') {
                    mentionGroupMember();
//                    }
                }
                // delete
                if (before == 1 && count == 0) {
                    Editable text = editText.getText();
                    MentionBroadSpan[] spans = text.getSpans(0, text.length(), MentionBroadSpan.class);
                    if (spans != null) {
                        for (MentionBroadSpan span : spans) {
                            if (text.getSpanEnd(span) == start && text.getSpanFlags(span) == Spanned.SPAN_INCLUSIVE_EXCLUSIVE) {
                                text.delete(text.getSpanStart(span), text.getSpanEnd(span));
                                text.removeSpan(span);
                                break;
                            } else if (start >= text.getSpanStart(span) && start < text.getSpanEnd(span)) {
                                text.removeSpan(span);
                                break;
                            }
                        }
                    }
                }

                // insert
                if (before == 0 && count > 0) {
                    Editable text = editText.getText();
                    MentionBroadSpan[] spans = text.getSpans(0, text.length(), MentionBroadSpan.class);
                    if (spans != null) {
                        for (MentionBroadSpan span : spans) {
                            if (start >= text.getSpanStart(span) && start < text.getSpanEnd(span)) {
                                text.removeSpan(span);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    private void mentionGroupMember() {
        Intent intent = new Intent(activity, MentionGroupMemberBroadActivity.class);
        GroupViewBroadModel groupViewModel = new ViewModelProvider(fragment).get(GroupViewBroadModel.class);
        GroupInfo groupInfo = groupViewModel.getGroupInfo(conversation.target, true);
        intent.putExtra("groupInfo", groupInfo);
        fragment.startActivityForResult(intent, REQUEST_BROAD_PICK_MENTION_CONTACT);
    }

    void afterInputTextChanged(Editable editable) {
        if (editText.getText().toString().trim().length() > 0) {
            if (activity.getCurrentFocus() == editText) {
                notifyTyping(TypingMessageContent.TYPING_TEXT);
            }
            sendButton.setVisibility(View.VISIBLE);
            extImageView.setVisibility(View.GONE);
        } else {
            sendButton.setVisibility(View.GONE);
            extImageView.setVisibility(View.VISIBLE);
        }
    }

    public void showChannelMenu() {
        ChannelInfo channelInfo = ChatManager.Instance().getChannelInfo(conversation.target, false);
        if (channelInfo.menus == null || channelInfo.menus.isEmpty()) {
            Toast.makeText(activity, "频道暂未配置菜单", Toast.LENGTH_SHORT).show();
            return;
        }

        if (channelMenuContainerLinearLayout.getVisibility() == VISIBLE) {
            menuImageView.setImageResource(R.mipmap.ic_chat_broad_menu);
            audioImageView.setVisibility(VISIBLE);
            extImageView.setVisibility(VISIBLE);
            emotionImageView.setVisibility(VISIBLE);
            editText.setVisibility(VISIBLE);
            channelMenuContainerLinearLayout.removeAllViews();
            channelMenuContainerLinearLayout.setVisibility(GONE);
            return;
        }
        menuImageView.setImageResource(R.mipmap.ic_chat_broad_keyboard);
        audioImageView.setVisibility(GONE);
        extImageView.setVisibility(GONE);
        emotionImageView.setVisibility(GONE);
        editText.setVisibility(GONE);
        rootLinearLayout.hideSoftkey(editText, null);
        channelMenuContainerLinearLayout.setVisibility(VISIBLE);
        channelMenuContainerLinearLayout.removeAllViews();
        if (channelInfo != null && channelInfo.menus != null) {
            for (ChannelMenu menu : channelInfo.menus) {
                TextView textView = new TextView(getContext());
                if (menu.subMenus != null && menu.subMenus.size() > 0) {
                    textView.setText("≡ " + menu.name);
                } else {
                    textView.setText(menu.name);
                }
                textView.setGravity(Gravity.CENTER);
                textView.setBackgroundColor(Color.parseColor("#F8F8F8"));
                Drawable drawable = getResources().getDrawable(R.drawable.selector_common_broad_item);
                textView.setBackground(drawable);

                textView.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
                textView.setOnClickListener(v -> {
                    if (menu.subMenus != null && menu.subMenus.size() > 0) {
                        PopupMenu popupMenu = new PopupMenu(getContext(), textView, Gravity.TOP);
                        for (ChannelMenu sm : menu.subMenus) {
                            MenuItem item = popupMenu.getMenu().add(sm.name);
                            item.setOnMenuItemClickListener(item1 -> {
                                openChannelMenu(sm);
                                return true;
                            });

                        }
                        popupMenu.show();
                    } else {
                        openChannelMenu(menu);
                    }
                });
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, 0);
                params.leftMargin = 2;
                params.weight = 1;
                textView.setLayoutParams(params);
                channelMenuContainerLinearLayout.addView(textView);
            }
        }
    }

    public void showRecordPanel(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (activity.checkCallingOrSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
//                eLogD("showRecordPanelClass","true");
                fragment.requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 100);
                ToastBroadUtils.show("请先授权麦克风录音权限！");
                return;
            } else {
//                eLogD("showRecordPanelClass","false");
            }
        }

        if (audioButton.isShown()) {
            hideAudioButton();
            editText.requestFocus();
            rootLinearLayout.showSoftkey(editText);
        } else {
//            editText.clearFocus();

            if (view.getId() == R.id.pttImageView) {
                isPttMode = true;
            }
            showAudioButton();
            hideEmotionLayout();
            rootLinearLayout.hideSoftkey(editText, null);
            hideConversationExtension();
        }
    }

    public void closeRecordPanel() {
        hideAudioButton();
    }

    private void openChannelMenu(ChannelMenu menu) {
        ChannelMenuEventMessageContent content = new ChannelMenuEventMessageContent();
        content.setMenu(menu);
        ChatManager.Instance().sendMessage(conversation, content, null, 0, null);

        switch (menu.type) {
            case "view":
                if (!TextUtils.isEmpty(menu.url)) {
                    WfcWebViewBroadActivity.loadUrl(getContext(), "", menu.url);
                }
                break;
            case "miniprogram":
                break;
            default:
                break;
        }
    }

    void sendMessage() {
        messageEmojiCount = 0;
        Editable content = editText.getText();
        if (TextUtils.isEmpty(content)) {
            return;
        }

        TextMessageContent txtContent = new TextMessageContent(content.toString().trim());
        if (this.quoteInfo != null) {
            txtContent.setQuoteInfo(quoteInfo);
        }
        clearQuoteMessage();

        if (conversation.type == Conversation.ConversationType.Group) {
            MentionBroadSpan[] mentions = content.getSpans(0, content.length(), MentionBroadSpan.class);
            if (mentions != null && mentions.length > 0) {
                txtContent.mentionedType = 1;
                List<String> mentionedIds = new ArrayList<>();
                for (MentionBroadSpan span : mentions) {
                    if (!mentionedIds.contains(span.getUid())) {
                        mentionedIds.add(span.getUid());
                    }
                    // mentionAll
                    if (span.isMentionAll()) {
                        txtContent.mentionedType = 2;
                        break;
                    }
                }
                if (txtContent.mentionedType == 1) {
                    txtContent.mentionedTargets = mentionedIds;
                }
            }
        }

        MyCommonBroadBridge.getInstance().setMyBridgeMethod(getContext(), BridgeImEnum.AI.getValue(), conversation.target, editText.getText().toString().trim(), null);
        messageViewModel.sendTextMsg(conversation, txtContent);
        editText.setText("");
    }

    void cancelMsgVoice(View v, boolean isShowSoftkey) {
//        showRecordPanel(v);
        if (audioRecorderPanel != null) {
            audioRecorderPanel.cancelRecord(isShowSoftkey);
        }
    }

    public void onKeyboardShown() {
        hideEmotionLayout();
    }

    public void onKeyboardHidden() {
        // do nothing
    }

    private void setDraft() {
        MentionBroadSpan[] spans = editText.getText().getSpans(0, editText.getText().length(), MentionBroadSpan.class);
        if (spans != null) {
            for (MentionBroadSpan span : spans) {
                editText.getText().removeSpan(span);
            }
        }
        ConversationInfo conversationInfo = conversationViewModel.getConversationInfo(conversation);
        if (conversationInfo == null || TextUtils.isEmpty(conversationInfo.draft)) {
            return;
        }
        Draft draft = Draft.fromDraftJson(conversationInfo.draft);
        if (draft == null || (TextUtils.isEmpty(draft.getContent()) && draft.getQuoteInfo() == null)) {
            return;
        }
        draftString = draft.getContent();
        messageEmojiCount = draft.getEmojiCount();

        quoteInfo = draft.getQuoteInfo();
        if (quoteInfo != null) {
            refRelativeLayout.setVisibility(VISIBLE);
            refEditText.setText(quoteInfo.getUserDisplayName() + ": " + quoteInfo.getMessageDigest());
        }

        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(draftString);
        List<Mention> mentions = draft.getMentions();
        if (mentions != null) {
            for (Mention mention : mentions) {
                if (mention.isMentionAll()) {
                    spannableStringBuilder.setSpan(new MentionBroadSpan(true), mention.getStart(), mention.getEnd(), Spanned.SPAN_MARK_MARK);
                } else {
                    spannableStringBuilder.setSpan(new MentionBroadSpan(mention.getUid()), mention.getStart(), mention.getEnd(), Spanned.SPAN_MARK_MARK);
                }
            }
        }

        editText.setText(spannableStringBuilder);
//         FIXME: 4/16/21 恢复草稿时，消息列表界面会抖动，且没有滑动到最后
//        editText.requestFocus();
    }

    public void setInputText(String text) {
        if (TextUtils.isEmpty(text)) {
            return;
        }
        editText.setText(text);
        editText.setSelection(text.length());
        editText.requestFocus();
        rootLinearLayout.showSoftkey(editText);
    }

    public void onActivityPause() {
        updateConversationDraft();
    }

    private void updateConversationDraft() {
        Editable editable = editText.getText();
        String draft = Draft.toDraftJson(editable, messageEmojiCount, quoteInfo);
        if (conversation != null) {
            messageViewModel.saveDraft(conversation, draft);
        }
    }

    private void showAudioButton() {
        mViewIntercept.setVisibility(View.VISIBLE);
        audioButton.setVisibility(View.VISIBLE);
        mIvVoiceCancel.setVisibility(View.VISIBLE);
        mIvVoiceSend.setVisibility(View.VISIBLE);
        audioImageView.setVisibility(View.GONE);
        if (mViewAnimateVoice != null) {
            mViewAnimateVoice.playAnimation();
        }
        if (isPttMode) {
            pttPanel.attach(rootLinearLayout, audioButton, conversation);
            pttImageView.setImageResource(R.mipmap.ic_chat_broad_keyboard);
        } else {
            audioRecorderPanel.attach(rootLinearLayout, audioButton);
            audioRecorderPanel.startRecord();
            audioImageView.setImageResource(R.mipmap.ic_chat_broad_keyboard);
        }
        editText.setVisibility(View.GONE);
        extImageView.setVisibility(GONE);
        sendButton.setVisibility(View.GONE);
        rootLinearLayout.hideCurrentInput(editText);
        rootLinearLayout.hideAttachedInput(true);
    }


    private void hideAudioButton() {
        mViewIntercept.setVisibility(View.GONE);
        audioButton.setVisibility(View.GONE);
        mIvVoiceCancel.setVisibility(View.GONE);
        mIvVoiceSend.setVisibility(View.GONE);
        audioImageView.setVisibility(View.VISIBLE);
        if (isPttMode) {
            pttPanel.deattch();
            pttImageView.setImageResource(R.mipmap.ic_broad_ptt);
            isPttMode = false;
        } else {
            audioRecorderPanel.deattch();
            audioImageView.setImageResource(R.mipmap.ic_msg_broad_voice);
        }
        editText.setVisibility(View.VISIBLE);
        if (TextUtils.isEmpty(editText.getText())) {
            extImageView.setVisibility(VISIBLE);
            sendButton.setVisibility(View.GONE);
        } else {
            extImageView.setVisibility(GONE);
            sendButton.setVisibility(View.VISIBLE);
        }
    }

    private void showEmotionLayout() {
        audioButton.setVisibility(View.GONE);
        emotionImageView.setImageResource(R.mipmap.ic_chat_broad_keyboard);
        rootLinearLayout.show(editText, emotionContainerFrameLayout,0);
        if (onConversationInputPanelStateChangeListener != null) {
            onConversationInputPanelStateChangeListener.onInputPanelExpanded();
        }
    }

    private void hideEmotionLayout() {
        emotionImageView.setImageResource(R.mipmap.ic_msg_broad_emo);
        if (onConversationInputPanelStateChangeListener != null) {
            onConversationInputPanelStateChangeListener.onInputPanelCollapsed();
        }
    }

    private void showConversationExtension() {
        rootLinearLayout.show(editText, extContainerFrameLayout,1);
        extImageView.setImageResource(R.mipmap.ic_msg_broad_add_off);
        if (audioButton.isShown()) {
            hideAudioButton();
        }
        if (onConversationInputPanelStateChangeListener != null) {
            onConversationInputPanelStateChangeListener.onInputPanelExpanded();
        }
    }

    private void hideConversationExtension() {
        extImageView.setImageResource(R.mipmap.ic_msg_broad_add);
        if (onConversationInputPanelStateChangeListener != null) {
            onConversationInputPanelStateChangeListener.onInputPanelCollapsed();
        }
    }

    void closeConversationInputPanel() {
        extension.reset();
        emotionImageView.setImageResource(R.mipmap.ic_msg_broad_emo);
        extImageView.setImageResource(R.mipmap.ic_msg_broad_add);
        rootLinearLayout.hideAttachedInput(true);
        rootLinearLayout.hideCurrentInput(editText);
    }

    private void notifyTyping(int type) {
        if (conversation.type == Conversation.ConversationType.Single || conversation.type == Conversation.ConversationType.Group) {
            long now = System.currentTimeMillis();
            if (now - lastTypingTime > TYPING_INTERVAL_IN_SECOND * 1000) {
                lastTypingTime = now;
                TypingMessageContent content = new TypingMessageContent(type);
                messageViewModel.sendMessage(conversation, content, false);
            }
        }
    }

    @Override
    public void onEmojiSelected(String key) {
        Editable editable = editText.getText();
        if (key.equals("/DEL")) {
            messageEmojiCount--;
            messageEmojiCount = messageEmojiCount < 0 ? 0 : messageEmojiCount;
            editText.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
        } else {
            if (messageEmojiCount >= MAX_EMOJI_PER_MESSAGE) {
                Toast.makeText(activity, "最多允许输入" + MAX_EMOJI_PER_MESSAGE + "个表情符号", Toast.LENGTH_SHORT).show();
                return;
            }
            messageEmojiCount++;
            int code = Integer.decode(key);
            char[] chars = Character.toChars(code);
            String value = Character.toString(chars[0]);
            for (int i = 1; i < chars.length; i++) {
                value += Character.toString(chars[i]);
            }

            int start = editText.getSelectionStart();
            int end = editText.getSelectionEnd();
            start = (start < 0 ? 0 : start);
            end = (start < 0 ? 0 : end);
            editable.replace(start, end, value);

            int editEnd = editText.getSelectionEnd();
            MoonBroadUtils.replaceEmoticons(LQREmotionBroadKit.getContext(), editable, 0, editable.toString().length());
            editText.setSelection(editEnd);
        }
    }

    @Override
    public void onStickerSelected(String categoryName, String stickerName, String stickerBitmapPath) {
        String key;
        if (conversation.type == Conversation.ConversationType.SecretChat) {
            key = conversation.target + "_" + stickerBitmapPath;
        } else {
            key = stickerBitmapPath;
        }
        String remoteUrl = sharedPreferences.getString(key, null);
        messageViewModel.sendStickerMsg(conversation, stickerBitmapPath, remoteUrl);
    }


    public interface OnConversationInputPanelStateChangeListener {
        /**
         * 输入面板展开
         */
        void onInputPanelExpanded();

        /**
         * 输入面板关闭
         */
        void onInputPanelCollapsed();
    }
}
