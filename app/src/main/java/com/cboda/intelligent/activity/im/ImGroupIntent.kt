package com.cboda.intelligent.activity.im

import android.content.Intent
import android.os.Build
import com.freddy.im.http.model.im.GroupVo

/** 自研群详情及相关子页 Intent 参数。 */
object ImGroupIntent {

    const val EXTRA_GROUP_ID = "im_group_id"
    const val EXTRA_GROUP_VO = "im_group_vo"
    const val EXTRA_MY_ROLE = "im_my_role"
    const val EXTRA_NOTICE = "im_notice"
    const val EXTRA_MEMBER_USER_ID = "im_member_user_id"
    const val EXTRA_MEMBER_UI_TYPE = "im_member_ui_type"
    const val EXTRA_FROM_PAGE = "im_from_page"
    const val EXTRA_GROUP_NAME = "im_group_name"

    fun putGroup(intent: Intent, groupId: String, groupVo: GroupVo? = null, myRole: Int = -1) {
        intent.putExtra(EXTRA_GROUP_ID, groupId)
        if (groupVo != null) {
            intent.putExtra(EXTRA_GROUP_VO, groupVo)
        }
        if (myRole >= 0) {
            intent.putExtra(EXTRA_MY_ROLE, myRole)
        }
    }

    fun getGroupId(intent: Intent?): String? = intent?.getStringExtra(EXTRA_GROUP_ID)

    fun getGroupVo(intent: Intent?): GroupVo? {
        if (intent == null) return null
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra(EXTRA_GROUP_VO, GroupVo::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra(EXTRA_GROUP_VO) as? GroupVo
        }
    }

    fun getMyRole(intent: Intent?): Int = intent?.getIntExtra(EXTRA_MY_ROLE, -1) ?: -1
}
