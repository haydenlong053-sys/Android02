package com.cboda.baselibrary.bean


data class GlobalChatGroupBean(
    var allJy: String? = null,
    var fantime: String? = null,
    var gsize: String? = null,
    var icon: String? = null,
    var id: String? = null,
    var idno: String? = null,
    var isJy: String? = null,
    var isZd: String? = null,
    var isfanyi: String? = null,
    var jiange: Int = 0,
    var jyEndTime: String? = null,
    var jyType: String? = null,
    var name: String? = null,
    var nickname: String? = null,
    var openMgc: String? = null,
    var openQygl: String? = null,
    var openQykj: String? = null,
    var openQysl: String? = null,
    var openQyyq: String? = null,
    var openQyyz: String? = null,
    var openQzhb: String? = null,
    var openQzts: String? = null,
    var qrcode: String? = null,
    var renameCount: Int = 0,
    var userList: List<User>? = null,
    var userType: String? = null
)

data class User(
    var icon: String? = null,
    var name: String? = null,
    var type: String? = null,
    var userId: String? = null
)

data class MeetUser(
    var icon: String? = null,
    var nickname: String? = null,
    var role: String? = null,
    var userId: String? = null,
    var idNo: String? = null,
    var seatNumbIndex: Int = -1,
)