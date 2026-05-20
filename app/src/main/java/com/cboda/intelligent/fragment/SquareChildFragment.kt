package com.cboda.intelligent.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.cboda.intelligent.base.BaseBroadFragment
import com.cboda.intelligent.databinding.SquareChildFragmentBinding
import com.cboda.intelligent.viewmodel.CleverBodyViewBroadModel

class SquareChildFragment : BaseBroadFragment<SquareChildFragmentBinding, CleverBodyViewBroadModel>() {
    override fun onCreateViewBinding(inflater: LayoutInflater, parent: ViewGroup?, savedInstanceState: Bundle?)=SquareChildFragmentBinding.inflate(inflater, parent, false)
}