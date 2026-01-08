package com.cboda.intelligent.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.cboda.intelligent.BuildConfig
import com.cboda.intelligent.R
import com.cboda.intelligent.base.BaseSimple2BroadActivity
import com.cboda.intelligent.dialog.pop.AuthCodeBroadPop
import com.cboda.intelligent.dialog.pop.AuthPasswordBroadPop
import com.cboda.intelligent.dialog.pop.AuthQuestionBroadPop
import com.cboda.intelligent.dialog.pop.AuthWayBroadPop
import com.cboda.intelligent.dialog.pop.SaveMoneyQrCodeTipBroadPop
import com.cboda.intelligent.dialog.pop.TransferSureTipBroadPop
import com.cboda.intelligent.configuration.CommonBroadConfig
import com.cboda.intelligent.configuration.SafeJumpBroadEnum
import com.cboda.intelligent.bean.ActionEventBroadBean
import com.cboda.intelligent.bean.TransferEventBroadBean
import com.cboda.intelligent.databinding.ActivityBroadTransferBinding
import com.cboda.intelligent.utils.copyToClipboard
import com.cboda.intelligent.utils.isSubmitCanGo
import com.cboda.intelligent.utils.isTransferLinkQrCode
import com.cboda.intelligent.utils.pasteToEditText
import com.cboda.intelligent.utils.showToastMain
import com.cboda.intelligent.utils.textChanges
import com.cboda.intelligent.model.PersonInfoBean
import com.cboda.intelligent.model.SafeCenterBroadBean
import com.cboda.intelligent.viewmodel.TransferViewBroadModel
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 转账
 */
class TransferBroadActivity : BaseSimple2BroadActivity() {
    private val viewModel: TransferViewBroadModel by viewModels()
    private var flag = 0
    private var paymentAddress: String? = null
    private var linkString = ""
    private var isFirst = true
    private var safeCenterBean = SafeCenterBroadBean()
    private var mBind: ActivityBroadTransferBinding? = null

    override fun getViewBindingClass(): Class<out ViewBinding>? {
        return ActivityBroadTransferBinding::class.java
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        linkString = intent.getStringExtra("data") ?: ""
        flag = intent.getIntExtra("type", 0)
        mBind = (binding as? ActivityBroadTransferBinding)
        EventBus.getDefault().register(this)
        getToolbarTitle()?.text = if (flag == 1) {
            getResources().getString(R.string.提现)
        } else {
            getResources().getString(R.string.mine_transfer)
        }
        mBind?.mClMoneyAddress?.isVisible = (flag == 0)
        getRightImg()?.setImageResource(R.mipmap.ic_tranfer_record)
        mBind?.mLlSysCurrency?.isSelected = true

        mBind?.mTvSysCurrency?.text = String.format(
            getString(R.string.mine_sys_currency),
            getString(R.string.webX_name)
        )
        mBind?.mTvMyMoneyAddress?.text = String.format(
            getString(R.string.mine_my_money_address),
            getString(R.string.webX_name)
        )
        mBind?.mTvSubmit?.isSubmitCanGo(false)
        initListener()
        initData()
    }

    private fun initListener() {
        getRightImg()?.setOnClickListener {
            startActivity(Intent(this@TransferBroadActivity, TransferListBroadActivity::class.java).apply {
                putExtra("type", flag)
            })
        }
        mBind?.mLlSysPoint?.setOnClickListener {
            showToastMain(getString(R.string.is_not_yet_available))
        }
        mBind?.mTvMoneyQrcode?.setOnClickListener {
            if (paymentAddress?.isNotEmpty() == true) {
                SaveMoneyQrCodeTipBroadPop(this@TransferBroadActivity, paymentAddress).showPopupWindow()
            }

        }
        mBind?.mIvCopy?.setOnClickListener {
            if (paymentAddress?.isNotEmpty() == true) {
                copyToClipboard(this, paymentAddress)
            }
        }
        mBind?.mTvPaste?.setOnClickListener {
            pasteToEditText(mBind?.mEtInputAddress!!)
        }
        mBind?.mEtInputAddress?.addTextChangedListener { editable ->
            checkNestBtn()
        }

        mBind?.mEtInputMoney?.addTextChangedListener { editable ->
            checkNestBtn()
        }

        mBind?.mTvSubmit?.setOnClickListener {
            val inputAddressString =
                mBind?.mEtInputAddress?.text?.trim().toString()
            val inputMoneyString =
                mBind?.mEtInputMoney?.text?.trim().toString()
            if (inputAddressString.isNotEmpty() && inputMoneyString.isNotEmpty()) {
                performNetworkRequest(inputMoneyString)
                inputAddressString.let {
                    if (flag == 0) {
                        lifecycleScope.launch {
                            viewModel.getTransactionPersonInfo(it)
                                .observe(this@TransferBroadActivity) { bean ->
                                    bean.amount = inputMoneyString
                                    EventBus.getDefault().postSticky(bean)
                                    val mPop = TransferSureTipBroadPop(this@TransferBroadActivity,mBind?.mTvActualAmount?.text?.toString()?:"")
                                    mPop.sureInvoke = {
                                        goNest()
                                    }
                                    mPop.showPopupWindow()
                                }
                        }
                    } else {
                        EventBus.getDefault().postSticky(PersonInfoBean().apply {
                            this.amount = inputMoneyString
                        })
                        goNest()
                    }

                }
            }

        }

        setupTextWatcherWithFlow()
    }

    private fun checkNestBtn() {
        if (mBind?.mEtInputMoney?.text?.trim()?.toString()
                ?.isNotEmpty() == true && mBind?.mEtInputAddress?.text?.trim()?.toString()
                ?.isNotEmpty() == true
        ) {
            mBind?.mTvSubmit?.isSubmitCanGo(true)
        } else {
            mBind?.mTvSubmit?.isSubmitCanGo(false)
        }
    }

    private fun goNest() {
        //是否为首次提现或转账
        if (isFirst) {
            if (safeCenterBean.twoFactorCode?.isNotEmpty() == true) {
                val tipPop = AuthCodeBroadPop(
                    this@TransferBroadActivity,
                    SafeJumpBroadEnum.TransferAuthSure.sourceType
                )
                tipPop.transferInvoke = {
                    if (safeCenterBean.securityId?.isNotEmpty() == true) {
                        val tipPop = AuthQuestionBroadPop(
                            this@TransferBroadActivity,
                            SafeJumpBroadEnum.TransferAuthSure.sourceType
                        )
                        tipPop.showPopupWindow()

                    } else {
                        val mPop = AuthPasswordBroadPop(
                            this@TransferBroadActivity,
                            SafeJumpBroadEnum.TransferAuthSure.sourceType
                        )
                        mPop.showPopupWindow()
                    }
                }
                tipPop.showPopupWindow()
            } else {
                val tipPop = AuthQuestionBroadPop(
                    this@TransferBroadActivity,
                    SafeJumpBroadEnum.TransferAuthSure.sourceType
                )
                tipPop.showPopupWindow()
            }
        } else {
            val authWayPop = AuthWayBroadPop(this, SafeJumpBroadEnum.TransferAuthSure.sourceType)
            authWayPop.transferInvoke = {
                val mPop = AuthPasswordBroadPop(
                    this@TransferBroadActivity,
                    SafeJumpBroadEnum.TransferAuthSure.sourceType
                )
                mPop.showPopupWindow()
            }
            authWayPop.showPopupWindow()
        }
    }

    private fun setupTextWatcherWithFlow() {
        mBind?.mEtInputMoney!!.textChanges()
            .debounce(500L) // 延迟1秒
            .filter { it.isNotBlank() } // 过滤空文本
            .onEach { query ->
                performNetworkRequest(query.toString())
            }
            .launchIn(lifecycleScope)
    }

    @SuppressLint("SetTextI18n")
    private fun performNetworkRequest(query: String) {
        lifecycleScope.launch {
            //1:转账(入金wx) 2:提现 ，默认为1，type字段不传默认为1，转账，提现需传type=2
            val typeTransfer = if (flag == 1) {
                2
            } else {
                1
            }
            viewModel.getRateInfo(query, typeTransfer,mBind?.mEtInputAddress?.text?.trim()?.toString()?:"").observe(this@TransferBroadActivity) {
                mBind?.mTvActualAmount?.text =
                    (it.actualAmount.toString() + getString(R.string.webX_name))
                mBind?.mTvHandlingFee?.text =
                    "${it.rateAmount}${getString(R.string.webX_name)}(${(it.rateInfo * 100)}%)"
                mBind?.mTvHandlingFee?.isSelected = true
                mBind?.mTvActualAmount?.isSelected = true
            }
        }
    }

    private fun initData() {
        mBind?.mTvHandlingFee?.text = "0.00（0%）"
        mBind?.mTvHandlingFee?.isSelected = false
        mBind?.mTvActualAmount?.text = "0.00"
        mBind?.mTvActualAmount?.isSelected = false
        if (isTransferLinkQrCode(linkString)) {
            mBind?.mEtInputAddress?.setText(
                linkString.replace(
                    CommonBroadConfig.seekWeb4,
                    ""
                )
            )
        }
        lifecycleScope.launch {
            viewModel.goTransferInfo().observe(this@TransferBroadActivity) {
                paymentAddress = it.paymentAddress
                mBind?.mTvSysCurrencyValue?.text = it.balances
                mBind?.mTvSysPointValue?.text = it.pointTotal
                mBind?.mTvMoneyAddressKey?.text = it.paymentAddress

            }
        }
    }

    override fun onResume() {
        super.onResume()
        initAuthState()
    }

    private fun initAuthState() {
        lifecycleScope.launch {
            viewModel.goSecurityCenter().observe(this@TransferBroadActivity) { bean ->
                safeCenterBean = bean
            }
            viewModel.checkSecurityFirst().observe(this@TransferBroadActivity) { bean ->
                if (BuildConfig.DEBUG){
                    Log.d("zhanglong", "bean.result--${bean.result}")
                }
                isFirst = bean.result
            }
        }
    }


    override fun showTransparentTopTitleLayout(): Boolean {
        return true
    }

    override fun useViewBinding(): Boolean {
        return true
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAppEventReceiver(event: TransferEventBroadBean) {
        val inputAddressString =
            mBind?.mEtInputAddress?.text?.trim().toString()
        val inputMoneyString =
            mBind?.mEtInputMoney?.text?.trim().toString()
        if (event.isFinishAction && inputAddressString.isNotEmpty() && inputMoneyString.isNotEmpty()) {
            if (flag == 0) {
                lifecycleScope.launch {
                    viewModel.goTransactionAddOrder(inputAddressString, inputMoneyString)
                        .observe(this@TransferBroadActivity) {
                            startActivity(
                                Intent(
                                    this@TransferBroadActivity,
                                    PaySuccessBroadActivity::class.java
                                )
                            )
                        }
                }
            } else {
                lifecycleScope.launch {
                    viewModel.goWithdrawAddOrder(inputAddressString, inputMoneyString)
                        .observe(this@TransferBroadActivity) {
                            startActivity(
                                Intent(
                                    this@TransferBroadActivity,
                                    PaySuccessBroadActivity::class.java
                                )
                            )
                        }
                }
            }

        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAppEventReceiver(event: ActionEventBroadBean) {
        if (event.isFinishAction) {
            finish()
        }

    }


    override fun onDestroy() {
        EventBus.getDefault().removeAllStickyEvents()
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }


}