package com.cboda.baselibrary.bean

import com.cboda.baselibrary.utils.MMKVBroadUtils


object GlobalBroadInfo {
    var myFriendListBean = HashMap<String, GlobalFriendBroadBean>()
    var myGroupListData = HashMap<String, GlobalSmallGroupBroadBean>()

    fun queryMyFriendInfo(targetId: String): GlobalFriendBroadBean? {
        if (myFriendListBean.isEmpty()) {
            myFriendListBean = MMKVBroadUtils.getFriendListData()
        }
        return myFriendListBean[targetId]
    }

    fun queryGroupInfo(targetId: String): GlobalSmallGroupBroadBean? {
        if (myGroupListData.isEmpty()) {
            myGroupListData = MMKVBroadUtils.getGroupListData()
        }
        return myGroupListData[targetId]
    }
}