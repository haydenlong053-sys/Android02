package com.cboda.baselibrary.bean

import com.cboda.baselibrary.utils.MMKVBroadUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


object GlobalBroadInfo {
    var myFriendListBean = HashMap<String, GlobalFriendBroadBean>()
    var myGroupListData = HashMap<String, GlobalSmallGroupBroadBean>()

    fun getFriendData(viewModelScope: CoroutineScope? = null, callBack: (HashMap<String, GlobalFriendBroadBean>) -> Unit) {
        if (myFriendListBean.isEmpty()) {
            viewModelScope?.launch {
                withContext(Dispatchers.IO) {
                    myFriendListBean = MMKVBroadUtils.getFriendListData()
                    withContext(Dispatchers.Main) {
                        callBack.invoke(myFriendListBean)
                    }
                }
            } ?: let {
                GlobalScope.launch(Dispatchers.IO) {
                    myFriendListBean = MMKVBroadUtils.getFriendListData()
                    GlobalScope.launch(Dispatchers.Main) {
                        callBack.invoke(myFriendListBean)
                    }
                }
            }
        } else {
            callBack.invoke(myFriendListBean)
        }
    }

    fun getGroupData(viewModelScope: CoroutineScope? = null, callBack: (HashMap<String, GlobalSmallGroupBroadBean>) -> Unit) {
        if (myGroupListData.isEmpty()) {
            viewModelScope?.launch {
                withContext(Dispatchers.IO) {
                    myGroupListData = MMKVBroadUtils.getGroupListData()
                    withContext(Dispatchers.Main) {
                        callBack.invoke(myGroupListData)
                    }
                }
            } ?: let {
                GlobalScope.launch(Dispatchers.IO) {
                    myGroupListData = MMKVBroadUtils.getGroupListData()
                    GlobalScope.launch(Dispatchers.Main) {
                        callBack.invoke(myGroupListData)
                    }
                }
            }
        } else {
            callBack.invoke(myGroupListData)
        }
    }

    fun queryMyFriendInfo(targetId: String, viewModelScope: CoroutineScope? = null, callBack: (GlobalFriendBroadBean?) -> Unit) {
        getFriendData(viewModelScope) {
            callBack.invoke(it[targetId])
        }
    }

    fun queryGroupInfo(targetId: String, viewModelScope: CoroutineScope? = null, callBack: (GlobalSmallGroupBroadBean?) -> Unit) {
        getGroupData(viewModelScope) {
            callBack.invoke(it[targetId])
        }
    }
}