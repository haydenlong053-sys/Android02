package com.lxkj.aiim.activity

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cboda.chatui.conversation.ConversationBroadActivity
import com.cboda.chatui.conversation.message.model.UiBroadMessage
import cn.wildfirechat.model.Conversation
import com.cboda.baselibrary.toast.ToastBroadUtils
import com.cboda.intelligent.R
import com.lxkj.aiim.adapter.TouSuLeixingAdapter2
import com.lxkj.aiim.base.BaseSimpleActivity
import com.cboda.baselibrary.bean.CommonDataListBroadBean
import com.cboda.chatui.bridge.TouSuJiLuBroadEvent
import com.lxkj.aiim.bean.TouSuLeiXingEvent
import com.lxkj.aiim.holder.MutliPicSelectHolder
import com.lxkj.aiim.utils.AppUtil
import com.lxkj.aiim.utils.JumpUtils
import com.lxkj.aiim.viewmodel.CommViewModel
import com.lxkj.aiim.viewmodel.UploadFileViewModel
import com.lxkj.aiim.widge.WrapContentLinearLayoutManager
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class QunTouSuActivity:BaseSimpleActivity() {

    private var qunId = ""
    private var qunName = ""
    private var viewModel: CommViewModel? = null
    private var mAdapter: TouSuLeixingAdapter2? = null
    private var mDataList = arrayListOf<CommonDataListBroadBean.CommonBean>()
    private var holder :MutliPicSelectHolder? = null
    private var mDataList2 = arrayListOf<UiBroadMessage>()
    private var uploadViewModel: UploadFileViewModel? = null

    override fun getLayoutId(): Int {
        return R.layout.activity_tousuqun
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViews()
        qunId = intent?.getStringExtra("qunid") ?: ""
        qunName = intent?.getStringExtra("qunName")?:""
        getToolbarTitle()?.text = getString(R.string.tousuqun)

        viewModel = ViewModelProvider(this)[CommViewModel::class.java]
        viewModel?.errorLiveData?.observe(this) {
            when (it.type) {
                "tousuSuc" -> {
                    finish()
                }
            }
        }

        mAdapter = TouSuLeixingAdapter2(R.layout.item_tousuleixing_layout2)
        recyclerView?.apply {
            layoutManager = WrapContentLinearLayoutManager(
                this@QunTouSuActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = mAdapter
        }
        mAdapter?.setList(mDataList)
        tousulieixng?.setOnClickListener {
            JumpUtils.startTouSuLeiXingActivity(this)
        }

        liaotianzhengju?.setOnClickListener {
//            JumpUtils.startQunChatHisActivity(this,qunId,qunName)
            val conversation = Conversation()
            conversation.type =  Conversation.ConversationType.Group
            conversation.target = qunId
            // 点击会话
            val intent = Intent(
                this,
                ConversationBroadActivity::class.java
            )
            intent.putExtra("conversation",conversation )
            intent.putExtra("frompage","tousu")
            startActivity(intent)
        }

        tvPhoneLogin?.setOnClickListener {
            if (mDataList.isEmpty()){
                ToastBroadUtils.show(getString(R.string.xuanztousuqunleixing))
                return@setOnClickListener
            }
            if (mDataList2.isEmpty()){
                ToastBroadUtils.show(getString(R.string.uxanzhengjiju))
                return@setOnClickListener
            }
            val typestr = StringBuilder()
            mDataList.forEachIndexed { index, s ->
                if (index != ((mDataList.size) - 1)) {
                    typestr.append(s.type?:"")
                    typestr.append("|")
                } else {
                    typestr.append(s.type?:"")
                }
            }

            val log = StringBuilder()
            mDataList2.forEachIndexed { index, s ->
                if (index != ((mDataList2.size) - 1)) {
                    log.append((s.message?.messageUid?:0L).toString())
                    log.append("|")
                } else {
                    log.append((s.message?.messageUid?:0L).toString())
                }
            }

            showLoading()
            if (holder?.lubanFileList?.isNotEmpty() == true) {
                if (uploadViewModel == null) {
                    uploadViewModel =
                        ViewModelProvider(this)[UploadFileViewModel::class.java]
                }
                MainScope().launch {
                    val urlBeans = uploadViewModel?.uploadListSyn(holder!!.lubanFileList)
                    if (urlBeans?.urls.isNullOrEmpty()) {
                        dismissLoading()
                        AppUtil.showToast(this@QunTouSuActivity, urlBeans?.erroToaastInfo?:"")
                        return@launch
                    }
                    dismissLoading()
                    val strBuilder = StringBuilder()
                    urlBeans?.urls?.forEachIndexed { index, s ->
                        if (index != ((urlBeans.urls?.size ?: 0) - 1)) {
                            strBuilder.append(s)
                            strBuilder.append("|")
                        } else {
                            strBuilder.append(s)
                        }
                    }
                    imgs = strBuilder.toString()
                    AppUtil.debug("长传结果-->", "${strBuilder.toString()}")
                    viewModel?.tousu(this@QunTouSuActivity,qunId,typestr.toString(),log.toString(),imgs,edtBeizhu?.text?.toString()?:"")
                }
            } else {
                viewModel?.tousu(this@QunTouSuActivity,qunId,typestr.toString(),log.toString(),imgs,edtBeizhu?.text?.toString()?:"")
            }

        }



        holder = MutliPicSelectHolder(this,recyclerView2)
        holder?.tvtupuanshu = tvtupuanshu

        EventBus.getDefault().register(this)

    }
    private var imgs = ""
    private lateinit var tvPhoneLogin: TextView
    private lateinit var edtBeizhu: EditText
    private lateinit var recyclerView2: RecyclerView
    private lateinit var tvtupuanshu: TextView
    private lateinit var liaotianzhengju: ConstraintLayout
    private lateinit var zhengjucon: TextView
    private lateinit var zhengju: TextView
    private lateinit var tousulieixng: ConstraintLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var pengyouquan:
            TextView

    private fun initViews() {
        pengyouquan = findViewById(R.id.pengyouquan)
        recyclerView = findViewById(R.id.recyclerView)
        tousulieixng = findViewById(R.id.tousulieixng)
        zhengju = findViewById(R.id.zhengju)
        zhengjucon = findViewById(R.id.zhengjucon)
        liaotianzhengju = findViewById(R.id.liaotianzhengju)
        tvtupuanshu = findViewById(R.id.tvtupuanshu)
        recyclerView2 = findViewById(R.id.recyclerView2)
        edtBeizhu = findViewById(R.id.edtBeizhu)
        tvPhoneLogin = findViewById(R.id.tvPhoneLogin)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMsgReceive(event: TouSuLeiXingEvent) {
        mDataList.clear()
        mDataList.addAll(event.datalist)
        mAdapter?.setList(mDataList)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMsgReceive(event: TouSuJiLuBroadEvent) {
        mDataList2.clear()
        mDataList2.addAll(event.dataList)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}