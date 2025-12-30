package com.lxkj.aiim.base


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.base.commonlibrary.utils.getResumeActivity
import com.lxkj.aiim.databinding.LayoutDataViewBinding

class LoadConfig(private var binding: LayoutDataViewBinding? = null) {


    /**
     * 添加加载布局
     */
    fun initLoadingView(context: Context, layout: Int): LoadConfig {
        binding?.flLoading?.addView(LayoutInflater.from(context).inflate(layout, null))
        return this
    }



    fun release() {
        binding = null
    }
}