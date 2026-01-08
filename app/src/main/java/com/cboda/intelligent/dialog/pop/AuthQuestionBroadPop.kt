package com.cboda.intelligent.dialog.pop

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.cboda.baselibrary.popup.BaseBottomBroadPop
import com.cboda.intelligent.BuildConfig
import com.cboda.intelligent.R
import com.cboda.intelligent.bean.ResultBroadBean
import com.cboda.intelligent.configuration.SafeJumpBroadEnum
import com.cboda.intelligent.databinding.DialogPopAuthBroadQuestionBinding
import com.cboda.intelligent.utils.BaseBroadCallback
import com.cboda.intelligent.utils.isSubmitCanGo
import com.cboda.intelligent.viewmodel.SafeCenterViewBroadModel
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import okhttp3.Request
import okhttp3.Response


//密保问题验证
class AuthQuestionBroadPop(val context: Context, val jumpSource: Int = SafeJumpBroadEnum.None.sourceType,val isDeepColor: Boolean =false,val account:String?=null,) :
    BaseBottomBroadPop(context) {
    private val scope = MainScope() // 创建主线程的协程作用域
    private var viewModel: SafeCenterViewBroadModel? = null
    @JvmField
    var backInvoke:()-> Unit ={}
    private var binding: DialogPopAuthBroadQuestionBinding = DialogPopAuthBroadQuestionBinding.inflate(
        LayoutInflater.from(context)
    )

    init {
        contentView = binding.root
        setBackgroundColor(Color.parseColor("#8f000000"))

    }

    override fun onViewCreated(contentView: View) {
        super.onViewCreated(contentView)
        initDeepColor()
        binding.mTvSubmit.isSubmitCanGo(false)
        binding.mEtAnswer.addTextChangedListener {
            binding.mTvSubmit.isSubmitCanGo(binding.mEtAnswer.text.trim().toString().isNotEmpty())
        }
        binding.mIvClose.setOnClickListener {
            dismiss()
        }
        initData()
    }

    private fun initDeepColor() {
        if (isDeepColor){
            binding.mViewTopBg.setBackgroundResource(R.drawable.bg_rect_deep_only_top_18dp)
            binding.mIvClose.setImageResource(R.mipmap.ic_auth_close2)
            binding.mIvGo.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.white))
            binding.mTvTitle.setTextColor(ContextCompat.getColor(context,R.color.white))
            binding.mTvDes2.setTextColor(ContextCompat.getColor(context, com.cboda.baselibrary.R.color.color_white_65))
            binding.mTvDes3.setTextColor(ContextCompat.getColor(context, com.cboda.baselibrary.R.color.color_white_65))
            binding.mTvSecurityName.setBackgroundResource(R.drawable.bg_rect_save_deep_edit_16dp)
            binding.mEtAnswer.setBackgroundResource(R.drawable.bg_rect_save_deep_edit_16dp)
            binding.mEtAnswer.setTextColor(ContextCompat.getColor(context,R.color.white))
            binding.mEtAnswer.setHintTextColor(ContextCompat.getColor(context, com.cboda.baselibrary.R.color.color_629CF8_35))


        }
    }

    private fun initData() {
        if (context is FragmentActivity) {
            val activity = context
            // 获取该 Activity 作用域的 ViewModel
            viewModel =
                ViewModelProvider(activity).get<SafeCenterViewBroadModel>(SafeCenterViewBroadModel::class.java)
            // 现在可以使用 viewModel 进行数据操作
            if (jumpSource == SafeJumpBroadEnum.LoginForgetPassword.sourceType){
                scope.launch {
                    viewModel?.goSecurityInfo(account)?.observe(activity){
                        binding.mTvSecurityName.text = it.securityQuestions
                    }
                }

            }else {
                viewModel?.userInfo(activity, object : BaseBroadCallback<ResultBroadBean>() {
                    override fun onError(response: Response, code: Int, e: Exception) {}
                    override fun onSuccess(response: Response?, t: ResultBroadBean) {
                        binding.mTvSecurityName.text = t.data.mbName

                    }

                    override fun onBeforeRequest(request: Request?) {
                    }

                    override fun onFailure(request: Request?, e: java.lang.Exception?) {
                    }

                    override fun onResponse(response: Response?) {
                    }
                })
            }


            binding.mTvSubmit.setOnClickListener {
                if (binding.mTvSecurityName.text.trim()
                        .isNotEmpty() && binding.mEtAnswer.text.trim().isNotEmpty()
                ) {
                    scope.launch {
                        val type = if (jumpSource == SafeJumpBroadEnum.TransferAuthSure.sourceType) {
                            1
                        } else if (jumpSource == SafeJumpBroadEnum.LoginForgetPassword.sourceType){
                            2
                        }else {
                            0
                        }
                        viewModel?.checkSecurityInfo(
                            binding.mTvSecurityName.text.trim().toString(),
                            binding.mEtAnswer.text.trim().toString(), type,account
                        )?.observe(activity) {
                            if (BuildConfig.DEBUG) {
                                Log.d("zhanglong", "jumpSource==$jumpSource")
                            }

                            when (jumpSource) {
                                SafeJumpBroadEnum.GoogleAuth.sourceType -> {
                                    backInvoke.invoke()
                                }

                                SafeJumpBroadEnum.PasswordAuth.sourceType -> {
                                    backInvoke.invoke()

                                }

                                SafeJumpBroadEnum.QuestionAuth.sourceType -> {
                                    backInvoke.invoke()
                                }

                                SafeJumpBroadEnum.TransferAuthSure.sourceType -> {
                                    val mPop = AuthPasswordBroadPop(
                                        context,
                                        SafeJumpBroadEnum.TransferAuthSure.sourceType
                                    )
                                    mPop.showPopupWindow()

                                }
                                SafeJumpBroadEnum.ChangeMachineAuth.sourceType -> {
                                    backInvoke.invoke()
                                }
                                SafeJumpBroadEnum.LoginNewMachine.sourceType -> {
                                    backInvoke.invoke()
                                }
                                SafeJumpBroadEnum.LoginForgetPassword.sourceType -> {
                                    backInvoke.invoke()
                                }

                            }
                            dismiss()
                        }
                    }
                }
            }

        }

    }

    override fun onDismiss() {
        scope.cancel()
        super.onDismiss()
    }
}