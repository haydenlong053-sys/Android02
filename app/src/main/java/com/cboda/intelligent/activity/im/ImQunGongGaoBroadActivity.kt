package com.cboda.intelligent.activity.im

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import com.cboda.baselibrary.common.MyEventCodeEnum
import com.cboda.baselibrary.utils.MMKVBroadUtils.Companion.saveGroupNotice
import com.cboda.baselibrary.toast.ToastBroadUtils
import com.cboda.chatui.bridge.MyBroadEvent
import com.cboda.intelligent.R
import com.cboda.intelligent.base.BaseSimple2BroadActivity
import com.freddy.im.http.ImGroupHttpExample
import com.freddy.im.http.model.im.BaseMsgResponse
import com.freddy.im.listener.OnHttpImListener
import org.greenrobot.eventbus.EventBus

/** 自研群公告编辑（对齐野火 {@link com.cboda.intelligent.activity.QunGongGaoBroadActivity}）。 */
class ImQunGongGaoBroadActivity : BaseSimple2BroadActivity() {

    private var qunId = ""
    private lateinit var tvAgree: TextView
    private lateinit var tvNotAgree: TextView
    private lateinit var tvNumHint: TextView
    private lateinit var edtBeizhu: EditText

    override fun getLayoutId(): Int = R.layout.activity_xiu_gai_qun_gong_broad_gao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setRootBgColor(R.color.white)
        qunId = intent?.getStringExtra("qunid") ?: ""
        val notice = intent?.getStringExtra(ImGroupIntent.EXTRA_NOTICE) ?: ""
        edtBeizhu = findViewById(R.id.edtBeizhu)
        tvNumHint = findViewById(R.id.tvNumHint)
        tvNotAgree = findViewById(R.id.tvNotAgree)
        tvAgree = findViewById(R.id.tvAgree)
        getToolbarTitle()?.text = getString(R.string.qungonggapo)
        if (notice.isNotEmpty()) edtBeizhu.setText(notice)

        tvNotAgree.setOnClickListener { finish() }
        edtBeizhu.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                tvNumHint.text = "${s?.length ?: 0}/600"
            }
        })
        tvAgree.setOnClickListener {
            val content = edtBeizhu.text?.trim()?.toString().orEmpty()
            if (content.isEmpty()) {
                ToastBroadUtils.show(getString(R.string.qingshuruqungonggao))
                return@setOnClickListener
            }
            ImGroupHttpExample.requestUpdateGroupNotice(qunId, content,
                object : OnHttpImListener<BaseMsgResponse<Void>> {
                    override fun onHttpImSuccess(result: BaseMsgResponse<Void>) {
                        runOnUiThread {
                            saveGroupNotice(qunId, content)
                            EventBus.getDefault().post(
                                MyBroadEvent(
                                    MyEventCodeEnum.UPDATE_ANNOUNCEMENT.value,
                                    content,
                                    qunId
                                )
                            )
                            finish()
                        }
                    }
                }, {
                    runOnUiThread {
                        ToastBroadUtils.show(getString(R.string.group_notice_save_failed))
                    }
                })
        }
    }

    override fun showTransparentTopTitleLayout(): Boolean = true
}
