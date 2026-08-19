package com.cboda.intelligent.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.cboda.intelligent.base.BaseViewBroadModel
import com.freddy.chat.ConversationSessionCategory
import com.freddy.chat.im.IMSClientBootstrap
import com.freddy.im.db.ImLocalStore
import com.freddy.im.http.GroupDetailFetcher
import com.freddy.im.http.ImMessageHttpExample
import com.freddy.im.http.model.im.ConversationListContent
import com.freddy.im.http.model.im.SessionItem
import com.freddy.im.listener.OnHttpImListener
import android.os.Handler
import android.os.Looper
import java.util.LinkedHashMap
import java.util.concurrent.atomic.AtomicBoolean

class ConversationParentViewModel : BaseViewBroadModel() {

    /** 与 LiveData 同步的内存快照；postValue 异步到达前 merge 仍能读到最新列表。 */
    @Volatile
    private var publishedSnapshot: List<SessionItem> = emptyList()

    private val _allSessions = MutableLiveData<List<SessionItem>>(emptyList())
    val allSessions: LiveData<List<SessionItem>> = _allSessions

    val singleSessions: LiveData<List<SessionItem>> = _allSessions.map { sessions ->
        ConversationSessionCategory.filter(sessions, ConversationSessionCategory.SINGLE, currentUserId())
    }

    val myGroupSessions: LiveData<List<SessionItem>> = _allSessions.map { sessions ->
        ConversationSessionCategory.filter(sessions, ConversationSessionCategory.MY_GROUP, currentUserId())
    }

    val otherGroupSessions: LiveData<List<SessionItem>> = _allSessions.map { sessions ->
        ConversationSessionCategory.filter(sessions, ConversationSessionCategory.OTHER_GROUP, currentUserId())
    }

    val singleUnreadCount: LiveData<Int> = singleSessions.map { ConversationSessionCategory.sumUnread(it) }
    val myGroupUnreadCount: LiveData<Int> = myGroupSessions.map { ConversationSessionCategory.sumUnread(it) }
    val otherGroupUnreadCount: LiveData<Int> = otherGroupSessions.map { ConversationSessionCategory.sumUnread(it) }

    /**
     * 社交底栏会话未读 = 对话 + 私群 + 公群（与 Message 页顶栏三分角标之和一致，均已排除免打扰）。
     * 底栏最终展示还需再加上「好友」通讯录角标（fris+gros）。
     */
    val totalUnreadCount: LiveData<Int> = _allSessions.map { sessions ->
        val uid = currentUserId()
        ConversationSessionCategory.sumUnread(
            ConversationSessionCategory.filter(sessions, ConversationSessionCategory.SINGLE, uid)
        ) + ConversationSessionCategory.sumUnread(
            ConversationSessionCategory.filter(sessions, ConversationSessionCategory.MY_GROUP, uid)
        ) + ConversationSessionCategory.sumUnread(
            ConversationSessionCategory.filter(sessions, ConversationSessionCategory.OTHER_GROUP, uid)
        )
    }

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    /** 会话列表拉取失败；有数据时仅记录状态，空列表时由 UI 展示重试。 */
    private val _loadFailed = MutableLiveData(false)
    val loadFailed: LiveData<Boolean> = _loadFailed

    /** 首次网络拉取已完成（成功或失败），此前禁用下拉刷新。 */
    private val _initialLoadCompleted = MutableLiveData(false)
    val initialLoadCompleted: LiveData<Boolean> = _initialLoadCompleted

    /** 当前可见的会话 Tab；null 表示不在会话 Tab（如好友页）。 */
    private val _activeConversationTab = MutableLiveData<ConversationSessionCategory?>(ConversationSessionCategory.SINGLE)
    val activeConversationTab: LiveData<ConversationSessionCategory?> = _activeConversationTab

    /** 用户下拉刷新发起方；其它 Tab 不展示刷新动画。 */
    private val _pullRefreshInitiator = MutableLiveData<ConversationSessionCategory?>(null)
    val pullRefreshInitiator: LiveData<ConversationSessionCategory?> = _pullRefreshInitiator

    /** 防止 Main / 重试 / 信令同时触发重复 getList。 */
    private val requestInFlight = AtomicBoolean(false)

    /** 合并 SESSION_LIST_UPDATED，避免短时间多次 DiffUtil。 */
    private val mergeHandler = Handler(Looper.getMainLooper())
    private val pendingMergeById = LinkedHashMap<String, SessionItem>()
    private val flushPendingMerge = Runnable {
        val batch = ArrayList(pendingMergeById.values)
        pendingMergeById.clear()
        if (batch.isNotEmpty()) {
            mergeSessionsNow(batch)
        }
    }

    fun observeSessions(category: ConversationSessionCategory): LiveData<List<SessionItem>> {
        return when (category) {
            ConversationSessionCategory.SINGLE -> singleSessions
            ConversationSessionCategory.MY_GROUP -> myGroupSessions
            ConversationSessionCategory.OTHER_GROUP -> otherGroupSessions
        }
    }

    fun setActiveConversationTab(category: ConversationSessionCategory?) {
        _activeConversationTab.value = category
    }

    /**
     * @param fromPullRefresh 用户下拉刷新
     * @param initiator 发起刷新的 Tab；数据仍写入共享 _allSessions
     * @return 是否成功发起请求（false 表示已有请求进行中，不重复刷新）
     */
    fun loadConversations(
        fromPullRefresh: Boolean = false,
        initiator: ConversationSessionCategory? = null,
    ): Boolean {
        if (!requestInFlight.compareAndSet(false, true)) {
            if (fromPullRefresh) {
                _pullRefreshInitiator.postValue(null)
            }
            return false
        }
        _loadFailed.postValue(false)
        _loading.postValue(true)
        if (fromPullRefresh && initiator != null) {
            _pullRefreshInitiator.postValue(initiator)
        }
        ImLocalStore.loadSessions { cached ->
            val hasLocal = !cached.isNullOrEmpty()
            if (hasLocal) {
                if (publishedSnapshot.isEmpty()) {
                    publishSessions(cached)
                } else {
                    val existingIds = publishedSnapshot.mapNotNull { it.sessionId }.toHashSet()
                    val extras = cached.filter { item ->
                        val id = item.sessionId
                        !id.isNullOrEmpty() && !existingIds.contains(id)
                    }
                    if (extras.isNotEmpty()) {
                        mergeSessionsNow(extras)
                    }
                }
                ensureGroupCreateTimes(cached)
            }
            ImMessageHttpExample.requestGetConversationList(
                !hasLocal,
                0,
                object : OnHttpImListener<ConversationListContent> {
                    override fun onHttpImSuccess(content: ConversationListContent) {
                        finishConversationLoad(success = true)
                        val list = content.sessionList
                        if (content.isIncremental) {
                            if (!list.isNullOrEmpty()) {
                                mergeSessionsNow(list)
                            }
                        } else {
                            publishRemoteSessions(list)
                        }
                        ensureGroupCreateTimes(list)
                    }
                },
                {
                    finishConversationLoad(success = false)
                },
            )
        }
        return true
    }

    private fun finishConversationLoad(success: Boolean) {
        requestInFlight.set(false)
        _initialLoadCompleted.postValue(true)
        _pullRefreshInitiator.postValue(null)
        _loading.postValue(false)
        _loadFailed.postValue(!success)
    }

    /**
     * 群会话缺 createTime / renameCount / 群主时拉 im/group/detail，写入会话库并刷新列表。
     */
    private fun ensureGroupCreateTimes(sessions: List<SessionItem>?) {
        if (sessions.isNullOrEmpty()) {
            return
        }
        val missingIds = sessions
            .filter { needsGroupDetailBackfill(it) }
            .mapNotNull { it.sessionId?.takeIf { id -> id.isNotBlank() } }
            .distinct()
        if (missingIds.isEmpty()) {
            return
        }
        GroupDetailFetcher.fetchSessionItems(missingIds) { map ->
            val updates = ArrayList(
                map.values.filter {
                    it.createTime > 0L
                        || it.renameCount != null
                        || !it.owerId.isNullOrBlank()
                        || it.conversation > 0
                },
            )
            if (updates.isEmpty()) {
                return@fetchSessionItems
            }
            ImLocalStore.saveSessionsThen(updates) {
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    mergeSessions(updates)
                }
            }
        }
    }

    /**
     * 立即刷新待合并的会话（进入聊天清未读等需要即时反馈的场景）。
     */
    fun mergeSessionsImmediate(updates: List<SessionItem>?) {
        if (updates.isNullOrEmpty()) {
            return
        }
        mergeHandler.removeCallbacks(flushPendingMerge)
        for (update in updates) {
            val id = update.sessionId ?: continue
            if (id.isEmpty()) continue
            pendingMergeById[id] = update
        }
        flushPendingMerge.run()
    }

    fun mergeSessions(updates: List<SessionItem>?) {
        if (updates.isNullOrEmpty()) {
            return
        }
        for (update in updates) {
            val id = update.sessionId ?: continue
            if (id.isEmpty()) continue
            pendingMergeById[id] = update
        }
        mergeHandler.removeCallbacks(flushPendingMerge)
        mergeHandler.postDelayed(flushPendingMerge, 200L)
    }

    private fun mergeSessionsNow(updates: List<SessionItem>) {
        val current = publishedSnapshot.toMutableList()
        for (update in updates) {
            if (update.sessionId.isNullOrEmpty()) {
                continue
            }
            val index = current.indexOfFirst { it.sessionId == update.sessionId }
            if (index >= 0) {
                current[index] = update
            } else {
                current.add(0, update)
            }
        }
        publishSessions(current)
    }

    /**
     * 仅就地补全会话展示字段（昵称/头像），不触发 LiveData，避免列表二次刷新闪烁。
     */
    fun patchSessionDisplayFields(updates: List<SessionItem>?) {
        if (updates.isNullOrEmpty()) {
            return
        }
        val current = publishedSnapshot
        if (current.isEmpty()) {
            return
        }
        for (update in updates) {
            val sessionId = update.sessionId ?: continue
            val target = current.firstOrNull { it.sessionId == sessionId } ?: continue
            if (!update.sessionName.isNullOrBlank()) {
                target.sessionName = update.sessionName
            }
            if (!update.icon.isNullOrBlank()) {
                target.icon = update.icon
            }
            if (!update.username.isNullOrBlank()) {
                target.username = update.username
            }
            if (!update.oppositeUserId.isNullOrBlank()) {
                target.oppositeUserId = update.oppositeUserId
            }
        }
    }

    /** 进入会话后清零未读：更新内存列表并通知观察者（列表侧用 DiffUtil 局部刷新）。 */
    fun clearSessionUnread(sessionId: String?) {
        if (sessionId.isNullOrEmpty()) {
            return
        }
        val current = publishedSnapshot.toMutableList()
        val index = current.indexOfFirst { it.sessionId == sessionId }
        if (index < 0) {
            return
        }
        val item = current[index]
        // Adapter 可能已就地清零同一引用；仍需 post，以便底栏/Tab 未读 LiveData 重算
        item.unreadCount = 0
        publishSessions(current)
    }

    fun removeSession(sessionId: String?) {
        if (sessionId.isNullOrEmpty()) {
            return
        }
        val filtered = publishedSnapshot.filter { it.sessionId != sessionId }
        publishSessions(filtered)
    }

    /**
     * 全量 getList：以服务端列表为主，并保留本地多出的会话（缺条时不冲掉）。
     * 增量走 [mergeSessionsNow]，空增量不动列表。
     */
    private fun publishRemoteSessions(remote: List<SessionItem>?) {
        if (remote.isNullOrEmpty()) {
            if (publishedSnapshot.isEmpty()) {
                publishSessions(emptyList())
            }
            return
        }
        val current = publishedSnapshot
        if (current.isEmpty()) {
            publishSessions(remote)
            return
        }
        val remoteIds = HashSet<String>(remote.size)
        for (item in remote) {
            val id = item.sessionId
            if (!id.isNullOrEmpty()) {
                remoteIds.add(id)
            }
        }
        val extras = current.filter { item ->
            val id = item.sessionId
            !id.isNullOrEmpty() && !remoteIds.contains(id)
        }
        if (extras.isEmpty()) {
            publishSessions(remote)
        } else {
            publishSessions(remote + extras)
        }
    }

    private fun needsGroupDetailBackfill(session: SessionItem): Boolean {
        if (!ConversationSessionCategory.isGroupSession(session)) {
            return false
        }
        return session.createTime <= 0L
            || session.renameCount == null
            || session.owerId.isNullOrBlank()
            || session.conversation <= 0
    }

    private fun publishSessions(sessions: List<SessionItem>?) {
        val sorted = sessions
            ?.filter { !it.sessionId.isNullOrEmpty() }
            ?.distinctBy { it.sessionId }
            ?.sortedWith(
                compareByDescending<SessionItem> { it.topNum }
                    .thenByDescending { it.lastMessageTime },
            )
            ?: emptyList()
        publishedSnapshot = sorted
        _allSessions.postValue(sorted)
    }

    override fun onCleared() {
        mergeHandler.removeCallbacks(flushPendingMerge)
        pendingMergeById.clear()
        super.onCleared()
    }

    private fun currentUserId(): String? = IMSClientBootstrap.getInstance().currentUserId
}
