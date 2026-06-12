package com.freddy.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cboda.chatui.conversation.forward.ForwardPromptBroadView;
import com.cboda.chatui.dialog.ShareBroadPop;
import com.freddy.chat.forward.ForwardMode;
import com.freddy.chat.forward.MessageForwardHelper;
import com.freddy.im.bean.AppMessage;
import com.freddy.im.db.ImLocalStore;
import com.freddy.im.http.ImMessageHttpExample;
import com.freddy.im.http.model.im.ConversationListContent;
import com.freddy.im.http.model.im.SessionItem;
import com.freddy.im.listener.OnHttpImListener;
import com.cboda.intelligent.R;

import java.util.ArrayList;
import java.util.List;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;

/**
 * 选择会话转发消息（对齐野火 {@code ForwardBroadActivity} + {@code PickOrCreateConversationBroadActivity}）。
 */
public class MessageForwardActivity extends AppCompatActivity {

    public static final String EXTRA_MSG_IDS = "msg_ids";
    public static final String EXTRA_FORWARD_MODE = "forward_mode";
    public static final String EXTRA_SOURCE_SESSION_ID = "source_session_id";
    public static final String EXTRA_SOURCE_IS_GROUP = "source_is_group";
    public static final String EXTRA_SOURCE_PEER_NAME = "source_peer_name";
    public static final String EXTRA_INLINE_MESSAGE_CONTENT = "inline_message_content";
    public static final String EXTRA_INLINE_MESSAGE_CONTENT_TYPE = "inline_message_content_type";
    public static final String INLINE_COLLECT_MSG_ID = "inline_collect";

    private ConversationListAdapter adapter;
    private List<AppMessage> sourceMessages = new ArrayList<>();
    private ForwardMode forwardMode = ForwardMode.ONE_BY_ONE;
    private boolean sourceIsGroup;
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

    /** 收藏夹等场景：无本地 msgId，直接携带内容与类型转发。 */
    @NonNull
    public static Intent buildInlineForwardIntent(@NonNull Context context,
                                                  @NonNull AppMessage message) {
        Intent intent = new Intent(context, MessageForwardActivity.class);
        intent.putExtra(EXTRA_INLINE_MESSAGE_CONTENT,
                message.getContent() != null ? message.getContent() : "");
        intent.putExtra(EXTRA_INLINE_MESSAGE_CONTENT_TYPE, message.getMsgContentType());
        ArrayList<String> msgIds = new ArrayList<>();
        msgIds.add(INLINE_COLLECT_MSG_ID);
        intent.putStringArrayListExtra(EXTRA_MSG_IDS, msgIds);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ArrayList<String> msgIds = getIntent().getStringArrayListExtra(EXTRA_MSG_IDS);
        if (msgIds == null || msgIds.isEmpty()) {
            finish();
            return;
        }
        forwardMode = parseForwardMode(getIntent().getStringExtra(EXTRA_FORWARD_MODE));
        sourceIsGroup = getIntent().getBooleanExtra(EXTRA_SOURCE_IS_GROUP, false);
        sourcePeerName = getIntent().getStringExtra(EXTRA_SOURCE_PEER_NAME);
        String sourceSessionId = getIntent().getStringExtra(EXTRA_SOURCE_SESSION_ID);

        setContentView(com.cboda.chatui.R.layout.pick_or_create_conversation_broad_activity);

        Toolbar toolbar = findViewById(com.cboda.chatui.R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getString(com.cboda.chatui.R.string.message_forward));
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        FrameLayout container = findViewById(com.cboda.chatui.R.id.containerFrameLayout);
        RecyclerView recyclerView = new RecyclerView(this);
        recyclerView.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        if (container != null) {
            container.addView(recyclerView);
        }

        adapter = new ConversationListAdapter();
        adapter.setOnSessionClickListener(this::onTargetSessionPicked);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        EditText searchEditText = findViewById(com.cboda.chatui.R.id.searchEditText);
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

        loadSourceMessages(sourceSessionId, msgIds);
        loadInlineSourceMessageIfNeeded();
        ImLocalStore.loadSessions(cached -> runOnUiThread(() -> {
            if (!cached.isEmpty()) {
                adapter.setNewData(cached);
            }
        }));
//        loadSessions();
    }

    private void loadSourceMessages(@Nullable String sourceSessionId, @NonNull List<String> msgIds) {
        if (TextUtils.isEmpty(sourceSessionId)) {
            return;
        }
        ImLocalStore.loadMessages(sourceSessionId, all -> runOnUiThread(() ->
                sourceMessages = MessageForwardHelper.filterMessagesByIds(all, msgIds)));
    }

    private void loadInlineSourceMessageIfNeeded() {
        String inlineContent = getIntent().getStringExtra(EXTRA_INLINE_MESSAGE_CONTENT);
        if (TextUtils.isEmpty(inlineContent)) {
            return;
        }
        AppMessage message = new AppMessage();
        message.setMsgId(INLINE_COLLECT_MSG_ID);
        message.setContent(inlineContent);
        message.setMsgContentType(getIntent().getIntExtra(
                EXTRA_INLINE_MESSAGE_CONTENT_TYPE,
                com.freddy.im.MessageType.MessageContentType.TEXT.getMsgContentType()));
        sourceMessages = new ArrayList<>();
        sourceMessages.add(message);
    }

//    private void loadSessions() {
//        ImMessageHttpExample.requestGetConversationList(0, 0, new OnHttpImListener<ConversationListContent>() {
//            @Override
//            public void onHttpImSuccess(@NonNull ConversationListContent content) {
//                runOnUiThread(() -> finishLoad(content.getSessionList(), false));
//            }
//        }, () -> runOnUiThread(() -> finishLoad(null, true)));
//    }

//    private void finishLoad(@Nullable List<SessionItem> list, boolean failed) {
//        if (list != null && !list.isEmpty()) {
//            adapter.setNewData(list);
//            ImLocalStore.saveSessions(list);
//        } else if (failed && adapter.getItemCount() <= 0) {
//            Toast.makeText(this, R.string.conversation_list_empty, Toast.LENGTH_SHORT).show();
//        }
//    }

    private void onTargetSessionPicked(@NonNull SessionItem session) {
        ArrayList<String> msgIds = getIntent().getStringArrayListExtra(EXTRA_MSG_IDS);
        if (msgIds == null || msgIds.isEmpty()) {
            finish();
            return;
        }
        if (TextUtils.isEmpty(MessageForwardHelper.resolveTargetId(session))) {
            Toast.makeText(this, R.string.message_forward_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        showForwardConfirmDialog(session);
    }

    private void showForwardConfirmDialog(@NonNull SessionItem target) {
        ForwardPromptBroadView promptView = new ForwardPromptBroadView(this);
        List<AppMessage> previewMessages = sourceMessages.isEmpty()
                ? buildPlaceholderMessages(getIntent().getStringArrayListExtra(EXTRA_MSG_IDS))
                : sourceMessages;
        MessageForwardHelper.bindForwardPrompt(promptView, target, previewMessages, forwardMode,
                sourceIsGroup, sourcePeerName);

        ShareBroadPop sharePop = new ShareBroadPop(this, promptView);
        sharePop.setSureInvoke(new Function0<Unit>() {
            @Override
            public Unit invoke() {
                performForward(target, promptView.getEditText(), previewMessages);
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
                                @NonNull List<AppMessage> messages) {
        MessageForwardHelper.executeForward(forwardMode, messages, target, extraText,
                () -> runOnUiThread(() -> {
                    Toast.makeText(MessageForwardActivity.this,
                            R.string.message_forward_success, Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                }),
                () -> runOnUiThread(() ->
                        Toast.makeText(MessageForwardActivity.this,
                                R.string.message_forward_failed, Toast.LENGTH_SHORT).show()));
    }

    @NonNull
    private static ForwardMode parseForwardMode(@Nullable String raw) {
        if (ForwardMode.MERGE.name().equals(raw)) {
            return ForwardMode.MERGE;
        }
        return ForwardMode.ONE_BY_ONE;
    }
}
