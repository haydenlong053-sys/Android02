package com.cboda.intelligent.viewmodel

import android.text.TextUtils
import androidx.lifecycle.viewModelScope
import com.cboda.baselibrary.bean.FriendBean
import com.cboda.baselibrary.bean.PayLsOrderBean
import com.cboda.baselibrary.bean.PriceConversion
import com.cboda.baselibrary.bean.UserInfoBean
import com.cboda.baselibrary.bean.UserInfoBroadModel
import com.cboda.baselibrary.network.repository.NetworkBroadRepository
import com.cboda.baselibrary.network.result.onError
import com.cboda.baselibrary.network.result.onFailure
import com.cboda.baselibrary.network.result.onSuccess
import com.cboda.intelligent.base.BaseViewBroadModel
import com.cboda.baselibrary.livedata.SingleLiveBroadData
import com.cboda.baselibrary.network.repository.NetworkListBroadResult
import com.cboda.baselibrary.utils.MMKVBroadUtils
import com.cboda.baselibrary.utils.MMKVBroadUtils.Companion.PRICE_ODIC
import com.cboda.baselibrary.utils.RealmUtil
import com.cboda.baselibrary.utils.eLogD
import com.cboda.baselibrary.utils.toJson2
import com.cboda.intelligent.bean.ComposeItem
import com.cboda.intelligent.bean.CreditScoreBean
import com.cboda.intelligent.bean.CreditScoreFuncBean
import com.cboda.intelligent.configuration.CommonBroadConfig
import com.cboda.intelligent.utils.UrlBroadBroadConstants
import com.cboda.intelligent.model.SignInfoViewBroadModel
import com.cboda.intelligent.utils.dismissLoading
import com.cboda.intelligent.utils.showLoading
import com.cboda.intelligent.utils.showToastMain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal

class NewMirrorViewBroadModel : BaseViewBroadModel() {


    fun checkOrder(orderId: String?, haveLoading: Boolean, closeLoading: Boolean): SingleLiveBroadData<PriceConversion?> {
        val liveData = SingleLiveBroadData<PriceConversion?>()
        val params = hashMapOf<String, Any?>("orderId" to orderId)
        if (haveLoading) {
            showLoading()
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val priceConversion = PriceConversion()
                    val result = NetworkBroadRepository.postEncrypted(
                        UrlBroadBroadConstants.SELECT_ORDER,
                        PriceConversion::class.java, params
                    )
                    result.onSuccess { data ->
                        if (closeLoading) {
                            dismissLoading()
                        }
                        liveData.postValue(data)
                    }.onFailure { _, message ->
                        if (closeLoading) {
                            dismissLoading()
                        }
                        priceConversion.priceOdic = message
                        liveData.postValue(priceConversion)
//                        showToastMain(message)
                    }.onError { error ->
                        priceConversion.priceOdic = error.message ?: ""
                        if (closeLoading) {
                            dismissLoading()
                        }
//                        showToastMain("${error.message}")
                        liveData.postValue(priceConversion)
                    }
                } catch (_: Exception) {
                    if (closeLoading) {
                        dismissLoading()
                    }
                }
            }
        }
        return liveData
    }

    fun getActivatePrice(haveLoading: Boolean, closeLoading: Boolean): SingleLiveBroadData<String?> {
        val liveData = SingleLiveBroadData<String?>()
        val params = hashMapOf<String, Any?>("amount" to "1")
        if (haveLoading) {
            showLoading()
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val result = NetworkBroadRepository.postEncrypted(
                        UrlBroadBroadConstants.GET_ACTIVATE_PRICE,
                        PriceConversion::class.java, params
                    )
                    result.onSuccess { data ->
                        if (closeLoading) {
                            dismissLoading()
                        }
                        val bigDecimal = BigDecimal(data?.priceOdic)
                        val bigDecimalStr = bigDecimal.toString()
                        eLogD("getActivatePriceData_bigDecimal", bigDecimal)
                        eLogD("getActivatePriceData_bigDecimalStr", bigDecimalStr)
                        MMKVBroadUtils.saveSqlStrData(PRICE_ODIC, bigDecimalStr)
                        liveData.postValue(bigDecimalStr)
                    }.onFailure { _, _ ->
                        if (closeLoading) {
                            dismissLoading()
                        }
                        liveData.postValue(null)
                    }.onError { _ ->
                        if (closeLoading) {
                            dismissLoading()
                        }
                        liveData.postValue(null)
                    }
                } catch (_: Exception) {
                    if (closeLoading) {
                        dismissLoading()
                    }
                    liveData.postValue(null)
                }
            }
        }
        return liveData
    }

    fun createPayOrder(scene: String, haveLoading: Boolean, closeLoading: Boolean): SingleLiveBroadData<PayLsOrderBean> {
        val liveData = SingleLiveBroadData<PayLsOrderBean>()
        val params = hashMapOf<String, Any?>("scene" to scene)
        if (haveLoading) {
            showLoading()
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val result = NetworkBroadRepository.postEncrypted(
                        UrlBroadBroadConstants.CREATE_LS_PAY_ORDER,
                        PayLsOrderBean::class.java, params
                    )
                    result.onSuccess { data ->
                        if (closeLoading) {
                            dismissLoading()
                        }
                        data?.let {
                            liveData.postValue(data)
                        } ?: showToastMain("数据为空")
                    }.onFailure { _, message ->
                        if (closeLoading) {
                            dismissLoading()
                        }
                        showToastMain(message)
                    }.onError { error ->
                        if (closeLoading) {
                            dismissLoading()
                        }
                        showToastMain("${error.message}")
                    }
                } catch (_: Exception) {
                    if (closeLoading) {
                        dismissLoading()
                    }
                }
            }
        }
        return liveData
    }

    /**
     * 开通信用分
     */
    fun goOpenCredit(orderId: String?, haveLoading: Boolean, closeLoading: Boolean): SingleLiveBroadData<Boolean> {
        val mLiveData = SingleLiveBroadData<Boolean>()
        val params = hashMapOf<String, Any?>("orderId" to orderId, "payType" to "chain")
        if (haveLoading) {
            showLoading()
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    NetworkBroadRepository.postEncrypted2(
                        UrlBroadBroadConstants.CREDIT_ACTIVATECREDIT,
                        Any::class.java,
                        params
                    ).onSuccess { userInfo ->
                        if (closeLoading) {
                            dismissLoading()
                        }
                        mLiveData.postValue(true)
                    }.onFailure { _, message ->
                        if (closeLoading) {
                            dismissLoading()
                        }
                        showToastMain(message)
                    }.onError { error ->
                        if (closeLoading) {
                            dismissLoading()
                        }
                        showToastMain("${error.message}")
                    }

                } catch (e: Exception) {
                    if (closeLoading) {
                        dismissLoading()
                    }
                }
            }
        }
        return mLiveData
    }


    /**
     * 查询信用分详情
     */
    suspend fun getCreditDetails(): SingleLiveBroadData<CreditScoreBean> {
        showLoading()
        val safeCenterLiveData = SingleLiveBroadData<CreditScoreBean>()
        val params = hashMapOf<String, Any>()
        withContext(Dispatchers.IO) {
            try {
                NetworkBroadRepository.postEncrypted2<CreditScoreBean>(
                    UrlBroadBroadConstants.CREDIT_GET_CREDIT_DETAILS,
                    CreditScoreBean::class.java,
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
     * 查询信用构成列表
     */
    suspend fun getCreditConstituteList(): SingleLiveBroadData<NetworkListBroadResult<ComposeItem>> {
        val safeCenterLiveData = SingleLiveBroadData<NetworkListBroadResult<ComposeItem>>()
        val params = hashMapOf("code" to "")
        withContext(Dispatchers.IO) {
            try {
                NetworkBroadRepository.postEncryptedList2<ComposeItem>(
                    UrlBroadBroadConstants.CREDIT_CREDITCONSTITUTELIST,
                    ComposeItem::class.java,
                    params
                ).onSuccess { bean ->
                    //此处无分页只需拿list
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
     * 信用分记录分页
     */
    suspend fun getLogPageList(pageNo: Int = 1, pageSize: Int = CommonBroadConfig.pageSize_20): SingleLiveBroadData<NetworkListBroadResult<ComposeItem>> {
        val safeCenterLiveData = SingleLiveBroadData<NetworkListBroadResult<ComposeItem>>()
        val params = hashMapOf("pageNo" to pageNo, "pageSize" to pageSize)
        withContext(Dispatchers.IO) {
            try {
                NetworkBroadRepository.postEncryptedList2<ComposeItem>(
                    UrlBroadBroadConstants.CREDIT_GETLOGPAGE,
                    ComposeItem::class.java,
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

    fun getSignInfo(): SingleLiveBroadData<Boolean> {
        val liveData = SingleLiveBroadData<Boolean>()
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val result = NetworkBroadRepository.postEncrypted(
                        UrlBroadBroadConstants.SIGN_INFO,
                        SignInfoViewBroadModel::class.java, HashMap<String, Any>()
                    )
                    result.onSuccess { data ->
                        liveData.postValue(1 == data?.isSign)
                    }.onFailure { _, _ ->
                        liveData.postValue(false)
                    }.onError { _ ->
                        liveData.postValue(false)
                    }
                } catch (_: Exception) {
                    liveData.postValue(false)
                }
            }
        }
        return liveData
    }

    fun getUserInfo(): SingleLiveBroadData<UserInfoBean?> {
        val liveData = SingleLiveBroadData<UserInfoBean?>()
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val result = NetworkBroadRepository.postEncrypted(
                        UrlBroadBroadConstants.GET_USER_INFO,
                        UserInfoBean::class.java, HashMap<String, Any>()
                    )
                    result.onSuccess { data ->
                        RealmUtil.addUserData(data)
                        eLogD("NewMirrorViewBroadModelClass", "data:${toJson2(data)}")
                        liveData.postValue(data)
                    }.onFailure { _, _ ->
                        eLogD("NewMirrorViewBroadModelClass", "getUserInfo onFailure")
                        liveData.postValue(null)
                    }.onError { _ ->
                        eLogD("NewMirrorViewBroadModelClass", "getUserInfo onError")
                        liveData.postValue(null)
                    }
                } catch (_: Exception) {
                    eLogD("NewMirrorViewBroadModelClass", "getUserInfo Exception")
                    liveData.postValue(null)
                }
            }
        }
        return liveData
    }

    /**
     * 查询信用分分数说明
     */
    suspend fun getCreditScoreFunc(): SingleLiveBroadData<CreditScoreFuncBean> {
        showLoading()
        val safeCenterLiveData = SingleLiveBroadData<CreditScoreFuncBean>()
        val params = hashMapOf<String, Any>()
        withContext(Dispatchers.IO) {
            try {
                NetworkBroadRepository.postEncrypted2<CreditScoreFuncBean>(
                    UrlBroadBroadConstants.CREDIT_GET_SCORE_DESC,
                    CreditScoreFuncBean::class.java,
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

}