package com.cboda.baselibrary.utils


import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.text.TextUtils
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.cboda.baselibrary.R
import com.cboda.baselibrary.bean.BroadConstant
import com.blankj.utilcode.util.FileUtils
import com.cboda.select.picture.basic.PictureSelector
import com.cboda.select.picture.config.SelectMimeType.ofAll
import com.cboda.select.picture.config.SelectMimeType.ofImage
import com.cboda.select.picture.config.SelectMimeType.ofVideo
import com.cboda.select.picture.config.SelectModeConfig.MULTIPLE
import com.cboda.select.picture.config.SelectModeConfig.SINGLE
import com.cboda.select.picture.engine.CompressFileEngine
import com.cboda.select.picture.engine.CropFileEngine
import com.cboda.select.picture.engine.UriToFileTransformEngine
import com.cboda.select.picture.entity.LocalMedia
import com.cboda.select.picture.interfaces.OnKeyValueResultCallbackListener
import com.cboda.select.picture.interfaces.OnResultCallbackListener
import com.cboda.select.picture.language.LanguageConfig
import com.cboda.select.picture.permissions.PermissionConfig.READ_MEDIA_AUDIO
import com.cboda.select.picture.permissions.PermissionConfig.READ_MEDIA_IMAGES
import com.cboda.select.picture.permissions.PermissionConfig.READ_MEDIA_VIDEO
import com.cboda.select.picture.utils.SandboxTransformUtils
import com.cboda.select.picture.utils.SdkVersionUtils
import com.yalantis.ucrop.UCrop
import com.yalantis.ucrop.UCropImageEngine
import top.zibin.luban.Luban
import top.zibin.luban.OnNewCompressListener
import java.io.File
import java.io.Serializable
import java.util.LinkedList


fun chooseMedia(
    canCamera: Boolean = true,
    canChooseGif: Boolean = false,
    chooseType: Int = BroadConstant.TYPE_PIC,
    imgMaxNum: Int = 1,
    imgMinNum: Int = 1,
    videoMaxNum: Int = 1,
    videoMinNum: Int = 1,
    recordVideoMaxSecond: Int = 30,
    recordVideoMinSecond: Int = 3,
    filterVideoMaxSecond: Int = 31,
    maxFileSize: Long? = null,
    withSelectVideoImage: Boolean = false,
    needCrop: Boolean = false,
    cropBean: CropBroadBean = CropBroadBean(),
    selectList: LinkedList<LocalMedia?>? = null,
    block: (result: LinkedList<LocalMedia>?) -> Unit
) {
    requestPermission(needPermissions) {
        val chooseMode = when (chooseType) {
            BroadConstant.TYPE_PIC -> ofImage()
            BroadConstant.TYPE_VIDEO -> ofVideo()
            BroadConstant.TYPE_ALL_INT -> ofAll()//只有视频和图片
            else -> ofImage()
        }
        val pictureSelector = PictureSelector.create(getAppContext(null)).openGallery(chooseMode)
        pictureSelector.setImageEngine(GlideBroadBroadEngine.createGlideEngine())
            .setCompressEngine(ImageCompressFileBroadEngine())
            .setSandboxFileEngine(MeSandboxFileBroadEngine())
            .setLanguage(LanguageConfig.SYSTEM_LANGUAGE)
            .isDisplayTimeAxis(false)//显示资源时间轴
            .isDisplayCamera(canCamera)
            .isWithSelectVideoImage(withSelectVideoImage)//是否支持视频图片同时选择
        if (null != selectList) {
            pictureSelector.setSelectedData(selectList)
        }
        if (null != maxFileSize) {
            pictureSelector.setSelectMaxFileSize(maxFileSize)
        }
        if (chooseType == BroadConstant.TYPE_PIC || chooseType == BroadConstant.TYPE_ALL_INT) {
            pictureSelector.isGif(canChooseGif)//是否显示gif
                .isWebp(true)
                .setMinSelectNum(imgMinNum)
                .setMaxSelectNum(imgMaxNum)
            if (chooseType == BroadConstant.TYPE_PIC) {
                pictureSelector.setSelectionMode(if (imgMaxNum > 1) MULTIPLE else SINGLE)
            }
        }
        if (chooseType == BroadConstant.TYPE_VIDEO || chooseType == BroadConstant.TYPE_ALL_INT) {
            pictureSelector.setRecordVideoMaxSecond(recordVideoMaxSecond)//录制视频的最大时长
                .setRecordVideoMinSecond(recordVideoMinSecond)
                .setFilterVideoMaxSecond(filterVideoMaxSecond)//可选择的视频的最大时长
                .setMaxVideoSelectNum(videoMaxNum)
                .setMinVideoSelectNum(videoMinNum)
            if (chooseType == BroadConstant.TYPE_VIDEO) {
                pictureSelector.setSelectionMode(if (videoMaxNum > 1) MULTIPLE else SINGLE)
            }
        }
        if (needCrop) {
            pictureSelector.setCropEngine(ImageFileCropBroadEngine(cropBean))
        }
        val list = LinkedList<LocalMedia>()
        pictureSelector.forResult(object : OnResultCallbackListener<LocalMedia> {
            override fun onResult(result: ArrayList<LocalMedia>?) {
                result?.also {
                    if (result.isNotEmpty()) {
                        result.forEach { item ->
                            if (FileUtils.isFileExists(item.compressPath) || FileUtils.isFileExists(
                                    item.realPath
                                ) || FileUtils.isFileExists(item.sandboxPath)
                            ) {
                                list.add(item)
                            }
                        }
                        if (list.isNotEmpty()) {
                            block.invoke(list)
                        } else {
                            block.invoke(list)
                        }
                    } else {
                        block.invoke(list)
                    }
                } ?: also {
                    block.invoke(null)
                }
            }

            override fun onCancel() {
                block.invoke(null)
            }

        })

    }
}

fun chooseVideo(
    canCamera: Boolean = true,
    canChooseGif: Boolean = false,
    chooseType: Int = BroadConstant.TYPE_VIDEO,
    imgMaxNum: Int = 1,
    imgMinNum: Int = 1,
    videoMaxNum: Int = 1,
    videoMinNum: Int = 1,
    recordVideoMaxSecond: Int = 30,
    recordVideoMinSecond: Int = 3,
    filterVideoMaxSecond: Int = 31,
    maxFileSize: Long? = null,
    withSelectVideoImage: Boolean = false,
    needCrop: Boolean = false,
    cropBean: CropBroadBean = CropBroadBean(),
    selectList: ArrayList<Pair<String, LocalMedia?>>? = null,
    block: (result: LinkedList<Pair<String, LocalMedia>>?) -> Unit
) {
    val mSelectList = selectList?.let {
        LinkedList(selectList.mapNotNull { it.second })
    }
    chooseMedia(
        canCamera,
        canChooseGif,
        chooseType,
        imgMaxNum,
        imgMinNum,
        videoMaxNum,
        videoMinNum,
        recordVideoMaxSecond,
        recordVideoMinSecond,
        filterVideoMaxSecond,
        maxFileSize,
        withSelectVideoImage,
        needCrop,
        cropBean,
        mSelectList
    ) { list ->
        val resultList = LinkedList<Pair<String, LocalMedia>>()
        list?.forEach { item ->
            val imgPath = when {
                FileUtils.isFileExists(item.compressPath) -> item.compressPath
                FileUtils.isFileExists(item.sandboxPath) -> item.sandboxPath
                FileUtils.isFileExists(item.realPath) -> item.realPath
                else -> ""
            }
            if (!TextUtils.isEmpty(imgPath) && FileUtils.isFileExists(imgPath)) {
                resultList.add(Pair(imgPath, item))
            }
        }
        block.invoke(if (null == list) null else resultList)
    }

}

fun choosePicture(
    canCamera: Boolean = true,
    canChooseGif: Boolean = false,
    chooseType: Int = BroadConstant.TYPE_PIC,
    imgMaxNum: Int = 1,
    imgMinNum: Int = 1,
    videoMaxNum: Int = 1,
    videoMinNum: Int = 1,
    recordVideoMaxSecond: Int = 30,
    recordVideoMinSecond: Int = 3,
    filterVideoMaxSecond: Int = 31,
    maxFileSize: Long? = null,
    withSelectVideoImage: Boolean = false,
    needCrop: Boolean = false,
    cropBean: CropBroadBean = CropBroadBean(),
    selectList: ArrayList<Pair<String, LocalMedia?>>? = null,
    block: (result: LinkedList<Pair<String, LocalMedia>>?) -> Unit
) {
    val mSelectList = selectList?.let {
        LinkedList(selectList.mapNotNull { it.second })
    }
    chooseMedia(
        canCamera,
        canChooseGif,
        chooseType,
        imgMaxNum,
        imgMinNum,
        videoMaxNum,
        videoMinNum,
        recordVideoMaxSecond,
        recordVideoMinSecond,
        filterVideoMaxSecond,
        maxFileSize,
        withSelectVideoImage,
        needCrop,
        cropBean,
        mSelectList
    ) { list ->
        val resultList = LinkedList<Pair<String, LocalMedia>>()
        list?.forEach { item ->
            val imgPath = when {
                FileUtils.isFileExists(item.compressPath) -> item.compressPath
                FileUtils.isFileExists(item.sandboxPath) -> item.sandboxPath
                FileUtils.isFileExists(item.realPath) -> item.realPath
                else -> ""
            }
            if (!TextUtils.isEmpty(imgPath) && FileUtils.isFileExists(imgPath)) {
                resultList.add(Pair(imgPath, item))
            }
        }
        block.invoke(if (null == list) null else resultList)
    }
}


val needPermissions =
    //适配Android 13版本申请权限
    if (SdkVersionUtils.isTIRAMISU()) {
        arrayOf(
            Manifest.permission.CAMERA,
            READ_MEDIA_IMAGES,
            READ_MEDIA_VIDEO,
            READ_MEDIA_AUDIO
        )
    } else {
        arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        )

    }

class ImageCompressFileBroadEngine : CompressFileEngine {
    override fun onStartCompress(
        context: Context,
        source: ArrayList<Uri>,
        call: OnKeyValueResultCallbackListener
    ) {
        Luban.with(context).load(source).ignoreBy(100)
            .setCompressListener(object : OnNewCompressListener {
                override fun onStart() {

                }

                override fun onSuccess(source: String?, compressFile: File?) {
                    call.onCallback(source, compressFile?.absolutePath)
                }

                override fun onError(source: String?, e: Throwable?) {
                    call.onCallback(source, null)
                }

            }).launch()
    }
}

/**
 * 自定义沙盒文件处理
 */
private class MeSandboxFileBroadEngine : UriToFileTransformEngine {
    override fun onUriToFileAsyncTransform(
        context: Context?,
        srcPath: String?,
        mineType: String?,
        listener: OnKeyValueResultCallbackListener?
    ) {
        val sandboxPath = SandboxTransformUtils.copyPathToSandbox(context, srcPath, mineType)
        listener?.onCallback(srcPath, sandboxPath)

    }
}

class ImageFileCropBroadEngine(private val cropBean: CropBroadBean = CropBroadBean()) : CropFileEngine {
    override fun onStartCrop(
        fragment: Fragment?,
        srcUri: Uri?,
        destinationUri: Uri?,
        dataSource: ArrayList<String>?,
        requestCode: Int
    ) {
        if (null != srcUri && null != destinationUri && null != dataSource && null != fragment) {
            val uCrop = UCrop.of(srcUri, destinationUri, dataSource)
            uCrop.withOptions(buildOptions(cropBean))
            uCrop.setImageEngine(object : UCropImageEngine {
                override fun loadImage(context: Context?, url: String?, imageView: ImageView?) {
                    imageView?.loadImg(url)
                }

                override fun loadImage(
                    context: Context?,
                    url: Uri?,
                    maxWidth: Int,
                    maxHeight: Int,
                    call: UCropImageEngine.OnCallbackListener<Bitmap>?
                ) {
                }
            })
            uCrop.start(getAppContext(null), fragment, requestCode)
        }
    }

}

class CropBroadBean : Serializable {
    var isCircle = false//是否裁成圆
    var showGrid = false//是否显示网格线
    var widthAs = 1f//长宽比例
    var heightAs = 1f//长宽比例
    var maxScale = 0f//缩放最大比例(0为默认)
}

private fun buildOptions(
    cropBean: CropBroadBean = CropBroadBean()
): UCrop.Options {
    val options = UCrop.Options()
    options.setCompressionQuality(100) // 设置图片压缩质量
    options.setFreeStyleCropEnabled(false)//是否让用户调整范围
    options.setHideBottomControls(true)//隐藏底部工具
    options.setCircleDimmedLayer(cropBean.isCircle)// 圆
    options.setShowCropGrid(cropBean.showGrid)//是否显示网格线
    options.setCompressionFormat(if (cropBean.isCircle) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG) //图片类型
    options.setToolbarWidgetColor(getAppColor(null, R.color.white))//标题字的颜色以及按钮颜色
    options.setStatusBarColor(getAppColor(null, R.color.black_85))
    options.setToolbarColor(getAppColor(null, R.color.black_85)) // 设置标题栏颜色
    if (!cropBean.isCircle) {
        options.withAspectRatio(cropBean.widthAs, cropBean.heightAs)//长宽比例
    }
    if (0f != cropBean.maxScale) {
        options.setMaxScaleMultiplier(cropBean.maxScale)//缩放最大比例
    }
    options.setStatusBarColor(getAppColor(null, R.color.black_85))//设置状态栏颜色
//    options.setCropGridColor(Color.YELLOW);//设置裁剪网格的颜色
//    options.setCropFrameColor(Color.YELLOW);//设置裁剪框的颜色
    //    options.setDimmedLayerColor(Color.parseColor("#AA000000"))//设置裁剪外颜色
    return options
}


//.setSelectorUIStyle(); 设置相册主题
//.setLanguage(); 设置相册语言
//.setImageEngine(); 设置相册图片加载引擎
//.setCompressEngine(); 设置相册压缩引擎
//.setCropEngine(); 设置相册裁剪引擎
//.setVideoPlayerEngine(); 创建自定义播放器
//.setSandboxFileEngine(); 设置相册沙盒目录拷贝引擎
//.setOriginalFileEngine(); 设置相册图片原图处理引擎
//.setExtendLoaderEngine(); 设置相册数据源加载引擎
//.setLoaderFactoryEngine(); 设置自定义相册数据源加载器
//.setCameraInterceptListener(); 拦截相机事件，实现自定义相机
//.setRecordAudioInterceptListener(); 拦截录音事件，实现自定义录音组件
//.setEditMediaInterceptListener(); 拦截资源编辑事件，实现自定义编辑
//.setPermissionDeniedListener(); 自定义权限被拒处理回调
//.setPermissionDescriptionListener(); 自定义权限描述说明
//.setPermissionsInterceptListener(); 拦截相册权限处理事件，实现自定义权限
//.setSelectLimitTipsListener(); 拦截选择限制事件，可实现自定义提示
//.setInjectLayoutResourceListener(); 注入自定义布局回调
//.setQueryFilterListener(); 自定义查询拦截条件
//.setCustomLoadingListener(); 自定义loading样式
//.setSelectFilterListener(); 拦截不支持的选择项
//.setAttachViewLifecycle(); 监听选择器页面生命周期
//.setGridItemSelectAnimListener(); 设置Item列表选中/取消动画
//.setSelectAnimListener(); 设置选中Item动画
//.setInjectActivityPreviewFragment(); 自定义外部预览
//.setVideoThumbnailListener(); 自定义获取视频缩略图
//.setAddBitmapWatermarkListener(); 自定义添加水印
//.isCameraForegroundService(); 拍照时是否开启一个前台服务
//.setRequestedOrientation(); 设置屏幕旋转方向
//.setSelectedData(); 相册已选数据
//.setRecyclerAnimationMode(); 相册列表动画效果
//.setImageSpanCount(); 相册列表每行显示个数
//.isDisplayCamera(); 是否显示相机入口
//.isPageStrategy(); 是否开启分页模式
//.selectionMode(); 单选或是多选
//.setMaxSelectNum(); 图片最大选择数量
//.setMinSelectNum(); 图片最小选择数量
//.setMaxVideoSelectNum(); 视频最大选择数量
//.setMinVideoSelectNum(); 视频最小选择数量
//.setRecordVideoMaxSecond(); 视频录制最大时长
//.setRecordVideoMinSecond(); 视频录制最小时长
//.setFilterVideoMaxSecond(); 过滤视频最大时长
//.setFilterVideoMinSecond(); 过滤视频最小时长
//.setSelectMaxDurationSecond(); 选择最大时长视频或音频
//.setSelectMinDurationSecond(); 选择最小时长视频或音频
//.setVideoQuality(); 系统相机录制视频质量
//.isVideoPauseResumePlay(); 视频支持暂停与播放
//.isQuickCapture(); 使用系统摄像机录制后，是否支持使用系统播放器立即播放视频
//.isPreviewAudio(); 是否支持音频预览
//.isPreviewImage(); 是否支持预览图片
//.isPreviewVideo(); 是否支持预览视频
//.isPreviewFullScreenMode(); 预览点击全屏效果
//.isEmptyResultReturn(); 支持未选择返回
//.isWithSelectVideoImage(); 是否支持视频图片同选
//.isSelectZoomAnim(); 选择缩略图缩放效果
//.isOpenClickSound(); 是否开启点击音效
//.isCameraAroundState(); 是否开启前置摄像头；系统相机 只支持部分机型
//.isCameraRotateImage(); 拍照是否纠正旋转图片
//.isGif(); 是否显示gif文件
//.isWebp(); 是否显示webp文件
//.isBmp(); 是否显示bmp文件
//.isHidePreviewDownload(); 是否隐藏预览下载功能
//.isAutoScalePreviewImage(); 预览图片自动放大充满屏幕
//.setOfAllCameraType(); isWithSelectVideoImage模式下相机优先使用权
//.isMaxSelectEnabledMask(); 达到最大选择数是否开启禁选蒙层
//.isSyncCover(); isPageModel模式下是否强制同步封面，默认false
//.isAutomaticTitleRecyclerTop(); 点击相册标题是否快速回到第一项
//.isAutoVideoPlay(); 预览视频是否自动播放
//.isLoopAutoVideoPlay(); 预览视频是否循环播放
//.isFilterSizeDuration(); 是否过滤图片或音视频大小时长为0的资源
//.isFastSlidingSelect(); 快速滑动选择
//.isDirectReturnSingle(); 单选时是否立即返回
//.setCameraImageFormat(); 拍照图片输出格式
//.setCameraImageFormatForQ(); 拍照图片输出格式，Android Q以上
//.setCameraVideoFormat(); 拍照视频输出格式
//.setCameraVideoFormatForQ(); 拍照视频输出格式，Android Q以上
//.setOutputCameraDir(); 使用相机输出路径
//.setOutputAudioDir();使用录音输出路径
//.setOutputCameraImageFileName(); 图片输出文件名
//.setOutputCameraVideoFileName(); 视频输出文件名
//.setOutputAudioFileName(); 录音输出文件名
//.setQuerySandboxDir(); 查询指定目录下的资源
//.isOnlyObtainSandboxDir(); 是否只查询指定目录下的资源
//.setFilterMaxFileSize(); 过滤最大文件
//.setFilterMinFileSize(); 过滤最小文件
//.setSelectMaxFileSize(); 最大可选文件大小
//.setSelectMinFileSize(); 最小可选文件大小
//.setQueryOnlyMimeType(); 查询指定文件类型
//.setSkipCropMimeType(); 跳过不需要裁剪的类型
//.setMagicalEffectInterpolator(); 设置预览缩放模式插值器效果
//.isPageSyncAlbumCount(); 分页模式下设置过滤条件后是否同步专辑下资源的数量
//.isAllFilesAccessOf11(); Android 11下未申请权限时，进入系统设置所有权限界面
//.isFilterSizeDuration(); 过滤视频小于1秒和文件小于1kb
//.isUseSystemVideoPlayer(); 使用系统播放器