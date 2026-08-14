package com.cboda.baselibrary.network.model

/**
 * 网络请求列表响应。
 * 兼容旧协议 `data` / `dataList`，以及 IM 统一信封 `content` 为数组。
 */
class NetworkListBroadResponse2<T> : NetworkBroadResponse<List<T>> {

    var totalCount: Int = 0
    var totalPage: Int = 0
    var unreadCount: Int = 0
    var dataList: List<T>? = null

    constructor() : super()

    constructor(
        code: Int = 0,
        msg: String? = null,
        success: Boolean = false,
        data: List<T>? = null,
        totalCount: Int = 0,
        totalPage: Int = 0,
        unreadCount: Int = 0,
        dataList: List<T>? = null,
    ) : super(code, msg, success, data) {
        this.totalCount = totalCount
        this.totalPage = totalPage
        this.unreadCount = unreadCount
        this.dataList = dataList
    }

    /**
     * 列表业务数据：`dataList` > `data` > `content`
     */
    fun getList(): List<T>? {
        return dataList ?: getPayload()
    }

    fun getMyDataList(): List<T>? = dataList

    fun hasListData(): Boolean = !getList().isNullOrEmpty()

    fun getListSize(): Int = getList()?.size ?: 0

    fun hasMoreData(currentPage: Int): Boolean = currentPage < totalPage

    override fun toString(): String {
        return "NetworkListBroadResponse2(totalCount=$totalCount, totalPage=$totalPage, " +
                "code=$code, errorCode=$errorCode, msg='$msg', success=$success, " +
                "listSize=${getListSize()}, data=$data, content=$content, dataList=$dataList)"
    }
}
