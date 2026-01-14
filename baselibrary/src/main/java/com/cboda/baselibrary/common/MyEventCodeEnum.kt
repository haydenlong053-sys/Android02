package com.cboda.baselibrary.common

/**
 * 事件代码枚举
 */
enum class MyEventCodeEnum(val value: String) {
    // 单聊标题
    SINGLE_CHAT_TITLE("danliaobiaoti"),

    // 重新加载消息
    RELOAD_MESSAGE("reloadMsg"),

    // 重新加载转发消息
    RELOAD_FORWARD_MESSAGE("reloadMsgZhuanFa"),

    // 群详情
    GROUP_DETAILS("qunxiangqing"),

    // 更新公告了
    UPDATE_ANNOUNCEMENT("xiugaigongaole"),

    // 用户信息
    USER_INFO("yonghuxinxi"),

    // 群详情失败
    GROUP_DETAILS_FAILED("qunxiangqingshibai"),

    // 更新发言时间
    UPDATE_SEND_TIME("xiugaifayanshijian"),

    // 被封
    BANNED("beifeng"),

    // 发送接收转账消息
    SEND_RECEIVE_TRANSFER_MESSAGE("fajieshouzhuanzhangxiaoxi"),

    // 不是好友发消息
    NOT_FRIEND_SEND_MESSAGE("bushihaoyoufaxiaoxi"),

    // 会议室
    MEETING_ROOM("huiyishi"),

    // 群会议
    GROUP_MEETING("qunMeeting"),

    // 发送表情
    SEND_EMOJI("fasongbiaoqing"),

    // 投诉
    COMPLAINT("tousu");

    companion object {
        /**
         * 根据字符串值获取枚举
         */
        fun fromValue(value: String?): MyEventCodeEnum? {
            return entries.find { it.value == value }
        }
    }
}
