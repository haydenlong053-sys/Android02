package com.lxkj.aiim.lj;

import static com.cboda.select.picture.config.PictureConfig.CHOOSE_REQUEST;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.cboda.select.picture.basic.PictureSelector;
import com.cboda.select.picture.config.SelectMimeType;
import com.cboda.select.picture.entity.LocalMedia;
import com.cboda.intelligent.R;
import com.lxkj.aiim.base.BaseSimpleActivity;
import com.lxkj.aiim.lj.adapter.UpLoadImageAdapter;
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

public class FeedbackActivity extends BaseSimpleActivity {

    private UpLoadImageAdapter imageAdapter;
    private List<String> images = new ArrayList<>();
    private EditText etContent;
    private RecyclerView ryImage;
    private EditText etCall;
    private TextView tvSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getToolbarTitle().setText(getResources().getString(R.string.意见反馈));
        initViews();
        ryImage.setLayoutManager(new GridLayoutManager(this, 3));
        imageAdapter = new UpLoadImageAdapter(this, images, 3, 120);
        ryImage.setAdapter(imageAdapter);
        imageAdapter.setOnItemClickListener(new UpLoadImageAdapter.OnItemClickListener() {
            @Override
            public void OnDeleteClick(int position) {
                if (images.size() > position)
                    images.remove(position);
                imageAdapter.notifyDataSetChanged();
            }

            @Override
            public void OnAddClick() {
                String[] perms = {
                        Permission.READ_MEDIA_IMAGES,
                        Permission.CAMERA};
                if (XXPermissions.isGranted(FeedbackActivity.this, perms)) {
                    pmsSuccess();
                } else {
                    new NormalDialog(FeedbackActivity.this, getString(R.string.温馨提示), "使用选择图片功能需要相机、存储权限，是否请求权限？", "取消", "确定", true)
                            .setOnButtonClickListener(new NormalDialog.OnButtonClick() {
                                @Override
                                public void OnRightClick() {
                                    XXPermissions permission = XXPermissions.with(FeedbackActivity.this).permission(perms);
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

        tvSubmit.setOnClickListener(v -> {
            if (StringUtil.isEmpty(etContent.getText().toString())) {
                ToastUtil.show(getString(R.string.请填写15字以上描述以便我们更好地为您提供帮助));
                return;
            }
            if (etContent.getText().toString().length() < 15) {
                ToastUtil.show(getString(R.string.请填写15字以上描述以便我们更好地为您提供帮助));
                return;
            }
            if (StringUtil.isEmpty(etCall.getText().toString())) {
                ToastUtil.show(getString(R.string.请填写您的微信或者手机号码));
                return;
            }
            String imgs = "";
            for (int i = 0; i < images.size(); i++) {
                imgs += images.get(i) + "|";
            }
            fankui(imgs);
        });
    }

    /**
     * 意见反馈
     */
    private void fankui(String imgs) {
        Map<String, Object> param = new HashMap<>();
        param.put("info", etContent.getText().toString());
        if (!StringUtil.isEmpty(imgs)) {
            param.put("imgs", imgs.substring(0, imgs.length() - 1));
        }
        param.put("phone", etCall.getText().toString());
        OkHttpHelper.getInstance().post_json(this, Url.fankui, param, new SpotsCallBack<ResultBean>(this) {

            @Override
            public void onSuccess(Response response, ResultBean resultBean) {
                ToastUtil.show(resultBean.msg);
                finish();
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
                images.addAll(resultBean.urls);
                imageAdapter.notifyDataSetChanged();
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
                .setMaxSelectNum(3 - images.size())
                .setMinSelectNum(1)
                .setImageSpanCount(3)// 每行显示个数 int
                .isDisplayCamera(true)// 是否显示拍照按钮 true or false
                .forResult(CHOOSE_REQUEST);//结果回调onActivityResult code
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_feedback;
    }

    private void initViews() {
        etContent = findViewById(R.id.etContent);
        ryImage = findViewById(R.id.ryImage);
        etCall = findViewById(R.id.etCall);
        tvSubmit = findViewById(R.id.tvSubmit);
    }
}