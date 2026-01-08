package com.cboda.intelligent.activity;

import android.os.Bundle;

import com.cboda.intelligent.R;
import com.cboda.intelligent.base.BaseSimpleBroadActivity;


public class AboutUsBroadActivity extends BaseSimpleBroadActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getToolbarTitle().setText(getResources().getString(R.string.关于我们));
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_about_broad_us;
    }
}