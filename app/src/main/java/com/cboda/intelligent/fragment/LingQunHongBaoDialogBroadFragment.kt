package com.cboda.intelligent.fragment

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
import com.blankj.utilcode.util.GsonUtils
import com.cboda.baselibrary.utils.MMKVBroadUtils
import com.cboda.intelligent.R
import com.cboda.intelligent.base.BaseDialogBroadFragment
import com.cboda.baselibrary.bean.CommonResultBroadBean
import com.cboda.intelligent.bean.HongBaoBroadBiangeng
import com.cboda.intelligent.holder.SendCustomBroadMsg
import com.cboda.intelligent.utils.*
import com.cboda.intelligent.viewmodel.CommViewBroadModel

class LingQunHongBaoDialogBroadFragment : BaseDialogBroadFragment() {
    private var viewModel: CommViewBroadModel? = null
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
            View.inflate(activity, R.layout.ling_hong_broad_bao_layout, null)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        viewModel = ViewModelProvider(this)[CommViewBroadModel::class.java]
        viewModel?.hongbaoLiveData?.observe(this) {
            it.data?.apply {
                dataBean = it
                GlideLoaderBroadHelper.loadCenterCopRoundedCornersUrl(
                    context,
                   this.icon?:"",
                    ivTouxiang,
                    PixelBroadBroadUtils.dip2px(context, 10f)
                )
                if (msgBean != null && msgBean!!.message != null && msgBean!!.message.content is HongBaoMessageContent &&( msgBean!!.message.content as HongBaoMessageContent).hongbaoLeixing == "3") {
                    tvname?.text = StringBroadUtils.format(getString(R.string.shuidezhuanchuhong), (msgBean!!.message.content as HongBaoMessageContent) .zhuanshuUserName?:"")
                } else {
                    tvname?.text = StringBroadUtils.format(getString(R.string.mourendehongbao), this.name?:"")
                }
                tvnicheng?.text = this.info?:""

            }
        }

        viewModel?.errorLiveData?.observe(this){
            when(it.type){
                "linghongbaosuc"->{
                    dismiss()
                    JumpBroadUtils.startHongBaoPageActivity(context,hongbaoId,isgroup)

//                    if (msgBean != null && msgBean!!.message != null && msgBean!!.message.content is HongBaoMessageContent) {
//                        SendCustomBroadMsg().sendMyImLingQu(msgBean!!.message.conversation.target,"",
//                            dataBean!!.data!!.uid!!,
//                            dataBean!!.data!!.name!!,
//                            MMKVBroadUtils.getRealUserId()!!, MMKVBroadUtils.getUserName()!!
//                        )
//                    }

//                    if (it.type2 == "1"){
//                        if (msgBean is CustomHongBaoMessageBean){
//                            val gson = Gson()
//                            (msgBean as CustomHongBaoMessageBean).lingquStatus = "1"
//                            AppBroadUtil.debug("变更群","-->${msgBean?.v2TIMMessage?.toString()}  ${msgBean?.v2TIMMessage?.customElem} ")
//                            msgBean?.v2TIMMessage?.customElem?.data?.apply {
//                                AppBroadUtil.debug("变更群","-->${String(this)} ")
//                                val biangeng = gson.fromJson(String(msgBean?.v2TIMMessage?.customElem?.data!!),CustomHongBaoMessageBean::class.java)
//                                biangeng.lingquStatus = "1"
//                                msgBean?.v2TIMMessage?.cloudCustomData = gson.toJson(biangeng)
//                                AppBroadUtil.debug("变更群内容","--> ${gson.toJson(biangeng)}")
//                                V2TIMManager.getMessageManager()?.modifyMessage(msgBean!!.v2TIMMessage,object :
//                                    V2TIMCompleteCallback<V2TIMMessage>{
//                                    override fun onComplete(code: Int, desc: String?, t: V2TIMMessage?) {
//                                        AppBroadUtil.debug("变更群","消息onComplete--> $code $desc")
//                                    }
//                                })
//                            }
//
//                        }
//                    } else {
//                        if (msgBean is CustomHongBaoMessageBean){
//                            msgBean?.v2TIMMessage?.customElem?.data?.apply {
//                                val gson = Gson()
//                                AppBroadUtil.debug("变更群单个","-->${String(msgBean?.v2TIMMessage?.customElem?.data!!)} ")
//                                val biangeng = gson.fromJson(String(msgBean?.v2TIMMessage?.customElem?.data!!),CustomHongBaoMessageBean::class.java)
//                                biangeng.lqUsers.add(MainBroadActivity.currtentLoginUser?.id?:"")
//                                msgBean?.v2TIMMessage?.cloudCustomData = gson.toJson(biangeng)
//                                AppBroadUtil.debug("变更群单个内容","--> ${gson.toJson(biangeng)}")
//                                V2TIMManager.getMessageManager()?.modifyMessage(msgBean!!.v2TIMMessage,object :
//                                    V2TIMCompleteCallback<V2TIMMessage>{
//                                    override fun onComplete(code: Int, desc: String?, t: V2TIMMessage?) {
//                                        AppBroadUtil.debug("变更群单个","消息onComplete--> $code $desc ${t?.cloudCustomData}")
//                                    }
//                                })
//                            }
//                        }
//                    }
                }
            }
        }
        kaihongbao?.setImageResource(R.mipmap.ic_linghongbao_broad_bg2)
        viewModel?.qunhongbaoInfo(context, hongbaoId)
        kaihongbao?.setOnClickListener {
            val bean = HongBaoBroadBiangeng()
            bean.id = hongbaoId
            bean.messageUid = messageUid
            bean.payload = gson.fromJson(payload, HongBaoBroadBiangeng.HongBaoPre::class.java)
            viewModel?.lingqunhongbao(context,gson.toJson(bean))
        }
        ivClose?.setOnClickListener {
            dismiss()
        }
    }
    private var gson = GsonUtils.getGson()
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