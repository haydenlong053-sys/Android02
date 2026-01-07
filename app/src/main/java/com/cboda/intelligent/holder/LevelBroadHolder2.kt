package com.cboda.intelligent.holder

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import com.cboda.intelligent.utils.PixelBroadBroadUtils

/**
 * 简述： <功能描述>
 * Created by ysh on 2023/7/13.
 */
class LevelBroadHolder2(
    private val context: Context?,
    private val levelIcon: AppCompatImageView?,
    private val levelTxt: TextView? = null,

    ) {



    /**
     * 财富等级
     */
    fun setWealthLevel(level: Int) {
        levelTxt?.text = level.toString()
        var mLevel = level
        when (mLevel) {
            in 1..10 -> {
                mLevel = 1
            }
            in 11..20 -> {
                mLevel = 2
            }
            in 21..30 -> {
                mLevel = 3
            }
            in 31..40 -> {
                mLevel = 4
            }
            in 41..50 -> {
                mLevel = 5
            }
            in 51..60 -> {
                mLevel = 6
            }
            in 61..70 -> {
                mLevel = 7
            }
            in 71..80 -> {
                mLevel = 8
            }
            in 81..90 -> {
                mLevel = 9
            }
            in 91..120 -> {
                mLevel = 10
            }
        }
        val reId =
            context?.resources?.getIdentifier("ic_caifu_${mLevel}", "mipmap", context.packageName)
        levelIcon?.setImageResource(reId ?: 0)
        levelIcon?.visibility = if (reId == 0) View.GONE else View.VISIBLE
        levelTxt?.visibility = if (reId == 0) View.GONE else View.VISIBLE
        val params  = levelIcon?.layoutParams
        params?.width = PixelBroadBroadUtils.dip2px(context,40f)
        params?.height = PixelBroadBroadUtils.dip2px(context,20f)
        levelIcon?.layoutParams = params

    }


    /**
     * 人气等级
     */
    fun setPopularityLevel(level: Int) {
        levelTxt?.text = level.toString()
        var mLevel = level
        when (mLevel) {
            in 1..6 -> {
                mLevel = 1
            }
            in 7..12 -> {
                mLevel = 2
            }
            in 13..19 -> {
                mLevel = 3
            }
            in 20..24 -> {
                mLevel = 4
            }
            in 25..29 -> {
                mLevel = 5
            }
            in 30..35 -> {
                mLevel = 6
            }
            in 36..41 -> {
                mLevel = 7
            }
            in 42..47 -> {
                mLevel = 8
            }
            in 48..53 -> {
                mLevel = 9
            }
            in 54..60 -> {
                mLevel = 10
            }
        }


        val reId =
            context?.resources?.getIdentifier("ic_renqi_${mLevel}", "mipmap", context.packageName)
        levelIcon?.setImageResource(reId ?: 0)
        levelIcon?.visibility = if (reId == 0) View.GONE else View.VISIBLE
        levelTxt?.visibility = if (reId == 0) View.GONE else View.VISIBLE
        val params  = levelIcon?.layoutParams
        params?.width = PixelBroadBroadUtils.dip2px(context,40f)
        params?.height = PixelBroadBroadUtils.dip2px(context,20f)
        levelIcon?.layoutParams = params

    }


}