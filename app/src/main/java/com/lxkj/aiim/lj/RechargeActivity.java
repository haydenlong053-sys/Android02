package com.lxkj.aiim.lj;

import static com.luck.picture.lib.config.PictureConfig.CHOOSE_REQUEST;

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
import com.luck.picture.lib.basic.PictureSelector;
import com.luck.picture.lib.config.SelectMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.lxkj.aiim.R;
import com.lxkj.aiim.base.BaseSimpleActivity;
import com.lxkj.aiim.lj.bean.ResultBean;
import com.lxkj.aiim.lj.dialogfra.NormalDialog;
import com.lxkj.aiim.lj.http.BaseCallback;
import com.lxkj.aiim.lj.http.OkHttpHelper;
import com.lxkj.aiim.lj.http.SpotsCallBack;
import com.lxkj.aiim.lj.http.Url;
import com.lxkj.aiim.lj.utils.GlideBroadEngine;
import com.lxkj.aiim.lj.utils.GlideUtil;
import com.lxkj.aiim.lj.utils.ListUtil;
import com.lxkj.aiim.lj.utils.StringUtil;
import com.lxkj.aiim.lj.utils.ToastUtil;
import com.lxkj.aiim.utils.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Request;
import okhttp3.Response;
import top.zibin.luban.Luban;

public class RechargeActivity extends BaseSimpleActivity implements View.OnClickListener {

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
        return R.layout.activity_recharge;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
        getToolbarTitle().setText(getString(R.string.充值));
        getRightView().setText(getString(R.string.充值记录));
        getRightView().setOnClickListener(view -> {//充值记录
            Intent intent = new Intent(this, RechargeRecordActivity.class);
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
            ToastUtil.show(getString(R.string.复制成功));
        }else  if (view.getId() ==R.id.imCopy2){
            ClipboardManager clipboard1 = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip1 = ClipData.newPlainText("label", tvShoukuanZhanghu.getText().toString());
            clipboard1.setPrimaryClip(clip1);
            ToastUtil.show(getString(R.string.复制成功));
        }else  if (view.getId() ==R.id.imImage){
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
                                XXPermissions permission = XXPermissions.with(RechargeActivity.this).permission(perms);
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
        }else  if (view.getId() ==R.id.tvEnter){
            if (StringUtil.isEmpty(etMoney.getText().toString())) {
                ToastUtil.show(getString(R.string.请输入充值金额));
                return;
            }
            if (StringUtil.isEmpty(icon)) {
                ToastUtil.show(getString(R.string.请上传充值凭证));
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
        OkHttpHelper.getInstance().post_json(this, Url.rechage, param, new SpotsCallBack<ResultBean>(this) {
            @Override
            public void onSuccess(Response response, ResultBean resultBean) {
                ToastUtil.show(resultBean.msg);
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
        OkHttpHelper.getInstance().post_json(this, Url.userInfo, param, new BaseCallback<ResultBean>() {
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
                tvYue.setText(StringUtils.removeDecimalPart(resultBean.data.balalce) + getString(R.string.yuanqian));
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
                tvBiaoti.setText(resultBean.data.cz);
                GlideUtil.setImagOverSize(RechargeActivity.this, resultBean.data.skm, imBiaoti, 100, 100);
                tvShoukuanUser.setText(resultBean.data.sname);
                tvShoukuanZhanghu.setText(resultBean.data.sno);
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
                GlideUtil.setImag(RechargeActivity.this, icon, imImage);
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