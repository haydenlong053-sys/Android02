package com.lxkj.aiim.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.cboda.baselibrary.network.repository.NetworkListBroadResult
import com.lxkj.aiim.adapter.AiRecordListAdapter
import com.lxkj.aiim.base.BaseFragment
import com.cboda.intelligent.databinding.FragmentAiRecordBinding
import com.lxkj.aiim.lj.bean.AiResultListBean
import com.lxkj.aiim.utils.JumpUtils
import com.lxkj.aiim.viewmodel.CheckAiViewModel
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener
import kotlinx.coroutines.launch

class AiRecordFragment(val index: Int) :
    BaseFragment<FragmentAiRecordBinding, CheckAiViewModel>() {
    private var page = 1
    private var totalPage: Int = 1
    private val mAdapter = AiRecordListAdapter()
    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentAiRecordBinding.inflate(inflater, parent, false)

    override fun initListener() {

    }

    override fun initView() {
        initAdapter()
    }

    private fun initAdapter() {
        mBinding?.mRvList?.let {
            it.layoutManager = LinearLayoutManager(requireContext())
            it.adapter = mAdapter
        }
        mAdapter.setOnItemClickListener { adapter, view, position ->
            val noticeInfoBean = mAdapter.data[position]
            JumpUtils.jumpAiCheckResultActivity(requireContext(),noticeInfoBean.id)

        }
        mBinding?.smart?.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onRefresh(refreshLayout: RefreshLayout) {
                page = 1
                initMyData()
                refreshLayout.setNoMoreData(false)
            }

            override fun onLoadMore(refreshLayout: RefreshLayout) {
                if (page >= totalPage) {
                    refreshLayout.setNoMoreData(true)
                    return
                }
                page++
                initMyData()
            }
        })
    }

    override fun lazyLoad() {
        initMyData()
    }

    private fun initMyData() {
        lifecycleScope.launch {
            val resourceType= when (index) {
                0 -> {
                    "document"
                }
                1 -> {
                    "image"
                }
                2 -> {
                    "voice"
                }
                3 -> {
                    "video"
                }
                else -> {
                    "document"
                }
            }
            viewModel.getAiResultInfoList(resourceType,page)
                .observe(this@AiRecordFragment) { resultBean ->
                    showData(resultBean)
                }

        }
    }

    private fun showData(resultBean: NetworkListBroadResult<AiResultListBean>) {
        totalPage = resultBean.totalPage
        mBinding?.smart?.finishLoadMore()
        mBinding?.smart?.finishRefresh()

        if (page == 1) {
            mAdapter.setList(resultBean.list)
        } else {
            resultBean.list?.let {
                mAdapter.addData(it)
            }
        }
        if (resultBean.list?.isEmpty() == true) {
            mBinding?.llNoData?.visibility = View.VISIBLE
        } else {
            mBinding?.llNoData?.visibility = View.INVISIBLE
        }

    }
}