package com.cboda.chatemoji;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;

import java.util.List;

public class StickerBroadAdapter2 extends BaseAdapter {

    private final Context mContext;
    private final List<EmoBroadBean.EmoItemBean> dataList;
    private int mEmotionLayoutWidth;
    private int mEmotionLayoutHeight;
    private float mPerWidth;
    private float mPerHeight;
    private float mIvSize;
    
    public StickerBroadAdapter2(Context context, int emotionLayoutWidth, int emotionLayoutHeight, List<EmoBroadBean.EmoItemBean> dataList){
        this.mContext = context;

        mEmotionLayoutWidth = emotionLayoutWidth;
        mEmotionLayoutHeight = emotionLayoutHeight - LQREmotionBroadKit.dip2px(35 + 26 + 50);//减去底部的tab高度、小圆点的高度才是viewpager的高度，再减少30dp是让表情整体的顶部和底部有个外间距
        mPerWidth = mEmotionLayoutWidth * 1f / EmotionBroadLayout.STICKER_COLUMNS;
        mPerHeight = mEmotionLayoutHeight * 1f / EmotionBroadLayout.STICKER_ROWS;

        float ivWidth = mPerWidth * .8f;
        float ivHeight = mPerHeight * .8f;
        mIvSize = Math.min(ivWidth, ivHeight);
        this.dataList = dataList;
    }
    
    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public Object getItem(int position) {
        return dataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
       StickerViewHolder2 viewHolder;
        if (convertView == null) {
            RelativeLayout rl = new RelativeLayout(mContext);
            rl.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, (int) mPerHeight));

            ImageView imageView = new ImageView(mContext);
//            imageView.setImageResource(R.drawable.ic_tab_emoji);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.width = (int) mIvSize;
            params.height = (int) mIvSize;
            params.addRule(RelativeLayout.CENTER_IN_PARENT);
            imageView.setLayoutParams(params);

            rl.addView(imageView);

            viewHolder = new StickerViewHolder2();
            viewHolder.mImageView = imageView;

            convertView = rl;
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (StickerViewHolder2) convertView.getTag();
        }

        Glide.with(mContext).load(dataList.get(position).emoji).into(viewHolder.mImageView);

        return convertView;
    }

    class StickerViewHolder2 {
        public ImageView mImageView;
    }
}
