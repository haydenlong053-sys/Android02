package com.cboda.intelligent.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.cboda.intelligent.R;
import com.squareup.picasso.Picasso;

/**
 * Created by kxn on 2018/7/17 0017.
 * 不要用Picasso，外包代码配置的有问题，在release环境会无法展示图片
 */
public class GlideBroadUtil {

    public static void setImagOverSize(Context context, String url, ImageView iv,int width,int height) {
        if (!StringBroadUtil.isEmpty(url)) {
            if (url.startsWith("http")){
                if (url.endsWith("gif")) {
                    // 使用Glide播放gif图片
                    Glide.with(context)
                            .asGif()
                            .load(url)
                            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                            .override(width,height)
                            .into(iv);
                } else
                    Glide.with(context).load(url).apply(new RequestOptions().error(R.mipmap.ic_defaut).placeholder(R.mipmap.ic_defaut)).override(width,height).into(iv);
            }else
                Glide.with(context).load(BroadUrl.WebIP + url).apply(new RequestOptions().placeholder(R.mipmap.ic_defaut).dontAnimate().error(R.mipmap.ic_defaut)).override(width,height).into(iv);
        } else
            Picasso.with(context).load(R.mipmap.ic_defaut).into(iv);
    }
    public static void setImag(Context context, String url, ImageView iv) {
        if (!StringBroadUtil.isEmpty(url)) {
            if (url.startsWith("http")){
                if (url.endsWith("gif")) {
                    // 使用Glide播放gif图片
                    Glide.with(context)
                            .asGif()
                            .load(url)
                            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                            .into(iv);
                } else
                    Glide.with(context).load(url).apply(new RequestOptions().error(R.mipmap.ic_defaut).placeholder(R.mipmap.ic_defaut)).into(iv);
            }else
                Glide.with(context).load(BroadUrl.WebIP + url).apply(new RequestOptions().placeholder(R.mipmap.ic_defaut).dontAnimate().error(R.mipmap.ic_defaut)).into(iv);
        } else
            Picasso.with(context).load(R.mipmap.ic_defaut).into(iv);
    }

    public static void setImag(Context context,int res, String imageUrl, ImageView iv) {
        Glide.with(context)
                .load(imageUrl)
                .placeholder(res) // 加载中显示
                .error(res) // 加载失败显示
                .fallback(res) // 空URL或null时显示
                .into(iv);
    }


    public static void setHeadImag(Context context, String url, ImageView iv) {
        Glide.with(context)
                .load(url)
                .placeholder(R.mipmap.ic_head) // 加载中显示
                .error(R.mipmap.ic_head) // 加载失败显示
                .fallback(R.mipmap.ic_head) // 空URL或null时显示
                .into(iv);
    }



    public static void loadBitmapThumbnail(Context context, Bitmap thumbnail, ImageView imageView) {
        if (thumbnail != null) {
            Glide.with(context)
                    .load(thumbnail)
                    .into(imageView);
        }
    }

}
