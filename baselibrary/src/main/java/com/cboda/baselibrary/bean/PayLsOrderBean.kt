package com.cboda.baselibrary.bean

import java.io.Serializable

class PayLsOrderBean : Serializable {
    var groupId: String? = null
    var groupName: String? = null
    var amountDisplay: String = ""
    var priceUsdt: String = ""
    var priceOdic: String = ""
    var chainId: String = ""
    var orderId: String = ""
    var tokenSymbol: String = ""
    var wcUri: String = ""
    var paymentType: Int = -1
    var txParams: PayLsOrderTxParams? = null

    class PayLsOrderTxParams : Serializable {
        var data: String = ""
        var gas: String = ""
        var maxFeePerGas: String = ""
        var maxPriorityFeePerGas: String = ""
        var to: String = ""
        var value: String = ""
    }
}

class PriceConversion : Serializable {
    var priceOdic: String = ""
    var priceUsdt: String = ""
    var success: Boolean = false
}

class CreateGroupData : Serializable {
    var groupIdNo: String? = null//群 ID
    var durationConfigId: String? = null//时长ID
    var userTierConfigId: String? = null//人数ID

    var roomName: String? = null //会议室名称
    var forceNewPay: Boolean? = null
    var planId: String? = null
    var groupId: String? = null
}

