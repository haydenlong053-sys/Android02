package com.cboda.intelligent.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.graphics.toColorInt
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.cboda.baselibrary.network.repository.NetworkListBroadResult
import com.cboda.baselibrary.utils.MMKVBroadUtils
import com.cboda.baselibrary.utils.anyToInt
import com.cboda.baselibrary.widge.VpGradeView
import com.cboda.intelligent.R
import com.cboda.intelligent.base.BaseSimple2BroadActivity
import com.cboda.intelligent.bean.CommentListBean
import com.cboda.intelligent.bean.SquarePostBean
import com.cboda.intelligent.configuration.CommonBroadConfig
import com.cboda.intelligent.databinding.LayoutSquareDetailPostBinding
import com.cboda.intelligent.databinding.SquareDetailActivityBinding
import com.cboda.intelligent.dialog.InputDialogBroadFra
import com.cboda.intelligent.bean.SquarePostDeleteBroadEvent
import com.cboda.intelligent.bean.SquarePostTopBroadEvent
import com.cboda.intelligent.dialog.pop.SquarePostOwnerActionPopup
import com.cboda.intelligent.dialog.pop.SquarePostReportPopup
import com.cboda.intelligent.dialog.pop.SquarePublishPopup
import com.cboda.intelligent.holder.CommanConfimBroadHolder
import com.cboda.intelligent.utils.SquarePostLikeBroadHelper
import com.cboda.intelligent.utils.SquarePostQuoteBroadHelper
import com.cboda.intelligent.utils.TimeBroadUtils
import com.cboda.intelligent.utils.ToastBroadUtil
import com.cboda.intelligent.viewmodel.CleverBodyViewBroadModel
import com.cboda.intelligent.widge.SquarePostImageLayoutHelper
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus

class SquareDetailActivity : BaseSimple2BroadActivity() {

    private var mBinding: SquareDetailActivityBinding? = null
    private var postBinding: LayoutSquareDetailPostBinding? = null
    private val viewModel: CleverBodyViewBroadModel by viewModels()
    private var postId: String = ""
    private var pageNo = 1
    private var totalPage = 1
    private val commentList = mutableListOf<CommentListBean>()
    private val nameColor = "#FF110F67".toColorInt()
    private var replyParentId: String = "0"
    private var currentPost: SquarePostBean? = null
    private var reportPopup: SquarePostReportPopup? = null
    private var ownerActionPopup: SquarePostOwnerActionPopup? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = binding as? SquareDetailActivityBinding
        postBinding = mBinding?.squareDetailPost
        setRootBgColor(R.color.white)
        getToolbarTitle()?.setText(com.cboda.baselibrary.R.string.square_detail_title)
        postId = intent.getStringExtra(EXTRA_POST_ID).orEmpty()
        viewModel.recordSquarePostView(postId)
        initLoadMore()
        initCommentInput()
        initMoreAction()
        initLikeAction()
        initShareAction()
        loadPostDetail()
    }

    override fun onDestroy() {
        reportPopup?.dismiss()
        reportPopup = null
        ownerActionPopup?.dismiss()
        ownerActionPopup = null
        postBinding?.squareImageContainer?.let { SquarePostImageLayoutHelper.unbind(it) }
        postBinding?.squareYyImageList?.let { SquarePostImageLayoutHelper.unbind(it) }
        super.onDestroy()
    }

    override fun getViewBindingClass() = SquareDetailActivityBinding::class.java
    override fun showTransparentTopTitleLayout() = false
    override fun useViewBinding() = true
    override fun showOrHideTitleLayout() = true
    override fun useStatusBarDarkFont() = true
    override fun showBgTopTitleLayout() = true

    private fun initCommentInput() {
        mBinding?.squareDetailCommentInput?.setOnClickListener {
            replyParentId = "0"
            showCommentInputDialog()
        }
    }

    private fun initMoreAction() {
        postBinding?.squareMoreAction?.setOnClickListener { anchor ->
            val post = currentPost ?: return@setOnClickListener
            if (isOwnPost(post)) {
                showOwnerActionPopup(anchor)
            } else {
                showReportPopup(anchor)
            }
        }
    }

    private fun isOwnPost(post: SquarePostBean): Boolean {
        val currentUserId = MMKVBroadUtils.getRealUserId().orEmpty().trim()
        val authorId = post.userId.trim()
        return currentUserId.isNotEmpty() && authorId.isNotEmpty() && currentUserId == authorId
    }

    private fun showReportPopup(anchor: View) {
        if (reportPopup?.isShowing() == true) {
            reportPopup?.dismiss()
            return
        }
        reportPopup = SquarePostReportPopup(this, anchor)
        reportPopup?.show {
            SquareReportTypeActivity.start(
                context = this,
                targetType = SquareReportTypeActivity.TARGET_TYPE_SQUARE,
                targetId = postId,
            )
        }
    }

    private fun showOwnerActionPopup(anchor: View) {
        val post = currentPost ?: return
        if (ownerActionPopup?.isShowing() == true) {
            ownerActionPopup?.dismiss()
            return
        }
        ownerActionPopup = SquarePostOwnerActionPopup(this, anchor)
        ownerActionPopup?.show(
            isTop = post.isTop,
            onDeleteClick = { confirmDeletePost() },
            onTopClick = { toggleTopPost() },
        )
    }

    private fun confirmDeletePost() {
        if (postId.isBlank()) return
        val holder = CommanConfimBroadHolder(
            this,
            getString(R.string.wenxintishi),
            getString(com.cboda.baselibrary.R.string.square_post_delete_confirm),
            getString(R.string.shanchu),
            getString(R.string.quxiao),
        )
        holder.callBack = object : CommanConfimBroadHolder.OnConfimCallBack {
            override fun onClickConfim(isConfim: Boolean) {
                if (isConfim) {
                    deletePost()
                }
            }
        }
        holder.show()
    }

    private fun deletePost() {
        if (postId.isBlank()) return
        lifecycleScope.launch {
            viewModel.deleteSquarePost(postId).observe(this@SquareDetailActivity) { success ->
                if (success != true) return@observe
                ToastBroadUtil.show(getString(com.cboda.baselibrary.R.string.square_post_delete_success))
                EventBus.getDefault().post(SquarePostDeleteBroadEvent(postId.trim()))
                finish()
            }
        }
    }

    private fun toggleTopPost() {
        val post = currentPost ?: return
        if (postId.isBlank()) return
        val top = post.isTop != 1
        lifecycleScope.launch {
            viewModel.topSquarePost(postId, top).observe(this@SquareDetailActivity) { success ->
                if (success != true) return@observe
                val newIsTop = if (top) 1 else 0
                currentPost = post.copy(isTop = newIsTop)
                ToastBroadUtil.show(
                    getString(
                        if (top) {
                            com.cboda.baselibrary.R.string.square_post_top_success
                        } else {
                            com.cboda.baselibrary.R.string.square_post_untop_success
                        },
                    ),
                )
                EventBus.getDefault().post(
                    SquarePostTopBroadEvent(
                        postId = postId.trim(),
                        isTop = newIsTop,
                    ),
                )
            }
        }
    }

    private fun initLikeAction() {
        mBinding?.squareDetailLikeLayout?.setOnClickListener {
            toggleLike()
        }
    }

    private fun initShareAction() {
        mBinding?.squareDetailShareLayout?.setOnClickListener {
            val post = currentPost ?: return@setOnClickListener
            if (post.id.trim().isEmpty()) return@setOnClickListener
            SquarePublishPopup(this).apply {
                setForwardQuote(post)
            }.showPopupWindow()
        }
    }

    private fun toggleLike() {
        val post = currentPost ?: return
        if (postId.isBlank()) return
        val like = post.isLike != 1
        lifecycleScope.launch {
            viewModel.likeSquarePost(postId, like).observe(this@SquareDetailActivity) { success ->
                if (success != true) return@observe
                currentPost = SquarePostLikeBroadHelper.buildUpdatedPost(post, like)
                bindBottomBar(currentPost!!)
                SquarePostLikeBroadHelper.notifyLikeChanged(
                    postId = postId,
                    isLike = currentPost!!.isLike,
                    likeCount = currentPost!!.likeCount,
                )
            }
        }
    }

    private fun showCommentInputDialog(replyName: String = "") {
        InputDialogBroadFra()
            .setShaixuanClick(replyName, object : InputDialogBroadFra.OnShaixuanClick {
                override fun onShaixuanClick(content: String) {
                    submitComment(content, replyParentId)
                }
            })
            .show(supportFragmentManager, TAG_COMMENT_INPUT)
    }

    private fun submitComment(content: String, parentId: String) {
        if (postId.isBlank()) return
        lifecycleScope.launch {
            viewModel.postSquareComment(postId, content, parentId)
                .observe(this@SquareDetailActivity) { success ->
                    if (success != true) return@observe
                    replyParentId = "0"
                    ToastBroadUtil.show(getString(R.string.fasongshenqing))
                    refreshCommentList()
                    refreshPostCommentCount()
                }
        }
    }

    private fun refreshCommentList() {
        pageNo = 1
        totalPage = 1
        mBinding?.smart?.setEnableLoadMore(true)
        mBinding?.smart?.setNoMoreData(false)
        loadCommentList(append = false)
    }

    private fun refreshPostCommentCount() {
        lifecycleScope.launch {
            viewModel.getSquarePostDetail(postId).observe(this@SquareDetailActivity) { result ->
                result.post?.let { bindBottomBar(it) }
            }
        }
    }

    private fun initLoadMore() {
        mBinding?.smart?.setEnableRefresh(false)
        mBinding?.smart?.setEnableLoadMore(true)
        mBinding?.smart?.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onRefresh(refreshLayout: RefreshLayout) {
                refreshLayout.finishRefresh()
            }

            override fun onLoadMore(refreshLayout: RefreshLayout) {
                if (pageNo >= totalPage) {
                    refreshLayout.setNoMoreData(true)
                    mBinding?.smart?.finishLoadMore()
                    return
                }
                pageNo++
                loadCommentList(append = true)
            }
        })
    }

    private fun loadPostDetail() {
        if (postId.isBlank()) {
            showEmpty(true)
            return
        }
        lifecycleScope.launch {
            viewModel.getSquarePostDetail(postId).observe(this@SquareDetailActivity) { result ->
                val emptyMessage = result.emptyMessage
                if (!emptyMessage.isNullOrBlank()) {
                    showEmpty(true, emptyMessage)
                    return@observe
                }
                val post = result.post
                if (post == null) {
                    showEmpty(true)
                    return@observe
                }
                bindPost(post)
                currentPost = post
                bindBottomBar(post)
                showEmpty(false)
                pageNo = 1
                totalPage = 1
                mBinding?.smart?.setEnableLoadMore(true)
                mBinding?.smart?.setNoMoreData(false)
                loadCommentList(append = false)
            }
        }
    }

    private fun loadCommentList(append: Boolean) {
        lifecycleScope.launch {
            viewModel.getSquareCommentList(postId, pageNo, CommonBroadConfig.pageSize_20)
                .observe(this@SquareDetailActivity) { result ->
                    showCommentData(result, append)
                }
        }
    }

    private fun showCommentData(
        result: NetworkListBroadResult<CommentListBean>,
        append: Boolean,
    ) {
        mBinding?.smart?.finishLoadMore()
        totalPage = result.totalPage.coerceAtLeast(1)
        if (!append) {
            commentList.clear()
        }
        commentList.addAll(result.list.orEmpty())
        bindComments()
        if (pageNo >= totalPage) {
            mBinding?.smart?.setEnableLoadMore(false)
        }
    }

    private fun bindPost(post: SquarePostBean) {
        val postView = postBinding ?: return
        postView.squareAvatarWrap.setData(
            post.icon,
            anyToInt(post.isLiang) == 1,
            anyToInt(post.isvip) == 1,
            post.grade,
            post.registDay,
        )
        postView.squareAvatarWrap.setOnClickListener {
            openUserProfile(post.userId)
        }
        postView.squareUserName.text = post.name
        postView.squarePublishTime.text = TimeBroadUtils.formatSquarePostDate(post.time)
        postView.squareContent.text = post.content
        postView.squareUserBio.isGone = post.sign.isBlank()
        postView.squareUserBio.text = post.sign
        SquarePostImageLayoutHelper.bindPostMedia(
            postView.squareImageContainer,
            post.squareDisplayImages(),
            post.squareVideoUrl().takeIf { it.isNotBlank() },
            enableImagePreview = true,
        )
        val quote = post.quote
        postView.squareYyLayout.isGone = quote == null
        if (quote != null) {
            SquarePostQuoteBroadHelper.bindQuote(postView.squareYyContent, quote)
            SquarePostImageLayoutHelper.bind(
                postView.squareYyImageList,
                SquarePostQuoteBroadHelper.quoteImageUrls(quote),
                enableImagePreview = true,
            )
        }
    }

    private fun bindComments() {
        val container = mBinding?.squareDetailCommentContainer ?: return
        container.removeAllViews()
        if (commentList.isEmpty()) {
            container.isGone = true
            return
        }
        container.isVisible = true
        val inflater = LayoutInflater.from(this)
        commentList.forEachIndexed { index, comment ->
            val itemView = inflater.inflate(R.layout.item_square_detail_comment, container, false)
            bindCommentItem(itemView, comment, index == commentList.lastIndex)
            container.addView(itemView)
        }
    }

    private fun bindCommentItem(itemView: View, comment: CommentListBean, isLast: Boolean) {
        itemView.findViewById<VpGradeView>(R.id.squareDetailCommentAvatar).apply {
            setData(
                comment.icon,
                anyToInt(comment.isLiang) == 1,
                anyToInt(comment.isvip) == 1,
                comment.grade,
                comment.registDay,
            )
            setOnClickListener {
                openUserProfile(comment.userId)
            }
        }
        itemView.findViewById<TextView>(R.id.squareDetailCommentName).text =
            buildDetailCommentName(comment)
        itemView.findViewById<TextView>(R.id.squareDetailCommentTime).text =
            TimeBroadUtils.formatSquarePublishTime(comment.time)
        itemView.findViewById<TextView>(R.id.squareDetailCommentContent).apply {
            text = comment.content
            setOnClickListener {
                replyParentId = comment.parentIdForReply().ifBlank { "0" }
                showCommentInputDialog(replyName = comment.name.orEmpty())
            }
        }
        itemView.findViewById<View>(R.id.squareDetailCommentDivider).isGone = isLast
    }

    private fun buildDetailCommentName(comment: CommentListBean): CharSequence {
        val name = comment.name.orEmpty()
        var replyTarget = ""
        val parentId = anyToInt(comment.parentId)
        if (parentId != 0) {
            replyTarget = commentList.find { anyToInt(it.id) == parentId }?.name ?: ""
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
        val replyPrefix = getString(com.cboda.baselibrary.R.string.square_comment_reply)
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

    private fun openUserProfile(userId: String) {
        val id = userId.trim()
        if (id.isNotEmpty()) {
            SquareUserActivity.start(this, id)
        }
    }

    private fun bindBottomBar(post: SquarePostBean) {
        mBinding?.squareDetailBottomCommentCount?.text =
            anyToInt(post.commentCount).toString()
        mBinding?.squareDetailBottomLikeCount?.text =
            anyToInt(post.likeCount).toString()
        mBinding?.squareDetailBottomShareCount?.text =
            anyToInt(post.quoteCount).toString()
        mBinding?.squareDetailLikeIcon?.let {
            SquarePostLikeBroadHelper.bindLikeIcon(it, post.isLike)
        }
    }

    private fun limitImages(imgs: Array<String>?): Array<String>? {
        return imgs
            ?.filter { it.isNotBlank() }
            ?.take(MAX_IMAGE_DISPLAY_COUNT)
            ?.toTypedArray()
    }

    private fun showEmpty(show: Boolean, message: String? = null) {
        mBinding?.llNoData?.isVisible = show
        mBinding?.smart?.isVisible = !show
        mBinding?.squareDetailBottomBar?.isVisible = !show
        mBinding?.squareDetailNoDataText?.text =
            message?.takeIf { it.isNotBlank() } ?: getString(R.string.no_data)
    }

    companion object {
        private const val EXTRA_POST_ID = "postId"
        private const val TAG_COMMENT_INPUT = "squareCommentInput"
        private const val MAX_IMAGE_DISPLAY_COUNT = 3

        fun start(context: Context, postId: String) {
            context.startActivity(
                Intent(context, SquareDetailActivity::class.java)
                    .putExtra(EXTRA_POST_ID, postId),
            )
        }
    }
}
