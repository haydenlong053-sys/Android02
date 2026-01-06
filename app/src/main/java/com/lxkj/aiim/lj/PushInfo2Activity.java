package com.lxkj.aiim.lj;

import static com.luck.picture.lib.config.PictureConfig.CHOOSE_REQUEST;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cboda.baselibrary.utils.OnSingleClickBroadListener;
import com.blankj.utilcode.util.BarUtils;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.luck.picture.lib.basic.PictureSelector;
import com.luck.picture.lib.config.SelectMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.cboda.intelligent.R;
import com.lxkj.aiim.base.BaseSimpleActivity;
import com.lxkj.aiim.lj.bean.EventBusBean;
import com.lxkj.aiim.lj.bean.ResultBean;
import com.lxkj.aiim.lj.configuration.AppConsts;
import com.lxkj.aiim.lj.dialogfra.NormalDialog;
import com.lxkj.aiim.lj.http.BaseCallback;
import com.lxkj.aiim.lj.http.OkHttpHelper;
import com.lxkj.aiim.lj.http.SpotsCallBack;
import com.lxkj.aiim.lj.http.Url;
import com.lxkj.aiim.lj.utils.BitmapUtils1;
import com.lxkj.aiim.lj.utils.GlideBroadEngine;
import com.lxkj.aiim.lj.utils.GlideUtil;
import com.lxkj.aiim.lj.utils.ListUtil;
import com.lxkj.aiim.lj.utils.SharePrefBroadUtil;
import com.lxkj.aiim.lj.utils.StringUtil;
import com.lxkj.aiim.lj.utils.ToastUtil;
import com.lxkj.aiim.utils.AppUtil;
import com.makeramen.roundedimageview.RoundedImageView;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Request;
import okhttp3.Response;
import top.zibin.luban.Luban;

public class PushInfo2Activity extends BaseSimpleActivity implements View.OnClickListener {

    private View mIvMyBack;
    private View myToolView;
    private TextView tvMyTitle;
    private String icon;
    private String apptoken;
    private String uid;
    private String userSign;
    private String imtoken, idno, qrcode, account, pwd;
    private ResultBean ptbean;
    private RoundedImageView riIcon;
    private RelativeLayout rlIcon;
    private TextView tvName;
    private TextView tvIdno;
    private RoundedImageView ivQrcode;
    private View llSave;
    private TextView tvRegInfo;
    private EditText etName;
    private EditText etPsw;
    private TextView tvSave;
    private TextView tvLogin;
    private View mTvShowQrcode;
    private View mIvCloseEyes;
    private View mViewDash;
    private View mIvBlur;
    private View mViewRootBg;

    @Override
    public boolean showOrHideTitleLayout() {
        return false;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userSign = getIntent().getStringExtra("userSign");
        apptoken = getIntent().getStringExtra("apptoken");
        uid = getIntent().getStringExtra("uid");
        imtoken = getIntent().getStringExtra("imtoken");
        idno = getIntent().getStringExtra("idno");
        qrcode = getIntent().getStringExtra("qrcode");
        account = getIntent().getStringExtra("account");
        pwd = getIntent().getStringExtra("pwd");
        initViews();
        BarUtils.addMarginTopEqualStatusBarHeight(myToolView);
        tvMyTitle.setText(getResources().getString(R.string.m_account_info));
        mIvMyBack.setOnClickListener(new OnSingleClickBroadListener() {
            @Override
            public void onSingleClick(View v) {
                finish();
            }
        });

        etName.setText(account);
        etPsw.setText(pwd);
        SharePrefBroadUtil.saveString(PushInfo2Activity.this, AppConsts.APPTOKEN, apptoken);
        GlideUtil.setImag(PushInfo2Activity.this, qrcode, ivQrcode);
        tvName.setText("用户" + idno);
//        rlIcon.setOnClickListener(this);
        tvName.setOnClickListener(this);
//        llUserName.setOnClickListener(this);
        tvSave.setOnClickListener(this);
        tvLogin.setOnClickListener(this);
        tvIdno.setText("ID:" + idno);

        mTvShowQrcode.setOnClickListener(new OnSingleClickBroadListener() {
            @Override
            public void onSingleClick(View v) {
                mIvCloseEyes.setVisibility(View.VISIBLE);
                ivQrcode.setVisibility(View.VISIBLE);
                mViewDash.setVisibility(View.VISIBLE);
                mIvBlur.setVisibility(View.GONE);
                mTvShowQrcode.setVisibility(View.GONE);

            }
        });
        mIvCloseEyes.setOnClickListener(new OnSingleClickBroadListener() {
            @Override
            public void onSingleClick(View v) {
                mIvCloseEyes.setVisibility(View.GONE);
                ivQrcode.setVisibility(View.GONE);
                mViewDash.setVisibility(View.GONE);
                mIvBlur.setVisibility(View.VISIBLE);
                mTvShowQrcode.setVisibility(View.VISIBLE);

            }
        });

        ptConfig();
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
                ptbean = resultBean;
                tvRegInfo.setText(resultBean.data.appinfo);
            }

            @Override
            public void onError(Response response, int code, Exception e) {
            }
        });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_push_info2;
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        if (view.getId() ==R.id.rlIcon){
            String[] perms = {
                    Permission.READ_MEDIA_IMAGES,
                    Permission.CAMERA};
            if (XXPermissions.isGranted(this, perms)) {
                pmsSuccess();
            } else {
                new NormalDialog(this, getString(R.string.温馨提示), "使用选择图片功能需要相机、存储权限，是否请求权限？", "取消", "确定", true)
                        .setOnButtonClickListener(new NormalDialog.OnButtonClick() {
                            @Override
                            public void OnRightClick() {
                                XXPermissions permission = XXPermissions.with(PushInfo2Activity.this).permission(perms);
                                permission.request(new OnPermissionCallback() {
                                    @Override
                                    public void onGranted(@NonNull List<String> permissions, boolean allGranted) {
                                        pmsSuccess();
                                    }

                                    @Override
                                    public void onDenied(@NonNull List<String> permissions, boolean doNotAskAgain) {
                                        OnPermissionCallback.super.onDenied(permissions, doNotAskAgain);
                                        ToastUtil.show(getString(R.string.权限被拒绝));
                                    }
                                });
                            }

                            @Override
                            public void OnLeftClick() {
                            }
                        }).show();
            }
        }else  if (view.getId() ==R.id.tvName){
            intent = new Intent(this, NickNameActivity.class);
            intent.putExtra("name", tvName.getText().toString());
            startActivityForResult(intent, 111);
        }else  if (view.getId() ==R.id.tvLogin){
            wanshan();
        }else  if (view.getId() ==R.id.tvSave){

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                String[] perms = {
                        Permission.READ_MEDIA_IMAGES};
                if (XXPermissions.isGranted(this, perms)) {
                    goNest();

                } else {
                    new NormalDialog(this, this.getString(R.string.温馨提示), "保存图片功能需要存储权限，是否请求权限？", "取消", "确定", true)
                            .setOnButtonClickListener(new NormalDialog.OnButtonClick() {
                                @Override
                                public void OnRightClick() {
                                    XXPermissions permission = XXPermissions.with(PushInfo2Activity.this).permission(perms);
                                    permission.request(new OnPermissionCallback() {
                                        @Override
                                        public void onGranted(@NonNull List<String> permissions, boolean allGranted) {
                                           goNest();

                                        }

                                        @Override
                                        public void onDenied(@NonNull List<String> permissions, boolean doNotAskAgain) {
                                            OnPermissionCallback.super.onDenied(permissions, doNotAskAgain);
                                            ToastUtil.show(getString(R.string.权限被拒绝));
                                        }
                                    });
                                }

                                @Override
                                public void OnLeftClick() {
                                }
                            }).show();
                }

            } else {
                String[] perms1 = {
                        Permission.WRITE_EXTERNAL_STORAGE,
                };
                if (XXPermissions.isGranted(this, perms1)) {
                   goNest();
                } else {
                    new NormalDialog(this, getString(R.string.温馨提示), "保存图片功能需要存储权限，是否请求权限？", "取消", "确定", true)
                            .setOnButtonClickListener(new NormalDialog.OnButtonClick() {
                                @Override
                                public void OnRightClick() {
                                    XXPermissions permission = XXPermissions.with(PushInfo2Activity.this).permission(perms1);
                                    permission.request(new OnPermissionCallback() {
                                        @Override
                                        public void onGranted(@NonNull List<String> permissions, boolean allGranted) {
                                            goNest();
                                        }

                                        @Override
                                        public void onDenied(@NonNull List<String> permissions, boolean doNotAskAgain) {
                                            OnPermissionCallback.super.onDenied(permissions, doNotAskAgain);
                                            ToastUtil.show(getString(R.string.权限被拒绝));
                                        }
                                    });
                                }

                                @Override
                                public void OnLeftClick() {
                                }
                            }).show();
                }
            }
        }
//        switch (view.getId()) {
//            case R.id.rlIcon://头像
//
//                break;
//            case R.id.tvName://昵称
//
//                break;
////            case R.id.llUserName://用户名
////                intent = new Intent(this, UserNameActivity.class);
////                intent.putExtra("name",tvUserName.getText().toString());
////                startActivityForResult(intent,222);
////                break;
//            case R.id.tvLogin://保存
////                if (StringUtil.isEmpty(icon)) {
////                    ToastUtil.show(getString(R.string.请上传头像));
////                    return;
////                }
////                if (StringUtil.isEmpty(tvName.getText().toString())) {
////                    ToastUtil.show(getString(R.string.请输入昵称));
////                    return;
////                }
//                wanshan();
//                break;
//            case R.id.tvSave:
//
//                break;
//        }
    }

    private void goNest() {
        tvSave.setVisibility(View.GONE);
        mTvShowQrcode.setVisibility(View.GONE);
        mIvCloseEyes.setVisibility(View.GONE);
        mIvBlur.setVisibility(View.GONE);
        mViewDash.setVisibility(View.VISIBLE);
        ivQrcode.setVisibility(View.VISIBLE);
        BitmapUtils1.saveBitmap2(BitmapUtils1.convertMeasureBitmap(mViewRootBg), this);
        ToastUtil.show("保存成功");

        EventBusBean eventBusBean = new EventBusBean();
        eventBusBean.msg = "登录成功";
        EventBus.getDefault().post(eventBusBean);
        AppUtil.Companion.imLogin(PushInfo2Activity.this, apptoken, userSign, uid, imtoken);

    }

    /**
     * 修改资料
     */
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
        if (!StringUtil.isEmpty(icon))
            param.put("icon", icon);
        param.put("name", tvName.getText().toString());
        param.put("account", etName.getText().toString());
        param.put("pwd", etPsw.getText().toString());

        OkHttpHelper.getInstance().post_json(this, Url.wanshan, param, new SpotsCallBack<ResultBean>(this) {
            @Override
            public void onSuccess(Response response, ResultBean resultBean) {

//                if (ptbean != null && !TextUtils.isEmpty(ptbean.data.regInfo)) {

//                    ZhuceHuanyingyuHolder zhuceHuanyingyuHolder = new ZhuceHuanyingyuHolder(PushInfoActivity.this);
//                    zhuceHuanyingyuHolder.setOnConfimCallBack(new ZhuceHuanyingyuHolder.OnConfimCallBack() {
//                        @Override
//                        public void onClickConfim(boolean isConfim) {
//                            if (isConfim) {
                SharePrefBroadUtil.saveString(PushInfo2Activity.this, AppConsts.UID, uid);
                EventBusBean eventBusBean = new EventBusBean();
                eventBusBean.msg = "登录成功";
                EventBus.getDefault().post(eventBusBean);
                AppUtil.Companion.imLogin(PushInfo2Activity.this, apptoken, userSign, uid, imtoken);
//                            }
//                        }
//                    });
//                    zhuceHuanyingyuHolder.show(ptbean.data.regInfo);

//                } else {
//                    SharePrefBroadUtil.saveString(PushInfoActivity.this, AppConsts.UID, uid);
//                    EventBusBean eventBusBean = new EventBusBean();
//                    eventBusBean.msg = "登录成功";
//                    EventBus.getDefault().post(eventBusBean);
//                    AppUtil.Companion.imLogin(PushInfoActivity.this, apptoken, userSign, uid, imtoken);
//                }
            }

            @Override
            public void onError(Response response, int code, Exception e) {
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHOOSE_REQUEST && resultCode == RESULT_OK) {
            ArrayList<String> photoLists = new ArrayList<>();
            List<LocalMedia> selectList = PictureSelector.obtainSelectorList(data);
            for (int i = 0; i < selectList.size(); i++) {
                if (!StringUtil.isEmpty(selectList.get(i).getRealPath()))
                    photoLists.add(selectList.get(i).getRealPath());
                else if (!StringUtil.isEmpty(selectList.get(i).getSandboxPath()))
                    photoLists.add(selectList.get(i).getSandboxPath());
                else if (!StringUtil.isEmpty(selectList.get(i).getPath()))
                    photoLists.add(selectList.get(i).getPath());
            }
            if (photoLists != null && !photoLists.isEmpty()) {
                File file = new File(photoLists.get(0));//获取第一张图片
                if (file.exists()) {
                    uploadFiles(photoLists);
                }
            }
        } else if (requestCode == 111 && resultCode == 111) {
            tvName.setText(data.getStringExtra("name"));
        }
    }

    /**
     * 上传文件
     */
    private void uploadFiles(List<String> path) {
        List<String> reason = new ArrayList<>();
        List<File> file = new ArrayList<>();
        reason.addAll(path);
        try {
            file.addAll(Luban.with(this).load(reason).get());
        } catch (IOException e) {
            e.printStackTrace();
        }
        Map<String, List<File>> files = new HashMap<>();
        if (!ListUtil.isEmpty(file))
            files.put("files", file);
        OkHttpHelper.getInstance().post_file(this, Url.uploadFiles, files, new SpotsCallBack<ResultBean>(this) {
            @Override
            public void onSuccess(Response response, ResultBean resultBean) {
                icon = resultBean.urls.get(0);
                GlideUtil.setImag(PushInfo2Activity.this, icon, riIcon);
            }

            @Override
            public void onError(Response response, int code, Exception e) {
            }
        });
    }

    public void pmsSuccess() {
        PictureSelector.create(this)//在Fragment中使用则是 fragment.this
                .openGallery(SelectMimeType.ofImage())//全部.PictureMimeType.ofAll()、图片.ofImage()、视频.ofVideo()、音频.ofAudio()
                .setImageEngine(GlideBroadEngine.createGlideEngine())
                .setMaxSelectNum(1)
                .setMinSelectNum(1)
                .setImageSpanCount(3)// 每行显示个数 int
                .isDisplayCamera(true)// 是否显示拍照按钮 true or false
                .forResult(CHOOSE_REQUEST);//结果回调onActivityResult code
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
        tvSave = findViewById(R.id.tvSave);
        tvLogin = findViewById(R.id.tvLogin);

        mTvShowQrcode = findViewById(R.id.mTvShowQrcode);
        mIvCloseEyes = findViewById(R.id.mIvCloseEyes);
        mViewDash = findViewById(R.id.mViewDash);
        mIvBlur = findViewById(R.id.mIvBlur);
        mViewRootBg = findViewById(R.id.mViewRootBg);
    }
}