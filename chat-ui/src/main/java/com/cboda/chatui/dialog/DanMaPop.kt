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
        binding.upMicIv.isGone=!canUpMic
        binding.upMicTv.isGone=!canUpMic
        binding.downMicIv.isGone=!canDownMic
        binding.downMicTv.isGone=!canDownMic
        binding.applicationListIv.isGone=!haveApplicationList
        binding.applicationListTv.isGone=!haveApplicationList
        binding.applicationListNum.isGone=!haveApplicationList || !haveListUnRead
        width = ViewGroup.LayoutParams.WRAP_CONTENT
        binding.flowerIv.click {
            clickInvoke.invoke(DanMaPopAction.FLOWER)
            dismiss()
        }
        binding.flowerTv.click {
            clickInvoke.invoke(DanMaPopAction.FLOWER)
            dismiss()
        }
        binding.acclamationIv.click {
            clickInvoke.invoke(DanMaPopAction.ACCLAMATION)
            dismiss()
        }
        binding.acclamationTv.click {
            clickInvoke.invoke(DanMaPopAction.ACCLAMATION)
            dismiss()
        }
        binding.picIv.click {
            clickInvoke.invoke(DanMaPopAction.PIC)
            dismiss()
        }
        binding.picTv.click {
            clickInvoke.invoke(DanMaPopAction.PIC)
            dismiss()
        }
        binding.gagIv.click {
            clickInvoke.invoke(DanMaPopAction.GAG)
            dismiss()
        }
        binding.gagTv.click {
            clickInvoke.invoke(DanMaPopAction.GAG)
            dismiss()
        }
        binding.removeGagIv.click {
            clickInvoke.invoke(DanMaPopAction.REMOVE_GAG)
            dismiss()
        }
        binding.removeGagTv.click {
            clickInvoke.invoke(DanMaPopAction.REMOVE_GAG)
            dismiss()
        }


        binding.upMicIv.click {
            clickInvoke.invoke(DanMaPopAction.UP_MIC)
            dismiss()
        }
        binding.upMicTv.click {
            clickInvoke.invoke(DanMaPopAction.UP_MIC)
            dismiss()
        }
        binding.downMicIv.click {
            clickInvoke.invoke(DanMaPopAction.DOWN_MIC)
            dismiss()
        }
        binding.downMicTv.click {
            clickInvoke.invoke(DanMaPopAction.DOWN_MIC)
            dismiss()
        }
        binding.applicationListIv.click {
            clickInvoke.invoke(DanMaPopAction.APPLICATION_LIST)
            dismiss()
        }
        binding.applicationListTv.click {
            clickInvoke.invoke(DanMaPopAction.APPLICATION_LIST)
            dismiss()
        }
    }

    override fun onDismiss() {
        super.onDismiss()
    }
}