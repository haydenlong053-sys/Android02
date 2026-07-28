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
import com.cboda.intelligent.holder.CommanConfimBroadHolder
import com.cboda.intelligent.utils.isSubmitCanGo
import com.cboda.intelligent.viewmodel.CommViewBroadModel
import com.cboda.intelligent.widge.WrapContentLinearLayoutBroadManager
import com.cboda.susindexbar.IndexBar.widget.IndexBar2
import com.freddy.chat.im.IMSClientBootstrap
import com.freddy.chat.im.MessageProcessor
import com.freddy.im.db.ImLocalStore
import com.freddy.im.db.mapper.GroupUserVoMapper
import com.freddy.im.http.ImGroupHttpExample
import com.freddy.im.http.model.im.BaseMsgResponse
import com.freddy.im.http.model.im.GroupFriendBean
import com.freddy.im.listener.OnHttpImListener
import com.freddy.im.message.ImGroupTransferManager
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

        getToolbarTitle()?.text = when (fromPage) {
            "selectManager" -> getString(R.string.qunguanliyuan)
            "selectNewOwner" -> getString(R.string.xuanxinqunzhu)
            ImGroupIntent.FROM_PAGE_SELECT_CALL_MEMBERS -> getString(R.string.select_group_members)
            else -> getString(R.string.xuanzelianxiren)
        }
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
            if (fromPage == "selectNewOwner") {
                for (i in mDataList.indices) {
                    mDataList[i].isSelect = i == position
                }
            } else {
                mDataList[position].isSelect = !mDataList[position].isSelect
            }
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
                if (fromPage != "add" && fromPage != "remove"
                    && fromPage != "selectManager" && fromPage != "selectNewOwner"
                    && fromPage != ImGroupIntent.FROM_PAGE_SELECT_CALL_MEMBERS) return
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
            "selectNewOwner" -> loadSelectNewOwnerMembers()
            ImGroupIntent.FROM_PAGE_SELECT_CALL_MEMBERS -> loadSelectCallMembers()
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
            "selectNewOwner" -> {
                val keyword = currentSearchKey()
                if (keyword.isEmpty()) {
                    loadSelectNewOwnerMembers()
                } else {
                    searchSelectNewOwnerMembers(keyword)
                }
            }
            ImGroupIntent.FROM_PAGE_SELECT_CALL_MEMBERS -> {
                val keyword = currentSearchKey()
                if (keyword.isEmpty()) {
                    loadSelectCallMembers()
                } else {
                    searchSelectCallMembers(keyword)
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

    /** 转让群主：本地群成员库加载可候选成员。 */
    private fun loadSelectNewOwnerMembers() {
        if (qunId.isEmpty()) return
        val currentUserId = IMSClientBootstrap.getInstance().currentUserId
        ImLocalStore.loadGroupMembersMap(qunId) { map ->
            val beans = ImGroupMemberUiMapper.toCommonBeansFromMemberItems(map.values.toList())
            val result = ImGroupMemberUiMapper.applySelectNewOwnerRules(beans, currentUserId)
            runOnUiThread { submitRemoveList(result) }
        }
    }

    /** 转让群主：ES 模糊搜索群成员。 */
    private fun searchSelectNewOwnerMembers(keyword: String) {
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
                    val sorted = ImGroupMemberUiMapper.applySelectNewOwnerRules(beans, currentUserId)
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


    /** 群语音通话：本地群成员库加载（排除自己）。 */
    private fun loadSelectCallMembers() {
        if (qunId.isEmpty()) return
        val currentUserId = IMSClientBootstrap.getInstance().currentUserId
        ImLocalStore.loadGroupMembersMap(qunId) { map ->
            val beans = ImGroupMemberUiMapper.toCommonBeansFromMemberItems(map.values.toList())
                .filter { it.userId != currentUserId }
            runOnUiThread { submitRemoveList(beans) }
        }
    }

    /** 群语音通话：ES 模糊搜索群成员（排除自己）。 */
    private fun searchSelectCallMembers(keyword: String) {
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
                    runOnUiThread { submitRemoveList(beans) }
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
        val selected = mDataList.filter { it.isSelect && it.isZy != "1" }
        if (selected.isEmpty()) {
            ToastBroadUtils.show(getString(R.string.qingxuanzelianxiren))
            return
        }
        when (fromPage) {
            "remove" -> {
                val ids = selected.mapNotNull { it.userId }
                ImGroupHttpExample.requestKickOut(qunId, ids, object : OnHttpImListener<String> {
                    override fun onHttpImSuccess(result: String) {
                        runOnUiThread {
                            ToastBroadUtils.show(getString(R.string.group_apply_action_success))
                            finish()
                        }
                    }
                }, {
//                    runOnUiThread {
//                        ToastBroadUtils.show(getString(R.string.group_kick_failed))
//                    }
                })
            }
            "selectManager" -> {
                val ids = selected.mapNotNull { it.userId }
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
            "selectNewOwner" -> confirmTransferOwner(selected.first())
            "add" -> {
                val ids = selected.mapNotNull { it.userId }
                ImGroupHttpExample.requestAddMembers(qunId, ids, object : OnHttpImListener<com.freddy.im.http.model.im.GroupVo> {
                    override fun onHttpImSuccess(result: com.freddy.im.http.model.im.GroupVo) {
                        runOnUiThread {
                            ToastBroadUtils.show(getString(R.string.group_apply_action_success))
                            finish()
                        }
                    }
                }, {
//                    runOnUiThread {
//                        ToastBroadUtils.show(getString(R.string.group_add_member_failed))
//                    }
                })
            }
            ImGroupIntent.FROM_PAGE_SELECT_CALL_MEMBERS -> {
                val ids = selected.mapNotNull { it.userId }
                setResult(
                    RESULT_OK,
                    android.content.Intent().putStringArrayListExtra(
                        ImGroupIntent.EXTRA_SELECTED_USER_IDS,
                        ArrayList(ids)
                    )
                )
                finish()
            }
        }
    }

    private fun confirmTransferOwner(target: CommonDataListBroadBean.CommonBean) {
        val newOwnerId = target.userId ?: return
        val displayName = target.name?.takeIf { it.isNotBlank() } ?: newOwnerId
        CommanConfimBroadHolder(
            this,
            getString(R.string.group_row_transfer),
            getString(R.string.group_transfer_confirm, displayName),
            getString(R.string.queding),
            getString(R.string.quxiao)
        ).apply {
            callBack = object : CommanConfimBroadHolder.OnConfimCallBack {
                override fun onClickConfim(isConfim: Boolean) {
                    if (isConfim) doTransferOwner(newOwnerId)
                }
            }
            show()
        }
    }

    private fun doTransferOwner(newOwnerId: String) {
        ImGroupHttpExample.requestTransferOwner(
            qunId,
            newOwnerId,
            object : OnHttpImListener<BaseMsgResponse<Void>> {
                override fun onHttpImSuccess(result: BaseMsgResponse<Void>) {
                    runOnUiThread {
                        ImGroupTransferManager.getInstance().handleTransfer(qunId, newOwnerId) { gid, sessions ->
//                            if (!sessions.isNullOrEmpty()) {
//                                MessageProcessor.getInstance().notifySessionsUpdated(sessions)
//                            }
//                            if (!gid.isNullOrEmpty()) {
//                                MessageProcessor.getInstance().notifyGroupMemberRolesUpdated(gid)
//                            }
                            MessageProcessor.getInstance().notifyConversationList()
                        }
                        ToastBroadUtils.show(getString(R.string.group_transfer_success))
                        setResult(RESULT_OK)
                        finish()
                    }
                }
            },
            {
                runOnUiThread {
                    ToastBroadUtils.show(getString(R.string.group_transfer_failed))
                }
            },
        )
    }

    override fun onDestroy() {
        searchRunnable?.let { runnable -> searchHandler.removeCallbacks(runnable) }
        super.onDestroy()
    }

    companion object {
        private const val SEARCH_DEBOUNCE_MS = 600L
    }
}
