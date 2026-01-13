package com.cboda.baselibrary.common

/**
 * 桥接IM方法类型枚举
 */
enum class BridgeImEnum(val value: String) {
    // 创建新聊天
    CREATE_NEW_CHAT("cjxlt"),

    // 禁言信息
    JINYAN_INFO("jinyaninfo"),

    // 单聊更多
    DANLIAO_GENGDUO("danliaogengduo"),

    // 群聊主页
    QUNLIAO_ZHUYE("qunliaozhuye"),

    // 获取用户数据
    GET_USER_DATA("getUserData"),

    // 添加好友
    TIANJIA_HAOYOU("tianjiahaoyou"),

    // 未读消息添加好友
    WEIDUXIAOXI_TIANJIA_HAOYOU("weiduxiaoxitianjiahaoyou"),

    // 名片
    MINGPIAN("mingpian"),

    // 转账
    ZHUANZHANG("zhuanzhang"),

    // 红包
    HONGBAO("hongbao"),

    // 群红包
    QUN_HONGBAO("qunhongbao"),

    // 编辑
    BIANJI("bianji"),

    // 添加动画表情
    TIANJIA_DONGHUA_BIAOQING("tianjiadonghuabiaoqing"),

    // 收藏
    SHOUCANG("shoucang"),

    // 翻译
    FANYI("fanyi"),

    // 领转账
    LING_ZHUANZHANG("lingzhuanzhang"),

    // 接收领转账
    JIESHOU_LING_ZHUANZHANG("jieshoulingzhuanzhang"),

    // 领红包
    LING_HONGBAO("linghongbao"),

    // 领群红包
    LING_QUN_HONGBAO("lingqunhongbao"),

    // 新的朋友
    XINDE_PENGYOU("xindepengyou"),

    // 入群邀请
    RUQUN_YAOQING("ruqunyaoqing"),

    // 群聊列表
    QUNLIAO_LIEBIAO("qunliaoliebiao"),

    // 被封
    BEIFENG("beifeng"),

    // 获取群Meeting
    HUOQU_QUN_MEETING("huoququnMeeting"),

    // 进入会议
    ENTER_MEETING("enterMeeting"),

    // 获取群详情
    HUOQU_QUN_XIANGQING("huoququnxiangqing"),

    // 查看群公告
    CHAKAN_QUN_GONGGAO("chakanqungonggao"),

    // 更新公告了
    GENGXIN_GONGGAOLE("gengxingonggaole"),

    // 获取用户信息
    HUOQU_YONGHU_XINXI("huoquyonghuxinxi"),

    // 位置
    WEIZHI("weizhi"),

    // 导航
    DAOHANG("daohang"),

    // 敏感词触发检验
    MINGANCI_CHUFA_JIANYAN("mingancichufajianyan"),

    // 播放视频
    BOFANG_SHIPIN("bofangshipin"),

    // AI
    AI("AI"),

    // 收藏IM
    SHOUCANG_IM("shoucangim"),

    // 会议室
    HUIYISHI("huiyishi"),

    // 会议
    MEETING("meeting");

    companion object {
        /**
         * 根据字符串值获取枚举
         */
        fun fromValue(value: String?): BridgeImEnum? {
            return values().find { it.value == value }
        }
    }
}