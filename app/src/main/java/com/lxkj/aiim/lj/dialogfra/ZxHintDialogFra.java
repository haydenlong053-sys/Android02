package com.lxkj.aiim.lj.dialogfra;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.cboda.intelligent.R;
import com.lxkj.aiim.lj.utils.AppUtil;
import com.lxkj.aiim.lj.utils.StringUtil;


/**
 * @author:李迪迦
 * @date: 2024/9/21
 * @Type:
 */
public class ZxHintDialogFra extends DialogFragment {
    Window window;
    View frView;

    private String hint = "";
    private TextView tvHint;
    private TextView tvConfirm;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        frView = inflater.inflate(R.layout.dialog_zxsb, null);
        initViews(frView);
        if (!StringUtil.isEmpty(hint)) {
            tvHint.setText(hint);
        }
        tvConfirm.setOnClickListener(v -> {
            dismiss();
        });

        return frView;
    }


    public ZxHintDialogFra setContent(String hint) {
        this.hint = hint;
        return this;
    }

    @Override
    public void onResume() {
        super.onResume();
        window = getDialog().getWindow();
        // 如果不设置这句代码, 那么弹框就会与四边都有一定的距离
        window.setBackgroundDrawableResource(android.R.color.transparent);
        // 设置动画
        window.setWindowAnimations(R.style.ani_bottom);
        WindowManager.LayoutParams params = window.getAttributes();
        params.gravity = Gravity.BOTTOM;
        // 如果不设置宽度,那么即使你在布局中设置宽度为 match_parent 也不会起作用
        params.width = AppUtil.getScreenWidth(getContext());
        window.setAttributes(params);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void initViews(View view) {
        tvHint = view.findViewById(R.id.tvHint);
        tvConfirm = view.findViewById(R.id.tvConfirm);
    }
}