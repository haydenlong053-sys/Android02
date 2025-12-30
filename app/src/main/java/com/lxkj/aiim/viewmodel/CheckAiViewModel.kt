package com.lxkj.aiim.viewmodel

import android.text.TextUtils
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.base.commonlibrary.network.repository.NetworkListResult
import com.base.commonlibrary.network.repository.NetworkRepository
import com.base.commonlibrary.network.result.onError
import com.base.commonlibrary.network.result.onFailure
import com.base.commonlibrary.network.result.onSuccess
import com.base.commonlibrary.utils.CompressionUtil.videoCompress
import com.base.commonlibrary.utils.eLogD
import com.base.commonlibrary.utils.toJson2
import com.base.commonlibrary.utils.toLog
import com.lxkj.aiim.R
import com.lxkj.aiim.base.BaseViewModel
import com.lxkj.aiim.fragment.AiAllFragment.Companion.VIDEO_TYPE
import com.lxkj.aiim.livedata.SingleLiveData
import com.lxkj.aiim.lj.bean.AiCheckResultBean
import com.lxkj.aiim.lj.bean.AiResultListBean
import com.lxkj.aiim.lj.configuration.CommonConfig
import com.lxkj.aiim.lj.http.UrlConstants
import com.lxkj.aiim.lj.utils.dismissLoading
import com.lxkj.aiim.lj.utils.showLoading
import com.lxkj.aiim.lj.utils.showToastMain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.collections.get


class CheckAiViewModel : BaseViewModel() {


    private val tAG = "CheckAiViewModelClass"
    private fun postFile(filePath: String, callBack: (String?, String?) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                eLogD(tAG, "postFile_filePath:$filePath")
                try {
                    val result = NetworkRepository.postFile<Map<String, String>>(
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
        liveData: SingleLiveData<Pair<AiCheckResultBean?, String?>>
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
    ): SingleLiveData<Pair<AiCheckResultBean?, String?>> {
        eLogD(tAG, "postFiles1")
        val liveData = SingleLiveData<Pair<AiCheckResultBean?, String?>>()
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
        callBack: (AiCheckResultBean?, String?) -> Unit
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
                    NetworkRepository.postEncrypted2(
                        UrlConstants.AI_RESULT,
                        AiCheckResultBean::class.java,
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
    suspend fun goAiResultInfo(id: Int): SingleLiveData<AiCheckResultBean> {
        showLoading()
        val safeCenterLiveData = SingleLiveData<AiCheckResultBean>()
        val params = hashMapOf("id" to id)
        withContext(Dispatchers.IO) {
            try {
                NetworkRepository.postEncrypted2<AiCheckResultBean>(
                    UrlConstants.AI_DETAIL,
                    AiCheckResultBean::class.java,
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
    ): SingleLiveData<NetworkListResult<AiResultListBean>> {
        val safeCenterLiveData = SingleLiveData<NetworkListResult<AiResultListBean>>()
        val params = hashMapOf(
            "pageNo" to pageNo,
            "pageSize" to CommonConfig.pageSize,
            "resourceType" to resourceType
        )
        withContext(Dispatchers.IO) {
            try {
                NetworkRepository.postEncryptedList2<AiResultListBean>(
                    UrlConstants.AI_PAGE,
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