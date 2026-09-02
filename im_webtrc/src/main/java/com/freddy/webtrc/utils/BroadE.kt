package com.freddy.webtrc.utils

import android.Manifest
import android.content.Context
import android.text.TextUtils
import com.cboda.baselibrary.R
import com.cboda.baselibrary.toast.ToastBroadUtils
import com.cboda.baselibrary.utils.anyToInt
import com.cboda.baselibrary.utils.eLogD
import com.cboda.baselibrary.utils.getAppContext
import com.cboda.baselibrary.utils.getAppStr
import com.cboda.baselibrary.utils.requestPermission
import com.cboda.baselibrary.utils.toJson2
import com.freddy.im.http.ImCallHttpExample
import com.freddy.im.http.ImGroupHttpExample
import com.freddy.im.http.model.im.CallTokenContent
import com.freddy.im.http.model.im.GroupUserVo
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

    /**
     * 查询指定用户是否在群内。
     * POST `api/im/group/getSpecifyMembers`，body：`groupId` + `members`。
     *
     * @param groupId 群 ID
     * @param members 待查询 userId 列表
     * @param onSuccess 返回在群内的成员信息（不在群内的 userId 不会出现在列表中）
     * @param onFail 请求失败回调
     */
    @JvmOverloads
    @JvmStatic
    fun getSpecifyMembers(
        groupId: String?,
        members: List<String?>,
        callBack: (Pair<List<GroupUserVo>?, String?>) -> Unit = {}
    ) {

        val ids = members.filter { !it.isNullOrBlank() }
        if (!TextUtils.isEmpty(groupId) && ids.isNotEmpty()) {
            ImGroupHttpExample.requestGetSpecifyMembers(
                groupId ?: "",
                ids,
                { result ->
                    eLogD("getSpecifyMembers", toJson2(result))
                    callBack.invoke(Pair(result, null))
                },
                {
                    callBack.invoke(Pair(null, getAppStr(R.string.http_error)))
                }
            )
        } else {
            callBack.invoke(Pair(null, getAppStr(R.string.group_user_not_is_null)))
        }

    }

    @JvmOverloads
    @JvmStatic
    fun checkPeopleInGroup(groupId: String?, userId: String?, callBack: (Pair<Boolean, String?>) -> Unit = {}) {
        val list = arrayListOf<String?>()
        if (!TextUtils.isEmpty(userId)) {
            list.add(userId)
        }
        getSpecifyMembers(
            groupId,
            list
        ) { pair ->
            val returnList = pair.first
            if (null != returnList && anyToInt(returnList.size) > 0) {
                callBack.invoke(Pair(returnList[0].userId == userId, pair.second))
            } else {
                callBack.invoke(Pair(false, pair.second))
            }
        }
    }

    /**
     * 判断 [userIds] 中哪些用户在群内。
     * @return onResult：`inGroupIds` 为在群内的 userId；`members` 为对应成员详情
     */

    @JvmOverloads
    @JvmStatic
    fun checkMembersInGroup(
        groupId: String?,
        userIds: List<String>
//        ,onResult: (inGroupIds: Set<String>, members: List<GroupUserVo>) -> Unit = { _, _ -> },
//        onFail: () -> Unit = {}
    ) {
        getSpecifyMembers(
            groupId,
            userIds
//            ,
//            onSuccess = { list ->
//                val inGroupIds = list.mapNotNull { it.userId?.takeIf { id -> id.isNotBlank() } }.toSet()
//                onResult.invoke(inGroupIds, list)
//            },
//            onFail = onFail
        )
    }

    //发起通话：接口成功后打开 CallActivity，由页面通过 CallUtil 进房
    @JvmOverloads
    @JvmStatic
    fun startCall(
        id: String?,
        video: Boolean = false,
        scope: CoroutineScope? = null,
        context: Context? = null,
        groupId: String? = null
    ) {
        if (TextUtils.isEmpty(id)) {
            return
        }
        startCall(listOf(id!!), video, scope, context, groupId)
    }

    /** 发起通话（支持多被叫，如群聊选人后语音通话）。群通话需传 [groupId]。 */
    @JvmOverloads
    @JvmStatic
    fun startCall(
        toUserIds: List<String>?,
        video: Boolean = false,
        scope: CoroutineScope? = null,
        context: Context? = null,
        groupId: String? = null
    ) {
        val ids = toUserIds?.filter { !it.isNullOrBlank() }.orEmpty()
        if (ids.isEmpty()) {
            return
        }
        requestPermission(arrayOf(Manifest.permission.RECORD_AUDIO)) {
            ImCallHttpExample.requestCallStart(
                ids,
                video,
                groupId,
                { result ->
                    if (!TextUtils.isEmpty(result.token) && !TextUtils.isEmpty(result.url)) {
                        CallActivity.startOutgoing(
                            getAppContext(context),
                            ids.first(),
                            result.url,
                            result.token,
                            result.roomName,
                            video,
                            ids,
                            groupId
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
