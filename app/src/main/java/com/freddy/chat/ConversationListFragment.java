package com.freddy.chat;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.cboda.intelligent.utils.JumpBroadUtils;
import com.freddy.chat.event.CEventCenter;
import com.freddy.chat.im.IMSClientBootstrap;
import com.freddy.chat.event.Events;
import com.freddy.chat.event.I_CEventListener;
import com.freddy.chat.group.GroupListActivity;
import com.freddy.chat.utils.WeChatUiHelper;
import com.freddy.im.db.ImLocalStore;
import com.freddy.im.http.ImConversationHttpExample;
import com.freddy.im.http.ImMessageHttpExample;
import com.freddy.im.http.model.im.ConversationListContent;
import com.freddy.im.http.model.im.SessionItem;
import com.freddy.im.listener.OnHttpImListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.cboda.intelligent.R;

/**
 * 会话列表 Fragment，可嵌入宿主 Activity 或通过 {@link ConversationListActivity} 展示。
 */
public class ConversationListFragment extends Fragment implements I_CEventListener {

    private static final String[] EVENTS = {
            Events.NOTIFY_CONVERSATION_LIST,
            Events.SESSION_LIST_UPDATED,
            Events.SESSION_REMOVED
    };

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshConversations;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private ConversationListAdapter adapter;

    @NonNull
    public static ConversationListFragment newInstance() {
        return new ConversationListFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_conversation_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.rv_conversations);
        swipeRefreshConversations = view.findViewById(R.id.swipe_refresh_conversations);
        progressBar = view.findViewById(R.id.progress);
        tvEmpty = view.findViewById(R.id.tv_empty);
        Button btnCreateGroup = view.findViewById(R.id.btn_create_group);

        adapter = new ConversationListAdapter();
        adapter.setCurrentUserId(IMSClientBootstrap.getInstance().getCurrentUserId());
        adapter.setOnSessionClickListener(sessionItem -> {
            sessionItem.setUnreadCount(0);
            adapter.mergeSessions(Collections.singletonList(sessionItem));
            JumpBroadUtils.Companion.startChatMsgActivity(sessionItem, requireContext());


        });
        adapter.setOnSessionLongClickListener(sessionItem -> {
            showSessionActions(sessionItem);
            return true;
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        btnCreateGroup.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), CreateGroupActivity.class)));
        view.findViewById(R.id.btn_my_groups).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), GroupListActivity.class)));

        // 禁用下拉刷新；列表由 onViewCreated / 信令事件触发 loadConversationList
        swipeRefreshConversations.setEnabled(false);

        showCachedConversations();
        //同时获取服务器最近会话，防止本地卸载找不到会话的情况
        loadConversationList();
        CEventCenter.registerEventListener(this, EVENTS);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroyView() {
        CEventCenter.unregisterEventListener(this, EVENTS);
        super.onDestroyView();
    }

    private void showCachedConversations() {
        ImLocalStore.loadSessions(cached -> {
            if (!isAdded() || cached.isEmpty()) {
                return;
            }
            requireActivity().runOnUiThread(() -> {
                adapter.setNewData(cached);
                tvEmpty.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            });
        });
    }

    private void loadConversationList() {
        if (!isAdded()) {
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);

        ImMessageHttpExample.requestGetConversationList(0, 0, new OnHttpImListener<ConversationListContent>() {
            @Override
            public void onHttpImSuccess(@NonNull ConversationListContent content) {
                runOnUiThreadSafe(() -> finishLoadConversationList(content.getSessionList(), false));
            }
        }, () -> runOnUiThreadSafe(() -> finishLoadConversationList(null, true)));
    }

    private void finishLoadConversationList(@Nullable List<SessionItem> list, boolean failed) {
        if (!isAdded()) {
            return;
        }
        progressBar.setVisibility(View.GONE);
        if (failed) {
            if (adapter.getItemCount() <= 0) {
                tvEmpty.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
            return;
        }
        boolean empty = list == null || list.isEmpty();
        tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
        if (!empty) {
            adapter.mergeSessions(list);
        }
    }

    private void showSessionActions(@NonNull SessionItem sessionItem) {
        String sessionId = sessionItem.getSessionId();
        if (TextUtils.isEmpty(sessionId)) {
            return;
        }
        boolean pinned = sessionItem.getTopNum() > 0;
        String pinLabel = getString(pinned ? R.string.cancel_zhiding : R.string.zhiding);
        CharSequence[] items = new CharSequence[]{pinLabel, getString(R.string.action_remove)};
        new AlertDialog.Builder(requireContext())
                .setItems(items, (d, which) -> {
                    if (which == 0) {
                        setSessionTop(sessionItem, !pinned);
                    } else if (which == 1) {
                        confirmRemoveConversation(sessionItem);
                    }
                })
                .show();
    }

    private void setSessionTop(@NonNull SessionItem sessionItem, boolean top) {
        String sessionId = sessionItem.getSessionId();
        if (TextUtils.isEmpty(sessionId)) {
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        ImConversationHttpExample.requestSessionTop(sessionId, top, result -> runOnUiThreadSafe(() -> {
            ImLocalStore.updateSessionTopMute(sessionId, top, sessionItem.isMuteNotify());
            progressBar.setVisibility(View.GONE);
            Toast.makeText(requireContext(),
                    top ? R.string.session_top_success : R.string.session_untop_success,
                    Toast.LENGTH_SHORT).show();
        }), () -> runOnUiThreadSafe(() -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(requireContext(), R.string.session_top_failed, Toast.LENGTH_SHORT).show();
        }));
    }

    private void confirmRemoveConversation(@NonNull SessionItem sessionItem) {
        String sessionId = sessionItem.getSessionId();
        if (TextUtils.isEmpty(sessionId)) {
            return;
        }
        String currentUserId = IMSClientBootstrap.getInstance().getCurrentUserId();
        String displayName = WeChatUiHelper.resolveSessionListDisplayName(sessionItem, currentUserId);
        if (TextUtils.isEmpty(displayName)) {
            displayName = getString(R.string.conversation_name_fallback);
        }
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.remove_conversation_title)
                .setMessage(getString(R.string.remove_conversation_message, displayName))
                .setPositiveButton(R.string.action_remove, (d, which) -> removeConversation(sessionId))
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }


    private void removeConversation(@NonNull String sessionId) {
        progressBar.setVisibility(View.VISIBLE);
        ImMessageHttpExample.requestRemoveConversation(sessionId, content -> runOnUiThreadSafe(() -> {
            progressBar.setVisibility(View.GONE);
            goLocalDbRemoveConversation(sessionId);

        }), () -> runOnUiThreadSafe(() -> {
            progressBar.setVisibility(View.GONE);
            goLocalDbRemoveConversation(sessionId);
//            Toast.makeText(requireContext(), R.string.remove_conversation_failed, Toast.LENGTH_SHORT).show();
        }));
    }

    private void goLocalDbRemoveConversation(String sessionId) {
        ImLocalStore.removeSessionCompletely(sessionId, () -> runOnUiThreadSafe(() -> {
            Toast.makeText(requireContext(), R.string.remove_conversation_success, Toast.LENGTH_SHORT).show();
            applySessionRemoved(sessionId);
        }));
    }

    @Override
    public void onCEvent(String topic, int msgCode, int resultCode, Object obj) {
        if (Events.NOTIFY_CONVERSATION_LIST.equals(topic)) {
            runOnUiThreadSafe(this::loadConversationList);
            return;
        }
        if (Events.SESSION_LIST_UPDATED.equals(topic) && obj instanceof List) {
            @SuppressWarnings("unchecked")
            List<SessionItem> updated = (List<SessionItem>) obj;
            runOnUiThreadSafe(() -> applySessionListUpdated(updated));
            return;
        }
        if (Events.SESSION_REMOVED.equals(topic) && obj instanceof String) {
            runOnUiThreadSafe(() -> applySessionRemoved((String) obj));
        }
    }

    private void applySessionRemoved(@NonNull String sessionId) {
        if (!isAdded()) {
            return;
        }
        adapter.removeBySessionId(sessionId);
        boolean empty = adapter.getItemCount() <= 0;
        tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void applySessionListUpdated(@NonNull List<SessionItem> updated) {
        if (!isAdded() || updated.isEmpty()) {
            return;
        }
        adapter.mergeSessions(new ArrayList<>(updated));
        tvEmpty.setVisibility(adapter.getItemCount() > 0 ? View.GONE : View.VISIBLE);
        recyclerView.setVisibility(adapter.getItemCount() > 0 ? View.VISIBLE : View.GONE);
        if (adapter.getItemCount() > 0) {
            recyclerView.scrollToPosition(0);
        }
    }

    private void runOnUiThreadSafe(@NonNull Runnable action) {
        if (!isAdded()) {
            return;
        }
        requireActivity().runOnUiThread(action);
    }
}
