package com.base.commonlibrary.network.model

/**
 * 网络请求列表响应数据模型
 * 继承自NetworkResponse，增加分页信息
 *
 * @author CommonLibrary
 * @param T 列表数据类型
 */
class NetworkListResponse2<T>(
    code: Int = 0,
    msg: String? = null,
    success: Boolean = false,
    data: List<T>? = null,
    /**
     * 总记录数
     */
    var totalCount: Int = 0,

    /**
     * 总页数
     */
    var totalPage: Int = 0,
    var dataList: List<T>? = null,
) : NetworkResponse<List<T>>(code, msg, success, data) {

    /**
     * 获取列表数据
     * @return 列表数据，如果为空则返回null
     */
    fun getList(): List<T>? {
        return data
    }

    fun getMyDataList(): List<T>? {
        return dataList
    }


    /**
     * 判断是否有列表数据
     * @return true表示有数据，false表示无数据
     */
    fun hasListData(): Boolean {
        return !data.isNullOrEmpty()
    }

    /**
     * 获取列表数据大小
     * @return 数据大小，如果为空则返回0
     */
    fun getListSize(): Int {
        return data?.size ?: 0
    }

    /**
     * 判断是否还有更多数据（用于分页）
     * @param currentPage 当前页码（从1开始）
     * @return true表示还有更多数据，false表示没有更多数据
     */
    fun hasMoreData(currentPage: Int): Boolean {
        return currentPage < totalPage
    }

    override fun toString(): String {
        return "NetworkListResponse(totalCount=$totalCount, totalPage=$totalPage, " +
                "code=$code, msg='$msg', success=$success, listSize=${getListSize()})" + ",data=$data" + ",dataList=$dataList"
    }
}