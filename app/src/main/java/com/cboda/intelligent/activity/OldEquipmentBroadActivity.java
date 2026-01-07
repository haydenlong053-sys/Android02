package com.cboda.intelligent.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cboda.intelligent.R;
import com.cboda.intelligent.base.BaseSimpleBroadActivity;
import com.cboda.baselibrary.utils.MMKVBroadUtils;


public class OldEquipmentBroadActivity extends BaseSimpleBroadActivity implements View.OnClickListener {


    private View viHead;
    private TextView tvLanguage;
    private EditText etUser;
    private TextView tvLogin;

    @Override
    public boolean showOrHideTitleLayout() {
        return false;
    }

    @Override
    protected boolean transparentStatus() {
        return true;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_old_equipment;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height",
                "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        LinearLayout.LayoutParams linearParams = (LinearLayout.LayoutParams) viHead.getLayoutParams(); //取控件textView当前的布局参数
        linearParams.height = result;// 控件的高强制设成20
        viHead.setLayoutParams(linearParams);

        tvLanguage.setText(MMKVBroadUtils.Companion.getYuYan());

        tvLanguage.setOnClickListener(this);
        tvLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        if (view.getId() == R.id.tvLanguage){
            intent = new Intent(this, LanguageBroadActivity.class);
            startActivity(intent);
        }else if (view.getId() == R.id.tvLogin){
            intent = new Intent(this, LoginBroadActivity.class);
            startActivity(intent);
        }

    }

    private void initViews() {
        viHead = findViewById(R.id.viHead);
        tvLanguage = findViewById(R.id.tvLanguage);
        etUser = findViewById(R.id.etUser);
        tvLogin = findViewById(R.id.tvLogin);
    }
}