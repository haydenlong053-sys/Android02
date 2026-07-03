package com.cboda.intelligent.bean

import androidx.annotation.DrawableRes

data class PaymentMethodBroadBean(
    val id: String,
    val name: String,
    @DrawableRes val iconRes: Int,
    val packName: String,
)

object PaymentMethodDefaults {
    const val META_MASK = "metamask"
    const val IM_TOKEN = "imtoken"
    const val TP = "tp"

    fun defaultList(): List<PaymentMethodBroadBean> = listOf(
        PaymentMethodBroadBean(META_MASK, "MetaMask", com.cboda.intelligent.R.mipmap.ic_payment_metamask,"io.metamask"),
        PaymentMethodBroadBean(IM_TOKEN, "ImToken", com.cboda.intelligent.R.mipmap.ic_payment_imtoken,"im.token.app"),
        PaymentMethodBroadBean(TP, "TP", com.cboda.intelligent.R.mipmap.ic_payment_tp,"vip.mytokenpocket"),
    )
}
