package com.cboda.intelligent.activity.im

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.cboda.baselibrary.bean.CommonDataListBroadBean
import com.cboda.baselibrary.toast.ToastBroadUtils
import com.cboda.intelligent.R
import com.cboda.intelligent.adapter.QunGuanLiBroadAdapter
import com.cboda.intelligent.base.BaseSimple2BroadActivity
import com.cboda.intelligent.utils.JumpBroadUtils
import com.cboda.intelligent.widge.WrapContentLinearLayoutBroadManager
import com.freddy.chat.event.CEventCenter
import com.freddy.chat.event.Events
import com.freddy.chat.event.I_CEventListener
import com.freddy.chat.im.IMSClientBootstrap
import com.freddy.im.http.ImGroupHttpExample
import com.freddy.im.http.model.im.GroupUserVo
import com.freddy.im.listener.OnHttpImListener

/**
 * 自研群管理员（对齐野火 {@link com.cboda.intelligent.activity.QunManagerBroadActivity}）。
 */
class ImQunManagerBroadActivity : BaseSimple2BroadActivity(), I_CEventListener {

    companion object {
        private val EVENTS = arrayOf(Events.GROUP_MEMBERS_ROLE_UPDATE)
    }

    private var qunId = ""
    private var mAdapter: QunGuanLiBroadAdapter? = null
    private val mDataList = arrayListOf<CommonDataListBroadBean.CommonBean>()
    private lateinit var tvPhoneLogin: View
    private lateinit var recyclerView: RecyclerView

    override fun getLayoutId(): Int = R.layout.activity_quan_guan_broad_li

    override fun showTransparentTopTitleLayout(): Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setRootBgColor(R.color.white)
        recyclerView = findViewById(R.id.recyclerView)
        tvPhoneLogin = findViewById(R.id.tvPhoneLogin)
        qunId = intent?.getStringExtra("qunid") ?: ""

        mAdapter = QunGuanLiBroadAdapter(R.layout.item_qun_guan_broad_li_layout)
        recyclerView.layoutManager = WrapContentLinearLayoutBroadManager(this)
        recyclerView.adapter = mAdapter
        mAdapter?.setList(mDataList)
        mAdapter?.setOnItemClickListener { _, _, position ->
            if (position !in mDataList.indices) return@setOnItemClickListener
            val bean = mDataList[position]
            if (bean.type == "1" || bean.isZy == "1") return@setOnItemClickListener
            removeManager(bean.userId ?: return@setOnItemClickListener)
        }

        tvPhoneLogin.setOnClickListener {
            JumpBroadUtils.startImQunMemberListActivity(this, qunId, "selectManager", "1")
        }
        CEventCenter.registerEventListener(this, EVENTS)
    }

    override fun onDestroy() {
        CEventCenter.unregisterEventListener(this, EVENTS)
        super.onDestroy()
    }

    override fun onCEvent(topic: String?, msgCode: Int, resultCode: Int, obj: Any?) {
        if (topic == Events.GROUP_MEMBERS_ROLE_UPDATE && obj is String && obj == qunId) {
            runOnUiThread { loadManagers() }
        }
    }

    override fun onResume() {
        super.onResume()
        loadManagers()
    }

    private fun loadManagers() {
        val userId = IMSClientBootstrap.getInstance().currentUserId
        if (qunId.isEmpty() || userId.isNullOrEmpty()) return
        ImGroupHttpExample.requestGetGroupManagers(
            qunId,
            userId,
            object : OnHttpImListener<List<GroupUserVo>> {
                override fun onHttpImSuccess(result: List<GroupUserVo>) {
                    runOnUiThread {
                        mDataList.clear()
                        mDataList.addAll(ImGroupMemberUiMapper.toCommonBeans(result).onEach { bean ->
                            if (bean.type == "1") {
                                bean.isZy = "1"
                            }
                        })
                        mAdapter?.setList(mDataList)
                    }
                }
            },
            null,
        )
    }

    private fun removeManager(userId: String) {
        ImGroupHttpExample.requestSetGroupManager(
            qunId,
            listOf(userId),
            false,
            object : OnHttpImListener<String> {
                override fun onHttpImSuccess(result: String) {
                    runOnUiThread { loadManagers() }
                }
            },
            {
                runOnUiThread {
                    ToastBroadUtils.show(getString(R.string.group_apply_action_failed))
                }
            },
        )
    }
}
