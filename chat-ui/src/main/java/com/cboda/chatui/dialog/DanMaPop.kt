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
import razerdp.basepopup.BasePopupWindow

class DanMaPop(context: Context, val canSendPic: Boolean, val canGag: Boolean, val isGag: Boolean, val canUpMic: Boolean, val canDownMic: Boolean, val haveApplicationList: Boolean, val haveListUnRead: Boolean) :
    BasePopupWindow(context) {

    var clickInvoke: (Int) -> Unit = {}
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
            clickInvoke.invoke(1)
            dismiss()
        }
        binding.flowerTv.click {
            clickInvoke.invoke(1)
            dismiss()
        }
        binding.acclamationIv.click {
            clickInvoke.invoke(2)
            dismiss()
        }
        binding.acclamationTv.click {
            clickInvoke.invoke(2)
            dismiss()
        }
        binding.picIv.click {
            clickInvoke.invoke(3)
            dismiss()
        }
        binding.picTv.click {
            clickInvoke.invoke(3)
            dismiss()
        }
        binding.gagIv.click {
            clickInvoke.invoke(4)
            dismiss()
        }
        binding.gagTv.click {
            clickInvoke.invoke(4)
            dismiss()
        }
        binding.removeGagIv.click {
            clickInvoke.invoke(5)
            dismiss()
        }
        binding.removeGagTv.click {
            clickInvoke.invoke(5)
            dismiss()
        }


        binding.upMicIv.click {
            clickInvoke.invoke(6)
            dismiss()
        }
        binding.upMicTv.click {
            clickInvoke.invoke(6)
            dismiss()
        }
        binding.downMicIv.click {
            clickInvoke.invoke(7)
            dismiss()
        }
        binding.downMicTv.click {
            clickInvoke.invoke(7)
            dismiss()
        }
        binding.applicationListIv.click {
            clickInvoke.invoke(8)
            dismiss()
        }
        binding.applicationListTv.click {
            clickInvoke.invoke(8)
            dismiss()
        }
    }

    override fun onDismiss() {
        super.onDismiss()
    }
}