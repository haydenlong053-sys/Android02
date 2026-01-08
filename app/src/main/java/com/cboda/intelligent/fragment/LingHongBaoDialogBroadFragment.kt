package com.cboda.intelligent.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.lifecycle.ViewModelProvider
import com.cboda.baselibrary.toast.ToastBroadUtils
import com.google.gson.Gson
import com.cboda.intelligent.R
import com.cboda.intelligent.base.BaseDialogBroadFragment
import com.cboda.intelligent.bean.HongBaoBroadBiangeng
import com.cboda.intelligent.utils.click
import com.cboda.intelligent.utils.*
import com.cboda.intelligent.viewmodel.CommViewBroadModel

class LingHongBaoDialogBroadFragment : BaseDialogBroadFragment() {
    private var viewModel: CommViewBroadModel? = null
    var hongbaoId = ""
    var isgroup = ""
    var messageUid = ""
    var payload = ""

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
                GlideLoaderBroadHelper.loadCenterCopRoundedCornersUrl(
                    context,
                    this.icon ?: "",
                    ivTouxiang,
                    PixelBroadBroadUtils.dip2px(context, 10f)
                )
                tvname?.text =
                    StringBroadUtils.format(getString(R.string.mourendehongbao), this.name ?: "")
                tvnicheng?.text = this.info ?: ""
                if (this.tuiStatus == 1) {
                    ToastBroadUtils.show(getString(R.string.string_hongbao_expired))
                    dismiss()
                }

            }
        }

        viewModel?.errorLiveData?.observe(this) {
            when (it.type) {
                "linghongbaosuc" -> {
                    dismiss()
                    JumpBroadUtils.startHongBaoPageActivity(context, hongbaoId, isgroup)
//                    if (msgBean is CustomHongBaoMessageBean){
//                        msgBean?.v2TIMMessage?.customElem?.data?.apply {
//                            val gson = Gson()
//                            (msgBean as CustomHongBaoMessageBean).lingquStatus = "1"
//                            AppBroadUtil.debug("变更","-->${String(msgBean?.v2TIMMessage?.customElem?.data!!)} ")
//                            val biangeng = gson.fromJson(String(msgBean?.v2TIMMessage?.customElem?.data!!),CustomHongBaoMessageBean::class.java)
//                            biangeng.lingquStatus = "1"
//                            msgBean?.v2TIMMessage?.cloudCustomData = gson.toJson(biangeng)
//                            AppBroadUtil.debug("变更内容","--> ${gson.toJson(biangeng)}")
//                            V2TIMManager.getMessageManager()?.modifyMessage(msgBean!!.v2TIMMessage,object :
//                                V2TIMCompleteCallback<V2TIMMessage>{
//                                override fun onComplete(code: Int, desc: String?, t: V2TIMMessage?) {
//                                    AppBroadUtil.debug("变更","消息onComplete--> $code $desc")
//                                }
//                            })
//                        }
//                    }
                }
            }
        }

        viewModel?.hongbaoInfo(context, hongbaoId)
        kaihongbao?.setOnClickListener {
            val bean = HongBaoBroadBiangeng()
            bean.id = hongbaoId
            bean.messageUid = messageUid
            bean.payload = gson.fromJson(payload, HongBaoBroadBiangeng.HongBaoPre::class.java)
            viewModel?.linghongbao(context, gson.toJson(bean))
        }
        ivClose.click {
            dismiss()
        }
    }

    private var gson = Gson()
    private lateinit var ivClose: ImageView
    private lateinit var tvnicheng: TextView
    private lateinit var tvname: TextView
    private lateinit var ivTouxiang: ImageView
    private lateinit var kaihongbao: AppCompatImageView

    private fun initViews(view: View) {
        kaihongbao = view.findViewById(R.id.kaihongbao)
        ivTouxiang = view.findViewById(R.id.ivTouxiang)
        tvname = view.findViewById(R.id.tvname)
        tvnicheng = view.findViewById(R.id.tvnicheng)
        ivClose = view.findViewById(R.id.ivClose)
    }

}