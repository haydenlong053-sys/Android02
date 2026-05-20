package com.cboda.intelligent.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.cboda.intelligent.base.BaseBroadFragment
import com.cboda.intelligent.databinding.FriendsCircleFragmentBinding
import com.cboda.intelligent.viewmodel.CleverBodyViewBroadModel

class FriendCircleFragment : BaseBroadFragment<FriendsCircleFragmentBinding, CleverBodyViewBroadModel>() {
    override fun onCreateViewBinding(inflater: LayoutInflater, parent: ViewGroup?, savedInstanceState: Bundle?) = FriendsCircleFragmentBinding.inflate(inflater, parent, false)
}