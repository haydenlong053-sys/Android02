package com.lxkj.aiim.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.cboda.baselibrary.utils.AppPermissionBroadUtils
import com.cboda.baselibrary.utils.NetworkFileBroadSaver
import com.lxkj.aiim.R
import com.lxkj.aiim.base.BaseLazyFragment
import com.lxkj.aiim.compose.MirrorScreen
import com.lxkj.aiim.dialog.pop.UserQrCodeTipPop
import com.lxkj.aiim.lj.SignInActivity
import com.lxkj.aiim.lj.UserInfoActivity
import com.lxkj.aiim.lj.utils.ToastUtil
import com.lxkj.aiim.viewmodel.MirrorViewModel
import kotlinx.coroutines.launch

@Deprecated("废弃")
class MirrorFragment: BaseLazyFragment() {
    private  val viewModel: MirrorViewModel by  viewModels()
    companion object {
        fun getInstance(): Fragment {
            return MirrorFragment()
        }
    }
    private  lateinit var  mirrorView: ComposeView

    override fun loadData() {
    }

    override fun getLayoutId(): Int {
      return  R.layout.fragment_mirror

    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                // 控制二维码弹窗显示的状态
                var showQr by remember { mutableStateOf(false) }
                val uiState by viewModel.uiState.collectAsState()

                MirrorScreen(
                    viewModel,
                    toRedPacket = {
                        context?.startActivity(Intent(context, SignInActivity::class.java))
                    },
                    toQrCodePage = {
                        UserQrCodeTipPop(context, uiState.userInfo).showPopupWindow()
                        showQr = true
                    },
                    toInfoPage = {
                        val intent = Intent(context, UserInfoActivity::class.java)
                        context?.startActivity(intent)
                    }
                )

                if (showQr) {

//                    QrCodeDialog(
//                        userName = uiState.userInfo?.name,
//                        userId = uiState.userInfo?.idno ?: uiState.userInfo?.id,
//                        avatarResId = R.mipmap.ic_head_defaut,
//                        avatarUrl = uiState.userInfo?.icon,
//                        qrContent = uiState.userInfo?.qrcode?:"",
//                        onDismiss = { showQr = false },
//                        onSaveToGallery = {
//                            val hasStoragePermission =
//                                AppPermissionBroadUtils.hasStoragePermission(requireContext())
//                            if (hasStoragePermission){
//                                saveQrCodeToGallery(it)
//                            }else{
//                                viewModel.saveQrCodeUrl=it
//                                PermissionExplainBroadPopup.Builder(requireActivity())
//                                    .setTitle("温馨提示")
//                                    .addPermissionExplain("为了让你可以保存二维码,我们需要访问你的存储权限")
//                                    .setOnConfirm {
//                                        AppPermissionBroadUtils.checkAndRequestStoragePermission(this@MirrorFragment,1002,object :
//                                            AppPermissionBroadUtils.SimplePermissionCallback{
//                                            override fun onGranted() {
//                                                saveQrCodeToGallery(it)
//                                            }
//
//                                            override fun onDenied(neverAskAgain: Boolean) {
//                                                val message = if (neverAskAgain) {
//                                                    "存储权限被永久拒绝，请到设置中手动开启"
//
//                                                } else {
//                                                    "需要存储权限才能保存二维码"
//                                                }
//                                                ToastUtil.show(message)
//
//
//
//
//                                            }
//
//                                        })
//                                    }
//                                    .show()
//
//                            }
//
//
//                        }
//                    )
                }
            }
        }
    }

//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        mirrorView =view.findViewById(R.id.mirror_view)
//        mirrorView.setContent {
//            // 控制二维码弹窗显示的状态
//            var showQr by remember { mutableStateOf(false) }
//            val uiState by viewModel.uiState.collectAsState()
//
//            MirrorScreen(
//                viewModel,
//                toRedPacket = {
//                    context?.startActivity(Intent(context, SignInActivity::class.java))
//                },
//                toQrCodePage = {
//                    showQr = true
//                }
//            )
//
//            if (showQr) {
//                QrCodeDialog(
//                    userName = uiState.userInfo?.name,
//                    userId = uiState.userInfo?.idno ?: uiState.userInfo?.id,
//                    avatarResId = R.mipmap.ic_head_defaut,
//                    avatarUrl = uiState.userInfo?.icon,
//                    qrContent = uiState.userInfo?.qrcode?:"",
//                    onDismiss = { showQr = false },
//                    onSaveToGallery = {
//                        val hasStoragePermission =
//                            AppPermissionBroadUtils.hasStoragePermission(requireContext())
//                        if (hasStoragePermission){
//                             saveQrCodeToGallery(it)
//                        }else{
//                            viewModel.saveQrCodeUrl=it
//                            PermissionExplainBroadPopup.Builder(requireActivity())
//                                .setTitle("温馨提示")
//                                .addPermissionExplain("为了让你可以保存二维码,我们需要访问你的存储权限")
//                                .setOnConfirm {
//                                    AppPermissionBroadUtils.checkAndRequestStoragePermission(this,1002,object :
//                                        AppPermissionBroadUtils.SimplePermissionCallback{
//                                        override fun onGranted() {
//                                            saveQrCodeToGallery(it)
//                                        }
//
//                                        override fun onDenied(neverAskAgain: Boolean) {
//                                            val message = if (neverAskAgain) {
//                                                "存储权限被永久拒绝，请到设置中手动开启"
//
//                                            } else {
//                                                "需要存储权限才能保存二维码"
//                                            }
//                                            ToastUtil.show(message)
//
//
//
//
//                                        }
//
//                                    })
//                                }
//                                .show()
//
//                        }
//
//
//                    }
//                )
//            }
//        }
//    }
    private  fun saveQrCodeToGallery(codeUrl:String){
        val fileName= "${requireContext().getString(R.string.app_name)}-${System.currentTimeMillis()}-qrcode"
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.saveQrCodeUrl=""
            val saveResult = NetworkFileBroadSaver.saveFileFromUrl(requireActivity(), codeUrl,fileName)
            if (saveResult.success){
                ToastUtil.show("保存成功")
            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1002->{
                if (AppPermissionBroadUtils.hasStoragePermission(requireContext())){
                    saveQrCodeToGallery(viewModel.saveQrCodeUrl)
                }

            }

        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.getUserInfo()
    }
}



