package com.lxkj.aiim.net.manager

import android.content.Context
import com.lxkj.aiim.lj.bean.ResultBean
import com.lxkj.aiim.lj.http.BaseCallback
import com.lxkj.aiim.lj.http.OkHttpHelper
import com.lxkj.aiim.lj.http.SpotsCallBack
import com.lxkj.aiim.lj.http.Url
import okhttp3.Response
import java.lang.Exception

/**
 * 用户相关的接口调用
 */
object UserServerManager {

    /**
     * 设备号登录
     */
    fun qeLogin(context: Context?, params: Map<String, Any>, requestUrl: String? = Url.eqLogin,callBack: SpotsCallBack<ResultBean>){
         context?.let {ctx->
             OkHttpHelper.getInstance().post_json(ctx,requestUrl,params,object :SpotsCallBack<ResultBean>(ctx){
                 override fun onSuccess(response: Response?, t: ResultBean?) {
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
    fun getCode(context: Context?, params: Map<String, Any>, requestUrl: String? = Url.getAccCode, callBack: SpotsCallBack<ResultBean>) {
        context?.let { ctx ->
            OkHttpHelper.getInstance().post_json(ctx, requestUrl, params, object : SpotsCallBack<ResultBean>(ctx) {
                override fun onSuccess(response: Response?, t: ResultBean?) {
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
    fun accLogin(context: Context?, params: Map<String, Any>, requestUrl: String? = Url.accLogin, callBack: SpotsCallBack<String>) {
        context?.let { ctx ->
            OkHttpHelper.getInstance().post_json(ctx, requestUrl, params, object : SpotsCallBack<String>(ctx) {
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
    fun regist(context: Context?, params: Map<String, Any>, requestUrl: String? = Url.regist, callBack: SpotsCallBack<ResultBean>) {
        context?.let { ctx ->
            OkHttpHelper.getInstance().post_json(ctx, requestUrl, params, object : SpotsCallBack<ResultBean>(ctx) {
                override fun onSuccess(response: Response?, t: ResultBean?) {
                    callBack.onSuccess(response, t)
                }

                override fun onError(response: Response?, code: Int, e: Exception?) {
                    callBack.onError(response, code, e)
                }
            })
        }
    }
}