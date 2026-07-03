package com.cboda.intelligent.activity

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.widget.AppCompatImageView
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.GsonUtils
import com.cboda.baselibrary.bean.PayLsOrderBean
import com.cboda.baselibrary.network.NetworkBroadUtils
import com.cboda.baselibrary.network.NetworkBroadUtils.postEncryptedList
import com.cboda.baselibrary.toast.ToastBroadUtils
import com.cboda.baselibrary.utils.HttpsBroadConfig
import com.cboda.baselibrary.utils.anyToDouble
import com.cboda.baselibrary.utils.anyToInt
import com.cboda.baselibrary.utils.eLogD
import com.cboda.baselibrary.utils.getResumeActivity
import com.cboda.baselibrary.utils.showCommonStrTip
import com.cboda.chatui.quickmeet.MeetCallManager
import com.cboda.intelligent.R
import com.cboda.intelligent.base.BaseSimpleBroadActivity
import com.cboda.intelligent.bean.ResultBroadBean
import com.cboda.intelligent.bean.VipOpenPlanBroadBean
import com.cboda.intelligent.dialog.InputJhmBroadDialog
import com.cboda.intelligent.net.BroadUrl
import com.cboda.intelligent.utils.GlideBroadUtil
import com.cboda.intelligent.utils.JumpBroadUtils.Companion.startCreateQunActivity
import com.cboda.intelligent.utils.OkHttpBroadHelper
import com.cboda.intelligent.utils.SpotsCallBroadBack
import com.cboda.intelligent.utils.UrlBroadBroadConstants
import com.cboda.intelligent.utils.showSelectPayMethodPop
import com.cboda.intelligent.utils.showToastMain
import com.cboda.intelligent.viewmodel.NewMirrorViewBroadModel
import com.google.gson.reflect.TypeToken
import com.makeramen.roundedimageview.RoundedImageView
import kotlinx.coroutines.launch
import okhttp3.Response
import java.math.BigDecimal
import kotlin.getValue
import kotlin.math.round

class HyzxBroadActivity : BaseSimpleBroadActivity(), View.OnClickListener {
    private var bean: ResultBroadBean? = null
    private var viHead: View? = null
    private var imFinish: AppCompatImageView? = null
    private var ivIcon: RoundedImageView? = null
    private var tvName: TextView? = null
    private var tvVipState: TextView? = null
    private var tvVipTime: TextView? = null
    private var tvTq: TextView? = null
    private var tvKt: TextView? = null
    private var tvDh: TextView? = null
    private var tvPay: TextView? = null
    private val newMirrorViewBroadModel: NewMirrorViewBroadModel by viewModels()
    public override fun showOrHideTitleLayout(): Boolean {
        return false
    }

    override fun transparentStatus(): Boolean {
        return true
    }

    override fun fitsSystemWindows(): Boolean {
        return true
    }

    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViews()
        if (HttpsBroadConfig.isReviewEnv) {
            tvDh!!.setVisibility(View.GONE)
            tvPay!!.setVisibility(View.GONE)
        }
        var result = 0
        val resourceId = getResources().getIdentifier(
            "status_bar_height",
            "dimen", "android"
        )
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId)
        }
        val linearParams = viHead!!.getLayoutParams() as LinearLayout.LayoutParams //取控件textView当前的布局参数
        linearParams.height = result // 控件的高强制设成20
        viHead!!.setLayoutParams(linearParams)
        imFinish!!.setOnClickListener(this)
        tvDh!!.setOnClickListener(this)
        tvKt!!.setOnClickListener(this)
        tvPay?.setOnClickListener(this)
        userInfo()
        ptConfig()
        loadVipOpenPlans()
    }


    override fun getLayoutId(): Int {
        return R.layout.activity_hy_broad_zx
    }

    override fun onClick(view: View) {
        if (view.getId() == R.id.imFinish) {
            finish()
        } else if (view.getId() == R.id.tvDh) {
            InputJhmBroadDialog(this, object : InputJhmBroadDialog.OnInputListener {
                override fun onInput(num: String?) {
                    duivip(num)
                }
            }).show()
        } else if (view.getId() == R.id.tvKt) {
            startCreateQunActivity(this)
        } else if (view.getId() == R.id.tvPay) {
            newMirrorViewBroadModel.createPayOrder("vip_open", true, true) {
                payLsOrderBean = it
                showSelectPayMethod()
            }
        }
    }


    override fun onResume() {
        super.onResume()
        checkOrder()
    }

    private fun checkOrder() {
        if (canPay && payLsOrderBean != null && !TextUtils.isEmpty(payLsOrderBean?.orderId)) {
            newMirrorViewBroadModel.checkOrder(payLsOrderBean?.orderId, true, false) { data ->
                if (data?.success == true) {
                    newMirrorViewBroadModel.openMember(payLsOrderBean?.orderId, true, true) {
                        canPay = false
                        payLsOrderBean = null
                        userInfo()
                    }
                } else {
                    com.cboda.intelligent.utils.dismissLoading()
                    runOnUiThread {
                        showCommonStrTip("订单支付", "该订单是否支付？", "取消支付", "已支付", getResumeActivity(), isOutSideTouchable = false) { toCheck ->
                            if (toCheck) {
                                newMirrorViewBroadModel.checkOrder(payLsOrderBean?.orderId, true, true) { data ->
                                    if (data?.success == true) {
                                        newMirrorViewBroadModel.openMember(payLsOrderBean?.orderId, true, true) {
                                            canPay = false
                                            payLsOrderBean = null
                                            userInfo()
                                        }
                                    } else {
                                        runOnUiThread {
                                            showCommonStrTip("订单支付", "查询到该订单未成功支付，是否继续？", "取消支付", "重新支付", getResumeActivity(), isOutSideTouchable = false) { toCheck2 ->
                                                if (toCheck2) {
                                                    showSelectPayMethod()
                                                } else {
                                                    payLsOrderBean = null
                                                    canPay = false
                                                }
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
    }

    private fun showSelectPayMethod() {
        eLogD("createYsGroupPayOrder", "showSelectPayMethod1")
        showSelectPayMethodPop { paymentMethod ->
            canPay = true
            newMirrorViewBroadModel.openWalletPay(payLsOrderBean, paymentMethod, this)
        }
    }

    private var payLsOrderBean: PayLsOrderBean? = null
    private var canPay = false
    private fun ptConfig() {
        val param: MutableMap<String?, Any?> = HashMap<String?, Any?>()
        OkHttpBroadHelper.getInstance().post_json(this, BroadUrl.ptConfig, param, object : SpotsCallBroadBack<ResultBroadBean?>(this) {

            override fun onSuccess(response: Response?, resultBean: ResultBroadBean?) {
                tvTq?.text = resultBean?.data?.vipinfo
            }

            override fun onError(response: Response?, code: Int, e: Exception?) {
            }
        })
    }

    /**
     * 获取会员开通套餐，展示第一条 priceUsdt
     */
    private fun loadVipOpenPlans() {
        val param: MutableMap<String?, Any?> = HashMap<String?, Any?>()
        postEncryptedList<VipOpenPlanBroadBean?>(
            UrlBroadBroadConstants.VIP_OPEN_PLANS,
            param,
            object : NetworkBroadUtils.ListCallback<VipOpenPlanBroadBean?> {


                override fun onSuccess(list: List<VipOpenPlanBroadBean?>?, totalCount: Int, totalPage: Int) {
                    if (list == null || list.isEmpty()) {
                        return
                    }
                    try {
                        val planList = GsonUtils.getGson().fromJson<MutableList<VipOpenPlanBroadBean?>?>(
                            GsonUtils.getGson().toJson(list),
                            object : TypeToken<MutableList<VipOpenPlanBroadBean?>?>() {
                            }.getType()
                        )
                        if (planList == null || planList.isEmpty() || planList.get(0) == null) {
                            return
                        }
                        val priceUsdt = anyToDouble(planList.get(0)!!.priceUsdt)
                        tvPay!!.setText(formatDirectPurchaseText(priceUsdt))
                    } catch (ignored: Exception) {
                    }
                }

                override fun onFailure(code: Int, message: String) {
                }

                override fun onError(throwable: Throwable) {
                }
            }
        )
    }

    private fun formatDirectPurchaseText(priceUsdt: Double): String {
        return "直接购买 " + formatUsdtPrice(priceUsdt) + " USDT"
    }

    private fun formatUsdtPrice(priceUsdt: Double): String {
        if (priceUsdt <= 0) {
            return "0"
        }
        if (priceUsdt == round(priceUsdt)) {
            return priceUsdt.toLong().toString()
        }
        return BigDecimal(priceUsdt.toString()).stripTrailingZeros().toPlainString()
    }

    /**
     * 用户信息
     */
    private fun userInfo() {
        val param: MutableMap<String?, Any?> = HashMap<String?, Any?>()
        OkHttpBroadHelper.getInstance().post_json(this, BroadUrl.userInfo, param, object : SpotsCallBroadBack<ResultBroadBean?>(this) {
            override fun onSuccess(response: Response?, resultBean: ResultBroadBean?) {
                bean = resultBean
                tvName?.setText(bean?.data?.name)
                if (!HttpsBroadConfig.isReviewEnv) {
                    if (1 == resultBean?.data?.vipRedeemEnabled && anyToInt(resultBean?.data?.isvip) != 1) {
                        tvDh!!.setVisibility(View.VISIBLE)
                    } else {
                        tvDh!!.setVisibility(View.GONE)
                    }
                    if (1 == resultBean?.data?.vipPurchaseEnabled && anyToInt(resultBean?.data?.isvip) != 1) {
                        tvPay!!.setVisibility(View.VISIBLE)
                    } else {
                        tvPay!!.setVisibility(View.GONE)
                    }
                }


                GlideBroadUtil.setImag(this@HyzxBroadActivity, bean!!.data.icon, ivIcon)
                if (null != bean?.data?.isvip && bean?.data?.isvip == "1" && !HttpsBroadConfig.isReviewEnv) {
                    tvVipState!!.setVisibility(View.GONE)
                    tvVipTime!!.setVisibility(View.VISIBLE)
                    tvVipTime!!.setText(bean!!.data.vipTime + getString(R.string.到期))
                    tvKt!!.setVisibility(View.VISIBLE)
                } else {
                    tvVipState!!.setVisibility(View.VISIBLE)
                    tvVipTime!!.setVisibility(View.GONE)
                    tvKt!!.setVisibility(View.GONE)
                }
            }


            override fun onError(response: Response?, code: Int, e: Exception?) {
            }
        })
    }


    /**
     * 兑换会员
     * 
     * @param code
     */
    private fun duivip(code: String?) {
        val param: MutableMap<String?, Any?> = HashMap<String?, Any?>()
        param.put("code", code)
        OkHttpBroadHelper.getInstance().post_json(this, BroadUrl.duivip, param, object : SpotsCallBroadBack<ResultBroadBean?>(this) {
            override fun onSuccess(response: Response?, resultBean: ResultBroadBean?) {
                userInfo()
            }

            override fun onError(response: Response?, code: Int, e: Exception?) {
            }
        })
    }

    private fun initViews() {
        viHead = findViewById<View>(R.id.viHead)
        imFinish = findViewById<AppCompatImageView>(R.id.imFinish)
        ivIcon = findViewById<RoundedImageView?>(R.id.ivIcon)
        tvName = findViewById<TextView>(R.id.tvName)
        tvVipState = findViewById<TextView>(R.id.tvVipState)
        tvVipTime = findViewById<TextView>(R.id.tvVipTime)
        tvTq = findViewById<TextView>(R.id.tvTq)
        tvKt = findViewById<TextView>(R.id.tvKt)
        tvDh = findViewById<TextView>(R.id.tvDh)
        tvPay = findViewById<TextView>(R.id.tvPay)
    }

    override fun useStatusBarDarkFont(): Boolean {
        return false
    }
}