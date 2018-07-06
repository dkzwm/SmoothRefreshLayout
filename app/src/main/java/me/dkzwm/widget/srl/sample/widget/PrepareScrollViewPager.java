package me.dkzwm.widget.srl.sample.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class PrepareScrollViewPager extends ViewPager {
    private boolean mCanScroll = false;

    public PrepareScrollViewPager(@NonNull Context context) {
        super(context);
    }

    public PrepareScrollViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void setEnableScroll(boolean canScroll) {
        mCanScroll = canScroll;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return mCanScroll && super.onTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mCanScroll && super.onInterceptTouchEvent(ev);
    }
}
