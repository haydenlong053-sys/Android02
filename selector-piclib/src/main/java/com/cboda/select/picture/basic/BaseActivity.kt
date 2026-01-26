package com.cboda.select.picture.basic

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cboda.select.picture.utils.LanguageUtil.getLanguageContext
import android.content.res.Configuration
import android.os.Build
import android.text.TextUtils
import com.cboda.select.picture.utils.LanguageUtil
import com.cboda.select.picture.utils.LanguageUtil.setAppLanguage

open class BaseActivity : AppCompatActivity() {

    @SuppressLint("ObsoleteSdkInt")
    override fun onCreate(savedInstanceState: Bundle?) {
//        val languageKey = getAppLanguageKey(this)
////        Log.d("getLanguageContextClass", "languageKey:$languageKey")
//        val local=LocaleTransform.getLanguage(languageKey)
//        Log.d("getLanguageContextClass", "languageKey:$languageKey ,la:${local.language}")
//        PictureLanguageUtils.applyLanguage(this, local)
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            setAppLanguage(LanguageUtil.getAppLanguageKey(this, false), this)
        }
    }

//    override fun attachBaseContext(newBase: Context?) {
//        super.attachBaseContext(getLanguageContext(getAppContext(newBase), false))
//    }

    override fun attachBaseContext(newBase: Context?) {
        if (TextUtils.isEmpty(LanguageUtil.getAppLanguageKey(newBase, true))) {
            super.attachBaseContext(getLanguageContext(newBase, false))
        } else {
            super.attachBaseContext(newBase)
        }
    }

    override fun onResume() {
        super.onResume()

    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
//        val languageKey = getAppLanguageKey(this)
//        Log.d("getLanguageContextClass", "languageKey:$languageKey")

    }
}