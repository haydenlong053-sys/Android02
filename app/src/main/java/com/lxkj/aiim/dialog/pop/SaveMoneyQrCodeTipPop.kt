package com.lxkj.aiim.dialog.pop

import android.content.Context
import android.os.Handler
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.LifecycleCoroutineScope
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.king.zxing.util.CodeUtils
import com.cboda.intelligent.R
import com.cboda.intelligent.databinding.ActivityAuthStep1Binding
import com.cboda.intelligent.databinding.DialogPopSaveAuthBinding
import com.cboda.intelligent.databinding.DialogPopSaveMoneyQrcodeBinding
import com.lxkj.aiim.lj.configuration.CommonConfig
import com.lxkj.aiim.lj.dialogfra.NormalDialog
import com.lxkj.aiim.lj.event.ActionEventBean
import com.lxkj.aiim.lj.utils.BitmapUtils1
import com.lxkj.aiim.lj.utils.ToastUtil
import com.lxkj.aiim.lj.utils.copyToClipboard
import com.lxkj.aiim.lj.utils.showBase64QrCodeIv
import com.lxkj.aiim.model.TwoStepAuthBean
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import razerdp.basepopup.BasePopupWindow

//保存提示pop
class SaveMoneyQrCodeTipPop(val context: Context,val qrCodeString: String?) : BasePopupWindow(context) {
    private var binding: DialogPopSaveMoneyQrcodeBinding = DialogPopSaveMoneyQrcodeBinding.inflate(
        LayoutInflater.from(context)
    )

    init {
        contentView = binding.root
        popupGravity = Gravity.CENTER

    }


    override fun onViewCreated(contentView: View) {
        super.onViewCreated(contentView)
        initData()
        binding.mLlGoogleAuthCopy.setOnClickListener {
            copyToClipboard(context, binding.mTvCopyDes.text.toString())
        }
        binding.mTvSaveQrcode.setOnClickListener {
            val perms = arrayOf(Permission.READ_MEDIA_IMAGES, Permission.CAMERA)
            if (XXPermissions.isGranted(context, perms)) {
                goSaveQr()

            } else {
                NormalDialog(
                    context,
                    context.getString(R.string.温馨提示),
                    "保存图片功能需要存储权限，是否请求权限？",
                    "取消",
                    "确定",
                    true
                )
                    .setOnButtonClickListener(object : NormalDialog.OnButtonClick {
                        override fun OnRightClick() {
                            val permission = XXPermissions.with(context).permission(perms);
                            permission.request(object : OnPermissionCallback {
                                override fun onGranted(
                                    permissions: List<String?>,
                                    allGranted: Boolean
                                ) {
                                    goSaveQr()
                                }

                                override fun onDenied(
                                    permissions: List<String?>,
                                    doNotAskAgain: Boolean
                                ) {
                                    super.onDenied(permissions, doNotAskAgain)
                                }

                            })
                        }

                        override fun OnLeftClick() {

                        }


                    }).show()
            }
        }
        binding.mViewClose.setOnClickListener {
            dismiss()
        }
    }

    private fun initData() {
        binding.mTvCopyDes.text =qrCodeString
        try {
            val bitmap = CodeUtils.createQRCode(CommonConfig.seekWeb4+qrCodeString, 400)
            binding.mIvQrCodeAuth.setImageBitmap(bitmap)
        }catch (e : Exception){
            e.printStackTrace()
        }

    }

    private fun goSaveQr() {
//        binding.mTvSaveQrcode.visibility = View.INVISIBLE
//        Handler().postDelayed({
//            BitmapUtils1.saveBitmap2(
//                BitmapUtils1.convertMeasureBitmap(binding.mLlSaveView),
//                context
//            )
//            ToastUtil.show("保存成功！")
//            dismiss();
//        }, 500)
        BitmapUtils1.saveBitmap2(
            BitmapUtils1.convertMeasureBitmap(binding.mLlSaveView),
            context
        )
        ToastUtil.show("保存成功！")
        dismiss()
    }


    override fun onDismiss() {
        super.onDismiss()
    }
}