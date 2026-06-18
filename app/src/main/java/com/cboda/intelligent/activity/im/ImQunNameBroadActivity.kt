package com.cboda.intelligent.activity.im

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.cboda.baselibrary.toast.ToastBroadUtils
import com.cboda.intelligent.R
import com.cboda.intelligent.base.BaseSimple2BroadActivity
import com.cboda.intelligent.utils.click
import com.freddy.im.http.ImGroupHttpExample
import com.freddy.im.http.model.im.GroupVo
import com.freddy.im.listener.OnHttpImListener

/** 自研改群名（对齐野火 {@link com.cboda.intelligent.activity.QunNameBroadActivity}）。 */
class ImQunNameBroadActivity : BaseSimple2BroadActivity() {

    private var qunId = ""
    private lateinit var tvPhoneLogin: TextView
    private lateinit var edtBeizhu: EditText

    override fun getLayoutId(): Int = R.layout.activity_xiu_broad_gai_qun_name

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setRootBgColor(R.color.white)
        qunId = intent?.getStringExtra("qunid") ?: ""
        val qunName = intent?.getStringExtra("qunName") ?: ""
        edtBeizhu = findViewById(R.id.edtBeizhu)
        tvPhoneLogin = findViewById(R.id.tvPhoneLogin)
        getToolbarTitle()?.text = getString(R.string.qubmingcheng)
        if (qunName.isNotEmpty()) edtBeizhu.setText(qunName)

        findViewById<View>(R.id.ivqingchu).setOnClickListener { edtBeizhu.setText("") }
        findViewById<View>(R.id.tvPhoneCancel).click { finish() }
        edtBeizhu.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) = Unit
        })
        tvPhoneLogin.setOnClickListener {
            val name = edtBeizhu.text?.trim()?.toString().orEmpty()
            if (name.isEmpty()) {
                ToastBroadUtils.show(getString(R.string.qingshuruqunmning))
                return@setOnClickListener
            }
            ImGroupHttpExample.requestModifyGroup(qunId, name, null, object : OnHttpImListener<GroupVo> {
                override fun onHttpImSuccess(result: GroupVo) {
                    runOnUiThread { finish() }
                }
            }, {
                runOnUiThread {
                    ToastBroadUtils.show(getString(R.string.group_rename_failed))
                }
            })
        }
    }

    override fun showTransparentTopTitleLayout(): Boolean = true
}
