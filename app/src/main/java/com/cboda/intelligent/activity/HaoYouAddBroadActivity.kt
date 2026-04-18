//package com.cboda.intelligent.activity
//
//import android.os.Bundle
//import android.widget.ImageView
//import android.widget.TextView
//import androidx.core.view.isVisible
//import androidx.lifecycle.ViewModelProvider
//import com.cboda.intelligent.R
//import com.cboda.intelligent.base.BaseSimpleBroadActivity
//import com.cboda.baselibrary.bean.BroadConstant
//import com.cboda.intelligent.utils.GlideLoaderBroadHelper
//import com.cboda.intelligent.utils.JumpBroadUtils
//import com.cboda.intelligent.utils.PixelBroadBroadUtils
//import com.cboda.intelligent.viewmodel.CommViewBroadModel
//
//class HaoYouAddBroadActivity : BaseSimpleBroadActivity() {
//    private var uid = ""
//    private var source = ""
//    private var viewModel: CommViewBroadModel? = null
//    private lateinit var tianjiadaotongxun: TextView
//    private lateinit var qianmingname: TextView
//    private lateinit var qianming: TextView
//    private lateinit var ivxingbie: ImageView
//    private lateinit var tvname: TextView
//    private lateinit var ivTouxiang:
//            ImageView
//
//    private fun initViews() {
//        ivTouxiang = findViewById(R.id.ivTouxiang)
//        tvname = findViewById(R.id.tvname)
//        ivxingbie = findViewById(R.id.ivxingbie)
//        qianming = findViewById(R.id.qianming)
//        qianmingname = findViewById(R.id.qianmingname)
//        tianjiadaotongxun = findViewById(R.id.tianjiadaotongxun)
//    }
//
//    override fun getLayoutId(): Int {
//        return R.layout.activity_hao_broad_you_add
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        initViews()
//        source = intent?.getStringExtra("source") ?: ""
//        uid = intent?.getStringExtra("uid") ?: ""
//
//        viewModel = ViewModelProvider(this)[CommViewBroadModel::class.java]
//        viewModel?.imUserLiveData?.observe(this) {
//            it.data?.apply {
//                GlideLoaderBroadHelper.loadCenterCopRoundedCornersUrl(
//                    this@HaoYouAddBroadActivity,
//                    this.icon ?: "",
//                    ivTouxiang,
//                    PixelBroadBroadUtils.dip2px(this@HaoYouAddBroadActivity, 10f)
//                )
//
//                tvname?.text = this.name ?: ""
//                ivxingbie?.setImageResource(if (this.sex == "1") R.mipmap.ic_broad_nan else R.mipmap.ic_broad_nv)
//                qianmingname?.text = this.sign ?: ""
//
//                tianjiadaotongxun?.isVisible = this.id != BroadConstant.getRealUid()
//            }
//        }
//
//        viewModel?.imUser(this, uid)
//
//        tianjiadaotongxun?.setOnClickListener {
//            JumpBroadUtils.startHaoyouApplyActivity(
//                this,
//                uid,
//                tvname?.text?.toString() ?: "",
//                source,
//                "",
//                "1"
//            )
//        }
//    }
//}