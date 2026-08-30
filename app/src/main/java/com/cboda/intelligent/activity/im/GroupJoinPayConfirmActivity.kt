package com.cboda.intelligent.activity.im

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import com.cboda.baselibrary.bean.PayLsOrderBean
import com.cboda.baselibrary.toast.ToastBroadUtils
import com.cboda.baselibrary.utils.anyToInt
import com.cboda.baselibrary.utils.showCommonStrTip
import com.cboda.intelligent.R
import com.cboda.intelligent.base.BaseSimple2BroadActivity
import com.cboda.intelligent.dialog.pop.PaymentMethodPopup
import com.cboda.intelligent.utils.JumpBroadUtils
import com.cboda.intelligent.utils.click
import com.cboda.intelligent.viewmodel.NewMirrorViewBroadModel
import com.hjq.shape.view.ShapeTextView
import kotlin.getValue

/**
 * 收费进群确认支付页：从 [GroupJoinPayActivity] 点击立即支付进入。
 */
class GroupJoinPayConfirmActivity : BaseSimple2BroadActivity() {

    private var groupInfo: PayLsOrderBean? = null

    private lateinit var backIcon2: View
    private lateinit var amountTv: TextView
    private lateinit var usdTv: TextView
    private lateinit var payeeTv: TextView
    private lateinit var orderNoTv: TextView
    private lateinit var bottomAmountTv: TextView
    private lateinit var confirmPayBtn: ShapeTextView
    private val viewModel: NewMirrorViewBroadModel by viewModels()

    override fun getLayoutId(): Int = R.layout.activity_group_join_pay_confirm

    override fun showOrHideTitleLayout(): Boolean = false

    override fun fitsSystemWindows(): Boolean = true

    override fun useStatusBarDarkFont(): Boolean = true

    private var payLsOrderBean: PayLsOrderBean? = null
    private var canPay = false


    private fun showSelectPayMethod() {
        val payPopup = PaymentMethodPopup(this@GroupJoinPayConfirmActivity)
        payPopup.setOnSelected {
            canPay = true
            viewModel.openWalletPay(payLsOrderBean, it, this@GroupJoinPayConfirmActivity)
            payPopup.dismiss()
        }
        payPopup.showPopupWindow()
    }

    private fun checkOrder() {
        if (canPay && payLsOrderBean != null && !TextUtils.isEmpty(payLsOrderBean?.orderId)) {
            viewModel.checkOrder(payLsOrderBean?.orderId, true, false).observe(this) { data ->
                if (data?.success == true) {
//                        viewModel.goOpenCredit(payLsOrderBean?.orderId, false, true).observe(this) { isSuccess ->
//                            if (isSuccess) {
//                                canPay = false
//                                payLsOrderBean = null
//
//
//
//                            }
//                        }
                } else {
                    showCommonStrTip("订单支付", "该订单是否支付？", "取消支付", "已支付", this, isOutSideTouchable = false) { toCheck ->
                        if (toCheck) {
                            viewModel.checkOrder(payLsOrderBean?.orderId, true, false).observe(this) { data ->
                                if (data?.success == true) {
//                                        viewModel.goOpenCredit(payLsOrderBean?.orderId, false, true).observe(this) { isSuccess ->
//                                            if (isSuccess) {
//                                                canPay = false
//                                                payLsOrderBean = null
//                                                JumpBroadUtils.startCreditScoreActivity(this)
//                                            }
//                                        }
                                } else {
                                    showCommonStrTip("订单支付", "查询到该订单未成功支付，是否继续？", "取消支付", "重新支付", this, isOutSideTouchable = false) { toCheck2 ->
                                        if (toCheck2) {
                                            showSelectPayMethod()
                                        } else {
                                            payLsOrderBean = null
                                            canPay = false
                                        }
                                    }
                                }
                            }
                        } else {
                            payLsOrderBean = null
                            canPay = false
                        }
                    }
                }
            }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setRootBgColor(R.color.color_group_join_pay_bg)
        groupInfo = readGroupInfo(intent)
        if (groupInfo == null || TextUtils.isEmpty(groupInfo?.groupId)) {
            finish()
            return
        }
        bindViews()
        bindClicks()
        bindData(groupInfo!!)
    }

    private fun bindViews() {
        backIcon2 = findViewById(R.id.returnView)
        amountTv = findViewById(R.id.amountTv)
        usdTv = findViewById(R.id.usdTv)
        payeeTv = findViewById(R.id.payeeTv)
        orderNoTv = findViewById(R.id.orderNoTv)
        bottomAmountTv = findViewById(R.id.bottomAmountTv)
        confirmPayBtn = findViewById(R.id.confirmPayBtn)
        addHeight(backIcon2)
    }

    private fun bindClicks() {
        backIcon2.click { finish() }
        confirmPayBtn.click {
            payLsOrderBean = groupInfo
            showSelectPayMethod()
        }
    }

    private fun bindData(info: PayLsOrderBean) {
        val price = resolveJoinPayPrice(info)
        amountTv.text = price
        bottomAmountTv.text = price

        val usd = info.priceUsdt
        usdTv.text = if (usd.isEmpty()) {
            ""
        } else {
            getString(com.cboda.baselibrary.R.string.group_join_pay_usd_approx, usd)
        }

        val groupName = info.groupName?.takeIf { it.isNotBlank() } ?: info.groupId.orEmpty()
        payeeTv.text = getString(com.cboda.baselibrary.R.string.group_join_pay_confirm_payee_value, groupName)
        orderNoTv.text = info.orderId
    }

    private fun resolveJoinPayPrice(info: PayLsOrderBean): String {
        val price = info.amountDisplay
        return price.ifEmpty { "0" }
    }

    //    /** 展示用订单号；正式下单接口接入后可替换为服务端订单号。 */
//    private fun buildDisplayOrderNo(groupId: String): String {
//        val time = SimpleDateFormat("yyyyMMddHHmmss", Locale.US).format(Date())
//        val suffix = (100000 + Random().nextInt(900000)).toString()
//        val idPart = groupId.filter { it.isDigit() }.takeLast(4)
//        return time + idPart + suffix
//    }


    companion object {
        const val EXTRA_GROUP_QR_INFO = GroupJoinPayActivity.EXTRA_GROUP_QR_INFO

        fun start(context: Context, groupInfo: PayLsOrderBean) {
            val intent = Intent(context, GroupJoinPayConfirmActivity::class.java)
            intent.putExtra(EXTRA_GROUP_QR_INFO, groupInfo)
            context.startActivity(intent)
        }

        fun readGroupInfo(intent: Intent?): PayLsOrderBean? {
            if (intent == null) return null
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getSerializableExtra(EXTRA_GROUP_QR_INFO, PayLsOrderBean::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getSerializableExtra(EXTRA_GROUP_QR_INFO) as? PayLsOrderBean
            }
        }
    }
}
