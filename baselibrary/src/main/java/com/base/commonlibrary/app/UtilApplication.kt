package com.base.commonlibrary.app

import android.app.Application

open class UtilApplication : Application() {
    companion object {
        lateinit var cxt: UtilApplication
    }

    override fun onCreate() {
        cxt = this
        super.onCreate()
    }
}