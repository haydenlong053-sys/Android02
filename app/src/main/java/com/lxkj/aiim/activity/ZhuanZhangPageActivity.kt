package com.lxkj.aiim.activity

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.cboda.chatui.bridge.YeHuoBroadEvent
import com.google.gson.Gson
import com.lxkj.aiim.R
import com.lxkj.aiim.base.BaseSimpleActivity
import com.lxkj.aiim.bean.ZhuanZhangBiangeng
import com.lxkj.aiim.viewmodel.CommViewModel
import org.greenrobot.eventbus.EventBus

class ZhuanZhangPageActivity:BaseSimpleActivity() {

    private var hongbaoId = ""
    private var viewModel: CommViewModel? = null
    private var messageUid = ""
    private var payload = ""
    private var gson = Gson()

    private var mprice = ""
    private lateinit var tvPhoneLogin: TextView
    private lateinit var shoukuanshijian: LinearLayoutCompat
    private lateinit var shoukuanshijian1: TextView
    private lateinit var shijian: TextView
    private lateinit var qianNum: TextView
    private lateinit var tvnicheng: TextView
    private lateinit var ivTouxiang:
            ImageView

    private fun initViews() {
        ivTouxiang = findViewById(R.id.ivTouxiang)
        tvnicheng = findViewById(R.id.tvnicheng)
        qianNum = findViewById(R.id.qianNum)
        shijian = findViewById(R.id.shijian)
        shoukuanshijian1 = findViewById(R.id.shoukuanshijian1)
        shoukuanshijian = findViewById(R.id.shoukuanshijian)
        tvPhoneLogin = findViewById(R.id.tvPhoneLogin)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViews()
        messageUid = intent?.getStringExtra("messageUid") ?: ""
        payload = intent?.getStringExtra("payload") ?: ""
        hongbaoId = intent?.getStringExtra("hongbaoId") ?: ""
        viewModel = ViewModelProvider(this)[CommViewModel::class.java]
        viewModel?.zhuanzhangLiveData?.observe(this){
            it.data?.apply {
                ivTouxiang?.setImageResource(if (this.isLing == "1")R.mipmap.ic_yishoukuan else R.mipmap.ic_daishoukuan)
                tvnicheng?.text = if (this.isLing == "1")getString(R.string.cunruyue) else getString(R.string.daishoulkuan)
                qianNum?.text = this.money?:""
                shijian?.text = this.zzTime?:""
                mprice = this.money?:""
                shoukuanshijian?.isVisible = this.isLing == "1"
                shoukuanshijian1?.text = this.skTime?:""
                tvPhoneLogin?.isVisible = this.canLing == "1"
            }
        }
        viewModel?.errorLiveData?.observe(this) {
            when (it.type) {
                "shoukuanSuc" -> {
                    viewModel?.zhuanzhangInfo(this,hongbaoId)
                    EventBus.getDefault().post(YeHuoBroadEvent("fajieshouzhuanzhangxiaoxi",hongbaoId,mprice))

//                    val customMsg = SendCustomMsg()
//                    customMsg.sendYeHuoZhuanZhangJieShou(
//                        "",
//
//                    )

                    //

//                    if (msgBean is CustomZhuanZhangMessageBean){
//                        msgBean?.v2TIMMessage?.customElem?.data?.apply {
//                            val gson = Gson()
//                            (msgBean as CustomZhuanZhangMessageBean).lingquStatus = "1"
//                            AppUtil.debug("变更转账","-->${String(msgBean?.v2TIMMessage?.customElem?.data!!)} ")
//                            val biangeng = gson.fromJson(String(msgBean?.v2TIMMessage?.customElem?.data!!),
//                                CustomZhuanZhangMessageBean::class.java)
//                            biangeng.lingquStatus = "1"
//                            msgBean?.v2TIMMessage?.cloudCustomData = gson.toJson(biangeng)
//                            AppUtil.debug("转账变更内容","--> ${gson.toJson(biangeng)}")
//                            V2TIMManager.getMessageManager()?.modifyMessage(msgBean!!.v2TIMMessage,object :
//                                V2TIMCompleteCallback<V2TIMMessage> {
//                                override fun onComplete(code: Int, desc: String?, t: V2TIMMessage?) {
//                                    AppUtil.debug("转账变更","消息onComplete--> $code $desc")
//                                }
//                            })
//                        }
//                    }

                }
            }
        }

        viewModel?.zhuanzhangInfo(this,hongbaoId)

        tvPhoneLogin?.setOnClickListener {
            val bean = ZhuanZhangBiangeng()
            bean.id = hongbaoId
            bean.messageUid = messageUid
            bean.payload = gson.fromJson(payload,ZhuanZhangBiangeng.ZhuanZhanPre::class.java)
            viewModel?.shoukuan(this,gson.toJson(bean))
        }

    }

    override fun getLayoutId(): Int {
        return R.layout.activity_zhuanzhang_page
    }
}