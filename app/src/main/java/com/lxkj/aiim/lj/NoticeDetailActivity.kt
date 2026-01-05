package com.lxkj.aiim.lj

import android.content.Intent
import android.os.Bundle
import android.webkit.WebViewClient
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.cboda.chatui.conversation.forward.ForwardActivity
import cn.wildfirechat.message.Message
import cn.wildfirechat.message.NoticeMessageContent
import com.lxkj.aiim.R
import com.lxkj.aiim.base.BaseSimple2Activity
import com.lxkj.aiim.databinding.ActivityNoticeDetailBinding
import com.lxkj.aiim.model.NoticeInfoBean
import com.lxkj.aiim.viewmodel.NoticeViewModel
import kotlinx.coroutines.launch
import kotlin.getValue

class NoticeDetailActivity: BaseSimple2Activity() {
    private var mBind: ActivityNoticeDetailBinding? = null
    private val viewModel: NoticeViewModel by viewModels()
    private var noticeInfoBean: NoticeInfoBean?=null

    override fun getViewBindingClass(): Class<out ViewBinding>? {
        return ActivityNoticeDetailBinding::class.java
    }
    override fun showOrHideTitleLayout(): Boolean {
        return false
    }

    override fun showTransparentTopTitleLayout(): Boolean {
        return true
    }

    override fun useViewBinding(): Boolean {
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBind = (binding as? ActivityNoticeDetailBinding)
        noticeInfoBean= intent.getSerializableExtra("data") as? NoticeInfoBean
        initListener()
        initData()
        initWebView()
    }

    private fun initWebView() {
        mBind?.mWebView?.settings?.javaScriptEnabled =true
        mBind?.mWebView?.settings?.builtInZoomControls =true
        mBind?.mWebView?.settings?.domStorageEnabled =true
        mBind?.mWebView?.webViewClient = WebViewClient()
        noticeInfoBean?.contentUrl?.let {
            mBind?.mWebView?.loadUrl(it)
        }
    }

    private fun initData() {
        lifecycleScope.launch {
            viewModel.goNoticeRead(noticeInfoBean?.id)
                .observe(this@NoticeDetailActivity) { resultBean ->
                    setResult(RESULT_OK, Intent().apply {
                        putExtra("data",noticeInfoBean)
                    })
                }

        }
    }

    private fun initListener() {
        mBind?.toolbar?.mTvTitle?.text =getString(R.string.sys_notice_detail)
        mBind?.toolbar?.mFlView?.isVisible =true
        mBind?.toolbar?.mViewBack?.setOnClickListener {
            finish()
        }
        mBind?.toolbar?.mFlView?.setOnClickListener {
            if (noticeInfoBean!=null){
                val message = Message()
                message.content= NoticeMessageContent(noticeInfoBean?.contentUrl,noticeInfoBean?.id,noticeInfoBean?.createTime,noticeInfoBean?.noticeTitle,noticeInfoBean?.previewContent,noticeInfoBean?.status?:false)
                val intent = Intent(this@NoticeDetailActivity, ForwardActivity::class.java)
                intent.putExtra("message",message)
                this@NoticeDetailActivity.startActivity(intent)
            }

        }
    }

}