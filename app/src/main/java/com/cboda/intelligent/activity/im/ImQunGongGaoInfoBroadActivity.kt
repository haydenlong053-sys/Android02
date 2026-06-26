package com.cboda.intelligent.activity.im

import android.os.Bundle
import android.widget.TextView
import com.cboda.baselibrary.common.MyEventCodeEnum
import com.cboda.baselibrary.utils.MMKVBroadUtils.Companion.getGroupNotice
import com.cboda.baselibrary.utils.MMKVBroadUtils.Companion.saveGroupNotice
import com.cboda.chatui.bridge.MyBroadEvent
import com.cboda.intelligent.R
import com.cboda.intelligent.base.BaseSimple2BroadActivity
import com.freddy.im.http.ImGroupHttpExample
import com.freddy.im.http.model.im.GroupNoticeRes
import com.freddy.im.listener.OnHttpImListener
import org.greenrobot.eventbus.EventBus

/** 自研群公告查看页（对齐野火 {@link com.cboda.intelligent.activity.QunGonggaoInfoBroadActivity}）。 */
class ImQunGongGaoInfoBroadActivity : BaseSimple2BroadActivity() {

    private var qunId = ""
    private lateinit var gonggaoxinxi: TextView

    override fun getLayoutId(): Int = R.layout.activity_qun_gong_broad_gao

    override fun showTransparentTopTitleLayout(): Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gonggaoxinxi = findViewById(R.id.gonggaoxinxi)
        qunId = intent?.getStringExtra("qunid") ?: ""
        getToolbarTitle()?.text = getString(R.string.qungonggapo)
        showCacheGroupNoticeData()
        loadGroupNoticeFromServer()
    }

    private fun showCacheGroupNoticeData() {
        if (qunId.isEmpty()) return
        val groupNotice = getGroupNotice(qunId)
        if (!groupNotice.isNullOrEmpty()) {
            gonggaoxinxi.text = groupNotice
        }
    }

    private fun loadGroupNoticeFromServer() {
        if (qunId.isEmpty()) return
        ImGroupHttpExample.requestGetGroupNotice(qunId, object : OnHttpImListener<GroupNoticeRes> {
            override fun onHttpImSuccess(result: GroupNoticeRes) {
                runOnUiThread {
                    val content = result.content?.trim().orEmpty()
                    gonggaoxinxi.text = content
                    saveGroupNotice(qunId, content)
                    EventBus.getDefault().post(
                        MyBroadEvent(
                            MyEventCodeEnum.UPDATE_ANNOUNCEMENT.value,
                            content,
                            qunId
                        )
                    )
                }
            }
        }, {})
    }
}
