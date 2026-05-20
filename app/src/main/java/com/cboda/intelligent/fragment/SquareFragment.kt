package com.cboda.intelligent.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.cboda.intelligent.base.BaseBroadFragment
import com.cboda.intelligent.databinding.SquareFragmentBinding
import com.cboda.intelligent.viewmodel.CleverBodyViewBroadModel

class SquareFragment : BaseBroadFragment<SquareFragmentBinding, CleverBodyViewBroadModel>() {
    override fun onCreateViewBinding(inflater: LayoutInflater, parent: ViewGroup?, savedInstanceState: Bundle?) = SquareFragmentBinding.inflate(inflater, parent, false)
}