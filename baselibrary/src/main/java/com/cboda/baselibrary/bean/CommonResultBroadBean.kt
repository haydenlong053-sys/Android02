package com.cboda.baselibrary.bean

import com.google.gson.annotations.SerializedName

class CommonResultBroadBean : BaseBroadResponse() {
    var data: CommonResult? = null
    var dataList: List<CommonResult>? = null
    var url: String? = null
    var number: String? = null//升级编号
    var version: String? = null//版本号
    var content: String? = null//更新内容
    var type: String? = null//强制更新 0否 1是


    class CommonResult {
        var tQrcode = ""
        var eqno = ""
        var creditScoreNewUserDays = ""
        var creditScoreNewInGroupDays: Int = 0
        var canSendLink = false
        var renameCount: Int = 0
        var joinTime: String = ""
        var fileType: String? = null
        var id: String? = null
        var content: String? = null
        var version: String? = null
        var type: String? = null
        var url: String? = null
        var xcxUrl: String? = null
        var icon: String? = null
        var isLiang: String? = null
        /**
         * 信用分：duiInfo 可能返回字符串 "0"，用 Any 承接再转 Double，避免 Gson 解析失败。
         */
        @SerializedName("grade")
        private var gradeRaw: Any? = null
        var characterTags: String? = null
        var characterTagsId: Int? = null
        var remarkName: String? = null
        /** 备注；与 remarkName 同义 */
        var bei: String? = null
        var name: String? = null
        var uid: String? = null
        var sex: String? = null
        var sign: String? = null

        var phone: String? = null
        /** 账号，文档拼写 acount（少一个 c） */
        var acount: String? = null
        var quan: List<String>? = null
        /** "1" 已是好友 / "0" 不是 */
        var isFri: String? = null
        var isOpenQuan: String? = null
        var isZd: String? = null
        var isMdr: String? = null
        var isBlack: String? = null
        var isfanyi: String? = null
        var fantime: String? = null
        var isshop: String? = null
        var showShop: String? = null
        var shopLink: String? = null

        var time: String? = null
        var money: String? = null
        var benrenMoney: String? = null
        var canLing: String? = null
        var isLing: String? = null
        var lianghao: String? = null

        var tuiStatus: Int = 0
        var tuiTime: String? = ""
        var count: String? = null
        var ylq: String? = null
        var ylqMoney: String? = null
        var info: String? = null

        var zzTime: String? = null
        var skTime: String? = null

        var loginType: String? = null
        var hasInvit: String? = null
        var showFx: String? = null
        var showXcx: String? = null
        var openBj: String? = null
        var openCh: String? = null

        var sname: String? = null
        var sno: String? = null
        var skm: String? = null
        var openMibao: String? = null

        var tx1: String? = null
        var tx2: String? = null
        var cz: String? = null
        var hongbao: String? = null
        var zhuanzhang: String? = null
        var tonghua: String? = null
        var sendadd: String? = null

        var idno: String? = null
        var qrcode: String? = null
        var gonggao: String? = null
        var openQyyq: String? = null
        var openQyyz: String? = null
        var openQzhb: String? = null
        var openQzts: String? = null
        var openQygl: String? = null
        var openQysl: String? = null
        var openQykj: String? = null
        var jiange: String? = null
        var mgc: String? = null
        var openMgc: String? = null
        var allJy: String? = null
        var isJy: String? = null
        var userType: String? = null
        var userList: List<CommonDataListBroadBean.CommonBean>? = null
        var userId: String? = null
        var nickname: String? = null
        var jyType: String? = null
        var showKf: String? = null
        var keUrl: String? = null
        var chatImg: String? = null
        var fris = 0L
        var gros = 0L

        /** friExamine：本次是否真正建立好友关系 */
        var accepted: Boolean? = null
        /** friExamine：申请人 userId */
        var applicantUserId: String? = null
        /** friExamine content.message */
        var message: String? = null

        var kfid: String? = null
        var autofanyi: String? = null
        var isvip: String? = null
        var biandabianyi: String? = null
        var mId: String? = null

        var profession: String? = null
        var openAbsentKick: String? = null // 本群会议不参会自动踢人开关："0" / "1"
        var absentKickLimit: String? = null // 全局连续未参会次数，如 5
        var gradeStatus: Int? = null
        var imPullErrToast: String? = null // 展示吐司开关

        var memberInterestList: Array<String>? = null

        /** 注册天数：旧字段 3dDoll，新 duiInfo 为 doll3d */
        @SerializedName(value = "doll3d", alternate = ["3dDoll"])
        var registerDayBean: RegisterDayBean? = null

        val grade: Double?
            get() = when (val raw = gradeRaw) {
                null -> null
                is Number -> raw.toDouble()
                is String -> raw.toDoubleOrNull()
                else -> raw.toString().toDoubleOrNull()
            }
    }

    class RegisterDayBean {
        var registDay: Int = 0

        @SerializedName("newUserDays")
        var registrationDayScore = ""
    }
}
