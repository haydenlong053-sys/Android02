package com.cboda.intelligent.adapter

import android.view.View
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cboda.baselibrary.utils.anyToInt
import com.cboda.baselibrary.widge.VpGradeView
import com.cboda.intelligent.R
import com.cboda.intelligent.bean.SquarePostBean
import com.cboda.intelligent.utils.GlideBroadUtil
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

class SquarePostBroadAdapter :
    BaseQuickAdapter<SquarePostBean, BaseViewHolder>(R.layout.item_square_post) {

    override fun convert(holder: BaseViewHolder, item: SquarePostBean) {
        holder.apply {
            setText(R.id.squareUserName, item.name)
            getView<VpGradeView>(R.id.squareAvatarWrap).setData(item.icon, anyToInt(item.isLiang) == 1, anyToInt(item.isvip) == 1, item.grade, item.registDay)
//        holder.setText(R.id.squareUserBio, item.userBio)
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
            val imageListView = getView<RecyclerView>(R.id.squareImageList)
            imageListView.isGone = item.imgs.isNullOrEmpty()
            if (!item.imgs.isNullOrEmpty()) {
                imageListView.layoutManager = GridLayoutManager(context, 3)
                val imageAdapter = SquarePostImageBroadAdapter()
                imageListView.adapter = imageAdapter
                imageAdapter.setList(item.imgs.toList())
            }
            if (item.quote != null) {

            }
        }

//        val creditScoreView = holder.getView<View>(R.id.squareCreditScore)
//        if (item.creditScore.isBlank()) {
//            creditScoreView.isGone = true
//        } else {
//            creditScoreView.isVisible = true
//            holder.setText(
//
//                R.id.squareCreditScore,
//                context.getString(R.string.square_score_format, item.creditScore),
//            )
//        }
//        holder.getView<View>(R.id.squareCrown).isVisible = item.isNiceNumber
//
//        val pledgeBadge = holder.getView<View>(R.id.squarePledgeBadge)
//        if (item.pledgeAmount.isBlank()) {
//            pledgeBadge.isGone = true
//        } else {
//            pledgeBadge.isVisible = true
//            holder.setText(
//                R.id.squarePledgeText,
//                context.getString(R.string.square_staked_format, item.pledgeAmount),
//            )
//        }
//
//        GlideBroadUtil.setImag(context, item.userAvatar, holder.getView(R.id.squareAvatar))
//
//        val imageListView = holder.getView<RecyclerView>(R.id.squareImageList)
//        if (item.imageList.isEmpty()) {
//            imageListView.isGone = true
//        } else {
//            imageListView.isVisible = true
//            val spanCount = when {
//                item.imageList.size == 1 -> 1
//                item.imageList.size == 2 -> 2
//                else -> 3
//            }
//            imageListView.layoutManager = GridLayoutManager(context, spanCount)
//            val imageAdapter = SquarePostImageBroadAdapter()
//            imageListView.adapter = imageAdapter
//            imageAdapter.setList(item.imageList)
//        }
    }
}

class SquarePostImageBroadAdapter :
    BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_square_post_image) {

    override fun convert(holder: BaseViewHolder, item: String) {
        GlideBroadUtil.setImag(context, item, holder.getView(R.id.squarePostImage))
    }
}
