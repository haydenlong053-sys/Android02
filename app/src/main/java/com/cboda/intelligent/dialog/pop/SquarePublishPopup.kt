package com.cboda.intelligent.dialog.pop

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.cboda.baselibrary.bean.BroadConstant
import com.cboda.baselibrary.popup.BaseBottomBroadPop
import com.cboda.baselibrary.utils.CropBroadBean
import com.cboda.baselibrary.utils.CompressionBroadUtil
import com.cboda.baselibrary.utils.MMKVBroadUtils
import com.cboda.baselibrary.utils.chooseVideoAndImg
import com.cboda.baselibrary.utils.click
import com.cboda.baselibrary.utils.setBackColor
import com.cboda.intelligent.R
import com.cboda.intelligent.activity.SquareUserActivity
import com.cboda.intelligent.adapter.SelectPicBroadAdapter
import com.cboda.intelligent.bean.SquarePostBean
import com.cboda.intelligent.bean.SquarePostPublishBroadEvent
import com.cboda.intelligent.configuration.SafeJumpBroadEnum
import com.cboda.intelligent.databinding.SquarePublishPopupBinding
import com.cboda.intelligent.dialog.pop.AuthPasswordBroadPop
import com.cboda.intelligent.utils.showGuidePasswordTip
import com.cboda.intelligent.utils.showToastMain
import com.cboda.intelligent.utils.SquareCommentGateBroadHelper
import com.cboda.intelligent.utils.SquarePostQuoteBroadHelper
import com.cboda.intelligent.utils.dismissLoading
import com.cboda.intelligent.utils.showLoading
import com.cboda.intelligent.viewmodel.CleverBodyViewBroadModel
import com.cboda.intelligent.viewmodel.UploadFileViewBroadModel
import com.cboda.select.picture.config.PictureMimeType
import com.cboda.select.picture.config.SelectMimeType
import com.cboda.select.picture.entity.LocalMedia
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.util.LinkedList

class SquarePublishPopup(
    private val activity: FragmentActivity,
) : BaseBottomBroadPop(activity) {

    private val binding = SquarePublishPopupBinding.inflate(LayoutInflater.from(context))
    private val viewModel by lazy {
        ViewModelProvider(activity)[CleverBodyViewBroadModel::class.java]
    }
    private val uploadViewModel by lazy {
        ViewModelProvider(activity)[UploadFileViewBroadModel::class.java]
    }

    /** 0=广场，1=朋友圈 */
    private var publishType = PUBLISH_TYPE_SQUARE
    private var odicStakeAmount: String = DEFAULT_PLEDGE_AMOUNT
    private val mediaPaths = mutableListOf<String>()
    private var videoPath: String? = null
    private var mediaAdapter: SelectPicBroadAdapter? = null
    private var isPublishing = false
    private var forwardQuotePost: SquarePostBean? = null
    private var quoteRefId: String = ""
    private var quoteRefType: String = ""
    private var squareDraft = PublishDraft()
    private var momentsDraft = PublishDraft()

    private data class PublishDraft(
        val content: String = "",
        val mediaPaths: List<String> = emptyList(),
        val videoPath: String? = null,
        val quoteRefId: String = "",
        val quoteRefType: String = "",
        val forwardQuotePost: SquarePostBean? = null,
        val commentGateTypes: Set<String> = setOf(SquareCommentGateBroadHelper.GATE_TYPE_EVERYONE),
        val commentCreditMin: Int = SquareCommentGateBroadHelper.MIN_CREDIT_SCORE,
    )

    var onHistoryClick: () -> Unit = {}

    init {
        contentView = binding.root
        setBackgroundColor(Color.parseColor("#8f000000"))
    }

    override fun onViewCreated(contentView: View) {
        super.onViewCreated(contentView)
        initMediaPicker()
        loadHotConfig()
        restoreDraft(PUBLISH_TYPE_SQUARE, initial = true)

        binding.publishTabSquare.click { switchPublishType(PUBLISH_TYPE_SQUARE) }
        binding.publishTabMoments.click { switchPublishType(PUBLISH_TYPE_MOMENTS) }
        binding.publishHistory.click { openSelfUserProfile() }
        binding.publishCommentRow.click { showCommentGatePopup() }
        binding.publishSubmit.click { handlePublishClick() }
    }

    override fun showPopupWindow() {
        if (publishType == PUBLISH_TYPE_SQUARE) {
            quoteRefId = squareDraft.quoteRefId
            quoteRefType = squareDraft.quoteRefType
            forwardQuotePost = squareDraft.forwardQuotePost
            applyQuoteFromDraft(squareDraft)
        }
        super.showPopupWindow()
    }

    fun setForwardQuote(post: SquarePostBean) {
        quoteRefId = post.id.trim()
        quoteRefType = SquarePostQuoteBroadHelper.quoteRefTypeForSquarePost()
        forwardQuotePost = post
        squareDraft = squareDraft.copy(
            quoteRefId = quoteRefId,
            quoteRefType = quoteRefType,
            forwardQuotePost = post,
        )
        if (publishType == PUBLISH_TYPE_SQUARE) {
            applyQuoteFromDraft(squareDraft)
        }
    }

    private fun openSelfUserProfile() {
        val userId = MMKVBroadUtils.getRealUserId().orEmpty().trim()
        if (userId.isEmpty()) return
        dismiss()
        SquareUserActivity.start(activity, userId)
        onHistoryClick.invoke()
    }

    private fun switchPublishType(type: Int) {
        if (publishType == type) return
        saveCurrentDraft()
        restoreDraft(type)
    }

    private fun saveCurrentDraft() {
        val gateTypes = if (publishType == PUBLISH_TYPE_SQUARE) {
            squareDraft.commentGateTypes
        } else {
            momentsDraft.commentGateTypes
        }
        val creditMin = if (publishType == PUBLISH_TYPE_SQUARE) {
            squareDraft.commentCreditMin
        } else {
            momentsDraft.commentCreditMin
        }
        val draft = PublishDraft(
            content = binding.publishContentInput.text?.toString().orEmpty(),
            mediaPaths = mediaPaths.toList(),
            videoPath = videoPath,
            quoteRefId = quoteRefId,
            quoteRefType = quoteRefType,
            forwardQuotePost = forwardQuotePost,
            commentGateTypes = gateTypes,
            commentCreditMin = creditMin,
        )
        if (publishType == PUBLISH_TYPE_SQUARE) {
            squareDraft = draft
        } else {
            momentsDraft = draft
        }
    }

    private fun restoreDraft(type: Int, initial: Boolean = false) {
        if (!initial && publishType == type) return
        if (!initial) {
            publishType = type
        } else {
            publishType = PUBLISH_TYPE_SQUARE
        }
        val draft = if (type == PUBLISH_TYPE_SQUARE) squareDraft else momentsDraft
        binding.publishContentInput.setText(draft.content)
        mediaPaths.clear()
        mediaPaths.addAll(draft.mediaPaths)
        videoPath = draft.videoPath
        quoteRefId = draft.quoteRefId
        quoteRefType = draft.quoteRefType
        forwardQuotePost = draft.forwardQuotePost
        updateCommentGateUi(draft.commentGateTypes, draft.commentCreditMin)
        applyQuoteFromDraft(draft)
        refreshMediaList()
        updatePublishTypeUi(type)
    }

    private fun applyQuoteFromDraft(draft: PublishDraft) {
        val post = draft.forwardQuotePost
        if (draft.quoteRefId.isNotEmpty() && post != null) {
            binding.publishQuoteLayout.isVisible = true
            binding.publishQuoteContent.isVisible = true
            SquarePostQuoteBroadHelper.bindQuoteText(
                binding.publishQuoteContent,
                post.name,
                post.content,
                SquarePostQuoteBroadHelper.imageCount(post),
            )
        } else {
            binding.publishQuoteLayout.isVisible = false
        }
    }

    private fun showCommentGatePopup() {
        if (publishType != PUBLISH_TYPE_SQUARE) return
        val draft = squareDraft
        SquareCommentGatePopup(
            activity = activity,
            initialTypes = draft.commentGateTypes,
            initialCreditMin = draft.commentCreditMin,
        ) { types, creditMin ->
            applyCommentGate(types, creditMin)
        }.showPopupWindow()
    }

    private fun applyCommentGate(types: Set<String>, creditMin: Int) {
        squareDraft = squareDraft.copy(
            commentGateTypes = types,
            commentCreditMin = creditMin,
        )
        updateCommentGateUi(types, creditMin)
    }

    private fun updateCommentGateUi(types: Set<String>, creditMin: Int) {
        binding.publishCommentValue.text = SquareCommentGateBroadHelper.buildDisplayLabel(
            context,
            types,
            creditMin,
        )
    }

    private fun updateCommentRowVisibility(visible: Boolean) {
        binding.publishCommentRow.isVisible = visible
        binding.publishCommentIcon.isVisible = visible
        binding.publishCommentLabel.isVisible = visible
        binding.publishCommentArrow.isVisible = visible
        binding.publishCommentValue.isVisible = visible
    }

    private fun loadHotConfig() {
        activity.lifecycleScope.launch {
            viewModel.getSquareHotConfig().observe(activity) { config ->
                odicStakeAmount = config?.odicStakeAmount()?.takeIf { it.isNotBlank() }
                    ?: DEFAULT_PLEDGE_AMOUNT
                binding.publishInfoTip.text = config?.stakeTip?.takeIf { it.isNotBlank() }
                    ?: context.getString(R.string.square_publish_tip, odicStakeAmount)
            }
        }
    }

    private fun initMediaPicker() {
        mediaAdapter = SelectPicBroadAdapter(R.layout.item_add_comment_pic_broad_layout)
        binding.publishMediaList.apply {
            layoutManager = GridLayoutManager(context, MEDIA_SPAN_COUNT)
            adapter = mediaAdapter
        }
        mediaAdapter?.addChildClickViewIds(R.id.deleteIcon1)
        mediaAdapter?.setOnItemChildClickListener { _, view, _ ->
            if (view.id != R.id.deleteIcon1) return@setOnItemChildClickListener
            val path = view.tag as? String ?: return@setOnItemChildClickListener
            if (SelectPicBroadAdapter.isVideoItem(path)) {
                videoPath = null
            } else {
                mediaPaths.remove(path)
            }
            refreshMediaList()
        }
        mediaAdapter?.setOnItemClickListener { _, _, position ->
            val item = mediaAdapter?.getItem(position) ?: return@setOnItemClickListener
            if (item == SelectPicBroadAdapter.ADD_ITEM) {
                openMediaPicker()
            }
        }
        refreshMediaList()
    }

    private fun refreshMediaList() {
        val displayList = mutableListOf<String>()
        val currentVideoPath = videoPath
        if (!currentVideoPath.isNullOrBlank()) {
            displayList.add(SelectPicBroadAdapter.wrapVideoPath(currentVideoPath))
        } else {
            displayList.addAll(mediaPaths)
            if (displayList.size < MAX_MEDIA_COUNT) {
                displayList.add(SelectPicBroadAdapter.ADD_ITEM)
            }
        }
        mediaAdapter?.setList(displayList)
    }

    private fun openMediaPicker() {
        if (!videoPath.isNullOrBlank()) {
            return
        }
        val leftCount = MAX_MEDIA_COUNT - mediaPaths.size
        if (leftCount <= 0) {
            showToastMain(context.getString(R.string.square_publish_max_images, MAX_MEDIA_COUNT))
            return
        }
        val chooseType = if (mediaPaths.isNotEmpty()) {
            BroadConstant.TYPE_PIC
        } else {
            BroadConstant.TYPE_ALL_INT
        }
        chooseVideoAndImg(
            canCamera = true,
            canChooseGif = false,
            chooseType = chooseType,
            imgMaxNum = leftCount,
            imgMinNum = 1,
            videoMaxNum = 1,
            videoMinNum = 1,
            recordVideoMaxSecond = 30,
            recordVideoMinSecond = 3,
            filterVideoMaxSecond = 31,
            maxFileSize = null,
            withSelectVideoImage = false,
            needCrop = false,
            cropBean = CropBroadBean(),
            selectList = null,
        ) { pairs ->
            handleChooseMediaResult(pairs)
        }
    }

    private fun handleChooseMediaResult(pairs: LinkedList<Pair<String, LocalMedia>>?) {
        if (pairs.isNullOrEmpty()) return
        val firstPair = pairs.first()
        val firstPath = firstPair.first
        val firstMedia = firstPair.second
        if (firstPath.isBlank() || firstMedia == null || !File(firstPath).exists()) {
            return
        }
        val isVideo = PictureMimeType.getMimeType(firstMedia.mimeType) == SelectMimeType.TYPE_VIDEO
        if (isVideo) {
            if (mediaPaths.isNotEmpty()) {
                showToastMain(context.getString(R.string.square_publish_video_image_conflict))
                return
            }
            compressSelectedVideo(firstPath)
            return
        }
        if (!videoPath.isNullOrBlank()) {
            showToastMain(context.getString(R.string.square_publish_video_image_conflict))
            return
        }
        val leftCount = MAX_MEDIA_COUNT - mediaPaths.size
        if (leftCount <= 0) return
        val imagePaths = pairs
            .filter { pair ->
                pair.second?.let { media ->
                    PictureMimeType.getMimeType(media.mimeType) != SelectMimeType.TYPE_VIDEO
                } == true && pair.first.isNotBlank() && File(pair.first).exists()
            }
            .map { it.first }
            .take(leftCount)
        if (imagePaths.isEmpty()) {
            showToastMain(context.getString(R.string.square_publish_image_only))
            return
        }
        mediaPaths.addAll(imagePaths)
        refreshMediaList()
    }

    private fun compressSelectedVideo(rawPath: String) {
        CompressionBroadUtil.sendVideoCompress(rawPath, context) { resultPath, errorMsg ->
            if (!resultPath.isNullOrBlank()) {
                videoPath = resultPath
                mediaPaths.clear()
                refreshMediaList()
            } else if (!errorMsg.isNullOrBlank()) {
                showToastMain(errorMsg)
            }
        }
    }

    private fun hasMedia(): Boolean {
        return mediaPaths.isNotEmpty() || !videoPath.isNullOrBlank()
    }

    private fun handlePublishClick() {
        if (isPublishing) return
        val content = binding.publishContentInput.text?.toString()?.trim().orEmpty()
        if (content.isBlank() && !hasMedia() && quoteRefId.isBlank()) {
            showToastMain(context.getString(R.string.square_publish_empty))
            return
        }
        if (publishType == PUBLISH_TYPE_SQUARE) {
            activity.lifecycleScope.launch {
                viewModel.checkPayPasswordSet().observe(activity) { hasPassword ->
                    if (hasPassword == true) {
                        showPayPasswordPop(content)
                    } else {
                        showGuidePasswordTip(context)
                    }
                }
            }
        } else {
            publish(content, payPwd = "")
        }
    }

    private fun showPayPasswordPop(content: String) {
        val tipPop = AuthPasswordBroadPop(context, SafeJumpBroadEnum.OnlyReturn.sourceType)
        tipPop.backPwdInvoke = { payPwd ->
            tipPop.dismiss()
            publish(content, payPwd)
        }
        tipPop.showPopupWindow()
    }

    private fun publish(content: String, payPwd: String) {
        isPublishing = true
        binding.publishSubmit.isEnabled = false
        showLoading()
        activity.lifecycleScope.launch {
            val mediaUploadResult = uploadMedia()
            if (mediaUploadResult == null) {
                resetPublishState()
                return@launch
            }
            val (imageUrls, videoUrl) = mediaUploadResult
            val commentGateTypes = SquareCommentGateBroadHelper.buildCommentGateTypes(squareDraft.commentGateTypes)
            val commentCreditMin = SquareCommentGateBroadHelper.buildCommentCreditMin(
                squareDraft.commentGateTypes,
                squareDraft.commentCreditMin,
            )
            val liveData = if (publishType == PUBLISH_TYPE_SQUARE) {
                viewModel.publishSquarePost(
                    content = content,
                    imageUrls = imageUrls,
                    video = videoUrl,
                    odicStake = odicStakeAmount,
                    payPwd = payPwd,
                    commentGateTypes = commentGateTypes,
                    commentCreditMin = commentCreditMin,
                    quoteRefType = quoteRefType,
                    quoteRefId = quoteRefId,
                )
            } else {
                viewModel.publishMomentPost(
                    content = content,
                    imageUrls = imageUrls,
                    video = videoUrl,
                    quoteRefType = quoteRefType,
                    quoteRefId = quoteRefId,
                )
            }
            liveData.observe(activity) { success ->
                resetPublishState()
                if (success != true) return@observe
                showToastMain(context.getString(R.string.square_publish_success))
                EventBus.getDefault().post(SquarePostPublishBroadEvent(publishType))
                dismiss()
            }
        }
    }

    private suspend fun uploadMedia(): Pair<String, String>? {
        val currentVideoPath = videoPath?.takeIf { it.isNotBlank() }
        if (!currentVideoPath.isNullOrBlank()) {
            val file = File(currentVideoPath)
            if (!file.exists()) {
                return Pair("", "")
            }
            val uploadResult = uploadViewModel.uploadSyn(file)
            if (uploadResult?.url.isNullOrBlank()) {
                showToastMain(uploadResult?.erroToaastInfo.orEmpty())
                return null
            }
            return Pair("", uploadResult?.url.orEmpty())
        }
        if (mediaPaths.isEmpty()) {
            return Pair("", "")
        }
        val files = mediaPaths.map { File(it) }.filter { it.exists() }
        if (files.isEmpty()) {
            return Pair("", "")
        }
        val uploadResult = uploadViewModel.uploadListSyn(files)
        if (uploadResult?.urls.isNullOrEmpty()) {
            showToastMain(uploadResult?.erroToaastInfo.orEmpty())
            return null
        }
        return Pair(uploadResult?.urls.orEmpty().joinToString("|"), "")
    }

    private fun resetPublishState() {
        isPublishing = false
        binding.publishSubmit.isEnabled = true
        dismissLoading()
    }

    private fun updatePublishTypeUi(type: Int) {
        publishType = type
        val selectedText = ContextCompat.getColor(context, R.color.white)
        val normalText = ContextCompat.getColor(context, R.color.black_65)

        val isSquare = type == PUBLISH_TYPE_SQUARE
        binding.publishTabSquare.apply {
            setBackColor(if (isSquare) R.color.color_3B47DD else R.color.transparent)
            setTextColor(if (isSquare) selectedText else normalText)
        }
        binding.publishTabMoments.apply {
            setBackColor(if (isSquare) R.color.transparent else R.color.color_3B47DD)
            setTextColor(if (isSquare) normalText else selectedText)
        }
        binding.publishInfoBanner.isVisible = isSquare
        updateCommentRowVisibility(isSquare)
        binding.publishQuoteLayout.isVisible = isSquare && quoteRefId.isNotEmpty()
        binding.publishSubmit.text = context.getString(
            if (isSquare) {
                R.string.square_publish_submit
            } else {
                R.string.square_publish_submit_moments
            },
        )
    }

    companion object {
        const val PUBLISH_TYPE_SQUARE = 0
        const val PUBLISH_TYPE_MOMENTS = 1
        const val DEFAULT_PLEDGE_AMOUNT = "2.0"
        private const val MAX_MEDIA_COUNT = 9
        private const val MEDIA_SPAN_COUNT = 4
    }
}
