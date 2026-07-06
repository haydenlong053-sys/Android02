package com.cboda.intelligent.viewmodel

import android.content.Context
import android.text.TextUtils
import androidx.lifecycle.viewModelScope
import com.cboda.baselibrary.bean.CreateGroupData
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
import com.cboda.baselibrary.toast.ToastBroadUtils
import com.cboda.baselibrary.utils.MMKVBroadUtils
import com.cboda.baselibrary.utils.MMKVBroadUtils.Companion.PRICE_ODIC
import com.cboda.baselibrary.utils.RealmUtil
import com.cboda.baselibrary.utils.anyToInt
import com.cboda.baselibrary.utils.eLogD
import com.cboda.baselibrary.utils.openH5InImToken
import com.cboda.baselibrary.utils.openH5InMetaMask
import com.cboda.baselibrary.utils.openH5InTokenPocket
import com.cboda.baselibrary.utils.toJson2
import com.cboda.chatui.quickmeet.bean.CallData
import com.cboda.intelligent.bean.ComposeItem
import com.cboda.intelligent.bean.CreditScoreBean
import com.cboda.intelligent.bean.CreditScoreFuncBean
import com.cboda.intelligent.bean.PaymentMethodBroadBean
import com.cboda.intelligent.bean.PaymentMethodDefaults.IM_TOKEN
import com.cboda.intelligent.bean.PaymentMethodDefaults.META_MASK
import com.cboda.intelligent.bean.PaymentMethodDefaults.TP
import com.cboda.intelligent.configuration.CommonBroadConfig
import com.cboda.intelligent.dialog.pop.PaymentMethodPopup
import com.cboda.intelligent.utils.UrlBroadBroadConstants
import com.cboda.intelligent.model.SignInfoViewBroadModel
import com.cboda.intelligent.utils.dismissLoading
import com.cboda.intelligent.utils.showLoading
import com.cboda.intelligent.utils.showToastMain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal

open class NewMirrorViewBroadModel : BaseViewBroadModel() {


    fun checkOrder(orderId: String?, haveLoading: Boolean, closeLoading: Boolean, callBack: (PriceConversion?) -> Unit = {}): SingleLiveBroadData<PriceConversion?> {
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
                        callBack.invoke(data)
                        liveData.postValue(data)
                    }.onFailure { _, message ->
                        if (closeLoading) {
                            dismissLoading()
                        }
                        priceConversion.priceOdic = message
                        callBack.invoke(priceConversion)
                        liveData.postValue(priceConversion)
//                        showToastMain(message)
                    }.onError { error ->
                        priceConversion.priceOdic = error.message ?: ""
                        if (closeLoading) {
                            dismissLoading()
                        }
//                        showToastMain("${error.message}")
                        callBack.invoke(priceConversion)
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


    fun openWalletPay(data: PayLsOrderBean?, paymentMethod: PaymentMethodBroadBean, context: Context?) {
        if (data == null) {
            ToastBroadUtils.show("订单数据为空")
            return
        }
        eLogD("openWalletPay_data", toJson2(data))
        val priceOdic = BigDecimal(data.priceOdic)
        val chainId = anyToInt(data.chainId)
        val h5Url = "https://pay.web4x.io/pay?amountDisplay=$priceOdic" +
                "&chainId=$chainId" +
                "&orderId=${data.orderId}" +
                "&tokenSymbol=ODIC&paymentType=${data.paymentType}"
        eLogD("openWalletPay_h5Url", h5Url)
        when (paymentMethod.id) {
            IM_TOKEN -> openH5InImToken(h5Url, context)
            TP -> openH5InTokenPocket(h5Url, context, chainId)
            META_MASK -> openH5InMetaMask(h5Url, context)
        }
    }


    fun openMember(orderId: String?, haveLoading: Boolean, closeLoading: Boolean, callBack: (Boolean) -> Unit) {
        val params = hashMapOf<String, Any?>("payType" to "chain", "orderId" to orderId)
        if (haveLoading) {
            showLoading()
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val result = NetworkBroadRepository.postEncrypted(
                        UrlBroadBroadConstants.OPEN_MEMBER,
                        Any::class.java, params
                    )
                    result.onSuccess {
                        if (closeLoading) {
                            dismissLoading()
                        }
                        callBack.invoke(true)
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
    }

    fun createYsGroupPayOrder(callData: CallData, haveLoading: Boolean, closeLoading: Boolean, callBack: (PayLsOrderBean) -> Unit) {
        val createGroupData = CreateGroupData()
        createGroupData.groupIdNo = callData.quickDetailMeetBean?.groupIdNo ?: ""
        createGroupData.durationConfigId = callData.quickDetailMeetBean?.durationConfigId?.toString() ?: ""
        createGroupData.userTierConfigId = callData.quickDetailMeetBean?.userTierConfigId?.toString() ?: ""
        val params = hashMapOf<String, Any?>("scene" to "meeting_extend", "bizParams" to createGroupData)
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
                            callBack.invoke(data)
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
    }

    fun createPayOrder(scene: String, haveLoading: Boolean, closeLoading: Boolean, callBack: (PayLsOrderBean) -> Unit = {}): SingleLiveBroadData<PayLsOrderBean> {
        val liveData = SingleLiveBroadData<PayLsOrderBean>()
        val params = if (scene == "vip_open") {
            val createGroupData = CreateGroupData()
            createGroupData.planId = "1"
            hashMapOf<String, Any?>("scene" to scene, "bizParams" to createGroupData)
        } else {
            hashMapOf<String, Any?>("scene" to scene)
        }
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
                            callBack.invoke(data)
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