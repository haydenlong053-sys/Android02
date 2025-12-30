package com.lxkj.aiim.lj.http;

import android.content.Context;


import com.android.tu.loadingdialog.LoadingDailog;

import okhttp3.Request;
import okhttp3.Response;


public abstract class SpotsCallBack<T> extends BaseCallback<T> {
    private Context mContext;
    private LoadingDailog mDialog;
    LoadingDailog.Builder loadBuilder;

    public SpotsCallBack(Context context) {
        mContext = context;
        loadBuilder = new LoadingDailog.Builder(mContext)
                .setMessage("加载中...")
                .setCancelable(true)
                .setCancelOutside(true);
        mDialog = loadBuilder.create();
    }


    public void showDialog() {
        try {
            if (!mDialog.isShowing())
                mDialog.show();
        } catch (Exception e) {

        }
    }

    public void dismissDialog() {
        try {
            mDialog.dismiss();
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
