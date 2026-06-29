package com.cboda.intelligent.activity.im

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cboda.baselibrary.bean.CommonDataListBroadBean
import com.cboda.baselibrary.toast.ToastBroadUtils
import com.cboda.intelligent.R
import com.cboda.intelligent.adapter.QunGuanLiBroadAdapter
import com.cboda.intelligent.base.BaseSimpleBroadActivity
import com.freddy.chat.im.MessageProcessor
import com.freddy.im.db.ImLocalStore
import com.freddy.im.http.ImGroupHttpExample
import com.freddy.im.listener.OnHttpImListener

/** 自研群已禁言成员列表，点击解除禁言。 */
class ImQunJinYanMemberBroadActivity : BaseSimpleBroadActivity() {

    private var qunId = ""
    private val dataList = arrayListOf<CommonDataListBroadBean.CommonBean>()
    private var adapter: QunGuanLiBroadAdapter? = null

    override fun getLayoutId(): Int = R.layout.activity_im_muted_member_list

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        qunId = intent?.getStringExtra("qunid") ?: ""
        getToolbarTitle()?.text = getString(R.string.yijinyanmingdan)

        adapter = QunGuanLiBroadAdapter(R.layout.item_qun_guan_broad_li_layout, "jiechujinyan")
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
        ImGroupHttpExample.requestMutedMembers(qunId, object : OnHttpImListener<List<String>> {
            override fun onHttpImSuccess(result: List<String>) {
                ImLocalStore.loadGroupMembersMap(qunId) { map ->
                    val rows = ArrayList<CommonDataListBroadBean.CommonBean>()
                    result.forEach { userId ->
                        val member = map[userId]
                        val bean = CommonDataListBroadBean.CommonBean()
                        bean.userId = userId
                        bean.name = member?.nickname?.takeIf { it.isNotBlank() } ?: userId
                        bean.icon = member?.headPortrait
                        rows.add(bean)
                    }
                    rows.sortBy { it.name?.lowercase() ?: "" }
                    runOnUiThread {
                        dataList.clear()
                        dataList.addAll(rows)
                        adapter?.setList(dataList)
                        if (dataList.isEmpty()) {
                            adapter?.setEmptyView(R.layout.list_empty_broad_layout)
                        }
                    }
                }
            }
        }, {
            runOnUiThread {
                ToastBroadUtils.show(getString(R.string.group_detail_load_failed))
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
                    ToastBroadUtils.show(getString(R.string.group_notice_save_failed))
                }
            })
    }
}
