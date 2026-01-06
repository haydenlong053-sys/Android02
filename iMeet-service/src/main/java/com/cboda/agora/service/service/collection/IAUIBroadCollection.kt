package com.cboda.agora.service.service.collection

/**
 * IAUIBroadCollection
 *
 * @constructor Create empty IAUIBroadCollection
 */
interface IAUIBroadCollection {

    /**
     * Subscribe will add
     *
     * @param closure
     */
    fun subscribeWillAdd(closure: ((publisherId: String, valueCmd: String?, value: Map<String, Any>, attrs: AUIAttributesModel) -> AUICollectionBroadException?)?)

    /**
     * Subscribe will update
     *
     * @param closure
     */
    fun subscribeWillUpdate(closure: ((publisherId: String, valueCmd: String?, newValue: Map<String, Any>, oldValue: Map<String, Any>) -> AUICollectionBroadException?)?)

    /**
     * Subscribe will merge
     *
     * @param closure
     */
    fun subscribeWillMerge(closure: ((publisherId: String, valueCmd: String?, newValue: Map<String, Any>, oldValue: Map<String, Any>) -> AUICollectionBroadException?)?)

    /**
     * Subscribe will remove
     *
     * @param closure
     */
    fun subscribeWillRemove(closure: ((publisherId: String, valueCmd: String?, value: Map<String, Any>) -> AUICollectionBroadException?)?)

    /**
     * Subscribe will calculate
     *
     * @param closure
     */
    fun subscribeWillCalculate(closure: ((publisherId: String, valueCmd: String?, value: Map<String, Any>, cKey: List<String>, cValue: Int, cMin: Int, cMax: Int) -> AUICollectionBroadException?)?)

    /**
     * Subscribe attributes did changed
     *
     * @param closure
     */
    fun subscribeAttributesDidChanged(closure: ((channelName: String, observeKey: String, value: AUIAttributesModel) -> Unit)?)

    /**
     * Subscribe attributes will set
     *
     * @param closure
     */
    fun subscribeAttributesWillSet(closure: ((channelName: String, observeKey: String, valueCmd: String?, value: AUIAttributesModel) -> Unit)?)

    fun subscribeValueWillChange(closure: ((publisherId: String, valueCmd: String?, value: Map<String, Any>) -> Map<String, Any>?)?)

    /**
     * Get meta data
     *
     * @param callback
     */
    fun getMetaData(callback: ((error: AUICollectionBroadException?, value: Any?) -> Unit)?)

    /**
     * Update meta data
     *
     * @param valueCmd
     * @param value
     * @param filter
     * @param callback
     */
    fun updateMetaData(
        valueCmd: String?,
        value: Map<String, Any>,
        filter: List<Map<String, Any>>? = null,
        callback: ((error: AUICollectionBroadException?) -> Unit)?
    )

    /**
     * Merge meta data
     *
     * @param valueCmd
     * @param value
     * @param filter
     * @param callback
     */
    fun mergeMetaData(
        valueCmd: String?,
        value: Map<String, Any>,
        filter: List<Map<String, Any>>? = null,
        callback: ((error: AUICollectionBroadException?) -> Unit)?
    )

    /**
     * Add meta data
     *
     * @param valueCmd
     * @param value
     * @param filter
     * @param callback
     */
    fun addMetaData(
        valueCmd: String?,
        value: Map<String, Any>,
        filter: List<Map<String, Any>>? = null,
        callback: ((error: AUICollectionBroadException?) -> Unit)?
    )

    /**
     * Remove meta data
     *
     * @param valueCmd
     * @param filter
     * @param callback
     */
    fun removeMetaData(
        valueCmd: String?,
        filter: List<Map<String, Any>>? = null,
        callback: ((error: AUICollectionBroadException?) -> Unit)?
    )

    /**
     * Calculate meta data
     *
     * @param valueCmd
     * @param key
     * @param value
     * @param min
     * @param max
     * @param filter
     * @param callback
     */
    fun calculateMetaData(
        valueCmd: String?,
        key: List<String>,
        value: Int,
        min: Int,
        max: Int,
        filter: List<Map<String, Any>>? = null,
        callback: ((error: AUICollectionBroadException?) -> Unit)?
    )

    /**
     * Clean meta data
     *
     * @param callback
     */
    fun cleanMetaData(callback: ((error: AUICollectionBroadException?) -> Unit)?)

    /**
     * Release
     *
     */
    fun release()
}