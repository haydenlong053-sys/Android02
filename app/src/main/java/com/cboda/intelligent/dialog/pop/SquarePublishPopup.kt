package com.cboda.intelligent.dialog.pop

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.cboda.baselibrary.R
import com.cboda.baselibrary.popup.BaseBottomBroadPop
import com.cboda.baselibrary.utils.click
import com.cboda.baselibrary.utils.getAppColor
import com.cboda.baselibrary.utils.setBackColor
import com.cboda.intelligent.databinding.SquarePublishPopupBinding

class SquarePublishPopup(
    context: Context,
    private val pledgeAmount: String = DEFAULT_PLEDGE_AMOUNT,
) : BaseBottomBroadPop(context) {

    private val binding = SquarePublishPopupBinding.inflate(LayoutInflater.from(context))

    /** 0=广场，1=朋友圈 */
    private var publishType = PUBLISH_TYPE_SQUARE

    var onPublishClick: (content: String, publishType: Int, commentScope: String) -> Unit = { _, _, _ -> }
    var onHistoryClick: () -> Unit = {}
    var onCommentScopeClick: (currentScope: String) -> Unit = {}
    var onAddMediaClick: () -> Unit = {}

    init {
        contentView = binding.root
        setBackgroundColor(Color.parseColor("#8f000000"))
    }

    override fun onViewCreated(contentView: View) {
        super.onViewCreated(contentView)
        binding.publishInfoTip.text =
            context.getString(com.cboda.intelligent.R.string.square_publish_tip, pledgeAmount)
        updatePublishType(PUBLISH_TYPE_SQUARE)

        binding.publishTabSquare.click { updatePublishType(PUBLISH_TYPE_SQUARE) }
        binding.publishTabMoments.click { updatePublishType(PUBLISH_TYPE_MOMENTS) }
        binding.publishHistory.click { onHistoryClick.invoke() }
        binding.publishAddMedia.click { onAddMediaClick.invoke() }
        binding.publishAddMediaIcon.click { onAddMediaClick.invoke() }
        binding.publishCommentRow.click {
            onCommentScopeClick.invoke(binding.publishCommentValue.text?.toString().orEmpty())
        }
        binding.publishSubmit.click {
            onPublishClick.invoke(
                binding.publishContentInput.text?.toString()?.trim().orEmpty(),
                publishType,
                binding.publishCommentValue.text?.toString().orEmpty(),
            )
        }
    }

    fun setCommentScope(scope: String) {
        binding.publishCommentValue.text = scope
    }

    fun getCommentScope(): String = binding.publishCommentValue.text?.toString().orEmpty()

    private fun updatePublishType(type: Int) {
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
        binding.publishSubmit.text = context.getString(
            if (isSquare) {
                com.cboda.intelligent.R.string.square_publish_submit
            } else {
                com.cboda.intelligent.R.string.square_publish_submit_moments
            }
        )
    }

    companion object {
        const val PUBLISH_TYPE_SQUARE = 0
        const val PUBLISH_TYPE_MOMENTS = 1
        const val DEFAULT_PLEDGE_AMOUNT = "2.0"
    }
}
