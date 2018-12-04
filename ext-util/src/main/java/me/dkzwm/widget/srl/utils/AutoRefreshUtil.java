package me.dkzwm.widget.srl.utils;

import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
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
public class AutoRefreshUtil implements ILifecycleObserver,
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

    public void autoRefresh(boolean atOnce,
                            boolean autoRefreshUseSmoothScroll) {
        if (mRefreshLayout != null) {
            if (mStatus != SmoothRefreshLayout.SR_STATUS_INIT)
                return;
            if (mRefreshLayout.isNotYetInEdgeCannotMoveHeader()) {
                if (mRefreshLayout.getSupportScrollAxis() == ViewCompat.SCROLL_AXIS_VERTICAL) {
                    ScrollCompat.flingCompat(mTargetView, -mMaximumFlingVelocity);
                } else if (mRefreshLayout.getSupportScrollAxis() == ViewCompat
                        .SCROLL_AXIS_HORIZONTAL) {
                    HorizontalScrollCompat.flingCompat(mTargetView, -mMaximumFlingVelocity);
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

    public void autoLoadMore(boolean atOnce,
                             boolean autoRefreshUseSmoothScroll) {
        if (mRefreshLayout != null) {
            if (mStatus != SmoothRefreshLayout.SR_STATUS_INIT)
                return;
            if (mRefreshLayout.isNotYetInEdgeCannotMoveFooter()) {
                if (mRefreshLayout.getSupportScrollAxis() == ViewCompat.SCROLL_AXIS_VERTICAL) {
                    ScrollCompat.flingCompat(mTargetView, mMaximumFlingVelocity);
                } else if (mRefreshLayout.getSupportScrollAxis() == ViewCompat
                        .SCROLL_AXIS_HORIZONTAL) {
                    HorizontalScrollCompat.flingCompat(mTargetView, mMaximumFlingVelocity);
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
                if (mRefreshLayout.autoRefresh(mCachedActionAtOnce, mCachedAutoRefreshUseSmoothScroll)) {
                    ScrollCompat.stopFling(mTargetView);
                    mNeedToTriggerRefresh = false;
                    mCachedActionAtOnce = false;
                    mCachedAutoRefreshUseSmoothScroll = false;
                }
            } else if (mNeedToTriggerLoadMore && !mRefreshLayout.isNotYetInEdgeCannotMoveFooter()) {
                if (mRefreshLayout.autoLoadMore(mCachedActionAtOnce, mCachedAutoRefreshUseSmoothScroll)) {
                    ScrollCompat.stopFling(mTargetView);
                    mNeedToTriggerLoadMore = false;
                    mCachedActionAtOnce = false;
                    mCachedAutoRefreshUseSmoothScroll = false;
                }
            }
        }
    }

}
