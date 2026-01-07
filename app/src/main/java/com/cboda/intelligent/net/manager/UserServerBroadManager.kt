package com.cboda.intelligent.net.manager

import android.content.Context
import com.cboda.intelligent.bean.ResultBroadBean
import com.cboda.intelligent.utils.OkHttpBroadHelper
import com.cboda.intelligent.utils.SpotsCallBroadBack
import com.cboda.intelligent.net.BroadUrl
import okhttp3.Response
import java.lang.Exception

/**
 * 用户相关的接口调用
 */
object UserServerBroadManager {

    /**
     * 设备号登录
     */
    fun qeLogin(context: Context?, params: Map<String, Any>, requestUrl: String? = BroadUrl.eqLogin,callBack: SpotsCallBroadBack<ResultBroadBean>){
         context?.let {ctx->
             OkHttpBroadHelper.getInstance().post_json(ctx,requestUrl,params,object :SpotsCallBroadBack<ResultBroadBean>(ctx){
                 override fun onSuccess(response: Response?, t: ResultBroadBean?) {
                     callBack.onSuccess(response, t)
                 }

                 override fun onError(response: Response?, code: Int, e: Exception?) {
                     callBack.onError(response, code, e)
                 }
             })
         }
    }

    /**
     * 获取验证码
     */
    fun getCode(context: Context?, params: Map<String, Any>, requestUrl: String? = BroadUrl.getAccCode, callBack: SpotsCallBroadBack<ResultBroadBean>) {
        context?.let { ctx ->
            OkHttpBroadHelper.getInstance().post_json(ctx, requestUrl, params, object : SpotsCallBroadBack<ResultBroadBean>(ctx) {
                override fun onSuccess(response: Response?, t: ResultBroadBean?) {
                    callBack.onSuccess(response, t)
                }

                override fun onError(response: Response?, code: Int, e: Exception?) {
                    callBack.onError(response, code, e)
                }
            })
        }
    }

    /**
     * 账号密码登录
     */
    fun accLogin(context: Context?, params: Map<String, Any>, requestUrl: String? = BroadUrl.accLogin, callBack: SpotsCallBroadBack<String>) {
        context?.let { ctx ->
            OkHttpBroadHelper.getInstance().post_json(ctx, requestUrl, params, object : SpotsCallBroadBack<String>(ctx) {
                override fun onSuccess(response: Response?, t: String?) {
                    callBack.onSuccess(response, t)
                }

                override fun onError(response: Response?, code: Int, e: Exception?) {
                    callBack.onError(response, code, e)
                }
            })
        }
    }

    /**
     * 注册
     */
    fun regist(context: Context?, params: Map<String, Any>, requestUrl: String? = BroadUrl.regist, callBack: SpotsCallBroadBack<ResultBroadBean>) {
        context?.let { ctx ->
            OkHttpBroadHelper.getInstance().post_json(ctx, requestUrl, params, object : SpotsCallBroadBack<ResultBroadBean>(ctx) {
                override fun onSuccess(response: Response?, t: ResultBroadBean?) {
                    callBack.onSuccess(response, t)
                }

                override fun onError(response: Response?, code: Int, e: Exception?) {
                    callBack.onError(response, code, e)
                }
            })
        }
    }
}