package com.freddy.chat

import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isGone
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import cn.wildfirechat.viewmodel.HomeUIViewModel
import cn.wildfirechat.viewmodel.SocialAction
import com.cboda.baselibrary.utils.imsConnectLiveData
import com.cboda.chatui.widget.ProgressBroadFragment
import com.cboda.intelligent.R
import com.cboda.intelligent.utils.HomeFloatingBottomInset
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
    private val uiViewModel: HomeUIViewModel by activityViewModels()
    private lateinit var adapter: ConversationListAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: View
    private lateinit var errorContainer: View
    private var networkBanner: View? = null
    private var sessionsEmpty: Boolean = true
    private var isSelectMode: Boolean = false
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun contentLayout(): Int = R.layout.conversation_session_list_fragment

    override fun afterViews(view: View) {
        recyclerView = view.findViewById(R.id.rv_conversations)
        swipeRefresh = view.findViewById(R.id.swipe_refresh_conversations)
        progressBar = view.findViewById(R.id.progress)
        tvEmpty = view.findViewById(R.id.tv_empty)
        errorContainer = view.findViewById(R.id.conversation_list_error)
        networkBanner = view.findViewById(R.id.network_unavailable_banner)
        view.findViewById<View>(R.id.btn_retry_conversations).setOnClickListener {
            parentViewModel.loadConversations()
        }

        adapter = ConversationListAdapter().apply {
            setCurrentUserId(IMSClientBootstrap.getInstance().currentUserId)
            setOnSessionClickListener { session ->
                if (isSelectMode) {
                    val position = findPositionBySessionId(session.sessionId)
                    toggleSelect(session, position)
                    syncBottomBtnState()
                } else {
                    // 先跳转，再静默清未读，避免进页前整表刷新闪烁
                    JumpBroadUtils.startChatMsgActivity(session, requireContext())
                    clearUnreadAfterEnterChat(session)
                }
            }
            setOnSessionLongClickListener { session ->
                if (isSelectMode) {
                    return@setOnSessionLongClickListener true
                }
                showSessionActions(session)
                true
            }
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        // 关闭默认换页动画，减轻局部刷新时的视觉跳动
        recyclerView.itemAnimator = null
        // 悬浮底栏盖住列表：末项可滚出底栏上方
        HomeFloatingBottomInset.applyTo(recyclerView)

        // 禁用下拉刷新；会话数据由 ViewModel 主动拉取 / 信令更新
        swipeRefresh.isEnabled = false
//        swipeRefresh.setOnRefreshListener { parentViewModel.loadConversations() }
//        swipeRefresh.setColorSchemeResources(R.color.wechat_green)


        parentViewModel.observeSessions(sessionCategory).observe(viewLifecycleOwner) { sessions ->
            if (!shouldEnrichSessionDisplay() || !needsDisplayEnrich(sessions)) {
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
        parentViewModel.loading.observe(viewLifecycleOwner) {
            applyOverlayState()
        }
        parentViewModel.loadFailed.observe(viewLifecycleOwner) {
            applyOverlayState()
        }
        networkBanner?.alpha = 1f
        imsConnectLiveData.observe(this) { type ->
            networkBanner?.post {
                when (type) {
                    1 -> networkBanner?.isGone = false
                    2 -> networkBanner?.isGone = true
                }
            }
        }

        // 对齐野火：底部管理栏事件只由当前可见 Tab 消费
        uiViewModel.actionEventLiveData.observe(viewLifecycleOwner) { action ->
            if (action == null || !isResumed || adapter.itemCount == 0) {
                return@observe
            }
            when (action) {
                is SocialAction.SelectAll -> {
                    adapter.selectAll()
                    syncBottomBtnState()
                }
                is SocialAction.CancelSelectAll -> {
                    adapter.cancelSelectAll()
                    uiViewModel.updateBottomBtnState(false, true, false)
                }
                is SocialAction.MarkAllAsRead -> {
                    changeOnlyThisMode(false)
                    adapter.selectAll()
                    clearUnreadForSelected()
                }
                is SocialAction.MarkSingleAsRead -> {
                    changeOnlyThisMode(false)
                    clearUnreadForSelected()
                }
                is SocialAction.DeleteSelected -> {
                    changeOnlyThisMode(false)
                    deleteSelectedConversations()
                }
                is SocialAction.CloseSelect -> {
                    adapter.closeSelect()
                    uiViewModel.updateBottomBtnState(false, true, false)
                }
            }
        }
    }

    override fun updateUIMode(tag: String, selectModel: Boolean) {
        if (tag != sessionCategory.uiModeTag()) {
            return
        }
        changeOnlyThisMode(selectModel)
        if (selectModel) {
            uiViewModel.updateBottomBtnState(false, true, false)
        }
    }

    private fun changeOnlyThisMode(selectModel: Boolean) {
        isSelectMode = selectModel
        adapter.updateSelectMode(selectModel)
    }

    private fun syncBottomBtnState() {
        val total = adapter.itemCount
        val selected = adapter.selectedCount
        val isSelectAll = total > 0 && selected == total
        val isAllRead = isSelectAll || selected == 0
        val enableDelete = selected > 0
        uiViewModel.updateBottomBtnState(isSelectAll, isAllRead, enableDelete)
    }

    private fun clearUnreadForSelected() {
        val selected = adapter.selectInfos
        for (session in selected) {
            markSessionAsRead(session)
        }
        mainHandler.postDelayed({ uiViewModel.closeSelect() }, 200L)
    }

    private fun deleteSelectedConversations() {
        val selected = adapter.selectInfos
        if (selected.isEmpty()) {
            mainHandler.postDelayed({ uiViewModel.closeSelect() }, 200L)
            return
        }
        progressBar.visibility = View.VISIBLE
        val remaining = java.util.concurrent.atomic.AtomicInteger(selected.size)
        for (session in selected) {
            val sessionId = session.sessionId
            if (TextUtils.isEmpty(sessionId)) {
                if (remaining.decrementAndGet() <= 0) {
                    finishBatchDelete()
                }
                continue
            }
            ImMessageHttpExample.requestRemoveConversation(
                sessionId,
                {
                    ImLocalStore.removeSessionCompletely(sessionId) {
                        if (!isAdded) {
                            return@removeSessionCompletely
                        }
                        requireActivity().runOnUiThread {
                            parentViewModel.removeSession(sessionId)
                            if (remaining.decrementAndGet() <= 0) {
                                finishBatchDelete()
                            }
                        }
                    }
                },
                {
                    ImLocalStore.removeSessionCompletely(sessionId) {
                        if (!isAdded) {
                            return@removeSessionCompletely
                        }
                        requireActivity().runOnUiThread {
                            parentViewModel.removeSession(sessionId)
                            if (remaining.decrementAndGet() <= 0) {
                                finishBatchDelete()
                            }
                        }
                    }
                },
            )
        }
    }

    private fun finishBatchDelete() {
        if (!isAdded) {
            return
        }
        progressBar.visibility = View.GONE
        Toast.makeText(
            requireContext(),
            com.cboda.baselibrary.R.string.remove_conversation_success,
            Toast.LENGTH_SHORT,
        ).show()
        mainHandler.postDelayed({ uiViewModel.closeSelect() }, 200L)
    }

    protected open fun shouldEnrichSessionDisplay(): Boolean = false

    /** 仅缺昵称/头像时再 enrich，未读/预览局部更新不走头像查询。 */
    private fun needsDisplayEnrich(sessions: List<SessionItem>?): Boolean {
        if (sessions.isNullOrEmpty()) {
            return false
        }
        return sessions.any { item ->
            item.sessionName.isNullOrBlank() || item.icon.isNullOrBlank()
        }
    }

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
        sessionsEmpty = list.isEmpty()
        applyOverlayState()
    }

    /** 空态 / 加载中 / 失败重试，并关闭 ProgressBroad 全屏 loading。 */
    private fun applyOverlayState() {
        val failed = parentViewModel.loadFailed.value == true
        val loading = parentViewModel.loading.value == true
        if (failed && sessionsEmpty) {
            errorContainer.visibility = View.VISIBLE
            tvEmpty.visibility = View.GONE
            progressBar.visibility = View.GONE
            recyclerView.visibility = View.GONE
            showContent()
            return
        }
        errorContainer.visibility = View.GONE
        tvEmpty.visibility = if (sessionsEmpty && !loading) View.VISIBLE else View.GONE
        recyclerView.visibility = if (sessionsEmpty) View.GONE else View.VISIBLE
        progressBar.visibility = if (loading && sessionsEmpty) View.VISIBLE else View.GONE
        if (!sessionsEmpty || !loading) {
            showContent()
        }
    }

    /** 进入聊天后清未读：先改 Adapter 角标，再同步 ViewModel/本地库，避免整表闪烁。 */
    private fun clearUnreadAfterEnterChat(session: SessionItem) {
        val sessionId = session.sessionId ?: return
        if (session.unreadCount <= 0) {
            return
        }
        // 注意：Adapter 与点击回调持有同一 SessionItem 引用。
        // 不可先改 session.unreadCount=0，否则 clearUnread 会因「已是 0」跳过 notify。
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
            getString(com.cboda.baselibrary.R.string.cancel_zhiding)
        } else {
            getString(R.string.zhiding)
        }
        val readLabel = if (unread > 0) {
            getString(com.cboda.chatui.R.string.mark_as_read)
        } else {
            getString(com.cboda.chatui.R.string.mark_as_unread)
        }
        val items = arrayOf(pinLabel, readLabel, getString(com.cboda.baselibrary.R.string.action_remove))
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
        parentViewModel.mergeSessionsImmediate(listOf(session))
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
        parentViewModel.mergeSessionsImmediate(listOf(session))
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
                            if (top) com.cboda.baselibrary.R.string.session_top_success else com.cboda.baselibrary.R.string.session_untop_success,
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }
            },
            {
                if (isAdded) {
                    requireActivity().runOnUiThread {
                        progressBar.visibility = View.GONE
                        Toast.makeText(requireContext(), com.cboda.baselibrary.R.string.session_top_failed, Toast.LENGTH_SHORT).show()
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
            displayName = getString(com.cboda.baselibrary.R.string.conversation_name_fallback)
        }
        AlertDialog.Builder(requireContext())
            .setTitle(com.cboda.baselibrary.R.string.remove_conversation_title)
            .setMessage(getString(com.cboda.baselibrary.R.string.remove_conversation_message, displayName))
            .setPositiveButton(com.cboda.baselibrary.R.string.action_remove) { _, _ -> removeConversation(sessionId) }
            .setNegativeButton(com.cboda.baselibrary.R.string.action_cancel, null)
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
                Toast.makeText(requireContext(), com.cboda.baselibrary.R.string.remove_conversation_success, Toast.LENGTH_SHORT).show()
                parentViewModel.removeSession(sessionId)
            }
        }
    }
}
