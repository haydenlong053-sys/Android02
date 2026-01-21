package com.cboda.select.picture.config;

import android.content.pm.ActivityInfo;

import com.cboda.select.picture.basic.IBridgeLoaderFactory;
import com.cboda.select.picture.basic.IBridgeViewLifecycle;
import com.cboda.select.picture.basic.InterpolatorFactory;
import com.cboda.select.picture.engine.CompressEngine;
import com.cboda.select.picture.engine.CompressFileEngine;
import com.cboda.select.picture.engine.CropEngine;
import com.cboda.select.picture.engine.CropFileEngine;
import com.cboda.select.picture.engine.ExtendLoaderEngine;
import com.cboda.select.picture.engine.ImageEngine;
import com.cboda.select.picture.engine.SandboxFileEngine;
import com.cboda.select.picture.engine.UriToFileTransformEngine;
import com.cboda.select.picture.engine.VideoPlayerEngine;
import com.cboda.select.picture.entity.LocalMedia;
import com.cboda.select.picture.entity.LocalMediaFolder;
import com.cboda.select.picture.interfaces.OnBitmapWatermarkEventListener;
import com.cboda.select.picture.interfaces.OnCameraInterceptListener;
import com.cboda.select.picture.interfaces.OnCustomLoadingListener;
import com.cboda.select.picture.interfaces.OnExternalPreviewEventListener;
import com.cboda.select.picture.interfaces.OnGridItemSelectAnimListener;
import com.cboda.select.picture.interfaces.OnInjectActivityPreviewListener;
import com.cboda.select.picture.interfaces.OnInjectLayoutResourceListener;
import com.cboda.select.picture.interfaces.OnMediaEditInterceptListener;
import com.cboda.select.picture.interfaces.OnPermissionDeniedListener;
import com.cboda.select.picture.interfaces.OnPermissionDescriptionListener;
import com.cboda.select.picture.interfaces.OnPermissionsInterceptListener;
import com.cboda.select.picture.interfaces.OnPreviewInterceptListener;
import com.cboda.select.picture.interfaces.OnQueryFilterListener;
import com.cboda.select.picture.interfaces.OnRecordAudioInterceptListener;
import com.cboda.select.picture.interfaces.OnResultCallbackListener;
import com.cboda.select.picture.interfaces.OnSelectAnimListener;
import com.cboda.select.picture.interfaces.OnSelectFilterListener;
import com.cboda.select.picture.interfaces.OnSelectLimitTipsListener;
import com.cboda.select.picture.interfaces.OnVideoThumbnailEventListener;
import com.cboda.select.picture.language.LanguageConfig;
import com.cboda.select.picture.magical.BuildRecycleItemViewParams;
import com.cboda.select.picture.style.PictureSelectorStyle;
import com.cboda.select.picture.thread.PictureThreadUtils;
import com.cboda.select.picture.utils.FileDirMap;
import com.cboda.select.picture.utils.LanguageUtil;
import com.cboda.select.picture.utils.SdkVersionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author：luck
 * @date：2017-05-24 17:02
 * @describe：PictureSelector BroadConfig
 */

public final class SelectorConfig {
    public int chooseMode;
    public boolean isOnlyCamera;
    public boolean isDirectReturnSingle;
    public String cameraImageFormat;
    public String cameraVideoFormat;
    public String cameraImageFormatForQ;
    public String cameraVideoFormatForQ;
    public int requestedOrientation;
    public boolean isCameraAroundState;
    public int selectionMode;
    public int maxSelectNum;
    public int minSelectNum;
    public int maxVideoSelectNum;
    public int minVideoSelectNum;
    public int minAudioSelectNum;
    public int videoQuality;
    public int filterVideoMaxSecond;
    public int filterVideoMinSecond;
    public int selectMaxDurationSecond;
    public int selectMinDurationSecond;
    public int recordVideoMaxSecond;
    public int recordVideoMinSecond;
    public int imageSpanCount;
    public long filterMaxFileSize;
    public long filterMinFileSize;
    public long selectMaxFileSize;
    public long selectMinFileSize;
    public String language;
    public String defaultLanguage;
    public boolean isDisplayCamera;
    public boolean isGif;
    public boolean isWebp;
    public boolean isBmp;
    public boolean isHeic;
    public boolean isEnablePreviewImage;
    public boolean isEnablePreviewVideo;
    public boolean isEnablePreviewAudio;
    public boolean isPreviewFullScreenMode;
    public boolean isPreviewZoomEffect;
    public boolean isOpenClickSound;
    public boolean isEmptyResultReturn;
    public boolean isHidePreviewDownload;
    public boolean isWithVideoImage;
    public List<String> queryOnlyImageList;
    public List<String> queryOnlyVideoList;
    public List<String> queryOnlyAudioList;
    public List<String> skipCropList;
    public boolean isCheckOriginalImage;
    public String outPutCameraImageFileName;
    public String outPutCameraVideoFileName;
    public String outPutAudioFileName;
    public String outPutCameraDir;
    public String outPutAudioDir;
    public String sandboxDir;
    public String originalPath;
    public String cameraPath;
    public String sortOrder;
    public String defaultAlbumName;
    public int pageSize;
    public boolean isPageStrategy;
    public boolean isFilterInvalidFile;
    public boolean isMaxSelectEnabledMask;
    public int animationMode;
    public boolean isAutomaticTitleRecyclerTop;
    public boolean isQuickCapture;
    public boolean isCameraRotateImage;
    public boolean isAutoRotating;
    public boolean isSyncCover;
    public int ofAllCameraType;
    public boolean isOnlySandboxDir;
    public boolean isCameraForegroundService;
    public boolean isResultListenerBack;
    public boolean isInjectLayoutResource;
    public boolean isActivityResultBack;
    public boolean isCompressEngine;
    public boolean isLoaderDataEngine;
    public boolean isLoaderFactoryEngine;
    public boolean isSandboxFileEngine;
    public boolean isOriginalControl;
    public boolean isDisplayTimeAxis;
    public boolean isFastSlidingSelect;
    public boolean isSelectZoomAnim;
    public boolean isAutoVideoPlay;
    public boolean isLoopAutoPlay;
    public boolean isFilterSizeDuration;
    public boolean isPageSyncAsCount;
    public boolean isPauseResumePlay;
    public boolean isSyncWidthAndHeight;
    public boolean isOriginalSkipCompress;
    public boolean isPreloadFirst;
    public boolean isUseSystemVideoPlayer;
    public boolean isNewKeyBackMode;
    public PictureSelectorStyle selectorStyle;

    public SelectorConfig() {
        initDefaultValue();
    }

    private void initDefaultValue() {
        chooseMode = SelectMimeType.ofImage();
        isOnlyCamera = false;
        selectionMode = SelectModeConfig.MULTIPLE;
        selectorStyle = new PictureSelectorStyle();
        maxSelectNum = 9;
        minSelectNum = 0;
        maxVideoSelectNum = 1;
        minVideoSelectNum = 0;
        minAudioSelectNum = 0;
        videoQuality = VideoQuality.VIDEO_QUALITY_HIGH;
        language = "";
        defaultLanguage = "";
        filterVideoMaxSecond = 0;
        filterVideoMinSecond = 0;
        selectMaxDurationSecond = 0;
        selectMinDurationSecond = 0;
        filterMaxFileSize = 0;
        filterMinFileSize = 0;
        selectMaxFileSize = 0;
        selectMinFileSize = 0;
        recordVideoMaxSecond = 60;
        recordVideoMinSecond = 0;
        imageSpanCount = PictureConfig.DEFAULT_SPAN_COUNT;
        isCameraAroundState = false;
        isWithVideoImage = false;
        isDisplayCamera = true;
        isGif = false;
        isWebp = true;
        isBmp = true;
        isHeic = true;
        isCheckOriginalImage = false;
        isDirectReturnSingle = false;
        isEnablePreviewImage = true;
        isEnablePreviewVideo = true;
        isEnablePreviewAudio = true;
        isHidePreviewDownload = false;
        isOpenClickSound = false;
        isEmptyResultReturn = false;
        cameraImageFormat = PictureMimeType.JPEG;
        cameraVideoFormat = PictureMimeType.MP4;
        cameraImageFormatForQ = PictureMimeType.MIME_TYPE_IMAGE;
        cameraVideoFormatForQ = PictureMimeType.MIME_TYPE_VIDEO;
        outPutCameraImageFileName = "";
        outPutCameraVideoFileName = "";
        outPutAudioFileName = "";
        queryOnlyImageList = new ArrayList<>();
        queryOnlyVideoList = new ArrayList<>();
        queryOnlyAudioList = new ArrayList<>();
        outPutCameraDir = "";
        outPutAudioDir = "";
        sandboxDir = "";
        originalPath = "";
        cameraPath = "";
        pageSize = PictureConfig.MAX_PAGE_SIZE;
        isPageStrategy = true;
        isFilterInvalidFile = false;
        isMaxSelectEnabledMask = false;
        animationMode = -1;
        isAutomaticTitleRecyclerTop = true;
        isQuickCapture = true;
        isCameraRotateImage = true;
        isAutoRotating = true;
        isSyncCover = !SdkVersionUtils.isQ();
        ofAllCameraType = SelectMimeType.ofAll();
        isOnlySandboxDir = false;
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
        isCameraForegroundService = false;
        isResultListenerBack = true;
        isActivityResultBack = false;
        isCompressEngine = false;
        isLoaderDataEngine = false;
        isLoaderFactoryEngine = false;
        isSandboxFileEngine = false;
        isPreviewFullScreenMode = true;
        isPreviewZoomEffect = chooseMode != SelectMimeType.ofAudio();
        isOriginalControl = false;
        isInjectLayoutResource = false;
        isDisplayTimeAxis = true;
        isFastSlidingSelect = false;
        skipCropList = new ArrayList<>();
        sortOrder = "";
        isSelectZoomAnim = true;
        defaultAlbumName = "";
        isAutoVideoPlay = false;
        isLoopAutoPlay = false;
        isFilterSizeDuration = true;
        isPageSyncAsCount = false;
        isPauseResumePlay = false;
        isSyncWidthAndHeight = true;
        isOriginalSkipCompress = false;
        isPreloadFirst = true;
        isNewKeyBackMode = true;
        isUseSystemVideoPlayer = false;
    }

    /**
     * Callback listening
     */
    public ImageEngine imageEngine;
    public CompressEngine compressEngine;
    public CompressFileEngine compressFileEngine;
    public CropEngine cropEngine;
    public CropFileEngine cropFileEngine;
    public SandboxFileEngine sandboxFileEngine;
    public UriToFileTransformEngine uriToFileTransformEngine;
    public ExtendLoaderEngine loaderDataEngine;
    public VideoPlayerEngine videoPlayerEngine;
    public IBridgeViewLifecycle viewLifecycle;
    public IBridgeLoaderFactory loaderFactory;
    public InterpolatorFactory interpolatorFactory;
    public OnCameraInterceptListener onCameraInterceptListener;
    public OnSelectLimitTipsListener onSelectLimitTipsListener;
    public OnResultCallbackListener<LocalMedia> onResultCallListener;
    public OnExternalPreviewEventListener onExternalPreviewEventListener;
    public OnInjectActivityPreviewListener onInjectActivityPreviewListener;
    public OnMediaEditInterceptListener onEditMediaEventListener;
    public OnPermissionsInterceptListener onPermissionsEventListener;
    public OnInjectLayoutResourceListener onLayoutResourceListener;
    public OnPreviewInterceptListener onPreviewInterceptListener;
    public OnSelectFilterListener onSelectFilterListener;
    public OnPermissionDescriptionListener onPermissionDescriptionListener;
    public OnPermissionDeniedListener onPermissionDeniedListener;
    public OnRecordAudioInterceptListener onRecordAudioListener;
    public OnQueryFilterListener onQueryFilterListener;
    public OnBitmapWatermarkEventListener onBitmapWatermarkListener;
    public OnVideoThumbnailEventListener onVideoThumbnailEventListener;
    public OnGridItemSelectAnimListener onItemSelectAnimListener;
    public OnSelectAnimListener onSelectAnimListener;
    public OnCustomLoadingListener onCustomLoadingListener;

    /**
     * selected current album folder
     */
    public LocalMediaFolder currentLocalMediaFolder;

    /**
     * selected result
     */
    public final ArrayList<LocalMedia> selectedResult = new ArrayList<>();

    public synchronized ArrayList<LocalMedia> getSelectedResult() {
        return selectedResult;
    }

    public int getSelectCount() {
        return selectedResult.size();
    }

    public void addSelectResult(LocalMedia media) {
        selectedResult.add(media);
    }

    public void addAllSelectResult(ArrayList<LocalMedia> result) {
        selectedResult.addAll(result);
    }

    public String getResultFirstMimeType() {
        return selectedResult.size() > 0 ? selectedResult.get(0).getMimeType() : "";
    }

    /**
     * selected preview result
     */
    public final ArrayList<LocalMedia> selectedPreviewResult = new ArrayList<>();

    public void addSelectedPreviewResult(ArrayList<LocalMedia> list) {
        if (list != null) {
            selectedPreviewResult.clear();
            selectedPreviewResult.addAll(list);
        }
    }

    /**
     * all album data source
     */
    public final ArrayList<LocalMediaFolder> albumDataSource = new ArrayList<>();

    public void addAlbumDataSource(List<LocalMediaFolder> list) {
        if (list != null) {
            albumDataSource.clear();
            albumDataSource.addAll(list);
        }
    }

    /**
     * all data source
     */
    public final ArrayList<LocalMedia> dataSource = new ArrayList<>();

    public void addDataSource(ArrayList<LocalMedia> list) {
        if (list != null) {
            dataSource.clear();
            dataSource.addAll(list);
        }
    }

    /**
     * 释放监听器
     */
    public void destroy() {
        imageEngine = null;
        compressEngine = null;
        compressFileEngine = null;
        cropEngine = null;
        cropFileEngine = null;
        sandboxFileEngine = null;
        uriToFileTransformEngine = null;
        loaderDataEngine = null;
        onResultCallListener = null;
        onCameraInterceptListener = null;
        onExternalPreviewEventListener = null;
        onInjectActivityPreviewListener = null;
        onEditMediaEventListener = null;
        onPermissionsEventListener = null;
        onLayoutResourceListener = null;
        onPreviewInterceptListener = null;
        onSelectLimitTipsListener = null;
        onSelectFilterListener = null;
        onPermissionDescriptionListener = null;
        onPermissionDeniedListener = null;
        onRecordAudioListener = null;
        onQueryFilterListener = null;
        onBitmapWatermarkListener = null;
        onVideoThumbnailEventListener = null;
        viewLifecycle = null;
        loaderFactory = null;
        interpolatorFactory = null;
        onItemSelectAnimListener = null;
        onSelectAnimListener = null;
        videoPlayerEngine = null;
        onCustomLoadingListener = null;
        currentLocalMediaFolder = null;
        dataSource.clear();
        selectedResult.clear();
        albumDataSource.clear();
        selectedPreviewResult.clear();
        PictureThreadUtils.cancel(PictureThreadUtils.getIoPool());
        BuildRecycleItemViewParams.clear();
        FileDirMap.clear();
        LocalMedia.destroyPool();
    }
}
