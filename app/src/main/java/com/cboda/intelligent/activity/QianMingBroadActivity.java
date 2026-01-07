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


public class QianMingBroadActivity extends BaseSimpleBroadActivity implements View.OnClickListener {

    private String sign;
    private EditText etSign;
    private ImageView imDelate;
    private TextView tvSave;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_qian_ming;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
        getToolbarTitle().setText(getResources().getString(R.string.设置签名));

        sign = getIntent().getStringExtra("sign");
        etSign.setText(sign);
        if (StringBroadUtil.isEmpty(etSign.getText().toString())) {
            imDelate.setVisibility(View.GONE);
        } else {
            imDelate.setVisibility(View.VISIBLE);
        }

        etSign.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (StringBroadUtil.isEmpty(etSign.getText().toString())) {
                    imDelate.setVisibility(View.GONE);
                } else {
                    imDelate.setVisibility(View.VISIBLE);
                }

            }
        });

        imDelate.setOnClickListener(this);
        tvSave.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.imDelate){
            etSign.setText("");
            imDelate.setVisibility(View.GONE);
        }
        if (view.getId() == R.id.tvSave){
//            if (StringBroadUtil.isEmpty(etSign.getText().toString())) {
//                ToastBroadUtil.show(getString(R.string.请输入昵称));
//                return;
//            }
            Intent i = new Intent();
            i.putExtra("name", etSign.getText().toString());
            setResult(444, i);
            finish();
        }
    }

    private void initViews() {
        etSign = findViewById(R.id.etSign);
        imDelate = findViewById(R.id.imDelate);
        tvSave = findViewById(R.id.tvSave);
    }
}