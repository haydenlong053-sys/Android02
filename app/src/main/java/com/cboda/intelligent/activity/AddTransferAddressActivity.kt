package com.cboda.intelligent.activity

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.viewModels
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
//        mIvBgTop.setImageResource(com.cboda.baselibrary.R.color.white)
//        mIvBgTop.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
//        mIvBgTop.requestLayout() // 或直接调用 setLayoutParams 会自动触发
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
        lifecycleScope.launch {
            viewModel.getTransferAddress().observe(this@AddTransferAddressActivity) {
                if (null != it) {
                    mBind?.addressLayout?.isGone = false
                    mBind?.address?.text = it.toAddress
                    mBind?.time?.text = it.createTime
                } else {
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