package com.cboda.intelligent.base

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.cboda.baselibrary.widge.LoadingBroadDialog

abstract class BaseLazyBroadFragment : Fragment() {
    /**
     * 是否是第一次进入fragment 第一次进入加载数据
     */
    private var isFirstLoad = true
    var rootView: View? = null

    abstract fun loadData()

    override fun onResume() {
        super.onResume()
        if (isFirstLoad) {
            loadData()
        }
        isFirstLoad = false
    }

    protected abstract fun getLayoutId(): Int

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            rootView = inflater.inflate(getLayoutId(), container, false)
        } else {
            val parent = rootView?.parent as ViewGroup
            parent.removeView(rootView)
        }
        return rootView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dialogHelper = null
    }

    private var dialogHelper: LoadingBroadDialog? = null
    protected fun showLoading() {
        context?.apply {
            if ((this is Activity) && (this.isFinishing || this.isDestroyed)) {
                return
            }
            if (dialogHelper == null) {
                dialogHelper = LoadingBroadDialog(this)
            }
            dialogHelper?.show()
            dialogHelper?.setCanceledOnTouchOutside(true)
            dialogHelper?.setCancelable(true)
        }
    }

    protected fun isDisMissLoading(isDismiss: Boolean) {
        dialogHelper?.setCanceledOnTouchOutside(isDismiss)
        dialogHelper?.setCancelable(isDismiss)
    }

    fun dismissLoading() {
        dialogHelper?.dismiss()
    }
}