package com.cboda.intelligent.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.content.ContextCompat
import com.cboda.intelligent.R
import com.cboda.intelligent.base.BaseSimpleBroadActivity
import com.cboda.intelligent.utils.AppBroadUtils
import com.cboda.intelligent.webview.SonicRuntimeBroadImpl
import com.cboda.intelligent.webview.SonicSessionClientBroadImpl
import com.tencent.sonic.sdk.SonicConfig
import com.tencent.sonic.sdk.SonicEngine
import com.tencent.sonic.sdk.SonicSessionConfig

/**
 * 简述：H5页面
 * Created by ysh on 2023/3/29.
 */
class H5BroadActivity : BaseSimpleBroadActivity() {

    private var url = ""
    private var fromPage = ""
    private var mloadUrl: String? = null
    private lateinit var webView:
            WebView

    private fun initViews() {
        webView = findViewById(R.id.webView)
    }
//    private var sonicSession: SonicSession? = null


    override fun getLayoutId(): Int {
        return R.layout.activity_broad_h5
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViews()
        url = intent?.getStringExtra("url") ?: ""
        fromPage = intent?.getStringExtra("fromPage") ?: ""
        getToolbarTitle()?.text = fromPage
        webViewSet()
    }

    private fun webViewSet() {
        showLoading()
        if (!SonicEngine.isGetInstanceAllowed()) {
            SonicEngine.createInstance(SonicRuntimeBroadImpl(this), SonicConfig.Builder().build())
        }

        var sonicSessionClient: SonicSessionClientBroadImpl? = null
        val sessionConfigBuilder = SonicSessionConfig.Builder()
        sessionConfigBuilder.setSupportLocalServer(true)
//        sonicSession = SonicEngine.getInstance().createSession(url, sessionConfigBuilder.build())
//        sonicSession?.bindClient(SonicSessionClientBroadImpl().also { sonicSessionClient = it })
        webView?.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
//                if (sonicSession != null) {
//                    sonicSession?.sessionClient?.pageFinish(url)
//                }
                dismissLoading()
                AppBroadUtils.debug("loadUrl--->", mloadUrl ?: "")
                if (mloadUrl?.startsWith("weixin:") == true) {
                    AppBroadUtils.debug("webView", "加载特殊url加载完成----->")
                    finish()
                }
                if (fromPage != "Web4X官网") {
                    val colorContent =
                                        ContextCompat.getColor(this@H5BroadActivity, R.color.color_text0)
                    val colorBg = ContextCompat.getColor(this@H5BroadActivity, R.color.bg_main)
                    val colorContentCode = String.format("#%06X", (0xFFFFFF and colorContent))
                    val colorBgCode = String.format("#%06X", (0xFFFFFF and colorBg))
                    webView.evaluateJavascript(
                        "(function() {" +
                                "document.body.style.backgroundColor = '" + colorBgCode + "';" +
                                "document.body.style.color = '" + colorContentCode + "';" +
                                "})();",
                        null
                    )
                }
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                try {
                    val loadUrl = request?.url?.toString()
                    mloadUrl = loadUrl
                    return if ((loadUrl?.startsWith("http") == true) || (loadUrl?.startsWith("https") == true)) {
                        view?.loadUrl(loadUrl)
                        AppBroadUtils.debug("webView", "加载http、https----->")
                        false
                    } else {
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse(loadUrl)
                        startActivity(intent)
                        AppBroadUtils.debug("webView", "加载特殊url----->")
                        true
                    }
                } catch (e: Exception) {
                    AppBroadUtils.debug("webView Exception", e.toString())
                    return false
                }
            }

        }


        val webSettings: WebSettings? = webView?.settings
        // init webview settings
        webSettings?.javaScriptEnabled = true
        webSettings?.allowContentAccess = true
        webSettings?.databaseEnabled = true
        webSettings?.domStorageEnabled = true
        webSettings?.useWideViewPort = true
        webSettings?.loadWithOverviewMode = true
        if (sonicSessionClient != null) {
            sonicSessionClient?.bindWebView(webView)
            sonicSessionClient?.clientReady()
        } else { // default mode
            webView?.loadUrl(url)
        }



    }
}