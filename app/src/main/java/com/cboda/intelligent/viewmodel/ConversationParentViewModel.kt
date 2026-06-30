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
            ?.sortedByDescending { it.lastMessageTime }
            ?: emptyList()
        _allSessions.postValue(sorted)
    }

    private fun currentUserId(): String? = IMSClientBootstrap.getInstance().currentUserId
}
