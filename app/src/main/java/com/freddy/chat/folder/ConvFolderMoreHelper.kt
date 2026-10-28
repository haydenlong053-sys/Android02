package com.freddy.chat.folder

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.cboda.baselibrary.bean.ComBottomListBean
import com.cboda.baselibrary.toast.ToastBroadUtils
import com.cboda.baselibrary.utils.showCommonBottomListPop
import com.cboda.intelligent.R
import com.freddy.chat.im.MessageProcessor
import com.freddy.im.http.model.im.ConvFolderItem
import com.freddy.im.message.ConvFolderSyncHelper

/**
 * 分组「更多」底部菜单：重命名 / 编辑会话 / 移除会话 / 删除分组。
 */
object ConvFolderMoreHelper {

    fun show(
        context: Context,
        folder: ConvFolderItem,
        onChanged: (() -> Unit)? = null,
    ) {
        val list = listOf(
            ComBottomListBean().apply { itemContent = context.getString(R.string.conv_folder_more_rename) },
            ComBottomListBean().apply { itemContent = context.getString(R.string.conv_folder_more_edit) },
            ComBottomListBean().apply { itemContent = context.getString(R.string.conv_folder_more_remove) },
            ComBottomListBean().apply { itemContent = context.getString(R.string.conv_folder_more_delete) },
        )
        showCommonBottomListPop(list, context) { pos, _ ->
            when (pos) {
                0 -> ConvFolderEditActivity.startRename(context, folder.id.orEmpty())
                1 -> ConvFolderPickSessionsActivity.startAdd(context, folder.id.orEmpty())
                2 -> ConvFolderPickSessionsActivity.startRemove(context, folder.id.orEmpty())
                3 -> confirmDelete(context, folder, onChanged)
            }
        }
    }

    private fun confirmDelete(
        context: Context,
        folder: ConvFolderItem,
        onDone: (() -> Unit)?,
    ) {
        val name = folder.name.orEmpty()
        AlertDialog.Builder(context)
            .setMessage(context.getString(R.string.conv_folder_delete_confirm, name))
            .setNegativeButton(R.string.conv_folder_cancel, null)
            .setPositiveButton(R.string.conv_folder_more_delete) { _, _ ->
                val folderId = folder.id.orEmpty()
                ConvFolderSyncHelper.saveWithRetry(
                    { current -> current.filter { it.id != folderId } },
                    object : ConvFolderSyncHelper.OnSaveResult {
                        override fun onSuccess(version: Long, folders: List<ConvFolderItem>) {
                            MessageProcessor.getInstance().notifyConvFoldersUpdated()
                            onDone?.invoke()
                        }

                        override fun onFail(errorCode: Int, errorMsg: String?) {
                            ToastBroadUtils.show(
                                errorMsg?.takeIf { it.isNotBlank() }
                                    ?: context.getString(R.string.conv_folder_save_failed),
                            )
                        }
                    },
                )
            }
            .show()
    }
}
