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
import com.cboda.baselibrary.popup.BaseBottomBroadPop
import com.cboda.baselibrary.utils.MMKVBroadUtils
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
import com.cboda.intelligent.holder.ImagePickerBroadHolder
import com.cboda.intelligent.utils.showGuidePasswordTip
import com.cboda.intelligent.utils.showToastMain
import com.cboda.intelligent.utils.SquareCommentGateBroadHelper
import com.cboda.intelligent.utils.SquarePostQuoteBroadHelper
import com.cboda.intelligent.utils.dismissLoading
import com.cboda.intelligent.utils.showLoading
import com.cboda.intelligent.viewmodel.CleverBodyViewBroadModel
import com.cboda.intelligent.viewmodel.UploadFileViewBroadModel
import com.cboda.select.picture.config.PictureMimeType
import com.cboda.select.picture.entity.LocalMedia
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import java.io.File

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
    private var mediaAdapter: SelectPicBroadAdapter? = null
    private var imagePickerHolder: ImagePickerBroadHolder? = null
    private var isPublishing = false
    private var forwardQuotePost: SquarePostBean? = null
    private var quoteRefId: String = ""
    private var quoteRefType: String = ""
    private var squareDraft = PublishDraft()
    private var momentsDraft = PublishDraft()

    private data class PublishDraft(
        val content: String = "",
        val mediaPaths: List<String> = emptyList(),
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
            mediaPaths.remove(path)
            refreshMediaList()
        }
        mediaAdapter?.setOnItemClickListener { _, _, position ->
            if (position == mediaPaths.size) {
                openImagePicker()
            }
        }
        refreshMediaList()
    }

    private fun refreshMediaList() {
        val displayList = mediaPaths.toMutableList()
        if (displayList.size < MAX_MEDIA_COUNT) {
            displayList.add(ADD_ITEM)
        }
        mediaAdapter?.setList(displayList)
    }

    private fun openImagePicker() {
        val leftCount = MAX_MEDIA_COUNT - mediaPaths.size
        if (leftCount <= 0) {
            showToastMain(context.getString(R.string.square_publish_max_images, MAX_MEDIA_COUNT))
            return
        }
        if (imagePickerHolder == null) {
            imagePickerHolder = ImagePickerBroadHolder(activity)
            imagePickerHolder?.callBack = object : ImagePickerBroadHolder.OnResultCallback {
                override fun onResult(result: ArrayList<LocalMedia>?) {
                    appendSelectedImages(result)
                }

                override fun onCancel() = Unit
            }
        }
        imagePickerHolder?.show(
            isVideo = false,
            isSingle = false,
            isNumber = leftCount,
            isGif = false,
        )
    }

    private fun appendSelectedImages(result: ArrayList<LocalMedia>?) {
        val leftCount = MAX_MEDIA_COUNT - mediaPaths.size
        if (leftCount <= 0) return
        val selectedList = result.orEmpty()
        val imagePaths = selectedList
            .filter { isImageMedia(it) }
            .mapNotNull { media -> media.realPath.takeIf { it.isNotBlank() } }
            .take(leftCount)
        if (imagePaths.isEmpty() && selectedList.isNotEmpty()) {
            showToastMain(context.getString(R.string.square_publish_image_only))
            return
        }
        mediaPaths.addAll(imagePaths)
        refreshMediaList()
    }

    private fun isImageMedia(media: LocalMedia): Boolean {
        val mimeType = media.mimeType
        if (mimeType.isNullOrBlank()) return true
        return !PictureMimeType.isHasVideo(mimeType) && !PictureMimeType.isHasAudio(mimeType)
    }

    private fun handlePublishClick() {
        if (isPublishing) return
        val content = binding.publishContentInput.text?.toString()?.trim().orEmpty()
        if (content.isBlank() && mediaPaths.isEmpty() && quoteRefId.isBlank()) {
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
            val imageUrls = uploadImages()
            if (imageUrls == null) {
                resetPublishState()
                return@launch
            }
            val commentGateTypes = SquareCommentGateBroadHelper.buildCommentGateTypes(squareDraft.commentGateTypes)
            val commentCreditMin = SquareCommentGateBroadHelper.buildCommentCreditMin(
                squareDraft.commentGateTypes,
                squareDraft.commentCreditMin,
            )
            val liveData = if (publishType == PUBLISH_TYPE_SQUARE) {
                viewModel.publishSquarePost(
                    content = content,
                    imageUrls = imageUrls,
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

    private suspend fun uploadImages(): String? {
        if (mediaPaths.isEmpty()) return ""
        val files = mediaPaths.map { File(it) }.filter { it.exists() }
        if (files.isEmpty()) return ""
        val uploadResult = uploadViewModel.uploadListSyn(files)
        if (uploadResult?.urls.isNullOrEmpty()) {
            showToastMain(uploadResult?.erroToaastInfo.orEmpty())
            return null
        }
        return uploadResult?.urls.orEmpty().joinToString("|")
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
        private const val ADD_ITEM = "add"
    }
}
