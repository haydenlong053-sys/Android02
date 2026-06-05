package com.freddy.chat.media;

import android.content.Context;

import androidx.annotation.NonNull;

import com.cboda.select.picture.engine.UriToFileTransformEngine;
import com.cboda.select.picture.interfaces.OnKeyValueResultCallbackListener;
import com.freddy.chat.utils.MediaUploadFileResolver;

import java.io.File;

/**
 * PictureSelector 沙盒引擎：高版本分区存储下将 {@code content://} 拷贝到应用目录。
 */
public final class PictureSelectorSandboxEngine implements UriToFileTransformEngine {

    private static final class Holder {
        static final PictureSelectorSandboxEngine INSTANCE = new PictureSelectorSandboxEngine();
    }

    private PictureSelectorSandboxEngine() {
    }

    @NonNull
    public static PictureSelectorSandboxEngine get() {
        return Holder.INSTANCE;
    }

    @Override
    public void onUriToFileAsyncTransform(@NonNull Context context,
                                          @NonNull String srcPath,
                                          @NonNull String mineType,
                                          @NonNull OnKeyValueResultCallbackListener call) {
        File file = MediaUploadFileResolver.resolve(context, srcPath);
        if (file != null) {
            call.onCallback(srcPath, file.getAbsolutePath());
        } else {
            call.onCallback(srcPath, null);
        }
    }
}
