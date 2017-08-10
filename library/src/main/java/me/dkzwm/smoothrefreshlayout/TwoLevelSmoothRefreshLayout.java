package me.dkzwm.smoothrefreshlayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;

import me.dkzwm.smoothrefreshlayout.extra.TwoLevelRefreshView;
import me.dkzwm.smoothrefreshlayout.indicator.DefaultTwoLevelIndicator;
import me.dkzwm.smoothrefreshlayout.indicator.ITwoLevelIndicator;

/**
 * Created by dkzwm on 2017/6/12.
 * <p>
 * Support Two-Level refresh feature;<br/>
 * </p>
 *
 * @author dkzwm
 */
public class TwoLevelSmoothRefreshLayout extends SmoothRefreshLayout {
    private TwoLevelRefreshView mTwoLevelRefreshView;
    private ITwoLevelIndicator mTwoLevelIndicator;
    private boolean mEnabledTwoLevelRefresh = true;
    private boolean mOnTwoLevelRefreshing = false;
    private int mDurationOfBackToTwoLevelHeaderHeight = 500;
    private int mDurationToCloseTwoLevelHeader = 500;

    public TwoLevelSmoothRefreshLayout(Context context) {
        this(context, null);
    }

    public TwoLevelSmoothRefreshLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TwoLevelSmoothRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        DefaultTwoLevelIndicator indicator = new DefaultTwoLevelIndicator();
        indicator.convert(indicator);
        mIndicator = indicator;
        mTwoLevelIndicator = indicator;
        TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.TwoLevelSmoothRefreshLayout, 0, 0);
        if (arr != null) {
            setEnableTwoLevelPullToRefresh(arr.getBoolean(R.styleable
                    .TwoLevelSmoothRefreshLayout_sr_enable_two_level_pull_to_refresh, true));
            arr.recycle();
        }
    }

    public void setRatioOfHeaderHeightToHintTwoLevelRefresh(float ratio) {
        mTwoLevelIndicator.setRatioOfHeaderHeightToHintTwoLevelRefresh(ratio);
    }

    public void setRatioOfHeaderHeightToTwoLevelRefresh(float ratio) {
        mTwoLevelIndicator.setRatioOfHeaderHeightToTwoLevelRefresh(ratio);
    }

    public void setOffsetRatioToKeepTwoLevelHeaderWhileLoading(float ratio) {
        mTwoLevelIndicator.setOffsetRatioToKeepTwoLevelHeaderWhileLoading(ratio);
    }

    public boolean isEnableTwoLevelPullToRefresh() {
        return mEnabledTwoLevelRefresh;
    }

    public void setEnableTwoLevelPullToRefresh(boolean enable) {
        mEnabledTwoLevelRefresh = enable;
    }

    public void setDurationOfBackToKeepTwoLeveHeaderViewPosition(int duration) {
        mDurationOfBackToTwoLevelHeaderHeight = duration;
    }

    public void setDurationToCloseTwoLevelHeader(int duration) {
        mDurationToCloseTwoLevelHeader = duration;
    }

    public boolean isTwoLevelRefreshing() {
        return super.isRefreshing() && mOnTwoLevelRefreshing;
    }

    @Override
    protected void ensureFreshView(View child) {
        super.ensureFreshView(child);
        if (child instanceof TwoLevelRefreshView) {
            mTwoLevelRefreshView = (TwoLevelRefreshView) child;
        }
    }

    @Override
    protected void updateYPos(int change) {
        if (canPerformTwoLevelPullToRefresh() && (mStatus == SR_STATUS_PREPARE
                || (mStatus == SR_STATUS_COMPLETE && mTwoLevelIndicator.crossTwoLevelCompletePos()
                && isEnabledNextPtrAtOnce()))) {
            // reach fresh height while moving from top to bottom or reach load more height while
            // moving from bottom to top
            if (mIndicator.hasTouched() && !isAutoRefresh() && isEnabledPullToRefresh()) {
                if (isMovingHeader() && mTwoLevelIndicator.crossTwoLevelRefreshLine())
                    tryToPerformRefresh();
            }
        }
        super.updateYPos(change);
    }


    @Override
    protected void onFingerUp(boolean stayForLoading) {
        if (canPerformTwoLevelPullToRefresh() && mTwoLevelIndicator
                .crossTwoLevelRefreshLine() && mStatus == SR_STATUS_PREPARE) {
            onRelease(0);
            return;
        }
        super.onFingerUp(stayForLoading);
    }

    @Override
    protected void onRelease(int duration) {
        if (canPerformTwoLevelPullToRefresh()) {
            tryToPerformRefresh();
        }
        if (isEnableTwoLevelPullToRefresh() && isMovingHeader() && isTwoLevelRefreshing()
                && mTwoLevelIndicator.crossTwoLevelRefreshLine()) {
            if (isEnabledKeepRefreshView())
                tryToScrollTo(mTwoLevelIndicator.getOffsetToKeepTwoLevelHeaderWhileLoading(),
                        mDurationOfBackToTwoLevelHeaderHeight);
            else
                tryToScrollTo(mTwoLevelIndicator.getOffsetToKeepTwoLevelHeaderWhileLoading(),
                        mDurationToCloseTwoLevelHeader);
            return;
        }
        super.onRelease(duration);
    }

    @Override
    protected void tryToPerformRefresh() {
        if (canPerformTwoLevelPullToRefresh() && mStatus == SR_STATUS_PREPARE
                && mTwoLevelIndicator.crossTwoLevelRefreshLine()) {
            mStatus = SR_STATUS_REFRESHING;
            mOnTwoLevelRefreshing = true;
            performRefresh();
            return;
        }
        super.tryToPerformRefresh();
    }


    @Override
    protected void performRefresh() {
        if (canPerformTwoLevelPullToRefresh() && isTwoLevelRefreshing()
                && mTwoLevelIndicator.crossTwoLevelRefreshLine()) {
            mLoadingStartTime = SystemClock.uptimeMillis();
            mNeedNotifyRefreshComplete = true;
            if (mTwoLevelRefreshView != null) {
                mTwoLevelRefreshView.onTwoLevelRefreshBegin(this, mIndicator, mTwoLevelIndicator);
            }
            if (mRefreshListener != null && mRefreshListener instanceof OnRefreshListener)
                ((OnRefreshListener) mRefreshListener).onTwoLevelRefreshBegin();
            return;
        }
        super.performRefresh();
    }


    @Override
    protected void notifyUIRefreshComplete(boolean scroll) {
        if (mOnTwoLevelRefreshing) {
            mOnTwoLevelRefreshing = false;
            mTwoLevelIndicator.onTwoLevelRefreshComplete();
            if (mTwoLevelIndicator.crossTwoLevelCompletePos()) {
                super.notifyUIRefreshComplete(false);
                tryScrollBackToTop(mDurationToCloseTwoLevelHeader);
                return;
            }
        }
        super.notifyUIRefreshComplete(true);
    }

    private boolean canPerformTwoLevelPullToRefresh() {
        return !isDisabledRefresh() && mTwoLevelRefreshView != null
                && isEnableTwoLevelPullToRefresh() && canPerformRefresh() && isMovingHeader();
    }


    public interface OnRefreshListener extends SmoothRefreshLayout.OnRefreshListener {
        void onTwoLevelRefreshBegin();
    }
}

