package com.cboda.intelligent.dialog.pop

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
import com.cboda.baselibrary.popup.BaseBottomBroadPop
import com.cboda.intelligent.R
import com.cboda.intelligent.bean.ResultBroadBean
import com.cboda.intelligent.configuration.SafeJumpBroadEnum
import com.cboda.intelligent.bean.ActionRefreshBroadEvent
import com.cboda.intelligent.bean.TransferEventBroadBean
import com.cboda.intelligent.utils.SpotsCallBroadBack
import com.cboda.intelligent.utils.SoftKeyBoardBroadUtils
import com.cboda.intelligent.utils.showToastMain
import com.cboda.intelligent.model.PersonInfoBean
import com.cboda.intelligent.viewmodel.PasswordViewBroadModel
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
class AuthPasswordBroadPop(
    val context: Context,
    val jumpSource: Int = SafeJumpBroadEnum.None.sourceType,
    val oldPassWord: String = "",
    val isDeepColor: Boolean = false,
    val account:String?=null,
) :
    BaseBottomBroadPop(context) {
    private val scope = MainScope() // 创建主线程的协程作用域
    private var viewModel: PasswordViewBroadModel? = null
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
            setContentView(R.layout.dialog_pop_password_auth_deep_broad_color)
        } else {
            setContentView(R.layout.dialog_pop_password_broad_auth)

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
        SoftKeyBoardBroadUtils.openSoftKeyBoard(pswEdit)
        keyboardHelper.enable()
        mIvClose.setOnClickListener {
            dismiss()
        }
        pswEdit.setOnTextChangeListener(object : OnTextChangeListener {
            override fun onTextChange(text: String, isComplete: Boolean) {
                if (isComplete && context is AppCompatActivity) {
                    val activity = context
                    if (SafeJumpBroadEnum.PasswordChange.sourceType == jumpSource) {
                        val mPop = AuthPasswordBroadPop(
                            context,
                            SafeJumpBroadEnum.PasswordChangeSure.sourceType,
                            text
                        )
                        mPop.showPopupWindow()
                        dismiss()

                    } else if (SafeJumpBroadEnum.PasswordBind.sourceType == jumpSource) {
                        val mPop =
                            AuthPasswordBroadPop(context, SafeJumpBroadEnum.PasswordBindSure.sourceType, text)
                        mPop.showPopupWindow()
                        dismiss()

                    } else if (SafeJumpBroadEnum.PasswordChangeSure.sourceType == jumpSource || SafeJumpBroadEnum.PasswordBindSure.sourceType == jumpSource) {
                        if (oldPassWord == text) {
                            viewModel =
                                ViewModelProvider(activity).get<PasswordViewBroadModel>(PasswordViewBroadModel::class.java)
                            viewModel?.setPaypwd(
                                context,
                                text,
                                object : SpotsCallBroadBack<ResultBroadBean>(context) {
                                    override fun onSuccess(
                                        response: Response?,
                                        resultBean: ResultBroadBean?
                                    ) {
                                        //重置密码成功
                                        dismiss()
                                        EventBus.getDefault().post(ActionRefreshBroadEvent())

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
                            ViewModelProvider(activity).get<PasswordViewBroadModel>(PasswordViewBroadModel::class.java)
                        // 现在可以使用 viewModel 进行数据操作
                        val type = if (jumpSource == SafeJumpBroadEnum.TransferAuthSure.sourceType) {
                            1
                        }else if (jumpSource == SafeJumpBroadEnum.LoginForgetPassword.sourceType){
                            2
                        } else {
                            0
                        }
                        viewModel?.checkPayPwd(
                            context,
                            text,
                            type,
                            account,
                            object : SpotsCallBroadBack<ResultBroadBean>(context) {
                                override fun onSuccess(
                                    response: Response?,
                                    resultBean: ResultBroadBean?
                                ) {
                                    dismiss()
                                    when (jumpSource) {
                                        //验证逻辑
                                        SafeJumpBroadEnum.GoogleAuth.sourceType -> {
                                            backInvoke.invoke()
                                        }

                                        SafeJumpBroadEnum.PasswordAuth.sourceType -> {
                                            backInvoke.invoke()
                                        }

                                        SafeJumpBroadEnum.QuestionAuth.sourceType -> {
                                            backInvoke.invoke()
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

                                        SafeJumpBroadEnum.TransferAuthSure.sourceType -> {
                                            EventBus.getDefault().post(TransferEventBroadBean().apply {
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
        mLlMoney.isVisible = (jumpSource == SafeJumpBroadEnum.TransferAuthSure.sourceType)
        val string = when (jumpSource) {
            SafeJumpBroadEnum.PasswordAuth.sourceType, SafeJumpBroadEnum.GoogleAuth.sourceType, SafeJumpBroadEnum.QuestionAuth.sourceType, SafeJumpBroadEnum.ChangeMachineAuth.sourceType, SafeJumpBroadEnum.LoginNewMachine.sourceType, SafeJumpBroadEnum.LoginForgetPassword.sourceType -> {
                context.getString(R.string.mine_password_title_3)
            }

            SafeJumpBroadEnum.PasswordChange.sourceType -> {
                context.getString(R.string.mine_password_title_4)
            }

            SafeJumpBroadEnum.PasswordChangeSure.sourceType -> {
                context.getString(R.string.mine_password_title_5)
            }

            SafeJumpBroadEnum.PasswordBind.sourceType -> {
                context.getString(R.string.mine_password_title_1)
            }

            SafeJumpBroadEnum.PasswordBindSure.sourceType -> {
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
//                viewModel = ViewModelProvider(activity).get<SafeCenterViewBroadModel>(SafeCenterViewBroadModel::class.java)
//                // 现在可以使用 viewModel 进行数据操作
//                scope.launch {
//                    viewModel?.goValidateTwoFactor("")?.observe(activity) {
//                        dismiss()
//                        context.startActivity(Intent(context, GoogleAuthStep1BroadActivity::class.java))
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