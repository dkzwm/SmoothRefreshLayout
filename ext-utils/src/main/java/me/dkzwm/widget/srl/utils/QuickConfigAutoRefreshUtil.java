package me.dkzwm.widget.srl.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;

import me.dkzwm.widget.srl.ILifecycleObserver;
import me.dkzwm.widget.srl.SmoothRefreshLayout;
import me.dkzwm.widget.srl.indicator.IIndicator;

/**
 * Created by dkzwm on 2017/12/23.
 *
 * @author dkzwm
 */
public class QuickConfigAutoRefreshUtil implements ILifecycleObserver, ViewTreeObserver
        .OnScrollChangedListener, SmoothRefreshLayout.OnUIPositionChangedListener {
    private SmoothRefreshLayout mRefreshLayout;
    private View mTargetView;
    private ViewTreeObserver mViewTreeObserver;
    private Runnable mDelayAddListenerRunnable = new Runnable() {
        @Override
        public void run() {
            if (mViewTreeObserver != null) {
                if (mViewTreeObserver.isAlive()) {
                    mViewTreeObserver.addOnScrollChangedListener(QuickConfigAutoRefreshUtil.this);
                } else {
                    mTargetView.postDelayed(this, 50);
                }
            }
        }
    };
    private ObjectAnimator mAnimator;
    private int mStatus;
    private boolean mNeedToTriggerRefresh = false;
    private boolean mNeedToTriggerLoadMore = false;
    private boolean mCachedActionAtOnce = false;
    private boolean mCachedAutoRefreshUseSmoothScroll = false;

    public QuickConfigAutoRefreshUtil(@NonNull View targetScrollableView) {
        mTargetView = targetScrollableView;
    }

    @Override
    public void onAttached(SmoothRefreshLayout layout) {
        mRefreshLayout = layout;
        mViewTreeObserver = mTargetView.getViewTreeObserver();
        if (mViewTreeObserver.isAlive())
            mViewTreeObserver.addOnScrollChangedListener(this);
        else
            mTargetView.postDelayed(mDelayAddListenerRunnable, 50);
        mRefreshLayout.addOnUIPositionChangedListener(this);
    }

    @Override
    public void onDetached(SmoothRefreshLayout layout) {
        mRefreshLayout.removeOnUIPositionChangedListener(this);
        mTargetView.removeCallbacks(mDelayAddListenerRunnable);
        if (mViewTreeObserver.isAlive())
            mViewTreeObserver.removeOnScrollChangedListener(this);
        else
            SRReflectUtil.safelyRemoveListeners(mViewTreeObserver, this);
        cancelAnimator();
        mRefreshLayout = null;
    }

    @Override
    public void onScrollChanged() {
        if (mRefreshLayout != null) {
            if (mNeedToTriggerRefresh && !mRefreshLayout.isChildNotYetInEdgeCannotMoveHeader()) {
                mRefreshLayout.autoRefresh(mCachedActionAtOnce, mCachedAutoRefreshUseSmoothScroll);
                mNeedToTriggerRefresh = false;
                mCachedActionAtOnce = false;
                mCachedAutoRefreshUseSmoothScroll = false;
            } else if (mNeedToTriggerLoadMore && !mRefreshLayout.isChildNotYetInEdgeCannotMoveFooter()) {
                mRefreshLayout.autoLoadMore(mCachedActionAtOnce, mCachedAutoRefreshUseSmoothScroll);
                mNeedToTriggerLoadMore = false;
                mCachedActionAtOnce = false;
                mCachedAutoRefreshUseSmoothScroll = false;
            }
        }
    }

    public void autoRefresh(boolean scrollToEdgeUseSmoothScroll,
                            boolean atOnce,
                            boolean autoRefreshUseSmoothScroll) {
        if (mRefreshLayout != null) {
            if (mStatus != SmoothRefreshLayout.SR_STATUS_INIT)
                return;
            if (mRefreshLayout.isChildNotYetInEdgeCannotMoveHeader()) {
                if (mRefreshLayout.getSupportScrollAxis() == ViewCompat.SCROLL_AXIS_VERTICAL) {
                    verticalScrollToEdge(true, scrollToEdgeUseSmoothScroll);
                } else if (mRefreshLayout.getSupportScrollAxis() == ViewCompat
                        .SCROLL_AXIS_HORIZONTAL) {
                    horizontalScrollToEdge(true, scrollToEdgeUseSmoothScroll);
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

    public void autoLoadMore(boolean scrollToEdgeUseSmoothScroll,
                             boolean atOnce,
                             boolean autoRefreshUseSmoothScroll) {
        if (mRefreshLayout != null) {
            if (mStatus != SmoothRefreshLayout.SR_STATUS_INIT)
                return;
            if (mRefreshLayout.isChildNotYetInEdgeCannotMoveFooter()) {
                if (mRefreshLayout.getSupportScrollAxis() == ViewCompat.SCROLL_AXIS_VERTICAL) {
                    verticalScrollToEdge(false, scrollToEdgeUseSmoothScroll);
                } else if (mRefreshLayout.getSupportScrollAxis() == ViewCompat
                        .SCROLL_AXIS_HORIZONTAL) {
                    horizontalScrollToEdge(false, scrollToEdgeUseSmoothScroll);
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

    private void verticalScrollToEdge(final boolean toTop, boolean useSmoothScroll) {
        if (mTargetView instanceof ScrollView) {
            ScrollView scrollView = (ScrollView) mTargetView;
            if (scrollView.getChildCount() > 0) {
                if (useSmoothScroll) {
                    scrollView.smoothScrollTo(0, toTop ? 0 : scrollView.getHeight());
                } else {
                    scrollView.scrollTo(0, toTop ? 0 : scrollView.getHeight());
                }
            }
        } else if (mTargetView instanceof AbsListView) {
            AbsListView absListView = (AbsListView) mTargetView;
            if (absListView.getChildCount() > 0) {
                if (useSmoothScroll) {
                    absListView.smoothScrollToPosition(toTop ? 0 : absListView.getAdapter()
                            .getCount() - 1);
                } else {
                    absListView.setSelection(toTop ? 0 : absListView.getAdapter()
                            .getCount() - 1);
                }
            }
        } else if (mTargetView instanceof NestedScrollView) {
            NestedScrollView scrollView = (NestedScrollView) mTargetView;
            if (useSmoothScroll) {
                scrollView.smoothScrollTo(0, toTop ? 0 : scrollView.getHeight());
            } else {
                scrollView.scrollTo(0, toTop ? 0 : scrollView.getHeight());
            }
        } else {
            try {
                if (mTargetView instanceof RecyclerView) {
                    RecyclerView recyclerView = (RecyclerView) mTargetView;
                    if (recyclerView.getChildCount() > 0) {
                        if (useSmoothScroll) {
                            recyclerView.smoothScrollToPosition(toTop ? 0 : recyclerView
                                    .getAdapter().getItemCount() - 1);
                        } else {
                            recyclerView.scrollToPosition(toTop ? 0 : recyclerView.getAdapter()
                                    .getItemCount() - 1);
                        }
                    }
                    return;
                }
            } catch (NoClassDefFoundError e) {
                //ignore exception
            }
            if (useSmoothScroll) {
                cancelAnimator();
                final float distance;
                if (toTop) distance = mTargetView.getScrollY();
                else distance = mTargetView.getHeight() - mTargetView.getScrollY();
                mAnimator = ObjectAnimator.ofInt(mTargetView, "scrollY",
                        mTargetView.getScrollY(), toTop ? 0 : mTargetView.getHeight());
                DisplayMetrics dm = mTargetView.getResources().getDisplayMetrics();
                mAnimator.setDuration((long) (distance / dm.heightPixels * 200));
                mAnimator.start();
                mAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (mRefreshLayout != null)
                            mRefreshLayout.setOnFingerDownListener(null);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        if (mRefreshLayout != null)
                            mRefreshLayout.setOnFingerDownListener(null);
                        mNeedToTriggerRefresh = false;
                        mNeedToTriggerLoadMore = false;
                    }
                });
                mRefreshLayout.setOnFingerDownListener(new SmoothRefreshLayout
                        .OnFingerDownListener() {
                    @Override
                    public void onFingerDown() {
                        cancelAnimator();
                    }
                });
            } else {
                cancelAnimator();
                mTargetView.scrollTo(mTargetView.getScrollX(), toTop ? 0 : mTargetView.getHeight());
            }
        }
    }

    private void horizontalScrollToEdge(final boolean toLeft, boolean useSmoothScroll) {
        if (mTargetView instanceof HorizontalScrollView) {
            HorizontalScrollView scrollView = (HorizontalScrollView) mTargetView;
            if (useSmoothScroll) {
                scrollView.smoothScrollTo(toLeft ? 0 : scrollView.getWidth(), 0);
            } else {
                scrollView.scrollTo(toLeft ? 0 : scrollView.getWidth(), 0);
            }
        } else {
            try {
                if (mTargetView instanceof RecyclerView) {
                    RecyclerView recyclerView = (RecyclerView) mTargetView;
                    if (recyclerView.getChildCount() > 0) {
                        if (useSmoothScroll) {
                            recyclerView.smoothScrollToPosition(toLeft ? 0 : recyclerView.getAdapter()
                                    .getItemCount() - 1);
                        } else {
                            recyclerView.scrollToPosition(toLeft ? 0 : recyclerView.getAdapter()
                                    .getItemCount() - 1);
                        }
                    }
                    return;
                }
            } catch (NoClassDefFoundError e) {
                //ignore exception
            }
            if (useSmoothScroll) {
                cancelAnimator();
                final float distance;
                if (toLeft) distance = mTargetView.getScrollX();
                else distance = mTargetView.getWidth() - mTargetView.getScrollX();
                mAnimator = ObjectAnimator.ofInt(mTargetView, "scrollX",
                        mTargetView.getScrollX(), toLeft ? 0 : mTargetView.getWidth());
                DisplayMetrics dm = mTargetView.getResources().getDisplayMetrics();
                mAnimator.setDuration((long) (distance / dm.widthPixels * 200));
                mAnimator.start();
                mAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (mRefreshLayout != null)
                            mRefreshLayout.setOnFingerDownListener(null);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        if (mRefreshLayout != null)
                            mRefreshLayout.setOnFingerDownListener(null);
                        mNeedToTriggerRefresh = false;
                        mNeedToTriggerLoadMore = false;
                    }
                });
                mRefreshLayout.setOnFingerDownListener(new SmoothRefreshLayout
                        .OnFingerDownListener() {
                    @Override
                    public void onFingerDown() {
                        cancelAnimator();
                    }
                });
            } else {
                cancelAnimator();
                mTargetView.scrollTo(toLeft ? 0 : mTargetView.getHeight(), mTargetView.getScrollY());
            }
        }
    }

    private void cancelAnimator() {
        if (mAnimator != null && mAnimator.isRunning())
            mAnimator.cancel();
        mAnimator = null;
    }

    @Override
    public void onChanged(byte status, IIndicator indicator) {
        mStatus = status;
    }
}
