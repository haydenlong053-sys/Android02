package com.freddy.chat

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cboda.baselibrary.bean.GlobalNoticeBroadBean
import com.cboda.chatui.conversationlist.MyHeaderBroadAdapter
import com.cboda.intelligent.R
import org.greenrobot.eventbus.EventBus

/** 单聊会话列表（含顶部系统公告，对齐野火 ConversationSingleBroadFragment） */
class ConversationSingleFragment : BaseConversationSessionFragment() {
    override val sessionCategory: ConversationSessionCategory = ConversationSessionCategory.SINGLE

    private var headerAdapter: MyHeaderBroadAdapter? = null
    private var globalNoticeBean: GlobalNoticeBroadBean? = null

    override fun contentLayout(): Int = R.layout.conversation_single_session_list_fragment

    override fun shouldEnrichSessionDisplay(): Boolean = true

    override fun afterViews(view: View) {
        super.afterViews(view)
        val rvHead = view.findViewById<RecyclerView>(R.id.mRvHead) ?: return
        headerAdapter = MyHeaderBroadAdapter(globalNoticeBean) { notice ->
            EventBus.getDefault().post(notice ?: GlobalNoticeBroadBean())
        }
        rvHead.layoutManager = LinearLayoutManager(requireContext())
        rvHead.isNestedScrollingEnabled = false
        rvHead.adapter = headerAdapter
    }

    /** 由 MessageBroadFragment 刷新最新系统公告摘要。 */
    fun setGlobalNoticeBean(noticeBean: GlobalNoticeBroadBean?) {
        globalNoticeBean = noticeBean
        if (noticeBean != null) {
            headerAdapter?.refreshHeaderData(noticeBean)
        }
    }
}
