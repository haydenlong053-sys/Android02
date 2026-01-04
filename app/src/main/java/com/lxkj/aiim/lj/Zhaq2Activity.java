package com.lxkj.aiim.lj;

import static com.lxkj.aiim.lj.utils.ViewExpandUtilKt.showGuidePasswordTip;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.cboda.baselibrary.utils.OnSingleClickBroadListener;
import com.blankj.utilcode.util.BarUtils;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.lxkj.aiim.R;
import com.lxkj.aiim.base.BaseSimpleActivity;
import com.lxkj.aiim.dialog.pop.CommonTipPop;
import com.lxkj.aiim.lj.bean.ResultBean;
import com.lxkj.aiim.lj.dialogfra.NormalDialog;
import com.lxkj.aiim.lj.http.BaseCallback;
import com.lxkj.aiim.lj.http.OkHttpHelper;
import com.lxkj.aiim.lj.http.SpotsCallBack;
import com.lxkj.aiim.lj.http.Url;
import com.lxkj.aiim.lj.utils.BitmapUtils1;
import com.lxkj.aiim.lj.utils.GlideUtil;
import com.lxkj.aiim.lj.utils.StringUtil;
import com.lxkj.aiim.lj.utils.ToastUtil;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Request;
import okhttp3.Response;

//暂时不启用
@Deprecated
public class Zhaq2Activity extends BaseSimpleActivity implements View.OnClickListener {


    private View mIvMyBack;
    private View myToolView;
    private TextView tvMyTitle;
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
    private CommonTipPop commonTipPop;

    @Override
    public boolean showOrHideTitleLayout() {
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
        BarUtils.addMarginTopEqualStatusBarHeight(myToolView);
        tvMyTitle.setText(getResources().getString(R.string.m_account_info));
        mIvMyBack.setOnClickListener(new OnSingleClickBroadListener() {
            @Override
            public void onSingleClick(View v) {
                finish();
            }
        });

        tvSave.setOnClickListener(this);
        tvSaveInfo.setOnClickListener(this);
        tvEdit.setOnClickListener(this);
        ptConfig();
//        userInfo();

    }

    private void ptConfig() {
        Map<String, Object> param = new HashMap<>();
        OkHttpHelper.getInstance().post_json(this, Url.ptConfig, param, new BaseCallback<ResultBean>() {
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
            public void onSuccess(Response response, ResultBean resultBean) {
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
            ToastUtil.show(getString(R.string.请输入用户名));
            return;
        }
        if (TextUtils.isEmpty(etPsw.getText())) {
            ToastUtil.show(getString(R.string.请输入密码));
            return;
        }
        Map<String, Object> param = new HashMap<>();
        param.put("account", etName.getText().toString());
        param.put("pwd", etPsw.getText().toString());
        OkHttpHelper.getInstance().post_json(this, Url.wanshan, param, new SpotsCallBack<ResultBean>(this) {
            @Override
            public void onSuccess(Response response, ResultBean resultBean) {
                finish();
            }

            @Override
            public void onError(Response response, int code, Exception e) {
            }
        });
    }


    @Override
    protected int getLayoutId() {
        return R.layout.activity_zhaq2;
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
//                            BitmapUtils1.saveBitmap2(BitmapUtils1.convertMeasureBitmap(llSave), this);
//                            ToastUtil.show("保存成功");
//                        } else {
//                            new NormalDialog(this, getString(R.string.温馨提示), "保存图片功能需要存储权限，是否请求权限？", "取消", "确定", true)
//                                    .setOnButtonClickListener(new NormalDialog.OnButtonClick() {
//                                        @Override
//                                        public void OnRightClick() {
//                                            XXPermissions permission = XXPermissions.with(ZhaqActivity.this).permission(perms1);
//                                            permission.request(new OnPermissionCallback() {
//                                                @Override
//                                                public void onGranted(@NonNull List<String> permissions, boolean allGranted) {
//                                                    BitmapUtils1.saveBitmap2(BitmapUtils1.convertMeasureBitmap(llSave), ZhaqActivity.this);
//                                                    ToastUtil.show("保存成功");
//                                                }
//
//                                                @Override
//                                                public void onDenied(@NonNull List<String> permissions, boolean doNotAskAgain) {
//                                                    OnPermissionCallback.super.onDenied(permissions, doNotAskAgain);
//                                                    ToastUtil.show(getString(R.string.权限被拒绝));
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
//                        BitmapUtils1.saveBitmap2(BitmapUtils1.convertMeasureBitmap(llSave), this);
//                        ToastUtil.show("保存成功");
//                    } else {
//                        new NormalDialog(this, getString(R.string.温馨提示), "保存图片功能需要存储权限，是否请求权限？", "取消", "确定", true)
//                                .setOnButtonClickListener(new NormalDialog.OnButtonClick() {
//                                    @Override
//                                    public void OnRightClick() {
//                                        XXPermissions permission = XXPermissions.with(ZhaqActivity.this).permission(perms1);
//                                        permission.request(new OnPermissionCallback() {
//                                            @Override
//                                            public void onGranted(@NonNull List<String> permissions, boolean allGranted) {
//                                                BitmapUtils1.saveBitmap2(BitmapUtils1.convertMeasureBitmap(llSave), ZhaqActivity.this);
//                                                ToastUtil.show("保存成功");
//                                            }
//
//                                            @Override
//                                            public void onDenied(@NonNull List<String> permissions, boolean doNotAskAgain) {
//                                                OnPermissionCallback.super.onDenied(permissions, doNotAskAgain);
//                                                ToastUtil.show(getString(R.string.权限被拒绝));
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
                        BitmapUtils1.saveBitmap2(BitmapUtils1.convertMeasureBitmap(llSave), Zhaq2Activity.this);
                        ToastUtil.show("保存成功！");
                    }
                }, 1000);
            } else {
                new NormalDialog(this, this.getString(R.string.温馨提示), "保存图片功能需要存储权限，是否请求权限？", "取消", "确定", true)
                        .setOnButtonClickListener(new NormalDialog.OnButtonClick() {
                            @Override
                            public void OnRightClick() {
                                XXPermissions permission = XXPermissions.with(Zhaq2Activity.this).permission(perms);
                                permission.request(new OnPermissionCallback() {
                                    @Override
                                    public void onGranted(@NonNull List<String> permissions, boolean allGranted) {
                                        tvSave.setVisibility(View.INVISIBLE);
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                BitmapUtils1.saveBitmap2(BitmapUtils1.convertMeasureBitmap(llSave), Zhaq2Activity.this);
                                                ToastUtil.show("保存成功！");

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
            wanshan();
        }
        if (view.getId() == R.id.tvEdit) {
            if (null != userBean) {
                if (userBean.data.hasPaypwd.equals("1")) {
                    startActivity(new Intent(Zhaq2Activity.this, VerifyPayPasswordActivity.class));
                } else {
                    new NormalDialog(Zhaq2Activity.this, getString(R.string.温馨提示), getString(R.string.未设置支付密码), getString(R.string.取消), getString(R.string.前去设置), false)
                            .setOnButtonClickListener(new NormalDialog.OnButtonClick() {
                                @Override
                                public void OnRightClick() {
                                    if (StringUtil.isEmpty(userBean.data.mb)) {
                                        if (configBean.data.openMibao.equals("1")) {
                                            Intent intent = new Intent(Zhaq2Activity.this, MiBaoActivity.class);
                                            intent.putExtra("type", "0");
                                            startActivity(intent);
                                        } else {
                                            Intent intent = new Intent(Zhaq2Activity.this, SetPayPasswordActivity.class);
                                            intent.putExtra("editIndex", "1");
                                            startActivity(intent);
                                        }
                                    } else {
                                        Intent intent = new Intent(Zhaq2Activity.this, SetPayPasswordActivity.class);
                                        intent.putExtra("editIndex", "1");
                                        startActivity(intent);
                                    }
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

    private ResultBean userBean, configBean;

    /**
     * 用户信息
     */
    private void userInfo() {
        Map<String, Object> param = new HashMap<>();
        OkHttpHelper.getInstance().post_json(this, Url.userInfo, param, new SpotsCallBack<ResultBean>(this) {

            @Override
            public void onSuccess(Response response, ResultBean resultBean) {
                tvName.setText(resultBean.data.name);
                GlideUtil.setImag(Zhaq2Activity.this, resultBean.data.qrcode2, ivQrcode);
                GlideUtil.setImag(Zhaq2Activity.this, resultBean.data.icon, riIcon);
                tvIdno.setText("ID:" + resultBean.data.idno);
                etName.setText(resultBean.data.acount);
//                etPsw.setText(resultBean.data.pwd);
                userBean = resultBean;
                isHasPassword = !StringUtil.isEmpty(resultBean.data.paypwd);
                if (!isHasPassword && commonTipPop ==null){
                    commonTipPop=showGuidePasswordTip(Zhaq2Activity.this);
                }
            }

            @Override
            public void onError(Response response, int code, Exception e) {
            }
        });
    }

    private void initViews() {
        myToolView = findViewById(R.id.myToolView);
        mIvMyBack = findViewById(R.id.mIvMyBack);
        tvMyTitle = findViewById(R.id.tvMyTitle);
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