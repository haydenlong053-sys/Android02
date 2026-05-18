package com.cboda.intelligent.dialog

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.cboda.baselibrary.R
import com.cboda.baselibrary.popup.BaseBottomBroadPop
import com.cboda.baselibrary.utils.click
import com.cboda.baselibrary.utils.getAppColor
import com.cboda.intelligent.databinding.DelCollPopBinding

class DelCollPop(val context: Context) : BaseBottomBroadPop(context) {
    @JvmField
    var backInvoke: () -> Unit = { }
    private val binding: DelCollPopBinding = DelCollPopBinding.inflate(
        LayoutInflater.from(context)
    )

    init {
        contentView = binding.root
        setBackgroundColor(getAppColor(context, R.color.transparent))
    }

    override fun onViewCreated(contentView: View) {
        super.onViewCreated(contentView)
        binding.delCollPopSure.click {
            backInvoke.invoke()
            dismiss()
        }
        binding.delCollPopCancel.click {
            dismiss()
        }
    }

}