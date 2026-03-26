package com.cboda.intelligent.viewmodel

import androidx.lifecycle.MutableLiveData
import com.cboda.baselibrary.base.BaseViewModel
import com.cboda.intelligent.bean.UrlBroadBean
import com.cboda.intelligent.bean.UrlsBroadBean
import com.cboda.intelligent.utils.AppBroadUtils
import com.cboda.baselibrary.bean.BroadConstant
import com.cboda.baselibrary.utils.EncryptUtil
import com.cboda.baselibrary.utils.HttpsBroadConfig
import com.cboda.intelligent.utils.JsonBroadUtils
import com.lzy.okgo.OkGo
import com.lzy.okgo.callback.StringCallback
import com.lzy.okgo.model.Progress
import com.lzy.okgo.model.Response
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
        OkGo.post<String>(HttpsBroadConfig.appBaseUrl + "/uploadFile")?.tag(this)
            ?.params("file", file)?.isMultipart(true)
            ?.execute(object : StringCallback() {
                override fun onSuccess(response: Response<String>?) {
                    var body = response?.body()
                    body = EncryptUtil.aesDecrypt(body)
                    val bean = JsonBroadUtils.getSuccessJsonBean(body)
                    bean?.code?.apply {
                        if (this == BroadConstant.RESULT_OK&& bean.success) {
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

    fun uploadList(files: List<File>) {
        OkGo.post<String>(HttpsBroadConfig.appBaseUrl + "/uploadFiles")?.tag(this)
            ?.addFileParams("files", files)?.isMultipart(true)
            ?.execute(object : StringCallback() {
                override fun onSuccess(response: Response<String>?) {
                    var body = response?.body()
                    body = EncryptUtil.aesDecrypt(body)
                    val bean = JsonBroadUtils.getSuccessJsonBean(body)
                    bean?.code?.apply {
                        if (this == BroadConstant.RESULT_OK&& bean.success) {
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
                    AppBroadUtils.debug("OKGO上传多个文件进度：：", " ${progress?.currentSize} ${progress?.totalSize}")
                }

            })

    }


    suspend fun uploadSyn(file: File):UrlBroadBean? {
        return suspendCoroutine{
            OkGo.post<String>(HttpsBroadConfig.appBaseUrl + "/uploadFile")?.tag(this)
                ?.params("file", file)?.isMultipart(true)
                ?.execute(object : StringCallback() {
                    override fun onSuccess(response: Response<String>?) {
                        var body = response?.body()
                        body = EncryptUtil.aesDecrypt(body)
                        val bean = JsonBroadUtils.getSuccessJsonBean(body)
                        bean?.code?.apply {
                            if (this == BroadConstant.RESULT_OK&& bean.success) {
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
            OkGo.post<String>(HttpsBroadConfig.appBaseUrl + "/uploadFiles")?.tag(this)
                ?.addFileParams("files", files)?.isMultipart(true)
                ?.execute(object : StringCallback() {
                    override fun onSuccess(response: Response<String>?) {
                        var body = response?.body()
                        body = EncryptUtil.aesDecrypt(body)
                        val bean = JsonBroadUtils.getSuccessJsonBean(body)
                        bean?.code?.apply {
                            if (this == BroadConstant.RESULT_OK&& bean.success) {
                                val uploadResultBean = JsonBroadUtils.getUrlsBean(body)
                                uploadResultBean?.urls?.apply {
                                    it.resume(uploadResultBean)
                                }
                                //test
//                                val uploadResultBean = UrlsBroadBean()
//                                val list = ArrayList<String>()
//                                list.add(BroadConstant.TEST_IMAGE)
//                                list.add(BroadConstant.TEST_IMAGE1)
//                                uploadResultBean.urls = list
//                                it.resume(uploadResultBean)

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

}