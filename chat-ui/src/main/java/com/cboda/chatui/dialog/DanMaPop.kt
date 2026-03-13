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

class DanMaPop(context: Context, val canSendPic: Boolean, val canGag: Boolean, val isGag: Boolean) : BasePopupWindow(context) {

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
    }

    override fun onDismiss() {
        super.onDismiss()
    }
}