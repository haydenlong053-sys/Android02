package com.cboda.intelligent.utils

import android.content.Context
import com.blankj.utilcode.util.GsonUtils
import com.cboda.baselibrary.toast.ToastBroadUtils
import com.cboda.intelligent.bean.AddMeetingTimeBroadBean
import com.cboda.baselibrary.bean.BaseBroadResponse
import com.cboda.intelligent.bean.BaseBroadResponse2
import com.cboda.baselibrary.bean.CommonDataListBroadBean
import com.cboda.baselibrary.bean.BroadConstant
import com.cboda.baselibrary.utils.eLogD
import com.cboda.baselibrary.bean.CommonResultBroadBean
import com.cboda.baselibrary.utils.HttpsBroadConfig
import com.cboda.baselibrary.utils.toLog
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
import org.json.JSONArray
import org.json.JSONObject

/**
 * 简述： <功能描述>
 * Created by ysh on 2024/2/26.
 */
class OKGoBroadHelper {

    private var dialogHelper: LoadingBroadDialog? = null

    /**
     * POST 明文 JSON（不加解密）。
     * 响应若为 IM 统一信封 `{errorCode,errorMsg,content}`，先展平为
     * `{code,success,msg,data/dataList}` 再按原 Bean 解析。
     *
     * @see <a href="https://d274bn8ctb3rl6.cloudfront.net/wujie/docs/%E8%B4%A6%E5%8F%B7%E4%B8%8E%E5%A5%BD%E5%8F%8B%E6%8E%A5%E5%8F%A3%E6%96%87%E6%A1%A3.html">账号与好友接口文档</a>
     */
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

        val fullUrl = if (hasBasUrl) {
            url
        } else {
            joinUrl(HttpsBroadConfig.appBaseUrl, url)
        }
        eLogD("OKGoHelperClass接口请求", " url-->$fullUrl  body--> $json ")

        OkGo.post<String>(fullUrl)
            ?.upJson(json)
            ?.execute(object : StringCallback() {
                override fun onSuccess(response: Response<String>?) {
                    try {
                        if (showLoading) dismissLoading()
                        if (bean != null) {
                            val body = normalizeImEnvelope(response?.body())
                            eLogD("OKGoHelperClass接口返回", " url-->$url  body--> $body ")

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
                                } else if (this == "401" || this == "201"
                                    || this == "106" || this == "1001007"
                                ) {
                                    ToastBroadUtils.show(baseBean.tipMessage())
                                    exit(context)
                                } else {
                                    if (showToast) ToastBroadUtils.show(baseBean.tipMessage())
                                    callBack?.onFail(baseBean)
                                }
                            } ?: run {
                                if (showToast) ToastBroadUtils.show(
                                    baseBean?.tipMessage()?.takeIf { it.isNotEmpty() } ?: "响应解析失败"
                                )
                                callBack?.onFail(baseBean)
                            }
                        }
                    } catch (e: Exception) {
                        e.toLog("OKGoHelperClass解析异常")
                        if (showLoading) dismissLoading()
                        callBack?.onFail(null)
                    }
                }

                override fun onError(response: Response<String>?) {
                    super.onError(response)
                    eLogD("OKGoHelperClass", "网络请求异常")
                    if (showToast) ToastBroadUtils.show("网络请求异常")
                    if (showLoading) dismissLoading()
                    callBack?.onFail(null)
                }
            })
    }

    /**
     * 拼接基址与相对路径，保证中间恰好一个 `/`。
     * 测试环境 appHost2 为 `https://im-test.wujie168.net`（无尾斜杠），
     * 路径为 `im/friend/duiInfo`（无前斜杠）时，直接相加会少 `/`。
     */
    private fun joinUrl(baseUrl: String, path: String): String {
        val normalizedBase = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        val normalizedPath = path.trimStart('/')
        return normalizedBase + normalizedPath
    }

    private fun normalizeImEnvelope(body: String?): String? {
        if (body.isNullOrBlank()) {
            return body
        }
        return try {
            val trim = body.trim()
            if (!trim.startsWith("{")) {
                return body
            }
            val root = JSONObject(trim)
            if (!root.has("errorCode")) {
                return body
            }
            val errorCode = root.optInt("errorCode", -1)
            val ok = errorCode == 0
            val errorMsg = root.optString("errorMsg", "")
            val out = JSONObject()
            out.put("errorCode", errorCode)
            out.put("errorMsg", errorMsg)
            out.put("code", if (ok) BroadConstant.RESULT_OK else errorCode.toString())
            out.put("success", ok)
            out.put("msg", errorMsg)
            when (val content = root.opt("content")) {
                is JSONObject -> {
                    normalizeFriendContentObject(content)
                    out.put("data", content)
                    val list = content.opt("list")
                    if (list is JSONArray) {
                        out.put("dataList", list)
                    }
                    if (content.has("count")) {
                        out.put("count", content.opt("count"))
                    }
                }
                is JSONArray -> {
                    out.put("dataList", content)
                    out.put("data", content)
                }
                JSONObject.NULL, null -> Unit
                else -> out.put("data", content)
            }
            out.toString()
        } catch (e: Exception) {
            e.toLog("OKGoHelperClass normalizeImEnvelope")
            body
        }
    }

    /**
     * duiInfo content 兼容：id→userId、bei↔remarkName、grade 字符串转数字、doll3d。
     */
    private fun normalizeFriendContentObject(content: JSONObject) {
        val userId = content.optString("userId", "")
        val id = content.optString("id", "")
        if (userId.isEmpty() && id.isNotEmpty()) {
            content.put("userId", id)
        }
        val bei = content.optString("bei", "")
        val remarkName = if (content.isNull("remarkName")) "" else content.optString("remarkName", "")
        if (bei.isEmpty() && remarkName.isNotEmpty()) {
            content.put("bei", remarkName)
        }
        if (remarkName.isEmpty() && bei.isNotEmpty()) {
            content.put("remarkName", bei)
        }
        // grade 可能是 "0" 字符串，转成数字避免部分路径再踩坑
        if (content.has("grade") && !content.isNull("grade")) {
            val gradeVal = content.opt("grade")
            if (gradeVal is String) {
                val num = gradeVal.toDoubleOrNull()
                if (num != null) {
                    content.put("grade", num)
                }
            }
        }
        // characterTagsId: null 去掉，避免非空 Int 默认值干扰
        if (content.has("characterTagsId") && content.isNull("characterTagsId")) {
            content.remove("characterTagsId")
        }
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
                                            val fromJson = GsonUtils.getGson().fromJson(
                                                body,
                                                MeetingDetailBroadBean::class.java
                                            )
                                            callBack?.onSuccess(fromJson)
                                        }

                                        is CreateRoomBroadBean -> {
                                            val fromJson = GsonUtils.getGson().fromJson(
                                                body,
                                                CreateRoomBroadBean::class.java
                                            )
                                            callBack?.onSuccess(fromJson)
                                        }

                                        is AddMeetingTimeBroadBean -> {
                                            val fromJson = GsonUtils.getGson().fromJson(
                                                body,
                                                AddMeetingTimeBroadBean::class.java
                                            )
                                            callBack?.onSuccess(fromJson)
                                        }

                                        is UniqueRoomBroadBean -> {
                                            val fromJson = GsonUtils.getGson().fromJson(
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
                                            val fromJson = GsonUtils.getGson().fromJson(
                                                body,
                                                UpdateRoomBroadBean::class.java
                                            )
                                            callBack?.onSuccess(fromJson)
                                        }

                                        is GroupMeetingBroadBean -> {
                                            val fromJson = GsonUtils.getGson().fromJson(
                                                body,
                                                GroupMeetingBroadBean::class.java
                                            )
                                            callBack?.onSuccess(fromJson)
                                        }

                                        is NoResultBroadBean -> {
                                            callBack?.onSuccess(bean)
                                        }

                                        is KickListBroadBean -> {
                                            val commonDataListBean = GsonUtils.getGson().fromJson(
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
                                            val fromJson = GsonUtils.getGson().fromJson(
                                                body,
                                                CreateRoomBroadBean::class.java
                                            )
                                            callBack?.onSuccess(fromJson)
                                        }

                                        is UniqueRoomBroadBean -> {
                                            val fromJson = GsonUtils.getGson().fromJson(
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
                                            val fromJson = GsonUtils.getGson().fromJson(
                                                body,
                                                UpdateRoomBroadBean::class.java
                                            )
                                            callBack?.onSuccess(fromJson)
                                        }

                                        is GroupMeetingBroadBean -> {
                                            val fromJson = GsonUtils.getGson().fromJson(
                                                body,
                                                GroupMeetingBroadBean::class.java
                                            )
                                            callBack?.onSuccess(fromJson)
                                        }

                                        is NoResultBroadBean -> {
                                            callBack?.onSuccess(bean)
                                        }

                                        is KickListBroadBean -> {
                                            val commonDataListBean = GsonUtils.getGson().fromJson(
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