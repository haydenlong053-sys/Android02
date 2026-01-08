package com.cboda.intelligent.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.cboda.intelligent.R;
import com.cboda.intelligent.base.BaseSimpleBroadActivity;
import com.cboda.intelligent.utils.StringBroadUtil;
import com.cboda.intelligent.utils.ToastBroadUtil;


public class UserNameBroadActivity extends BaseSimpleBroadActivity implements View.OnClickListener {


    private EditText etUserName;
    private ImageView ivClear;
    private TextView tvSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
        getToolbarTitle().setText(getResources().getString(R.string.设置用户名));

        etUserName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (StringBroadUtil.isEmpty(etUserName.getText().toString())) {
                    ivClear.setVisibility(View.GONE);
                } else {
                    ivClear.setVisibility(View.VISIBLE);
                }

            }
        });

        ivClear.setOnClickListener(this);
        tvSave.setOnClickListener(this);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_user_broad_name;
    }

    @Override
    public void onClick(View view) {
        if (view.getId()==R.id.ivClear){
            etUserName.setText("");
            ivClear.setVisibility(View.GONE);
        }else  if (view.getId() ==R.id.tvSave){
            if (StringBroadUtil.isEmpty(etUserName.getText().toString())) {
                ToastBroadUtil.show(getString(R.string.请输入用户名称));
                return;
            }
            Intent i = new Intent();
            i.putExtra("name", etUserName.getText().toString());
            setResult(222, i);
            finish();
        }
    }

    private void initViews() {
        etUserName = findViewById(R.id.etUserName);
        ivClear = findViewById(R.id.ivClear);
        tvSave = findViewById(R.id.tvSave);
    }
}