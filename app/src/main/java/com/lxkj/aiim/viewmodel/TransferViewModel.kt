package com.lxkj.aiim.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.cboda.baselibrary.network.repository.NetworkListBroadResult
import com.cboda.baselibrary.network.repository.NetworkBroadRepository
import com.cboda.baselibrary.network.result.onError
import com.cboda.baselibrary.network.result.onFailure
import com.cboda.baselibrary.network.result.onSuccess
import com.cboda.intelligent.R
import com.lxkj.aiim.livedata.SingleLiveData
import com.lxkj.aiim.lj.bean.CommonSelectBean
import com.lxkj.aiim.lj.bean.ResultBean
import com.lxkj.aiim.lj.configuration.CommonConfig
import com.lxkj.aiim.lj.http.BaseCallback
import com.lxkj.aiim.lj.http.OkHttpHelper
import com.lxkj.aiim.lj.http.Url
import com.lxkj.aiim.lj.http.UrlBroadConstants
import com.lxkj.aiim.lj.utils.AppUtil
import com.lxkj.aiim.lj.utils.getTopBaseSimple2Activity
import com.lxkj.aiim.lj.utils.showToastMain
import com.lxkj.aiim.model.OrderInfoBean
import com.lxkj.aiim.model.PersonInfoBean
import com.lxkj.aiim.model.RateInfoBean
import com.lxkj.aiim.model.ResultBooBean
import com.lxkj.aiim.model.SafeCenterBean
import com.lxkj.aiim.model.TransferInfoBean
import com.lxkj.aiim.model.TwoFactorValidate
import com.lxkj.aiim.model.TwoStepAuthBean
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.onFailure
import kotlin.onSuccess


class TransferViewModel: ViewModel() {

    /**
     * 获取资产初始信息
     */
    suspend fun goTransferInfo() :SingleLiveData<TransferInfoBean>{
        val safeCenterLiveData = SingleLiveData<TransferInfoBean>()
        val params = hashMapOf("code" to "")
        withContext(Dispatchers.IO) {
            try {
                NetworkBroadRepository.postEncrypted2<TransferInfoBean>(
                    UrlBroadConstants.TRANSACTION_INITIALIZE,
                    TransferInfoBean::class.java,
                    params
                ).onSuccess { userInfo ->
                    userInfo?.let {
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
     * 获取转账人信息
     */
    suspend fun getTransactionPersonInfo(receivingAddress: String) :SingleLiveData<PersonInfoBean>{
        getTopBaseSimple2Activity()?.showLoading()
        val mLiveData = SingleLiveData<PersonInfoBean>()
        val params = hashMapOf("receivingAddress" to receivingAddress)
        withContext(Dispatchers.IO) {
            try {
                NetworkBroadRepository.postEncrypted2(
                    UrlBroadConstants.TRANSACTION_PERSONINFO,
                    PersonInfoBean::class.java,
                    params
                ).onSuccess { userInfo ->
                    getTopBaseSimple2Activity()?.dismissLoading()
                    userInfo?.let {
                        mLiveData.postValue(it)
                    }

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
     * 获取费率
     * type=1:转账(入金wx) 2:提现
     */
    suspend fun getRateInfo(amount: String,type: Int=1,address: String) :SingleLiveData<RateInfoBean>{
        val mLiveData = SingleLiveData<RateInfoBean>()
        val params = hashMapOf<String, Any?>("amount" to amount)
        params["type"] = type
        params["address"] = address
        withContext(Dispatchers.IO) {
            try {
                NetworkBroadRepository.postEncrypted2(
                    UrlBroadConstants.TRANSACTION_RATEINFO,
                    RateInfoBean::class.java,
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
     * 转账交易下单
     */
    suspend fun goTransactionAddOrder(receivingAddress: String,amount: String) :SingleLiveData<OrderInfoBean>{
        getTopBaseSimple2Activity()?.showLoading()
        val mLiveData = SingleLiveData<OrderInfoBean>()
        val params = hashMapOf<String, Any>()
        params["receivingAddress"] = receivingAddress
        params["amount"] = amount
        withContext(Dispatchers.IO) {
            try {
                NetworkBroadRepository.postEncrypted2(
                    UrlBroadConstants.TRANSACTION_ADD_ORDER,
                    OrderInfoBean::class.java,
                    params
                ).onSuccess { info ->
                    getTopBaseSimple2Activity()?.dismissLoading()
                    info?.let {
                        mLiveData.postValue(it)
                    }
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
     * 提现下单/asset/withdraw/add
     */
    suspend fun goWithdrawAddOrder(receivingAddress: String,amount: String) :SingleLiveData<OrderInfoBean>{
        getTopBaseSimple2Activity()?.showLoading()
        val mLiveData = SingleLiveData<OrderInfoBean>()
        val params = hashMapOf<String, Any>()
        params["receivingAddress"] = receivingAddress
        params["amount"] = amount
        withContext(Dispatchers.IO) {
            try {
                NetworkBroadRepository.postEncrypted2(
                    UrlBroadConstants.WITHDRAW_ADD_ORDER,
                    OrderInfoBean::class.java,
                    params
                ).onSuccess { info ->
                    getTopBaseSimple2Activity()?.dismissLoading()
                    info?.let {
                        mLiveData.postValue(it)
                    }
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
     * 转账列表
     */
    suspend fun getTransferInfoList(pageNo: Int =1) :SingleLiveData<NetworkListBroadResult<OrderInfoBean>>{
        val safeCenterLiveData = SingleLiveData<NetworkListBroadResult<OrderInfoBean>>()
        val params = hashMapOf("pageNo" to pageNo,"pageSize" to CommonConfig.pageSize)
        withContext(Dispatchers.IO) {
            try {
                NetworkBroadRepository.postEncryptedList2<OrderInfoBean>(
                    UrlBroadConstants.TRANSACTION_PAGE,
                    OrderInfoBean::class.java,
                    params
                ).onSuccess { bean ->
                    bean?.let {
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
     * 提现列表
     */
    suspend fun getWithdrawInfoList(pageNo: Int =1) :SingleLiveData<NetworkListBroadResult<OrderInfoBean>>{
        val safeCenterLiveData = SingleLiveData<NetworkListBroadResult<OrderInfoBean>>()
        val params = hashMapOf("pageNo" to pageNo,"pageSize" to CommonConfig.pageSize)
        withContext(Dispatchers.IO) {
            try {
                NetworkBroadRepository.postEncryptedList2<OrderInfoBean>(
                    UrlBroadConstants.WITHDRAW_PAGE,
                    OrderInfoBean::class.java,
                    params
                ).onSuccess { bean ->
                    bean?.let {
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
     * 转账详情
     */
    suspend fun goTransactionOrderDetail(id: String) :SingleLiveData<OrderInfoBean>{
        val mLiveData = SingleLiveData<OrderInfoBean>()
        val params = hashMapOf("id" to id)
        withContext(Dispatchers.IO) {
            try {
                NetworkBroadRepository.postEncrypted2(
                    UrlBroadConstants.TRANSACTION_ORDER_DETAIL,
                    OrderInfoBean::class.java,
                    params
                ).onSuccess { info ->
                    info?.let {
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
     * 提现详情/asset/withdraw/detail
     */
    suspend fun goWithdrawOrderDetail(id: String) :SingleLiveData<OrderInfoBean>{
        val mLiveData = SingleLiveData<OrderInfoBean>()
        val params = hashMapOf("id" to id)
        withContext(Dispatchers.IO) {
            try {
                NetworkBroadRepository.postEncrypted2(
                    UrlBroadConstants.WITHDRAW_ORDER_DETAIL,
                    OrderInfoBean::class.java,
                    params
                ).onSuccess { info ->
                    info?.let {
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
     * 安全中心
     */
    suspend fun goSecurityCenter() :SingleLiveData<SafeCenterBean>{
        val safeCenterLiveData = SingleLiveData<SafeCenterBean>()
        val params = hashMapOf("code" to "")
        withContext(Dispatchers.IO) {
            try {
                NetworkBroadRepository.postEncrypted2<SafeCenterBean>(
                    UrlBroadConstants.SECURITY_CENTER,
                    SafeCenterBean::class.java,
                    params
                ).onSuccess { userInfo ->
                    userInfo?.let {
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
     * 是否第一次交易/api/member//security/first
     */
    suspend fun checkSecurityFirst() :SingleLiveData<ResultBooBean>{
        val mLiveData = SingleLiveData<ResultBooBean>()
        val params = hashMapOf("code" to "")
        withContext(Dispatchers.IO) {
            try {
                NetworkBroadRepository.postEncrypted2(
                    UrlBroadConstants.IS_SECURITY_FIRST,
                    ResultBooBean::class.java,
                    params
                ).onSuccess { bean ->
                    bean?.let {
                        mLiveData.postValue(bean)
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

}