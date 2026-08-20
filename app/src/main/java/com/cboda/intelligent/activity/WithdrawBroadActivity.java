package com.cboda.intelligent.activity;

import static com.cboda.select.picture.config.PictureConfig.CHOOSE_REQUEST;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.GsonUtils;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.cboda.select.picture.basic.PictureSelector;
import com.cboda.select.picture.config.SelectMimeType;
import com.cboda.select.picture.entity.LocalMedia;
import com.cboda.intelligent.R;
import com.cboda.intelligent.base.BaseSimpleBroadActivity;
import com.cboda.intelligent.adapter.TixianConfigBroadAdapter;
import com.cboda.select.picture.entity.DataListBroadBean;
import com.cboda.intelligent.bean.ResultBroadBean;
import com.cboda.intelligent.dialog.NormalBroadDialog;
import com.cboda.intelligent.dialog.PayPassworDialogBroadFra;
import com.cboda.intelligent.utils.BaseBroadCallback;
import com.cboda.intelligent.utils.OkHttpBroadHelper;
import com.cboda.intelligent.utils.SpotsCallBroadBack;
import com.cboda.intelligent.net.BroadUrl;
import com.cboda.intelligent.utils.GlideBroadBroadEngine;
import com.cboda.intelligent.utils.GlideBroadUtil;
import com.cboda.intelligent.utils.ListBroadUtil;
import com.cboda.intelligent.utils.StringBroadUtil;
import com.cboda.intelligent.utils.ToastBroadUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Request;
import okhttp3.Response;
import top.zibin.luban.Luban;

public class WithdrawBroadActivity extends BaseSimpleBroadActivity implements View.OnClickListener {

    private TixianConfigBroadAdapter tixianConfigAdapter;
    private List<DataListBroadBean> listBeans = new ArrayList<>();
    private String icon;
    private EditText etMoney;
    private TextView tvBigBiaoti;
    private RecyclerView ryTixian;
    private TextView tvSmallBiaoti;
    private ImageView imImage;
    private TextView tvEnter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
        getToolbarTitle().setText(getString(R.string.提现));
        getRightView().setText(getString(R.string.提现记录));
        getRightView().setOnClickListener(view -> {//提现记录
            Intent intent = new Intent(this, WithdrawRecordBroadActivity.class);
            startActivity(intent);
        });

        tixianConfigAdapter = new TixianConfigBroadAdapter(this, listBeans);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        ryTixian.setLayoutManager(linearLayoutManager);
        ryTixian.setAdapter(tixianConfigAdapter);
        tixianConfigAdapter.setOnItemClickListener(new TixianConfigBroadAdapter.OnItemClickListener() {
            @Override
            public void OnItemClickListener(int position, String content) {
                listBeans.get(position).context = content;
            }
        });

        ptConfig();
        txInfo();

        tvEnter.setOnClickListener(this);
        imImage.setOnClickListener(this);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_broad_withdraw;
    }

    @Override
    public void onClick(View view) {
         if (view.getId() == R.id.tvEnter){
             if (StringBroadUtil.isEmpty(etMoney.getText().toString())) {
                 ToastBroadUtil.show(getString(R.string.请输入提现金额));
                 return;
             }
             boolean isAll = true;
             for (int i = 0; i < listBeans.size(); i++) {
                 if (StringBroadUtil.isEmpty(listBeans.get(i).context)) {
                     isAll = false;
                 }
             }
             if (!isAll) {
                 ToastBroadUtil.show(getString(R.string.请完善信息));
                 return;
             }
             if (StringBroadUtil.isEmpty(icon)) {
                 ToastBroadUtil.show(getString(R.string.请上传图片));
                 return;
             }
             new PayPassworDialogBroadFra().setShaixuanClick(etMoney.getText().toString(), new PayPassworDialogBroadFra.OnShaixuanClick() {
                 @Override
                 public void onShaixuanClick(String content) {
                     tixian(content);
                 }
             }).show(getSupportFragmentManager(), "");
         }else  if (view.getId() == R.id.imImage){
             String[] perms = {
                     Permission.READ_MEDIA_IMAGES,
                     Permission.CAMERA};
             if (XXPermissions.isGranted(this, perms)) {
                 pmsSuccess();
             } else {
                 new NormalBroadDialog(this, getString(R.string.温馨提示), "使用选择图片功能需要相机、存储权限，是否请求权限？", "取消", "确定", true)
                         .setOnButtonClickListener(new NormalBroadDialog.OnButtonClick() {
                             @Override
                             public void OnRightClick() {
                                 XXPermissions permission = XXPermissions.with(WithdrawBroadActivity.this).permission(perms);
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
    }

    /**
     * 提现
     */
    private void tixian(String paypwd) {
        Map<String, Object> param = new HashMap<>();
        param.put("money", etMoney.getText().toString());
        param.put("img", icon);
        param.put("info", GsonUtils.getGson().toJson(listBeans));
        param.put("paypwd", paypwd);
        OkHttpBroadHelper.getInstance().post_json(this, BroadUrl.tixian, param, new SpotsCallBroadBack<ResultBroadBean>(this) {
            @Override
            public void onSuccess(Response response, ResultBroadBean resultBean) {
                ToastBroadUtil.show(resultBean.msg);
            }

            @Override
            public void onError(Response response, int code, Exception e) {
            }
        });
    }

    /**
     * 提现标题
     */
    private void txInfo() {
        Map<String, Object> param = new HashMap<>();
        OkHttpBroadHelper.getInstance().post_json(this, BroadUrl.txInfo, param, new BaseBroadCallback<ResultBroadBean>() {
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
                listBeans.clear();
                listBeans.addAll(resultBean.dataList);
                tixianConfigAdapter.notifyDataSetChanged();
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
        OkHttpBroadHelper.getInstance().post_json(this, BroadUrl.ptConfig, param, new BaseBroadCallback<ResultBroadBean>() {
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
                tvBigBiaoti.setText(resultBean.data.tx1);
                tvSmallBiaoti.setText(resultBean.data.tx2);
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
                icon = resultBean.urls.get(0);
                GlideBroadUtil.setImag(WithdrawBroadActivity.this, icon, imImage);
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
        etMoney = findViewById(R.id.etMoney);
        tvBigBiaoti = findViewById(R.id.tvBigBiaoti);
        ryTixian = findViewById(R.id.ryTixian);
        tvSmallBiaoti = findViewById(R.id.tvSmallBiaoti);
        imImage = findViewById(R.id.imImage);
        tvEnter = findViewById(R.id.tvEnter);
    }
}