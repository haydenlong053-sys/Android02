package com.cboda.agora.service.service.collection

import android.util.Log
import com.cboda.agora.service.service.imp.AUIInvitationBroadCmd
import com.google.gson.reflect.TypeToken
import com.cboda.agora.service.service.rtm.AUIRtmBroadException
import com.cboda.agora.service.service.rtm.AUIRtmBroadManager
import com.cboda.agora.service.utils.GsonBroadTools
import java.util.UUID

class AUIListBroadCollection(
    private val channelName: String,
    private val observeKey: String,
    private val rtmManager: AUIRtmBroadManager
) : AUIBaseBroadCollection(channelName, observeKey, rtmManager) {

    private var currentList = listOf<Map<String, Any>>()
        set(value) {
            field = value
            attributesDidChangedClosure?.invoke(channelName, observeKey, AUIAttributesModel(value))
        }

    override fun getMetaData(callback: ((error: AUICollectionBroadException?, value: Any?) -> Unit)?) {
        rtmManager.getMetadata(
            channelName = channelName,
            completion = { error, metaData ->
                if (error != null) {
                    callback?.invoke(AUICollectionBroadException.ErrorCode.unknown.toException("rtm getMetadata error: $error"), null)
                    return@getMetadata
                }
                val data = metaData?.get(observeKey)
                if (data == null) {
                    callback?.invoke(null, null)
                    return@getMetadata
                }

                val list = GsonBroadTools.toBean<List<Map<String, Any>>>(
                    data,
                    object : TypeToken<List<Map<String, Any>>>() {}.type
                )
                if (list == null) {
                    callback?.invoke(
                        AUICollectionBroadException.ErrorCode.encodeToJsonStringFail.toException(),
                        null
                    )
                    return@getMetadata
                }

                callback?.invoke(null, list)
            }
        )
    }

    override fun updateMetaData(
        valueCmd: String?,
        value: Map<String, Any>,
        filter: List<Map<String, Any>>?,
        callback: ((error: AUICollectionBroadException?) -> Unit)?
    ) {
        if (isArbiter()) {
            rtmUpdateMetaData(localUid(), valueCmd, value, filter, callback)
            return
        }

        val uniqueId = UUID.randomUUID().toString()
        val data = AUICollectionMessage(
            channelName = channelName,
            uniqueId = uniqueId,
            sceneKey = observeKey,
            payload = AUICollectionMessagePayload(
                dataCmd = valueCmd,
                data = value,
                filter = filter
            )
        )
        val jsonStr = GsonBroadTools.beanToString(data)
        if (jsonStr == null) {
            callback?.invoke(AUICollectionBroadException.ErrorCode.encodeToJsonStringFail.toException())
            return
        }
        rtmManager.publishAndWaitReceipt(
            channelName = channelName,
            userId = arbiterUid(),
            message = jsonStr,
            uniqueId = uniqueId
        ) { error ->
            if (error != null) {
                callback?.invoke(
                    AUICollectionBroadException.ErrorCode.recvErrorReceipt.toException()
                )
            } else {
                callback?.invoke(null)
            }
        }
    }

    override fun mergeMetaData(
        valueCmd: String?,
        value: Map<String, Any>,
        filter: List<Map<String, Any>>?,
        callback: ((error: AUICollectionBroadException?) -> Unit)?
    ) {
        if (isArbiter()) {
            rtmMergeMetaData(localUid(), valueCmd, value, filter, callback)
            return
        }

        val uniqueId = UUID.randomUUID().toString()
        val data = AUICollectionMessage(
            channelName = channelName,
            uniqueId = uniqueId,
            sceneKey = observeKey,
            payload = AUICollectionMessagePayload(
                type = AUICollectionOperationTypeMerge,
                dataCmd = valueCmd,
                data = value,
                filter = filter
            )
        )
        val jsonStr = GsonBroadTools.beanToString(data)
        if (jsonStr == null) {
            callback?.invoke(AUICollectionBroadException.ErrorCode.encodeToJsonStringFail.toException())
            return
        }
        rtmManager.publishAndWaitReceipt(
            channelName = channelName,
            userId = arbiterUid(),
            message = jsonStr,
            uniqueId = uniqueId
        ) { error ->
            if (error != null) {
                callback?.invoke(
                    AUICollectionBroadException.ErrorCode.recvErrorReceipt.toException()
                )
            } else {
                callback?.invoke(null)
            }
        }
    }

    override fun addMetaData(
        valueCmd: String?,
        value: Map<String, Any>,
        filter: List<Map<String, Any>>?,
        callback: ((error: AUICollectionBroadException?) -> Unit)?
    ) {
        if (isArbiter()) {
            rtmAddMetaData(localUid(), valueCmd, value, filter, callback)
            return
        }

        val uniqueId = UUID.randomUUID().toString()
        val data = AUICollectionMessage(
            channelName = channelName,
            uniqueId = uniqueId,
            sceneKey = observeKey,
            payload = AUICollectionMessagePayload(
                type = AUICollectionOperationTypeAdd,
                dataCmd = valueCmd,
                data = value,
                filter = filter
            )
        )
        val jsonStr = GsonBroadTools.beanToString(data)
        if (jsonStr == null) {
            callback?.invoke(AUICollectionBroadException.ErrorCode.encodeToJsonStringFail.toException())
            return
        }
        rtmManager.publishAndWaitReceipt(
            channelName = channelName,
            userId = arbiterUid(),
            message = jsonStr,
            uniqueId = uniqueId
        ) { error ->
            if (error != null) {
                callback?.invoke(
                    AUICollectionBroadException.ErrorCode.recvErrorReceipt.toException()
                )
            } else {
                callback?.invoke(null)
            }
        }
    }

    override fun removeMetaData(
        valueCmd: String?,
        filter: List<Map<String, Any>>?,
        callback: ((error: AUICollectionBroadException?) -> Unit)?
    ) {
        if (isArbiter()) {
            rtmRemoveMetaData(localUid(), valueCmd, filter, callback)
            return
        }

        val uniqueId = UUID.randomUUID().toString()
        val data = AUICollectionMessage(
            channelName = channelName,
            uniqueId = uniqueId,
            sceneKey = observeKey,
            payload = AUICollectionMessagePayload(
                type = AUICollectionOperationTypeRemove,
                dataCmd = valueCmd,
                data = null,
                filter = filter
            )
        )
        val jsonStr = GsonBroadTools.beanToString(data)
        if (jsonStr == null) {
            callback?.invoke(AUICollectionBroadException.ErrorCode.encodeToJsonStringFail.toException())
            return
        }
        rtmManager.publishAndWaitReceipt(
            channelName = channelName,
            userId = arbiterUid(),
            message = jsonStr,
            uniqueId = uniqueId
        ) { error ->
            if (error != null) {
                callback?.invoke(
                    AUICollectionBroadException.ErrorCode.recvErrorReceipt.toException()
                )
            } else {
                callback?.invoke(null)
            }
        }
    }

    override fun calculateMetaData(
        valueCmd: String?,
        key: List<String>,
        value: Int,
        min: Int,
        max: Int,
        filter: List<Map<String, Any>>?,
        callback: ((error: AUICollectionBroadException?) -> Unit)?
    ) {
        if (isArbiter()) {
            rtmCalculateMetaData(
                localUid(),
                valueCmd,
                key,
                AUICollectionCalcValue(value, min, max),
                filter,
                callback
            )
            return
        }

        val uniqueId = UUID.randomUUID().toString()
        val data = AUICollectionMessage(
            channelName = channelName,
            uniqueId = uniqueId,
            sceneKey = observeKey,
            payload = AUICollectionMessagePayload(
                type = AUICollectionOperationTypeCalculate,
                dataCmd = valueCmd,
                filter = filter,
                data = GsonBroadTools.beanToMap(
                    AUICollectionCalcData(
                        key,
                        AUICollectionCalcValue(value, min, max)
                    )
                )
            )
        )
        val jsonStr = GsonBroadTools.beanToString(data)
        if (jsonStr == null) {
            callback?.invoke(AUICollectionBroadException.ErrorCode.encodeToJsonStringFail.toException())
            return
        }
        rtmManager.publishAndWaitReceipt(
            channelName = channelName,
            userId = arbiterUid(),
            message = jsonStr,
            uniqueId = uniqueId
        ) { error ->
            if (error != null) {
                callback?.invoke(
                    AUICollectionBroadException.ErrorCode.recvErrorReceipt.toException()
                )
            } else {
                callback?.invoke(null)
            }
        }
    }

    override fun cleanMetaData(callback: ((error: AUICollectionBroadException?) -> Unit)?) {
        if (isArbiter()) {
            rtmCleanMetaData(callback)
            return
        }

        val uniqueId = UUID.randomUUID().toString()
        val data = AUICollectionMessage(
            channelName = channelName,
            uniqueId = uniqueId,
            sceneKey = observeKey,
            payload = AUICollectionMessagePayload(
                type = AUICollectionOperationTypeClean,
                dataCmd = "",
                data = null
            )
        )
        val jsonStr = GsonBroadTools.beanToString(data)
        if (jsonStr == null) {
            callback?.invoke(AUICollectionBroadException.ErrorCode.encodeToJsonStringFail.toException())
            return
        }
        rtmManager.publishAndWaitReceipt(
            channelName = channelName,
            userId = arbiterUid(),
            message = jsonStr,
            uniqueId = uniqueId
        ) { error ->
            if (error != null) {
                callback?.invoke(
                    AUICollectionBroadException.ErrorCode.removeMetaDataFail.toException()
                )
            } else {
                callback?.invoke(null)
            }
        }
    }


    private fun rtmAddMetaData(
        publisherId: String,
        valueCmd: String?,
        value: Map<String, Any>,
        filter: List<Map<String, Any>>?,
        callback: ((error: AUICollectionBroadException?) -> Unit)?
    ) {
        val itemIndexes = AUICollectionBroadUtils.getItemIndexes(currentList, filter)
        if (itemIndexes?.isNotEmpty() == true) {
            callback?.invoke(
                AUICollectionBroadException.ErrorCode.filterNotFound.toException()
            )
            return
        }

        val newValue = valueWillChangeClosure?.invoke(publisherId, valueCmd, value) ?: value

        val attrs = AUIAttributesModel(ArrayList(currentList))
        val error = metadataWillAddClosure?.invoke(publisherId, valueCmd, newValue, attrs)
        if (error != null) {
            callback?.invoke(error)
            return
        }

        val list = ArrayList<Map<String, Any>>()
        attrs.getList()?.let {
            list.addAll(it)
        }
        list.add(newValue)
        val outAttrs = AUIAttributesModel(list)
        attributesWillSetClosure?.invoke(
                channelName,
                observeKey,
                valueCmd,
                outAttrs
            )
        val retList = outAttrs.getList()

        val data = GsonBroadTools.beanToString(retList)
        if (data == null) {
            callback?.invoke(AUICollectionBroadException.ErrorCode.encodeToJsonStringFail.toException())
            return
        }

        rtmManager.setBatchMetadata(
            channelName,
            metadata = mapOf(Pair(observeKey, data)),
        ) { e ->
            if (e != null) {
                callback?.invoke(
                    AUICollectionBroadException.ErrorCode.unknown.toException("rtm setBatchMetadata error: $e")
                )
            } else {
                callback?.invoke(null)
            }
        }
    }

    private fun rtmUpdateMetaData(
        publisherId: String,
        valueCmd: String?,
        value: Map<String, Any>,
        filter: List<Map<String, Any>>?,
        callback: ((error: AUICollectionBroadException?) -> Unit)?
    ) {
        val itemIndexes = AUICollectionBroadUtils.getItemIndexes(currentList, filter)
        if (itemIndexes == null) {
            callback?.invoke(
                AUICollectionBroadException.ErrorCode.filterNotFound.toException()
            )
            return
        }

        val newValue = valueWillChangeClosure?.invoke(publisherId, valueCmd, value) ?: value

        val list = ArrayList(currentList)
        itemIndexes.forEach { itemIdx ->
            val item = list[itemIdx]
            val error = metadataWillUpdateClosure?.invoke(publisherId, valueCmd, newValue, item)
            if (error != null) {
                callback?.invoke(error)
                return
            }

            val tempItem = HashMap(item)
            newValue.forEach { key, value ->
                tempItem[key] = value
            }
            list[itemIdx] = tempItem
        }

        val outAttrs = AUIAttributesModel(list)
        attributesWillSetClosure?.invoke(
            channelName,
            observeKey,
            valueCmd,
            outAttrs
        )
        val retList = outAttrs.getList()

        val data = GsonBroadTools.beanToString(retList)
        if (data == null) {
            callback?.invoke(AUICollectionBroadException.ErrorCode.encodeToJsonStringFail.toException())
            return
        }

        rtmManager.setBatchMetadata(
            channelName,
            metadata = mapOf(Pair(observeKey, data)),
        ) { e ->
            if (e != null) {
                callback?.invoke(
                    AUICollectionBroadException.ErrorCode.unknown.toException("rtm setBatchMetadata error: $e")
                )
            } else {
                callback?.invoke(null)
            }
        }
    }

    private fun rtmMergeMetaData(
        publisherId: String,
        valueCmd: String?,
        value: Map<String, Any>,
        filter: List<Map<String, Any>>?,
        callback: ((error: AUICollectionBroadException?) -> Unit)?
    ) {
        val itemIndexes = AUICollectionBroadUtils.getItemIndexes(currentList, filter)
        if (itemIndexes == null) {
            callback?.invoke(
                AUICollectionBroadException.ErrorCode.filterNotFound.toException()
            )
            return
        }
        val newValue = valueWillChangeClosure?.invoke(publisherId, valueCmd, value) ?: value
        val list = ArrayList(currentList)
        itemIndexes.forEach { itemIdx ->
            val item = list[itemIdx]
            val error = metadataWillMergeClosure?.invoke(publisherId, valueCmd, newValue, item)
            if (error != null) {
                callback?.invoke(error)
                return
            }

            val tempItem = AUICollectionBroadUtils.mergeMap(item, newValue)
            list[itemIdx] = tempItem
        }
        val outAttr = AUIAttributesModel(list)
        attributesWillSetClosure?.invoke(
            channelName,
            observeKey,
            valueCmd,
            outAttr
        )
        val retList = outAttr.getList()
        val data = GsonBroadTools.beanToString(retList)
        if (data == null) {
            callback?.invoke(AUICollectionBroadException.ErrorCode.encodeToJsonStringFail.toException())
            return
        }

        rtmManager.setBatchMetadata(
            channelName,
            metadata = mapOf(Pair(observeKey, data)),
        ) { e ->
            if (e != null) {
                callback?.invoke(
                    AUICollectionBroadException.ErrorCode.unknown.toException("rtm setBatchMetadata error: $e")
                )
            } else {
                callback?.invoke(null)
            }
        }
    }

    private fun rtmRemoveMetaData(
        publisherId: String,
        valueCmd: String?,
        filter: List<Map<String, Any>>?,
        callback: ((error: AUICollectionBroadException?) -> Unit)?
    ) {
        val itemIndexes = AUICollectionBroadUtils.getItemIndexes(currentList, filter)
        if (itemIndexes == null) {
            callback?.invoke(
                AUICollectionBroadException.ErrorCode.filterNotFound.toException()
            )
            return
        }
        var list = ArrayList(currentList)
        itemIndexes.forEach { itemIdx ->
            val item = list[itemIdx]
            val error = metadataWillRemoveClosure?.invoke(publisherId, valueCmd, item)
            if (error != null) {
                callback?.invoke(error)
                return
            }
        }


        val filterList = list.filter { !itemIndexes.contains(list.indexOf(it)) }
        list = ArrayList(filterList)
        val outAttr = AUIAttributesModel(list)
        attributesWillSetClosure?.invoke(
            channelName,
            observeKey,
            valueCmd,
            outAttr
        )
        val retList = outAttr.getList()
        val data = GsonBroadTools.beanToString(retList)
        if (data == null) {
            callback?.invoke(AUICollectionBroadException.ErrorCode.encodeToJsonStringFail.toException())
            return
        }

        rtmManager.setBatchMetadata(
            channelName,
            metadata = mapOf(Pair(observeKey, data)),
        ) { e ->
            if (e != null) {
                callback?.invoke(
                    AUICollectionBroadException.ErrorCode.unknown.toException("rtm setBatchMetadata error: $e")
                )
            } else {
                callback?.invoke(null)
            }
        }
    }

    private fun rtmCalculateMetaData(
        publisherId: String,
        valueCmd: String?,
        key: List<String>,
        value: AUICollectionCalcValue,
        filter: List<Map<String, Any>>?,
        callback: ((error: AUICollectionBroadException?) -> Unit)?
    ) {

        val itemIndexes = AUICollectionBroadUtils.getItemIndexes(currentList, filter)
        if (itemIndexes == null) {
            callback?.invoke(AUICollectionBroadException.ErrorCode.filterNotFound.toException())
            return
        }
        val list = ArrayList(currentList)
        itemIndexes.forEach { itemIdx ->
            val item = list[itemIdx]
            val error = metadataWillCalculateClosure?.invoke(
                publisherId, valueCmd, item,
                key, value.value, value.min, value.max
            )
            if (error != null) {
                callback?.invoke(error)
                return
            }

            val calcItem = AUICollectionBroadUtils.calculateMap(
                item,
                key,
                value.value,
                value.min,
                value.max
            )
            if (calcItem == null) {
                callback?.invoke(
                    AUICollectionBroadException.ErrorCode.calculateMapFail.toException()
                )
                return
            }
            list[itemIdx] = calcItem
        }

        val outAttr = AUIAttributesModel(list)
        attributesWillSetClosure?.invoke(
            channelName,
            observeKey,
            valueCmd,
            outAttr
        )
        val retList = outAttr.getList()
        val data = GsonBroadTools.beanToString(retList)
        if (data == null) {
            callback?.invoke(AUICollectionBroadException.ErrorCode.encodeToJsonStringFail.toException())
            return
        }

        rtmManager.setBatchMetadata(
            channelName,
            metadata = mapOf(Pair(observeKey, data)),
        ) { e ->
            if (e != null) {
                callback?.invoke(AUICollectionBroadException.ErrorCode.unknown.toException("rtm setBatchMetadata error: $e"))
            } else {
                callback?.invoke(null)
            }
        }
    }

    private fun rtmCleanMetaData(callback: ((error: AUICollectionBroadException?) -> Unit)?) {
        rtmManager.cleanBatchMetadata(
            channelName = channelName,
            remoteKeys = listOf(observeKey),
            completion = { error ->
                if (error != null) {
                    callback?.invoke(AUICollectionBroadException.ErrorCode.removeMetaDataFail.toException())
                } else {
                    callback?.invoke(null)
                }
            }
        )
    }

    override fun onAttributeChanged(value: Any) {
        val strValue = value as? String ?: return
        val list = GsonBroadTools.toBean<List<Map<String, Any>>>(
            strValue,
            object : TypeToken<List<Map<String, Any>>>() {}.type
        ) ?: return
        currentList = list
    }

    override fun onMessageReceive(publisherId: String, message: String) {
        val messageModel = GsonBroadTools.toBean(message, AUICollectionMessage::class.java) ?: return

        val uniqueId = messageModel.uniqueId
        if (uniqueId == null
            || messageModel.channelName != channelName
            || messageModel.sceneKey != observeKey
        ) {
            return
        }

        if (messageModel.messageType == AUICollectionMessageTypeReceipt) {
            // receipt message from arbiter
            val collectionError = GsonBroadTools.toBean(
                GsonBroadTools.beanToString(messageModel.payload?.data),
                AUICollectionError::class.java
            )
            if (collectionError == null) {
                rtmManager.markReceiptFinished(
                    uniqueId, AUIRtmBroadException(
                        -1, "data is not a map", "receipt message"
                    )
                )
                return
            }

            val code = collectionError.code
            val reason = collectionError.reason
            if (code == 0) {
                // success
                rtmManager.markReceiptFinished(uniqueId, null)
            } else {
                // failure
                rtmManager.markReceiptFinished(
                    uniqueId, AUIRtmBroadException(
                        code,
                        reason,
                        "receipt message from arbiter"
                    )
                )
            }
            return
        }
        var updateType = messageModel.payload?.type
        if (updateType == null) {
            sendReceipt(
                publisherId,
                uniqueId,
                AUICollectionBroadException.ErrorCode.updateTypeNotFound.toException()
            )
            return
        }
        val valueCmd = messageModel.payload!!.dataCmd
        val filter = GsonBroadTools.toBean<List<Map<String, Any>>>(
            GsonBroadTools.beanToString(messageModel.payload!!.filter),
            object : TypeToken<List<Map<String, Any>>>() {}.type
        )
//        Log.e("AUICollection", "valueCmd: $valueCmd  name ${AUIInvitationBroadCmd.sendApply.name} " )
        Log.e("AUICollection", "currentList: $currentList  filter ${filter} " )
        if (valueCmd == AUIInvitationBroadCmd.sendApply.name){
            Log.e("AUICollection","合并啦 updateType:$updateType")
            val itemIndexes = AUICollectionBroadUtils.getItemIndexes(currentList, filter)
            if (itemIndexes?.isNotEmpty()== true){
                updateType=AUICollectionOperationTypeMerge

            }

        }
        Log.e("AUICollection","检测时候需要合并后 updateType:$updateType")

        var error: AUICollectionBroadException? = null
        when (updateType) {
            AUICollectionOperationTypeAdd, AUICollectionOperationTypeUpdate, AUICollectionOperationTypeMerge -> {
                val data = messageModel.payload!!.data
                if (data != null) {
                    if (updateType == AUICollectionOperationTypeAdd) {
                        rtmAddMetaData(publisherId, valueCmd, data, filter) {
                            sendReceipt(publisherId, uniqueId, it)
                        }
                    } else if (updateType == AUICollectionOperationTypeMerge) {
                        rtmMergeMetaData(publisherId, valueCmd, data, filter) {
                            sendReceipt(publisherId, uniqueId, it)
                        }
                    } else {
                        Log.e("AUICollection", "updateType: $updateType rtmUpdateMetaData" )
                        rtmUpdateMetaData(publisherId, valueCmd, data, filter) {
                            sendReceipt(publisherId, uniqueId, it)
                        }
                    }

                } else {
                    error = AUICollectionBroadException.ErrorCode.invalidPayloadType.toException()
                }
            }

            AUICollectionOperationTypeClean -> {
                rtmCleanMetaData {
                    sendReceipt(publisherId, uniqueId, it)
                }
            }

            AUICollectionOperationTypeRemove -> {
                rtmRemoveMetaData(publisherId, valueCmd, filter) {
                    sendReceipt(publisherId, uniqueId, it)
                }
            }

            AUICollectionOperationTypeCalculate -> {
                val calcData = GsonBroadTools.toBean(
                    GsonBroadTools.beanToString(messageModel.payload!!.data),
                    AUICollectionCalcData::class.java
                )
                if (calcData != null) {
                    rtmCalculateMetaData(
                        publisherId,
                        valueCmd,
                        calcData.key,
                        calcData.value,
                        filter
                    ) {
                        sendReceipt(publisherId, uniqueId, it)
                    }
                } else {
                    error = AUICollectionBroadException.ErrorCode.invalidPayloadType.toException()
                }
            }

        }

        if (error != null) {
            sendReceipt(
                publisherId,
                uniqueId,
                error
            )
        }
    }


}