package com.lxkj.aiim.viewmodel

import android.content.Context
import com.lxkj.aiim.lj.bean.ResultBean
import com.lxkj.aiim.lj.http.OkHttpHelper
import com.lxkj.aiim.lj.http.SpotsCallBack
import com.lxkj.aiim.lj.http.Url
import com.lxkj.aiim.lj.utils.ToastUtil
import okhttp3.Response
import kotlin.collections.set

class PasswordViewModel : SafeCenterViewModel() {

    /**
     * 验证支付密码
     */
    fun checkPayPwd(context: Context, pwd1: String?, type: Int =0, account: String?=null, callBack: SpotsCallBack<ResultBean>) {
        val param: MutableMap<String, Any?> = HashMap()
        param["pwd"] = pwd1
        if (type !=0){
            param["type"] = type
        }
        if (!account.isNullOrEmpty()){
            param["account"] = account
        }
        OkHttpHelper.getInstance()
            .post_json(context, Url.checkPayPwd, param, callBack)
    }

    /**
     * 设置支付密码
     */
    fun setPaypwd(context: Context, pwd3: String?, callBack: SpotsCallBack<ResultBean>) {
        val param: MutableMap<String?, Any?> = java.util.HashMap()
        param.put("pwd", pwd3)
        OkHttpHelper.getInstance()
            .post_json(context, Url.setPaypwd, param, callBack)
    }
}