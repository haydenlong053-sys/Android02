package com.cboda.chatui;

import android.view.View;


import com.gyf.immersionbar.ImmersionBar;

public abstract class WfcFullBaseBroadActivity  extends WfcBaseBroadActivity{


    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
//        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        ImmersionBar.with(this).statusBarColor(com.cboda.baselibrary.R.color.transparent)
                .statusBarDarkFont(true).keyboardEnable(false).init();

    }

    protected void addTitleBarMarginTop(View view) {
        ImmersionBar.with(this).titleBarMarginTop(view);
    }

    @Override
    protected boolean isWfcStatusBar() {
        return false;
    }

}
