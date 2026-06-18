package com.cboda.intelligent.activity.im

import com.cboda.baselibrary.bean.CommonDataListBroadBean
import com.freddy.im.group.GroupRole
import com.freddy.im.http.model.im.GroupUserVo

object ImGroupMemberUiMapper {

    fun toCommonBean(member: GroupUserVo): CommonDataListBroadBean.CommonBean {
        val bean = CommonDataListBroadBean.CommonBean()
        bean.userId = member.userId
        bean.name = member.displayName
        bean.icon = member.headPortrait
        bean.type = GroupRole.toWildfireUiType(member.role)
        return bean
    }

    fun toCommonBeans(members: List<GroupUserVo>?): List<CommonDataListBroadBean.CommonBean> {
        if (members.isNullOrEmpty()) return emptyList()
        return members.mapNotNull { m ->
            if (m.userId.isNullOrEmpty()) null else toCommonBean(m)
        }
    }

    fun resolveMyRole(
        members: List<GroupUserVo>?,
        currentUserId: String?,
        ownerId: String?
    ): Int {
        if (!currentUserId.isNullOrEmpty() && currentUserId == ownerId) {
            return GroupRole.OWNER
        }
        if (!currentUserId.isNullOrEmpty() && members != null) {
            members.firstOrNull { it.userId == currentUserId }?.let { return it.role }
        }
        return GroupRole.MEMBER
    }

    /** 构建成员网格：群主/管含 +/-，普通成员仅成员列表。 */
    fun buildGridItems(
        members: List<GroupUserVo>?,
        myRole: Int,
        previewLimit: Int = 10
    ): List<CommonDataListBroadBean.CommonBean> {
        val list = ArrayList<CommonDataListBroadBean.CommonBean>()
        if (GroupRole.isOwnerOrAdmin(myRole)) {
            list.add(CommonDataListBroadBean.CommonBean().apply { type = "add" })
            list.add(CommonDataListBroadBean.CommonBean().apply { type = "remove" })
        }
        val memberBeans = toCommonBeans(members)
        val remain = (previewLimit - list.size).coerceAtLeast(0)
        list.addAll(memberBeans.take(remain))
        return list
    }

    fun wildfireUserTypeForRole(myRole: Int): String = when (myRole) {
        GroupRole.OWNER -> "1"
        GroupRole.ADMIN -> "2"
        else -> "0"
    }
}
