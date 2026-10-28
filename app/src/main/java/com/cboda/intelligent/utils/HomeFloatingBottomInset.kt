package com.cboda.intelligent.utils

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.blankj.utilcode.util.SizeUtils

/**
 * 首页悬浮底栏（「社交 / 中心」）叠在内容之上时，给可滚动列表预留底部 inset，
 * 避免末条被遮挡。高度与 [com.cboda.intelligent.compose.ExpandableBottomBar] 的 64.dp 对齐，
 * 并额外留一点呼吸间距 + 系统导航栏。
 */
object HomeFloatingBottomInset {
    /** 与 ExpandableBottomBar.BarHeight 一致 */
    const val BAR_HEIGHT_DP = 64

    /** 末项与底栏之间的额外空隙 */
    const val EXTRA_GAP_DP = 16

    fun contentBottomPx(navigationBarsBottomPx: Int): Int {
        return SizeUtils.dp2px((BAR_HEIGHT_DP + EXTRA_GAP_DP).toFloat()) + navigationBarsBottomPx
    }

    /**
     * 给 View 设置 bottom padding，并保持可滚出 padding 区域（需 XML 已设 clipToPadding=false，
     * 或由本方法一并打开）。
     */
    fun applyTo(view: View, alsoDisableClipToPadding: Boolean = true) {
        if (alsoDisableClipToPadding) {
            (view as? android.view.ViewGroup)?.clipToPadding = false
        }
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val navBottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            v.updatePadding(bottom = contentBottomPx(navBottom))
            insets
        }
        ViewCompat.requestApplyInsets(view)
    }
}
