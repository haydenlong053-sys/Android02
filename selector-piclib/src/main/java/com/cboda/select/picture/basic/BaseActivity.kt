//package com.cboda.select.picture.basic
//
//import android.content.Context
//import android.os.Bundle
//import androidx.appcompat.app.AppCompatActivity
//import com.cboda.select.picture.utils.LanguageUtil.getLanguageContext
//
//open class BaseActivity : AppCompatActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//    }
//
//    override fun attachBaseContext(newBase: Context?) {
//        newBase?.also {
//            super.attachBaseContext(getLanguageContext(newBase, false))
//        } ?: super.attachBaseContext(null)
//    }
//}