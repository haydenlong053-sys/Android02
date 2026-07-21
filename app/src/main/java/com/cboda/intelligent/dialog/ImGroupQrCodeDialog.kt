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
import com.cboda.baselibrary.utils.getAppStr
import com.cboda.baselibrary.utils.loadImg
import com.cboda.baselibrary.utils.requestPermission
import com.cboda.chatui.utils.E.loadGroupImg
import com.cboda.intelligent.R
import com.cboda.intelligent.databinding.GroupQrCodeDialogBinding
import com.cboda.intelligent.utils.BitmapBroadUtils1
import com.cboda.intelligent.utils.click
import com.cboda.intelligent.utils.showToastMain
import com.freddy.im.http.ImGroupHttpExample
import com.freddy.im.http.model.im.GroupQrCodeRes
import com.freddy.im.http.model.im.GroupVo
import com.freddy.im.listener.OnHttpImListener
import razerdp.basepopup.BasePopupWindow

/**
 * 自研 IM 群二维码弹框（UI 对齐 [GroupQrCodeDialog]，数据来自 [GroupVo] + `getGroupQrCode`）。
 */
class ImGroupQrCodeDialog(
    context: Context,
    private val groupId: String,
    private val groupVo: GroupVo?,
) : BasePopupWindow(context) {

    private val binding = GroupQrCodeDialogBinding.inflate(LayoutInflater.from(context))

    init {
        contentView = binding.root
        popupGravity = Gravity.CENTER
    }

    @SuppressLint("SetTextI18n")
    private fun bindGroupInfo() {
        val vo = groupVo ?: return
        loadGroupImg(binding.groupImg, groupId, vo.icon)
        val idno = vo.busiExt?.idno ?: ""
        binding.groupId.text = "${getAppStr(com.cboda.baselibrary.R.string.msg_group_id, context)}$idno"
        binding.groupName.text = vo.name ?: ""
    }

    private fun loadQrCode() {
        ImGroupHttpExample.requestGetGroupQrCode(groupId, object : OnHttpImListener<GroupQrCodeRes> {
            override fun onHttpImSuccess(result: GroupQrCodeRes) {
                val url = result.codeUrl
                if (TextUtils.isEmpty(url)) {
                    return
                }
                binding.groupCodeImg.post {
                    binding.groupCodeImg.loadImg(url)
                }
            }
        }, null)
    }

    override fun onViewCreated(contentView: View) {
        super.onViewCreated(contentView)
        bindGroupInfo()
        loadQrCode()
        binding.saveGroupCode.click {
            requestPermission(
                arrayOf(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        Manifest.permission.READ_MEDIA_IMAGES
                    } else {
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    }
                )
            ) {
                binding.saveGroupCode.isGone = true
                binding.mLlSaveView.postDelayed({
                    BitmapBroadUtils1.saveBitmap2(
                        BitmapBroadUtils1.convertMeasureBitmap(binding.mLlSaveView),
                        context
                    )
                    showToastMain(R.string.tipSaveSuccess)
//                    binding.saveGroupCode.isGone = false
                    this.dismiss()
                }, 500)
            }
        }
    }
}
