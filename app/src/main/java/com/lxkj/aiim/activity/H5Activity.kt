package com.lxkj.aiim.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.content.ContextCompat
import com.cboda.intelligent.R
import com.lxkj.aiim.base.BaseSimpleActivity
import com.lxkj.aiim.utils.AppUtil
import com.lxkj.aiim.webview.SonicRuntimeImpl
import com.lxkj.aiim.webview.SonicSessionClientImpl
import com.tencent.sonic.sdk.SonicConfig
import com.tencent.sonic.sdk.SonicEngine
import com.tencent.sonic.sdk.SonicSession
import com.tencent.sonic.sdk.SonicSessionConfig

/**
 * 简述：H5页面
 * Created by ysh on 2023/3/29.
 */
class H5Activity : BaseSimpleActivity() {

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
        return R.layout.activity_h5
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
            SonicEngine.createInstance(SonicRuntimeImpl(this), SonicConfig.Builder().build())
        }

        var sonicSessionClient: SonicSessionClientImpl? = null
        val sessionConfigBuilder = SonicSessionConfig.Builder()
        sessionConfigBuilder.setSupportLocalServer(true)
//        sonicSession = SonicEngine.getInstance().createSession(url, sessionConfigBuilder.build())
//        sonicSession?.bindClient(SonicSessionClientImpl().also { sonicSessionClient = it })
        webView?.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
//                if (sonicSession != null) {
//                    sonicSession?.sessionClient?.pageFinish(url)
//                }
                dismissLoading()
                AppUtil.debug("loadUrl--->", mloadUrl ?: "")
                if (mloadUrl?.startsWith("weixin:") == true) {
                    AppUtil.debug("webView", "加载特殊url加载完成----->")
                    finish()
                }
                if (fromPage != "Web4X官网") {
                    val colorContent =
                                        ContextCompat.getColor(this@H5Activity, R.color.color_text0)
                    val colorBg = ContextCompat.getColor(this@H5Activity, R.color.bg_main)
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
                        AppUtil.debug("webView", "加载http、https----->")
                        false
                    } else {
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse(loadUrl)
                        startActivity(intent)
                        AppUtil.debug("webView", "加载特殊url----->")
                        true
                    }
                } catch (e: Exception) {
                    AppUtil.debug("webView Exception", e.toString())
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