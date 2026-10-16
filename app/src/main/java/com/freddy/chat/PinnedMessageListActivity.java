package com.freddy.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cboda.baselibrary.toast.ToastBroadUtils;
import com.cboda.baselibrary.utils.OnSingleClickBroadListener;
import com.cboda.intelligent.R;
import com.cboda.intelligent.base.BaseSimple2BroadActivity;
import com.freddy.chat.event.CEventCenter;
import com.freddy.chat.event.Events;
import com.freddy.chat.event.I_CEventListener;
import com.freddy.chat.im.MessageProcessor;
import com.freddy.chat.menu.MessageContextMenuHelper;
import com.freddy.chat.menu.MessageListActions;
import com.freddy.chat.menu.MessageListMode;
import com.freddy.im.bean.AppMessage;
import com.freddy.im.db.ImLocalStore;
import com.freddy.im.db.mapper.PinnedMessageRealmMapper;
import com.freddy.im.group.GroupRole;
import com.freddy.im.http.ImGroupHttpExample;
import com.freddy.im.http.model.im.GroupMemberItem;
import com.freddy.im.http.model.im.PinnedMessageItem;
import com.freddy.im.message.GroupPinnedSyncHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 群置顶消息列表：复用聊天气泡 UI，长按仅「取消置顶」。
 */
public final class PinnedMessageListActivity extends BaseSimple2BroadActivity
        implements MessageListActions, I_CEventListener {

    private static final String EXTRA_GROUP_ID = "extra_group_id";
    private static final String EXTRA_GROUP_ROLE = "extra_group_role";

    private String groupId;
    private int groupRole = GroupRole.MEMBER;
    private MessageListAdapter adapter;
    private TextView tvEmpty;

    @NonNull
    private static final String[] TOPICS = {Events.GROUP_PINNED_UPDATED};

    public static void start(@NonNull Context context, @NonNull String groupId, int groupRole) {
        Intent intent = new Intent(context, PinnedMessageListActivity.class);
        intent.putExtra(EXTRA_GROUP_ID, groupId);
        intent.putExtra(EXTRA_GROUP_ROLE, groupRole);
        context.startActivity(intent);
    }

    @Override
    public boolean showOrHideTitleLayout() {
        return false;
    }

    @Override
    public boolean showTransparentTopTitleLayout() {
        return true;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_pinned_message_list;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRootBgColor(com.cboda.chatui.R.color.color_F2F3F5);

        groupId = getIntent().getStringExtra(EXTRA_GROUP_ID);
        groupRole = getIntent().getIntExtra(EXTRA_GROUP_ROLE, GroupRole.MEMBER);
        if (TextUtils.isEmpty(groupId)) {
            finish();
            return;
        }

        View backView = findViewById(com.cboda.chatui.R.id.page_title_left_icon);
        if (backView != null) {
            backView.setOnClickListener(new OnSingleClickBroadListener() {
                @Override
                public void onSingleClick(View v) {
                    finish();
                }
            });
        }
        TextView titleView = findViewById(com.cboda.chatui.R.id.page_title);
        if (titleView != null) {
            titleView.setText(com.cboda.baselibrary.R.string.pinned_message_list_title);
        }
        tvEmpty = findViewById(R.id.tv_empty);
        if (tvEmpty != null) {
            tvEmpty.setText(com.cboda.baselibrary.R.string.pinned_message_empty);
        }

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MessageListAdapter(true, null, null, groupId, null, null, null);
        adapter.setMessageActions(this);
        recyclerView.setAdapter(adapter);
        ImLocalStore.loadGroupMembersMap(groupId, map -> {
            if (isFinishing() || adapter == null) {
                return;
            }
            Map<String, GroupMemberItem> members = map != null ? new HashMap<>(map) : new HashMap<>();
            adapter.setGroupMembers(members);
        });

        CEventCenter.registerEventListener(this, TOPICS);
        loadFromLocal();
        GroupPinnedSyncHelper.syncFromServer(groupId, (id, list) -> {
            if (isFinishing() || !groupId.equals(id)) {
                return;
            }
            bindList(list);
        });
    }

    @Override
    protected void onDestroy() {
        MessageContextMenuHelper.dismissShowing();
        CEventCenter.unregisterEventListener(this, TOPICS);
        super.onDestroy();
    }

    private void loadFromLocal() {
        ImLocalStore.loadGroupPinnedMessages(groupId, this::bindList);
    }

    private void bindList(@Nullable List<PinnedMessageItem> items) {
        if (isFinishing()) {
            return;
        }
        List<AppMessage> messages = new ArrayList<>();
        if (items != null) {
            for (PinnedMessageItem item : items) {
                if (item != null) {
                    messages.add(PinnedMessageRealmMapper.toAppMessage(item));
                }
            }
        }
        adapter.setNewMessage(messages);
        boolean empty = messages.isEmpty();
        if (tvEmpty != null) {
            tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onCEvent(String topic, int msgCode, int resultCode, Object obj) {
        if (Events.GROUP_PINNED_UPDATED.equals(topic) && obj instanceof String
                && groupId.equals(obj)) {
            loadFromLocal();
        }
    }

    @Override
    public int getListMode() {
        return MessageListMode.NORMAL;
    }

    @Override
    public boolean isMessageChecked(@NonNull AppMessage message) {
        return false;
    }

    @Override
    public void onRecallMessage(@NonNull AppMessage message) {
    }

    @Override
    public void onEditMessage(@NonNull AppMessage message) {
    }

    @Override
    public void onCopyMessage(@NonNull AppMessage message) {
    }

    @Override
    public void onEnterMultiSelectMode(@NonNull AppMessage message) {
    }

    @Override
    public void onDeleteMessage(@NonNull AppMessage message) {
    }

    @Override
    public void onForwardMessages(@NonNull List<AppMessage> messages) {
    }

    @Override
    public void onCollectMessage(@NonNull AppMessage message) {
    }

    @Override
    public void onToggleMessageChecked(@NonNull AppMessage message, boolean checked) {
    }

    @Override
    public void onExitMultiSelectMode() {
    }

    @Override
    public boolean isPinnedMessageListMode() {
        return true;
    }

    @Override
    public boolean canManageGroupPins() {
        return GroupRole.isOwnerOrAdmin(groupRole);
    }

    @Override
    public boolean isMessagePinned(@NonNull AppMessage message) {
        return true;
    }

    @Override
    public void onPinMessage(@NonNull AppMessage message) {
    }

    @Override
    public void onUnpinMessage(@NonNull AppMessage message) {
        if (!canManageGroupPins()) {
            return;
        }
        String msgId = message.getMsgId();
        if (TextUtils.isEmpty(msgId)) {
            return;
        }
        ImGroupHttpExample.requestUnpinGroupMessage(groupId, msgId, success ->
                GroupPinnedSyncHelper.syncFromServer(groupId, (id, list) -> {
                    if (isFinishing() || !groupId.equals(id)) {
                        return;
                    }
                    bindList(list);
                    MessageProcessor.getInstance().notifyGroupPinnedUpdated(id);
                    ToastBroadUtils.show(com.cboda.baselibrary.R.string.pinned_message_unpin_success);
                }), null);
    }
}
