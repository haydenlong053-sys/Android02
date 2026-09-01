package com.cboda.intelligent.activity


import android.content.Intent
import android.text.TextUtils
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.cboda.baselibrary.event.AppBroadEvent
import com.cboda.baselibrary.event.AppEventBroadCode
import com.cboda.baselibrary.network.repository.NetworkBroadRepository
import com.cboda.baselibrary.network.result.onError
import com.cboda.baselibrary.network.result.onFailure
import com.cboda.baselibrary.network.result.onSuccess
import com.cboda.baselibrary.utils.DeviceBroadUtil
import com.cboda.baselibrary.utils.HttpsBroadConfig
import com.cboda.baselibrary.utils.APP_TAG
import com.cboda.baselibrary.utils.MMKVBroadUtils
import com.cboda.baselibrary.utils.eLogD
import com.cboda.baselibrary.utils.getAppStr
import com.cboda.baselibrary.utils.toJson2
import com.cboda.chatui.utils.E.checkUserIsGroup
import com.cboda.intelligent.R
import com.cboda.intelligent.activity.im.GroupJoinPayActivity
import com.cboda.intelligent.activity.im.GroupJoinPayResultActivity
import com.cboda.intelligent.bean.CheckGroupJoiBean
import com.cboda.intelligent.bean.ResultBroadBean
import com.cboda.intelligent.configuration.ScanJumpBroadEnum
import com.cboda.intelligent.dialog.NormalBroadDialog
import com.cboda.intelligent.utils.AppBroadUtils
import com.cboda.intelligent.utils.GlideBroadBroadEngine
import com.cboda.intelligent.utils.JumpBroadUtils
import com.cboda.intelligent.utils.OkHttpBroadHelper
import com.cboda.intelligent.utils.SpotsCallBroadBack
import com.cboda.intelligent.utils.StringBroadUtil
import com.cboda.intelligent.utils.ToastBroadUtil
import com.cboda.intelligent.utils.UrlBroadBroadConstants
import com.cboda.intelligent.utils.isEthWalletLinkQrCode
import com.cboda.intelligent.utils.isTransferLinkQrCode
import com.cboda.intelligent.utils.showToastMain
import com.cboda.intelligent.viewmodel.LoginViewBroadModel
import com.cboda.select.picture.basic.PictureSelector
import com.cboda.select.picture.config.PictureConfig.CHOOSE_REQUEST
import com.cboda.select.picture.config.SelectMimeType
import com.freddy.im.http.ImGroupHttpExample
import com.freddy.im.http.model.im.GroupQrInfoRes
import com.freddy.im.http.model.im.GroupVo
import com.freddy.im.listener.OnHttpImListener
import com.freddy.im.util.ImGroupQrCodeHelper
import com.freddy.im.util.groupToSession
import com.freddy.webtrc.utils.BroadE.checkMembersInGroup
import com.freddy.webtrc.utils.BroadE.checkPeopleInGroup
import com.google.zxing.Result
import com.gyf.immersionbar.ImmersionBar
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.king.camera.scan.AnalyzeResult
import com.king.camera.scan.CameraScan
import com.king.camera.scan.analyze.Analyzer
import com.king.zxing.BarcodeCameraScanActivity
import com.king.zxing.DecodeConfig
import com.king.zxing.DecodeFormatManager
import com.king.zxing.analyze.MultiFormatAnalyzer
import com.king.zxing.util.CodeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Response
import org.greenrobot.eventbus.EventBus
import java.io.File

class ScanActivity : BarcodeCameraScanActivity(), View.OnClickListener {
    //
    private var isopen = false
    private lateinit var viHead: View
    private lateinit var rlFinish: RelativeLayout
    private lateinit var tvXiangce: TextView
    private lateinit var tvZhaoming: TextView
    private var jumpSource = 0
    private lateinit var loginViewBroadModel: LoginViewBroadModel
    override fun initUI() {
        super.initUI()
        jumpSource = intent.getIntExtra("jumpSource", 0)
        loginViewBroadModel = ViewModelProvider(this)[LoginViewBroadModel::class.java]
        AppBroadUtils.activityList.add(this)
        initViews()
        var result = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        val linearParams = viHead.layoutParams as LinearLayout.LayoutParams
        linearParams.height = result
        viHead.layoutParams = linearParams

        tvZhaoming.setOnClickListener(this)
        rlFinish.setOnClickListener(this)
        tvXiangce.setOnClickListener {
            val perms = arrayOf(Permission.READ_MEDIA_IMAGES)
            if (XXPermissions.isGranted(this, *perms)) {
                pmsSuccess()
            } else {
                NormalBroadDialog(
                    this,
                    getString(R.string.温馨提示),
                    getString(R.string.scan_storage_permission_request),
                    getString(R.string.取消),
                    getString(R.string.确定),
                    true
                ).setOnButtonClickListener(object : NormalBroadDialog.OnButtonClick {
                    override fun OnRightClick() {
                        XXPermissions.with(this@ScanActivity).permission(*perms)
                            .request(object : OnPermissionCallback {
                                override fun onGranted(permissions: MutableList<String>, allGranted: Boolean) {
                                    pmsSuccess()
                                }

                                override fun onDenied(permissions: MutableList<String>, doNotAskAgain: Boolean) {
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
    //    override fun onCreate(savedInstanceState: Bundle?) {

    //    }
//
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        ImmersionBar.with(this)
            .statusBarColor(R.color.transparent)
            .statusBarDarkFont(true)
            .init()
    }

    //
    override fun getLayoutId(): Int {
        return R.layout.activity_broad_scan
    }

    //
//    /** 使用页面自带的「轻触开灯」，不使用库自带手电筒控件 */
    override fun getFlashlightId(): Int {
        return View.NO_ID
    }

    //
    override fun initCameraScan(cameraScan: CameraScan<Result>) {
        super.initCameraScan(cameraScan)
        cameraScan.setPlayBeep(true)
    }

    override fun createAnalyzer(): Analyzer<Result>? {
        val decodeConfig = DecodeConfig()
        decodeConfig.setHints(DecodeFormatManager.QR_CODE_HINTS)
            .setFullAreaScan(true)
        return MultiFormatAnalyzer(decodeConfig)
    }

    override fun startCamera() {
        val perms = arrayOf(Permission.CAMERA)
        if (XXPermissions.isGranted(this, *perms)) {
            cameraScan?.startCamera()
        } else {
            NormalBroadDialog(
                this,
                getString(R.string.温馨提示),
                getString(R.string.scan_camera_permission_request),
                getString(R.string.取消),
                getString(R.string.确定),
                true
            ).setOnButtonClickListener(object : NormalBroadDialog.OnButtonClick {
                override fun OnRightClick() {
                    XXPermissions.with(this@ScanActivity).permission(*perms)
                        .request(object : OnPermissionCallback {
                            override fun onGranted(permissions: MutableList<String>, allGranted: Boolean) {
                                cameraScan?.startCamera()
                            }

                            override fun onDenied(permissions: MutableList<String>, doNotAskAgain: Boolean) {
                                super.onDenied(permissions, doNotAskAgain)
                                ToastBroadUtil.show(getString(R.string.权限被拒绝))
                            }
                        })
                }

                override fun OnLeftClick() {
                    finish()
                }
            }).show()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CHOOSE_REQUEST && resultCode == RESULT_OK) {
            val photoLists = ArrayList<String>()
            val selectList = PictureSelector.obtainSelectorList(data)
            for (i in selectList.indices) {
                when {
                    !StringBroadUtil.isEmpty(selectList[i].realPath) ->
                        photoLists.add(selectList[i].realPath)

                    !StringBroadUtil.isEmpty(selectList[i].sandboxPath) ->
                        photoLists.add(selectList[i].sandboxPath)

                    !StringBroadUtil.isEmpty(selectList[i].path) ->
                        photoLists.add(selectList[i].path)
                }
            }
            if (photoLists.isNotEmpty()) {
                val file = File(photoLists[0])
                if (file.exists()) {
                    parsePhoto(photoLists[0])
                }
            }
        }
    }

    fun pmsSuccess() {
        PictureSelector.create(this)
            .openGallery(SelectMimeType.ofImage())
            .setImageEngine(GlideBroadBroadEngine.createGlideEngine())
            .setMaxSelectNum(1)
            .setMinSelectNum(1)
            .setImageSpanCount(3)
            .isDisplayCamera(true)
            .forResult(CHOOSE_REQUEST)
    }

    override fun onClick(view: View) {
        if (view.id == R.id.rlFinish) {
            finish()
        }
        if (view.id == R.id.tvZhaoming) {
            isopen = !isopen
            cameraScan?.enableTorch(isopen)
        }
    }

    override fun onScanResultCallback(result: AnalyzeResult<Result?>) {
        cameraScan?.setAnalyzeImage(false)
        val text = result.result.text
        eLogD(APP_TAG, "onScannerCompletion========$text")
        if (jumpSource == ScanJumpBroadEnum.ETH_TRANSFER.sourceType && isEthWalletLinkQrCode(text)) {
            val intent = Intent()
            intent.putExtra("scan_result", text)
            setResult(RESULT_OK, intent)
            finish()
            return
        }
        if (jumpSource != ScanJumpBroadEnum.None.sourceType
            && jumpSource != ScanJumpBroadEnum.Login.sourceType
            && isTransferLinkQrCode(text)
        ) {
            val intent = Intent(this, TransferBroadActivity::class.java)
            intent.putExtra("data", text)
            startActivity(intent)
            finish()
            return
        }
        if (!TextUtils.isEmpty(text)) {
            handleResult(text!!)
        } else {
            restartScan()
        }
    }

    //
//    /** 启动协程解析二维码图片 */
    private fun parsePhoto(path: String) {
        lifecycleScope.launch {
            val content = withContext(Dispatchers.IO) {
                CodeUtils.parseCode(path)
            }
            handleQrCode(content)
        }
    }

    //
    private fun initViews() {
        viHead = findViewById(R.id.viHead)
        rlFinish = findViewById(R.id.rlFinish)
        tvXiangce = findViewById(R.id.tvXiangce)
        tvZhaoming = findViewById(R.id.tvZhaoming)
    }
//
    /** 处理图片二维码解析的数据 */
    fun handleQrCode(s: String?) {
        eLogD(APP_TAG, "====handleQrCode====$s")
        if (s == null) {
            ToastBroadUtil.show(getString(R.string.scan_image_qr_code_unrecognizable))
            restartScan()
        } else {
            if (jumpSource == ScanJumpBroadEnum.ETH_TRANSFER.sourceType && isEthWalletLinkQrCode(s)) {
                val intent = Intent()
                intent.putExtra("scan_result", s)
                setResult(RESULT_OK, intent)
                finish()
            } else if (jumpSource != ScanJumpBroadEnum.None.sourceType
                && jumpSource != ScanJumpBroadEnum.Login.sourceType
                && isTransferLinkQrCode(s)
            ) {
                val intent = Intent(this, TransferBroadActivity::class.java)
                intent.putExtra("data", s)
                startActivity(intent)
                finish()
            } else {
                handleResult(s)
            }
        }
    }

    //
    private fun handleResult(con: String) {
        try {
            // 自研群二维码：https://.../api/im/group/getGroupByCodeId/{codeId}?type=1
            if (ImGroupQrCodeHelper.isGetGroupByCodeIdUrl(con)) {
                if (jumpSource == ScanJumpBroadEnum.Login.sourceType) {
                    ToastBroadUtil.show(getString(R.string.scan_invalid_qr_code_type))
                    finish()
                    return
                }
                handleSelfImGroupQr(ImGroupQrCodeHelper.parseCodeId(con))
                return
            }
            val result = con.split("-")
            if (result.size < 2) {
                ToastBroadUtil.show(getString(R.string.scan_invalid_qr_code))
                restartScan()
                return
            }
            val linkId = result[1] //用户id or 群id
            when (result[0]) {
                "1" -> { //用户
                    if (jumpSource == ScanJumpBroadEnum.Login.sourceType) {
                        ToastBroadUtil.show(getString(R.string.scan_invalid_qr_code_type))
                        finish()
                        return
                    }
                    val param = HashMap<String, Any>()
                    param["id"] = linkId
                    OkHttpBroadHelper.getInstance().post_json(
                        this,
                        HttpsBroadConfig.appBaseUrl + "/duiInfo",
                        param,
                        object : SpotsCallBroadBack<ResultBroadBean>(this) {
                            override fun onSuccess(response: Response?, commonResultBean: ResultBroadBean?) {
                                JumpBroadUtils.startHaoYouAddActivity(this@ScanActivity, linkId, "3")
                                finish()
                            }

                            override fun onError(response: Response?, code: Int, e: Exception?) {
                                ToastBroadUtil.show(getString(R.string.scan_network_request_failed))
                                restartScan()
                            }
                        }
                    )
                }

                "2" -> { //群（兼容旧 2-{groupId}；自研主路径为 getGroupByCodeId URL）
                    if (jumpSource == ScanJumpBroadEnum.Login.sourceType) {
                        ToastBroadUtil.show(getString(R.string.scan_invalid_qr_code_type))
                        finish()
                        return
                    }
                    if (HttpsBroadConfig.useSelfImOnly) {
                        applySelfImJoinGroup(linkId, null)
                        return
                    }
//                    val param1 = HashMap<String, Any>()
//                    param1["id"] = linkId
//                    OkHttpBroadHelper.getInstance().post_json(
//                        this,
//                        HttpsBroadConfig.appBaseUrl + "/groupScan",
//                        param1,
//                        object : SpotsCallBroadBack<ResultBroadBean>(this) {
//                            override fun onSuccess(response: Response?, commonResultBean: ResultBroadBean?) {
//                                if (commonResultBean != null && !TextUtils.isEmpty(commonResultBean.groupId)) {
//                                    AppBroadUtils.goChat(this@ScanActivity, commonResultBean.groupId, "1", null)
//                                    finish()
//                                } else {
//                                    ToastBroadUtil.show(commonResultBean?.msg ?: getString(R.string.scan_applied_waiting_group_owner))
//                                    finish()
//                                }
//                            }
//
//                            override fun onError(response: Response?, code: Int, e: Exception?) {
//                                ToastBroadUtil.show(getString(R.string.scan_network_request_failed))
//                                restartScan()
//                            }
//                        }
//                    )
                }

                "3" -> { //扫描登录
                    idLogin(linkId)
                }

                else -> {
                    ToastBroadUtil.show(getString(R.string.scan_invalid_qr_code_type))
                    restartScan()
                }
            }
        } catch (e: Exception) {
            ToastBroadUtil.show(getString(R.string.scan_invalid_qr_code))
            restartScan()
        }
    }

    /** 自研：扫码 codeId → getGroupByCodeId → applyInGroup */
    private fun handleSelfImGroupQr(codeId: String?) {
        if (TextUtils.isEmpty(codeId)) {
            ToastBroadUtil.show(getString(R.string.scan_invalid_qr_code))
            restartScan()
            return
        }
        ImGroupHttpExample.requestGetGroupByCodeId(codeId ?: "", { result ->
            if (TextUtils.isEmpty(result.id)) {
                showToastMain(R.string.scan_invalid_qr_code)
                restartScan()
            } else {
                checkPeopleInGroup(result.id, MMKVBroadUtils.getRealUserId()) { pair ->
                    if (pair.first) {
                        JumpBroadUtils.startChatMsgActivity(groupToSession(result), this@ScanActivity)
                        finish()
                    } else if (!TextUtils.isEmpty(pair.second)) {
                        restartScan()
                        showToastMain(pair.second ?: "")
                    } else {
                        checkGroupJoinEligibility(result.id, result)
                    }
                }
            }
        }, {
            runOnUiThread {
                ToastBroadUtil.show(getString(R.string.scan_network_request_failed))
                restartScan()
            }
        })
    }

    /** 自研：校验黑名单与入群收费，通过后再申请入群。 */
    private fun checkGroupJoinEligibility(groupId: String, groupInfo: GroupQrInfoRes) {
        lifecycleScope.launch {
            val params = hashMapOf<String, Any?>("id" to groupId)
            // checkGroupJoin 为扁平响应；需支付时 success=false，需用 Raw 才能拿到业务字段
            val response = withContext(Dispatchers.IO) {
                NetworkBroadRepository.postEncryptedFlatRaw2(
                    UrlBroadBroadConstants.CHECK_GROUP_JOIN,
                    CheckGroupJoiBean::class.java,
                    params,
                )
            }
            withContext(Dispatchers.Main) {
                response.onSuccess { data ->
                    if (data == null) {
                        ToastBroadUtil.show(getString(R.string.scan_network_request_failed))
                        restartScan()
                        return@onSuccess
                    }
                    when {
                        data.isBlacklisted() -> {
                            showToastMain(R.string.scan_group_blacklisted)
                            restartScan()
                        }

                        data.isNeedPay() -> {
                            mergeCheckGroupJoinIntoQrInfo(groupInfo, data)
                            GroupJoinPayActivity.start(this@ScanActivity, groupInfo)
                            finish()
                        }

                        data.isAllowJoin() || data.success -> {
                            applySelfImJoinGroup(groupId, groupInfo)
                        }

                        else -> {
                            showToastMain(
                                data.msg?.takeIf { it.isNotBlank() }
                                    ?: getString(R.string.scan_network_request_failed)
                            )
                            restartScan()
                        }
                    }
                }.onFailure { _, message ->
                    showToastMain(
                        message.takeIf { it.isNotBlank() }
                            ?: getString(R.string.scan_network_request_failed)
                    )
                    restartScan()
                }.onError {
                    showToastMain(R.string.scan_network_request_failed)
                    restartScan()
                }
            }
        }
    }

    /** 用 checkGroupJoin 返回的价格/群信息补全扫码结果，供收费进群页展示。 */
    private fun mergeCheckGroupJoinIntoQrInfo(groupInfo: GroupQrInfoRes, check: CheckGroupJoiBean) {
        if (!check.groupName.isNullOrBlank()) {
            groupInfo.name = check.groupName
        }
        if (!check.groupIcon.isNullOrBlank()) {
            groupInfo.icon = check.groupIcon
        }
        if (groupInfo.memCount <= 0 && check.memberCount > 0) {
            groupInfo.memCount = check.memberCount
        }
        if (!check.priceDualx.isNullOrBlank()) {
            groupInfo.joinPayPrice = check.priceDualx
        }
        val payFlag = when {
            check.openJoinPay == "1" || check.needPay == "1" -> "1"
            else -> check.openJoinPay ?: groupInfo.openJoinPay
        }
        groupInfo.openJoinPay = payFlag
        groupInfo.ensureBusiExt().apply {
            openJoinPay = payFlag
            if (!check.priceUsdt.isNullOrBlank()) {
                joinPayPriceUsdt = check.priceUsdt
            }
            if (!check.feeRate.isNullOrBlank()) {
                joinPayFeeRate = check.feeRate
            }
        }
    }

    /** 自研申请入群。对齐野火：无需审核则进群聊；需审核则提示等待群主。 */
    private fun applySelfImJoinGroup(groupId: String, groupInfo: GroupVo?) {
        ImGroupHttpExample.requestApplyInGroup(groupId, object : OnHttpImListener<Boolean> {
            override fun onHttpImSuccess(result: Boolean) {
                runOnUiThread {
                    if (!result) {
                        showToastMain(getString(R.string.scan_network_request_failed))
                        restartScan()
                        return@runOnUiThread
                    }
                    if (isJoinNeedVerify(groupInfo)) {
                        showToastMain(getString(R.string.scan_applied_waiting_group_owner))
                        finish()
                    } else {
                        openSelfImGroupChat(groupInfo, groupId)
                    }
                }
            }
        }, {
            runOnUiThread {
                showToastMain(getString(R.string.scan_network_request_failed))
                restartScan()
            }
        })
    }

    private fun openSelfImGroupChat(groupInfo: GroupVo?, groupId: String) {
        var vo = groupInfo
        if (vo == null) {
            vo = GroupVo()
            vo.id = groupId
        }
        val session = groupToSession(vo)
        if (session != null) {
            JumpBroadUtils.startChatMsgActivity(session, this)
        }
        finish()
    }

    private fun idLogin(linkId: String) {
        if (jumpSource != ScanJumpBroadEnum.Login.sourceType) {
            ToastBroadUtil.show(getString(R.string.mine_change_machine_tip))
            restartScan()
            return
        }
        if (HttpsBroadConfig.currentHostHttpUrlType > 1000) {
            val param = HashMap<String, Any>()
            param["id"] = linkId
            param["clientId"] = DeviceBroadUtil.getAndroidId(this)
            param["type"] = "2"
            param["model"] = DeviceBroadUtil.getDeviceBrand() + " " + DeviceBroadUtil.getDeviceModel()
            OkHttpBroadHelper.getInstance().post_json(
                this,
                HttpsBroadConfig.appBaseUrl + "/idLogin",
                param,
                object : SpotsCallBroadBack<ResultBroadBean>(this) {
                    override fun onSuccess(response: Response?, resultBean: ResultBroadBean?) {
                        if (resultBean == null) {
                            showToastMain(getString(R.string.scan_login_request_failed))
                            restartScan()
                            return
                        }
                        if (jumpSource == ScanJumpBroadEnum.Login.sourceType
                            && !TextUtils.equals(resultBean.eqno, DeviceBroadUtil.getAndroidId(this@ScanActivity))
                        ) {
//                            EventBus.getDefault().post(AppBroadEvent(AppEventBroadCode.EVENT_NEW_MACHINE_CHECK, resultBean))
                        } else {
                            AppBroadUtils.imLogin(
                                this@ScanActivity,
                                resultBean.apptoken,
                                resultBean.userId,
                                resultBean.imtoken,
                                "ScanActivity"
                            )
                        }
                        finish()
                    }

                    override fun onError(response: Response?, code: Int, e: Exception?) {
                        showToastMain(getString(R.string.scan_login_request_failed))
                        restartScan()
                    }
                }
            )
        } else {
            loginViewBroadModel.getScanLoginDetail(linkId).observe(this) { loginBean ->
                if (loginBean.loginBean != null) {
                    val newIntent = Intent()
                    newIntent.putExtra("loginBean", toJson2(loginBean.loginBean))
                    setResult(RESULT_OK, newIntent)
                    finish()
                } else {
                    restartScan()
                    showToastMain(if (loginBean.msg != null) "${loginBean.msg}" else getAppStr(R.string.scan_login_request_failed, null))
                }
            }
        }
    }

    /** 重新启动扫描器 */
    private fun restartScan() {
        cameraScan?.setAnalyzeImage(true)
    }

    override fun startActivity(intent: Intent?) {
        super.startActivity(intent)
        overridePendingTransition(R.anim.anim_right_broad_in, R.anim.anim_left_broad_out)
    }

    override fun onDestroy() {
        AppBroadUtils.activityList.remove(this)
        super.onDestroy()
    }

    companion object {
        private fun isJoinNeedVerify(groupInfo: GroupVo?): Boolean {
            if (groupInfo == null) {
                return false
            }
            if (1 == groupInfo.joinNeedVerify) {
                return true
            }

            return "1" == groupInfo.busiExt?.openQyyz
        }
    }
}
