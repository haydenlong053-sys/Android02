package com.cboda.intelligent.activity.im

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.cboda.baselibrary.base.BaseViewModel
import com.cboda.baselibrary.livedata.SingleLiveBroadData
import com.cboda.baselibrary.network.repository.NetworkBroadRepository
import com.cboda.baselibrary.network.result.onError
import com.cboda.baselibrary.network.result.onFailure
import com.cboda.baselibrary.network.result.onSuccess
import com.cboda.intelligent.bean.GroupBlacklistItem
import com.cboda.intelligent.bean.GroupBlacklistPageBean
import com.cboda.intelligent.utils.UrlBroadBroadConstants
import com.cboda.intelligent.utils.dismissLoading
import com.cboda.intelligent.utils.showLoading
import com.cboda.intelligent.utils.showToastMain
import com.freddy.im.db.ImLocalStore
import com.freddy.im.http.model.im.GroupMemberItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class GroupBlacklistUiState(
    val blacklist: List<GroupBlacklistItem> = emptyList(),
    val totalCount: Int = 0,
    val candidates: List<GroupMemberItem> = emptyList(),
    val loading: Boolean = false,
)

class ImGroupBlacklistViewModel : BaseViewModel() {

    private val _state = MutableLiveData(GroupBlacklistUiState(loading = true))
    val state: LiveData<GroupBlacklistUiState> = _state

    fun load(groupId: String, currentUserId: String?) {
        if (groupId.isEmpty()) return
        _state.value = (_state.value ?: GroupBlacklistUiState()).copy(loading = true)
        loadBlacklist(groupId)
        loadCandidates(groupId, currentUserId)
    }

    fun refreshBlacklist(groupId: String) {
        loadBlacklist(groupId)
    }

    private fun loadBlacklist(groupId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val params = hashMapOf<String, Any?>(
                    "id" to groupId,
                    "pageNo" to 1,
                    "pageSize" to 100,
                )
                NetworkBroadRepository.postEncrypted2(
                    UrlBroadBroadConstants.GROUP_BLACKLIST,
                    GroupBlacklistPageBean::class.java,
                    params,
                ).onSuccess { data ->
                    val list = data?.list.orEmpty()
                    val total = data?.count?.takeIf { it > 0 } ?: list.size
                    val cur = _state.value ?: GroupBlacklistUiState()
                    _state.postValue(
                        cur.copy(
                            blacklist = list,
                            totalCount = total,
                            loading = false,
                        ),
                    )
                }.onFailure { _, message ->
                    val cur = _state.value ?: GroupBlacklistUiState()
                    _state.postValue(cur.copy(loading = false))
                    showToastMain(message)
                }.onError {
                    val cur = _state.value ?: GroupBlacklistUiState()
                    _state.postValue(cur.copy(loading = false))
                }
            } catch (_: Exception) {
                val cur = _state.value ?: GroupBlacklistUiState()
                _state.postValue(cur.copy(loading = false))
            }
        }
    }

    private fun loadCandidates(groupId: String, currentUserId: String?) {
        ImLocalStore.loadGroupMembersPage(
            groupId,
            currentUserId,
            null,
            null,
            -1,
            0,
            500,
        ) { page ->
            val cur = _state.value ?: GroupBlacklistUiState()
            _state.postValue(cur.copy(candidates = page.members))
        }
    }

    /**
     * 加入/移除黑名单共用传参：id=群ID，uid=用户ID（多人逗号拼接）。
     */
    private suspend fun postBlacklistAction(
        url: String,
        groupId: String,
        uid: String,
    ): SingleLiveBroadData<Boolean> {
        showLoading()
        val liveData = SingleLiveBroadData<Boolean>()
        val params = hashMapOf<String, Any?>(
            "id" to groupId,
            "uid" to uid,
        )
        withContext(Dispatchers.IO) {
            try {
                NetworkBroadRepository.postEncrypted2(
                    url,
                    Any::class.java,
                    params,
                ).onSuccess {
                    dismissLoading()
                    liveData.postValue(true)
                }.onFailure { _, message ->
                    dismissLoading()
                    showToastMain(message)
                    liveData.postValue(false)
                }.onError {
                    dismissLoading()
                    liveData.postValue(false)
                }
            } catch (_: Exception) {
                dismissLoading()
                liveData.postValue(false)
            }
        }
        return liveData
    }

    /** 移除黑名单：{@code api/removeGroupBlacklist}（id + uid）。 */
    suspend fun removeBlacklist(groupId: String, uid: String): SingleLiveBroadData<Boolean> {
        return postBlacklistAction(UrlBroadBroadConstants.REMOVE_GROUP_BLACKLIST, groupId, uid)
    }

    /** 加入黑名单：{@code api/addGroupBlacklist}（id + uid，多人用逗号拼接）。 */
    suspend fun addBlacklist(groupId: String, uid: String): SingleLiveBroadData<Boolean> {
        return postBlacklistAction(UrlBroadBroadConstants.ADD_GROUP_BLACKLIST, groupId, uid)
    }
}
