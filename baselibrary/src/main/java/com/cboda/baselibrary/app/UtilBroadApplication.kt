package com.cboda.baselibrary.app

import android.app.Application
import android.content.Context
import com.cboda.baselibrary.utils.getAppContext
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
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(getLanguageContext(getAppContext(base), true))
    }
}