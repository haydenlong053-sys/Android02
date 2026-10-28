package com.freddy.chat.folder

import android.content.Context
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cboda.baselibrary.toast.ToastBroadUtils
import com.cboda.intelligent.R
import com.cboda.intelligent.base.BaseSimple2BroadActivity
import com.freddy.chat.im.MessageProcessor
import com.freddy.im.db.ImLocalStore
import com.freddy.im.http.model.im.ConvFolderItem
import com.freddy.im.message.ConvFolderSyncHelper

/**
 * 新建 / 重命名会话分组（名称 + 本地图标）。
 */
class ConvFolderEditActivity : BaseSimple2BroadActivity() {

    companion object {
        private const val EXTRA_FOLDER_ID = "folder_id"
        private const val EXTRA_RENAME = "rename"

        fun startCreate(context: Context) {
            context.startActivity(
                Intent(context, ConvFolderEditActivity::class.java)
                    .putExtra(EXTRA_RENAME, false),
            )
        }

        fun startRename(context: Context, folderId: String) {
            context.startActivity(
                Intent(context, ConvFolderEditActivity::class.java)
                    .putExtra(EXTRA_RENAME, true)
                    .putExtra(EXTRA_FOLDER_ID, folderId),
            )
        }
    }

    private var renameMode = false
    private var folderId: String = ""
    private var selectedIcon = 1
    private lateinit var etName: EditText
    private lateinit var tvCounter: TextView
    private lateinit var iconAdapter: IconAdapter

    override fun showOrHideTitleLayout(): Boolean = false
    override fun showTransparentTopTitleLayout(): Boolean {
        return true
    }


    override fun getLayoutId(): Int = R.layout.activity_conv_folder_edit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setRootBgColor(R.color.color_msg_page_bg)
        renameMode = intent.getBooleanExtra(EXTRA_RENAME, false)
        folderId = intent.getStringExtra(EXTRA_FOLDER_ID).orEmpty()

        findViewById<View>(R.id.iv_back).setOnClickListener { finish() }
        findViewById<View>(R.id.btn_cancel).setOnClickListener { finish() }
        val tvTitle = findViewById<TextView>(R.id.tv_title)
        val btnConfirm = findViewById<TextView>(R.id.btn_confirm)
        if (renameMode) {
            tvTitle.setText(R.string.conv_folder_rename_title)
            btnConfirm.setText(R.string.conv_folder_confirm_save)
        } else {
            tvTitle.setText(R.string.conv_folder_create_title)
            btnConfirm.setText(R.string.conv_folder_confirm_create)
        }

        etName = findViewById(R.id.et_name)
        tvCounter = findViewById(R.id.tv_counter)
        updateCounter(0)
        etName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                updateCounter(s?.length ?: 0)
            }
        })

        val rv = findViewById<RecyclerView>(R.id.rv_icons)
        iconAdapter = IconAdapter()
        rv.layoutManager = GridLayoutManager(this, 4)
        rv.adapter = iconAdapter

        if (renameMode && folderId.isNotEmpty()) {
            ImLocalStore.loadConvFolders { _, list ->
                val folder = list.firstOrNull { it.id == folderId } ?: return@loadConvFolders
                etName.setText(folder.name.orEmpty())
                selectedIcon = folder.iconType.coerceIn(0, ConvFolderIcons.COUNT_SELECTABLE - 1)
                iconAdapter.notifyDataSetChanged()
            }
        }

        btnConfirm.setOnClickListener { submit() }
    }

    private fun updateCounter(len: Int) {
        tvCounter.text = getString(R.string.conv_folder_name_counter, len)
    }

    private fun submit() {
        val name = etName.text?.toString()?.trim().orEmpty()
        if (name.isEmpty() || name.length > ConvFolderSyncHelper.MAX_NAME_LEN) {
            ToastBroadUtils.show(getString(R.string.conv_folder_name_hint))
            return
        }
        if (renameMode) {
            ConvFolderSyncHelper.saveWithRetry(
                { current ->
                    current.map { item ->
                        if (item.id == folderId) {
                            item.copy().also {
                                it.name = name
                                it.iconType = selectedIcon
                            }
                        } else item
                    }
                },
                saveCallback(null),
            )
        } else {
            ImLocalStore.loadConvFolders { _, current ->
                if (current.size >= ConvFolderSyncHelper.MAX_FOLDERS) {
                    ToastBroadUtils.show("最多创建 20 个分组")
                    return@loadConvFolders
                }
                val newId = ConvFolderSyncHelper.newFolderId()
                ConvFolderSyncHelper.saveWithRetry(
                    { list ->
                        val created = ConvFolderItem().apply {
                            id = newId
                            this.name = name
                            iconType = selectedIcon
                            setSessionIds(ArrayList())
                        }
                        list + created
                    },
                    saveCallback(newId),
                )
            }
        }
    }

    private fun saveCallback(newFolderId: String?): ConvFolderSyncHelper.OnSaveResult {
        return object : ConvFolderSyncHelper.OnSaveResult {
            override fun onSuccess(version: Long, folders: List<ConvFolderItem>) {
                MessageProcessor.getInstance().notifyConvFoldersUpdated()
                if (!renameMode && !newFolderId.isNullOrEmpty()) {
                    ConvFolderPickSessionsActivity.startAdd(this@ConvFolderEditActivity, newFolderId)
                }
                finish()
            }

            override fun onFail(errorCode: Int, errorMsg: String?) {
                ToastBroadUtils.show(
                    errorMsg?.takeIf { it.isNotBlank() }
                        ?: getString(R.string.conv_folder_save_failed),
                )
            }
        }
    }

    private inner class IconAdapter : RecyclerView.Adapter<IconVH>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IconVH {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_conv_folder_icon_pick, parent, false)
            return IconVH(v)
        }

        override fun getItemCount(): Int = ConvFolderIcons.COUNT_SELECTABLE + 1

        override fun onBindViewHolder(holder: IconVH, position: Int) {
            holder.bind(position)
        }
    }

    private inner class IconVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val flIcon: View = itemView.findViewById(R.id.fl_icon)
        private val ivIcon: ImageView = itemView.findViewById(R.id.iv_icon)

        fun bind(type: Int) {
            ivIcon.setImageResource(ConvFolderIcons.iconRes(type))
            val selected = type == selectedIcon && type != ConvFolderIcons.TYPE_MORE
            val gd = GradientDrawable().apply {
                cornerRadius = itemView.resources.displayMetrics.density * 12f
                setColor(ConvFolderIcons.bgColor(type))
                if (selected) {
                    setStroke(
                        (2 * itemView.resources.displayMetrics.density).toInt(),
                        resources.getColor(R.color.color_brand_primary, theme),
                    )
                }
            }
            flIcon.background = gd
            itemView.alpha = if (type == ConvFolderIcons.TYPE_MORE) 0.5f else 1f
            itemView.setOnClickListener {
                if (type == ConvFolderIcons.TYPE_MORE) return@setOnClickListener
                selectedIcon = type
                iconAdapter.notifyDataSetChanged()
            }
        }
    }
}
