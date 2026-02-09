package com.cboda.baselibrary.bean

import com.cboda.baselibrary.utils.MMKVBroadUtils


object GlobalBroadInfo {
    var myFriendListBean = HashMap<String, GlobalFriendBroadBean>()
    var myGroupListData = HashMap<String, GlobalSmallGroupBroadBean>()

    fun getFriendData(): HashMap<String, GlobalFriendBroadBean> {
        if (myFriendListBean.isEmpty()) {
            myFriendListBean = MMKVBroadUtils.getFriendListData()
        }
        return myFriendListBean
    }

    fun getGroupData(): HashMap<String, GlobalSmallGroupBroadBean> {
        if (myGroupListData.isEmpty()) {
            myGroupListData = MMKVBroadUtils.getGroupListData()
        }
        return myGroupListData
    }

    fun queryMyFriendInfo(targetId: String): GlobalFriendBroadBean? {
        return getFriendData()[targetId]
    }

    fun queryGroupInfo(targetId: String): GlobalSmallGroupBroadBean? {
        return getGroupData()[targetId]
    }
}