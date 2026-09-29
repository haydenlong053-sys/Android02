package com.cboda.intelligent.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import com.cboda.baselibrary.utils.MMKVBroadUtils
import com.cboda.intelligent.R
import com.cboda.intelligent.configuration.AppBroadConsts
import com.cboda.intelligent.holder.CommanConfimBroadHolder
import com.freddy.im.http.ImGroupHttpExample
import com.freddy.im.http.model.im.GroupInviteInfoRes
import com.freddy.im.http.model.im.GroupVo
import com.freddy.im.listener.OnHttpImListener
import com.freddy.im.util.groupToSession

/**
 * 外部邀请链接 `qixun://join/{code}`：解析、登录后待处理、确认入群。
 */
object GroupInviteDeepLinkHelper {

    private const val KEY_PENDING_CODE = "pending_group_invite_code"

    @Volatile
    private var handling = false

    /** 从 Intent 解析邀请码并写入 pending（冷启动 / 热启动均可）。 */
    fun captureFromIntent(intent: Intent?): String? {
        val code = parseJoinCode(intent?.data) ?: return null
        savePending(code)
        return code
    }

    /** `qixun://join/{code}` → scheme=qixun, host=join, path=/{code} */
    fun parseJoinCode(uri: Uri?): String? {
        if (uri == null) return null
        if (!"qixun".equals(uri.scheme, ignoreCase = true)) return null
        if (!"join".equals(uri.host, ignoreCase = true)) return null
        val code = uri.pathSegments?.firstOrNull()?.trim().orEmpty()
        return code.takeIf { it.isNotEmpty() }
    }

    fun savePending(code: String) {
        if (TextUtils.isEmpty(code)) return
        MMKVBroadUtils.saveSqlStrData(KEY_PENDING_CODE, code)
    }

    fun peekPending(): String? =
        MMKVBroadUtils.getSqlStrData(KEY_PENDING_CODE).takeIf { !TextUtils.isEmpty(it) }

    fun clearPending() {
        MMKVBroadUtils.saveSqlStrData(KEY_PENDING_CODE, "")
    }

    fun isLoggedIn(context: Context): Boolean {
        val uid = SharePrefBroadBroadUtil.getString(context, AppBroadConsts.UID, "")
        val realId = MMKVBroadUtils.getRealUserId()
        return !TextUtils.isEmpty(uid) || !TextUtils.isEmpty(realId)
    }

    /**
     * 已登录且存在 pending 邀请码时：拉 [inviteInfo] 预览并弹出 [CommanConfimBroadHolder]。
     * 确认后调 [joinByInvite]。
     */
    fun tryHandlePendingInvite(activity: Activity) {
        if (handling) return
        if (!isLoggedIn(activity)) return
        val code = peekPending() ?: return
        handling = true
        ImGroupHttpExample.requestGetGroupInviteInfo(code, object : OnHttpImListener<GroupInviteInfoRes> {
            override fun onHttpImSuccess(result: GroupInviteInfoRes) {
                activity.runOnUiThread {
                    clearPending()
                    handling = false
                    showInviteConfirm(activity, code, result)
                }
            }
        }, {
            activity.runOnUiThread {
                clearPending()
                handling = false
                ToastBroadUtil.show(activity.getString(R.string.scan_network_request_failed))
            }
        })
    }

    private fun showInviteConfirm(activity: Activity, code: String, info: GroupInviteInfoRes) {
        val holder = CommanConfimBroadHolder(
            activity,
            "加入群聊",
            info.displayGroupName()
        )
        holder.callBack = object : CommanConfimBroadHolder.OnConfimCallBack {
            override fun onClickConfim(isConfim: Boolean) {
                if (!isConfim) return
                if (info.isAlreadyMember){
                    //已经是群成员了，直接打开群聊
                    openGroupChat(activity, info)
                }else{
                    joinByInvite(activity, code, info)
                }

            }
        }
        holder.show()
    }

    private fun joinByInvite(activity: Activity, code: String, info: GroupInviteInfoRes) {
        ImGroupHttpExample.requestJoinByInvite(code, object : OnHttpImListener<Boolean> {
            override fun onHttpImSuccess(result: Boolean) {
                activity.runOnUiThread {
                    if (!result) {
                        // false：已提交申请，等群主确认
                        ToastBroadUtil.show(activity.getString(R.string.scan_applied_waiting_group_owner))
                        return@runOnUiThread
                    }
                    openGroupChat(activity, info)
                }
            }
        }, {
            activity.runOnUiThread {
                ToastBroadUtil.show(activity.getString(R.string.scan_network_request_failed))
            }
        })
    }

    private fun openGroupChat(activity: Activity, info: GroupInviteInfoRes) {
        val groupId = info.resolveGroupId()
        if (TextUtils.isEmpty(groupId)) return
        val vo = GroupVo()
        vo.id = groupId
        vo.name = info.displayGroupName()
        vo.icon = info.icon
        vo.memCount = info.memCount
        val session = groupToSession(vo) ?: return
        JumpBroadUtils.startChatMsgActivity(session, activity)
    }
}
