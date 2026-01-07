package com.cboda.intelligent.activity;

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
import com.cboda.intelligent.base.BaseSimpleBroadActivity;
import com.cboda.intelligent.bean.ResultBroadBean;
import com.cboda.intelligent.dialog.NormalBroadDialog;
import com.cboda.intelligent.utils.OkHttpBroadHelper;
import com.cboda.intelligent.utils.SpotsCallBroadBack;
import com.cboda.intelligent.net.BroadUrl;
import com.cboda.intelligent.utils.GlideBroadBroadEngine;
import com.cboda.intelligent.utils.ListBroadUtil;
import com.cboda.intelligent.utils.StringBroadUtil;
import com.cboda.intelligent.utils.ToastBroadUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Response;
import top.zibin.luban.Luban;

public class ChatBgBroadActivity extends BaseSimpleBroadActivity {

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
                new NormalBroadDialog(this, getString(R.string.温馨提示), "使用选择图片功能需要存储权限，是否请求权限？", "取消", "确定", true)
                        .setOnButtonClickListener(new NormalBroadDialog.OnButtonClick() {
                            @Override
                            public void OnRightClick() {
                                XXPermissions permission = XXPermissions.with(ChatBgBroadActivity.this).permission(perms);
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
        });
        llPai.setOnClickListener(v -> {
            String[] perms = {
                    Permission.CAMERA};
            if (XXPermissions.isGranted(this, perms)) {
                pmsPaizhaoSuccess();
            } else {
                new NormalBroadDialog(this, getString(R.string.温馨提示), "使用选择拍照功能需要相机权限，是否请求权限？", "取消", "确定", true)
                        .setOnButtonClickListener(new NormalBroadDialog.OnButtonClick() {
                            @Override
                            public void OnRightClick() {
                                XXPermissions permission = XXPermissions.with(ChatBgBroadActivity.this).permission(perms);
                                permission.request(new OnPermissionCallback() {
                                    @Override
                                    public void onGranted(@NonNull List<String> permissions, boolean allGranted) {
                                        pmsPaizhaoSuccess();
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
        OkHttpBroadHelper.getInstance().post_json(this, BroadUrl.editUserinfo, param, new SpotsCallBroadBack<ResultBroadBean>(this) {
            @Override
            public void onSuccess(Response response, ResultBroadBean resultBean) {
                ToastBroadUtil.show(resultBean.msg);
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
                .setImageEngine(GlideBroadBroadEngine.createGlideEngine())
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