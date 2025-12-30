package com.lxkj.aiim.fragment

import android.os.Bundle
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.lxkj.aiim.R
import com.lxkj.aiim.base.BaseLazyFragment
import com.lxkj.aiim.holder.XiaochengxuMaHolder
import com.lxkj.aiim.viewmodel.CommViewModel

class XiaoChengXuFragment : BaseLazyFragment() {
    private var viewModel: CommViewModel? = null
    private lateinit var webView: WebView
    private lateinit var ivsousuo: AppCompatImageView
    private lateinit var ivAdd: AppCompatImageView
    private lateinit var tuihui: AppCompatImageView
    private lateinit var hometool:
            View

    private fun initViews(view: View) {
        hometool = view.findViewById(R.id.hometool)
        tuihui = view.findViewById(R.id.tuihui)
        ivAdd = view.findViewById(R.id.ivAdd)
        ivsousuo = view.findViewById(R.id.ivsousuo)
        webView = view.findViewById(R.id.webView)
    }

    companion object {
        fun getInstance(): Fragment {
            return XiaoChengXuFragment()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        var result = 0
        val resourceId = resources.getIdentifier(
            "status_bar_height",
            "dimen", "android"
        )
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        val linearParams = hometool?.layoutParams
        linearParams?.height = result
        hometool?.layoutParams = linearParams
    }

    override fun loadData() {
        viewModel = ViewModelProvider(this)[CommViewModel::class.java]
        viewModel?.xiaochengxuLiveData?.observe(this) {
            if (it.url?.isNotEmpty() == true){
                webView?.loadUrl(it.url?:"")
            }
        }
        viewModel?.userLiveData?.observe(this){
            if (it.data?.xcxUrl.isNullOrEmpty()){
                showDialog()
            } else {
                webView?.loadUrl(it.data?.xcxUrl?:"")
            }
        }
        viewModel?.getUserInfo(context, "")

        val webSettings: WebSettings? = webView?.settings
        // init webview settings
        webSettings?.javaScriptEnabled = true
        webSettings?.allowContentAccess = true
        webSettings?.databaseEnabled = true
        webSettings?.domStorageEnabled = true
        webSettings?.useWideViewPort = true
        webSettings?.loadWithOverviewMode = true


        ivAdd?.setOnClickListener {
            webView?.reload()
        }

        tuihui?.setOnClickListener {
            if (webView?.canGoBack() == true) {
                webView?.goBack()
            }
        }

        ivsousuo?.setOnClickListener {
            showDialog()
        }
    }

    private fun showDialog(){
        val holder = XiaochengxuMaHolder(context)
        holder.callBack = object : XiaochengxuMaHolder.OnConfimCallBack {
            override fun onClickConfim(content: String) {
                viewModel?.getxiaochengxu(context, content)
            }
        }
        holder.show()
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_xiaochengxu
    }
}