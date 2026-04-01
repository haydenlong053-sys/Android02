package com.cboda.chatui.quickmeet.dialog

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cboda.baselibrary.popup.BaseBottomBroadPop
import com.cboda.baselibrary.toast.ToastBroadUtils
import com.cboda.baselibrary.utils.loadImg
import com.cboda.baselibrary.view.AppRoundBroadImage
import com.cboda.chatui.databinding.DialogPopMeetSeatApplyBinding
import com.cboda.chatui.R
import com.cboda.chatui.quickmeet.bean.SeatApplyItem
import com.cboda.chatui.quickmeet.vm.QuickMeetMicViewModel
import com.hjq.shape.view.ShapeTextView
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

//极速会议--麦位申请列表
class MeetSeatApplyPop(val context: Context, val groupIdNo: String?) : BaseBottomBroadPop(context) {
    private var sureInvoke: (SeatApplyItem) -> Unit = {}
    private val scope = MainScope() // 创建主线程的协程作用域
    private var binding: DialogPopMeetSeatApplyBinding = DialogPopMeetSeatApplyBinding.inflate(
        LayoutInflater.from(context)
    )
    private var viewModel: QuickMeetMicViewModel? = null

    init {
        contentView = binding.root
        setBackgroundColor(Color.parseColor("#8f000000"))

    }

    override fun onViewCreated(contentView: View) {
        super.onViewCreated(contentView)

        binding.rvMeetSeatApplyList.layoutManager = LinearLayoutManager(context)
        initData()
    }

    private fun initData() {
        if (context is FragmentActivity) {
            val activity = context
            // 获取该 Activity 作用域的 ViewModel
            viewModel =
                ViewModelProvider(activity).get<QuickMeetMicViewModel>(QuickMeetMicViewModel::class.java)
            // 现在可以使用 viewModel 进行数据操作
            scope.launch {
                viewModel?.getApplyMicList(groupIdNo)?.observe(activity) {
                    binding.ivNoData.isGone = (((it.applyList?.size ?: 0) > 0))
                    binding.rvMeetSeatApplyList.adapter = MeetSeatApplyAdapter(it.applyList ?: arrayListOf())
                }

            }
            sureInvoke ={
                scope.launch {
                    viewModel?.goAgreeUpMic(it.userId,groupIdNo)?.observe(activity) {
                        if (it){
                            ToastBroadUtils.show("已同意")
                        }
                        dismiss()
                    }
                }
            }
        }

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
            val lasePositionBoo = (position == (items.size - 1))
            holder.bind(items[position], lasePositionBoo)

        }

        override fun getItemCount(): Int = items.size
    }

    private inner class MeetSeatApplyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val avatarIv: ImageView = itemView.findViewById(R.id.ivMeetSeatApplyAvatar)
        private val nameTv: TextView = itemView.findViewById(R.id.tvMeetSeatApplyName)
        private val roleTagTv: ShapeTextView = itemView.findViewById(R.id.tvMeetSeatApplyRoleTag)
        private val agreeTv: ShapeTextView = itemView.findViewById(R.id.tvMeetSeatApplyAgree)
        private val viewLine: View = itemView.findViewById(R.id.viewMeetSeatApplyDivider)

        fun bind(item: SeatApplyItem, lasePositionBoo: Boolean) {
            //owner(群主) / member(普通成员) admin(管理员)
            nameTv.text = item.nickname
            if (item.role =="admin"){
                roleTagTv.isVisible =true
                roleTagTv.text = roleTagTv.resources.getString(com.cboda.baselibrary.R.string.msg_meeting_admin)
            }else if (item.role =="owner"){
                roleTagTv.isVisible =true
                roleTagTv.text = roleTagTv.resources.getString(R.string.qunzhushenfen)

            }else{
                roleTagTv.isVisible =false
            }

            avatarIv.loadImg(item.icon)
            agreeTv.setOnClickListener {
                sureInvoke.invoke(item)
            }
            viewLine.isGone = lasePositionBoo
        }
    }

    override fun onDismiss() {
        scope.cancel()
        super.onDismiss()
    }
}