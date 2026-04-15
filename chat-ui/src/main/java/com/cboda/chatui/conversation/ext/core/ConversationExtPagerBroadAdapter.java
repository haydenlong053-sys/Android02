/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversation.ext.core;

import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import java.util.List;

public class ConversationExtPagerBroadAdapter extends PagerAdapter {
    private SparseArray<ConversationExtPageBroadView> pagers = new SparseArray<>();
    private List<ConversationBroadExt> exts;
    private ConversationExtPageBroadView.OnExtViewClickListener listener;

    public ConversationExtPagerBroadAdapter(List<ConversationBroadExt> exts, ConversationExtPageBroadView.OnExtViewClickListener listener) {
        this.exts = exts;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        ConversationExtPageBroadView view = pagers.get(position);
        if (view == null) {
            view = new ConversationExtPageBroadView(container.getContext());
            view.setPageIndex(position);
            view.setOnExtViewClickListener(listener);
            int startIndex = ConversationExtPageBroadView.EXT_PER_PAGE * position;
            int end = startIndex + ConversationExtPageBroadView.EXT_PER_PAGE > exts.size() ? exts.size() : startIndex + ConversationExtPageBroadView.EXT_PER_PAGE;
            view.updateExtViews(exts.subList(startIndex, end));

            container.addView(view);
            pagers.put(position, view);
        }
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return exts == null ? 0 : (exts.size() + 9) / 10;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }
}
