package com.cboda.chatui.conversationlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cboda.chatui.R
import com.cboda.baselibrary.bean.GlobalNoticeBroadBean

class MyHeaderBroadAdapter(var globalNoticeBean: GlobalNoticeBroadBean?,val clickListener:(globalNoticeBean: GlobalNoticeBroadBean?)-> Unit ={}) : RecyclerView.Adapter<MyHeaderBroadAdapter.VH>() {
    private var nameTextView: TextView? = null
    private var timeTextView: TextView? = null
    private var portraitImageView: ImageView? = null
    private var silentImageView: ImageView? = null
    private var unreadCountTextView: TextView? = null
    private var redDotView: View? = null
    private var contentTextView: TextView? = null
    private var promptTextView: TextView? = null
    private var statusImageView: ImageView? = null
    private var secretChatIndicator: ImageView? = null

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.conversationlist_item_broad_conversation, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        bindViews(holder.itemView)
        // 绑定头部数据
//        holder.itemView.findViewById<TextView>(R.id.tv_title).text = "热门推荐"
        //        itemView.setBackgroundResource(conversationInfo.top > 0 ? R.drawable.selector_stick_top_broad_item : R.drawable.selector_common_broad_item);
        holder.itemView.setBackgroundResource(R.drawable.selector_stick_top_broad_item)
        nameTextView?.text = holder.itemView.context.getString(R.string.sys_notice_info)
        portraitImageView?.setImageResource(R.mipmap.ic_head_defaut_broad_msg)
        contentTextView?.text = globalNoticeBean?.noticeTitle ?: ""
        timeTextView?.text = globalNoticeBean?.createTime ?: ""
        holder.itemView.setOnClickListener {
            clickListener.invoke(globalNoticeBean?:GlobalNoticeBroadBean())
        }

    }

    private fun bindViews(itemView: View) {
        nameTextView = itemView.findViewById(R.id.nameTextView)
        timeTextView = itemView.findViewById(R.id.timeTextView)
        portraitImageView = itemView.findViewById(R.id.portraitImageView)
        silentImageView = itemView.findViewById(R.id.slient)
        unreadCountTextView = itemView.findViewById(R.id.unreadCountTextView)
        redDotView = itemView.findViewById(R.id.redDotView)
        contentTextView = itemView.findViewById(R.id.contentTextView)
        promptTextView = itemView.findViewById(R.id.promptTextView)
        statusImageView = itemView.findViewById(R.id.statusImageView)
        secretChatIndicator = itemView.findViewById(R.id.secretChatIndicator)

    }

    fun refreshHeaderData(mNoticeBean: GlobalNoticeBroadBean) {
        globalNoticeBean = mNoticeBean
        notifyItemChanged(0)
    }


    override fun getItemCount() = 1  // 只有一个头部


}