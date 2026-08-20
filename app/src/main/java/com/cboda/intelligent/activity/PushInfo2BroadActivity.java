package com.cboda.intelligent.activity;

import static com.blankj.utilcode.util.SizeUtils.dp2px;
import static com.cboda.baselibrary.utils.ELogUtilKt.eLogD;
import static com.cboda.baselibrary.utils.GlideUtilsKt.loadImg;
import static com.cboda.baselibrary.utils.MMKVBroadUtils.APP_TOKEN;
import static com.cboda.select.picture.config.PictureConfig.CHOOSE_REQUEST;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.cboda.baselibrary.network.config.NetworkBroadConfig;
import com.cboda.baselibrary.utils.HttpsBroadConfig;
import com.cboda.baselibrary.utils.MMKVBroadUtils;
import com.cboda.baselibrary.utils.OnSingleClickBroadListener;
import com.blankj.utilcode.util.BarUtils;
import com.cboda.chatui.viewmodel.MessageViewBroadModel;
import com.cboda.intelligent.viewmodel.LoginViewBroadModel;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.cboda.select.picture.basic.PictureSelector;
import com.cboda.select.picture.config.SelectMimeType;
import com.cboda.select.picture.entity.LocalMedia;
import com.cboda.intelligent.R;
import com.cboda.intelligent.base.BaseSimpleBroadActivity;
import com.cboda.intelligent.bean.EventBusBroadBean;
import com.cboda.intelligent.bean.ResultBroadBean;
import com.cboda.intelligent.configuration.AppBroadConsts;
import com.cboda.intelligent.dialog.NormalBroadDialog;
import com.cboda.intelligent.utils.BaseBroadCallback;
import com.cboda.intelligent.utils.OkHttpBroadHelper;
import com.cboda.intelligent.utils.SpotsCallBroadBack;
import com.cboda.intelligent.net.BroadUrl;
import com.cboda.intelligent.utils.BitmapBroadUtils1;
import com.cboda.intelligent.utils.GlideBroadBroadEngine;
import com.cboda.intelligent.utils.GlideBroadUtil;
import com.cboda.intelligent.utils.ListBroadUtil;
import com.cboda.intelligent.utils.SharePrefBroadBroadUtil;
import com.cboda.intelligent.utils.StringBroadUtil;
import com.cboda.intelligent.utils.ToastBroadUtil;
import com.cboda.intelligent.utils.AppBroadUtils;
import com.king.zxing.util.CodeUtils;
import com.makeramen.roundedimageview.RoundedImageView;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import okhttp3.Request;
import okhttp3.Response;
import top.zibin.luban.Luban;

public class PushInfo2BroadActivity extends BaseSimpleBroadActivity implements View.OnClickListener {

    private LoginViewBroadModel loginViewModel;

    private View mIvMyBack;
    private View myToolView, space;
    private TextView tvMyTitle, requestQrCode;
    private String icon;
    private String apptoken;
    private String uid;
    private String userSign;
    private String imtoken, idno, qrcode, account, pwd;
    private String qrCodeNew = "";
    private ResultBroadBean ptbean;
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
    public void finish() {
        super.finish();
        NetworkBroadConfig.INSTANCE.updateAppToken("");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NetworkBroadConfig.INSTANCE.updateAppToken("");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userSign = getIntent().getStringExtra("userSign");
        apptoken = getIntent().getStringExtra(APP_TOKEN);
        uid = getIntent().getStringExtra("uid");
        imtoken = getIntent().getStringExtra("imtoken");
        idno = getIntent().getStringExtra("idno");
        qrcode = getIntent().getStringExtra("qrcode");
        qrCodeNew = getIntent().getStringExtra("qrCodeNew");
        account = getIntent().getStringExtra("account");
        pwd = getIntent().getStringExtra("pwd");
        initViews();
        //82环境登录隐藏二维码刷新
        if (HttpsBroadConfig.currentHostHttpUrlType != 2) {
            requestQrCode.setVisibility(View.GONE);
        } else {
            requestQrCode.setVisibility(View.VISIBLE);
        }
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
//        SharePrefBroadBroadUtil.saveString(PushInfo2BroadActivity.this, AppBroadConsts.APPTOKEN, apptoken);
        MMKVBroadUtils.Companion.saveUserAppToken(apptoken);
        setQrcodeData();

        tvName.setText(getString(R.string.user_prefix) + idno);
//        rlIcon.setOnClickListener(this);
        tvName.setOnClickListener(this);
//        llUserName.setOnClickListener(this);
        tvSave.setOnClickListener(this);

        tvLogin.setOnClickListener(this);
        requestQrCode.setOnClickListener(this);
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
        setNavBarHeightListener(new Function1<Integer, Unit>() {
            @Override
            public Unit invoke(Integer integer) {
                if (integer != null && integer > 0) {
                    ViewGroup.LayoutParams params = space.getLayoutParams();
                    params.height = integer;
                    space.setLayoutParams(params);
                    space.setVisibility(View.VISIBLE);
                }
                return null;
            }
        });

    }


    private void setQrcodeData() {
        if (!TextUtils.isEmpty(qrCodeNew)) {
            loadImg(ivQrcode, CodeUtils.createQRCode(qrCodeNew, dp2px(260f)), null);
        } else if (qrcode.endsWith(".jpg")) {
            GlideBroadUtil.setImag(PushInfo2BroadActivity.this, qrcode, ivQrcode);
        } else {
            loadImg(ivQrcode, CodeUtils.createQRCode(qrcode, dp2px(260f)), null);
        }
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
        return R.layout.activity_push_broad_info2;
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        if (view.getId() == R.id.rlIcon) {
            String[] perms = {
                    Permission.READ_MEDIA_IMAGES,
                    Permission.CAMERA};
            if (XXPermissions.isGranted(this, perms)) {
                pmsSuccess();
            } else {
                new NormalBroadDialog(this, getString(R.string.温馨提示), getString(R.string.select_image_permission_request), getString(R.string.quxiao), getString(R.string.queding), true)
                        .setOnButtonClickListener(new NormalBroadDialog.OnButtonClick() {
                            @Override
                            public void OnRightClick() {
                                XXPermissions permission = XXPermissions.with(PushInfo2BroadActivity.this).permission(perms);
                                permission.request(new OnPermissionCallback() {
                                    @Override
                                    public void onGranted(@NonNull List<String> permissions, boolean allGranted) {
                                        pmsSuccess();
                                    }

                                    @Override
                                    public void onDenied(@NonNull List<String> permissions, boolean doNotAskAgain) {
                                        OnPermissionCallback.super.onDenied(permissions, doNotAskAgain);
                                        ToastBroadUtil.show(getString(R.string.权限被拒绝));
                                    }
                                });
                            }

                            @Override
                            public void OnLeftClick() {
                            }
                        }).show();
            }
        } else if (view.getId() == R.id.tvName) {
            intent = new Intent(this, NickNameBroadActivity.class);
            intent.putExtra("name", tvName.getText().toString());
            startActivityForResult(intent, 111);
        } else if (view.getId() == R.id.tvLogin) {
            wanshan();
        } else if (view.getId() == R.id.tvSave) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                String[] perms = {
                        Permission.READ_MEDIA_IMAGES};
                if (XXPermissions.isGranted(this, perms)) {
                    goNest();

                } else {
                    new NormalBroadDialog(this, this.getString(R.string.温馨提示), getString(R.string.save_image_permission_request), getString(R.string.quxiao), getString(R.string.queding), true)
                            .setOnButtonClickListener(new NormalBroadDialog.OnButtonClick() {
                                @Override
                                public void OnRightClick() {
                                    XXPermissions permission = XXPermissions.with(PushInfo2BroadActivity.this).permission(perms);
                                    permission.request(new OnPermissionCallback() {
                                        @Override
                                        public void onGranted(@NonNull List<String> permissions, boolean allGranted) {
                                            goNest();

                                        }

                                        @Override
                                        public void onDenied(@NonNull List<String> permissions, boolean doNotAskAgain) {
                                            OnPermissionCallback.super.onDenied(permissions, doNotAskAgain);
                                            ToastBroadUtil.show(getString(R.string.权限被拒绝));
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
                    new NormalBroadDialog(this, getString(R.string.温馨提示), getString(R.string.save_image_permission_request), getString(R.string.quxiao), getString(R.string.queding), true)
                            .setOnButtonClickListener(new NormalBroadDialog.OnButtonClick() {
                                @Override
                                public void OnRightClick() {
                                    XXPermissions permission = XXPermissions.with(PushInfo2BroadActivity.this).permission(perms1);
                                    permission.request(new OnPermissionCallback() {
                                        @Override
                                        public void onGranted(@NonNull List<String> permissions, boolean allGranted) {
                                            goNest();
                                        }

                                        @Override
                                        public void onDenied(@NonNull List<String> permissions, boolean doNotAskAgain) {
                                            OnPermissionCallback.super.onDenied(permissions, doNotAskAgain);
                                            ToastBroadUtil.show(getString(R.string.权限被拒绝));
                                        }
                                    });
                                }

                                @Override
                                public void OnLeftClick() {
                                }
                            }).show();
                }
            }
        } else if (view.getId() == R.id.requestQrCode) {
            if (!TextUtils.isEmpty(apptoken)) {
                NetworkBroadConfig.INSTANCE.updateAppToken(apptoken);
            }
            loginViewModel.resetQrCode().observe(this, s -> {
                NetworkBroadConfig.INSTANCE.updateAppToken("");
                if (null != s && !TextUtils.isEmpty(s)) {
                    if (!s.endsWith(".jpg")) {
                        qrCodeNew = s;
                    }
                    qrcode = s;
                    setQrcodeData();
                }
            });
        }
//        switch (view.getId()) {
//            case R.id.rlIcon://头像
//
//                break;
//            case R.id.tvName://昵称
//
//                break;
////            case R.id.llUserName://用户名
////                intent = new Intent(this, UserNameBroadActivity.class);
////                intent.putExtra("name",tvUserName.getText().toString());
////                startActivityForResult(intent,222);
////                break;
//            case R.id.tvLogin://保存
////                if (StringBroadUtil.isEmpty(icon)) {
////                    ToastBroadUtil.show(getString(R.string.请上传头像));
////                    return;
////                }
////                if (StringBroadUtil.isEmpty(tvName.getText().toString())) {
////                    ToastBroadUtil.show(getString(R.string.请输入昵称));
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
        requestQrCode.setVisibility(View.GONE);
        BitmapBroadUtils1.saveBitmap2(BitmapBroadUtils1.convertMeasureBitmap(mViewRootBg), this);
        ToastBroadUtil.show(getString(R.string.tipSaveSuccess));

        EventBusBroadBean eventBusBean = new EventBusBroadBean();
        eventBusBean.msg = getString(R.string.login_success);
        EventBus.getDefault().post(eventBusBean);
        MMKVBroadUtils.Companion.saveSqlStrData(MMKVBroadUtils.OLD_QR_CODE, "");
        AppBroadUtils.Companion.imLogin(PushInfo2BroadActivity.this, apptoken, uid, imtoken, "PushInfo2BroadActivity");
    }

    /**
     * 修改资料
     */
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
        if (!StringBroadUtil.isEmpty(icon))
            param.put("icon", icon);
        param.put("name", tvName.getText().toString());
        param.put("account", etName.getText().toString());
        param.put("pwd", etPsw.getText().toString());

        OkHttpBroadHelper.getInstance().post_json(this, BroadUrl.wanshan, param, new SpotsCallBroadBack<ResultBroadBean>(this) {
            @Override
            public void onSuccess(Response response, ResultBroadBean resultBean) {

//                if (ptbean != null && !TextUtils.isEmpty(ptbean.data.regInfo)) {

//                    ZhuceHuanyingyuBroadHolder zhuceHuanyingyuHolder = new ZhuceHuanyingyuBroadHolder(PushInfoBroadActivity.this);
//                    zhuceHuanyingyuHolder.setOnConfimCallBack(new ZhuceHuanyingyuBroadHolder.OnConfimCallBack() {
//                        @Override
//                        public void onClickConfim(boolean isConfim) {
//                            if (isConfim) {
                SharePrefBroadBroadUtil.saveString(PushInfo2BroadActivity.this, AppBroadConsts.UID, uid);
                EventBusBroadBean eventBusBean = new EventBusBroadBean();
                eventBusBean.msg = getString(R.string.login_success);
                EventBus.getDefault().post(eventBusBean);
                AppBroadUtils.Companion.imLogin(PushInfo2BroadActivity.this, apptoken, uid, imtoken, "PushInfo2BroadActivity");
//                            }
//                        }
//                    });
//                    zhuceHuanyingyuHolder.show(ptbean.data.regInfo);

//                } else {
//                    SharePrefBroadBroadUtil.saveString(PushInfoBroadActivity.this, AppBroadConsts.UID, uid);
//                    EventBusBroadBean eventBusBean = new EventBusBroadBean();
//                    eventBusBean.msg = "登录成功";
//                    EventBus.getDefault().post(eventBusBean);
//                    AppBroadUtil.Companion.imLogin(PushInfoBroadActivity.this, apptoken, userSign, uid, imtoken);
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
                if (!StringBroadUtil.isEmpty(selectList.get(i).getRealPath()))
                    photoLists.add(selectList.get(i).getRealPath());
                else if (!StringBroadUtil.isEmpty(selectList.get(i).getSandboxPath()))
                    photoLists.add(selectList.get(i).getSandboxPath());
                else if (!StringBroadUtil.isEmpty(selectList.get(i).getPath()))
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
        if (!ListBroadUtil.isEmpty(file))
            files.put("files", file);
        OkHttpBroadHelper.getInstance().post_file(this, BroadUrl.uploadFiles, files, new SpotsCallBroadBack<ResultBroadBean>(this) {
            @Override
            public void onSuccess(Response response, ResultBroadBean resultBean) {
                icon = resultBean.urls.get(0);
                GlideBroadUtil.setImag(PushInfo2BroadActivity.this, icon, riIcon);
            }

            @Override
            public void onError(Response response, int code, Exception e) {
            }
        });
    }

    public void pmsSuccess() {
        PictureSelector.create(this)//在Fragment中使用则是 fragment.this
                .openGallery(SelectMimeType.ofImage())//全部.PictureMimeType.ofAll()、图片.ofImage()、视频.ofVideo()、音频.ofAudio()
                .setImageEngine(GlideBroadBroadEngine.createGlideEngine())
                .setMaxSelectNum(1)
                .setMinSelectNum(1)
                .setImageSpanCount(3)// 每行显示个数 int
                .isDisplayCamera(true)// 是否显示拍照按钮 true or false
                .forResult(CHOOSE_REQUEST);//结果回调onActivityResult code
    }

    private void initViews() {
        requestQrCode = findViewById(R.id.requestQrCode);
        space = findViewById(R.id.space);
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
        loginViewModel = new ViewModelProvider(this).get(LoginViewBroadModel.class);
    }

    @Override
    protected boolean fitsSystemWindows() {
        return true;
    }
}