package com.cboda.intelligent.activity.im

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import com.cboda.baselibrary.toast.ToastBroadUtils
import com.cboda.intelligent.R
import com.cboda.intelligent.base.BaseSimple2BroadActivity
import com.freddy.im.http.ImGroupHttpExample
import com.freddy.im.http.model.im.BaseMsgResponse
import com.freddy.im.http.model.im.GroupWelcomeRes
import com.freddy.im.listener.OnHttpImListener

/**
 * 自研入群欢迎语编辑页（UI 对齐群公告编辑 [ImQunGongGaoBroadActivity]）。
 * 保存时：有内容 `enabled=true`，无内容 `enabled=false`。
 */
class ImQunWelcomeBroadActivity : BaseSimple2BroadActivity() {

    companion object {
        private const val MAX_LENGTH = 500
    }

    private var qunId = ""
    private lateinit var tvAgree: TextView
    private lateinit var tvNotAgree: TextView
    private lateinit var tvNumHint: TextView
    private lateinit var edtBeizhu: EditText

    override fun getLayoutId(): Int = R.layout.activity_xiu_gai_qun_welcome_broad

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setRootBgColor(R.color.white)
        qunId = intent?.getStringExtra("qunid") ?: ""
        if (qunId.isEmpty()) {
            finish()
            return
        }
        edtBeizhu = findViewById(R.id.edtBeizhu)
        tvNumHint = findViewById(R.id.tvNumHint)
        tvNotAgree = findViewById(R.id.tvNotAgree)
        tvAgree = findViewById(R.id.tvAgree)
        getToolbarTitle()?.text = getString(R.string.欢迎语)

        tvNotAgree.setOnClickListener { finish() }
        edtBeizhu.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                tvNumHint.text = "${s?.length ?: 0}/$MAX_LENGTH"
            }
        })
        tvAgree.setOnClickListener { saveWelcome() }
        loadWelcome()
    }

    private fun loadWelcome() {
        ImGroupHttpExample.requestGetGroupWelcome(qunId,
            object : OnHttpImListener<GroupWelcomeRes> {
                override fun onHttpImSuccess(result: GroupWelcomeRes) {
                    runOnUiThread {
                        val content = result.content?.trim().orEmpty()
                        if (content.isNotEmpty()) {
                            edtBeizhu.setText(content)
                            edtBeizhu.setSelection(content.length.coerceAtMost(MAX_LENGTH))
                        }
                        tvNumHint.text = "${edtBeizhu.text?.length ?: 0}/$MAX_LENGTH"
                    }
                }
            },
            null
        )
    }

    private fun saveWelcome() {
        val content = edtBeizhu.text?.trim()?.toString().orEmpty()
        // 有内容传开，无内容传关
        val enabled = content.isNotEmpty()
        ImGroupHttpExample.requestSetGroupWelcome(
            qunId,
            enabled,
            content,
            object : OnHttpImListener<BaseMsgResponse<Void>> {
                override fun onHttpImSuccess(result: BaseMsgResponse<Void>) {
                    runOnUiThread {
                        ToastBroadUtils.show(com.cboda.baselibrary.R.string.group_apply_action_success)
                        finish()
                    }
                }
            },
            {
                runOnUiThread {
                    ToastBroadUtils.show(getString(com.cboda.baselibrary.R.string.group_welcome_save_failed))
                }
            }
        )
    }

    override fun showTransparentTopTitleLayout(): Boolean = true
}
