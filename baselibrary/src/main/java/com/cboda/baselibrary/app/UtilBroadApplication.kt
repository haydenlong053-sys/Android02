package com.cboda.baselibrary.app

import android.app.Application

open class UtilBroadApplication : Application() {
    companion object {
        lateinit var cxt: UtilBroadApplication
    }

    override fun onCreate() {
        super.onCreate()
        cxt = this
    }
}