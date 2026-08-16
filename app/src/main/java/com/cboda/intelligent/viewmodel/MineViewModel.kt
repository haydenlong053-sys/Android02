package com.cboda.intelligent.viewmodel

import androidx.lifecycle.viewModelScope
import com.cboda.baselibrary.base.BaseViewModel
import com.cboda.baselibrary.network.repository.NetworkBroadRepository
import com.cboda.baselibrary.network.result.onError
import com.cboda.baselibrary.network.result.onFailure
import com.cboda.baselibrary.network.result.onSuccess
import com.cboda.baselibrary.utils.eLogD
import com.cboda.baselibrary.utils.toLog
import com.cboda.intelligent.base.BaseViewBroadModel
import com.cboda.intelligent.utils.dismissLoading
import com.cboda.intelligent.utils.showLoading
import com.cboda.intelligent.utils.showToastMain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MineViewModel : BaseViewBroadModel() {
    private val tAG = "MineViewModelClass"

    fun postFile(filePath: String, callBack: (String?, String?) -> Unit = { _, _ -> }) {
        showLoading()
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                eLogD(tAG, "postFile_filePath:$filePath")
                try {
                    val result = NetworkBroadRepository.postFile<Map<String, String>>(
                        "api/AI/file",
                        filePath
                    )
                    result.onSuccess { data ->
                        eLogD(tAG, "postFile_success:$data")
                        dismissLoading()
                        val fileUrl = data?.get("url")
                        callBack.invoke(fileUrl, null)
                    }.onFailure { code, message ->
                        eLogD(tAG, "postFile_Failure:$code ,errorMsg:$message")
                        dismissLoading()
                        callBack.invoke(null, message)
                        showToastMain("上传日志失败1：$code ,$message")
                    }.onError {
                        val msg = it.toLog(tAG)
                        dismissLoading()
                        callBack.invoke(null, null)
                        showToastMain("上传日志失败2：$msg")
                    }
                } catch (e: Exception) {
                    val msg = e.toLog(tAG)
                    dismissLoading()
                    callBack.invoke(null, null)
                    showToastMain("上传日志失败3：$msg")
                }
            }
        }
    }
}