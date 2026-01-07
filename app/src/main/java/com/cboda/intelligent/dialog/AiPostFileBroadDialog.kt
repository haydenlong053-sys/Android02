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
import com.cboda.intelligent.fragment.AiAllBroadFragment.Companion.DOCUMENT_TYPE
import com.cboda.intelligent.fragment.AiAllBroadFragment.Companion.IMAGE_TYPE
import com.cboda.intelligent.fragment.AiAllBroadFragment.Companion.PROJECT_RISK_TYPE
import com.cboda.intelligent.fragment.AiAllBroadFragment.Companion.VOICE_TYPE

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
                        "团队背景真实性（学历、头像、过往经历）",
                        "商业模式是否可持续",
                        "是否具有资金盘特征",
                        "盈利模式是否合理",
                        "是否使用夸大承诺、保底收益",
                        "是否存在伪造证书、虚构资质",
                        "不合理条款或高危表述"
                    )
                } else {
                    listOf(
                        "单方免责",
                        "单方终止权",
                        "过度权利授予",
                        "数据隐私问题",
                        "违约金不合理",
                        "缺失关键条款",
                        "模糊条款"
                    )
                }
            }

            IMAGE_TYPE -> {
                listOf(
                    "AI生成特征（指纹、光影、结构异常）",
                    "Deepfake 特征",
                    "拼接痕迹",
                    "伪造身份证/证书",
                    "图像与背景是否一致",
                    "元信息（exif）缺失或异常"
                )
            }

            VOICE_TYPE -> {
                listOf(
                    "AI伪造",
                    "声线克隆",
                    "节奏/频谱异常",
                    "混音剪辑"
                )
            }

            else -> {
                listOf(
                    "人脸替换痕迹",
                    "帧间闪烁",
                    "口型不一致",
                    "AI生成纹理",
                    "背景合成",
                    "缺失关键条款",
                    "音频与视频是否一致"
                )
            }
        }


        recyclerView?.also { recyclerView ->
            recyclerView.setOnTouchListener { _, _ ->
                true
            }
            val texts = listBeforeString.map { "正在分析$it" }
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
                contentTv.text = "正在深度分析$afterStr"
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
        contentView = LayoutInflater.from(context).inflate(R.layout.ai_post_file_dialog, null)

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