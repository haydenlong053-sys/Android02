package com.cboda.intelligent.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.cboda.intelligent.base.BaseViewBroadModel
import com.freddy.chat.ConversationSessionCategory
import com.freddy.chat.im.IMSClientBootstrap
import com.freddy.im.db.ImLocalStore
import com.freddy.im.http.ImMessageHttpExample
import com.freddy.im.http.model.im.ConversationListContent
import com.freddy.im.http.model.im.SessionItem
import com.freddy.im.listener.OnHttpImListener

class ConversationParentViewModel : BaseViewBroadModel() {

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

    fun observeSessions(category: ConversationSessionCategory): LiveData<List<SessionItem>> {
        return when (category) {
            ConversationSessionCategory.SINGLE -> singleSessions
            ConversationSessionCategory.MY_GROUP -> myGroupSessions
            ConversationSessionCategory.OTHER_GROUP -> otherGroupSessions
        }
    }

    fun loadConversations() {
        ImLocalStore.loadSessions { cached ->
            if (!cached.isNullOrEmpty()) {
                publishSessions(cached)
            }
        }
        _loading.postValue(true)
        ImMessageHttpExample.requestGetConversationList(
            0,
            0,
            object : OnHttpImListener<ConversationListContent> {
                override fun onHttpImSuccess(content: ConversationListContent) {
                    _loading.postValue(false)
                    publishSessions(content.getSessionList())
                }
            },
            { _loading.postValue(false) },
        )
    }

    fun mergeSessions(updates: List<SessionItem>?) {
        if (updates.isNullOrEmpty()) {
            return
        }
        val current = _allSessions.value?.toMutableList() ?: mutableListOf()
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
        val current = _allSessions.value ?: return
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
        val current = _allSessions.value?.toMutableList() ?: return
        val index = current.indexOfFirst { it.sessionId == sessionId }
        if (index < 0) {
            return
        }
        val item = current[index]
        // Adapter 可能已就地清零同一引用；仍需 post，以便底栏/Tab 未读 LiveData 重算
        item.unreadCount = 0
        _allSessions.value = ArrayList(current)
    }

    fun removeSession(sessionId: String?) {
        if (sessionId.isNullOrEmpty()) {
            return
        }
        val filtered = _allSessions.value
            ?.filter { it.sessionId != sessionId }
            ?: emptyList()
        _allSessions.postValue(filtered)
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
        _allSessions.postValue(sorted)
    }

    private fun currentUserId(): String? = IMSClientBootstrap.getInstance().currentUserId
}
