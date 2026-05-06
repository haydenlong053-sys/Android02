package com.cboda.intelligent.activity;

import static com.cboda.baselibrary.utils.GsonUtilsKt.toJson2;
import static com.cboda.intelligent.utils.ViewExpandUtilKt.isEthWalletLinkQrCode;
import static com.cboda.intelligent.utils.ViewExpandUtilKt.showToastMain;
import static com.cboda.select.picture.config.PictureConfig.CHOOSE_REQUEST;
import static com.cboda.intelligent.utils.ViewExpandUtilKt.isTransferLinkQrCode;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.cboda.baselibrary.bean.LoginBean;
import com.cboda.baselibrary.event.AppBroadEvent;
import com.cboda.baselibrary.event.AppEventBroadCode;
import com.cboda.baselibrary.utils.DeviceBroadUtil;
import com.cboda.baselibrary.utils.HttpsBroadConfig;
import com.cboda.chatui.group.GroupViewBroadModel;
import com.cboda.intelligent.utils.GsonBroadUtil;
import com.cboda.intelligent.viewmodel.LoginViewBroadModel;
import com.google.zxing.Result;
import com.google.zxing.client.result.ParsedResult;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.cboda.select.picture.basic.PictureSelector;
import com.cboda.select.picture.config.SelectMimeType;
import com.cboda.select.picture.entity.LocalMedia;
import com.cboda.intelligent.R;
import com.cboda.intelligent.base.BaseSimpleBroadActivity;
import com.cboda.intelligent.bean.ResultBroadBean;
import com.cboda.intelligent.configuration.ScanJumpBroadEnum;
import com.cboda.intelligent.dialog.NormalBroadDialog;
import com.cboda.intelligent.utils.OkHttpBroadHelper;
import com.cboda.intelligent.utils.SpotsCallBroadBack;
import com.cboda.intelligent.utils.GlideBroadBroadEngine;
import com.cboda.intelligent.utils.StringBroadUtil;
import com.cboda.intelligent.utils.ToastBroadUtil;
import com.cboda.intelligent.utils.AppBroadUtils;
import com.cboda.intelligent.utils.JumpBroadUtils;
import com.cboda.intelligent.utils.QRCodeParseBroadUtils;
import com.mylhyl.zxing.scanner.OnScannerCompletionListener;
import com.mylhyl.zxing.scanner.ScannerView;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cboda.chatui.ChatManagerBroadHolder;

import okhttp3.Response;

public class ScanBroadActivity extends BaseSimpleBroadActivity implements View.OnClickListener {

    private boolean isopen = false;
    private ScannerView scanner;
    private View viHead;
    private RelativeLayout rlFinish;
    private TextView tvXiangce;
    private TextView tvZhaoming;
    private int jumpSource = 0;
    //    private String eqno;
    private LoginViewBroadModel loginViewBroadModel;

    @Override
    public boolean showOrHideTitleLayout() {
        return false;
    }

    @Override
    protected boolean transparentStatus() {
        return true;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_broad_scan;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        jumpSource = getIntent().getIntExtra("jumpSource", 0);
//        eqno = DeviceBroadUtil.INSTANCE.getAndroidId(this);
        loginViewBroadModel = new ViewModelProvider(this).get(LoginViewBroadModel.class);
        initViews();
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height",
                "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        LinearLayout.LayoutParams linearParams = (LinearLayout.LayoutParams) viHead.getLayoutParams(); //取控件textView当前的布局参数
        linearParams.height = result;// 控件的高强制设成20
        viHead.setLayoutParams(linearParams);
        String[] perms = {
                Permission.CAMERA};
        if (XXPermissions.isGranted(this, perms)) {
            initData();
        } else {
            new NormalBroadDialog(this, getString(R.string.温馨提示), getString(R.string.scan_camera_permission_request), getString(R.string.取消), getString(R.string.确定), true)
                    .setOnButtonClickListener(new NormalBroadDialog.OnButtonClick() {
                        @Override
                        public void OnRightClick() {
//                            AppPermissionBroadUtils.INSTANCE.checkAndRequestCameraPermission(ScanBroadActivity.this, 1002, new AppPermissionBroadUtils.SimplePermissionCallback() {
//                                @Override
//                                public void onGranted() {
//                                    initData();
//
//                                }
//
//                                @Override
//                                public void onDenied(boolean neverAskAgain) {
//                                    ToastBroadUtil.show("相机权限被永久拒绝，请到设置中手动开启");
//                                    finish();
//                                }
//                            });
//                            AppPermissionBroadUtils.INSTANCE.requestPermissions(ScanBroadActivity.this,perms,1002);
                            XXPermissions permission = XXPermissions.with(ScanBroadActivity.this).permission(perms);
                            permission.request(new OnPermissionCallback() {
                                @Override
                                public void onGranted(@NonNull List<String> permissions, boolean allGranted) {
                                    initData();
                                }

                                @Override
                                public void onDenied(@NonNull List<String> permissions, boolean doNotAskAgain) {
                                    OnPermissionCallback.super.onDenied(permissions, doNotAskAgain);
                                    ToastBroadUtil.show(getString(R.string.权限被拒绝));
//                                    finish();
                                }
                            });
                        }

                        @Override
                        public void OnLeftClick() {
                            finish();
                        }
                    }).show();
        }

        tvZhaoming.setOnClickListener(this);
        rlFinish.setOnClickListener(this);
        tvXiangce.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] perms = {
                        Permission.READ_MEDIA_IMAGES,
                };
                if (XXPermissions.isGranted(ScanBroadActivity.this, perms)) {
                    pmsSuccess();
                } else {
                    new NormalBroadDialog(ScanBroadActivity.this, getString(R.string.温馨提示), getString(R.string.scan_storage_permission_request), getString(R.string.取消), getString(R.string.确定), true)
                            .setOnButtonClickListener(new NormalBroadDialog.OnButtonClick() {
                                @Override
                                public void OnRightClick() {
//                                    AppPermissionBroadUtils.INSTANCE.checkAndRequestStoragePermission(ScanBroadActivity.this,1002,
//                                            new AppPermissionBroadUtils.SimplePermissionCallback(){
//
//                                                @Override
//                                                public void onDenied(boolean neverAskAgain) {
//                                                    ToastBroadUtil.show("存储权限被永久拒绝，请到设置中手动开启");
//                                                }
//
//                                                @Override
//                                                public void onGranted() {
//                                                    pmsSuccess();
//
//                                                }
//                                            }
//                                    );
                                    XXPermissions permission = XXPermissions.with(ScanBroadActivity.this).permission(perms);
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
                    parsePhoto(photoLists.get(0));
                }
            }
        }
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

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.rlFinish) {
            finish();
        }
        if (view.getId() == R.id.tvZhaoming) {
            isopen = !isopen;
            scanner.toggleLight(isopen);
        }

    }


    /**
     * 启动线程解析二维码图片
     *
     * @param path
     */
    private void parsePhoto(String path) {
        //启动线程完成图片扫码
        new QrCodeAsyncTask(ScanBroadActivity.this, path).execute(path);
    }

    private void initViews() {
        scanner = findViewById(R.id.scanner);
        viHead = findViewById(R.id.viHead);
        rlFinish = findViewById(R.id.rlFinish);
        tvXiangce = findViewById(R.id.tvXiangce);
        tvZhaoming = findViewById(R.id.tvZhaoming);
    }

    /**
     * AsyncTask 静态内部类，防止内存泄漏
     */
    class QrCodeAsyncTask extends AsyncTask<String, Integer, String> {
        private WeakReference<Activity> mWeakReference;
        private String path;

        public QrCodeAsyncTask(Activity activity, String path) {
            mWeakReference = new WeakReference<>(activity);
            this.path = path;
        }

        @Override
        protected String doInBackground(String... strings) {
            // 解析二维码/条码
            return QRCodeParseBroadUtils.syncDecodeQRCode(path);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //识别出图片二维码/条码，内容为s
            handleQrCode(s);
        }
    }

    /**
     * 处理图片二维码解析的数据
     *
     * @param s
     */
    public void handleQrCode(String s) {
        if (null == s) {
            ToastBroadUtil.show(getString(R.string.scan_image_qr_code_unrecognizable));
            restartScan();
        } else {
            if (jumpSource == ScanJumpBroadEnum.ETH_TRANSFER.getSourceType() && isEthWalletLinkQrCode(s)) {
                Intent intent = new Intent();
                intent.putExtra("scan_result", s);
                setResult(RESULT_OK, intent);
                finish();
            } else if (jumpSource != ScanJumpBroadEnum.None.getSourceType() && jumpSource != ScanJumpBroadEnum.Login.getSourceType() && isTransferLinkQrCode(s)) {
                Intent intent = new Intent(this, TransferBroadActivity.class);
                intent.putExtra("data", s);
                startActivity(intent);
                finish();
            } else {
                handleResult(s);
            }

        }
    }

    private void handleResult(String con) {
        try {
            String[] result = con.split("-");
            if (result.length < 2) {
                ToastBroadUtil.show(getString(R.string.scan_invalid_qr_code));
                restartScan();
                return;
            }
            String linkId = result[1];//用户id or 群id
            switch (result[0]) {
                case "1"://用户
                    if (jumpSource == ScanJumpBroadEnum.Login.getSourceType()) {
                        ToastBroadUtil.show(getString(R.string.scan_invalid_qr_code_type));
                        finish();
                        return;
                    }
                    Map<String, Object> param = new HashMap<>();
                    param.put("id", linkId);
                    OkHttpBroadHelper.getInstance().post_json(ScanBroadActivity.this, HttpsBroadConfig.INSTANCE.getAppBaseUrl() + "/duiInfo", param, new SpotsCallBroadBack<ResultBroadBean>(ScanBroadActivity.this) {

                        @Override
                        public void onSuccess(Response response, ResultBroadBean commonResultBean) {
                            if (commonResultBean != null && commonResultBean.data != null && "1".equals(commonResultBean.data.isFri)) {
                                JumpBroadUtils.Companion.startHaoYouAddActivity(ScanBroadActivity.this, linkId, "3");
                            } else {
                                JumpBroadUtils.Companion.startHaoYouAddActivity(ScanBroadActivity.this, linkId, "3");
                            }
                            finish();
                        }

                        @Override
                        public void onError(Response response, int code, Exception e) {
                            ToastBroadUtil.show(getString(R.string.scan_network_request_failed));
                            restartScan();
                        }
                    });
                    break;
                case "2"://群
                    if (jumpSource == ScanJumpBroadEnum.Login.getSourceType()) {
                        ToastBroadUtil.show(getString(R.string.scan_invalid_qr_code_type));
                        finish();
                        return;
                    }
                    Map<String, Object> param1 = new HashMap<>();
                    param1.put("id", linkId);
                    OkHttpBroadHelper.getInstance().post_json(ScanBroadActivity.this, HttpsBroadConfig.INSTANCE.getAppBaseUrl() + "/groupScan", param1, new SpotsCallBroadBack<ResultBroadBean>(ScanBroadActivity.this) {

                        @Override
                        public void onSuccess(Response response, ResultBroadBean commonResultBean) {
                            if (commonResultBean != null && !TextUtils.isEmpty(commonResultBean.groupId)) {
//                                        Bundle param = new Bundle();
//                                        param.putInt(
//                                                TUIConstants.TUIChat.CHAT_TYPE,
//                                                V2TIMConversation.V2TIM_GROUP
//                                        );
//                                        param.putString(TUIConstants.TUIChat.CHAT_ID, commonResultBean.groupId);
//                                        TUICore.startActivity(TUIConstants.TUIChat.GROUP_CHAT_ACTIVITY_NAME, param);
                                AppBroadUtils.Companion.goChat(ScanBroadActivity.this, commonResultBean.groupId, "1");
                                finish();
                            } else {
                                ToastBroadUtil.show(getString(R.string.scan_applied_waiting_group_owner));
                                finish();
                            }
                        }

                        @Override
                        public void onError(Response response, int code, Exception e) {
                            ToastBroadUtil.show(getString(R.string.scan_network_request_failed));
                            restartScan();
                        }
                    });
                    break;
                case "3"://扫描登录
                    idLogin(linkId);
                    break;
                default:
                    ToastBroadUtil.show(getString(R.string.scan_invalid_qr_code_type));
                    restartScan();
                    break;
            }
        } catch (Exception e) {
            ToastBroadUtil.show(getString(R.string.scan_invalid_qr_code));
            restartScan();
        }

    }

    private void idLogin(String linkId) {
        if (jumpSource != ScanJumpBroadEnum.Login.getSourceType()) {
            ToastBroadUtil.show(getString(R.string.mine_change_machine_tip));
            restartScan();
            return;
        }
        //82环境使用老登录
//        if (HttpsBroadConfig.currentHostHttpUrlType == HttpsBroadConfig.RELEASE_TYPE) {
        if (HttpsBroadConfig.currentHostHttpUrlType < 1000) {
            Map<String, Object> param = new HashMap<>();
            param.put("id", linkId);
            param.put("clientId", ChatManagerBroadHolder.gChatManager.getClientId());
            param.put("type", "2");
            param.put("model", DeviceBroadUtil.INSTANCE.getDeviceBrand() + " " + DeviceBroadUtil.INSTANCE.getDeviceModel());
            OkHttpBroadHelper.getInstance().post_json(ScanBroadActivity.this, HttpsBroadConfig.INSTANCE.getAppBaseUrl() + "/idLogin", param, new SpotsCallBroadBack<ResultBroadBean>(ScanBroadActivity.this) {

                @Override
                public void onSuccess(Response response, ResultBroadBean resultBean) {
                    if (jumpSource == ScanJumpBroadEnum.Login.getSourceType() && !TextUtils.equals(resultBean.eqno, DeviceBroadUtil.INSTANCE.getAndroidId(ScanBroadActivity.this, true))) {
                        EventBus.getDefault().post(new AppBroadEvent<>(AppEventBroadCode.EVENT_NEW_MACHINE_CHECK, resultBean));
                    } else {
                        AppBroadUtils.Companion.imLogin(ScanBroadActivity.this, resultBean.apptoken, resultBean.userId, resultBean.imtoken, "ScanBroadActivity");
                    }
                    finish();
                }

                @Override
                public void onError(Response response, int code, Exception e) {
                    ToastBroadUtil.show(getString(R.string.scan_login_request_failed));
                    restartScan();
                }
            });
        } else {
            loginViewBroadModel.getScanLoginDetail(linkId).observe(this, loginBean -> {
                if (null != loginBean) {
//                loginBean.setType("scanLogin");
                    loginBean.setLinkId(linkId);
                    loginBean.setHaveOldDeviceCode(true);
                    Intent newIntent = new Intent(ScanBroadActivity.this, LoginVerificationActivity.class);
                    newIntent.putExtra("loginBean", toJson2(loginBean));
                    startActivity(newIntent);
                    finish();
                } else {
                    restartScan();
                    showToastMain(R.string.scan_login_request_failed);
                }
            });
        }
    }


    private void initData() {
        scanner.setDrawTextColor(Color.parseColor("#00000000"));
        scanner.isScanFullScreen(true);
        scanner.setMediaResId(R.raw.beep);//设置扫描成功的声音
        scanner.setOnScannerCompletionListener(new OnScannerCompletionListener() {
            @Override
            public void onScannerCompletion(Result rawResult, ParsedResult parsedResult, Bitmap barcode) {
//                handleQrCode(rawResult.getText());
                if (jumpSource == ScanJumpBroadEnum.ETH_TRANSFER.getSourceType() && isEthWalletLinkQrCode(rawResult.getText())) {
                    Intent intent = new Intent();
                    intent.putExtra("scan_result", rawResult.getText());
                    setResult(RESULT_OK, intent);
                    finish();
                    return;
                }
                if (jumpSource != ScanJumpBroadEnum.None.getSourceType() && jumpSource != ScanJumpBroadEnum.Login.getSourceType() && isTransferLinkQrCode(rawResult.getText())) {
                    Intent intent = new Intent(ScanBroadActivity.this, TransferBroadActivity.class);
                    intent.putExtra("data", rawResult.getText());
                    startActivity(intent);
                    finish();
                    return;
                }
                if (!TextUtils.isEmpty(rawResult.getText()) && rawResult.getText().contains("-")) {
                    String[] result = rawResult.getText().split("-");
                    if (result.length < 2) {
                        ToastBroadUtil.show(getString(R.string.scan_invalid_qr_code));
                        restartScan();
                        return;
                    }
                    String linkId = result[1];//用户id or 群id
                    switch (result[0]) {
                        case "1"://用户
                            if (jumpSource == ScanJumpBroadEnum.Login.getSourceType()) {
                                ToastBroadUtil.show(getString(R.string.scan_invalid_qr_code_type));
                                finish();
                                return;
                            }
                            Map<String, Object> param = new HashMap<>();
                            param.put("id", linkId);
                            OkHttpBroadHelper.getInstance().post_json(ScanBroadActivity.this, HttpsBroadConfig.INSTANCE.getAppBaseUrl() + "/duiInfo", param, new SpotsCallBroadBack<ResultBroadBean>(ScanBroadActivity.this) {

                                @Override
                                public void onSuccess(Response response, ResultBroadBean commonResultBean) {
                                    if (commonResultBean != null && commonResultBean.data != null && "1".equals(commonResultBean.data.isFri)) {
                                        JumpBroadUtils.Companion.startHaoYouAddActivity(ScanBroadActivity.this, linkId, "3");
                                    } else {
                                        JumpBroadUtils.Companion.startHaoYouAddActivity(ScanBroadActivity.this, linkId, "3");
                                    }
                                    finish();

                                }

                                @Override
                                public void onError(Response response, int code, Exception e) {
                                    ToastBroadUtil.show(getString(R.string.scan_network_request_failed));
                                    restartScan();
                                }
                            });
                            break;
                        case "2"://群
                            if (jumpSource == ScanJumpBroadEnum.Login.getSourceType()) {
                                ToastBroadUtil.show(getString(R.string.scan_invalid_qr_code_type));
                                finish();
                                return;
                            }
                            Map<String, Object> param1 = new HashMap<>();
                            param1.put("id", linkId);
                            OkHttpBroadHelper.getInstance().post_json(ScanBroadActivity.this, HttpsBroadConfig.INSTANCE.getAppBaseUrl() + "/groupScan", param1, new SpotsCallBroadBack<ResultBroadBean>(ScanBroadActivity.this) {

                                @Override
                                public void onSuccess(Response response, ResultBroadBean commonResultBean) {
                                    if (commonResultBean != null && !TextUtils.isEmpty(commonResultBean.groupId)) {
//                                        Bundle param = new Bundle();
//                                        param.putInt(
//                                                TUIConstants.TUIChat.CHAT_TYPE,
//                                                V2TIMConversation.V2TIM_GROUP
//                                        );
//                                        param.putString(TUIConstants.TUIChat.CHAT_ID, commonResultBean.groupId);
//                                        TUICore.startActivity(TUIConstants.TUIChat.GROUP_CHAT_ACTIVITY_NAME, param);
                                        AppBroadUtils.Companion.goChat(ScanBroadActivity.this, commonResultBean.groupId, "1");
                                        finish();
                                    } else {
                                        ToastBroadUtil.show(getString(R.string.scan_applied_waiting_group_owner));
                                        finish();
                                    }
                                }

                                @Override
                                public void onError(Response response, int code, Exception e) {
                                    ToastBroadUtil.show(getString(R.string.scan_network_request_failed));
                                    restartScan();
                                }
                            });
                            break;
                        case "3"://扫描登录
                            idLogin(linkId);
                            break;
                        default:
                            ToastBroadUtil.show(getString(R.string.scan_invalid_qr_code_type));
                            restartScan();
                            break;
                    }
                } else {
                    // 二维码格式不正确，继续扫描
                    restartScan();
                }

            }
        });
    }

    /**
     * 重新启动扫描器
     */
    private void restartScan() {
        if (scanner != null) {
            scanner.onPause();
            scanner.onResume();
        }
    }

    @Override
    public void onResume() {
        scanner.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        scanner.onPause();
        super.onPause();
    }

    @Override
    protected boolean fitsSystemWindows() {
        return true;
    }
}