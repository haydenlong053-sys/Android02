package com.cboda.select.picture.basic

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cboda.select.picture.utils.LanguageUtil.getLanguageContext
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.text.TextUtils
import android.view.View
import android.view.WindowManager
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.blankj.utilcode.util.BarUtils
import com.cboda.select.picture.utils.LanguageUtil

open class BaseActivity : AppCompatActivity() {


    private var navBarHeightInvoke: ((Int) -> Unit)? = null

    @SuppressLint("ObsoleteSdkInt")
    override fun onCreate(savedInstanceState: Bundle?) {
        //        val languageKey = getAppLanguageKey(this)
////        Log.d("getLanguageContextClass", "languageKey:$languageKey")
//        val local=LocaleTransform.getLanguage(languageKey)
//        Log.d("getLanguageContextClass", "languageKey:$languageKey ,la:${local.language}")
//        PictureLanguageUtils.applyLanguage(this, local)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.TRANSPARENT // 使状态栏透明
            // 可选：让内容延伸到导航栏（如果也需要铺满底部）
            // window.setNavigationBarColor(Color.TRANSPARENT);
        }
        super.onCreate(savedInstanceState)

        val rootView = findViewById<View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val navBarHeight = insets.systemWindowInsetBottom
            navBarHeightInvoke?.invoke(navBarHeight)
            insets
        }
    }


    fun setNavBarHeightListener(l: (Int) -> Unit) {
        navBarHeightInvoke = l
    }

    fun addHeight(view: View) {
        BarUtils.addMarginTopEqualStatusBarHeight(view)
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