package com.cboda.intelligent.viewmodel

import android.text.TextUtils
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.FileUtils
import com.cboda.baselibrary.base.BaseViewModel
import com.cboda.baselibrary.network.repository.NetworkBroadRepository
import com.cboda.baselibrary.network.result.onError
import com.cboda.baselibrary.network.result.onFailure
import com.cboda.baselibrary.network.result.onSuccess
import com.cboda.baselibrary.utils.eLogD
import com.cboda.baselibrary.utils.getAppContext
import com.cboda.baselibrary.utils.getIsDebug
import com.cboda.baselibrary.utils.toLog
import com.cboda.intelligent.base.BaseViewBroadModel
import com.cboda.intelligent.utils.copyToClipboard
import com.cboda.intelligent.utils.dismissLoading
import com.cboda.intelligent.utils.showLoading
import com.cboda.intelligent.utils.showToastMain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MineViewModel : BaseViewBroadModel() {
    private val tAG = "MineViewModelClass"

    fun postFile(filePath: String, callBack: (String?, String?) -> Unit = { _, _ -> }) {
        if (FileUtils.isFileExists(filePath)) {
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
                            if (!TextUtils.isEmpty(fileUrl)) {
                                eLogD("NewMineBroadFragmentClass", "netUrl:$fileUrl")
                                if (getIsDebug()) {
                                    copyToClipboard(getAppContext(), fileUrl, false)
                                }
                                showToastMain(com.cboda.baselibrary.R.string.post_log_success)
                            }
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
        } else {
            showToastMain("日志文件不存在")
        }

    }
}