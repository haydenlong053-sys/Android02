package com.lxkj.aiim.adapter

import android.text.TextUtils
import android.widget.ImageView
import com.cboda.baselibrary.base.BaseBroadAdapter
import com.cboda.baselibrary.utils.loadImg
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.luck.picture.lib.entity.LocalMedia
import com.cboda.intelligent.R

class AIImgAdapter : BaseBroadAdapter<Pair<String, LocalMedia?>>(R.layout.ai_img_adapter) {
    override fun onBindData(
        holder: BaseViewHolder,
        bean: Pair<String, LocalMedia?>
    ) {
        holder.apply {
            setGone(R.id.img, TextUtils.isEmpty(bean.first))
            setGone(R.id.group, !TextUtils.isEmpty(bean.first))
            if (!TextUtils.isEmpty(bean.first)) {
                val img = getView<ImageView>(R.id.img)
                img.loadImg(bean.first)
            }
        }
    }
}