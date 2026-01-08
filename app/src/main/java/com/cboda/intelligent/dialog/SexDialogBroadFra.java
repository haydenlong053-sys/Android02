package com.cboda.intelligent.dialog;

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
import com.cboda.intelligent.utils.AppBroadUtil;
import com.cboda.intelligent.utils.StringBroadUtil;


/**
 * @author:李迪迦
 * @date: 2024/9/21
 * @Type:
 */
public class SexDialogBroadFra extends DialogFragment {
    Window window;
    View frView;

    private String sex = "1";

    private OnShaixuanClick onShaixuanClick;
    private TextView tvNan;
    private TextView tvNv;
    private TextView tvConfirm;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        frView = inflater.inflate(R.layout.dialog_broad_sex, null);
        initViews(frView);
        if (StringBroadUtil.isEmpty(sex)) {
            sex = "1";
        }
        if (sex.equals("1")) {
            tvNan.setTextColor(getResources().getColor(R.color.color_text_select_sex));
            tvNv.setTextColor(getResources().getColor(R.color.color_text_unselect_sex));
        } else {
            tvNan.setTextColor(getResources().getColor(R.color.color_text_unselect_sex));
            tvNv.setTextColor(getResources().getColor(R.color.color_text_select_sex));
        }

        tvNan.setOnClickListener(v -> {
            sex = "1";
            tvNan.setTextColor(getResources().getColor(R.color.color_text_select_sex));
            tvNv.setTextColor(getResources().getColor(R.color.color_text_unselect_sex));
        });
        tvNv.setOnClickListener(v -> {
            sex = "2";
            tvNan.setTextColor(getResources().getColor(R.color.color_text_unselect_sex));
            tvNv.setTextColor(getResources().getColor(R.color.color_text_select_sex));
        });
        tvConfirm.setOnClickListener(v -> {
            onShaixuanClick.onShaixuanClick(sex);
            dismiss();
        });
        return frView;
    }


    public SexDialogBroadFra setShaixuanClick(String sex, OnShaixuanClick onShaixuanClickListener) {
        this.onShaixuanClick = onShaixuanClickListener;
        this.sex = sex;
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
        params.width = AppBroadUtil.getScreenWidth(getContext());
        window.setAttributes(params);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void initViews(View view) {
        tvNan = view.findViewById(R.id.tvNan);
        tvNv = view.findViewById(R.id.tvNv);
        tvConfirm = view.findViewById(R.id.tvConfirm);
    }


    public interface OnShaixuanClick {
        void onShaixuanClick(String sex);
    }
}