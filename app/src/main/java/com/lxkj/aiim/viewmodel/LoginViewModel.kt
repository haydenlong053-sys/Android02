package com.lxkj.aiim.viewmodel

import android.content.Context
import android.content.Intent
import android.text.TextUtils
import androidx.lifecycle.viewModelScope
import cn.wildfire.chat.kit.ChatManagerHolder
import com.base.commonlibrary.bean.GlobalSmallGroupBean
import com.base.commonlibrary.network.repository.NetworkRepository
import com.base.commonlibrary.network.result.onError
import com.base.commonlibrary.network.result.onFailure
import com.base.commonlibrary.network.result.onSuccess
import com.base.commonlibrary.utils.DeviceUtil.getAndroidId
import com.base.commonlibrary.utils.DeviceUtil.getDeviceBrand
import com.base.commonlibrary.utils.DeviceUtil.getDeviceModel
import com.base.commonlibrary.utils.eLogD
import com.base.commonlibrary.utils.fromJson2
import com.base.commonlibrary.utils.getAppContext
import com.lxkj.aiim.base.BaseViewModel
import com.lxkj.aiim.dialog.QrdlDialog
import com.lxkj.aiim.livedata.SingleLiveData
import com.lxkj.aiim.lj.PushInfo2Activity
import com.lxkj.aiim.lj.bean.ResultBean
import com.lxkj.aiim.lj.http.SpotsCallBack
import com.lxkj.aiim.lj.http.Url
import com.lxkj.aiim.lj.http.UrlConstants
import com.lxkj.aiim.lj.utils.dismissLoading
import com.lxkj.aiim.lj.utils.showLoading
import com.lxkj.aiim.lj.utils.showToastMain
import com.lxkj.aiim.model.SafeCenterBean
import com.lxkj.aiim.net.manager.UserServerManager
import com.lxkj.aiim.utils.AppUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Response
import java.lang.Exception

class LoginViewModel : BaseViewModel() {


    fun updatePassword(account: String, pwd: String, secondPwd: String):SingleLiveData<Boolean> {
        val liveData = SingleLiveData<Boolean>()
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
                    val result = NetworkRepository.postEncrypted(
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
    fun getMnemonicWords(): SingleLiveData<String?> {
        val liveData = SingleLiveData<String?>()
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val result = NetworkRepository.postEncrypted(
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
    suspend fun checkAccount(account: String): SingleLiveData<SafeCenterBean> {
        showLoading()
        val safeCenterLiveData = SingleLiveData<SafeCenterBean>()
        val params = hashMapOf("account" to account)
        withContext(Dispatchers.IO) {
            try {
                NetworkRepository.postEncrypted2<SafeCenterBean>(
                    UrlConstants.SECURITY_CENTER_FORGET_PASSWORD,
                    SafeCenterBean::class.java,
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
                "clientId" to ChatManagerHolder.gChatManager.getClientId(),
                "type" to 2,
                "model" to model
            )
        eLogD("LoginViewModelClass_oneClickLoginMap", param)
        UserServerManager.qeLogin(
            mContext,
            param,
            callBack = object : SpotsCallBack<ResultBean>(mContext) {
                override fun onSuccess(
                    response: Response?,
                    resultBean: ResultBean?
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

    private fun imLogin(resultBean: ResultBean, context: Context = getAppContext()) {
        AppUtil.imLogin(
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
                "clientId" to ChatManagerHolder.gChatManager.getClientId(),
                "type" to 2,
                "model" to model
            )
        eLogD("LoginViewModelClass_pwdLoginMap", param)
        UserServerManager.accLogin(
            mContext,
            param,
            callBack = object : SpotsCallBack<String>(mContext) {
                override fun onSuccess(response: Response?, result: String?) {
                    if (!TextUtils.isEmpty(result)) {
                        val resultBean = fromJson2(result, ResultBean::class.java)
                        if (resultBean.code == "202") {
                            QrdlDialog(
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
                "clientId" to ChatManagerHolder.gChatManager.getClientId(),
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
        UserServerManager.qeLogin(
            mContext,
            param,
            Url.regist,
            callBack = object : SpotsCallBack<ResultBean>(mContext) {
                override fun onSuccess(
                    response: Response?,
                    resultBean: ResultBean?
                ) {
                    resultBean?.let {
                        if (resultBean.isws == "0") {
                            val newIntent = Intent(mContext, PushInfo2Activity::class.java)
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