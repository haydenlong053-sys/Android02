package com.cboda.agora.service.service.rtm

class AUIThrottlerUpdateMetaDataBroadModel {

    val throttler = AUIBroadThrottler()

    private val _metaData = mutableMapOf<String, String>()
    val metadata : Map<String, String>
        get() = LinkedHashMap(_metaData)

    private val _callbacks = mutableListOf<(AUIRtmBroadException?) -> Unit>()
    val callbacks : List<(AUIRtmBroadException?) -> Unit>
        get() = ArrayList(_callbacks)

    fun appendMetaDataInfo(metadata: Map<String, String>, callback: (AUIRtmBroadException?) -> Unit) {
        metadata.forEach { (key, value) ->
            _metaData[key] = value
        }
        _callbacks.add(callback)
    }

    fun reset() {
        _callbacks.clear()
        throttler.clean()
        _metaData.clear()
    }
}

class AUIThrottlerRemoveMetaDataModel {

    val throttler = AUIBroadThrottler()

    private val _keys= mutableListOf<String>()
    val keys : List<String>
        get() = ArrayList(_keys)

    private val _callbacks = mutableListOf<(AUIRtmBroadException?) -> Unit>()
    val callbacks : List<(AUIRtmBroadException?) -> Unit>
        get() = ArrayList(_callbacks)

    fun appendMetaDataInfo(keys: List<String>, callback: (AUIRtmBroadException?) -> Unit) {
        _keys.addAll(keys)
        _callbacks.add(callback)
    }

    fun reset() {
        _callbacks.clear()
        throttler.clean()
        _keys.clear()
    }
}