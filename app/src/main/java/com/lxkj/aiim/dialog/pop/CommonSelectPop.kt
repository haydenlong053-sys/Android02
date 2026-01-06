package com.lxkj.aiim.dialog.pop

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.cboda.baselibrary.popup.BaseBottomBroadPop
import com.lxkj.aiim.adapter.CommonSelectAdapter
import com.cboda.intelligent.databinding.DialogPopCommonSelectBinding
import com.lxkj.aiim.lj.bean.CommonSelectBean



//选择弹框
class CommonSelectPop(private val mContext: Context, val mTitle : String, val mData: List<CommonSelectBean>, val invokeSelect :(select:CommonSelectBean)-> Unit ={}) : BaseBottomBroadPop(mContext) {
    private var  binding: DialogPopCommonSelectBinding = DialogPopCommonSelectBinding.inflate(
        LayoutInflater.from(mContext))
    init {
        contentView = binding.root

    }

    override fun onViewCreated(contentView: View) {
        super.onViewCreated(contentView)
        binding.mTvTitle.text =mTitle
        binding.mIvClose.setOnClickListener {
            dismiss()
        }
        binding.mTvCancel.setOnClickListener {
            dismiss()
        }

        initAdapter()
    }

    private fun initAdapter() {
      val  mAdapter = CommonSelectAdapter()
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(mContext)
            adapter = mAdapter
        }
        mAdapter.setList(mData)
        mAdapter.setOnItemClickListener { _, _, position ->
            dismiss()
            invokeSelect.invoke(mData[position])
        }

    }

}