package com.cboda.intelligent.activity.im

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import com.cboda.baselibrary.bean.PayLsOrderBean.JoinGroupBean
import com.cboda.baselibrary.utils.loadImg
import com.cboda.intelligent.R
import com.cboda.intelligent.base.BaseSimple2BroadActivity
import com.cboda.intelligent.utils.click
import com.cboda.intelligent.viewmodel.NewMirrorViewBroadModel
import com.freddy.im.http.model.im.GroupQrInfoRes
import com.hjq.shape.view.ShapeTextView
import kotlin.getValue

/**
 * 收费进群页：扫码检测到需付费时，携带 [GroupQrInfoRes] 进入。
 */
class GroupJoinPayActivity : BaseSimple2BroadActivity() {

    private var groupInfo: GroupQrInfoRes? = null

    private val viewModel: NewMirrorViewBroadModel by viewModels()
    private lateinit var backIcon2: View
    private lateinit var groupAvatar: ImageView
    private lateinit var groupNameTv: TextView
    private lateinit var groupIdTv: TextView
    private lateinit var groupMemberTv: TextView
    private lateinit var feeAmountTv: TextView
    private lateinit var feeUsdTv: TextView
    private lateinit var payBtn: ShapeTextView

    override fun getLayoutId(): Int = R.layout.activity_group_join_pay

    override fun showOrHideTitleLayout(): Boolean = false

    override fun fitsSystemWindows(): Boolean = true

    override fun useStatusBarDarkFont(): Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setRootBgColor(R.color.color_group_join_pay_bg)
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
        backIcon2 = findViewById(R.id.returnView)
        groupAvatar = findViewById(R.id.groupAvatar)
        groupNameTv = findViewById(R.id.groupNameTv)
        groupIdTv = findViewById(R.id.groupIdTv)
        groupMemberTv = findViewById(R.id.groupMemberTv)
        feeAmountTv = findViewById(R.id.feeAmountTv)
        feeUsdTv = findViewById(R.id.feeUsdTv)
        payBtn = findViewById(R.id.payBtn)
        addHeight(backIcon2)
        addHeight(findViewById(R.id.contentScroll))
    }

    private fun bindClicks() {
        backIcon2.click {
            finish()
        }
        payBtn.click {
            val info = groupInfo ?: return@click
            viewModel.createPayJoinOrder(info.id, haveLoading = true, closeLoading = true) {
                val joinGroupBean = JoinGroupBean()
                joinGroupBean.groupId = info.id
                joinGroupBean.groupName = info.name
                joinGroupBean.joinNeedVerify = info.joinNeedVerify
                joinGroupBean.openQyyz = info.busiExt.openQyyz
                it.joinGroupBean = joinGroupBean
                GroupJoinPayConfirmActivity.start(this, it)
                finish()
            }
        }
    }

    private fun bindData(info: GroupQrInfoRes) {
        groupNameTv.text = info.name.orEmpty()
        val displayId = info.busiExt?.idno?.takeIf { it.isNotBlank() } ?: info.id.orEmpty()
        groupIdTv.text = getString(com.cboda.baselibrary.R.string.group_join_pay_group_id, displayId)
        groupMemberTv.text = getString(
            com.cboda.baselibrary.R.string.group_join_pay_group_members,
            info.memCount,
        )

        val price = resolveJoinPayPrice(info)
        feeAmountTv.text = price
        payBtn.text = getString(com.cboda.baselibrary.R.string.group_join_pay_btn_format, price)

        val usd = info.busiExt?.joinPayPriceUsdt?.trim().orEmpty()
        feeUsdTv.text = if (usd.isEmpty()) {
            ""
        } else {
            getString(com.cboda.baselibrary.R.string.group_join_pay_usd_approx, usd)
        }
        groupAvatar.loadImg(info.icon, com.cboda.chatui.R.mipmap.ic_group_broad_chat)
    }

    private fun resolveJoinPayPrice(info: GroupQrInfoRes): String {
        val price = info.joinPayPrice?.trim().orEmpty()
        return price.ifEmpty { "0" }
    }

    companion object {
        const val EXTRA_GROUP_QR_INFO = "extra_group_qr_info"

        fun start(context: Context, groupInfo: GroupQrInfoRes) {
            val intent = Intent(context, GroupJoinPayActivity::class.java)
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
