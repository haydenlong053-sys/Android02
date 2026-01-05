package com.lxkj.aiim.lj;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.cboda.baselibrary.utils.ELogUtilKt;
import com.lxkj.aiim.R;
import com.lxkj.aiim.base.BaseSimpleActivity;
import com.lxkj.aiim.lj.utils.GlideUtil;
import com.lxkj.aiim.lj.utils.ToastUtil;
import com.lxkj.aiim.widge.LiXinStandardGSYVideoPlayer;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.FileCallback;
import com.lzy.okgo.model.Progress;
import com.lzy.okgo.model.Response;
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder;
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack;
import com.shuyu.gsyvideoplayer.utils.GSYVideoType;

import java.io.File;

import com.cboda.chatui.utils.FileBroadUtils;


/**
 * Created by kxn on 2019/12/6 0006.
 */
public class VideoPlayOnlyActivity extends BaseSimpleActivity {


    private String video; // 视频URL
    private static final int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 1001;
    private LiXinStandardGSYVideoPlayer videoplayer;
    private View viHead;
    private AppCompatImageView imFinish;
    private ImageView saveImageView;
    private ImageView mIvTemp;

    @Override
    public boolean showOrHideTitleLayout() {
        return false;
    }

    @Override
    protected boolean transparentStatus() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }


    @Override
    protected int getLayoutId() {
        return R.layout.activity_video;
    }

    private void initView() {
        initViews();

        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height",
                "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        LinearLayout.LayoutParams linearParams = (LinearLayout.LayoutParams) viHead.getLayoutParams(); //取控件textView当前的布局参数
        linearParams.height = result;// 控件的高强制设成20
        viHead.setLayoutParams(linearParams);

        video = getIntent().getStringExtra("video");
//        Log.d("zhanglong","video_url==="+video);
        String videoImage = getIntent().getStringExtra("videoImage");
        Bitmap thumbnail = getIntent().getParcelableExtra("thumbnail");
        showLoading();
//        if (thumbnail!=null &&thumbnail.getWidth()>0 && thumbnail.getHeight()>0){
//            ELogUtilKt.eLogD("zhanglong","w=="+thumbnail.getWidth());
//            ELogUtilKt.eLogD("zhanglong","h=="+thumbnail.getHeight());
//            int screenWidth = ScreenBroadUtil.getScreenWidth(this);
//
//            mIvTemp.getLayoutParams().height = screenWidth/ thumbnail.getWidth() *thumbnail.getHeight();
//        }

        GlideUtil.loadBitmapThumbnail(this,thumbnail, mIvTemp);
//        jzVideo.setUp(video, "");
//        jzVideo.startVideo();

        //    2.引用
//        LinkedHashMap map = new LinkedHashMap();
//        String proxyUrl = LiXinApp.getProxy(this).getProxyUrl(video);
//        map.put("高清", proxyUrl);
//        JZDataSource jzDataSource = new JZDataSource(map, "");
//        jzDataSource.looping = true;
//        jzVideo.setUp(jzDataSource
//                , 0);
//        jzVideo.startVideo();

        ELogUtilKt.eLogE("zhanglong", "videoUrl==="+video);

        imFinish.setOnClickListener(v -> finish());

        saveImageView.setOnClickListener(v -> {
            checkPermissionAndSaveVideo();
        });

        GSYVideoType.setShowType(GSYVideoType.SCREEN_TYPE_DEFAULT);
        
        // 配置边播放边缓存
        // 方法一：使用 GSYVideoOptionBuilder（推荐）
        GSYVideoOptionBuilder gsyVideoOption = new GSYVideoOptionBuilder();
        if(FileBroadUtils.isFileExists(video)){
            gsyVideoOption
                    .setUrl(video)  // 设置视频URL
                    .setVideoTitle("")  // 设置视频标题
                    .build(videoplayer);

        }else {
            gsyVideoOption
                    .setUrl(video)  // 设置视频URL
                    .setCacheWithPlay(true)  // 开启边播放边缓存
                    .setVideoTitle("")  // 设置视频标题
                    .build(videoplayer);
        }

        videoplayer.setAutoFullWithSize(true);
        videoplayer.setLooping(true);
        videoplayer.setVideoAllCallBack(new GSYSampleCallBack(){
            @Override
            public void onPrepared(String url, Object... objects) {
                super.onPrepared(url, objects);
                // 视频已准备就绪，可以在这里添加开始播放后的逻辑
                // 例如：隐藏封面图、开始记录播放时间等
                Log.d("GSYVideoPlayer", "视频准备完成，开始播放");
                dismissLoading();
                mIvTemp.setVisibility(View.GONE);
            }

        });
        videoplayer.startPlayLogic();


    }

    /**
     * 检查是否有存储权限
     */
    private boolean hasStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10及以上版本不需要WRITE_EXTERNAL_STORAGE权限来写入公共目录
            return true;
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }
    }

    /**
     * 请求存储权限
     */
    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
    }

    /**
     * 检查权限并保存视频
     */
    private void checkPermissionAndSaveVideo() {
        if (hasStoragePermission()) {
            ToastUtil.show("正在保存视频");
            saveVideoToDevice(video);
        } else {
            requestStoragePermission();
        }
    }

    private void saveVideoToDevice(String videoUrl) {
        String TargetPath = "/sdcard/Download/";
        String name = System.currentTimeMillis() + ".mp4";

        OkGo.<File>get(videoUrl).execute(new FileCallback(TargetPath, name) {
            @Override
            public void onSuccess(Response<File> response) {
                // 下载成功后，手动触发媒体扫描，让相册能识别新文件
                File savedFile = response.body();
                if (savedFile != null && savedFile.exists()) {
                    // 方法1：使用MediaScannerConnection扫描文件
                    MediaScannerConnection.scanFile(
                            VideoPlayOnlyActivity.this,
                            new String[]{savedFile.getAbsolutePath()},
                            new String[]{"video/mp4"},
                            new MediaScannerConnection.OnScanCompletedListener() {
                                @Override
                                public void onScanCompleted(String path, Uri uri) {
                                    Log.e("野火", "媒体扫描完成: " + path);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            ToastUtil.show("保存成功，相册已更新");
                                        }
                                    });
                                }
                            }
                    );
                } else {
                    ToastUtil.show("保存成功");
                }
            }

            @Override
            public void downloadProgress(Progress progress) {
                super.downloadProgress(progress);
                Log.e("野火", "下载" + progress.toString());
            }

            @Override
            public void onError(Response<File> response) {
                super.onError(response);
                ToastUtil.show("下载失败");
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 权限被授予，开始保存视频
                ToastUtil.show("正在保存视频");
                saveVideoToDevice(video);
            } else {
                // 权限被拒绝
                ToastUtil.show("需要存储权限才能保存视频");
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        videoplayer.onVideoPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        videoplayer.release();
    }

    private void initViews() {
        videoplayer = findViewById(R.id.videoplayer);
        viHead = findViewById(R.id.viHead);
        imFinish = findViewById(R.id.imFinish);
        saveImageView = findViewById(R.id.saveImageView);
        mIvTemp = findViewById(R.id.mIvTemp);
    }
}
