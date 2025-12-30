package com.lxkj.aiim.adapter

import android.net.Uri
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.lxkj.aiim.R

class SelectPicAdapter (layoutResId: Int) :
    BaseQuickAdapter<String, BaseViewHolder>(layoutResId) {
    override fun convert(holder: BaseViewHolder, item: String) {
        if (item == "add"){
            holder.setGone(R.id.deleteIcon1,true)
            holder.setImageResource(R.id.pic,R.mipmap.ic_add_pic1)
        } else {
            holder.setGone(R.id.deleteIcon1,false)
            if (item.startsWith("http") || item.startsWith("https")) {
                holder.getView<ImageView>(R.id.pic).setImageURI(Uri.parse(item))
            } else {
                holder.getView<ImageView>(R.id.pic).setImageURI(Uri.parse("file://${item}"))
            }
        }
        holder.getView<AppCompatImageView>(R.id.deleteIcon1)?.tag = item

//        val params = holder.itemView.pic?.layoutParams
//        params?.width = (PixelUtils.getScreenWidth(context) - PixelUtils.dip2px(context,90f))/3
//        params?.height = (PixelUtils.getScreenWidth(context) - PixelUtils.dip2px(context,90f))/3
//        holder.itemView.pic?.layoutParams = params
    }
}