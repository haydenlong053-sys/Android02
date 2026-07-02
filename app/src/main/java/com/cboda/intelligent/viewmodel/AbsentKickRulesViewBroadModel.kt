package com.cboda.intelligent.viewmodel

import com.cboda.baselibrary.base.BaseViewModel
import com.cboda.baselibrary.livedata.SingleLiveBroadData
import com.cboda.baselibrary.network.repository.NetworkBroadRepository
import com.cboda.baselibrary.network.result.onError
import com.cboda.baselibrary.network.result.onFailure
import com.cboda.baselibrary.network.result.onSuccess
import com.cboda.intelligent.bean.AbsentKickRulesBean
import com.cboda.intelligent.utils.UrlBroadBroadConstants
import com.cboda.intelligent.utils.dismissLoading
import com.cboda.intelligent.utils.showLoading
import com.cboda.intelligent.utils.showToastMain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AbsentKickRulesViewBroadModel : BaseViewModel() {

    suspend fun loadRules(): SingleLiveBroadData<AbsentKickRulesBean> {
        showLoading()
        val liveData = SingleLiveBroadData<AbsentKickRulesBean>()
        withContext(Dispatchers.IO) {
            try {
                NetworkBroadRepository.postEncrypted2(
                    UrlBroadBroadConstants.ABSENT_KICK_RULES,
                    AbsentKickRulesBean::class.java,
                    hashMapOf<String, Any>()
                ).onSuccess { data ->
                    dismissLoading()
                    data?.let { liveData.postValue(it) }
                }.onFailure { _, message ->
                    dismissLoading()
                    showToastMain(message)
                }.onError {
                    dismissLoading()
                }
            } catch (e: Exception) {
                dismissLoading()
            }
        }
        return liveData
    }
}
