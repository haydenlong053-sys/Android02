package com.lxkj.aiim.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.lxkj.aiim.R;
import com.lxkj.aiim.lj.dialogfra.NormalDialog;
import com.lxkj.aiim.lj.utils.BitmapUtils1;
import com.lxkj.aiim.lj.utils.GlideUtil;
import com.lxkj.aiim.lj.utils.ToastUtil;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.List;


public class UserEwmDialog extends Dialog {

    ImageView ivQrcode;
    RoundedImageView ivIcon;
    FrameLayout flSave;
    TextView tvSave,tvName,tvId;
    private Context context;      // 上下文

    public UserEwmDialog(final Context context, String qrcode, String icon,String name,String id) {
        super(context, com.base.commonlibrary.R.style.Theme_dialog); //dialog的样式
        this.context = context;
        Window window = getWindow();
        window.setGravity(Gravity.CENTER); // 此处可以设置dialog显示的位置为居中
        setContentView(R.layout.layout_dialog_ewm_mine);
        WindowManager windowManager = ((Activity) context).getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.width = display.getWidth() * 6 / 7; // 设置dialog宽度为屏幕的4/5
        lp.dimAmount = 0.5f;//dimAmount在0.0f和1.0f之间，0.0f完全不暗，即背景是可见的 ，1.0f时候，背景全部变黑暗。
        getWindow().setAttributes(lp);
        //要达到背景全变暗的效果，需设置getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND); 否则，背景无效果
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        //遍历控件id,添加点击事件
        ivQrcode  = (ImageView) findViewById(R.id.ivQrcode);
        ivIcon  = (RoundedImageView) findViewById(R.id.ivIcon);
        tvSave  = (TextView) findViewById(R.id.tvSave);
        tvName = (TextView) findViewById(R.id.tvName);
        tvId = (TextView) findViewById(R.id.tvId);
        flSave = (FrameLayout) findViewById(R.id.flSave);
        GlideUtil.setImag(context,qrcode,ivQrcode);
        GlideUtil.setImag(context,icon,ivIcon);
        tvName.setText(name);
        tvId.setText("账户ID："+id);
        tvSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] perms = {
                        Permission.READ_MEDIA_IMAGES,
                        Permission.CAMERA};
                if (XXPermissions.isGranted(context, perms)) {
                    tvSave.setVisibility(View.GONE);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            BitmapUtils1.saveBitmap2(BitmapUtils1.convertMeasureBitmap(flSave), context);
                            ToastUtil.show("保存成功！");
                            dismiss();
                        }
                    },1000);
                } else {
                    new NormalDialog(context, context.getString(R.string.温馨提示), "保存图片功能需要存储权限，是否请求权限？", "取消", "确定", true)
                            .setOnButtonClickListener(new NormalDialog.OnButtonClick() {
                                @Override
                                public void OnRightClick() {
                                    XXPermissions permission = XXPermissions.with(context).permission(perms);
                                    permission.request(new OnPermissionCallback() {
                                        @Override
                                        public void onGranted(@NonNull List<String> permissions, boolean allGranted) {
                                            tvSave.setVisibility(View.GONE);
                                            new Handler().postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    BitmapUtils1.saveBitmap2(BitmapUtils1.convertMeasureBitmap(flSave), context);
                                                    ToastUtil.show("保存成功！");
                                                    dismiss();
                                                }
                                            },1000);
                                        }

                                        @Override
                                        public void onDenied(@NonNull List<String> permissions, boolean doNotAskAgain) {
                                            OnPermissionCallback.super.onDenied(permissions, doNotAskAgain);
                                        }
                                    });
                                }

                                @Override
                                public void OnLeftClick() {
                                }
                            }).show();
                }
            }
        });
    }

}

