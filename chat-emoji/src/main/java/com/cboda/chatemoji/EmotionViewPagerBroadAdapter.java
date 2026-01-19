package com.cboda.chatemoji;

import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.RelativeLayout;

import androidx.viewpager.widget.PagerAdapter;

import java.util.List;

import static com.cboda.chatemoji.EmotionBroadLayout.EMOJI_PER_PAGE;
import static com.cboda.chatemoji.EmotionBroadLayout.STICKER_PER_PAGE;

/**
 * CSDN_LQR
 * 表情控件的ViewPager适配器(emoji + 贴图)
 */

public class EmotionViewPagerBroadAdapter extends PagerAdapter {

    private int mPageCount = 0;

    private int mEmotionLayoutWidth;
    private int mEmotionLayoutHeight;

    private IEmotionSelectedBroadListener listener;

    public EmotionViewPagerBroadAdapter(int emotionLayoutWidth, int emotionLayoutHeight, boolean stickerVisible, IEmotionSelectedBroadListener listener) {
        mEmotionLayoutWidth = emotionLayoutWidth;
        mEmotionLayoutHeight = emotionLayoutHeight;
        mPageCount = (int) Math.ceil(EmojiBroadManager.getDisplayCount() / (float) EMOJI_PER_PAGE);
        if (stickerVisible) {
            for (int i = 0; i < StickerBroadManager.getInstance().getStickerCategories().size(); i++) {
                mPageCount += StickerBroadManager.getInstance().getStickerCategories().get(i).getStickers().size() / STICKER_PER_PAGE;
            }
        }
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return mPageCount == 0 ? 1 : mPageCount;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
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

        int tabPosition = positionToCategoryTabIndex(position);

        gridView.setTag((tabPosition & 0xFFF) << 12 | (position & 0xFFF)); // category index | pageIndex

        if (tabPosition == 0) {
            gridView.setOnItemClickListener(emojiListener);
            gridView.setAdapter(new EmojiBroadAdapter(context, mEmotionLayoutWidth, mEmotionLayoutHeight, position * EMOJI_PER_PAGE));
            gridView.setNumColumns(EmotionBroadLayout.EMOJI_COLUMNS);
        } else {
            int categoryStickerPageIndex = positionToCategoryPageIndex(position);
            StickerBroadCategory category = StickerBroadManager.getInstance().getCategory(StickerBroadManager.getInstance().getStickerCategories().get(tabPosition - 1).getName());
            gridView.setOnItemClickListener(stickerListener);
            gridView.setAdapter(new StickerBroadAdapter(context, category, mEmotionLayoutWidth, mEmotionLayoutHeight, categoryStickerPageIndex * STICKER_PER_PAGE));
            gridView.setNumColumns(EmotionBroadLayout.STICKER_COLUMNS);
        }

        rl.addView(gridView);
        container.addView(rl);
        return rl;
    }

    /**
     * 根据categoryTabIndex计算page position
     *
     * @param categoryTabIndex
     * @return
     */
    int categoryTabIndexToPagePosition(int categoryTabIndex) {
        int position = (int) Math.ceil(EmojiBroadManager.getDisplayCount() / (float) EMOJI_PER_PAGE);
        if (categoryTabIndex == 0) {
            return 0;
        } else {
            for (int i = 0; i < categoryTabIndex - 1; i++) {
                position += (int) Math.ceil(StickerBroadManager.getInstance().getStickerCategories().get(i).getStickers().size() / (float) EmotionBroadLayout.STICKER_PER_PAGE);
            }
        }
        return position;
    }

    /**
     * 根据page position，计算tab index
     *
     * @param position
     * @return
     */
    int positionToCategoryTabIndex(int position) {
        int categoryTabindex = 0;
        int emojiPageCount = (int) Math.ceil(EmojiBroadManager.getDisplayCount() / (float) EMOJI_PER_PAGE);
        if (position >= emojiPageCount) {
            int stickerPageCount = 0;
            for (int i = 0; i < StickerBroadManager.getInstance().getStickerCategories().size(); i++) {
                stickerPageCount += (int) Math.ceil(StickerBroadManager.getInstance().getStickerCategories().get(i).getStickers().size() / (float) EmotionBroadLayout.STICKER_PER_PAGE);
                if (position < emojiPageCount + stickerPageCount) {
                    categoryTabindex = 1 + i;
                    break;
                }
            }
        }
        return categoryTabindex;
    }

    /**
     * 根据page position，计算Category内部的pageIndex
     *
     * @param position
     * @return
     */
    int positionToCategoryPageIndex(int position) {
        int emojiPageCount = (int) Math.ceil(EmojiBroadManager.getDisplayCount() / (float) EMOJI_PER_PAGE);
        int categoryPageIndex = -1;
        if (position < emojiPageCount) {
            categoryPageIndex = position;
        } else {
            int stickerPageCount = 0;
            for (int i = 0; i < StickerBroadManager.getInstance().getStickerCategories().size(); i++) {
                categoryPageIndex = position - emojiPageCount - stickerPageCount;
                stickerPageCount += (int) Math.ceil(StickerBroadManager.getInstance().getStickerCategories().get(i).getStickers().size() / (float) EmotionBroadLayout.STICKER_PER_PAGE);
                if (position < emojiPageCount + stickerPageCount) {
                    break;
                }
            }
        }
        return categoryPageIndex;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    public AdapterView.OnItemClickListener emojiListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            int index = position + (Integer) parent.getTag() * EmotionBroadLayout.EMOJI_PER_PAGE;
            int count = EmojiBroadManager.getDisplayCount();
            if (position == EmotionBroadLayout.EMOJI_PER_PAGE || index >= count) {
                if (listener != null) {
                    listener.onEmojiSelected("/DEL");
                }
            } else {
                String text = EmojiBroadManager.getDisplayText((int) id);
                if (!TextUtils.isEmpty(text)) {
                    if (listener != null) {
                        listener.onEmojiSelected(text);
                    }
                }
            }
        }
    };
    public AdapterView.OnItemClickListener stickerListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            int tag = (int) parent.getTag();
            int pagePosition = tag & 0xFFF;
            int categoryIndex = (tag >> 12) & 0xFFF;
            int index = position;
            if (pagePosition > 0) {
                int categoryStartPagePosition = categoryTabIndexToPagePosition(categoryIndex);
                index += (pagePosition - categoryStartPagePosition) * EmotionBroadLayout.STICKER_PER_PAGE;
            }
            StickerBroadCategory category = StickerBroadManager.getInstance().getStickerCategories().get(categoryIndex - 1);
            List<StickerBroadItem> stickers = category.getStickers();

            if (index >= stickers.size()) {
//                Log.i("CSDN_LQR", "index " + index + " larger than size " + stickers.size());
                return;
            }

            if (listener != null) {
                StickerBroadItem sticker = stickers.get(index);
                StickerBroadCategory real = StickerBroadManager.getInstance().getCategory(sticker.getCategory());

                if (real == null) {
                    return;
                }

                listener.onStickerSelected(sticker.getCategory(), sticker.getName(), StickerBroadManager.getInstance().getStickerBitmapPath(sticker.getCategory(), sticker.getName()));
            }
        }
    };
}
