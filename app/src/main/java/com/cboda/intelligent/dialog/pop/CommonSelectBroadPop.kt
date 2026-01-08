package com.cboda.intelligent.dialog.pop

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.cboda.baselibrary.popup.BaseBottomBroadPop
import com.cboda.intelligent.adapter.CommonSelectBroadAdapter
import com.cboda.intelligent.bean.CommonSelectBroadBean
import com.cboda.intelligent.databinding.DialogPopCommonBroadSelectBinding


//选择弹框
class CommonSelectBroadPop(private val mContext: Context, val mTitle : String, val mData: List<CommonSelectBroadBean>, val invokeSelect :(select:CommonSelectBroadBean)-> Unit ={}) : BaseBottomBroadPop(mContext) {
    private var  binding: DialogPopCommonBroadSelectBinding = DialogPopCommonBroadSelectBinding.inflate(
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
      val  mAdapter = CommonSelectBroadAdapter()
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