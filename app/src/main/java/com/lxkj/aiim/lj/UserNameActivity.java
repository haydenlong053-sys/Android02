package com.lxkj.aiim.lj;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.cboda.intelligent.R;
import com.lxkj.aiim.base.BaseSimpleActivity;
import com.lxkj.aiim.lj.utils.StringUtil;
import com.lxkj.aiim.lj.utils.ToastUtil;


public class UserNameActivity extends BaseSimpleActivity implements View.OnClickListener {


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
                if (StringUtil.isEmpty(etUserName.getText().toString())) {
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
        return R.layout.activity_user_name;
    }

    @Override
    public void onClick(View view) {
        if (view.getId()==R.id.ivClear){
            etUserName.setText("");
            ivClear.setVisibility(View.GONE);
        }else  if (view.getId() ==R.id.tvSave){
            if (StringUtil.isEmpty(etUserName.getText().toString())) {
                ToastUtil.show(getString(R.string.请输入用户名称));
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