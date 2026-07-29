package com.cboda.intelligent.viewmodel

import androidx.lifecycle.viewModelScope
import com.cboda.baselibrary.base.BaseViewModel
import com.cboda.baselibrary.livedata.SingleLiveBroadData
import com.cboda.baselibrary.network.repository.NetworkBroadRepository
import com.cboda.baselibrary.network.result.onError
import com.cboda.baselibrary.network.result.onFailure
import com.cboda.baselibrary.network.result.onSuccess
import com.cboda.baselibrary.utils.eLogD
import com.cboda.baselibrary.utils.toJson2
import com.cboda.baselibrary.utils.toLog
import com.cboda.intelligent.bean.VersionUpdateBean
import com.cboda.intelligent.net.VersionUpdateUrlConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 版本更新，写法对齐 [com.cboda.chatui.quickmeet.vm.QuickMeetViewBroadModel.checkPayMeet]。
 */
class VersionUpdateViewBroadModel : BaseViewModel() {

    fun versionUpdate(): SingleLiveBroadData<VersionUpdateBean> {
        val liveData = SingleLiveBroadData<VersionUpdateBean>()
        val params = hashMapOf<String, Any?>()
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val result = NetworkBroadRepository.postEncryptedFlat2(
                        VersionUpdateUrlConstants.VERSION_UPDATE,
                        VersionUpdateBean::class.java,
                        params
                    )
                    result.onSuccess { data ->
                        eLogD("versionUpdate", toJson2(data))
                        data?.let { liveData.postValue(it) }
                            ?: eLogD("versionUpdate", "onSuccess_data=null")
                    }.onFailure { _, message ->
                        eLogD("versionUpdate", "onFailure_$message")
                    }.onError { error ->
                        eLogD("versionUpdate", "onError_${error?.message}")
                    }
                } catch (e: Exception) {
                    e.toLog("versionUpdate_onException")
                }
            }
        }
        return liveData
    }
}
