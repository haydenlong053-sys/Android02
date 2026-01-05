package com.cboda.agora.ui.ui.member.impl

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ListAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.cboda.agora.ui.R
import com.cboda.agora.ui.databinding.AuiMemberListItemBinding
import com.cboda.agora.ui.databinding.AuiMemberListViewLayoutBinding
import com.cboda.agora.ui.ui.member.MemberBroadInfo
import com.cboda.agora.ui.ui.member.MemberItemBroadModel
import com.cboda.agora.ui.utils.BindingViewBroadHolder

class AUIRoomMemberListBroadView : FrameLayout {

    private val mBinding by lazy { AuiMemberListViewLayoutBinding.inflate(
        LayoutInflater.from(
            context
        )
    ) }

    private lateinit var listAdapter: ListAdapter<MemberItemBroadModel, BindingViewBroadHolder<AuiMemberListItemBinding>>
    private var listener:ActionListener?=null
    private var isOwner:Boolean?=false
    private var ownerId:String? = ""

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        addView(mBinding.root)
        initView()
    }

    private fun initView() {
        mBinding.rvUserList.layoutManager = GridLayoutManager(context, 1)
        listAdapter =
            object : ListAdapter<MemberItemBroadModel, BindingViewBroadHolder<AuiMemberListItemBinding>>(object :
                DiffUtil.ItemCallback<MemberItemBroadModel>() {
                override fun areItemsTheSame(oldItem: MemberItemBroadModel, newItem: MemberItemBroadModel) =
                    oldItem.user?.userId == newItem.user?.userId

                override fun areContentsTheSame(
                    oldItem: MemberItemBroadModel,
                    newItem: MemberItemBroadModel
                ) = false

            }) {
                override fun onCreateViewHolder(
                    parent: ViewGroup,
                    viewType: Int
                ) =
                    BindingViewBroadHolder(
                        AuiMemberListItemBinding.inflate(
                            LayoutInflater.from(parent.context)
                        )
                    )

                override fun onBindViewHolder(
                    holder: BindingViewBroadHolder<AuiMemberListItemBinding>,
                    position: Int
                ) {
                    val item = getItem(position)
                    holder.binding.tvUserName.text = item.user?.userName

                    holder.binding.tvKick.setText(context.getString(R.string.aui_member_item_kick))
                    holder.binding.tvKick.setOnClickListener{
                        listener?.onKickClick(it,position,item.user)
                    }

                    if (isOwner != true || item.user?.userId == ownerId){
                        holder.binding.tvKick.visibility = GONE
                    }

                    if (item.micIndex != null) {
                        holder.binding.tvUserInfo.visibility = VISIBLE
                        holder.binding.tvUserInfo.text = item.micString()
                    } else {
                        holder.binding.tvUserInfo.visibility = GONE
                    }

                    Glide.with(holder.binding.ivAvatar)
                        .load(item.user?.userAvatar)
                        .apply(RequestOptions.circleCropTransform())
                        .into(holder.binding.ivAvatar)
                }
            }
        mBinding.rvUserList.adapter = listAdapter
    }

    fun setMembers(members: List<MemberBroadInfo?>, seatMap: Map<Int, String?>) {
        val temp = mutableListOf<MemberItemBroadModel>()
        members.forEach {  user ->
            val micIndex = seatMap.entries.find { it.value == user?.userId }?.key
            val item = MemberItemBroadModel(user, micIndex)
            temp.add(item)
        }
        listAdapter.submitList(temp)
    }

    fun setIsOwner(isOwner:Boolean,ownerId:String?){
        this.isOwner = isOwner
        this.ownerId = ownerId
    }

    interface ActionListener{
        fun onKickClick(view: View, position:Int, user: MemberBroadInfo?){}
    }

    fun setMemberActionListener(listener:ActionListener){
        this.listener = listener
    }
}