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
import com.cboda.baselibrary.utils.MMKVBroadUtils
import com.cboda.baselibrary.utils.RealmUtil
import com.cboda.baselibrary.utils.eLogD
import com.cboda.baselibrary.utils.fromJson2
import com.cboda.baselibrary.utils.fromListJson2
import com.cboda.baselibrary.utils.toJson2
import com.cboda.baselibrary.utils.toLog

object E {

    fun getUserName(userId: String, groupId: String, refresh: Boolean = false, isGroup: Boolean = false, callBack: (String) -> Unit) {
        //   缓存userInfo
//        eLogD(APP_TAG,"userId==$userId---groupId==$groupId,")
        var displayName: String? = ""
        var displayName2 = ""
//        var displayName3: String? = ""
//        var displayName5 = ""
//        if (isGroup && !TextUtils.isEmpty(groupId) && !TextUtils.isEmpty(userId)) {
//            val userGroupBean = UserGroupBean()
//            userGroupBean.groupId = groupId
//            userGroupBean.userId = userId
//            RealmUtil.findUserGroupData(userGroupBean) { getUserGroupBean ->
//                getUserGroupBean?.let {
//                    if (null != getUserGroupBean.groupNickName && !TextUtils.isEmpty(getUserGroupBean.groupNickName)) {
//                        displayName3 = getUserGroupBean.groupNickName
//                    }
//                }
//                val userInfo = ChatManager.Instance().getUserInfo(userId, groupId, refresh)
//                if (userInfo != null) {
//                    if (!TextUtils.isEmpty(userInfo.groupAlias)) {
//                        displayName5 = userInfo.groupAlias
//                    } else if (!TextUtils.isEmpty(userInfo.friendAlias)) {
//                        displayName5 = userInfo.friendAlias
//                    } else if (!TextUtils.isEmpty(userInfo.displayName)) {
//                        displayName5 = userInfo.displayName
//                    }
//                }
//            }
//        }
        RealmUtil.findUserData(userId) { friendBean ->
            friendBean?.let {
                if (null != friendBean.remarkName && !TextUtils.isEmpty(friendBean.remarkName)) {
                    displayName = friendBean.remarkName
                }
                if (null != friendBean.nickName && !TextUtils.isEmpty(friendBean.nickName)) {
                    displayName2 = friendBean.nickName ?: ""
                }
            }
//            eLogD("EClass", "1:$displayName ,2:$displayName2 ,3:$displayName3 ,5:$displayName5")
            if (!TextUtils.isEmpty(displayName)) {
                callBack.invoke(displayName ?: "")
            } else {
                if (isGroup && !TextUtils.isEmpty(groupId) && !TextUtils.isEmpty(userId)) {
                    val userGroupBean = UserGroupBean()
                    userGroupBean.groupId = groupId
                    userGroupBean.userId = userId
                    RealmUtil.findUserGroupData(userGroupBean) { getUserGroupBean ->
                        getUserGroupBean?.let {
                            if (null != getUserGroupBean.groupNickName && !TextUtils.isEmpty(getUserGroupBean.groupNickName)) {
                                displayName = getUserGroupBean.groupNickName
                            }
                        }
                        if (!TextUtils.isEmpty(displayName)) {
                            callBack.invoke(displayName ?: "")
                        } else {
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
                                callBack.invoke(if (!TextUtils.isEmpty(displayName2)) displayName2 else "<$userId>")
                            } else {
                                callBack.invoke(displayName ?: "")
                            }
                        }
                    }
                } else {
                    callBack.invoke(if (!TextUtils.isEmpty(displayName2)) displayName2 else "<$userId>")
                }
            }


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
                        val groupList = fromListJson2(toJson2(list), GroupUserBean::class.java)
                        RealmUtil.addUserGroupData(groupId, groupList)
                        RealmUtil.addUserData2(groupList)
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

    fun getMapUserData(uIdMap: HashMap<String, ArrayList<String>>) {
        if (uIdMap.isNotEmpty()) {
            val uIdList = arrayListOf<UIDListBean>()
            uIdMap.forEach { (groupId, list) ->
                if (list.isNotEmpty() && !TextUtils.isEmpty(groupId)) {
                    val uIDListBean = UIDListBean()
                    uIDListBean.groupId = groupId
                    uIDListBean.uidList = list
                    uIdList.add(uIDListBean)
                }
            }
            if (uIdList.isNotEmpty()) {
                getUIDListData(uIdList, 0)
            }
        }
    }


    private fun getUIDListData(uIdList: List<UIDListBean>, listPos: Int) {
        if (uIdList.size > listPos) {
            val uIDListBean = uIdList[listPos]
            val uids = uIDListBean.uidList
            if (uids.size <= 1) {
                val userGroupBean = UserGroupBean()
                userGroupBean.userId = uids[0]
                userGroupBean.groupId = uIDListBean.groupId
                getDetailUserData(userGroupBean) {
                    getUIDListData(uIdList, listPos + 1)
                }
            } else {
                getListUserData(uIDListBean.groupId, uids) {
                    getUIDListData(uIdList, listPos + 1)
                }
            }

        }
    }

    private fun getListUserData(groupId: String?, uidList: List<String?>?, callBack: (Boolean) -> Unit = {}) {
        if (!TextUtils.isEmpty(groupId) && !uidList.isNullOrEmpty()) {
            val uids = uidList.toTypedArray()
            val map = mapOf("id" to groupId, "uids" to uids)
            postEncryptedList(
                "api/groupUsersInfoBatch",
                map,
                object : NetworkBroadUtils.ListCallback<GroupUserBean> {
                    override fun onSuccess(list: List<GroupUserBean>?, totalCount: Int, totalPage: Int) {
                        //                        eLogD("getListUserDataClass", "success:${toJson2(list)}")
                        val groupList = fromListJson2(toJson2(list), GroupUserBean::class.java)
                        RealmUtil.addUserGroupData(groupId, groupList)
                        RealmUtil.addUserData2(groupList)
                        callBack.invoke(true)
                    }

                    override fun onFailure(code: Int, message: String) {
//                        eLogD("getListUserDataClass", "onFailure:$code ,$message")
                        callBack.invoke(false)
                    }

                    override fun onError(throwable: Throwable) {
//                        eLogD("getListUserDataClass", "onError:${throwable.toLog("groupMeUserClass")}")
                        callBack.invoke(false)
                    }

                })
        } else {
            callBack.invoke(false)
        }
    }

    fun getListUserDataNotCall(groupId: String?, uidList: List<String?>?) {
        if (!TextUtils.isEmpty(groupId) && !uidList.isNullOrEmpty()) {
            if (uidList.size <= 1) {
                val userGroupBean = UserGroupBean()
                userGroupBean.userId = uidList[0] ?: ""
                userGroupBean.groupId = groupId ?: ""
                getDetailUserData(userGroupBean)
            } else {
                getListUserData(groupId, uidList)
            }

//            val uids = uidList.filterNotNull().filter { it.isNotBlank() && !it.contains(MMKVBroadUtils.getKeFuId()) && !it.contains(MMKVBroadUtils.getKeFuId()) }.distinct().toTypedArray()
//            val map = mapOf("id" to groupId, "uids" to uids)
//            postEncryptedList(
//                "api/groupUsersInfoBatch",
//                map,
//                object : NetworkBroadUtils.ListCallback<GroupUserBean> {
//                    override fun onSuccess(list: List<GroupUserBean>?, totalCount: Int, totalPage: Int) {
//                        //                        eLogD("getListUserDataClass", "success:${toJson2(list)}")
//                        val groupList = fromListJson2(toJson2(list), GroupUserBean::class.java)
//                        RealmUtil.addUserGroupData(groupId, groupList)
//                        RealmUtil.addUserData2(groupList)
//                    }
//
//                    override fun onFailure(code: Int, message: String) {
////                        eLogD("getListUserDataClass", "onFailure:$code ,$message")
//                    }
//
//                    override fun onError(throwable: Throwable) {
////                        eLogD("getListUserDataClass", "onError:${throwable.toLog("groupMeUserClass")}")
//                    }
//
//                })
        }
    }


    fun getDetailUserData(
        userGroupBean: UserGroupBean,
        callBack: (bean: GroupUserBean?) -> Unit = {}
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

    class UIDListBean {
        var groupId = ""
        var uidList = arrayListOf<String>()
    }

}