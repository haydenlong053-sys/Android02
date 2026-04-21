package com.cboda.baselibrary.widge

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.StringRes
import androidx.core.view.isGone
import com.cboda.baselibrary.R
import com.cboda.baselibrary.databinding.VpGuViewBinding
import com.cboda.baselibrary.utils.MMKVBroadUtils
import com.cboda.baselibrary.utils.MMKVBroadUtils.Companion.USER_REGISTRATION_DAY_SCORE
import com.cboda.baselibrary.utils.anyToInt
import com.cboda.baselibrary.utils.formatWithBigDecimal
import com.cboda.baselibrary.utils.getAppStr
import com.cboda.baselibrary.utils.loadImg
import com.cboda.baselibrary.utils.setBackColor
import com.cboda.baselibrary.utils.setBackColor2
import com.cboda.baselibrary.utils.setMarginBottom
import com.cboda.baselibrary.utils.setPaddingHorizontal
import com.cboda.baselibrary.utils.setTxtColor

class VpGradeView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {
    private lateinit var binding: VpGuViewBinding
    private var imgWidth = 0
    private var gradeTextSize = 0
    private var vipWidth = 0
    private var gradeHeight = 0
    private var gradePad = 0
    private var vipMarginBottom = 0


    init {
        attrs?.let {
            context.obtainStyledAttributes(it, R.styleable.VpGradeView).apply {
                imgWidth = getDimensionPixelSize(R.styleable.VpGradeView_user_img_width, resources.getDimensionPixelSize(R.dimen.dp_48))
                vipWidth = getDimensionPixelSize(R.styleable.VpGradeView_user_vip_width, resources.getDimensionPixelSize(R.dimen.dp_25))
                gradeHeight = getDimensionPixelSize(R.styleable.VpGradeView_user_grade_height, resources.getDimensionPixelSize(R.dimen.dp_14))
                gradePad = getDimensionPixelSize(R.styleable.VpGradeView_user_grade_pad, resources.getDimensionPixelSize(R.dimen.dp_4))
                gradeTextSize = getDimensionPixelSize(R.styleable.VpGradeView_user_grade_text_size, resources.getDimensionPixelSize(R.dimen.dp_8))
                vipMarginBottom = getDimensionPixelSize(R.styleable.VpGradeView_user_vip_margin_bottom, resources.getDimensionPixelSize(R.dimen.dp_10_minus))
                recycle()
            }
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        val inflate =
            LayoutInflater.from(context).inflate(R.layout.vp_gu_view, this, false)
        addView(inflate)
        initView(inflate)
        initListener()
    }

    private fun initView(view: View) {
        binding = VpGuViewBinding.bind(view)
        binding.vpGvUserImg.layoutParams.width = imgWidth
        binding.vpGvUserImg.layoutParams.height = imgWidth
        binding.vpGvVip2.layoutParams.width = vipWidth
        binding.vpGvVip2.layoutParams.height = vipWidth
        binding.vpGvGradeTv.layoutParams.height = gradeHeight
        binding.vpGvGradeTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, gradeTextSize.toFloat())
        binding.vpGvGradeTv.setPaddingHorizontal(gradePad)
        binding.vpGvVip2.setMarginBottom(vipMarginBottom)
    }

    @SuppressLint("SetTextI18n")
    fun setData3(isNiceNum: Boolean?, isVip: Boolean?, grade: String?, registerDay: String?) {
        binding.vpGvVip2.isGone = isVip != true
        binding.vpGvVip1.isGone = isVip != true
        if (null != grade) {
            if (isNiceNum == true) {
                binding.vpGvGradeTv.setBackColor2(R.color.black, R.color.color_31231A, R.color.black)
                binding.vpGvGradeTv.setTxtColor(R.color.color_FAE77C)
            } else {
                val registrationDayScore = anyToInt(MMKVBroadUtils.getSqlStrData(USER_REGISTRATION_DAY_SCORE, ""))
                val registrationDay = anyToInt(registerDay)
                if (null != registerDay && registrationDay >= registrationDayScore) {
                    binding.vpGvGradeTv.setBackColor(R.color.color_956BEC, R.color.color_7A66FA)
                } else {
                    binding.vpGvGradeTv.setBackColor(R.color.color_D5271E)
                }
                binding.vpGvGradeTv.setTxtColor(R.color.white)
            }
        }
        setGradeData(grade)
    }


    fun setData(imgUrl: String?, isNiceNum: Boolean?, isVip: Boolean?, grade: String?, registerDay: String?) {
        setData3(isNiceNum, isVip, grade, registerDay)
        loadUserImg(imgUrl)
    }

    fun setData2(imgUrl: String?, isNiceNum: Boolean?, isVip: Boolean?, grade: Double?, registerDay: Int?) {
        val mGrade = grade?.toString()
        val mRegisterDay = registerDay?.toString()
        setData(imgUrl, isNiceNum, isVip, mGrade, mRegisterDay)
    }

    fun setData5(isNiceNum: Boolean?, isVip: Boolean?, grade: Double?, registerDay: Int?) {
        val mGrade = grade?.toString()
        val mRegisterDay = registerDay?.toString()
        setData3(isNiceNum, isVip, mGrade, mRegisterDay)
    }

    @SuppressLint("SetTextI18n")
    fun setGradeData(grade: Any?) {
        if (null != grade) {
            binding.vpGvGradeTv.text = formatWithBigDecimal(grade) + getAppStr(R.string.msg_score_unit3, context)
        } else {
            binding.vpGvGradeTv.text = ""
        }
        binding.vpGvGradeTv.isGone = null == grade
    }

    @SuppressLint("SetTextI18n")
    fun setRegisterDay(registerDay: Int?, @StringRes res: Int = R.string.msg_score_unit3) {
        if (null != registerDay) {
            binding.vpGvGradeTv.setBackColor(R.color.color_956BEC, R.color.color_7A66FA)
            binding.vpGvGradeTv.setTxtColor(R.color.white)
            binding.vpGvGradeTv.text = registerDay.toString() + getAppStr(res, context)
            binding.vpGvGradeTv.isGone = false
        } else {
            binding.vpGvGradeTv.isGone = true
        }

    }

    fun loadUserImg(imgUrl: String?) {
        binding.vpGvUserImg.loadImg(imgUrl)
    }

    fun getUserImgView() = binding.vpGvUserImg

    private fun initListener() {


    }


    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
    }

}