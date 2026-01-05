package com.example.asceneskit.impl

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.cboda.agora.ui.ui.basic.AUIImageBroadView
import com.cboda.agora.ui.ui.member.listener.AUIRoomMembersActionBroadListener
import com.cboda.agora.ui.ui.member.IAUIRoomMembersBroadView
import com.cboda.agora.ui.ui.member.MemberBroadInfo
import com.example.asceneskit.R
import com.example.asceneskit.databinding.AuiRoomRightBroadLayoutBinding
import com.cboda.agora.ui.utils.FastClickBroadTools

class AUIRightMeetingTopBroadView: ConstraintLayout, IAUIRoomMembersBroadView {
    private val mRoomViewBinding = AuiRoomRightBroadLayoutBinding.inflate(LayoutInflater.from(context))
    private var aUpperRightListener: AUIRoomMembersActionBroadListener? = null
    private var aContext: Context? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        this.aContext = context
        addView(mRoomViewBinding.root)
        initListener()
    }

    private fun initListener() {
        mRoomViewBinding.llMemberRank.setOnClickListener{
//            if (!FastClickBroadTools.isFastClick(it)){
//                aUpperRightListener?.onMemberRankClickListener(it)
//            }
            if (!FastClickBroadTools.isFastClick(it)){
                aUpperRightListener?.onMemberRightUserMoreClickListener(it)
            }
        }

        mRoomViewBinding.btnMore.setOnClickListener{
            if (!FastClickBroadTools.isFastClick(it)){
                aUpperRightListener?.onMemberRankClickListener(it)
            }
        }


    }

    override fun setMemberActionListener(listener: AUIRoomMembersActionBroadListener?) {
        this.aUpperRightListener = listener
    }

    /**
     * 设置排行榜前三
     */
    override fun setMemberData(memberList: List<MemberBroadInfo?>){
        val size = memberList.size
        post { mRoomViewBinding.tvCount.text="$size" }
        size.let {
            if (it > 3){
                setMemberView(3, memberList)
                return
            }
            setMemberView(it, memberList)
        }


    }

    private fun setMemberView(size:Int,rankList:List<MemberBroadInfo?>){
        when(size){
            0 -> {
                mRoomViewBinding.ivMember1.visibility = GONE
                mRoomViewBinding.ivMember2.visibility = GONE
                mRoomViewBinding.ivMember3.visibility = GONE
//                mRoomViewBinding.ivMember4.visibility = GONE
            }
            1 -> {
                mRoomViewBinding.ivMember1.visibility = VISIBLE
                mRoomViewBinding.ivMember2.visibility = GONE
                mRoomViewBinding.ivMember3.visibility = GONE
//                mRoomViewBinding.ivMember4.visibility = GONE
                rankList[0]?.let { setResources(it.userAvatar,mRoomViewBinding.ivMember1) }
            }
            2 -> {
                mRoomViewBinding.ivMember1.visibility = VISIBLE
                mRoomViewBinding.ivMember2.visibility = VISIBLE
                mRoomViewBinding.ivMember3.visibility = GONE
//                mRoomViewBinding.ivMember4.visibility = GONE
                rankList[0]?.let { setResources(it.userAvatar,mRoomViewBinding.ivMember1) }
                rankList[1]?.let { setResources(it.userAvatar,mRoomViewBinding.ivMember2) }
            }
            3 -> {
                mRoomViewBinding.ivMember1.visibility = VISIBLE
                mRoomViewBinding.ivMember2.visibility = VISIBLE
                mRoomViewBinding.ivMember3.visibility = VISIBLE
//                mRoomViewBinding.ivMember4.visibility = GONE

                rankList[0]?.let { setResources(it.userAvatar,mRoomViewBinding.ivMember1) }
                rankList[1]?.let { setResources(it.userAvatar,mRoomViewBinding.ivMember2) }
                rankList[2]?.let { setResources(it.userAvatar,mRoomViewBinding.ivMember3) }

            }
//            4->{
//                mRoomViewBinding.ivMember1.visibility = VISIBLE
//                mRoomViewBinding.ivMember2.visibility = VISIBLE
//                mRoomViewBinding.ivMember3.visibility = VISIBLE
//                mRoomViewBinding.ivMember4.visibility = VISIBLE
//                rankList[0]?.let { setResources(it.userAvatar,mRoomViewBinding.ivMember1) }
//                rankList[1]?.let { setResources(it.userAvatar,mRoomViewBinding.ivMember2) }
//                rankList[2]?.let { setResources(it.userAvatar,mRoomViewBinding.ivMember3) }
//                rankList[3]?.let { setResources(it.userAvatar,mRoomViewBinding.ivMember4) }
//
//            }
        }
    }

    private fun setResources(url:String,view: AUIImageBroadView){
        aContext?.let {
            Glide.with(it)
                .load(url)
                .error(R.color.aui_white)
                .apply(RequestOptions.circleCropTransform())
                .into(view)
        }
    }

}