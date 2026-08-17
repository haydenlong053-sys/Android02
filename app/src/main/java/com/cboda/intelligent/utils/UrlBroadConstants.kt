package com.cboda.intelligent.utils

import com.cboda.baselibrary.utils.HttpsBroadConfig


object UrlBroadBroadConstants {
    var HOST = HttpsBroadConfig.appHost2

    const val GET_UNIQUE_ROOM_ID = "/v2/room/getUniqueRoomId"

    //    const val  GET_ROOM_INFO="/v2/room/detail/"
    const val GET_ROOM_INFO = "/meetings/settings/detail/"

    const val UPDATE_ROOM_INFO = "/v2/room/settings/update"


    const val GROUP_MEETING_OPENING = "/v2/room/group/meetings"

    const val UPDATE_MEETING_INFO = "/meetings/settings/update"


    //预创建会议接口--点击创建按钮后请求的接口
    const val PRE_CREATE_MEETING = "meetings/create/groupId"

    //获取创建会议时的人数、时长等信息接口
    const val GET_MEETING_CONFIG = "meetings/get/config/v2"

    //续费会议时长
    const val ADD_MEETING_TIME = "meetings/add/group/time"

    //获取群内是否有正在进行的会议信息
    const val GET_GROUP_MEETING = "meetings/get/groupById"

    const val CALCULATE_MEETING_PRICE = "meetings/calculate/owner"


    /**
     * 验证支付密码
     */
    const val CHECK_PAY_PASSWORD = "api/checkPayPwd"
    const val GET_USER_INFO = "im/user/getUser"
    const val SECURITY_CENTER = "api/member/security/center"
    const val SECURITY_ACCOUNT = "api/account/querySecurity"
    const val SECURITY_CENTER_FORGET_PASSWORD = "api/account/query"
    const val SECURITY_QUESTION_LIST = "api/mbFaqList"
    const val SET_MIBAO = "api/setMibao"
    const val CHANGE_MIBAO = "api/changeMibao"
    const val TWO_STEP_VERIFICATION_QRCODE = "asset/google/getTwoStepVerificationQRCode"
    const val BINDING_TWO_FACTOR_VALIDATE = "asset/google/bindingTwoFactorValidate"
    const val TWO_FACTOR_VALIDATE = "asset/google/validateGoogleTwoFactor"
    const val TRANSACTION_INITIALIZE = "asset/transaction/initialize"

    const val ADD_TRANSACTION_ADDRESS = "asset/withdraw/address/add"
    const val GET_TRANSACTION_ADDRESS = "asset/withdraw/address/getByIdno"

    const val TRANSACTION_PERSONINFO = "asset/transaction/personInfo"
    const val TRANSACTION_RATEINFO = "asset/transaction/rateInfo"
    const val TRANSACTION_ADD_ORDER = "asset/transaction/add"
    const val TRANSACTION_ORDER_DETAIL = "asset/transaction/detail"
    const val TRANSACTION_PAGE = "asset/transaction/page"
    const val WITHDRAW_ADD_ORDER = "asset/withdraw/add"
    const val WITHDRAW_PAGE = "asset/withdraw/page"
    const val WITHDRAW_ORDER_DETAIL = "asset/withdraw/detail"
    const val IS_SECURITY_FIRST = "api/member/security/first"

    //获取公告分页列表
    const val NOTICE_LIST = "api/notice/list"
    const val NOTICE_READ = "api/notice/read"
//    const val DEVICE_GET_AUTH = "api/device/get/auth"
//    const val DEVICE_BIND = "api/device/bind"


    /**
     * 镜像相关
     */
    const val SIGN_INFO = "api/signInfo"

    /** 广场帖子列表 */
    const val SQUARE_POST_LIST = "api/piamom/square/list"

    /** 广场热门/发布配置 */
    const val SQUARE_HOT_CONFIG = "api/piamom/square/hotConfig"

    /** 广场帖子详情 */
    const val SQUARE_POST_DETAIL = "api/piamom/square/detail"

    /** 广场帖子浏览上报 */
    const val SQUARE_POST_VIEW = "api/piamom/square/view"

    /** 广场帖子评论列表 */
    const val SQUARE_POST_COMMENT_LIST = "api/piamom/square/commentList"

    /** 广场帖子发表评论 */
    const val SQUARE_POST_COMMENT = "api/piamom/square/comment"

    /** 广场帖子点赞/取消点赞 */
    const val SQUARE_POST_LIKE = "api/piamom/square/like"

    /** 广场帖子置顶/取消置顶 */
    const val SQUARE_POST_PIN = "api/piamom/square/pin"

    /** 举报类型配置列表 */
    const val SQUARE_REPORT_CONFIG_LIST = "api/piamom/report/configList"

    /** 提交举报 */
    const val SQUARE_REPORT_SUBMIT = "api/piamom/report/submit"

    /** 发布广场帖子 */
    const val SQUARE_POST_PUBLISH = "api/piamom/square/publish"

    /** 发布朋友圈 */
    const val MOMENT_POST_PUBLISH = "api/piamom/moment/publish"

    /** 朋友圈动态点赞/取消点赞 */
    const val MOMENT_POST_LIKE = "api/piamom/moment/like"

    /** 朋友圈动态详情 */
    const val MOMENT_POST_DETAIL = "api/piamom/moment/detail"

    /** 朋友圈动态浏览上报 */
    const val MOMENT_POST_VIEW = "api/piamom/moment/view"

    /** 朋友圈动态发表评论 */
    const val MOMENT_POST_COMMENT = "api/piamom/moment/comment"

    /** 朋友圈动态置顶/取消置顶 */
    const val MOMENT_POST_PIN = "api/piamom/moment/pin"

    /** 删除动态（朋友圈） */
    const val MOMENT_POST_DELETE = "api/piamom/moment/delete"

    /** 删除动态（广场） */
    const val SQUARE_POST_DELETE = "api/piamom/square/delete"

    /** 朋友圈列表 */
    const val FRIEND_POST_LIST = "api/piamom/moment/feedList"

    /** 用户主页信息 */
    const val SQUARE_USER_PROFILE = "api/piamom/user/profile"

    /** 关注/取关用户 */
    const val SQUARE_USER_FOLLOW = "api/piamom/user/follow"

    /** 拉黑/解除拉黑用户 */
    const val SQUARE_USER_BLACK = "api/piamom/user/blacklist"

    /** 粉丝黑名单列表 */
    const val SQUARE_USER_BLACKLIST_LIST = "api/piamom/user/blacklistList"

    /** 用户主页广场列表 */
    const val SQUARE_USER_LIST = "api/piamom/square/userList"

    /** 用户主页朋友圈列表 */
    const val MOMENT_USER_LIST = "api/piamom/moment/userList"

    /** 用户粉丝列表 */
    const val SQUARE_USER_FOLLOWER_LIST = "api/piamom/user/followerList"

    /** 用户关注列表 */
    const val SQUARE_USER_FOLLOWING_LIST = "api/piamom/user/followingList"

    /** 消息中心列表 */
    const val SQUARE_USER_MESSAGE_LIST = "api/piamom/user/messageList"

    /** 消息中心一键去红点 */
    const val SQUARE_USER_MESSAGE_READ_ALL = "api/piamom/user/readMessage"

    /** 消息中心清空消息 */
    const val SQUARE_USER_DELETE_MESSAGE = "api/piamom/user/deleteMessage"


    /**
     * 好友相关
     */
    //获取好友页面的分组标签
    const val FRIEND_CHARACTER = "api/friendship/character"

    //获取好友申请数量
    const val FRIEND_APPLY_COUNT = "im/friend/applyCount"

    //好友列表
    const val FRIEND_CONTACT_LIST = "im/friend/friendList"

    /** 修改个人资料 POST im/user/updateProfile，body: nickname/avatar/sex/sign（null=不改） */
    const val USER_UPDATE_PROFILE = "im/user/updateProfile"

    /** 单文件上传 POST filehub/upload（独立 OSS 域名） */
    const val FILEHUB_UPLOAD = "filehub/upload"

    /** 批量文件上传 POST filehub/batchUpload（独立 OSS 域名） */
    const val FILEHUB_BATCH_UPLOAD = "filehub/batchUpload"

    /** 对方资料 / 名片页 POST im/friend/duiInfo，body: friendUserId */
    const val FRIEND_DUI_INFO = "im/friend/duiInfo"

    /** 发起好友申请 POST im/friend/addFriend */
    const val FRIEND_ADD = "im/friend/addFriend"

    /** 新的朋友（收到的申请） POST im/friend/newFriList */
    const val FRIEND_NEW_LIST = "im/friend/newFriList"

    /** 处理好友申请 POST im/friend/friExamine */
    const val FRIEND_EXAMINE = "im/friend/friExamine"

    /** 设置好友备注 POST im/friend/setBei */
    const val FRIEND_SET_BEI = "im/friend/setBei"

    /** 拉黑 / 解除拉黑 POST im/friend/black */
    const val FRIEND_BLACK = "im/friend/black"

    /** 删除好友 POST im/friend/delFriend */
    const val FRIEND_DEL = "im/friend/delFriend"


    //群列表
    const val GROUP_LIST = "api/groupList"

    //ai报告详情
    const val AI_DETAIL = "api/AI/detail"

    //检测列表-分页
    const val AI_PAGE = "api/AI/page"

    const val AI_RESULT = "api/AI/result"

    //WT相关
    // 钱包地址
    const val WT_WALLETS = "v1/wallets" // 创建/导入(POST), 列表(GET)
    const val WT_WALLETS_DETAIL = "v1/wallets/detail" // 详情(GET)
    const val WT_WALLETS_UPDATE = "v1/wallets/update" // 更新(POST)
    const val WT_WALLETS_SOURCE = "v1/wallets/source" // 更新钱包来源地址(POST)
    const val WT_WALLETS_DELETE = "v1/wallets/delete" // 删除(POST)
    const val WT_WALLETS_SORT = "v1/wallets/sort" // 排序(POST)

    // 用户代币
    const val WT_TOKENS_PREVIEW = "v1/tokens/preview" // 代币预览(POST)
    const val WT_TOKENS = "v1/tokens" // 添加代币(POST), 列表(GET)
    const val WT_TOKENS_DELETE = "v1/tokens/delete" // 移除代币(POST)

    // 资产与价格
    const val WT_ASSETS = "v1/assets" // 资产列表(GET)
    const val WT_PRICES = "v1/prices" // 价格批量查询(POST)

    // 交易与账本
    const val WT_TX_GAS = "v1/tx/gas" // Gas 预估(POST)
    const val WT_TX_VALIDATE = "v1/tx/validate" // 转账校验(POST)
    const val WT_TX_BROADCAST = "v1/tx/broadcast" // 交易广播(POST)
    const val WT_TX_STATUS = "v1/txs" // 交易状态查询(GET)
    const val WT_LEDGER = "v1/ledger" // 账本流水查询(GET)

    //信用分
    const val CREDIT_ACTIVATECREDIT = "api/credit/activateCredit" // 开通信用分
    const val CREDIT_GET_CREDIT_DETAILS = "api/credit/getCreditDetails" // 查询信用分详情
    const val CREDIT_GET_SCORE_DESC = "api/credit/getScoreDesc" // 查询信用分分数说明

    /** 缺席自动移出规则 */
    const val ABSENT_KICK_RULES = "api/absentKickRules"
    /** 设置群缺席自动移出开关 */
    const val SET_GROUP_ABSENT_KICK = "api/setGroupAbsentKick"
    /** 设置群商业功能开关（对齐野火 {@code /setGroupShop}） */
    const val SET_GROUP_SHOP = "api/setGroupShop"
    /** 修改群信息/开关（对齐野火 {@code /editGroup}，如 openQyyq / openQyyz / openQzts） */
    const val EDIT_GROUP = "api/editGroup"

    const val CREDIT_CREDITCONSTITUTELIST = "api/credit/creditConstituteList" // 查询信用构成列表
    const val CREDIT_GETLOGPAGE = "api/credit/getLogPage" // 信用分记录分页
    const val CREATE_LS_PAY_ORDER = "api/wallet/pay/prepare" // 链上支付创建支付单
    const val GET_ACTIVATE_PRICE = "api/wallet/pay/convertOdicPrice" // 价格汇率换算

    const val SELECT_ORDER = "api/wallet/pay/poll"//查这笔链上支付有没有成功

    const val VIP_OPEN_PLANS = "api/vip/open/plans" // 会员开通套餐列表

    const val OPEN_MEMBER = "api/vip/open/activate" // 开通会员


}