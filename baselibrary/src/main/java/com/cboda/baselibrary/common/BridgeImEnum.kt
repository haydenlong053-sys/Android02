package com.cboda.baselibrary.common

/**
 * 桥接IM方法类型枚举
 */
enum class BridgeImEnum(val value: String) {
    // 创建新聊天
    CREATE_NEW_CHAT("cjxlt"),

    // 禁言信息
    MUTE_INFO("jinyaninfo"),

    // 单聊更多
    SINGLE_CHAT_MORE("danliaogengduo"),

    // 群聊主页
    GROUP_CHAT_HOME("qunliaozhuye"),

    // 群聊禁言
    GROUP_GAG("groupGag"),

    // 获取用户数据
    GET_USER_DATA("getUserData"),

    // 添加好友
    ADD_FRIEND("tianjiahaoyou"),

    // 未读消息添加好友
    UNREAD_MESSAGE_ADD_FRIEND("weiduxiaoxitianjiahaoyou"),

    // 名片
    BUSINESS_CARD("mingpian"),

    // 转账
    TRANSFER("zhuanzhang"),

    // 红包
    RED_PACKET("hongbao"),

    // 群红包
    GROUP_RED_PACKET("qunhongbao"),

    // 编辑
    EDIT("bianji"),

    // 添加动画表情
    ADD_ANIMATED_EMOJI("tianjiadonghuabiaoqing"),

    // 收藏
    COLLECT("shoucang"),

    // 翻译
    TRANSLATE("fanyi"),

    // 领转账
    RECEIVE_TRANSFER("lingzhuanzhang"),

    // 接收领转账
    ACCEPT_RECEIVE_TRANSFER("jieshoulingzhuanzhang"),

    // 领红包
    RECEIVE_RED_PACKET("linghongbao"),

    // 领群红包
    RECEIVE_GROUP_RED_PACKET("lingqunhongbao"),

    // 新的朋友
    NEW_FRIEND("xindepengyou"),

    // 入群邀请
    JOIN_GROUP_INVITATION("ruqunyaoqing"),

    // 群聊列表
    GROUP_CHAT_LIST("qunliaoliebiao"),

    // 被封
    BANNED("beifeng"),

    // 获取群Meeting
    GET_GROUP_MEETING("huoququnMeeting"),

    // 进入会议
    ENTER_MEETING("enterMeeting"),

    // 获取群详情
    GET_GROUP_DETAILS("huoququnxiangqing"),

    // 查看群公告
    VIEW_GROUP_ANNOUNCEMENT("chakanqungonggao"),

    // 更新公告了
    UPDATE_ANNOUNCEMENT("gengxingonggaole"),

    // 获取用户信息
    GET_USER_INFO("huoquyonghuxinxi"),

    // 位置
    LOCATION("weizhi"),

    // 导航
    NAVIGATION("daohang"),

    // 敏感词触发检验
    SENSITIVE_WORD_TRIGGER_CHECK("mingancichufajianyan"),

    // 播放视频
    PLAY_VIDEO("bofangshipin"),

    // AI
    AI("AI"),

    // 收藏IM
    COLLECT_IM("shoucangim"),

    // 会议室
    MEETING_ROOM("huiyishi"),

    // 会议
    MEETING("meeting"),

    // AI反诈
    AI_ANTI_FRAUD("aifanzha");

    companion object {
        /**
         * 根据字符串值获取枚举
         */
        fun fromValue(value: String?): BridgeImEnum? {
            return entries.find { it.value == value }
        }
    }
}