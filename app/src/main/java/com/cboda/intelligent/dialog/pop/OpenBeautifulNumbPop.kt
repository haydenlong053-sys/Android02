package com.cboda.intelligent.dialog.pop

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import com.cboda.baselibrary.popup.BaseBottomBroadPop
import com.cboda.intelligent.R
import com.cboda.intelligent.configuration.SafeJumpBroadEnum
import com.cboda.intelligent.databinding.DialogPopAuthBroadCodeBinding
import com.cboda.intelligent.databinding.DialogPopOpenBeautifulAccoundBinding
import com.cboda.intelligent.utils.click
import com.cboda.intelligent.utils.isSubmitCanGo
import com.cboda.intelligent.utils.pasteToEditText
import com.cboda.intelligent.viewmodel.SafeCenterViewBroadModel
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch


//开通靓号pop
class OpenBeautifulNumbPop(@JvmField val context: Context, @JvmField val backInvoke: (String) -> Unit = {}) :
    BaseBottomBroadPop(context) {

    private var binding: DialogPopOpenBeautifulAccoundBinding = DialogPopOpenBeautifulAccoundBinding.inflate(
        LayoutInflater.from(context)
    )

    init {
        contentView = binding.root
        setBackgroundColor(Color.parseColor("#8f000000"))
    }

    override fun onViewCreated(contentView: View) {
        super.onViewCreated(contentView)
        initListener()
        initData()
    }

    private fun initListener() {
        binding.mIvClose.setOnClickListener {
            dismiss()
        }
        binding.mTvCopy.setOnClickListener {
            pasteToEditText(binding.mEtCode)
        }
        binding.mTvNext.isSubmitCanGo(false)
        binding.mEtCode.addTextChangedListener { editable ->
            binding.mTvNext.isSubmitCanGo(binding.mEtCode.text.trim().toString().isNotEmpty())
        }
    }

    private fun initData() {
        binding.mTvNext.click {
            backInvoke.invoke(binding.mEtCode.text.toString().trim())
        }
    }

    override fun onDismiss() {
        super.onDismiss()
    }
}