package com.cboda.intelligent.dialog.pop

import android.content.Context
import android.graphics.Color
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import androidx.compose.ui.text.toLowerCase
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.util.StringUtils.getString
import com.cboda.baselibrary.popup.BaseBottomBroadPop
import com.cboda.baselibrary.toast.ToastBroadUtils
import com.cboda.intelligent.R
import com.cboda.intelligent.databinding.AddTransAddressPopBinding
import com.cboda.intelligent.databinding.DialogPopAuthBroadQuestionBinding
import com.cboda.intelligent.utils.click
import com.cboda.intelligent.utils.isSubmitCanGo
import com.cboda.intelligent.utils.pasteToEditText
import com.cboda.intelligent.viewmodel.SafeCenterViewBroadModel
import com.cboda.intelligent.viewmodel.TransferViewBroadModel
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class AddTransAddressPop(val context: Context) : BaseBottomBroadPop(context) {
    private val scope = MainScope() // 创建主线程的协程作用域
    private var viewModel: TransferViewBroadModel? = null

    @JvmField
    var backInvoke: (String) -> Unit = {}
    private val binding: AddTransAddressPopBinding = AddTransAddressPopBinding.inflate(
        LayoutInflater.from(context)
    )

    init {
        contentView = binding.root
        setBackgroundColor(Color.parseColor("#8f000000"))
    }

    override fun onViewCreated(contentView: View) {
        super.onViewCreated(contentView)
        if (context is FragmentActivity) {
            val activity = context
            viewModel = ViewModelProvider(activity)[TransferViewBroadModel::class.java]
            binding.mTvSubmit.click {
                val address = binding.mEtInputAddress.text.toString().trim()
                if (!TextUtils.isEmpty(address)) {
                    if (!address.lowercase().startsWith("wx", true)) {
                        ToastBroadUtils.show(context.getString(R.string.add_trans_receiving_address_format_error))
                        return@click
                    }
                    scope.launch {
                        viewModel?.addTransferAddress(address)?.observe(activity) {
                            if (it) {
                                backInvoke.invoke(address)
                            }
                        }
                    }
                }
            }
        }
        binding.mIvClose.click {
            dismiss()
        }
        binding.cvTv.click {
            pasteToEditText(binding.mEtInputAddress)
        }

        binding.mEtInputAddress.addTextChangedListener { editable ->
            val address = binding.mEtInputAddress.text.toString().trim()
            binding.mTvSubmit.isSubmitCanGo(!TextUtils.isEmpty(address))
        }
    }


    override fun onDismiss() {
        scope.cancel()
        super.onDismiss()
    }
}