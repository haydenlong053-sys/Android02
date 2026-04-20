package com.cboda.intelligent.viewmodel

import android.content.Context
import android.content.Intent
import android.text.TextUtils
import androidx.lifecycle.viewModelScope
import com.cboda.baselibrary.bean.GlobalFriendBroadBean
import com.cboda.baselibrary.bean.LoginBean
import com.cboda.baselibrary.bean.UserInfoBean
import com.cboda.chatui.ChatManagerBroadHolder
import com.cboda.baselibrary.network.repository.NetworkBroadRepository
import com.cboda.baselibrary.network.result.onError
import com.cboda.baselibrary.network.result.onFailure
import com.cboda.baselibrary.network.result.onSuccess
import com.cboda.baselibrary.utils.DeviceBroadUtil.getAndroidId
import com.cboda.baselibrary.utils.DeviceBroadUtil.getDeviceBrand
import com.cboda.baselibrary.utils.DeviceBroadUtil.getDeviceModel
import com.cboda.baselibrary.utils.eLogD
import com.cboda.baselibrary.utils.fromJson2
import com.cboda.baselibrary.utils.getAppContext
import com.cboda.intelligent.base.BaseViewBroadModel
import com.cboda.intelligent.dialog.QrdlBroadDialog
import com.cboda.baselibrary.livedata.SingleLiveBroadData
import com.cboda.baselibrary.utils.MMKVBroadUtils
import com.cboda.baselibrary.utils.MMKVBroadUtils.Companion.OLD_QR_CODE
import com.cboda.baselibrary.utils.anyToDouble
import com.cboda.baselibrary.utils.anyToInt
import com.cboda.intelligent.R
import com.cboda.intelligent.activity.PushInfo2BroadActivity
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
import com.cboda.intelligent.utils.showGuidePasswordTip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Response
import java.lang.Exception
import kotlin.jvm.java

class LoginViewBroadModel : BaseViewBroadModel() {


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

    fun updatePwd(pwd: String): SingleLiveBroadData<Boolean> {
        val liveData = SingleLiveBroadData<Boolean>()
        val params = mapOf("pwd" to pwd)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val result = NetworkBroadRepository.postEncrypted("api/wanshan", Any::class.java, params)
                    result.onSuccess { _ ->
                        liveData.postValue(true)
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
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val result = NetworkBroadRepository.postEncrypted("api/userInfo", UserInfoBean::class.java, params)
                    result.onSuccess { data ->
                        data?.let {
                            val bean = GlobalFriendBroadBean()
                            bean.userId = data.id
                            bean.icon = data.userImg
                            bean.nickName = data.name
                            bean.grade = anyToDouble(data.grade)
                            bean.registerDay = anyToInt(data.registerDayBean?.registrationDays ?: "")
                            bean.isVip = data.isVip == "1"
                            bean.isNiceNum = !TextUtils.isEmpty(data.niceNumberId)
                            MMKVBroadUtils.saveUserData(bean)
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
                            if (!data.twoFactorCode && !data.existMbName && !data.existPayPassword) {
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
                        showToastMain("${error.message}")
                        liveData.postValue(null)
                    }
                } catch (_: kotlin.Exception) {
                    liveData.postValue(null)
                }
            }
        }
        return liveData
    }

    fun getScanLoginDetail(linkId: String): SingleLiveBroadData<LoginBean?> {
        val params = hashMapOf("idno" to linkId, "eqno" to getAndroidId())
        val liveData = SingleLiveBroadData<LoginBean?>()
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val result = NetworkBroadRepository.postEncrypted("api/account/getScanQrCodeYz", LoginBean::class.java, params)
                    result.onSuccess { data ->
//                        eLogD("LoginViewBroadModelClass", "getScanLoginDetail:${toJson2(data)}")
                        if (data?.oldDevice == true) {
                            oneClickLogin()
                        } else {
                            liveData.postValue(data)
                        }
                    }.onFailure { _, _ ->
                        liveData.postValue(null)
                    }.onError { _ ->
                        liveData.postValue(null)
                    }
                } catch (_: kotlin.Exception) {
                    liveData.postValue(null)
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
                        eLogD("updatePasswordClass", "onSuccess")
                        liveData.postValue(true)
                        dismissLoading()
                    }.onFailure { code, message ->
                        eLogD("updatePasswordClass", "onFailure:$code ,$message")
                        dismissLoading()
                        showToastMain(message)
                    }.onError { error ->
                        eLogD("updatePasswordClass", "onError:${error.message}")
                        dismissLoading()
                        showToastMain("${error.message}")
                    }
                } catch (e: kotlin.Exception) {
                    eLogD("updatePasswordClass", "catch:${e.message}")
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
                "clientId" to ChatManagerBroadHolder.gChatManager.getClientId(),
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

    fun pwdLogin(account: String, pwd: String, code: String = "", payPwd: String = "", googleCode: String = "", securityQuestion: String = "") {
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
                "clientId" to ChatManagerBroadHolder.gChatManager.getClientId(),
                "type" to 2,
                "model" to model
            )
        if (!TextUtils.isEmpty(payPwd)) {
            param["paywd"] = payPwd
        }
        if (!TextUtils.isEmpty(googleCode)) {
            param["googleCode"] = googleCode
        }
        if (!TextUtils.isEmpty(securityQuestion)) {
            param["mbda"] = securityQuestion
        }
//        eLogD("LoginViewModelClass_pwdLoginMap", param)
        UserServerBroadManager.accLogin(
            mContext,
            param,
            callBack = object : SpotsCallBroadBack<String>(mContext) {
                override fun onSuccess(response: Response?, result: String?) {
                    if (!TextUtils.isEmpty(result)) {
                        val resultBean = fromJson2(result, ResultBroadBean::class.java)
                        if (resultBean.code == "202") {
                            QrdlBroadDialog(
                                mContext,
                                { code -> pwdLogin(account, pwd, code ?: "") }, account
                            ).show()
                        } else if (resultBean.code == "200" && resultBean.success) {
                            imLogin(resultBean, mContext)
                        } else if (!TextUtils.isEmpty(resultBean.msg)) {
                            showToastMain(resultBean.msg)
                        }
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


    fun registerAccount(account: String, pwd: String, secondPwd: String) {
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
        val model = "${getDeviceBrand()} ${getDeviceModel()}"
        val param =
            mapOf(
                "account" to account,
                "pwd" to pwd,
                "eqno" to getAndroidId(),
                "clientId" to ChatManagerBroadHolder.gChatManager.getClientId(),
                "type" to 2,
                "model" to model
            )
//        val param =
//            mapOf(
//                "acc" to account,
//                "pwd" to pwd,
//                "eqno" to getUuIdMd5(),
//                "clientId" to getUuIdMd5(),
//                "type" to 2,
//                "model" to getUuIdMd5()
//            )
//        eLogD("LoginViewModelClass_registeredMap", param)
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
                        if (resultBean.isws == "0") {
                            val newIntent = Intent(mContext, PushInfo2BroadActivity::class.java)
                            newIntent.putExtra("apptoken", resultBean.apptoken)
                            newIntent.putExtra("uid", resultBean.userId)
                            newIntent.putExtra("userSign", resultBean.userSign)
                            newIntent.putExtra("imtoken", resultBean.imtoken)
                            newIntent.putExtra("idno", resultBean.idno)
                            newIntent.putExtra("qrcode", resultBean.qrcode)
                            newIntent.putExtra("account", account)
                            newIntent.putExtra("pwd", pwd)
                            mContext.startActivity(newIntent)
                        } else {
                            imLogin(resultBean, mContext)
                        }
                    } ?: showToastMain(com.cboda.baselibrary.R.string.service_error_token_null)
                }

                override fun onError(
                    response: Response?,
                    code: Int,
                    e: Exception?
                ) {

                }
            })
    }

}