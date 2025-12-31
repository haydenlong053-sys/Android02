package cn.wildfire.chat.kit.conversation.message.viewholder;

import static com.cboda.baselibrary.utils.ELogUtilKt.eLogD;
import static com.cboda.baselibrary.utils.ViewExpandUtilKt.setBackColorAndRadius;
import static com.blankj.utilcode.util.SizeUtils.dp2px;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.cboda.baselibrary.utils.OnSingleClickListener;
import com.cboda.baselibrary.view.RoundedImageView;
import com.hjq.shape.view.ShapeTextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.wildfire.chat.kit.annotation.EnableContextMenu;
import cn.wildfire.chat.kit.annotation.MessageContentType;
import cn.wildfire.chat.kit.bridge.YeHuoCommonBridge;
import cn.wildfire.chat.kit.conversation.ConversationFragment;
import cn.wildfire.chat.kit.conversation.message.model.UiMessage;
import cn.wildfirechat.message.HongBaoMessageContent;
import cn.wildfirechat.message.core.MessageDirection;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.remote.ChatManager;

@MessageContentType(HongBaoMessageContent.class)
@EnableContextMenu
public class HongBaoMessageContentViewHolder extends NormalMessageContentViewHolder {
    private RoundedImageView hongbaoBg;
    private View allGroup;
    private TextView tvyilinghongbaodesc, tvhoongbaotips;
    private ShapeTextView statusTv;

    public HongBaoMessageContentViewHolder(ConversationFragment fragment, RecyclerView.Adapter adapter, View itemView) {
        super(fragment, adapter, itemView);
        bindViews(itemView);
        bindEvents(itemView);
    }

    private void bindViews(View itemView) {
        statusTv = itemView.findViewById(com.cboda.baselibrary.R.id.statusTv);
        allGroup = itemView.findViewById(com.cboda.baselibrary.R.id.allGroup);
        hongbaoBg = itemView.findViewById(com.cboda.baselibrary.R.id.hongbaoBg);
        tvyilinghongbaodesc = itemView.findViewById(com.cboda.baselibrary.R.id.tvyilinghongbaodesc);
        tvhoongbaotips = itemView.findViewById(com.cboda.baselibrary.R.id.tvhoongbaotips);
    }

    private void bindEvents(View itemView) {

        itemView.findViewById(com.cboda.baselibrary.R.id.allView).setOnClickListener(new OnSingleClickListener() {

            @Override
            public void onSingleClick(View v) {
                if (message != null) {
                    HongBaoMessageContent zhuanzhangMessage = (HongBaoMessageContent) message.message.content;
                    String params2 = "";
                    if (message.message.direction == MessageDirection.Send) {
                        params2 = "1";
                    }
                    JSONObject objWrite = new JSONObject();
                    try {
                        objWrite.put("type", "2000");
                        objWrite.put("hongbaoId", zhuanzhangMessage.hongbaoId);
                        objWrite.put("price", zhuanzhangMessage.price);
                        objWrite.put("hongbaoDesc", zhuanzhangMessage.hongbaoDesc);
                        objWrite.put("lingquStatus", zhuanzhangMessage.lingquStatus);
                        objWrite.put("hongbaoLeixing", zhuanzhangMessage.hongbaoLeixing);
                        objWrite.put("zhuanshuUserId", zhuanzhangMessage.zhuanshuUserId);
                        objWrite.put("zhuanshuUserName", zhuanzhangMessage.zhuanshuUserName);
                        objWrite.put("zhuanshuUserAvatar", zhuanzhangMessage.zhuanshuUserAvatar);
                        if (zhuanzhangMessage.lqUsers != null) {
                            JSONArray objArray = new JSONArray();
                            for (int i = 0; i < zhuanzhangMessage.lqUsers.size(); i++) {
                                objArray.put(i, zhuanzhangMessage.lqUsers.get(i));
                            }
                            objWrite.put("lqUsers", objArray);
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    String lingstatus = zhuanzhangMessage.lingquStatus;
                    if (!"1".equals(lingstatus)) {
                        if (zhuanzhangMessage.lqUsers != null && zhuanzhangMessage.lqUsers.contains(ChatManager.Instance().getUserId())) {
                            lingstatus = "1";
                        }
                    }
                    eLogD("hbInfoClass", "setYeHuoQiaoJie");
                    if (message.message.conversation.type == Conversation.ConversationType.Group) {
                        YeHuoCommonBridge.getInstance().setYeHuoQiaoJie(tvhoongbaotips.getContext(), "lingqunhongbao", zhuanzhangMessage.hongbaoId + "|" + lingstatus + "|" + message.message.messageUid + "|" + objWrite, params2, message);
                    } else {
                        YeHuoCommonBridge.getInstance().setYeHuoQiaoJie(tvhoongbaotips.getContext(), "linghongbao", zhuanzhangMessage.hongbaoId + "|" + lingstatus + "|" + message.message.messageUid + "|" + objWrite, params2, message);
                    }

                }
            }
        });
    }

    @Override
    public void onBind(UiMessage message) {

        if (message.message.direction == MessageDirection.Send) {
            hongbaoBg.setCornerRadius(dp2px(12f), 0f, 0f, 0f);
        } else {
            hongbaoBg.setCornerRadius(0f, dp2px(12f), 0f, 0f);
        }
        HongBaoMessageContent zhuanzhangMessage = (HongBaoMessageContent) message.message.content;
//        if ("3".equals(zhuanzhangMessage.hongbaoLeixing)) {
//            //专属
//            tvhoongbaotips.setText("已解锁");
//        } else {
//            if (message.message.direction == MessageDirection.Send) {
//                tvhoongbaotips.setText("已解锁");
//            } else {
//                tvhoongbaotips.setText("已解锁");
//            }
//        }

        if ("1".equals(zhuanzhangMessage.lingquStatus)) {
//            hongbaoTopBg.setVisibility(View.VISIBLE);
//            hongbaoBg.setAlpha(0.5f);
            allGroup.setAlpha(0.45f);
//            if (!"3".equals(zhuanzhangMessage.hongbaoLeixing)) {
//                tvhoongbaotips.setText("已解锁");
//            }
            if (message.message.direction == MessageDirection.Send) {
                statusTv.setVisibility(View.GONE);
                tvhoongbaotips.setText("已被解锁");
            } else {
                statusTv.setVisibility(View.VISIBLE);
                setBackColorAndRadius(statusTv, com.cboda.baselibrary.R.color.color_E9A94F, com.cboda.baselibrary.R.dimen.dp_0, com.cboda.baselibrary.R.dimen.dp_12, com.cboda.baselibrary.R.dimen.dp_12, com.cboda.baselibrary.R.dimen.dp_0);
                tvhoongbaotips.setText("已解锁");
                statusTv.setText("已领取");
            }
        } else if ("3".equals(zhuanzhangMessage.lingquStatus)) {
            allGroup.setAlpha(1f);
            statusTv.setVisibility(View.VISIBLE);
            statusTv.setText("已过期");
            if (message.message.direction == MessageDirection.Send) {
                setBackColorAndRadius(statusTv, com.cboda.baselibrary.R.color.color_777777, com.cboda.baselibrary.R.dimen.dp_12, com.cboda.baselibrary.R.dimen.dp_0, com.cboda.baselibrary.R.dimen.dp_0, com.cboda.baselibrary.R.dimen.dp_0);
            } else {
                setBackColorAndRadius(statusTv, com.cboda.baselibrary.R.color.color_777777, com.cboda.baselibrary.R.dimen.dp_0, com.cboda.baselibrary.R.dimen.dp_12, com.cboda.baselibrary.R.dimen.dp_12, com.cboda.baselibrary.R.dimen.dp_0);
            }
            tvhoongbaotips.setText("解锁你的数字未来");
        } else {
            if (zhuanzhangMessage.lqUsers != null && zhuanzhangMessage.lqUsers.contains(ChatManager.Instance().getUserId())) {
//                hongbaoTopBg.setVisibility(View.VISIBLE);
//                hongbaoBg.setAlpha(0.5f);
                allGroup.setAlpha(0.45f);
//                if (!"3".equals(zhuanzhangMessage.hongbaoLeixing)) {
                if (message.message.direction == MessageDirection.Send) {
                    statusTv.setVisibility(View.GONE);
                    tvhoongbaotips.setText("已被解锁");
                } else {
                    tvhoongbaotips.setText("已解锁");
                    statusTv.setVisibility(View.VISIBLE);
                    setBackColorAndRadius(statusTv, com.cboda.baselibrary.R.color.color_E9A94F, com.cboda.baselibrary.R.dimen.dp_0, com.cboda.baselibrary.R.dimen.dp_12, com.cboda.baselibrary.R.dimen.dp_12, com.cboda.baselibrary.R.dimen.dp_0);
                    statusTv.setText("已领取");
                }
//                }
            } else if ("2".equals(zhuanzhangMessage.lingquStatus) && message.message.conversation.type == Conversation.ConversationType.Single) {
                allGroup.setAlpha(0.45f);
//                if (!"3".equals(zhuanzhangMessage.hongbaoLeixing)) {
                if (message.message.direction == MessageDirection.Send) {
                    statusTv.setVisibility(View.GONE);
                    tvhoongbaotips.setText("已被拒绝");
                } else {
                    tvhoongbaotips.setText("已拒绝");
                    statusTv.setVisibility(View.VISIBLE);
                    setBackColorAndRadius(statusTv, com.cboda.baselibrary.R.color.color_44A7C8, com.cboda.baselibrary.R.dimen.dp_0, com.cboda.baselibrary.R.dimen.dp_12, com.cboda.baselibrary.R.dimen.dp_12, com.cboda.baselibrary.R.dimen.dp_0);
                    statusTv.setText("已拒绝");
                }

            } else {
//                hongbaoBg.setAlpha(1f);
                allGroup.setAlpha(1f);
                tvhoongbaotips.setText("解锁你的数字未来");
                statusTv.setVisibility(View.GONE);
//                if (!"3".equals(zhuanzhangMessage.hongbaoLeixing)) {
//                    tvhoongbaotips.setText("解锁你的数字未来");
//                }
            }
        }
        tvyilinghongbaodesc.setText(zhuanzhangMessage.hongbaoDesc);

    }
}
