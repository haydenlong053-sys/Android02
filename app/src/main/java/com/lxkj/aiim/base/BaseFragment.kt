package com.lxkj.aiim.base

import com.cboda.intelligent.databinding.LayoutDataViewBinding
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.blankj.utilcode.util.BarUtils
import java.lang.reflect.ParameterizedType

abstract class BaseFragment<V : ViewBinding, VM : BaseViewModel> : Fragment() {
    lateinit var binding: V
    var mBinding: V? = null
    lateinit var viewModel: VM
    private var isFirst = true
    lateinit var dataViewBinding: LayoutDataViewBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        beforeLayout()
        binding = onCreateViewBinding(inflater, container, savedInstanceState)
        mBinding = binding
        dataViewBinding = LayoutDataViewBinding.inflate(layoutInflater)
        //如果返回的view是binding.root 请注释掉这一行 不然NameLockFragment会崩溃
//        dataViewBinding.flData.addView(binding.root)

        val type = javaClass.genericSuperclass
        val modelClass = if (type is ParameterizedType) {
            type.actualTypeArguments[1] as Class<BaseViewModel>
        } else {
            //如果没有指定泛型参数，则默认使用BaseViewModel
            BaseViewModel::class.java
        }
        viewModel = createViewModel(this, modelClass as Class<VM>)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //当viewPage不使用预加载时，保证isFirst = true
        isFirst = true
        initView()
        initViewModel()
        initData()
        initListener()
    }


    protected open fun initView() {}

    protected open fun initData() {}

    protected open fun initListener() {}

    protected open fun initViewModel() {}

    protected open fun beforeLayout() {}
    open fun <T : ViewModel> createViewModel(fragment: Fragment, cls: Class<T>): T {
        return ViewModelProvider(fragment)[cls]
    }

    abstract fun onCreateViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        savedInstanceState: Bundle?,
    ): V


    open fun addHeight(view: View) {
        BarUtils.addMarginTopEqualStatusBarHeight(view)
    }

    override fun onResume() {
        super.onResume()
        onVisible()
    }

    /**
     * 是否需要懒加载
     */
    private fun onVisible() {
        if (lifecycle.currentState == Lifecycle.State.STARTED && isFirst) {
            lazyLoad()
            isFirst = false
        } else {
            onResumeTwo()
        }
    }

    protected open fun onResumeTwo() {}

    open fun lazyLoad() {

    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
    }
}