package com.cboda.intelligent.activity.im

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.widget.ImageView
import android.widget.TextView
import com.cboda.baselibrary.toast.ToastBroadUtils
import com.cboda.intelligent.R
import com.cboda.intelligent.base.BaseSimple2BroadActivity
import com.cboda.intelligent.utils.click
import com.freddy.im.http.model.im.GroupQrInfoRes
import com.hjq.shape.view.ShapeTextView
import com.hjq.shape.view.ShapeView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Random

/**
 * 收费进群确认支付页：从 [GroupJoinPayActivity] 点击立即支付进入。
 */
class GroupJoinPayConfirmActivity : BaseSimple2BroadActivity() {

    private var groupInfo: GroupQrInfoRes? = null

    private lateinit var returnView: ShapeView
    private lateinit var backIcon2: ImageView
    private lateinit var amountTv: TextView
    private lateinit var usdTv: TextView
    private lateinit var payeeTv: TextView
    private lateinit var orderNoTv: TextView
    private lateinit var bottomAmountTv: TextView
    private lateinit var confirmPayBtn: ShapeTextView

    override fun getLayoutId(): Int = R.layout.activity_group_join_pay_confirm

    override fun showOrHideTitleLayout(): Boolean = false

    override fun fitsSystemWindows(): Boolean = false

    override fun useStatusBarDarkFont(): Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setRootBgColor(R.color.color_group_join_pay_bg)
        groupInfo = readGroupInfo(intent)
        if (groupInfo == null || TextUtils.isEmpty(groupInfo?.id)) {
            finish()
            return
        }
        bindViews()
        bindClicks()
        bindData(groupInfo!!)
    }

    private fun bindViews() {
        returnView = findViewById(R.id.returnView)
        backIcon2 = findViewById(R.id.backIcon2)
        amountTv = findViewById(R.id.amountTv)
        usdTv = findViewById(R.id.usdTv)
        payeeTv = findViewById(R.id.payeeTv)
        orderNoTv = findViewById(R.id.orderNoTv)
        bottomAmountTv = findViewById(R.id.bottomAmountTv)
        confirmPayBtn = findViewById(R.id.confirmPayBtn)
    }

    private fun bindClicks() {
        returnView.click { finish() }
        backIcon2.click { finish() }
        confirmPayBtn.click {
            ToastBroadUtils.show(getString(com.cboda.baselibrary.R.string.group_join_pay_coming_soon))
        }
    }

    private fun bindData(info: GroupQrInfoRes) {
        val price = resolveJoinPayPrice(info)
        amountTv.text = price
        bottomAmountTv.text = price

        val usd = info.busiExt?.joinPayPriceUsdt?.trim().orEmpty()
        usdTv.text = if (usd.isEmpty()) {
            ""
        } else {
            getString(com.cboda.baselibrary.R.string.group_join_pay_usd_approx, usd)
        }

        val groupName = info.name?.takeIf { it.isNotBlank() } ?: info.id.orEmpty()
        payeeTv.text = getString(com.cboda.baselibrary.R.string.group_join_pay_confirm_payee_value, groupName)
        orderNoTv.text = buildDisplayOrderNo(info.id.orEmpty())
    }

    private fun resolveJoinPayPrice(info: GroupQrInfoRes): String {
        val price = info.joinPayPrice?.trim().orEmpty()
        return price.ifEmpty { "0" }
    }

    /** 展示用订单号；正式下单接口接入后可替换为服务端订单号。 */
    private fun buildDisplayOrderNo(groupId: String): String {
        val time = SimpleDateFormat("yyyyMMddHHmmss", Locale.US).format(Date())
        val suffix = (100000 + Random().nextInt(900000)).toString()
        val idPart = groupId.filter { it.isDigit() }.takeLast(4)
        return time + idPart + suffix
    }

    companion object {
        const val EXTRA_GROUP_QR_INFO = GroupJoinPayActivity.EXTRA_GROUP_QR_INFO

        fun start(context: Context, groupInfo: GroupQrInfoRes) {
            val intent = Intent(context, GroupJoinPayConfirmActivity::class.java)
            intent.putExtra(EXTRA_GROUP_QR_INFO, groupInfo)
            context.startActivity(intent)
        }

        fun readGroupInfo(intent: Intent?): GroupQrInfoRes? {
            if (intent == null) return null
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getSerializableExtra(EXTRA_GROUP_QR_INFO, GroupQrInfoRes::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getSerializableExtra(EXTRA_GROUP_QR_INFO) as? GroupQrInfoRes
            }
        }
    }
}
