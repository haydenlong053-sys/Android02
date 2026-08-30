package com.cboda.intelligent.activity.im

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.cboda.baselibrary.bean.PayLsOrderBean
import com.cboda.intelligent.R
import com.cboda.intelligent.base.BaseSimple2BroadActivity
import com.cboda.intelligent.utils.click
import com.hjq.shape.view.ShapeView

/**
 * 收费进群：支付成功 / 验证入群资格中。
 * 默认停在第 2 步「验证资格」；验证通过后可调用 [markJoinSuccess] 切到第 3 步。
 */
class GroupJoinPayResultActivity : BaseSimple2BroadActivity() {

    private var orderBean: PayLsOrderBean? = null

    private lateinit var returnView: ShapeView
    private lateinit var backIcon: ImageView
    private lateinit var titleTv: TextView
    private lateinit var subtitleTv: TextView
    private lateinit var step1Icon: ImageView
    private lateinit var step2Icon: ImageView
    private lateinit var step3Icon: ImageView
    private lateinit var stepLine1: View
    private lateinit var stepLine2: View
    private lateinit var step1Tv: TextView
    private lateinit var step2Tv: TextView
    private lateinit var step3Tv: TextView

    /** 当前进度：1 支付成功，2 验证资格，3 入群成功 */
    private var currentStep = STEP_VERIFY

    override fun getLayoutId(): Int = R.layout.activity_group_join_pay_result

    override fun showOrHideTitleLayout(): Boolean = false

    override fun fitsSystemWindows(): Boolean = true

    override fun useStatusBarDarkFont(): Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        orderBean = readOrder(intent)
        bindViews()
        bindClicks()
        currentStep = intent.getIntExtra(EXTRA_STEP, STEP_VERIFY).coerceIn(STEP_PAY, STEP_JOIN)
        renderStep(currentStep)
    }

    private fun bindViews() {
        returnView = findViewById(R.id.returnView)
        backIcon = findViewById(R.id.backIcon)
        titleTv = findViewById(R.id.titleTv)
        subtitleTv = findViewById(R.id.subtitleTv)
        step1Icon = findViewById(R.id.step1Icon)
        step2Icon = findViewById(R.id.step2Icon)
        step3Icon = findViewById(R.id.step3Icon)
        stepLine1 = findViewById(R.id.stepLine1)
        stepLine2 = findViewById(R.id.stepLine2)
        step1Tv = findViewById(R.id.step1Tv)
        step2Tv = findViewById(R.id.step2Tv)
        step3Tv = findViewById(R.id.step3Tv)
        addHeight(returnView)
    }

    private fun bindClicks() {
        returnView.click { finish() }
        backIcon.click { finish() }
    }

    /** 验证通过后切到「入群成功」态。 */
    fun markJoinSuccess() {
        renderStep(STEP_JOIN)
    }

    private fun renderStep(step: Int) {
        currentStep = step
        val blue = ContextCompat.getColor(this, R.color.color_3B47DD)
        val black = ContextCompat.getColor(this, R.color.black)
        val grey = ContextCompat.getColor(this, R.color.black_35)

        when (step) {
            STEP_PAY -> {
                titleTv.setText(com.cboda.baselibrary.R.string.group_join_pay_result_title)
                subtitleTv.setText(com.cboda.baselibrary.R.string.group_join_pay_result_subtitle)
                step1Icon.setImageResource(R.drawable.ic_group_join_step_current)
                step2Icon.setImageResource(R.drawable.ic_group_join_step_pending)
                step3Icon.setImageResource(R.drawable.ic_group_join_step_pending)
                stepLine1.setBackgroundResource(R.drawable.bg_group_join_step_line_pending)
                stepLine2.setBackgroundResource(R.drawable.bg_group_join_step_line_pending)
                step1Tv.setTextColor(blue)
                step2Tv.setTextColor(black)
                step3Tv.setTextColor(black)
            }

            STEP_VERIFY -> {
                titleTv.setText(com.cboda.baselibrary.R.string.group_join_pay_result_title)
                subtitleTv.setText(com.cboda.baselibrary.R.string.group_join_pay_result_subtitle)
                step1Icon.setImageResource(R.drawable.ic_group_join_step_done)
                step2Icon.setImageResource(R.drawable.ic_group_join_step_current)
                step3Icon.setImageResource(R.drawable.ic_group_join_step_pending)
                stepLine1.setBackgroundResource(R.drawable.bg_group_join_step_line_active)
                stepLine2.setBackgroundResource(R.drawable.bg_group_join_step_line_pending)
                step1Tv.setTextColor(blue)
                step2Tv.setTextColor(blue)
                step3Tv.setTextColor(black)
            }

            else -> {
                titleTv.setText(com.cboda.baselibrary.R.string.group_join_pay_result_join_title)
                subtitleTv.setText(com.cboda.baselibrary.R.string.group_join_pay_result_join_subtitle)
                step1Icon.setImageResource(R.drawable.ic_group_join_step_done)
                step2Icon.setImageResource(R.drawable.ic_group_join_step_done)
                step3Icon.setImageResource(R.drawable.ic_group_join_step_done)
                stepLine1.setBackgroundResource(R.drawable.bg_group_join_step_line_active)
                stepLine2.setBackgroundResource(R.drawable.bg_group_join_step_line_active)
                step1Tv.setTextColor(blue)
                step2Tv.setTextColor(blue)
                step3Tv.setTextColor(blue)
                subtitleTv.setTextColor(grey)
            }
        }
    }

    companion object {
        const val EXTRA_ORDER = "extra_group_join_pay_order"
        const val EXTRA_STEP = "extra_group_join_pay_step"

        const val STEP_PAY = 1
        const val STEP_VERIFY = 2
        const val STEP_JOIN = 3

        fun start(
            context: Context,
            order: PayLsOrderBean? = null,
            step: Int = STEP_VERIFY,
        ) {
            val intent = Intent(context, GroupJoinPayResultActivity::class.java)
            if (order != null) {
                intent.putExtra(EXTRA_ORDER, order)
            }
            intent.putExtra(EXTRA_STEP, step)
            context.startActivity(intent)
        }

        fun readOrder(intent: Intent?): PayLsOrderBean? {
            if (intent == null) return null
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getSerializableExtra(EXTRA_ORDER, PayLsOrderBean::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getSerializableExtra(EXTRA_ORDER) as? PayLsOrderBean
            }
        }
    }
}
