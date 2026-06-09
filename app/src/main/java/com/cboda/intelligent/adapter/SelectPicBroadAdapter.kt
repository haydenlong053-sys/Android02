package com.cboda.intelligent.adapter

import android.net.Uri
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cboda.baselibrary.utils.VideoThumbnailUtils
import com.cboda.intelligent.R

class SelectPicBroadAdapter(layoutResId: Int) :
    BaseQuickAdapter<String, BaseViewHolder>(layoutResId) {

    override fun convert(holder: BaseViewHolder, item: String) {
        if (item == ADD_ITEM) {
            holder.setGone(R.id.deleteIcon1, true)
            holder.setGone(R.id.videoPlayIcon, true)
            holder.setImageResource(R.id.pic, R.mipmap.ic_add_broad_pic1)
        } else if (isVideoItem(item)) {
            holder.setGone(R.id.deleteIcon1, false)
            holder.setVisible(R.id.videoPlayIcon, true)
            val videoPath = unwrapVideoPath(item).orEmpty()
            VideoThumbnailUtils.getVideoFirstFrame(context, videoPath, object : VideoThumbnailUtils.ThumbnailCallback {
                override fun onSuccess(bitmap: android.graphics.Bitmap?) {
                    holder.getView<ImageView>(R.id.pic).setImageBitmap(bitmap)
                }

                override fun onError(message: String?) {
                    holder.setImageResource(R.id.pic, R.mipmap.ic_broad_video)
                }
            })
        } else {
            holder.setGone(R.id.deleteIcon1, false)
            holder.setGone(R.id.videoPlayIcon, true)
            if (item.startsWith("http") || item.startsWith("https")) {
                Glide.with(context).load(item).into(holder.getView(R.id.pic))
            } else {
                holder.getView<ImageView>(R.id.pic).setImageURI(Uri.parse("file://$item"))
            }
        }
        holder.getView<AppCompatImageView>(R.id.deleteIcon1)?.tag = item
    }

    companion object {
        const val ADD_ITEM = "add"
        private const val VIDEO_PREFIX = "video://"

        fun wrapVideoPath(path: String): String = VIDEO_PREFIX + path

        fun unwrapVideoPath(item: String): String? {
            return if (item.startsWith(VIDEO_PREFIX)) item.removePrefix(VIDEO_PREFIX) else null
        }

        fun isVideoItem(item: String): Boolean = item.startsWith(VIDEO_PREFIX)
    }
}
