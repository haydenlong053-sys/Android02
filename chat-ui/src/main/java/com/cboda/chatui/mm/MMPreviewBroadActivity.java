/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.mm;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.cboda.baselibrary.utils.ELogUtilKt;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import com.cboda.chatui.BroadConfig;
import com.cboda.chatui.FileBroadUtil;
import com.cboda.chatui.R;
import com.cboda.chatui.third.utils.ImageBroadUtils;
import com.cboda.chatui.third.utils.UIBroadUtils;
import com.cboda.chatui.utils.DownloadBroadManager;
import com.cboda.chatui.utils.FileBroadUtils;
import com.cboda.chatui.voip.ZoomableFrameBroadLayout;
import com.cboda.chatui.widget.PhotoBroadView;
import cn.wildfirechat.message.ImageMessageContent;
import cn.wildfirechat.message.Message;
import cn.wildfirechat.message.VideoMessageContent;
import cn.wildfirechat.remote.ChatManager;

/**
 * @author imndx
 */
public class MMPreviewBroadActivity extends AppCompatActivity implements PhotoBroadView.OnDragListener, ZoomableFrameBroadLayout.OnDragListener {
    private SparseArray<View> views;
    private View currentVideoView;
    private ProgressBar videoLoadProgressBar;
    private ImageView videoPlayButton;

    private PhotoBroadView currentPhotoView;
    private ViewPager viewPager;
    private MMPagerAdapter adapter;
    private boolean secret;
    private DiskCacheStrategy diskCacheStrategy = DiskCacheStrategy.AUTOMATIC;

    private static int currentPosition = -1;
    private static List<MediaBroadEntry> entries;
    private boolean pendingPreviewInitialMedia;

    public static final String TAG = "MMPreviewBroadActivity";


    @Override
    public void onDragToFinish() {
        finish();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            super.finishAfterTransition();
//        } else {
//            finish();
//        }
    }

    @Override
    public void onDragOffset(float offset, float maxOffset) {
        View view = findViewById(R.id.bgMaskView);
        float alpha = 1 - offset / maxOffset;
        view.setAlpha(Math.max(alpha, 0.2f));

        if (videoPlayButton != null) {
            videoPlayButton.setVisibility(offset != 0.0 ? View.GONE : View.VISIBLE);
        }
        if (videoLoadProgressBar != null && offset != 0.0) {
            videoLoadProgressBar.setVisibility(View.GONE);
        }
    }

    private class MMPagerAdapter extends PagerAdapter {
        private List<MediaBroadEntry> entries;

        public MMPagerAdapter(List<MediaBroadEntry> entries) {
            this.entries = entries;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            View view;
            MediaBroadEntry entry = entries.get(position);
            if (entry.getType() == MediaBroadEntry.TYPE_IMAGE) {
                view = LayoutInflater.from(MMPreviewBroadActivity.this).inflate(R.layout.preview_photo, null);
            } else {
                view = LayoutInflater.from(MMPreviewBroadActivity.this).inflate(R.layout.preview_video, null);
            }

            container.addView(view);
            views.put(position % 3, view);
            if (pendingPreviewInitialMedia) {
                preview(view, entry);
            }
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, @NonNull Object object) {
            // do nothing ?
            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            return entries == null ? 0 : entries.size();
        }

        public MediaBroadEntry getEntry(int position) {
            return entries.get(position);
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }
    }

    final ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            // TODO 可以在此控制透明度
        }

        @Override
        public void onPageSelected(int position) {
            View view = views.get(position % 3);
            if (view == null) {
                // pending layout
                return;
            }
            if (currentVideoView != null) {
                resetVideoView(currentVideoView);
                currentVideoView = null;
                videoPlayButton = null;
                videoLoadProgressBar = null;
            }
            if (currentPhotoView != null) {
                currentPhotoView.resetScale();
            }
            MediaBroadEntry entry = adapter.getEntry(position);
            preview(view, entry);
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    private void preview(View view, MediaBroadEntry message) {
        if (message.getType() == MediaBroadEntry.TYPE_IMAGE) {
            previewImage(view, message);
        } else {
            previewVideo(view, message);
        }
        // 朋友圈
        if (message.getMessage() != null) {
            ChatManager.Instance().setMediaMessagePlayed(message.getMessage().messageId);
        }
    }

    private void resetVideoView(View view) {
        PhotoBroadView photoView = view.findViewById(R.id.photoView);
        ProgressBar loadingProgressBar = view.findViewById(R.id.loading);
        ImageView playButton = view.findViewById(R.id.btnVideo);
        VideoView videoView = view.findViewById(R.id.videoView);

        photoView.setVisibility(View.VISIBLE);
        loadingProgressBar.setVisibility(View.GONE);
        playButton.setVisibility(View.VISIBLE);
        videoView.stopPlayback();
        videoView.setVisibility(View.INVISIBLE);
    }

    private void previewVideo(View view, MediaBroadEntry entry) {

        PhotoBroadView photoView = view.findViewById(R.id.photoView);
        photoView.setOnDragListener(this);
        currentPhotoView = photoView;

        ImageView saveImageView = view.findViewById(R.id.saveImageView);
        ZoomableFrameBroadLayout zoomableFrameLayout = view.findViewById(R.id.zoomableFrameLayout);
        zoomableFrameLayout.setEnableZoom(true);
        zoomableFrameLayout.setOnDragListener(this);
        saveImageView.setVisibility(View.GONE);

        if (entry.getThumbnail() != null) {
            Glide.with(photoView).load(entry.getThumbnail()).diskCacheStrategy(diskCacheStrategy).into(photoView);
        } else {
            Glide.with(photoView).load(entry.getThumbnailUrl()).diskCacheStrategy(diskCacheStrategy).into(photoView);
        }

        VideoView videoView = view.findViewById(R.id.videoView);
        videoView.setVisibility(View.INVISIBLE);

        videoLoadProgressBar = view.findViewById(R.id.loading);
        videoLoadProgressBar.setVisibility(View.GONE);

        videoPlayButton = view.findViewById(R.id.btnVideo);
        videoPlayButton.setVisibility(View.VISIBLE);


        videoPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videoPlayButton.setVisibility(View.GONE);
                if (TextUtils.isEmpty(entry.getMediaLocalPath())) {
                    File videoFile;
                    if (entry.getMessage() != null) {
                        videoFile = DownloadBroadManager.mediaMessageContentFile(entry.getMessage());
                    } else {
                        String name = DownloadBroadManager.getNameFromUrl(entry.getMediaUrl());
                        name = TextUtils.isEmpty(name) ? System.currentTimeMillis() + "" : name;
                        videoFile = new File(BroadConfig.VIDEO_SAVE_DIR, name);
                    }
                    if (videoFile == null) {
                        return;
                    }
                    if (!videoFile.exists() || secret) {
                        String tag = System.currentTimeMillis() + "";
                        view.setTag(tag);
                        ProgressBar loadingProgressBar = view.findViewById(R.id.loading);
                        loadingProgressBar.setVisibility(View.VISIBLE);
                        final WeakReference<View> viewWeakReference = new WeakReference<>(view);
                        DownloadBroadManager.download(entry.getMediaUrl(), videoFile.getParent(), videoFile.getName(), new DownloadBroadManager.OnDownloadListener() {
                            @Override
                            public void onSuccess(File file) {
                                UIBroadUtils.postTaskSafely(() -> {
                                    View targetView = viewWeakReference.get();
                                    if (targetView != null && tag.equals(targetView.getTag())) {
                                        targetView.findViewById(R.id.loading).setVisibility(View.GONE);
                                        playVideo(targetView, file.getAbsolutePath());
                                    }
                                    saveMedia2Album(file, false);
                                });
                            }

                            @Override
                            public void onProgress(int progress) {
                                // TODO update progress
                                Log.e(MMPreviewBroadActivity.class.getSimpleName(), "video downloading progress: " + progress);
                            }

                            @Override
                            public void onFail() {
                                View targetView = viewWeakReference.get();
                                UIBroadUtils.postTaskSafely(() -> {
                                    if (targetView != null && tag.equals(targetView.getTag())) {
                                        targetView.findViewById(R.id.loading).setVisibility(View.GONE);
                                        targetView.findViewById(R.id.btnVideo).setVisibility(View.VISIBLE);
                                    }
                                });
                            }
                        });
                    } else {
                        playVideo(view, videoFile.getAbsolutePath());
                    }
                } else {
                    playVideo(view, entry.getMediaLocalPath());
                }
            }
        });
    }

    private void playVideo(View view, String videoUrl) {
        VideoView videoView = view.findViewById(R.id.videoView);
        videoView.setVisibility(View.INVISIBLE);

        PhotoBroadView photoView = view.findViewById(R.id.photoView);
        photoView.setVisibility(View.GONE);

        ImageView btn = view.findViewById(R.id.btnVideo);
        btn.setVisibility(View.GONE);

        ProgressBar loadingProgressBar = view.findViewById(R.id.loading);
        loadingProgressBar.setVisibility(View.GONE);

        view.findViewById(R.id.loading).setVisibility(View.GONE);
        currentVideoView = view;

        videoView.setVisibility(View.VISIBLE);
        videoView.setVideoPath(videoUrl);
        videoView.setOnErrorListener((mp, what, extra) -> {
            Toast.makeText(MMPreviewBroadActivity.this, "play error", Toast.LENGTH_SHORT).show();
            resetVideoView(view);
            return true;
        });
        videoView.setOnCompletionListener(mp -> {
            resetVideoView(view);
        });
        videoView.start();

    }

    private void previewImage(View view, MediaBroadEntry entry) {
        PhotoBroadView photoView = view.findViewById(R.id.photoView);
        photoView.setOnDragListener(this);
        currentPhotoView = photoView;
        ImageView saveImageView = view.findViewById(R.id.saveImageView);

        String mediaUrl = entry.getMediaUrl();
//        if (TextUtils.isEmpty(entry.getMediaLocalPath()) && !TextUtils.isEmpty(mediaUrl)) {
            if (secret) {
                saveImageView.setVisibility(View.GONE);
            } else {
                saveImageView.setVisibility(View.VISIBLE);
                saveImageView.setOnClickListener(v -> {
                    Toast.makeText(this, "图片保存中", Toast.LENGTH_SHORT).show();
                    File file = null;
                    if (entry.getMessage() != null) {
                        file = DownloadBroadManager.mediaMessageContentFile(entry.getMessage());
                    } else {
                        String name = DownloadBroadManager.getNameFromUrl(entry.getMediaUrl());
                        name = TextUtils.isEmpty(name) ? System.currentTimeMillis() + "" : name;
                        file = new File(BroadConfig.FILE_SAVE_DIR, name);
                    }
                    if (file == null) {
                        Toast.makeText(MMPreviewBroadActivity.this, "图片保存失败 file == null", Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (file.exists()) {
//                        saveMedia2Album(file, true);
                       boolean isfini = FileBroadUtil.saveImageToGallery(MMPreviewBroadActivity.this,file.getAbsolutePath());
                        if (isfini){
                            Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        File finalFile = file;
                        DownloadBroadManager.download(entry.getMediaUrl(), file.getParent(), file.getName(), new DownloadBroadManager.SimpleOnDownloadListener() {
                            @Override
                            public void onUiSuccess(File file1) {
                                if (isFinishing()) {
                                    return;
                                }
                                boolean isfini = FileBroadUtil.saveImageToGallery(MMPreviewBroadActivity.this,finalFile.getAbsolutePath());
                                if (isfini){
                                    Toast.makeText(MMPreviewBroadActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                                }
//                                saveMedia2Album(finalFile, true);
                            }
                        });
                    }
                });
            }
//        } else {
//            saveImageView.setVisibility(View.GONE);
//        }
        ELogUtilKt.eLogD("zhanglong","entry.getMediaLocalPath()=="+entry.getMediaLocalPath());
        ELogUtilKt.eLogD("zhanglong","entry.getThumbnail()=="+entry.getThumbnail());
        ELogUtilKt.eLogD("zhanglong","entry.getMediaUrl()=="+entry.getMediaUrl());
        if (!TextUtils.isEmpty(entry.getMediaLocalPath()) && FileBroadUtils.isFileExists(entry.getMediaLocalPath())){
            Glide.with(MMPreviewBroadActivity.this).load(entry.getMediaLocalPath()).into(photoView);

        }else if (entry.getThumbnail() != null) {
            RequestBuilder<Drawable> thumbnailRequest = Glide
                    .with(MMPreviewBroadActivity.this)
                    .load(entry.getThumbnail());
            Glide.with(MMPreviewBroadActivity.this).load(entry.getMediaUrl()).diskCacheStrategy(diskCacheStrategy)
                    .thumbnail(thumbnailRequest)
//                .placeholder(new BitmapDrawable(getResources(), entry.getThumbnail()))
                .into(photoView);
        } else {
            Glide.with(MMPreviewBroadActivity.this).load(entry.getMediaUrl()).diskCacheStrategy(diskCacheStrategy)
                .placeholder(new BitmapDrawable(getResources(), entry.getThumbnailUrl()))
                .into(photoView);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mm_preview);
        supportPostponeEnterTransition();

        views = new SparseArray<>(3);
        viewPager = findViewById(R.id.viewPager);
        adapter = new MMPagerAdapter(entries);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(1);
        viewPager.addOnPageChangeListener(pageChangeListener);
        if (currentPosition == 0) {
            viewPager.post(() -> {
                pageChangeListener.onPageSelected(0);
            });
        } else {
            viewPager.setCurrentItem(currentPosition);
            pendingPreviewInitialMedia = true;
        }
        secret = getIntent().getBooleanExtra("secret", false);
        diskCacheStrategy = secret ? DiskCacheStrategy.NONE : DiskCacheStrategy.AUTOMATIC;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (currentVideoView != null) {
            resetVideoView(currentVideoView);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (secret) {
            for (MediaBroadEntry entry : entries) {
                if (entry.getType() == MediaBroadEntry.TYPE_VIDEO) {
                    File secretVideoFile = DownloadBroadManager.mediaMessageContentFile(entry.getMessage());
                    if (secretVideoFile.exists()) {
                        secretVideoFile.delete();
                    }
                }
            }
        }
        entries = null;
    }

    private void saveMedia2Album(File file, boolean isImage) {
        String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT <= Build.VERSION_CODES.TIRAMISU) {
            boolean granted = checkSelfPermission(permissions[0]) == PackageManager.PERMISSION_GRANTED;
            if (granted) {
                ImageBroadUtils.saveMedia2Album(this, file, isImage);
                Toast.makeText(MMPreviewBroadActivity.this, "图片保存成功", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(MMPreviewBroadActivity.this, "请授权访问外部存储设备权限", Toast.LENGTH_LONG).show();
                requestPermissions(permissions, 100);
            }
        } else {
            ImageBroadUtils.saveMedia2Album(this, file, isImage);
            Toast.makeText(MMPreviewBroadActivity.this, "图片保存成功", Toast.LENGTH_LONG).show();
        }
    }

    public static void previewMedia(Context context, List<MediaBroadEntry> entries, int current) {
        previewMedia(context, entries, current, false);
    }

    public static void previewMedia(Context context, List<MediaBroadEntry> entries, int current, boolean secret) {
        if (entries == null || entries.isEmpty()) {
            Log.w(MMPreviewBroadActivity.class.getSimpleName(), "message is null or empty");
            return;
        }
        MMPreviewBroadActivity.entries = entries;
        MMPreviewBroadActivity.currentPosition = current;
        Intent intent = new Intent(context, MMPreviewBroadActivity.class);
        intent.putExtra("secret", secret);
        context.startActivity(intent);
    }

    public static void previewImage(Context context, Message message) {
        if (!(message.content instanceof ImageMessageContent)) {
            Log.e(TAG, "previewImage without imageMessageContent");
            return;
        }

        List<MediaBroadEntry> entries = new ArrayList<>();
        MediaBroadEntry entry = new MediaBroadEntry(message);
        entries.add(entry);
        previewMedia(context, entries, 0, false);
    }

    public static void previewImage(Context context, String imageUrl) {
        List<MediaBroadEntry> entries = new ArrayList<>();

        MediaBroadEntry entry = new MediaBroadEntry();
        entry.setType(MediaBroadEntry.TYPE_IMAGE);
        entry.setMediaUrl(imageUrl);
        entries.add(entry);
        previewMedia(context, entries, 0, false);
    }

    public static void previewVideo(Context context, Message message) {
        if (!(message.content instanceof VideoMessageContent)) {
            Log.e(TAG, "previewVideo without videoMessageContent");
            return;
        }
        List<MediaBroadEntry> entries = new ArrayList<>();

        MediaBroadEntry entry = new MediaBroadEntry(message);
        entries.add(entry);
        previewMedia(context, entries, 0, false);
    }
}
