package com.cboda.baselibrary.base

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cboda.baselibrary.utils.getAppContext
import com.cboda.select.picture.utils.LanguageUtil.getLanguageContext

open class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(getLanguageContext(getAppContext(newBase), false))
    }
}