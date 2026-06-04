package com.cboda.intelligent.adapter

import com.cboda.baselibrary.utils.anyToInt
import com.cboda.baselibrary.widge.VpGradeView
import com.cboda.intelligent.R
import com.cboda.intelligent.bean.SquarePostBean
import com.cboda.intelligent.widge.SquarePostImageLayoutHelper
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

class SquarePostBroadAdapter :
    BaseQuickAdapter<SquarePostBean, BaseViewHolder>(R.layout.item_square_post) {

    override fun onViewRecycled(holder: BaseViewHolder) {
        super.onViewRecycled(holder)
        SquarePostImageLayoutHelper.unbind(holder.getView(R.id.squareImageContainer))
    }

    override fun convert(holder: BaseViewHolder, item: SquarePostBean) {
        holder.apply {
            setText(R.id.squareUserName, item.name)
            getView<VpGradeView>(R.id.squareAvatarWrap).setData(
                item.icon,
                anyToInt(item.isLiang) == 1,
                anyToInt(item.isvip) == 1,
                item.grade,
                item.registDay,
            )
            setText(R.id.squarePublishTime, item.time)
            setText(R.id.squareContent, item.content)
            setText(R.id.squareViewCount, anyToInt(item.viewCount).toString())
            setText(R.id.squareCommentCount, anyToInt(item.commentCount).toString())
            setText(R.id.squareLikeCount, anyToInt(item.likeCount).toString())
            setText(R.id.squareShareCount, anyToInt(item.quoteCount).toString())
            setGone(R.id.squarePledgeBadge, anyToInt(item.odicStakeStatus) != 0)
            if (anyToInt(item.odicStakeStatus) == 0) {
                setText(R.id.squarePledgeText, item.odicStakeLabel)
            }
            SquarePostImageLayoutHelper.bind(
                getView(R.id.squareImageContainer),
                item.imgs,
            )

            if (item.quote != null) {
           
            }
        }
    }
}
