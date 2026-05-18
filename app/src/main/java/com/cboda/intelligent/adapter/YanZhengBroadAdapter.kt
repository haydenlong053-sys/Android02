package com.cboda.intelligent.adapter

import android.widget.ImageView
import cn.wildfirechat.remote.ChatManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cboda.intelligent.R
import com.cboda.baselibrary.bean.CommonDataListBroadBean
import androidx.core.graphics.toColorInt
import com.cboda.baselibrary.utils.loadImg
import com.cboda.baselibrary.widge.VpGradeView
import com.cboda.intelligent.activity.VerifyBroadActivity.Companion.GROUP_INVITED

class YanZhengBroadAdapter(layoutResId: Int, private val fromPage: String) :
    BaseQuickAdapter<CommonDataListBroadBean.CommonBean, BaseViewHolder>(layoutResId) {
    override fun convert(holder: BaseViewHolder, item: CommonDataListBroadBean.CommonBean) {
        val ivTouxiang = holder.getView<VpGradeView>(R.id.ivTouxiang)
        if (fromPage == "xindepengyou" || item.icon?.isNotEmpty() == true) {
//            GlideLoaderBroadHelper.loadCenterCopRoundedCornersUrl(
//                context,
//                item.icon?:"",
//                holder.getView(R.id.ivTouxiang),
//                PixelBroadBroadUtils.dip2px(context,5f),
//                R.mipmap.ic_qun_broad_moren
//            )
            ivTouxiang.setData2(item.icon, item.isLiang == "1", item.isvip == "1", item.grade, item.registDay)
//            holder.getView<ImageView>(R.id.ivTouxiang).loadImg(item.icon ?: "")
        } else {
            val groupInfo = ChatManager.Instance().getGroupInfo(item.id, false)
//            Glide.with(context).load( groupInfo?.portrait?:"").placeholder(com.cboda.chatui.R.mipmap.ic_group_broad_chat).into(  holder.getView(R.id.ivTouxiang))
            ivTouxiang.loadUserImg(groupInfo?.portrait ?: "")
//            holder.getView<ImageView>(R.id.ivTouxiang).loadImg(groupInfo?.portrait ?: "")
        }


        holder.setText(R.id.tvname, item.name ?: "")
        if (fromPage == GROUP_INVITED) {
            holder.setText(R.id.tvnicheng, item.info ?: "")
        } else {
            holder.setText(R.id.tvnicheng, item.sinfo ?: "")
        }


        when (item.state) {
            "1" -> {
                holder.setGone(R.id.tvNotAgree, false)
                holder.setGone(R.id.tvAgree, false)
                holder.setGone(R.id.yitianjia, true)
                holder.setText(R.id.yitianjia, "")
            }

            "2" -> {
                holder.setGone(R.id.tvNotAgree, true)
                holder.setGone(R.id.tvAgree, true)
                holder.setGone(R.id.yitianjia, false)
                holder.setText(R.id.yitianjia, context.getString(R.string.yitianjia))
                holder.setTextColor(R.id.yitianjia, "#59000000".toColorInt())
            }

            else -> {
                holder.setGone(R.id.tvNotAgree, true)
                holder.setGone(R.id.tvAgree, true)
                holder.setGone(R.id.yitianjia, false)
                holder.setText(R.id.yitianjia, context.getString(R.string.yijujue))
                holder.setTextColor(R.id.yitianjia, "#EB534B".toColorInt())
            }
        }

    }
}