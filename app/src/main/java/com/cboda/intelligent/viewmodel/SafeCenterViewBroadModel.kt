package com.cboda.intelligent.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.cboda.baselibrary.base.BaseViewModel
import com.cboda.baselibrary.network.repository.NetworkBroadRepository
import com.cboda.baselibrary.network.result.onError
import com.cboda.baselibrary.network.result.onFailure
import com.cboda.baselibrary.network.result.onSuccess
import com.cboda.intelligent.livedata.SingleLiveBroadData
import com.cboda.intelligent.bean.CommonSelectBroadBean
import com.cboda.intelligent.bean.ResultBroadBean
import com.cboda.intelligent.utils.BaseBroadCallback
import com.cboda.intelligent.utils.OkHttpBroadHelper
import com.cboda.intelligent.net.BroadUrl
import com.cboda.intelligent.utils.UrlBroadBroadConstants
import com.cboda.intelligent.utils.dismissLoading
import com.cboda.intelligent.utils.getTopBaseSimple2Activity
import com.cboda.intelligent.utils.showLoading
import com.cboda.intelligent.utils.showToastMain
import com.cboda.intelligent.model.SafeCenterBroadBean
import com.cboda.intelligent.model.SecurityInfoBroadBean
import com.cboda.intelligent.model.TwoFactorBroadValidate
import com.cboda.intelligent.model.TwoStepAuthBroadBean
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


open class SafeCenterViewBroadModel: BaseViewModel() {

    /**
     * 根据账号查询密保问题
     */
    suspend fun goSecurityInfo(account: String?) :SingleLiveBroadData<SecurityInfoBroadBean>{
        showLoading()
        val safeCenterLiveData = SingleLiveBroadData<SecurityInfoBroadBean>()
        val params = hashMapOf("account" to account)
        withContext(Dispatchers.IO) {
            try {
                NetworkBroadRepository.postEncrypted2<SecurityInfoBroadBean>(
                    UrlBroadBroadConstants.SECURITY_ACCOUNT,
                    SecurityInfoBroadBean::class.java,
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
    suspend fun goSecurityCenter() :SingleLiveBroadData<SafeCenterBroadBean>{
        showLoading()
        val safeCenterLiveData = SingleLiveBroadData<SafeCenterBroadBean>()
        val params = hashMapOf("code" to "")
        withContext(Dispatchers.IO) {
            try {
                NetworkBroadRepository.postEncrypted2<SafeCenterBroadBean>(
                    UrlBroadBroadConstants.SECURITY_CENTER,
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

            } catch (e: Exception) {
                dismissLoading()
            }
        }
        return safeCenterLiveData
    }

    /**
     * 获得两步验证二维码
     */
    suspend fun goTwoStepAuth() :SingleLiveBroadData<TwoStepAuthBroadBean>{
        val mLiveData = SingleLiveBroadData<TwoStepAuthBroadBean>()
        val params = hashMapOf("code" to "")
        withContext(Dispatchers.IO) {
            try {
                NetworkBroadRepository.postEncrypted2(
                    UrlBroadBroadConstants.TWO_STEP_VERIFICATION_QRCODE,
                    TwoStepAuthBroadBean::class.java,
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
    suspend fun goBindTwoFactorValidate(mBody: TwoFactorBroadValidate) :SingleLiveBroadData<Boolean>{
        getTopBaseSimple2Activity()?.showLoading()
        val mLiveData = SingleLiveBroadData<Boolean>()
        withContext(Dispatchers.IO) {
            try {
                NetworkBroadRepository.postEncrypted2(
                    UrlBroadBroadConstants.BINDING_TWO_FACTOR_VALIDATE,
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
    suspend fun goValidateTwoFactor(inputGoogleCode: String,type: Int =0,account:String?=null) :SingleLiveBroadData<Boolean>{
        showLoading()
        val mLiveData = SingleLiveBroadData<Boolean>()
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
                    UrlBroadBroadConstants.TWO_FACTOR_VALIDATE,
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
    suspend fun goSecurityInfo(id: String?,title: String?,answer: String?) :SingleLiveBroadData<Boolean>{
        getTopBaseSimple2Activity()?.showLoading()
        val mLiveData = SingleLiveBroadData<Boolean>()
        val params = hashMapOf("id" to id,"title" to title,"answer" to answer)
        withContext(Dispatchers.IO) {
            try {
                NetworkBroadRepository.postEncrypted2(
                    UrlBroadBroadConstants.SET_MIBAO,
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
    suspend fun checkSecurityInfo(title: String?,answer: String?,type: Int =0,account: String?=null) :SingleLiveBroadData<Boolean>{
        showLoading()
        val mLiveData = SingleLiveBroadData<Boolean>()
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
                    UrlBroadBroadConstants.CHANGE_MIBAO,
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
    suspend fun getQuestionList() :SingleLiveBroadData<List<CommonSelectBroadBean>>{
        val safeCenterLiveData = SingleLiveBroadData<List<CommonSelectBroadBean>>()
        val params = hashMapOf("code" to "")
        withContext(Dispatchers.IO) {
            try {
                NetworkBroadRepository.postEncryptedList2<CommonSelectBroadBean>(
                    UrlBroadBroadConstants.SECURITY_QUESTION_LIST,
                    CommonSelectBroadBean::class.java,
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
     fun userInfo(context: Context,callback: BaseBroadCallback<ResultBroadBean>) {
        val param: MutableMap<String, Any> = HashMap()
        OkHttpBroadHelper.getInstance()
            .post_json(context, BroadUrl.userInfo, param, callback)
    }
}