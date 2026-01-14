package com.cboda.agora.impl

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.annotation.DrawableRes
import androidx.annotation.GravityInt
import androidx.annotation.IdRes
import com.cboda.agora.ui.ui.chatBottomBar.AUIChatBottomBarIcon
import com.cboda.agora.ui.ui.chatBottomBar.IAUIChatBottomBarBroadView
import com.cboda.agora.ui.ui.chatBottomBar.listener.AUIExpressionClickBroadListener
import com.cboda.agora.ui.ui.chatBottomBar.listener.AUIMenuItemClickBroadListener
import com.cboda.agora.ui.ui.chatBottomBar.listener.AUISoftKeyboardHeightChangeBroadListener
import com.cboda.agora.ui.ui.chatBottomBar.utils.KeyboardBroadUtils
import com.cboda.agora.ui.ui.chatBottomBar.impl.AUIEmojiBroadUtils
import com.cboda.agora.R
import com.cboda.agora.databinding.AuiChatBottomBroadBarNewBinding
import com.cboda.agora.ui.utils.DeviceBroadTools

class MeetingBottomBarBroadView : RelativeLayout , IAUIChatBottomBarBroadView,
    AUIExpressionClickBroadListener,
    AUISoftKeyboardHeightChangeBroadListener {
    private val mViewBinding = AuiChatBottomBroadBarNewBinding.inflate(LayoutInflater.from(context))
    private val inputManager: InputMethodManager
    private val itemModels = ArrayList<MenuItemModel>()
    private var listener: AUIMenuItemClickBroadListener?=null

    //是否显示表情
    private var isShowEmoji = false
    private var softKeyHeight = 0
    private var mMenuMicOn:Int = R.drawable.ic_voice_bottom_mic_broad_on
    private var mMenuMicOff = R.drawable.ic_voice_bottom_mic_broad_off
    private var mFaceIcon:Int = R.drawable.voice_icon_face_broad_light
    private var mFaceIcon2:Int = R.drawable.voice_icon_broad_key

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        addView(mViewBinding.root)
        inputManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager


        initListener()
    }

    private fun initListener(){
        mViewBinding.expressionView.setExpressionListener(this)
        mViewBinding.clMic.setOnClickListener {
            listener?.onChatExtendMenuItemClick(R.id.voice_extend_item_mic,it)
        }
        mViewBinding.clManager.setOnClickListener {
            listener?.onChatExtendMenuItemClick(R.id.voice_extend_item_more,it)
        }
        mViewBinding.inputEditView.setOnEditorActionListener { v, actionId, event ->
            when (actionId and EditorInfo.IME_MASK_ACTION) {
                EditorInfo.IME_ACTION_DONE -> {
                    sendMessage()
                }
                EditorInfo.IME_ACTION_NEXT -> {}
            }
            false
        }

        mViewBinding.inputEditView.onFocusChangeListener =
            OnFocusChangeListener { v, hasFocus ->
                if (!hasFocus) {
                    if (!isShowEmoji) mViewBinding.inputView.visibility = GONE
                }
            }

        mViewBinding.tvInput.setOnClickListener{
            mViewBinding.inputView.visibility = VISIBLE
            mViewBinding.inputEditView.requestFocus()
            showInputMethod(mViewBinding.inputEditView)
            mViewBinding.expressionView.visibility = GONE
            mViewBinding.vKeyboardBg.visibility = INVISIBLE
            mViewBinding.normalLayout.visibility = GONE
            mViewBinding.tvInput.visibility = GONE
//            mViewBinding.inputIcon.isEnabled = false
            mViewBinding.iconEmoji.setImageResource(mFaceIcon)
            isShowEmoji = false
        }

        mViewBinding.iconEmoji.setOnClickListener{
            isShowEmoji = !isShowEmoji
            softShowing(isShowEmoji)
            checkShowExpression(isShowEmoji)
        }

        mViewBinding.inputSend.setOnClickListener{
            Log.e("apex","inputSend")
            sendMessage()
        }
    }

    private fun sendMessage(){
        if (!TextUtils.isEmpty(mViewBinding.inputEditView.text)){
            listener?.onSendMessage(
                mViewBinding.inputEditView.text.toString().trim { it <= ' ' })
        }
        hideKeyboard()
        showNormalLayout()
    }
    override fun setMenuItemClickListener(listener: AUIMenuItemClickBroadListener?) {
        this.listener = listener
    }

    private fun setViewLayoutParams(view: View, width: Int, height: Int) {
        val lp = view.layoutParams
        lp.width = width
        lp.height = height
        view.layoutParams = lp
    }

    private fun softShowing(isShowEmoji: Boolean) {
        if (isShowEmoji) {
            Log.e("apex","softShowing $softKeyHeight")
            setViewLayoutParams(
                mViewBinding.expressionView,
                LayoutParams.MATCH_PARENT,
                softKeyHeight
            )
            setViewLayoutParams(
                mViewBinding.vKeyboardBg,
                LayoutParams.MATCH_PARENT,
                softKeyHeight
            )
        } else {
            setViewLayoutParams(
                mViewBinding.expressionView, LayoutParams.MATCH_PARENT, DeviceBroadTools.dp2px(
                    context, 55f
                )
            )
        }
    }

    override fun setEnableMic(isEnable: Boolean) {
        post {
            val mic: ImageView =
                mViewBinding.ivMic
            if (!isEnable) {
                mic.setImageResource(mMenuMicOn)
            } else {
                mic.setImageResource(mMenuMicOff)
            }
        }
    }

    override fun setShowMic(isShow: Boolean) {
        post {
            val mic =
                mViewBinding.clMic
            if (isShow) {
                mic.visibility = VISIBLE
            } else {
                mic.visibility = GONE
            }
        }
    }

    override fun setShowMoreStatus(isOwner: Boolean?, isShowHandStatus: Boolean) {
         post {
             if ( isOwner == true ){
                 mViewBinding.redPoint.visibility=if (isShowHandStatus) VISIBLE else GONE
             }
             mViewBinding.clManager.visibility=if (isOwner==true) VISIBLE else GONE
         }
//        post {
//            val moreStatus: ImageView =
//                mViewBinding.menuLayout.findViewById<ImageView>(R.id.voice_extend_item_more_status)
//            if (isOwner == true) {
//                if (isShowHandStatus) {
//                    moreStatus.visibility = VISIBLE
//                } else {
//                    moreStatus.visibility = GONE
//                }
//            } else {
//                moreStatus.visibility = GONE
//            }
//        }
    }

    private fun checkShowExpression(isShow: Boolean) {
        isShowEmoji = isShow
        if (isShowEmoji) {
            mViewBinding.iconEmoji.setImageResource(mFaceIcon2)
            mViewBinding.expressionView.visibility = VISIBLE
            hideKeyboard()
        } else {
            mViewBinding.iconEmoji.setImageResource(mFaceIcon)
            mViewBinding.expressionView.visibility = INVISIBLE
            showInputMethod(mViewBinding.inputEditView)
        }
    }

    override fun hideKeyboard() {
        KeyboardBroadUtils.hideKeyboard(this)
    }

    private fun showInputMethod(editText: EditText) {
        KeyboardBroadUtils.showKeyboard(editText)
    }
    private fun hideExpressionView(isShowEx: Boolean) {
        if (isShowEx) {
            mViewBinding.expressionView.visibility = VISIBLE
        } else {
            mViewBinding.expressionView.visibility = GONE
            setViewLayoutParams(
                mViewBinding.vKeyboardBg,
                LayoutParams.MATCH_PARENT,
                DeviceBroadTools.dp2px(context, 55f)
            )
        }
    }

    private fun showNormalLayout(): Boolean {
        if (mViewBinding.tvInput.visibility != VISIBLE) {
            mViewBinding.inputEditView.setText("")
            showInput()
            mViewBinding.normalLayout.visibility = VISIBLE
            mViewBinding.tvInput.visibility = VISIBLE
            hideExpressionView(false)
            return true
        }
        return false
    }


    private fun showInput() {
        mViewBinding.inputView.visibility = GONE
//        mViewBinding.inputIcon.visibility = VISIBLE
//        mViewBinding.inputIcon.isEnabled = true
    }


    fun showMuteLayout(isOwnerManage: Boolean,isMute: Boolean){
          if (!isOwnerManage){
              mViewBinding.tvInput.text=if (isMute) "房主/管理员已开启禁言" else "这里输入你想说的"
              mViewBinding.tvInput.isEnabled=!isMute
              showNormalLayout()
          }

    }


    data class MenuItemModel(
        @DrawableRes val image: Int,
        @IdRes val id: Int,
        @GravityInt var gravity: Int
    )

    override fun onDeleteImageClicked() {
        if (!TextUtils.isEmpty(mViewBinding.inputEditView.text)) {
            val event =
                KeyEvent(0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL)
            mViewBinding.inputEditView.dispatchKeyEvent(event)
        }
    }

    override fun onExpressionClicked(emojiIcon: AUIChatBottomBarIcon?) {
        if (emojiIcon != null) {
            mViewBinding.inputEditView.append(AUIEmojiBroadUtils.getSmiledText(context, emojiIcon.labelString))
        }
    }

    override fun onSoftKeyboardHeightChanged(isKeyboardShowed: Boolean,keyboardHeight:Int?) {
        Log.e("键盘高度","$keyboardHeight isKeyboardShowed: $isKeyboardShowed")
        val lp = mViewBinding.vKeyboardBg.layoutParams
        if (isKeyboardShowed) {
            if (keyboardHeight != null) {
                lp?.height = keyboardHeight
                softKeyHeight = keyboardHeight
//                if (!isShowEmoji){
//                    lp.height = DeviceBroadTools.dp2px(context, 55f)
//                }
            }
        } else {
            if (!isShowEmoji) {
                lp?.height = DeviceBroadTools.dp2px(context, 55f)
                showNormalLayout()
            }
        }
        mViewBinding.vKeyboardBg.layoutParams = lp
    }

    override fun setSoftKeyListener(){
        listener?.setSoftKeyBoardHeightChangedListener(this)
    }

}