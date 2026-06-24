package com.cboda.baselibrary.bean

import java.io.Serializable

class PayLsOrderBean : Serializable {
    var amountDisplay: String = ""
    var priceUsdt: String = ""
    var priceOdic: String = ""
    var chainId: String = ""
    var orderId: String = ""
    var tokenSymbol: String = ""
    var wcUri: String = ""
    var paymentType: String = "2"
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
    var groupId = ""//群 ID
    var roomName = ""//会议室名称
    var meetingTime = ""//时长（分钟），如 "30"
    var meetingMaxUsers = ""//人数档位值，如 "100"
}