package com.cboda.intelligent.adapter

import androidx.core.view.isGone
import com.blankj.utilcode.util.SizeUtils
import com.cboda.baselibrary.utils.anyToInt
import com.cboda.baselibrary.utils.setBackColor
import com.cboda.baselibrary.utils.setStrokeColorAndStrokeWidth
import com.cboda.baselibrary.utils.setTxtColor
import com.cboda.baselibrary.widge.VpGradeView
import com.cboda.intelligent.R
import com.cboda.intelligent.bean.SquareFollowerUserBean
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.hjq.shape.view.ShapeTextView

class SquareFollowingUserBroadAdapter :
    BaseQuickAdapter<SquareFollowerUserBean, BaseViewHolder>(R.layout.item_square_following_user) {

    init {
        addChildClickViewIds(R.id.followingAvatar, R.id.followBtn)
    }

    override fun convert(holder: BaseViewHolder, item: SquareFollowerUserBean) {
        holder.setText(R.id.followingName, item.name)
        val sign = item.sign.trim()
        holder.setText(R.id.followingSign, sign)
        holder.getView<androidx.appcompat.widget.AppCompatTextView>(R.id.followingSign).isGone = sign.isBlank()

        val grade = item.grade.takeIf { anyToInt(item.gradeStatus) == 1 && it.isNotBlank() }
        holder.getView<VpGradeView>(R.id.followingAvatar).setData(
            item.icon,
            anyToInt(item.isLiang) == 1,
            anyToInt(item.isvip) == 1,
            grade,
            null,
        )
        bindFollowButton(holder.getView(R.id.followBtn), anyToInt(item.isFollow) == 1)
    }

    private fun bindFollowButton(button: ShapeTextView, followed: Boolean) {
        if (followed) {
            button.text = context.getString(com.cboda.baselibrary.R.string.square_user_unfollow)
            button.setBackColor(com.cboda.baselibrary.R.color.white)
            button.setTxtColor(com.cboda.baselibrary.R.color.color_3B47DD)
            button.setStrokeColorAndStrokeWidth(
                com.cboda.baselibrary.R.color.color_3B47DD,
                SizeUtils.dp2px(1f),
            )
        } else {
            button.text = context.getString(com.cboda.baselibrary.R.string.square_user_follow)
            button.setBackColor(com.cboda.baselibrary.R.color.color_3B47DD)
            button.setTxtColor(com.cboda.baselibrary.R.color.white)
            button.setStrokeColorAndStrokeWidth(
                com.cboda.baselibrary.R.color.color_3B47DD,
                0,
            )
        }
    }

    fun updateFollowState(userId: String, isFollow: String) {
        val index = data.indexOfFirst { it.userId == userId }
        if (index >= 0) {
            data[index] = data[index].copy(isFollow = isFollow)
            notifyItemChanged(index)
        }
    }
}
