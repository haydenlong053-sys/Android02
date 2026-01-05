package com.lxkj.aiim.fragment

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
import com.lxkj.aiim.R
import com.lxkj.aiim.base.BaseDiaogFragment
import com.lxkj.aiim.bean.HongBaoBiangeng
import com.lxkj.aiim.lj.utils.click
import com.lxkj.aiim.utils.*
import com.lxkj.aiim.viewmodel.CommViewModel

class LingHongBaoDialogFragment : BaseDiaogFragment() {
    private var viewModel: CommViewModel? = null
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
            View.inflate(activity, R.layout.linghong_bao_layout, null)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        viewModel = ViewModelProvider(this)[CommViewModel::class.java]
        viewModel?.hongbaoLiveData?.observe(this) {
            it.data?.apply {
                GlideLoaderHelper.loadCenterCopRoundedCornersUrl(
                    context,
                    this.icon ?: "",
                    ivTouxiang,
                    PixelBroadUtils.dip2px(context, 10f)
                )
                tvname?.text =
                    StringUtils.format(getString(R.string.mourendehongbao), this.name ?: "")
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
                    JumpUtils.startHongBaoPageActivity(context, hongbaoId, isgroup)
//                    if (msgBean is CustomHongBaoMessageBean){
//                        msgBean?.v2TIMMessage?.customElem?.data?.apply {
//                            val gson = Gson()
//                            (msgBean as CustomHongBaoMessageBean).lingquStatus = "1"
//                            AppUtil.debug("变更","-->${String(msgBean?.v2TIMMessage?.customElem?.data!!)} ")
//                            val biangeng = gson.fromJson(String(msgBean?.v2TIMMessage?.customElem?.data!!),CustomHongBaoMessageBean::class.java)
//                            biangeng.lingquStatus = "1"
//                            msgBean?.v2TIMMessage?.cloudCustomData = gson.toJson(biangeng)
//                            AppUtil.debug("变更内容","--> ${gson.toJson(biangeng)}")
//                            V2TIMManager.getMessageManager()?.modifyMessage(msgBean!!.v2TIMMessage,object :
//                                V2TIMCompleteCallback<V2TIMMessage>{
//                                override fun onComplete(code: Int, desc: String?, t: V2TIMMessage?) {
//                                    AppUtil.debug("变更","消息onComplete--> $code $desc")
//                                }
//                            })
//                        }
//                    }
                }
            }
        }

        viewModel?.hongbaoInfo(context, hongbaoId)
        kaihongbao?.setOnClickListener {
            val bean = HongBaoBiangeng()
            bean.id = hongbaoId
            bean.messageUid = messageUid
            bean.payload = gson.fromJson(payload, HongBaoBiangeng.HongBaoPre::class.java)
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