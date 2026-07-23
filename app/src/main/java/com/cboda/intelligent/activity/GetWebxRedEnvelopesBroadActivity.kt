package com.cboda.intelligent.activity

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.activity.viewModels
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.cboda.baselibrary.bean.GlobalBroadInfo
import com.cboda.baselibrary.utils.OnSingleClickBroadListener
import com.cboda.baselibrary.utils.fromJson2
import com.cboda.baselibrary.utils.toJson2
import com.blankj.utilcode.util.TimeUtils
import com.bumptech.glide.Glide
import com.cboda.baselibrary.utils.RealmUtil
import com.cboda.intelligent.R
import com.cboda.intelligent.adapter.LingQuUserBroadAdapter
import com.cboda.intelligent.base.BaseSimple2BroadActivity
import com.cboda.intelligent.bean.HongBaoBroadBiangeng
import com.cboda.intelligent.databinding.GetWeBroadBxRedEnvelopesActivityBinding
import com.cboda.intelligent.viewmodel.CommViewBroadModel


class GetWebxRedEnvelopesBroadActivity : BaseSimple2BroadActivity() {


    private val msgTarget by lazy(LazyThreadSafetyMode.NONE) {
        intent?.getStringExtra("msgTarget") ?: ""
    }
    private val messageTime by lazy(LazyThreadSafetyMode.NONE) {
        intent?.getLongExtra("messageTime", 0L) ?: 0L
    }
    private val redEnvelopesId by lazy(LazyThreadSafetyMode.NONE) {
        intent?.getStringExtra("redEnvelopesId") ?: ""
    }

    private val isGroup by lazy(LazyThreadSafetyMode.NONE) {
        intent?.getBooleanExtra("isGroup", false) ?: false
    }

    private val getIsReceived by lazy(LazyThreadSafetyMode.NONE) {
        intent?.getBooleanExtra("isReceived", false) ?: false
    }

    private val payload by lazy(LazyThreadSafetyMode.NONE) {
        intent?.getStringExtra("payload") ?: ""
    }
    private val messageUid by lazy(LazyThreadSafetyMode.NONE) {
        intent?.getStringExtra("messageUid") ?: ""
    }

    private val messageId by lazy(LazyThreadSafetyMode.NONE) {
        intent?.getLongExtra("messageId", 0L) ?: 0L
    }
    private val sender by lazy(LazyThreadSafetyMode.NONE) {
        intent?.getStringExtra("sender") ?: ""
    }
    private var isReceived = false
    private var mAdapter: LingQuUserBroadAdapter? = null

    private var sendUserId = ""
    private var sendUserName = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBind = (binding as? GetWeBroadBxRedEnvelopesActivityBinding)
        mIvBgTop.setBackgroundResource(R.drawable.ic_red_envelope_webx_broad_back)
        mAdapter = LingQuUserBroadAdapter(R.layout.item_ling_broad_qu_user_layout)
        mAdapter?.setEmptyView(R.layout.list_empty_broad_layout)
        isReceived = getIsReceived
        mBind?.apply {
            animationImg.imageAssetsFolder = "bluepackimg"//233X226
            listView.adapter = mAdapter
            noCollection.isGone = isReceived
            haveCollection.isGone = !isReceived
            refusedGroup.isGone = isGroup || isReceived
            if (messageTime != 0L) {
                createTime.text = getString(
                    R.string.webx_transfer_time_format,
                    TimeUtils.millis2String(
                        messageTime,
                        getString(R.string.webx_transfer_time_pattern)
                    )
                )
            }
            backIcon.setOnClickListener(object : OnSingleClickBroadListener() {
                override fun onSingleClick(v: View?) {
                    finish()
                }
            })
            collectionBtn.setOnClickListener(object : OnSingleClickBroadListener() {
                override fun onSingleClick(v: View?) {
                    val bean = HongBaoBroadBiangeng()
                    bean.id = redEnvelopesId
                    bean.messageUid = messageUid
                    bean.payload = fromJson2(payload, HongBaoBroadBiangeng.HongBaoPre::class.java)
                    viewModel.linghongbao2(
                        this@GetWebxRedEnvelopesBroadActivity,
                        toJson2(bean),
                        isGroup,
                        msgTarget,
                        sendUserId,
                        sendUserName, messageId, sender
                    )
                }
            })
            refusedBtn.setOnClickListener(object : OnSingleClickBroadListener() {
                override fun onSingleClick(v: View?) {
                    viewModel.refuseRedEnvelopes(
                        this@GetWebxRedEnvelopesBroadActivity,
                        redEnvelopesId,
                        isGroup, messageId
                    )
                }
            })
//            userImg.setImageResource(com.cboda.baselibrary.R.mipmap.ic_logo_login)
//            userName.text = "JOY Follow发出的红包"
//            redTitle.text = "人人皆股东 好运连连"
//            createTime.text = "转账时间：2024年08月30日 14:34:30"
//            moneyNum.text = "899"
//            getTheTitle.text = "已领取 2/10个，共1000DUALX"
            viewModel.lingquListLiveData.observe(this@GetWebxRedEnvelopesBroadActivity) {
                it.data?.also { it1 ->
                    redTitle.text = it1.info
                    if (isGroup) {
                        sendUserId = it1.uid ?: ""
                        sendUserName = it1.name ?: ""
                    }
                    RealmUtil.findUserData(sender) { it2 ->
                        it2?.let {
                            if (!TextUtils.isEmpty(it2.remarkName)) {
                                userName.text = getString(
                                    R.string.webx_red_envelope_from_user,
                                    it2.remarkName
                                )
                            } else if (!TextUtils.isEmpty(it2.nickName)) {
                                userName.text = getString(
                                    R.string.webx_red_envelope_from_user,
                                    it2.nickName
                                )
                            }
                        }
                    }
                    Glide.with(this@GetWebxRedEnvelopesBroadActivity)
                        .load(it1.icon)
                        .placeholder(com.cboda.baselibrary.R.mipmap.ic_logo_broad_login)
                        .into(userImg)
                    moneyNum.text = it1.money
                    if (isGroup) {
                        getTheTitle.text = getString(
                            R.string.webx_group_received_summary,
                            it1.ylq?.toInt() ?: 0,
                            it1.count?.toInt() ?: 0,
                            it1.money
                        )
                    }
                }
                mAdapter?.setList(it.dataList ?: arrayListOf())
            }
            viewModel.errorLiveData.observe(this@GetWebxRedEnvelopesBroadActivity) {
                when (it.type) {
                    "linghongbaosuc" -> {
                        isReceived = true
                        noCollection.isGone = true
                        refusedGroup.isGone = true
                        haveCollection.isGone = false
                        getLingQuList()
//                        if (it.isGroup && isGroup && !TextUtils.isEmpty(msgTarget) && !TextUtils.isEmpty(
//                                sendUserId
//                            )
//                        ) {
//                            SendCustomBroadMsg().sendMyImLingQu(
//                                msgTarget, "",
//                                sendUserId,
//                                sendUserName,
//                                MMKVBroadUtils.getRealUserId()!!, MMKVBroadUtils.getUserName()!!
//                            )
//                        }
                    }
                }
            }
            viewModel.refuseRedEnvelopesLiveData.observe(this@GetWebxRedEnvelopesBroadActivity) {
                if (it.second) {
                    finish()
                }
            }
            if (!isReceived) {
                getLingQuList()
            }
        }
//        BarUtils.setStatusBarColor(this, ActivityCompat.getColor(this, R.color.transparent))
    }

    override fun onResume() {
        super.onResume()
        if (isReceived) {
            getLingQuList()
        }
    }


    private fun getLingQuList() {
        viewModel.getLingquList(
            this,
            redEnvelopesId,
            isGroup,
        )
    }

    private var mBind: GetWeBroadBxRedEnvelopesActivityBinding? = null
    private val viewModel: CommViewBroadModel by viewModels()
    override fun getViewBindingClass() = GetWeBroadBxRedEnvelopesActivityBinding::class.java
    override fun showTransparentTopTitleLayout() = false
    override fun useViewBinding() = true
    override fun showOrHideTitleLayout() = false
    override fun useStatusBarDarkFont() = false
}