package com.cboda.intelligent.activity;

import static com.cboda.select.picture.config.PictureConfig.CHOOSE_REQUEST;

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
import com.cboda.select.picture.basic.PictureSelector;
import com.cboda.select.picture.config.PictureMimeType;
import com.cboda.select.picture.config.SelectMimeType;
import com.cboda.select.picture.entity.LocalMedia;
import com.cboda.intelligent.R;
import com.cboda.intelligent.base.BaseSimpleBroadActivity;
import com.cboda.intelligent.adapter.UpLoadImageBroadAdapter;
import com.cboda.baselibrary.bean.DataListBroadBean;
import com.cboda.intelligent.bean.EventBusBroadBean;
import com.cboda.intelligent.bean.ResultBroadBean;
import com.cboda.intelligent.dialog.NormalBroadDialog;
import com.cboda.intelligent.utils.OkHttpBroadHelper;
import com.cboda.intelligent.utils.SpotsCallBroadBack;
import com.cboda.intelligent.net.BroadUrl;
import com.cboda.intelligent.utils.GlideBroadBroadEngine;
import com.cboda.intelligent.utils.ListBroadUtil;
import com.cboda.intelligent.utils.MediaBroadFile;
import com.cboda.intelligent.utils.StringBroadUtil;
import com.cboda.intelligent.utils.ToastBroadUtil;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Response;
import top.zibin.luban.Luban;

public class PushBroadActivity extends BaseSimpleBroadActivity implements View.OnClickListener {
    private UpLoadImageBroadAdapter imageAdapter;
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
        return R.layout.activity_broad_push;
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
        imageAdapter = new UpLoadImageBroadAdapter(this, images, 9, 74);
        ryImage.setAdapter(imageAdapter);
        imageAdapter.setOnItemClickListener(new UpLoadImageBroadAdapter.OnItemClickListener() {
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
                if (XXPermissions.isGranted(PushBroadActivity.this, perms)) {
                    pmsSuccess();
                } else {
                    new NormalBroadDialog(PushBroadActivity.this, getString(R.string.温馨提示), "使用选择图片功能需要相机、存储权限，是否请求权限？", "取消", "确定", true)
                            .setOnButtonClickListener(new NormalBroadDialog.OnButtonClick() {
                                @Override
                                public void OnRightClick() {
                                    XXPermissions permission = XXPermissions.with(PushBroadActivity.this).permission(perms);
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
            if (StringBroadUtil.isEmpty(etContent.getText().toString())) {
                ToastBroadUtil.show(getString(R.string.请输入发表内容));
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
            if (MediaBroadFile.isVideoFileType(images.get(0))) {
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
            if (!StringBroadUtil.isEmpty(imgs)) {
                param.put("imgs", imgs);
            }
        }
        param.put("address", tvSite.getText().toString());
        param.put("lon", lon);
        param.put("lat", lat);
        OkHttpBroadHelper.getInstance().post_json(this, BroadUrl.pushDy, param, new SpotsCallBroadBack<ResultBroadBean>(this) {
            @Override
            public void onSuccess(Response response, ResultBroadBean resultBean) {
                EventBusBroadBean eventBusBean = new EventBusBroadBean();
                eventBusBean.msg = "发布成功";
                EventBus.getDefault().post(eventBusBean);
                ToastBroadUtil.show(resultBean.msg);
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
            DataListBroadBean dataListBean = (DataListBroadBean) data.getSerializableExtra("bean");
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
        if (!ListBroadUtil.isEmpty(file))
            files.put("file", file);
        OkHttpBroadHelper.getInstance().post_file(this, BroadUrl.uploadFile, files, new SpotsCallBroadBack<ResultBroadBean>(this) {
            @Override
            public void onSuccess(Response response, ResultBroadBean resultBean) {
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
        if (!ListBroadUtil.isEmpty(file))
            files.put("files", file);
        OkHttpBroadHelper.getInstance().post_file(this, BroadUrl.uploadFiles, files, new SpotsCallBroadBack<ResultBroadBean>(this) {
            @Override
            public void onSuccess(Response response, ResultBroadBean resultBean) {
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
            if (MediaBroadFile.isVideoFileType(images.get(i))) {
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
                .setImageEngine(GlideBroadBroadEngine.createGlideEngine())
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