package com.cboda.intelligent.activity;

import static com.cboda.intelligent.utils.ViewExpandUtilKt.showGuidePasswordTip;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.cboda.intelligent.R;
import com.cboda.intelligent.base.BaseSimpleBroadActivity;
import com.cboda.baselibrary.popup.CommonTipBroadPop;
import com.cboda.intelligent.bean.ResultBroadBean;
import com.cboda.intelligent.dialog.NormalBroadDialog;
import com.cboda.intelligent.utils.BaseBroadCallback;
import com.cboda.intelligent.utils.OkHttpBroadHelper;
import com.cboda.intelligent.utils.SpotsCallBroadBack;
import com.cboda.intelligent.net.BroadUrl;
import com.cboda.intelligent.utils.BitmapBroadUtils1;
import com.cboda.intelligent.utils.GlideBroadUtil;
import com.cboda.intelligent.utils.StringBroadUtil;
import com.cboda.intelligent.utils.ToastBroadUtil;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Request;
import okhttp3.Response;

//账号与安全
public class ZhaqBroadActivity extends BaseSimpleBroadActivity implements View.OnClickListener {


    private RoundedImageView riIcon;
    private RelativeLayout rlIcon;
    private TextView tvName;
    private TextView tvIdno;
    private RoundedImageView ivQrcode;
    private View llSave;
    private TextView tvRegInfo;
    private EditText etName;
    private TextView etPsw;
    private TextView tvEdit;
    private TextView tvSaveInfo;
    private TextView tvSave;
    private boolean isHasPassword =false;
    private CommonTipBroadPop commonTipPop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
        getToolbarTitle().setText(getResources().getString(R.string.账号与安全));

        tvSave.setOnClickListener(this);
        tvSaveInfo.setOnClickListener(this);
        tvEdit.setOnClickListener(this);
        ptConfig();
//        userInfo();

    }

    private void ptConfig() {
        Map<String, Object> param = new HashMap<>();
        OkHttpBroadHelper.getInstance().post_json(this, BroadUrl.ptConfig, param, new BaseBroadCallback<ResultBroadBean>() {
            @Override
            public void onBeforeRequest(Request request) {

            }

            @Override
            public void onFailure(Request request, Exception e) {

            }

            @Override
            public void onResponse(Response response) {

            }

            @Override
            public void onSuccess(Response response, ResultBroadBean resultBean) {
                tvRegInfo.setText(resultBean.data.appinfo);
                configBean = resultBean;

            }

            @Override
            public void onError(Response response, int code, Exception e) {
            }
        });
    }


    private void wanshan() {
        if (TextUtils.isEmpty(etName.getText())) {
            ToastBroadUtil.show(getString(R.string.请输入用户名));
            return;
        }
        if (TextUtils.isEmpty(etPsw.getText())) {
            ToastBroadUtil.show(getString(R.string.请输入密码));
            return;
        }
        Map<String, Object> param = new HashMap<>();
        param.put("account", etName.getText().toString());
        param.put("pwd", etPsw.getText().toString());
        OkHttpBroadHelper.getInstance().post_json(this, BroadUrl.wanshan, param, new SpotsCallBroadBack<ResultBroadBean>(this) {
            @Override
            public void onSuccess(Response response, ResultBroadBean resultBean) {
                finish();
            }

            @Override
            public void onError(Response response, int code, Exception e) {
            }
        });
    }


    @Override
    protected int getLayoutId() {
        return R.layout.activity_zha_broad_q;
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        if (view.getId() == R.id.tvSave){
            //                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//                    if (Environment.isExternalStorageManager()) {
//                        // 权限已被授予，可以访问外部存储
//                        String[] perms1 = {
//                                Permission.WRITE_EXTERNAL_STORAGE};
//                        if (XXPermissions.isGranted(this, perms1)) {
//                            BitmapBroadUtils1.saveBitmap2(BitmapBroadUtils1.convertMeasureBitmap(llSave), this);
//                            ToastBroadUtil.show("保存成功");
//                        } else {
//                            new NormalBroadDialog(this, getString(R.string.温馨提示), "保存图片功能需要存储权限，是否请求权限？", "取消", "确定", true)
//                                    .setOnButtonClickListener(new NormalBroadDialog.OnButtonClick() {
//                                        @Override
//                                        public void OnRightClick() {
//                                            XXPermissions permission = XXPermissions.with(ZhaqBroadActivity.this).permission(perms1);
//                                            permission.request(new OnPermissionCallback() {
//                                                @Override
//                                                public void onGranted(@NonNull List<String> permissions, boolean allGranted) {
//                                                    BitmapBroadUtils1.saveBitmap2(BitmapBroadUtils1.convertMeasureBitmap(llSave), ZhaqBroadActivity.this);
//                                                    ToastBroadUtil.show("保存成功");
//                                                }
//
//                                                @Override
//                                                public void onDenied(@NonNull List<String> permissions, boolean doNotAskAgain) {
//                                                    OnPermissionCallback.super.onDenied(permissions, doNotAskAgain);
//                                                    ToastBroadUtil.show(getString(R.string.权限被拒绝));
//                                                }
//                                            });
//                                        }
//
//                                        @Override
//                                        public void OnLeftClick() {
//                                        }
//                                    }).show();
//                        }
//                    } else {
//                        // 权限未被授予，可以提示用户授权
//                        intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
//                        startActivity(intent);
//                    }
//                } else {
//                    String[] perms1 = {
//                            Permission.WRITE_EXTERNAL_STORAGE,
//                    };
//                    if (XXPermissions.isGranted(this, perms1)) {
//                        BitmapBroadUtils1.saveBitmap2(BitmapBroadUtils1.convertMeasureBitmap(llSave), this);
//                        ToastBroadUtil.show("保存成功");
//                    } else {
//                        new NormalBroadDialog(this, getString(R.string.温馨提示), "保存图片功能需要存储权限，是否请求权限？", "取消", "确定", true)
//                                .setOnButtonClickListener(new NormalBroadDialog.OnButtonClick() {
//                                    @Override
//                                    public void OnRightClick() {
//                                        XXPermissions permission = XXPermissions.with(ZhaqBroadActivity.this).permission(perms1);
//                                        permission.request(new OnPermissionCallback() {
//                                            @Override
//                                            public void onGranted(@NonNull List<String> permissions, boolean allGranted) {
//                                                BitmapBroadUtils1.saveBitmap2(BitmapBroadUtils1.convertMeasureBitmap(llSave), ZhaqBroadActivity.this);
//                                                ToastBroadUtil.show("保存成功");
//                                            }
//
//                                            @Override
//                                            public void onDenied(@NonNull List<String> permissions, boolean doNotAskAgain) {
//                                                OnPermissionCallback.super.onDenied(permissions, doNotAskAgain);
//                                                ToastBroadUtil.show(getString(R.string.权限被拒绝));
//                                            }
//                                        });
//                                    }
//
//                                    @Override
//                                    public void OnLeftClick() {
//                                    }
//                                }).show();
//                    }
//                }
            String[] perms = {
                    Permission.READ_MEDIA_IMAGES,
                    Permission.CAMERA};
            if (XXPermissions.isGranted(this, perms)) {
                tvSave.setVisibility(View.INVISIBLE);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        BitmapBroadUtils1.saveBitmap2(BitmapBroadUtils1.convertMeasureBitmap(llSave), ZhaqBroadActivity.this);
                        ToastBroadUtil.show("保存成功！");
                    }
                }, 1000);
            } else {
                new NormalBroadDialog(this, this.getString(R.string.温馨提示), getString(R.string.save_image_permission_request), getString(R.string.quxiao), getString(R.string.queding),  true)
                        .setOnButtonClickListener(new NormalBroadDialog.OnButtonClick() {
                            @Override
                            public void OnRightClick() {
                                XXPermissions permission = XXPermissions.with(ZhaqBroadActivity.this).permission(perms);
                                permission.request(new OnPermissionCallback() {
                                    @Override
                                    public void onGranted(@NonNull List<String> permissions, boolean allGranted) {
                                        tvSave.setVisibility(View.INVISIBLE);
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                BitmapBroadUtils1.saveBitmap2(BitmapBroadUtils1.convertMeasureBitmap(llSave), ZhaqBroadActivity.this);
                                                ToastBroadUtil.show(getString(R.string.tipSaveSuccess));

                                            }
                                        }, 1000);
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
        if (view.getId() == R.id.tvSaveInfo){
            //在跳转的对应页面直接保存信息，此处只做关闭页面效果
            finish();
//            wanshan();
        }
        if (view.getId() == R.id.tvEdit) {
            if (null != userBean) {
                if (userBean.data.hasPaypwd.equals("1")) {
                    startActivity(new Intent(ZhaqBroadActivity.this, VerifyPayPasswordBroadActivity.class));
                } else {
                    new NormalBroadDialog(ZhaqBroadActivity.this, getString(R.string.温馨提示), getString(R.string.未设置支付密码), getString(R.string.取消), getString(R.string.前去设置), false)
                            .setOnButtonClickListener(new NormalBroadDialog.OnButtonClick() {
                                @Override
                                public void OnRightClick() {
                                    Intent intent = new Intent(ZhaqBroadActivity.this, SetPayPasswordBroadActivity.class);
                                    intent.putExtra("editIndex", "1");
                                    startActivity(intent);

//                                    if (StringBroadUtil.isEmpty(userBean.data.mb)) {
//                                        if (configBean.data.openMibao.equals("1")) {
//                                            Intent intent = new Intent(ZhaqBroadActivity.this, MiBaoBroadActivity.class);
//                                            intent.putExtra("type", "0");
//                                            startActivity(intent);
//                                        } else {
//                                            Intent intent = new Intent(ZhaqBroadActivity.this, SetPayPasswordBroadActivity.class);
//                                            intent.putExtra("editIndex", "1");
//                                            startActivity(intent);
//                                        }
//                                    } else {
//                                        Intent intent = new Intent(ZhaqBroadActivity.this, SetPayPasswordBroadActivity.class);
//                                        intent.putExtra("editIndex", "1");
//                                        startActivity(intent);
//
//                                    }
                                }

                                @Override
                                public void OnLeftClick() {
                                    finish();
                                }
                            }).show();
                }
            } else {
                userInfo();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        userInfo();

    }

    private ResultBroadBean userBean, configBean;

    /**
     * 用户信息
     */
    private void userInfo() {
        Map<String, Object> param = new HashMap<>();
        OkHttpBroadHelper.getInstance().post_json(this, BroadUrl.userInfo, param, new SpotsCallBroadBack<ResultBroadBean>(this) {

            @Override
            public void onSuccess(Response response, ResultBroadBean resultBean) {
                tvName.setText(resultBean.data.name);
                GlideBroadUtil.setImag(ZhaqBroadActivity.this, resultBean.data.qrcode2, ivQrcode);
                GlideBroadUtil.setImag(ZhaqBroadActivity.this, resultBean.data.icon, riIcon);
                tvIdno.setText("ID:" + resultBean.data.idno);
                etName.setText(resultBean.data.acount);
//                etPsw.setText(resultBean.data.pwd);
                userBean = resultBean;
                isHasPassword = !StringBroadUtil.isEmpty(resultBean.data.paypwd);
                if (!isHasPassword && commonTipPop ==null){
                    commonTipPop=showGuidePasswordTip(ZhaqBroadActivity.this);
                }
            }

            @Override
            public void onError(Response response, int code, Exception e) {
            }
        });
    }

    private void initViews() {
        riIcon = findViewById(R.id.riIcon);
        rlIcon = findViewById(R.id.rlIcon);
        tvName = findViewById(R.id.tvName);
        tvIdno = findViewById(R.id.tvIdno);
        ivQrcode = findViewById(R.id.ivQrcode);
        llSave = findViewById(R.id.llSave);
        tvRegInfo = findViewById(R.id.tvRegInfo);
        etName = findViewById(R.id.etName);
        etPsw = findViewById(R.id.etPsw);
        tvEdit = findViewById(R.id.tvEdit);
        tvSaveInfo = findViewById(R.id.tvSaveInfo);
        tvSave = findViewById(R.id.tvSave);
    }
}