package com.freddy.chat.group;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cboda.intelligent.R;

/** 绑定 {@link com.cboda.intelligent.R.layout#item_group_setting} 根节点。 */
public final class GroupSettingRow {

    public final TextView label;
    public final TextView value;
    public final ImageView arrow;
    public final View divider;
    public final View root;

    public GroupSettingRow(@NonNull View root) {
        this.root = root;
        label = root.findViewById(R.id.tv_label);
        value = root.findViewById(R.id.tv_value);
        arrow = root.findViewById(R.id.iv_arrow);
        divider = root.findViewById(R.id.divider);
    }

    public void bind(@NonNull String title, @Nullable String valueText, boolean showArrow) {
        label.setText(title);
        if (!TextUtils.isEmpty(valueText)) {
            value.setVisibility(View.VISIBLE);
            value.setText(valueText);
        } else {
            value.setVisibility(View.GONE);
        }
        arrow.setVisibility(showArrow ? View.VISIBLE : View.GONE);
    }

    public void hideDivider() {
        divider.setVisibility(View.GONE);
    }
}
