package com.cboda.intelligent.viewmodel

import android.text.TextUtils
import androidx.lifecycle.viewModelScope
import com.cboda.baselibrary.network.repository.NetworkListBroadResult
import com.cboda.baselibrary.network.repository.NetworkBroadRepository
import com.cboda.baselibrary.network.result.onError
import com.cboda.baselibrary.network.result.onFailure
import com.cboda.baselibrary.network.result.onSuccess
import com.cboda.baselibrary.utils.CompressionBroadUtil.videoCompress
import com.cboda.baselibrary.utils.eLogD
import com.cboda.baselibrary.utils.toLog
import com.cboda.intelligent.base.BaseViewBroadModel
import com.cboda.intelligent.fragment.AiAllFragment.Companion.VIDEO_TYPE
import com.cboda.intelligent.livedata.SingleLiveBroadData
import com.cboda.intelligent.bean.AiCheckResultBroadBean
import com.cboda.intelligent.bean.AiResultListBean
import com.cboda.intelligent.configuration.CommonBroadConfig
import com.cboda.intelligent.utils.UrlBroadBroadConstants
import com.cboda.intelligent.utils.dismissLoading
import com.cboda.intelligent.utils.showLoading
import com.cboda.intelligent.utils.showToastMain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class CheckAiViewModel : BaseViewBroadModel() {


    private val tAG = "CheckAiViewModelClass"
    private fun postFile(filePath: String, callBack: (String?, String?) -> Unit = { _, _ -> }) {
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
                        val fileUrl = data?.get("url")
                        callBack.invoke(fileUrl, null)
                    }.onFailure { code, message ->
                        eLogD(tAG, "postFile_Failure:$code ,errorMsg:$message")
                        callBack.invoke(null, message)
                    }.onError {
                        it.toLog(tAG)
                        callBack.invoke(null, null)
                    }
                } catch (e: Exception) {
                    e.toLog(tAG)
                    callBack.invoke(null, null)
                }
            }
        }
    }


    private fun compressVideo(
        postFilePathBefore: ArrayList<String>,
        postFilePathAfter: ArrayList<String>,
        callBack: (ArrayList<String>) -> Unit
    ) {
        eLogD(tAG, "compressVideo_removeSize:${postFilePathBefore.size}")
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                eLogD(tAG, "compressVideo_removeSize2:${postFilePathAfter.size}")
                if (postFilePathBefore.isNotEmpty()) {
                    videoCompress(postFilePathBefore[0], viewModelScope) { path, _ ->
                        postFilePathAfter.add(if (!TextUtils.isEmpty(path)) path else postFilePathBefore[0])
                        postFilePathBefore.removeAt(0)
                        compressVideo(postFilePathBefore, postFilePathAfter, callBack)
                    }
                } else {
                    callBack.invoke(postFilePathAfter)
                }
            }
        }
    }

    private fun postFiles(
        riskType: String,
        resourceType: String,
        filePaths: List<String>,
        postFilePaths: ArrayList<String>,
        liveData: SingleLiveBroadData<Pair<AiCheckResultBroadBean?, String?>>
    ) {
        postFile(arrayListOf(), postFilePaths) { resultUrls, errorMsg ->
            eLogD(tAG, "postFiles2_filePaths:${filePaths.size}")
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    if (null != resultUrls && resultUrls.size == filePaths.size) {
                        eLogD(tAG, "postFiles3")
                        getAiResult(
                            riskType,
                            resourceType,
                            resultUrls.toTypedArray()
                        ) { aiCheckResultBean, errorMsg2 ->
                            liveData.postValue(Pair(aiCheckResultBean, errorMsg2))
                        }
                    } else {
                        liveData.postValue(Pair(null, errorMsg))
                    }
                }
            }
        }
    }

    fun postFiles(
        riskType: String,
        resourceType: String,
        filePaths: List<String>
    ): SingleLiveBroadData<Pair<AiCheckResultBroadBean?, String?>> {
        eLogD(tAG, "postFiles1")
        val liveData = SingleLiveBroadData<Pair<AiCheckResultBroadBean?, String?>>()
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                if (resourceType == VIDEO_TYPE) {
                    val postFilePathBefore = arrayListOf<String>()
                    postFilePathBefore.addAll(filePaths)
                    compressVideo(postFilePathBefore, arrayListOf()) { resultPath ->
                        postFiles(riskType, resourceType, filePaths, resultPath, liveData)
                    }
                } else {
                    val postFilePaths = arrayListOf<String>()
                    postFilePaths.addAll(filePaths)
                    postFiles(riskType, resourceType, filePaths, postFilePaths, liveData)
                }
            }
        }
        return liveData
    }


    private fun postFile(
        fileUrls: ArrayList<String>,
        postFilePaths: ArrayList<String>,
        callBack: (ArrayList<String>?, String?) -> Unit
    ) {
        eLogD(tAG, "postFile_removeSize:${postFilePaths.size}")
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                eLogD(tAG, "postFile_removeSize2:${fileUrls.size}")
                if (postFilePaths.isNotEmpty()) {
                    postFile(postFilePaths[0]) { fileUrl, errorMsg ->
                        fileUrl?.let {
                            fileUrls.add(it)
                            postFilePaths.removeAt(0)
                            postFile(fileUrls, postFilePaths, callBack)
                        } ?: let {
                            callBack.invoke(null, errorMsg)
                        }
                    }
                } else {
                    callBack.invoke(fileUrls, null)
                }
            }
        }
    }


    private fun getAiResult(
        riskType: String,
        resourceType: String,
        urls: Array<String>,
        callBack: (AiCheckResultBroadBean?, String?) -> Unit
    ) {
        val fileNames = urls.map { it.substringAfterLast('/') }.toTypedArray()
        val params =
            hashMapOf("resourceType" to resourceType, "urls" to urls, "fileNames" to fileNames)
        if (!TextUtils.isEmpty(riskType)) {
            params["riskType"] = riskType
        }
        viewModelScope.launch {
            eLogD(tAG, "getAiResult1")
            withContext(Dispatchers.IO) {
                try {
                    NetworkBroadRepository.postEncrypted2(
                        UrlBroadBroadConstants.AI_RESULT,
                        AiCheckResultBroadBean::class.java,
                        params
                    ).onSuccess { it ->
                        eLogD(tAG, "getAiResult2")
                        callBack.invoke(it, null)
                    }.onFailure { code, message ->
//                        检测失败，请稍后重试
                        eLogD(tAG, "getAiResult_Failure:$code ,errorMsg:$message")
                        callBack.invoke(null, message)
                    }.onError {
                        it.toLog(tAG)
//                        showToastMain("测试AI网络异常！")
                        callBack.invoke(null, null)
                    }

                } catch (e: Exception) {
                    e.toLog(tAG)
                    callBack.invoke(null, null)
                }
            }
        }
    }


    /**
     * ai详情
     */
    suspend fun goAiResultInfo(id: Int): SingleLiveBroadData<AiCheckResultBroadBean> {
        showLoading()
        val safeCenterLiveData = SingleLiveBroadData<AiCheckResultBroadBean>()
        val params = hashMapOf("id" to id)
        withContext(Dispatchers.IO) {
            try {
                NetworkBroadRepository.postEncrypted2<AiCheckResultBroadBean>(
                    UrlBroadBroadConstants.AI_DETAIL,
                    AiCheckResultBroadBean::class.java,
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
     *ai列表
     *resourceType== 文档：document,图片：image,音频:voice,视频：video
     */
    suspend fun getAiResultInfoList(
        resourceType: String?,
        pageNo: Int = 1
    ): SingleLiveBroadData<NetworkListBroadResult<AiResultListBean>> {
        val safeCenterLiveData = SingleLiveBroadData<NetworkListBroadResult<AiResultListBean>>()
        val params = hashMapOf(
            "pageNo" to pageNo,
            "pageSize" to CommonBroadConfig.pageSize,
            "resourceType" to resourceType
        )
        withContext(Dispatchers.IO) {
            try {
                NetworkBroadRepository.postEncryptedList2<AiResultListBean>(
                    UrlBroadBroadConstants.AI_PAGE,
                    AiResultListBean::class.java,
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


}