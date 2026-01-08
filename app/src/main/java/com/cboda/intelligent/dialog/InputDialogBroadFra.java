package com.cboda.intelligent.dialog;

import static android.content.Context.INPUT_METHOD_SERVICE;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.cboda.intelligent.R;
import com.cboda.intelligent.utils.AppBroadUtil;
import com.cboda.intelligent.utils.SoftKeyBoardBroadUtils;
import com.cboda.intelligent.utils.StringBroadUtil;
import com.cboda.intelligent.utils.ToastBroadUtil;


/**
 * @author:李迪迦
 * @date: 2024/9/26
 * @Type:
 */
public class InputDialogBroadFra extends DialogFragment {
    Window window;
    View frView;

    private OnShaixuanClick onShaixuanClick;
    private String userName;
    private EditText etContent;
    private TextView tvQuxiao;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        frView = inflater.inflate(R.layout.dialog_broad_input, null);
        initViews(frView);
        if (!StringBroadUtil.isEmpty(userName)) {
            etContent.setHint(getResources().getString(R.string.回复) + userName);
        }
        etContent.postDelayed(new Runnable() {
            @Override
            public void run() {
                SoftKeyBoardBroadUtils.openSoftKeyBoard(etContent);
            }
        }, 200);

        tvQuxiao.setOnClickListener(view -> {
            if (StringBroadUtil.isEmpty(etContent.getText().toString())) {
                ToastBroadUtil.show(getString(R.string.请输入内容));
                return;
            }
            onShaixuanClick.onShaixuanClick(etContent.getText().toString());
            dismiss();
        });
        return frView;
    }

    // 显示软键盘的自定义方法
    private void showSoftKeyboard(View view) {
        if (view.requestFocus()) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public InputDialogBroadFra setShaixuanClick(String userName, OnShaixuanClick onShaixuanClickListener) {
        this.onShaixuanClick = onShaixuanClickListener;
        this.userName = userName;
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
        etContent = view.findViewById(R.id.etContent);
        tvQuxiao = view.findViewById(R.id.tvQuxiao);
    }

    public interface OnShaixuanClick {
        void onShaixuanClick(String content);
    }
}