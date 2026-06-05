package com.freddy.chat.media;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;


import com.cboda.select.picture.config.PictureMimeType;
import com.cboda.select.picture.engine.CompressFileEngine;
import com.cboda.select.picture.interfaces.OnKeyValueResultCallbackListener;
import com.cboda.select.picture.utils.DateUtils;

import java.io.File;
import java.util.ArrayList;

import top.zibin.luban.CompressionPredicate;
import top.zibin.luban.Luban;
import top.zibin.luban.OnNewCompressListener;
import top.zibin.luban.OnRenameListener;

/**
 * PictureSelector 图片压缩（Luban），参考官方 Demo {@code ImageFileCompressEngine}。
 */
public final class PictureSelectorCompressEngine implements CompressFileEngine {

    /** 小于该大小（KB）的图片不压缩 */
    private static final int IGNORE_SIZE_KB = 100;

    private static final class Holder {
        static final PictureSelectorCompressEngine INSTANCE = new PictureSelectorCompressEngine();
    }

    private PictureSelectorCompressEngine() {
    }

    @NonNull
    public static PictureSelectorCompressEngine get() {
        return Holder.INSTANCE;
    }

    @Override
    public void onStartCompress(@NonNull Context context,
                                @NonNull ArrayList<Uri> source,
                                @NonNull OnKeyValueResultCallbackListener call) {
        Luban.with(context)
                .load(source)
                .ignoreBy(IGNORE_SIZE_KB)
                .setRenameListener(new OnRenameListener() {
                    @Override
                    public String rename(String filePath) {
                        int indexOf = filePath.lastIndexOf('.');
                        String postfix = indexOf != -1 ? filePath.substring(indexOf) : ".jpg";
                        return DateUtils.getCreateFileName("CMP_") + postfix;
                    }
                })
                .filter(new CompressionPredicate() {
                    @Override
                    public boolean apply(String path) {
                        if (!TextUtils.isEmpty(path) && PictureMimeType.isUrlHasVideo(path)) {
                            return false;
                        }
                        if (!TextUtils.isEmpty(path) && path.startsWith("content://")) {
                            return !PictureMimeType.isUrlHasGif(path);
                        }
                        if (PictureMimeType.isUrlHasImage(path) && !PictureMimeType.isHasHttp(path)) {
                            return true;
                        }
                        return !PictureMimeType.isUrlHasGif(path);
                    }
                })
                .setCompressListener(new OnNewCompressListener() {
                    @Override
                    public void onStart() {
                    }

                    @Override
                    public void onSuccess(String sourcePath, File compressFile) {
                        call.onCallback(sourcePath, compressFile.getAbsolutePath());
                    }

                    @Override
                    public void onError(String sourcePath, Throwable e) {
                        call.onCallback(sourcePath, null);
                    }
                })
                .launch();
    }
}
