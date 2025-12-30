package com.lxkj.aiim.lj.http

import com.base.commonlibrary.utils.HttpsConfig.appHost


object UrlConstants {
    var HOST: String = "$appHost/lixin"

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
    const val GET_USER_INFO = "api/userInfo"
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


    /**
     * 好友相关
     */
    //获取好友页面的分组标签
    const val FRIEND_CHARACTER = "api/friendship/character"

    //获取好友申请数量
    const val FRIEND_APPLY_COUNT = "api/friendship/applyCount"

    //好友列表
    const val FRIEND_CONTACT_LIST = "api/friendship/page"


    //群列表
    const val GROUP_LIST = "api/groupList"

    //ai报告详情
    const val AI_DETAIL = "api/AI/detail"
    //检测列表-分页
    const val AI_PAGE = "api/AI/page"

    const val AI_RESULT = "api/AI/result"
}