package com.cboda.baselibrary.popup

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.widget.LinearLayout
import android.widget.TextView
import com.cboda.baselibrary.R
import razerdp.basepopup.BasePopupWindow
import razerdp.util.animation.AnimationHelper
import razerdp.util.animation.Direction
import razerdp.util.animation.TranslationConfig

/**
 * 权限说明弹窗
 * 用于在请求权限前向用户说明权限用途
 * 
 * @author CommonLibrary
 */
class PermissionExplainBroadPopup(
    context: Context,
    private val title: String = "权限说明",
    private val permissionExplains: List<String>,
    private val onConfirm: Runnable? = null,
    private val onCancel: Runnable? = null
) : BasePopupWindow(context) {

    private lateinit var tvTitle: TextView
    private lateinit var llPermissionList: LinearLayout
    private lateinit var btnCancel: TextView
    private lateinit var btnConfirm: TextView

    init {
        setContentView(R.layout.popup_permission_explain)
        setPopupGravity(Gravity.BOTTOM)
        setOutSideDismiss(false)
        setBackPressEnable(false)
    }

    override fun onViewCreated(contentView: View) {
        super.onViewCreated(contentView)
        initViews(contentView)
        setupData()
        setupClickListeners()
    }

    private fun initViews(contentView: View) {
        tvTitle = contentView.findViewById(R.id.tv_title)
        llPermissionList = contentView.findViewById(R.id.ll_permission_list)
        btnCancel = contentView.findViewById(R.id.btn_cancel)
        btnConfirm = contentView.findViewById(R.id.btn_confirm)
    }

    private fun setupData() {
        // 设置标题
        tvTitle.text = title
        
        // 清空权限列表容器
        llPermissionList.removeAllViews()
        
        // 添加权限说明项
        permissionExplains.forEach { explain ->
            addPermissionExplainItem(explain)
        }
    }

    private fun addPermissionExplainItem(explainText: String) {
        val itemView = LayoutInflater.from(context).inflate(
            R.layout.item_permission_explain, 
            llPermissionList, 
            false
        )
        
        val tvPermissionText = itemView.findViewById<TextView>(R.id.tv_permission_text)
        tvPermissionText.text = explainText
        
        llPermissionList.addView(itemView)
    }

    private fun setupClickListeners() {
        btnCancel.setOnClickListener {
            onCancel?.run()
            dismiss()
        }
        
        btnConfirm.setOnClickListener {
            onConfirm?.run()
            dismiss()
        }
    }

    override fun onCreateShowAnimation(): Animation? {
        return AnimationHelper.asAnimation()
            .withTranslation(TranslationConfig.FROM_BOTTOM)
            .toShow()
    }

    override fun onCreateDismissAnimation(): Animation? {
        return AnimationHelper.asAnimation()
            .withTranslation(TranslationConfig.TO_BOTTOM)
            .toDismiss()
    }

    /**
     * 构建器模式，方便创建弹窗
     */
    class BroadBuilder(private val context: Context) {
        private var title: String = "权限说明"
        private val permissionExplains = mutableListOf<String>()
        private var onConfirm: Runnable? = null
        private var onCancel: Runnable? = null

        fun setTitle(title: String): BroadBuilder {
            this.title = title
            return this
        }

        fun addPermissionExplain(explain: String): BroadBuilder {
            this.permissionExplains.add(explain)
            return this
        }

        fun setPermissionExplains(explains: List<String>): BroadBuilder {
            this.permissionExplains.clear()
            this.permissionExplains.addAll(explains)
            return this
        }

        fun setOnConfirm(onConfirm: Runnable): BroadBuilder {
            this.onConfirm = onConfirm
            return this
        }
        
        fun setOnCancel(onCancel: Runnable): BroadBuilder {
            this.onCancel = onCancel
            return this
        }

        fun build(): PermissionExplainBroadPopup {
            return PermissionExplainBroadPopup(
                context = context,
                title = title,
                permissionExplains = permissionExplains,
                onConfirm = onConfirm,
                onCancel = onCancel
            )
        }

        fun show(): PermissionExplainBroadPopup {
            val popup = build()
            popup.showPopupWindow()
            return popup
        }
    }
}