package com.lxkj.aiim.bean

class HongBaoBiangeng {
    var id:String? = null
    var messageUid:String? = null
    var payload:HongBaoPre? = null

    class HongBaoPre{
        var price:String? = null
        var hongbaoDesc:String? = null
        var lingquStatus:String? = null
        var type:String? = null
        var hongbaoId:String? = null
        var hongbaoLeixing:String? = null
        var zhuanshuUserId:String? = null
        var zhuanshuUserName:String? = null
        var zhuanshuUserAvatar:String? = null
        var lqUsers:List<String>? = null

    }
}