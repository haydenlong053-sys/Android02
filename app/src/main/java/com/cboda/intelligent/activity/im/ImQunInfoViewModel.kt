package com.cboda.intelligent.activity.im

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.cboda.baselibrary.base.BaseViewModel
import com.cboda.baselibrary.bean.PriceConversion
import com.cboda.baselibrary.livedata.SingleLiveBroadData
import com.cboda.baselibrary.network.repository.NetworkBroadRepository
import com.cboda.baselibrary.network.result.onError
import com.cboda.baselibrary.network.result.onFailure
import com.cboda.baselibrary.network.result.onSuccess
import com.cboda.baselibrary.utils.MMKVBroadUtils
import com.cboda.baselibrary.utils.MMKVBroadUtils.Companion.PRICE_DUALX
import com.cboda.baselibrary.utils.anyToInt
import com.cboda.intelligent.bean.GroupBlacklistItem
import com.cboda.intelligent.bean.GroupBlacklistPageBean
import com.cboda.intelligent.bean.GroupJoinPayPriceBean
import com.cboda.intelligent.utils.UrlBroadBroadConstants
import com.cboda.intelligent.utils.dismissLoading
import com.cboda.intelligent.utils.showLoading
import com.cboda.intelligent.utils.showToastMain
import com.freddy.im.db.GroupMemberPageResult
import com.freddy.im.db.ImLocalStore
import com.freddy.im.group.GroupRole
import com.freddy.im.http.ImConversationHttpExample
import com.freddy.im.http.ImGroupHttpExample
import com.freddy.im.http.model.im.GroupMemberItem
import com.freddy.im.http.model.im.GroupNoticeRes
import com.freddy.im.http.model.im.GroupUserVo
import com.freddy.im.http.model.im.GroupVo
import com.freddy.im.listener.OnHttpImListener
import com.freddy.im.message.ImGroupMemberChangeManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.RoundingMode
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
        // 群资料页需要成员网格：提速闲时中的全量 memberList
        ImGroupMemberChangeManager.getInstance().promoteFullSyncPriority(groupId)
        _state.value = ImGroupInfoUiState(loading = true)

        var groupVo: GroupVo? = null
        var notice: String? = null
        var pinned = false
        var muted = false
        var detailFailed = false
        val pending = AtomicInteger(3)

        fun publish() {
            if (pending.decrementAndGet() > 0) return
            if (detailFailed && groupVo == null) {
                _state.postValue(ImGroupInfoUiState(loadFailed = true, loading = false))
                return
            }
            loadMembersFromLocal(groupId, currentUserId, groupVo) { members, myRole ->
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
    }

    /** 本地修改群公告成功后回写 UI（EventBus），避免详情页仍显示旧文案。 */
    fun updateNoticeContent(content: String?) {
        val cur = _state.value ?: return
        if (cur.loading) return
        _state.postValue(cur.copy(noticeContent = content?.takeIf { it.isNotBlank() }))
    }

    fun refreshMembers(groupId: String, currentUserId: String?) {
        val cur = _state.value ?: ImGroupInfoUiState()
        loadMembersFromLocal(groupId, currentUserId, cur.groupVo) { members, myRole ->
            _state.postValue(cur.copy(members = members, myRole = myRole, loading = false))
        }
    }

    /** 信令更新本地成员角色后，从 Realm 刷新网格与 myRole。 */
    fun refreshMembersFromLocal(groupId: String, currentUserId: String?) {
        refreshMembers(groupId, currentUserId)
    }

    private fun loadMembersFromLocal(
        groupId: String,
        currentUserId: String?,
        groupVo: GroupVo?,
        onDone: (List<GroupUserVo>, Int) -> Unit,
    ) {
        ImLocalStore.loadGroupMembersPage(
            groupId,
            currentUserId,
            groupVo?.owerId,
            groupVo?.busiExt?.openQykj,
            -1,
            0,
            DETAIL_MEMBER_PREVIEW_LIMIT,
        ) { page: GroupMemberPageResult ->
            onDone(page.members.map { toGroupUserVo(it) }, page.myRole)
        }
    }

    private fun toGroupUserVo(item: GroupMemberItem): GroupUserVo {
        return GroupUserVo().apply {
            userId = item.userId
            nickname = item.nickname
            groupNickname = item.groupNickname
            username = item.username
            headPortrait = item.headPortrait
            role = item.role
        }
    }

    /**
     * 会话置顶 / 取消置顶。
     * 接口：POST {@code api/im/conversation/sessionTop?sessionId=&flag=}
     * 开关展示态以群详情 {@code busiExt.isZd} 为准，成功后同步回写。
     */
    fun setSessionTop(groupId: String, top: Boolean, onDone: (Boolean) -> Unit) {
        ImConversationHttpExample.requestSessionTop(groupId, top, object : OnHttpImListener<Boolean> {
            override fun onHttpImSuccess(result: Boolean) {
                val muted = _state.value?.sessionMuted == true
                ImLocalStore.updateSessionTopMute(groupId, top, muted)
                val cur = _state.value
                val vo = cur?.groupVo
                vo?.ensureBusiExt()?.isZd = if (top) "1" else "0"
                _state.postValue(cur?.copy(groupVo = vo, sessionPinned = top) ?: cur)
                onDone(true)
            }
        }, { onDone(false) })
    }

    /**
     * 会话免打扰 / 取消免打扰。
     * 接口：POST {@code api/im/conversation/muteNotify?sessionId=&flag=}
     */
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
                        vo.ensureBusiExt().apply {
                            isshop = flag
                            showShop = flag
                        }
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

    /**
     * 修改群管理开关（复用业务接口 {@code /editGroup}）。
     * 对齐野火：openQyyq / openQyyz / openQzts / openQygl / openQysl / openQykj / jiange。
     */
    suspend fun editGroupField(groupId: String, key: String, value: String): SingleLiveBroadData<String> {
        return editGroupField(groupId, key, value, showLoading = true)
    }

    /**
     * @param showLoading 发言间隔等频繁微调场景传 false，避免每次弹出 loading。
     */
    suspend fun editGroupField(
        groupId: String,
        key: String,
        value: String,
        showLoading: Boolean,
    ): SingleLiveBroadData<String> {
        if (showLoading) {
            showLoading()
        }
        val liveData = SingleLiveBroadData<String>()
        val params = hashMapOf<String, Any?>(
            "id" to groupId,
            key to value
        )
        withContext(Dispatchers.IO) {
            try {
                NetworkBroadRepository.postEncrypted2(
                    UrlBroadBroadConstants.EDIT_GROUP,
                    Any::class.java,
                    params
                ).onSuccess {
                    if (showLoading) {
                        dismissLoading()
                    }
                    val cur = _state.value
                    val vo = cur?.groupVo
                    if (vo != null) {
                        applyEditGroupField(vo, key, value)
                        _state.postValue(cur.copy(groupVo = vo))
                    }
                    liveData.postValue(value)
                }.onFailure { _, message ->
                    if (showLoading) {
                        dismissLoading()
                    }
                    showToastMain(message)
                }.onError {
                    if (showLoading) {
                        dismissLoading()
                    }
                }
            } catch (e: Exception) {
                if (showLoading) {
                    dismissLoading()
                }
            }
        }
        return liveData
    }

    /** 拉取群黑名单人数（{@code api/groupBlacklist}，取 data.count）。 */
    suspend fun fetchGroupBlacklistCount(groupId: String): SingleLiveBroadData<Int> {
        val liveData = SingleLiveBroadData<Int>()
        if (groupId.isEmpty()) {
            liveData.postValue(0)
            return liveData
        }
        val params = hashMapOf<String, Any?>(
            "id" to groupId,
            "pageNo" to 1,
            "pageSize" to 1,
        )
        withContext(Dispatchers.IO) {
            try {
                NetworkBroadRepository.postEncrypted2(
                    UrlBroadBroadConstants.GROUP_BLACKLIST,
                    GroupBlacklistPageBean::class.java,
                    params,
                ).onSuccess { data ->
                    val total = data?.count ?: 0
                    val size = data?.list?.size ?: 0
                    liveData.postValue(if (total > 0) total else size)
                }.onFailure { _, _ ->
                    liveData.postValue(0)
                }.onError {
                    liveData.postValue(0)
                }
            } catch (_: Exception) {
                liveData.postValue(0)
            }
        }
        return liveData
    }

    /** 查询群已设置的入群收费价格（{@code api/getGroupJoinPayPrice}，扁平响应取 priceDualx）。 */
    suspend fun fetchGroupJoinPayPrice(groupId: String): SingleLiveBroadData<GroupJoinPayPriceBean?> {
        val liveData = SingleLiveBroadData<GroupJoinPayPriceBean?>()
        if (groupId.isEmpty()) {
            liveData.postValue(null)
            return liveData
        }
        val params = hashMapOf<String, Any?>("id" to groupId)
        withContext(Dispatchers.IO) {
            try {
                NetworkBroadRepository.postEncryptedFlat2(
                    UrlBroadBroadConstants.GET_GROUP_JOIN_PAY_PRICE,
                    GroupJoinPayPriceBean::class.java,
                    params,
                ).onSuccess { data ->
                    liveData.postValue(data)
                }.onFailure { _, _ ->
                    liveData.postValue(null)
                }.onError {
                    liveData.postValue(null)
                }
            } catch (_: Exception) {
                liveData.postValue(null)
            }
        }
        return liveData
    }

    /** 设置群收费开关/价格（{@code api/setGroupJoinPay}）。 */
    suspend fun setGroupJoinPay(
        groupId: String,
        enabled: Boolean,
        price: String,
    ): SingleLiveBroadData<Boolean> {
        showLoading()
        val liveData = SingleLiveBroadData<Boolean>()
        val normalizedPrice = price.trim().ifEmpty { "1" }
        val params = hashMapOf<String, Any?>(
            "id" to groupId,
            "state" to if (enabled) "1" else "0",
            "price" to normalizedPrice,
        )
        withContext(Dispatchers.IO) {
            try {
                NetworkBroadRepository.postEncrypted2(
                    UrlBroadBroadConstants.SET_GROUP_JOIN_PAY,
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

    /** DUALX 金额换算约合 USD（{@code api/wallet/pay/convertOdicPrice}）。 */
    suspend fun convertJoinPayUsd(amount: String): SingleLiveBroadData<String?> {
        val liveData = SingleLiveBroadData<String?>()
        val amountBd = amount.trim().toBigDecimalOrNull()
        if (amountBd == null || amountBd <= BigDecimal.ZERO) {
            liveData.postValue(null)
            return liveData
        }
        val params = hashMapOf<String, Any?>("amount" to amount.trim())
        withContext(Dispatchers.IO) {
            try {
                NetworkBroadRepository.postEncrypted2(
                    UrlBroadBroadConstants.GET_ACTIVATE_PRICE,
                    PriceConversion::class.java,
                    params,
                ).onSuccess { data ->
//                    val usdt = data?.priceUsdt?.takeIf { it.isNotBlank() }
//                    if (!usdt.isNullOrBlank()) {
//                        liveData.postValue(formatUsd(usdt))
//                        return@onSuccess
//                    }
                    val odic = data?.priceOdic?.takeIf { it.isNotBlank() }
                    if (!odic.isNullOrBlank() && amountBd.compareTo(BigDecimal.ONE) == 0) {
                        MMKVBroadUtils.saveSqlStrData(PRICE_DUALX, odic)
                    }
                    liveData.postValue(approxUsdFromCache(amountBd))
                }.onFailure { _, _ ->
                    liveData.postValue(approxUsdFromCache(amountBd))
                }.onError {
                    liveData.postValue(approxUsdFromCache(amountBd))
                }
            } catch (_: Exception) {
                liveData.postValue(approxUsdFromCache(amountBd))
            }
        }
        return liveData
    }

    private fun approxUsdFromCache(amount: BigDecimal): String? {
        val rate = MMKVBroadUtils.getSqlStrData(PRICE_DUALX).toBigDecimalOrNull() ?: return null
        if (rate <= BigDecimal.ZERO) return null
        return formatUsd(amount.multiply(rate).toPlainString())
    }

    private fun formatUsd(raw: String): String {
        return try {
            BigDecimal(raw).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()
        } catch (_: Exception) {
            raw
        }
    }

    companion object {
        /** 群详情网格预览人数（对齐原 memberList anchor=0,count=13） */
        const val DETAIL_MEMBER_PREVIEW_LIMIT = 13

        /** 将 editGroup 成功结果回写到本地 GroupVo / busiExt。 */
        fun applyEditGroupField(vo: GroupVo, key: String, value: String) {
            val ext = vo.ensureBusiExt()
            when (key) {
                "openQyyq" -> {
                    ext.openQyyq = value
                    vo.allowMemberInvite = anyToInt(value)
                }

                "openQyyz" -> {
                    ext.openQyyz = value
                    vo.joinNeedVerify = anyToInt(value)
                }

                "openQzts" -> ext.openQzts = value
                "openQygl" -> ext.openQygl = value
                "openQysl" -> ext.openQysl = value
                "openQykj" -> ext.openQykj = value
                "jiange" -> ext.jiange = value.toIntOrNull()?.coerceAtLeast(0) ?: 0
            }
        }
    }
}
