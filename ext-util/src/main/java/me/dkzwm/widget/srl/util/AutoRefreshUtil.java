/*
 * MIT License
 *
 * Copyright (c) 2017 dkzwm
 * Copyright (c) 2015 liaohuqiu.net
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package me.dkzwm.widget.srl.util;

import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewConfiguration;
import me.dkzwm.widget.srl.ILifecycleObserver;
import me.dkzwm.widget.srl.SmoothRefreshLayout;
import me.dkzwm.widget.srl.indicator.IIndicator;

/**
 * Created by dkzwm on 2017/12/23.
 *
 * @author dkzwm
 */
public class AutoRefreshUtil
        implements ILifecycleObserver,
                SmoothRefreshLayout.OnNestedScrollChangedListener,
                SmoothRefreshLayout.OnUIPositionChangedListener {
    private SmoothRefreshLayout mRefreshLayout;
    private View mTargetView;
    private int mStatus;
    private boolean mNeedToTriggerRefresh = false;
    private boolean mNeedToTriggerLoadMore = false;
    private boolean mCachedActionAtOnce = false;
    private boolean mCachedAutoRefreshUseSmoothScroll = false;
    private int mMaximumFlingVelocity;

    public AutoRefreshUtil(@NonNull View targetScrollableView) {
        mTargetView = targetScrollableView;
        ViewConfiguration configuration = ViewConfiguration.get(targetScrollableView.getContext());
        mMaximumFlingVelocity = configuration.getScaledMaximumFlingVelocity();
    }

    @Override
    public void onAttached(SmoothRefreshLayout layout) {
        mRefreshLayout = layout;
        mRefreshLayout.addOnUIPositionChangedListener(this);
        mRefreshLayout.addOnNestedScrollChangedListener(this);
    }

    @Override
    public void onDetached(SmoothRefreshLayout layout) {
        mRefreshLayout.removeOnUIPositionChangedListener(this);
        mRefreshLayout.removeOnNestedScrollChangedListener(this);
        mRefreshLayout = null;
    }

    public void autoRefresh(boolean atOnce, boolean autoRefreshUseSmoothScroll) {
        if (mRefreshLayout != null) {
            if (mStatus != SmoothRefreshLayout.SR_STATUS_INIT) return;
            if (mRefreshLayout.isNotYetInEdgeCannotMoveHeader()) {
                if (mRefreshLayout.getSupportScrollAxis() == ViewCompat.SCROLL_AXIS_VERTICAL) {
                    ScrollCompat.flingCompat(mTargetView, -mMaximumFlingVelocity);
                } else if (mRefreshLayout.getSupportScrollAxis()
                        == ViewCompat.SCROLL_AXIS_HORIZONTAL) {
                    if (mTargetView instanceof ViewPager) {
                        final ViewPager pager = (ViewPager) mTargetView;
                        final PagerAdapter adapter = pager.getAdapter();
                        if (adapter == null) return;
                        if (adapter.getCount() <= 0) return;
                        pager.setCurrentItem(0, true);
                    } else {
                        HorizontalScrollCompat.flingCompat(mTargetView, -mMaximumFlingVelocity);
                    }
                }
                mNeedToTriggerRefresh = true;
                mCachedActionAtOnce = atOnce;
                mCachedAutoRefreshUseSmoothScroll = autoRefreshUseSmoothScroll;
            } else {
                mRefreshLayout.autoRefresh(atOnce, autoRefreshUseSmoothScroll);
                mNeedToTriggerRefresh = false;
                mCachedActionAtOnce = false;
                mCachedAutoRefreshUseSmoothScroll = false;
            }
        }
    }

    public void autoLoadMore(boolean atOnce, boolean autoRefreshUseSmoothScroll) {
        if (mRefreshLayout != null) {
            if (mStatus != SmoothRefreshLayout.SR_STATUS_INIT) return;
            if (mRefreshLayout.isNotYetInEdgeCannotMoveFooter()) {
                if (mRefreshLayout.getSupportScrollAxis() == ViewCompat.SCROLL_AXIS_VERTICAL) {
                    ScrollCompat.flingCompat(mTargetView, mMaximumFlingVelocity);
                } else if (mRefreshLayout.getSupportScrollAxis()
                        == ViewCompat.SCROLL_AXIS_HORIZONTAL) {
                    if (mTargetView instanceof ViewPager) {
                        final ViewPager pager = (ViewPager) mTargetView;
                        final PagerAdapter adapter = pager.getAdapter();
                        if (adapter == null) return;
                        if (adapter.getCount() <= 0) return;
                        pager.setCurrentItem(adapter.getCount() - 1, true);
                    } else {
                        HorizontalScrollCompat.flingCompat(mTargetView, mMaximumFlingVelocity);
                    }
                }
                mNeedToTriggerLoadMore = true;
                mCachedActionAtOnce = atOnce;
                mCachedAutoRefreshUseSmoothScroll = autoRefreshUseSmoothScroll;
            } else {
                mRefreshLayout.autoLoadMore(atOnce, autoRefreshUseSmoothScroll);
                mNeedToTriggerLoadMore = false;
                mCachedActionAtOnce = false;
                mCachedAutoRefreshUseSmoothScroll = false;
            }
        }
    }

    @Override
    public void onChanged(byte status, IIndicator indicator) {
        mStatus = status;
    }

    @Override
    public void onNestedScrollChanged() {
        if (mRefreshLayout != null) {
            if (mNeedToTriggerRefresh && !mRefreshLayout.isNotYetInEdgeCannotMoveHeader()) {
                if (mRefreshLayout.autoRefresh(
                        mCachedActionAtOnce, mCachedAutoRefreshUseSmoothScroll)) {
                    ScrollCompat.stopFling(mTargetView);
                    mNeedToTriggerRefresh = false;
                    mCachedActionAtOnce = false;
                    mCachedAutoRefreshUseSmoothScroll = false;
                }
            } else if (mNeedToTriggerLoadMore && !mRefreshLayout.isNotYetInEdgeCannotMoveFooter()) {
                if (mRefreshLayout.autoLoadMore(
                        mCachedActionAtOnce, mCachedAutoRefreshUseSmoothScroll)) {
                    ScrollCompat.stopFling(mTargetView);
                    mNeedToTriggerLoadMore = false;
                    mCachedActionAtOnce = false;
                    mCachedAutoRefreshUseSmoothScroll = false;
                }
            }
        }
    }
}
