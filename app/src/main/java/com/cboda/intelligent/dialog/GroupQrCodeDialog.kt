package com.cboda.intelligent.dialog

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.isGone
import com.blankj.utilcode.util.SizeUtils.dp2px
import com.cboda.baselibrary.bean.CommonResultBroadBean
import com.cboda.baselibrary.utils.getAppStr
import com.cboda.baselibrary.utils.loadImg
import com.cboda.baselibrary.utils.requestPermission
import com.cboda.chatui.utils.E.loadGroupImg
import com.cboda.intelligent.R
import com.cboda.intelligent.databinding.GroupQrCodeDialogBinding
import com.cboda.intelligent.utils.BitmapBroadUtils1
import com.cboda.intelligent.utils.click
import com.cboda.intelligent.utils.showToastMain
import com.king.zxing.util.CodeUtils
import razerdp.basepopup.BasePopupWindow

class GroupQrCodeDialog(val context: Context, val groupId: String, val commonResult: CommonResultBroadBean.CommonResult? = null) : BasePopupWindow(context) {
    private val binding = GroupQrCodeDialogBinding.inflate(LayoutInflater.from(context))

    init {
        contentView = binding.root
        popupGravity = Gravity.CENTER
    }

    @SuppressLint("SetTextI18n")
    private fun setData(commonResult: CommonResultBroadBean.CommonResult? = null) {
        commonResult?.let {
            loadGroupImg(binding.groupImg, groupId, it.icon)
            binding.groupId.text = "${getAppStr(com.cboda.baselibrary.R.string.msg_group_id, context)}${it.idno ?: ""}"
            binding.groupName.text = it.name ?: ""
            if (!TextUtils.isEmpty(it.tQrcode)) {
                binding.groupCodeImg.loadImg(CodeUtils.createQRCode(it.tQrcode, dp2px(260f)), null)
            } else if (it.qrcode?.isNotEmpty() == true) {
                binding.groupCodeImg.loadImg(it.qrcode)
            }


        }

    }

    override fun onViewCreated(contentView: View) {
        super.onViewCreated(contentView)
        setData(commonResult)
        binding.saveGroupCode.click {
            requestPermission(arrayOf(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_MEDIA_IMAGES else Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                binding.saveGroupCode.isGone = true
                binding.mLlSaveView.postDelayed({
                    BitmapBroadUtils1.saveBitmap2(BitmapBroadUtils1.convertMeasureBitmap(binding.mLlSaveView), context)
                    showToastMain(R.string.tipSaveSuccess)
                    binding.saveGroupCode.isGone = false
                }, 500)
            }
        }
    }

}
