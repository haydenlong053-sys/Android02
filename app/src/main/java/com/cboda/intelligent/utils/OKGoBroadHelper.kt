package com.cboda.intelligent.utils

import android.content.Context
import com.cboda.baselibrary.toast.ToastBroadUtils
import com.google.gson.Gson
import com.cboda.intelligent.bean.AddMeetingTimeBroadBean
import com.cboda.baselibrary.bean.BaseBroadResponse
import com.cboda.intelligent.bean.BaseBroadResponse2
import com.cboda.baselibrary.bean.CommonDataListBroadBean
import com.cboda.baselibrary.bean.BroadConstant
import com.cboda.baselibrary.utils.eLogD
import com.cboda.baselibrary.bean.CommonResultBroadBean
import com.cboda.baselibrary.utils.HttpsBroadConfig
import com.cboda.intelligent.bean.CreateRoomBroadBean
import com.cboda.intelligent.bean.GroupMeetingBroadBean
import com.cboda.intelligent.bean.KickListBroadBean
import com.cboda.intelligent.bean.MeetingDetailBroadBean
import com.cboda.intelligent.bean.NoResultBroadBean
import com.cboda.intelligent.bean.UniqueRoomBroadBean
import com.cboda.intelligent.bean.UpdateRoomBroadBean
import com.cboda.intelligent.utils.AppBroadUtils.Companion.exit
import com.cboda.baselibrary.widge.LoadingBroadDialog
import com.lzy.okgo.OkGo
import com.lzy.okgo.callback.StringCallback
import com.lzy.okgo.model.Response
import org.json.JSONObject

/**
 * 简述： <功能描述>
 * Created by ysh on 2024/2/26.
 */
class OKGoBroadHelper {

    private var dialogHelper: LoadingBroadDialog? = null

    fun post(
        context: Context?,
        url: String,
        json: String,
        bean: BaseBroadResponse? = null,
        showToast: Boolean = true,
        showLoading: Boolean = true,
        canDis: Boolean = true,
        callBack: RequestCallBack? = null,
        hasBasUrl: Boolean = false
    ) {
        if (showLoading) showLoading(context, canDis)

//        eLogD("OKGoHelperClass接口请求", " url-->${url} 加密前--> $json ")
        val map = mapOf(
            "encrData" to AEBroadS.encrypt(json),
        )

        OkGo.post<String>(if (hasBasUrl) url else (HttpsBroadConfig.appBaseUrl + url))
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
//                                " url-->${url}  加密后--> ${AEBroadS.decrypt(body)} "
//                            )
                            body = AEBroadS.decrypt(body)

                            val baseBean = JsonBroadUtils.getSuccessJsonBean(body)
                            baseBean?.code?.apply {
                                if (this == BroadConstant.RESULT_OK && baseBean.success) {
                                    when (bean) {
                                        is NoResultBroadBean -> {
                                            val noResultBean = JsonBroadUtils.getNoResultBean(body)
                                            callBack?.onSuccess(noResultBean)
                                        }

                                        is CommonResultBroadBean -> {
                                            val commonResultBean =
                                                JsonBroadUtils.getCommonResultBean(body)
                                            callBack?.onSuccess(commonResultBean)
                                        }

                                        is CommonDataListBroadBean -> {
                                            val commonDataListBean =
                                                JsonBroadUtils.getCommonDataListBean(body)
                                            callBack?.onSuccess(commonDataListBean)
                                        }

                                        else -> {
                                            callBack?.onFail(baseBean)
                                            eLogD("OKGoHelperClass数据解析", "需新加对象--->")
                                        }
                                    }
                                } else if (this == "401" || this == "201") {
                                    ToastBroadUtils.show(baseBean.msg)
                                    exit(context)
                                } else {
                                    if (showToast) ToastBroadUtils.show(baseBean.msg)
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
                    if (showToast) ToastBroadUtils.show("网络请求异常")
                    if (showLoading) dismissLoading()
                }

            })
    }

    fun postNoAes(
        context: Context?,
        url: String,
        json: String,
        bean: BaseBroadResponse? = null,
        showToast: Boolean = true,
        showLoading: Boolean = true,
        canDis: Boolean = true,
        callBack: RequestCallBack? = null,
        hasBaseUrl: Boolean = true

    ) {
        if (showLoading) showLoading(context, canDis)

        AppBroadUtils.debug("接口请求", " url-->${url} 加密前--> $json ")

        OkGo.post<String>(
            (if (hasBaseUrl) {
                ""
            } else {
                HttpsBroadConfig.appBaseUrl
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
//                            body = AEBroadS.decrypt(body)


                            val baseBean = JsonBroadUtils.getSuccessJsonBean(body)
                            baseBean?.code?.apply {
                                if (this == "200") {
                                    when (bean) {

                                        is MeetingDetailBroadBean -> {
                                            val fromJson = Gson().fromJson(
                                                body,
                                                MeetingDetailBroadBean::class.java
                                            )
                                            callBack?.onSuccess(fromJson)
                                        }

                                        is CreateRoomBroadBean -> {
                                            val fromJson = Gson().fromJson(
                                                body,
                                                CreateRoomBroadBean::class.java
                                            )
                                            callBack?.onSuccess(fromJson)
                                        }

                                        is AddMeetingTimeBroadBean -> {
                                            val fromJson = Gson().fromJson(
                                                body,
                                                AddMeetingTimeBroadBean::class.java
                                            )
                                            callBack?.onSuccess(fromJson)
                                        }

                                        is UniqueRoomBroadBean -> {
                                            val fromJson = Gson().fromJson(
                                                body,
                                                UniqueRoomBroadBean::class.java
                                            )
                                            if (fromJson.data?.exists == 0) {

                                                callBack?.onSuccess(fromJson)
                                            } else {
                                                callBack?.onFail(fromJson)
                                            }
                                        }

                                        is UpdateRoomBroadBean -> {
                                            val fromJson = Gson().fromJson(
                                                body,
                                                UpdateRoomBroadBean::class.java
                                            )
                                            callBack?.onSuccess(fromJson)
                                        }

                                        is GroupMeetingBroadBean -> {
                                            val fromJson = Gson().fromJson(
                                                body,
                                                GroupMeetingBroadBean::class.java
                                            )
                                            callBack?.onSuccess(fromJson)
                                        }

                                        is NoResultBroadBean -> {
                                            callBack?.onSuccess(bean)
                                        }

                                        is KickListBroadBean -> {
                                            val commonDataListBean = Gson().fromJson(
                                                body,
                                                KickListBroadBean::class.java
                                            )
                                            callBack?.onSuccess(commonDataListBean)
                                        }


                                        else -> {
                                            callBack?.onFail(baseBean)
                                            AppBroadUtils.debug("数据解析", "需新加对象--->")
                                        }
                                    }
                                } else if (this == "401" || this == "201") {
                                    ToastBroadUtils.show(baseBean.msg)
                                    exit(context)
                                } else {
                                    if (showToast) ToastBroadUtils.show(baseBean.msg)
                                    callBack?.onFail(baseBean)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        AppBroadUtils.debug("解析异常2", "--->$e")
                    }

                }

                override fun onError(response: Response<String>?) {
                    super.onError(response)
                    if (showToast) ToastBroadUtils.show("网络请求异常")
                    if (showLoading) dismissLoading()
                }

            })
    }

    fun getNoAes(
        context: Context?,
        url: String,
        json: String,
        bean: BaseBroadResponse? = null,
        showToast: Boolean = true,
        showLoading: Boolean = true,
        canDis: Boolean = true,
        callBack: RequestCallBack? = null,
        hasBaseUrl: Boolean = true

    ) {
        if (showLoading) showLoading(context, canDis)

        AppBroadUtils.debug("接口请求", " url-->${url} 加密前--> $json ")
        OkGo.get<String>(
            (if (hasBaseUrl) {
                ""
            } else {
                HttpsBroadConfig.appBaseUrl
            }) + url
        )?.execute(
            object : StringCallback() {
                override fun onSuccess(response: Response<String?>?) {
                    try {
                        if (showLoading) dismissLoading()
                        if (bean != null) {
                            var body = response?.body()
                            AppBroadUtils.debug(
                                "接口返回",
                                " url-->${url}  加密后--> ${body} "
                            )
//                            body = AEBroadS.decrypt(body)


                            val baseBean = JsonBroadUtils.getSuccessJsonBean(body)
                            baseBean?.code?.apply {
                                if (this == "200") {
                                    when (bean) {

                                        is CreateRoomBroadBean -> {
                                            val fromJson = Gson().fromJson(
                                                body,
                                                CreateRoomBroadBean::class.java
                                            )
                                            callBack?.onSuccess(fromJson)
                                        }

                                        is UniqueRoomBroadBean -> {
                                            val fromJson = Gson().fromJson(
                                                body,
                                                UniqueRoomBroadBean::class.java
                                            )
                                            if (fromJson.data?.exists == 0) {

                                                callBack?.onSuccess(fromJson)
                                            } else {
                                                callBack?.onFail(fromJson)
                                            }
                                        }

                                        is UpdateRoomBroadBean -> {
                                            val fromJson = Gson().fromJson(
                                                body,
                                                UpdateRoomBroadBean::class.java
                                            )
                                            callBack?.onSuccess(fromJson)
                                        }

                                        is GroupMeetingBroadBean -> {
                                            val fromJson = Gson().fromJson(
                                                body,
                                                GroupMeetingBroadBean::class.java
                                            )
                                            callBack?.onSuccess(fromJson)
                                        }

                                        is NoResultBroadBean -> {
                                            callBack?.onSuccess(bean)
                                        }

                                        is KickListBroadBean -> {
                                            val commonDataListBean = Gson().fromJson(
                                                body,
                                                KickListBroadBean::class.java
                                            )
                                            callBack?.onSuccess(commonDataListBean)
                                        }


                                        else -> {
                                            callBack?.onFail(baseBean)
                                            AppBroadUtils.debug("数据解析", "需新加对象--->")
                                        }
                                    }
                                } else if (this == "401" || this == "201") {
                                    ToastBroadUtils.show(baseBean.msg)
                                    exit(context)
                                } else {
                                    if (showToast) ToastBroadUtils.show(baseBean.msg)
                                    callBack?.onFail(baseBean)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        AppBroadUtils.debug("解析异常2", "--->$e")
                    }
                }

                override fun onError(response: Response<String?>?) {
                    super.onError(response)
                    if (showToast) ToastBroadUtils.show("网络请求异常")
                    if (showLoading) dismissLoading()

                }

            }
        )

    }

    private fun showLoading(context: Context?, canDis: Boolean) {
        try {
            context?.apply {
                if (dialogHelper == null) {
                    dialogHelper = LoadingBroadDialog(context,"")
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
        fun onSuccess(bean: BaseBroadResponse?)
        fun onFail(bean: BaseBroadResponse?)
    }

    interface RequestCallBack2 {
        fun onSuccess(bean: BaseBroadResponse2?)
        fun onFail(bean: BaseBroadResponse2?)
    }
}