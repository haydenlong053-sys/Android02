package com.cboda.intelligent.activity;

import static com.cboda.select.picture.config.PictureConfig.CHOOSE_REQUEST;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.cboda.select.picture.basic.PictureSelector;
import com.cboda.select.picture.config.SelectMimeType;
import com.cboda.select.picture.entity.LocalMedia;
import com.cboda.intelligent.R;
import com.cboda.intelligent.base.BaseSimpleBroadActivity;
import com.cboda.intelligent.bean.ResultBroadBean;
import com.cboda.intelligent.dialog.NormalBroadDialog;
import com.cboda.intelligent.utils.BaseBroadCallback;
import com.cboda.intelligent.utils.OkHttpBroadHelper;
import com.cboda.intelligent.utils.SpotsCallBroadBack;
import com.cboda.intelligent.net.BroadUrl;
import com.cboda.intelligent.utils.GlideBroadBroadEngine;
import com.cboda.intelligent.utils.GlideBroadUtil;
import com.cboda.intelligent.utils.ListBroadUtil;
import com.cboda.intelligent.utils.StringBroadUtil;
import com.cboda.intelligent.utils.ToastBroadUtil;
import com.cboda.intelligent.utils.StringBroadUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Request;
import okhttp3.Response;
import top.zibin.luban.Luban;

public class RechargeBroadActivity extends BaseSimpleBroadActivity implements View.OnClickListener {

    private String icon;
    private TextView tvYue;
    private TextView tvShoukuanUser;
    private ImageView imCopy1;
    private TextView tvShoukuanZhanghu;
    private ImageView imCopy2;
    private TextView tvBiaoti;
    private ImageView imBiaoti;
    private EditText etMoney;
    private ImageView imImage;
    private TextView tvEnter;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_broad_recharge;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
        getToolbarTitle().setText(getString(R.string.充值));
        getRightView().setText(getString(R.string.充值记录));
        getRightView().setOnClickListener(view -> {//充值记录
            Intent intent = new Intent(this, RechargeRecordBroadActivity.class);
            startActivity(intent);
        });

        imCopy1.setOnClickListener(this);
        imCopy2.setOnClickListener(this);
        imImage.setOnClickListener(this);
        tvEnter.setOnClickListener(this);

        ptConfig();
        userInfo();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.imCopy1){
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("label", tvShoukuanUser.getText().toString());
            clipboard.setPrimaryClip(clip);
            ToastBroadUtil.show(getString(R.string.复制成功));
        }else  if (view.getId() ==R.id.imCopy2){
            ClipboardManager clipboard1 = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip1 = ClipData.newPlainText("label", tvShoukuanZhanghu.getText().toString());
            clipboard1.setPrimaryClip(clip1);
            ToastBroadUtil.show(getString(R.string.复制成功));
        }else  if (view.getId() ==R.id.imImage){
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
                                XXPermissions permission = XXPermissions.with(RechargeBroadActivity.this).permission(perms);
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
        }else  if (view.getId() ==R.id.tvEnter){
            if (StringBroadUtil.isEmpty(etMoney.getText().toString())) {
                ToastBroadUtil.show(getString(R.string.请输入充值金额));
                return;
            }
            if (StringBroadUtil.isEmpty(icon)) {
                ToastBroadUtil.show(getString(R.string.请上传充值凭证));
                return;
            }
            rechage();
        }
    }

    /**
     * 充值
     */
    private void rechage() {
        Map<String, Object> param = new HashMap<>();
        param.put("money", etMoney.getText().toString());
        param.put("img", icon);
        OkHttpBroadHelper.getInstance().post_json(this, BroadUrl.rechage, param, new SpotsCallBroadBack<ResultBroadBean>(this) {
            @Override
            public void onSuccess(Response response, ResultBroadBean resultBean) {
                ToastBroadUtil.show(resultBean.msg);
                userInfo();
            }

            @Override
            public void onError(Response response, int code, Exception e) {
            }
        });
    }

    /**
     * 用户信息
     */
    private void userInfo() {
        Map<String, Object> param = new HashMap<>();
        OkHttpBroadHelper.getInstance().post_json(this, BroadUrl.userInfo, param, new BaseBroadCallback<ResultBroadBean>() {
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
                tvYue.setText(StringBroadUtils.removeDecimalPart(resultBean.data.balalce) + getString(R.string.yuanqian));
            }

            @Override
            public void onError(Response response, int code, Exception e) {
            }
        });
    }

    /**
     * 平台参数
     */
    private void ptConfig() {
//        Map<String, Object> param = new HashMap<>();
//        OkHttpBroadHelper.getInstance().post_json(this, BroadUrl.ptConfig, param, new BaseBroadCallback<ResultBroadBean>() {
//            @Override
//            public void onBeforeRequest(Request request) {
//
//            }
//
//            @Override
//            public void onFailure(Request request, Exception e) {
//
//            }
//
//            @Override
//            public void onResponse(Response response) {
//
//            }
//
//            @Override
//            public void onSuccess(Response response, ResultBroadBean resultBean) {
//                tvBiaoti.setText(resultBean.data.cz);
//                GlideBroadUtil.setImagOverSize(RechargeBroadActivity.this, resultBean.data.skm, imBiaoti, 100, 100);
//                tvShoukuanUser.setText(resultBean.data.sname);
//                tvShoukuanZhanghu.setText(resultBean.data.sno);
//            }
//
//            @Override
//            public void onError(Response response, int code, Exception e) {
//            }
//        });
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
                GlideBroadUtil.setImag(RechargeBroadActivity.this, icon, imImage);
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
        tvYue = findViewById(R.id.tvYue);
        tvShoukuanUser = findViewById(R.id.tvShoukuanUser);
        imCopy1 = findViewById(R.id.imCopy1);
        tvShoukuanZhanghu = findViewById(R.id.tvShoukuanZhanghu);
        imCopy2 = findViewById(R.id.imCopy2);
        tvBiaoti = findViewById(R.id.tvBiaoti);
        imBiaoti = findViewById(R.id.imBiaoti);
        etMoney = findViewById(R.id.etMoney);
        imImage = findViewById(R.id.imImage);
        tvEnter = findViewById(R.id.tvEnter);
    }
}