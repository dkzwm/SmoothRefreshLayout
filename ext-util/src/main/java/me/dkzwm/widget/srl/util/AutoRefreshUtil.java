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

import android.view.View;
import android.view.ViewConfiguration;
import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import me.dkzwm.widget.srl.ILifecycleObserver;
import me.dkzwm.widget.srl.SmoothRefreshLayout;

/** @author dkzwm */
public class AutoRefreshUtil
        implements ILifecycleObserver, SmoothRefreshLayout.OnStatusChangedListener, Runnable {
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
        mRefreshLayout.addOnStatusChangedListener(this);
    }

    @Override
    public void onDetached(SmoothRefreshLayout layout) {
        mRefreshLayout.removeCallbacks(this);
        mRefreshLayout.removeOnStatusChangedListener(this);
        mRefreshLayout = null;
    }

    public void autoRefresh(boolean atOnce, boolean autoRefreshUseSmoothScroll) {
        if (mRefreshLayout != null) {
            if (mStatus != SmoothRefreshLayout.SR_STATUS_INIT) return;
            if (mRefreshLayout.isNotYetInEdgeCannotMoveHeader()) {
                if (mRefreshLayout.isVerticalOrientation()) {
                    ScrollCompat.flingCompat(mTargetView, -mMaximumFlingVelocity);
                } else {
                    if (ViewCatcherUtil.isViewPager(mTargetView)) {
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
            ViewCompat.postOnAnimation(mRefreshLayout, this);
        }
    }

    public void autoLoadMore(boolean atOnce, boolean autoRefreshUseSmoothScroll) {
        if (mRefreshLayout != null) {
            if (mStatus != SmoothRefreshLayout.SR_STATUS_INIT) return;
            if (mRefreshLayout.isNotYetInEdgeCannotMoveFooter()) {
                if (mRefreshLayout.isVerticalOrientation()) {
                    ScrollCompat.flingCompat(mTargetView, mMaximumFlingVelocity);
                } else {
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
            ViewCompat.postOnAnimation(mRefreshLayout, this);
        }
    }

    @Override
    public void run() {
        if (mRefreshLayout != null) {
            if (mNeedToTriggerRefresh && !mRefreshLayout.isNotYetInEdgeCannotMoveHeader()) {
                if (mRefreshLayout.autoRefresh(
                        mCachedActionAtOnce, mCachedAutoRefreshUseSmoothScroll)) {
                    ScrollCompat.stopFling(mTargetView);
                    mNeedToTriggerRefresh = false;
                    mCachedActionAtOnce = false;
                    mCachedAutoRefreshUseSmoothScroll = false;
                    mRefreshLayout.removeCallbacks(this);
                    return;
                }
            } else if (mNeedToTriggerLoadMore && !mRefreshLayout.isNotYetInEdgeCannotMoveFooter()) {
                if (mRefreshLayout.autoLoadMore(
                        mCachedActionAtOnce, mCachedAutoRefreshUseSmoothScroll)) {
                    ScrollCompat.stopFling(mTargetView);
                    mNeedToTriggerLoadMore = false;
                    mCachedActionAtOnce = false;
                    mCachedAutoRefreshUseSmoothScroll = false;
                    mRefreshLayout.removeCallbacks(this);
                    return;
                }
            }
            ViewCompat.postOnAnimation(mRefreshLayout, this);
        }
    }

    @Override
    public void onStatusChanged(byte old, byte now) {
        mStatus = now;
    }
}
