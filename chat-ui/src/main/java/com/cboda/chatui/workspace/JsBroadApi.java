/*
 * Copyright (c) 2022 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.workspace;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.util.SparseArray;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import com.cboda.chatui.WfcWebViewBroadActivity;
import com.cboda.chatui.contact.pick.PickContactBroadActivity;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.remote.ChatManager;
import cn.wildfirechat.remote.GeneralCallback;
import cn.wildfirechat.remote.GeneralCallback2;
import wendu.dsbridge.CompletionBroadHandler;
import wendu.dsbridge.DWebBroadView;

public class JsBroadApi {
    private static final String TAG = "JsBroadApi";
    private Activity activity;
    private Fragment fragment;
    private DWebBroadView webView;
    private String appUrl;
    private String currentUrl;
    private SparseArray<CompletionBroadHandler> jsCallbackHandlers;
    private boolean ready;

    private static final int REQUEST_CODE_PICK_CONTACT = 300;

    public JsBroadApi(Activity context, DWebBroadView webView, String url) {
        this.activity = context;
        this.webView = webView;
        this.appUrl = url;
        this.currentUrl = url;
        jsCallbackHandlers = new SparseArray<>();
    }

    public JsBroadApi(Fragment fragment, DWebBroadView webView, String url) {
        this(fragment.getActivity(), webView, url);
        this.fragment = fragment;
    }

    public void setCurrentUrl(String currentUrl) {
        this.currentUrl = currentUrl;
    }

    @JavascriptInterface
    public void openUrl(Object url) {
//        if (!BroadConfig.WORKSPACE_URL.equals(this.appUrl)) {
//            Log.e(TAG, "only workspace can call openurl " + this.appUrl);
//            return;
//        }
        WfcWebViewBroadActivity.loadUrl(activity, "", url.toString());
    }

    @JavascriptInterface
    public void close(Object obj, CompletionBroadHandler handler) {
        activity.finish();
        JSONObject resultObj = new JSONObject();
        try {
            resultObj.put("code", 0);
            handler.complete(resultObj);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @JavascriptInterface
    public void getAuthCode(Object obj, CompletionBroadHandler handler) {
        JSONObject jsonObject = (JSONObject) obj;
        String appId = jsonObject.optString("appId");
        int type = jsonObject.optInt("appType");
        String host = null;
        try {
            host = new URL(appUrl).getHost().toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        // 开发调试时，将 host 固定写是为开发平台上该应用的回调地址对应的 host
        ChatManager.Instance().getAuthCode(appId, type, host, new GeneralCallback2() {
            @Override
            public void onSuccess(String result) {
                JSONObject resultObj = new JSONObject();
                try {
                    resultObj.put("code", 0);
                    resultObj.put("data", result);
                    handler.complete(resultObj);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFail(int errorCode) {
                JSONObject resultObj = new JSONObject();
                try {
                    resultObj.put("code", errorCode);
                    handler.complete(resultObj);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @JavascriptInterface
    public void config(Object obj) {
        JSONObject jsonObject = (JSONObject) obj;
        String appId = jsonObject.optString("appId");
        int type = jsonObject.optInt("appType");
        long timestamp = jsonObject.optLong("timestamp");
        String nonce = jsonObject.optString("nonceStr");
        String signature = jsonObject.optString("signature");
        ChatManager.Instance().configApplication(appId, type, timestamp, nonce, signature, new GeneralCallback() {
            @Override
            public void onSuccess() {
                JsBroadApi.this.ready = true;
                webView.callHandler("ready", (Object[]) null);
            }

            @Override
            public void onFail(int errorCode) {
                webView.callHandler("error", new String[]{"" + errorCode});
            }
        });
    }

    @JavascriptInterface
    public void toast(Object text) {
        Toast.makeText(activity, text.toString(), Toast.LENGTH_SHORT).show();
    }

    @JavascriptInterface
    public void chooseContacts(Object obj, CompletionBroadHandler handler) {
        if (!preCheck()) {
            callbackJs(handler, -2);
            return;
        }
        JSONObject jsonObject = (JSONObject) obj;
        int max = jsonObject.optInt("max", 0);
        Intent intent = PickContactBroadActivity.buildPickIntent(activity, max, null, null);
        startActivityForResult(intent, REQUEST_CODE_PICK_CONTACT);
        this.jsCallbackHandlers.put(REQUEST_CODE_PICK_CONTACT, handler);
    }

    public boolean onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        boolean handled = true;
        switch (requestCode) {
            case REQUEST_CODE_PICK_CONTACT:
                handlePickContactResult(requestCode, resultCode, data);
                break;
            default:
                handled = false;
                break;
        }
        return handled;
    }

    private void handlePickContactResult(int requestCode, int resultCode, Intent data) {
        CompletionBroadHandler handler = this.jsCallbackHandlers.get(requestCode);
        if (resultCode == Activity.RESULT_OK) {
            if (data != null) {
                List<UserInfo> userInfos = data.getParcelableArrayListExtra(PickContactBroadActivity.RESULT_PICKED_USERS);
                callbackJs(handler, 0, userInfos);
            } else {
                callbackJs(handler, -1);
            }
        } else {
            callbackJs(handler, -1);
        }
        this.jsCallbackHandlers.remove(requestCode);
    }

    private void callbackJs(CompletionBroadHandler handler, int code) {
        callbackJs(handler, code, null);
    }

    private void callbackJs(CompletionBroadHandler handler, int code, Object result) {
        if (handler == null) {
            return;
        }
        JSONObject object = new JSONObject();
        try {
            if (result instanceof JSONObject || result instanceof String) {
                object.put("data", result);
            } else {
                object.put("data", new Gson().toJson(result));
            }
            object.put("code", code);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        handler.complete(object);
    }

    private void startActivityForResult(Intent intent, int requestCode) {
        if (fragment != null) {
            fragment.startActivityForResult(intent, requestCode);
        } else {
            activity.startActivityForResult(intent, requestCode);
        }

    }

    // 开发调试时，可以直接返回 true
    private boolean preCheck() {
        if (!this.ready) {
            return false;
        }
        return TextUtils.equals(getHost(this.appUrl), getHost(this.currentUrl));
    }

    private String getHost(String url) {
        try {
            String host = new URL(url).getHost();
            return host;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

}
