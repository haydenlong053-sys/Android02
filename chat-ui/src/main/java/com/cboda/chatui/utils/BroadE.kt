package com.cboda.chatui.utils

import android.text.TextUtils
import cn.wildfirechat.remote.ChatManager
import com.blankj.utilcode.util.GsonUtils
import com.cboda.baselibrary.bean.CommonDataListBroadBean
import com.cboda.baselibrary.bean.GlobalBroadInfo.queryMyFriendInfo
import com.cboda.baselibrary.bean.GlobalFriendBroadBean
import com.cboda.baselibrary.bean.GroupUserBean
import com.cboda.baselibrary.bean.UserInfoBean
import com.cboda.baselibrary.common.UrlBroadConstants
import com.cboda.baselibrary.network.NetworkBroadUtils
import com.cboda.baselibrary.network.NetworkBroadUtils.postEncrypted
import com.cboda.baselibrary.network.NetworkBroadUtils.postEncryptedList
import com.cboda.baselibrary.utils.APP_TAG
import com.cboda.baselibrary.utils.MMKVBroadUtils
import com.cboda.baselibrary.utils.MMKVBroadUtils.Companion.getRealUserId
import com.cboda.baselibrary.utils.eLogD
import com.cboda.baselibrary.utils.toJson2
import com.cboda.baselibrary.utils.toLog
import com.google.gson.reflect.TypeToken

object E {

    fun getUserName(userId: String, groupId: String, refresh: Boolean = false, callBack: (String) -> Unit) {
        // TODO optimize 缓存userInfo吧
//        eLogD(APP_TAG,"userId==$userId---groupId==$groupId,")
        var displayName: String? = ""
        val userInfo = ChatManager.Instance().getUserInfo(userId, groupId, refresh)
        if (userInfo != null) {
//            eLogD("BroadEClass", "groupAlias:${userInfo.groupAlias} ,friendAlias:${userInfo.friendAlias} ,displayName:${userInfo.displayName}")
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
                callBack.invoke(displayName ?: "")
            }
        } else {
            callBack.invoke(displayName ?: "")
        }
    }

    fun getListUserData(groupId: String?, uidList: List<String>?) {
        if (!TextUtils.isEmpty(groupId) && !uidList.isNullOrEmpty()) {
            val uids = uidList.toTypedArray()
            val map = mapOf("id" to groupId, "uids" to uids)
            postEncryptedList(
                "api/groupUsersInfoBatch",
                map,
                object : NetworkBroadUtils.ListCallback<GroupUserBean> {
//                override fun onError(throwable: Throwable) {
//                    callBack.invoke(null)
//                }
//
//                override fun onFailure(code: Int, message: String) {
//                    callBack.invoke(null)
//                }

                    //                override fun onSuccess(
//                    list: List<CommonDataListBroadBean.CommonBean>?,
//                    totalCount: Int,
//                    totalPage: Int
//                ) {
//                    list?.also {
//                        val gson = GsonUtils.getGson()
//                        val jsonStr = gson.toJson(list)
//                        val userList = gson.fromJson<MutableList<CommonDataListBroadBean.CommonBean>?>(
//                            jsonStr,
//                            object :
//                                TypeToken<MutableList<CommonDataListBroadBean.CommonBean>?>() {}.type
//                        )
//                        userList?.also {
//                            var commonBean: CommonDataListBroadBean.CommonBean? = null
//                            for (bean in userList) {
//                                if (bean.userId == getRealUserId()) {
//                                    commonBean = bean
//                                    break
//                                }
//                            }
//                            callBack.invoke(commonBean)
//                        } ?: also {
//                            callBack.invoke(null)
//                        }
//                    } ?: also {
//                        callBack.invoke(null)
//                    }
//                }
//                    override fun onSuccess(data: GroupUserBean?) {
//                        eLogD("getListUserDataClass", "success:${toJson2(data)}")
//                    }
//
//                    override fun onFailure(code: Int, message: String, data: GroupUserBean?) {
//                        eLogD("groupMeUserClass", "onFailure:$code ,$message")
//                    }
//
//                    override fun onError(throwable: Throwable) {
//                        eLogD("groupMeUserClass", "onError:${throwable.toLog("groupMeUserClass")}")
//                    }
                    override fun onSuccess(list: List<GroupUserBean>?, totalCount: Int, totalPage: Int) {
                        eLogD("getListUserDataClass", "success:${toJson2(list)}")
                    }

                    override fun onFailure(code: Int, message: String) {
                        eLogD("getListUserDataClass", "onFailure:$code ,$message")
                    }

                    override fun onError(throwable: Throwable) {
                        eLogD("getListUserDataClass", "onError:${throwable.toLog("groupMeUserClass")}")
                    }

                })
        }
    }


    fun getDetailUserData(
        groupId: String?,
        callBack: (bean: GroupUserBean?) -> Unit
    ) {
        if (!TextUtils.isEmpty(groupId)) {
            val map = mapOf("id" to groupId, "uid" to getRealUserId())
            postEncrypted(
                UrlBroadConstants.GROUP_ME_USER,
                map,
                object : NetworkBroadUtils.NetworkCallback<GroupUserBean> {
//                override fun onError(throwable: Throwable) {
//                    callBack.invoke(null)
//                }
//
//                override fun onFailure(code: Int, message: String) {
//                    callBack.invoke(null)
//                }

                    //                override fun onSuccess(
//                    list: List<CommonDataListBroadBean.CommonBean>?,
//                    totalCount: Int,
//                    totalPage: Int
//                ) {
//                    list?.also {
//                        val gson = GsonUtils.getGson()
//                        val jsonStr = gson.toJson(list)
//                        val userList = gson.fromJson<MutableList<CommonDataListBroadBean.CommonBean>?>(
//                            jsonStr,
//                            object :
//                                TypeToken<MutableList<CommonDataListBroadBean.CommonBean>?>() {}.type
//                        )
//                        userList?.also {
//                            var commonBean: CommonDataListBroadBean.CommonBean? = null
//                            for (bean in userList) {
//                                if (bean.userId == getRealUserId()) {
//                                    commonBean = bean
//                                    break
//                                }
//                            }
//                            callBack.invoke(commonBean)
//                        } ?: also {
//                            callBack.invoke(null)
//                        }
//                    } ?: also {
//                        callBack.invoke(null)
//                    }
//                }
                    override fun onSuccess(data: GroupUserBean?) {
                        eLogD("groupMeUserClass", "success:${toJson2(data)}")
                    }

                    override fun onFailure(code: Int, message: String, data: GroupUserBean?) {
                        eLogD("groupMeUserClass", "onFailure:$code ,$message")
                    }

                    override fun onError(throwable: Throwable) {
                        eLogD("groupMeUserClass", "onError:${throwable.toLog("groupMeUserClass")}")
                    }

                })
        } else {
            callBack.invoke(null)
        }
    }
}