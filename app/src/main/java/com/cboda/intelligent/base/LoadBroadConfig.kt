package com.cboda.intelligent.base


import android.content.Context
import android.view.LayoutInflater
import com.cboda.intelligent.databinding.LayoutDataBroadViewBinding

class LoadBroadConfig(private var binding: LayoutDataBroadViewBinding? = null) {


    /**
     * 添加加载布局
     */
    fun initLoadingView(context: Context, layout: Int): LoadBroadConfig {
        binding?.flLoading?.addView(LayoutInflater.from(context).inflate(layout, null))
        return this
    }



    fun release() {
        binding = null
    }
}