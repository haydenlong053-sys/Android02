package com.lxkj.aiim.bean

class ZhuanZhangBiangeng {
    var id:String? = null
    var messageUid:String? = null
    var payload:ZhuanZhanPre? = null

    class ZhuanZhanPre{
        var price:String? = null
        var hongbaoDesc:String? = null
        var lingquStatus:String? = null
        var type:String? = null
        var zhuanzhangId:String? = null
    }
}