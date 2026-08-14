package com.cboda.intelligent.utils;


import static com.cboda.baselibrary.utils.ELogUtilKt.eLogD;
import static com.cboda.baselibrary.utils.ELogUtilKt.getIsDebug;
import static com.cboda.baselibrary.utils.ELogUtilKt.toLog;
import static com.cboda.baselibrary.utils.ViewExpandUtilKt.getAppVersion;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;

import com.blankj.utilcode.util.GsonUtils;
import com.cboda.baselibrary.network.ResBroadInterceptor;
import com.cboda.baselibrary.network.config.NetworkBroadConfig;
import com.cboda.baselibrary.network.interceptor.HeaderBroadInterceptor;
import com.cboda.baselibrary.utils.EncryptUtil;
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
        beforeBuilder.addInterceptor(new HeaderBroadInterceptor());
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
                    String resultStrAes = decodeResponseBody(resultStr);
                    resultStrAes = normalizeImUserResponse(resultStrAes, request.url().toString());
                    eLogD(TAG, "http--http---", resultStrAes);
//                    Log.d(APP_TAG,"callback.mType=="+callback.mType);
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
                .addHeader("Accept-Language", getAcceptLanguage())
                .addHeader("App-Platform", "Android")
                .addHeader("App-Version", getAppVersion(null))
                .url(url);
        String json = GsonUtils.getGson().toJson(params);
        eLogD(TAG, "http--json", json);
        if (methodType == HttpMethodType.POST) {
//            Map<String, Object> paramsAes = new HashMap<>();
//            try {
//                paramsAes.put("encrData", EncryptUtil.INSTANCE.aesEncrypt(GsonUtils.getGson().toJson(params), "AES", null, null));
//            } catch (Exception e) {
//                toLog(e, TAG);
//            }
//            RequestBody body = builderFormData(paramsAes);
            RequestBody body = builderFormData(params);
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
                .addHeader("Accept-Language", getAcceptLanguage())
                .addHeader("App-Platform", "Android")
                .addHeader("App-Version", getAppVersion(null))
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
                        NetworkBroadConfig.INSTANCE.updateAppToken("");
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
//                            Log.d(APP_TAG,"obj=="+obj);
                            callback.onSuccess(response, obj);
                        } else {
                            BaseBroadBean bean = (BaseBroadBean) obj;
                            // IM 失败信封可能只有 errorCode/errorMsg，无 code/msg
                            if ((bean.code == null || bean.code.isEmpty()) && bean.errorCode != 0) {
                                bean.code = String.valueOf(bean.errorCode);
                                bean.success = false;
                            }
                            if ((bean.msg == null || bean.msg.isEmpty())
                                    && bean.errorMsg != null && !bean.errorMsg.isEmpty()) {
                                bean.msg = bean.errorMsg;
                            }
                            String code = bean.code != null ? bean.code : "";
                            String tip = resolveBizMessage(bean);
                            if ("200".equals(code) && bean.success) {
                                callback.onSuccess(response, obj);
                            } else if ("401".equals(code) || "201".equals(code)) {
                                SharePrefBroadBroadUtil.saveString(context, AppBroadConsts.APPTOKEN, "");
                                SharePrefBroadBroadUtil.saveString(context, AppBroadConsts.UID, "");
                                NetworkBroadConfig.INSTANCE.updateAppToken("");
                                ToastBroadUtil.show(tip);
                                AppBroadUtils.Companion.exit(context);
                            } else {
                                ToastBroadUtil.show(tip);
                                if ("200".equals(code) && !bean.success) {
                                    callback.onError(response, 2, new Exception(tip));
                                } else {
                                    callback.onError(response, 1, new Exception(tip));
                                }
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

    /**
     * 响应体解密：明文 JSON 跳过 AES，密文走原解密逻辑。
     */
    private String decodeResponseBody(String body) {
        if (body == null) {
            return "";
        }
        String trim = body.trim();
        if (trim.startsWith("{") || trim.startsWith("[")) {
            return body;
        }
        try {
            return EncryptUtil.INSTANCE.aesDecrypt(body, "AES", null, null);
        } catch (Exception e) {
            eLogD(TAG, "http--http", "--->Exception " + e);
            return body;
        }
    }

    /** 业务提示：优先 msg，其次 errorMsg。 */
    private static String resolveBizMessage(BaseBroadBean bean) {
        if (bean == null) {
            return "";
        }
        if (bean.msg != null && !bean.msg.isEmpty()) {
            return bean.msg;
        }
        if (bean.errorMsg != null && !bean.errorMsg.isEmpty()) {
            return bean.errorMsg;
        }
        return "";
    }

    /**
     * IM 用户接口返回 {@code {"errorCode":0,"content":{...}}} 时，展平为 ResultBroadBean 可解析形态。
     * <ul>
     *   <li>登录/注册：content 展平到根（token→apptoken/imtoken 等）</li>
     *   <li>失败且 content 为 null：仍映射 errorCode/errorMsg → code/msg，便于弹窗</li>
     *   <li>getUser 资料：再写入 {@code data}，供 UserInfoBroadActivity 使用 bean.data</li>
     * </ul>
     */
    private String normalizeImUserResponse(String body, String url) {
        if (body == null || body.isEmpty()) {
            return body;
        }
        boolean imUserApi = url != null && url.contains("/im/user/");
            boolean isGetUser = url != null && url.contains("/im/user/getUser");
            boolean isDuiInfo = url != null && url.contains("/im/friend/duiInfo");
        try {
            String trim = body.trim();
            if (!trim.startsWith("{")) {
                return body;
            }
            org.json.JSONObject root = new org.json.JSONObject(trim);
            if (!root.has("content") && !root.has("errorCode")) {
                return body;
            }
            Object contentObj = root.has("content") ? root.opt("content") : null;
            // 失败常见：{"content":null,"errorCode":-1,"errorMsg":"..."}
            if (!(contentObj instanceof org.json.JSONObject)) {
                if (!root.has("errorCode")) {
                    return body;
                }
                int errorCode = root.optInt("errorCode", -1);
                boolean ok = errorCode == 0;
                org.json.JSONObject flat = new org.json.JSONObject();
                flat.put("success", ok);
                flat.put("code", ok ? "200" : String.valueOf(errorCode));
                flat.put("errorCode", errorCode);
                String errorMsg = root.optString("errorMsg", "");
                flat.put("errorMsg", errorMsg);
                flat.put("msg", errorMsg);
                return flat.toString();
            }
            org.json.JSONObject content = (org.json.JSONObject) contentObj;
            boolean looksLikeAuth = content.has("success") || content.has("userId")
                    || content.has("token") || content.has("apptoken") || content.has("imtoken");
            if (!imUserApi && !looksLikeAuth && !root.has("errorCode")) {
                return body;
            }
            org.json.JSONObject flat = new org.json.JSONObject(content.toString());
            if (root.has("errorCode")) {
                int errorCode = root.optInt("errorCode", -1);
                boolean ok = errorCode == 0
                        && (!content.has("success") || content.optBoolean("success", true));
                flat.put("success", ok);
                flat.put("code", ok ? "200" : String.valueOf(errorCode));
                if (root.has("errorMsg") && !root.isNull("errorMsg")) {
                    flat.put("msg", root.optString("errorMsg", ""));
                }
            } else if (content.has("success")) {
                boolean ok = content.optBoolean("success", false);
                flat.put("success", ok);
                flat.put("code", ok ? "200" : "500");
            } else {
                flat.put("success", true);
                flat.put("code", "200");
            }
            if ((!flat.has("msg") || flat.isNull("msg") || flat.optString("msg").isEmpty())
                    && content.has("message") && !content.isNull("message")) {
                flat.put("msg", content.optString("message", ""));
            }
            // 新注册/登录：token → apptoken；无 imtoken 时复用 token
            String token = content.optString("token", "");
            if (!token.isEmpty()) {
                if (!flat.has("apptoken") || flat.isNull("apptoken")
                        || flat.optString("apptoken").isEmpty()) {
                    flat.put("apptoken", token);
                }
                if (!flat.has("imtoken") || flat.isNull("imtoken")
                        || flat.optString("imtoken").isEmpty()) {
                    flat.put("imtoken", token);
                }
            }
            // 名片二维码
            String cardQrUrl = content.optString("cardQrUrl", "");
            if (!cardQrUrl.isEmpty()
                    && (!flat.has("qrcode") || flat.isNull("qrcode")
                    || flat.optString("qrcode").isEmpty())) {
                flat.put("qrcode", cardQrUrl);
            }

            // getUser 资料页依赖 ResultBroadBean.data
            boolean isProfile = isGetUser || isDuiInfo
                    || (!content.has("token")
                    && (content.has("nickname") || content.has("idno")
                    || content.has("cardQrUrl") || content.has("isFri")));
            if (isProfile) {
                org.json.JSONObject data = new org.json.JSONObject(content.toString());
                if (data.has("nickname")
                        && (!data.has("name") || data.isNull("name")
                        || data.optString("name").isEmpty())) {
                    data.put("name", data.optString("nickname", ""));
                }
                String userId = data.optString("userId", "");
                if (!userId.isEmpty()) {
                    if (!data.has("mId") || data.isNull("mId") || data.optString("mId").isEmpty()) {
                        data.put("mId", userId);
                    }
                    if (!data.has("id") || data.isNull("id") || data.optString("id").isEmpty()) {
                        data.put("id", userId);
                    }
                }
                if (!cardQrUrl.isEmpty()
                        && (!data.has("qrcode") || data.isNull("qrcode")
                        || data.optString("qrcode").isEmpty())) {
                    data.put("qrcode", cardQrUrl);
                }
                // isVip 数字 → isvip 字符串，避免 Gson 解析 Data.isvip(String) 失败
                if (data.has("isVip") && !data.isNull("isVip")) {
                    data.put("isvip", normalizeVipFlag(data.opt("isVip")));
                } else if (data.has("isvip") && !data.isNull("isvip")) {
                    data.put("isvip", normalizeVipFlag(data.opt("isvip")));
                }
                // 根上若残留数字 isVip，去掉以免干扰
                flat.remove("isVip");
                flat.put("data", data);
                if (!userId.isEmpty()) {
                    flat.put("userId", userId);
                }
                if (!data.optString("idno").isEmpty()) {
                    flat.put("idno", data.optString("idno"));
                }
                if (!data.optString("name").isEmpty()) {
                    flat.put("name", data.optString("name"));
                }
                if (!data.optString("icon").isEmpty()) {
                    flat.put("icon", data.optString("icon"));
                }
            }
            return flat.toString();
        } catch (Exception e) {
            toLog(e, TAG);
            return body;
        }
    }

    /** 将 isVip/isvip 统一成 "0"/"1" 字符串 */
    private static String normalizeVipFlag(Object raw) {
        if (raw == null || raw == org.json.JSONObject.NULL) {
            return "0";
        }
        if (raw instanceof Number) {
            return ((Number) raw).intValue() != 0 ? "1" : "0";
        }
        if (raw instanceof Boolean) {
            return ((Boolean) raw) ? "1" : "0";
        }
        String s = String.valueOf(raw).trim();
        if ("1".equals(s) || "true".equalsIgnoreCase(s)) {
            return "1";
        }
        return "0";
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

    private String getAcceptLanguage() {
        return NetworkBroadConfig.INSTANCE.getNetAppLanguage();
    }

}