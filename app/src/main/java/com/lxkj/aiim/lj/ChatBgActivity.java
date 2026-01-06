package com.lxkj.aiim.lj;

import static com.cboda.select.picture.config.PictureConfig.CHOOSE_REQUEST;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.cboda.select.picture.basic.PictureSelector;
import com.cboda.select.picture.config.SelectMimeType;
import com.cboda.select.picture.entity.LocalMedia;
import com.cboda.select.picture.interfaces.OnResultCallbackListener;
import com.cboda.intelligent.R;
import com.lxkj.aiim.base.BaseSimpleActivity;
import com.lxkj.aiim.lj.bean.ResultBean;
import com.lxkj.aiim.lj.dialogfra.NormalDialog;
import com.lxkj.aiim.lj.http.OkHttpHelper;
import com.lxkj.aiim.lj.http.SpotsCallBack;
import com.lxkj.aiim.lj.http.Url;
import com.lxkj.aiim.lj.utils.GlideBroadEngine;
import com.lxkj.aiim.lj.utils.ListUtil;
import com.lxkj.aiim.lj.utils.StringUtil;
import com.lxkj.aiim.lj.utils.ToastUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Response;
import top.zibin.luban.Luban;

public class ChatBgActivity extends BaseSimpleActivity {

    private String type;
    private LinearLayout llHf;
    private LinearLayout llXiangce;
    private LinearLayout llPai;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();


        type = getIntent().getStringExtra("type");
        if (type.equals("0")) {
            getToolbarTitle().setText(getResources().getString(R.string.朋友圈背景));
        } else {
            getToolbarTitle().setText(getResources().getString(R.string.聊天背景));
        }

        llXiangce.setOnClickListener(v -> {
            String[] perms = {
                    Permission.READ_MEDIA_IMAGES};
            if (XXPermissions.isGranted(this, perms)) {
                pmsSuccess();
            } else {
                new NormalDialog(this, getString(R.string.温馨提示), "使用选择图片功能需要存储权限，是否请求权限？", "取消", "确定", true)
                        .setOnButtonClickListener(new NormalDialog.OnButtonClick() {
                            @Override
                            public void OnRightClick() {
                                XXPermissions permission = XXPermissions.with(ChatBgActivity.this).permission(perms);
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
        });
        llPai.setOnClickListener(v -> {
            String[] perms = {
                    Permission.CAMERA};
            if (XXPermissions.isGranted(this, perms)) {
                pmsPaizhaoSuccess();
            } else {
                new NormalDialog(this, getString(R.string.温馨提示), "使用选择拍照功能需要相机权限，是否请求权限？", "取消", "确定", true)
                        .setOnButtonClickListener(new NormalDialog.OnButtonClick() {
                            @Override
                            public void OnRightClick() {
                                XXPermissions permission = XXPermissions.with(ChatBgActivity.this).permission(perms);
                                permission.request(new OnPermissionCallback() {
                                    @Override
                                    public void onGranted(@NonNull List<String> permissions, boolean allGranted) {
                                        pmsPaizhaoSuccess();
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
        });
        llHf.setOnClickListener(v -> {
            editUserinfo("");
        });
    }


    /**
     * 修改资料
     */
    private void editUserinfo(String img) {
        Map<String, Object> param = new HashMap<>();
        if (type.equals("0")) {
            param.put("img", img);
        } else {
            param.put("chatImg", img);
        }
        OkHttpHelper.getInstance().post_json(this, Url.editUserinfo, param, new SpotsCallBack<ResultBean>(this) {
            @Override
            public void onSuccess(Response response, ResultBean resultBean) {
                ToastUtil.show(resultBean.msg);
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
                editUserinfo(resultBean.urls.get(0));
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
                .isDisplayCamera(false)// 是否显示拍照按钮 true or false
                .forResult(CHOOSE_REQUEST);//结果回调onActivityResult code
    }

    public void pmsPaizhaoSuccess() {
        PictureSelector.create(this)
                .openCamera(SelectMimeType.ofImage())
                .forResult(new OnResultCallbackListener<LocalMedia>() {
                    @Override
                    public void onResult(ArrayList<LocalMedia> selectList) {
                        ArrayList<String> photoLists = new ArrayList<>();
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

                    @Override
                    public void onCancel() {

                    }
                });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_chat_bg;
    }

    private void initViews() {
        llHf = findViewById(R.id.llHf);
        llXiangce = findViewById(R.id.llXiangce);
        llPai = findViewById(R.id.llPai);
    }
}