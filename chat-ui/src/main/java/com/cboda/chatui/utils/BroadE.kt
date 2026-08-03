package com.cboda.chatui.utils

import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.widget.ImageView
import cn.wildfirechat.model.GroupMember
import cn.wildfirechat.model.UserInfo
import cn.wildfirechat.remote.ChatManager
import cn.wildfirechat.remote.GetGroupMembersCallback
import com.cboda.baselibrary.bean.FriendBean
import com.cboda.baselibrary.bean.GlobalBroadInfo
import com.cboda.baselibrary.bean.GroupUserBean
import com.cboda.baselibrary.bean.UserGroupBean
import com.cboda.baselibrary.common.UrlBroadConstants
import com.cboda.baselibrary.network.NetworkBroadUtils
import com.cboda.baselibrary.network.NetworkBroadUtils.postEncrypted
import com.cboda.baselibrary.network.NetworkBroadUtils.postEncryptedList
import com.cboda.baselibrary.utils.MMKVBroadUtils
import com.cboda.baselibrary.utils.MMKVBroadUtils.Companion.getGroupDateListData
import com.cboda.baselibrary.utils.MMKVBroadUtils.Companion.getGroupSaveDateData
import com.cboda.baselibrary.utils.RealmUtil
import com.cboda.baselibrary.utils.RealmUtil.vagueFindGroup
import com.cboda.baselibrary.utils.TimeBroadBroadUtils.getNowTime
import com.cboda.baselibrary.utils.TimeBroadBroadUtils.getNowTimeHoursDifference
import com.cboda.baselibrary.utils.eLogD
import com.cboda.baselibrary.utils.fromJson2
import com.cboda.baselibrary.utils.fromListJson2
import com.cboda.baselibrary.utils.getIsDebug
import com.cboda.baselibrary.utils.loadImg
import com.cboda.baselibrary.utils.toJson2
import com.cboda.chatui.ChatManagerBroadHolder
import com.cboda.chatui.R
import com.cboda.chatui.conversation.mention.MentionBroadSpan
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.isNotEmpty

object E {
    private const val MAX_SAVE_TIME = 24L
    //加载群头像（portrait：头像url）
    fun loadGroupImg(imageView: ImageView?, groupId: String?, portrait: String? = null) {
        imageView?.loadGroupImg2(portrait)
        //自研已不需要下面的头像处理逻辑
//        imageView?.let {
//            if (null != groupId && !TextUtils.isEmpty(groupId)) {
//                GlobalBroadInfo.getGroupData(null) { stringGlobalSmallGroupBroadBeanHashMap ->
//                    val globalSmallGroupBroadBean = stringGlobalSmallGroupBroadBeanHashMap[groupId]
//                    if (null != globalSmallGroupBroadBean && !TextUtils.isEmpty(globalSmallGroupBroadBean.icon)) {
//                        imageView.loadGroupImg2(globalSmallGroupBroadBean.icon)
//                    } else if (null != portrait && !TextUtils.isEmpty(portrait) && !portrait.startsWith("file://")) {
//                        imageView.loadGroupImg2(portrait)
//                    } else {
//                        val groupInfo = ChatManager.Instance().getGroupInfo(groupId, false)
//                        val getPortrait = groupInfo?.portrait
//                        if (null != getPortrait && !TextUtils.isEmpty(getPortrait) && !getPortrait.startsWith("file://")) {
//                            imageView.loadGroupImg2(getPortrait)
//                        } else {
//                            imageView.loadGroupImg2()
//                            ChatManagerBroadHolder.gChatManager.getGroupMembers(groupId, false, object : GetGroupMembersCallback {
//                                override fun onSuccess(groupMembers: List<GroupMember?>?) {
//                                    groupMembers?.let {
//                                        if (groupMembers.isNotEmpty()) {
//                                            val memberIds = arrayListOf<String>()
//                                            groupMembers.forEach { item ->
//                                                //   // 获取最多30个成员的用户ID
//                                                if (null != item && !TextUtils.isEmpty(item.memberId) && memberIds.size < 30) {
//                                                    memberIds.add(item.memberId)
//                                                }
//                                            }
//                                            // 获取用户信息（包括头像）
//                                            val userInfos = ChatManagerBroadHolder.gChatManager.getUserInfos(memberIds, groupId)
//                                            if (null != userInfos && userInfos.isNotEmpty()) {
//                                                memberIds.clear()
//                                                userInfos.forEach { item ->
//                                                    // 获取最多9个成员的用户头像
//                                                    if (null != item && !TextUtils.isEmpty(item.portrait) && memberIds.size < 9) {
//                                                        memberIds.add(item.portrait)
//                                                    } else if (null != item && !TextUtils.isEmpty(item.uid) && memberIds.size < 9) {
//                                                        RealmUtil.findUserData(item.uid) { globalFriendBean ->
//                                                            if (null != globalFriendBean && null != globalFriendBean.userImg && !TextUtils.isEmpty(globalFriendBean.userImg)) {
//                                                                memberIds.add(globalFriendBean.userImg ?: "")
//                                                            }
//                                                        }
//                                                    }
//                                                }
//                                                GroupAvatarBroadLoader.load(imageView.context, memberIds, imageView, groupId)
//                                            }
//                                        }
//                                    }
//
//                                }
//
//                                override fun onFail(errorCode: Int) {
//                                    imageView.loadGroupImg2()
//                                }
//                            })
//                        }
//                    }
//                }
//            } else {
//                imageView.loadGroupImg2(portrait)
//            }
//        }
    }

    private fun ImageView.loadGroupImg2(url: String? = "") {
        this.loadImg(url, R.mipmap.ic_group_broad_chat)
    }

    fun getUserName(userId: String, groupId: String, refresh: Boolean = false, isGroup: Boolean = false, callBack: (String) -> Unit) {
        //   缓存userInfo
        var displayName: String? = ""
        var displayName2 = ""
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


//    private SpannableString mentionSpannable(UserInfo userInfo) {
//
//        String text = "@" + ChatManager.Instance().getGroupMemberDisplayName2(userInfo, conversationBroad.type == Conversation.ConversationType.Group ? conversationBroad.target : "") + " ";
//        SpannableString spannableString = new SpannableString(text);
//        spannableString.setSpan(new MentionBroadSpan(userInfo.uid), 0, spannableString.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
//        return spannableString;
//    }

    fun mentionImSpannable(userInfo: UserInfo?, groupId: String, callBack: (SpannableString) -> Unit) {
        getGroupMemberDisplayName(userInfo, groupId) { userName ->
            val spannableString = SpannableString("@$userName ")
            userInfo?.let {
                spannableString.setSpan(MentionBroadSpan(userInfo.uid), 0, spannableString.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
            }
            callBack.invoke(spannableString)
        }
    }

    private fun getGroupMemberDisplayName(userInfo: UserInfo?, groupId: String, callBack: (String) -> Unit) {
        userInfo?.let {
            if (!TextUtils.isEmpty(groupId)) {
                val userGroupBean = UserGroupBean()
                userGroupBean.groupId = groupId
                userGroupBean.userId = userInfo.uid
                RealmUtil.findUserGroupData(userGroupBean) { getUserGroupBean ->
                    if (null != getUserGroupBean && null != getUserGroupBean.groupNickName && !TextUtils.isEmpty(getUserGroupBean.groupNickName)) {
                        callBack.invoke(getUserGroupBean.groupNickName ?: "")
                    } else {
                        callBack.invoke(ChatManager.Instance().getGroupMemberDisplayName(userInfo))
                    }
                }
            } else {
                callBack.invoke(ChatManager.Instance().getGroupMemberDisplayName(userInfo))
            }
        } ?: callBack.invoke("")
    }

    @Deprecated("废弃")
    fun getAllListUserData(groupId: String?) {
        if (!TextUtils.isEmpty(groupId)) {
            val saveMap = getGroupDateListData()
            val saveTime = getGroupSaveDateData(groupId, saveMap)
            if (groupId == "b8121c31a75f4fdaafcf87392a7908c8" || groupId == "6bbedbc73b0f46d7891dbeacba250614") {
                vagueFindGroup(groupId) { boolean ->
                    if (!boolean) {
                        getAllListUserData(groupId, saveMap)
                    }
                }
            } else {
                //距离上次调用获取群成员数据接口24小时后才能再次调接口
                if (null != saveTime && getNowTimeHoursDifference(saveTime) < MAX_SAVE_TIME) {
                    return
                }
                getAllListUserData(groupId, saveMap)
            }
        }
    }

    @Deprecated("废弃")
    private fun getAllListUserData(groupId: String?, saveMap: HashMap<String, Long>) {
        val map = mapOf("id" to groupId)
        postEncryptedList(
            "api/groupUserList", map, object : NetworkBroadUtils.ListCallback<Any> {
                override fun onSuccess(list: List<Any>?, totalCount: Int, totalPage: Int) {
//                        eLogD("getListUserDataClass", "success:${toJson2(list)}")
                    val groupList = fromListJson2(toJson2(list), GroupUserBean::class.java)
                    RealmUtil.addUserGroupData(groupId, groupList)
                    RealmUtil.addUserData2(groupList)
                    saveMap[groupId ?: ""] = getNowTime()
                    MMKVBroadUtils.saveGroupDate(saveMap)
                }

                override fun onFailure(code: Int, message: String) {
//                        eLogD("getListUserDataClass", "onFailure:$code ,$message")
                }

                override fun onError(throwable: Throwable) {
//                        eLogD("getListUserDataClass", "onError:${throwable.toLog("groupMeUserClass")}")
                }

            })
    }

    fun getMapUserData(uIdMap: HashMap<String, HashMap<String, String>>) {
        if (uIdMap.isNotEmpty()) {
            val uIdList = arrayListOf<UIDListBean>()
            uIdMap.forEach { (groupId, item) ->
                if (item.isNotEmpty() && !TextUtils.isEmpty(groupId)) {
                    val uIDListBean = UIDListBean()
                    uIDListBean.groupId = groupId
                    val list = arrayListOf<String>()
                    item.forEach { (_, value) ->
                        if (!TextUtils.isEmpty(value)) {
                            val userGroupBean = UserGroupBean()
                            userGroupBean.groupId = groupId
                            userGroupBean.userId = value
                            RealmUtil.findUserGroupData(userGroupBean) { returnUserGroupBean ->
                                //距离上次调用获取群成员数据接口24小时后才能再次调接口
                                if (null != returnUserGroupBean && returnUserGroupBean.joinTime != 0L && getNowTimeHoursDifference(returnUserGroupBean.joinTime ?: 0L) < MAX_SAVE_TIME) {
                                } else {
                                    list.add(value)
                                }
                            }
                        }
                    }
                    if (list.isNotEmpty()) {
                        uIDListBean.uidList = list
                        uIdList.add(uIDListBean)
                    }
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
            if (uids.isNotEmpty()) {
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
            } else {
                getUIDListData(uIdList, listPos + 1)
            }
        }
    }

    private fun getListUserData(groupId: String?, uidList: List<String?>?, callBack: (Boolean) -> Unit = {}) {
        if (!TextUtils.isEmpty(groupId) && !uidList.isNullOrEmpty()) {
            val uids = uidList.toTypedArray()
            val map = mapOf("id" to groupId, "uids" to uids)
            postEncryptedList(
                "api/groupUsersInfoBatch", map, object : NetworkBroadUtils.ListCallback<GroupUserBean> {
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

    fun getListUserDataNotCall(groupId: String?, uidList: HashMap<String, String>) {
        if (!TextUtils.isEmpty(groupId) && uidList.isNotEmpty()) {
            val list = arrayListOf<String>()
            uidList.forEach { (_, value) ->
                if (!TextUtils.isEmpty(value)) {
                    list.add(value)
                }
            }
            if (list.isNotEmpty()) {
                if (list.size <= 1) {
                    val userGroupBean = UserGroupBean()
                    userGroupBean.userId = list[0]
                    userGroupBean.groupId = groupId ?: ""
                    getDetailUserData(userGroupBean)
                } else {
                    getListUserData(groupId, list)
                }
            }
        }
    }


    fun getDetailUserData(
        userGroupBean: UserGroupBean, callBack: (bean: GroupUserBean?) -> Unit = {}
    ) {
        if (!TextUtils.isEmpty(userGroupBean.groupId) && !TextUtils.isEmpty(userGroupBean.userId)) {
            val map = mapOf("id" to userGroupBean.groupId, "uid" to userGroupBean.userId)
            postEncrypted(
                UrlBroadConstants.GROUP_ME_USER, map, object : NetworkBroadUtils.NetworkCallback<Any> {
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