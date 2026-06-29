package com.cboda.intelligent.activity.im

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cboda.baselibrary.toast.ToastBroadUtils
import com.cboda.intelligent.R
import com.cboda.intelligent.adapter.im.ImQunGuanLiBroadAdapter
import com.cboda.intelligent.base.BaseSimpleBroadActivity
import com.freddy.chat.im.MessageProcessor
import com.freddy.im.http.ImGroupHttpExample
import com.freddy.im.http.model.im.GroupUserVo
import com.freddy.im.listener.OnHttpImListener

/** 自研群已禁言成员列表，点击解除禁言。 */
class ImQunJinYanMemberBroadActivity : BaseSimpleBroadActivity() {

    private var qunId = ""
    private val dataList = arrayListOf<GroupUserVo>()
    private var adapter: ImQunGuanLiBroadAdapter? = null

    override fun getLayoutId(): Int = R.layout.activity_im_muted_member_list

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        qunId = intent?.getStringExtra("qunid") ?: ""
        getToolbarTitle()?.text = getString(R.string.yijinyanmingdan)

        adapter = ImQunGuanLiBroadAdapter(R.layout.item_qun_guan_broad_li_layout, "jiechujinyan")
        findViewById<RecyclerView>(R.id.recyclerView).apply {
            layoutManager = LinearLayoutManager(this@ImQunJinYanMemberBroadActivity)
            this.adapter = this@ImQunJinYanMemberBroadActivity.adapter
        }
        adapter?.setList(dataList)
        adapter?.setOnItemClickListener { _, _, position ->
            if (position !in dataList.indices) return@setOnItemClickListener
            val userId = dataList[position].userId ?: return@setOnItemClickListener
            unmuteMember(userId)
        }
        loadMutedMembers()
    }

    override fun onResume() {
        super.onResume()
        loadMutedMembers()
    }

    private fun loadMutedMembers() {
        if (qunId.isEmpty()) return
        ImGroupHttpExample.requestMutedMembers(qunId, object : OnHttpImListener<List<GroupUserVo>> {
            override fun onHttpImSuccess(result: List<GroupUserVo>) {
                val rows = ArrayList(result)
                rows.sortBy { it.displayName.lowercase() }
                runOnUiThread {
                    dataList.clear()
                    dataList.addAll(rows)
                    adapter?.setList(dataList)
                    if (dataList.isEmpty()) {
                        adapter?.setEmptyView(R.layout.list_empty_broad_layout)
                    }
                }
            }
        }, {
            runOnUiThread {
                ToastBroadUtils.show(getString(R.string.group_apply_action_failed))
            }
        })
    }

    private fun unmuteMember(userId: String) {
        ImGroupHttpExample.requestSetMembersMute(
            qunId, listOf(userId), false, null,
            object : OnHttpImListener<Boolean> {
                override fun onHttpImSuccess(result: Boolean) {
                    runOnUiThread {
                        MessageProcessor.getInstance().notifyGroupMuteUpdated(qunId)
                        loadMutedMembers()
                    }
                }
            }, {
                runOnUiThread {
                    ToastBroadUtils.show(getString(R.string.group_apply_action_failed))
                }
            })
    }
}
