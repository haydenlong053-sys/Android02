package com.cboda.baselibrary.utils

import android.app.Activity.RESULT_OK
import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import com.blankj.utilcode.util.UriUtils

class LauncherActivityBroadHelper(fragment: FragmentActivity) {
    private var resultCallback: ((ActivityResult?, String?) -> Unit)? = null
    private var canReturnFilePath = false
    private val launcher = fragment.registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK && null != resultCallback) {
            resultCallback?.invoke(
                result,
                if (canReturnFilePath && null != result.data?.data) UriUtils.uri2File(result.data?.data).absolutePath else null
            )
        }
        resultCallback = null
    }

    fun lunchResult(
        intent: Intent,
        returnFilePath: Boolean = true,
        block: (result: ActivityResult?, filePath: String?) -> Unit = { _, _ -> }
    ) {
        canReturnFilePath = returnFilePath
        resultCallback = block
        launcher.launch(intent)
    }

}