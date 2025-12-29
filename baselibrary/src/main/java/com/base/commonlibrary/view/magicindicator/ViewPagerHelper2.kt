package com.base.commonlibrary.view.magicindicator

import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.base.commonlibrary.view.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter

fun bindIndicatorAndViewPager(
    magicIndicator: MagicIndicator,
    viewPager: ViewPager2,
    commonNavigatorAdapter: CommonNavigatorAdapter? = null,
    smoothScroll: Boolean = false,
    commonNavigatorAdapterCanResult: Boolean = true,
    callBack: (Int?, Int?, Float?, Int?, Int?) -> Unit = { _, _, _, _, _ -> }
) {
    viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
            magicIndicator.onPageScrolled(position, positionOffset, positionOffsetPixels)
            callBack.invoke(0, position, positionOffset, positionOffsetPixels, null)
        }

        override fun onPageSelected(position: Int) {
            magicIndicator.onPageSelected(position)
            callBack.invoke(1, position, 0f, 0, null)
        }

        override fun onPageScrollStateChanged(state: Int) {
            magicIndicator.onPageScrollStateChanged(state)
            callBack.invoke(2, state, 0f, 0, null)
        }
    })
    commonNavigatorAdapter?.setOnIndicatorTapClickListener { index ->
        if (commonNavigatorAdapterCanResult) {
            viewPager.setCurrentItem(index, smoothScroll)
        } else {
            callBack.invoke(null, null, null, null, index)
        }
    }
}