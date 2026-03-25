package com.cboda.chatui.dialog

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import com.cboda.baselibrary.utils.click
import com.cboda.chatui.databinding.DanMaPopularBinding
import com.cboda.chatui.quickmeet.bean.EnumerationBean.DanMaPopAction
import razerdp.basepopup.BasePopupWindow

/**
 * canSendPic：能否发送图片
 * canGag：能否解禁言和开启禁言
 * isGag：是否禁言状态
 * canUpMic：能否上麦
 * canDownMic：能否下麦
 * haveApplicationList：是否展示申请列表入口
 * haveListUnRead：是否有未读消息（申请上麦消息）
 */
class DanMaPop(context: Context, val canSendPic: Boolean, val canGag: Boolean, val isGag: Boolean, val canUpMic: Boolean, val canDownMic: Boolean, val haveApplicationList: Boolean, val haveListUnRead: Boolean) :
    BasePopupWindow(context) {

    var clickInvoke: (DanMaPopAction) -> Unit = {}
    private var binding = DanMaPopularBinding.inflate(
        LayoutInflater.from(context)
    )

    init {
        contentView = binding.root
        popupGravity = Gravity.TOP
        setBackgroundColor(Color.parseColor("#00ffffff"))
    }

    override fun onViewCreated(contentView: View) {
        super.onViewCreated(contentView)
        binding.picIv.isGone = !canSendPic
        binding.picTv.isGone = !canSendPic
        binding.gagIv.isGone = !canGag || isGag
        binding.gagTv.isGone = !canGag || isGag
        binding.removeGagIv.isGone = !canGag || !isGag
        binding.removeGagTv.isGone = !canGag || !isGag
        binding.upMicIv.isGone = !canUpMic
        binding.upMicTv.isGone = !canUpMic
        binding.downMicIv.isGone = !canDownMic
        binding.downMicTv.isGone = !canDownMic
        binding.applicationListIv.isGone = !haveApplicationList
        binding.applicationListTv.isGone = !haveApplicationList
        binding.applicationListNum.isGone = !haveApplicationList || !haveListUnRead
        width = ViewGroup.LayoutParams.WRAP_CONTENT
        binding.flowerIv.click {
            clickInvoke(DanMaPopAction.FLOWER)
        }
        binding.flowerTv.click {
            clickInvoke(DanMaPopAction.FLOWER)
        }
        binding.acclamationIv.click {
            clickInvoke(DanMaPopAction.ACCLAMATION)
        }
        binding.acclamationTv.click {
            clickInvoke(DanMaPopAction.ACCLAMATION)
        }
        binding.picIv.click {
            clickInvoke(DanMaPopAction.PIC)
        }
        binding.picTv.click {
            clickInvoke(DanMaPopAction.PIC)
        }
        binding.gagIv.click {
            clickInvoke(DanMaPopAction.GAG)
        }
        binding.gagTv.click {
            clickInvoke(DanMaPopAction.GAG)
        }
        binding.removeGagIv.click {
            clickInvoke(DanMaPopAction.REMOVE_GAG)
        }
        binding.removeGagTv.click {
            clickInvoke(DanMaPopAction.REMOVE_GAG)
        }
        binding.upMicIv.click {
            clickInvoke(DanMaPopAction.UP_MIC)
        }
        binding.upMicTv.click {
            clickInvoke(DanMaPopAction.UP_MIC)
        }
        binding.downMicIv.click {
            clickInvoke(DanMaPopAction.DOWN_MIC)
        }
        binding.downMicTv.click {
            clickInvoke(DanMaPopAction.DOWN_MIC)
        }
        binding.applicationListIv.click {
            clickInvoke(DanMaPopAction.APPLICATION_LIST)
        }
        binding.applicationListTv.click {
            clickInvoke(DanMaPopAction.APPLICATION_LIST)
        }
    }

    private fun clickInvoke(action: DanMaPopAction) {
        clickInvoke.invoke(action)
        dismiss()
    }

    override fun onDismiss() {
        super.onDismiss()
    }
}