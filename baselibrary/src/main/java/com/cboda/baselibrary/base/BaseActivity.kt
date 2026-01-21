//package com.cboda.baselibrary.base
//
//import android.content.Context
//import android.content.res.Configuration
//import android.os.Bundle
//import android.text.TextUtils
//import androidx.appcompat.app.AppCompatActivity
//import com.cboda.baselibrary.utils.getAppContext
//import com.cboda.select.picture.utils.LanguageUtil
//import com.cboda.select.picture.utils.LanguageUtil.getLanguageContext
//
//open class BaseActivity : AppCompatActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
////        val languageKey = getAppLanguageKey(this)
//////        Log.d("getLanguageContextClass", "languageKey:$languageKey")
////        val local=LocaleTransform.getLanguage(languageKey)
////        Log.d("getLanguageContextClass", "languageKey:$languageKey ,la:${local.language}")
////        PictureLanguageUtils.applyLanguage(this, local)
//        super.onCreate(savedInstanceState)
//    }
//
////    override fun attachBaseContext(newBase: Context?) {
////        super.attachBaseContext(getLanguageContext(getAppContext(newBase), false))
////    }
//
//    override fun attachBaseContext(newBase: Context?) {
//        if (TextUtils.isEmpty(LanguageUtil.getAppLanguageKey(newBase, true))) {
//            super.attachBaseContext(getLanguageContext(getAppContext(newBase), false))
//        } else {
//            super.attachBaseContext(newBase)
//        }
//    }
//
//    override fun onResume() {
//        super.onResume()
//
//    }
//
//    override fun onConfigurationChanged(newConfig: Configuration) {
//        super.onConfigurationChanged(newConfig)
////        val languageKey = getAppLanguageKey(this)
////        Log.d("getLanguageContextClass", "languageKey:$languageKey")
//
//    }
//}