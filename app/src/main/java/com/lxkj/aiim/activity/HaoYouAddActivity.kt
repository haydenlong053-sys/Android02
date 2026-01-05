package com.lxkj.aiim.activity

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.lxkj.aiim.R
import com.lxkj.aiim.base.BaseSimpleActivity
import com.cboda.baselibrary.bean.BroadConstant
import com.lxkj.aiim.utils.GlideLoaderHelper
import com.lxkj.aiim.utils.JumpUtils
import com.lxkj.aiim.utils.PixelBroadUtils
import com.lxkj.aiim.viewmodel.CommViewModel

class HaoYouAddActivity : BaseSimpleActivity() {
    private var uid = ""
    private var source = ""
    private var viewModel: CommViewModel? = null
    private lateinit var tianjiadaotongxun: TextView
    private lateinit var qianmingname: TextView
    private lateinit var qianming: TextView
    private lateinit var ivxingbie: ImageView
    private lateinit var tvname: TextView
    private lateinit var ivTouxiang:
            ImageView

    private fun initViews() {
        ivTouxiang = findViewById(R.id.ivTouxiang)
        tvname = findViewById(R.id.tvname)
        ivxingbie = findViewById(R.id.ivxingbie)
        qianming = findViewById(R.id.qianming)
        qianmingname = findViewById(R.id.qianmingname)
        tianjiadaotongxun = findViewById(R.id.tianjiadaotongxun)
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_haoyou_add
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViews()
        source = intent?.getStringExtra("source") ?: ""
        uid = intent?.getStringExtra("uid") ?: ""

        viewModel = ViewModelProvider(this)[CommViewModel::class.java]
        viewModel?.imUserLiveData?.observe(this) {
            it.data?.apply {
                GlideLoaderHelper.loadCenterCopRoundedCornersUrl(
                    this@HaoYouAddActivity,
                    this.icon ?: "",
                    ivTouxiang,
                    PixelBroadUtils.dip2px(this@HaoYouAddActivity, 10f)
                )

                tvname?.text = this.name ?: ""
                ivxingbie?.setImageResource(if (this.sex == "1") R.mipmap.ic_nan else R.mipmap.ic_nv)
                qianmingname?.text = this.sign ?: ""

                tianjiadaotongxun?.isVisible = this.id != BroadConstant.getRealUid()
            }
        }

        viewModel?.imUser(this, uid)

        tianjiadaotongxun?.setOnClickListener {
            JumpUtils.startHaoyouApplyActivity(
                this,
                uid,
                tvname?.text?.toString()?:"",
                source,
                ""
            )
        }
    }
}