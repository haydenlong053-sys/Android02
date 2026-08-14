package com.cboda.intelligent.activity

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import com.cboda.baselibrary.utils.MMKVBroadUtils
import com.cboda.intelligent.R
import com.cboda.intelligent.app.MyBroadApp
import com.cboda.intelligent.base.BaseSimpleBroadActivity
import com.cboda.intelligent.configuration.AppBroadConsts
import com.cboda.intelligent.utils.AppBroadUtils
import com.cboda.intelligent.utils.JumpBroadUtils
import com.cboda.intelligent.utils.SharePrefBroadBroadUtil
import com.cboda.select.picture.utils.LanguageUtil

class SplashBroadActivity : BaseSimpleBroadActivity() {
    override fun getLayoutId(): Int {
        return R.layout.splash_activity
    }

    override fun showOrHideTitleLayout(): Boolean {
        return false
    }

    override fun transparentStatus(): Boolean {
        return true
    }

    override fun useStatusBarDarkFont(): Boolean {
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        if (MMKVBroadUtils.isPrivacyready()) {
//            jumpAct()
//        } else {
//            JCollectionAuth.setAuth(this, false)
//            PrivacyContentBroadDialog(this, object : PrivacyContentBroadDialog.OnAgreeListener {
//                override fun onAgree() {
//                    MMKVBroadUtils.setPrivacyready(true)
//                    lateInitSdk()
//                    jumpAct()
//                }
//                override fun onDisagree() {
//                    finish()
//                }
//            }).show()
//        }

//        lateInitSdk()
        jumpAct()
    }


    fun lateInitSdk() {
        MyBroadApp.Companion.initOkGo()
    }

    fun jumpAct() {

        ptConfig()
    }

    /**
     * 平台参数
     * */
    private fun ptConfig() {
        val language = LanguageUtil.getAppLanguageKey(this, true)
        if (TextUtils.isEmpty(language)) {
            LanguageUtil.saveAppLanguageKey(LanguageUtil.ENGLISH, this)
            LanguageUtil.setAppLanguage(LanguageUtil.ENGLISH, this@SplashBroadActivity)
        }


//        val param: MutableMap<String, Any> = HashMap()
//        OkHttpBroadHelper.getInstance().post_json(this, BroadUrl.ptConfig, param, object : BaseBroadCallback<ResultBroadBean?>() {
//                override fun onError(response: Response, code: Int, e: Exception) {}
//                override fun onSuccess(response: Response?, t: ResultBroadBean?) {
//                    SharePrefBroadBroadUtil.saveString(this@SplashBroadActivity, AppBroadConsts.showFx, t?.data?.showFx)
//                    SharePrefBroadBroadUtil.saveString(this@SplashBroadActivity, AppBroadConsts.showXcx, t?.data?.showXcx)
//                    SharePrefBroadBroadUtil.saveString(this@SplashBroadActivity, AppBroadConsts.LOGINTYPE, t?.data?.loginType)
        if (TextUtils.isEmpty(SharePrefBroadBroadUtil.getString(this@SplashBroadActivity, AppBroadConsts.UID, ""))) {
//                        when(t?.data?.loginType){
//                            "1"->{//设备号登录

            intent = Intent(this@SplashBroadActivity, NewEquipmentBroadActivity::class.java)
//            intent = Intent(this@SplashBroadActivity, EquipmentBroadActivity::class.java)
            startActivity(intent)
//                            }
//                            "2","3","4"->{//用户名密码登录
//                                intent = Intent(this@SplashBroadActivity, LoginBroadActivity::class.java)
//                                startActivity(intent)
//                            }
//                        }
        } else {
            AppBroadUtils.Companion.goLoginMyIm(MMKVBroadUtils.Companion.getUserAppToken(), MMKVBroadUtils.Companion.getRealUserId())
            JumpBroadUtils.Companion.startMainActivity(this@SplashBroadActivity, "SplashBroadActivity")
        }
        finish()

//                }
//
//                override fun onBeforeRequest(request: Request?) {
//                }
//
//                override fun onFailure(request: Request?, e: java.lang.Exception?) {
//                }
//
//                override fun onResponse(response: Response?) {
//                }
//            })
    }
}
