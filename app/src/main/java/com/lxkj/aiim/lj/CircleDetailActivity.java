package com.lxkj.aiim.lj;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lxkj.aiim.R;
import com.lxkj.aiim.base.BaseSimpleActivity;
import com.lxkj.aiim.lj.adapter.HuiFuAdapter;
import com.lxkj.aiim.lj.adapter.ImageAdapter;
import com.lxkj.aiim.lj.bean.CircleCommListBean;
import com.lxkj.aiim.lj.bean.ResultBean;
import com.lxkj.aiim.lj.configuration.AppConsts;
import com.lxkj.aiim.lj.dialogfra.InputDialogFra;
import com.lxkj.aiim.lj.dialogfra.NormalDialog;
import com.lxkj.aiim.lj.http.BaseCallback;
import com.lxkj.aiim.lj.http.OkHttpHelper;
import com.lxkj.aiim.lj.http.Url;
import com.lxkj.aiim.lj.utils.GlideUtil;
import com.lxkj.aiim.lj.utils.ListUtil;
import com.lxkj.aiim.lj.utils.SharePrefUtil;
import com.lxkj.aiim.lj.utils.StringUtil;
import com.lxkj.aiim.lj.view.ExpandableTextView;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cc.shinichi.library.ImagePreview;
import okhttp3.Request;
import okhttp3.Response;

public class CircleDetailActivity extends BaseSimpleActivity implements View.OnClickListener {

    private HuiFuAdapter huiFuAdapter;
    private List<CircleCommListBean> listBeans = new ArrayList<>();
    private ImageAdapter imageAdapter;
    private List<String> imageList = new ArrayList<>();
    private String id;
    private boolean isShowMore = false;
    private ResultBean bean;
    private RoundedImageView riIcon;
    private TextView tvName;
    private TextView tvContent;
    private RoundedImageView riVideo;
    private ImageView imVideo;
    private RelativeLayout rlVideo;
    private RecyclerView ryImage;
    private TextView tvSite;
    private TextView tvTime;
    private ImageView imDelate;
    private LinearLayout llMore;
    private LinearLayout llMoreLayout;
    private ImageView imDianzan;
    private LinearLayout llDianzan;
    private LinearLayout llPinglun;
    private LinearLayout llMoreShow;
    private ImageView imDianzanAll;
    private ExpandableTextView tvDianzanName;
    private LinearLayout llDianzanAll;
    private RecyclerView ryHuifu;
    private LinearLayout llPinglunItem;
    private LinearLayout llPinglunAll;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_circle_detail;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
        getToolbarTitle().setText(getResources().getString(R.string.详情));

        id = getIntent().getStringExtra("id");

        imageAdapter = new ImageAdapter(this, imageList, 147, 3);
        ryImage.setLayoutManager(new GridLayoutManager(this, 3));
        ryImage.setAdapter(imageAdapter);
        imageAdapter.setOnItemClickListener(new ImageAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(int firstposition) {
                ImagePreview
                        .getInstance()
                        .setContext(CircleDetailActivity.this)
                        .setIndex(firstposition)
                        .setImageList(imageList)
                        .start();
            }
        });

        huiFuAdapter = new HuiFuAdapter(CircleDetailActivity.this, listBeans);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(CircleDetailActivity.this);
        ryHuifu.setLayoutManager(linearLayoutManager);
        ryHuifu.setAdapter(huiFuAdapter);
        huiFuAdapter.setOnItemClickListener(new HuiFuAdapter.OnItemClickListener() {
            @Override
            public void OnItemClickListener(int position, String content) {
                switch (content) {
                    case "回复":
                        new InputDialogFra().setShaixuanClick(listBeans.get(position).name, new InputDialogFra.OnShaixuanClick() {
                            @Override
                            public void onShaixuanClick(String content) {
                                quanComm(listBeans.get(position).id, content);
                            }
                        }).show(getSupportFragmentManager(), "");
                        break;
                    case "主页":
//                        Intent intent1 = new Intent(CircleDetailActivity.this, MyCircleActivity.class);
//                        intent1.putExtra("id",listBeans.get(position).userId);
//                        startActivity(intent1);
                        break;
                }
            }
        });

        llMore.setOnClickListener(this);
        imDelate.setOnClickListener(this);
        llDianzan.setOnClickListener(this);
        llPinglun.setOnClickListener(this);
        tvSite.setOnClickListener(this);
        rlVideo.setOnClickListener(this);

        quanInfo();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.llMore){
            if (isShowMore) {
                isShowMore = false;
                llMoreShow.setVisibility(View.GONE);
            } else {
                isShowMore = true;
                llMoreShow.setVisibility(View.VISIBLE);
            }
        }
        if (view.getId() == R.id.imDelate) {
            new NormalDialog(CircleDetailActivity.this, getString(R.string.温馨提示), "确认删除该条动态？", "取消", "确定", true)
                    .setOnButtonClickListener(new NormalDialog.OnButtonClick() {
                        @Override
                        public void OnRightClick() {
                            delQuan();
                        }

                        @Override
                        public void OnLeftClick() {
                        }
                    }).show();
        }
        if (view.getId() == R.id.llDianzan) {
            if (bean.data.isDz.equals("0")) {
                quanLike("1");
            } else {
                quanLike("0");
            }
        }
        if (view.getId() == R.id.llPinglun){
            new InputDialogFra().setShaixuanClick("", new InputDialogFra.OnShaixuanClick() {
                @Override
                public void onShaixuanClick(String content) {
                    quanComm("", content);
                }
            }).show(getSupportFragmentManager(), "");
        }

        if (view.getId() == R.id.rlVideo) {
            if (bean.data.type.equals("1")) {
                ImagePreview
                        .getInstance()
                        .setContext(this)
                        .setIndex(0)
                        .setImageList(bean.data.imgs)
                        .start();
            } else {
                Intent intent = new Intent(this, VideoPlayOnlyActivity.class);
                intent.putExtra("video", bean.data.video);
                intent.putExtra("videoImage", bean.data.video);
                startActivity(intent);
            }
        }

    }

    /**
     * 评论朋友圈
     */
    private void quanComm(String commId, String info) {
        Map<String, Object> param = new HashMap<>();
        param.put("id", id);
        param.put("commId", commId);
        param.put("info", info);
        OkHttpHelper.getInstance().post_json(this, Url.quanComm, param, new BaseCallback<ResultBean>() {
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
                CircleCommListBean circleCommListBean = new CircleCommListBean();
                circleCommListBean.id = resultBean.data.id;
                circleCommListBean.icon = resultBean.data.icon;
                circleCommListBean.name = resultBean.data.name;
                circleCommListBean.info = resultBean.data.info;
                circleCommListBean.huifu = resultBean.data.huifu;
                circleCommListBean.time = resultBean.data.time;
                listBeans.add(circleCommListBean);
                huiFuAdapter.notifyDataSetChanged();

                shuaxinquanInfo();
            }

            @Override
            public void onError(Response response, int code, Exception e) {
            }
        });
    }

    /**
     * 删除朋友圈
     */
    private void delQuan() {
        Map<String, Object> param = new HashMap<>();
        param.put("id", id);
        OkHttpHelper.getInstance().post_json(this, Url.delQuan, param, new BaseCallback<ResultBean>() {
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
                finish();
            }

            @Override
            public void onError(Response response, int code, Exception e) {
            }
        });
    }

    /**
     * 点赞朋友圈
     */
    private void quanLike(String type) {
        Map<String, Object> param = new HashMap<>();
        param.put("id", id);
        param.put("type", type);
        OkHttpHelper.getInstance().post_json(this, Url.quanLike, param, new BaseCallback<ResultBean>() {
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
                if (type.equals("0")) {
                    imDianzan.setImageResource(R.mipmap.ic_circle_zan_false);
                    imDianzanAll.setImageResource(R.mipmap.ic_dianzan_num_false);
                } else {
                    imDianzan.setImageResource(R.mipmap.ic_circle_zan_true);
                    imDianzanAll.setImageResource(R.mipmap.ic_circle_zan_true);
                }
                tvDianzanName.setText(resultBean.dzNames);

                shuaxinquanInfo();
            }

            @Override
            public void onError(Response response, int code, Exception e) {
            }
        });
    }

    /**
     * 朋友圈详情
     */
    private void quanInfo() {
        Map<String, Object> param = new HashMap<>();
        param.put("id", id);
        OkHttpHelper.getInstance().post_json(this, Url.quanInfo, param, new BaseCallback<ResultBean>() {
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

                bean = resultBean;

                GlideUtil.setHeadImag(CircleDetailActivity.this, resultBean.data.icon, riIcon);
                tvName.setText(resultBean.data.name);
                tvContent.setText(resultBean.data.info);
                if (resultBean.data.type.equals("1")) {//图片
                    if (resultBean.data.imgs.size() == 1) {
                        rlVideo.setVisibility(View.VISIBLE);
                        imVideo.setVisibility(View.GONE);
                        ryImage.setVisibility(View.GONE);
                        GlideUtil.setImag(CircleDetailActivity.this, resultBean.data.imgs.get(0), riVideo);
                    } else {
                        rlVideo.setVisibility(View.GONE);
                        ryImage.setVisibility(View.VISIBLE);
                        imageList.clear();
                        imageList.addAll(resultBean.data.imgs);
                        imageAdapter.notifyDataSetChanged();
                    }

                } else {
                    rlVideo.setVisibility(View.VISIBLE);
                    imVideo.setVisibility(View.VISIBLE);
                    ryImage.setVisibility(View.GONE);
                    GlideUtil.setImag(CircleDetailActivity.this, resultBean.data.vimg, riVideo);
                }

                if (StringUtil.isEmpty(resultBean.data.address)) {
                    tvSite.setVisibility(View.GONE);
                } else {
                    tvSite.setVisibility(View.VISIBLE);
                    tvSite.setText(resultBean.data.address);
                }
                tvTime.setText(resultBean.data.time);
                if (resultBean.data.userId.equals(SharePrefUtil.getString(CircleDetailActivity.this, AppConsts.UID, ""))) {
                    imDelate.setVisibility(View.VISIBLE);
                } else {
                    imDelate.setVisibility(View.GONE);
                }

                if (resultBean.data.isDz.equals("0")) {
                    imDianzan.setImageResource(R.mipmap.ic_circle_zan_false);
                    imDianzanAll.setImageResource(R.mipmap.ic_dianzan_num_false);
                } else {
                    imDianzan.setImageResource(R.mipmap.ic_circle_zan_true);
                    imDianzanAll.setImageResource(R.mipmap.ic_circle_zan_true);
                }

                tvDianzanName.setMovementMethod(LinkMovementMethod.getInstance());
                tvDianzanName.setHighlightColor(ContextCompat.getColor(CircleDetailActivity.this, android.R.color.transparent));
                if (StringUtil.isEmpty(resultBean.data.dzNames)) {
                    llDianzanAll.setVisibility(View.GONE);
                } else {
                    llDianzanAll.setVisibility(View.VISIBLE);
                    tvDianzanName.setText(resultBean.data.dzNames);
                    List<String> list1 = new ArrayList<>();
                    for (int i = 0; i < resultBean.data.likeList.size(); i++) {
                        list1.add(resultBean.data.likeList.get(i).name);
                    }
                    if (ListUtil.isEmpty(resultBean.data.likeList)) {
                        tvDianzanName.setOriginalText(resultBean.data.dzNames);
                    } else {
                        tvDianzanName.setOriginalText(showRequestRecord(resultBean.data.dzNames, list1));
                    }
                }

                if (!ListUtil.isEmpty(resultBean.data.commList)) {
                    llPinglunItem.setVisibility(View.VISIBLE);
                    listBeans.clear();
                    listBeans.addAll(resultBean.data.commList);
                    huiFuAdapter.notifyDataSetChanged();
                } else {
                    llPinglunItem.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(Response response, int code, Exception e) {
            }
        });
    }

    /**
     * 朋友圈详情
     */
    private void shuaxinquanInfo() {
        Map<String, Object> param = new HashMap<>();
        param.put("id", id);
        OkHttpHelper.getInstance().post_json(this, Url.quanInfo, param, new BaseCallback<ResultBean>() {
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

                bean = resultBean;

            }

            @Override
            public void onError(Response response, int code, Exception e) {
            }
        });
    }

    public SpannableString showRequestRecord(String desc, List<String> stringlist) {
        //创建 SpannableString 对象
        SpannableString mStyledText = new SpannableString(desc);
        for (int i = 0; i < stringlist.size(); i++) {
            Pattern mPattern = Pattern.compile(stringlist.get(i));
            Matcher mMatcher = mPattern.matcher(desc);
            while (mMatcher.find()) {
                int finalI = i;
                ClickableSpan what = new ClickableSpan() {
                    @Override
                    public void onClick(View view) {
                        //这里的代码是对按钮点击事件的处理。
                        for (int i = 0; i < bean.data.likeList.size(); i++) {
                            if (!StringUtil.isEmpty(bean.data.likeList.get(i).name)) {
                                if (stringlist.get(finalI).contains(bean.data.likeList.get(i).name)) {
                                    Intent intent1 = new Intent(CircleDetailActivity.this, MyCircleActivity.class);
                                    intent1.putExtra("id", bean.data.likeList.get(i).id);
                                    startActivity(intent1);
                                }
                            }
                        }
                    }

                    @Override
                    public void updateDrawState(TextPaint ds) {
                        super.updateDrawState(ds);
                        ds.setUnderlineText(false);//去掉下划线
                        ds.setColor(ContextCompat.getColor(CircleDetailActivity.this, R.color.huati));//设置字体颜色
                        ds.setTypeface(Typeface.DEFAULT_BOLD);
                    }
                };
                mStyledText.setSpan(what, mMatcher.start(), mMatcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        return mStyledText;
    }

    private void initViews() {
        riIcon = findViewById(R.id.riIcon);
        tvName = findViewById(R.id.tvName);
        tvContent = findViewById(R.id.tvContent);
        riVideo = findViewById(R.id.riVideo);
        imVideo = findViewById(R.id.imVideo);
        rlVideo = findViewById(R.id.rlVideo);
        ryImage = findViewById(R.id.ryImage);
        tvSite = findViewById(R.id.tvSite);
        tvTime = findViewById(R.id.tvTime);
        imDelate = findViewById(R.id.imDelate);
        llMore = findViewById(R.id.llMore);
        llMoreLayout = findViewById(R.id.llMoreLayout);
        imDianzan = findViewById(R.id.imDianzan);
        llDianzan = findViewById(R.id.llDianzan);
        llPinglun = findViewById(R.id.llPinglun);
        llMoreShow = findViewById(R.id.llMoreShow);
        imDianzanAll = findViewById(R.id.imDianzanAll);
        tvDianzanName = findViewById(R.id.tvDianzanName);
        llDianzanAll = findViewById(R.id.llDianzanAll);
        ryHuifu = findViewById(R.id.ryHuifu);
        llPinglunItem = findViewById(R.id.llPinglunItem);
        llPinglunAll = findViewById(R.id.llPinglunAll);
    }
}