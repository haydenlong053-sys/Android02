package com.lxkj.aiim.dialog.pop

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import cn.wildfirechat.remote.ChatManager
import com.cboda.baselibrary.utils.eLogD
import com.bumptech.glide.Glide
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.lxkj.aiim.BuildConfig
import com.lxkj.aiim.R
import com.lxkj.aiim.databinding.DialogPopSaveGroupQrcodeBinding
import com.lxkj.aiim.databinding.DialogPopSaveUserQrcodeBinding
import com.lxkj.aiim.lj.bean.ResultBean
import com.lxkj.aiim.lj.configuration.SafeJumpEnum
import com.lxkj.aiim.lj.dialogfra.NormalDialog
import com.lxkj.aiim.lj.http.BaseCallback
import com.lxkj.aiim.lj.utils.BitmapUtils1
import com.lxkj.aiim.lj.utils.GlideUtil
import com.lxkj.aiim.lj.utils.ToastUtil
import com.lxkj.aiim.model.UserInfoModel
import com.lxkj.aiim.utils.GlideLoaderHelper
import com.lxkj.aiim.viewmodel.CommViewModel
import com.lxkj.aiim.viewmodel.SafeCenterViewModel
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import okhttp3.Request
import okhttp3.Response
import razerdp.basepopup.BasePopupWindow

//群保存二维码提示pop
class SaveGroupQrCodePop(val context: Context,val qunId: String) : BasePopupWindow(context) {
//    private val scope = MainScope() // 创建主线程的协程作用域
    private var viewModel: CommViewModel? = null
    private var binding: DialogPopSaveGroupQrcodeBinding = DialogPopSaveGroupQrcodeBinding.inflate(
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

    @SuppressLint("SetTextI18n")
    private fun initViewModel() {
        if (context is FragmentActivity) {
            val activity = context
            // 获取该 Activity 作用域的 ViewModel
            viewModel = ViewModelProvider(activity).get<CommViewModel>(CommViewModel::class.java)
            // 现在可以使用 viewModel 进行数据操作
            eLogD("zhanglong","viewModel=="+viewModel)
            viewModel?.gonggaoLiveData?.observe(context) {
                it.data?.apply {
                    binding.mTvName.text = this.name ?: ""
                    binding.mTvId.text = "账号ID：${this.idno}"
                    if (this.qrcode?.isNotEmpty() == true) {
                        GlideUtil.setImag(context, this.qrcode ?: "", binding.mIvQrCodeAuth)
                    }
                    val groupInfo = ChatManager.Instance().getGroupInfo(qunId, false)
                    GlideUtil.setHeadImag(context, groupInfo?.portrait ?: "", binding.mIvIcon)

                }
            }

            viewModel?.gonggaoxiangqing(context, qunId)

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
//        scope.cancel()
        super.onDismiss()
    }
}