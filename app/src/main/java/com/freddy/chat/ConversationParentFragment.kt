package com.freddy.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.cboda.intelligent.base.BaseBroadFragment
import com.cboda.intelligent.databinding.ConversationParentFragmentBinding
import com.cboda.intelligent.viewmodel.ConversationParentViewModel
import com.freddy.chat.event.CEventCenter
import com.freddy.chat.event.Events
import com.freddy.chat.event.I_CEventListener
import com.freddy.im.db.ImLocalStore
import com.freddy.im.db.OnLoadCallback
import com.freddy.im.http.ImMessageHttpExample
import com.freddy.im.http.model.im.SessionItem

class ConversationParentFragment : BaseBroadFragment<ConversationParentFragmentBinding, ConversationParentViewModel>(), I_CEventListener {

    private val chatEvents = arrayOf<String?>(Events.NOTIFY_CONVERSATION_LIST, Events.SESSION_LIST_UPDATED, Events.SESSION_REMOVED)


    override fun onCEvent(topic: String?, msgCode: Int, resultCode: Int, obj: Any?) {
        if (Events.NOTIFY_CONVERSATION_LIST == topic) {
            getServiceConversationList {}
            return
        }
        if (Events.SESSION_LIST_UPDATED == topic && obj is MutableList<*>) {
//            val updated = obj as MutableList<SessionItem?>
//            runOnUiThreadSafe(Runnable { applySessionListUpdated(updated) })
            return
        }
        if (Events.SESSION_REMOVED == topic && obj is String) {
//            runOnUiThreadSafe(Runnable { applySessionRemoved(obj) })
        }
    }

    override fun afterDestroyView() {

        CEventCenter.unregisterEventListener(this, chatEvents)
    }

    override fun initView() {
        CEventCenter.registerEventListener(this, chatEvents)
    }

    private fun getLocalConversationList(callBack: (List<SessionItem>?) -> Unit) {
        ImLocalStore.loadSessions(OnLoadCallback { callBack.invoke(it) })
    }

    private fun getServiceConversationList(callBack: (List<SessionItem>?) -> Unit) {
        ImMessageHttpExample.requestGetConversationList(0, 0, { content -> callBack.invoke(content.sessionList) }, { callBack.invoke(null) })
    }


    override fun onCreateViewBinding(inflater: LayoutInflater, parent: ViewGroup?, savedInstanceState: Bundle?) = ConversationParentFragmentBinding.inflate(inflater, parent, false)

}