package com.cboda.intelligent.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cboda.intelligent.R
import com.cboda.baselibrary.bean.CommonDataListBroadBean
import com.cboda.intelligent.utils.GlideLoaderBroadHelper
import com.cboda.intelligent.utils.PixelBroadBroadUtils

class QunMemberBroadAdapter(layoutResId: Int,private var fromPage:String = "") :
    BaseQuickAdapter<CommonDataListBroadBean.CommonBean, BaseViewHolder>(layoutResId) {
    override fun convert(holder: BaseViewHolder, item: CommonDataListBroadBean.CommonBean) {

        when(fromPage){
            "zhuanshuhongbao","createGroup"->{
                GlideLoaderBroadHelper.loadCenterCopRoundedCornersUrl(
                    context,
                    item.icon?:"",
                    holder.getView(R.id.ivTouxiang),
                    PixelBroadBroadUtils.dip2px(context, 5f)
                )

                if (item.bei.isNullOrEmpty()){
                    holder.setGone(R.id.tvname,true)
                    holder.setGone(R.id.tvnicheng,true)
                    holder.setGone(R.id.tvname2,false)
                    holder.setText(R.id.tvname2, item.name?:"")
                } else {
                    holder.setGone(R.id.tvname,false)
                    holder.setGone(R.id.tvnicheng,false)
                    holder.setGone(R.id.tvname2,true)
                    holder.setText(R.id.tvname, item.bei?:"")
                    holder.setText(R.id.tvnicheng, "昵称：${item.name?:""}")
                }

                holder.setImageResource(
                    R.id.uxanzview,
                    if (item.isSelect)R.drawable.ic_broad_yixuanz else R.drawable.ic_broad_weixuanz
                )
            }
            else->{
                GlideLoaderBroadHelper.loadCenterCopRoundedCornersUrl(
                    context,
                   item.icon?:"",
                    holder.getView(R.id.ivTouxiang),
                    PixelBroadBroadUtils.dip2px(context, 5f)
                )

                if (item.bei.isNullOrEmpty()){
                    holder.setGone(R.id.tvname,true)
                    holder.setGone(R.id.tvnicheng,true)
                    holder.setGone(R.id.tvname2,false)
                    holder.setText(R.id.tvname2, item.name?:"")
                } else {
                    holder.setGone(R.id.tvname,false)
                    holder.setGone(R.id.tvnicheng,false)
                    holder.setGone(R.id.tvname2,true)
                    holder.setText(R.id.tvname, item.bei?:"")
                    holder.setText(R.id.tvnicheng, "昵称：${item.name?:""}")
                }

                when (fromPage) {
                    "add" -> {
                        if (item.isZy == "1"){
                            holder.setGone(R.id.uxanzview,true)
                            holder.setGone(R.id.uxanzview2,false)

                        } else {
                            holder.setGone(R.id.uxanzview,false)
                            holder.setGone(R.id.uxanzview2,true)
                        }
                        holder.setImageResource(
                            R.id.uxanzview,
                            if (item.isSelect) R.drawable.ic_broad_yixuanz else R.drawable.ic_broad_weixuanz
                        )
                    }
                    "remove" -> {
                        if (item.isZy == "1"){
                            holder.setGone(R.id.uxanzview,true)
                            holder.setGone(R.id.uxanzview2,false)

                        } else {
                            holder.setGone(R.id.uxanzview,false)
                            holder.setGone(R.id.uxanzview2,true)
                        }
                        holder.setImageResource(
                            R.id.uxanzview,
                            if (item.isSelect) R.drawable.ic_broad_yixuanz else R.drawable.ic_broad_weixuanz
                        )
                    }
                    else -> {
                        holder.setImageResource(
                            R.id.uxanzview,
                            if (item.isSelect) R.drawable.ic_broad_yixuanz else R.drawable.ic_broad_weixuanz
                        )
                    }
                }


            }
        }




        if ("zhuanshuhongbao" == fromPage){
            holder.setGone(R.id.uxanzview,true)
        } else {
            holder.setGone(R.id.uxanzview,false)
        }
    }
}