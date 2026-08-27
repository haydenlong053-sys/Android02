package com.freddy.chat.viewholder;

import static com.cboda.baselibrary.utils.ViewExpandUtilKt.setBackColorAndRadius;

import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.cboda.baselibrary.utils.OnSingleClickBroadListener;
import com.cboda.baselibrary.view.RoundedImageBroadView;
import com.cboda.chatui.R;
import com.freddy.chat.hongbao.HongBaoClickHelper;
import com.freddy.chat.utils.HongBaoMessageContentUtil;
import com.freddy.im.bean.AppMessage;
import com.hjq.shape.view.ShapeTextView;

import java.util.List;

/**
 * 自研红包气泡（复用 {@code chat_hongbao_broad_layout}，对齐野火 HongBaoMessageContentViewBroadHolder）。
 */
public final class HongBaoMessageVH extends BaseMessageVH {

    private View allGroup;
    private RoundedImageBroadView hongbaoBg;
    private TextView tvyilinghongbaodesc;
    private TextView tvhoongbaotips;
    private ShapeTextView statusTv;
    @NonNull
    private AppMessage boundMessage = new AppMessage();

    public HongBaoMessageVH(@NonNull View itemView) {
        super(itemView, com.cboda.baselibrary.R.layout.chat_hongbao_broad_layout, false);
        cacheViews();
        bindClick();
    }

    public void bind(@NonNull AppMessage message, int position, @NonNull MessageViewHolderContext context) {
        boundMessage = message;
        bindCommon(message, position, context);
        bindHongBaoUi(message, context);
    }

    private void cacheViews() {
        allGroup = contentView.findViewById(com.cboda.baselibrary.R.id.allGroup);
        hongbaoBg = contentView.findViewById(com.cboda.baselibrary.R.id.hongbaoBg);
        tvyilinghongbaodesc = contentView.findViewById(com.cboda.baselibrary.R.id.tvyilinghongbaodesc);
        tvhoongbaotips = contentView.findViewById(com.cboda.baselibrary.R.id.tvhoongbaotips);
        statusTv = contentView.findViewById(com.cboda.baselibrary.R.id.statusTv);
    }

    private void bindClick() {
        View allView = contentView.findViewById(com.cboda.baselibrary.R.id.allView);
        if (allView == null) {
            return;
        }
        allView.setOnClickListener(new OnSingleClickBroadListener() {
            @Override
            public void onSingleClick(View v) {
                HongBaoClickHelper.open(v.getContext(), boundMessage, boundContext);
            }
        });
    }

    private MessageViewHolderContext boundContext;

    private void bindHongBaoUi(@NonNull AppMessage message, @NonNull MessageViewHolderContext context) {
        this.boundContext = context;
        boolean sentByMe = message.isSender(context.currentUserId);
        String content = message.getContent();
        String lingquStatus = HongBaoMessageContentUtil.resolveLingquStatus(content);
        List<String> lqUsers = HongBaoMessageContentUtil.resolveLqUsers(content);
        String desc = HongBaoMessageContentUtil.resolveHongbaoDesc(content);
        tvyilinghongbaodesc.setText(desc);

        if ("1".equals(lingquStatus)
                || (!TextUtils.isEmpty(context.currentUserId) && lqUsers.contains(context.currentUserId))) {
            allGroup.setAlpha(1f);
            if (sentByMe) {
                statusTv.setVisibility(View.GONE);
                tvhoongbaotips.setText(itemView.getContext().getString(R.string.hongbao_unlocked_by_others));
            } else {
                statusTv.setVisibility(View.VISIBLE);
                setBackColorAndRadius(statusTv, com.cboda.baselibrary.R.color.color_E9A94F,
                        com.cboda.baselibrary.R.dimen.dp_0, com.cboda.baselibrary.R.dimen.dp_12,
                        com.cboda.baselibrary.R.dimen.dp_12, com.cboda.baselibrary.R.dimen.dp_0);
                tvhoongbaotips.setText(itemView.getContext().getString(R.string.hongbao_unlocked));
                statusTv.setText(itemView.getContext().getString(R.string.hongbao_received));
            }
        } else if ("3".equals(lingquStatus)) {
            allGroup.setAlpha(1f);
            statusTv.setVisibility(View.VISIBLE);
            statusTv.setText(itemView.getContext().getString(R.string.hongbao_expired));
            if (sentByMe) {
                setBackColorAndRadius(statusTv, com.cboda.baselibrary.R.color.color_777777,
                        com.cboda.baselibrary.R.dimen.dp_12, com.cboda.baselibrary.R.dimen.dp_0,
                        com.cboda.baselibrary.R.dimen.dp_0, com.cboda.baselibrary.R.dimen.dp_0);
            } else {
                setBackColorAndRadius(statusTv, com.cboda.baselibrary.R.color.color_777777,
                        com.cboda.baselibrary.R.dimen.dp_0, com.cboda.baselibrary.R.dimen.dp_12,
                        com.cboda.baselibrary.R.dimen.dp_12, com.cboda.baselibrary.R.dimen.dp_0);
            }
            tvhoongbaotips.setText(itemView.getContext().getString(R.string.hongbao_unlock_digital_future));
        } else if ("2".equals(lingquStatus) && !context.isGroupChat) {
            allGroup.setAlpha(1f);
            if (sentByMe) {
                statusTv.setVisibility(View.GONE);
                tvhoongbaotips.setText(itemView.getContext().getString(R.string.hongbao_rejected_by_others));
            } else {
                tvhoongbaotips.setText(itemView.getContext().getString(R.string.hongbao_rejected));
                statusTv.setVisibility(View.VISIBLE);
                setBackColorAndRadius(statusTv, com.cboda.baselibrary.R.color.color_44A7C8,
                        com.cboda.baselibrary.R.dimen.dp_0, com.cboda.baselibrary.R.dimen.dp_12,
                        com.cboda.baselibrary.R.dimen.dp_12, com.cboda.baselibrary.R.dimen.dp_0);
                statusTv.setText(itemView.getContext().getString(R.string.hongbao_rejected));
            }
        } else {
            allGroup.setAlpha(1f);
            tvhoongbaotips.setText(itemView.getContext().getString(R.string.hongbao_unlock_digital_future));
            statusTv.setVisibility(View.GONE);
        }
        if (hongbaoBg != null) {
            hongbaoBg.setAlpha(1f);
        }
    }
}
