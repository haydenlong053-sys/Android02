package com.cboda.intelligent.activity.im

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cboda.baselibrary.bean.CommonDataListBroadBean
import com.cboda.baselibrary.toast.ToastBroadUtils
import com.cboda.intelligent.R
import com.cboda.intelligent.adapter.QunMemberBroadAdapter
import com.cboda.intelligent.adapter.YiXuanQunMemberBroadAdapter
import com.cboda.intelligent.base.BaseSimple2BroadActivity
import com.cboda.intelligent.utils.isSubmitCanGo
import com.cboda.intelligent.widge.WrapContentLinearLayoutBroadManager
import com.freddy.im.group.GroupRole
import com.freddy.im.http.ImGroupHttpExample
import com.freddy.im.http.ImMessageHttpExample
import com.freddy.im.http.model.im.BaseMsgResponse
import com.freddy.im.http.model.im.GroupUserVo
import com.freddy.im.http.model.im.UserInfoItem
import com.freddy.im.http.model.im.UserInfoListRes
import com.freddy.im.listener.OnHttpImListener

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
    private lateinit var tvAgree: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerView2: RecyclerView
    private lateinit var edtSousuo: EditText

    override fun showBgTopTitleLayout(): Boolean = true
    override fun getLayoutId(): Int = R.layout.activity_xu_broad_an_ze_lian_xi_ren

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setRootBgColor(R.color.white)
        fromPage = intent?.getStringExtra("fromPage") ?: ""
        qunId = intent?.getStringExtra("qunid") ?: ""
        userType = intent?.getStringExtra("userType") ?: ""
        tvAgree = findViewById(R.id.tvAgree)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView2 = findViewById(R.id.recyclerView2)
        edtSousuo = findViewById(R.id.edtSousuo)
        findViewById<View>(R.id.indexBar)?.isVisible = fromPage == "add"
        findViewById<View>(R.id.smartLayout)?.isVisible = false

        getToolbarTitle()?.text = getString(R.string.xuanzelianxiren)
        tvAgree.isSubmitCanGo(false)

        mAdapter = QunMemberBroadAdapter(R.layout.item_qun_member_broad_layout, fromPage)
        recyclerView.layoutManager = LinearLayoutManager(this)
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
        edtSousuo.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                loadData()
                true
            } else false
        }
        edtSousuo.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                if (fromPage == "add") loadData()
            }
        })
        loadData()
    }

    private fun loadData() {
        if (fromPage == "remove") {
            loadRemoveCandidates()
        } else if (fromPage == "add") {
            loadAddCandidates()
        }
    }

    private fun loadRemoveCandidates() {
        ImGroupHttpExample.requestGetAllMembers(qunId, object : OnHttpImListener<List<GroupUserVo>> {
            override fun onHttpImSuccess(result: List<GroupUserVo>) {
                runOnUiThread {
                    mDataList.clear()
                    result.forEach { member ->
                        val bean = ImGroupMemberUiMapper.toCommonBean(member)
                        if (userType == "1" && member.role == GroupRole.OWNER) {
                            bean.isZy = "1"
                        } else if (userType == "2" && (member.role == GroupRole.OWNER || member.role == GroupRole.ADMIN)) {
                            bean.isZy = "1"
                        }
                        if (member.role != GroupRole.OWNER) {
                            mDataList.add(bean)
                        }
                    }
                    mAdapter?.setList(mDataList)
                }
            }
        }, null)
    }

    private fun loadAddCandidates() {
        val keyword = edtSousuo.text?.toString()?.trim().orEmpty()
        ImMessageHttpExample.requestGetUserList(
            if (keyword.isEmpty()) "用户" else keyword,
            1,
            50,
            object : OnHttpImListener<BaseMsgResponse<UserInfoListRes>> {
                override fun onHttpImSuccess(result: BaseMsgResponse<UserInfoListRes>) {
                    val list = result.content?.userList ?: emptyList()
                    runOnUiThread {
                        mDataList.clear()
                        list.forEach { user: UserInfoItem ->
                            val bean = CommonDataListBroadBean.CommonBean()
                            bean.userId = user.userId
                            bean.name = user.userName
                            bean.icon = user.icon ?: user.avatar ?: user.headImg ?: user.headUrl
                            mDataList.add(bean)
                        }
                        mAdapter?.setList(mDataList)
                    }
                }
            },
            null
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
}
