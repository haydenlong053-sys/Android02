package com.cboda.intelligent.activity

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.ClipboardUtils
import com.cboda.intelligent.R
import com.cboda.intelligent.base.BaseSimple2BroadActivity
import com.cboda.intelligent.databinding.AddTransferAddressActivityBinding
import com.cboda.intelligent.dialog.pop.AddTransAddressPop
import com.cboda.intelligent.utils.click
import com.cboda.intelligent.utils.showToastMain
import com.cboda.intelligent.viewmodel.TransferViewBroadModel
import kotlinx.coroutines.launch
import kotlin.getValue

class AddTransferAddressActivity : BaseSimple2BroadActivity() {
    private val viewModel: TransferViewBroadModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBind = (binding as? AddTransferAddressActivityBinding)
        getToolbarTitle()?.text = "提现地址"
        getRightImg()?.click {
            showAddPop(false)
        }
        mBind?.editAddressIv?.click {
            ClipboardUtils.copyText(mBind?.address?.text?.toString())
            showToastMain("已复制到剪贴板！")
        }
        mBind?.addressLayout?.click {
            setResult(mBind?.address?.text?.toString())
        }
        // 侧滑露出的「复制」
        mBind?.swipeActionCopy?.click {
            ClipboardUtils.copyText(mBind?.address?.text?.toString())
            showToastMain("已复制到剪贴板！")
            mBind?.swipeRevealLayout?.closeSwipe()
        }
        // 侧滑露出的「删除」
        mBind?.swipeActionDelete?.click {
            AlertDialog.Builder(this)
                .setMessage("确定删除该提现地址？")
                .setNegativeButton(getString(R.string.quxiao), null)
                .setPositiveButton(getString(R.string.queding)) { _, _ ->
//                    lifecycleScope.launch {
//                        viewModel.deleteTransferAddress().observe(this@AddTransferAddressActivity) { success ->
//                            if (success) {
//                                showToastMain("已删除")
//                                refreshAddress()
//                            }
//                        }
//                    }
                }
                .show()
        }
        lifecycleScope.launch {
            viewModel.getTransferAddress().observe(this@AddTransferAddressActivity) {
                if (null != it) {
                    mBind?.swipeRevealLayout?.isGone = false
                    mBind?.addressLayout?.isGone = false
                    mBind?.address?.text = it.toAddress
                    mBind?.time?.text = it.createTime
                } else {
                    mBind?.swipeRevealLayout?.isGone = true
                    getRightImg()?.setImageResource(R.mipmap.ic_add_broad_black)
                    showAddPop(true)
                }
            }
        }
    }

    private fun refreshAddress() {
        lifecycleScope.launch {
            viewModel.getTransferAddress().observe(this@AddTransferAddressActivity) {
                if (null != it) {
                    mBind?.swipeRevealLayout?.isGone = false
                    mBind?.addressLayout?.isGone = false
                    mBind?.address?.text = it.toAddress
                    mBind?.time?.text = it.createTime
                } else {
                    mBind?.swipeRevealLayout?.isGone = true
                    getRightImg()?.setImageResource(R.mipmap.ic_add_broad_black)
                    showAddPop(true)
                }
            }
        }
    }


    private fun setResult(result: String?) {
        val resultIntent = Intent()
        resultIntent.putExtra("address", result ?: "")
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    private fun showAddPop(isReturn: Boolean) {
        val addPop = AddTransAddressPop(this)
        addPop.backInvoke = {
            if (isReturn) {
                setResult(it)
            }
        }
        addPop.showPopupWindow()
    }


    private var mBind: AddTransferAddressActivityBinding? = null

    //    private val viewModel: CommViewBroadModel by viewModels()
    override fun getViewBindingClass() = AddTransferAddressActivityBinding::class.java
    override fun showTransparentTopTitleLayout() = true
    override fun useViewBinding() = true
    override fun showOrHideTitleLayout() = true
    override fun useStatusBarDarkFont() = true

}