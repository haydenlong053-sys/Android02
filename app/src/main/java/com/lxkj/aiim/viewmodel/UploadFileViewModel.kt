package com.lxkj.aiim.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.base.commonlibrary.BuildConfig
import com.lxkj.aiim.bean.UrlBean
import com.lxkj.aiim.bean.UrlsBean
import com.lxkj.aiim.utils.AES
import com.lxkj.aiim.utils.AppUtil
import com.base.commonlibrary.bean.Constant
import com.base.commonlibrary.utils.HttpsConfig
import com.lxkj.aiim.utils.JsonUtils
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
class UploadFileViewModel : ViewModel() {
    private var liveData: MutableLiveData<UrlBean>? = null
    override fun onCleared() {
        super.onCleared()
        OkGo.getInstance().cancelTag(this)
        liveData = null
    }

    fun cancel(){
        liveData = null
    }

    fun getLiveData(): MutableLiveData<UrlBean>? {
        if (liveData == null) {
            liveData = MutableLiveData()
        }
        return liveData
    }

    fun upload(file: File) {
        OkGo.post<String>(HttpsConfig.appBaseUrl + "/uploadFile")?.tag(this)
            ?.params("file", file)?.isMultipart(true)
            ?.execute(object : StringCallback() {
                override fun onSuccess(response: Response<String>?) {
                    var body = response?.body()
                    body = AES.decrypt(body)
                    val bean = JsonUtils.getSuccessJsonBean(body)
                    bean?.code?.apply {
                        if (this == Constant.RESULT_OK&& bean.success) {
                            val uploadResultBean = JsonUtils.getUrlBean(body)
                            uploadResultBean?.url?.apply {
                                liveData?.value = uploadResultBean
                            }

                        } else {
                            val urlBean = UrlBean()
                            urlBean.erroToaastInfo = bean.msg
                            liveData?.value = urlBean
                        }
                    }
                }

                override fun uploadProgress(progress: Progress?) {
                    super.uploadProgress(progress)
                    AppUtil.debug("OKGO上传进度：：", " ${progress?.currentSize} ${progress?.totalSize}")
                }

            })

    }

    fun uploadList(files: List<File>) {
        OkGo.post<String>(HttpsConfig.appBaseUrl + "/uploadFiles")?.tag(this)
            ?.addFileParams("files", files)?.isMultipart(true)
            ?.execute(object : StringCallback() {
                override fun onSuccess(response: Response<String>?) {
                    var body = response?.body()
                    body = AES.decrypt(body)
                    val bean = JsonUtils.getSuccessJsonBean(body)
                    bean?.code?.apply {
                        if (this == Constant.RESULT_OK&& bean.success) {
                            val uploadResultBean = JsonUtils.getUrlBean(body)
                            uploadResultBean?.url?.apply {
                                liveData?.value = uploadResultBean
                            }

                        } else {
                            val urlBean = UrlBean()
                            urlBean.erroToaastInfo = bean.msg
                            liveData?.value = urlBean
                        }
                    }
                }

                override fun uploadProgress(progress: Progress?) {
                    super.uploadProgress(progress)
                    AppUtil.debug("OKGO上传多个文件进度：：", " ${progress?.currentSize} ${progress?.totalSize}")
                }

            })

    }


    suspend fun uploadSyn(file: File):UrlBean? {
        return suspendCoroutine{
            OkGo.post<String>(HttpsConfig.appBaseUrl + "/uploadFile")?.tag(this)
                ?.params("file", file)?.isMultipart(true)
                ?.execute(object : StringCallback() {
                    override fun onSuccess(response: Response<String>?) {
                        var body = response?.body()
                        body = AES.decrypt(body)
                        val bean = JsonUtils.getSuccessJsonBean(body)
                        bean?.code?.apply {
                            if (this == Constant.RESULT_OK&& bean.success) {
                                val uploadResultBean = JsonUtils.getUrlBean(body)
                                it.resume(uploadResultBean)
                            } else {
                                val erro = UrlBean()
                                erro.erroToaastInfo = bean.msg
                                it.resume(erro)
                            }

                        }
                    }

                    override fun uploadProgress(progress: Progress?) {
                        super.uploadProgress(progress)
                        AppUtil.debug("OKGO上传进度：：", " ${progress?.currentSize} ${progress?.totalSize}")
                    }

                })
        }


    }

    suspend fun uploadListSyn(files: List<File>) : UrlsBean?{
        return suspendCoroutine{
            OkGo.post<String>(HttpsConfig.appBaseUrl + "/uploadFiles")?.tag(this)
                ?.addFileParams("files", files)?.isMultipart(true)
                ?.execute(object : StringCallback() {
                    override fun onSuccess(response: Response<String>?) {
                        var body = response?.body()
                        body = AES.decrypt(body)
                        val bean = JsonUtils.getSuccessJsonBean(body)
                        bean?.code?.apply {
                            if (this == Constant.RESULT_OK&& bean.success) {
                                val uploadResultBean = JsonUtils.getUrlsBean(body)
                                uploadResultBean?.urls?.apply {
                                    it.resume(uploadResultBean)
                                }
                                //test
//                                val uploadResultBean = UrlsBean()
//                                val list = ArrayList<String>()
//                                list.add(Constant.TEST_IMAGE)
//                                list.add(Constant.TEST_IMAGE1)
//                                uploadResultBean.urls = list
//                                it.resume(uploadResultBean)

                            } else {
                                val erro = UrlsBean()
                                erro.erroToaastInfo = bean.msg
                                it.resume(erro)
                            }
                        }
                    }

                    override fun uploadProgress(progress: Progress?) {
                        super.uploadProgress(progress)
                        AppUtil.debug("OKGO上传多个文件进度：：", " ${progress?.currentSize} ${progress?.totalSize}")
                    }

                })
        }


    }

}