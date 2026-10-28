package com.freddy.chat.folder

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.cboda.baselibrary.widge.VpGradeView
import com.cboda.intelligent.R
import com.freddy.chat.utils.MessageAvatarBinder
import com.freddy.chat.utils.WeChatUiHelper
import com.freddy.im.http.model.im.ConvFolderItem
import com.freddy.im.http.model.im.SessionItem

sealed class FolderListRow {
    data class Header(
        val folder: ConvFolderItem,
        val unread: Int,
        val expanded: Boolean,
    ) : FolderListRow()

    data class Session(val folderId: String, val session: SessionItem) : FolderListRow()
}

class ConversationFolderListAdapter(
    private val currentUserId: String?,
    private val onToggle: (ConvFolderItem) -> Unit,
    private val onMore: (ConvFolderItem) -> Unit,
    private val onSessionClick: (SessionItem) -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val rows = ArrayList<FolderListRow>()

    fun submit(rows: List<FolderListRow>) {
        this.rows.clear()
        this.rows.addAll(rows)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (rows[position]) {
            is FolderListRow.Header -> 0
            is FolderListRow.Session -> 1
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == 0) {
            HeaderVH(inflater.inflate(R.layout.item_conv_folder_header, parent, false))
        } else {
            SessionVH(inflater.inflate(R.layout.item_conv_folder_session, parent, false))
        }
    }

    override fun getItemCount(): Int = rows.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val row = rows[position]) {
            is FolderListRow.Header -> (holder as HeaderVH).bind(row)
            is FolderListRow.Session -> (holder as SessionVH).bind(row.session)
        }
    }

    private inner class HeaderVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivArrow: ImageView = itemView.findViewById(R.id.iv_arrow)
        private val flIcon: View = itemView.findViewById(R.id.fl_icon)
        private val ivIcon: ImageView = itemView.findViewById(R.id.iv_icon)
        private val tvName: TextView = itemView.findViewById(R.id.tv_name)
        private val tvUnread: TextView = itemView.findViewById(R.id.tv_unread)
        private val ivMore: ImageView = itemView.findViewById(R.id.iv_more)

        fun bind(row: FolderListRow.Header) {
            val folder = row.folder
            tvName.text = folder.name.orEmpty()
            ivArrow.rotation = if (row.expanded) 90f else 0f
            val type = folder.iconType
            ivIcon.setImageResource(ConvFolderIcons.iconRes(type))
            val bg = flIcon.background
            if (bg is GradientDrawable) {
                bg.setColor(ConvFolderIcons.bgColor(type))
            } else {
                val gd = GradientDrawable().apply {
                    cornerRadius = itemView.resources.displayMetrics.density * 12f
                    setColor(ConvFolderIcons.bgColor(type))
                }
                flIcon.background = gd
            }
            if (row.unread > 0) {
                tvUnread.isVisible = true
                tvUnread.text = if (row.unread > 99) "99+" else row.unread.toString()
            } else {
                tvUnread.isVisible = false
            }
            itemView.setOnClickListener { onToggle(folder) }
            ivMore.setOnClickListener { onMore(folder) }
        }
    }

    private inner class SessionVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val root: View = itemView.findViewById(R.id.include_session)
        private val avatar: VpGradeView = root.findViewById(R.id.avatar)
        private val tvName: TextView = root.findViewById(R.id.tv_name)
        private val tvSubtitle: TextView = root.findViewById(R.id.tv_subtitle)
        private val tvUnread: TextView = root.findViewById(R.id.tv_unread)
        private val muteDot: View = root.findViewById(R.id.v_mute_dot)
        private val ivCheck: View = root.findViewById(R.id.iv_check)

        fun bind(session: SessionItem) {
            ivCheck.visibility = View.GONE
            MessageAvatarBinder.bindSessionListAvatar(avatar, session, currentUserId)
            val name = WeChatUiHelper.resolveSessionListDisplayName(session, currentUserId)
            tvName.text = name.ifEmpty {
                itemView.context.getString(com.cboda.baselibrary.R.string.conversation_name_fallback)
            }
            tvSubtitle.text = WeChatUiHelper.formatSessionPreview(session, currentUserId)
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
            root.setOnClickListener { onSessionClick(session) }
        }
    }
}
