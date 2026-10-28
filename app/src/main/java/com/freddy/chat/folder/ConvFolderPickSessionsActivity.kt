package com.freddy.chat.folder

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cboda.baselibrary.toast.ToastBroadUtils
import com.cboda.baselibrary.widge.VpGradeView
import com.cboda.chatui.third.utils.TimeBroadBroadUtils
import com.cboda.intelligent.R
import com.cboda.intelligent.base.BaseSimple2BroadActivity
import com.freddy.chat.im.IMSClientBootstrap
import com.freddy.chat.im.MessageProcessor
import com.freddy.chat.utils.MessageAvatarBinder
import com.freddy.chat.utils.WeChatUiHelper
import com.freddy.im.db.ImLocalStore
import com.freddy.im.http.model.im.ConvFolderItem
import com.freddy.im.http.model.im.SessionItem
import com.freddy.im.message.ConvFolderSyncHelper
import com.freddy.im.message.ImMessageManager

/**
 * 向分组添加 / 从分组移出会话（多选）。
 */
class ConvFolderPickSessionsActivity : BaseSimple2BroadActivity() {

    companion object {
        private const val EXTRA_FOLDER_ID = "folder_id"
        private const val EXTRA_MODE_REMOVE = "mode_remove"

        fun startAdd(context: Context, folderId: String) {
            context.startActivity(
                Intent(context, ConvFolderPickSessionsActivity::class.java)
                    .putExtra(EXTRA_FOLDER_ID, folderId)
                    .putExtra(EXTRA_MODE_REMOVE, false),
            )
        }

        fun startRemove(context: Context, folderId: String) {
            context.startActivity(
                Intent(context, ConvFolderPickSessionsActivity::class.java)
                    .putExtra(EXTRA_FOLDER_ID, folderId)
                    .putExtra(EXTRA_MODE_REMOVE, true),
            )
        }
    }

    private var folderId = ""
    private var folderName = ""
    private var removeMode = false
    private val selectedIds = LinkedHashSet<String>()
    private var allSessions: List<SessionItem> = emptyList()
    private var folderSessionIds: Set<String> = emptySet()
    private lateinit var adapter: PickAdapter
    private lateinit var btnConfirm: TextView
    private lateinit var tvSubtitle: TextView
    private val currentUserId: String?
        get() = IMSClientBootstrap.getInstance().currentUserId

    override fun showOrHideTitleLayout(): Boolean = false
    override fun showTransparentTopTitleLayout(): Boolean {
        return true
    }

    override fun getLayoutId(): Int = R.layout.activity_conv_folder_pick

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setRootBgColor(R.color.color_msg_page_bg)
        folderId = intent.getStringExtra(EXTRA_FOLDER_ID).orEmpty()
        removeMode = intent.getBooleanExtra(EXTRA_MODE_REMOVE, false)
        if (folderId.isEmpty()) {
            finish()
            return
        }

        findViewById<View>(R.id.iv_back).setOnClickListener { finish() }
        findViewById<View>(R.id.btn_cancel).setOnClickListener { finish() }
        val tvTitle = findViewById<TextView>(R.id.tv_title)
        tvSubtitle = findViewById(R.id.tv_subtitle)
        btnConfirm = findViewById(R.id.btn_confirm)
        tvTitle.setText(
            if (removeMode) R.string.conv_folder_pick_remove_title
            else R.string.conv_folder_add_title,
        )

        val rv = findViewById<RecyclerView>(R.id.rv_sessions)
        adapter = PickAdapter()
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        btnConfirm.setOnClickListener { confirm() }
        refreshConfirm()

        ImLocalStore.loadConvFolders { _, folders ->
            val folder = folders.firstOrNull { it.id == folderId }
            folderName = folder?.name.orEmpty()
            folderSessionIds = folder?.sessionIds?.filter { !it.isNullOrEmpty() }?.toHashSet().orEmpty()
            updateSubtitle()
            ImLocalStore.loadSessions { sessions ->
                allSessions = sessions.orEmpty().filter {
                    !ImMessageManager.isFileAssistantSession(it.sessionId)
                }
                refreshList()
            }
        }
    }

    private fun filteredSessions(): List<SessionItem> {
        if (removeMode) {
            return allSessions.filter { folderSessionIds.contains(it.sessionId) }
        }
        // 创建/添加：展示全部会话，排除已在组内的
        return allSessions.filter { !folderSessionIds.contains(it.sessionId) }
    }

    private fun refreshList() {
        adapter.submit(filteredSessions())
    }

    private fun updateSubtitle() {
        if (selectedIds.isEmpty()) {
            tvSubtitle.isVisible = false
            return
        }
        tvSubtitle.isVisible = true
        tvSubtitle.text = if (removeMode) {
            getString(R.string.conv_folder_pick_remove_subtitle, selectedIds.size, folderName)
        } else {
            getString(R.string.conv_folder_pick_add_subtitle, selectedIds.size, folderName)
        }
    }

    private fun refreshConfirm() {
        val n = selectedIds.size
        btnConfirm.text = if (removeMode) {
            getString(R.string.conv_folder_remove_n, n)
        } else {
            getString(R.string.conv_folder_confirm_n, n)
        }
        btnConfirm.isEnabled = n > 0
        btnConfirm.alpha = if (n > 0) 1f else 0.5f
        updateSubtitle()
    }

    private fun confirm() {
        if (selectedIds.isEmpty()) return
        val picked = HashSet(selectedIds)
        ConvFolderSyncHelper.saveWithRetry(
            { current ->
                current.map { item ->
                    if (item.id != folderId) {
                        item
                    } else {
                        item.copy().also { f ->
                            val ids = ArrayList(f.sessionIds)
                            if (removeMode) {
                                ids.removeAll(picked)
                            } else {
                                for (id in picked) {
                                    if (!ids.contains(id)) ids.add(id)
                                }
                            }
                            f.setSessionIds(ids)
                        }
                    }
                }
            },
            object : ConvFolderSyncHelper.OnSaveResult {
                override fun onSuccess(version: Long, folders: List<ConvFolderItem>) {
                    MessageProcessor.getInstance().notifyConvFoldersUpdated()
                    finish()
                }

                override fun onFail(errorCode: Int, errorMsg: String?) {
                    ToastBroadUtils.show(
                        errorMsg?.takeIf { it.isNotBlank() }
                            ?: getString(R.string.conv_folder_save_failed),
                    )
                }
            },
        )
    }

    private inner class PickAdapter : RecyclerView.Adapter<PickVH>() {
        private val items = ArrayList<SessionItem>()

        fun submit(list: List<SessionItem>) {
            items.clear()
            items.addAll(list)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PickVH {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_conversation, parent, false)
            return PickVH(v)
        }

        override fun getItemCount(): Int = items.size

        override fun onBindViewHolder(holder: PickVH, position: Int) {
            holder.bind(items[position])
        }
    }

    private inner class PickVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivCheck: ImageView = itemView.findViewById(R.id.iv_check)
        private val avatar: VpGradeView = itemView.findViewById(R.id.avatar)
        private val tvName: TextView = itemView.findViewById(R.id.tv_name)
        private val tvSubtitle: TextView = itemView.findViewById(R.id.tv_subtitle)
        private val tvTime: TextView? = itemView.findViewById(R.id.tv_time)
        private val tvUnread: TextView = itemView.findViewById(R.id.tv_unread)
        private val muteDot: View = itemView.findViewById(R.id.v_mute_dot)

        fun bind(session: SessionItem) {
            ivCheck.isVisible = true
            val sid = session.sessionId.orEmpty()
            val selected = selectedIds.contains(sid)
            ivCheck.setImageResource(
                if (selected) com.cboda.chatui.R.mipmap.ic_check_cnv_broad_select
                else com.cboda.chatui.R.mipmap.ic_check_cnv_broad_normal,
            )
            MessageAvatarBinder.bindSessionListAvatar(avatar, session, currentUserId)
            val name = WeChatUiHelper.resolveSessionListDisplayName(session, currentUserId)
            tvName.text = name.ifEmpty {
                getString(com.cboda.baselibrary.R.string.conversation_name_fallback)
            }
            tvSubtitle.text = WeChatUiHelper.formatSessionPreview(session, currentUserId)
            val millis = WeChatUiHelper.normalizeToMillis(session.lastMessageTime)
            val time = if (millis > 0L) TimeBroadBroadUtils.getMsgFormatTime(millis) else ""
            tvTime?.text = time
            tvTime?.visibility = if (TextUtils.isEmpty(time)) View.INVISIBLE else View.VISIBLE
            val unread = session.unreadCount
            if (session.isMuteNotify) {
                tvUnread.visibility = View.GONE
                muteDot.visibility = if (unread > 0) View.VISIBLE else View.GONE
            } else if (unread > 0) {
                muteDot.visibility = View.GONE
                tvUnread.visibility = View.VISIBLE
                tvUnread.text = if (unread > 99) "99+" else unread.toString()
            } else {
                tvUnread.visibility = View.GONE
                muteDot.visibility = View.GONE
            }
            itemView.setOnClickListener {
                if (sid.isEmpty()) return@setOnClickListener
                if (!selectedIds.add(sid)) {
                    selectedIds.remove(sid)
                }
                refreshConfirm()
                adapter.notifyItemChanged(bindingAdapterPosition)
            }
        }
    }
}
