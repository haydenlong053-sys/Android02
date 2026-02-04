package com.cboda.baselibrary.utils.imgs.load.util;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class HttpUtils {


    public static void close(BufferedInputStream bis) {
        try {
            bis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void close(InputStream is) {
        try {
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void close(BufferedOutputStream bos) {
        try {
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
