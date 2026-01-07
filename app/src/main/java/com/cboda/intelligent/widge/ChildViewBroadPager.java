package com.cboda.intelligent.widge;


import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.viewpager.widget.ViewPager;

public class ChildViewBroadPager extends ViewPager {
    private final static String TAG = "ChildViewBroadPager";
    private int maxItemPosition;

    public ChildViewBroadPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                getParent().requestDisallowInterceptTouchEvent(true);
                break;

            case MotionEvent.ACTION_MOVE:

                if (isScrollToLeft() || isScrollToRight())
                {
                    getParent().requestDisallowInterceptTouchEvent(false);
                }

                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                getParent().requestDisallowInterceptTouchEvent(false);
                break;

            default:
        }
        return super.onTouchEvent(ev);
    }

    /**
     * 是否已经滑到了最右边
     *
     * @return
     */
    private boolean isScrollToRight() {
        return getCurrentItem()==maxItemPosition;
    }

    /**
     * 是否已经滑到了最左边
     * @return
     */
    private boolean isScrollToLeft() {
        return getCurrentItem() == 0;
    }

    public void setMaxItemPosition(int maxItemPosition){
        this.maxItemPosition=maxItemPosition;
    }
}
