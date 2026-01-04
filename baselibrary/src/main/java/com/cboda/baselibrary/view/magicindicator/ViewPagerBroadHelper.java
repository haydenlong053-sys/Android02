package com.cboda.baselibrary.view.magicindicator;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;


import com.blankj.utilcode.util.Utils;

import org.jetbrains.annotations.NotNull;

/**
 * 简化和ViewPager绑定
 * Created by hackware on 2016/8/17.
 */

public class ViewPagerBroadHelper {
    /**
     * rv累计的偏移值
     */
    private static int offset;
    private static float positionOffset;
    /**
     * 滑动的偏移值
     */
    private static int positionOffsetPixels;

    public static void bind(final MagicBroadIndicator magicIndicator, ViewPager viewPager) {
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                magicIndicator.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                magicIndicator.onPageSelected(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                magicIndicator.onPageScrollStateChanged(state);
            }
        });
    }

    public static void bind(final MagicBroadIndicator magicIndicator, ViewPager2 viewPager) {
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                magicIndicator.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                magicIndicator.onPageSelected(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                magicIndicator.onPageScrollStateChanged(state);
            }
        });
    }

}
