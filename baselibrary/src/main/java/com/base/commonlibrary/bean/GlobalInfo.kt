package com.base.commonlibrary.bean

import com.base.commonlibrary.utils.MMKVUtils


object GlobalInfo {
    var myFriendListBean = HashMap<String, GlobalFriendBean>()
    var myGroupListData = HashMap<String, GlobalSmallGroupBean>()

    fun queryMyFriendInfo(targetId: String): GlobalFriendBean? {
        if (myFriendListBean.isEmpty()) {
            myFriendListBean = MMKVUtils.getFriendListData()
        }
        return myFriendListBean[targetId]
    }

    fun queryGroupInfo(targetId: String): GlobalSmallGroupBean? {
        if (myGroupListData.isEmpty()) {
            myGroupListData = MMKVUtils.getGroupListData()
        }
        return myGroupListData[targetId]
    }
}