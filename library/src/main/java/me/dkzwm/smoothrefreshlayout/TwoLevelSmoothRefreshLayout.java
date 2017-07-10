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
    private static final byte FLAG_ENABLE_TWO_LEVEL_PULL_TO_REFRESH = 0x01;
    private static final byte FLAG_ENABLE_BACK_TO_START_POS_AT_ONCE = 0x01 << 1;
    private int mTwoLevelFlag = 0x00;
    private TwoLevelRefreshView mTwoLevelRefreshView;
    private ITwoLevelIndicator mTwoLevelIndicator;
    private boolean mOnTwoLevelRefreshing = false;

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
        TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.SmoothRefreshLayout, 0, 0);
        if (arr != null) {
            setEnableTwoLevelPullToRefresh(arr.getBoolean(R.styleable
                    .SmoothRefreshLayout_sr_enable_two_level_pull_to_refresh, false));
            arr.recycle();
        }
    }

    public void setRatioOfHeaderHeightToHintTwoLevelRefresh(float ratio) {
        mTwoLevelIndicator.setRatioOfHeaderHeightToHintTwoLevelRefresh(ratio);
    }

    public void setRatioOfHeaderHeightToTwoLevelRefresh(float ratio) {
        mTwoLevelIndicator.setRatioOfHeaderHeightToTwoLevelRefresh(ratio);
    }

    public boolean isEnableBackToStartPosAtOnce() {
        return (mTwoLevelFlag & FLAG_ENABLE_BACK_TO_START_POS_AT_ONCE) > 0;
    }

    public void setEnableBackToStartPosAtOnce(boolean enable) {
        if (enable) {
            mTwoLevelFlag = mTwoLevelFlag | FLAG_ENABLE_BACK_TO_START_POS_AT_ONCE;
        } else {
            mTwoLevelFlag = mTwoLevelFlag & ~FLAG_ENABLE_BACK_TO_START_POS_AT_ONCE;
        }
    }

    public boolean isEnableTwoLevelPullToRefresh() {
        return (mTwoLevelFlag & FLAG_ENABLE_TWO_LEVEL_PULL_TO_REFRESH) > 0;
    }

    public void setEnableTwoLevelPullToRefresh(boolean enable) {
        if (enable) {
            mTwoLevelFlag = mTwoLevelFlag | FLAG_ENABLE_TWO_LEVEL_PULL_TO_REFRESH;
        } else {
            mTwoLevelFlag = mTwoLevelFlag & ~FLAG_ENABLE_TWO_LEVEL_PULL_TO_REFRESH;
        }
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
        if (canPerformTwoLevelPullToRefresh()
                && (mStatus == SR_STATUS_PREPARE
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
        if (isEnableTwoLevelPullToRefresh() && mStatus == SR_STATUS_REFRESHING
                && mTwoLevelIndicator.crossTwoLevelRefreshLine()) {
            final boolean atOnce = isEnableBackToStartPosAtOnce();
            if (isEnabledKeepRefreshView() && !atOnce)
                tryScrollBackToHeaderHeight();
            else if (!atOnce)
                tryScrollBackToTop(mDurationToCloseHeader);
            else
                tryScrollBackToTop(0);
            return;
        }
        super.onRelease(duration);
    }

    @Override
    protected void tryToPerformRefresh() {
        if (canPerformTwoLevelPullToRefresh() && mStatus == SR_STATUS_PREPARE
                && mTwoLevelIndicator.crossTwoLevelRefreshLine()) {
            mStatus = SR_STATUS_REFRESHING;
            performRefresh();
        }
        super.tryToPerformRefresh();
    }


    @Override
    protected void performRefresh() {
        if (canPerformTwoLevelPullToRefresh() && mTwoLevelIndicator.crossTwoLevelRefreshLine()) {
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
    protected void notifyUIRefreshComplete() {
        if (mOnTwoLevelRefreshing) {
            mTwoLevelIndicator.onTwoLevelRefreshComplete();
        }
        mOnTwoLevelRefreshing = false;
        super.notifyUIRefreshComplete();
    }

    private boolean canPerformTwoLevelPullToRefresh() {
        return (mMode == MODE_REFRESH || mMode == MODE_BOTH) && mTwoLevelRefreshView != null
                && isEnableTwoLevelPullToRefresh() && canPerformRefresh() && isMovingHeader();
    }


    public interface OnRefreshListener extends SmoothRefreshLayout.OnRefreshListener {
        void onTwoLevelRefreshBegin();
    }
}

