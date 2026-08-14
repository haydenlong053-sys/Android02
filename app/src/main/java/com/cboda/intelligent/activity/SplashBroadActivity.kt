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
            LanguageUtil.saveAppLanguageKey(LanguageUtil.SIMPLIFIED, this)
            LanguageUtil.setAppLanguage(LanguageUtil.SIMPLIFIED, this@SplashBroadActivity)
        }


//        val param: MutableMap<String, Any> = HashMap()
//        OkHttpBroadHelper.getInstance().post_json(this, BroadUrl.ptConfig, param, object : BaseBroadCallback<ResultBroadBean?>() {
//                override fun onError(response: Response, code: Int, e: Exception) {}
//                override fun onSuccess(response: Response?, t: ResultBroadBean?) {
//                    SharePrefBroadBroadUtil.saveString(this@SplashBroadActivity, AppBroadConsts.showFx, t?.data?.showFx)
//                    SharePrefBroadBroadUtil.saveString(this@SplashBroadActivity, AppBroadConsts.showXcx, t?.data?.showXcx)
//                    SharePrefBroadBroadUtil.saveString(this@SplashBroadActivity, AppBroadConsts.LOGINTYPE, t?.data?.loginType)
        if (TextUtils.isEmpty(SharePrefBroadBroadUtil.getString(this@SplashBroadActivity, AppBroadConsts.UID, ""))
            && TextUtils.isEmpty(MMKVBroadUtils.getRealUserId())
        ) {
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
            val uid = MMKVBroadUtils.getRealUserId()
                ?.takeIf { it.isNotEmpty() }
                ?: SharePrefBroadBroadUtil.getString(this@SplashBroadActivity, AppBroadConsts.UID, "")
            val token = MMKVBroadUtils.getUserAppToken()
                ?.takeIf { it.isNotEmpty() }
                ?: SharePrefBroadBroadUtil.getString(this@SplashBroadActivity, AppBroadConsts.APPTOKEN, "")
            // 兼容旧版本只写了 MMKV、或只写了 SharePref 的登录态
            if (!TextUtils.isEmpty(uid) && TextUtils.isEmpty(SharePrefBroadBroadUtil.getString(this@SplashBroadActivity, AppBroadConsts.UID, ""))) {
                SharePrefBroadBroadUtil.saveString(this@SplashBroadActivity, AppBroadConsts.UID, uid)
            }
            if (!TextUtils.isEmpty(token) && TextUtils.isEmpty(SharePrefBroadBroadUtil.getString(this@SplashBroadActivity, AppBroadConsts.APPTOKEN, ""))) {
                SharePrefBroadBroadUtil.saveString(this@SplashBroadActivity, AppBroadConsts.APPTOKEN, token)
            }
            if (!TextUtils.isEmpty(uid) && TextUtils.isEmpty(MMKVBroadUtils.getRealUserId())) {
                MMKVBroadUtils.saveRealUserId(uid)
            }
            if (!TextUtils.isEmpty(token) && TextUtils.isEmpty(MMKVBroadUtils.getUserAppToken())) {
                MMKVBroadUtils.saveUserAppToken(token)
            }
            AppBroadUtils.goLoginMyIm(token, uid)
            JumpBroadUtils.startMainActivity(this@SplashBroadActivity, "SplashBroadActivity")
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