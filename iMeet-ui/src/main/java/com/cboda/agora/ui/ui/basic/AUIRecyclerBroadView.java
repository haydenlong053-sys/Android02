package com.cboda.agora.ui.ui.basic;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.cboda.agora.ui.R;


public class AUIRecyclerBroadView extends RecyclerView {
    private AUISpaceItemBroadDecoration spaceItemDecoration;

    public AUIRecyclerBroadView(@NonNull Context context) {
        this(context, null);
    }

    public AUIRecyclerBroadView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AUIRecyclerBroadView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.AUIRecyclerBroadView, defStyleAttr, 0);
        int spaceHorizontal = typedArray.getDimensionPixelOffset(R.styleable.AUIRecyclerBroadView_aui_spaceHorizontal, 0);
        int spaceVertical = typedArray.getDimensionPixelOffset(R.styleable.AUIRecyclerBroadView_aui_spaceVertical, 0);
        int orientation = typedArray.getInt(R.styleable.AUIRecyclerBroadView_android_orientation, 0); // 0: h; 1: v
        Drawable listDivider = typedArray.getDrawable(R.styleable.AUIRecyclerBroadView_aui_listDivider);
        typedArray.recycle();

        spaceItemDecoration = new AUISpaceItemBroadDecoration(spaceHorizontal, spaceVertical);
        addItemDecoration(spaceItemDecoration);

        if (listDivider != null) {
            DividerItemDecoration divider = new DividerItemDecoration(context, orientation);
            divider.setDrawable(listDivider);
            addItemDecoration(divider);
        }
    }

    public void setSpaceHorizontal(int space) {
        if (spaceItemDecoration != null && spaceItemDecoration.getSpaceHorizontal() == space) {
            return;
        }
        if (spaceItemDecoration != null) {
            removeItemDecoration(spaceItemDecoration);
            spaceItemDecoration = new AUISpaceItemBroadDecoration(space, spaceItemDecoration.getSpaceVertical());
        } else {
            spaceItemDecoration = new AUISpaceItemBroadDecoration(space, 0);
        }
        addItemDecoration(spaceItemDecoration);
    }

    public void setSpaceVertical(int space) {
        if (spaceItemDecoration != null && spaceItemDecoration.getSpaceVertical() == space) {
            return;
        }
        if (spaceItemDecoration != null) {
            removeItemDecoration(spaceItemDecoration);
            spaceItemDecoration = new AUISpaceItemBroadDecoration(spaceItemDecoration.getSpaceHorizontal(), space);
        } else {
            spaceItemDecoration = new AUISpaceItemBroadDecoration(0, space);
        }
        addItemDecoration(spaceItemDecoration);
    }

    public int getSpaceHorizontal() {
        if (spaceItemDecoration != null) {
            return spaceItemDecoration.getSpaceHorizontal();
        }
        return 0;
    }

    public int getSpaceVertical() {
        if (spaceItemDecoration != null) {
            return spaceItemDecoration.getSpaceVertical();
        }
        return 0;
    }


}
