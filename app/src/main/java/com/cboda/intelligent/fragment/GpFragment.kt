package com.cboda.intelligent.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.cboda.intelligent.base.BaseBroadFragment
import com.cboda.intelligent.databinding.GpFragmentBinding
import com.cboda.intelligent.viewmodel.CleverBodyViewBroadModel

class GpFragment : BaseBroadFragment<GpFragmentBinding, CleverBodyViewBroadModel>() {
    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        savedInstanceState: Bundle?
    ) = GpFragmentBinding.inflate(inflater, parent, false)
}