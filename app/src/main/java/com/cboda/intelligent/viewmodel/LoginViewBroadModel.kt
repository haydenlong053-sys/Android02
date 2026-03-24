package com.cboda.intelligent.viewmodel

import android.content.Context
import android.content.Intent
import android.text.TextUtils
import androidx.lifecycle.viewModelScope
import com.cboda.baselibrary.bean.LoginBean
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
import com.cboda.baselibrary.utils.toJson2
import com.cboda.intelligent.activity.PushInfo2BroadActivity
import com.cboda.intelligent.bean.ResultBroadBean
import com.cboda.intelligent.utils.SpotsCallBroadBack
import com.cboda.intelligent.net.BroadUrl
import com.cboda.intelligent.utils.UrlBroadBroadConstants
import com.cboda.intelligent.utils.dismissLoading
import com.cboda.intelligent.utils.showLoading
import com.cboda.intelligent.utils.showToastMain
import com.cboda.intelligent.model.SafeCenterBroadBean
import com.cboda.intelligent.net.manager.UserServerBroadManager
import com.cboda.intelligent.utils.AppBroadUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Response
import java.lang.Exception
import kotlin.jvm.java

class LoginViewBroadModel : BaseViewBroadModel() {


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
                } catch (e: kotlin.Exception) {
                    liveData.postValue(null)
                }
            }
        }
        return liveData
    }

    fun updatePassword(account: String, pwd: String, secondPwd: String): SingleLiveBroadData<Boolean> {
        val liveData = SingleLiveBroadData<Boolean>()
        if (TextUtils.isEmpty(pwd)) {
            showToastMain("请输入您的密码！")
            return liveData
        }
        if (TextUtils.isEmpty(secondPwd)) {
            showToastMain("请再次输入您的密码！")
            return liveData
        }
        if (pwd != secondPwd) {
            showToastMain("您两次输入的密码不一致！")
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

            } catch (e: kotlin.Exception) {
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
        eLogD("LoginViewModelClass_oneClickLoginMap", param)
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
                    } ?: showToastMain("服务器错误：imToken为空")
                }

                override fun onError(
                    response: Response?,
                    code: Int,
                    e: Exception?
                ) {
//                    showToastMain("服务器错误：imToken为空")
                }
            })

    }

    private fun imLogin(resultBean: ResultBroadBean, context: Context = getAppContext()) {
        AppBroadUtils.imLogin(
            context,
            resultBean.apptoken,
            resultBean.userSign,
            resultBean.userId,
            resultBean.imtoken
        )
    }

    fun pwdLogin(account: String, pwd: String, code: String = "") {
        if (TextUtils.isEmpty(account)) {
            showToastMain("请输入您的帐号！")
            return
        }
        if (TextUtils.isEmpty(pwd)) {
            showToastMain("请输入您的密码！")
            return
        }
        val mContext = getAppContext()
        val model = "${getDeviceBrand()} ${getDeviceModel()}"
        val param =
            mapOf(
                "acc" to account,
                "pwd" to pwd,
                "code" to code,
                "eqno" to getAndroidId(),
                "clientId" to ChatManagerBroadHolder.gChatManager.getClientId(),
                "type" to 2,
                "model" to model
            )
        eLogD("LoginViewModelClass_pwdLoginMap", param)
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
                        showToastMain("服务器错误：数据为空")
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
            showToastMain("请输入您的帐号！")
            return
        }
        if (TextUtils.isEmpty(pwd)) {
            showToastMain("请输入您的密码！")
            return
        }
        if (TextUtils.isEmpty(secondPwd)) {
            showToastMain("请再次输入您的密码！")
            return
        }
        if (pwd != secondPwd) {
            showToastMain("您两次输入的密码不一致！")
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
        eLogD("LoginViewModelClass_registeredMap", param)
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
                            newIntent.putExtra("apptoken", resultBean.apptoken);
                            newIntent.putExtra("uid", resultBean.userId);
                            newIntent.putExtra("userSign", resultBean.userSign);
                            newIntent.putExtra("imtoken", resultBean.imtoken);
                            newIntent.putExtra("idno", resultBean.idno);
                            newIntent.putExtra("qrcode", resultBean.qrcode);
                            newIntent.putExtra("account", account)
                            newIntent.putExtra("pwd", pwd)
                            mContext.startActivity(newIntent)
                        } else {
                            imLogin(resultBean, mContext)
                        }
                    } ?: showToastMain("服务器错误：imToken为空")
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