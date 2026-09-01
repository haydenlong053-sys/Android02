package com.cboda.intelligent.activity.im

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.view.isGone
import com.blankj.utilcode.util.ThreadUtils
import com.cboda.baselibrary.bean.PayLsOrderBean
import com.cboda.baselibrary.bean.PayLsOrderBean.JoinGroupBean
import com.cboda.baselibrary.toast.ToastBroadUtils
import com.cboda.baselibrary.utils.TimeBroadBroadUtils
import com.cboda.baselibrary.utils.TimeBroadBroadUtils.getNowTime
import com.cboda.baselibrary.utils.anyToInt
import com.cboda.baselibrary.utils.deepClone
import com.cboda.baselibrary.utils.setTxt
import com.cboda.baselibrary.utils.showCommonStrTip
import com.cboda.intelligent.R
import com.cboda.intelligent.activity.im.GroupJoinPayActivity.Companion.EXTRA_GROUP_QR_INFO
import com.cboda.intelligent.base.BaseSimple2BroadActivity
import com.cboda.intelligent.dialog.pop.PaymentMethodPopup
import com.cboda.intelligent.utils.JumpBroadUtils
import com.cboda.intelligent.utils.click
import com.cboda.intelligent.utils.showToastMain
import com.cboda.intelligent.viewmodel.NewMirrorViewBroadModel
import com.hjq.shape.view.ShapeTextView
import kotlin.getValue

/**
 * 收费进群确认支付页
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
    private var openPay = false
    private lateinit var amountTv2: TextView
    private lateinit var orderNoTv2: TextView
    private lateinit var createTimeTv: TextView
    private lateinit var titleTv: TextView
    private lateinit var body2: View
    private lateinit var body1: View
    private lateinit var bottomBar1: View
    private lateinit var bottomBar2: View

    private lateinit var paidBtn: TextView
    private lateinit var retryBtn: TextView


    private fun showSelectPayMethod() {
        val payPopup = PaymentMethodPopup(this@GroupJoinPayConfirmActivity)
        payPopup.setOnSelected {
            openPay = true
            canPay = true
            viewModel.openWalletPay(payLsOrderBean, it, this@GroupJoinPayConfirmActivity)
            payPopup.dismiss()
        }
        payPopup.showPopupWindow()
    }


    private fun checkOrder() {
        if (canPay && payLsOrderBean != null && !TextUtils.isEmpty(payLsOrderBean?.orderId)) {
            viewModel.checkOrder(payLsOrderBean?.orderId, true, true).observe(this) { data ->
                if (data?.success == true) {
                    canPay = false
                    payLsOrderBean = null
                    JumpBroadUtils.startToGroupJoinPayResult(groupInfo, this@GroupJoinPayConfirmActivity)
                    finish()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        openPay()
        checkOrder()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setRootBgColor(R.color.color_group_join_pay_bg)
        groupInfo = readGroupInfo(intent)
        if (groupInfo == null || TextUtils.isEmpty(groupInfo?.joinGroupBean?.groupId)) {
            finish()
            return
        }
        bindViews()
        bindClicks()
        groupInfo?.also {
            bindData(it)
        }
    }


    private fun bindViews() {
        backIcon2 = findViewById(R.id.returnView)
        amountTv = findViewById(R.id.amountTv)
        usdTv = findViewById(R.id.usdTv)
        payeeTv = findViewById(R.id.payeeTv)
        orderNoTv = findViewById(R.id.orderNoTv)
        bottomAmountTv = findViewById(R.id.bottomAmountTv)
        confirmPayBtn = findViewById(R.id.confirmPayBtn)

        amountTv2 = findViewById(R.id.amountTv2)
        orderNoTv2 = findViewById(R.id.orderNoTv2)
        createTimeTv = findViewById(R.id.createTimeTv)
        titleTv = findViewById(R.id.titleTv)
        body2 = findViewById(R.id.body2)
        body1 = findViewById(R.id.body1)
        bottomBar1 = findViewById(R.id.bottomBar1)
        bottomBar2 = findViewById(R.id.bottomBar2)
        paidBtn = findViewById(R.id.paidBtn)
        retryBtn = findViewById(R.id.retryBtn)
        addHeight(backIcon2)
    }


    private fun openPay() {
        bottomBar1.isGone = !openPay
        body1.isGone = !openPay
        titleTv.isGone = openPay
        bottomBar2.isGone = openPay
        body2.isGone = openPay
    }

    private fun bindClicks() {
        findViewById<View>(R.id.backBtn).click { finish() }
        backIcon2.click { finish() }
        retryBtn.click {
            viewModel.createPayJoinOrder(groupInfo?.joinGroupBean?.groupId ?: "", haveLoading = true, closeLoading = true) {
                groupInfo?.joinGroupBean?.createTime = getNowTime()
                it.joinGroupBean = groupInfo?.joinGroupBean
                groupInfo = it
                payLsOrderBean = deepClone(it, PayLsOrderBean::class.java)
                ThreadUtils.getMainHandler().post {
                    bindData(it)
                    showSelectPayMethod()
                }
            }
        }
        paidBtn.click {
            viewModel.checkOrder(payLsOrderBean?.orderId, true, true).observe(this) { data ->
                if (data?.success == true) {
                    canPay = false
                    payLsOrderBean = null
                    JumpBroadUtils.startToGroupJoinPayResult(groupInfo, this@GroupJoinPayConfirmActivity)
                    finish()
                } else {
                    showToastMain("查询到该订单未成功支付,请重新发起支付！")
                }
            }
        }
        confirmPayBtn.click {
            groupInfo?.let {
                payLsOrderBean = deepClone(it, PayLsOrderBean::class.java)
                showSelectPayMethod()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun bindData(info: PayLsOrderBean) {
        val price = resolveJoinPayPrice(info)
        amountTv.text = price
        bottomAmountTv.text = price
        amountTv2.text = "$price ${getString(R.string.人民币)}"

        val usd = info.priceUsdt
        usdTv.text = if (usd.isEmpty()) {
            ""
        } else {
            getString(com.cboda.baselibrary.R.string.group_join_pay_usd_approx, usd)
        }
        val groupName = info.joinGroupBean?.groupName?.takeIf { it.isNotBlank() } ?: info.joinGroupBean?.groupId.orEmpty()
        payeeTv.text = getString(com.cboda.baselibrary.R.string.group_join_pay_confirm_payee_value, groupName)
        orderNoTv.text = info.orderId
        orderNoTv2.text = info.orderId
        val showTime = TimeBroadBroadUtils.formatTimestamp(info.joinGroupBean?.createTime)
        createTimeTv.text = showTime ?: ""
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
