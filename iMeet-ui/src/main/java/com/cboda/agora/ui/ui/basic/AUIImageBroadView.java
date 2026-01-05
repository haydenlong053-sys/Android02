package com.cboda.agora.ui.ui.basic;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

import com.google.android.material.imageview.ShapeableImageView;

public class AUIImageBroadView extends ShapeableImageView {
    public AUIImageBroadView(Context context) {
        this(context, null);
    }

    public AUIImageBroadView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public AUIImageBroadView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
}
