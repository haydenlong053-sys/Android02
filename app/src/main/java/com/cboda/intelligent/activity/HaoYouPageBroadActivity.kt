package com.cboda.intelligent.activity

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cc.shinichi.library.ImagePreview
import com.cboda.chatui.WfcUIBroadKit
import com.cboda.chatui.conversation.ConversationBroadActivity
import com.cboda.chatui.conversation.ConversationViewBroadModel
import com.cboda.chatui.utils.MyIMActivityBroadUtils
import cn.wildfirechat.model.Conversation
import cn.wildfirechat.remote.ChatManager
import com.cboda.baselibrary.utils.SpanBroadUtils
import com.gyf.immersionbar.ImmersionBar
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.cboda.intelligent.R
import com.cboda.intelligent.adapter.PengYouQuanBroadAdapter
import com.cboda.intelligent.base.BaseSimpleBroadActivity
import com.cboda.baselibrary.bean.CommonDataListBroadBean
import com.cboda.baselibrary.bean.CommonResultBroadBean
import com.cboda.intelligent.holder.CommanConfimBroadHolder
import com.cboda.intelligent.utils.AppBroadUtils
import com.cboda.baselibrary.bean.BroadConstant
import com.cboda.intelligent.utils.GlideLoaderBroadHelper
import com.cboda.intelligent.utils.JumpBroadUtils
import com.cboda.intelligent.utils.PixelBroadBroadUtils
import com.cboda.intelligent.utils.StringBroadUtils
import com.cboda.intelligent.viewmodel.CommViewBroadModel
import com.cboda.intelligent.widge.WrapContentLinearLayoutBroadManager
import androidx.core.graphics.toColorInt
import androidx.core.view.isGone
import com.cboda.baselibrary.utils.MMKVBroadUtils
import com.cboda.intelligent.utils.goTempTranslateString


class HaoYouPageBroadActivity : BaseSimpleBroadActivity() {

    private val source by lazy(LazyThreadSafetyMode.NONE) { intent?.getStringExtra("source") ?: "" }
    private var otherUid = ""
    private var viewModel: CommViewBroadModel? = null
    private var mAdapter: PengYouQuanBroadAdapter? = null
    private var mDataList = arrayListOf<CommonDataListBroadBean.CommonBean>()
    private var conversationViewModel: ConversationViewBroadModel? = null

    private var yonghubena: CommonResultBroadBean.CommonResult? = null
    private lateinit var faxiaoxitonghua: LinearLayoutCompat
    private lateinit var yuyintonghua: ConstraintLayout
    private lateinit var faxiaoxi: ConstraintLayout
    private lateinit var qingkongSx: ConstraintLayout
    private lateinit var qingkong: ConstraintLayout
    private lateinit var pengyouquanlay: ConstraintLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var pengyouquan: TextView
    private lateinit var beizhulay: ConstraintLayout
    private lateinit var beizhuname: TextView
    private lateinit var beizhu: TextView
    private lateinit var qianmingname: TextView
    private lateinit var qianming: TextView
    private lateinit var tvID: TextView
    private lateinit var ivLiang: ImageView
    private lateinit var tvnicheng: TextView
    private lateinit var ivxingbie: ImageView
    private lateinit var tvname: TextView
    private lateinit var ivTouxiang: ImageView
    private lateinit var ivMirrorSex: ImageView

    //    private lateinit var rlTagName: RelativeLayout
    private lateinit var tvTagName: TextView
    private lateinit var friendNickName: TextView
    private lateinit var friendName: TextView


    private lateinit var otherLayout: LinearLayoutCompat

    override fun showOrHideTitleLayout(): Boolean {
        return false
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_hao_broad_you_page
    }


    private fun initViews() {
        ImmersionBar.with(this).titleBar(findViewById<View>(R.id.view_status)).init()
        ivTouxiang = findViewById(R.id.ivTouxiang)
        tvname = findViewById(R.id.tvname)
        ivxingbie = findViewById(R.id.ivxingbie)
        tvnicheng = findViewById(R.id.tvnicheng)
        ivLiang = findViewById(R.id.ivLiang)
        tvID = findViewById(R.id.tvID)
        qianming = findViewById(R.id.qianming)
        qianmingname = findViewById(R.id.qianmingname)
        beizhu = findViewById(R.id.beizhu)
        beizhuname = findViewById(R.id.beizhuname)
        beizhulay = findViewById(R.id.beizhulay)
        pengyouquan = findViewById(R.id.pengyouquan)
        recyclerView = findViewById(R.id.recyclerView)
        pengyouquanlay = findViewById(R.id.pengyouquanlay)
        qingkong = findViewById(R.id.qingkong)
        qingkongSx = findViewById(R.id.qingkongSx)
        faxiaoxi = findViewById(R.id.faxiaoxi)
        yuyintonghua = findViewById(R.id.yuyintonghua)
        faxiaoxitonghua = findViewById(R.id.faxiaoxitonghua)
//        rlTagName = findViewById(R.id.rl_tag_name)
        ivMirrorSex = findViewById(R.id.iv_mirror_sex)
        tvTagName = findViewById(R.id.tv_tag_name)
        friendName = findViewById(R.id.friendName)
        friendNickName = findViewById(R.id.friendNickName)
        otherLayout = findViewById(R.id.otherLayout)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        otherUid = intent?.getStringExtra("uid") ?: ""
//        eLogD("zhanglong", "userInfo.otherUid----" + otherUid)
        initViews()
        getRightImg()?.isVisible = false
        getRightImg()?.setImageResource(R.mipmap.ic_gengduo_broad_white)
//        val isFriend = ChatManager.Instance().isMyFriend(otherUid)
        getRightImg()?.setOnClickListener {
            JumpBroadUtils.startHaoYouSetActivity(this, otherUid, "")
        }
        conversationViewModel = WfcUIBroadKit.getAppScopeViewModel(
            ConversationViewBroadModel::class.java
        )

        viewModel = ViewModelProvider(this)[CommViewBroadModel::class.java]
        viewModel?.imUserLiveData?.observe(this) {
            it.data?.apply {
                yonghubena = this
                GlideLoaderBroadHelper.loadCenterCopRoundedCornersUrl(
                    this@HaoYouPageBroadActivity,
                    this.icon ?: "",
                    ivTouxiang,
                    PixelBroadBroadUtils.dip2px(this@HaoYouPageBroadActivity, 10f)
                )
                if (sex == null || sex == "0") {
                    ivMirrorSex.setImageResource(R.drawable.ic_mirror_broad_female)
                } else {
                    ivMirrorSex.setImageResource(if (sex == "1") R.drawable.ic_mirror_broad_male else R.drawable.ic_mirror_broad_female)

                }
                val isFriend = "1" == it.data?.isFri
                otherLayout.isGone = otherUid == MMKVBroadUtils.getRealUserId() || !isFriend
                findViewById<ImageView>(R.id.iv_float_more).isGone =
                    otherUid == MMKVBroadUtils.getRealUserId() || !isFriend
                findViewById<TextView>(R.id.addFriend).isGone =
                    isFriend || otherUid == MMKVBroadUtils.getRealUserId()
                val daysTv = findViewById<TextView>(R.id.daysTv)
                val registerDay = it.data?.registerDayBean?.registDay
                daysTv.isGone = registerDay == null
                daysTv.text = "${registerDay}${getString(R.string.days_unit)}"
                val vocationalTv = findViewById<TextView>(R.id.vocationalTv)
                vocationalTv.isGone = TextUtils.isEmpty(it.data?.profession)
                vocationalTv.text = it.data?.profession ?: ""
                val hobbyTv1 = findViewById<TextView>(R.id.hobbyTv1)
                val hobbyTv2 = findViewById<TextView>(R.id.hobbyTv2)
                val hobbyTv3 = findViewById<TextView>(R.id.hobbyTv3)
                val hobbyTv4 = findViewById<TextView>(R.id.hobbyTv4)
                val hobbyTv5 = findViewById<TextView>(R.id.hobbyTv5)
                val hobbyTv6 = findViewById<TextView>(R.id.hobbyTv6)

                val memberInterestList = it.data?.memberInterestList ?: arrayOf()
                hobbyTv1.isVisible = memberInterestList.isNotEmpty()
                hobbyTv2.isVisible = memberInterestList.size > 1
                hobbyTv3.isVisible = memberInterestList.size > 2
                hobbyTv4.isVisible = memberInterestList.size > 3
                hobbyTv5.isVisible = memberInterestList.size > 4
                hobbyTv6.isVisible = memberInterestList.size > 5

                memberInterestList.forEachIndexed { index, hobby ->
                    when (index) {
                        0 -> hobbyTv1.text = hobby
                        1 -> hobbyTv2.text = hobby
                        2 -> hobbyTv3.text = hobby
                        3 -> hobbyTv4.text = hobby
                        4 -> hobbyTv5.text = hobby
                        5 -> hobbyTv6.text = hobby
                    }
                }


//                eLogD("zhanglong","otherUid-->$otherUid======getRealUserId==${MMKVBroadUtils.getRealUserId()}")
                var showName = ""
                tvTagName.text = "${getString(R.string.user_id_prefix)}${idno}"
                if (bei.isNullOrEmpty()) {
                    showName = name ?: ""
                    friendNickName.isGone = true
                } else {
                    showName = bei ?: ""
                    friendNickName.text = getString(R.string.nickname_with_prefix, name ?: "")
                    friendNickName.isGone = false
                }
                tvTagName.isGone = false
                friendName.text = showName
                findViewById<LinearLayout>(R.id.ll_grade).visibility = View.VISIBLE
                val tvGrade = findViewById<TextView>(R.id.tv_grade)
                val fullGradeString = "${grade}${getString(R.string.score_unit)}"
                tvGrade.text = SpanBroadUtils.small(fullGradeString, getString(R.string.score_unit), 12f, "#031256".toColorInt())


                if (isLiang != null && isLiang == "1") {
                    ivLiang?.visibility = View.VISIBLE
                    tvID?.setTextColor(resources.getColor(R.color.color_text_liang))
                } else {
                    ivLiang?.visibility = View.GONE
                }

                tvID?.text = "${getString(R.string.user_id_prefix)}${this.idno}"
                tvname?.text = this.bei ?: ""
                tvnicheng?.text = StringBroadUtils.format(getString(R.string.nichengshi), "${this.name}")
                ivxingbie?.setImageResource(if (this.sex == "1") R.mipmap.ic_broad_nan else R.mipmap.ic_broad_nv)
                qianmingname?.text = this.sign ?: ""
                beizhuname?.text = goTempTranslateString(this@HaoYouPageBroadActivity,this.characterTags?: "")
                pengyouquanlay?.isVisible = this.isOpenQuan == "1"

                if (BroadConstant.getRealUid() == this.id) {
                    getRightImg()?.isVisible = false
                    faxiaoxitonghua?.isVisible = false
                } else {
                    getRightImg()?.isVisible = true
                    faxiaoxitonghua?.isVisible = this.isFri == "1"
                }

            }

            it.data?.quan?.apply {
                mDataList.clear()
                this.forEachIndexed { index, s ->
                    val comm = CommonDataListBroadBean.CommonBean()
                    comm.icon = s
                    mDataList.add(comm)
                }
                mAdapter?.setList(mDataList)
            }
            ChatManager.Instance().workHandler.post {
                ChatManager.Instance().getUserInfo(otherUid, true)
            }

        }
        viewModel?.errorLiveData?.observe(this) {
            when (it.type) {
                "qingkongSuc" -> {

                }

                "shezhibeizhuSuc" -> {
                    viewModel?.imUser(this, otherUid)
                }
            }
        }

        findViewById<ImageView>(R.id.iv_float_more).setOnClickListener {
            JumpBroadUtils.startHaoYouSetActivity(this, otherUid, "")
        }
        findViewById<ImageView>(R.id.mViewBack).setOnClickListener {
            finish()
        }
        findViewById<TextView>(R.id.addFriend).setOnClickListener {
            JumpBroadUtils.startHaoyouApplyActivity(
                this,
                otherUid,
                friendName?.text?.toString() ?: "",
                source,
                ""
            )
        }
        beizhulay?.setOnClickListener {
//            JumpBroadUtils.startHaoYouBeizhuActivity(this, otherUid, beizhuname?.text?.toString() ?: "")
            val oldName = friendName?.text?.toString() ?: ""
            CircleAndRemarkBroadActivity.toPage(
                this,
                startForResult,
                oldName,
                beizhuname?.text?.toString() ?: "",
                CircleAndRemarkBroadActivity.TYPE_EDIT
            )
        }

        ivTouxiang?.setOnClickListener {
            ImagePreview
                .getInstance()
                .setContext(this)
                .setIndex(0)
                .setImage(yonghubena?.icon!!)
                .start()
        }

        pengyouquanlay?.setOnClickListener {
            val intent = Intent(this, MyCircleBroadActivity::class.java)
            intent.putExtra("id", otherUid)
            startActivity(intent)
        }

        faxiaoxi?.setOnClickListener {

            MyIMActivityBroadUtils.activities.forEachIndexed { index, activity ->
                if (activity is ConversationBroadActivity) {
                    activity.finish()
                }
            }

            AppBroadUtils.goChat(this, otherUid, "")

        }
        yuyintonghua?.setOnClickListener {
//            TUICallKit.createInstance(this).call(otherUid, TUICallDefine.MediaType.Audio)
            XXPermissions.with(this).permission(Permission.RECORD_AUDIO)
                .request(object : OnPermissionCallback {
                    override fun onGranted(permissions: MutableList<String>, allGranted: Boolean) {
                        WfcUIBroadKit.singleCall(this@HaoYouPageBroadActivity, otherUid, true)

                    }
                })
        }
        qingkong?.setOnClickListener {
            val holder = CommanConfimBroadHolder(
                this,
                getString(R.string.qingkongliaotian),
                StringBroadUtils.format(getString(R.string.qingkongtishi, yonghubena?.name ?: "")),
                getString(R.string.queding),
                getString(R.string.quxiao)
            )
            holder.callBack = object : CommanConfimBroadHolder.OnConfimCallBack {
                override fun onClickConfim(isConfim: Boolean) {
                    if (isConfim) {
                        val conversation = Conversation()
                        conversation.type = Conversation.ConversationType.Single
                        conversation.line = 0
                        conversation.target = otherUid
//                        conversationViewModel?.clearRemoteConversationMessage(conversation)
                        conversationViewModel?.clearConversationMessage(conversation)
                    }
                }
            }
            holder.show()
        }

        qingkongSx?.setOnClickListener {
            val holder = CommanConfimBroadHolder(
                this,
                getString(R.string.clear_chat_history_both_sides),
                StringBroadUtils.format(getString(R.string.qingkongtishi, yonghubena?.name ?: "")),
                getString(R.string.queding),
                getString(R.string.quxiao)
            )
            holder.callBack = object : CommanConfimBroadHolder.OnConfimCallBack {
                override fun onClickConfim(isConfim: Boolean) {
                    if (isConfim) {
                        val conversation = Conversation()
                        conversation.type = Conversation.ConversationType.Single
                        conversation.line = 0
                        conversation.target = otherUid
                        conversationViewModel?.clearRemoteConversationMessage(conversation)
//                        conversationViewModel?.clearConversationMessage(conversation)
                    }
                }
            }
            holder.show()
        }

//        for (i in 0 until 3) {
//            mDataList.add(CommonDataListBroadBean.CommonBean())
//        }

        mAdapter = PengYouQuanBroadAdapter(R.layout.item_peng_broad_you_quan_layout)
        recyclerView?.apply {
            layoutManager = WrapContentLinearLayoutBroadManager(
                this@HaoYouPageBroadActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = mAdapter
        }
        mAdapter?.setList(mDataList)
    }

    override fun onResume() {
        super.onResume()
        viewModel?.imUser(this, otherUid)
    }

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                val data = it.data
                data?.let {
                    val circleId = it.getIntExtra("circleId", -1)
                    val remarkName = it.getStringExtra("remark")
                    toEditFriendInfo(circleId, remarkName ?: "")


                }
            }

        }

    private fun toEditFriendInfo(circleId: Int, remarkName: String) {
        viewModel?.shezhibeizhu(this, otherUid, remarkName, circleId)
    }

}


