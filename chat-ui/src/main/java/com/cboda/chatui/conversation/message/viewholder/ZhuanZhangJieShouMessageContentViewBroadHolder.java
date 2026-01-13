package com.cboda.chatui.conversation.message.viewholder;

import android.view.View;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

import com.cboda.chatui.R;
import com.cboda.chatui.annotation.EnableContextBroadMenu;
import com.cboda.chatui.annotation.MessageContentBroadType;
import com.cboda.chatui.bridge.MyCommonBroadBridge;
import com.cboda.chatui.conversation.ConversationBroadFragment;
import com.cboda.chatui.conversation.message.model.UiBroadMessage;
import cn.wildfirechat.message.ZhuanZhangJieShouMessageContent;
import cn.wildfirechat.message.core.MessageDirection;
import com.cboda.baselibrary.common.BridgeImEnum;

@MessageContentBroadType(ZhuanZhangJieShouMessageContent.class)
@EnableContextBroadMenu
public class ZhuanZhangJieShouMessageContentViewBroadHolder extends NormalMessageContentViewBroadHolder {

    private AppCompatImageView hongbaoBg,hongbaoyijingBg;
    private View yilingqu, hongbaoTopBg;
    private TextView tvyilinghongbaodesc, tvlingqu;
    private Gson gson = new Gson();

    public ZhuanZhangJieShouMessageContentViewBroadHolder(ConversationBroadFragment fragment, RecyclerView.Adapter adapter, View itemView) {
        super(fragment, adapter, itemView);
        bindViews(itemView);
        bindEvents(itemView);
    }

    private void bindViews(View itemView) {
        hongbaoyijingBg = itemView.findViewById(R.id.hongbaoyijingBg);
        hongbaoBg = itemView.findViewById(R.id.hongbaoBg);
        yilingqu = itemView.findViewById(R.id.yilingqu);
        tvyilinghongbaodesc = itemView.findViewById(R.id.tvyilinghongbaodesc);
        hongbaoTopBg = itemView.findViewById(R.id.hongbaoTopBg);
        tvlingqu = itemView.findViewById(R.id.tvlingqu);
    }

    private void bindEvents(View itemView) {
        itemView.findViewById(R.id.zhuanzhanglay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (message != null) {
                    ZhuanZhangJieShouMessageContent zhuanzhangMessage = (ZhuanZhangJieShouMessageContent) message.message.content;
                    Map<String, String> hashMap = new HashMap<>();
                    hashMap.put("type","2001");
                    hashMap.put("zhuanzhangId",zhuanzhangMessage.zhuanzhangId);
                    hashMap.put("price",zhuanzhangMessage.price);
                    hashMap.put("hongbaoDesc",zhuanzhangMessage.hongbaoDesc);
                    hashMap.put("lingquStatus","1");
                    MyCommonBroadBridge.getInstance().setMyBridgeMethod(v.getContext(), BridgeImEnum.ACCEPT_RECEIVE_TRANSFER.getValue(), zhuanzhangMessage.zhuanzhangId + "|" + message.message.messageUid, gson.toJson(hashMap), message);
                }
            }
        });
    }

    @Override
    public void onBind(UiBroadMessage message) {
        ZhuanZhangJieShouMessageContent zhuanzhangMessage = (ZhuanZhangJieShouMessageContent) message.message.content;

        tvyilinghongbaodesc.setText("￥" + zhuanzhangMessage.price);

        if (message.message.direction == MessageDirection.Send) {
//            if ("1".equals(zhuanzhangMessage.lingquStatus)) {
                tvlingqu.setText("已收款");
//            } else {
//                tvlingqu.setText("发起了一笔转账");
//            }
            hongbaoBg.setImageResource(R.drawable.ic_hongbao_broad_bg);
            hongbaoyijingBg.setImageResource(R.drawable.ic_hongbao_broad_bg);
        } else {
            hongbaoBg.setImageResource(R.drawable.ic_hongbao_broad_bg2);
            hongbaoyijingBg.setImageResource(R.drawable.ic_hongbao_broad_bg2);
//            if ("1".equals(zhuanzhangMessage.lingquStatus)) {
                tvlingqu.setText("已收款");
//            } else {
//                tvlingqu.setText("对方发起了一笔转账");
//            }
        }

        hongbaoTopBg.setVisibility(View.GONE);
//        if ("1".equals(zhuanzhangMessage.lingquStatus)) {
//            hongbaoTopBg.setVisibility(View.VISIBLE);
//            hongbaoBg.setAlpha(0.5f);
            hongbaoyijingBg.setVisibility(View.VISIBLE);
            hongbaoBg.setVisibility(View.GONE);
        /*} else {
//            hongbaoBg.setAlpha(1f);
            hongbaoyijingBg.setVisibility(View.GONE);
            hongbaoBg.setVisibility(View.VISIBLE);
        }*/

    }
}
