package com.cboda.baselibrary.bean

import java.io.Serializable

class PayLsOrderBean : Serializable {
    var amountDisplay: String = ""
    var chainId: String = ""
    var orderId: String = ""
    var tokenSymbol: String = ""
    var wcUri: String = ""
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
