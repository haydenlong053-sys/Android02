package com.cboda.intelligent.adapter

import androidx.core.view.isGone
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cboda.baselibrary.utils.anyToInt
import com.cboda.baselibrary.widge.VpGradeView
import com.cboda.intelligent.R
import com.cboda.intelligent.bean.CommentListBean
import com.cboda.intelligent.bean.SquarePostBean
import com.cboda.intelligent.utils.SquarePostLikeBroadHelper
import com.cboda.intelligent.utils.SquarePostQuoteBroadHelper
import com.cboda.intelligent.utils.TimeBroadUtils
import com.cboda.intelligent.widge.SquarePostImageLayoutHelper
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

class SquarePostBroadAdapter(val type: String) :
    BaseQuickAdapter<SquarePostBean, BaseViewHolder>(R.layout.item_square_post) {

    init {
        addChildClickViewIds(R.id.squareLikeLayout, R.id.squareAvatarWrap, R.id.squareShareLayout)
    }

    companion object {
        const val TYPE_SQUARE = "square"
        const val TYPE_FRIEND = "friend"
        private const val MAX_COMMENT_DISPLAY_COUNT = 4
        private const val MAX_IMAGE_DISPLAY_COUNT = 3
    }

    override fun onViewRecycled(holder: BaseViewHolder) {
        super.onViewRecycled(holder)
        SquarePostImageLayoutHelper.unbind(holder.getView(R.id.squareImageContainer))
        SquarePostImageLayoutHelper.unbind(holder.getView(R.id.squareYyImageList))
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
            setText(R.id.squarePublishTime, TimeBroadUtils.formatSquarePublishTime(item.time))
            setText(R.id.squareContent, item.content)
            setText(R.id.squareViewCount, anyToInt(item.viewCount).toString())
            setText(R.id.squareCommentCount, anyToInt(item.commentCount).toString())
            setText(R.id.squareLikeCount, anyToInt(item.likeCount).toString())
            SquarePostLikeBroadHelper.bindLikeIcon(getView(R.id.squareLikeIcon), item.isLike)
            setText(R.id.squareShareCount, anyToInt(item.quoteCount).toString())
            setGone(R.id.squarePledgeBadge, anyToInt(item.odicStakeStatus) != 0 || type != TYPE_SQUARE)
            setGone(R.id.squareShareLayout, type != TYPE_SQUARE)
            setGone(R.id.squareMoreAction, true)
            setGone(R.id.squareUserBio, true)
            val commentList = item.commentList.orEmpty()
                .filter { it.name.isNotBlank() || it.content.isNotBlank() }
                .take(MAX_COMMENT_DISPLAY_COUNT)
            setGone(
                R.id.squarePlLayout,
                commentList.isEmpty() || type == TYPE_SQUARE,
            )
            bindCommentList(getView(R.id.squarePlList), commentList)
            if (anyToInt(item.odicStakeStatus) == 0) {
                setText(R.id.squarePledgeText, item.odicStakeLabel)
            }
            SquarePostImageLayoutHelper.bind(
                getView(R.id.squareImageContainer),
                limitImages(item.displayImages()),
            )
            setGone(R.id.squareYyLayout, item.quote == null)
            item.quote?.let { quote ->
                SquarePostQuoteBroadHelper.bindQuote(getView(R.id.squareYyContent), quote)
                SquarePostImageLayoutHelper.bind(
                    getView(R.id.squareYyImageList),
                    limitImages(SquarePostQuoteBroadHelper.quoteImageUrls(quote)),
                )
            }
        }
    }

    fun updateLikeState(postId: String, isLike: Int, likeCount: String) {
        val index = data.indexOfFirst { it.id.trim() == postId.trim() }
        if (index < 0) return
        val item = data[index]
        data[index] = item.copy(isLike = isLike, likeCount = likeCount)
        notifyItemChanged(index)
    }

    fun removePost(postId: String) {
        val index = data.indexOfFirst { it.id.trim() == postId.trim() }
        if (index < 0) return
        removeAt(index)
    }

    private fun limitImages(imgs: Array<String>?): Array<String>? {
        return imgs
            ?.filter { it.isNotBlank() }
            ?.take(MAX_IMAGE_DISPLAY_COUNT)
            ?.toTypedArray()
    }

    private fun bindCommentList(recyclerView: RecyclerView, commentList: List<CommentListBean>) {
        if (recyclerView.layoutManager == null) {
            recyclerView.layoutManager = LinearLayoutManager(recyclerView.context)
            recyclerView.isNestedScrollingEnabled = false
            recyclerView.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        }
        var adapter = recyclerView.adapter as? SquarePostCommentBroadAdapter
        if (adapter == null) {
            adapter = SquarePostCommentBroadAdapter()
            recyclerView.adapter = adapter
        }
        adapter.setList(commentList)
    }

}
