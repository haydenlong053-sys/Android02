package com.cboda.intelligent.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Environment;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 简述： <功能描述>
 * Created by ysh on 2023/11/28.
 */
public class BitmapBroadUtils1 {
    public static File saveBitmap(Bitmap bm, Context mContext) {
        File file = null;
        String name = System.currentTimeMillis() + ".jpg";
        //指定我们想要存储文件的地址
        String TargetPath = mContext.getFilesDir() + "/images/";
        //判断指定文件夹的路径是否存在
        if (!fileIsExist(TargetPath)) {
        } else {
            //如果指定文件夹创建成功，那么我们则需要进行图片存储操作
            File saveFile = new File(TargetPath, name);
            try {
                FileOutputStream saveImgOut = new FileOutputStream(saveFile);
                // compress - 压缩的意思
                bm.compress(Bitmap.CompressFormat.JPEG, 80, saveImgOut);
                //存储完成后需要清除相关的进程
                saveImgOut.flush();
                saveImgOut.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            file = saveFile;
        }
        return file;

    }

    public static File saveBitmap2(Bitmap bm,Context context) {
        File file = null;
        String name = System.currentTimeMillis() + ".jpg";
        //指定我们想要存储文件的地址
        String TargetPath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Camera")
                .getAbsolutePath() + File.separator ;
        //判断指定文件夹的路径是否存在
        if (!fileIsExist(TargetPath)) {
        } else {
            //如果指定文件夹创建成功，那么我们则需要进行图片存储操作
            File saveFile = new File(TargetPath, name);

            try {
                FileOutputStream saveImgOut = new FileOutputStream(saveFile);
                // compress - 压缩的意思
                bm.compress(Bitmap.CompressFormat.JPEG, 80, saveImgOut);
                //存储完成后需要清除相关的进程
                saveImgOut.flush();
                saveImgOut.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            file = saveFile;
            scanPhotos(saveFile,context);
        }
        return file;

    }

    public static void scanPhotos(File file, Context context) {
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(file);
        intent.setData(uri);
        context.sendBroadcast(intent);
    }

    /**
     * 判断指定目录的文件夹是否存在，如果不存在则需要创建新的文件夹
     *
     * @param fileName 指定目录
     * @return 返回创建结果 TRUE or FALSE
     */

    static boolean fileIsExist(String fileName) {
        //传入指定的路径，然后判断路径是否存在
        File file = new File(fileName);
        if (file.exists())
            return true;
        else {
            //file.mkdirs() 创建文件夹的意思
            return file.mkdirs();
        }

    }

    public static Bitmap convertMeasureBitmap(View view) {
        view.measure(View.MeasureSpec.makeMeasureSpec(view.getWidth(), View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec
                (view.getHeight(), View.MeasureSpec.AT_MOST));
        return convertViewToBitmap(view, view.getMeasuredWidth(), view.getMeasuredHeight());
    }

    public static Bitmap convertViewToBitmap(View view, int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        view.draw(new Canvas(bitmap));
        return bitmap;
    }
}
