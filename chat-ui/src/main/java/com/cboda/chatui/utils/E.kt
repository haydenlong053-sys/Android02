package com.cboda.chatui.utils

import com.cboda.baselibrary.bean.CommonDataListBroadBean
import com.cboda.baselibrary.common.UrlBroadConstants
import com.cboda.baselibrary.network.NetworkBroadUtils
import com.cboda.baselibrary.network.NetworkBroadUtils.postEncryptedList
import com.cboda.baselibrary.utils.MMKVBroadUtils.Companion.getRealUserId
import com.blankj.utilcode.util.GsonUtils
import com.google.gson.reflect.TypeToken

object E {
    fun getDetailUserData(
        groupId: String?,
        callBack: (bean: CommonDataListBroadBean.CommonBean?) -> Unit
    ) {
        val map = mapOf("id" to groupId)
        postEncryptedList(
            UrlBroadConstants.GROUP_USER_LIST,
            map,
            object : NetworkBroadUtils.ListBroadCallback<CommonDataListBroadBean.CommonBean> {
                override fun onError(throwable: Throwable) {
                    callBack.invoke(null)
                }

                override fun onFailure(code: Int, message: String) {
                    callBack.invoke(null)
                }

                override fun onSuccess(
                    list: List<CommonDataListBroadBean.CommonBean>?,
                    totalCount: Int,
                    totalPage: Int
                ) {
                    list?.also {
                        val gson = GsonUtils.getGson()
                        val jsonStr = gson.toJson(list)
                        val userList = gson.fromJson<MutableList<CommonDataListBroadBean.CommonBean>?>(
                            jsonStr,
                            object :
                                TypeToken<MutableList<CommonDataListBroadBean.CommonBean>?>() {}.type
                        )
                        userList?.also {
                            var commonBean: CommonDataListBroadBean.CommonBean? = null
                            for (bean in userList) {
                                if (bean.userId == getRealUserId()) {
                                    commonBean = bean
                                    break
                                }
                            }
                            callBack.invoke(commonBean)
                        } ?: also {
                            callBack.invoke(null)
                        }
                    } ?: also {
                        callBack.invoke(null)
                    }
                }
            })
    }
}