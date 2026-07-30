package com.freddy.chat

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.cboda.chatui.widget.ProgressBroadFragment
import com.cboda.intelligent.R
import com.cboda.intelligent.utils.JumpBroadUtils
import com.cboda.intelligent.viewmodel.ConversationParentViewModel
import com.freddy.chat.im.IMSClientBootstrap
import com.freddy.chat.utils.SessionDisplayEnricher
import com.freddy.chat.utils.WeChatUiHelper
import com.freddy.im.db.ImLocalStore
import com.freddy.im.http.ImConversationHttpExample
import com.freddy.im.http.ImMessageHttpExample
import com.freddy.im.http.model.im.SessionItem

abstract class BaseConversationSessionFragment : ProgressBroadFragment() {

    protected abstract val sessionCategory: ConversationSessionCategory

    private val parentViewModel: ConversationParentViewModel by activityViewModels()
    private lateinit var adapter: ConversationListAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView

    override fun contentLayout(): Int = R.layout.conversation_session_list_fragment

    override fun afterViews(view: View) {
        recyclerView = view.findViewById(R.id.rv_conversations)
        swipeRefresh = view.findViewById(R.id.swipe_refresh_conversations)
        progressBar = view.findViewById(R.id.progress)
        tvEmpty = view.findViewById(R.id.tv_empty)

        adapter = ConversationListAdapter().apply {
            setCurrentUserId(IMSClientBootstrap.getInstance().currentUserId)
            setOnSessionClickListener { session ->
                // 先跳转，再静默清未读，避免进页前整表刷新闪烁
                JumpBroadUtils.startChatMsgActivity(session, requireContext())
                clearUnreadAfterEnterChat(session)
            }
            setOnSessionLongClickListener { session ->
                showSessionActions(session)
                true
            }
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        // 关闭默认换页动画，减轻局部刷新时的视觉跳动
        recyclerView.itemAnimator = null

        // 禁用下拉刷新；会话数据由 ViewModel 主动拉取 / 信令更新
        swipeRefresh.isEnabled = false
//        swipeRefresh.setOnRefreshListener { parentViewModel.loadConversations() }
//        swipeRefresh.setColorSchemeResources(R.color.wechat_green)


        parentViewModel.observeSessions(sessionCategory).observe(viewLifecycleOwner) { sessions ->
            if (!shouldEnrichSessionDisplay()) {
                renderSessions(sessions)
                return@observe
            }
            SessionDisplayEnricher.enrichSingleChatSessions(
                sessions,
                IMSClientBootstrap.getInstance().currentUserId,
            ) { enriched ->
                if (!isAdded) {
                    return@enrichSingleChatSessions
                }
                renderSessions(enriched)
                // 回写昵称/头像到 ViewModel，但不二次 post，避免再触发一轮观察导致闪烁
                val changed = collectDisplayInfoUpdates(sessions, enriched)
                if (changed.isNotEmpty()) {
                    parentViewModel.patchSessionDisplayFields(changed)
                }
            }
        }
        parentViewModel.loading.observe(viewLifecycleOwner) { loading ->
            if (loading == true && adapter.itemCount <= 0) {
                progressBar.visibility = View.VISIBLE
            } else if (loading != true) {
                progressBar.visibility = View.GONE
            }
        }
    }

    override fun updateUIMode(tag: String, selectModel: Boolean) {
        if (tag != sessionCategory.uiModeTag()) {
            return
        }
        // 自研 IM 会话列表暂不支持多选管理模式，后续可在此扩展。
    }

    protected open fun shouldEnrichSessionDisplay(): Boolean = false

    private fun collectDisplayInfoUpdates(
        original: List<SessionItem>?,
        enriched: List<SessionItem>,
    ): List<SessionItem> {
        if (original.isNullOrEmpty()) {
            return emptyList()
        }
        val originalById = original.associateBy { it.sessionId.orEmpty() }
        return enriched.mapNotNull { item ->
            val before = originalById[item.sessionId.orEmpty()] ?: return@mapNotNull null
            val nameChanged = !TextUtils.equals(before.sessionName, item.sessionName)
            val iconChanged = !TextUtils.equals(before.icon, item.icon)
            if (nameChanged || iconChanged) item else null
        }
    }

    private fun renderSessions(sessions: List<SessionItem>?) {
        val list = sessions ?: emptyList()
        adapter.setNewData(list)
        val empty = list.isEmpty()
        tvEmpty.visibility = if (empty) View.VISIBLE else View.GONE
        recyclerView.visibility = if (empty) View.GONE else View.VISIBLE
        if (!empty) {
            showContent()
        } else if (parentViewModel.loading.value != true) {
            showContent()
        }
    }

    /** 进入聊天后清未读：先改 Adapter 角标，再同步 ViewModel/本地库，避免整表闪烁。 */
    private fun clearUnreadAfterEnterChat(session: SessionItem) {
        val sessionId = session.sessionId ?: return
        if (session.unreadCount <= 0) {
            return
        }
        session.unreadCount = 0
        adapter.clearUnread(sessionId)
        parentViewModel.clearSessionUnread(sessionId)
        ImLocalStore.clearSessionUnread(sessionId)
        val lastMessageId = session.lastMessageId
        if (!TextUtils.isEmpty(lastMessageId)) {
            com.freddy.im.message.ImMessageManager.getInstance().reportMessageRead(lastMessageId)
        }
    }

    private fun showSessionActions(session: SessionItem) {
        val sessionId = session.sessionId
        if (TextUtils.isEmpty(sessionId)) {
            return
        }
        val unread = session.unreadCount
        val pinned = session.topNum > 0
        val pinLabel = if (pinned) {
            getString(R.string.cancel_zhiding)
        } else {
            getString(R.string.zhiding)
        }
        val readLabel = if (unread > 0) {
            getString(com.cboda.chatui.R.string.mark_as_read)
        } else {
            getString(com.cboda.chatui.R.string.mark_as_unread)
        }
        val items = arrayOf(pinLabel, readLabel, getString(R.string.action_remove))
        AlertDialog.Builder(requireContext())
            .setItems(items) { _, which ->
                when (which) {
                    0 -> setSessionTop(session, !pinned)
                    1 -> if (unread > 0) {
                        markSessionAsRead(session)
                    } else {
                        markSessionAsUnread(session)
                    }
                    2 -> confirmRemoveConversation(session)
                }
            }
            .show()
    }

    /** 对齐野火：长按「设为已读」清零未读角标。 */
    private fun markSessionAsRead(session: SessionItem) {
        val sessionId = session.sessionId ?: return
        session.unreadCount = 0
        parentViewModel.mergeSessions(listOf(session))
        ImLocalStore.clearSessionUnread(sessionId)
        val lastMessageId = session.lastMessageId
        if (!TextUtils.isEmpty(lastMessageId)) {
            com.freddy.im.message.ImMessageManager.getInstance().reportMessageRead(lastMessageId)
        }
    }

    /** 对齐野火：长按「标记未读」将未读数置为 1。 */
    private fun markSessionAsUnread(session: SessionItem) {
        val sessionId = session.sessionId ?: return
        session.unreadCount = 1
        parentViewModel.mergeSessions(listOf(session))
        ImLocalStore.markSessionUnread(sessionId, 1)
    }

    private fun setSessionTop(session: SessionItem, top: Boolean) {
        val sessionId = session.sessionId ?: return
        progressBar.visibility = View.VISIBLE
        ImConversationHttpExample.requestSessionTop(
            sessionId,
            top,
            {
                ImLocalStore.updateSessionTopMute(sessionId, top, session.isMuteNotify)
                if (isAdded) {
                    requireActivity().runOnUiThread {
                        progressBar.visibility = View.GONE
                        Toast.makeText(
                            requireContext(),
                            if (top) R.string.session_top_success else R.string.session_untop_success,
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }
            },
            {
                if (isAdded) {
                    requireActivity().runOnUiThread {
                        progressBar.visibility = View.GONE
                        Toast.makeText(requireContext(), R.string.session_top_failed, Toast.LENGTH_SHORT).show()
                    }
                }
            },
        )
    }

    private fun confirmRemoveConversation(session: SessionItem) {
        val sessionId = session.sessionId
        if (TextUtils.isEmpty(sessionId)) {
            return
        }
        val currentUserId = IMSClientBootstrap.getInstance().currentUserId
        var displayName = WeChatUiHelper.resolveSessionListDisplayName(session, currentUserId)
        if (TextUtils.isEmpty(displayName)) {
            displayName = getString(R.string.conversation_name_fallback)
        }
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.remove_conversation_title)
            .setMessage(getString(R.string.remove_conversation_message, displayName))
            .setPositiveButton(R.string.action_remove) { _, _ -> removeConversation(sessionId) }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }

    private fun removeConversation(sessionId: String) {
        progressBar.visibility = View.VISIBLE
        ImMessageHttpExample.requestRemoveConversation(
            sessionId,
            { goLocalDbRemoveConversation(sessionId) },
            { goLocalDbRemoveConversation(sessionId) },
        )
    }

    private fun goLocalDbRemoveConversation(sessionId: String) {
        ImLocalStore.removeSessionCompletely(sessionId) {
            if (!isAdded) {
                return@removeSessionCompletely
            }
            requireActivity().runOnUiThread {
                progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), R.string.remove_conversation_success, Toast.LENGTH_SHORT).show()
                parentViewModel.removeSession(sessionId)
            }
        }
    }
}
