package com.cboda.chatemoji;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

/**
 * CSDN_LQR
 * 表情布局
 */
public class EmotionBroadLayout extends LinearLayout implements View.OnClickListener {

    public static final int EMOJI_COLUMNS = 7;
    public static final int EMOJI_ROWS = 3;
    public static final int EMOJI_PER_PAGE = EMOJI_COLUMNS * EMOJI_ROWS - 1;//最后一个是删除键

    public static final int STICKER_COLUMNS = 4;
    public static final int STICKER_ROWS = 2;
    public static final int STICKER_PER_PAGE = STICKER_COLUMNS * STICKER_ROWS;

    private int mMeasuredWidth;
    private int mMeasuredHeight;

    private int mTabPosi = 0;
    private Context mContext;
    private View emotionLayout;
    private ViewPagerBroadFixed mVpEmotioin;
    private ViewPagerBroadFixed vpEmotioin2;
    private LinearLayout mLlPageNumber;
    private LinearLayout llPageNumber2;
    private LinearLayout mLlTabContainer;
    private RelativeLayout mRlEmotionAdd;

    private int mTabCount;
    private SparseArray<View> mTabViewArray = new SparseArray<>();
    private EmotionBroadTab mSettingTab;
    private IEmotionSelectedBroadListener mEmotionSelectedListener;
    private IEmotionExtClickBroadListener mEmotionExtClickListener;
    private boolean mEmotionAddVisiable = false;
    private boolean mEmotionSettingVisiable = false;
    private RelativeLayout zidingyibiaoqiang;
    private RelativeLayout yehuobiaoqiang;
    //去掉之前贴图
    private boolean stickerVisible = false;

    public EmotionBroadLayout(Context context) {
        this(context, null);
    }

    public EmotionBroadLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EmotionBroadLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    public void setEmotionSelectedListener(IEmotionSelectedBroadListener emotionSelectedListener) {
        if (emotionSelectedListener != null) {
            this.mEmotionSelectedListener = emotionSelectedListener;
        } else {
            Log.i("CSDN_LQR", "IEmotionSelectedListener is null");
        }
    }


    public void setEmotionExtClickListener(IEmotionExtClickBroadListener emotionExtClickListener) {
        if (emotionExtClickListener != null) {
            this.mEmotionExtClickListener = emotionExtClickListener;
        } else {
            Log.i("CSDN_LQR", "IEmotionSettingTabClickListener is null");
        }
    }


    /**
     * 设置表情添加按钮的显隐
     *
     * @param visiable
     */
    public void setEmotionAddVisiable(boolean visiable) {
        mEmotionAddVisiable = visiable;
        if (mRlEmotionAdd != null) {
            mRlEmotionAdd.setVisibility(mEmotionAddVisiable ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * 设置表情设置按钮的显隐
     *
     * @param visiable
     */
    public void setEmotionSettingVisiable(boolean visiable) {
        mEmotionSettingVisiable = visiable;
        if (mSettingTab != null) {
            mSettingTab.setVisibility(mEmotionSettingVisiable ? View.VISIBLE : View.GONE);
        }
    }

    public void setStickerVisible(boolean visible) {
        stickerVisible = visible;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mMeasuredWidth = measureWidth(widthMeasureSpec);
        mMeasuredHeight = measureHeight(heightMeasureSpec);
        setMeasuredDimension(mMeasuredWidth, mMeasuredHeight);
    }


    private int measureWidth(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = LQREmotionBroadKit.dip2px(200);
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    private int measureHeight(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = LQREmotionBroadKit.dip2px(200);
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    private void init() {
        if (emotionLayout != null) {
            ((ViewGroup) emotionLayout).removeAllViews();
            removeView(emotionLayout);
        }
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        emotionLayout = inflater.inflate(R.layout.emotion_broad_layout, this);
        yehuobiaoqiang = findViewById(R.id.yehuobiaoqiang);
        zidingyibiaoqiang = findViewById(R.id.zidingyibiaoqiang);
        vpEmotioin2 = findViewById(R.id.vpEmotioin2);
        llPageNumber2 = findViewById(R.id.llPageNumber2);
        mVpEmotioin = (ViewPagerBroadFixed) findViewById(R.id.vpEmotioin);
        mLlPageNumber = (LinearLayout) findViewById(R.id.llPageNumber);
        mLlTabContainer = (LinearLayout) findViewById(R.id.llTabContainer);
        mRlEmotionAdd = (RelativeLayout) findViewById(R.id.rlEmotionAdd);
        yehuobiaoqiang.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                zidingyibiaoqiang.setBackgroundResource(R.drawable.shape_tab_broad_normal);
                yehuobiaoqiang.setBackgroundResource(R.drawable.shape_tab_broad_press);
                vpEmotioin2.setVisibility(GONE);
                mVpEmotioin.setVisibility(VISIBLE);
                llPageNumber2.setVisibility(GONE);
                mLlPageNumber.setVisibility(VISIBLE);
            }
        });
        zidingyibiaoqiang.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                zidingyibiaoqiang.setBackgroundResource(R.drawable.shape_tab_broad_press);
                yehuobiaoqiang.setBackgroundResource(R.drawable.shape_tab_broad_normal);
                vpEmotioin2.setVisibility(VISIBLE);
                mVpEmotioin.setVisibility(GONE);
                llPageNumber2.setVisibility(VISIBLE);
                mLlPageNumber.setVisibility(GONE);
            }
        });

//        mVpEmotioin.removeAllViews();
//        mLlPageNumber.removeAllViews();
//        mLlTabContainer.removeAllViews();

        setEmotionAddVisiable(mEmotionAddVisiable);
        emotionLayout.post(new Runnable() {
            @Override
            public void run() {
                initTabs();
                initListener();
            }
        });
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventBus.getDefault().unregister(this);
    }

    private Gson gson = new Gson();
    private List<EmoBroadBean.EmoItemBean> emiList = new ArrayList<>();

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMsgReceive(EmotionBroadEvent emotionBroadEvent) {
        if ("biaoqiangneirong".equals(emotionBroadEvent.type)) {
            EmoBroadBean emoBroadBean = gson.fromJson(emotionBroadEvent.params, EmoBroadBean.class);
            Log.e("野火", "表情数据--> " + emotionBroadEvent.params + "  " + mLlTabContainer.getChildCount());
            if (emoBroadBean != null && emoBroadBean.dataList != null && emoBroadBean.dataList.size() > 0) {
                zidingyibiaoqiang.setVisibility(VISIBLE);
                yehuobiaoqiang.setVisibility(VISIBLE);
                emiList.clear();
                emiList.addAll(emoBroadBean.dataList);
                List<List<EmoBroadBean.EmoItemBean>> newList = splitList(emiList,8);
//                for (int i = 0; i < newList.size(); i++) {
//                    List<EmoBean.EmoItemBean> innerList = new ArrayList<>();
//                    for (int j = 0; j < newList.get(i).size(); j++) {
//                        innerList.add(newList.get(i).get(j));
//                    }
//                }

                EmotionViewPagerAdapter2 fragmentAdapter = new EmotionViewPagerAdapter2(mMeasuredWidth, mMeasuredHeight, newList);
                vpEmotioin2.setAdapter(fragmentAdapter);
                vpEmotioin2.setOffscreenPageLimit(newList.size());

            }

        }
    }

    private List<List<EmoBroadBean.EmoItemBean>> splitList(
             List<EmoBroadBean.EmoItemBean> sourceList,
             int groupSize
    ) {
        int length = sourceList.size();
        // 计算可以分成多少组
        int num = (length + groupSize - 1) / groupSize;
        List<List<EmoBroadBean.EmoItemBean>> newList = new ArrayList<>(num);
        for (int i = 0; i < num; i++) {
            // 开始位置
            int fromIndex = i * groupSize;
            // 结束位置
            int toIndex = Math.min(((i + 1) * groupSize), length);
            newList.add(sourceList.subList(fromIndex, toIndex));
        }
        return newList;
    }

    private void initTabs() {
        //默认添加一个表情tab
        EmotionBroadTab emojiTab = new EmotionBroadTab(mContext, R.drawable.ic_broad_tab_emoji);
        mTabCount = 1;
        mLlTabContainer.addView(emojiTab);
        mTabViewArray.put(0, emojiTab);

        //添加所有的贴图tab
        if (stickerVisible) {
            List<StickerBroadCategory> stickerCategories = StickerBroadManager.getInstance().getStickerCategories();
            for (int i = 0; i < stickerCategories.size(); i++) {
                StickerBroadCategory category = stickerCategories.get(i);
                EmotionBroadTab tab = new EmotionBroadTab(mContext, category.getCoverImgPath());
                mLlTabContainer.addView(tab);
                mTabViewArray.put(i + 1, tab);
                mTabCount++;
            }
        }
        EventBus.getDefault().register(this);

        EventBus.getDefault().post(new EmotionBroadEvent("addEmotion", ""));

        //最后添加一个表情设置Tab
//        if (mEmotionSettingVisiable) {
//            mSettingTab = new EmotionTab(mContext, R.drawable.ic_emotion_setting);
//            StateListDrawable drawable = new StateListDrawable();
//            Drawable unSelected = mContext.getResources().getDrawable(R.color.white);
//            drawable.addState(new int[]{-android.R.attr.state_pressed}, unSelected);
//            Drawable selected = mContext.getResources().getDrawable(R.color.gray);
//            drawable.addState(new int[]{android.R.attr.state_pressed}, selected);
//            mSettingTab.setBackground(drawable);
//            mLlTabContainer.addView(mSettingTab);
//            mTabViewArray.put(mTabViewArray.size(), mSettingTab);
//        }

        selectTab(0);
        fillVpEmotioin(0);

    }

    private void initListener() {
        if (mLlTabContainer != null) {
            for (int position = 0; position < mTabCount; position++) {
                View tab = mLlTabContainer.getChildAt(position);
                tab.setTag(position);
                tab.setOnClickListener(this);
            }
        }

        mVpEmotioin.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                setCurPageCommon(position);
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        if (mRlEmotionAdd != null) {
            mRlEmotionAdd.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mEmotionExtClickListener != null) {
                        mEmotionExtClickListener.onEmotionAddClick(v);
                    }
                }
            });
        }
        if (mSettingTab != null) {
            mSettingTab.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mEmotionExtClickListener != null) {
                        mEmotionExtClickListener.onEmotionSettingClick(v);
                    }
                }
            });
        }
    }

    private void setCurPageCommon(int position) {
        mTabPosi = adapter.positionToCategoryTabIndex(position);
        if (mTabPosi == 0)
            setCategoryPageIndexIndicator(position, (int) Math.ceil(EmojiBroadManager.getDisplayCount() / (float) EmotionBroadLayout.EMOJI_PER_PAGE));
        else {
            StickerBroadCategory category = StickerBroadManager.getInstance().getStickerCategories().get(mTabPosi - 1);
            setCategoryPageIndexIndicator(position, (int) Math.ceil(category.getStickers().size() / (float) EmotionBroadLayout.STICKER_PER_PAGE));
        }
        selectTab(mTabPosi);
    }


    @Override
    public void onClick(View v) {
//        int targetTabIndex = (int) v.getTag();
//        if (targetTabIndex == mTabPosi) {
//            return;
//        }
//        mTabPosi = targetTabIndex;
//        selectTab(mTabPosi);
//        int position = adapter.categoryTabIndexToPagePosition(mTabPosi);
//        mLlPageNumber.removeAllViews();
//        mVpEmotioin.setCurrentItem(position, false);

        zidingyibiaoqiang.setBackgroundResource(R.drawable.shape_tab_broad_normal);
        selectTab(0);
    }

    private void selectTab(int tabPosi) {
//        if (tabPosi == mTabViewArray.size() - 1)
//            return;

        for (int i = 0; i < mTabCount; i++) {
            View tab = mTabViewArray.get(i);
            tab.setBackgroundResource(R.drawable.shape_tab_broad_normal);
        }
        mTabViewArray.get(tabPosi).setBackgroundResource(R.drawable.shape_tab_broad_press);

    }

    private EmotionViewPagerBroadAdapter adapter;

    private void fillVpEmotioin(int tabPosi) {
        adapter = new EmotionViewPagerBroadAdapter(mMeasuredWidth, mMeasuredHeight, stickerVisible, mEmotionSelectedListener);
        mVpEmotioin.setAdapter(adapter);
        mVpEmotioin.setOffscreenPageLimit(1);
        setCurPageCommon(0);
    }

    private void setCategoryPageIndexIndicator(int position, int pageCount) {
        int hasCount = mLlPageNumber.getChildCount();

        int categoryPageIndex = adapter.positionToCategoryPageIndex(position);

        ImageView ivCur = null;
        if (hasCount > pageCount) {
            for (int i = pageCount; i < hasCount; i++) {
                // 循环删除index是pageCount的view
                mLlPageNumber.removeViewAt(pageCount);
            }
        }
        for (int i = 0; i < pageCount; i++) {
            if (pageCount <= hasCount) {
                ivCur = (ImageView) mLlPageNumber.getChildAt(i);
            } else {
                if (i < hasCount) {
                    ivCur = (ImageView) mLlPageNumber.getChildAt(i);
                } else {
                    ivCur = new ImageView(mContext);
                    ivCur.setBackgroundResource(R.drawable.selector_view_pager_broad_indicator);
                    LayoutParams params = new LayoutParams(LQREmotionBroadKit.dip2px(8), LQREmotionBroadKit.dip2px(8));
                    ivCur.setLayoutParams(params);
                    params.leftMargin = LQREmotionBroadKit.dip2px(3);
                    params.rightMargin = LQREmotionBroadKit.dip2px(3);
                    mLlPageNumber.addView(ivCur);
                }
            }

            ivCur.setId(i);
            ivCur.setSelected(i == categoryPageIndex);
            ivCur.setVisibility(View.VISIBLE);
        }
    }
}
