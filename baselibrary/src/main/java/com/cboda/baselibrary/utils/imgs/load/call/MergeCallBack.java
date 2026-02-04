package com.cboda.baselibrary.utils.imgs.load.call;


import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import java.util.List;


public interface MergeCallBack {
    Bitmap merge(List<Bitmap> bitmapArray, Context context, ImageView imageView);
    //用于链接标志。切换不同的加载方式，防止缓存碰撞
    String getMark();
}
