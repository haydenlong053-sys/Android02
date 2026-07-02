package com.freddy.webtrc.utils

import android.text.TextUtils
import com.cboda.baselibrary.bean.FriendBean
import com.cboda.baselibrary.bean.GroupUserBean
import com.cboda.baselibrary.bean.UserGroupBean
import com.cboda.baselibrary.common.UrlBroadConstants
import com.cboda.baselibrary.network.NetworkBroadUtils
import com.cboda.baselibrary.network.NetworkBroadUtils.postEncrypted
import com.cboda.baselibrary.utils.RealmUtil
import com.cboda.baselibrary.utils.eLogD
import com.cboda.baselibrary.utils.getUuIdMd5
import com.cboda.baselibrary.utils.toJson2

object BroadE {
    fun getCallToken(
        id: String?, callBack: (bean: GroupUserBean?) -> Unit = {}
    ) {
        if (!TextUtils.isEmpty(id)) {
            RealmUtil.findUserData(id) { friendBean ->
                val map = mutableMapOf("identity" to id, "roomName" to getUuIdMd5(), "video" to false)
                if (!TextUtils.isEmpty(friendBean?.nickName)) {
                    map["name"] = friendBean?.nickName
                }
//                postEncrypted(
//                    UrlBroadConstants.GET_CALL_TOKEN, map, object : NetworkBroadUtils.NetworkCallback<Any> {
//                        override fun onSuccess(data: Any?) {
//                            eLogD("getCallToken_data", toJson2(data))
////                            val groupUserBean = fromJson2(toJson2(data), GroupUserBean::class.java)
////                            RealmUtil.addUserGroupData(userGroupBean, groupUserBean)
////                            callBack.invoke(groupUserBean)
//                        }
//
//                        override fun onFailure(code: Int, message: String, data: Any?) {
////                        eLogD("groupMeUserClass", "onFailure:$code ,$message")
//                            callBack.invoke(null)
//                        }
//
//                        override fun onError(throwable: Throwable) {
////                        eLogD("groupMeUserClass", "onError:${throwable.toLog("groupMeUserClass")}")
//                            callBack.invoke(null)
//                        }
//
//                    })
            }

        } else {
            callBack.invoke(null)
        }
    }
}