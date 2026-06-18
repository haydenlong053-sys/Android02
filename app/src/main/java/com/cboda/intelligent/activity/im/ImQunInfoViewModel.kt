package com.cboda.intelligent.activity.im

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.freddy.im.db.ImLocalStore
import com.freddy.im.group.GroupRole
import com.freddy.im.http.ImConversationHttpExample
import com.freddy.im.http.ImGroupHttpExample
import com.freddy.im.http.model.im.GroupNoticeRes
import com.freddy.im.http.model.im.GroupUserVo
import com.freddy.im.http.model.im.GroupVo
import com.freddy.im.listener.OnHttpImListener
import java.util.concurrent.atomic.AtomicInteger

data class ImGroupInfoUiState(
    val groupVo: GroupVo? = null,
    val noticeContent: String? = null,
    val members: List<GroupUserVo> = emptyList(),
    val myRole: Int = GroupRole.MEMBER,
    val sessionPinned: Boolean = false,
    val sessionMuted: Boolean = false,
    val loading: Boolean = false,
    val loadFailed: Boolean = false
)

class ImQunInfoViewModel : ViewModel() {

    private val _state = MutableLiveData(ImGroupInfoUiState(loading = true))
    val state: LiveData<ImGroupInfoUiState> = _state

    fun load(groupId: String, currentUserId: String?) {
        if (groupId.isEmpty()) return
        _state.value = ImGroupInfoUiState(loading = true)

        var groupVo: GroupVo? = null
        var notice: String? = null
        var members: List<GroupUserVo> = emptyList()
        var pinned = false
        var muted = false
        var detailFailed = false
        val pending = AtomicInteger(4)

        fun publish() {
            if (pending.decrementAndGet() > 0) return
            if (detailFailed && groupVo == null) {
                _state.postValue(ImGroupInfoUiState(loadFailed = true, loading = false))
                return
            }
            val myRole = ImGroupMemberUiMapper.resolveMyRole(members, currentUserId, groupVo?.owerId)
            _state.postValue(
                ImGroupInfoUiState(
                    groupVo = groupVo,
                    noticeContent = notice,
                    members = members,
                    myRole = myRole,
                    sessionPinned = pinned,
                    sessionMuted = muted,
                    loading = false
                )
            )
        }

        ImLocalStore.loadSessions { sessions ->
            val session = sessions.firstOrNull { it.sessionId == groupId }
            pinned = session != null && session.topNum > 0
            muted = session?.isMuteNotify == true
            publish()
        }

        ImGroupHttpExample.requestGetGroupDetail(groupId, object : OnHttpImListener<GroupVo> {
            override fun onHttpImSuccess(result: GroupVo) {
                groupVo = result
                publish()
            }
        }, {
            detailFailed = true
            publish()
        })

        ImGroupHttpExample.requestGetGroupNotice(groupId, object : OnHttpImListener<GroupNoticeRes> {
            override fun onHttpImSuccess(result: GroupNoticeRes) {
                notice = result.content?.takeIf { it.isNotBlank() }
                publish()
            }
        }, { publish() })

        ImGroupHttpExample.requestGetAllMembers(groupId, object : OnHttpImListener<List<GroupUserVo>> {
            override fun onHttpImSuccess(result: List<GroupUserVo>) {
                members = result
                publish()
            }
        }, { publish() })
    }

    fun refreshMembers(groupId: String, currentUserId: String?) {
        ImGroupHttpExample.requestGetAllMembers(groupId, object : OnHttpImListener<List<GroupUserVo>> {
            override fun onHttpImSuccess(result: List<GroupUserVo>) {
                val cur = _state.value ?: return
                val myRole = ImGroupMemberUiMapper.resolveMyRole(
                    result, currentUserId, cur.groupVo?.owerId
                )
                _state.postValue(cur.copy(members = result, myRole = myRole))
            }
        }, null)
    }

    fun setSessionTop(groupId: String, top: Boolean, onDone: (Boolean) -> Unit) {
        ImConversationHttpExample.requestSessionTop(groupId, top, object : OnHttpImListener<Boolean> {
            override fun onHttpImSuccess(result: Boolean) {
                val muted = _state.value?.sessionMuted == true
                ImLocalStore.updateSessionTopMute(groupId, top, muted)
                _state.postValue(_state.value?.copy(sessionPinned = top))
                onDone(true)
            }
        }, { onDone(false) })
    }

    fun setSessionMute(groupId: String, mute: Boolean, onDone: (Boolean) -> Unit) {
        ImConversationHttpExample.requestMuteNotify(groupId, mute, object : OnHttpImListener<Boolean> {
            override fun onHttpImSuccess(result: Boolean) {
                val pinned = _state.value?.sessionPinned == true
                ImLocalStore.updateSessionTopMute(groupId, pinned, mute)
                _state.postValue(_state.value?.copy(sessionMuted = mute))
                onDone(true)
            }
        }, { onDone(false) })
    }
}
