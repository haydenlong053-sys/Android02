package com.cboda.intelligent.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cboda.intelligent.R
import com.cboda.intelligent.utils.GlideBroadUtil
import com.cboda.intelligent.model.NoticeInfoBroadBean

class NoticeListBroadAdapter(
    layoutResId: Int = R.layout.item_rv_broad_notice
) :
    BaseQuickAdapter<NoticeInfoBroadBean, BaseViewHolder>(layoutResId) {
    override fun convert(holder: BaseViewHolder, item: NoticeInfoBroadBean) {
        if (item.isRead) {
            holder.setBackgroundResource(R.id.mViewReadBlue, R.drawable.bg_circle_3b37dd_broad_20)
            holder.setTextColorRes(R.id.mTvDes,R.color.black_or_white_65)
        } else {
            holder.setBackgroundResource(R.id.mViewReadBlue, R.drawable.bg_circle_broad_3b37dd)
            holder.setTextColorRes(R.id.mTvDes,R.color.black_or_white)
        }
        holder.setText(R.id.mTvDes,item.noticeTitle)
        holder.setText(R.id.mTvTime,item.createTime)
        if (item.imageUrl?.isNotEmpty() == true){
            holder.setGone(R.id.mIvPic,false)
            GlideBroadUtil.setImag(context,item.imageUrl,holder.getView(R.id.mIvPic))

        }else{
            holder.setGone(R.id.mIvPic,true)
        }
        holder.setGone(R.id.mViewLine, holder.layoutPosition == (this.data.size - 1))
    }
}