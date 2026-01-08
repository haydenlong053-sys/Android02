package com.cboda.intelligent.activity;

import static com.cboda.select.picture.config.PictureConfig.CHOOSE_REQUEST;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewbinding.ViewBinding;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.cboda.intelligent.databinding.ActivityUserBroadInfoBinding;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.gson.Gson;
import com.gyf.immersionbar.ImmersionBar;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.cboda.select.picture.basic.PictureSelector;
import com.cboda.select.picture.config.SelectMimeType;
import com.cboda.select.picture.engine.CropFileEngine;
import com.cboda.select.picture.entity.LocalMedia;
import com.cboda.intelligent.R;
import com.cboda.intelligent.adapter.InterestBroadAdapter;
import com.cboda.intelligent.base.BaseSimpleBroadActivity;
import com.cboda.intelligent.dialog.UserEwmBroadDialog;
import com.cboda.intelligent.dialog.pop.CommonTipBroadPop;
import com.cboda.intelligent.bean.ResultBroadBean;
import com.cboda.intelligent.dialog.NormalBroadDialog;
import com.cboda.intelligent.dialog.SexDialogBroadFra;
import com.cboda.intelligent.utils.BaseBroadCallback;
import com.cboda.intelligent.utils.OkHttpBroadHelper;
import com.cboda.intelligent.utils.SpotsCallBroadBack;
import com.cboda.intelligent.net.BroadUrl;
import com.cboda.intelligent.utils.GlideBroadBroadEngine;
import com.cboda.intelligent.utils.GlideBroadUtil;
import com.cboda.intelligent.utils.ListBroadUtil;
import com.cboda.intelligent.utils.StringBroadUtil;
import com.cboda.intelligent.utils.ToastBroadUtil;

import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropImageEngine;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cboda.chatui.user.UserViewBroadModel;
import cn.wildfirechat.model.ModifyMyInfoEntry;
import cn.wildfirechat.model.ModifyMyInfoType;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import okhttp3.Request;
import okhttp3.Response;
import top.zibin.luban.Luban;

public class UserInfoBroadActivity extends BaseSimpleBroadActivity implements View.OnClickListener {


    private ResultBroadBean bean;
    private ResultBroadBean oldBean;
    private UserViewBroadModel userViewModel;
    private ActivityUserBroadInfoBinding mBinding;
    private InterestBroadAdapter interestAdapter;

    @Override
    public boolean showOrHideTitleLayout() {
        return false;
    }

    @Override
    protected boolean useViewBinding() {
        return true;
    }

    @Override
    protected boolean transparentStatus() {
        return false;
    }

    @Nullable
    @Override
    protected Class<? extends ViewBinding> getViewBindingClass() {
        return ActivityUserBroadInfoBinding.class;
    }

//    @Override
//    protected int getLayoutId() {
//        return R.layout.activity_user_info;
//    }

    public final static int EDIT_PROFESSION = 10003;
    public final static int EDIT_INTEREST = 10004;
    public final static int EDIT_ADDRESS = 10005;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = (ActivityUserBroadInfoBinding) getBinding();
        ImmersionBar.with(this).statusBarView(mBinding.viewStatus).init();
        mBinding.toolbar.tvPageTitle.setText(getResources().getString(R.string.个人信息));

        userViewModel = ViewModelProviders.of(this).get(UserViewBroadModel.class);

        userInfo();
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(this);
        layoutManager.setFlexDirection(FlexDirection.ROW);        // 横向排列
        layoutManager.setFlexWrap(FlexWrap.WRAP);                 // 允许换行（标签流必选）
        layoutManager.setJustifyContent(JustifyContent.FLEX_START); // 左对齐
        layoutManager.setAlignItems(AlignItems.CENTER);
        mBinding.ryInterest.setLayoutManager(layoutManager);
        interestAdapter = new InterestBroadAdapter();
        mBinding.ryInterest.setAdapter(interestAdapter);

        mBinding.tvUserName.setOnClickListener(this);
        mBinding.llSex.setOnClickListener(this);
        mBinding.llErweima.setOnClickListener(this);
        mBinding.llQianming.setOnClickListener(this);
        mBinding.ivUserAvatar.setOnClickListener(this);
        mBinding.tvSave.setOnClickListener(this);
        mBinding.toolbar.rlLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mBinding.tvSave.isEnabled()) {
                    showGuideTip();
                } else {
                    finish();
                }

            }
        });
        mBinding.llProfession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String profession = "";
                if (bean == null || bean.data == null || bean.data.profession == null) {
                    profession = "";
                } else {
                    profession = bean.data.profession;
                }
                EditProfessionBroadActivity.Companion.toPage(UserInfoBroadActivity.this, profession);
            }
        });
        mBinding.llRegion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String city = "";
                if (bean == null || bean.data == null || bean.data.city == null) {
                    city = "";
                } else {
                    city = bean.data.city;
                }
                EditAddressBroadActivity.Companion.toPage(UserInfoBroadActivity.this, city);
            }
        });
        mBinding.llInterestEmpty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<String> list = new ArrayList<>();
                if (bean == null || bean.data == null || bean.data.memberInterestList == null) {
                    list.clear();
                } else {
                    list = bean.data.memberInterestList;
                }
                EditInterestBroadActivity.Companion.toPage(UserInfoBroadActivity.this, list);
            }
        });
        interestAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                List<String> list = new ArrayList<>();
                if (bean == null || bean.data == null || bean.data.memberInterestList == null) {
                    list.clear();
                } else {
                    list = bean.data.memberInterestList;
                }
                EditInterestBroadActivity.Companion.toPage(UserInfoBroadActivity.this, list);
            }
        });
        mBinding.ryInterest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<String> list = new ArrayList<>();
                if (bean == null || bean.data == null || bean.data.memberInterestList == null) {
                    list.clear();
                } else {
                    list = bean.data.memberInterestList;
                }
                EditInterestBroadActivity.Companion.toPage(UserInfoBroadActivity.this, list);
            }
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // 这里是你自定义的逻辑
                if (mBinding.tvSave.isEnabled()) {
                    showGuideTip();
                    // 注意：这里不要调用 isEnabled = false，也不要 remove
                    // 直接什么都不做，就拦截住了！
                } else {
                    // 条件不满足，直接放行返回
                    setEnabled(false);  // 临时关闭本回调
                    onBackPressed();    // 或者 getOnBackPressedDispatcher().onBackPressed();
                    // 也可以直接 finish();
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        if (view.getId() == R.id.iv_user_avatar) {
            String[] perms = {
                    Permission.READ_MEDIA_IMAGES,
                    Permission.CAMERA};
            if (XXPermissions.isGranted(this, perms)) {
                pmsSuccess();
            } else {
                new NormalBroadDialog(this, getString(R.string.温馨提示), getString(R.string.使用选择图片功能需要相机存储权限是否请求权限), getString(R.string.取消), getString(R.string.确定), true)
                        .setOnButtonClickListener(new NormalBroadDialog.OnButtonClick() {
                            @Override
                            public void OnRightClick() {
                                XXPermissions permission = XXPermissions.with(UserInfoBroadActivity.this).permission(perms);
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
        if (view.getId() == R.id.tv_user_name) {
            intent = new Intent(this, NickNameBroadActivity.class);
            intent.putExtra("name", mBinding.tvUserName.getText().toString());
            startActivityForResult(intent, 111);
        }
        if (view.getId() == R.id.llSex) {
            new SexDialogBroadFra().setShaixuanClick(bean.data.sex, new SexDialogBroadFra.OnShaixuanClick() {
                @Override
                public void onShaixuanClick(String sex) {
                    bean.data.sex = sex;
                    boolean isChange = false;
                    if (oldBean.data.sex == null) {
                        isChange = true;
                    } else if (oldBean.data.sex.isEmpty()) {
                        isChange = true;
                    } else {
                        Log.e("看看值", "新的值:" + bean.data.sex);
                        Log.e("看看老值", "老的值:" + oldBean.data.sex);
                        Log.e("看看值相等", "相等:" + oldBean.data.sex.equals(bean.data.sex));
                        if (!bean.data.sex.equals(oldBean.data.sex)) {
                            isChange = true;
                        }
                    }
                    showSaveButtonUI(isChange);
                    setInfo();
                }
            }).show(getSupportFragmentManager(), "");
        }
        if (view.getId() == R.id.llErweima) {
            new UserEwmBroadDialog(this, bean.data.qrcode, bean.data.icon, bean.data.name, bean.data.idno).show();
        }
        if (view.getId() == R.id.llQianming) {
            intent = new Intent(this, QianMingBroadActivity.class);
            intent.putExtra("sign", mBinding.tvQianming.getText().toString());
            startActivityForResult(intent, 444);

        }
        if (view.getId() == R.id.tvSave) {
            editUserinfo();
        }
    }

    private void updateToIM(String iconUrl, String name) {
        List<ModifyMyInfoEntry> entries = new ArrayList<>();
        entries.add(new ModifyMyInfoEntry(ModifyMyInfoType.Modify_Portrait, iconUrl));
        entries.add(new ModifyMyInfoEntry(ModifyMyInfoType.Modify_DisplayName, name));
        userViewModel.modifyMyInfo(entries).observe(this, booleanOperateResult -> {
            if (booleanOperateResult.isSuccess()) {
                finish();
            } else {
                ToastBroadUtil.show("更新信息到聊天服务器失败" + booleanOperateResult.getErrorCode());
            }


        });


    }

    /**
     * 修改资料
     */
    private void editUserinfo() {
        Map<String, Object> param = new HashMap<>();
        param.put("icon", bean.data.icon);
        param.put("name", bean.data.name);
        param.put("sex", bean.data.sex);
        param.put("sign", bean.data.sign);
        if (bean.data.city != null) {
            param.put("city", bean.data.city);
        }
        if (bean.data.profession != null) {
            param.put("profession", bean.data.profession);
        }
        if (bean.data.memberInterestList != null) {
            param.put("memberInterestList", bean.data.memberInterestList);
        }
        OkHttpBroadHelper.getInstance().post_json(this, BroadUrl.editUserinfo, param, new SpotsCallBroadBack<ResultBroadBean>(this) {

            @Override
            public void onSuccess(Response response, ResultBroadBean resultBean) {
                ToastBroadUtil.show(resultBean.msg);
                showSaveButtonUI(false);
                updateToIM(bean.data.icon, bean.data.name);

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
                photoLists.add(selectList.get(i).getCutPath());
            }
            if (photoLists != null && !photoLists.isEmpty()) {
                File file = new File(photoLists.get(0));//获取第一张图片
                if (file.exists()) {
                    uploadFiles(photoLists);
                }
            }
        } else if (requestCode == 111 && resultCode == 111) {
            bean.data.name = data.getStringExtra("name");
            boolean isChange = false;
            if (oldBean.data.name == null || oldBean.data.name.isEmpty()) {
                isChange = true;
            } else {
                if (!oldBean.data.name.equals(bean.data.name)) {
                    isChange = true;
                }
            }
            showSaveButtonUI(isChange);
            setInfo();
        } else if (requestCode == 444 && resultCode == 444) {
            bean.data.sign = data.getStringExtra("name");
            boolean isChange = false;
            if (oldBean.data.sign == null || oldBean.data.sign.isEmpty()) {
                isChange = true;
            } else {
                if (!oldBean.data.sign.equals(bean.data.sign)) {
                    isChange = true;
                }
            }
            showSaveButtonUI(isChange);
            setInfo();
        } else if (requestCode == EDIT_PROFESSION && resultCode == EDIT_PROFESSION) {
            bean.data.profession = data.getStringExtra("profession");

            boolean isChange = false;
            if (oldBean.data.profession == null || oldBean.data.profession.isEmpty()) {
                isChange = true;
            } else {
                if (!oldBean.data.profession.equals(bean.data.profession)) {
                    isChange = true;
                }
            }
            showSaveButtonUI(isChange);

            setInfo();
        } else if (requestCode == EDIT_ADDRESS && resultCode == EDIT_ADDRESS) {
            bean.data.city = data.getStringExtra("address");

            boolean isChange = false;
            if (oldBean.data.city == null || oldBean.data.city.isEmpty()) {
                isChange = true;
            } else {
                if (!oldBean.data.city.equals(bean.data.city)) {
                    isChange = true;
                }
            }
            showSaveButtonUI(isChange);

            setInfo();
        } else if (requestCode == EDIT_INTEREST && resultCode == EDIT_INTEREST) {
            ArrayList<String> interest = data.getStringArrayListExtra("interest");
            bean.data.memberInterestList = interest;
            boolean isChange = false;
            if (oldBean.data.memberInterestList == null || oldBean.data.memberInterestList.isEmpty()) {
                isChange = true;
            } else {
                if (!oldBean.data.memberInterestList.equals(bean.data.memberInterestList)) {
                    isChange = true;
                }
            }
            showSaveButtonUI(isChange);
            showInterestLayout();
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
                bean.data.icon = resultBean.urls.get(0);
                boolean isChange = false;
                if (oldBean.data.icon == null || oldBean.data.icon.isEmpty()) {
                    isChange = true;
                } else {
                    if (!oldBean.data.icon.equals(bean.data.icon)) {
                        isChange = true;
                    }
                }
                showInterestLayout();
                showSaveButtonUI(isChange);
                setInfo();
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
                .setCropEngine(new ImageFileCropEngine())
                .forResult(CHOOSE_REQUEST);//结果回调onActivityResult code
    }


    /**
     * 自定义裁剪
     */
    private class ImageFileCropEngine implements CropFileEngine {

        @Override
        public void onStartCrop(Fragment fragment, Uri srcUri, Uri destinationUri, ArrayList<String> dataSource, int requestCode) {
            UCrop.Options options = buildOptions();
            UCrop uCrop = UCrop.of(srcUri, destinationUri, dataSource);
            uCrop.withOptions(options);
            uCrop.setImageEngine(new UCropImageEngine() {
                @Override
                public void loadImage(Context context, String url, ImageView imageView) {
                    Glide.with(context).load(url).override(180, 180).into(imageView);
                }

                @Override
                public void loadImage(Context context, Uri url, int maxWidth, int maxHeight, OnCallbackListener<Bitmap> call) {
                    Glide.with(context).asBitmap().load(url).override(maxWidth, maxHeight).into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            if (call != null) {
                                call.onCall(resource);
                            }
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                            if (call != null) {
                                call.onCall(null);
                            }
                        }
                    });
                }
            });
            uCrop.start(fragment.requireActivity(), fragment, requestCode);
        }
    }


    /**
     * 配制UCrop，可根据需求自我扩展
     *
     * @return
     */
    private UCrop.Options buildOptions() {
        UCrop.Options options = new UCrop.Options();
        options.withAspectRatio(1, 1);
        return options;
    }

    private Gson mGson;

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

                bean = resultBean;
                if (mGson == null) {
                    mGson = new Gson();
                }
                oldBean = mGson.fromJson(mGson.toJson(bean), bean.getClass());
                showSaveButtonUI(false);
                setInfo();
            }

            @Override
            public void onError(Response response, int code, Exception e) {
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void setInfo() {
        GlideBroadUtil.setHeadImag(UserInfoBroadActivity.this, bean.data.icon, mBinding.ivUserAvatar);
        mBinding.tvUserName.setText(bean.data.name);
        if (!StringBroadUtil.isEmpty(bean.data.sex)) {
            if (bean.data.sex.equals("1")) {
                mBinding.tvSex.setText("男");
            } else {
                mBinding.tvSex.setText("女");
            }
        }
        mBinding.tvAccountId.setText(getString(R.string.账户ID) + ":" + (!TextUtils.isEmpty(bean.data.idno) ? bean.data.idno : bean.data.mId));
        mBinding.tvQianming.setText(bean.data.sign);
        mBinding.tvRegion.setText(bean.data.city);
        mBinding.tvProfession.setText(bean.data.profession);
        showInterestLayout();
    }

    private void showSaveButtonUI(boolean isModify) {
        mBinding.tvSave.setBackgroundResource(!isModify ? R.drawable.bg_btn_outline : R.drawable.bg_btn_blue);
        mBinding.tvSave.setTextColor(ContextCompat.getColor(UserInfoBroadActivity.this, !isModify ? com.cboda.chatui.R.color.color_3B47DD : R.color.white));
        mBinding.tvSave.setEnabled(isModify);

    }

    private void showInterestLayout() {
        if (bean.data.memberInterestList == null || bean.data.memberInterestList.isEmpty()) {
            mBinding.llInterestEmpty.setVisibility(View.VISIBLE);
            mBinding.ryInterest.setVisibility(View.GONE);
        } else {
            mBinding.ryInterest.setVisibility(View.VISIBLE);
            mBinding.llInterestEmpty.setVisibility(View.GONE);
            interestAdapter.setList(bean.data.memberInterestList);
        }
    }

    private void showGuideTip() {
        CommonTipBroadPop commonTipPop = new CommonTipBroadPop(
                this,
                "您的个人信息已更改",
                "是否更新并保存"
        );
        commonTipPop.setCancelBtn(getString(R.string.cancel));
        commonTipPop.setSureBtn(getString(R.string.action_save));
        commonTipPop.setSureInvoke(new Function0<Unit>() {
            @Override
            public Unit invoke() {
                editUserinfo();
                return null;
            }
        });
        commonTipPop.setCancelInvoke(new Function0<Unit>() {
            @Override
            public Unit invoke() {
                finish();
                return null;
            }
        });
        commonTipPop.showPopupWindow();
    }

}