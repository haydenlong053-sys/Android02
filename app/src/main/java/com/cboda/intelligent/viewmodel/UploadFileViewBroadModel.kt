package com.cboda.intelligent.viewmodel

import androidx.lifecycle.MutableLiveData
import com.cboda.baselibrary.base.BaseViewModel
import com.cboda.baselibrary.bean.BroadConstant
import com.cboda.intelligent.bean.UrlBroadBean
import com.cboda.intelligent.bean.UrlsBroadBean
import com.cboda.intelligent.net.BroadUrl
import com.cboda.intelligent.utils.AppBroadUtils
import com.cboda.intelligent.utils.JsonBroadUtils
import com.cboda.intelligent.utils.OkHttpBroadHelper
import com.freddy.im.util.FileHashUtil
import com.lzy.okgo.OkGo
import com.lzy.okgo.callback.StringCallback
import com.lzy.okgo.model.Progress
import com.lzy.okgo.model.Response
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * 简述： <功能描述>
 * Created by ysh on 2023/4/1.
 */
class UploadFileViewBroadModel : BaseViewModel() {
    private var liveData: MutableLiveData<UrlBroadBean>? = null
    override fun onCleared() {
        super.onCleared()
        OkGo.getInstance().cancelTag(this)
        liveData = null
    }

    fun cancel(){
        liveData = null
    }

    fun getLiveData(): MutableLiveData<UrlBroadBean>? {
        if (liveData == null) {
            liveData = MutableLiveData()
        }
        return liveData
    }

    fun upload(file: File) {
        OkGo.post<String>(BroadUrl.uploadFile())?.tag(this)
            ?.upRequestBody(buildFilehubBody(listOf(file), batch = false))
            ?.execute(object : StringCallback() {
                override fun onSuccess(response: Response<String>?) {
                    val body = flattenFilehub(response?.body(), BroadUrl.uploadFile())
                    val bean = JsonBroadUtils.getSuccessJsonBean(body)
                    bean?.code?.apply {
                        if (this == BroadConstant.RESULT_OK && bean.success) {
                            val uploadResultBean = JsonBroadUtils.getUrlBean(body)
                            uploadResultBean?.url?.apply {
                                liveData?.value = uploadResultBean
                            }

                        } else {
                            val urlBean = UrlBroadBean()
                            urlBean.erroToaastInfo = bean.msg
                            liveData?.value = urlBean
                        }
                    }
                }

                override fun uploadProgress(progress: Progress?) {
                    super.uploadProgress(progress)
                    AppBroadUtils.debug("OKGO上传进度：：", " ${progress?.currentSize} ${progress?.totalSize}")
                }

            })

    }

    suspend fun uploadSyn(file: File):UrlBroadBean? {
        return suspendCoroutine{
            OkGo.post<String>(BroadUrl.uploadFile())?.tag(this)
                ?.upRequestBody(buildFilehubBody(listOf(file), batch = false))
                ?.execute(object : StringCallback() {
                    override fun onSuccess(response: Response<String>?) {
                        val body = flattenFilehub(response?.body(), BroadUrl.uploadFile())
                        val bean = JsonBroadUtils.getSuccessJsonBean(body)
                        bean?.code?.apply {
                            if (this == BroadConstant.RESULT_OK && bean.success) {
                                val uploadResultBean = JsonBroadUtils.getUrlBean(body)
                                it.resume(uploadResultBean)
                            } else {
                                val erro = UrlBroadBean()
                                erro.erroToaastInfo = bean.msg
                                it.resume(erro)
                            }

                        }
                    }

                    override fun uploadProgress(progress: Progress?) {
                        super.uploadProgress(progress)
                        AppBroadUtils.debug("OKGO上传进度：：", " ${progress?.currentSize} ${progress?.totalSize}")
                    }

                })
        }


    }

    suspend fun uploadListSyn(files: List<File>) : UrlsBroadBean?{
        return suspendCoroutine{
            OkGo.post<String>(BroadUrl.uploadFiles())?.tag(this)
                ?.upRequestBody(buildFilehubBody(files, batch = true))
                ?.execute(object : StringCallback() {
                    override fun onSuccess(response: Response<String>?) {
                        val body = flattenFilehub(response?.body(), BroadUrl.uploadFiles())
                        val bean = JsonBroadUtils.getSuccessJsonBean(body)
                        bean?.code?.apply {
                            if (this == BroadConstant.RESULT_OK && bean.success) {
                                val uploadResultBean = JsonBroadUtils.getUrlsBean(body)
                                uploadResultBean?.urls?.apply {
                                    it.resume(uploadResultBean)
                                }
                            } else {
                                val erro = UrlsBroadBean()
                                erro.erroToaastInfo = bean.msg
                                it.resume(erro)
                            }
                        }
                    }

                    override fun uploadProgress(progress: Progress?) {
                        super.uploadProgress(progress)
                        AppBroadUtils.debug("OKGO上传多个文件进度：：", " ${progress?.currentSize} ${progress?.totalSize}")
                    }

                })
        }


    }

    private fun flattenFilehub(raw: String?, url: String): String? {
        if (raw.isNullOrBlank()) {
            return raw
        }
        val trim = raw.trim()
        val decoded = if (trim.startsWith("{") || trim.startsWith("[")) {
            raw
        } else {
            com.cboda.baselibrary.utils.EncryptUtil.aesDecrypt(raw)
        }
        return OkHttpBroadHelper.flattenFilehubResponse(decoded, url)
    }

    private fun buildFilehubBody(files: List<File>, batch: Boolean): MultipartBody {
        val partName = if (batch) "files" else "file"
        val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
        files.forEach { file ->
            if (file.isFile) {
                builder.addFormDataPart(
                    partName,
                    file.name,
                    file.asRequestBody(guessMime(file).toMediaType())
                )
            }
        }
        val param = JSONObject()
        if (!batch && files.size == 1) {
            val hash = FileHashUtil.md5Hex(files[0])
            if (!hash.isNullOrEmpty()) {
                param.put("fileHash", hash)
            }
        }
        builder.addFormDataPart(
            "param",
            null,
            param.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        )
        return builder.build()
    }

    private fun guessMime(file: File): String {
        val name = file.name.lowercase()
        return when {
            name.endsWith(".png") -> "image/png"
            name.endsWith(".jpg") || name.endsWith(".jpeg") -> "image/jpeg"
            name.endsWith(".gif") -> "image/gif"
            name.endsWith(".webp") -> "image/webp"
            name.endsWith(".mp4") -> "video/mp4"
            name.endsWith(".mov") -> "video/quicktime"
            name.endsWith(".pdf") -> "application/pdf"
            else -> "application/octet-stream"
        }
    }

}
