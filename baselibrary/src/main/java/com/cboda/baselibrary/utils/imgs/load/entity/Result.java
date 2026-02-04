package com.cboda.baselibrary.utils.imgs.load.entity;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;


public class Result {
    public Bitmap bitmap;
    public String url;
    public ImageView imageView;
    public View joinView;

    public ArrayList<Bitmap> bitmaps;

    public Result(Bitmap bitmap, String url, ImageView imageView) {
        this.bitmap = bitmap;
        this.url = url;
        this.imageView = imageView;
    }

    public Result(ArrayList<Bitmap> bitmaps, String url, View joinView) {
        this.bitmaps = bitmaps;
        this.url = url;
        this.joinView = joinView;
    }
}