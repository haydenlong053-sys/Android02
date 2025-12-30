package com.lxkj.aiim.lj;

import android.os.Bundle;

import com.lxkj.aiim.R;
import com.lxkj.aiim.base.BaseSimpleActivity;


public class NetWorkActivity extends BaseSimpleActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getToolbarTitle().setText(getResources().getString(R.string.网络检测));
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_net_work;
    }
}