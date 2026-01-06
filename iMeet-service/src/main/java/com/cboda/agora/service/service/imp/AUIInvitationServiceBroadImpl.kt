package com.cboda.agora.service.service.imp

import com.cboda.agora.service.model.AUIInvitationBroadInfo
import com.cboda.agora.service.model.AUIUserBroadInfo
import com.cboda.agora.service.service.IAUIInvitationBroadService
import com.cboda.agora.service.service.callback.AUIBroadCallback
import com.cboda.agora.service.service.callback.AUIBroadException
import com.cboda.agora.service.service.collection.AUIAttributesModel
import com.cboda.agora.service.service.collection.AUICollectionBroadException
import com.cboda.agora.service.service.collection.AUIListBroadCollection
import com.cboda.agora.service.service.rtm.AUIRtmBroadManager
import com.cboda.agora.service.service.rtm.AUIBroadThrottler
import com.cboda.agora.service.utils.GsonBroadTools
import com.cboda.agora.service.utils.ObservableBroadHelper
import java.util.Timer
import kotlin.concurrent.scheduleAtFixedRate

const val kInvitationKey = "invitation"

enum class AUIInvitationBroadCmd {
    sendApply,
    cancelApply,
    acceptApply,
    rejectApply,

    sendInvit,
    cancelInvit,
    acceptInvit,
    rejectInvit,
}

private val kSeatNoKey = "seatNo"
private val kStatusKey = "status"

private val kEditTimeKey = "editTime"

class AUIInvitationServiceImpl(
    private val channelName: String,
    private val rtmManager: AUIRtmBroadManager
) : IAUIInvitationBroadService {

    private val invitationCollection = AUIListBroadCollection(channelName, kInvitationKey, rtmManager)

    private val invitationMap = mutableMapOf<String, AUIInvitationBroadInfo>()
    private val checkThrottler by lazy { AUIBroadThrottler() }
    private val observerList = mutableListOf<AUIInvitationBroadInfo>()
    private var timer: Timer? = null
        set(value) {
            value?.cancel()
            field = value
        }

    init {
        invitationCollection.subscribeValueWillChange(this::valueWillChange)
        invitationCollection.subscribeWillAdd(this::metadataWillAdd)
        invitationCollection.subscribeWillMerge(this::metadataWillMerge)
        invitationCollection.subscribeAttributesWillSet(this::metadataWillSet)
        invitationCollection.subscribeAttributesDidChanged(this::onAttributeChanged)
    }


    override fun cleanUserInfo(userId: String, completion: AUIBroadCallback?) {
        super.cleanUserInfo(userId, completion)
        if(roomContext.getArbiter(channelName)?.isArbiter() != true){
            return
        }
        invitationCollection.removeMetaData(
            "",
            listOf(
                mapOf("userId" to userId)
            )
        ){
            completion?.onResult(
                if (it == null) null else AUIBroadException(
                    AUIBroadException.ERROR_CODE_RTM_COLLECTION,
                    " $it"
                )
            )
        }
    }

    override fun deInitService(completion: AUIBroadCallback?) {
        super.deInitService(completion)
        invitationCollection.cleanMetaData {
            completion?.onResult(
                if (it == null) null else AUIBroadException(
                    AUIBroadException.ERROR_CODE_RTM_COLLECTION,
                    " $it"
                )
            )
        }
        invitationCollection.release()
    }

    private val observableHelper =
        ObservableBroadHelper<IAUIInvitationBroadService.AUIInvitationRespObserver>()

    override fun registerRespObserver(observer: IAUIInvitationBroadService.AUIInvitationRespObserver?) {
        observableHelper.subscribeEvent(observer)
    }

    override fun unRegisterRespObserver(observer: IAUIInvitationBroadService.AUIInvitationRespObserver?) {
        observableHelper.unSubscribeEvent(observer)
    }

    override fun sendInvitation(userId: String, seatIndex: Int, callback: AUIBroadCallback?) {
        val info = AUIInvitationBroadInfo()
        info.seatNo = seatIndex
        info.userId = userId
        info.type = AUIInvitationBroadInfo.AUIInvitationType.Invite

        invitationCollection.addMetaData(
            AUIInvitationBroadCmd.sendInvit.name,
            GsonBroadTools.beanToMap(info),
            listOf(
                mapOf("userId" to userId, kStatusKey to AUIInvitationBroadInfo.AUIInvitationStatus.Waiting),
                mapOf("userId" to userId, kStatusKey to AUIInvitationBroadInfo.AUIInvitationStatus.Accept),
            )
        ) {
            callback?.onResult(
                if (it == null) null else AUIBroadException(
                    AUIBroadException.ERROR_CODE_RTM_COLLECTION,
                    "$it"
                )
            )
        }
    }

    override fun acceptInvitation(userId: String, seatIndex: Int, callback: AUIBroadCallback?) {
        invitationCollection.mergeMetaData(
            AUIInvitationBroadCmd.acceptInvit.name,
            mapOf(
                kStatusKey to AUIInvitationBroadInfo.AUIInvitationStatus.Accept,
                kEditTimeKey to roomContext.ntpTime
            ),
            listOf(mapOf("userId" to userId, "type" to AUIInvitationBroadInfo.AUIInvitationType.Invite))
        ) {
            callback?.onResult(
                if (it == null) null else AUIBroadException(
                    AUIBroadException.ERROR_CODE_RTM_COLLECTION,
                    "$it"
                )
            )
        }
    }

    override fun rejectInvitation(userId: String, callback: AUIBroadCallback?) {
        invitationCollection.mergeMetaData(
            AUIInvitationBroadCmd.rejectInvit.name,
            mapOf(
                kStatusKey to AUIInvitationBroadInfo.AUIInvitationStatus.Reject,
                kEditTimeKey to roomContext.ntpTime
            ),
            listOf(mapOf("userId" to userId, "type" to AUIInvitationBroadInfo.AUIInvitationType.Invite))
        ) {
            callback?.onResult(
                if (it == null) null else AUIBroadException(
                    AUIBroadException.ERROR_CODE_RTM_COLLECTION,
                    "$it"
                )
            )
        }
    }

    override fun cancelInvitation(userId: String, callback: AUIBroadCallback?) {
        invitationCollection.mergeMetaData(
            AUIInvitationBroadCmd.cancelInvit.name,
            mapOf(
                kStatusKey to AUIInvitationBroadInfo.AUIInvitationStatus.Cancel,
                kEditTimeKey to roomContext.ntpTime
            ),
            listOf(
                mapOf("userId" to userId, "type" to AUIInvitationBroadInfo.AUIInvitationType.Invite)
            )
        ) {
            callback?.onResult(
                if (it == null) null else AUIBroadException(
                    AUIBroadException.ERROR_CODE_RTM_COLLECTION,
                    "$it"
                )
            )
        }
    }

    override fun sendApply(seatIndex: Int, callback: AUIBroadCallback) {
        val info = AUIInvitationBroadInfo()
        info.seatNo = seatIndex
        info.userId = roomContext.currentUserInfo.userId
        info.type = AUIInvitationBroadInfo.AUIInvitationType.Apply

        invitationCollection.addMetaData(
            AUIInvitationBroadCmd.sendApply.name,
            GsonBroadTools.beanToMap(info),
            listOf(
//                mapOf("userId" to info.userId, kStatusKey to AUIInvitationStatus.Waiting),
//                mapOf("userId" to info.userId, kStatusKey to AUIInvitationStatus.Accept)
            )
        ) {
            callback.onResult(
                if (it == null) null else AUIBroadException(
                    AUIBroadException.ERROR_CODE_RTM_COLLECTION,
                    "$it"
                )
            )
        }
    }

    override fun cancelApply(callback: AUIBroadCallback) {
        cancelApplyByUserId(roomContext.currentUserInfo.userId, callback)
    }

    private fun cancelApplyByUserId(userId: String, callback: AUIBroadCallback) {
        invitationCollection.mergeMetaData(
            AUIInvitationBroadCmd.cancelApply.name,
            mapOf(
                kStatusKey to AUIInvitationBroadInfo.AUIInvitationStatus.Cancel,
                kEditTimeKey to roomContext.ntpTime
            ),
            listOf(
                mapOf(
                    "userId" to userId,
                    "type" to AUIInvitationBroadInfo.AUIInvitationType.Apply
                )
            )
        ) {
            callback.onResult(
                if (it == null) null else AUIBroadException(
                    AUIBroadException.ERROR_CODE_RTM_COLLECTION,
                    "$it"
                )
            )
        }
    }

    override fun acceptApply(userId: String, seatIndex: Int, callback: AUIBroadCallback) {
        invitationCollection.mergeMetaData(
            AUIInvitationBroadCmd.acceptApply.name,
            mapOf(
                kStatusKey to AUIInvitationBroadInfo.AUIInvitationStatus.Accept,
                kEditTimeKey to roomContext.ntpTime
            ),
            listOf(
//                mapOf("userId" to userId, "type" to AUIInvitationStatus.Waiting),
                mapOf("userId" to userId, "type" to AUIInvitationBroadInfo.AUIInvitationType.Apply)
            )
        ) {
            callback.onResult(
                if (it == null) null else AUIBroadException(
                    AUIBroadException.ERROR_CODE_RTM_COLLECTION,
                    "$it"
                )
            )
        }
    }

    override fun rejectApply(userId: String, callback: AUIBroadCallback) {
        invitationCollection.mergeMetaData(
            AUIInvitationBroadCmd.rejectApply.name,
            mapOf(
                kStatusKey to AUIInvitationBroadInfo.AUIInvitationStatus.Reject,
                kEditTimeKey to roomContext.ntpTime
            ),
            listOf(mapOf("userId" to userId, "type" to AUIInvitationBroadInfo.AUIInvitationType.Apply))
        ) {
            callback.onResult(
                if (it == null) null else AUIBroadException(
                    AUIBroadException.ERROR_CODE_RTM_COLLECTION,
                    "$it"
                )
            )
        }
    }


    override fun getChannelName() = channelName

    private fun valueWillChange(
        publisherId: String,
        valueCmd: String?,
        value: Map<String, Any>
    ): Map<String, Any>? {
        val tempItem = mutableMapOf<String, Any>()
        tempItem.putAll(value)
        val currentTime = roomContext.ntpTime
        tempItem[kEditTimeKey] = currentTime
        when (valueCmd) {
            AUIInvitationBroadCmd.sendInvit.name,
            AUIInvitationBroadCmd.sendApply.name -> tempItem["createTime"] = currentTime
        }
        return tempItem
    }

    private fun metadataWillAdd(
        publisherId: String,
        valueCmd: String?,
        value: Map<String, Any>,
        attrs: AUIAttributesModel
    ): AUICollectionBroadException? {
        return when (valueCmd) {
            AUIInvitationBroadCmd.sendApply.name, AUIInvitationBroadCmd.sendInvit.name -> {
                attrs.setList(attrs.getList()?.filter { it["userId"] != value["userId"] })
                null
            }
            else -> {
                AUICollectionBroadException.ErrorCode.unknown.toException("UnKnown cmd : $valueCmd")
            }
        }
    }

    private fun metadataWillMerge(
        publisherId: String,
        valueCmd: String?,
        newValue: Map<String, Any>,
        oldValue: Map<String, Any>
    ): AUICollectionBroadException? {
        return when (valueCmd) {
            AUIInvitationBroadCmd.acceptInvit.name -> {
                val userId = oldValue["userId"] as? String ?: ""
                val seatIndex = (oldValue[kSeatNoKey] as? Long)?.toInt() ?: 0
                var error: AUIBroadException? = null
                observableHelper.notifyEventHandlers {
                    error = it.onInviteWillAccept(userId, seatIndex)
                }
                if (error != null) AUICollectionBroadException.ErrorCode.unknown.toException() else null
            }

            AUIInvitationBroadCmd.acceptApply.name -> {
                val userId = oldValue["userId"] as? String ?: ""
                val seatIndex = (oldValue[kSeatNoKey] as? Long)?.toInt() ?: 0
                var error: AUIBroadException? = null
                observableHelper.notifyEventHandlers {
                    error = it.onApplyWillAccept(userId, seatIndex)
                }
                if (error != null) AUICollectionBroadException.ErrorCode.unknown.toException() else null
            }

            AUIInvitationBroadCmd.cancelInvit.name,
            AUIInvitationBroadCmd.cancelApply.name,
            AUIInvitationBroadCmd.rejectInvit.name,
            AUIInvitationBroadCmd.rejectApply.name -> null
            else -> AUICollectionBroadException.ErrorCode.unknown.toException()
        }
    }

    private fun metadataWillSet(
        channelName: String,
        observeKey: String,
        valueCmd: String?,
        value: AUIAttributesModel
    ) {
        val list = value.getList() ?: return
        val currentTime = roomContext.ntpTime
        var filterList = rejectDuplicateAcceptedRequests(list) ?: list
        filterList = filterList.filter { attr ->
            val editTime = attr[kEditTimeKey] as? Long ?: 0
            val invalidTs = attr["invalidTs"] as? Long ?: 0
            val status = attr[kStatusKey] as? Int ?: 0
            if (status == AUIInvitationBroadInfo.AUIInvitationStatus.Waiting) {
                return@filter true
            }
            if (currentTime - editTime < invalidTs) {
                return@filter true
            }
            return@filter false
        }
        value.setList(filterList)
    }

    private fun onAttributeChanged(channelName: String, key: String, value: AUIAttributesModel) {
        val list =
            GsonBroadTools.toList(GsonBroadTools.beanToString(value.getList()), AUIInvitationBroadInfo::class.java)
                ?: return

        // 申请列表
        val newApplyList = list.filter { it.type == AUIInvitationBroadInfo.AUIInvitationType.Apply }
        val oldApplyList =
            invitationMap.values.filter { it.type == AUIInvitationBroadInfo.AUIInvitationType.Apply }

        // 邀请列表
        val newInviteList = list.filter { it.type == AUIInvitationBroadInfo.AUIInvitationType.Invite }
        val oldInviteList =
            invitationMap.values.filter { it.type == AUIInvitationBroadInfo.AUIInvitationType.Invite }

        invitationMap.clear()
        list.forEach {
            invitationMap[it.userId] = it
        }

        // 处理申请差异列表
        var applyListChanged = false
        newApplyList.forEach { newApply ->
            val oldApply = oldApplyList.find { it.userId == newApply.userId }
            if (oldApply?.status != newApply.status) {
                applyListChanged = true
                when (newApply.status) {
                    AUIInvitationBroadInfo.AUIInvitationStatus.Accept -> {
                        observableHelper.notifyEventHandlers {
                            it.onApplyAccepted(newApply.userId, newApply.seatNo)
                        }
                    }

                    AUIInvitationBroadInfo.AUIInvitationStatus.Reject -> {
                        observableHelper.notifyEventHandlers {
                            it.onApplyRejected(newApply.userId)
                        }
                    }

                    AUIInvitationBroadInfo.AUIInvitationStatus.Timeout -> {
                    }

                    AUIInvitationBroadInfo.AUIInvitationStatus.Waiting -> {
                        observableHelper.notifyEventHandlers {
                            it.onReceiveNewApply(newApply.userId, newApply.seatNo)
                        }
                    }
                }
            }
        }
        oldApplyList.forEach { oldApply ->
            val newApply = newApplyList.find { it.userId == oldApply.userId }
            if (newApply == null) {
                if (oldApply.status == AUIInvitationBroadInfo.AUIInvitationStatus.Waiting) {
                    observableHelper.notifyEventHandlers {
                        it.onApplyCanceled(oldApply.userId)
                    }
                }
                applyListChanged = true
                return@forEach
            }
        }
        if (applyListChanged) {
            observableHelper.notifyEventHandlers {
                it.onApplyListUpdate(newApplyList.filter { it.status == AUIInvitationBroadInfo.AUIInvitationStatus.Waiting }.map {
                    AUIUserBroadInfo().apply {
                        userId = it.userId
                        micIndex = it.seatNo
                        isRead = if (it.status== AUIInvitationBroadInfo.AUIInvitationStatus.Waiting) 0 else 1
                    }
                })
            }
        }

        // 处理邀请差异列表
        newInviteList.forEach { newInvite ->
            val oldInvite = oldApplyList.find { it.userId == newInvite.userId }
            if (oldInvite?.status != newInvite.status) {
                when (newInvite.status) {
                    AUIInvitationBroadInfo.AUIInvitationStatus.Accept -> {
                        observableHelper.notifyEventHandlers {
                            it.onInviteeAccepted(newInvite.userId, newInvite.seatNo)
                        }
                    }

                    AUIInvitationBroadInfo.AUIInvitationStatus.Reject -> {
                        observableHelper.notifyEventHandlers {
                            it.onInviteeRejected(newInvite.userId)
                        }
                    }

                    AUIInvitationBroadInfo.AUIInvitationStatus.Timeout -> {

                    }

                    AUIInvitationBroadInfo.AUIInvitationStatus.Waiting -> {
                        observableHelper.notifyEventHandlers {
                            it.onReceiveInvitation(newInvite.userId, newInvite.seatNo)
                        }
                    }
                }
            }
        }
        oldInviteList.forEach { oldInvite ->
            val newInvite = newApplyList.find { it.userId == oldInvite.userId }
            if (newInvite == null && oldInvite.status == AUIInvitationBroadInfo.AUIInvitationStatus.Waiting) {
                observableHelper.notifyEventHandlers {
                    it.onInvitationCancelled(oldInvite.userId)
                }
                return@forEach
            }
        }

        checkThrottler.triggerLastEvent(delay = 300) {
            checkWaitingTimeout()
        }
    }

    private fun rejectDuplicateAcceptedRequests(list: List<Map<String, Any>>): List<Map<String, Any>>? {
        //获取accept的麦位数组
        val acceptSeatNos = list.map { item ->
            if (item[kStatusKey] == AUIInvitationBroadInfo.AUIInvitationStatus.Accept.toLong()) {
                return@map item[kSeatNoKey] as? Long
            }
            return@map null
        }

        val updateList = mutableListOf<Map<String, Any>>()
        list.forEach { i ->
            val item = HashMap(i)
            if(item[kStatusKey] == AUIInvitationBroadInfo.AUIInvitationStatus.Waiting.toLong() && acceptSeatNos.contains(item[kSeatNoKey])){
                val currentTime = roomContext.ntpTime
                item[kStatusKey] = AUIInvitationBroadInfo.AUIInvitationStatus.Reject
                item[kEditTimeKey] = currentTime
            }
            updateList.add(item)
        }
        return updateList
    }

    private fun checkWaitingTimeout() {
        if (roomContext.getArbiter(channelName)?.isArbiter() != true) {
            return
        }
        val observerList =
            invitationMap.values.filter { it.status == AUIInvitationBroadInfo.AUIInvitationStatus.Waiting }
        if (observerList.isEmpty()) {
            timer = null
            return
        }
        this.observerList.clear()
        this.observerList.addAll(observerList)
        timer = Timer().apply {
            scheduleAtFixedRate(0, 1000) {
                val currentTs = roomContext.ntpTime
                val observerList = ArrayList(this@AUIInvitationServiceImpl.observerList)
                this@AUIInvitationServiceImpl.observerList.clear()
                observerList.forEach { info ->
                    if (currentTs - info.createTime > info.timeoutTs) {
                        when (info.type) {
                            AUIInvitationBroadInfo.AUIInvitationType.Apply -> cancelApplyByUserId(info.userId) {}
                            AUIInvitationBroadInfo.AUIInvitationType.Invite -> cancelInvitation(info.userId) {}
                        }
                    } else {
                        this@AUIInvitationServiceImpl.observerList.add(info)
                    }
                }
                if (this@AUIInvitationServiceImpl.observerList.isEmpty()) {
                    timer = null
                }
            }
        }
    }
}