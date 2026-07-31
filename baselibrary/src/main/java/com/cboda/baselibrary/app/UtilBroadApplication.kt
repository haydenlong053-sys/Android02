package com.cboda.baselibrary.app

import android.app.Application
import android.content.Context
import com.cboda.baselibrary.utils.RealmUtil.initRealm
import com.cboda.select.picture.utils.LanguageUtil.getLanguageContext
import com.cboda.select.picture.utils.LanguageUtil.initLanguage

open class UtilBroadApplication : Application() {
    companion object {
        lateinit var cxt: UtilBroadApplication
    }

    override fun onCreate() {
        super.onCreate()
        cxt = this
        initLanguage(this)
        initRealm(this)
    }


    override fun attachBaseContext(base: Context?) {
        // Application 尚未就绪，不要走 getAppContext（会碰 lateinit cxt）
        super.attachBaseContext(getLanguageContext(base, true) ?: base)
    }
}