package com.cboda.intelligent.dialog.pop

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import cn.wildfirechat.remote.ChatManager
import com.blankj.utilcode.util.SizeUtils.dp2px
import com.cboda.baselibrary.utils.APP_TAG
import com.cboda.baselibrary.utils.eLogD
import com.cboda.baselibrary.utils.loadImg
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.cboda.intelligent.R
import com.cboda.intelligent.databinding.DialogPopSaveGroupBroadQrcodeBinding
import com.cboda.intelligent.dialog.NormalBroadDialog
import com.cboda.intelligent.utils.BitmapBroadUtils1
import com.cboda.intelligent.utils.GlideBroadUtil
import com.cboda.intelligent.utils.ToastBroadUtil
import com.cboda.intelligent.viewmodel.CommViewBroadModel
import com.king.zxing.util.CodeUtils
import razerdp.basepopup.BasePopupWindow

//群保存二维码提示pop
class SaveGroupQrCodeBroadPop(val context: Context, val qunId: String) : BasePopupWindow(context) {
    //    private val scope = MainScope() // 创建主线程的协程作用域
    private var viewModel: CommViewBroadModel? = null
    private var binding: DialogPopSaveGroupBroadQrcodeBinding = DialogPopSaveGroupBroadQrcodeBinding.inflate(
        LayoutInflater.from(context)
    )

    init {
        contentView = binding.root
        popupGravity = Gravity.CENTER

    }


    @SuppressLint("SetTextI18n")
    override fun onViewCreated(contentView: View) {
        super.onViewCreated(contentView)
        initViewModel()
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

    @SuppressLint("SetTextI18n")
    private fun initViewModel() {
        if (context is FragmentActivity) {
            val activity = context
            // 获取该 Activity 作用域的 ViewModel
            viewModel = ViewModelProvider(activity).get<CommViewBroadModel>(CommViewBroadModel::class.java)
            // 现在可以使用 viewModel 进行数据操作
            eLogD(APP_TAG, "viewModel==" + viewModel)
            viewModel?.gonggaoLiveData?.observe(context) {
                it.data?.apply {
                    binding.mTvName.text = this.name ?: ""
                    binding.mTvId.text = "账号ID：${this.idno}"
                    if (!TextUtils.isEmpty(this.tQrcode)) {
                        binding.mIvQrCodeAuth.loadImg(CodeUtils.createQRCode(this.tQrcode, dp2px(260f)), null)
                    } else if (this.qrcode?.isNotEmpty() == true) {
                        GlideBroadUtil.setImag(context, this.qrcode ?: "", binding.mIvQrCodeAuth)
                    }
                    val groupInfo = ChatManager.Instance().getGroupInfo(qunId, false)
                    GlideBroadUtil.setHeadImag(context, groupInfo?.portrait ?: "", binding.mIvIcon)

                }
            }

            viewModel?.gonggaoxiangqing(context, qunId)

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
        ToastBroadUtil.show(context.getString(R.string.tipSaveSuccess))
        dismiss()
    }


    override fun onDismiss() {
//        scope.cancel()
        super.onDismiss()
    }
}