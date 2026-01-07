package com.cboda.intelligent.utils;



import static com.cboda.baselibrary.utils.ELogUtilKt.eLogD;
import static com.cboda.baselibrary.utils.ELogUtilKt.getIsDebug;
import static com.cboda.baselibrary.utils.ELogUtilKt.toLog;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import com.blankj.utilcode.util.GsonUtils;
import com.cboda.baselibrary.network.ResBroadInterceptor;
import com.cboda.baselibrary.network.config.NetworkBroadConfig;
import com.cboda.baselibrary.utils.AESBroadUtil;
import com.cboda.intelligent.R;
import com.cboda.intelligent.bean.BaseBroadBean;
import com.cboda.intelligent.configuration.AppBroadConsts;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 封装的 okhttp
 */
public class OkHttpBroadHelper {

    public static final String TAG = "OkHttpBroadHelper";

    private static OkHttpBroadHelper mInstance;
    private OkHttpClient mHttpClient;
    private Context context;
    private Handler mHandler;
    private Map<String, Map<String, String>> mMap;


    enum HttpMethodType {
        GET,
        POST,
    }

    static {
        mInstance = new OkHttpBroadHelper();
    }

    private OkHttpBroadHelper() {
        OkHttpClient.Builder beforeBuilder = new OkHttpClient.Builder();
        beforeBuilder.connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .retryOnConnectionFailure(false);
        if (getIsDebug()) {
            ResBroadInterceptor loggingInterceptor = new ResBroadInterceptor("AppHttpOkHttp");
            beforeBuilder.addInterceptor(loggingInterceptor);
        }
        mHttpClient = beforeBuilder.build();
        mHandler = new Handler(Looper.getMainLooper());
        mMap = new HashMap<>();

    }

    /**
     * 获取 OkHttp 实例
     *
     * @return
     */
    public static OkHttpBroadHelper getInstance() {
        return mInstance;
    }

    /**
     * get 请求
     *
     * @param url
     * @param param
     * @param callback
     */
    public void get(String url, Map<String, Object> param, BaseBroadCallback callback) {
        Request request = new Request.Builder()
                .get()
                .url(url)
                .build();
        request(request, callback);
    }

    /**
     * post 请求方法
     *
     * @param url
     * @param param
     * @param callback
     */
    public void post(Context context, String url, Map<String, Object> param, BaseBroadCallback callback) {
        this.context = context;
        Request request = buildPostRequest(url, param);
        request(request, callback);
    }

    /**
     * post请求 将请求参数封装为json
     *
     * @param context
     * @param url
     * @param params   json 参数Map
     * @param callback
     */

    public void post_json(Context context, String url, Map<String, Object> params, BaseBroadCallback callback) {
        this.context = context;
        Request request = buildPostRequest(url, params);
        request(request, callback);
    }


    public void post_file(Context context, String url, Map<String, List<File>> fileParams, BaseBroadCallback callback) {
        this.context = context;
        Request request = buildFiles(url, fileParams);
        request(request, callback);
    }

    /**
     * 除文件外其他参数josn形式传递
     *
     * @param context
     * @param url
     * @param params
     * @param fileParams
     * @param callback
     */
    public void post_json_file(Context context, String url, Map<String, Object> params, Map<String, List<File>> fileParams, BaseBroadCallback callback) {
        this.context = context;
        Request request = buildRequestFiles(url, params, fileParams);
        request(request, callback);
    }

    /**
     * 文件上传 参数以 key value 形式上传
     *
     * @param context
     * @param url
     * @param params
     * @param fileParams
     * @param callback
     */
    public void post_map_file(Context context, String url, Map<String, Object> params, Map<String, List<File>> fileParams, BaseBroadCallback callback) {
        this.context = context;
        Request request = buildRequestMapFiles(url, params, fileParams);
        request(request, callback);
    }


    /**
     * 执行 网络请求 get\post\post_header
     *
     * @param request
     * @param callback
     */
    private void request(final Request request, final BaseBroadCallback callback) {
        callback.onBeforeRequest(request);
//        mHttpClient
        mHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callbackFailure(callback, request, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                callbackResponse(callback, response);
                callback.onResponse(response);

                if (response.isSuccessful()) {
                    eLogD(TAG, "http--url", request.url().toString());
                    String resultStr = response.body().string();
                    String resultStrAes = "";
                    try {
                        resultStrAes = AESBroadUtil.decrypt(resultStr);
                    } catch (Exception e) {
//                        throw new RuntimeException(e);
                        eLogD(TAG, "http--http", "--->Exception " + e);
                    }
                    eLogD(TAG, "http--http---", resultStrAes);
//                    Log.d("zhanglong","callback.mType=="+callback.mType);
                    if (callback.mType == String.class) {
//                        callbackSuccess(callback, response, resultStr);
                        callbackSuccessVpn(callback, response, resultStrAes);
                    } else {
                        try {
                            Object obj = GsonUtils.getGson().fromJson(resultStrAes, callback.mType);
//                            callbackSuccess(callback, response, obj);
                            callbackSuccessVpn(callback, response, obj);
                        } catch (com.google.gson.JsonParseException e) { // Json解析的错误
                            toLog(e, TAG);
                            callbackError(callback, response, e);
                        }
                    }
                } else {
                    callbackError(callback, response, null);
                }
            }
        });
    }

    /**
     * 构建请求参数
     *
     * @param url
     * @param methodType
     * @param params
     * @return
     */
    private Request buildRequest(String url, HttpMethodType methodType, Map<String, Object> params) {
        eLogD(TAG, "buildRequest: " + url);
        Request.Builder builder = new Request.Builder()
                .addHeader("apptoken", SharePrefBroadBroadUtil.getString(context, AppBroadConsts.APPTOKEN, ""))
                .url(url);
        String json = GsonUtils.getGson().toJson(params);
        eLogD(TAG, "http--json", json);
        if (methodType == HttpMethodType.POST) {
            Map<String, Object> paramsAes = new HashMap<>();
            try {
                paramsAes.put("encrData", AESBroadUtil.encrypt(GsonUtils.getGson().toJson(params)));
            } catch (Exception e) {
                toLog(e, TAG);
            }
            RequestBody body = builderFormData(paramsAes);
            builder.post(body);
        } else if (methodType == HttpMethodType.GET) {
            builder.get();
        }
        return builder.build();
    }

    private Request buildFiles(String url, Map<String, List<File>> fileParams) {
        eLogD(TAG, "buildFiles: " + url);
        Request.Builder builder = new Request.Builder()
                .addHeader("apptoken", SharePrefBroadBroadUtil.getString(context, AppBroadConsts.APPTOKEN, ""))
                .url(url);
        RequestBody body = builderFiles(fileParams);
        builder.post(body);
        return builder.build();
    }

    private Request buildRequestFiles(String url, Map<String, Object> params, Map<String, List<File>> fileParams) {
        Request.Builder builder = new Request.Builder()
                .url(url);
        RequestBody body = builderFormDataFiles(params, fileParams);
        builder.post(body);
        return builder.build();
    }

    private Request buildRequestMapFiles(String url, Map<String, Object> params, Map<String, List<File>> fileParams) {
        Request.Builder builder = new Request.Builder()
                .url(url);
        RequestBody body = builderFormDataMapFiles(params, fileParams);
        builder.post(body);
        return builder.build();
    }


    /**
     * 组装表单数据
     *
     * @param params
     * @return
     */
    private RequestBody builderFormData(Map<String, Object> params) {
        RequestBody body = FormBody.create(MediaType.parse("application/json"), GsonUtils.getGson().toJson(params));
//        FormBody.Builder builder = new FormBody.Builder();
//        if (params != null) {
//            for (Map.Entry<String, String> entry : params.entrySet()) {
//                builder.add(entry.getKey(), entry.getValue());
//            }
//        }
        return body;
    }

    /**
     * 组装表单数据带文件
     *
     * @param params
     * @return
     */
    private MultipartBody builderFormData_file(Map<String, String> params, File file, String key) {
        MultipartBody.Builder requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM);
        if (file != null) {
            // MediaType.parse() 里面是上传的文件类型。
            RequestBody body = RequestBody.create(MediaType.parse("application/octet-stream"), file);
            String filename = file.getName();
            // 参数分别为， 请求key ，文件名称 ， RequestBody
            requestBody.addFormDataPart(key, filename, body);
        }

        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                requestBody.addFormDataPart(entry.getKey(), entry.getValue());
            }
        }
        return requestBody.build();
    }

    /**
     * 多文件上传
     *
     * @param params
     * @param files
     * @param key
     * @return
     */
    private MultipartBody builderFormData_files(Map<String, String> params, List<File> files, String key) {
        MultipartBody.Builder requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM);
        if (files != null) {
            // MediaType.parse() 里面是上传的文件类型。
            for (int i = 0; i < files.size(); i++) {
                RequestBody body = RequestBody.create(MediaType.parse("image/png"), files.get(i));
                String filename = files.get(i).getName();
                // 参数分别为， 请求key ，文件名称 ， RequestBody
                requestBody.addFormDataPart(key + "[]", filename, body);
            }
        }
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                requestBody.addFormDataPart(entry.getKey(), entry.getValue());
            }
        }
        return requestBody.build();
    }

    private MultipartBody builderFiles(Map<String, List<File>> fileParams) {
        MultipartBody.Builder requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM);
        if (fileParams != null) {
            for (Map.Entry<String, List<File>> entry : fileParams.entrySet()) {
                List<File> files = entry.getValue();
                // MediaType.parse() 里面是上传的文件类型。
                for (int i = 0; i < files.size(); i++) {
                    RequestBody body = RequestBody.create(MediaType.parse("image/png"), files.get(i));
                    String filename = files.get(i).getName();
                    // 参数分别为， 请求key ，文件名称 ， RequestBody
                    requestBody.addFormDataPart(entry.getKey(), filename, body);
                }
            }
        }
        return requestBody.build();
    }


    /**
     * 多文件上传
     *
     * @param params
     * @param fileParams
     * @return
     */
    private MultipartBody builderFormDataFiles(Map<String, Object> params, Map<String, List<File>> fileParams) {
        MultipartBody.Builder requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM);
        if (fileParams != null) {
            for (Map.Entry<String, List<File>> entry : fileParams.entrySet()) {
                List<File> files = entry.getValue();
                // MediaType.parse() 里面是上传的文件类型。
                for (int i = 0; i < files.size(); i++) {
                    RequestBody body = RequestBody.create(MediaType.parse("image/png"), files.get(i));
                    String filename = files.get(i).getName();
                    // 参数分别为， 请求key ，文件名称 ， RequestBody
                    requestBody.addFormDataPart(entry.getKey(), filename, body);
                }
            }
        }
        if (params != null) {
            eLogD(TAG, GsonUtils.getGson().toJson(params));
            requestBody.addFormDataPart("json", GsonUtils.getGson().toJson(params));
        }
        return requestBody.build();
    }

    private MultipartBody builderFormDataMapFiles(Map<String, Object> params, Map<String, List<File>> fileParams) {
        MultipartBody.Builder requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM);
        if (fileParams != null) {
            for (Map.Entry<String, List<File>> entry : fileParams.entrySet()) {
                List<File> files = entry.getValue();
                // MediaType.parse() 里面是上传的文件类型。
                for (int i = 0; i < files.size(); i++) {
                    RequestBody body = RequestBody.create(MediaType.parse("image/png"), files.get(i));
                    String filename = files.get(i).getName();
                    // 参数分别为， 请求key ，文件名称 ， RequestBody
                    requestBody.addFormDataPart(entry.getKey(), filename, body);
                }
            }
        }
        if (params != null) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                requestBody.addFormDataPart(entry.getKey(), (String) entry.getValue());
            }
        }
        return requestBody.build();
    }

    private void callbackSuccess(final BaseBroadCallback callback, final Response response, final Object obj) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    if (callback.mType == String.class) {
                        callback.onSuccess(response, obj);
                    } else if (((BaseBroadBean) obj).code.equals("200") && ((BaseBroadBean) obj).success) {
                        callback.onSuccess(response, obj);
                    } else if (((BaseBroadBean) obj).code.equals("401")) {
                        SharePrefBroadBroadUtil.saveString(context, AppBroadConsts.APPTOKEN, "");
                        NetworkBroadConfig.INSTANCE.updateAppToken(null);
                        SharePrefBroadBroadUtil.saveString(context, AppBroadConsts.UID, "");
                        ToastBroadUtil.show(((BaseBroadBean) obj).msg);
                    } else {
                        ToastBroadUtil.show(((BaseBroadBean) obj).msg);
                        callback.onError(response, 1, new Exception());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void callbackSuccessVpn(final BaseBroadCallback callback, final Response response, final Object obj) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
//                    callback.onSuccess(response, obj);
                    try {
                        if (callback.mType == String.class) {
//                            Log.d("zhanglong","obj=="+obj);
                            callback.onSuccess(response, obj);
                        } else if (((BaseBroadBean) obj).code.equals("200") && ((BaseBroadBean) obj).success) {
                            callback.onSuccess(response, obj);
                        } else if (((BaseBroadBean) obj).code.equals("401") || ((BaseBroadBean) obj).code.equals("201")) {
                            SharePrefBroadBroadUtil.saveString(context, AppBroadConsts.APPTOKEN, "");
                            SharePrefBroadBroadUtil.saveString(context, AppBroadConsts.UID, "");
                            NetworkBroadConfig.INSTANCE.updateAppToken(null);
                            ToastBroadUtil.show(((BaseBroadBean) obj).msg);
                            AppBroadUtils.Companion.exit(context);
                        } else {
                            ToastBroadUtil.show(((BaseBroadBean) obj).msg);
                            BaseBroadBean baseBean = (BaseBroadBean) obj;
                            if (baseBean.code.equals("200") && !baseBean.success) {
                                callback.onError(response, 2, new Exception());
                            } else {

                                callback.onError(response, 1, new Exception());
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void callbackError(final BaseBroadCallback callback, final Response response, final Exception e) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
//                callback.onError(response, response.code(), e);
                ToastBroadUtil.show(context.getString(R.string.httperror));

            }
        });
    }

    private void callbackFailure(final BaseBroadCallback callback, final Request request, final IOException e) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                callback.onFailure(request, e);
                ToastBroadUtil.show(context.getString(R.string.httperror));
            }
        });
    }

    private void callbackResponse(final BaseBroadCallback callback, final Response response) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                callback.onResponse(response);
            }
        });
    }

    private Request buildGetRequest(String url) {
        return buildRequest(url, HttpMethodType.GET, null);
    }

    private Request buildPostRequest(String url, Map<String, Object> params) {

        return buildRequest(url, HttpMethodType.POST, params);
    }


    public PackageInfo getPackageInfo(Context context) {
        PackageInfo pi = null;
        try {
            PackageManager pm = context.getPackageManager();
            pi = pm.getPackageInfo(context.getPackageName(),
                    PackageManager.GET_CONFIGURATIONS);
            return pi;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pi;
    }

}