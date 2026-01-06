package com.lxkj.aiim.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.cboda.baselibrary.network.repository.NetworkBroadRepository
import com.cboda.baselibrary.network.result.onError
import com.cboda.baselibrary.network.result.onFailure
import com.cboda.baselibrary.network.result.onSuccess
import com.cboda.intelligent.R
import com.lxkj.aiim.livedata.SingleLiveData
import com.lxkj.aiim.lj.bean.CommonSelectBean
import com.lxkj.aiim.lj.bean.ResultBean
import com.lxkj.aiim.lj.http.BaseCallback
import com.lxkj.aiim.lj.http.OkHttpHelper
import com.lxkj.aiim.lj.http.Url
import com.lxkj.aiim.lj.http.UrlBroadConstants
import com.lxkj.aiim.lj.utils.dismissLoading
import com.lxkj.aiim.lj.utils.getTopBaseSimple2Activity
import com.lxkj.aiim.lj.utils.showLoading
import com.lxkj.aiim.lj.utils.showToastMain
import com.lxkj.aiim.model.SafeCenterBean
import com.lxkj.aiim.model.SecurityInfoBean
import com.lxkj.aiim.model.TwoFactorValidate
import com.lxkj.aiim.model.TwoStepAuthBean
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


open class SafeCenterViewModel: ViewModel() {

    /**
     * 根据账号查询密保问题
     */
    suspend fun goSecurityInfo(account: String?) :SingleLiveData<SecurityInfoBean>{
        showLoading()
        val safeCenterLiveData = SingleLiveData<SecurityInfoBean>()
        val params = hashMapOf("account" to account)
        withContext(Dispatchers.IO) {
            try {
                NetworkBroadRepository.postEncrypted2<SecurityInfoBean>(
                    UrlBroadConstants.SECURITY_ACCOUNT,
                    SecurityInfoBean::class.java,
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

            } catch (e: Exception) {
                e.printStackTrace()
                dismissLoading()
            }
        }
        return safeCenterLiveData
    }

    /**
     * 安全中心
     */
    suspend fun goSecurityCenter() :SingleLiveData<SafeCenterBean>{
        showLoading()
        val safeCenterLiveData = SingleLiveData<SafeCenterBean>()
        val params = hashMapOf("code" to "")
        withContext(Dispatchers.IO) {
            try {
                NetworkBroadRepository.postEncrypted2<SafeCenterBean>(
                    UrlBroadConstants.SECURITY_CENTER,
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

            } catch (e: Exception) {
                dismissLoading()
            }
        }
        return safeCenterLiveData
    }

    /**
     * 获得两步验证二维码
     */
    suspend fun goTwoStepAuth() :SingleLiveData<TwoStepAuthBean>{
        val mLiveData = SingleLiveData<TwoStepAuthBean>()
        val params = hashMapOf("code" to "")
        withContext(Dispatchers.IO) {
            try {
                NetworkBroadRepository.postEncrypted2(
                    UrlBroadConstants.TWO_STEP_VERIFICATION_QRCODE,
                    TwoStepAuthBean::class.java,
                    params
                ).onSuccess { userInfo ->
                    userInfo?.let {
                        mLiveData.postValue(it)
                    }

                }.onFailure { code, message ->
                    showToastMain(message)
                }.onError {

                }

            } catch (e: Exception) {

            }
        }
        return mLiveData
    }

    /**
     * 执行谷歌两步验证绑定
     */
    suspend fun goBindTwoFactorValidate(mBody: TwoFactorValidate) :SingleLiveData<Boolean>{
        getTopBaseSimple2Activity()?.showLoading()
        val mLiveData = SingleLiveData<Boolean>()
        withContext(Dispatchers.IO) {
            try {
                NetworkBroadRepository.postEncrypted2(
                    UrlBroadConstants.BINDING_TWO_FACTOR_VALIDATE,
                    Any::class.java,
                    mBody
                ).onSuccess { userInfo ->
                    getTopBaseSimple2Activity()?.dismissLoading()
                    mLiveData.postValue(true)

                }.onFailure { code, message ->
                    getTopBaseSimple2Activity()?.dismissLoading()
                    showToastMain(message)
                }.onError {
                    getTopBaseSimple2Activity()?.dismissLoading()
                }

            } catch (e: Exception) {
                getTopBaseSimple2Activity()?.dismissLoading()
            }
        }
        return mLiveData
    }

    /**
     * 验证谷歌两步code
     */
    suspend fun goValidateTwoFactor(inputGoogleCode: String,type: Int =0,account:String?=null) :SingleLiveData<Boolean>{
        showLoading()
        val mLiveData = SingleLiveData<Boolean>()
        val params = hashMapOf<String, Any?>("inputGoogleCode" to inputGoogleCode)
        if (type !=0){
            params["type"] = type
        }
        if (!account.isNullOrEmpty()){
            params["account"] = account
        }
        withContext(Dispatchers.IO) {
            try {
                NetworkBroadRepository.postEncrypted2(
                    UrlBroadConstants.TWO_FACTOR_VALIDATE,
                    Any::class.java,
                    params
                ).onSuccess { userInfo ->
                    dismissLoading()
                    mLiveData.postValue(true)

                }.onFailure { code, message->
                    dismissLoading()
                    showToastMain(message)

                }.onError {
                    dismissLoading()
                }

            } catch (e: Exception) {
                dismissLoading()
            }
        }
        return mLiveData
    }


    /**
     * 设置密保
     */
    suspend fun goSecurityInfo(id: String?,title: String?,answer: String?) :SingleLiveData<Boolean>{
        getTopBaseSimple2Activity()?.showLoading()
        val mLiveData = SingleLiveData<Boolean>()
        val params = hashMapOf("id" to id,"title" to title,"answer" to answer)
        withContext(Dispatchers.IO) {
            try {
                NetworkBroadRepository.postEncrypted2(
                    UrlBroadConstants.SET_MIBAO,
                    Any::class.java,
                    params
                ).onSuccess { userInfo ->
                    getTopBaseSimple2Activity()?.dismissLoading()
                    mLiveData.postValue(true)

                }.onFailure { code, message->
                    getTopBaseSimple2Activity()?.dismissLoading()
                    showToastMain(message)

                }.onError {
                    getTopBaseSimple2Activity()?.dismissLoading()
                }

            } catch (e: Exception) {
                getTopBaseSimple2Activity()?.dismissLoading()
            }
        }
        return mLiveData
    }


    /**
     * 验证密保问题
     */
    suspend fun checkSecurityInfo(title: String?,answer: String?,type: Int =0,account: String?=null) :SingleLiveData<Boolean>{
        showLoading()
        val mLiveData = SingleLiveData<Boolean>()
        val params = hashMapOf<String, Any?>("title" to title,"answer" to answer)
        if (type !=0){
            params["type"] = type
        }
        if (!account.isNullOrEmpty()){
            params["account"] = account
        }
        withContext(Dispatchers.IO) {
            try {
                NetworkBroadRepository.postEncrypted2(
                    UrlBroadConstants.CHANGE_MIBAO,
                    Any::class.java,
                    params
                ).onSuccess { userInfo ->
                    dismissLoading()
                    mLiveData.postValue(true)

                }.onFailure { code, message->
                    dismissLoading()
                    showToastMain(message)

                }.onError {
                    dismissLoading()
                }

            } catch (e: Exception) {
                dismissLoading()
            }
        }
        return mLiveData
    }


    /**
     * 密保问题列表
     */
    suspend fun getQuestionList() :SingleLiveData<List<CommonSelectBean>>{
        val safeCenterLiveData = SingleLiveData<List<CommonSelectBean>>()
        val params = hashMapOf("code" to "")
        withContext(Dispatchers.IO) {
            try {
                NetworkBroadRepository.postEncryptedList2<CommonSelectBean>(
                    UrlBroadConstants.SECURITY_QUESTION_LIST,
                    CommonSelectBean::class.java,
                    params
                ).onSuccess { bean ->
                    bean?.list?.let {
                        safeCenterLiveData.postValue(it)
                    }

                }.onFailure { code, message ->
                    showToastMain(message)
                }.onError {

                }

            } catch (e: Exception) {

            }
        }
        return safeCenterLiveData
    }


    /**
     * 用户信息
     * */
     fun userInfo(context: Context,callback: BaseCallback<ResultBean>) {
        val param: MutableMap<String, Any> = HashMap()
        OkHttpHelper.getInstance()
            .post_json(context, Url.userInfo, param, callback)
    }
}