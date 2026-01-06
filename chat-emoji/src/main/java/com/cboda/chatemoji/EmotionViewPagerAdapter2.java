package com.cboda.chatemoji;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

public class EmotionViewPagerAdapter2 extends PagerAdapter {

    private final List<List<EmoBroadBean.EmoItemBean>> newList;
    private int mEmotionLayoutWidth;
    private int mEmotionLayoutHeight;
    public EmotionViewPagerAdapter2(int emotionLayoutWidth, int emotionLayoutHeight,List<List<EmoBroadBean.EmoItemBean>> newList){
        mEmotionLayoutWidth = emotionLayoutWidth;
        mEmotionLayoutHeight = emotionLayoutHeight;
        this.newList = newList;
    }
    @Override
    public int getCount() {
        return newList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        Context context = container.getContext();
        RelativeLayout rl = new RelativeLayout(context);
        rl.setGravity(Gravity.CENTER);

        GridView gridView = new GridView(context);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        gridView.setLayoutParams(params);
        gridView.setGravity(Gravity.CENTER);
        gridView.setAdapter(new StickerBroadAdapter2(context,  mEmotionLayoutWidth, mEmotionLayoutHeight, newList.get(position)));
        gridView.setNumColumns(EmotionBroadLayout.STICKER_COLUMNS);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                EventBus.getDefault().post(new EmotionBroadEvent("fasongbiaoqing",newList.get(position).get(pos).emoji));
            }
        });
        rl.addView(gridView);
        container.addView(rl);
        return rl;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }
}
