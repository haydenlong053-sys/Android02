package com.cboda.baselibrary.popup

import android.content.Context
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.isGone
import com.cboda.baselibrary.R
import com.cboda.baselibrary.databinding.CommonTipBroadPopBinding
import com.cboda.baselibrary.utils.setBackColorAndStrokeColor
import com.cboda.baselibrary.utils.setTxtColor
import razerdp.basepopup.BasePopupWindow

//简单基础提示pop
class CommonTipBroadPop(
    val context: Context, val title: String = "", val des: String = "", val backColor: Int = R.color.white, val strokeColor: Int = R.color.white,
    val titleColor: Int = R.color.black, val contentColor: Int = R.color.black_65, val cancelTvColor: Int = R.color.color_3B47DDk, val sureTvColor: Int = R.color.white,
    val cancelStrokeColor: Int = R.color.color_3B47DD, val sureStrokeColor: Int = R.color.color_3B47DD,
    val cancelStartColor: Int = R.color.white, val cancelEndColor: Int = R.color.white,
    val sureStartColor: Int = R.color.color_3B47DD, val sureEndColor: Int = R.color.color_3B47DD, getOutSideTouchable: Boolean = true
) :
    BasePopupWindow(context) {
    //    private val scope = MainScope() // 创建主线程的协程作用域
//    private var viewModel: TransferViewBroadModel? = null
    var sureInvoke: () -> Unit = {}

    //添加取消回調
    var cancelInvoke: () -> Unit = {}
    private var binding = CommonTipBroadPopBinding.inflate(
        LayoutInflater.from(context)
    )

    init {
        contentView = binding.root
        popupGravity = Gravity.CENTER
        isOutSideTouchable = getOutSideTouchable
        setOutSideDismiss(getOutSideTouchable)
        setBackPressEnable(getOutSideTouchable)
    }

    override fun onViewCreated(contentView: View) {
        super.onViewCreated(contentView)
        binding.mTvCancel.setOnClickListener {
            cancelInvoke.invoke()
            dismiss()
        }
        binding.mTvSure.setOnClickListener {
            dismiss()
            sureInvoke.invoke()
        }
        initData()

    }

    fun setSureBtn(strName: String = ""): BasePopupWindow {
        binding.mTvSure.text = strName
        return this
    }

    fun setCancelBtn(strName: String = ""): BasePopupWindow {
        binding.mTvCancel.text = strName
        binding.mTvCancel.isGone = TextUtils.isEmpty(strName)
        return this
    }

    private fun initData() {
        binding.mTvTitle.text = title
        binding.mTvDes.text = des
        binding.mLlSaveView.setBackColorAndStrokeColor(backColor, strokeColor)
        binding.mTvTitle.setTxtColor(titleColor)
        binding.mTvDes.setTxtColor(contentColor)
        binding.mTvCancel.setBackColorAndStrokeColor(cancelStartColor, cancelEndColor, cancelStrokeColor)
        binding.mTvCancel.setTxtColor(cancelTvColor)
        binding.mTvSure.setBackColorAndStrokeColor(sureStartColor, sureEndColor, sureStrokeColor)
        binding.mTvSure.setTxtColor(sureTvColor)
//        if (context is AppCompatActivity) {
//            val activity = context
//            viewModel =
//                ViewModelProvider(activity).get<TransferViewBroadModel>(TransferViewBroadModel::class.java)
//        }

    }


    override fun onDismiss() {
//        scope.cancel()
        super.onDismiss()
    }
}