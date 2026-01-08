package com.cboda.intelligent.dialog.pop

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.king.zxing.util.CodeUtils
import com.cboda.intelligent.R
import com.cboda.intelligent.configuration.CommonBroadConfig
import com.cboda.intelligent.databinding.DialogPopSaveMoneyBroadQrcodeBinding
import com.cboda.intelligent.dialog.NormalBroadDialog
import com.cboda.intelligent.utils.BitmapBroadUtils1
import com.cboda.intelligent.utils.ToastBroadUtil
import com.cboda.intelligent.utils.copyToClipboard
import razerdp.basepopup.BasePopupWindow

//保存提示pop
class SaveMoneyQrCodeTipBroadPop(val context: Context,val qrCodeString: String?) : BasePopupWindow(context) {
    private var binding: DialogPopSaveMoneyBroadQrcodeBinding = DialogPopSaveMoneyBroadQrcodeBinding.inflate(
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
                NormalBroadDialog(
                    context,
                    context.getString(R.string.温馨提示),
                    "保存图片功能需要存储权限，是否请求权限？",
                    "取消",
                    "确定",
                    true
                )
                    .setOnButtonClickListener(object : NormalBroadDialog.OnButtonClick {
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
            val bitmap = CodeUtils.createQRCode(CommonBroadConfig.seekWeb4+qrCodeString, 400)
            binding.mIvQrCodeAuth.setImageBitmap(bitmap)
        }catch (e : Exception){
            e.printStackTrace()
        }

    }

    private fun goSaveQr() {
//        binding.mTvSaveQrcode.visibility = View.INVISIBLE
//        Handler().postDelayed({
//            BitmapBroadUtils1.saveBitmap2(
//                BitmapBroadUtils1.convertMeasureBitmap(binding.mLlSaveView),
//                context
//            )
//            ToastBroadUtil.show("保存成功！")
//            dismiss();
//        }, 500)
        BitmapBroadUtils1.saveBitmap2(
            BitmapBroadUtils1.convertMeasureBitmap(binding.mLlSaveView),
            context
        )
        ToastBroadUtil.show("保存成功！")
        dismiss()
    }


    override fun onDismiss() {
        super.onDismiss()
    }
}