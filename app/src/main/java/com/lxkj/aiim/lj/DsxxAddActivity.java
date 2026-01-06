package com.lxkj.aiim.lj;

import static com.cboda.select.picture.config.PictureConfig.CHOOSE_REQUEST;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.listener.OnTimeSelectListener;
import com.bigkoo.pickerview.view.TimePickerView;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.cboda.select.picture.basic.PictureSelector;
import com.cboda.select.picture.config.PictureMimeType;
import com.cboda.select.picture.config.SelectMimeType;
import com.cboda.select.picture.entity.LocalMedia;
import com.cboda.intelligent.R;
import com.lxkj.aiim.base.BaseSimpleActivity;
import com.lxkj.aiim.holder.HongBaoBottomDialogHolder;
import com.lxkj.aiim.lj.adapter.UpLoadImageAdapter;
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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cboda.chatui.utils.FileBroadUtils;
import okhttp3.Response;
import top.zibin.luban.Luban;

public class DsxxAddActivity extends BaseSimpleActivity {


    private String id;
    private UpLoadImageAdapter imageAdapter;
    private List<String> images = new ArrayList<>();
    private boolean isHaveVideo = false;
    private ArrayList<String> typeList = new ArrayList<>();
    private int selectType = 0;//0图片 1视频 2PDF
    private String pdf, size, miao, name, txType = "1", time;
    private ArrayList<String> fsList = new ArrayList<>();
    private EditText etContent;
    private RecyclerView ryImage;
    private ImageView ivFile;
    private TextView tvFileName;
    private LinearLayout llAdd;
    private TextView tvTime;
    private LinearLayout llTime;
    private TextView tvType;
    private LinearLayout llType;
    private TextView tvConfirm;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_dsxx_add;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
        getToolbarTitle().setText(getResources().getString(R.string.添加定时消息));

        id = getIntent().getStringExtra("id");
        ryImage.setLayoutManager(new GridLayoutManager(this, 3));
        imageAdapter = new UpLoadImageAdapter(this, images, 9, 74);
        ryImage.setAdapter(imageAdapter);
        imageAdapter.setOnItemClickListener(new UpLoadImageAdapter.OnItemClickListener() {
            @Override
            public void OnDeleteClick(int position) {
                if (images.size() > position)
                    images.remove(position);
                imageAdapter.notifyDataSetChanged();

                if (images.size() == 0) {
                    llAdd.setVisibility(View.VISIBLE);
                    ryImage.setVisibility(View.GONE);
                    tvFileName.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void OnAddClick() {
                String[] perms = {
                        Permission.READ_MEDIA_IMAGES,
                        Permission.READ_MEDIA_VIDEO,
                        Permission.CAMERA};
                if (XXPermissions.isGranted(DsxxAddActivity.this, perms)) {
                    pmsSuccess();
                } else {
                    new NormalDialog(DsxxAddActivity.this, getString(R.string.温馨提示), "使用选择图片功能需要相机、存储权限，是否请求权限？", "取消", "确定", true)
                            .setOnButtonClickListener(new NormalDialog.OnButtonClick() {
                                @Override
                                public void OnRightClick() {
                                    XXPermissions permission = XXPermissions.with(DsxxAddActivity.this).permission(perms);
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
        typeList.add(getString(R.string.图片));
        typeList.add(getString(R.string.视频));
        typeList.add(getString(R.string.PDF));
        llAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HongBaoBottomDialogHolder holder = new HongBaoBottomDialogHolder(DsxxAddActivity.this, typeList);
                holder.setCallBack(new HongBaoBottomDialogHolder.BottomDialogClickCallBack() {
                    @Override
                    public void clickItem(int position) {
                        selectType = position;
                        select();
                    }
                });
                holder.show();

            }
        });


        fsList.add(getString(R.string.仅一次));
        fsList.add(getString(R.string.重复));
        llType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HongBaoBottomDialogHolder holder = new HongBaoBottomDialogHolder(DsxxAddActivity.this, fsList);
                holder.setCallBack(new HongBaoBottomDialogHolder.BottomDialogClickCallBack() {
                    @Override
                    public void clickItem(int position) {
                        txType = String.valueOf(position + 1);
                        tvType.setText(fsList.get(position));
                    }
                });
                holder.show();
            }
        });

        llTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectDate();
            }
        });

        tvConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addDs();
            }
        });
    }

    /**
     * 添加、修改群定时消息
     */
    private void addDs() {
        if (TextUtils.isEmpty(etContent.getText())) {
            ToastUtil.show(getString(R.string.请填写消息内容));
            return;
        }

        if (StringUtil.isEmpty(time)) {
            ToastUtil.show(getString(R.string.请选择提醒时间));
            return;
        }
        Map<String, Object> param = new HashMap<>();
        param.put("gid", id);
        param.put("txt", etContent.getText().toString());
        param.put("time", time);
        param.put("txType", txType);
        if (!StringUtil.isEmpty(name))
            param.put("name", name);
        if (!StringUtil.isEmpty(pdf))
            param.put("pdf", pdf);
        if (!StringUtil.isEmpty(size))
            param.put("size", size);
        param.put("type", getType());
        if (!ListUtil.isEmpty(images)) {
            if (images.size() == 1) {
                if (MediaFile.isVideoFileType(images.get(0))) {
                    param.put("video", images.get(0));
                    param.put("vimg", images.get(0));
                    param.put("miao", miao);
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
        }

        OkHttpHelper.getInstance().post_json(this, Url.addDs, param, new SpotsCallBack<ResultBean>(this) {
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


    private void selectDate() {
        Calendar startDate = Calendar.getInstance();
        Calendar endDate = Calendar.getInstance();
        endDate.set(endDate.get(Calendar.YEAR) + 1, endDate.get(Calendar.MONTH) + 1, endDate.get(Calendar.DAY_OF_MONTH));
        final TimePickerView startTimePickerView = new TimePickerBuilder(this, new OnTimeSelectListener() {
            @Override
            public void onTimeSelect(Date date, View v) {
                time = formatDate(date.getTime(), "yyyy-MM-dd HH:mm:ss");
                tvTime.setText(time);
            }
        })
                .setRangDate(startDate, endDate)//起始终止年月日设定
                .setTextColorCenter(0xff998cf5)
                .setTitleBgColor(0xffffffff)
                .setSubmitColor(0xff998cf5)
//                .setDate(startDate)// 如果不设置的话，默认是系统时间*/
                .setType(new boolean[]{true, true, true, true, true, true})
                .build();
        startTimePickerView.show();
    }

    public String formatDate(long milliseconds, String format) {
        String resultD = "";
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try {
            Date d = new Date(milliseconds);
            resultD = sdf.format(d);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultD;
    }

    public int getVideoDuration(String filePath) {
        int duration = 0;
        try {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(filePath);
            String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            duration = Integer.parseInt(durationStr) / 1000; // 转换为秒
        } catch (Exception e) {
            e.printStackTrace();
        }
        return duration;
    }

    public String getType() {
        String type = "1";
        if (images.size() == 0 && StringUtil.isEmpty(pdf))
            type = "1";
        else if (images.size() == 1) {
            if (MediaFile.isVideoFileType(images.get(0))) {
                type = "3";
            } else
                type = "2";
        } else if (images.size() > 1)
            type = "2";
        else
            type = "4";
        return type;
    }

    private void select() {
        String[] permissions = new String[]{
                Permission.READ_MEDIA_IMAGES,
                Permission.READ_MEDIA_VIDEO,
                Permission.CAMERA
        };
        if (XXPermissions.isGranted(DsxxAddActivity.this, permissions)) {
            pmsSuccess();
        } else {
            new NormalDialog(DsxxAddActivity.this, getString(R.string.温馨提示), "使用选择图片功能需要相机、存储权限，是否请求权限？", "取消", "确定", true)
                    .setOnButtonClickListener(new NormalDialog.OnButtonClick() {
                        @Override
                        public void OnRightClick() {
                            XXPermissions permission = XXPermissions.with(DsxxAddActivity.this).permission(permissions);
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
                        size = file.length() + "";
                        miao = getVideoDuration(photoLists.get(0)) + "";
                    } else {
                        uploadFiles(photoLists);
                    }
                }
            }
        }

        if (resultCode == Activity.RESULT_OK && requestCode == 100) {
            Uri uri = data.getData();
            String path = FileBroadUtils.getPath(this, uri);
            if (TextUtils.isEmpty(path)) {
                Toast.makeText(this, "选择文件错误", Toast.LENGTH_SHORT).show();
                return;
            }
            String type = path.substring(path.lastIndexOf("."));
            File file = new File(path);
            switch (type) {
                case ".pdf":
                    tvFileName.setVisibility(View.VISIBLE);
                    tvFileName.setText(path.substring(path.lastIndexOf("/") + 1));
                    ivFile.setImageResource(R.mipmap.ic_wj_item);
                    size = file.length() + "";
                    uploadVideo(path);
                    break;
            }
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
                if (selectType == 2) {
                    pdf = resultBean.url;
                    name = tvFileName.getText().toString();
                } else {
                    ryImage.setVisibility(View.VISIBLE);
                    llAdd.setVisibility(View.GONE);
                    images.clear();
                    images.add(resultBean.url);
                    imageAdapter.setMaxNum(1);
                    imageAdapter.notifyDataSetChanged();
                }
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
                ryImage.setVisibility(View.VISIBLE);
                llAdd.setVisibility(View.GONE);
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
        if (selectType == 2) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/pdf");//无类型限制
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(intent, 100);
        } else {
            int chooseMode = SelectMimeType.ofAll();
            switch (selectType) {
                case 0:
                    chooseMode = SelectMimeType.ofImage();
                    break;
                case 1:
                    chooseMode = SelectMimeType.ofAll();
                    break;
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
    }

    private void initViews() {
        etContent = findViewById(R.id.etContent);
        ryImage = findViewById(R.id.ryImage);
        ivFile = findViewById(R.id.ivFile);
        tvFileName = findViewById(R.id.tvFileName);
        llAdd = findViewById(R.id.llAdd);
        tvTime = findViewById(R.id.tvTime);
        llTime = findViewById(R.id.llTime);
        tvType = findViewById(R.id.tvType);
        llType = findViewById(R.id.llType);
        tvConfirm = findViewById(R.id.tvConfirm);
    }
}