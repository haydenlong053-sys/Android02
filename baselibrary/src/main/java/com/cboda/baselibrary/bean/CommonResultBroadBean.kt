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
        var creditScoreNewUserDays = ""
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
        var grade: Double? = 0.0
        var characterTags: String? = null
        var remarkName: String? = null
        var bei: String? = null
        var name: String? = null
        var uid: String? = null
        var sex: String? = null
        var sign: String? = null

        var phone: String? = null
        var acount: String? = null
        var quan: List<String>? = null
        var isFri: String? = null
        var isOpenQuan: String? = null
        var isZd: String? = null
        var isMdr: String? = null
        var isBlack: String? = null
        var isfanyi: String? = null
        var fantime: String? = null


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

        var kfid: String? = null
        var autofanyi: String? = null
        var isvip: String? = null
        var biandabianyi: String? = null
        var mId: String? = null

        var profession: String? = null

        var memberInterestList: Array<String>? = null

        @SerializedName("3dDoll")
        var registerDayBean: RegisterDayBean? = null
    }

    class RegisterDayBean {
        var registDay: Int = 0
        @SerializedName("newUserDays")
        var registrationDayScore = ""
    }
}