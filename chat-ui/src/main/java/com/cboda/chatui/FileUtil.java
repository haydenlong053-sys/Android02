package com.cboda.chatui;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.ImageHeaderParser;
import com.bumptech.glide.load.ImageHeaderParserUtils;
import com.bumptech.glide.load.engine.bitmap_recycle.ArrayPool;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class FileUtil {
    private static final String TAG = "野火文件保存";

    public static boolean saveVideoToGallery(Context context, String videoPath) {
        Log.d(TAG, "saveVideoToGallery called with path: " + videoPath);
        Log.d(TAG, "Android SDK version: " + Build.VERSION.SDK_INT + ", Android Q: " + Build.VERSION_CODES.Q);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Log.d(TAG, "Using MediaStore API for Android Q+");
            boolean result = saveVideoToGalleryByMediaStore(context, videoPath);
            Log.d(TAG, "MediaStore API result: " + result);
            return result;
        } else {
            Log.d(TAG, "Using File API for Android < Q");
            boolean result = saveVideoToGalleryByFile(context, videoPath);
            Log.d(TAG, "File API result: " + result);
            return result;
        }
    }

    public static boolean saveImageToGallery(Context context, String imagePath) {
        String processedImagePath = detectImageTypeAndGetNewPath(context,imagePath);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return saveImageToGalleryByMediaStore(context, processedImagePath);
        } else {
            return saveImageToGalleryByFile(context, processedImagePath);
        }
    }

    /**
     * @param outFile
     * @param bitmap
     * @return
     */
    public static File storeBitmap(File outFile, Bitmap bitmap) {
        if (!outFile.exists() || outFile.isDirectory()) {
            outFile.getParentFile().mkdirs();
        }
        FileOutputStream fOut = null;
        try {
            outFile.deleteOnExit();
            outFile.createNewFile();
            fOut = new FileOutputStream(outFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
        } catch (IOException e1) {
            outFile.deleteOnExit();
        } finally {
            if (null != fOut) {
                try {
                    fOut.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    outFile.deleteOnExit();
                }
            }
        }
        return outFile;
    }

    private static String detectImageTypeAndGetNewPath(Context context,String imagePath) {
        String mimeType = getMimeType(imagePath);
        String processedImagePath = imagePath;
        if (TextUtils.isEmpty(mimeType) || !mimeType.startsWith("image")) {
            File file = new File(imagePath);
            FileInputStream fileInputStream = null;
            try {
                fileInputStream = new FileInputStream(file);
                List<ImageHeaderParser> parsers = Glide.get(context).getRegistry().getImageHeaderParsers();
                ArrayPool arrayPool = Glide.get(context).getArrayPool();
                ImageHeaderParser.ImageType imageType = ImageHeaderParserUtils.getType(parsers, fileInputStream, arrayPool);
                if (imageType == ImageHeaderParser.ImageType.UNKNOWN) {
                    // Not one of the following image formats : GIF/JPEG/RAW/PNG/WEBP
                } else if (imageType == ImageHeaderParser.ImageType.GIF) {
                    processedImagePath = imagePath + ".gif";
                    File processedImageFile = new File(processedImagePath);
                    FileUtil.saveFileFromPath(processedImageFile, imagePath);
                } else {
                    Bitmap bitmap = Glide.with(context).asBitmap().load(imagePath).submit().get();
                    processedImagePath = imagePath + ".png";
                    File processedImageFile = new File(processedImagePath);
                   storeBitmap(processedImageFile, bitmap);
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                return imagePath;
            } finally {
                try {
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        }
        return processedImagePath;
    }

    public static boolean saveVideoToGalleryByMediaStore(Context context, String videoPath) {
        Log.d(TAG, "saveVideoToGalleryByMediaStore started");
        if (TextUtils.isEmpty(videoPath) || context == null) {
            Log.e(TAG, "param invalid - videoPath: " + videoPath + ", context: " + context);
            return false;
        }

        String videoFileName = getFileName(videoPath);
        String videoMimeType = getMimeType(videoPath);
        String appName = getAppName(context);
        Log.d(TAG, "Video details - fileName: " + videoFileName + ", mimeType: " + videoMimeType + ", appName: " + appName);
        
        final long now = System.currentTimeMillis();
        ContentValues videoImageValues = new ContentValues();
        videoImageValues.put(MediaStore.Video.Media.DATE_ADDED, now / 1000);
        videoImageValues.put(MediaStore.Video.Media.DATE_MODIFIED, now / 1000);
        videoImageValues.put(MediaStore.Video.Media.DISPLAY_NAME, videoFileName);
        videoImageValues.put(MediaStore.Video.Media.MIME_TYPE, videoMimeType);
        // insert to gallery
        Uri videoExternalContentUri = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        videoImageValues.put(MediaStore.Video.Media.IS_PENDING, 1);
        String relativePath = Environment.DIRECTORY_MOVIES + "/" + appName + "/";
        videoImageValues.put(MediaStore.Video.Media.RELATIVE_PATH, relativePath);
        Log.d(TAG, "Relative path: " + relativePath);

        ContentResolver resolver = context.getContentResolver();
        Uri uri;
        try {
            Log.d(TAG, "Inserting video to MediaStore...");
            uri = resolver.insert(videoExternalContentUri, videoImageValues);
            Log.d(TAG, "Insert result URI: " + uri);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "saveVideoToGalleryByMediaStore failed, " + e.getMessage());
            return false;
        }
        if (uri == null) {
            Log.e(TAG, "Failed to insert video to MediaStore - URI is null");
            return false;
        }
        
        // got permission, write file to public media dir
        Log.d(TAG, "Writing file to URI: " + uri);
        boolean saveSuccess = saveFileToUri(resolver, uri, videoPath);
        Log.d(TAG, "File write result: " + saveSuccess);
        if (!saveSuccess) {
            Log.e(TAG, "Failed to write file to URI, deleting entry");
            resolver.delete(uri, null, null);
            return false;
        }
        
        // Update IS_PENDING to make file visible
        videoImageValues.clear();
        videoImageValues.put(MediaStore.Video.Media.IS_PENDING, 0);
        Log.d(TAG, "Updating IS_PENDING to 0 to make file visible");
        int updateCount = resolver.update(uri, videoImageValues, null, null);
        Log.d(TAG, "Update result count: " + updateCount);

        // For Android Q and above, MediaStore API automatically adds the file to media database
        // However, as a backup measure, we also trigger manual media scan to ensure gallery refresh
        Log.d(TAG, "Video saved to gallery successfully via MediaStore API, URI: " + uri);
        
        // 作为备用方案，手动触发媒体扫描以确保相册刷新
        try {
            // 获取实际的文件路径进行媒体扫描
            String actualFilePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) 
                    + "/" + getAppName(context) + "/" + getFileName(videoPath);
            triggerMediaScan(context, actualFilePath);
        } catch (Exception e) {
            Log.w(TAG, "备用媒体扫描失败，但主要保存已成功: " + e.getMessage());
        }
        
        return true;
    }

    public static boolean saveVideoToGalleryByFile(Context context, String videoPath) {
        String videoDirPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) + "/" + getAppName(context) + "/";
        File dir = new File(videoDirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String videoName = getFileName(videoPath);
        String outputPath = videoDirPath + videoName;
        File outputFile = new File(outputPath);
        if (outputFile.exists()) {
            outputFile.delete();
        }
        boolean isSuccess = saveFileFromPath(outputFile, videoPath);
        if (!isSuccess) {
            return false;
        }
        String videoMimeType = getMimeType(videoPath);
        MediaScannerConnection.scanFile(context, new String[] {outputPath}, new String[] {videoMimeType}, null);
        return true;
    }

    public static boolean saveImageToGalleryByMediaStore(Context context, String imagePath) {
        if (TextUtils.isEmpty(imagePath) || context == null) {
            Log.e(TAG, "param invalid");
            return false;
        }

        String imageFileName = getFileName(imagePath);
        String imageMimeType = getMimeType(imagePath);
        final long now = System.currentTimeMillis();

        ContentValues newImageValues = new ContentValues();
        newImageValues.put(MediaStore.Images.Media.DATE_ADDED, now / 1000);
        newImageValues.put(MediaStore.Images.Media.DATE_MODIFIED, now / 1000);
        newImageValues.put(MediaStore.Images.Media.DISPLAY_NAME, imageFileName);
        newImageValues.put(MediaStore.Images.Media.MIME_TYPE, imageMimeType);
        // insert to gallery
        Uri imageExternalContentUri = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        newImageValues.put(MediaStore.Images.Media.IS_PENDING, 1);
        newImageValues.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/" + getAppName(context) + "/");

        ContentResolver resolver = context.getContentResolver();
        Uri uri;
        try {
            uri = resolver.insert(imageExternalContentUri, newImageValues);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "saveImageToGalleryByMediaStore failed, " + e.getMessage());
            return false;
        }
        if (uri == null) {
            return false;
        }
        // got permission, write file to public media dir
        boolean saveSuccess = saveFileToUri(resolver, uri, imagePath);
        if (!saveSuccess) {
            resolver.delete(uri, null, null);
            return false;
        }
        newImageValues.clear();
        newImageValues.put(MediaStore.Images.Media.IS_PENDING, 0);
        resolver.update(uri, newImageValues, null, null);

        // For Android Q and above, MediaStore API automatically adds the file to media database
        // However, as a backup measure, we also trigger manual media scan to ensure gallery refresh
        Log.d(TAG, "Image saved to gallery successfully via MediaStore API");
        
        // 作为备用方案，手动触发媒体扫描以确保相册刷新
        try {
            // 获取实际的文件路径进行媒体扫描
            String actualFilePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) 
                    + "/" + getAppName(context) + "/" + getFileName(imagePath);
            triggerMediaScan(context, actualFilePath);
        } catch (Exception e) {
            Log.w(TAG, "备用媒体扫描失败，但主要保存已成功: " + e.getMessage());
        }
        
        return true;
    }

    public static boolean saveImageToGalleryByFile(Context context, String imagePath) {
        String imageDirPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/" + getAppName(context) + "/";
        File dir = new File(imageDirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String imageName = getFileName(imagePath);
        String outputPath = imageDirPath + imageName;
        File outputFile = new File(outputPath);
        if (outputFile.exists()) {
            outputFile.delete();
        }
        boolean isSuccess = saveFileFromPath(outputFile, imagePath);
        if (!isSuccess) {
            return false;
        }
        String imageMimeType = getMimeType(imagePath);
        MediaScannerConnection.scanFile(context, new String[] {outputPath}, new String[] {imageMimeType}, null);
        return true;
    }

    public static String getFileExtensionFromUrl(String url) {
        if (!TextUtils.isEmpty(url)) {
            int fragment = url.lastIndexOf('#');
            if (fragment > 0) {
                url = url.substring(0, fragment);
            }

            int query = url.lastIndexOf('?');
            if (query > 0) {
                url = url.substring(0, query);
            }

            int filenamePos = url.lastIndexOf('/');
            String filename = 0 <= filenamePos ? url.substring(filenamePos + 1) : url;

            // if the filename contains special characters, we don't
            // consider it valid for our matching purposes:

            //          if (!filename.isEmpty() && Pattern.matches("[a-zA-Z_0-9\\.\\-\\(\\)\\%]+", filename))
            if (!filename.isEmpty()) {
                int dotPos = filename.lastIndexOf('.');
                if (0 <= dotPos) {
                    return filename.substring(dotPos + 1).toLowerCase();
                }
            }
        }

        return "";
    }

    public static String getMimeType(String filePath) {
        String fileExtension = getFileExtensionFromUrl(filePath);
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
    }

    public static String getFileName(String path) {
        int filenamePos = path.lastIndexOf('/');
        return 0 <= filenamePos ? path.substring(filenamePos + 1) : path;
    }

    public static boolean saveFileToUri(ContentResolver contentResolver, Uri destinationUri, String srcPath) {
        Log.d(TAG, "saveFileToUri开始 - 源文件: " + srcPath + ", 目标URI: " + destinationUri);
        
        File srcFile = new File(srcPath);
        if (!srcFile.exists()) {
            Log.e(TAG, "saveFileToUri失败 - 源文件不存在: " + srcPath);
            return false;
        }
        
        Log.d(TAG, "源文件大小: " + srcFile.length() + " bytes");
        
        InputStream is = null;
        BufferedOutputStream bos = null;
        long totalBytesWritten = 0;
        
        try {
            bos = new BufferedOutputStream(contentResolver.openOutputStream(destinationUri));
            is = new FileInputStream(srcPath);
            byte[] buf = new byte[1024];

            int actualBytes;
            while ((actualBytes = is.read(buf)) != -1) {
                bos.write(buf, 0, actualBytes);
                totalBytesWritten += actualBytes;
            }
            
            Log.d(TAG, "文件写入完成 - 总共写入字节数: " + totalBytesWritten);
            
        } catch (IOException e) {
            Log.e(TAG, "saveFileToUri异常: " + e.getMessage(), e);
            return false;
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (bos != null) {
                    bos.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "关闭流时异常: " + e.getMessage(), e);
            }
        }
        
        Log.d(TAG, "saveFileToUri成功完成");
        return true;
    }

    /**
     * 手动触发媒体扫描，确保文件在相册中显示
     * 这是一个备用方案，当MediaStore API无法正确更新相册时使用
     */
    public static void triggerMediaScan(Context context, String filePath) {
        Log.d(TAG, "手动触发媒体扫描: " + filePath);
        
        File file = new File(filePath);
        if (!file.exists()) {
            Log.e(TAG, "文件不存在，无法触发媒体扫描: " + filePath);
            return;
        }
        
        String mimeType = getMimeType(filePath);
        Log.d(TAG, "文件MIME类型: " + mimeType);
        
        MediaScannerConnection.scanFile(
            context,
            new String[]{filePath},
            new String[]{mimeType},
            new MediaScannerConnection.OnScanCompletedListener() {
                @Override
                public void onScanCompleted(String path, Uri uri) {
                    Log.d(TAG, "媒体扫描完成 - 路径: " + path + ", URI: " + uri);
                }
            }
        );
    }

    public static boolean saveFileFromPath(File desFile, String srcFilePath) {
        InputStream is = null;
        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(desFile));
            is = new FileInputStream(srcFilePath);
            byte[] buf = new byte[1024];

            int actualBytes;
            while ((actualBytes = is.read(buf)) != -1) {
                bos.write(buf, 0, actualBytes);
            }
        } catch (IOException e) {
            return false;
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (bos != null) {
                    bos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    private static String getAppName(Context context) {
        String appName = "";
        PackageManager packageManager = context.getPackageManager();
        try {
            ApplicationInfo applicationInfo =
                    packageManager.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            packageManager.getApplicationLabel(applicationInfo);
            CharSequence labelCharSequence = applicationInfo.loadLabel(packageManager);
            if (!TextUtils.isEmpty(labelCharSequence)) {
                appName = labelCharSequence.toString();
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "getAppName exception:" + e.getMessage());
        }

        return appName;
    }

    public static boolean isFileExists(String path) {
        try {
            File file = new File(path);
            return file.exists() && file.isFile();
        } catch (Exception e) {
            return false;
        }
    }
}
