package com.example.asceneskit.impl

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
import com.examole.asceneskit.R
import com.examole.asceneskit.databinding.MeetingListViewLayoutBinding
import com.examole.asceneskit.databinding.MeetingMemberListItemBinding
import io.agora.auikit.ui.member.MemberInfo
import io.agora.auikit.ui.member.MemberItemModel
import io.agora.auikit.utils.BindingViewHolder

class MeetingMemberListView : FrameLayout {
    private val mBinding by lazy { MeetingListViewLayoutBinding.inflate(
        LayoutInflater.from(
            context
        )
    ) }

    private lateinit var listAdapter: ListAdapter<MemberItemModel, BindingViewHolder<MeetingMemberListItemBinding>>
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
            object : ListAdapter<MemberItemModel, BindingViewHolder<MeetingMemberListItemBinding>>(object :
                DiffUtil.ItemCallback<MemberItemModel>() {
                override fun areItemsTheSame(oldItem: MemberItemModel, newItem: MemberItemModel) =
                    oldItem.user?.userId == newItem.user?.userId

                override fun areContentsTheSame(
                    oldItem: MemberItemModel,
                    newItem: MemberItemModel
                ) = false

            }) {
                override fun onCreateViewHolder(
                    parent: ViewGroup,
                    viewType: Int
                ) =
                    BindingViewHolder(
                        MeetingMemberListItemBinding.inflate(
                            LayoutInflater.from(parent.context)
                        )
                    )

                override fun onBindViewHolder(
                    holder: BindingViewHolder<MeetingMemberListItemBinding>,
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
                    if ( item.user?.userId == ownerId){
                        holder.binding.tvUserTag.visibility = VISIBLE
                        holder.binding.tvUserTag.text = context.getString(R.string.aui_micseat_roomowner)
                    }else{
                        holder.binding.tvUserTag.visibility = GONE
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

    fun setMembers(members: List<MemberInfo?>, seatMap: Map<Int, String?>) {
        val temp = mutableListOf<MemberItemModel>()
        members.forEach {  user ->
            val micIndex = seatMap.entries.find { it.value == user?.userId }?.key
            val item = MemberItemModel(user, micIndex)
            temp.add(item)
        }
        listAdapter.submitList(temp)
    }

    fun setIsOwner(isOwner:Boolean,ownerId:String?){
        this.isOwner = isOwner
        this.ownerId = ownerId
    }


    interface ActionListener{
        fun onKickClick(view: View, position:Int, user: MemberInfo?){}
    }

    fun setMemberActionListener(listener:ActionListener){
        this.listener = listener
    }
}