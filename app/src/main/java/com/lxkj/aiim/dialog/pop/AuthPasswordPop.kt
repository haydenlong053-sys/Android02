package com.lxkj.aiim.dialog.pop

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.base.commonlibrary.popup.BaseBottomPop
import com.lxkj.aiim.R
import com.lxkj.aiim.lj.bean.ResultBean
import com.lxkj.aiim.lj.configuration.SafeJumpEnum
import com.lxkj.aiim.lj.event.ActionRefreshEvent
import com.lxkj.aiim.lj.event.TransferEventBean
import com.lxkj.aiim.lj.http.SpotsCallBack
import com.lxkj.aiim.lj.utils.SoftKeyBoardUtils
import com.lxkj.aiim.lj.utils.showToastMain
import com.lxkj.aiim.model.PersonInfoBean
import com.lxkj.aiim.viewmodel.PasswordViewModel
import com.maning.pswedittextlibrary.MNPasswordEditText
import com.maning.pswedittextlibrary.MNPasswordEditText.OnTextChangeListener
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import okhttp3.Request
import okhttp3.Response
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


//支付密码pop
class AuthPasswordPop(
    val context: Context,
    val jumpSource: Int = SafeJumpEnum.None.sourceType,
    val oldPassWord: String = "",
    val isDeepColor: Boolean = false,
    val account:String?=null,
) :
    BaseBottomPop(context) {
    private val scope = MainScope() // 创建主线程的协程作用域
    private var viewModel: PasswordViewModel? = null
    var backInvoke: () -> Unit = {}
    private lateinit var keyboardHelper: KeyboardHelper
    private lateinit var mIvClose: ImageView
    private lateinit var pswEdit: MNPasswordEditText
    private lateinit var mLlMoney: View
    private lateinit var mTvTitle: TextView
    private lateinit var mTvMoney: TextView
    private lateinit var mViewRoot: ViewGroup


    init {
        if (isDeepColor) {
            setContentView(R.layout.dialog_pop_password_auth_deep_color)
        } else {
            setContentView(R.layout.dialog_pop_password_auth)

        }
        setBackgroundColor(Color.parseColor("#8f000000"))
    }

    override fun onViewCreated(contentView: View) {
        super.onViewCreated(contentView)
        initMyView(contentView)
        initTitle()
        //注意此处有粘性的event事件需要在初始化view之后调用
        EventBus.getDefault().register(this)
        keyboardHelper = KeyboardHelper(context as Activity, mViewRoot)
        SoftKeyBoardUtils.openSoftKeyBoard(pswEdit)
        keyboardHelper.enable()
        mIvClose.setOnClickListener {
            dismiss()
        }
        pswEdit.setOnTextChangeListener(object : OnTextChangeListener {
            override fun onTextChange(text: String, isComplete: Boolean) {
                if (isComplete && context is AppCompatActivity) {
                    val activity = context
                    if (SafeJumpEnum.PasswordChange.sourceType == jumpSource) {
                        val mPop = AuthPasswordPop(
                            context,
                            SafeJumpEnum.PasswordChangeSure.sourceType,
                            text
                        )
                        mPop.showPopupWindow()
                        dismiss()

                    } else if (SafeJumpEnum.PasswordBind.sourceType == jumpSource) {
                        val mPop =
                            AuthPasswordPop(context, SafeJumpEnum.PasswordBindSure.sourceType, text)
                        mPop.showPopupWindow()
                        dismiss()

                    } else if (SafeJumpEnum.PasswordChangeSure.sourceType == jumpSource || SafeJumpEnum.PasswordBindSure.sourceType == jumpSource) {
                        if (oldPassWord == text) {
                            viewModel =
                                ViewModelProvider(activity).get<PasswordViewModel>(PasswordViewModel::class.java)
                            viewModel?.setPaypwd(
                                context,
                                text,
                                object : SpotsCallBack<ResultBean>(context) {
                                    override fun onSuccess(
                                        response: Response?,
                                        resultBean: ResultBean?
                                    ) {
                                        //重置密码成功
                                        dismiss()
                                        EventBus.getDefault().post(ActionRefreshEvent())

                                    }

                                    override fun onError(
                                        response: Response?,
                                        code: Int,
                                        e: Exception?
                                    ) {
                                    }
                                })
                        } else {
                            showToastMain(context.getString(R.string.两次密码不一致))
                        }

                    } else {
                        // 获取该 Activity 作用域的 ViewModel
                        viewModel =
                            ViewModelProvider(activity).get<PasswordViewModel>(PasswordViewModel::class.java)
                        // 现在可以使用 viewModel 进行数据操作
                        val type = if (jumpSource == SafeJumpEnum.TransferAuthSure.sourceType) {
                            1
                        }else if (jumpSource == SafeJumpEnum.LoginForgetPassword.sourceType){
                            2
                        } else {
                            0
                        }
                        viewModel?.checkPayPwd(
                            context,
                            text,
                            type,
                            account,
                            object : SpotsCallBack<ResultBean>(context) {
                                override fun onSuccess(
                                    response: Response?,
                                    resultBean: ResultBean?
                                ) {
                                    dismiss()
                                    when (jumpSource) {
                                        //验证逻辑
                                        SafeJumpEnum.GoogleAuth.sourceType -> {
                                            backInvoke.invoke()
                                        }

                                        SafeJumpEnum.PasswordAuth.sourceType -> {
                                            backInvoke.invoke()
                                        }

                                        SafeJumpEnum.QuestionAuth.sourceType -> {
                                            backInvoke.invoke()
                                        }

                                        SafeJumpEnum.ChangeMachineAuth.sourceType -> {
                                            backInvoke.invoke()
                                        }

                                        SafeJumpEnum.LoginNewMachine.sourceType -> {
                                            backInvoke.invoke()
                                        }

                                        SafeJumpEnum.LoginForgetPassword.sourceType -> {
                                            backInvoke.invoke()
                                        }

                                        SafeJumpEnum.TransferAuthSure.sourceType -> {
                                            EventBus.getDefault().post(TransferEventBean().apply {
                                                this.isFinishAction = true
                                            })

                                        }

                                    }

                                }

                                override fun onFailure(request: Request?, e: java.lang.Exception?) {
                                    super.onFailure(request, e)
                                }

                                override fun onError(
                                    response: Response?,
                                    code: Int,
                                    e: Exception?
                                ) {
                                    dismiss()
                                }
                            })
                    }

                }
            }
        })
        initData()
    }

    private fun initMyView(contentView: View) {
        mIvClose = contentView.findViewById(R.id.mIvClose)
        pswEdit = contentView.findViewById(R.id.pswEdit)
        mLlMoney = contentView.findViewById(R.id.mLlMoney)
        mTvTitle = contentView.findViewById(R.id.mTvTitle)
        mTvMoney = contentView.findViewById(R.id.mTvMoney)
        mViewRoot = contentView.findViewById(R.id.mViewRoot)

    }

    private fun initTitle() {
        mLlMoney.isVisible = (jumpSource == SafeJumpEnum.TransferAuthSure.sourceType)
        val string = when (jumpSource) {
            SafeJumpEnum.PasswordAuth.sourceType, SafeJumpEnum.GoogleAuth.sourceType, SafeJumpEnum.QuestionAuth.sourceType, SafeJumpEnum.ChangeMachineAuth.sourceType, SafeJumpEnum.LoginNewMachine.sourceType, SafeJumpEnum.LoginForgetPassword.sourceType -> {
                context.getString(R.string.mine_password_title_3)
            }

            SafeJumpEnum.PasswordChange.sourceType -> {
                context.getString(R.string.mine_password_title_4)
            }

            SafeJumpEnum.PasswordChangeSure.sourceType -> {
                context.getString(R.string.mine_password_title_5)
            }

            SafeJumpEnum.PasswordBind.sourceType -> {
                context.getString(R.string.mine_password_title_1)
            }

            SafeJumpEnum.PasswordBindSure.sourceType -> {
                context.getString(R.string.mine_password_title_2)
            }

            else -> {
                context.getString(R.string.mine_password_title_1)
            }
        }
        mTvTitle.text = string
    }


    class KeyboardHelper(private val activity: Activity, private val rootLayout: ViewGroup) {

        private var defaultBottomPadding = 0

        fun enable() {
            // 存储原始的底部内边距
            defaultBottomPadding = rootLayout.paddingBottom
            val decorView = activity.window.decorView
            decorView.viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)
        }

        fun disable() {
            val decorView = activity.window.decorView
            decorView.viewTreeObserver.removeOnGlobalLayoutListener(globalLayoutListener)
            // 恢复原始布局
            rootLayout.setPadding(0, 0, 0, defaultBottomPadding)
        }

        private val globalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
            val rect = Rect()
            val decorView = activity.window.decorView
            decorView.getWindowVisibleDisplayFrame(rect)

            val screenHeight = decorView.height
            val keyboardHeight = screenHeight - rect.bottom

            // 通常认为键盘高度超过屏幕高度的15%即为显示
            val keyboardVisible = keyboardHeight > screenHeight * 0.15

            if (keyboardVisible) {
                // 键盘弹出，为根布局增加底部内边距，高度即为键盘高度
                rootLayout.setPadding(0, 0, 0, keyboardHeight)
                // 可选：如果你的MNPasswordEditText在特定位置，可以在这里触发滚动
                // scrollToTargetView()
            } else {
                // 键盘隐藏，恢复底部内边距
                rootLayout.setPadding(0, 0, 0, defaultBottomPadding)
            }
        }
    }

    private fun initData() {
//        binding.mTvNext.setOnClickListener {
//            if (context is AppCompatActivity && binding.mEtCode.text.trim().isNotEmpty()) {
//                val activity = context
//                // 获取该 Activity 作用域的 ViewModel
//                viewModel = ViewModelProvider(activity).get<SafeCenterViewModel>(SafeCenterViewModel::class.java)
//                // 现在可以使用 viewModel 进行数据操作
//                scope.launch {
//                    viewModel?.goValidateTwoFactor("")?.observe(activity) {
//                        dismiss()
//                        context.startActivity(Intent(context, GoogleAuthStep1Activity::class.java))
//                    }
//                }
//            }
//        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onAppEventReceiver(event: PersonInfoBean?) {
        event?.let {
            mTvMoney.text = it.amount
        }

    }


    override fun onDismiss() {
        EventBus.getDefault().unregister(this)
        keyboardHelper.disable()
        scope.cancel()
        super.onDismiss()
    }
}