package com.cboda.intelligent.activity

import android.content.Intent
import android.os.Bundle
import android.webkit.WebViewClient
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.cboda.chatui.conversation.forward.ForwardBroadActivity
import cn.wildfirechat.message.Message
import cn.wildfirechat.message.NoticeMessageContent
import com.cboda.intelligent.R
import com.cboda.intelligent.base.BaseSimple2BroadActivity
import com.cboda.intelligent.databinding.ActivityNoticeDetailBinding
import com.cboda.intelligent.model.NoticeInfoBroadBean
import com.cboda.intelligent.viewmodel.NoticeViewBroadModel
import kotlinx.coroutines.launch
import kotlin.getValue

class NoticeDetailBroadActivity: BaseSimple2BroadActivity() {
    private var mBind: ActivityNoticeDetailBinding? = null
    private val viewModel: NoticeViewBroadModel by viewModels()
    private var noticeInfoBean: NoticeInfoBroadBean?=null

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
        noticeInfoBean= intent.getSerializableExtra("data") as? NoticeInfoBroadBean
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
                .observe(this@NoticeDetailBroadActivity) { resultBean ->
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
                val intent = Intent(this@NoticeDetailBroadActivity, ForwardBroadActivity::class.java)
                intent.putExtra("message",message)
                this@NoticeDetailBroadActivity.startActivity(intent)
            }

        }
    }

}