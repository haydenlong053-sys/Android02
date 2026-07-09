package com.freddy.webtrc.utils

import android.text.TextUtils
import com.cboda.baselibrary.utils.RealmUtil
import com.cboda.baselibrary.utils.eLogD
import com.cboda.baselibrary.utils.getUuIdMd5
import com.cboda.baselibrary.utils.toJson2
import com.freddy.im.http.ImCallHttpExample
import com.freddy.im.http.model.im.CallTokenContent
import com.freddy.im.listener.OnHttpImListener
import com.freddy.webtrc.bean.CallTokenBean

object BroadE {
    //获取token
    fun getCallToken(
        id: String?,
        roomName: String?,
        userName: String?,
        video: Boolean = false,
        callBack: (bean: CallTokenBean?) -> Unit = {}
    ) {
        if (TextUtils.isEmpty(id) || TextUtils.isEmpty(roomName)) {
            callBack.invoke(null)
            return
        }
        ImCallHttpExample.requestCallToken(
            id ?: "",
            roomName ?: "",
            video,
            userName ?: "",
            object : OnHttpImListener<CallTokenContent> {
                override fun onHttpImSuccess(result: CallTokenContent) {
                    eLogD("getCallToken_data", toJson2(result))
                    callBack.invoke(
                        CallTokenBean(
                            token = result.token.orEmpty(),
                            url = result.url.orEmpty(),
                            roomName = result.roomName
                        )
                    )
                }
            },
            { callBack.invoke(null) }
        )

    }

    //发起通话
    fun startCall(
        id: String?,
        video: Boolean = false,
        callBack: (bean: CallTokenBean?) -> Unit = {}
    ) {
        if (TextUtils.isEmpty(id)) {
            callBack.invoke(null)
            return
        }
        ImCallHttpExample.requestCallStart(
            id ?: "",
            video,
            object : OnHttpImListener<CallTokenContent> {
                override fun onHttpImSuccess(result: CallTokenContent) {
                    eLogD("startCall_data", toJson2(result))
                    callBack.invoke(
                        CallTokenBean(
                            token = result.token.orEmpty(),
                            url = result.url.orEmpty(),
                            roomName = result.roomName
                        )
                    )
                }
            },
            { callBack.invoke(null) }
        )
    }
}
