package cn.wildfire.chat.kit.dialog

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import cn.wildfire.chat.kit.databinding.DialogPopShareBinding
import com.cboda.baselibrary.popup.BaseBottomBroadPop


class SharePop(val context: Context, val view: View) : BaseBottomBroadPop(context) {
    var sureInvoke: () -> Unit = {}
    private var binding: DialogPopShareBinding = DialogPopShareBinding.inflate(
        LayoutInflater.from(context)
    )

    init {
        contentView = binding.root
        setBackgroundColor(Color.parseColor("#8f000000"))

    }

    override fun onViewCreated(contentView: View) {
        super.onViewCreated(contentView)
        binding.mViewAddGroup.addView(view)
        binding.mTvCancel.setOnClickListener {
            dismiss()
        }
        binding.mTvSure.setOnClickListener {
            dismiss()
            sureInvoke.invoke()
        }
    }


}