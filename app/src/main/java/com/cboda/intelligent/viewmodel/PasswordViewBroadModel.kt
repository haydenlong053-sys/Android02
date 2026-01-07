package com.cboda.intelligent.viewmodel

import android.content.Context
import com.cboda.intelligent.bean.ResultBroadBean
import com.cboda.intelligent.utils.OkHttpBroadHelper
import com.cboda.intelligent.utils.SpotsCallBroadBack
import com.cboda.intelligent.net.BroadUrl
import kotlin.collections.set

class PasswordViewBroadModel : SafeCenterViewBroadModel() {

    /**
     * 验证支付密码
     */
    fun checkPayPwd(context: Context, pwd1: String?, type: Int =0, account: String?=null, callBack: SpotsCallBroadBack<ResultBroadBean>) {
        val param: MutableMap<String, Any?> = HashMap()
        param["pwd"] = pwd1
        if (type !=0){
            param["type"] = type
        }
        if (!account.isNullOrEmpty()){
            param["account"] = account
        }
        OkHttpBroadHelper.getInstance()
            .post_json(context, BroadUrl.checkPayPwd, param, callBack)
    }

    /**
     * 设置支付密码
     */
    fun setPaypwd(context: Context, pwd3: String?, callBack: SpotsCallBroadBack<ResultBroadBean>) {
        val param: MutableMap<String?, Any?> = java.util.HashMap()
        param.put("pwd", pwd3)
        OkHttpBroadHelper.getInstance()
            .post_json(context, BroadUrl.setPaypwd, param, callBack)
    }
}