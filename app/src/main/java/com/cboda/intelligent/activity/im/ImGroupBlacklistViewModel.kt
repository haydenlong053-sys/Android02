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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class GroupBlacklistUiState(
    val blacklist: List<GroupBlacklistItem> = emptyList(),
    val totalCount: Int = 0,
    val pageNo: Int = 0,
    val hasMore: Boolean = true,
    /** 首屏/下拉刷新 */
    val loading: Boolean = false,
    /** 上拉加载更多 */
    val loadingMore: Boolean = false,
)

class ImGroupBlacklistViewModel : BaseViewModel() {

    companion object {
        const val PAGE_SIZE = 20
    }

    private val _state = MutableLiveData(GroupBlacklistUiState(loading = true))
    val state: LiveData<GroupBlacklistUiState> = _state

    /** 重新从第 1 页加载。 */
    fun refresh(groupId: String) {
        if (groupId.isEmpty()) return
        loadPage(groupId, pageNo = 1, append = false)
    }

    /** 加载下一页。 */
    fun loadMore(groupId: String) {
        if (groupId.isEmpty()) return
        val cur = _state.value ?: GroupBlacklistUiState()
        if (cur.loading || cur.loadingMore || !cur.hasMore) return
        loadPage(groupId, pageNo = cur.pageNo + 1, append = true)
    }

    private fun loadPage(groupId: String, pageNo: Int, append: Boolean) {
        val cur = _state.value ?: GroupBlacklistUiState()
        _state.value = cur.copy(
            loading = !append,
            loadingMore = append,
        )
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val params = hashMapOf<String, Any?>(
                    "id" to groupId,
                    "pageNo" to pageNo,
                    "pageSize" to PAGE_SIZE,
                )
                NetworkBroadRepository.postEncrypted2(
                    UrlBroadBroadConstants.GROUP_BLACKLIST,
                    GroupBlacklistPageBean::class.java,
                    params,
                ).onSuccess { data ->
                    val pageList = data?.list.orEmpty()
                    val prev = _state.value ?: GroupBlacklistUiState()
                    val merged = if (append) prev.blacklist + pageList else pageList
                    val total = data?.count?.takeIf { it > 0 }
                        ?: if (append) maxOf(prev.totalCount, merged.size) else merged.size
                    val hasMore = pageList.isNotEmpty()
                        && pageList.size >= PAGE_SIZE
                        && (total <= 0 || merged.size < total)
                    _state.postValue(
                        prev.copy(
                            blacklist = merged,
                            totalCount = total,
                            pageNo = pageNo,
                            hasMore = hasMore,
                            loading = false,
                            loadingMore = false,
                        ),
                    )
                }.onFailure { _, message ->
                    val prev = _state.value ?: GroupBlacklistUiState()
                    _state.postValue(prev.copy(loading = false, loadingMore = false))
                    showToastMain(message)
                }.onError {
                    val prev = _state.value ?: GroupBlacklistUiState()
                    _state.postValue(prev.copy(loading = false, loadingMore = false))
                }
            } catch (_: Exception) {
                val prev = _state.value ?: GroupBlacklistUiState()
                _state.postValue(prev.copy(loading = false, loadingMore = false))
            }
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
            "uids" to uid,
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
