package com.cboda.intelligent.activity.im

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cboda.baselibrary.bean.CommonDataListBroadBean
import com.cboda.baselibrary.utils.APP_TAG
import com.cboda.baselibrary.utils.eLogI
import com.cboda.intelligent.R
import com.cboda.intelligent.adapter.QunUserBroadAdapter
import com.cboda.intelligent.base.BaseSimpleBroadActivity
import com.cboda.intelligent.utils.JumpBroadUtils
import com.freddy.im.db.ImLocalStore
import com.freddy.im.db.mapper.GroupUserVoMapper
import com.freddy.im.group.GroupRole
import com.freddy.im.http.ImGroupHttpExample
import com.freddy.im.http.ImMessageHttpExample
import com.freddy.im.http.model.im.GroupMemberListContent
import com.freddy.im.http.model.im.GroupUserVo
import com.freddy.im.listener.OnHttpImListener
import com.freddy.im.message.ImGroupMemberChangeManager
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener

/** 自研群成员全列表（对齐野火 {@link com.cboda.intelligent.activity.QunUserList2BroadActivity}）。 */
class ImQunUserList2BroadActivity : BaseSimpleBroadActivity() {

    private var qunId = ""
    private var myRole = GroupRole.MEMBER
    private var mAdapter: QunUserBroadAdapter? = null
    private lateinit var smartLayout: SmartRefreshLayout
    private var anchor = 0
    private var total = 0
    private var mDataList = arrayListOf<CommonDataListBroadBean.CommonBean>()

    override fun getLayoutId(): Int = R.layout.activity_qun_user_broad_list2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        qunId = intent?.getStringExtra("qunid") ?: ImGroupIntent.getGroupId(intent) ?: ""
        myRole = intent?.getIntExtra(ImGroupIntent.EXTRA_MY_ROLE, GroupRole.MEMBER) ?: GroupRole.MEMBER
        smartLayout = findViewById(R.id.smart)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)

        mAdapter = QunUserBroadAdapter(R.layout.item_qun_user_broad_layout)
        recyclerView.layoutManager = GridLayoutManager(this, 4)
        recyclerView.adapter = mAdapter
        mAdapter?.setOnItemClickListener { _, _, position ->
            val item = mAdapter?.getItem(position) ?: return@setOnItemClickListener
            JumpBroadUtils.startImQunGerenPageActivity(
                this, qunId, item.userId ?: "", item.type ?: "", myRole
            )
        }
        smartLayout.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onRefresh(refreshLayout: RefreshLayout) {
                anchor = 0
                total = 0
                smartLayout.setEnableLoadMore(true)
                loadMembers()
                refreshLayout.setNoMoreData(false)
            }

            override fun onLoadMore(refreshLayout: RefreshLayout) {
                if (anchor >= total) {
                    refreshLayout.setNoMoreData(true)
                    return
                }
                loadMembers()
            }
        })
        smartLayout.setEnableLoadMore(true)
        //此处仅用加载更多
        smartLayout.setEnableRefresh(false)
        loadMembers()
    }

    private fun loadMembers() {
        if (TextUtils.isEmpty(qunId)) {
            return
        }
        fetchMemberListPage(qunId, GroupMemberListContent.PAGE_SIZE_50)
    }


    private fun fetchMemberListPage(
        groupId: String,
        count: Int
    ) {
        ImGroupHttpExample.requestGetGroupMemberList(
            groupId, anchor, count,
            object : OnHttpImListener<GroupMemberListContent?> {
                override fun onHttpImSuccess(content: GroupMemberListContent) {
                    smartLayout.finishRefresh()
                    smartLayout.finishLoadMore()
                    total = content.total
                    content.members?.apply {
                        val result = ImGroupMemberUiMapper.toCommonBeans(content.members)
                        if (anchor == 0) {
                            mDataList.clear()
                        }
                        if (this.isNotEmpty()) {
                            anchor =content.anchor
                        }
//                        if (openQyKj == "1") {
//                            this.forEach { item ->
//                                if (item.type == "1" || item.type == "2") {
//                                    mDataList.add(item)
//                                }
//                            }
//                        } else {
//                            mDataList.addAll(this)
//                        }
                        mDataList.addAll(result)
                        mAdapter?.setList(mDataList)
                    }
                    findViewById<View>(R.id.llNoData)?.visibility = if (mDataList.isEmpty()) View.VISIBLE else View.GONE
                    if (anchor >= total) {
                        smartLayout.setEnableLoadMore(false)
                    }
                }
            }, Runnable {
                eLogI(APP_TAG,("memberList 失败 groupId=" + groupId
                        + " anchor=" + anchor))
            })
    }

}
