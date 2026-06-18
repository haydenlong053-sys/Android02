package com.cboda.intelligent.activity.im

import android.os.Bundle
import android.text.TextUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.blankj.utilcode.util.SizeUtils.dp2px
import com.cboda.baselibrary.utils.loadImg
import com.cboda.intelligent.R
import com.cboda.intelligent.base.BaseSimpleBroadActivity
import com.cboda.intelligent.utils.GlideLoaderBroadHelper
import com.freddy.im.http.ImGroupHttpExample
import com.freddy.im.http.model.im.GroupQrCodeRes
import com.freddy.im.http.model.im.GroupVo
import com.freddy.im.listener.OnHttpImListener
import com.king.zxing.util.CodeUtils

/** 自研群二维码（对齐野火 {@link com.cboda.intelligent.activity.QunCodeBroadActivity}）。 */
class ImQunCodeBroadActivity : BaseSimpleBroadActivity() {

    private var qunId = ""
    private var groupVo: GroupVo? = null
    private lateinit var erweima: ImageView
    private lateinit var tvusername: TextView
    private lateinit var ivTouxiang: ImageView

    override fun getLayoutId(): Int = R.layout.activity_qun_broad_code

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        qunId = intent?.getStringExtra("qunid") ?: ""
        groupVo = ImGroupIntent.getGroupVo(intent)
        erweima = findViewById(R.id.erweima)
        tvusername = findViewById(R.id.tvusername)
        ivTouxiang = findViewById(R.id.ivTouxiang)
        getToolbarTitle()?.text = getString(R.string.qunerweim)

        tvusername.text = groupVo?.name ?: ""
        val icon = groupVo?.icon
        if (!icon.isNullOrEmpty()) {
            GlideLoaderBroadHelper.loadUrl(this, icon, ivTouxiang)
        }
        loadQrCode()
    }

    private fun loadQrCode() {
        ImGroupHttpExample.requestGetGroupQrCode(qunId, object : OnHttpImListener<GroupQrCodeRes> {
            override fun onHttpImSuccess(result: GroupQrCodeRes) {
                runOnUiThread {
                    val url = result.codeUrl
                    if (!TextUtils.isEmpty(url)) {
                        erweima.loadImg(CodeUtils.createQRCode(url, dp2px(260f)), null)
                    }
                }
            }
        }, null)
    }
}
