package com.cboda.chatui.quickmeet.plugin

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.annotation.LayoutRes
import androidx.lifecycle.LifecycleOwner
import com.cboda.chatui.quickmeet.lifecycle.LifecycleImpl

abstract class PluginLayout(lifecycleOwner: LifecycleOwner) : LifecycleImpl(lifecycleOwner) {

    protected var mContext: Context? = null

    protected var mView: View? = null

    protected var mIsLayout = false

    protected var mContainer: ViewGroup? = null

    fun setupPanel(activity: Activity) {
        val container = activity.findViewById<ViewGroup>(Window.ID_ANDROID_CONTENT)
        addView(activity, container)
    }

    fun setupPanel(context: Context, container: ViewGroup) {
        addView(context, container)
    }

    fun  getLayoutView() =mView

    private fun addView(context: Context, container: ViewGroup) {
        if (mIsLayout) {
            return
        }
        mContext = context
        mContainer = container
        removeView()
        mView = LayoutInflater.from(mContext).inflate(getContentLayout(), mContainer, false)
        mContainer?.addView(mView)
        initView()
        mIsLayout = true
    }

    @LayoutRes
    abstract fun getContentLayout(): Int

    abstract fun initView()

    protected fun removeView() {
        try {
            mView?.let {
                mContainer?.removeView(it)
                mView = null
            }
        } catch (ignore: Exception){

        }
    }

    protected open fun <T : View?> findViewById(id: Int): T {
        return mView!!.findViewById(id)
    }

    fun isLayout(): Boolean {
        return mIsLayout
    }

    open fun setEnabled(enabled: Boolean){}
}