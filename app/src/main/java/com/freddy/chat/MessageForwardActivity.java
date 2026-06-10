package com.freddy.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.freddy.im.MessageType;
import com.freddy.im.db.ImLocalStore;
import com.freddy.im.http.ImMessageHttpExample;
import com.freddy.im.http.model.im.ConversationListContent;
import com.freddy.im.http.model.im.SessionItem;
import com.freddy.im.http.model.im.TranspondMessageParam;
import com.freddy.im.listener.OnHttpImListener;
import com.cboda.intelligent.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 选择会话转发消息（对齐野火 {@code ForwardBroadActivity}，对接 §4.6 transpond）。
 */
public class MessageForwardActivity extends AppCompatActivity {

    public static final String EXTRA_MSG_IDS = "msg_ids";
    public static final String EXTRA_CONTENT_TYPE = "content_type";

    private ConversationListAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;

    @NonNull
    public static Intent buildIntent(@NonNull Context context,
                                     @NonNull List<String> msgIds,
                                     @Nullable String contentType) {
        Intent intent = new Intent(context, MessageForwardActivity.class);
        intent.putStringArrayListExtra(EXTRA_MSG_IDS, new ArrayList<>(msgIds));
        if (!TextUtils.isEmpty(contentType)) {
            intent.putExtra(EXTRA_CONTENT_TYPE, contentType);
        }
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
        setContentView(R.layout.activity_message_forward);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getString(com.cboda.chatui.R.string.message_forward));
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        RecyclerView recyclerView = findViewById(R.id.rv_sessions);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        adapter = new ConversationListAdapter();
        adapter.setOnSessionClickListener(this::onTargetSessionPicked);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(this::loadSessions);
        swipeRefresh.setColorSchemeResources(R.color.wechat_green);

        ImLocalStore.loadSessions(cached -> runOnUiThread(() -> {
            if (!cached.isEmpty()) {
                adapter.setNewData(cached);
            }
        }));
        loadSessions();
    }

    private void loadSessions() {
        ImMessageHttpExample.requestGetConversationList(0, 0, new OnHttpImListener<ConversationListContent>() {
            @Override
            public void onHttpImSuccess(@NonNull ConversationListContent content) {
                runOnUiThread(() -> finishLoad(content.getSessionList(), false));
            }
        }, () -> runOnUiThread(() -> finishLoad(null, true)));
    }

    private void finishLoad(@Nullable List<SessionItem> list, boolean failed) {
        swipeRefresh.setRefreshing(false);
        if (list != null && !list.isEmpty()) {
            adapter.setNewData(list);
            ImLocalStore.saveSessions(list);
        } else if (failed && adapter.getItemCount() <= 0) {
            Toast.makeText(this, R.string.conversation_list_empty, Toast.LENGTH_SHORT).show();
        }
    }

    private void onTargetSessionPicked(@NonNull SessionItem session) {
        ArrayList<String> msgIds = getIntent().getStringArrayListExtra(EXTRA_MSG_IDS);
        if (msgIds == null || msgIds.isEmpty()) {
            finish();
            return;
        }
        String to = resolveTargetId(session);
        if (TextUtils.isEmpty(to)) {
            Toast.makeText(this, R.string.message_forward_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        TranspondMessageParam param = new TranspondMessageParam();
        param.setMsgIdlist(msgIds);
        param.setTo(to);
        param.setConversation(session.getConversation());
        String contentType = getIntent().getStringExtra(EXTRA_CONTENT_TYPE);
        if (TextUtils.isEmpty(contentType)) {
            contentType = String.valueOf(MessageType.MessageContentType.TEXT.getMsgContentType());
        }
        param.setContentType(contentType);

        ImMessageHttpExample.requestTranspondMessage(param, new OnHttpImListener<Boolean>() {
            @Override
            public void onHttpImSuccess(@NonNull Boolean result) {
                runOnUiThread(() -> {
                    Toast.makeText(MessageForwardActivity.this,
                            R.string.message_forward_success, Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                });
            }
        }, () -> runOnUiThread(() ->
                Toast.makeText(this, R.string.message_forward_failed, Toast.LENGTH_SHORT).show()));
    }

    @Nullable
    private static String resolveTargetId(@NonNull SessionItem session) {
        if (MessageType.isGroupChat(session.getConversation())) {
            return session.getSessionId();
        }
        if (!TextUtils.isEmpty(session.getOppositeUserId())) {
            return session.getOppositeUserId();
        }
        return session.getSessionId();
    }
}
