package io.agora.auikit.service.imp

import android.util.Log
import io.agora.auikit.model.AUIMicSeatBroadInfo
import io.agora.auikit.model.AUIMicSeatBroadStatus
import io.agora.auikit.model.AUIRoomBroadContext
import io.agora.auikit.model.AUIUserThumbnailBroadInfo
import io.agora.auikit.service.IAUIMicSeatBroadService
import io.agora.auikit.service.callback.AUIBroadCallback
import io.agora.auikit.service.callback.AUIBroadException
import io.agora.auikit.service.collection.AUIAttributesModel
import io.agora.auikit.service.collection.AUICollectionBroadException
import io.agora.auikit.service.collection.AUIMapBroadCollection
import io.agora.auikit.service.rtm.AUIRtmBroadManager
import io.agora.auikit.utils.AUIBroadLogger
import io.agora.auikit.utils.GsonBroadTools
import io.agora.auikit.utils.ObservableBroadHelper
import kotlin.collections.forEach

const val kSeatAttrKey = "micSeat"

enum class AUIMicSeatBroadCmd {
    initSeatCmd,
    leaveSeatCmd,
    enterSeatCmd,
    kickSeatCmd,
    muteAudioCmd,
    closeSeatCmd
}

class AUIMicSeatServiceImpl(
    private val channelName: String,
    private val rtmManager: AUIRtmBroadManager
) : IAUIMicSeatBroadService {

    private val observableHelper =
        ObservableBroadHelper<IAUIMicSeatBroadService.AUIMicSeatRespObserver>()

    private var micSeats = mutableMapOf<Int, AUIMicSeatBroadInfo>()

    private val mapCollection = AUIMapBroadCollection(channelName, kSeatAttrKey, rtmManager)


    init {
        mapCollection.subscribeWillMerge(this::metadataWillMerge)
        mapCollection.subscribeAttributesDidChanged(this::onAttributeChanged)
    }

    override fun initService(completion: AUIBroadCallback?) {
        if (!AUIRoomBroadContext.shared().isRoomOwner(channelName)) {
            return
        }
        val roomInfo = roomContext.getRoomInfo(channelName) ?: return
        val seatMap = mutableMapOf<String, Any>()
        for (i in 0 until roomInfo.micSeatCount) {
            val seat = AUIMicSeatBroadInfo()
            seat.seatIndex = i
            if (i == 0) {
                seat.user = roomContext.currentUserInfo
                seat.seatStatus = AUIMicSeatBroadStatus.used
            }else{
                seat.user = AUIUserThumbnailBroadInfo()
            }
            seatMap.put(i.toString(), seat)
        }
        AUIBroadLogger.logger().d(
            "AUIMicSeatServiceImp",
            "initService >> currentUid=${roomContext.currentUserInfo.userId} arbiterUid=${
                roomContext.getArbiter(
                    channelName
                )?.lockOwnerId()
            }"
        )
        mapCollection.updateMetaData(
            AUIMicSeatBroadCmd.initSeatCmd.name,
            seatMap,
        ) {
            completion?.onResult(
                if (it == null) null else AUIBroadException(
                    AUIBroadException.ERROR_CODE_RTM_COLLECTION,
                    "$it"
                )
            )
        }
    }

    override fun deInitService(completion: AUIBroadCallback?) {
        super.deInitService(completion)
        mapCollection.cleanMetaData {
            completion?.onResult(
                if (it == null) null else AUIBroadException(
                    AUIBroadException.ERROR_CODE_RTM_COLLECTION,
                    "$it"
                )
            )
        }
        mapCollection.release()
    }

    override fun registerRespObserver(observer: IAUIMicSeatBroadService.AUIMicSeatRespObserver?) {
        observableHelper.subscribeEvent(observer)
    }

    override fun unRegisterRespObserver(observer: IAUIMicSeatBroadService.AUIMicSeatRespObserver?) {
        observableHelper.unSubscribeEvent(observer)
    }

    override fun enterSeat(seatIndex: Int, callback: AUIBroadCallback?) {
        mapCollection.mergeMetaData(
            valueCmd = AUIMicSeatBroadCmd.enterSeatCmd.name,
            value = mapOf(
                Pair(
                    seatIndex.toString(),
                    mapOf(
                        Pair("owner", GsonBroadTools.beanToMap(roomContext.currentUserInfo)),
                        Pair("micSeatStatus", AUIMicSeatBroadStatus.used)
                    )
                )
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

    override fun leaveSeat(callback: AUIBroadCallback?) {
        val userId = roomContext.currentUserInfo.userId
        val micSeat = micSeats.values.find { it.user?.userId == userId }

        if (micSeat == null) {
            callback?.onResult(
                AUIBroadException(
                    AUIBroadException.ERROR_CODE_SEAT_NOT_ENTER,
                    "user not on seat"
                )
            )
            return
        }
        micSeat.seatIndex

        mapCollection.mergeMetaData(
            valueCmd = AUIMicSeatBroadCmd.leaveSeatCmd.name,
            value = mapOf(
                Pair(
                    micSeat.seatIndex.toString(),
                    mapOf(
                        Pair("owner", GsonBroadTools.beanToMap(AUIUserThumbnailBroadInfo())),
                        Pair("micSeatStatus", AUIMicSeatBroadStatus.idle)
                    )
                )
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

    override fun autoEnterSeat(callback: AUIBroadCallback?) {
        var toIndex: Int? = null
        val sortedKeys = micSeats.keys.sortedBy { key -> key }
        for (key in sortedKeys) {
            val value = micSeats[key]
            if (value?.seatStatus == AUIMicSeatBroadStatus.idle) {
                toIndex = key
                break
            }
        }
        if (toIndex != null) {
            enterSeat(toIndex, callback)
        } else {
            callback?.onResult(
                AUIBroadException(
                    -1,
                    "can not find empty mic seat"
                )
            )
        }
    }

    override fun kickSeat(seatIndex: Int, callback: AUIBroadCallback?) {
        if (!micSeats.containsKey(seatIndex)) {
            callback?.onResult(AUIBroadException(-1, "The $seatIndex seat is not exist."))
            return
        }
        mapCollection.mergeMetaData(
            valueCmd = AUIMicSeatBroadCmd.kickSeatCmd.name,
            value = mapOf(
                Pair(
                    seatIndex.toString(),
                    mapOf(
                        Pair("owner", GsonBroadTools.beanToMap(AUIUserThumbnailBroadInfo())),
                        Pair("micSeatStatus", AUIMicSeatBroadStatus.idle)
                    )
                )
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


    override fun pickSeat(seatIndex: Int,  user: AUIUserThumbnailBroadInfo , callback: AUIBroadCallback?) {
        // do nothing
        val value = mapOf(
            "$seatIndex" to mapOf(
                "owner" to user,
                "micSeatStatus" to AUIMicSeatBroadStatus.used
            )
        )
        mapCollection.mergeMetaData(
            AUIMicSeatBroadCmd.enterSeatCmd.name,
            value,
            null
        ){
            callback?.onResult(
                if (it == null) null else AUIBroadException(
                    AUIBroadException.ERROR_CODE_RTM_COLLECTION,
                    "$it"
                )
            )
        }
    }

    override fun muteAllAudioSeat(isMute: Boolean, callback: AUIBroadCallback?) {
        if (micSeats.isEmpty()){
            return
        }
        val seatIndex = HashMap<String, Map<String, Boolean>>()
        micSeats.forEach {
            seatIndex[it.key.toString()]= mapOf(Pair("isMuteAudio",if (it.key==0)false else isMute))
        }
        mapCollection.mergeMetaData(
            valueCmd = AUIMicSeatBroadCmd.muteAudioCmd.name,
            value = seatIndex
        ) {
            callback?.onResult(
                if (it == null) null else AUIBroadException(
                    AUIBroadException.ERROR_CODE_RTM_COLLECTION,
                    "$it"
                )
            )
        }
    }


    override fun muteAudioSeat(seatIndex: Int, isMute: Boolean, callback: AUIBroadCallback?) {
        if (!micSeats.containsKey(seatIndex)) {
            callback?.onResult(AUIBroadException(-1, "The $seatIndex seat is not exist."))
            return
        }
        mapCollection.mergeMetaData(
            valueCmd = AUIMicSeatBroadCmd.muteAudioCmd.name,
            value = mapOf(Pair(seatIndex.toString(), mapOf(Pair("isMuteAudio", isMute))))
        ) {
            callback?.onResult(
                if (it == null) null else AUIBroadException(
                    AUIBroadException.ERROR_CODE_RTM_COLLECTION,
                    "$it"
                )
            )
        }
    }

    override fun muteVideoSeat(seatIndex: Int, isMute: Boolean, callback: AUIBroadCallback?) {
        // do nothing
        throw RuntimeException("Not implement yet.")
    }

    override fun closeSeat(seatIndex: Int, isClose: Boolean, callback: AUIBroadCallback?) {
        if (!micSeats.containsKey(seatIndex)) {
            callback?.onResult(AUIBroadException(-1, "The $seatIndex seat is not exist."))
            return
        }
        val micSeat = micSeats[seatIndex]

        var status = AUIMicSeatBroadStatus.idle
        if (isClose) {
            status = AUIMicSeatBroadStatus.locked
        } else if (micSeat?.user?.userId?.isNotEmpty() == true) {
            status = AUIMicSeatBroadStatus.used
        }
        mapCollection.mergeMetaData(
            valueCmd = AUIMicSeatBroadCmd.closeSeatCmd.name,
            value = mapOf(Pair(seatIndex.toString(), mapOf(Pair("micSeatStatus", status))))
        ) {
            callback?.onResult(
                if (it == null) null else AUIBroadException(
                    AUIBroadException.ERROR_CODE_RTM_COLLECTION,
                    "$it"
                )
            )
        }
    }

    override fun onClickInvited(index: Int) {
        observableHelper.notifyEventHandlers {
            it.onShowInvited(index)
        }
    }

    override fun getMicSeatInfo(seatIndex: Int): AUIMicSeatBroadInfo? {
        return micSeats[seatIndex]
    }

    override fun getMicSeatIndex(userId: String): Int {
        var index = -1
        micSeats.forEach { (key, value) ->
            if (value.user?.userId == userId) {
                index = key
            }
        }
        return index
    }

    override fun getMicSeatSize(): Int {
        return micSeats.size
    }

    override fun getChannelName() = channelName

    private fun onAttributeChanged(channelName: String, key: String, value: AUIAttributesModel) {
        if (key != kSeatAttrKey) {
            return
        }
        Log.d("mic_seat_update", "class: ${value.javaClass}")
        val map: Map<String, Any> = HashMap()
        val seats = value.getMap() ?: GsonBroadTools.toBean(GsonBroadTools.beanToString(value), map.javaClass)
        Log.d("mic_seat_update", "seats: $seats")
        seats?.values?.forEach {
            val newSeatInfo =
                GsonBroadTools.toBean(GsonBroadTools.beanToString(it), AUIMicSeatBroadInfo::class.java) ?: return
            val index = newSeatInfo.seatIndex
            val oldSeatInfo = micSeats[index]
            micSeats[index] = newSeatInfo
            val newSeatUserId = newSeatInfo.user?.userId ?: ""
            val oldSeatUserId = oldSeatInfo?.user?.userId ?: ""
            if (oldSeatUserId.isEmpty() && newSeatUserId.isNotEmpty()) {
                Log.d("mic_seat_update", "onAnchorEnterSeat: $it")
                val newUser = newSeatInfo.user ?: return
                observableHelper.notifyEventHandlers { delegate ->
                    delegate.onAnchorEnterSeat(index, newUser)
                }
            }
            if (oldSeatUserId.isNotEmpty() && newSeatUserId.isEmpty()) {
                Log.d("mic_seat_update", "onAnchorLeaveSeat: $it")
                val originUser = oldSeatInfo?.user ?: return
                observableHelper.notifyEventHandlers { delegate ->
                    delegate.onAnchorLeaveSeat(index, originUser)
                }
            }
            if ((oldSeatInfo?.seatStatus ?: AUIMicSeatBroadStatus.idle) != newSeatInfo.seatStatus &&
                (oldSeatInfo?.seatStatus == AUIMicSeatBroadStatus.locked || newSeatInfo.seatStatus == AUIMicSeatBroadStatus.locked)
            ) {
                Log.d("mic_seat_update", "onSeatClose: $it")
                observableHelper.notifyEventHandlers { delegate ->
                    delegate.onSeatClose(index, (newSeatInfo.seatStatus == AUIMicSeatBroadStatus.locked))
                }
            }
            if (oldSeatInfo?.muteAudio != newSeatInfo.muteAudio) {
                Log.d("mic_seat_update", "onSeatAudioMute: $it")
                observableHelper.notifyEventHandlers { delegate ->
                    delegate.onSeatAudioMute(index, newSeatInfo.muteAudio)
                }
            }
            if (oldSeatInfo?.muteVideo != newSeatInfo.muteVideo) {
                Log.d("mic_seat_update", "onSeatVideoMute: $it")
                observableHelper.notifyEventHandlers { delegate ->
                    delegate.onSeatVideoMute(index, newSeatInfo.muteVideo)
                }
            }
        }
    }

    private fun metadataWillMerge(
        publisherId: String,
        valueCmd: String?,
        newValue: Map<String, Any>,
        oldValue: Map<String, Any>
    ): AUICollectionBroadException? {
        val seatInfoPair = newValue.toList()[0]
        val seatIndex = seatInfoPair.first.toInt()
        val seatInfoMap = seatInfoPair.second as? Map<*, *>
        var userId = (seatInfoMap?.get("owner") as? Map<*, *>)?.get("userId") as? String
        when (valueCmd) {
            AUIMicSeatBroadCmd.enterSeatCmd.name -> {
                if (micSeats.values.any { it.user?.userId == userId }) {
                    return AUICollectionBroadException.ErrorCode.unknown.toException("code: ${AUIBroadException.ERROR_CODE_SEAT_ALREADY_ENTER}")
                }
                val seatInfo = micSeats[seatIndex]
                userId = seatInfo?.user?.userId ?: ""
                if (seatInfo?.user != null && seatInfo.seatStatus != AUIMicSeatBroadStatus.idle) {
                    return AUICollectionBroadException.ErrorCode.unknown.toException(
                        "${
                            AUIBroadException(
                                AUIBroadException.ERROR_CODE_SEAT_NOT_IDLE,
                                "mic seat not idle"
                            )
                        }"
                    )
                }
            }

            AUIMicSeatBroadCmd.leaveSeatCmd.name -> {
                if (seatIndex == 0) {
                    return AUICollectionBroadException.ErrorCode.unknown.toException(
                        "${
                            AUIBroadException(
                                AUIBroadException.ERROR_CODE_PERMISSION_LEAK,
                                ""
                            )
                        }"
                    )
                }
                if (micSeats[seatIndex]?.user?.userId != publisherId || roomContext.isRoomOwner(
                        channelName,
                        publisherId
                    )
                ) {
                    return AUICollectionBroadException.ErrorCode.unknown.toException(
                        "${
                            AUIBroadException(
                                AUIBroadException.ERROR_CODE_SEAT_NOT_ENTER,
                                ""
                            )
                        }"
                    )
                }
                userId = micSeats[seatIndex]?.user?.userId ?: ""
                val metadata = mutableMapOf<String, String>()
                var error: AUIBroadException? = null
                observableHelper.notifyEventHandlers {
                    error = it.onSeatWillLeave(userId, metadata)?.let { return@notifyEventHandlers }
                }
                return if (error == null) null else AUICollectionBroadException.ErrorCode.unknown.toException(
                    "$error"
                )
            }

            AUIMicSeatBroadCmd.kickSeatCmd.name -> {
                if (seatIndex == 0) {
                    return AUICollectionBroadException.ErrorCode.unknown.toException("${AUIBroadException(AUIBroadException.ERROR_CODE_PERMISSION_LEAK, "")}")
                }
                userId = micSeats[seatIndex]?.user?.userId ?: ""
                val metadata = mutableMapOf<String, String>()
                var error: AUIBroadException? = null
                observableHelper.notifyEventHandlers {
                    error = it.onSeatWillLeave(userId, metadata)?.let { return@notifyEventHandlers }
                }
                return if (error == null) null else AUICollectionBroadException.ErrorCode.unknown.toException(
                    "$error"
                )
            }
        }
        return null
    }
}