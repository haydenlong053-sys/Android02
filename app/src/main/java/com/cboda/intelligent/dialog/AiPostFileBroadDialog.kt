package com.cboda.intelligent.dialog

import android.content.Context
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.OvershootInterpolator
import android.view.animation.ScaleAnimation
import androidx.appcompat.widget.AppCompatTextView
import com.airbnb.lottie.LottieAnimationView
import razerdp.basepopup.BasePopupFlag
import razerdp.basepopup.BasePopupWindow
import com.cboda.intelligent.R
import com.cboda.intelligent.adapter.VerticalTextBroadAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cboda.baselibrary.utils.eLogD
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.ThreadUtils
import com.cboda.intelligent.fragment.AiAllFragment.Companion.DOCUMENT_TYPE
import com.cboda.intelligent.fragment.AiAllFragment.Companion.IMAGE_TYPE
import com.cboda.intelligent.fragment.AiAllFragment.Companion.PROJECT_RISK_TYPE
import com.cboda.intelligent.fragment.AiAllFragment.Companion.VOICE_TYPE

class AiPostFileBroadDialog(context: Context, val riskType: String, val resourceType: String) :
    BasePopupWindow(context) {

    private var lottie: LottieAnimationView? = null
    private var recyclerView: RecyclerView? = null
    private var adapter: VerticalTextBroadAdapter? = null

    private var contentTv: AppCompatTextView? = null
    private var listViewIndex = 0
    private val runTime = 1100L

    var dismissInvoke: () -> Unit = {}

    var onBackPressListener: (() -> Unit)? = null

    override fun onViewCreated(contentView: View) {
        super.onViewCreated(contentView)
        this.setOnDismissListener(onDismissListener)

        // 设置按键监听，拦截返回键
        contentView.isFocusableInTouchMode = true
        contentView.requestFocus()
        contentView.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                handleBackPress()
                true // 返回 true 表示已处理该事件，拦截返回键
            } else {
                false
            }
        }

        lottie = contentView.findViewById(R.id.lottie)
        recyclerView = contentView.findViewById(R.id.listView)
        contentTv = contentView.findViewById(R.id.contentTv)
        val title = contentView.findViewById<AppCompatTextView>(R.id.title)
        lottie?.imageAssetsFolder = "aipost"
        BarUtils.addMarginTopEqualStatusBarHeight(title)
        val listBeforeString = when (resourceType) {
            DOCUMENT_TYPE -> {
                if (riskType == PROJECT_RISK_TYPE) {
                    listOf(
                        context.getString(R.string.ai_team_background_authenticity),
                        context.getString(R.string.ai_business_model_sustainable),
                        context.getString(R.string.ai_ponzi_like_features),
                        context.getString(R.string.ai_profit_model_reasonable),
                        context.getString(R.string.ai_exaggerated_promises),
                        context.getString(R.string.ai_fake_certificates),
                        context.getString(R.string.ai_unfair_or_high_risk_terms)
                    )
                } else {
                    listOf(
                        context.getString(R.string.ai_unilateral_exemption),
                        context.getString(R.string.ai_unilateral_termination),
                        context.getString(R.string.ai_excessive_rights_grant),
                        context.getString(R.string.ai_data_privacy_issues),
                        context.getString(R.string.ai_unreasonable_liquidated_damages),
                        context.getString(R.string.ai_missing_key_terms),
                        context.getString(R.string.ai_ambiguous_terms)
                    )
                }
            }

            IMAGE_TYPE -> {
                listOf(
                    context.getString(R.string.ai_generation_features),
                    context.getString(R.string.ai_deepfake_features),
                    context.getString(R.string.ai_splicing_traces),
                    context.getString(R.string.ai_fake_id_cert),
                    context.getString(R.string.ai_image_background_consistency),
                    context.getString(R.string.ai_missing_or_abnormal_exif)
                )
            }

            VOICE_TYPE -> {
                listOf(
                    context.getString(R.string.ai_voice_forgery),
                    context.getString(R.string.ai_voice_clone),
                    context.getString(R.string.ai_rhythm_spectrum_anomaly),
                    context.getString(R.string.ai_audio_editing)
                )
            }

            else -> {
                listOf(
                    context.getString(R.string.ai_face_replacement_traces),
                    context.getString(R.string.ai_frame_flicker),
                    context.getString(R.string.ai_lip_sync_mismatch),
                    context.getString(R.string.ai_generated_texture),
                    context.getString(R.string.ai_background_composite),
                    context.getString(R.string.ai_missing_key_terms),
                    context.getString(R.string.ai_audio_video_consistency)
                )
            }
        }


        recyclerView?.also { recyclerView ->
            recyclerView.setOnTouchListener { _, _ ->
                true
            }
            val texts = listBeforeString.map { item ->
                context.getString(R.string.ai_analyzing_item, item)
            }
            adapter = VerticalTextBroadAdapter(texts)
            recyclerView.adapter = adapter
            startAutoSlide()
        }

    }

    override fun showPopupWindow() {
        super.showPopupWindow()
        eLogD("AiPostFileDialogClass", "lottie????null")
        lottie?.also {
            eLogD("AiPostFileDialogClass", "lottie!=null")
            lottie?.playAnimation()
            startAutoSlide()
        }
    }

    private val onDismissListener = object : OnDismissListener() {
        override fun onDismiss() {
            ThreadUtils.getMainHandler().removeCallbacks(run)
            lottie?.cancelAnimation()
            dismissInvoke.invoke()
        }
    }

    private val run = object : Runnable {
        override fun run() {
            ThreadUtils.getMainHandler().removeCallbacks(this)
//            eLogD("AiPostFileDialogClass", "RunnableIndex:$listViewIndex")
            contentTv?.let { contentTv ->
                val afterStr = when (listViewIndex % 3) {
                    0 -> "."
                    1 -> ".."
                    else -> "..."
                }
                contentTv.text = context.getString(R.string.正在深度分析) + afterStr
            }
            recyclerView?.let { recyclerView ->
                if (null != recyclerView.layoutManager && recyclerView.layoutManager is LinearLayoutManager) {
                    (recyclerView.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(
                        listViewIndex,
                        0
                    )
                }
                adapter?.updateCurrentIndex(listViewIndex)
                listViewIndex += 1
            }
            ThreadUtils.getMainHandler().postDelayed(this, runTime)
        }
    }


    private fun startAutoSlide() {
        ThreadUtils.getMainHandler().removeCallbacks(run)
        ThreadUtils.getMainHandler().postDelayed(run, runTime)
    }

    /**
     * 处理返回键事件
     * @return true 表示已处理，false 表示未处理
     */
    private fun handleBackPress(): Boolean {
        // 如果设置了自定义返回键监听器，优先使用自定义处理
        return onBackPressListener?.let { listener ->
            listener.invoke()
            false
        } ?: true
    }

    /**
     * 重写 onBackPressed 方法，确保返回键被拦截
     */
    override fun onBackPressed(): Boolean {
        return handleBackPress()
    }


    init {
        // 设置全屏
        width = ViewGroup.LayoutParams.MATCH_PARENT
        height = ViewGroup.LayoutParams.MATCH_PARENT

        // 设置弹窗位置为顶部，覆盖状态栏
        popupGravity = Gravity.TOP

        // 设置覆盖状态栏和导航栏
        setOverlayStatusbarMode(BasePopupFlag.OVERLAY_CONTENT)
        setOverlayNavigationBarMode(BasePopupFlag.OVERLAY_CONTENT)

        // 设置背景透明，让状态栏可见
        setBackgroundColor(android.graphics.Color.TRANSPARENT)

        // 设置内容视图
        contentView = LayoutInflater.from(context).inflate(R.layout.ai_post_file_broad_dialog, null)

        // 禁止点击外部关闭
        setOutSideDismiss(false)
        setBackPressEnable(true)
    }


    /**
     * 创建显示动画：从小到大的弹性缩放动画
     */
    override fun onCreateShowAnimation(): Animation {
        val scaleAnimation = ScaleAnimation(
            0.3f, 1.0f,  // fromXScale, toXScale
            0.3f, 1.0f,  // fromYScale, toYScale
            Animation.RELATIVE_TO_SELF, 0.5f,  // pivotXType, pivotXValue
            Animation.RELATIVE_TO_SELF, 0.5f   // pivotYType, pivotYValue
        ).apply {
            duration = 400
            interpolator = OvershootInterpolator(1.5f) // 弹性插值器，1.5f 控制弹性强度
        }

        val alphaAnimation = android.view.animation.AlphaAnimation(0.0f, 1.0f).apply {
            duration = 300
        }

        val animationSet = android.view.animation.AnimationSet(true).apply {
            addAnimation(scaleAnimation)
            addAnimation(alphaAnimation)
        }

        return animationSet
    }

    /**
     * 创建关闭动画：从大到小的缩放动画
     */
    override fun onCreateDismissAnimation(): Animation {
        val scaleAnimation = ScaleAnimation(
            1.0f, 0.3f,  // fromXScale, toXScale
            1.0f, 0.3f,  // fromYScale, toYScale
            Animation.RELATIVE_TO_SELF, 0.5f,  // pivotXType, pivotXValue
            Animation.RELATIVE_TO_SELF, 0.5f   // pivotYType, pivotYValue
        ).apply {
            duration = 300
            interpolator = android.view.animation.AccelerateInterpolator()
        }
        val alphaAnimation = android.view.animation.AlphaAnimation(1.0f, 0.0f).apply {
            duration = 250
        }
        val animationSet = android.view.animation.AnimationSet(true).apply {
            addAnimation(scaleAnimation)
            addAnimation(alphaAnimation)
        }
        return animationSet
    }
}