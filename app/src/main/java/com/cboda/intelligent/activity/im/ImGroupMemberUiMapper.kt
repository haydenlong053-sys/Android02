package com.cboda.intelligent.activity.im

import com.cboda.baselibrary.bean.CommonDataListBroadBean
import com.freddy.im.group.GroupRole
import com.freddy.im.http.model.im.GroupFriendBean
import com.freddy.im.http.model.im.GroupMemberItem
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

    fun toCommonBean(member: GroupMemberItem): CommonDataListBroadBean.CommonBean {
        val bean = CommonDataListBroadBean.CommonBean()
        bean.userId = member.userId
        bean.name = member.displayName.takeIf { it.isNotBlank() } ?: member.userId
        bean.icon = member.headPortrait
        bean.type = GroupRole.toWildfireUiType(member.role)
        return bean
    }

    fun toCommonBeansFromMemberItems(
        members: List<GroupMemberItem>?
    ): List<CommonDataListBroadBean.CommonBean> {
        if (members.isNullOrEmpty()) return emptyList()
        return members.mapNotNull { member ->
            if (member.userId.isNullOrEmpty()) null else toCommonBean(member)
        }
    }

    /**
     * 踢人页不可选规则（对齐野火 QunMemberListBroadActivity）：
     * 群主不可踢自己；管理员不可踢群主/管理员。
     */
    fun applyRemoveKickRules(
        members: List<CommonDataListBroadBean.CommonBean>,
        operatorUserType: String,
        currentUserId: String?
    ): List<CommonDataListBroadBean.CommonBean> {
        members.forEach { bean ->
            bean.isZy = when {
                !currentUserId.isNullOrEmpty() && bean.userId == currentUserId -> "1"
                operatorUserType == "1" && bean.type == "1" -> "1"
                operatorUserType == "2" && (bean.type == "1" || bean.type == "2") -> "1"
                else -> null
            }
        }
        return members.sortedBy { it.name?.lowercase() ?: it.userId.orEmpty() }
    }

    /**
     * 设置管理员选人：仅普通成员可选（群主/已有管理员不可选）。
     */
    fun applySelectManagerRules(
        members: List<CommonDataListBroadBean.CommonBean>
    ): List<CommonDataListBroadBean.CommonBean> {
        members.forEach { bean ->
            bean.isZy = when (bean.type) {
                "1", "2" -> "1"
                else -> null
            }
        }
        return members.sortedBy { it.name?.lowercase() ?: it.userId.orEmpty() }
    }

    /**
     * 转让群主选人：当前群主与群主角色不可选，其余成员可选（对齐野火 ZhuanRangQunBroadActivity）。
     */
    fun applySelectNewOwnerRules(
        members: List<CommonDataListBroadBean.CommonBean>,
        currentUserId: String?
    ): List<CommonDataListBroadBean.CommonBean> {
        members.forEach { bean ->
            bean.isZy = when {
                !currentUserId.isNullOrEmpty() && bean.userId == currentUserId -> "1"
                bean.type == "1" -> "1"
                else -> null
            }
        }
        return members.sortedBy { it.name?.lowercase() ?: it.userId.orEmpty() }
    }

    fun toCommonBeans(members: List<GroupUserVo>?): List<CommonDataListBroadBean.CommonBean> {
        if (members.isNullOrEmpty()) return emptyList()
        return members.mapNotNull { m ->
            if (m.userId.isNullOrEmpty()) null else toCommonBean(m)
        }
    }

    fun toCommonBean(friend: GroupFriendBean): CommonDataListBroadBean.CommonBean {
        return CommonDataListBroadBean.CommonBean().apply {
            id = friend.id
            userId = friend.userId
            name = friend.name
            bei = friend.bei
            icon = friend.icon
            zimu = friend.zimu
            sex = friend.sex
            isvip = friend.isvip
            isLiang = friend.isLiang
            isZy = friend.isZy
            idno = friend.idno
        }
    }

    fun toCommonBeansFromGroupFriends(
        friends: List<GroupFriendBean>?
    ): ArrayList<CommonDataListBroadBean.CommonBean> {
        if (friends.isNullOrEmpty()) return arrayListOf()
        return friends.mapNotNull { friend ->
            if (friend.userId.isNullOrEmpty()) null else toCommonBean(friend)
        }.let { ArrayList(it) }
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
