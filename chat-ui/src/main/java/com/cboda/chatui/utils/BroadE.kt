package com.cboda.chatui.utils

import android.text.TextUtils
import cn.wildfirechat.remote.ChatManager
import com.cboda.baselibrary.bean.FriendBean
import com.cboda.baselibrary.bean.GroupUserBean
import com.cboda.baselibrary.bean.UserGroupBean
import com.cboda.baselibrary.common.UrlBroadConstants
import com.cboda.baselibrary.network.NetworkBroadUtils
import com.cboda.baselibrary.network.NetworkBroadUtils.postEncrypted
import com.cboda.baselibrary.network.NetworkBroadUtils.postEncryptedList
import com.cboda.baselibrary.utils.RealmUtil
import com.cboda.baselibrary.utils.eLogD
import com.cboda.baselibrary.utils.fromJson2
import com.cboda.baselibrary.utils.fromListJson2
import com.cboda.baselibrary.utils.toJson2
import com.cboda.baselibrary.utils.toLog

object E {

    fun getUserName(userId: String, groupId: String, refresh: Boolean = false, callBack: (String) -> Unit) {
        //   缓存userInfo
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
            RealmUtil.findUserData(userId) { globalFriendBean ->
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



    fun getAllListUserData(groupId: String?) {
        if (!TextUtils.isEmpty(groupId)) {
            val map = mapOf("id" to groupId)
            postEncryptedList(
                "api/groupUserList",
                map,
                object : NetworkBroadUtils.ListCallback<Any> {
                    override fun onSuccess(list: List<Any>?, totalCount: Int, totalPage: Int) {
//                        eLogD("getListUserDataClass", "success:${toJson2(list)}")
                        RealmUtil.addUserGroupData(groupId, fromListJson2(toJson2(list), GroupUserBean::class.java))
                    }

                    override fun onFailure(code: Int, message: String) {
//                        eLogD("getListUserDataClass", "onFailure:$code ,$message")
                    }

                    override fun onError(throwable: Throwable) {
//                        eLogD("getListUserDataClass", "onError:${throwable.toLog("groupMeUserClass")}")
                    }

                })
        }
    }
    fun getListUserData(groupId: String?, uidList: List<String?>?) {
        if (!TextUtils.isEmpty(groupId) && !uidList.isNullOrEmpty()) {
            val uids = uidList.filterNotNull().filter { it.isNotBlank() }.distinct().toTypedArray()
            val map = mapOf("id" to groupId, "uids" to uids)
            postEncryptedList(
                "api/groupUsersInfoBatch",
                map,
                object : NetworkBroadUtils.ListCallback<GroupUserBean> {
                    override fun onSuccess(list: List<GroupUserBean>?, totalCount: Int, totalPage: Int) {
                        RealmUtil.addUserGroupData(groupId, fromListJson2(toJson2(list), GroupUserBean::class.java))
//                        eLogD("getListUserDataClass", "success:${toJson2(list)}")
                    }

                    override fun onFailure(code: Int, message: String) {
//                        eLogD("getListUserDataClass", "onFailure:$code ,$message")
                    }

                    override fun onError(throwable: Throwable) {
//                        eLogD("getListUserDataClass", "onError:${throwable.toLog("groupMeUserClass")}")
                    }

                })
        }
    }


    fun getDetailUserData(
        userGroupBean: UserGroupBean,
        callBack: (bean: GroupUserBean?) -> Unit
    ) {
        if (!TextUtils.isEmpty(userGroupBean.groupId) && !TextUtils.isEmpty(userGroupBean.userId)) {
            val map = mapOf("id" to userGroupBean.groupId, "uid" to userGroupBean.userId)
            postEncrypted(
                UrlBroadConstants.GROUP_ME_USER,
                map,
                object : NetworkBroadUtils.NetworkCallback<Any> {
                    override fun onSuccess(data: Any?) {
                        val groupUserBean = fromJson2(toJson2(data), GroupUserBean::class.java)
                        RealmUtil.addUserGroupData(userGroupBean, groupUserBean)
                        callBack.invoke(groupUserBean)
                    }

                    override fun onFailure(code: Int, message: String, data: Any?) {
//                        eLogD("groupMeUserClass", "onFailure:$code ,$message")
                        callBack.invoke(null)
                    }

                    override fun onError(throwable: Throwable) {
//                        eLogD("groupMeUserClass", "onError:${throwable.toLog("groupMeUserClass")}")
                        callBack.invoke(null)
                    }

                })
        } else {
            callBack.invoke(null)
        }
    }


    fun getUserAndGroupData(groupUserBean: GroupUserBean?, meUserGroupBean: UserGroupBean?, callBack: (friendBean: FriendBean?, userGroupBean: UserGroupBean?) -> Unit) {
        if (null == groupUserBean && null != meUserGroupBean) {
            RealmUtil.findUserGroupData(meUserGroupBean) { groupBean ->
                RealmUtil.findUserData(meUserGroupBean.userId) { userBean ->
                    callBack.invoke(userBean, groupBean)
                }
            }
        } else if (null != groupUserBean) {
            callBack.invoke(RealmUtil.getFriendBean(groupUserBean), RealmUtil.getUserGroupBean(meUserGroupBean, groupUserBean))
        }
    }
}