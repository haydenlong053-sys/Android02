package com.freddy.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.cboda.intelligent.base.BaseBroadFragment
import com.cboda.intelligent.databinding.ConversationSingleFragmentBinding
import com.cboda.intelligent.viewmodel.ConversationParentViewModel

//单聊
class ConversationSingleFragment : BaseBroadFragment<ConversationSingleFragmentBinding, ConversationParentViewModel>() {
    override fun onCreateViewBinding(inflater: LayoutInflater, parent: ViewGroup?, savedInstanceState: Bundle?) = ConversationSingleFragmentBinding.inflate(inflater, parent, false)
}