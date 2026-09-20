package com.cboda.intelligent.viewmodel

import android.content.Context
import android.text.TextUtils
import androidx.lifecycle.viewModelScope
import com.cboda.baselibrary.bean.CaptchaBean
import com.cboda.baselibrary.bean.ChangePasswordBean
import com.cboda.baselibrary.bean.FriendBean
import com.cboda.baselibrary.bean.GlobalFriendBroadBean
import com.cboda.baselibrary.bean.LoginBean
import com.cboda.baselibrary.bean.LoginIdBean
import com.cboda.baselibrary.bean.LoginReturnBean
import com.cboda.baselibrary.bean.UserInfoBean
import com.cboda.chatui.ChatManagerBroadHolder
import com.cboda.baselibrary.network.repository.NetworkBroadRepository
import com.cboda.baselibrary.network.result.onError
import com.cboda.baselibrary.network.result.onFailure
import com.cboda.baselibrary.network.result.onSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.cboda.baselibrary.utils.DeviceBroadUtil.getAndroidId
import com.cboda.baselibrary.utils.DeviceBroadUtil.getDeviceBrand
import com.cboda.baselibrary.utils.DeviceBroadUtil.getDeviceModel
import com.cboda.baselibrary.utils.eLogD
import com.cboda.baselibrary.utils.fromJson2
import com.cboda.baselibrary.utils.getAppContext
import com.cboda.intelligent.base.BaseViewBroadModel
import com.cboda.intelligent.dialog.QrdlBroadDialog
import com.cboda.baselibrary.livedata.SingleLiveBroadData
import com.cboda.baselibrary.utils.DeviceBroadUtil
import com.cboda.baselibrary.utils.HttpsBroadConfig
import com.cboda.baselibrary.utils.MMKVBroadUtils
import com.cboda.baselibrary.utils.MMKVBroadUtils.Companion.APP_TOKEN
import com.cboda.baselibrary.utils.MMKVBroadUtils.Companion.OLD_QR_CODE
import com.cboda.baselibrary.utils.MMKVBroadUtils.Companion.USER_LOGIN_DATA
import com.cboda.baselibrary.utils.RealmUtil
import com.cboda.baselibrary.utils.anyToDouble
import com.cboda.baselibrary.utils.anyToInt
import com.cboda.baselibrary.utils.getAppStr
import com.cboda.baselibrary.utils.toJson2
import com.cboda.baselibrary.utils.toLog
import com.cboda.intelligent.R
import com.cboda.intelligent.bean.ResultBroadBean
import com.cboda.intelligent.configuration.SafeJumpBroadEnum
import com.cboda.intelligent.dialog.pop.AuthCodeBroadPop
import com.cboda.intelligent.dialog.pop.AuthPasswordBroadPop
import com.cboda.intelligent.dialog.pop.AuthQuestionBroadPop
import com.cboda.intelligent.dialog.pop.AuthWayBroadPop
import com.cboda.intelligent.utils.SpotsCallBroadBack
import com.cboda.intelligent.net.BroadUrl
import com.cboda.intelligent.utils.UrlBroadBroadConstants
import com.cboda.intelligent.utils.dismissLoading
import com.cboda.intelligent.utils.showLoading
import com.cboda.intelligent.utils.showToastMain
import com.cboda.intelligent.model.SafeCenterBroadBean
import com.cboda.intelligent.net.manager.UserServerBroadManager
import com.cboda.intelligent.utils.AppBroadUtils
import com.cboda.intelligent.utils.OkHttpBroadHelper
import com.cboda.intelligent.utils.showGuidePasswordTip
import okhttp3.Response
import java.lang.Exception
import java.security.MessageDigest
import java.util.Locale
import kotlin.jvm.java

open class LoginViewBroadModel : BaseViewBroadModel() {


    fun checkSafe(userInfoBean: UserInfoBean?, context: Context?, block: () -> Unit = {}) {
        userInfoBean?.let { userInfo ->
            val mContext = getAppContext(context)
            if (TextUtils.isEmpty(userInfo.payPwd)) {
                showGuidePasswordTip(mContext)
            } else {
                val haveMb = !TextUtils.isEmpty(userInfo.securityQuestionAnswer)
                val haveGoogleCode = !TextUtils.isEmpty(userInfo.googleCode)
                if (haveMb && haveGoogleCode) {
                    val authWayPop = AuthWayBroadPop(mContext, SafeJumpBroadEnum.ChangeLoginPassword.sourceType)
                    authWayPop.transferInvoke = {
                        checkPayPwd(mContext, block)
                    }
                    authWayPop.showPopupWindow()
                } else if (haveMb) {
                    val tipPop = AuthQuestionBroadPop(
                        mContext,
                        SafeJumpBroadEnum.ChangeLoginPassword.sourceType
                    )
                    tipPop.backInvoke = {
                        checkPayPwd(mContext, block)
                    }
                    tipPop.showPopupWindow()
                } else if (haveGoogleCode) {
                    val tipPop = AuthCodeBroadPop(
                        mContext,
                        SafeJumpBroadEnum.ChangeLoginPassword.sourceType
                    )
                    tipPop.transferInvoke = {
                        checkPayPwd(mContext, block)
                    }
                    tipPop.showPopupWindow()
                } else {
                    checkPayPwd(mContext, block)
                }
            }
        }
    }

    private fun checkPayPwd(context: Context, block: () -> Unit) {
        val mPop = AuthPasswordBroadPop(
            context,
            SafeJumpBroadEnum.ChangeLoginPassword.sourceType
        )
        mPop.backInvoke = {
            block.invoke()
        }
        mPop.showPopupWindow()
    }

    /**
     * POST /im/user/changePassword —— 修改登录密码。
     * oldPassword / newPassword 均为 `v1$` + hex(SHA-256(明文 + `\n` + account.lower))。
     */
    fun changeLoginPassword(
        account: String,
        oldPlainPwd: String,
        newPlainPwd: String,
    ): SingleLiveBroadData<ChangePasswordBean?> {
        val liveData = SingleLiveBroadData<ChangePasswordBean?>()
        viewModelScope.launch {
            try {
                com.cboda.intelligent.utils.showLoading()
                val saltAccount = account.trim().ifEmpty {
                    MMKVBroadUtils.getUserName()?.trim().orEmpty()
                }
                if (saltAccount.isEmpty()) {
                    showToastMain(R.string.please_enter_account)
                    liveData.postValue(null)
                    return@launch
                }
                withContext(Dispatchers.IO) {
                    val body = mapOf(
                        "oldPassword" to hashRegisterPassword(oldPlainPwd, saltAccount),
                        "newPassword" to hashRegisterPassword(newPlainPwd, saltAccount),
                    )
                    NetworkBroadRepository.post(
                        "im/user/changePassword",
                        ChangePasswordBean::class.java,
                        body,
                    ).onSuccess { data ->
                        liveData.postValue(data)
                    }.onFailure { _, message ->
                        showToastMain(message)
                        liveData.postValue(null)
                    }.onError {
                        showToastMain(R.string.httperror)
                        liveData.postValue(null)
                    }
                }
            } catch (_: Exception) {
                showToastMain(R.string.httperror)
                liveData.postValue(null)
            } finally {
                com.cboda.intelligent.utils.dismissLoading()
            }
        }
        return liveData
    }

    @Deprecated("使用 changeLoginPassword")
    fun updatePwd(pwd: String): SingleLiveBroadData<Boolean> {
        val liveData = SingleLiveBroadData<Boolean>()
        liveData.postValue(false)
        return liveData
    }

    fun resetQrCode(): SingleLiveBroadData<String?> {
        val liveData = SingleLiveBroadData<String?>()
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val result = NetworkBroadRepository.postEncrypted("api/account/freshQRCode2", UserInfoBean::class.java, HashMap<String, Any>())
                    result.onSuccess { data ->
                        liveData.postValue(data?.qrcode)
                    }.onFailure { _, msg ->
                        liveData.postValue(null)
                        showToastMain(msg)
                    }.onError { _ ->
                        liveData.postValue(null)
                        showToastMain(R.string.httperror)
                    }
                } catch (_: kotlin.Exception) {
                    liveData.postValue(null)
                }
            }
        }
        return liveData
    }

    fun getUserInfo(): SingleLiveBroadData<UserInfoBean> {
        val liveData = SingleLiveBroadData<UserInfoBean>()
        val params = HashMap<String, Any>()
        params["userId"] = MMKVBroadUtils.getRealUserId() ?: ""
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val result = NetworkBroadRepository.post("im/user/getUser", UserInfoBean::class.java, params)
                    result.onSuccess { data ->
                        data?.normalizeImFields()
//                        MMKVBroadUtils.saveSqlStrData(USER_LOGIN_DATA, toJson2(data))
                        RealmUtil.addUserData(data)
                        data?.let {
                            liveData.postValue(data)
                        }
                    }.onFailure { _, msg ->
                        showToastMain(msg)
                    }.onError { _ ->
                        showToastMain(R.string.httperror)
                    }
                } catch (_: kotlin.Exception) {
                }
            }
        }
        return liveData
    }


    fun getAccountLoginDetail(account: String, pwd: String): SingleLiveBroadData<LoginBean?> {
        val liveData = SingleLiveBroadData<LoginBean?>()
        if (TextUtils.isEmpty(account)) {
            showToastMain(R.string.please_enter_account)
            return liveData
        }
        if (TextUtils.isEmpty(pwd)) {
            showToastMain(R.string.please_enter_password)
            return liveData
        }
        val params = hashMapOf(
            "acc" to account,
            "pwd" to pwd,
            "eqno" to getAndroidId()
        )
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val result = NetworkBroadRepository.postEncrypted("api/account/getAccountYz", LoginBean::class.java, params)
                    result.onSuccess { data ->
                        data?.also {
                            if (!data.twoFactorCode && !data.existMbName && !data.existPayPassword && data.secondVerifyNeed == 0) {
                                pwdLogin(account, pwd)
                            } else {
                                liveData.postValue(data)
                            }
                        } ?: liveData.postValue(null)
//                        eLogD("LoginViewBroadModelClass", "getScanLoginDetail:${toJson2(data)}")
                    }.onFailure { _, msg ->
                        showToastMain(msg)
                        liveData.postValue(null)
                    }.onError { error ->
                        showToastMain(R.string.httperror)
                        liveData.postValue(null)
                    }
                } catch (_: kotlin.Exception) {
                    liveData.postValue(null)
                }
            }
        }
        return liveData
    }


    private fun idLogin(linkId: String, errMsg: String) {
        val params = hashMapOf("id" to linkId, "clientId" to getAndroidId(), "type" to "2", "model" to "${getDeviceBrand()} ${getDeviceModel()}")
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val result = NetworkBroadRepository.postEncryptedFlat2("api/idLogin", params)
                    result.onSuccess { map ->
                        if (!TextUtils.isEmpty(map?.get(APP_TOKEN)?.toString() ?: "") && !TextUtils.isEmpty(map?.get("userId")?.toString() ?: "") && !TextUtils.isEmpty(map?.get("imtoken")?.toString() ?: "")) {
                            AppBroadUtils.imLogin(getAppContext(null), map?.get(APP_TOKEN)?.toString(), map?.get("userId")?.toString(), map?.get("imtoken")?.toString(), "ScanBroadActivity")
                        } else {
                            showToastMain(errMsg)
                        }
                    }.onFailure { _, msg ->
                        showToastMain(msg)
                    }.onError { e ->
                        showToastMain("${e.message}")
                    }
                } catch (_: kotlin.Exception) {

                }
            }
        }


    }

    fun getScanLoginDetail(linkId: String): SingleLiveBroadData<LoginReturnBean> {
        val params = hashMapOf("idno" to linkId, "eqno" to getAndroidId())
        val liveData = SingleLiveBroadData<LoginReturnBean>()
        val loginReturnBean = LoginReturnBean()
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val result = NetworkBroadRepository.postEncrypted("api/account/getScanQrCodeYz", LoginBean::class.java, params)
                    result.onSuccess { data ->
//                        eLogD("LoginViewBroadModelClass", "getScanLoginDetail:${toJson2(data)}")
                        if (data?.oldDevice == true) {
                            oneClickLogin()
                        } else if (data?.secondVerifyNeed == 0 && !data.existMbName && !data.existPayPassword && !data.twoFactorCode) {
                            idLogin(linkId, data.secondVerifyUnsetHint)
                        } else {
                            loginReturnBean.loginBean = data
                            loginReturnBean.loginBean?.linkId = linkId
                            liveData.postValue(loginReturnBean)
                        }
                    }.onFailure { c, e ->
                        loginReturnBean.errorCode = c
                        loginReturnBean.msg = e
                        liveData.postValue(loginReturnBean)
                    }.onError { _ ->
                        loginReturnBean.msg = getAppStr(R.string.httperror)
                        liveData.postValue(loginReturnBean)
                    }
                } catch (e: kotlin.Exception) {
                    loginReturnBean.msg = e.message
                    liveData.postValue(loginReturnBean)
                }
            }
        }
        return liveData
    }

    fun updatePassword(account: String, pwd: String, secondPwd: String): SingleLiveBroadData<Boolean> {
        val liveData = SingleLiveBroadData<Boolean>()
        if (TextUtils.isEmpty(pwd)) {
            showToastMain(R.string.please_enter_password)
            return liveData
        }
        if (TextUtils.isEmpty(secondPwd)) {
            showToastMain(com.cboda.baselibrary.R.string.please_again_input_pwd)
            return liveData
        }
        if (pwd != secondPwd) {
            showToastMain(com.cboda.baselibrary.R.string.again_pwd_inconsistent)
            return liveData
        }
        val param =
            mapOf(
                "account" to account,
                "password" to pwd,
                "confirmPassword" to secondPwd
            )
        showLoading()
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val result = NetworkBroadRepository.postEncrypted(
                        "api/account/update",
                        Any::class.java, param
                    )
                    result.onSuccess { data ->
//                        eLogD("updatePasswordClass", "onSuccess")
                        liveData.postValue(true)
                        dismissLoading()
                    }.onFailure { code, message ->
//                        eLogD("updatePasswordClass", "onFailure:$code ,$message")
                        dismissLoading()
                        showToastMain(message)
                    }.onError { error ->
//                        eLogD("updatePasswordClass", "onError:${error.message}")
                        dismissLoading()
                        showToastMain("${error.message}")
                    }
                } catch (e: kotlin.Exception) {
//                    eLogD("updatePasswordClass", "catch:${e.message}")
                    dismissLoading()
                }
            }
        }
        return liveData
    }

    //获取助记词
    fun getMnemonicWords(): SingleLiveBroadData<String?> {
        val liveData = SingleLiveBroadData<String?>()
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val result = NetworkBroadRepository.postEncrypted(
                        "api/account/mnemonic",
                        Map::class.java, HashMap<String, Any>()
                    )
                    result.onSuccess { data ->
                        val mnemonic = data?.get("mnemonic")
                        mnemonic?.let { liveData.postValue(it.toString()) } ?: liveData.postValue(
                            null
                        )
                    }.onFailure { _, _ ->
                        liveData.postValue(null)
                    }.onError { _ ->
                        liveData.postValue(null)
                    }
                } catch (e: kotlin.Exception) {
                    liveData.postValue(null)
                }
            }
        }
        return liveData
    }

    //检查账号是否存在
    suspend fun checkAccount(account: String): SingleLiveBroadData<SafeCenterBroadBean> {
        showLoading()
        val safeCenterLiveData = SingleLiveBroadData<SafeCenterBroadBean>()
        val params = hashMapOf("account" to account)
        withContext(Dispatchers.IO) {
            try {
                NetworkBroadRepository.postEncrypted2<SafeCenterBroadBean>(
                    UrlBroadBroadConstants.SECURITY_CENTER_FORGET_PASSWORD,
                    SafeCenterBroadBean::class.java,
                    params
                ).onSuccess { userInfo ->
                    dismissLoading()
                    userInfo?.let {
                        safeCenterLiveData.postValue(it)
                    }

                }.onFailure { code, message ->
                    dismissLoading()
                    showToastMain(message)
                }.onError {
                    dismissLoading()
                }

            } catch (_: kotlin.Exception) {
                dismissLoading()
            }
        }
        return safeCenterLiveData
    }

    fun oneClickLogin() {
        val mContext = getAppContext()
        val model = "${getDeviceBrand()} ${getDeviceModel()}"
        val param =
            mapOf(
                "eqno" to getAndroidId(),
                "clientId" to getAndroidId(),
                "type" to 2,
                "model" to model
            )
//        eLogD("LoginViewModelClass_oneClickLoginMap", param)
        UserServerBroadManager.qeLogin(
            mContext,
            param,
            callBack = object : SpotsCallBroadBack<ResultBroadBean>(mContext) {
                override fun onSuccess(
                    response: Response?,
                    resultBean: ResultBroadBean?
                ) {
                    resultBean?.let {
                        imLogin(it, mContext)
                    } ?: showToastMain(com.cboda.baselibrary.R.string.service_error_token_null)
                }

                override fun onError(
                    response: Response?,
                    code: Int,
                    e: Exception?
                ) {
//                     showToastMain(com.cboda.baselibrary.R.string.service_error_token_null)
                }
            })

    }

    private fun imLogin(resultBean: ResultBroadBean, context: Context = getAppContext()) {
        if (!TextUtils.isEmpty(resultBean.qrcode)) {
            MMKVBroadUtils.saveSqlStrData(OLD_QR_CODE, resultBean.qrcode)
        }
        AppBroadUtils.imLogin(
            context,
            resultBean.apptoken,
            resultBean.userId,
            resultBean.imtoken,
            "NewEquipmentBroadActivity"
        )
    }

    fun pwdLogin2(account: String, pwd: String, code: String = "", payPwd: String? = "", googleCode: String? = "", securityQuestion: String? = "", block: (ResultBroadBean) -> Unit = {}) {
        if (TextUtils.isEmpty(account)) {
            showToastMain(R.string.please_enter_account)
            return
        }
        if (TextUtils.isEmpty(pwd)) {
            showToastMain(R.string.please_enter_password)
            return
        }
        val mContext = getAppContext()
        val model = "${getDeviceBrand()} ${getDeviceModel()}"
        val param =
            hashMapOf(
                "acc" to account,
                "pwd" to pwd,
                "code" to code,
                "eqno" to getAndroidId(),
                "type" to 2,
                "model" to model
            )
        param["clientId"] = getAndroidId()
        if (!TextUtils.isEmpty(payPwd)) {
            param["paywd"] = payPwd ?: ""
        }
        if (!TextUtils.isEmpty(googleCode)) {
            param["googleCode"] = googleCode ?: ""
        }
        if (!TextUtils.isEmpty(securityQuestion)) {
            param["mbda"] = securityQuestion ?: ""
        }
//        eLogD("LoginViewModelClass_pwdLoginMap", param)
        UserServerBroadManager.accLogin(
            mContext,
            param,
            callBack = object : SpotsCallBroadBack<String>(mContext) {
                override fun onSuccess(response: Response?, result: String?) {
                    if (!TextUtils.isEmpty(result)) {
                        val resultBean = fromJson2(result, ResultBroadBean::class.java)
                        block.invoke(resultBean)
//                        if (resultBean.code == "200" && resultBean.success) {
//                            imLogin(resultBean, mContext)
//                        } else if (!TextUtils.isEmpty(resultBean.msg)) {
//                            showToastMain(resultBean.msg)
//                        }
                    } else {
                        showToastMain(com.cboda.baselibrary.R.string.service_error_data_null)
                    }
                }

                override fun onError(
                    response: Response?,
                    code: Int,
                    e: Exception?
                ) {

                }

            })

    }

    /**
     * 拉取图形验证码。
     * @param scene `login` / `register`
     */
    fun fetchCaptcha(scene: String = "login"): SingleLiveBroadData<CaptchaBean> {
        val liveData = SingleLiveBroadData<CaptchaBean>()
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val params = hashMapOf<String, Any?>("scene" to scene)
                    NetworkBroadRepository.post(
                        "im/user/captcha",
                        CaptchaBean::class.java,
                        params,
                    ).onSuccess { data ->
                        liveData.postValue(data)
                    }.onFailure { _, message ->
                        showToastMain(message)
                        liveData.postValue(null)
                    }.onError {
                        liveData.postValue(null)
                    }
                } catch (_: Exception) {
                    liveData.postValue(null)
                }
            }
        }
        return liveData
    }

    fun pwdLogin(
        account: String,
        pwd: String,
        code: String = "",
        payPwd: String = "",
        googleCode: String = "",
        securityQuestion: String = "",
        captchaId: String = "",
        captcha: String = "",
        requireCaptcha: Boolean = true,
        onCaptchaInvalid: (() -> Unit)? = null,
    ) {
        if (TextUtils.isEmpty(account)) {
            showToastMain(R.string.please_enter_account)
            return
        }
        if (TextUtils.isEmpty(pwd)) {
            showToastMain(R.string.please_enter_password)
            return
        }
        if (pwd.trim().length < 6) {
            showToastMain(com.cboda.baselibrary.R.string.pwd_length_six)
            return
        }
        if (requireCaptcha) {
            if (TextUtils.isEmpty(captchaId) || TextUtils.isEmpty(captcha.trim())) {
                showToastMain(R.string.请输入图形验证码)
                return
            }
        }
        val mContext = getAppContext()

        val jmPwd = hashRegisterPassword(pwd, account)
        val param =
            hashMapOf(
                "account" to account,
                "password" to jmPwd
            )
        if (requireCaptcha || !TextUtils.isEmpty(captchaId) || !TextUtils.isEmpty(captcha)) {
            param["captchaId"] = captchaId
            param["captchaCode"] = captcha.trim()
        }
        UserServerBroadManager.accLogin(
            mContext,
            param,
            callBack = object : SpotsCallBroadBack<String>(mContext) {
                override fun onSuccess(response: Response?, result: String?) {
                    if (!TextUtils.isEmpty(result)) {
                        val resultBean = fromJson2(result, ResultBroadBean::class.java)
                        eLogD("pwdLogin", "result:$result")
                        if (resultBean.code == "200" && resultBean.success) {
                            imLogin(resultBean, mContext)
                        } else {
                            showToastMain(
                                if (!TextUtils.isEmpty(resultBean.errorMsg)) resultBean.errorMsg
                                else getAppStr(com.cboda.baselibrary.R.string.service_error_data_null)
                            )
                            onCaptchaInvalid?.invoke()
                        }
                    } else {
                        showToastMain(com.cboda.baselibrary.R.string.service_error_data_null)
                        onCaptchaInvalid?.invoke()
                    }
                }

                override fun onError(
                    response: Response?,
                    code: Int,
                    e: Exception?
                ) {
                    showToastMain(R.string.httperror)
                    onCaptchaInvalid?.invoke()
                }

            })

    }


    fun registerAccount(
        account: String,
        pwd: String,
        secondPwd: String,
        captchaId: String = "",
        captcha: String = "",
        onCaptchaInvalid: (() -> Unit)? = null,
    ) {
        val mContext = getAppContext()
        if (TextUtils.isEmpty(account)) {
            showToastMain(R.string.please_enter_account)
            return
        }
        if (TextUtils.isEmpty(pwd)) {
            showToastMain(R.string.please_enter_password)
            return
        }
        if (TextUtils.isEmpty(secondPwd)) {
            showToastMain(com.cboda.baselibrary.R.string.please_again_input_pwd)
            return
        }
        if (pwd != secondPwd) {
            showToastMain(com.cboda.baselibrary.R.string.again_pwd_inconsistent)
            return
        }
        if (pwd.trim().length < 6) {
            showToastMain(com.cboda.baselibrary.R.string.pwd_length_six)
            return
        }
        if (TextUtils.isEmpty(captchaId) || TextUtils.isEmpty(captcha.trim())) {
            showToastMain(R.string.请输入图形验证码)
            return
        }
        // 注册传输：v1$ + hex(SHA-256(明文密码 + "\n" + account.trim().lowercase()))
        val jmPwd = hashRegisterPassword(pwd, account)
        val param =
            mapOf(
                "account" to account,
                "password" to jmPwd,
                "captchaId" to captchaId,
                "captchaCode" to captcha.trim(),
            )
        UserServerBroadManager.qeLogin(
            mContext,
            param,
            BroadUrl.regist,
            callBack = object : SpotsCallBroadBack<ResultBroadBean>(mContext) {
                override fun onSuccess(
                    response: Response?,
                    resultBean: ResultBroadBean?
                ) {
                    resultBean?.let {
                        eLogD("LoginViewBroadModelClass", "resultBean:${toJson2(resultBean)}")
                        imLogin(resultBean, mContext)
                    } ?: run {
                        showToastMain(com.cboda.baselibrary.R.string.service_error_token_null)
                        onCaptchaInvalid?.invoke()
                    }
                }

                override fun onError(
                    response: Response?,
                    code: Int,
                    e: Exception?
                ) {
                    onCaptchaInvalid?.invoke()
                }
            })
    }

    /**
     * 注册密码传输哈希：`v1$` + hex(SHA-256(明文密码 + `\n` + account.trim().lowercase()))
     */
    private fun hashRegisterPassword(plainPwd: String, account: String): String {
        val material = plainPwd + "\n" + account.trim().lowercase(Locale.ROOT)
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(material.toByteArray(Charsets.UTF_8))
        val hex = buildString(digest.size * 2) {
            for (b in digest) {
                append("%02x".format(b))
            }
        }
        return "v1$$hex"
    }

}