package com.lxkj.aiim.lj;

import static com.luck.picture.lib.config.PictureConfig.CHOOSE_REQUEST;

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

import com.google.gson.Gson;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.luck.picture.lib.basic.PictureSelector;
import com.luck.picture.lib.config.SelectMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.cboda.intelligent.R;
import com.lxkj.aiim.base.BaseSimpleActivity;
import com.lxkj.aiim.lj.adapter.TixianConfigAdapter;
import com.lxkj.aiim.lj.bean.DataListBean;
import com.lxkj.aiim.lj.bean.ResultBean;
import com.lxkj.aiim.lj.dialogfra.NormalDialog;
import com.lxkj.aiim.lj.dialogfra.PayPassworDialogFra;
import com.lxkj.aiim.lj.http.BaseCallback;
import com.lxkj.aiim.lj.http.OkHttpHelper;
import com.lxkj.aiim.lj.http.SpotsCallBack;
import com.lxkj.aiim.lj.http.Url;
import com.lxkj.aiim.lj.utils.GlideBroadEngine;
import com.lxkj.aiim.lj.utils.GlideUtil;
import com.lxkj.aiim.lj.utils.ListUtil;
import com.lxkj.aiim.lj.utils.StringUtil;
import com.lxkj.aiim.lj.utils.ToastUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Request;
import okhttp3.Response;
import top.zibin.luban.Luban;

public class WithdrawActivity extends BaseSimpleActivity implements View.OnClickListener {

    private TixianConfigAdapter tixianConfigAdapter;
    private List<DataListBean> listBeans = new ArrayList<>();
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
            Intent intent = new Intent(this, WithdrawRecordActivity.class);
            startActivity(intent);
        });

        tixianConfigAdapter = new TixianConfigAdapter(this, listBeans);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        ryTixian.setLayoutManager(linearLayoutManager);
        ryTixian.setAdapter(tixianConfigAdapter);
        tixianConfigAdapter.setOnItemClickListener(new TixianConfigAdapter.OnItemClickListener() {
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
        return R.layout.activity_withdraw;
    }

    @Override
    public void onClick(View view) {
         if (view.getId() == R.id.tvEnter){
             if (StringUtil.isEmpty(etMoney.getText().toString())) {
                 ToastUtil.show(getString(R.string.请输入提现金额));
                 return;
             }
             boolean isAll = true;
             for (int i = 0; i < listBeans.size(); i++) {
                 if (StringUtil.isEmpty(listBeans.get(i).context)) {
                     isAll = false;
                 }
             }
             if (!isAll) {
                 ToastUtil.show(getString(R.string.请完善信息));
                 return;
             }
             if (StringUtil.isEmpty(icon)) {
                 ToastUtil.show(getString(R.string.请上传图片));
                 return;
             }
             new PayPassworDialogFra().setShaixuanClick(etMoney.getText().toString(), new PayPassworDialogFra.OnShaixuanClick() {
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
                 new NormalDialog(this, getString(R.string.温馨提示), "使用选择图片功能需要相机、存储权限，是否请求权限？", "取消", "确定", true)
                         .setOnButtonClickListener(new NormalDialog.OnButtonClick() {
                             @Override
                             public void OnRightClick() {
                                 XXPermissions permission = XXPermissions.with(WithdrawActivity.this).permission(perms);
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
    }

    /**
     * 提现
     */
    private void tixian(String paypwd) {
        Map<String, Object> param = new HashMap<>();
        param.put("money", etMoney.getText().toString());
        param.put("img", icon);
        param.put("info", new Gson().toJson(listBeans));
        param.put("paypwd", paypwd);
        OkHttpHelper.getInstance().post_json(this, Url.tixian, param, new SpotsCallBack<ResultBean>(this) {
            @Override
            public void onSuccess(Response response, ResultBean resultBean) {
                ToastUtil.show(resultBean.msg);
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
        OkHttpHelper.getInstance().post_json(this, Url.txInfo, param, new BaseCallback<ResultBean>() {
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
                GlideUtil.setImag(WithdrawActivity.this, icon, imImage);
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
        etMoney = findViewById(R.id.etMoney);
        tvBigBiaoti = findViewById(R.id.tvBigBiaoti);
        ryTixian = findViewById(R.id.ryTixian);
        tvSmallBiaoti = findViewById(R.id.tvSmallBiaoti);
        imImage = findViewById(R.id.imImage);
        tvEnter = findViewById(R.id.tvEnter);
    }
}