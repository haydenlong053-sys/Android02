package com.cboda.agora.service.service.collection

import com.cboda.agora.service.model.AUIRoomBroadContext
import com.cboda.agora.service.service.rtm.AUIRtmAttributeRespObserver
import com.cboda.agora.service.service.rtm.AUIRtmBroadManager
import com.cboda.agora.service.service.rtm.AUIRtmMessageRespObserver
import com.cboda.agora.service.utils.GsonBroadTools

abstract class AUIBaseBroadCollection(
    private val channelName: String,
    private val observeKey: String,
    private val rtmManager: AUIRtmBroadManager
) : IAUIBroadCollection {

    private val messageRespObserver = object : AUIRtmMessageRespObserver {
        override fun onMessageReceive(channelName: String, publisherId: String, message: String) {
            this@AUIBaseBroadCollection.onMessageReceive(publisherId, message)
        }
    }

    private val attributeRespObserver = object : AUIRtmAttributeRespObserver {
        override fun onAttributeChanged(channelName: String, key: String, value: Any) {
            if (this@AUIBaseBroadCollection.channelName != channelName || key != observeKey) {
                return
            }
            this@AUIBaseBroadCollection.onAttributeChanged(value)
        }
    }

    protected var metadataWillAddClosure: ((
        publisherId: String, valueCmd: String?, value: Map<String, Any>, attrs: AUIAttributesModel
    ) -> AUICollectionBroadException?)? = null

    protected var metadataWillUpdateClosure: ((
        publisherId: String, valueCmd: String?, newValue: Map<String, Any>, oldValue: Map<String, Any>
    ) -> AUICollectionBroadException?)? = null

    protected var metadataWillMergeClosure: ((
        publisherId: String, valueCmd: String?, newValue: Map<String, Any>, oldValue: Map<String, Any>
    ) -> AUICollectionBroadException?)? = null

    protected var metadataWillRemoveClosure: ((
        publisherId: String, valueCmd: String?, value: Map<String, Any>
    ) -> AUICollectionBroadException?)? = null

    protected var metadataWillCalculateClosure: ((
        publisherId: String, valueCmd: String?, value: Map<String, Any>, cKey: List<String>, cValue: Int, cMin: Int, cMax: Int
    ) -> AUICollectionBroadException?)? = null

    protected var attributesDidChangedClosure: ((
        channelName: String, observeKey: String, value: AUIAttributesModel
    ) -> Unit)? = null

    protected var attributesWillSetClosure: ((
        channelName: String, observeKey: String, valueCmd: String?, value: AUIAttributesModel
    ) -> Unit)? = null

    protected var valueWillChangeClosure: ((publisherId: String, valueCmd: String?, value: Map<String, Any>) -> Map<String, Any>?)? =
        null

    init {
        rtmManager.subscribeMessage(messageRespObserver)
        rtmManager.subscribeAttribute(channelName, observeKey, attributeRespObserver)
    }

    final override fun release() {
        rtmManager.unsubscribeMessage(messageRespObserver)
        rtmManager.unsubscribeAttribute(
            channelName, observeKey,
            attributeRespObserver
        )
    }

    final override fun subscribeWillAdd(closure: ((publisherId: String, valueCmd: String?, value: Map<String, Any>, attr: AUIAttributesModel) -> AUICollectionBroadException?)?) {
        metadataWillAddClosure = closure
    }

    final override fun subscribeWillUpdate(closure: ((publisherId: String, valueCmd: String?, newValue: Map<String, Any>, oldValue: Map<String, Any>) -> AUICollectionBroadException?)?) {
        metadataWillUpdateClosure = closure
    }

    final override fun subscribeWillMerge(closure: ((publisherId: String, valueCmd: String?, newValue: Map<String, Any>, oldValue: Map<String, Any>) -> AUICollectionBroadException?)?) {
        metadataWillMergeClosure = closure
    }

    final override fun subscribeWillRemove(closure: ((publisherId: String, valueCmd: String?, value: Map<String, Any>) -> AUICollectionBroadException?)?) {
        metadataWillRemoveClosure = closure
    }

    final override fun subscribeAttributesDidChanged(closure: ((channelName: String, observeKey: String, value: AUIAttributesModel) -> Unit)?) {
        attributesDidChangedClosure = closure
    }

    final override fun subscribeAttributesWillSet(closure: ((channelName: String, observeKey: String, valueCmd: String?, value: AUIAttributesModel) -> Unit)?) {
        attributesWillSetClosure = closure
    }

    override fun subscribeWillCalculate(closure: ((publisherId: String, valueCmd: String?, value: Map<String, Any>, cKey: List<String>, cValue: Int, cMin: Int, cMax: Int) -> AUICollectionBroadException?)?) {
        metadataWillCalculateClosure = closure
    }

    override fun subscribeValueWillChange(closure: ((publisherId: String, valueCmd: String?, value: Map<String, Any>) -> Map<String, Any>?)?) {
        valueWillChangeClosure = closure
    }

    protected fun localUid() = AUIRoomBroadContext.shared().currentUserInfo.userId

    protected fun arbiterUid() =
        AUIRoomBroadContext.shared().getArbiter(channelName)?.lockOwnerId() ?: ""

    protected fun isArbiter() =
        AUIRoomBroadContext.shared().getArbiter(channelName)?.isArbiter() ?: false

    protected fun sendReceipt(publisherId: String, uniqueId: String, error: AUICollectionBroadException?) {
        val collectionError = AUICollectionError(error?.code ?: 0, error?.message ?: "")
        val message = AUICollectionMessage(
            channelName = channelName,
            messageType = AUICollectionMessageTypeReceipt,
            uniqueId = uniqueId,
            sceneKey = observeKey,
            payload = AUICollectionMessagePayload(
                dataCmd = "",
                data = GsonBroadTools.beanToMap(collectionError),
            )
        )
        val jsonStr = GsonBroadTools.beanToString(message) ?: return

        rtmManager.publish(channelName, publisherId, jsonStr) {}
    }

    protected abstract fun onMessageReceive(publisherId: String, message: String)

    protected abstract fun onAttributeChanged(value: Any)
}