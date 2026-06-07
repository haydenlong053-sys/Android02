package com.cboda.intelligent.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.cboda.intelligent.R
import com.cboda.intelligent.base.BaseSimple2BroadActivity
import com.cboda.intelligent.databinding.SquareReportSubmitActivityBinding
import com.cboda.intelligent.holder.MutliPicSelectBroadHolder
import com.cboda.intelligent.utils.AppBroadUtils
import com.cboda.intelligent.utils.ToastBroadUtil
import com.cboda.intelligent.viewmodel.CleverBodyViewBroadModel
import com.cboda.intelligent.viewmodel.UploadFileViewBroadModel
import kotlinx.coroutines.launch

class SquareReportSubmitActivity : BaseSimple2BroadActivity() {

    private var mBinding: SquareReportSubmitActivityBinding? = null
    private val viewModel: CleverBodyViewBroadModel by viewModels()
    private var uploadViewModel: UploadFileViewBroadModel? = null
    private var picHolder: MutliPicSelectBroadHolder? = null

    private var targetType: String = SquareReportTypeActivity.TARGET_TYPE_SQUARE
    private var targetId: String = ""
    private var reportConfigId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = binding as? SquareReportSubmitActivityBinding
        setRootBgColor(com.cboda.baselibrary.R.color.color_F7F8FA)
        getToolbarTitle()?.setText(R.string.square_report_title)
        targetType = intent.getStringExtra(EXTRA_TARGET_TYPE).orEmpty()
            .ifBlank { SquareReportTypeActivity.TARGET_TYPE_SQUARE }
        targetId = intent.getStringExtra(EXTRA_TARGET_ID).orEmpty()
        reportConfigId = intent.getStringExtra(EXTRA_REPORT_CONFIG_ID).orEmpty()
        initView()
    }

    override fun getViewBindingClass() = SquareReportSubmitActivityBinding::class.java
    override fun useViewBinding() = true
    override fun showOrHideTitleLayout() = true
    override fun useStatusBarDarkFont() = true
    override fun showBgTopTitleLayout() = true


    override fun showTransparentTopTitleLayout() = false
    private fun initView() {
        val imageList = mBinding?.squareReportImageList ?: return
        picHolder = MutliPicSelectBroadHolder(this, imageList, maxPic = MAX_IMAGE_COUNT)
        mBinding?.squareReportSubmitBtn?.setOnClickListener {
            submitReport()
        }
    }

    private fun submitReport() {
        if (targetId.isBlank() || reportConfigId.isBlank()) return
        val reason = mBinding?.squareReportReasonInput?.text?.toString()?.trim().orEmpty()
        val localFiles = picHolder?.lubanFileList.orEmpty()
        showLoading()
        lifecycleScope.launch {
            val imageUrls = if (localFiles.isEmpty()) {
                emptyList()
            } else {
                if (uploadViewModel == null) {
                    uploadViewModel = ViewModelProvider(this@SquareReportSubmitActivity)[
                        UploadFileViewBroadModel::class.java,
                    ]
                }
                val uploadResult = uploadViewModel?.uploadListSyn(localFiles)
                if (uploadResult?.urls.isNullOrEmpty()) {
                    dismissLoading()
                    AppBroadUtils.showToast(
                        this@SquareReportSubmitActivity,
                        uploadResult?.erroToaastInfo.orEmpty(),
                    )
                    return@launch
                }
                uploadResult?.urls.orEmpty()
            }
            viewModel.submitReport(
                targetType = targetType,
                targetId = targetId,
                reportConfigId = reportConfigId,
                reason = reason,
                imageUrls = imageUrls,
            ).observe(this@SquareReportSubmitActivity) { success ->
                dismissLoading()
                if (success != true) return@observe
                ToastBroadUtil.show(getString(R.string.square_report_submit_success))
                finish()
            }
        }
    }

    companion object {
        private const val EXTRA_TARGET_TYPE = "targetType"
        private const val EXTRA_TARGET_ID = "targetId"
        private const val EXTRA_REPORT_CONFIG_ID = "reportConfigId"
        private const val MAX_IMAGE_COUNT = 9

        fun start(
            context: Context,
            targetType: String,
            targetId: String,
            reportConfigId: String,
        ) {
            context.startActivity(
                Intent(context, SquareReportSubmitActivity::class.java)
                    .putExtra(EXTRA_TARGET_TYPE, targetType)
                    .putExtra(EXTRA_TARGET_ID, targetId)
                    .putExtra(EXTRA_REPORT_CONFIG_ID, reportConfigId),
            )
        }
    }
}
