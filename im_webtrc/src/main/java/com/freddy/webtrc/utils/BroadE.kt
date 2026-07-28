package com.freddy.webtrc.utils

import android.Manifest
import android.content.Context
import android.text.TextUtils
import com.cboda.baselibrary.R
import com.cboda.baselibrary.toast.ToastBroadUtils
import com.cboda.baselibrary.utils.eLogD
import com.cboda.baselibrary.utils.getAppContext
import com.cboda.baselibrary.utils.requestPermission
import com.cboda.baselibrary.utils.toJson2
import com.freddy.im.http.ImCallHttpExample
import com.freddy.im.http.model.im.CallTokenContent
import com.freddy.im.listener.OnHttpImListener
import com.freddy.webtrc.activity.CallActivity
import com.freddy.webtrc.bean.CallTokenBean
import kotlinx.coroutines.CoroutineScope

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
                            roomName = roomName ?: ""
                        )
                    )
                }
            },
            { callBack.invoke(null) }
        )
    }

    //发起通话：接口成功后打开 CallActivity，由页面通过 CallUtil 进房
    @JvmOverloads
    @JvmStatic
    fun startCall(
        id: String?,
        video: Boolean = false,
        scope: CoroutineScope? = null,
        context: Context? = null
    ) {
        if (TextUtils.isEmpty(id)) {
            return
        }
        startCall(listOf(id!!), video, scope, context)
    }

    /** 发起通话（支持多被叫，如群聊选人后语音通话）。 */
    @JvmOverloads
    @JvmStatic
    fun startCall(
        toUserIds: List<String>?,
        video: Boolean = false,
        scope: CoroutineScope? = null,
        context: Context? = null
    ) {
        val ids = toUserIds?.filter { !it.isNullOrBlank() }.orEmpty()
        if (ids.isEmpty()) {
            return
        }
        requestPermission(arrayOf(Manifest.permission.RECORD_AUDIO)) {
            ImCallHttpExample.requestCallStart(
                ids,
                video,
                { result ->
                    if (!TextUtils.isEmpty(result.token) && !TextUtils.isEmpty(result.url)) {
                        CallActivity.startOutgoing(
                            getAppContext(context),
                            ids.first(),
                            result.url,
                            result.token,
                            result.roomName,
                            video,
                            ids
                        )
                    } else {
                        ToastBroadUtils.show(R.string.call_network_error2)
                    }
                },
                {
                    // 业务/网络错误已在 ImCallHttpExample.requestCallStart 中 Toast 接口返回文案
                }
            )
        }
    }
}
