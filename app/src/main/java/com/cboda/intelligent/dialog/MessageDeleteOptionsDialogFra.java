package com.cboda.intelligent.dialog;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.cboda.intelligent.R;
import com.cboda.intelligent.utils.AppBroadUtil;

/**
 * 消息删除方式选择：对所有人删除 / 从我这端删除（样式参考 {@link SexDialogBroadFra}）。
 */
public class MessageDeleteOptionsDialogFra extends DialogFragment {

    public static final String TAG = "MessageDeleteOptionsDialogFra";

    public interface OnDeleteOptionClick {
        void onDeleteForAll();

        void onDeleteForMe();
    }

    private OnDeleteOptionClick listener;
    private boolean enableDeleteForAll = true;

    @NonNull
    public static MessageDeleteOptionsDialogFra show(@NonNull FragmentManager fm,
                                                     boolean enableDeleteForAll,
                                                     @NonNull OnDeleteOptionClick listener) {
        MessageDeleteOptionsDialogFra dialog = new MessageDeleteOptionsDialogFra();
        dialog.enableDeleteForAll = enableDeleteForAll;
        dialog.listener = listener;
        dialog.show(fm, TAG);
        return dialog;
    }

    public MessageDeleteOptionsDialogFra setOnDeleteOptionClick(@NonNull OnDeleteOptionClick listener) {
        this.listener = listener;
        return this;
    }

    public MessageDeleteOptionsDialogFra setEnableDeleteForAll(boolean enable) {
        this.enableDeleteForAll = enable;
        return this;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = inflater.inflate(R.layout.dialog_message_delete_options, container, false);
        TextView tvDeleteForAll = view.findViewById(R.id.tvDeleteForAll);
        TextView tvDeleteForMe = view.findViewById(R.id.tvDeleteForMe);
        TextView tvCancel = view.findViewById(R.id.tvCancel);

        if (enableDeleteForAll) {
            tvDeleteForAll.setEnabled(true);
            tvDeleteForAll.setAlpha(1f);
            tvDeleteForAll.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteForAll();
                }
                dismiss();
            });
        } else {
            tvDeleteForAll.setEnabled(false);
            tvDeleteForAll.setAlpha(0.4f);
            tvDeleteForAll.setOnClickListener(null);
        }

        tvDeleteForMe.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteForMe();
            }
            dismiss();
        });
        tvCancel.setOnClickListener(v -> dismiss());
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Window window = getDialog() != null ? getDialog().getWindow() : null;
        if (window == null) {
            return;
        }
        window.setBackgroundDrawableResource(android.R.color.transparent);
        window.setWindowAnimations(R.style.ani_bottom);
        WindowManager.LayoutParams params = window.getAttributes();
        params.gravity = Gravity.BOTTOM;
        params.width = AppBroadUtil.getScreenWidth(getContext());
        window.setAttributes(params);
    }
}
