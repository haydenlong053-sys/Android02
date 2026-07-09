package com.cboda.intelligent.activity.im

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cboda.baselibrary.bean.CommonDataListBroadBean
import com.cboda.baselibrary.toast.ToastBroadUtils
import com.cboda.intelligent.R
import com.cboda.intelligent.adapter.QunMemberBroadAdapter
import com.cboda.intelligent.adapter.YiXuanQunMemberBroadAdapter
import com.cboda.intelligent.base.BaseSimple2BroadActivity
import com.cboda.intelligent.utils.isSubmitCanGo
import com.cboda.intelligent.viewmodel.CommViewBroadModel
import com.cboda.intelligent.widge.WrapContentLinearLayoutBroadManager
import com.cboda.susindexbar.IndexBar.widget.IndexBar2
import com.freddy.chat.im.IMSClientBootstrap
import com.freddy.im.db.ImLocalStore
import com.freddy.im.db.mapper.GroupUserVoMapper
import com.freddy.im.http.ImGroupHttpExample
import com.freddy.im.http.model.im.GroupFriendBean
import com.freddy.im.listener.OnHttpImListener
import com.scwang.smart.refresh.layout.SmartRefreshLayout

/**
 * 自研加人/踢人（对齐野火 {@link com.cboda.intelligent.activity.QunMemberListBroadActivity}）。
 */
class ImQunMemberListBroadActivity : BaseSimple2BroadActivity() {

    private var fromPage = ""
    private var qunId = ""
    private var userType = ""
    private var mAdapter: QunMemberBroadAdapter? = null
    private val mDataList = arrayListOf<CommonDataListBroadBean.CommonBean>()
    private val mDataList3 = arrayListOf<CommonDataListBroadBean.CommonBean>()
    private var mAdapter2: YiXuanQunMemberBroadAdapter? = null
    private var mManager: LinearLayoutManager? = null
    private lateinit var tvSideBarHint: TextView
    private lateinit var indexBar: IndexBar2
    private lateinit var tvAgree: TextView
    private lateinit var smartLayout: SmartRefreshLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerView2: RecyclerView
    private lateinit var edtSousuo: EditText
    private var viewModel: CommViewBroadModel? = null
    private var pageNo = 1
    private val searchHandler by lazy { Handler(Looper.getMainLooper()) }
    private var searchRunnable: Runnable? = null

    override fun showBgTopTitleLayout(): Boolean = true
    override fun getLayoutId(): Int = R.layout.activity_xu_broad_an_ze_lian_xi_ren

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setRootBgColor(R.color.white)
        fromPage = intent?.getStringExtra("fromPage") ?: ""
        qunId = intent?.getStringExtra("qunid") ?: ""
        userType = intent?.getStringExtra("userType") ?: ""
        viewModel = ViewModelProvider(this)[CommViewBroadModel::class.java]
        tvAgree = findViewById(R.id.tvAgree)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView2 = findViewById(R.id.recyclerView2)
        edtSousuo = findViewById(R.id.edtSousuo)
        smartLayout = findViewById(R.id.smartLayout)
        indexBar = findViewById(R.id.indexBar)
        tvSideBarHint = findViewById(R.id.tvSideBarHint)
        indexBar.isVisible = fromPage == "add"
        smartLayout.setEnableRefresh(false)
        smartLayout.setEnableLoadMore(false)

        getToolbarTitle()?.text = getString(R.string.xuanzelianxiren)
        tvAgree.isSubmitCanGo(false)

        mManager = LinearLayoutManager(this)
        indexBar.setmPressedShowTextView(tvSideBarHint)
            .setNeedRealIndex(true)
            .setmLayoutManager(mManager)
        mAdapter = QunMemberBroadAdapter(R.layout.item_qun_member_broad_layout, fromPage)
        recyclerView.layoutManager = mManager
        recyclerView.adapter = mAdapter
        mAdapter?.setOnItemClickListener { _, _, position ->
            if (position !in mDataList.indices) return@setOnItemClickListener
            if (mDataList[position].isZy == "1") return@setOnItemClickListener
            mDataList[position].isSelect = !mDataList[position].isSelect
            mAdapter?.setList(mDataList)
            if (fromPage == "add") {
                if (mDataList[position].isSelect) mDataList3.add(mDataList[position])
                else mDataList3.removeAll { it.userId == mDataList[position].userId }
                updateYiXuan()
            }
            checkCanGo()
        }

        mAdapter2 = YiXuanQunMemberBroadAdapter(R.layout.item_yi_xu_broad_an_qun_member_layout)
        recyclerView2.layoutManager = WrapContentLinearLayoutBroadManager(
            this, LinearLayoutManager.HORIZONTAL, false
        )
        recyclerView2.adapter = mAdapter2

        tvAgree.setOnClickListener { submit() }
        setupSearch()

        loadData()
    }

    private fun setupSearch() {
        edtSousuo.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                triggerSearch()
                true
            } else {
                false
            }
        }
        edtSousuo.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (fromPage != "add" && fromPage != "remove" && fromPage != "selectManager") return
                searchRunnable?.let { runnable -> searchHandler.removeCallbacks(runnable) }
                searchRunnable = Runnable { triggerSearch() }.also { runnable ->
                    searchHandler.postDelayed(runnable, SEARCH_DEBOUNCE_MS)
                }
            }
            override fun afterTextChanged(s: Editable?) = Unit
        })
    }

    private fun loadData() {
        when (fromPage) {
            "add" -> loadAddGroupData()
            "remove" -> loadRemoveMembers()
            "selectManager" -> loadSelectManagerMembers()
        }
    }

    private fun currentSearchKey(): String = edtSousuo.text?.toString()?.trim().orEmpty()

    private fun triggerSearch() {
        searchRunnable?.let { runnable -> searchHandler.removeCallbacks(runnable) }
        when (fromPage) {
            "add" -> {
                pageNo = 1
                mDataList.clear()
                mAdapter?.setList(mDataList)
                checkCanGo()
                loadAddGroupData()
            }
            "remove" -> {
                val keyword = currentSearchKey()
                if (keyword.isEmpty()) {
                    loadRemoveMembers()
                } else {
                    searchRemoveMembers(keyword)
                }
            }
            "selectManager" -> {
                val keyword = currentSearchKey()
                if (keyword.isEmpty()) {
                    loadSelectManagerMembers()
                } else {
                    searchSelectManagerMembers(keyword)
                }
            }
        }
    }

    private fun loadAddGroupData() {
        ImGroupHttpExample.requestGroupFriends(
            qunId,
            currentSearchKey(),
            object : OnHttpImListener<List<GroupFriendBean>> {
                override fun onHttpImSuccess(result: List<GroupFriendBean>) {
                    runOnUiThread {
                        smartLayout.finishRefresh()
                        smartLayout.finishLoadMore()
                        mDataList.clear()
                        val converted = ImGroupMemberUiMapper.toCommonBeansFromGroupFriends(result)
                        val existingUserIds = hashSetOf<String>()
                        val dedupedList = converted.filter { bean ->
                            val userId = bean.userId
                            !userId.isNullOrEmpty() && existingUserIds.add(userId)
                        }
                        val selectedIds = mDataList3.mapNotNull { it.userId }.toHashSet()
                        dedupedList.forEach { bean ->
                            if (bean.userId in selectedIds) {
                                bean.isSelect = true
                            }
                        }
                        mDataList.addAll(dedupedList)
                        indexBar.setmSourceDatas(mDataList)
                            .setHeaderViewCount(0)
                            .invalidate()
                        mAdapter?.setList(mDataList)
                        if (mDataList.isEmpty()) {
                            mAdapter?.setEmptyView(R.layout.list_empty_broad_layout)
                        }
                        smartLayout.setEnableLoadMore(false)
                        checkCanGo()
                    }
                }
            }, null)
    }

    /** 踢人：本地群成员库加载（对齐 ImXuanQunJinYanBroadActivity）。 */
    private fun loadRemoveMembers() {
        if (qunId.isEmpty()) return
        val currentUserId = IMSClientBootstrap.getInstance().currentUserId
        ImLocalStore.loadGroupMembersMap(qunId) { map ->
            val beans = ImGroupMemberUiMapper.toCommonBeansFromMemberItems(map.values.toList())
            val filtered = beans.filter { it.userId != currentUserId }
            val result = ImGroupMemberUiMapper.applyRemoveKickRules(filtered, userType, currentUserId)
            runOnUiThread { submitRemoveList(result) }
        }
    }

    /** 踢人：ES 模糊搜索群成员（api/im/group/searchMembers）。 */
    private fun searchRemoveMembers(keyword: String) {
        if (qunId.isEmpty()) return
        val currentUserId = IMSClientBootstrap.getInstance().currentUserId
        ImGroupHttpExample.requestSearchMembers(
            qunId,
            keyword,
            object : OnHttpImListener<List<com.freddy.im.http.model.im.GroupUserVo>> {
                override fun onHttpImSuccess(result: List<com.freddy.im.http.model.im.GroupUserVo>) {
                    if (keyword != currentSearchKey()) return
                    val items = GroupUserVoMapper.toItems(qunId, result)
                    val beans = ImGroupMemberUiMapper.toCommonBeansFromMemberItems(items)
                        .filter { it.userId != currentUserId }
                    val sorted = ImGroupMemberUiMapper.applyRemoveKickRules(beans, userType, currentUserId)
                    runOnUiThread { submitRemoveList(sorted) }
                }
            },
            {
                runOnUiThread {
                    if (keyword == currentSearchKey()) {
                        submitRemoveList(emptyList())
                    }
                }
            },
        )
    }

    private fun submitRemoveList(beans: List<CommonDataListBroadBean.CommonBean>) {
        val selectedIds = mDataList.filter { it.isSelect }.mapNotNull { it.userId }.toHashSet()
        mDataList.clear()
        beans.forEach { bean ->
            if (bean.userId in selectedIds && bean.isZy != "1") {
                bean.isSelect = true
            }
        }
        mDataList.addAll(beans)
        mAdapter?.setList(mDataList)
        if (mDataList.isEmpty()) {
            mAdapter?.setEmptyView(R.layout.list_empty_broad_layout)
        }
        checkCanGo()
    }

    /** 设置管理员：本地群成员库加载可候选普通成员。 */
    private fun loadSelectManagerMembers() {
        if (qunId.isEmpty()) return
        ImLocalStore.loadGroupMembersMap(qunId) { map ->
            val beans = ImGroupMemberUiMapper.toCommonBeansFromMemberItems(map.values.toList())
            val result = ImGroupMemberUiMapper.applySelectManagerRules(beans)
            runOnUiThread { submitRemoveList(result) }
        }
    }

    /** 设置管理员：ES 模糊搜索群成员。 */
    private fun searchSelectManagerMembers(keyword: String) {
        if (qunId.isEmpty()) return
        ImGroupHttpExample.requestSearchMembers(
            qunId,
            keyword,
            object : OnHttpImListener<List<com.freddy.im.http.model.im.GroupUserVo>> {
                override fun onHttpImSuccess(result: List<com.freddy.im.http.model.im.GroupUserVo>) {
                    if (keyword != currentSearchKey()) return
                    val items = GroupUserVoMapper.toItems(qunId, result)
                    val beans = ImGroupMemberUiMapper.toCommonBeansFromMemberItems(items)
                    val sorted = ImGroupMemberUiMapper.applySelectManagerRules(beans)
                    runOnUiThread { submitRemoveList(sorted) }
                }
            },
            {
                runOnUiThread {
                    if (keyword == currentSearchKey()) {
                        submitRemoveList(emptyList())
                    }
                }
            },
        )
    }


    private fun updateYiXuan() {
        mAdapter2?.setList(mDataList3)
        recyclerView2.isVisible = mDataList3.isNotEmpty()
    }

    private fun checkCanGo() {
        val any = mDataList.any { it.isSelect }
        tvAgree.isSubmitCanGo(any)
    }

    private fun submit() {
        val ids = mDataList.filter { it.isSelect }.mapNotNull { it.userId }
        if (ids.isEmpty()) {
            ToastBroadUtils.show(getString(R.string.qingxuanzelianxiren))
            return
        }
        when (fromPage) {
            "remove" -> {
                ImGroupHttpExample.requestKickOut(qunId, ids, object : OnHttpImListener<String> {
                    override fun onHttpImSuccess(result: String) {
                        runOnUiThread { finish() }
                    }
                }, {
                    runOnUiThread {
                        ToastBroadUtils.show(getString(R.string.group_kick_failed))
                    }
                })
            }
            "selectManager" -> {
                ImGroupHttpExample.requestSetGroupManager(
                    qunId,
                    ids,
                    true,
                    object : OnHttpImListener<String> {
                        override fun onHttpImSuccess(result: String) {
                            runOnUiThread { finish() }
                        }
                    },
                    {
                        runOnUiThread {
                            ToastBroadUtils.show(getString(R.string.group_apply_action_failed))
                        }
                    },
                )
            }
            "add" -> {
                ImGroupHttpExample.requestAddMembers(qunId, ids, object : OnHttpImListener<com.freddy.im.http.model.im.GroupVo> {
                    override fun onHttpImSuccess(result: com.freddy.im.http.model.im.GroupVo) {
                        runOnUiThread { finish() }
                    }
                }, {
                    runOnUiThread {
                        ToastBroadUtils.show(getString(R.string.group_add_member_failed))
                    }
                })
            }
        }
    }

    override fun onDestroy() {
        searchRunnable?.let { runnable -> searchHandler.removeCallbacks(runnable) }
        super.onDestroy()
    }

    companion object {
        private const val SEARCH_DEBOUNCE_MS = 600L
    }
}
