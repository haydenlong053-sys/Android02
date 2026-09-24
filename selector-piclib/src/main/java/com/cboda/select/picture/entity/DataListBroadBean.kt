package com.cboda.select.picture.entity

import java.io.Serializable

/**
 * @author:李迪迦
 * @date: 2024/9/20
 * @Type:
 */
class DataListBroadBean : Serializable {

    var idno: String? = null

    var isLiang: String? = null

    var isvip: String? = null

    var grade: Double? = null

    var registDay: Int? = null

    @JvmField
    var isChecked: Boolean = false

    @JvmField
    var isShowChecked: Boolean = false

    @JvmField
    var contry: String? = null

    @JvmField
    var emoji: String? = null

    @JvmField
    var no: String? = null

    @JvmField
    var id: String? = null

    @JvmField
    var icon: String? = null

    @JvmField
    var name: String? = null

    @JvmField
    var value: String? = null

    @JvmField
    var dyImg: String? = null

    @JvmField
    var dyId: String? = null

    @JvmField
    var dyInfo: String? = null

    @JvmField
    var title: String = ""
    var url: String = ""

    @JvmField
    var time: String? = null

    @JvmField
    var sname: String? = null

    @JvmField
    var sno: String? = null

    @JvmField
    var type: String? = null

    @JvmField
    var video: String? = null

    @JvmField
    var file: String? = null

    @JvmField
    var vimg: String? = null

    @JvmField
    var info: String? = null

    @JvmField
    var address: String? = null
    var lon: String? = null

    @JvmField
    var lat: String? = null

    @JvmField
    var minelat: String? = null

    @JvmField
    var lng: String? = null

    @JvmField
    var minelng: String? = null

    @JvmField
    var userId: String? = null
    var userIcon: String? = null
    var userName: String? = null
    /** 备注，对应 FriendItemVO 的 bei */
    @JvmField
    var bei: String? = null

    @JvmField
    var isDz: String? = null

    @JvmField
    var dzNames: String? = null

    @JvmField
    var content: String? = null
    var sprovince: String? = null
    var scity: String? = null
    var sregion: String? = null
    var saddress: String? = null

    @JvmField
    var money: String? = null

    @JvmField
    var state: String? = null

    @JvmField
    var context: String? = null

    @JvmField
    var tutitle: String? = null

    @JvmField
    var tu: String? = null

    @JvmField
    var pingzheng: String? = null

    @JvmField
    var miao: String? = null

    @JvmField
    var zhanghao: String? = null

    @JvmField
    var mima: String? = null

    @JvmField
    var imgs: MutableList<String?>? = null

    @JvmField
    var commList: MutableList<CircleCommListBroadBean?>? = null

    @JvmField
    var likeList: MutableList<CircleCommListBroadBean?>? = null

    @JvmField
    var sqlKey: String = ""
}