package com.cboda.intelligent.widge

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.widget.TextView
import androidx.core.view.isInvisible
import com.cboda.baselibrary.utils.MMKVBroadUtils
import com.cboda.baselibrary.utils.getAppColor
import com.cboda.baselibrary.utils.setTxt
import com.cboda.baselibrary.view.magicindicator.buildins.commonnavigator.titles.badge.BadgePagerTitleBroadView
import com.cboda.intelligent.R
import com.cboda.select.picture.utils.LanguageUtil

class CustomBadgePagerTitleView(context: Context) : BadgePagerTitleBroadView(context) {

    private val titleText: TextView
    private val badgeText: TextView
    init {
        val isAdjustMode = LanguageUtil.getLanList(MMKVBroadUtils.getAppLanguageKey(context)).startsWith("zh")
        val resource =if (isAdjustMode){
            R.layout.layout_item_badge_title_zh
        }else{
            R.layout.layout_item_badge_title
        }

        val view = LayoutInflater.from(context)
            .inflate(resource, this, true)
        titleText = view.findViewById(R.id.tvTitle)
        badgeText = view.findViewById(R.id.tvBadge)
    }

    fun setText(title: Int) {
        titleText.setTxt(title)
    }

    fun setBadgeCount(count: Int) {
        if (count > 0) {
            badgeText.isInvisible = false
            badgeText.text = if (count > 99) "99+" else "$count"
        } else {
            badgeText.isInvisible = true
        }
    }

    override fun onSelected(position: Int, totalCount: Int) {
        titleText.setTypeface(Typeface.DEFAULT, Typeface.NORMAL)
        titleText.setTextColor(getAppColor(context, com.cboda.baselibrary.R.color.color_2563EB))
    }

    override fun onDeselected(position: Int, totalCount: Int) {
        titleText.setTypeface(Typeface.DEFAULT, Typeface.NORMAL)
        titleText.setTextColor(getAppColor(context, com.cboda.baselibrary.R.color.black33))
    }
}