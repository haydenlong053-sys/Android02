package com.lxkj.aiim.dialog.pop

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.lxkj.aiim.R
import com.lxkj.aiim.databinding.DialogPopSaveUserQrcodeBinding
import com.lxkj.aiim.lj.dialogfra.NormalDialog
import com.lxkj.aiim.lj.utils.BitmapUtils1
import com.lxkj.aiim.lj.utils.GlideUtil
import com.lxkj.aiim.lj.utils.ToastUtil
import com.lxkj.aiim.model.UserInfoModel
import razerdp.basepopup.BasePopupWindow

//保存提示pop
class UserQrCodeTipPop(val context: Context, val userInfo: UserInfoModel?) : BasePopupWindow(context) {
    private var binding: DialogPopSaveUserQrcodeBinding = DialogPopSaveUserQrcodeBinding.inflate(
        LayoutInflater.from(context)
    )

    init {
        contentView = binding.root
        popupGravity = Gravity.CENTER

    }


    @SuppressLint("SetTextI18n")
    override fun onViewCreated(contentView: View) {
        super.onViewCreated(contentView)
        GlideUtil.setHeadImag(context, userInfo?.icon, binding.mIvIcon)
        GlideUtil.setImag(context, userInfo?.qrcode, binding.mIvQrCodeAuth)
        binding.mTvName.text = userInfo?.name
        binding.mTvId.text = "账号ID：${userInfo?.idno ?: userInfo?.id}"
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