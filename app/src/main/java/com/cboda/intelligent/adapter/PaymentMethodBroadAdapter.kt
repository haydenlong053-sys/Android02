package com.cboda.intelligent.adapter

import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cboda.baselibrary.utils.getAppColor
import com.cboda.intelligent.R
import com.cboda.intelligent.bean.PaymentMethodBroadBean
import com.hjq.shape.layout.ShapeConstraintLayout

class PaymentMethodBroadAdapter :
    BaseQuickAdapter<PaymentMethodBroadBean, BaseViewHolder>(R.layout.item_payment_method_option) {

    private var selectedId: String? = null

    fun setSelectedId(id: String?) {
        selectedId = id
        notifyDataSetChanged()
    }

    fun getSelectedId(): String? = selectedId

    override fun convert(holder: BaseViewHolder, item: PaymentMethodBroadBean) {
        val selected = item.id == selectedId
        holder.setImageResource(R.id.paymentOptionIcon, item.iconRes)
        holder.setText(R.id.paymentOptionName, item.name)
        holder.setVisible(R.id.paymentOptionSelectedBadge, selected)

        val container = holder.getView<ShapeConstraintLayout>(R.id.paymentOptionContainer)
        container.shapeDrawableBuilder.solidColor = getAppColor(
            context,
            if (selected) com.cboda.baselibrary.R.color.white else com.cboda.baselibrary.R.color.bg_main2,
        )
        container.shapeDrawableBuilder.strokeColor = getAppColor(
            context,
            com.cboda.baselibrary.R.color.color_3B47DD,
        )
        container.shapeDrawableBuilder.strokeWidth = if (selected) SizeUtils.dp2px(1f) else 0
        container.shapeDrawableBuilder.intoBackground()
    }
}
