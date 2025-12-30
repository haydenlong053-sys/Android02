package cn.wildfire.chat.kit.conversation.message.viewholder;

import android.view.View;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.annotation.EnableContextMenu;
import cn.wildfire.chat.kit.annotation.MessageContentType;
import cn.wildfire.chat.kit.bridge.YeHuoCommonBridge;
import cn.wildfire.chat.kit.conversation.ConversationFragment;
import cn.wildfire.chat.kit.conversation.message.model.UiMessage;
import cn.wildfirechat.message.ZhuanZhangJieShouMessageContent;
import cn.wildfirechat.message.core.MessageDirection;

@MessageContentType(ZhuanZhangJieShouMessageContent.class)
@EnableContextMenu
public class ZhuanZhangJieShouMessageContentViewHolder extends NormalMessageContentViewHolder {

    private AppCompatImageView hongbaoBg,hongbaoyijingBg;
    private View yilingqu, hongbaoTopBg;
    private TextView tvyilinghongbaodesc, tvlingqu;
    private Gson gson = new Gson();

    public ZhuanZhangJieShouMessageContentViewHolder(ConversationFragment fragment, RecyclerView.Adapter adapter, View itemView) {
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
                    YeHuoCommonBridge.getInstance().setYeHuoQiaoJie(v.getContext(), "jieshoulingzhuanzhang", zhuanzhangMessage.zhuanzhangId + "|" + message.message.messageUid, gson.toJson(hashMap), message);
                }
            }
        });
    }

    @Override
    public void onBind(UiMessage message) {
        ZhuanZhangJieShouMessageContent zhuanzhangMessage = (ZhuanZhangJieShouMessageContent) message.message.content;

        tvyilinghongbaodesc.setText("￥" + zhuanzhangMessage.price);

        if (message.message.direction == MessageDirection.Send) {
//            if ("1".equals(zhuanzhangMessage.lingquStatus)) {
                tvlingqu.setText("已收款");
//            } else {
//                tvlingqu.setText("发起了一笔转账");
//            }
            hongbaoBg.setImageResource(R.drawable.ic_hongbao_bg);
            hongbaoyijingBg.setImageResource(R.drawable.ic_hongbao_bg);
        } else {
            hongbaoBg.setImageResource(R.drawable.ic_hongbao_bg2);
            hongbaoyijingBg.setImageResource(R.drawable.ic_hongbao_bg2);
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
