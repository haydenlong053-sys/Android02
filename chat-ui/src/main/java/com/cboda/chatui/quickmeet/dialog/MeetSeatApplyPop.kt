package com.cboda.chatui.quickmeet.dialog

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cboda.baselibrary.popup.BaseBottomBroadPop
import com.cboda.baselibrary.view.AppRoundBroadImage
import com.cboda.chatui.databinding.DialogPopMeetSeatApplyBinding
import com.cboda.chatui.R
import com.cboda.chatui.quickmeet.bean.SeatApplyItem
import com.hjq.shape.view.ShapeTextView

//极速会议--麦位申请列表
class MeetSeatApplyPop(val context: Context) : BaseBottomBroadPop(context) {
    var sureInvoke: (SeatApplyItem) -> Unit = {}
    private var binding: DialogPopMeetSeatApplyBinding = DialogPopMeetSeatApplyBinding.inflate(
        LayoutInflater.from(context)
    )

    init {
        contentView = binding.root
        setBackgroundColor(Color.parseColor("#8f000000"))

    }

    override fun onViewCreated(contentView: View) {
        super.onViewCreated(contentView)

        // TODO: 后续替换成真实的申请列表数据；当前用假数据完整展示 UI 样式
        val fakeList = listOf(
            SeatApplyItem("COY FollowBB", "管理员", "申请上麦"),
            SeatApplyItem("COY FollowBB13455", "管理员", "申请上麦"),
            SeatApplyItem("COY FollowBB", "管理员", "申请上麦"),
            SeatApplyItem("COY FollowBB", "管理员", "申请上麦"),
            SeatApplyItem("COY FollowBB", "管理员", "申请上麦"),
            SeatApplyItem("COY FollowBB", "管理员", "申请上麦"),
        )

        binding.rvMeetSeatApplyList.layoutManager = LinearLayoutManager(context)
        binding.rvMeetSeatApplyList.adapter = MeetSeatApplyAdapter(fakeList)
    }

    private inner class MeetSeatApplyAdapter(
        private val items: List<SeatApplyItem>,
    ) : RecyclerView.Adapter<MeetSeatApplyViewHolder>() {

        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): MeetSeatApplyViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(
                R.layout.item_meet_seat_apply_broad,
                parent,
                false
            )
            return MeetSeatApplyViewHolder(view)
        }

        override fun onBindViewHolder(holder: MeetSeatApplyViewHolder, position: Int) {
            val lasePositionBoo= (position == (items.size-1))
            holder.bind(items[position],lasePositionBoo)

        }

        override fun getItemCount(): Int = items.size
    }

    private inner class MeetSeatApplyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val avatarIv: AppRoundBroadImage = itemView.findViewById(R.id.ivMeetSeatApplyAvatar)
        private val nameTv: TextView = itemView.findViewById(R.id.tvMeetSeatApplyName)
        private val roleTagTv: ShapeTextView = itemView.findViewById(R.id.tvMeetSeatApplyRoleTag)
        private val agreeTv: ShapeTextView = itemView.findViewById(R.id.tvMeetSeatApplyAgree)
        private val viewLine: View = itemView.findViewById(R.id.viewMeetSeatApplyDivider)

        fun bind(item: SeatApplyItem, lasePositionBoo: Boolean) {
            nameTv.text = item.name
            roleTagTv.text = item.role
            agreeTv.setOnClickListener {
                dismiss()
                // TODO: 接入“同意申请”业务逻辑
                sureInvoke.invoke(item)
            }
            viewLine.isGone = lasePositionBoo
        }
    }
}