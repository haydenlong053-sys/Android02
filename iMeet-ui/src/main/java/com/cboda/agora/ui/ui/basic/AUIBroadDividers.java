package com.cboda.agora.ui.basic;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cboda.agora.ui.R;

public class AUIBroadDividers extends FrameLayout {
    private View vLine;
    private TextView tvText;

    public AUIBroadDividers(@NonNull Context context) {
        this(context, null);
    }

    public AUIBroadDividers(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AUIBroadDividers(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, R.style.AUIBroadDividers);
        initView();
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.AUIBroadDividers, defStyleAttr, R.style.AUIBroadDividers);

        // line
        int dividersWidth = typedArray.getDimensionPixelSize(R.styleable.AUIBroadDividers_aui_dividers_line_width, 0);
        int dividerColor = typedArray.getColor(R.styleable.AUIBroadDividers_aui_dividers_line_color, Color.TRANSPARENT);
        int insertStart = typedArray.getDimensionPixelSize(R.styleable.AUIBroadDividers_aui_dividers_line_insertStart, 0);
        int insertEnd = typedArray.getDimensionPixelSize(R.styleable.AUIBroadDividers_aui_dividers_line_insertEnd, 0);
        int dashGap = typedArray.getDimensionPixelSize(R.styleable.AUIBroadDividers_aui_dividers_line_dash_gap, 0);
        int dashWidth = typedArray.getDimensionPixelSize(R.styleable.AUIBroadDividers_aui_dividers_line_dash_width, 0);

        LayoutParams layoutParams = (LayoutParams) vLine.getLayoutParams();
        layoutParams.height = dividersWidth + 1;
        layoutParams.leftMargin = insertStart;
        layoutParams.rightMargin = insertEnd;
        vLine.setLayoutParams(layoutParams);

        GradientDrawable lineDrawable = new GradientDrawable();
        lineDrawable.setShape(GradientDrawable.LINE);
        lineDrawable.setStroke(dividersWidth, dividerColor, dashWidth, dashGap);
        vLine.setBackground(lineDrawable);

        // Text
        Drawable textBackground = typedArray.getDrawable(R.styleable.AUIBroadDividers_android_background);
        String text = typedArray.getString(R.styleable.AUIBroadDividers_aui_dividers_text);
        int textSize = typedArray.getDimensionPixelSize(R.styleable.AUIBroadDividers_aui_dividers_textSize, 0);
        int textColor = typedArray.getColor(R.styleable.AUIBroadDividers_aui_dividers_textColor, 0);

        tvText.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        tvText.setBackground(textBackground);
        tvText.setTextColor(textColor);
        tvText.setText(text);

        typedArray.recycle();
    }

    private void initView(){
        View.inflate(getContext(), R.layout.aui_dividers_layout, this);
        vLine = findViewById(R.id.line);
        tvText = findViewById(R.id.text);
    }
}
