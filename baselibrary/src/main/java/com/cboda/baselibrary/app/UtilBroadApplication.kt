package com.cboda.baselibrary.app

import android.app.Application
import android.content.Context
import com.cboda.baselibrary.utils.RealmUtil.initRealm
import com.cboda.baselibrary.utils.logAppCreate
import com.cboda.baselibrary.utils.logAppTrimMemory
import com.cboda.select.picture.utils.LanguageUtil.getLanguageContext
import com.cboda.select.picture.utils.LanguageUtil.initLanguage

open class UtilBroadApplication : Application() {
    companion object {
        lateinit var cxt: UtilBroadApplication
    }

    override fun onCreate() {
        super.onCreate()
        cxt = this
        logAppCreate(this)
        initLanguage(this)
        initRealm(this)
        initLog()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        logAppTrimMemory()
    }

    private fun initLog() {
        //        val xlog = Xlog()
//        Log.setLogImp(xlog)
//        val sdCard = Environment.getExternalStorageDirectory().absolutePath
//        val logPath = "$sdCard/marssample/log"
//        val cachePath = "${this.filesDir}/xlog"
//        val logFileName = "stLog"
//        if (com.cboda.baselibrary.BuildConfig.IS_DEBUG) {
//            Log.setConsoleLogOpen(true)
//            Log.appenderOpen(Xlog.LEVEL_DEBUG, Xlog.AppednerModeAsync, cachePath, logPath, logFileName, 0);
//        } else {
//            Log.setConsoleLogOpen(false)
//            Log.appenderOpen(Xlog.LEVEL_INFO, Xlog.AppednerModeAsync, cachePath, logPath, logFileName, 0);
//        }
    }

    override fun attachBaseContext(base: Context?) {
        // Application 尚未就绪，不要走 getAppContext（会碰 lateinit cxt）
        super.attachBaseContext(getLanguageContext(base, true) ?: base)
    }
}