package com.cboda.intelligent.base

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout.LayoutParams
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.viewbinding.ViewBinding
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.cboda.baselibrary.utils.getAppColor
import com.gyf.immersionbar.ImmersionBar
import com.cboda.intelligent.R
import com.cboda.intelligent.app.MyBroadApp
import java.lang.reflect.Method
import com.cboda.intelligent.configuration.AppBroadConsts
import com.cboda.intelligent.utils.SharePrefBroadBroadUtil
import com.cboda.intelligent.utils.AppBroadUtils
import com.cboda.baselibrary.widge.LoadingBroadDialog
import com.cboda.select.picture.basic.BaseActivity

/**
 * 适配新UI
 */
abstract class BaseSimple2BroadActivity : BaseActivity() {

    /**
     * ViewBinding实例，仅在使用ViewBinding模式时可用
     * 使用Any类型以保持兼容性，实际使用时需要强制转换
     */
    protected var binding: ViewBinding? = null
        private set

    /**
     * 是否使用ViewBinding模式
     * 子类可以重写此方法返回true来启用ViewBinding
     */
    protected open fun useViewBinding(): Boolean = false

    /**
     * 获取ViewBinding的泛型类型
     * 子类可以重写此方法指定ViewBinding类型
     */
    protected open fun getViewBindingClass(): Class<out ViewBinding>? = null

    private val mHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        val language = MMKVBroadUtils.getYuYan()

//        val zhuti = getZt()

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            if (zhuti?.isNotEmpty() == true){
//                when (zhuti) {
//                    "深色模式" -> {
//                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
//                    }
//                    "浅色模式" -> {
//                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
//                    }
//                }
//            }
//
//        }
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
//        if (language?.isNotEmpty() == true) {
//            val yuyanconfig = resources?.configuration
//            when (language) {
//                "简体中文" -> {//简体中文
//                    yuyanconfig?.locale = Locale.CHINA
//                }
//
//                "繁體中文" -> {//繁体中文
////                    yuyanconfig?.locale = Locale.CHINESE
//                    yuyanconfig?.locale = Locale.forLanguageTag("zh-TW")
//                }
//
//                "English" -> {//英语
//                    yuyanconfig?.locale = Locale.ENGLISH
//                }
//
//                "हिन्दी" -> {//印地语
//                    yuyanconfig?.locale = Locale.forLanguageTag("hi")
//                }
//
//                "Español" -> {//西班牙语
//                    yuyanconfig?.locale = Locale.forLanguageTag("es")
//                }
//
//                "Français" -> {//法语
//                    yuyanconfig?.locale = Locale.FRENCH
//                }
//
//                "العربية" -> {//阿拉伯语
//                    yuyanconfig?.locale = Locale.forLanguageTag("ar")
//                }
//
//                "বাংলা" -> {//孟加拉语
//                    yuyanconfig?.locale = Locale.forLanguageTag("bn")
//                }
//
//                "русский" -> {//俄语
//                    yuyanconfig?.locale = Locale.forLanguageTag("ru")
//                }
//
//                "Português" -> {//葡萄牙语
//                    yuyanconfig?.locale = Locale.forLanguageTag("pt")
//                }
//
//                "Indonesia" -> {//印尼语
//                    yuyanconfig?.locale = Locale.forLanguageTag("id")
//                }
//
//                "اردو" -> {//乌尔都语
//                    yuyanconfig?.locale = Locale.forLanguageTag("ur")
//                }
//
//                "日本語" -> {//日本語
//                    yuyanconfig?.locale = Locale.forLanguageTag("ja")
//                }
//
//                "Deutsch" -> {//德语
//                    yuyanconfig?.locale = Locale.forLanguageTag("de")
//                }
//
//                "ਪੰਜਾਬੀ" -> {//旁遮普语
//                    yuyanconfig?.locale = Locale.forLanguageTag("de")
//                }
//
//                "Jawa" -> {//爪哇语
//                    yuyanconfig?.locale = Locale.forLanguageTag("jv")
//                }
//
//                "Filipino" -> {//菲律宾语
//                    yuyanconfig?.locale = Locale.forLanguageTag("in")
//                }
//
//                "Tiếng Việt" -> {//越南语
//                    yuyanconfig?.locale = Locale.forLanguageTag("vi")
//                }
//
//                "한국어" -> {//韩语
//                    yuyanconfig?.locale = Locale.forLanguageTag("ko")
//                }
//
//                "Türkçe" -> {//土耳其语
//                    yuyanconfig?.locale = Locale.forLanguageTag("tr")
//                }
//
//                "Italiano" -> {//意大利语
//                    yuyanconfig?.locale = Locale.forLanguageTag("it")
//                }
//
//                "ไทย" -> {//泰语
//                    yuyanconfig?.locale = Locale.forLanguageTag("th-TH")
//                }
//
//                "Bahasa Melayu" -> {//马来语
//                    yuyanconfig?.locale = Locale.forLanguageTag("ms")
//                }
//
//                "Polski" -> {//波兰语
//                    yuyanconfig?.locale = Locale.forLanguageTag("pl")
//                }
//
//                "Română" -> {//罗马尼亚语
//                    yuyanconfig?.locale = Locale.forLanguageTag("ro")
//                }
//
//                "ລາວ" -> {//老挝语
//                    yuyanconfig?.locale = Locale.forLanguageTag("lo")
//                }
//
//                "Монгол" -> {//蒙古语
//                    yuyanconfig?.locale = Locale.forLanguageTag("mn")
//                }
//
//                "Hausa" -> {//豪萨语
//                    yuyanconfig?.locale = Locale.forLanguageTag("ha")
//                }
//
//                "Kiswahili" -> {//斯瓦希里语
//                    yuyanconfig?.locale = Locale.forLanguageTag("sw")
//                }
//
//                "မြန်မာ" -> {//缅甸语
//                    yuyanconfig?.locale = Locale.forLanguageTag("my")
//                }
//
//                "नेपाली" -> {//尼泊尔语
//                    yuyanconfig?.locale = Locale.forLanguageTag("ne")
//                }
//
//                "isiZulu" -> {//祖鲁语
//                    yuyanconfig?.locale = Locale.forLanguageTag("zu")
//                }
//
//                "עברית" -> {//希伯来语
//                    yuyanconfig?.locale = Locale.forLanguageTag("he")
//                }
//
//                "Ελληνικά" -> {//希腊语
//                    yuyanconfig?.locale = Locale.forLanguageTag("el")
//                }
//
//                "українська" -> {//乌克兰语
//                    yuyanconfig?.locale = Locale.forLanguageTag("uk")
//                }
//            }
//            resources?.updateConfiguration(yuyanconfig, resources.displayMetrics)
//        }

        setContentView(R.layout.activity_broad_base2)
        initViews()
        initContentView()
//        var result = 0
//        val resourceId = resources.getIdentifier(
//            "status_bar_height",
//            "dimen", "android"
//        )
//        if (resourceId > 0) {
//            result = resources.getDimensionPixelSize(resourceId)
//        }
//        val linearParams = vitool?.layoutParams
//        linearParams?.height = result
//        vitool?.layoutParams = linearParams
        llContentLayout.fitsSystemWindows = !fitsSystemWindows()
        mFlTitleLayout?.visibility = if (showOrHideTitleLayout()) View.VISIBLE else View.GONE

        mIvBgTop?.visibility =
            if (showBgTopTitleLayout() || showTransparentTopTitleLayout()) View.GONE else View.VISIBLE
        if (showBgTopTitleLayout()) {
            llTitleLayout?.setBackgroundColor(getColor(R.color.white_or_black))
        } else if (showTransparentTopTitleLayout()) {
            llTitleLayout?.setBackgroundColor(getColor(R.color.transparent))
        }
        AppBroadUtils.activityList.add(this)
        backIcon?.setOnClickListener {
            onBackPressed()
        }
        AppBroadUtils.debug("当前页面", this.javaClass.simpleName ?: "")

        //        SharedPreferences sp = getSharedPreferences("wfc_kit_config", Context.MODE_PRIVATE);
//        if (sp.getBoolean("darkTheme", true)) {
//            // dark
//            toolbar.getContext().setTheme(R.style.AppTheme_DarkAppbar);
//            customToolbarAndStatusBarBackgroundColor(true);
//        } else {
        // light
//      setTheme(com.cboda.chatui.R.style.AppTheme_LightAppbar)

    }

    /**
     * 页面是否向状态栏扩展
     */
    protected open fun fitsSystemWindows(): Boolean {
        return false
    }

    private fun initContentView() {
        if (useViewBinding()) {
            // 使用ViewBinding模式
            try {
                binding = createViewBinding()
                binding?.let { vb ->
                    val imgLayout = LayoutParams(
                        LayoutParams.MATCH_PARENT,
                        LayoutParams.MATCH_PARENT
                    )
                    findViewById<LinearLayoutCompat>(R.id.llContentLayout).addView(
                        vb.root,
                        imgLayout
                    )
                }
            } catch (e: Exception) {
                // ViewBinding创建失败，回退到传统方式
                AppBroadUtils.debug("ViewBinding创建失败，回退到传统方式", e.message ?: "")
                LayoutInflater.from(this).inflate(getLayoutId(), llContentLayout, true)
            }
        } else {
            // 使用传统layoutId方式
            LayoutInflater.from(this).inflate(getLayoutId(), llContentLayout, true)
        }
    }

    /**
     * 创建ViewBinding实例
     * 使用反射自动创建，子类也可以重写此方法提供自定义实现
     */
    @Suppress("UNCHECKED_CAST")
    protected open fun createViewBinding(): ViewBinding? {
        return try {
            // 首先尝试从getViewBindingClass()获取类型
            val clazz = getViewBindingClass()
                ?: throw IllegalStateException("请重写getViewBindingClass()方法或createViewBinding()方法")

            val inflateMethod: Method = clazz.getMethod("inflate", LayoutInflater::class.java)
            inflateMethod.invoke(null, layoutInflater) as ViewBinding
        } catch (e: Exception) {
            AppBroadUtils.debug("ViewBinding反射创建失败", e.message ?: "")
            null
        }
    }

    /**
     * 获取布局ID - 保持向后兼容
     * 仅在不使用ViewBinding时需要实现
     */
    protected open fun getLayoutId(): Int {
        throw IllegalStateException("请实现getLayoutId()方法或启用ViewBinding模式")
    }


    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        ImmersionBar.with(this)
            .statusBarColor(if (transparentStatus()) R.color.transparent else getStatusBarColor())
            .statusBarDarkFont(useStatusBarDarkFont()).keyboardEnable(isEnableKeyboard()).init()
//        window.decorView.systemUiVisibility = (
//                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
////                        or View.SYSTEM_UI_FLAG_FULLSCREEN
//                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//                )
    }

    open fun isEnableKeyboard(): Boolean {
        return false
    }

    /**
     * 状态栏是否是透明的
     */
    protected open fun transparentStatus(): Boolean {
        return true
    }

    /**
     * 状态栏的颜色
     */
    protected open fun getStatusBarColor(): Int {
        return R.color.transparent
    }

    /**
     * 是否使用状态栏字体为深色
     */
    protected open fun useStatusBarDarkFont(): Boolean {
        return true
    }

    open fun getToolbarTitle(): TextView? {
        return tvTitle
    }

    open fun setRootBgColor(@ColorInt int: Int) {
        mViewRootBg.setBackgroundColor(getAppColor(this, int))
    }


    open fun getRightView(): TextView? {
        return tvRight
    }

    open fun getRightImg(): AppCompatImageView? {
        return rightIcon
    }

    open fun setIvBgTopImg(@DrawableRes resId: Int) {
        mIvBgTop.isVisible = true
        mIvBgTop.setImageResource(resId)
    }


    /**
     * 是否显示顶部导航
     */
    open fun showOrHideTitleLayout(): Boolean {
        return true
    }

    /**
     * 是否显示白色顶部背景+黑字
     */
    open fun showBgTopTitleLayout(): Boolean {
        return false
    }

    open fun showTransparentTopTitleLayout(): Boolean {
        return false
    }


    override fun onDestroy() {
        if (dialogHelper?.isShowing == true) {
            dismissLoading()
        } else {
            mHandler.removeCallbacks(mLoadingTask)
        }
        AppBroadUtils.activityList.remove(this)
        // 清理ViewBinding引用，防止内存泄漏
        binding = null
        super.onDestroy()
    }


    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        return try {
            if (ev.action == MotionEvent.ACTION_DOWN) {
                // 获得当前得到焦点的View，一般情况下就是EditText（特殊情况就是轨迹求或者实体案件会移动焦点）
                val v = currentFocus
                if (isShouldHideInput(v, ev)) {
                    hideSoftInput()
                }
            }
            super.dispatchTouchEvent(ev)
        } catch (e: Exception) {
            true
        }
    }

    open fun isShouldHideInput(v: View?, event: MotionEvent): Boolean {
        if (v != null && v is EditText) {
            val l = intArrayOf(0, 0)
            v.getLocationInWindow(l)
            val left = l[0]
            val top = l[1]
            val bottom = top + v.getHeight()
            val right = (left
                    + v.getWidth())
            return !(event.x > left && event.x < right && event.y > top && event.y < bottom)
        }
        // 如果焦点不是EditText则忽略，这个发生在视图刚绘制完，第一个焦点不在EditView上，和用户用轨迹球选择其他的焦点
        return false
    }

    open fun hideSoftInput() {
        if (currentFocus == null || currentFocus?.windowToken == null) {
            return
        }
        val inputMethodManager: InputMethodManager =
            getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(
            currentFocus?.windowToken,
            InputMethodManager.HIDE_NOT_ALWAYS
        )
    }

    override fun startActivity(intent: Intent?) {
        super.startActivity(intent)
        overridePendingTransition(R.anim.anim_right_broad_in, R.anim.anim_left_broad_out)
    }

    open fun getContentView(): View {
        return llContentLayout
    }

    private var dialogHelper: LoadingBroadDialog? = null
    private lateinit var llContentLayout: LinearLayoutCompat
    private lateinit var llTitleLayout: LinearLayout
    private lateinit var mFlTitleLayout: View
    lateinit var mIvBgTop: ImageView
    private lateinit var rightIcon: AppCompatImageView
    private lateinit var tvRight: TextView
    private lateinit var tvTitle: TextView
    private lateinit var backIcon: AppCompatImageView
    private lateinit var vitool:
            View
    private lateinit var mViewRootBg: View
    private lateinit var backLottie:
            LottieAnimationView

    private fun initViews() {
        backLottie = findViewById(R.id.backLottie)
        vitool = findViewById(R.id.vitool)
        backIcon = findViewById(R.id.backIcon)
        tvTitle = findViewById(R.id.tvTitle)
        tvRight = findViewById(R.id.tvRight)
        rightIcon = findViewById(R.id.rightIcon)
        llTitleLayout = findViewById(R.id.llTitleLayout)
        mIvBgTop = findViewById(R.id.mIvBgTop)
        llContentLayout = findViewById(R.id.llContentLayout)
        mFlTitleLayout = findViewById(R.id.mFlTitleLayout)
        mViewRootBg = findViewById(R.id.mViewRootBg)
    }

    private val mLoadingTask = Runnable {
        if (this.isFinishing || this.isDestroyed) {
            return@Runnable
        }
        if (dialogHelper == null) {
            dialogHelper = LoadingBroadDialog(this,"")
        }
        dialogHelper?.show()
        dialogHelper?.setCanceledOnTouchOutside(true)
        dialogHelper?.setCancelable(true)
    }

    fun setLottiePath(
        path: String,
        scaleType: ImageView.ScaleType? = null,
        repeatCount: Int? = null,
        imgPath: String? = null
    ) {
        backLottie.imageAssetsFolder = imgPath ?: "images"
        backLottie.scaleType = scaleType ?: ImageView.ScaleType.FIT_XY
        backLottie.repeatCount = repeatCount ?: LottieDrawable.INFINITE
        backLottie.setAnimation(path)
        backLottie.playAnimation()
        setBackLottieIsShow(true)
    }

    fun setBackLottieIsShow(boolean: Boolean) {
        backLottie.isGone = !boolean
    }

    fun setBackLottieIsShowAlpha(boolean: Boolean) {
        backLottie.alpha = if (boolean) 1f else 0f
    }

    fun showLoading() {
        mHandler.postDelayed(mLoadingTask, 300L)
    }

    protected fun isDisMissLoading(isDismiss: Boolean) {
        dialogHelper?.setCanceledOnTouchOutside(isDismiss)
        dialogHelper?.setCancelable(isDismiss)
    }

    fun dismissLoading() {
        mHandler.removeCallbacks(mLoadingTask)
        dialogHelper?.dismiss()
    }

    override fun getResources(): Resources {
        val res = super.getResources()
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N) {
            val config = res.configuration
            config.fontScale = getTextSize(MyBroadApp.mContext)
            res.updateConfiguration(config, res.displayMetrics)
        }
        return res
    }

    override fun attachBaseContext(newBase: Context?) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
            val res = newBase!!.resources
            val config = res.configuration
            config.fontScale = getTextSize(newBase)
            val newContext = newBase!!.createConfigurationContext(config)
            super.attachBaseContext(newContext)
        } else {
            super.attachBaseContext(newBase)
        }
    }

    //设置字体大、中、小
    protected fun getTextSize(newBase: Context?): Float {
        val fontScale: Float = SharePrefBroadBroadUtil.getString(newBase, AppBroadConsts.FontScale, "1").toFloat()
        return fontScale
    }

}