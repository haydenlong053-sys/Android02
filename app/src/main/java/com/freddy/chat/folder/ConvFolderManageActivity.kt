package com.freddy.chat.folder

import android.content.Context
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cboda.baselibrary.toast.ToastBroadUtils
import com.cboda.intelligent.R
import com.cboda.intelligent.base.BaseSimple2BroadActivity
import com.freddy.chat.ConversationSessionCategory
import com.freddy.chat.im.MessageProcessor
import com.freddy.im.db.ImLocalStore
import com.freddy.im.http.model.im.ConvFolderItem
import com.freddy.im.http.model.im.SessionItem
import com.freddy.im.message.ConvFolderSyncHelper
import java.util.Collections

/**
 * 会话分组管理：拖拽排序 + 更多菜单 + 新建分组。
 */
class ConvFolderManageActivity : BaseSimple2BroadActivity() {

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, ConvFolderManageActivity::class.java))
        }
    }

    private val folders = ArrayList<ConvFolderItem>()
    private var sessions: List<SessionItem> = emptyList()
    private lateinit var adapter: ManageAdapter
    private var orderDirty = false

    override fun showOrHideTitleLayout(): Boolean = false
    override fun showTransparentTopTitleLayout(): Boolean {
        return true
    }

    override fun getLayoutId(): Int = R.layout.activity_conv_folder_manage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setRootBgColor(R.color.color_msg_page_bg)
        findViewById<View>(R.id.iv_back).setOnClickListener { finishWithSaveIfNeeded() }
        findViewById<View>(R.id.tv_done).setOnClickListener { finishWithSaveIfNeeded() }
        findViewById<View>(R.id.btn_create).setOnClickListener {
            ConvFolderEditActivity.startCreate(this)
        }

        val rv = findViewById<RecyclerView>(R.id.rv_manage)
        adapter = ManageAdapter()
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            0,
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder,
            ): Boolean {
                val from = viewHolder.bindingAdapterPosition
                val to = target.bindingAdapterPosition
                if (from < 0 || to < 0 || from == to) return false
                Collections.swap(folders, from, to)
                adapter.notifyItemMoved(from, to)
                orderDirty = true
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) = Unit
        }).attachToRecyclerView(rv)

        reload()
    }

    override fun onResume() {
        super.onResume()
        if (!orderDirty) {
            reload()
        }
    }

    private fun reload() {
        ImLocalStore.loadSessions { list ->
            sessions = list.orEmpty()
            ImLocalStore.loadConvFolders { _, folderList ->
                folders.clear()
                folders.addAll(folderList.map { it.copy() })
                orderDirty = false
                adapter.notifyDataSetChanged()
                findViewById<View>(R.id.tv_empty).isVisible = folders.isEmpty()
            }
            ConvFolderSyncHelper.syncFromServer { _, folderList ->
                if (orderDirty) return@syncFromServer
                folders.clear()
                folders.addAll(folderList.map { it.copy() })
                adapter.notifyDataSetChanged()
                findViewById<View>(R.id.tv_empty).isVisible = folders.isEmpty()
            }
        }
    }

    private fun folderUnread(folder: ConvFolderItem): Int {
        val ids = folder.sessionIds.filter { !it.isNullOrEmpty() }.toHashSet()
        return ConversationSessionCategory.sumUnread(sessions.filter { ids.contains(it.sessionId) })
    }

    private fun finishWithSaveIfNeeded() {
        if (!orderDirty) {
            finish()
            return
        }
        val ordered = ArrayList(folders)
        ConvFolderSyncHelper.saveWithRetry(
            { ordered },
            object : ConvFolderSyncHelper.OnSaveResult {
                override fun onSuccess(version: Long, result: List<ConvFolderItem>) {
                    MessageProcessor.getInstance().notifyConvFoldersUpdated()
                    finish()
                }

                override fun onFail(errorCode: Int, errorMsg: String?) {
                    ToastBroadUtils.show(
                        errorMsg?.takeIf { it.isNotBlank() }
                            ?: getString(R.string.conv_folder_save_failed),
                    )
                    finish()
                }
            },
        )
    }

    private inner class ManageAdapter : RecyclerView.Adapter<ManageVH>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ManageVH {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_conv_folder_manage, parent, false)
            return ManageVH(v)
        }

        override fun getItemCount(): Int = folders.size

        override fun onBindViewHolder(holder: ManageVH, position: Int) {
            holder.bind(folders[position])
        }
    }

    private inner class ManageVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val flIcon: View = itemView.findViewById(R.id.fl_icon)
        private val ivIcon: ImageView = itemView.findViewById(R.id.iv_icon)
        private val tvName: TextView = itemView.findViewById(R.id.tv_name)
        private val tvUnread: TextView = itemView.findViewById(R.id.tv_unread)
        private val ivMore: ImageView = itemView.findViewById(R.id.iv_more)

        fun bind(folder: ConvFolderItem) {
            tvName.text = folder.name.orEmpty()
            val type = folder.iconType
            ivIcon.setImageResource(ConvFolderIcons.iconRes(type))
            flIcon.background = GradientDrawable().apply {
                cornerRadius = itemView.resources.displayMetrics.density * 12f
                setColor(ConvFolderIcons.bgColor(type))
            }
            //此处不展示未读数
//            val unread = folderUnread(folder)
//            if (unread > 0) {
//                tvUnread.isVisible = true
//                tvUnread.text = if (unread > 99) "99+" else unread.toString()
//            } else {
//                tvUnread.isVisible = false
//            }
            ivMore.setOnClickListener {
                ConvFolderMoreHelper.show(this@ConvFolderManageActivity, folder) {
                    orderDirty = false
                    reload()
                }
            }
        }
    }
}
