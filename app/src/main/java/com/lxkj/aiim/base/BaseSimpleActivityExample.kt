package com.lxkj.aiim.base

import androidx.viewbinding.ViewBinding
import android.view.LayoutInflater

/**
 * BaseSimpleActivity使用示例
 * 
 * 展示了三种使用方式：
 * 1. 传统方式（现有代码无需修改）
 * 2. ViewBinding类型指定模式（推荐）
 * 3. ViewBinding手动创建模式（高级用法）
 */

// ==================== 方式1：传统方式（现有代码无需修改） ====================
/**
 * 现有Activity继承BaseSimpleActivity，完全兼容
 * 无需修改任何现有代码
 */
/*
class ExistingActivity : BaseSimpleActivity() {
    override fun getLayoutId(): Int = R.layout.activity_existing
    
    // 其他代码保持不变...
}
*/

// ==================== 方式2：ViewBinding类型指定模式（推荐） ====================
/**
 * 新Activity使用ViewBinding，通过getViewBindingClass()指定类型
 */
/*
class NewActivityWithViewBinding : BaseSimpleActivity() {
    
    override fun useViewBinding(): Boolean = true
    
    override fun getViewBindingClass(): Class<out ViewBinding> = ActivityNewBinding::class.java
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 使用binding访问视图（需要强制转换）
        val viewBinding = binding as? ActivityNewBinding
        viewBinding?.apply {
            textView.text = "Hello ViewBinding!"
            button.setOnClickListener {
                // 处理点击事件
            }
        }
    }
}
*/

// ==================== 方式3：ViewBinding手动创建模式（高级用法） ====================
/**
 * 手动创建ViewBinding，适用于复杂场景
 */
/*
class AdvancedActivity : BaseSimpleActivity() {
    
    override fun useViewBinding(): Boolean = true
    
    // 重写createViewBinding方法，提供自定义创建逻辑
    override fun createViewBinding(): ViewBinding? {
        return try {
            ActivityAdvancedBinding.inflate(layoutInflater)
        } catch (e: Exception) {
            // 自定义错误处理
            null
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val viewBinding = binding as? ActivityAdvancedBinding
        viewBinding?.apply {
            // 使用ViewBinding...
        }
    }
}
*/

// ==================== 迁移指南 ====================
/**
 * 从传统方式迁移到ViewBinding的步骤：
 * 
 * 1. 确保项目已启用ViewBinding：
 *    android {
 *        buildFeatures {
 *            viewBinding true
 *        }
 *    }
 * 
 * 2. Activity继承保持不变：
 *    class MyActivity : BaseSimpleActivity()
 * 
 * 3. 启用ViewBinding：
 *    override fun useViewBinding(): Boolean = true
 * 
 * 4. 指定ViewBinding类型：
 *    override fun getViewBindingClass(): Class<out ViewBinding> = ActivityMyBinding::class.java
 * 
 * 5. 移除getLayoutId()方法
 * 
 * 6. 使用binding访问视图：
 *    从：findViewById<TextView>(R.id.textView)
 *    到：(binding as? ActivityMyBinding)?.textView
 */