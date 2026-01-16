package com.cboda.intelligent.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cboda.baselibrary.common.MyEventCodeEnum;
import com.cboda.baselibrary.toast.ToastBroadUtils;
import com.cboda.baselibrary.utils.ELogUtilKt;
import com.cboda.baselibrary.utils.GlideUtilsKt;
import com.cboda.baselibrary.utils.VideoThumbnailUtils;
import com.cboda.chatui.bridge.MyBroadEvent;
import com.cboda.chatui.conversation.forward.ForwardBroadActivity;
import com.cboda.intelligent.R;
import com.cboda.intelligent.base.BaseSimpleBroadActivity;
import com.cboda.intelligent.adapter.MyCollectBroadAdapter;
import com.cboda.intelligent.bean.DataListBroadBean;
import com.cboda.intelligent.bean.ResultBroadBean;
import com.cboda.intelligent.utils.BaseBroadCallback;
import com.cboda.intelligent.utils.OkHttpBroadHelper;
import com.cboda.intelligent.net.BroadUrl;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener;

import org.greenrobot.eventbus.EventBus;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.wildfirechat.message.ImageMessageContent;
import cn.wildfirechat.message.Message;
import cn.wildfirechat.message.MessageContent;
import cn.wildfirechat.message.TextMessageContent;
import cn.wildfirechat.message.VideoMessageContent;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.remote.ChatManager;
import cn.wildfirechat.remote.SendMessageCallback;
import cn.wildfirechat.utils.WeChatImageUtils;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import okhttp3.Request;
import okhttp3.Response;

public class MyCollectBroadActivity extends BaseSimpleBroadActivity {


    private MyCollectBroadAdapter myCollectAdapter;
    private List<DataListBroadBean> listBeans = new ArrayList<>();
    private int page = 1, totalPage = 1;
    private ImageView ivNoData;
    private TextView tvNoData;
    private LinearLayout llNoData;
    private RecyclerView ryCollect;
    private SmartRefreshLayout smart;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_my_broad_collect;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
        getToolbarTitle().setText(getResources().getString(R.string.我的收藏));

        myCollectAdapter = new MyCollectBroadAdapter(this, listBeans);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        ryCollect.setLayoutManager(linearLayoutManager);
        ryCollect.setAdapter(myCollectAdapter);
        myCollectAdapter.setOnItemClickListener(new MyCollectBroadAdapter.OnItemClickListener() {
            @Override
            public void OnItemClickListener(int position) {
                DataListBroadBean dataListBean = listBeans.get(position);
                if (dataListBean.info == null || "null".equals(dataListBean.info)) {
                    ToastBroadUtils.show("服务器查询数据失败" + dataListBean.info);
                    return;
                }

                // 从 Intent 获取 qunid 和 uid
                Intent intent = getIntent();
                String qunid = intent != null ? intent.getStringExtra("qunid") : null;
                String uid = intent != null ? intent.getStringExtra("uid") : null;

                // 判断是单聊还是群聊
                Conversation conversation = new Conversation();
                if (!TextUtils.isEmpty(qunid)) {
                    // 群聊
                    conversation.type = Conversation.ConversationType.Group;
                    conversation.target = qunid;
                } else if (!TextUtils.isEmpty(uid)) {
                    // 单聊
                    conversation.type = Conversation.ConversationType.Single;
                    conversation.target = uid;
                } else {
//                    ToastBroadUtils.show("缺少会话信息（qunid 或 uid）");
                    return;
                }

                // 根据类型创建消息内容并发送
                String type = dataListBean.type;
                if ("1".equals(type)) {
                    // 文字消息
                    TextMessageContent textContent = new TextMessageContent(dataListBean.info);
                    sendMessage(conversation, textContent);
                } else if ("2".equals(type)) {
                    // 图片消息
                    GlideUtilsKt.getBitmapFromUrl(MyCollectBroadActivity.this, dataListBean.info, new Function1<Bitmap, Unit>() {
                        @Override
                        public Unit invoke(Bitmap bitmap) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (bitmap == null) {
                                        ToastBroadUtils.show("图片加载失败");
                                    } else {
                                        ImageMessageContent imageContent = new ImageMessageContent(dataListBean.info, bitmap.getWidth(), bitmap.getHeight());
                                        sendMessage(conversation, imageContent);
                                    }
                                }
                            });
                            return null;
                        }
                    });
                } else if ("3".equals(type)) {
                    // 视频消息
                    showLoading();
                    VideoThumbnailUtils.getVideoFirstFrame(MyCollectBroadActivity.this, dataListBean.info, new VideoThumbnailUtils.ThumbnailCallback() {
                        @Override
                        public void onSuccess(Bitmap bitmap) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    dismissLoading();
                                    VideoMessageContent videoContent;
                                    if (TextUtils.isEmpty(dataListBean.miao)) {
                                        videoContent = new VideoMessageContent(dataListBean.info, 0, 0, 0);
                                    } else {
                                        videoContent = new VideoMessageContent(dataListBean.info, Long.parseLong(dataListBean.miao), 0, 0);
                                    }
                                    // 将 Bitmap 转换为 byte[] 作为缩略图
                                    if (bitmap != null) {
                                        int[] imageSize = WeChatImageUtils.getImageSizeByOrgSizeToWeChat2(bitmap.getWidth(), bitmap.getHeight());
                                        Bitmap thumbnail = ThumbnailUtils.extractThumbnail(bitmap, imageSize[0] / 2, imageSize[1] / 2);
                                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                        thumbnail.compress(Bitmap.CompressFormat.JPEG, 75, baos);
                                        byte[] thumbnailBytes = baos.toByteArray();
                                        ELogUtilKt.eLogD("zhanglong", "size==>" + thumbnailBytes.length / 1024 + "kb");
                                        videoContent.setThumbnailBytes(thumbnailBytes);
                                    }
                                    sendMessage(conversation, videoContent);
                                }
                            });
                        }

                        @Override
                        public void onError(String error) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    dismissLoading();
                                    // 即使获取缩略图失败，也发送消息
                                    VideoMessageContent videoContent;
                                    if (TextUtils.isEmpty(dataListBean.miao)) {
                                        videoContent = new VideoMessageContent(dataListBean.info, 0, 0, 0);
                                    } else {
                                        videoContent = new VideoMessageContent(dataListBean.info, Long.parseLong(dataListBean.miao), 0, 0);
                                    }
                                    sendMessage(conversation, videoContent);
                                }
                            });
                        }
                    });
                } else {
                    ToastBroadUtils.show("暂不支持的分享类型");
                }
            }

            @Override
            public void OnItemChildClickListener(View view, int position) {
                DataListBroadBean dataListBean = listBeans.get(position);
                if (dataListBean.info == null || "null".equals(dataListBean.info)) {
                    ToastBroadUtils.show("服务器查询数据失败" + dataListBean.info);
                } else if ("1".equals(dataListBean.type)) {
                    Message message = new Message();
                    message.content = new TextMessageContent(dataListBean.info);
                    Intent intent = new Intent(MyCollectBroadActivity.this, ForwardBroadActivity.class);
                    intent.putExtra("message", message);
                    startActivity(intent);
                } else if ("2".equals(dataListBean.type)) {
                    GlideUtilsKt.getBitmapFromUrl(MyCollectBroadActivity.this, dataListBean.info, new Function1<Bitmap, Unit>() {
                        @Override
                        public Unit invoke(Bitmap bitmap) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (bitmap == null) {
                                        ToastBroadUtils.show("图片错误null");
                                    } else {
                                        Message message = new Message();
                                        message.content = new ImageMessageContent(dataListBean.info, bitmap.getWidth(), bitmap.getHeight());
                                        Intent intent = new Intent(MyCollectBroadActivity.this, ForwardBroadActivity.class);
                                        intent.putExtra("message", message);
                                        startActivity(intent);
                                    }
                                }
                            });
                            return null;
                        }
                    });

                } else if ("3".equals(dataListBean.type)) {
                    showLoading();
                    VideoThumbnailUtils.getVideoFirstFrame(MyCollectBroadActivity.this, dataListBean.info, new VideoThumbnailUtils.ThumbnailCallback() {
                        @Override
                        public void onSuccess(Bitmap bitmap) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    dismissLoading();
                                    if (bitmap == null) {
                                        ToastBroadUtils.show("视频错误null");
                                    } else {
                                        Message message = new Message();
                                        if (TextUtils.isEmpty(dataListBean.miao)) {
                                            message.content = new VideoMessageContent(dataListBean.info, 0, 0, 0);
                                        } else {
                                            message.content = new VideoMessageContent(dataListBean.info, Long.parseLong(dataListBean.miao), 0, 0);
                                        }
                                        // 将 Bitmap 转换为 byte[]
                                        int[] imageSize = WeChatImageUtils.getImageSizeByOrgSizeToWeChat2(bitmap.getWidth(), bitmap.getHeight());
                                        Bitmap thumbnail = ThumbnailUtils.extractThumbnail(bitmap, imageSize[0] / 2, imageSize[1] / 2);
                                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                        thumbnail.compress(Bitmap.CompressFormat.JPEG, 75, baos);
                                        byte[] thumbnailBytes = baos.toByteArray();
                                        ELogUtilKt.eLogD("zhanglong", "size==>" + thumbnailBytes.length / 1024 + "kb");
                                        ((VideoMessageContent) message.content).setThumbnailBytes(thumbnailBytes);
                                        Intent intent = new Intent(MyCollectBroadActivity.this, ForwardBroadActivity.class);
                                        intent.putExtra("message", message);
                                        startActivity(intent);
                                    }
                                }
                            });
                        }

                        @Override
                        public void onError(String error) {
                            dismissLoading();
                        }
                    });
                } else {
                    ToastBroadUtils.show("暂不支持的转发类型");
                }
            }
        });

        smart.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                if (page >= totalPage) {
                    refreshLayout.setNoMoreData(true);
                    return;
                }
                page++;
                collList();
            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                page = 1;
                collList();
                refreshLayout.setNoMoreData(false);
            }
        });


        collList();

    }


    /**
     * 我的收藏
     */
    private void collList() {
        Map<String, Object> param = new HashMap<>();
        param.put("pageNo", page);
        param.put("pageSize", "10");
        OkHttpBroadHelper.getInstance().post_json(this, BroadUrl.collList, param, new BaseBroadCallback<ResultBroadBean>() {
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
                if (null != resultBean.totalPage)
                    totalPage = Integer.parseInt(resultBean.totalPage);
                smart.finishLoadMore();
                smart.finishRefresh();

                if (page == 1) {
                    listBeans.clear();
                }
                // 过滤掉 type 等于 "4" 的数据
                List<DataListBroadBean> filteredList = new ArrayList<>();
                if (resultBean.dataList != null) {
                    for (DataListBroadBean bean : resultBean.dataList) {
                        if (!"4".equals(bean.type)) {
                            filteredList.add(bean);
                        }
                    }
                }
                listBeans.addAll(filteredList);
                myCollectAdapter.notifyDataSetChanged();

                if (listBeans.size() == 0) {
                    llNoData.setVisibility(View.VISIBLE);
                } else {
                    llNoData.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onError(Response response, int code, Exception e) {
            }
        });
    }

    /**
     * 获取错误信息
     */
    private String getErrorMessage(int errorCode) {
        switch (errorCode) {
            case 9:
                return "不在群组中";
            case 10:
                return "无效的消息";
            case 20:
                return "不在聊天室中";
            case 21:
                return "不在频道中";
            case 29:
                return "不在群组中";
            case 35:
                // 错误码 35 可能是：会话不存在、目标用户不存在、群组不存在、权限不足等
                return "会话不存在或无权发送消息";
            case 246:
                return "您已被对方拉黑";
            case 247:
                return "被禁止发送消息";
            case 248:
                return "无权限操作";
            case 250:
                return "发送频率过高";
            case 251:
                return "参数无效";
            case 253:
                return "目标不存在";
            default:
                return "未知错误";
        }
    }

    /**
     * 发送消息
     */
    private void sendMessage(Conversation conversation, MessageContent content) {
        // 添加日志，方便调试
//        android.util.Log.d("MyCollectBroadActivity", "发送消息 - 会话类型: " + conversation.type + ", 目标: " + conversation.target);
        Message message = new Message();
        message.conversation = conversation;
        message.content = content;
        message.sender = ChatManager.Instance().getUserId();

        ChatManager.Instance().sendMessage(message, new SendMessageCallback() {
            @Override
            public void onSuccess(long messageUid, long timestamp) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        EventBus.getDefault().post(new MyBroadEvent(MyEventCodeEnum.RELOAD_FORWARD_MESSAGE.getValue(), "", ""));
                        ToastBroadUtils.show("转发成功");
                        // 发送成功后关闭当前页面
                        finish();
                    }
                });
            }

            @Override
            public void onFail(int errorCode) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String errorMsg = getErrorMessage(errorCode);
                        ToastBroadUtils.show("发送失败：" + errorMsg + "（错误码：" + errorCode + "）");
                        finish();
                    }
                });
            }

            @Override
            public void onPrepare(long messageId, long savedTime) {
                // 消息已准备
            }
        });
    }


    private void initViews() {
        ivNoData = findViewById(R.id.ivNoData);
        tvNoData = findViewById(R.id.tvNoData);
        llNoData = findViewById(R.id.llNoData);
        ryCollect = findViewById(R.id.ryCollect);
        smart = findViewById(R.id.smart);
    }
}