package com.cboda.intelligent.dialog.pop

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.widget.SeekBar
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import com.blankj.utilcode.util.SizeUtils
import com.cboda.baselibrary.popup.BaseBottomBroadPop
import com.cboda.baselibrary.utils.click
import com.cboda.baselibrary.utils.getAppColor
import com.cboda.intelligent.R
import com.cboda.intelligent.databinding.SquareCommentGatePopupBinding
import com.cboda.intelligent.utils.SquareCommentGateBroadHelper

class SquareCommentGatePopup(
    activity: FragmentActivity,
    initialTypes: Set<String>,
    initialCreditMin: Int,
    private val onSaved: (types: Set<String>, creditMin: Int) -> Unit,
) : BaseBottomBroadPop(activity) {

    private val binding = SquareCommentGatePopupBinding.inflate(LayoutInflater.from(context))
    private val selectedTypes = initialTypes.filter { it.isNotBlank() }.toMutableSet()
    private var creditMin = initialCreditMin.coerceIn(
        SquareCommentGateBroadHelper.MIN_CREDIT_SCORE,
        SquareCommentGateBroadHelper.MAX_CREDIT_SCORE,
    )

    init {
        contentView = binding.root
        setBackgroundColor(Color.parseColor("#8f000000"))
    }

    override fun onViewCreated(contentView: View) {
        super.onViewCreated(contentView)
        if (selectedTypes.isEmpty()) {
            selectedTypes.add(SquareCommentGateBroadHelper.GATE_TYPE_EVERYONE)
        }
        normalizeSelection()
        setupSeekBar()
        refreshOptionUi()
        binding.commentGateBack.click { dismiss() }
        binding.gateEveryoneOption.click { toggleType(SquareCommentGateBroadHelper.GATE_TYPE_EVERYONE) }
        binding.gateVipOption.click { toggleType(SquareCommentGateBroadHelper.GATE_TYPE_VIP) }
        binding.gateCreditOption.click { toggleType(SquareCommentGateBroadHelper.GATE_TYPE_CREDIT) }
        binding.commentGateSave.click {
            onSaved.invoke(selectedTypes.toSet(), creditMin)
            dismiss()
        }
    }

    private fun setupSeekBar() {
        binding.creditSeekBar.max = SquareCommentGateBroadHelper.MAX_CREDIT_SCORE -
            SquareCommentGateBroadHelper.MIN_CREDIT_SCORE
        binding.creditSeekBar.progress = creditMin - SquareCommentGateBroadHelper.MIN_CREDIT_SCORE
        binding.creditMinValue.text = creditMin.toString()
        binding.creditSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                creditMin = progress + SquareCommentGateBroadHelper.MIN_CREDIT_SCORE
                binding.creditMinValue.text = creditMin.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })
    }

    private fun toggleType(type: String) {
        when (type) {
            SquareCommentGateBroadHelper.GATE_TYPE_EVERYONE -> {
                if (SquareCommentGateBroadHelper.GATE_TYPE_EVERYONE in selectedTypes &&
                    selectedTypes.size == 1
                ) {
                    return
                }
                selectedTypes.clear()
                selectedTypes.add(SquareCommentGateBroadHelper.GATE_TYPE_EVERYONE)
            }
            SquareCommentGateBroadHelper.GATE_TYPE_VIP,
            SquareCommentGateBroadHelper.GATE_TYPE_CREDIT,
            -> {
                selectedTypes.remove(SquareCommentGateBroadHelper.GATE_TYPE_EVERYONE)
                if (type in selectedTypes) {
                    selectedTypes.remove(type)
                } else {
                    selectedTypes.add(type)
                }
                if (selectedTypes.isEmpty()) {
                    selectedTypes.add(SquareCommentGateBroadHelper.GATE_TYPE_EVERYONE)
                }
            }
        }
        refreshOptionUi()
    }

    private fun normalizeSelection() {
        val hasEveryone = SquareCommentGateBroadHelper.GATE_TYPE_EVERYONE in selectedTypes
        val hasRestricted = SquareCommentGateBroadHelper.GATE_TYPE_VIP in selectedTypes ||
            SquareCommentGateBroadHelper.GATE_TYPE_CREDIT in selectedTypes
        if (hasEveryone && hasRestricted) {
            selectedTypes.remove(SquareCommentGateBroadHelper.GATE_TYPE_VIP)
            selectedTypes.remove(SquareCommentGateBroadHelper.GATE_TYPE_CREDIT)
        }
        if (selectedTypes.isEmpty()) {
            selectedTypes.add(SquareCommentGateBroadHelper.GATE_TYPE_EVERYONE)
        }
    }

    private fun refreshOptionUi() {
        applyOptionStyle(
            binding.gateEveryoneOption,
            SquareCommentGateBroadHelper.GATE_TYPE_EVERYONE in selectedTypes,
        )
        applyOptionStyle(
            binding.gateVipOption,
            SquareCommentGateBroadHelper.GATE_TYPE_VIP in selectedTypes,
        )
        val creditSelected = SquareCommentGateBroadHelper.GATE_TYPE_CREDIT in selectedTypes
        applyOptionStyle(binding.gateCreditOption, creditSelected)
        binding.creditMinLayout.isVisible = creditSelected
        binding.creditSeekBar.isVisible = creditSelected
        if (creditSelected) {
            binding.creditMinValue.text = creditMin.toString()
            binding.creditSeekBar.progress = creditMin - SquareCommentGateBroadHelper.MIN_CREDIT_SCORE
        }
    }

    private fun applyOptionStyle(option: com.hjq.shape.layout.ShapeConstraintLayout, selected: Boolean) {
        option.shapeDrawableBuilder.solidColor = getAppColor(
            context,
            if (selected) com.cboda.baselibrary.R.color.white else com.cboda.baselibrary.R.color.bg_main2,
        )
        option.shapeDrawableBuilder.strokeColor = getAppColor(
            context,
            com.cboda.baselibrary.R.color.color_3B47DD,
        )
        option.shapeDrawableBuilder.strokeWidth = if (selected) SizeUtils.dp2px(1f) else 0
        option.shapeDrawableBuilder.intoBackground()
    }
}
