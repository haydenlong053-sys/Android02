package com.cboda.intelligent.adapter.im

import android.widget.ImageView
import com.cboda.baselibrary.utils.loadImg
import com.cboda.intelligent.R
import com.cboda.intelligent.utils.GlideLoaderBroadHelper
import com.cboda.intelligent.utils.PixelBroadBroadUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.freddy.im.http.model.im.GroupUserVo

/** 自研 IM 群管理列表（对齐 {@link com.cboda.intelligent.adapter.QunGuanLiBroadAdapter}）。 */
class ImQunGuanLiBroadAdapter(
    layoutResId: Int,
    private val type: String = ""
) : BaseQuickAdapter<GroupUserVo, BaseViewHolder>(layoutResId) {

    override fun convert(holder: BaseViewHolder, item: GroupUserVo) {
//        GlideLoaderBroadHelper.loadCenterCopRoundedCornersUrl(
//            context,
//            item.headPortrait ?: "",
//            holder.getView(R.id.ivTouxiang),
//            PixelBroadBroadUtils.dip2px(context, 5f)
//        )
        holder.setText(R.id.tvname, item.displayName)
        holder.getView<ImageView>(R.id.ivTouxiang).loadImg(item.headPortrait ?: "")

        if (type == "jiechujinyan") {
            holder.setText(R.id.tvNotAgree, context.getString(R.string.jiechujianyan))
        }
    }
}
