package com.cboda.intelligent.adapter

import androidx.core.view.isGone
import com.cboda.baselibrary.utils.anyToInt
import com.cboda.baselibrary.widge.VpGradeView
import com.cboda.intelligent.R
import com.cboda.intelligent.bean.SquareFollowerUserBean
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

class SquareBlacklistUserBroadAdapter :
    BaseQuickAdapter<SquareFollowerUserBean, BaseViewHolder>(R.layout.item_square_blacklist_user) {

    init {
        addChildClickViewIds(R.id.blacklistAvatar, R.id.unblockBtn)
    }

    override fun convert(holder: BaseViewHolder, item: SquareFollowerUserBean) {
        holder.setText(R.id.blacklistName, item.name)
        val sign = item.sign.trim()
        holder.setText(R.id.blacklistSign, sign)
        holder.getView<androidx.appcompat.widget.AppCompatTextView>(R.id.blacklistSign).isGone = sign.isBlank()

        val grade = item.grade.takeIf { anyToInt(item.gradeStatus) == 1 && it.isNotBlank() }
        holder.getView<VpGradeView>(R.id.blacklistAvatar).setData(
            item.icon,
            anyToInt(item.isLiang) == 1,
            anyToInt(item.isvip) == 1,
            grade,
            null,
        )
    }
}
