package com.lxkj.aiim.model

import java.io.Serializable

class TransferInfoBean : Serializable {
    var balances: String? = ""
    var paymentAddress: String? = ""
    var pointTotal: String? = ""

}

class PersonInfoBean {
    var icon: String? = ""
    var uid: String? = ""
    var userId: String? = ""
    var userName: String? = ""
    var amount: String? = ""
}

class RateInfoBean {
    var actualAmount: Double = 0.0
    var amount: Double = 0.0
    var rateAmount: Double = 0.0
    var rateInfo: Double = 0.0
}

class OrderInfoBean {
    var actualAmount: Double = 0.0
    var amount: Double = 0.0
    var paymentStatus: Int = 0
    var status: Int = 0
    var rateAmount: Double = 0.0
    var receivingAddress: String? = null
    var transactionNumber: String? = null
    var createTime: String? = null
    var updateTime: String? = null
    var userId: String? = null
    var id: String? = null
    var withdrawalHash: String? = null
    var nickName: String? = null
    var remark: String? = null
}

class ResultBooBean {
    var result =false
}

