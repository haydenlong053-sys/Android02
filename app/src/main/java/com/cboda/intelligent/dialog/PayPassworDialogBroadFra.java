package com.cboda.intelligent.dialog;

import static android.content.Context.INPUT_METHOD_SERVICE;

import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.DialogFragment;

import com.cboda.intelligent.R;
import com.cboda.intelligent.utils.AppBroadUtil;
import com.maning.pswedittextlibrary.MNPasswordEditText;


/**
 * @author:李迪迦
 * @date: 2024/9/21
 * @Type:
 */
public class PayPassworDialogBroadFra extends DialogFragment {
    Window window;
    View frView;

    private OnShaixuanClick onShaixuanClick;
    private InputMethodManager inputMethodManager;
    private String money;
    private AppCompatImageView imDismiss;
    private TextView tvMoney;
    private MNPasswordEditText pswEdit;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        frView = inflater.inflate(R.layout.dialog_pay_broad_password, null);
        initViews(frView);

        tvMoney.setText(money);

        inputMethodManager = (InputMethodManager) getContext().getSystemService(INPUT_METHOD_SERVICE);
        pswEdit.setInputType(InputType.TYPE_CLASS_NUMBER);
        pswEdit.postDelayed(new Runnable() {
            @Override
            public void run() {
                inputMethodManager.showSoftInput(pswEdit, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 200);

        pswEdit.setOnTextChangeListener(new MNPasswordEditText.OnTextChangeListener() {
            @Override
            public void onTextChange(String text, boolean isComplete) {
                if (isComplete) {
                    onShaixuanClick.onShaixuanClick(text);
                    dismiss();
                }
            }
        });

        imDismiss.setOnClickListener(view -> dismiss());

        return frView;
    }


    public PayPassworDialogBroadFra setShaixuanClick(String money, OnShaixuanClick onShaixuanClickListener) {
        this.onShaixuanClick = onShaixuanClickListener;
        this.money = money;
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
        imDismiss = view.findViewById(R.id.imDismiss);
        tvMoney = view.findViewById(R.id.tvMoney);
        pswEdit = view.findViewById(R.id.pswEdit);
    }


    public interface OnShaixuanClick {
        void onShaixuanClick(String content);
    }
}