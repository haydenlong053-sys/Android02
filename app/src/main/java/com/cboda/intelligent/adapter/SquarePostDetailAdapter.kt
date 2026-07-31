package com.cboda.intelligent.adapter

import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.graphics.toColorInt
import androidx.core.view.isGone
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SizeUtils
import com.cboda.baselibrary.base.BaseBroadAdapter
import com.cboda.baselibrary.utils.anyToInt
import com.cboda.baselibrary.widge.VpGradeView
import com.cboda.intelligent.R
import com.cboda.intelligent.bean.CommentListBean
import com.cboda.intelligent.bean.SquarePostBean
import com.cboda.intelligent.utils.SquarePostLikeBroadHelper
import com.cboda.intelligent.utils.SquarePostQuoteBroadHelper
import com.cboda.intelligent.utils.TimeBroadUtils
import com.cboda.intelligent.widge.SquarePostImageLayoutHelper
import com.chad.library.adapter.base.viewholder.BaseViewHolder

class SquarePostDetailAdapter(val type: String) :
    BaseBroadAdapter<SquarePostBean>(R.layout.square_post_detail_adapter) {

    private var detailComments: List<CommentListBean> = emptyList()
    private val itemHorizontalMarginPx = SizeUtils.dp2px(16f)
    private val itemBottomMarginPx = SizeUtils.dp2px(12f)
    private val nameColor = "#FF110F67".toColorInt()

    companion object {
        const val TYPE_SQUARE = "square"
        const val TYPE_FRIEND = "friend"
        const val TYPE_DETAIL = "square_detail"
        private const val MAX_COMMENT_DISPLAY_COUNT = 4
        private const val VIEW_TYPE_POST = 0
        private const val VIEW_TYPE_DETAIL_COMMENT = 1
    }

    fun submitDetail(post: SquarePostBean, comments: List<CommentListBean>) {
        detailComments = comments
        setList(listOf(post))
    }

    fun appendDetailComments(comments: List<CommentListBean>) {
        if (type != TYPE_DETAIL || comments.isEmpty()) return
        detailComments = detailComments + comments
        notifyItemRangeInserted(data.size + detailComments.size - comments.size, comments.size)
    }

    override fun getItemCount(): Int {
        return if (type == TYPE_DETAIL) {
            data.size + detailComments.size
        } else {
            super.getItemCount()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (type == TYPE_DETAIL && position >= data.size) {
            VIEW_TYPE_DETAIL_COMMENT
        } else {
            VIEW_TYPE_POST
        }
    }

    override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return if (viewType == VIEW_TYPE_DETAIL_COMMENT) {
            createBaseViewHolder(parent, R.layout.item_square_detail_comment)
        } else {
            super.onCreateDefViewHolder(parent, viewType)
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        if (type == TYPE_DETAIL && position >= data.size) {
            bindDetailComment(holder, detailComments[position - data.size], position)
            return
        }
        super.onBindViewHolder(holder, position)
    }

    override fun onViewRecycled(holder: BaseViewHolder) {
        super.onViewRecycled(holder)
        if (holder.itemViewType == VIEW_TYPE_POST) {
            SquarePostImageLayoutHelper.unbind(holder.getView(R.id.squareImageContainer))
            SquarePostImageLayoutHelper.unbind(holder.getView(R.id.squareYyImageList))
        }
    }

    override fun onBindData(holder: BaseViewHolder, bean: SquarePostBean) {
        bindPostItem(holder, bean)
    }

    private fun bindPostItem(holder: BaseViewHolder, item: SquarePostBean) {
        val isDetail = type == TYPE_DETAIL
        updatePostItemMargin(holder, isDetail)
        holder.apply {
            setText(R.id.squareUserName, item.name)
            getView<VpGradeView>(R.id.squareAvatarWrap).setData(
                item.icon,
                anyToInt(item.isLiang) == 1,
                anyToInt(item.isvip) == 1,
                item.grade,
                item.registDay,
            )
            setText(
                R.id.squarePublishTime,
                if (isDetail) {
                    TimeBroadUtils.formatSquarePostDate(item.time)
                } else {
                    TimeBroadUtils.formatSquarePublishTime(item.time)
                },
            )
            setText(R.id.squareContent, item.content)
            setText(R.id.squareViewCount, anyToInt(item.viewCount).toString())
            setText(R.id.squareCommentCount, anyToInt(item.commentCount).toString())
            setText(R.id.squareLikeCount, anyToInt(item.likeCount).toString())
            setText(R.id.squareShareCount, anyToInt(item.quoteCount).toString())
            setGone(R.id.squarePledgeBadge, anyToInt(item.odicStakeStatus) != 0 || type != TYPE_SQUARE)
            setGone(R.id.squareShareLayout, type == TYPE_SQUARE || isDetail)
            setGone(R.id.squareStatsRow, isDetail)
            setGone(R.id.squareMoreAction, !isDetail)
            setGone(R.id.squareUserBio, !isDetail || item.sign.isBlank())
            setText(R.id.squareUserBio, item.sign)
            val commentList = item.commentList.orEmpty()
                .filter { it.name.isNotBlank() || it.content.isNotBlank() }
                .take(MAX_COMMENT_DISPLAY_COUNT)
            setGone(
                R.id.squarePlLayout,
                commentList.isEmpty() || type == TYPE_SQUARE || isDetail,
            )
            bindCommentList(getView(R.id.squarePlList), commentList)
            if (anyToInt(item.odicStakeStatus) == 0) {
                setText(R.id.squarePledgeText, item.odicStakeLabel)
            }
            val displayImages = if (type == TYPE_FRIEND) {
                item.momentDisplayImages()
            } else {
                item.squareDisplayImages()
            }
            val videoUrl = if (type == TYPE_FRIEND) {
                item.momentVideoUrl()
            } else {
                item.squareVideoUrl()
            }
            SquarePostImageLayoutHelper.bindPostMedia(
                getView(R.id.squareImageContainer),
                displayImages,
                videoUrl.takeIf { it.isNotBlank() },
            )
            setGone(R.id.squareYyLayout, item.quote == null)
            item.quote?.let { quote ->
                SquarePostQuoteBroadHelper.bindQuote(getView(R.id.squareYyContent), quote)
                SquarePostImageLayoutHelper.bind(
                    getView(R.id.squareYyImageList),
                    SquarePostQuoteBroadHelper.quoteImageUrls(quote),
                )
            }
        }
    }

    private fun bindDetailComment(
        holder: BaseViewHolder,
        comment: CommentListBean,
        position: Int,
    ) {
        holder.apply {
            getView<VpGradeView>(R.id.squareDetailCommentAvatar).setData(
                comment.icon,
                anyToInt(comment.isLiang) == 1,
                anyToInt(comment.isvip) == 1,
                comment.grade,
                comment.registDay,
            )
            getView<TextView>(R.id.squareDetailCommentName).text =
                buildDetailCommentName(comment)
            setText(
                R.id.squareDetailCommentTime,
                TimeBroadUtils.formatSquarePublishTime(comment.time),
            )
            setText(R.id.squareDetailCommentContent, comment.content)
            val isLastComment = position == data.size + detailComments.size - 1
            setGone(R.id.squareDetailCommentDivider, isLastComment)
        }
    }

    private fun buildDetailCommentName(comment: CommentListBean): CharSequence {
        val name = comment.name.orEmpty()
        var replyTarget = ""
        val parentId = anyToInt(comment.parentId)
        if (parentId != 0) {
            replyTarget = data.find { anyToInt(it.id) == parentId }?.name ?: ""
        }
        if (replyTarget.isBlank()) {
            if (name.isEmpty()) return ""
            return SpannableString(name).apply {
                setSpan(
                    ForegroundColorSpan(nameColor),
                    0,
                    name.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
                )
            }
        }
        val replyPrefix = context.getString(com.cboda.baselibrary.R.string.square_comment_reply)
        val fullText = name + replyPrefix + replyTarget
        val spannable = SpannableString(fullText)
        if (name.isNotEmpty()) {
            spannable.setSpan(
                ForegroundColorSpan(nameColor),
                0,
                name.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
            )
        }
        val targetStart = name.length + replyPrefix.length
        val targetEnd = targetStart + replyTarget.length
        spannable.setSpan(
            ForegroundColorSpan(nameColor),
            targetStart,
            targetEnd,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
        )
        return spannable
    }

    private fun updatePostItemMargin(holder: BaseViewHolder, isDetail: Boolean) {
        val layoutParams = holder.itemView.layoutParams as? ViewGroup.MarginLayoutParams ?: return
        if (isDetail) {
            layoutParams.setMargins(0, 0, 0, 0)
        } else {
            layoutParams.setMargins(
                itemHorizontalMarginPx,
                0,
                itemHorizontalMarginPx,
                itemBottomMarginPx,
            )
        }
        holder.itemView.layoutParams = layoutParams
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
