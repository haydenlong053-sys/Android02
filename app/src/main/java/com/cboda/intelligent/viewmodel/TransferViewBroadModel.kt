package com.cboda.intelligent.viewmodel

import androidx.lifecycle.ViewModel
import com.cboda.baselibrary.base.BaseViewModel
import com.cboda.baselibrary.network.repository.NetworkListBroadResult
import com.cboda.baselibrary.network.repository.NetworkBroadRepository
import com.cboda.baselibrary.network.result.onError
import com.cboda.baselibrary.network.result.onFailure
import com.cboda.baselibrary.network.result.onSuccess
import com.cboda.intelligent.livedata.SingleLiveBroadData
import com.cboda.intelligent.configuration.CommonBroadConfig
import com.cboda.intelligent.utils.UrlBroadBroadConstants
import com.cboda.intelligent.utils.getTopBaseSimple2Activity
import com.cboda.intelligent.utils.showToastMain
import com.cboda.intelligent.model.OrderInfoBean
import com.cboda.intelligent.model.PersonInfoBean
import com.cboda.intelligent.model.RateInfoBean
import com.cboda.intelligent.model.ResultBooBean
import com.cboda.intelligent.model.SafeCenterBroadBean
import com.cboda.intelligent.model.TransferInfoBroadBean
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class TransferViewBroadModel: BaseViewModel() {

    /**
     * 获取资产初始信息
     */
    suspend fun goTransferInfo() :SingleLiveBroadData<TransferInfoBroadBean>{
        val safeCenterLiveData = SingleLiveBroadData<TransferInfoBroadBean>()
        val params = hashMapOf("code" to "")
        withContext(Dispatchers.IO) {
            try {
                NetworkBroadRepository.postEncrypted2<TransferInfoBroadBean>(
                    UrlBroadBroadConstants.TRANSACTION_INITIALIZE,
                    TransferInfoBroadBean::class.java,
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
    suspend fun getTransactionPersonInfo(receivingAddress: String) :SingleLiveBroadData<PersonInfoBean>{
        getTopBaseSimple2Activity()?.showLoading()
        val mLiveData = SingleLiveBroadData<PersonInfoBean>()
        val params = hashMapOf("receivingAddress" to receivingAddress)
        withContext(Dispatchers.IO) {
            try {
                NetworkBroadRepository.postEncrypted2(
                    UrlBroadBroadConstants.TRANSACTION_PERSONINFO,
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
    suspend fun getRateInfo(amount: String,type: Int=1,address: String) :SingleLiveBroadData<RateInfoBean>{
        val mLiveData = SingleLiveBroadData<RateInfoBean>()
        val params = hashMapOf<String, Any?>("amount" to amount)
        params["type"] = type
        params["address"] = address
        withContext(Dispatchers.IO) {
            try {
                NetworkBroadRepository.postEncrypted2(
                    UrlBroadBroadConstants.TRANSACTION_RATEINFO,
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
    suspend fun goTransactionAddOrder(receivingAddress: String,amount: String) :SingleLiveBroadData<OrderInfoBean>{
        getTopBaseSimple2Activity()?.showLoading()
        val mLiveData = SingleLiveBroadData<OrderInfoBean>()
        val params = hashMapOf<String, Any>()
        params["receivingAddress"] = receivingAddress
        params["amount"] = amount
        withContext(Dispatchers.IO) {
            try {
                NetworkBroadRepository.postEncrypted2(
                    UrlBroadBroadConstants.TRANSACTION_ADD_ORDER,
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
    suspend fun goWithdrawAddOrder(receivingAddress: String,amount: String) :SingleLiveBroadData<OrderInfoBean>{
        getTopBaseSimple2Activity()?.showLoading()
        val mLiveData = SingleLiveBroadData<OrderInfoBean>()
        val params = hashMapOf<String, Any>()
        params["receivingAddress"] = receivingAddress
        params["amount"] = amount
        withContext(Dispatchers.IO) {
            try {
                NetworkBroadRepository.postEncrypted2(
                    UrlBroadBroadConstants.WITHDRAW_ADD_ORDER,
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
    suspend fun getTransferInfoList(pageNo: Int =1) :SingleLiveBroadData<NetworkListBroadResult<OrderInfoBean>>{
        val safeCenterLiveData = SingleLiveBroadData<NetworkListBroadResult<OrderInfoBean>>()
        val params = hashMapOf("pageNo" to pageNo,"pageSize" to CommonBroadConfig.pageSize)
        withContext(Dispatchers.IO) {
            try {
                NetworkBroadRepository.postEncryptedList2<OrderInfoBean>(
                    UrlBroadBroadConstants.TRANSACTION_PAGE,
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
    suspend fun getWithdrawInfoList(pageNo: Int =1) :SingleLiveBroadData<NetworkListBroadResult<OrderInfoBean>>{
        val safeCenterLiveData = SingleLiveBroadData<NetworkListBroadResult<OrderInfoBean>>()
        val params = hashMapOf("pageNo" to pageNo,"pageSize" to CommonBroadConfig.pageSize)
        withContext(Dispatchers.IO) {
            try {
                NetworkBroadRepository.postEncryptedList2<OrderInfoBean>(
                    UrlBroadBroadConstants.WITHDRAW_PAGE,
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
    suspend fun goTransactionOrderDetail(id: String) :SingleLiveBroadData<OrderInfoBean>{
        val mLiveData = SingleLiveBroadData<OrderInfoBean>()
        val params = hashMapOf("id" to id)
        withContext(Dispatchers.IO) {
            try {
                NetworkBroadRepository.postEncrypted2(
                    UrlBroadBroadConstants.TRANSACTION_ORDER_DETAIL,
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
    suspend fun goWithdrawOrderDetail(id: String) :SingleLiveBroadData<OrderInfoBean>{
        val mLiveData = SingleLiveBroadData<OrderInfoBean>()
        val params = hashMapOf("id" to id)
        withContext(Dispatchers.IO) {
            try {
                NetworkBroadRepository.postEncrypted2(
                    UrlBroadBroadConstants.WITHDRAW_ORDER_DETAIL,
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
    suspend fun goSecurityCenter() :SingleLiveBroadData<SafeCenterBroadBean>{
        val safeCenterLiveData = SingleLiveBroadData<SafeCenterBroadBean>()
        val params = hashMapOf("code" to "")
        withContext(Dispatchers.IO) {
            try {
                NetworkBroadRepository.postEncrypted2<SafeCenterBroadBean>(
                    UrlBroadBroadConstants.SECURITY_CENTER,
                    SafeCenterBroadBean::class.java,
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
    suspend fun checkSecurityFirst() :SingleLiveBroadData<ResultBooBean>{
        val mLiveData = SingleLiveBroadData<ResultBooBean>()
        val params = hashMapOf("code" to "")
        withContext(Dispatchers.IO) {
            try {
                NetworkBroadRepository.postEncrypted2(
                    UrlBroadBroadConstants.IS_SECURITY_FIRST,
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