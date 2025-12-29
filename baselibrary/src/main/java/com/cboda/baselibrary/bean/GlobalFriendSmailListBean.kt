package com.cboda.baselibrary.bean


class GlobalFriendBean {
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
    var grade: Int? = null

    @JvmField
    var icon: String? = null

    @JvmField
    var nickName: String? = null

    @JvmField
    var remarkName: String? = null

    override fun toString(): String {
        return "GlobalFriendSmailListBean{" +
                "registrationDays=" + registerDay +
                ", grade=" + grade +
                ", icon='" + icon + '\'' +
                ", nickName='" + nickName + '\'' +
                ", remarkName='" + remarkName + '\'' +
                ", userId='" + userId + '\'' +
                '}'
    }

}
