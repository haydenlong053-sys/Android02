package com.cboda.intelligent.activity.im

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.cboda.baselibrary.base.BaseViewModel
import com.cboda.baselibrary.livedata.SingleLiveBroadData
import com.cboda.baselibrary.network.repository.NetworkBroadRepository
import com.cboda.baselibrary.network.result.onError
import com.cboda.baselibrary.network.result.onFailure
import com.cboda.baselibrary.network.result.onSuccess
import com.cboda.intelligent.utils.UrlBroadBroadConstants
import com.cboda.intelligent.utils.dismissLoading
import com.cboda.intelligent.utils.showLoading
import com.cboda.intelligent.utils.showToastMain
import com.freddy.im.db.ImLocalStore
import com.freddy.im.group.GroupRole
import com.freddy.im.http.ImConversationHttpExample
import com.freddy.im.http.ImGroupHttpExample
import com.freddy.im.http.model.im.GroupMemberListContent
import com.freddy.im.http.model.im.GroupNoticeRes
import com.freddy.im.http.model.im.GroupUserVo
import com.freddy.im.http.model.im.GroupVo
import com.freddy.im.listener.OnHttpImListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

class ImQunInfoViewModel : BaseViewModel() {

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

        ImGroupHttpExample.requestGetGroupMemberList(
            groupId, 0, 13,
            object : OnHttpImListener<GroupMemberListContent> {
                override fun onHttpImSuccess(content: GroupMemberListContent) {
                    members = content.members
                    publish()
                }
            }, Runnable {
                publish()
            })

    }

    fun refreshMembers(groupId: String, currentUserId: String?) {
        ImGroupHttpExample.requestGetGroupMemberList(
            groupId, 0, 13,
            object : OnHttpImListener<GroupMemberListContent> {
                override fun onHttpImSuccess(content: GroupMemberListContent) {
                    val cur = _state.value ?: return
                    val myRole = ImGroupMemberUiMapper.resolveMyRole(
                        content.members, currentUserId, cur.groupVo?.owerId
                    )
                    _state.postValue(cur.copy(members = content.members, myRole = myRole))
                }
            }, Runnable {
//                Log.w(ImGroupMemberChangeManager.TAG, ("memberList 失败 groupId=" + groupId + " anchor=" + anchor))
            })
    }

    /** 信令更新本地成员角色后，从 Realm 刷新网格与 myRole。 */
    fun refreshMembersFromLocal(groupId: String, currentUserId: String?) {
        ImLocalStore.loadGroupMembersMap(groupId) { map ->
            val members = map.values
                .sortedBy { it.joinTime }
                .map { item ->
                    GroupUserVo().apply {
                        userId = item.userId
                        nickname = item.nickname
                        groupNickname = item.groupNickname
                        username = item.username
                        headPortrait = item.headPortrait
                        role = item.role
                    }
                }
            val cur = _state.value ?: ImGroupInfoUiState()
            val myRole = if (!currentUserId.isNullOrEmpty() && currentUserId == cur.groupVo?.owerId) {
                GroupRole.OWNER
            } else if (!currentUserId.isNullOrEmpty()) {
                map[currentUserId]?.role ?: GroupRole.MEMBER
            } else {
                GroupRole.MEMBER
            }
            _state.postValue(cur.copy(members = members.take(13), myRole = myRole))
        }
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

    /** 设置群缺席自动移出开关（复用业务接口 {@code /setGroupAbsentKick}）。 */
    suspend fun setGroupAbsentKick(groupId: String, state: String): SingleLiveBroadData<String> {
        showLoading()
        val liveData = SingleLiveBroadData<String>()
        val params = hashMapOf<String, Any?>(
            "id" to groupId,
            "state" to state
        )
        withContext(Dispatchers.IO) {
            try {
                NetworkBroadRepository.postEncrypted2(
                    UrlBroadBroadConstants.SET_GROUP_ABSENT_KICK,
                    Any::class.java,
                    params
                ).onSuccess {
                    dismissLoading()
                    liveData.postValue(state)
                }.onFailure { _, message ->
                    dismissLoading()
                    showToastMain(message)
                }.onError {
                    dismissLoading()
                }
            } catch (e: Exception) {
                dismissLoading()
            }
        }
        return liveData
    }

    /** 设置群商业功能开关（复用业务接口 {@code /setGroupShop}）。 */
    suspend fun setCommercialFunction(groupId: String, enabled: Boolean): SingleLiveBroadData<Boolean> {
        showLoading()
        val liveData = SingleLiveBroadData<Boolean>()
        val params = hashMapOf<String, Any?>(
            "id" to groupId,
            "state" to if (enabled) "1" else "0"
        )
        withContext(Dispatchers.IO) {
            try {
                NetworkBroadRepository.postEncrypted2(
                    UrlBroadBroadConstants.SET_GROUP_SHOP,
                    Any::class.java,
                    params
                ).onSuccess {
                    dismissLoading()
                    val cur = _state.value
                    val vo = cur?.groupVo
                    if (vo != null) {
                        val flag = if (enabled) "1" else "0"
                        vo.isshop = flag
                        vo.showShop = flag
                        _state.postValue(cur.copy(groupVo = vo))
                    }
                    liveData.postValue(enabled)
                }.onFailure { _, message ->
                    dismissLoading()
                    showToastMain(message)
                    liveData.postValue(!enabled)
                }.onError {
                    dismissLoading()
                    liveData.postValue(!enabled)
                }
            } catch (e: Exception) {
                dismissLoading()
                liveData.postValue(!enabled)
            }
        }
        return liveData
    }
}
