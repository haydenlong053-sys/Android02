package com.cboda.intelligent.activity

import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import cn.wildfirechat.remote.ChatManager
import com.bumptech.glide.Glide
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.cboda.intelligent.R
import com.cboda.intelligent.base.BaseSimpleBroadActivity
import com.cboda.intelligent.dialog.NormalBroadDialog
import com.cboda.intelligent.dialog.NormalBroadDialog.OnButtonClick
import com.cboda.intelligent.utils.BitmapBroadUtils1
import com.cboda.intelligent.utils.ToastBroadUtil
import com.cboda.intelligent.utils.GlideLoaderBroadHelper
import com.cboda.intelligent.viewmodel.CommViewBroadModel

class QunCodeBroadActivity : BaseSimpleBroadActivity() {
    private var qunId = ""
    private var viewModel: CommViewBroadModel? = null
    private lateinit var tvSave: TextView
    private lateinit var llEwm: LinearLayout
    private lateinit var erweima: ImageView
    private lateinit var tvusername: TextView
    private lateinit var ivTouxiang:
            ImageView

    private fun initViews() {
        ivTouxiang = findViewById(R.id.ivTouxiang)
        tvusername = findViewById(R.id.tvusername)
        erweima = findViewById(R.id.erweima)
        llEwm = findViewById(R.id.llEwm)
        tvSave = findViewById(R.id.tvSave)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViews()
        qunId = intent?.getStringExtra("qunid") ?: ""
        viewModel = ViewModelProvider(this)[CommViewBroadModel::class.java]
        getToolbarTitle()?.text = getString(R.string.qunerweim)

        viewModel?.gonggaoLiveData?.observe(this) {
            it.data?.apply {
                tvusername?.text = this.name ?: ""
                if (this.qrcode?.isNotEmpty() == true) {
                    GlideLoaderBroadHelper.loadUrl(this@QunCodeBroadActivity, this.qrcode ?: "", erweima)
                }
                val groupInfo = ChatManager.Instance().getGroupInfo(qunId, false)
                Glide.with(this@QunCodeBroadActivity).load(groupInfo?.portrait ?: "")
                    .placeholder(com.cboda.chatui.R.mipmap.ic_group_chat)
                    .into(ivTouxiang)

//                if (this.icon?.isNotEmpty() == true){
//                    val iconList = arrayListOf(this.icon?:"")
//                    ivTouxiang?.setImageId(qunId)
//                    ivTouxiang?.displayImage(iconList as List<Any>?)?.load(qunId)
//                } else {
//                    ivTouxiang?.setImageId(qunId)
//                    val iconList = arrayListOf<String>()
//                    this.userList?.forEachIndexed { index, commonBean ->
//                        iconList.add(commonBean.icon?:"")
//                    }
//                    ivTouxiang?.displayImage(iconList as List<Any>?)?.load(qunId)
//                }

            }
        }

        viewModel?.gonggaoxiangqing(this, qunId)
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
                        "保存图片功能需要存储权限，是否请求权限？",
                        "取消",
                        "确定",
                        true
                    )
                        .setOnButtonClickListener(object : OnButtonClick {
                            override fun OnRightClick() {
                                val permission: XXPermissions =
                                    XXPermissions.with(this@QunCodeBroadActivity)
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
                        "保存图片功能需要存储权限，是否请求权限？",
                        "取消",
                        "确定",
                        true
                    )
                        .setOnButtonClickListener(object : OnButtonClick {
                            override fun OnRightClick() {
                                val permission: XXPermissions =
                                    XXPermissions.with(this@QunCodeBroadActivity).permission(*perms1)
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

    private fun goNest() {
        BitmapBroadUtils1.saveBitmap2(BitmapBroadUtils1.convertMeasureBitmap(llEwm), this@QunCodeBroadActivity)
        ToastBroadUtil.show("保存成功")
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_quncode
    }
}