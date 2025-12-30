package com.lxkj.aiim.utils

import android.content.Context
import com.base.commonlibrary.toast.ToastUtils
import com.google.gson.Gson
import com.lxkj.aiim.bean.AddMeetingTimeBean
import com.base.commonlibrary.bean.BaseResponse
import com.lxkj.aiim.bean.BaseResponse2
import com.base.commonlibrary.bean.CommonDataListBean
import com.base.commonlibrary.bean.Constant
import com.base.commonlibrary.utils.eLogD
import com.base.commonlibrary.bean.CommonResultBean
import com.base.commonlibrary.utils.HttpsConfig
import com.lxkj.aiim.bean.CreateRoomBean
import com.lxkj.aiim.bean.GroupMeetingBean
import com.lxkj.aiim.bean.KickListBean
import com.lxkj.aiim.bean.MeetingDetailBean
import com.lxkj.aiim.bean.NoResultBean
import com.lxkj.aiim.bean.UniqueRoomBean
import com.lxkj.aiim.bean.UpdateRoomBean
import com.lxkj.aiim.utils.AppUtil.Companion.exit
import com.base.commonlibrary.widge.LoadingDialog
import com.lzy.okgo.OkGo
import com.lzy.okgo.callback.StringCallback
import com.lzy.okgo.model.Response
import org.json.JSONObject

/**
 * 简述： <功能描述>
 * Created by ysh on 2024/2/26.
 */
class OKGoHelper {

    private var dialogHelper: LoadingDialog? = null

    fun post(
        context: Context?,
        url: String,
        json: String,
        bean: BaseResponse? = null,
        showToast: Boolean = true,
        showLoading: Boolean = true,
        canDis: Boolean = true,
        callBack: RequestCallBack? = null,
        hasBasUrl: Boolean = false
    ) {
        if (showLoading) showLoading(context, canDis)

//        eLogD("OKGoHelperClass接口请求", " url-->${url} 加密前--> $json ")
        val map = mapOf(
            "encrData" to AES.encrypt(json),
        )

        OkGo.post<String>(if (hasBasUrl) url else (HttpsConfig.appBaseUrl + url))
            ?.upJson(JSONObject(map).toString())
            ?.execute(object : StringCallback() {
                override fun onSuccess(response: Response<String>?) {
                    try {
                        if (showLoading) dismissLoading()
//                        eLogD("OKGoHelperClass", "开始解析数据")
                        if (bean != null) {
                            var body = response?.body()
//                            eLogD(
//                                "OKGoHelperClass接口返回",
//                                " url-->${url}  加密后--> ${AES.decrypt(body)} "
//                            )
                            body = AES.decrypt(body)

                            val baseBean = JsonUtils.getSuccessJsonBean(body)
                            baseBean?.code?.apply {
                                if (this == Constant.RESULT_OK && baseBean.success) {
                                    when (bean) {
                                        is NoResultBean -> {
                                            val noResultBean = JsonUtils.getNoResultBean(body)
                                            callBack?.onSuccess(noResultBean)
                                        }

                                        is CommonResultBean -> {
                                            val commonResultBean =
                                                JsonUtils.getCommonResultBean(body)
                                            callBack?.onSuccess(commonResultBean)
                                        }

                                        is CommonDataListBean -> {
                                            val commonDataListBean =
                                                JsonUtils.getCommonDataListBean(body)
                                            callBack?.onSuccess(commonDataListBean)
                                        }

                                        else -> {
                                            callBack?.onFail(baseBean)
                                            eLogD("OKGoHelperClass数据解析", "需新加对象--->")
                                        }
                                    }
                                } else if (this == "401" || this == "201") {
                                    ToastUtils.show(baseBean.msg)
                                    exit(context)
                                } else {
                                    if (showToast) ToastUtils.show(baseBean.msg)
                                    callBack?.onFail(baseBean)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        eLogD("OKGoHelperClass解析异常", "--->$e")
                    }

                }

                override fun onError(response: Response<String>?) {
                    super.onError(response)
                    eLogD("OKGoHelperClass", "网络请求异常")
                    if (showToast) ToastUtils.show("网络请求异常")
                    if (showLoading) dismissLoading()
                }

            })
    }

    fun postNoAes(
        context: Context?,
        url: String,
        json: String,
        bean: BaseResponse? = null,
        showToast: Boolean = true,
        showLoading: Boolean = true,
        canDis: Boolean = true,
        callBack: RequestCallBack? = null,
        hasBaseUrl: Boolean = true

    ) {
        if (showLoading) showLoading(context, canDis)

        AppUtil.debug("接口请求", " url-->${url} 加密前--> $json ")

        OkGo.post<String>(
            (if (hasBaseUrl) {
                ""
            } else {
                HttpsConfig.appBaseUrl
            }) + url
        )
            ?.upJson(json)
            ?.execute(object : StringCallback() {
                override fun onSuccess(response: Response<String>?) {
                    try {
                        if (showLoading) dismissLoading()
                        if (bean != null) {
                            var body = response?.body()
                            eLogD(
                                "接口返回",
                                " url-->${url}  解密后--> ${body} "
                            )
//                            body = AES.decrypt(body)


                            val baseBean = JsonUtils.getSuccessJsonBean(body)
                            baseBean?.code?.apply {
                                if (this == "200") {
                                    when (bean) {

                                        is MeetingDetailBean -> {
                                            val fromJson = Gson().fromJson(
                                                body,
                                                MeetingDetailBean::class.java
                                            )
                                            callBack?.onSuccess(fromJson)
                                        }

                                        is CreateRoomBean -> {
                                            val fromJson = Gson().fromJson(
                                                body,
                                                CreateRoomBean::class.java
                                            )
                                            callBack?.onSuccess(fromJson)
                                        }

                                        is AddMeetingTimeBean -> {
                                            val fromJson = Gson().fromJson(
                                                body,
                                                AddMeetingTimeBean::class.java
                                            )
                                            callBack?.onSuccess(fromJson)
                                        }

                                        is UniqueRoomBean -> {
                                            val fromJson = Gson().fromJson(
                                                body,
                                                UniqueRoomBean::class.java
                                            )
                                            if (fromJson.data?.exists == 0) {

                                                callBack?.onSuccess(fromJson)
                                            } else {
                                                callBack?.onFail(fromJson)
                                            }
                                        }

                                        is UpdateRoomBean -> {
                                            val fromJson = Gson().fromJson(
                                                body,
                                                UpdateRoomBean::class.java
                                            )
                                            callBack?.onSuccess(fromJson)
                                        }

                                        is GroupMeetingBean -> {
                                            val fromJson = Gson().fromJson(
                                                body,
                                                GroupMeetingBean::class.java
                                            )
                                            callBack?.onSuccess(fromJson)
                                        }

                                        is NoResultBean -> {
                                            callBack?.onSuccess(bean)
                                        }

                                        is KickListBean -> {
                                            val commonDataListBean = Gson().fromJson(
                                                body,
                                                KickListBean::class.java
                                            )
                                            callBack?.onSuccess(commonDataListBean)
                                        }


                                        else -> {
                                            callBack?.onFail(baseBean)
                                            AppUtil.debug("数据解析", "需新加对象--->")
                                        }
                                    }
                                } else if (this == "401" || this == "201") {
                                    ToastUtils.show(baseBean.msg)
                                    exit(context)
                                } else {
                                    if (showToast) ToastUtils.show(baseBean.msg)
                                    callBack?.onFail(baseBean)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        AppUtil.debug("解析异常2", "--->$e")
                    }

                }

                override fun onError(response: Response<String>?) {
                    super.onError(response)
                    if (showToast) ToastUtils.show("网络请求异常")
                    if (showLoading) dismissLoading()
                }

            })
    }

    fun getNoAes(
        context: Context?,
        url: String,
        json: String,
        bean: BaseResponse? = null,
        showToast: Boolean = true,
        showLoading: Boolean = true,
        canDis: Boolean = true,
        callBack: RequestCallBack? = null,
        hasBaseUrl: Boolean = true

    ) {
        if (showLoading) showLoading(context, canDis)

        AppUtil.debug("接口请求", " url-->${url} 加密前--> $json ")
        OkGo.get<String>(
            (if (hasBaseUrl) {
                ""
            } else {
                HttpsConfig.appBaseUrl
            }) + url
        )?.execute(
            object : StringCallback() {
                override fun onSuccess(response: Response<String?>?) {
                    try {
                        if (showLoading) dismissLoading()
                        if (bean != null) {
                            var body = response?.body()
                            AppUtil.debug(
                                "接口返回",
                                " url-->${url}  加密后--> ${body} "
                            )
//                            body = AES.decrypt(body)


                            val baseBean = JsonUtils.getSuccessJsonBean(body)
                            baseBean?.code?.apply {
                                if (this == "200") {
                                    when (bean) {

                                        is CreateRoomBean -> {
                                            val fromJson = Gson().fromJson(
                                                body,
                                                CreateRoomBean::class.java
                                            )
                                            callBack?.onSuccess(fromJson)
                                        }

                                        is UniqueRoomBean -> {
                                            val fromJson = Gson().fromJson(
                                                body,
                                                UniqueRoomBean::class.java
                                            )
                                            if (fromJson.data?.exists == 0) {

                                                callBack?.onSuccess(fromJson)
                                            } else {
                                                callBack?.onFail(fromJson)
                                            }
                                        }

                                        is UpdateRoomBean -> {
                                            val fromJson = Gson().fromJson(
                                                body,
                                                UpdateRoomBean::class.java
                                            )
                                            callBack?.onSuccess(fromJson)
                                        }

                                        is GroupMeetingBean -> {
                                            val fromJson = Gson().fromJson(
                                                body,
                                                GroupMeetingBean::class.java
                                            )
                                            callBack?.onSuccess(fromJson)
                                        }

                                        is NoResultBean -> {
                                            callBack?.onSuccess(bean)
                                        }

                                        is KickListBean -> {
                                            val commonDataListBean = Gson().fromJson(
                                                body,
                                                KickListBean::class.java
                                            )
                                            callBack?.onSuccess(commonDataListBean)
                                        }


                                        else -> {
                                            callBack?.onFail(baseBean)
                                            AppUtil.debug("数据解析", "需新加对象--->")
                                        }
                                    }
                                } else if (this == "401" || this == "201") {
                                    ToastUtils.show(baseBean.msg)
                                    exit(context)
                                } else {
                                    if (showToast) ToastUtils.show(baseBean.msg)
                                    callBack?.onFail(baseBean)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        AppUtil.debug("解析异常2", "--->$e")
                    }
                }

                override fun onError(response: Response<String?>?) {
                    super.onError(response)
                    if (showToast) ToastUtils.show("网络请求异常")
                    if (showLoading) dismissLoading()

                }

            }
        )

    }

    private fun showLoading(context: Context?, canDis: Boolean) {
        try {
            context?.apply {
                if (dialogHelper == null) {
                    dialogHelper = LoadingDialog(context)
                }
                dialogHelper?.show()
                dialogHelper?.setCanceledOnTouchOutside(canDis)
                dialogHelper?.setCancelable(canDis)
            }
        } catch (e: Exception) {

        }
    }

    private fun dismissLoading() {
        dialogHelper?.dismiss()
    }


    interface RequestCallBack {
        fun onSuccess(bean: BaseResponse?)
        fun onFail(bean: BaseResponse?)
    }

    interface RequestCallBack2 {
        fun onSuccess(bean: BaseResponse2?)
        fun onFail(bean: BaseResponse2?)
    }
}