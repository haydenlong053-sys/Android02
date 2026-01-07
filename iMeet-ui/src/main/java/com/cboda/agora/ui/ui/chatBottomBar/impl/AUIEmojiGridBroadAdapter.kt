package com.cboda.agora.ui.ui.chatBottomBar.impl

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.cboda.agora.ui.R
//import com.cboda.agora.ui.ui.basic.AUIImageBroadView
import com.cboda.agora.ui.chatBottomBar.AUIChatBottomBarIcon
import com.cboda.agora.ui.ui.basic.AUIImageBroadView

class AUIEmojiGridBroadAdapter(
    context: Context,
    resource: Int,
    objects: MutableList<AUIChatBottomBarIcon>
) : ArrayAdapter<AUIChatBottomBarIcon>(context, resource, objects) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.aui_emoji_item_broad_layout, null)
        }
        val imageView = convertView!!.findViewById<AUIImageBroadView>(R.id.iv_expression)
        val emojIcon: AUIChatBottomBarIcon? = getItem(position)
        emojIcon.let {
            if (emojIcon?.iconRes != 0) {
                emojIcon?.iconRes?.let { imageView.setImageResource(it) }
            } else if (emojIcon?.iconUrl != null) {
                Glide.with(context).load(emojIcon.iconUrl)
                    .apply(RequestOptions.placeholderOf(R.drawable.voice_icon_default_expression))
                    .into(imageView)
            }
            return convertView
        }
    }

}