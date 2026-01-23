package com.cboda.intelligent.utils;

import static com.cboda.baselibrary.utils.ViewExpandUtilKt.dismissLoadingDialog;
import static com.cboda.baselibrary.utils.ViewExpandUtilKt.showLoadingDialog;

import android.content.Context;

import okhttp3.Request;
import okhttp3.Response;


public abstract class SpotsCallBroadBack<T> extends BaseBroadCallback<T> {
    private Context mContext;
//    private LoadingDailog mDialog;
//    LoadingDailog.Builder loadBuilder;

    public SpotsCallBroadBack(Context context) {
        mContext = context;
//        loadBuilder = new LoadingDailog.Builder(mContext)
//                .setMessage("加载中...")
//                .setCancelable(true)
//                .setCancelOutside(true);
//        mDialog = loadBuilder.create();
    }


    public void showDialog() {
        try {
            showLoadingDialog(mContext);
        } catch (Exception e) {

        }
    }

    public void dismissDialog() {
        try {
            dismissLoadingDialog();
        } catch (Exception e) {

        }
    }

    @Override
    public void onFailure(Request request, Exception e) {
        dismissDialog();
    }

    @Override
    public void onBeforeRequest(Request request) {
        showDialog();
    }

    @Override
    public void onResponse(Response response) {
        dismissDialog();
    }
}
