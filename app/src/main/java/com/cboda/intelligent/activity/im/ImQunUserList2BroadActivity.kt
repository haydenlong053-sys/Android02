package com.cboda.intelligent.activity.im

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cboda.intelligent.R
import com.cboda.intelligent.adapter.QunUserBroadAdapter2
import com.cboda.intelligent.base.BaseSimpleBroadActivity
import com.cboda.intelligent.utils.JumpBroadUtils
import com.freddy.im.group.GroupRole
import com.freddy.im.http.ImGroupHttpExample
import com.freddy.im.http.model.im.GroupUserVo
import com.freddy.im.listener.OnHttpImListener

/** 自研群成员全列表（对齐野火 {@link com.cboda.intelligent.activity.QunUserList2BroadActivity}）。 */
class ImQunUserList2BroadActivity : BaseSimpleBroadActivity() {

    private var qunId = ""
    private var myRole = GroupRole.MEMBER
    private var mAdapter: QunUserBroadAdapter2? = null

    override fun getLayoutId(): Int = R.layout.activity_qun_user_broad_list2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        qunId = intent?.getStringExtra("qunid") ?: ImGroupIntent.getGroupId(intent) ?: ""
        myRole = intent?.getIntExtra(ImGroupIntent.EXTRA_MY_ROLE, GroupRole.MEMBER) ?: GroupRole.MEMBER
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        findViewById<View>(R.id.smart)?.isEnabled = false

        mAdapter = QunUserBroadAdapter2(R.layout.item_qun_user_broad_layout2)
        recyclerView.layoutManager = GridLayoutManager(this, 4)
        recyclerView.adapter = mAdapter
        mAdapter?.setOnItemClickListener { _, _, position ->
            val item = mAdapter?.getItem(position) ?: return@setOnItemClickListener
            JumpBroadUtils.startImQunGerenPageActivity(
                this, qunId, item.userId ?: "", item.type ?: "", myRole
            )
        }
        loadMembers()
    }

    private fun loadMembers() {
        ImGroupHttpExample.requestGetAllMembers(qunId, object : OnHttpImListener<List<GroupUserVo>> {
            override fun onHttpImSuccess(result: List<GroupUserVo>) {
                runOnUiThread {
                    mAdapter?.setList(ImGroupMemberUiMapper.toCommonBeans(result))
                    findViewById<View>(R.id.llNoData)?.visibility =
                        if (result.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }, {
            runOnUiThread {
                findViewById<View>(R.id.llNoData)?.visibility = View.VISIBLE
            }
        })
    }
}
