package com.lxkj.aiim.activity

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import com.lxkj.aiim.R
import com.lxkj.aiim.app.LiXinApp
import com.lxkj.aiim.base.BaseSimpleActivity
import com.lxkj.aiim.lj.NewEquipmentActivity
import com.lxkj.aiim.lj.configuration.AppConsts
import com.lxkj.aiim.lj.utils.SharePrefUtil
import com.lxkj.aiim.utils.JumpUtils.Companion.startMainActivity

class SplashActivity : BaseSimpleActivity() {
    override fun getLayoutId(): Int {
        return R.layout.activity_splash
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
//        if (MMKVUtils.isPrivacyready()) {
//            jumpAct()
//        } else {
//            JCollectionAuth.setAuth(this, false)
//            PrivacyContentDialog(this, object : PrivacyContentDialog.OnAgreeListener {
//                override fun onAgree() {
//                    MMKVUtils.setPrivacyready(true)
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
        LiXinApp.initOkGo()
    }

    fun jumpAct() {
        ptConfig()
    }

    /**
     * 平台参数
     * */
    private fun ptConfig() {
//        val param: MutableMap<String, Any> = HashMap()
//        OkHttpHelper.getInstance().post_json(this, Url.ptConfig, param, object : BaseCallback<ResultBean?>() {
//                override fun onError(response: Response, code: Int, e: Exception) {}
//                override fun onSuccess(response: Response?, t: ResultBean?) {
//                    SharePrefUtil.saveString(this@SplashActivity, AppConsts.showFx, t?.data?.showFx)
//                    SharePrefUtil.saveString(this@SplashActivity, AppConsts.showXcx, t?.data?.showXcx)
//                    SharePrefUtil.saveString(this@SplashActivity, AppConsts.LOGINTYPE, t?.data?.loginType)
        if (TextUtils.isEmpty(SharePrefUtil.getString(this@SplashActivity, AppConsts.UID, ""))) {
//                        when(t?.data?.loginType){
//                            "1"->{//设备号登录

            intent = Intent(this@SplashActivity, NewEquipmentActivity::class.java)
//            intent = Intent(this@SplashActivity, EquipmentActivity::class.java)
            startActivity(intent)
//                            }
//                            "2","3","4"->{//用户名密码登录
//                                intent = Intent(this@SplashActivity, LoginActivity::class.java)
//                                startActivity(intent)
//                            }
//                        }
        } else {
            startMainActivity(this@SplashActivity)
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