package com.cboda.chatui.quickmeet.bean

object EnumerationBean {

    /** 弹幕/礼物弹窗 [DanMaPop] 各入口，与业务侧 giftType 等数字约定一致 */
    enum class DanMaPopAction(val code: Int) {
        FLOWER(1),
        ACCLAMATION(2),
        PIC(3),
        GAG(4),
        REMOVE_GAG(5),
        UP_MIC(6),
        DOWN_MIC(7),
        APPLICATION_LIST(8),
    }
}