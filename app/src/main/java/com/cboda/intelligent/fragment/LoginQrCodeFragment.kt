package com.cboda.intelligent.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isGone
import com.blankj.utilcode.util.SizeUtils.dp2px
import com.cboda.baselibrary.bean.UserInfoBean
import com.cboda.baselibrary.utils.HttpsBroadConfig
import com.cboda.baselibrary.utils.MMKVBroadUtils
import com.cboda.baselibrary.utils.MMKVBroadUtils.Companion.OLD_QR_CODE
import com.cboda.baselibrary.utils.click
import com.cboda.baselibrary.utils.getAppStr
import com.cboda.baselibrary.utils.loadImg
import com.cboda.baselibrary.utils.requestPermission
import com.cboda.baselibrary.utils.showCommonStrTip
import com.cboda.baselibrary.utils.showCommonTip
import com.cboda.intelligent.R
import com.cboda.intelligent.activity.UpdatePwdActivity
import com.cboda.intelligent.base.BaseBroadFragment
import com.cboda.intelligent.databinding.LoginQrCodeFragmentBinding
import com.cboda.intelligent.utils.BitmapBroadUtils1
import com.cboda.intelligent.utils.showToastMain
import com.cboda.intelligent.viewmodel.LoginViewBroadModel
import com.king.zxing.util.CodeUtils

class LoginQrCodeFragment : BaseBroadFragment<LoginQrCodeFragmentBinding, LoginViewBroadModel>() {

    override fun onCreateViewBinding(inflater: LayoutInflater, parent: ViewGroup?, savedInstanceState: Bundle?) = LoginQrCodeFragmentBinding.inflate(inflater, parent, false)

    private var userInfoBean: UserInfoBean? = null
    private var closeEyesIsGone: Boolean = true
    private var widthIsBig = true

    fun setData(data: UserInfoBean) {
        userInfoBean = data
        setData()
    }


    @SuppressLint("SetTextI18n")
    private fun setData() {
        mBinding?.let {
            //82环境使用老登录隐藏二维码刷新
//            binding.requestQrCode.isGone = HttpsBroadConfig.currentHostHttpUrlType == HttpsBroadConfig.RELEASE_TYPE
            binding.requestQrCode.isGone = true
            userInfoBean?.let { userInfo ->
                binding.userImg.loadImg(userInfo.userImg)
                binding.userName.text = userInfo.name
                binding.userId.text = "ID:${userInfo.showId}"
                if (userInfo.loginQrcode.endsWith(".jpg")) {
                    if (!widthIsBig) {
                        val lp = binding.qrCode.layoutParams
                        lp.width = dp2px(260f)
                        lp.height = dp2px(260f)
                        binding.qrCode.layoutParams = lp
                    }
                    widthIsBig = true
                    binding.qrCode.loadImg(userInfo.loginQrcode)
                } else {
                    if (widthIsBig) {
                        val lp = binding.qrCode.layoutParams
                        lp.width = dp2px(200f)
                        lp.height = dp2px(200f)
                        binding.qrCode.layoutParams = lp
                    }
                    widthIsBig = false
                    binding.qrCode.loadImg(CodeUtils.createQRCode(userInfo.loginQrcode, dp2px(260f)), null)
                }
            }
        }
    }

    override fun initData() {
        super.initData()
        setData()
        //82环境登录隐藏旧二维码弹窗
        if (HttpsBroadConfig.currentHostHttpUrlType != HttpsBroadConfig.RELEASE_TYPE) {
            val oldQrCode = MMKVBroadUtils.getSqlStrData(OLD_QR_CODE, "")
            val mContext = requireContext()
            if (!TextUtils.isEmpty(oldQrCode)) {
                MMKVBroadUtils.saveSqlStrData(OLD_QR_CODE, "")
                showCommonStrTip(
                    getAppStr(com.cboda.baselibrary.R.string.urgent_reminder, mContext),
                    getAppStr(com.cboda.baselibrary.R.string.qr_code_expired, mContext),
                    "",
                    getAppStr(R.string.mine_ok, mContext),
                    mContext
                ) {
                    savePhoto()
                }
            }
        }
    }

    override fun initListener() {
        super.initListener()
        binding.showQrcodeTv.click {
            closeEyesIsGone = false
            setCloseEyesIsGone(false)
        }
        binding.closeEyes.click {
            closeEyesIsGone = true
            setCloseEyesIsGone(true)
        }
        binding.requestQrCode.click {
            viewModel.checkSafe(userInfoBean, requireContext()) {
                viewModel.resetQrCode().observe(this) {
                    if (null != it && !TextUtils.isEmpty(it)) {
                        userInfoBean?.loginQrcode = it
                        setData()
                    }
                }
            }
        }
        binding.save.click {
            savePhoto()
        }
    }


    private fun savePhoto() {
        requestPermission(arrayOf(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_MEDIA_IMAGES else Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            if (closeEyesIsGone) {
                setCloseEyesIsGone(false)
            }
            binding.closeEyes.isGone = true
            binding.save.isGone = true
            binding.requestQrCode.isGone = true
            binding.loginQrCodeFragmentLayout.postDelayed({
                BitmapBroadUtils1.saveBitmap2(BitmapBroadUtils1.convertMeasureBitmap(binding.loginQrCodeFragmentLayout), requireContext())
                showToastMain(R.string.tipSaveSuccess)
                if (closeEyesIsGone) {
                    setCloseEyesIsGone(true)
                } else {
                    binding.closeEyes.isGone = false
                }
                binding.save.isGone = false
                binding.requestQrCode.isGone = false
            }, 500)
        }
    }

    private fun setCloseEyesIsGone(isGone: Boolean) {
        binding.closeEyes.isGone = isGone
        binding.qrCode.isGone = isGone
//        binding.mViewDash.isInvisible = !isGone
        binding.qrCodeBlur.isGone = !isGone
        binding.showQrcodeTv.isGone = !isGone
    }


}