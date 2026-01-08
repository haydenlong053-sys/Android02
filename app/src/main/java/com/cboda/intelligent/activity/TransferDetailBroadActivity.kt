package com.cboda.intelligent.activity

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.cboda.intelligent.R
import com.cboda.intelligent.base.BaseSimple2BroadActivity
import com.cboda.intelligent.bean.ActionEventBroadBean
import com.cboda.intelligent.databinding.ActivityTansBroadFerDetailBinding
import com.cboda.intelligent.utils.copyToClipboard
import com.cboda.intelligent.model.OrderInfoBean

import com.cboda.intelligent.viewmodel.TransferViewBroadModel

import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus


/**
 * 转账详情
 */
class TransferDetailBroadActivity : BaseSimple2BroadActivity() {
    private val viewModel: TransferViewBroadModel by viewModels()

    //0是转账，1是提现
    private var flag = 0
    private var userId = ""
    private var mBind: ActivityTansBroadFerDetailBinding? = null

    override fun getViewBindingClass(): Class<out ViewBinding>? {
        return ActivityTansBroadFerDetailBinding::class.java
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBind = binding as? ActivityTansBroadFerDetailBinding
        flag = intent.getIntExtra("type", 0)
        userId = intent.getStringExtra("id") ?: ""

        getToolbarTitle()?.text = if (flag == 1) {
            getResources().getString(R.string.mine_withdraw_detail)
        } else {
            getResources().getString(R.string.mine_t_detail)
        }
        getRightImg()?.setImageResource(R.mipmap.ic_top_close)

        mBind?.mTvLinkman?.text =
            String.format(getString(R.string.mine_t_add_linkman), "")
        mBind?.mLlHash?.isVisible =(flag!=0)

        initListener()
        initData()
    }

    private fun initListener() {
        getRightImg()?.setOnClickListener {
            EventBus.getDefault().post(ActionEventBroadBean().apply {
                isFinishAction = true
            })
            finish()
        }
        mBind?.mLlHash?.setOnClickListener {
            copyToClipboard(this@TransferDetailBroadActivity, mBind?.mTvHash?.text?.toString())
        }
        mBind?.mLlOrderNo?.setOnClickListener {
            copyToClipboard(this@TransferDetailBroadActivity, mBind?.mTvOrderNo?.text?.toString())
        }
    }

    private fun initData() {
        lifecycleScope.launch {
            if (flag==0){
                viewModel.goTransactionOrderDetail(userId).observe(this@TransferDetailBroadActivity) {
                    showInfo(it)
                }
            }else{
                viewModel.goWithdrawOrderDetail(userId).observe(this@TransferDetailBroadActivity) {
                    showInfo(it)
                }
            }


        }

    }

    @SuppressLint("SetTextI18n")
    private fun showInfo(item: OrderInfoBean) {
        //flag---0是转账，1是提现
        if (flag == 0) {
            mBind?.mTvAddressName?.text = getString(R.string.mine_transfer_address)
            mBind?.mTvHashName?.text = getString(R.string.mine_transfer_hash)
            //(paymentStatus---0:处理中 1：成功 2：失败)
            when (item.paymentStatus) {
                0 -> {
                    mBind?.mTvStateName?.text = getString(R.string.mine_transfer_ing)
                    mBind?.mIvState?.setImageResource(R.mipmap.ic_icon_state_ing)
                }

                1 -> {
                    mBind?.mTvStateName?.text = getString(R.string.mine_transfer_success)
                    mBind?.mIvState?.setImageResource(R.mipmap.ic_icon_state_success)
                }

                2 -> {
                    mBind?.mTvStateName?.text = getString(R.string.mine_Transfer_fail)
                    mBind?.mIvState?.setImageResource(R.mipmap.ic_icon_state_fail)
                }
            }

        } else {
            mBind?.mTvAddressName?.text = getString(R.string.mine_t_Withdrawal_address)
            mBind?.mTvHashName?.text = getString(R.string.mine_t_Withdrawal_hash)
            //(0:发起提现 1:正在提现 2:提现成功 3:提现失败 4:申请中 5:被拒绝)
            when (item.status) {
                0,1,4 -> {
                    mBind?.mTvStateName?.text = getString(R.string.mine_t_Withdrawal_ing)
                    mBind?.mIvState?.setImageResource(R.mipmap.ic_icon_state_ing)
                }

                2 -> {
                    mBind?.mTvStateName?.text = getString(R.string.mine_t_Withdrawal_ok)
                    mBind?.mIvState?.setImageResource(R.mipmap.ic_icon_state_success)
                }

                3,5 -> {
                    mBind?.mTvStateName?.text = getString(R.string.mine_t_Withdrawal_fail)
                    mBind?.mIvState?.setImageResource(R.mipmap.ic_icon_state_fail)
                }
            }
        }
        if (item.amount>0){
            mBind?.mTvMoney?.text = "-${item.amount.toString()}"
        }else{
            mBind?.mTvMoney?.text = item.amount.toString()
        }

        if (item.remark?.isNotEmpty() == true){
            mBind?.mLlFailReason?.isVisible =true
            mBind?.mTvFailDes?.text = item.remark
        }

        mBind?.mTvAddress?.text = item.receivingAddress
        mBind?.mTvHash?.text = item.withdrawalHash
        mBind?.mTvOrderTime?.text = item.createTime
        mBind?.mTvOrderNo?.text = item.transactionNumber
        mBind?.mTvLinkman?.text =
            String.format(getString(R.string.mine_t_add_linkman), item.nickName)
    }


    override fun showTransparentTopTitleLayout(): Boolean {
        return true
    }

    override fun useViewBinding(): Boolean {
        return true
    }


}