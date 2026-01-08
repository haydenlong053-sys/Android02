package com.cboda.intelligent.activity;

import android.os.Bundle;

import com.cboda.intelligent.R;
import com.cboda.intelligent.base.BaseSimpleBroadActivity;


public class NetWorkBroadActivity extends BaseSimpleBroadActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getToolbarTitle().setText(getResources().getString(R.string.网络检测));
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_net_broad_work;
    }
}