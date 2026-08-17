package com.cboda.intelligent.dialog.pop

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import com.blankj.utilcode.util.SizeUtils.dp2px
import com.cboda.baselibrary.bean.UserInfoBean
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.cboda.intelligent.R
import com.cboda.intelligent.databinding.DialogPopSaveUserBroadQrcodeBinding
import com.cboda.intelligent.dialog.NormalBroadDialog
import com.cboda.intelligent.utils.BitmapBroadUtils1
import com.cboda.intelligent.utils.GlideBroadUtil
import com.cboda.intelligent.utils.ToastBroadUtil
import com.cboda.baselibrary.bean.UserInfoBroadModel
import com.cboda.baselibrary.utils.getAppNamedStr
import com.cboda.baselibrary.utils.loadImg
import com.king.zxing.util.CodeUtils
import razerdp.basepopup.BasePopupWindow

//保存提示pop
class UserQrCodeTipBroadPop(val context: Context, val userInfo: UserInfoBean?) : BasePopupWindow(context) {
    private var binding: DialogPopSaveUserBroadQrcodeBinding = DialogPopSaveUserBroadQrcodeBinding.inflate(
        LayoutInflater.from(context)
    )

    init {
        contentView = binding.root
        popupGravity = Gravity.CENTER

    }


    override fun onViewCreated(contentView: View) {
        super.onViewCreated(contentView)
        binding.mTvDes2.text = getAppNamedStr(R.string.user_qrcode_add_friend, context)
        GlideBroadUtil.setHeadImag(context, userInfo?.userImg, binding.mIvIcon)
        if (userInfo != null && !TextUtils.isEmpty(userInfo.businessQrCode)) {
            binding.mIvQrCodeAuth.loadImg(CodeUtils.createQRCode(userInfo.businessQrCode, dp2px(260f)), null)
        } else {
            binding.mIvQrCodeAuth.loadImg(userInfo?.qrcode, null)
        }
        binding.mTvName.text = userInfo?.name
        binding.mTvId.text = "ID：${userInfo?.showId ?: userInfo?.id}"
        binding.mTvSaveQrcode.setOnClickListener {
            val perms = arrayOf(Permission.READ_MEDIA_IMAGES, Permission.CAMERA)
            if (XXPermissions.isGranted(context, perms)) {
                goSaveQr()

            } else {
                NormalBroadDialog(
                    context,
                    context.getString(R.string.温馨提示),
                    context.getString(R.string.save_image_permission_request),
                    context.getString(R.string.quxiao),
                    context.getString(R.string.queding),
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
        ToastBroadUtil.show(context.getString(R.string.save_success_exclamation))
        dismiss()
    }


    override fun onDismiss() {
        super.onDismiss()
    }
}