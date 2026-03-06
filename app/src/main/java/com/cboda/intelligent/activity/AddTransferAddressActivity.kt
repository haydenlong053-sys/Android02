package com.cboda.intelligent.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.ClipboardUtils
import com.cboda.baselibrary.utils.getAppStr
import com.cboda.baselibrary.utils.showCommonStrTip
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
            showLxKfDialog()
        }
        mBind?.addressLayout?.click {
            setResult(mBind?.address?.text?.toString())
        }
        // 侧滑露出的「复制」
        mBind?.swipeActionCopy?.click {
            ClipboardUtils.copyText(mBind?.address?.text?.toString())
            mBind?.swipeRevealLayout?.closeSwipe()
            showToastMain("已复制到剪贴板！")
        }
        // 侧滑露出的「删除」
        mBind?.swipeActionDelete?.click {
            mBind?.swipeRevealLayout?.closeSwipe()
            showLxKfDialog()
        }
        refreshAddress(true)
    }

    private fun showLxKfDialog() {
        showCommonStrTip("暂时不可操作", "请联系客服008号", "", getAppStr(R.string.mine_ok, this@AddTransferAddressActivity), this@AddTransferAddressActivity) {
        }
    }

    private fun refreshAddress(isReturn: Boolean) {
        lifecycleScope.launch {
            viewModel.getTransferAddress().observe(this@AddTransferAddressActivity) {
                if (null != it && it.isNotEmpty()) {
                    mBind?.cardViewLayout?.isGone = false
                    mBind?.addressLayout?.isGone = false
                    mBind?.address?.text = it[0].toAddress
                    mBind?.time?.text = it[0].createTime
                } else {
                    mBind?.cardViewLayout?.isGone = true
                    getRightImg()?.setImageResource(R.mipmap.ic_add_broad_black)
                    showAddPop(isReturn)
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
    override fun getViewBindingClass() = AddTransferAddressActivityBinding::class.java
    override fun showTransparentTopTitleLayout() = true
    override fun useViewBinding() = true
    override fun showOrHideTitleLayout() = true
    override fun useStatusBarDarkFont() = true

}