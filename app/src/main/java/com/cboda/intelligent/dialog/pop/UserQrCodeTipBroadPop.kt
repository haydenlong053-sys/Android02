package com.cboda.intelligent.dialog.pop

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.cboda.intelligent.R
import com.cboda.intelligent.databinding.DialogPopSaveUserBroadQrcodeBinding
import com.cboda.intelligent.dialog.NormalBroadDialog
import com.cboda.intelligent.utils.BitmapBroadUtils1
import com.cboda.intelligent.utils.GlideBroadUtil
import com.cboda.intelligent.utils.ToastBroadUtil
import com.cboda.intelligent.model.UserInfoBroadModel
import razerdp.basepopup.BasePopupWindow

//保存提示pop
class UserQrCodeTipBroadPop(val context: Context, val userInfo: UserInfoBroadModel?) : BasePopupWindow(context) {
    private var binding: DialogPopSaveUserBroadQrcodeBinding = DialogPopSaveUserBroadQrcodeBinding.inflate(
        LayoutInflater.from(context)
    )

    init {
        contentView = binding.root
        popupGravity = Gravity.CENTER

    }


    @SuppressLint("SetTextI18n")
    override fun onViewCreated(contentView: View) {
        super.onViewCreated(contentView)
        GlideBroadUtil.setHeadImag(context, userInfo?.icon, binding.mIvIcon)
        GlideBroadUtil.setImag(context, userInfo?.qrcode, binding.mIvQrCodeAuth)
        binding.mTvName.text = userInfo?.name
        binding.mTvId.text = "ID：${userInfo?.idno ?: userInfo?.id}"
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