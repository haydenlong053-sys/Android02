package com.freddy.chat.merge;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cboda.chatui.R;
import com.freddy.chat.im.IMSClientBootstrap;
import com.freddy.im.bean.AppMessage;
import com.freddy.im.db.ImLocalStore;
import com.freddy.im.http.model.im.GroupMemberItem;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 合并转发详情页（自研版，对齐野火 {@link com.cboda.chatui.conversation.message.CompositeMessageContentBroadActivity}）。
 */
public final class MergeTranspondDetailActivity extends AppCompatActivity {

    public static final String EXTRA_MESSAGE = "message";
    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_SESSION_ID = "session_id";
    public static final String EXTRA_IS_GROUP = "is_group";
    public static final String EXTRA_PEER_DISPLAY_NAME = "peer_display_name";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.composite_message_broad_activity);

        AppMessage message = getIntent().getParcelableExtra(EXTRA_MESSAGE);
        if (message == null || TextUtils.isEmpty(message.getContent())) {
            finish();
            return;
        }

        String title = getIntent().getStringExtra(EXTRA_TITLE);
        if (TextUtils.isEmpty(title)) {
            title = MergeTranspondMessageContentUtil.buildTitle(
                    getIntent().getBooleanExtra(EXTRA_IS_GROUP, false),
                    getIntent().getStringExtra(EXTRA_PEER_DISPLAY_NAME));
        }

        View backView = findViewById(com.cboda.baselibrary.R.id.mViewBack);
        if (backView != null) {
            backView.setOnClickListener(v -> finish());
        }
        TextView titleView = findViewById(com.cboda.baselibrary.R.id.mTvTitle);
        if (titleView != null) {
            titleView.setText(title);
        }

        List<MergeTranspondMessageItem> items = MergeTranspondMessageContentUtil.parseMessageList(
                message.getContent());
        if (items.isEmpty()) {
            finish();
            return;
        }

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        boolean isGroup = getIntent().getBooleanExtra(EXTRA_IS_GROUP, false);
        String sessionId = getIntent().getStringExtra(EXTRA_SESSION_ID);
        String peerDisplayName = getIntent().getStringExtra(EXTRA_PEER_DISPLAY_NAME);
        String currentUserId = IMSClientBootstrap.getInstance().getCurrentUserId();

        if (isGroup && !TextUtils.isEmpty(sessionId)) {
            ImLocalStore.loadGroupMembersMap(sessionId, map ->
                    bindAdapter(recyclerView, items, map, currentUserId, peerDisplayName, isGroup));
        } else {
            bindAdapter(recyclerView, items, Collections.emptyMap(),
                    currentUserId, peerDisplayName, isGroup);
        }
    }

    private void bindAdapter(RecyclerView recyclerView,
                             List<MergeTranspondMessageItem> items,
                             Map<String, GroupMemberItem> members,
                             @Nullable String currentUserId,
                             @Nullable String peerDisplayName,
                             boolean isGroup) {
        Map<String, GroupMemberItem> memberMap = members != null
                ? new HashMap<>(members) : new HashMap<>();
        recyclerView.setAdapter(new MergeTranspondDetailAdapter(
                items, memberMap, currentUserId, peerDisplayName, isGroup));
    }
}
