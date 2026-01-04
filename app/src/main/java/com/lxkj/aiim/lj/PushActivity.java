package com.lxkj.aiim.lj;

import static com.luck.picture.lib.config.PictureConfig.CHOOSE_REQUEST;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.luck.picture.lib.basic.PictureSelector;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.config.SelectMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.lxkj.aiim.R;
import com.lxkj.aiim.base.BaseSimpleActivity;
import com.lxkj.aiim.lj.adapter.UpLoadImageAdapter;
import com.lxkj.aiim.lj.bean.DataListBean;
import com.lxkj.aiim.lj.bean.EventBusBean;
import com.lxkj.aiim.lj.bean.ResultBean;
import com.lxkj.aiim.lj.dialogfra.NormalDialog;
import com.lxkj.aiim.lj.http.OkHttpHelper;
import com.lxkj.aiim.lj.http.SpotsCallBack;
import com.lxkj.aiim.lj.http.Url;
import com.lxkj.aiim.lj.utils.GlideBroadEngine;
import com.lxkj.aiim.lj.utils.ListUtil;
import com.lxkj.aiim.lj.utils.MediaFile;
import com.lxkj.aiim.lj.utils.StringUtil;
import com.lxkj.aiim.lj.utils.ToastUtil;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Response;
import top.zibin.luban.Luban;

public class PushActivity extends BaseSimpleActivity implements View.OnClickListener {
    private UpLoadImageAdapter imageAdapter;
    private List<String> images = new ArrayList<>();
    private boolean isHaveVideo = false;
    private String lon;
    private String lat;
    private View viHead;
    private TextView tvFinish;
    private TextView tvPush;
    private EditText etContent;
    private RecyclerView ryImage;
    private TextView tvSite;
    private LinearLayout llSite;

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
        return R.layout.activity_push;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        ryImage.setLayoutManager(new GridLayoutManager(this, 3));
        imageAdapter = new UpLoadImageAdapter(this, images, 9, 74);
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
                        Permission.READ_MEDIA_VIDEO,
                        Permission.CAMERA};
                if (XXPermissions.isGranted(PushActivity.this, perms)) {
                    pmsSuccess();
                } else {
                    new NormalDialog(PushActivity.this, getString(R.string.温馨提示), "使用选择图片功能需要相机、存储权限，是否请求权限？", "取消", "确定", true)
                            .setOnButtonClickListener(new NormalDialog.OnButtonClick() {
                                @Override
                                public void OnRightClick() {
                                    XXPermissions permission = XXPermissions.with(PushActivity.this).permission(perms);
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

        tvFinish.setOnClickListener(this);
        llSite.setOnClickListener(this);
        tvPush.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        if (view.getId() == R.id.tvFinish){
            finish();
        }
        if (view.getId()== R.id.llSite){
//            intent = new Intent(this, SiteActivity.class);
//            startActivityForResult(intent,555);
        }
        if (view.getId() == R.id.tvPush){
            if (StringUtil.isEmpty(etContent.getText().toString())) {
                ToastUtil.show(getString(R.string.请输入发表内容));
                return;
            }
            pushDy();
        }
    }

    /**
     * 发布动态
     */
    private void pushDy() {
        Map<String, Object> param = new HashMap<>();
        param.put("info", etContent.getText().toString());

        if (images.size() == 1) {
            if (MediaFile.isVideoFileType(images.get(0))) {
                param.put("video", images.get(0));
                param.put("vimg", images.get(0));
            } else {
                param.put("imgs", images.get(0));
            }
        } else {
            String imgs = "";
            for (int i = 0; i < images.size(); i++) {
                imgs += images.get(i) + "|";
            }
            if (!StringUtil.isEmpty(imgs)) {
                param.put("imgs", imgs);
            }
        }
        param.put("address", tvSite.getText().toString());
        param.put("lon", lon);
        param.put("lat", lat);
        OkHttpHelper.getInstance().post_json(this, Url.pushDy, param, new SpotsCallBack<ResultBean>(this) {
            @Override
            public void onSuccess(Response response, ResultBean resultBean) {
                EventBusBean eventBusBean = new EventBusBean();
                eventBusBean.msg = "发布成功";
                EventBus.getDefault().post(eventBusBean);
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
                    LocalMedia media = selectList.get(0);
                    String pictureType = media.getMimeType();
                    int mediaType = PictureMimeType.getMimeType(pictureType);
                    if (mediaType == 2) {
                        uploadVideo(photoLists.get(0));
                    } else {
                        uploadFiles(photoLists);
                    }

                }
            }
        } else if (requestCode == 555 && resultCode == 555) {
            DataListBean dataListBean = (DataListBean) data.getSerializableExtra("bean");
            tvSite.setText(dataListBean.title);
            lon = dataListBean.lng;
            lat = dataListBean.lat;
        }
    }

    /**
     * 上传视频
     */
    private void uploadVideo(String path) {
        List<File> file = new ArrayList<>();
        file.add(new File(path));
        Map<String, List<File>> files = new HashMap<>();
        if (!ListUtil.isEmpty(file))
            files.put("file", file);
        OkHttpHelper.getInstance().post_file(this, Url.uploadFile, files, new SpotsCallBack<ResultBean>(this) {
            @Override
            public void onSuccess(Response response, ResultBean resultBean) {
                images.clear();
                images.add(resultBean.url);
                imageAdapter.setMaxNum(1);
                imageAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Response response, int code, Exception e) {
            }
        });
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
                imageAdapter.setMaxNum(9);
                imageAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Response response, int code, Exception e) {
            }
        });
    }

    public void pmsSuccess() {
        isHaveVideo = false;
        int chooseMode = SelectMimeType.ofAll();
        for (int i = 0; i < images.size(); i++) {
            if (MediaFile.isVideoFileType(images.get(i))) {
                isHaveVideo = true;
            }
        }
        if (!isHaveVideo && images.size() != 0) {
            chooseMode = SelectMimeType.ofImage();
        } else {
            chooseMode = SelectMimeType.ofAll();
        }
        PictureSelector.create(this)//在Fragment中使用则是 fragment.this
                .openGallery(chooseMode)//全部.PictureMimeType.ofAll()、图片.ofImage()、视频.ofVideo()、音频.ofAudio()
                .setImageEngine(GlideBroadEngine.createGlideEngine())
                .setMaxSelectNum(9 - images.size())
                .setMinSelectNum(1)
                .setImageSpanCount(3)// 每行显示个数 int
                .isDisplayCamera(true)// 是否显示拍照按钮 true or false
                .forResult(CHOOSE_REQUEST);//结果回调onActivityResult code
    }

    private void initViews() {
        viHead = findViewById(R.id.viHead);
        tvFinish = findViewById(R.id.tvFinish);
        tvPush = findViewById(R.id.tvPush);
        etContent = findViewById(R.id.etContent);
        ryImage = findViewById(R.id.ryImage);
        tvSite = findViewById(R.id.tvSite);
        llSite = findViewById(R.id.llSite);
    }
}