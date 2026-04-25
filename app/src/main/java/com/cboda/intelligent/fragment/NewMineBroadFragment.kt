package com.cboda.intelligent.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.cboda.intelligent.base.BaseBroadFragment
import com.cboda.intelligent.databinding.FragmentNewMineBinding

import com.cboda.intelligent.viewmodel.MineViewModel

class NewMineBroadFragment : BaseBroadFragment<FragmentNewMineBinding, MineViewModel>() {


    override fun initView() {
//        addHeight(binding.aiTime)
    }

    override fun initListener() {
        super.initListener()

    }

    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentNewMineBinding {
        return FragmentNewMineBinding.inflate(inflater, parent, false)
    }

}