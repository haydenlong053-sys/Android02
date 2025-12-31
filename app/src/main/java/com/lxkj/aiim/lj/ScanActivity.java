package com.lxkj.aiim.lj;

import static com.luck.picture.lib.config.PictureConfig.CHOOSE_REQUEST;
import static com.lxkj.aiim.lj.utils.ViewExpandUtilKt.isTransferLinkQrCode;

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

import com.cboda.baselibrary.BuildConfig;
import com.cboda.baselibrary.event.AppEvent;
import com.cboda.baselibrary.event.AppEventCode;
import com.cboda.baselibrary.utils.HttpsConfig;
import com.google.zxing.Result;
import com.google.zxing.client.result.ParsedResult;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.luck.picture.lib.basic.PictureSelector;
import com.luck.picture.lib.config.SelectMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.lxkj.aiim.R;
import com.lxkj.aiim.base.BaseSimpleActivity;
import com.lxkj.aiim.lj.bean.ResultBean;
import com.lxkj.aiim.lj.configuration.ScanJumpEnum;
import com.lxkj.aiim.lj.dialogfra.NormalDialog;
import com.lxkj.aiim.lj.http.OkHttpHelper;
import com.lxkj.aiim.lj.http.SpotsCallBack;
import com.lxkj.aiim.lj.utils.GlideEngine;
import com.lxkj.aiim.lj.utils.StringUtil;
import com.lxkj.aiim.lj.utils.ToastUtil;
import com.lxkj.aiim.utils.AppUtil;
import com.lxkj.aiim.utils.JumpUtils;
import com.lxkj.aiim.utils.QRCodeParseUtils;
import com.mylhyl.zxing.scanner.OnScannerCompletionListener;
import com.mylhyl.zxing.scanner.ScannerView;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.wildfire.chat.kit.ChatManagerHolder;
import okhttp3.Response;

public class ScanActivity extends BaseSimpleActivity implements View.OnClickListener {

    private boolean isopen = false;
    private ScannerView scanner;
    private View viHead;
    private RelativeLayout rlFinish;
    private TextView tvXiangce;
    private TextView tvZhaoming;
    private int jumpSource = 0;
    private String eqno;

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
        return R.layout.activity_scan;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        jumpSource = getIntent().getIntExtra("jumpSource", 0);
        eqno = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
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
            new NormalDialog(this, getString(R.string.温馨提示), "使用扫一扫功能需要相机权限，是否请求权限？", "取消", "确定", true)
                    .setOnButtonClickListener(new NormalDialog.OnButtonClick() {
                        @Override
                        public void OnRightClick() {
//                            AppPermissionUtils.INSTANCE.checkAndRequestCameraPermission(ScanActivity.this, 1002, new AppPermissionUtils.SimplePermissionCallback() {
//                                @Override
//                                public void onGranted() {
//                                    initData();
//
//                                }
//
//                                @Override
//                                public void onDenied(boolean neverAskAgain) {
//                                    ToastUtil.show("相机权限被永久拒绝，请到设置中手动开启");
//                                    finish();
//                                }
//                            });
//                            AppPermissionUtils.INSTANCE.requestPermissions(ScanActivity.this,perms,1002);
                            XXPermissions permission = XXPermissions.with(ScanActivity.this).permission(perms);
                            permission.request(new OnPermissionCallback() {
                                @Override
                                public void onGranted(@NonNull List<String> permissions, boolean allGranted) {
                                    initData();
                                }

                                @Override
                                public void onDenied(@NonNull List<String> permissions, boolean doNotAskAgain) {
                                    OnPermissionCallback.super.onDenied(permissions, doNotAskAgain);
                                    ToastUtil.show(getString(R.string.权限被拒绝));
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
                if (XXPermissions.isGranted(ScanActivity.this, perms)) {
                    pmsSuccess();
                } else {
                    new NormalDialog(ScanActivity.this, getString(R.string.温馨提示), "使用选择存储权限，是否请求权限？", "取消", "确定", true)
                            .setOnButtonClickListener(new NormalDialog.OnButtonClick() {
                                @Override
                                public void OnRightClick() {
//                                    AppPermissionUtils.INSTANCE.checkAndRequestStoragePermission(ScanActivity.this,1002,
//                                            new AppPermissionUtils.SimplePermissionCallback(){
//
//                                                @Override
//                                                public void onDenied(boolean neverAskAgain) {
//                                                    ToastUtil.show("存储权限被永久拒绝，请到设置中手动开启");
//                                                }
//
//                                                @Override
//                                                public void onGranted() {
//                                                    pmsSuccess();
//
//                                                }
//                                            }
//                                    );
                                    XXPermissions permission = XXPermissions.with(ScanActivity.this).permission(perms);
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
                    parsePhoto(photoLists.get(0));
                }
            }
        }
    }

    public void pmsSuccess() {
        PictureSelector.create(this)//在Fragment中使用则是 fragment.this
                .openGallery(SelectMimeType.ofImage())//全部.PictureMimeType.ofAll()、图片.ofImage()、视频.ofVideo()、音频.ofAudio()
                .setImageEngine(GlideEngine.createGlideEngine())
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
        new QrCodeAsyncTask(ScanActivity.this, path).execute(path);
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
            return QRCodeParseUtils.syncDecodeQRCode(path);
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
            ToastUtil.show("该图片无法识别二维码");
            restartScan();
        } else {
            if (jumpSource != ScanJumpEnum.None.getSourceType() && jumpSource != ScanJumpEnum.Login.getSourceType() && isTransferLinkQrCode(s)) {
                Intent intent = new Intent(this, TransferActivity.class);
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
                ToastUtil.show("无效的二维码");
                restartScan();
                return;
            }
            String linkId = result[1];//用户id or 群id
            switch (result[0]) {
                case "1"://用户
                    Map<String, Object> param = new HashMap<>();
                    param.put("id", linkId);
                    OkHttpHelper.getInstance().post_json(ScanActivity.this, HttpsConfig.INSTANCE.getAppBaseUrl() + "/duiInfo", param, new SpotsCallBack<ResultBean>(ScanActivity.this) {

                        @Override
                        public void onSuccess(Response response, ResultBean commonResultBean) {
                            if (commonResultBean != null && commonResultBean.data != null && "1".equals(commonResultBean.data.isFri)) {
                                JumpUtils.Companion.startHaoYouAddActivity(ScanActivity.this, linkId, "3");
                            } else {
                                JumpUtils.Companion.startHaoYouAddActivity(ScanActivity.this, linkId, "3");
                            }
                            finish();
                        }

                        @Override
                        public void onError(Response response, int code, Exception e) {
                            ToastUtil.show("网络请求失败，请重试");
                            restartScan();
                        }
                    });
                    break;
                case "2"://群
                    Map<String, Object> param1 = new HashMap<>();
                    param1.put("id", linkId);
                    OkHttpHelper.getInstance().post_json(ScanActivity.this, HttpsConfig.INSTANCE.getAppBaseUrl() + "/groupScan", param1, new SpotsCallBack<ResultBean>(ScanActivity.this) {

                        @Override
                        public void onSuccess(Response response, ResultBean commonResultBean) {
                            if (commonResultBean != null && !TextUtils.isEmpty(commonResultBean.groupId)) {
//                                        Bundle param = new Bundle();
//                                        param.putInt(
//                                                TUIConstants.TUIChat.CHAT_TYPE,
//                                                V2TIMConversation.V2TIM_GROUP
//                                        );
//                                        param.putString(TUIConstants.TUIChat.CHAT_ID, commonResultBean.groupId);
//                                        TUICore.startActivity(TUIConstants.TUIChat.GROUP_CHAT_ACTIVITY_NAME, param);
                                AppUtil.Companion.goChat(ScanActivity.this, commonResultBean.groupId, "1");
                                finish();
                            } else {
                                ToastUtil.show("已申请，等待群主同意");
                                finish();
                            }
                        }

                        @Override
                        public void onError(Response response, int code, Exception e) {
                            ToastUtil.show("网络请求失败，请重试");
                            restartScan();
                        }
                    });
                    break;
                case "3"://扫描登录
                    idLogin(linkId);
                    break;
                default:
                    ToastUtil.show("无效的二维码类型");
                    restartScan();
                    break;
            }
        } catch (Exception e) {
            ToastUtil.show("无效的二维码");
            restartScan();
        }

    }

    private void idLogin(String linkId) {
        if (jumpSource != ScanJumpEnum.Login.getSourceType()) {
            ToastUtil.show(getString(R.string.mine_change_machine_tip));
            restartScan();
            return;
        }
        Map<String, Object> param = new HashMap<>();
        param.put("id", linkId);
        param.put("clientId", ChatManagerHolder.gChatManager.getClientId());
        param.put("type", "2");
        param.put("model", Build.BRAND + " " + Build.MODEL);
        OkHttpHelper.getInstance().post_json(ScanActivity.this, HttpsConfig.INSTANCE.getAppBaseUrl() + "/idLogin", param, new SpotsCallBack<ResultBean>(ScanActivity.this) {

            @Override
            public void onSuccess(Response response, ResultBean resultBean) {
//                if (resultBean.isws.equals("0")) {//是否已完善资料
//                    Intent intent = new Intent(ScanActivity.this, PushInfoActivity.class);
//                    intent.putExtra("apptoken", resultBean.apptoken);
//                    intent.putExtra("uid", resultBean.userId);
//                    intent.putExtra("userSign", resultBean.userSign);
//                    intent.putExtra("imtoken", resultBean.imtoken);
//                    intent.putExtra("idno", linkId);
//                    startActivity(intent);
//                } else {
                if (jumpSource == ScanJumpEnum.Login.getSourceType() && !TextUtils.equals(resultBean.eqno, eqno)) {
                    //登录扫码，比对为新设备登录
                    EventBus.getDefault().post(new AppEvent<ResultBean>(AppEventCode.EVENT_NEW_MACHINE_CHECK, resultBean));
                } else {
                    AppUtil.Companion.imLogin(ScanActivity.this, resultBean.apptoken, resultBean.userSign, resultBean.userId, resultBean.imtoken);
                }
//                }
                finish();
            }

            @Override
            public void onError(Response response, int code, Exception e) {
                ToastUtil.show("登录请求失败，请重试");
                restartScan();
            }
        });
    }


    private void initData() {
        scanner.setDrawTextColor(Color.parseColor("#00000000"));
        scanner.isScanFullScreen(true);
        scanner.setMediaResId(R.raw.beep);//设置扫描成功的声音
        scanner.setOnScannerCompletionListener(new OnScannerCompletionListener() {
            @Override
            public void onScannerCompletion(Result rawResult, ParsedResult parsedResult, Bitmap barcode) {
                if (!TextUtils.isEmpty(rawResult.getText()) && rawResult.getText().contains("-")) {
                    String[] result = rawResult.getText().split("-");
                    if (result.length < 2) {
                        ToastUtil.show("无效的二维码");
                        restartScan();
                        return;
                    }
                    String linkId = result[1];//用户id or 群id
                    switch (result[0]) {
                        case "1"://用户
                            Map<String, Object> param = new HashMap<>();
                            param.put("id", linkId);
                            OkHttpHelper.getInstance().post_json(ScanActivity.this, HttpsConfig.INSTANCE.getAppBaseUrl() + "/duiInfo", param, new SpotsCallBack<ResultBean>(ScanActivity.this) {

                                @Override
                                public void onSuccess(Response response, ResultBean commonResultBean) {
                                    if (commonResultBean != null && commonResultBean.data != null && "1".equals(commonResultBean.data.isFri)) {
                                        JumpUtils.Companion.startHaoYouAddActivity(ScanActivity.this, linkId, "3");
                                    } else {
                                        JumpUtils.Companion.startHaoYouAddActivity(ScanActivity.this, linkId, "3");
                                    }
                                    finish();

                                }

                                @Override
                                public void onError(Response response, int code, Exception e) {
                                    ToastUtil.show("网络请求失败，请重试");
                                    restartScan();
                                }
                            });
                            break;
                        case "2"://群
                            Map<String, Object> param1 = new HashMap<>();
                            param1.put("id", linkId);
                            OkHttpHelper.getInstance().post_json(ScanActivity.this, HttpsConfig.INSTANCE.getAppBaseUrl() + "/groupScan", param1, new SpotsCallBack<ResultBean>(ScanActivity.this) {

                                @Override
                                public void onSuccess(Response response, ResultBean commonResultBean) {
                                    if (commonResultBean != null && !TextUtils.isEmpty(commonResultBean.groupId)) {
//                                        Bundle param = new Bundle();
//                                        param.putInt(
//                                                TUIConstants.TUIChat.CHAT_TYPE,
//                                                V2TIMConversation.V2TIM_GROUP
//                                        );
//                                        param.putString(TUIConstants.TUIChat.CHAT_ID, commonResultBean.groupId);
//                                        TUICore.startActivity(TUIConstants.TUIChat.GROUP_CHAT_ACTIVITY_NAME, param);
                                        AppUtil.Companion.goChat(ScanActivity.this, commonResultBean.groupId, "1");
                                        finish();
                                    } else {
                                        ToastUtil.show("已申请，等待群主同意");
                                        finish();
                                    }
                                }

                                @Override
                                public void onError(Response response, int code, Exception e) {
                                    ToastUtil.show("网络请求失败，请重试");
                                    restartScan();
                                }
                            });
                            break;
                        case "3"://扫描登录
                            idLogin(linkId);
                            break;
                        default:
                            ToastUtil.show("无效的二维码类型");
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

}