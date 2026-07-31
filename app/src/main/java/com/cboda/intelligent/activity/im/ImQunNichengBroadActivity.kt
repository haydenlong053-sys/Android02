package com.cboda.intelligent.activity.im

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.isVisible
import com.cboda.baselibrary.toast.ToastBroadUtils
import com.cboda.baselibrary.utils.click
import com.cboda.intelligent.R
import com.cboda.intelligent.base.BaseSimple2BroadActivity
import com.freddy.chat.im.IMSClientBootstrap
import com.freddy.im.db.ImDbExecutor
import com.freddy.im.db.ImLocalStore
import com.freddy.im.http.ImGroupHttpExample
import com.freddy.im.http.model.im.GroupMemberItem
import com.freddy.im.listener.OnHttpImListener
import java.util.Collections

/**
 * 自研设置群昵称（UI 对齐 [com.cboda.intelligent.activity.QunNichengBroadActivity]，
 * 接口对齐文档 §3.26 {@code POST /api/im/group/setNickname}）。
 */
class ImQunNichengBroadActivity : BaseSimple2BroadActivity() {

    private var qunId = ""
    private var beizhu = ""
    private lateinit var tvPhoneLogin: TextView
    private lateinit var ivqingchu: AppCompatImageView
    private lateinit var edtBeizhu: EditText

    override fun getLayoutId(): Int = R.layout.activity_bei_broad_zhu

    override fun showTransparentTopTitleLayout(): Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setRootBgColor(R.color.white)
        tvPhoneLogin = findViewById(R.id.tvPhoneLogin)
        ivqingchu = findViewById(R.id.ivqingchu)
        edtBeizhu = findViewById(R.id.edtBeizhu)

        getToolbarTitle()?.text = getString(com.cboda.baselibrary.R.string.group_nick_name)
        qunId = intent?.getStringExtra("qunid") ?: ""
        beizhu = intent?.getStringExtra("beizhu") ?: ""
        if (qunId.isEmpty()) {
            finish()
            return
        }

        if (beizhu.isNotEmpty()) {
            edtBeizhu.setText(beizhu)
            ivqingchu.isVisible = true
        }
        ivqingchu.setOnClickListener { edtBeizhu.setText("") }
        edtBeizhu.setHint(R.string.shuqubveizhutips)
        edtBeizhu.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                ivqingchu.isVisible = edtBeizhu.text?.trim()?.toString()?.isNotEmpty() == true
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        tvPhoneLogin.setOnClickListener {
            val nick = edtBeizhu.text?.trim()?.toString().orEmpty()
            if (nick.isEmpty()) {
                ToastBroadUtils.show(getString(R.string.shuqubveizhutips))
                return@setOnClickListener
            }
            submitNickname(nick)
        }
        findViewById<View>(R.id.tvPhoneCancel).click { finish() }
    }

    private fun submitNickname(nickname: String) {
        ImGroupHttpExample.requestSetGroupNickname(
            qunId,
            nickname,
            object : OnHttpImListener<Boolean> {
                override fun onHttpImSuccess(result: Boolean) {
                    persistLocalNickname(nickname)
                }
            },
            {
                runOnUiThread {
                    ToastBroadUtils.show(getString(com.cboda.baselibrary.R.string.group_apply_action_failed))
                }
            }
        )
    }

    /** 成功后回写本地当前用户群成员 nickname，供群详情 / 消息列表立即生效。 */
    private fun persistLocalNickname(nickname: String) {
        val userId = IMSClientBootstrap.getInstance().currentUserId
        if (userId.isNullOrEmpty()) {
            runOnUiThread { finish() }
            return
        }
        ImDbExecutor.runRead {
            val existing = ImLocalStore.queryGroupMember(qunId, userId)
            val item = existing ?: GroupMemberItem().apply {
                groupId = qunId
                this.userId = userId
            }
            item.groupNickname = nickname
            ImLocalStore.saveGroupMembersThen(Collections.singletonList(item), qunId) {
                runOnUiThread {
                    ToastBroadUtils.show(getString(com.cboda.baselibrary.R.string.group_apply_action_success))
                    finish()
                }
            }
        }
    }
}
