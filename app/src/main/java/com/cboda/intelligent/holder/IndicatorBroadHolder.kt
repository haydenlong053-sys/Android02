package com.cboda.intelligent.holder

import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.TextView
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.cboda.intelligent.R

/**
 * 简述： 指示器
 * Created by ysh on 2023/4/13.
 */
class IndicatorBroadHolder(
    private val context: Context?,
    private val mTitleDataList: ArrayList<String>?,
    private val indicator: TabLayout?,
    private val viewPager: ViewPager?,
    private val pressColor: String = "#333333",
    private val normalColor: String = "#333333",
    private val indicator2: TabLayout?,
    private val inditorColor: String = "#FFFFFF",
    ) {

    fun bindIndicator() {
        mTitleDataList?.forEachIndexed { index, title ->
            indicator?.apply {
                val tabItem = this.newTab()
                tabItem.setCustomView(R.layout.tab_item_broad_layout)
                val tvSelectName = tabItem.customView?.findViewById<TextView>(R.id.tvSelectName)
                val tvTabName = tabItem.customView?.findViewById<TextView>(R.id.tvTabName)
                tvSelectName?.setTextColor(Color.parseColor(pressColor))
                tvTabName?.setTextColor(Color.parseColor(normalColor))
                tvTabName?.text = title
                tvSelectName?.text = title
                tvSelectName?.visibility = if (index == 0) View.VISIBLE else View.GONE
                tvTabName?.visibility = if (index != 0) View.VISIBLE else View.GONE
                tabItem.customView?.findViewById<View>(R.id.tabIndicator)?.visibility =
                    if (index == 0) View.VISIBLE else View.GONE
                addTab(tabItem, index, index == 0)
            }

            indicator2?.apply {
                val tabItem = this.newTab()
                tabItem.setCustomView(R.layout.tab_item_broad_layout)
                val tvSelectName = tabItem.customView?.findViewById<TextView>(R.id.tvSelectName)
                val tvTabName = tabItem.customView?.findViewById<TextView>(R.id.tvTabName)
                tvSelectName?.setTextColor(Color.parseColor(pressColor))
                tvTabName?.setTextColor(Color.parseColor(normalColor))
                tvTabName?.text = title
                tvSelectName?.text = title
                tvSelectName?.visibility = if (index == 0) View.VISIBLE else View.GONE
                tvTabName?.visibility = if (index != 0) View.VISIBLE else View.GONE
                tabItem.customView?.findViewById<View>(R.id.tabIndicator)?.visibility =
                    if (index == 0) View.VISIBLE else View.GONE
                addTab(tabItem, index, index == 0)
            }
        }

        indicator?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.customView?.findViewById<TextView>(R.id.tvSelectName)?.visibility =
                    View.VISIBLE
                tab?.customView?.findViewById<TextView>(R.id.tvTabName)?.visibility = View.GONE
                tab?.customView?.findViewById<View>(R.id.tabIndicator)?.visibility = View.VISIBLE
                viewPager?.setCurrentItem(tab?.position ?: 0, false)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                tab?.customView?.findViewById<TextView>(R.id.tvSelectName)?.visibility = View.GONE
                tab?.customView?.findViewById<TextView>(R.id.tvTabName)?.visibility = View.VISIBLE
                tab?.customView?.findViewById<View>(R.id.tabIndicator)?.visibility = View.GONE
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

        })

        indicator2?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.customView?.findViewById<TextView>(R.id.tvSelectName)?.visibility =
                    View.VISIBLE
                tab?.customView?.findViewById<TextView>(R.id.tvTabName)?.visibility = View.GONE
                tab?.customView?.findViewById<View>(R.id.tabIndicator)?.visibility = View.VISIBLE
                viewPager?.setCurrentItem(tab?.position ?: 0, false)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                tab?.customView?.findViewById<TextView>(R.id.tvSelectName)?.visibility = View.GONE
                tab?.customView?.findViewById<TextView>(R.id.tvTabName)?.visibility = View.VISIBLE
                tab?.customView?.findViewById<View>(R.id.tabIndicator)?.visibility = View.GONE
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

        })

        viewPager?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                indicator?.apply {
                    selectTab(this.getTabAt(position))
                }

                indicator2?.apply {
                    selectTab(this.getTabAt(position))
                }
            }

            override fun onPageScrollStateChanged(state: Int) {
            }

        })
    }

}