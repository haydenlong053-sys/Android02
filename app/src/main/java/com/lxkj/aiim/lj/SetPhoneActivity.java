package com.lxkj.aiim.lj;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.cboda.intelligent.R;
import com.lxkj.aiim.base.BaseSimpleActivity;


public class SetPhoneActivity extends BaseSimpleActivity implements View.OnClickListener {


    private String phone;
    private TextView tvPhone;
    private TextView tvEnter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
        getToolbarTitle().setText(getResources().getString(R.string.更换手机号));
        tvEnter.setOnClickListener(this);
        phone = getIntent().getStringExtra("phone");
        tvPhone.setText(phone);

    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_set_phone;
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        if (view.getId() == R.id.tvEnter){
            intent = new Intent(this, YanzhengActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void initViews() {
        tvPhone = findViewById(R.id.tvPhone);
        tvEnter = findViewById(R.id.tvEnter);
    }
}