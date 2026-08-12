package com.cboda.intelligent.activity.im

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cboda.baselibrary.bean.CommonDataListBroadBean
import com.cboda.intelligent.R
import com.cboda.intelligent.adapter.QunUserBroadAdapter
import com.cboda.intelligent.base.BaseSimpleBroadActivity
import com.cboda.intelligent.utils.JumpBroadUtils
import com.freddy.im.db.ImLocalStore
import com.freddy.im.group.GroupRole
import com.freddy.im.http.model.im.GroupMemberListContent
import com.freddy.im.message.ImGroupMemberChangeManager
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener

/** 自研群成员全列表（本地库分页；对齐野火 {@link com.cboda.intelligent.activity.QunUserList2BroadActivity}）。 */
class ImQunUserList2BroadActivity : BaseSimpleBroadActivity() {

    private var qunId = ""
    private var myRole = GroupRole.MEMBER
    private var openQykj: String? = null
    private var mAdapter: QunUserBroadAdapter? = null
    private lateinit var smartLayout: SmartRefreshLayout
    private var offset = 0
    private var total = 0
    private var mDataList = arrayListOf<CommonDataListBroadBean.CommonBean>()

    override fun getLayoutId(): Int = R.layout.activity_qun_user_broad_list2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        qunId = intent?.getStringExtra("qunid") ?: ImGroupIntent.getGroupId(intent) ?: ""
        myRole = intent?.getIntExtra(ImGroupIntent.EXTRA_MY_ROLE, GroupRole.MEMBER) ?: GroupRole.MEMBER
        openQykj = intent?.getStringExtra(ImGroupIntent.EXTRA_OPEN_QYKJ)
        ImGroupMemberChangeManager.getInstance().promoteFullSyncPriority(qunId)
        smartLayout = findViewById(R.id.smart)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)

        mAdapter = QunUserBroadAdapter(R.layout.item_qun_user_broad_layout)
        recyclerView.layoutManager = GridLayoutManager(this, 4)
        recyclerView.adapter = mAdapter
        mAdapter?.setOnItemClickListener { _, _, position ->
            val item = mAdapter?.getItem(position) ?: return@setOnItemClickListener
            JumpBroadUtils.startImQunGerenPageActivity(
                this, qunId, item.userId ?: ""
            )
        }
        smartLayout.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onRefresh(refreshLayout: RefreshLayout) {
                offset = 0
                total = 0
                smartLayout.setEnableLoadMore(true)
                loadMembers()
                refreshLayout.setNoMoreData(false)
            }

            override fun onLoadMore(refreshLayout: RefreshLayout) {
                if (offset >= total) {
                    refreshLayout.setNoMoreData(true)
                    return
                }
                loadMembers()
            }
        })
        smartLayout.setEnableLoadMore(true)
        smartLayout.setEnableRefresh(false)
        loadMembers()
    }

    private fun loadMembers() {
        if (TextUtils.isEmpty(qunId)) {
            return
        }
        val pageSize = GroupMemberListContent.PAGE_SIZE_50
        ImLocalStore.loadGroupMembersPage(
            qunId,
            null,
            null,
            openQykj,
            myRole,
            offset,
            pageSize,
        ) { page ->
            smartLayout.finishRefresh()
            smartLayout.finishLoadMore()
            total = page.total
            val result = ImGroupMemberUiMapper.toCommonBeansFromMemberItems(page.members)
            if (offset == 0) {
                mDataList.clear()
            }
            if (result.isNotEmpty()) {
                offset += result.size
                mDataList.addAll(result)
            }
            mAdapter?.setList(mDataList)
            findViewById<View>(R.id.llNoData)?.visibility =
                if (mDataList.isEmpty()) View.VISIBLE else View.GONE
            if (offset >= total) {
                smartLayout.setEnableLoadMore(false)
            }
        }
    }
}
