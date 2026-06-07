package com.cboda.intelligent.adapter

import com.cboda.intelligent.R
import com.cboda.intelligent.bean.ReportConfigBean
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

class SquareReportTypeAdapter :
    BaseQuickAdapter<ReportConfigBean, BaseViewHolder>(R.layout.item_square_report_type) {

    override fun convert(holder: BaseViewHolder, item: ReportConfigBean) {
        holder.setText(R.id.squareReportTypeTitle, item.displayTitle())
        holder.setGone(R.id.squareReportTypeDesc, item.description.isBlank())
        holder.setText(R.id.squareReportTypeDesc, item.description)
    }
}
