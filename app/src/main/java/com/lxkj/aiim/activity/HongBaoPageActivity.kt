package com.lxkj.aiim.activity

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.isInvisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.lxkj.aiim.R
import com.lxkj.aiim.adapter.LingQuUserAdapter
import com.lxkj.aiim.base.BaseSimpleActivity
import com.cboda.baselibrary.bean.CommonResultBroadBean
import com.lxkj.aiim.utils.GlideLoaderHelper
import com.lxkj.aiim.utils.PixelUtils
import com.lxkj.aiim.utils.StringUtils
import com.lxkj.aiim.viewmodel.CommViewModel
import com.lxkj.aiim.widge.WrapContentLinearLayoutManager
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener

class HongBaoPageActivity : BaseSimpleActivity() {
    private var hongbaoId = ""
    private var viewModel: CommViewModel? = null
    private var mAdapter: LingQuUserAdapter? = null
    private var mDataList = arrayListOf<CommonResultBroadBean.CommonResult>()
    private var pageNo = 1
    private var isgroup = ""
    private lateinit var smartLayout: SmartRefreshLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvhongbaogeshu: TextView
    private lateinit var qianNum: TextView
    private lateinit var tvnicheng: TextView
    private lateinit var tvname: TextView
    private lateinit var ivTouxiang: ImageView
    private lateinit var fanhui: AppCompatImageView
    private lateinit var zhuanzhangvitool: View

    private fun initViews() {
        zhuanzhangvitool = findViewById(R.id.zhuanzhangvitool)
        fanhui = findViewById(R.id.fanhui)
        ivTouxiang = findViewById(R.id.ivTouxiang)
        tvname = findViewById(R.id.tvname)
        tvnicheng = findViewById(R.id.tvnicheng)
        qianNum = findViewById(R.id.qianNum)
        tvhongbaogeshu = findViewById(R.id.tvhongbaogeshu)
        recyclerView = findViewById(R.id.recyclerView)
        smartLayout = findViewById(R.id.smartLayout)
    }


    override fun getLayoutId(): Int {
        return R.layout.activity_hongbao
    }

    override fun showOrHideTitleLayout(): Boolean {
        return false
    }

    override fun transparentStatus(): Boolean {
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViews()
        initData()
        isgroup = intent?.getStringExtra("isgroup") ?: ""
        hongbaoId = intent?.getStringExtra("hongbaoId") ?: ""
        viewModel = ViewModelProvider(this)[CommViewModel::class.java]
        viewModel?.lingquListLiveData?.observe(this) {
            smartLayout?.finishRefresh()
            smartLayout?.finishLoadMore()
            it.dataList?.apply {
                if (this.isNotEmpty()) {
                    pageNo++
                }
                mDataList.addAll(this)
                mAdapter?.setList(mDataList)

                if (mDataList.isEmpty()) {
                    mAdapter?.setEmptyView(R.layout.list_empty_layout)
                }
                if (mDataList.size >= (it.totalCount ?: "0").toInt()) {
                    smartLayout?.setEnableLoadMore(false)
                }

            }

            it.data?.apply {
                GlideLoaderHelper.loadCenterCopRoundedCornersUrl(
                    this@HongBaoPageActivity,
                    this.icon ?: "",
                    ivTouxiang,
                    PixelUtils.dip2px(this@HongBaoPageActivity, 5f)
                )
                tvname?.isInvisible = false
                tvhongbaogeshu?.isInvisible = false
                tvname?.text =
                    StringUtils.format(getString(R.string.mourendehongbao), this.name ?: "")
                tvnicheng?.text = this.info ?: ""
                if (isgroup == "1") {
                    qianNum?.text = this.benrenMoney ?: ""
                } else {
                    qianNum?.text = this.money ?: ""
                }
                tvhongbaogeshu?.text = StringUtils.format(
                    getString(R.string.yilingquhongbao),
                    "${this.ylq ?: ""}/${this.count ?: ""}",
                    "${this.ylqMoney ?: ""}/${this.money ?: ""}"
                )
            }

        }

        var result = 0
        val resourceId = resources.getIdentifier(
            "status_bar_height",
            "dimen", "android"
        )
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        val linearParams = zhuanzhangvitool?.layoutParams
        linearParams?.height = result
        zhuanzhangvitool?.layoutParams = linearParams

        fanhui?.setOnClickListener {
            finish()
        }
        mAdapter = LingQuUserAdapter(R.layout.item_lingqu_user_layout)
        recyclerView?.apply {
            layoutManager = WrapContentLinearLayoutManager(this@HongBaoPageActivity)
            adapter = mAdapter
        }
        mAdapter?.setList(mDataList)
        smartLayout?.setEnableLoadMore(false)
        smartLayout?.setEnableRefresh(false)
        smartLayout?.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onRefresh(refreshLayout: RefreshLayout) {
                pageNo = 1
                mDataList.clear()
                request()
            }

            override fun onLoadMore(refreshLayout: RefreshLayout) {
                request()
            }

        })
        request()


    }

    private fun initData() {
        tvname?.isInvisible = true
        tvhongbaogeshu?.isInvisible = true
    }

    private fun request() {
        viewModel?.getLingquList(
            this,
            hongbaoId,
            isgroup == "1",
        )
    }
}