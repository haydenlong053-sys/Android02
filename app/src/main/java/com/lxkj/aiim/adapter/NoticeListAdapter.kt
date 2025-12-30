package com.lxkj.aiim.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.lxkj.aiim.R
import com.lxkj.aiim.lj.bean.CommonSelectBean
import com.lxkj.aiim.lj.utils.GlideUtil
import com.lxkj.aiim.model.NoticeInfoBean
import com.lxkj.aiim.model.OrderInfoBean

class NoticeListAdapter(
    layoutResId: Int = R.layout.item_rv_notice
) :
    BaseQuickAdapter<NoticeInfoBean, BaseViewHolder>(layoutResId) {
    override fun convert(holder: BaseViewHolder, item: NoticeInfoBean) {
        if (item.isRead) {
            holder.setBackgroundResource(R.id.mViewReadBlue, R.drawable.bg_circle_3b37dd_20)
            holder.setTextColorRes(R.id.mTvDes,R.color.black_or_white_65)
        } else {
            holder.setBackgroundResource(R.id.mViewReadBlue, R.drawable.bg_circle_3b37dd)
            holder.setTextColorRes(R.id.mTvDes,R.color.black_or_white)
        }
        holder.setText(R.id.mTvDes,item.noticeTitle)
        holder.setText(R.id.mTvTime,item.createTime)
        if (item.imageUrl?.isNotEmpty() == true){
            holder.setGone(R.id.mIvPic,false)
            GlideUtil.setImag(context,item.imageUrl,holder.getView(R.id.mIvPic))

        }else{
            holder.setGone(R.id.mIvPic,true)
        }
        holder.setGone(R.id.mViewLine, holder.layoutPosition == (this.data.size - 1))
    }
}