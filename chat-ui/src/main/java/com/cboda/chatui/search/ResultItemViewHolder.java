/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.search;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public abstract class ResultItemViewHolder<T> extends RecyclerView.ViewHolder {
    public ResultItemViewHolder(View itemView) {
        super(itemView);
    }

    abstract void onBind(T t);
}
