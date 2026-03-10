package com.cboda.chatui.utils

import android.text.TextUtils
import cn.wildfirechat.remote.ChatManager
import com.blankj.utilcode.util.GsonUtils
import com.cboda.baselibrary.bean.CommonDataListBroadBean
import com.cboda.baselibrary.bean.GlobalBroadInfo.queryMyFriendInfo
import com.cboda.baselibrary.bean.GlobalFriendBroadBean
import com.cboda.baselibrary.common.UrlBroadConstants
import com.cboda.baselibrary.network.NetworkBroadUtils
import com.cboda.baselibrary.network.NetworkBroadUtils.postEncryptedList
import com.cboda.baselibrary.utils.MMKVBroadUtils.Companion.getRealUserId
import com.google.gson.reflect.TypeToken

object E {
    fun getUserName(userId: String, groupId: String,callBack:(String)-> Unit) {
        // TODO optimize 缓存userInfo吧
        var displayName: String? = ""
        val userInfo = ChatManager.Instance().getUserInfo(userId, groupId, false)
        if (userInfo != null) {
            if (!TextUtils.isEmpty(userInfo.groupAlias)) {
                displayName = userInfo.groupAlias
            } else if (!TextUtils.isEmpty(userInfo.friendAlias)) {
                displayName = userInfo.friendAlias
            } else if (!TextUtils.isEmpty(userInfo.displayName)) {
                displayName = userInfo.displayName
            }
        }
        if (TextUtils.isEmpty(displayName)) {
            displayName = "<$userId>"
            queryMyFriendInfo(userId, null) { globalFriendBean: GlobalFriendBroadBean? ->
                if (null != globalFriendBean) {
                    if (!TextUtils.isEmpty(globalFriendBean.remarkName)) {
                        displayName = globalFriendBean.remarkName
                    } else if (!TextUtils.isEmpty(globalFriendBean.nickName)) {
                        displayName = globalFriendBean.nickName
                    }
                }
                callBack.invoke(displayName?:"")
            }
        } else {
            callBack.invoke(displayName?:"")
        }
    }

    fun getDetailUserData(
        groupId: String?,
        callBack: (bean: CommonDataListBroadBean.CommonBean?) -> Unit
    ) {
        val map = mapOf("id" to groupId)
        postEncryptedList(
            UrlBroadConstants.GROUP_USER_LIST,
            map,
            object : NetworkBroadUtils.ListCallback<CommonDataListBroadBean.CommonBean> {
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