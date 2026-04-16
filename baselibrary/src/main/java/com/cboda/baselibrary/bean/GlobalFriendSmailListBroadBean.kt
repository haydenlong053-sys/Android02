package com.cboda.baselibrary.bean


class GlobalFriendBroadBean {
    /**
     * characterTags : 工作圈
     * characterTagsId : 3
     * city : 深圳
     * days : 31
     * first : J
     * grade : 0
     * icon : http://106.52.42.244:82//userfiles/icon.png
     * id : 1
     * nickName : 张三
     * profession : 律师
     * remarkName : 机器人
     * sex : 1
     * userId : robot002
     * idno :
     * lianghao : 88888
     */
    @JvmField
    var userId: String? = null

    @JvmField
    var registerDay: Int? = null

    @JvmField
    var grade: Double? = null

    @JvmField
    var icon: String? = null

    @JvmField
    var nickName: String? = null

    @JvmField
    var remarkName: String? = null

    @JvmField
    var isVip: Boolean = false

    @JvmField
    var isNiceNum: Boolean = false //是否靓号


    override fun toString(): String {
        return "GlobalFriendBroadBean(userId=$userId, registerDay=$registerDay, grade=$grade, icon=$icon, nickName=$nickName, remarkName=$remarkName, isVip=$isVip, isNiceNum=$isNiceNum)"
    }

}