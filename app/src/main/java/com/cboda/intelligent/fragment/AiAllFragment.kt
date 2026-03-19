package com.cboda.intelligent.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isGone
import com.cboda.baselibrary.utils.ChooseFileBroadUtil
import com.cboda.baselibrary.utils.LauncherFragmentBroadHelper
import com.cboda.baselibrary.utils.choosePicture
import com.cboda.baselibrary.utils.chooseVideo
import com.cboda.baselibrary.utils.click
import com.cboda.baselibrary.utils.getNotScrollVerticalGridLayoutManager
import com.cboda.baselibrary.utils.seStrokeColor
import com.cboda.baselibrary.utils.setTxt
import com.blankj.utilcode.util.FileUtils
import com.cboda.select.picture.entity.LocalMedia
import com.cboda.intelligent.R
import com.cboda.intelligent.activity.TestActivity
import com.cboda.intelligent.adapter.AIImgBroadAdapter
import com.cboda.intelligent.base.BaseBroadFragment
import com.cboda.intelligent.databinding.AiAllFragmentBinding
import com.cboda.intelligent.dialog.AiPostFileBroadDialog
import com.cboda.intelligent.dialog.pop.UploadFileTipBroadPop
import com.cboda.intelligent.utils.showToastMain
import com.cboda.intelligent.utils.JumpBroadUtils.Companion.jumpAiCheckResultActivity
import com.cboda.intelligent.viewmodel.CheckAiViewModel

class AiAllFragment :
    BaseBroadFragment<AiAllFragmentBinding, CheckAiViewModel>() {

    companion object {
        const val DOCUMENT_TYPE = "document"
        const val IMAGE_TYPE = "image"
        const val VOICE_TYPE = "voice"
        const val VIDEO_TYPE = "video"

        const val PROJECT_RISK_TYPE = "projectRisk"
        const val CONTRACT_RISK_TYPE = "contractRisk"

        fun newInstance(param: String): AiAllFragment {
            val fragment = AiAllFragment()
            val args = Bundle()
            args.putString("resourceType", param)
            fragment.arguments = args
            return fragment
        }
    }

    private var resourceType = ""
    private var riskType = ""


    private val adapter = AIImgBroadAdapter()
    private val selectData = arrayListOf<String>()
    private val chooseImgData = arrayListOf<Pair<String, LocalMedia?>>()

    //    private var aiPostFileDialog: AiPostFileBroadDialog? = null
    private val childActivityLauncher = LauncherFragmentBroadHelper(this)
    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        savedInstanceState: Bundle?
    ) = AiAllFragmentBinding.inflate(inflater, parent, false)


    fun checkDataAndClear(callBack: () -> Unit) {
        if (selectData.isNotEmpty()) {
            val uploadFileTipPop = UploadFileTipBroadPop(requireContext())
            uploadFileTipPop.sureInvoke = {
                clearChooseData()
                callBack.invoke()
            }
            uploadFileTipPop.showPopupWindow()
        } else {
            callBack.invoke()
        }
    }

    private fun clearChooseData() {
        chooseImgData.clear()
        selectData.clear()
        checkData()
    }

    private fun checkData() {
        when (resourceType) {
            IMAGE_TYPE -> {
                binding.group5.isGone = chooseImgData.isNotEmpty()
                binding.group3.isGone = chooseImgData.isEmpty()
                setBtnAlpha(if (selectData.isNotEmpty()) 1f else 0.4f)
            }

            DOCUMENT_TYPE -> {
                setBtnAlpha(if (selectData.isNotEmpty() && riskType != "") 1f else 0.4f)
                binding.aiUploadImg.setImageResource(if (selectData.isNotEmpty()) R.mipmap.icon_ai_upload_broad_success else R.mipmap.icon_ai_upload_broad_document)
                binding.aiUploadTitle.setTxt(if (selectData.isNotEmpty()) R.string.upload_completed else R.string.click_upload_document)
            }

            VIDEO_TYPE -> {
                setBtnAlpha(if (selectData.isNotEmpty()) 1f else 0.4f)
                binding.aiUploadImg.setImageResource(if (selectData.isNotEmpty()) R.mipmap.icon_ai_upload_broad_success else R.mipmap.icon_ai_upload_broad_document)
                binding.aiUploadTitle.setTxt(if (selectData.isNotEmpty()) R.string.upload_completed else R.string.click_upload_video)
            }

            VOICE_TYPE -> {
                setBtnAlpha(if (selectData.isNotEmpty()) 1f else 0.4f)
                binding.aiUploadImg.setImageResource(if (selectData.isNotEmpty()) R.mipmap.icon_ai_upload_broad_success else R.mipmap.icon_ai_upload_broad_document)
                binding.aiUploadTitle.setTxt(if (selectData.isNotEmpty()) R.string.upload_completed else R.string.click_upload_audio)
            }
        }
    }

    private fun chooseDocument() {
        childActivityLauncher.lunchResult(ChooseFileBroadUtil.getChooseFileIntent()) { activityResult, _ ->
            ChooseFileBroadUtil.checkFileSizeAndIsLive(
                activityResult?.data?.data,
                context = requireContext()
            ) {
//                eLogD("AiAllFragmentClass", "filePath:${it.absolutePath}")
                selectData.clear()
                selectData.add(it.absolutePath)
                checkData()
            }
        }
    }

    private fun chooseVideo() {
        chooseVideo(
            recordVideoMaxSecond = 150,
            recordVideoMinSecond = 1,
            filterVideoMaxSecond = 151,
            maxFileSize = 100 * 1024 * 1024L
        ) { result ->
            if (result?.isNotEmpty() == true) {
                val videoPath = result[0].first
//                eLogD("AiAllFragmentClass", "videoPath:$videoPath")
                selectData.clear()
                selectData.add(videoPath)
            }
            checkData()
        }
    }

    private fun chooseAudio() {
        childActivityLauncher.lunchResult(ChooseFileBroadUtil.getChooseAudioIntent()) { activityResult, _ ->
            ChooseFileBroadUtil.checkFileSizeAndIsLive(
                activityResult?.data?.data,
                context = requireContext()
            ) {
//                eLogD("AiAllFragmentClass", "filePath:${it.absolutePath}")
                selectData.clear()
                selectData.add(it.absolutePath)
                checkData()
            }
        }
    }

    private fun chooseImg() {
        val imgMaxNum = 5
        choosePicture(imgMaxNum = imgMaxNum, selectList = chooseImgData) { list ->
            list?.let {
                chooseImgData.clear()
                selectData.clear()
                chooseImgData.addAll(list)
                selectData.addAll(list.filter { it.first.isNotEmpty() && FileUtils.isFileExists(it.first) }
                    .map { it.first })
                binding.aiUploadTitle5.text = String.format(getString(R.string.picture_has_been_selected),list.size.toString())
                if (chooseImgData.isNotEmpty() && chooseImgData.size <= imgMaxNum - 1) {
                    chooseImgData.add(Pair("", null))
                }
                adapter.setData(chooseImgData)
                checkData()
            }
        }
    }

//    fun updateResourceType(mResourceType: String) {
//        resourceType = mResourceType
//    }

    override fun initView() {
        super.initView()
        resourceType =arguments?.getString("resourceType")?:DOCUMENT_TYPE
        setBtnAlpha(0.4f)
        binding.aiUploadImg.setImageResource(R.mipmap.icon_ai_upload_broad_document)
        binding.aiImgListView.layoutManager =
            getNotScrollVerticalGridLayoutManager(requireContext(), 3)
        binding.aiImgListView.adapter = adapter
        binding.group5.isGone = false
        binding.group3.isGone = true
        when (resourceType) {
            DOCUMENT_TYPE -> {
                checkRiskType()
                binding.group1.isGone = false
                binding.group2.isGone = true
                binding.aiUploadTitle.setTxt(R.string.click_upload_document)
                binding.aiUploadTitle2.setTxt(R.string.supports_pdf_word_excel)
                binding.aiUploadTitle3.setTxt(R.string.select_monitoring_mode)
            }

            IMAGE_TYPE -> {
                binding.group1.isGone = true
                binding.group2.isGone = false
                binding.aiUploadTitle.setTxt(R.string.click_upload_img)
                binding.aiUploadTitle2.setTxt(R.string.supports_jpg_png)
                binding.aiUploadTitle3.setTxt(R.string.now_monitoring_mode)
            }

            VOICE_TYPE -> {
                binding.group1.isGone = true
                binding.group2.isGone = false
                binding.aiUploadTitle.setTxt(R.string.click_upload_audio)
                binding.aiUploadTitle2.setTxt(R.string.supports_mp3_wav)
                binding.aiUploadTitle3.setTxt(R.string.now_monitoring_mode)
            }

            VIDEO_TYPE -> {
                binding.group1.isGone = true
                binding.group2.isGone = false
                binding.aiUploadTitle.setTxt(R.string.click_upload_video)
                binding.aiUploadTitle2.setTxt(R.string.supports_mp4_mov_avi)
                binding.aiUploadTitle3.setTxt(R.string.now_monitoring_mode)
            }
        }
    }

    fun setBtnAlpha(alpha: Float) {
        binding.updateBtn.alpha = alpha
        binding.updateBtnIv.alpha = alpha
        binding.updateBtnTv.alpha = alpha
    }

    private fun checkRiskType() {
        binding.view8.seStrokeColor(if (riskType == PROJECT_RISK_TYPE) com.cboda.baselibrary.R.color.color_2563EB else com.cboda.baselibrary.R.color.white)
        binding.view5.seStrokeColor(if (riskType == CONTRACT_RISK_TYPE) com.cboda.baselibrary.R.color.color_2563EB else com.cboda.baselibrary.R.color.white)
        binding.aiProjectIv5.setImageResource(if (riskType == PROJECT_RISK_TYPE) R.mipmap.icon_ai_select_broad_project else R.mipmap.icon_ai_no_select_broad_project)
        binding.aiContractIv.setImageResource(if (riskType == CONTRACT_RISK_TYPE) R.mipmap.icon_ai_select_broad_contract else R.mipmap.icon_ai_no_select_broad_contract)
    }


    override fun initListener() {
        super.initListener()
        binding.aiClearImgTv.click {
            clearChooseData()
        }
        binding.aiClearImgIv.click {
            clearChooseData()
        }
        binding.shapeView.click {
            when (resourceType) {
                DOCUMENT_TYPE -> chooseDocument()
                IMAGE_TYPE -> chooseImg()
                VOICE_TYPE -> chooseAudio()
                VIDEO_TYPE -> chooseVideo()
            }
//            startActivity(Intent(activity, TestActivity::class.java))
        }
        adapter.setOnItemClickListener { _, _ ->
            chooseImg()
        }
        binding.view8.click {
            riskType = PROJECT_RISK_TYPE
            checkRiskType()
            checkData()
        }
        binding.view5.click {
            riskType = CONTRACT_RISK_TYPE
            checkRiskType()
            checkData()
        }

        binding.updateBtn.click {
            if (riskType == "" && resourceType == DOCUMENT_TYPE) {
                showToastMain(R.string.please_select_monitoring_mode)
                return@click
            }
            if (selectData.isEmpty()) {
                showToastMain(R.string.please_upload_files_tested_first)
                return@click
            }
            showAiPostFileDialog()
            viewModel.postFiles(riskType, resourceType, selectData).observe(this) { pair ->
                if (null != aiPostFileDialog) {
                    if (pair.first == null) {
                        showToastMain(pair.second ?: getString(R.string.ai_detection_failed_retry_later))
                    } else {
                        clearChooseData()
                        jumpAiCheckResultActivity(requireContext(), pair.first)
                    }
                }
                dismissAiPostFileDialog()
            }
        }
    }


    private var aiPostFileDialog: AiPostFileBroadDialog? = null

    private fun dismissAiPostFileDialog() {
        aiPostFileDialog?.dismiss()
        aiPostFileDialog = null
    }

    private fun showAiPostFileDialog() {
        aiPostFileDialog = AiPostFileBroadDialog(requireContext(), riskType, resourceType)
        aiPostFileDialog?.onBackPressListener = {
            val uploadFileTipPop = UploadFileTipBroadPop(requireContext())
            uploadFileTipPop.setContentTxt(R.string.are_you_abandoning_your_current_test)
            uploadFileTipPop.setCancelBtn(R.string.mine_sure)
            uploadFileTipPop.setSureBtn(R.string.quxiao)
            uploadFileTipPop.cancelInvoke = {
                clearChooseData()
                dismissAiPostFileDialog()
            }
            uploadFileTipPop.showPopupWindow()

        }
        aiPostFileDialog?.dismissInvoke = {
            aiPostFileDialog = null
        }
        aiPostFileDialog?.showPopupWindow()
    }
}