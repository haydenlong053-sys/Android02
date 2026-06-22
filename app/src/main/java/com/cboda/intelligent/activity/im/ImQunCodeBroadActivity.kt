package com.cboda.intelligent.activity.im

import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.blankj.utilcode.util.SizeUtils.dp2px
import com.cboda.baselibrary.utils.loadImg
import com.cboda.intelligent.R
import com.cboda.intelligent.base.BaseSimpleBroadActivity
import com.cboda.intelligent.dialog.NormalBroadDialog
import com.cboda.intelligent.dialog.NormalBroadDialog.OnButtonClick
import com.cboda.intelligent.utils.BitmapBroadUtils1
import com.cboda.intelligent.utils.GlideLoaderBroadHelper
import com.cboda.intelligent.utils.ToastBroadUtil
import com.freddy.im.http.ImGroupHttpExample
import com.freddy.im.http.model.im.GroupQrCodeRes
import com.freddy.im.http.model.im.GroupVo
import com.freddy.im.listener.OnHttpImListener
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.king.zxing.util.CodeUtils

/** 自研群二维码（对齐野火 {@link com.cboda.intelligent.activity.QunCodeBroadActivity}）。 */
class ImQunCodeBroadActivity : BaseSimpleBroadActivity() {

    private var qunId = ""
    private var groupVo: GroupVo? = null
    private lateinit var erweima: ImageView
    private lateinit var tvusername: TextView
    private lateinit var ivTouxiang: ImageView
    private lateinit var tvSave: TextView
    private lateinit var llEwm: LinearLayout

    override fun getLayoutId(): Int = R.layout.activity_qun_broad_code

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        qunId = intent?.getStringExtra("qunid") ?: ""
        groupVo = ImGroupIntent.getGroupVo(intent)
        erweima = findViewById(R.id.erweima)
        tvusername = findViewById(R.id.tvusername)
        ivTouxiang = findViewById(R.id.ivTouxiang)
        llEwm = findViewById(R.id.llEwm)
        tvSave = findViewById(R.id.tvSave)
        getToolbarTitle()?.text = getString(R.string.qunerweim)

        tvusername.text = groupVo?.name ?: ""
        val icon = groupVo?.icon
        if (!icon.isNullOrEmpty()) {
            GlideLoaderBroadHelper.loadUrl(this, icon, ivTouxiang)
        }

        loadQrCode()
        tvSave.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // 权限已被授予，可以访问外部存储
                val perms1 = arrayOf(Permission.READ_MEDIA_IMAGES)
                if (XXPermissions.isGranted(this, *perms1)) {
                    goNest()

                } else {
                    NormalBroadDialog(
                        this,
                        getString(R.string.温馨提示),
                        getString(R.string.save_image_permission_request),
                        getString(R.string.quxiao),
                        getString(R.string.queding),
                        true
                    )
                        .setOnButtonClickListener(object : OnButtonClick {
                            override fun OnRightClick() {
                                val permission: XXPermissions =
                                    XXPermissions.with(this@ImQunCodeBroadActivity)
                                        .permission(*perms1)
                                permission.request(object : OnPermissionCallback {
                                    override fun onGranted(
                                        permissions: List<String>,
                                        allGranted: Boolean
                                    ) {
                                        goNest()
                                    }

                                    override fun onDenied(
                                        permissions: List<String>,
                                        doNotAskAgain: Boolean
                                    ) {
                                        super.onDenied(permissions, doNotAskAgain)
                                        ToastBroadUtil.show(getString(R.string.权限被拒绝))
                                    }
                                })
                            }

                            override fun OnLeftClick() {
                            }
                        }).show()
                }
            } else {
                val perms1 = arrayOf(
                    Permission.WRITE_EXTERNAL_STORAGE,
                )
                if (XXPermissions.isGranted(this, *perms1)) {
                    goNest()
                } else {
                    NormalBroadDialog(
                        this,
                        getString(R.string.温馨提示),
                        getString(R.string.save_image_permission_request),
                        getString(R.string.quxiao),
                        getString(R.string.queding),
                        true
                    )
                        .setOnButtonClickListener(object : OnButtonClick {
                            override fun OnRightClick() {
                                val permission: XXPermissions =
                                    XXPermissions.with(this@ImQunCodeBroadActivity).permission(*perms1)
                                permission.request(object : OnPermissionCallback {
                                    override fun onGranted(
                                        permissions: List<String>,
                                        allGranted: Boolean
                                    ) {
                                        goNest()
                                    }

                                    override fun onDenied(
                                        permissions: List<String>,
                                        doNotAskAgain: Boolean
                                    ) {
                                        super.onDenied(permissions, doNotAskAgain)
                                        ToastBroadUtil.show(getString(R.string.权限被拒绝))
                                    }
                                })
                            }

                            override fun OnLeftClick() {
                            }
                        }).show()
                }
            }
        }

    }

    private fun loadQrCode() {
        ImGroupHttpExample.requestGetGroupQrCode(qunId, object : OnHttpImListener<GroupQrCodeRes> {
            override fun onHttpImSuccess(result: GroupQrCodeRes) {
                runOnUiThread {
                    val url = result.codeUrl
                    if (!TextUtils.isEmpty(url)) {
                        erweima.loadImg(CodeUtils.createQRCode(url, dp2px(260f)), null)
                    }
                }
            }
        }, null)
    }

    private fun goNest() {
        BitmapBroadUtils1.saveBitmap2(BitmapBroadUtils1.convertMeasureBitmap(llEwm), this@ImQunCodeBroadActivity)
        ToastBroadUtil.show(getString(R.string.tipSaveSuccess))
    }

}
