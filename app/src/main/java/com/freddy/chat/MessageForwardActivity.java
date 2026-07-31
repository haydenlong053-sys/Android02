package com.freddy.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.IntentCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cboda.chatui.conversation.forward.ForwardPromptBroadView;
import com.cboda.chatui.dialog.ShareBroadPop;
import com.freddy.chat.forward.ForwardConversationListAdapter;
import com.freddy.chat.forward.ForwardMode;
import com.freddy.chat.forward.MessageForwardHelper;
import com.freddy.im.bean.AppMessage;
import com.freddy.im.db.ImLocalStore;
import com.freddy.im.http.model.im.SessionItem;
import com.cboda.intelligent.R;

import java.util.ArrayList;
import java.util.List;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;

/**
 * 选择会话转发消息（对齐野火 {@code ForwardBroadActivity} + {@code PickOrCreateConversationBroadActivity}）。
 * <p>
 * 双通道传参：
 * <ul>
 *   <li>聊天内转发：{@link #EXTRA_MSG_IDS} + {@link #EXTRA_SOURCE_SESSION_ID}，从本地库加载</li>
 *   <li>内联转发（收藏/公告等）：{@link #EXTRA_FORWARD_MESSAGES}，直接携带 {@link AppMessage}</li>
 * </ul>
 */
public class MessageForwardActivity extends AppCompatActivity {

    public static final String EXTRA_MSG_IDS = "msg_ids";
    public static final String EXTRA_FORWARD_MODE = "forward_mode";
    public static final String EXTRA_SOURCE_SESSION_ID = "source_session_id";
    public static final String EXTRA_SOURCE_IS_GROUP = "source_is_group";
    public static final String EXTRA_SOURCE_PEER_NAME = "source_peer_name";
    /** 无本地 msgId 的场景：直接携带待转发的消息（Parcelable） */
    public static final String EXTRA_FORWARD_MESSAGES = "forward_messages";

    private ForwardConversationListAdapter adapter;
    private List<AppMessage> sourceMessages = new ArrayList<>();
    private ForwardMode forwardMode = ForwardMode.ONE_BY_ONE;
    private boolean sourceIsGroup;
    /** 内联转发（收藏/公告等），不走本地库与合并转发 */
    private boolean inlineForward;
    @Nullable
    private String sourcePeerName;

    @NonNull
    public static Intent buildIntent(@NonNull Context context,
                                     @NonNull List<String> msgIds,
                                     @NonNull ForwardMode mode,
                                     @Nullable String sourceSessionId,
                                     boolean sourceIsGroup,
                                     @Nullable String sourcePeerName) {
        Intent intent = new Intent(context, MessageForwardActivity.class);
        intent.putStringArrayListExtra(EXTRA_MSG_IDS, new ArrayList<>(msgIds));
        intent.putExtra(EXTRA_FORWARD_MODE, mode.name());
        if (!TextUtils.isEmpty(sourceSessionId)) {
            intent.putExtra(EXTRA_SOURCE_SESSION_ID, sourceSessionId);
        }
        intent.putExtra(EXTRA_SOURCE_IS_GROUP, sourceIsGroup);
        if (!TextUtils.isEmpty(sourcePeerName)) {
            intent.putExtra(EXTRA_SOURCE_PEER_NAME, sourcePeerName);
        }
        return intent;
    }

    /** 收藏/公告等场景：无本地 msgId，直接携带 {@link AppMessage} 转发。 */
    @NonNull
    public static Intent buildInlineForwardIntent(@NonNull Context context,
                                                  @NonNull AppMessage message) {
        Intent intent = new Intent(context, MessageForwardActivity.class);
        ArrayList<Parcelable> messages = new ArrayList<>(1);
        messages.add(message);
        intent.putParcelableArrayListExtra(EXTRA_FORWARD_MESSAGES, messages);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!resolveForwardSource(getIntent())) {
            finish();
            return;
        }
        forwardMode = parseForwardMode(getIntent().getStringExtra(EXTRA_FORWARD_MODE));
        sourceIsGroup = getIntent().getBooleanExtra(EXTRA_SOURCE_IS_GROUP, false);
        sourcePeerName = getIntent().getStringExtra(EXTRA_SOURCE_PEER_NAME);

        setContentView(R.layout.activity_message_forward_pick_conversation);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getString(com.cboda.chatui.R.string.msg_share_to));
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        FrameLayout container = findViewById(R.id.containerFrameLayout);
        RecyclerView recyclerView = new RecyclerView(this);
        recyclerView.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        if (container != null) {
            container.addView(recyclerView);
        }

        adapter = new ForwardConversationListAdapter();
        adapter.setOnSessionClickListener(this::onTargetSessionPicked);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        EditText searchEditText = findViewById(R.id.searchEditText);
        if (searchEditText != null) {
            searchEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    adapter.filterByKeyword(s != null ? s.toString() : "");
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }

        ImLocalStore.loadSessions(cached -> runOnUiThread(() -> {
            if (!cached.isEmpty()) {
                adapter.setNewData(cached);
            }
        }));
    }

    /**
     * @return 是否具备有效转发源
     */
    private boolean resolveForwardSource(@NonNull Intent intent) {
        ArrayList<AppMessage> inlineMessages = readParcelableMessages(intent);
        if (inlineMessages != null && !inlineMessages.isEmpty()) {
            inlineForward = true;
            forwardMode = ForwardMode.ONE_BY_ONE;
            sourceMessages = new ArrayList<>(inlineMessages);
            return true;
        }
        ArrayList<String> msgIds = intent.getStringArrayListExtra(EXTRA_MSG_IDS);
        if (msgIds == null || msgIds.isEmpty()) {
            return false;
        }
        String sourceSessionId = intent.getStringExtra(EXTRA_SOURCE_SESSION_ID);
        loadSourceMessages(sourceSessionId, msgIds);
        return true;
    }

    @Nullable
    private static ArrayList<AppMessage> readParcelableMessages(@NonNull Intent intent) {
        return IntentCompat.getParcelableArrayListExtra(intent, EXTRA_FORWARD_MESSAGES, AppMessage.class);
    }

    private void loadSourceMessages(@Nullable String sourceSessionId, @NonNull List<String> msgIds) {
        if (TextUtils.isEmpty(sourceSessionId)) {
            return;
        }
        ImLocalStore.loadMessages(sourceSessionId, all -> runOnUiThread(() ->
                sourceMessages = MessageForwardHelper.filterMessagesByIds(all, msgIds)));
    }

    private void onTargetSessionPicked(@NonNull SessionItem session) {
        if (!hasForwardSource()) {
            finish();
            return;
        }
        if (TextUtils.isEmpty(MessageForwardHelper.resolveTargetId(session))) {
            Toast.makeText(this, com.cboda.baselibrary.R.string.message_forward_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        showForwardConfirmDialog(session);
    }

    private boolean hasForwardSource() {
        if (!sourceMessages.isEmpty()) {
            return true;
        }
        if (inlineForward) {
            return false;
        }
        ArrayList<String> msgIds = getIntent().getStringArrayListExtra(EXTRA_MSG_IDS);
        return msgIds != null && !msgIds.isEmpty();
    }

    private void showForwardConfirmDialog(@NonNull SessionItem target) {
        ForwardPromptBroadView promptView = new ForwardPromptBroadView(this);
        List<AppMessage> previewMessages = sourceMessages.isEmpty()
                ? buildPlaceholderMessages(getIntent().getStringArrayListExtra(EXTRA_MSG_IDS))
                : sourceMessages;
        ForwardMode effectiveMode = inlineForward ? ForwardMode.ONE_BY_ONE : forwardMode;
        MessageForwardHelper.bindForwardPrompt(promptView, target, previewMessages, effectiveMode,
                sourceIsGroup, sourcePeerName);

        ShareBroadPop sharePop = new ShareBroadPop(this, promptView);
        sharePop.setSureInvoke(new Function0<Unit>() {
            @Override
            public Unit invoke() {
                performForward(target, promptView.getEditText(), previewMessages, effectiveMode);
                return Unit.INSTANCE;
            }
        });
        sharePop.showPopupWindow();
    }

    @NonNull
    private List<AppMessage> buildPlaceholderMessages(@Nullable ArrayList<String> msgIds) {
        List<AppMessage> placeholders = new ArrayList<>();
        if (msgIds == null) {
            return placeholders;
        }
        for (String msgId : msgIds) {
            AppMessage message = new AppMessage();
            message.setMsgId(msgId);
            message.setMsgContentType(com.freddy.im.MessageType.MessageContentType.TEXT.getMsgContentType());
            placeholders.add(message);
        }
        return placeholders;
    }

    private void performForward(@NonNull SessionItem target,
                                @Nullable String extraText,
                                @NonNull List<AppMessage> messages,
                                @NonNull ForwardMode mode) {
        MessageForwardHelper.executeForward(mode, messages, target, extraText,
                () -> {
                    runOnUiThread(() -> {
                        Toast.makeText(MessageForwardActivity.this,
                                com.cboda.baselibrary.R.string.message_forward_success, Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    });
                },
                () -> {
                    runOnUiThread(() ->
                            Toast.makeText(MessageForwardActivity.this,
                                    com.cboda.baselibrary.R.string.message_forward_failed, Toast.LENGTH_SHORT).show());
                });
    }

    @NonNull
    private static ForwardMode parseForwardMode(@Nullable String raw) {
        if (ForwardMode.MERGE.name().equals(raw)) {
            return ForwardMode.MERGE;
        }
        return ForwardMode.ONE_BY_ONE;
    }
}
