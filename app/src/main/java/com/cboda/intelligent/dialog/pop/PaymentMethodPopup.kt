package com.cboda.intelligent.dialog.pop

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.AppUtils.isAppInstalled
import com.cboda.baselibrary.popup.BaseBottomBroadPop
import com.cboda.baselibrary.utils.click
import com.cboda.intelligent.adapter.PaymentMethodBroadAdapter
import com.cboda.intelligent.bean.PaymentMethodBroadBean
import com.cboda.intelligent.bean.PaymentMethodDefaults
import com.cboda.intelligent.databinding.PaymentMethodPopupBinding
import com.cboda.intelligent.utils.showToastMain
import razerdp.basepopup.BasePopupWindow

/**
 * 支付方式选择弹窗。
 */
class PaymentMethodPopup(
    context: Context,
//    private val initialSelectedId: String? = PaymentMethodDefaults.META_MASK
) : BasePopupWindow(context) {

    private val binding = PaymentMethodPopupBinding.inflate(LayoutInflater.from(context))
    private val adapter = PaymentMethodBroadAdapter()
    private var onSelected: ((PaymentMethodBroadBean) -> Unit)? = null

    fun setOnSelected(l: (PaymentMethodBroadBean) -> Unit) {
        onSelected = l
    }


    init {
        contentView = binding.root
        popupGravity = Gravity.BOTTOM
        setBackgroundColor(Color.parseColor("#8f000000"))
    }

    override fun onViewCreated(contentView: View) {
        super.onViewCreated(contentView)
//        adapter.setSelectedId(initialSelectedId)
        val methods = PaymentMethodDefaults.defaultList()
        binding.paymentMethodList.layoutManager = LinearLayoutManager(context)
        binding.paymentMethodList.adapter = adapter
        adapter.setList(methods)

        binding.paymentMethodClose.click { dismiss() }
        binding.paymentMethodCancel.click { dismiss() }
        adapter.setOnItemClickListener { _, _, position ->
            val item = methods.getOrNull(position) ?: return@setOnItemClickListener
            if (isAppInstalled(item.packName)) {
                adapter.setSelectedId(item.id)
                onSelected?.invoke(item)
            } else {
                showToastMain("未安装应用，请先安装应用!")
            }

//            dismiss()
        }
    }
}
