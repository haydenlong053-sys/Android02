package com.lxkj.aiim.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.lifecycle.ViewModelProvider
import com.cboda.chatui.conversation.message.model.UiBroadMessage
import cn.wildfirechat.message.HongBaoMessageContent
import com.cboda.baselibrary.utils.MMKVBroadUtils
import com.google.gson.Gson
import com.cboda.intelligent.R
import com.lxkj.aiim.base.BaseDiaogFragment
import com.cboda.baselibrary.bean.CommonResultBroadBean
import com.lxkj.aiim.bean.HongBaoBiangeng
import com.lxkj.aiim.holder.SendCustomMsg
import com.lxkj.aiim.utils.*
import com.lxkj.aiim.viewmodel.CommViewModel

class LingQunHongBaoDialogFragment : BaseDiaogFragment() {
    private var viewModel: CommViewModel? = null
    var hongbaoId = ""
    var isgroup = ""
    var messageUid = ""
    var payload = ""
    var msgBean: UiBroadMessage? = null
    var dataBean : CommonResultBroadBean? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View =
            View.inflate(activity, R.layout.linghong_bao_layout, null)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        viewModel = ViewModelProvider(this)[CommViewModel::class.java]
        viewModel?.hongbaoLiveData?.observe(this) {
            it.data?.apply {
                dataBean = it
                GlideLoaderHelper.loadCenterCopRoundedCornersUrl(
                    context,
                   this.icon?:"",
                    ivTouxiang,
                    PixelBroadUtils.dip2px(context, 10f)
                )
                if (msgBean != null && msgBean!!.message != null && msgBean!!.message.content is HongBaoMessageContent &&( msgBean!!.message.content as HongBaoMessageContent).hongbaoLeixing == "3") {
                    tvname?.text = StringUtils.format(getString(R.string.shuidezhuanchuhong), (msgBean!!.message.content as HongBaoMessageContent) .zhuanshuUserName?:"")
                } else {
                    tvname?.text = StringUtils.format(getString(R.string.mourendehongbao), this.name?:"")
                }
                tvnicheng?.text = this.info?:""

            }
        }

        viewModel?.errorLiveData?.observe(this){
            when(it.type){
                "linghongbaosuc"->{
                    dismiss()
                    JumpUtils.startHongBaoPageActivity(context,hongbaoId,isgroup)

                    if (msgBean != null && msgBean!!.message != null && msgBean!!.message.content is HongBaoMessageContent) {
                        SendCustomMsg().sendYeHuoLingQu(msgBean!!.message.conversation.target,"",
                            dataBean!!.data!!.uid!!,
                            dataBean!!.data!!.name!!,
                            MMKVBroadUtils.getRealUserId()!!, MMKVBroadUtils.getUserName()!!
                        )
                    }

//                    if (it.type2 == "1"){
//                        if (msgBean is CustomHongBaoMessageBean){
//                            val gson = Gson()
//                            (msgBean as CustomHongBaoMessageBean).lingquStatus = "1"
//                            AppUtil.debug("变更群","-->${msgBean?.v2TIMMessage?.toString()}  ${msgBean?.v2TIMMessage?.customElem} ")
//                            msgBean?.v2TIMMessage?.customElem?.data?.apply {
//                                AppUtil.debug("变更群","-->${String(this)} ")
//                                val biangeng = gson.fromJson(String(msgBean?.v2TIMMessage?.customElem?.data!!),CustomHongBaoMessageBean::class.java)
//                                biangeng.lingquStatus = "1"
//                                msgBean?.v2TIMMessage?.cloudCustomData = gson.toJson(biangeng)
//                                AppUtil.debug("变更群内容","--> ${gson.toJson(biangeng)}")
//                                V2TIMManager.getMessageManager()?.modifyMessage(msgBean!!.v2TIMMessage,object :
//                                    V2TIMCompleteCallback<V2TIMMessage>{
//                                    override fun onComplete(code: Int, desc: String?, t: V2TIMMessage?) {
//                                        AppUtil.debug("变更群","消息onComplete--> $code $desc")
//                                    }
//                                })
//                            }
//
//                        }
//                    } else {
//                        if (msgBean is CustomHongBaoMessageBean){
//                            msgBean?.v2TIMMessage?.customElem?.data?.apply {
//                                val gson = Gson()
//                                AppUtil.debug("变更群单个","-->${String(msgBean?.v2TIMMessage?.customElem?.data!!)} ")
//                                val biangeng = gson.fromJson(String(msgBean?.v2TIMMessage?.customElem?.data!!),CustomHongBaoMessageBean::class.java)
//                                biangeng.lqUsers.add(MainActivity.currtentLoginUser?.id?:"")
//                                msgBean?.v2TIMMessage?.cloudCustomData = gson.toJson(biangeng)
//                                AppUtil.debug("变更群单个内容","--> ${gson.toJson(biangeng)}")
//                                V2TIMManager.getMessageManager()?.modifyMessage(msgBean!!.v2TIMMessage,object :
//                                    V2TIMCompleteCallback<V2TIMMessage>{
//                                    override fun onComplete(code: Int, desc: String?, t: V2TIMMessage?) {
//                                        AppUtil.debug("变更群单个","消息onComplete--> $code $desc ${t?.cloudCustomData}")
//                                    }
//                                })
//                            }
//                        }
//                    }
                }
            }
        }
        kaihongbao?.setImageResource(R.mipmap.ic_linghongbao_bg2)
        viewModel?.qunhongbaoInfo(context, hongbaoId)
        kaihongbao?.setOnClickListener {
            val bean = HongBaoBiangeng()
            bean.id = hongbaoId
            bean.messageUid = messageUid
            bean.payload = gson.fromJson(payload, HongBaoBiangeng.HongBaoPre::class.java)
            viewModel?.lingqunhongbao(context,gson.toJson(bean))
        }
        ivClose?.setOnClickListener {
            dismiss()
        }
    }
    private var gson = Gson()
    private lateinit var ivClose: ImageView
    private lateinit var tvnicheng: TextView
    private lateinit var tvname: TextView
    private lateinit var ivTouxiang: ImageView
    private lateinit var kaihongbao:
            AppCompatImageView

    private fun initViews(view: View) {
        kaihongbao = view.findViewById(R.id.kaihongbao)
        ivTouxiang = view.findViewById(R.id.ivTouxiang)
        tvname = view.findViewById(R.id.tvname)
        tvnicheng = view.findViewById(R.id.tvnicheng)
        ivClose = view.findViewById(R.id.ivClose)
    }


}