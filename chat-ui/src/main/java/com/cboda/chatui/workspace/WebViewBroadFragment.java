/*
 * Copyright (c) 2022 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.workspace;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.cboda.baselibrary.utils.CrashFactory;
import com.cboda.chatui.R;
import com.cboda.chatui.widget.webView.DWebBroadView;

public class WebViewBroadFragment extends Fragment {
    private String url;
    private String htmlContent;
    private JsBroadApi jsApi;

    DWebBroadView webView;

    public static WebViewBroadFragment loadUrl(String url) {
        WebViewBroadFragment fragment = new WebViewBroadFragment();
        Bundle bundle = new Bundle();
        bundle.putString("url", url);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_broad_webview, container, false);
        bindViews(view);
        init();
        return view;
    }

    private void bindViews(View view) {
        webView = view.findViewById(R.id.webview);
    }

    private void init() {
        Bundle bundle = getArguments();
        url = bundle.getString("url");
        htmlContent = bundle.getString("htmlContent");
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                CrashFactory.INSTANCE.getCrashLoad().crashWebView(view);
                super.onProgressChanged(view, newProgress);
            }
        });
        this.jsApi = new JsBroadApi(this, webView, url);
        webView.addJavascriptObject(this.jsApi, null);
        if (!TextUtils.isEmpty(htmlContent)) {
            webView.loadDataWithBaseURL("", htmlContent, "text/html", "UTF-8", "");
        } else {
            webView.loadUrl(url);
        }
        CrashFactory.INSTANCE.getCrashLoad().crashWebView(webView);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (!jsApi.onActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
