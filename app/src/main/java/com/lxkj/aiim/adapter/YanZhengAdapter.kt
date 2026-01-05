package com.lxkj.aiim.adapter

import android.graphics.Color
import cn.wildfirechat.remote.ChatManager
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.lxkj.aiim.R
import com.cboda.baselibrary.bean.CommonDataListBroadBean
import com.lxkj.aiim.utils.GlideLoaderHelper
import com.lxkj.aiim.utils.PixelBroadUtils

class YanZhengAdapter(layoutResId: Int,private val fromPage:String) :
    BaseQuickAdapter<CommonDataListBroadBean.CommonBean, BaseViewHolder>(layoutResId) {
    override fun convert(holder: BaseViewHolder, item: CommonDataListBroadBean.CommonBean) {
        if (fromPage == "xindepengyou" ||item.icon?.isNotEmpty() == true ){
            GlideLoaderHelper.loadCenterCopRoundedCornersUrl(
                context,
                item.icon?:"",
                holder.getView(R.id.ivTouxiang),
                PixelBroadUtils.dip2px(context,5f),
                R.mipmap.ic_qun_moren
            )
        } else {
            val groupInfo = ChatManager.Instance().getGroupInfo(item.id,false)
            Glide.with(context).load( groupInfo?.portrait?:"").placeholder(com.cboda.chatui.R.mipmap.ic_group_chat)
                .into(  holder.getView(R.id.ivTouxiang))
        }


        holder.setText(R.id.tvname,item.name?:"")
        holder.setText(R.id.tvnicheng,item.sinfo?:"")

        when(item.state){
            "1"->{
                holder.setGone(R.id.tvNotAgree,false)
                holder.setGone(R.id.tvAgree,false)
                holder.setGone(R.id.yitianjia,true)
                holder.setText(R.id.yitianjia,"")
            }
            "2"->{
                holder.setGone(R.id.tvNotAgree,true)
                holder.setGone(R.id.tvAgree,true)
                holder.setGone(R.id.yitianjia,false)
                holder.setText(R.id.yitianjia,context.getString(R.string.yitianjia))
                holder.setTextColor(R.id.yitianjia,Color.parseColor("#A96EDE"))
            }
            else->{
                holder.setGone(R.id.tvNotAgree,true)
                holder.setGone(R.id.tvAgree,true)
                holder.setGone(R.id.yitianjia,false)
                holder.setText(R.id.yitianjia,context.getString(R.string.yijujue))
                holder.setTextColor(R.id.yitianjia,Color.parseColor("#FF4F4F"))
            }
        }

    }
}