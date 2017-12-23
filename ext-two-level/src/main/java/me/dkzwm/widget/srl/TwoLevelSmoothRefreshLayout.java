package me.dkzwm.widget.srl;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.SystemClock;
import android.support.annotation.FloatRange;
import android.support.annotation.IntRange;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.lang.ref.WeakReference;

import me.dkzwm.widget.srl.extra.TwoLevelRefreshView;
import me.dkzwm.widget.srl.indicator.DefaultTwoLevelIndicator;
import me.dkzwm.widget.srl.indicator.IIndicator;
import me.dkzwm.widget.srl.indicator.ITwoLevelIndicator;
import me.dkzwm.widget.srl.utils.SRLog;

/**
 * Support Two-Level refresh feature;<br/>
 *
 * @author dkzwm
 */
public class TwoLevelSmoothRefreshLayout extends SmoothRefreshLayout {
    private TwoLevelRefreshView<ITwoLevelIndicator> mTwoLevelRefreshView;
    private ITwoLevelIndicator mTwoLevelIndicator;
    private boolean mEnabledTwoLevelRefresh = true;
    private boolean mOnTwoLevelRefreshing = false;
    private boolean mHasDealTwoLevelRefreshHint = false;
    private boolean mNeedFilterRefreshEvent = false;
    private boolean mAutoHintCanBeInterrupted = true;
    private int mDurationOfBackToTwoLevelHeaderHeight = 500;
    private int mDurationToCloseTwoLevelHeader = 500;
    private int mDurationToStayAtHintPos = 0;
    private DelayToBackToTop mDelayToBackToTopRunnable;

    public TwoLevelSmoothRefreshLayout(Context context) {
        this(context, null);
    }

    public TwoLevelSmoothRefreshLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TwoLevelSmoothRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray arr = context.obtainStyledAttributes(attrs, me.dkzwm.widget.srl.ext.twolevel.R.styleable
                .TwoLevelSmoothRefreshLayout, 0, 0);
        if (arr != null) {
            setDisableTwoLevelRefresh(!arr.getBoolean(me.dkzwm.widget.srl.ext.twolevel.R.styleable
                    .TwoLevelSmoothRefreshLayout_sr_enable_two_level_refresh, false));
            arr.recycle();
        }
    }

    @Override
    protected void createIndicator() {
        DefaultTwoLevelIndicator indicator = new DefaultTwoLevelIndicator();
        mIndicator = indicator;
        mTwoLevelIndicator = indicator;
    }

    /**
     * Set the height ratio of Header to trigger Two-Level refresh hint<br/>
     * <p>
     * 设置触发二级刷新提示时的位置占Header视图的高度比
     *
     * @param ratio Height ratio
     */
    public void setRatioOfHeaderHeightToHintTwoLevelRefresh(@FloatRange(from = 0, to = Float.MAX_VALUE) float ratio) {
        mTwoLevelIndicator.setRatioOfHeaderHeightToHintTwoLevelRefresh(ratio);
    }

    /**
     * Set the height ratio of Header to trigger Two-Level refresh<br/>
     * <p>
     * 设置触发二级刷新时的位置占Header视图的高度比
     *
     * @param ratio Height ratio
     */
    public void setRatioOfHeaderHeightToTwoLevelRefresh(@FloatRange(from = 0, to = Float.MAX_VALUE) float ratio) {
        mTwoLevelIndicator.setRatioOfHeaderHeightToTwoLevelRefresh(ratio);
    }

    /**
     * The offset of keep Header in Two-Level refreshing occupies the height ratio of the Header<br/>
     * <p>
     * 二级刷新中保持视图位置占Header视图的高度比（默认:`1f`）,该属性的值必须小于等于触发刷新高度比才会有效果，
     * 当开启了{@link SmoothRefreshLayout#isEnabledKeepRefreshView}后，该属性会生效
     *
     * @param ratio Height ratio
     */
    public void setOffsetRatioToKeepTwoLevelHeaderWhileLoading(@FloatRange(from = 0, to = Float.MAX_VALUE) float ratio) {
        mTwoLevelIndicator.setOffsetRatioToKeepTwoLevelHeaderWhileLoading(ratio);
    }

    /**
     * The flag has been set to disabled Two-Level refresh<br/>
     * <p>
     * 是否已经关闭二级刷新
     *
     * @return Disabled
     */
    public boolean isDisabledTwoLevelRefresh() {
        return !mEnabledTwoLevelRefresh;
    }

    /**
     * If @param disable has been set to true.Will disable Two-Level refresh<br/>
     * <p>
     * 设置是否关闭二级刷新
     *
     * @param disable Disable refresh
     */
    public void setDisableTwoLevelRefresh(boolean disable) {
        mEnabledTwoLevelRefresh = !disable;
    }

    /**
     * The duration of return to the keep Two-Level refresh view position when Header moves<br/>
     * <p>
     * 设置回滚到保持二级刷新Header视图位置的时间
     *
     * @param duration Millis
     */
    public void setDurationOfBackToKeepTwoLevelHeaderViewPosition(@IntRange(from = 0, to = Integer.MAX_VALUE) int duration) {
        mDurationOfBackToTwoLevelHeaderHeight = duration;
    }

    /**
     * The duration of return to the start position when Header moves<br/>
     * <p>
     * 设置二级刷新Header刷新完成回滚到起始位置的时间
     *
     * @param duration Millis
     */
    public void setDurationToCloseTwoLevelHeader(@IntRange(from = 0, to = Integer.MAX_VALUE) int duration) {
        mDurationToCloseTwoLevelHeader = duration;
    }

    /**
     * Whether it is being Two-Level refreshed<br/>
     * <p>
     * 是否在二级刷新中
     *
     * @return Refreshing
     */
    public boolean isTwoLevelRefreshing() {
        return super.isRefreshing() && mOnTwoLevelRefreshing;
    }

    /**
     * Auto trigger Two-Level refresh hint use smooth scrolling.<br/>
     * <p>
     * 自动触发二级刷新提示并滚动到触发提示位置后回滚回起始位置
     */
    public void autoTwoLevelRefreshHint() {
        autoTwoLevelRefreshHint(true, 0, true);
    }

    /**
     * Auto trigger Two-Level refresh hint use smooth scrolling.<br/>
     * <p>
     * 自动触发二级刷新提示并是否滚动到触发提示位置, `smoothScroll`是否滚动到触发位置
     */
    public void autoTwoLevelRefreshHint(boolean smoothScroll) {
        autoTwoLevelRefreshHint(smoothScroll, 0, true);
    }

    /**
     * Auto trigger Two-Level refresh hint use smooth scrolling.<br/>
     * <p>
     * 自动触发二级刷新提示并滚动到触发提示位置, `stayDuration`停留多长时间
     */
    public void autoTwoLevelRefreshHint(@IntRange(from = 0, to = Integer.MAX_VALUE) int stayDuration) {
        autoTwoLevelRefreshHint(true, stayDuration, true);
    }

    /**
     * If @param smoothScroll has been set to true. Auto perform Two-Level refresh hint use
     * smooth scrolling.<br/>
     * <p>
     * 自动触发二级刷新提示，`smoothScroll`是否滚动到触发位置，`stayDuration`停留多长时间
     *
     * @param smoothScroll Auto Two-Level refresh hint use smooth scrolling
     * @param stayDuration The header moved to the position of the hint, and then how long to stay.
     */
    public void autoTwoLevelRefreshHint(boolean smoothScroll, int stayDuration) {
        autoTwoLevelRefreshHint(smoothScroll, stayDuration, true);
    }

    /**
     * If @param smoothScroll has been set to true. Auto perform Two-Level refresh hint use
     * smooth scrolling.<br/>
     * <p>
     * 自动触发二级刷新提示，`smoothScroll`是否滚动到触发位置，`stayDuration`停留多长时间,
     * `canBeInterrupted`是否能被触摸打断
     *
     * @param smoothScroll     Auto Two-Level refresh hint use smooth scrolling
     * @param stayDuration     The header moved to the position of the hint, and then how long to stay.
     * @param canBeInterrupted The Two-Level refresh hint can be interrupted by touch handling.
     */
    public void autoTwoLevelRefreshHint(boolean smoothScroll, int stayDuration,
                                        boolean canBeInterrupted) {
        if (mStatus != SR_STATUS_INIT) {
            return;
        }
        if (sDebug) {
            SRLog.d(TAG, "autoTwoLevelRefreshHint(): smoothScroll:", smoothScroll);
        }
        mStatus = SR_STATUS_PREPARE;
        mNeedFilterRefreshEvent = true;
        mDurationToStayAtHintPos = stayDuration;
        if (mHeaderView != null)
            mHeaderView.onRefreshPrepare(this);
        mIndicator.setMovingStatus(IIndicator.MOVING_HEADER);
        mViewStatus = SR_VIEW_STATUS_HEADER_IN_PROCESSING;
        mAutomaticActionUseSmoothScroll = smoothScroll;
        mAutoHintCanBeInterrupted = canBeInterrupted;
        int offsetToRefreshHint = mTwoLevelIndicator.getOffsetToHintTwoLevelRefresh();
        if (offsetToRefreshHint <= 0) {
            mHasDealTwoLevelRefreshHint = false;
            mAutomaticActionInScrolling = false;
        } else {
            mHasDealTwoLevelRefreshHint = true;
            mScrollChecker.tryToScrollTo(offsetToRefreshHint, mAutomaticActionUseSmoothScroll
                    ? mDurationToCloseHeader : 0);
            mAutomaticActionInScrolling = mAutomaticActionUseSmoothScroll;
            if (!mAutomaticActionUseSmoothScroll)
                delayForStay();
        }
    }

    @Override
    public void autoRefresh(boolean atOnce, boolean smoothScroll) {
        if (mNeedFilterRefreshEvent) {
            throw new IllegalArgumentException("Unsupported operation , " +
                    "Auto Two-Level refresh hint is in process !!");
        }
        super.autoRefresh(atOnce, smoothScroll);
    }

    @Override
    public void autoLoadMore(boolean atOnce, boolean smoothScroll) {
        if (mNeedFilterRefreshEvent) {
            throw new IllegalArgumentException("Unsupported operation , " +
                    "Auto Two-Level refresh hint is in process !!");
        }
        super.autoLoadMore(atOnce, smoothScroll);
    }

    @Override
    protected boolean processDispatchTouchEvent(MotionEvent ev) {
        if (mAutoHintCanBeInterrupted) {
            mNeedFilterRefreshEvent = false;
            final int action = ev.getAction() & MotionEvent.ACTION_MASK;
            if (action == MotionEvent.ACTION_DOWN && mDelayToBackToTopRunnable != null) {
                removeCallbacks(mDelayToBackToTopRunnable);
            }
        }
        return super.processDispatchTouchEvent(ev);
    }

    @Override
    protected void tryToPerformAutoRefresh() {
        if (!mAutomaticActionInScrolling && mStatus == SR_STATUS_PREPARE
                && !mHasDealTwoLevelRefreshHint && isMovingHeader()) {
            if (mHeaderView == null || mIndicator.getHeaderHeight() <= 0)
                return;
            int offsetToRefreshHint = mTwoLevelIndicator.getOffsetToHintTwoLevelRefresh();
            if (offsetToRefreshHint > 0) {
                mNeedFilterRefreshEvent = true;
                mHasDealTwoLevelRefreshHint = true;
                mScrollChecker.tryToScrollTo(offsetToRefreshHint,
                        mAutomaticActionUseSmoothScroll ? mDurationToCloseHeader : 0);
                mAutomaticActionInScrolling = mAutomaticActionUseSmoothScroll;
                if (!mAutomaticActionUseSmoothScroll)
                    delayForStay();
            }
        }
        if (mNeedFilterRefreshEvent)
            super.tryToPerformAutoRefresh();
    }

    @Override
    protected void ensureFreshView(View child) {
        super.ensureFreshView(child);
        if (child instanceof TwoLevelRefreshView) {
            mTwoLevelRefreshView = (TwoLevelRefreshView<ITwoLevelIndicator>) child;
        }
    }

    @Override
    protected void updatePos(int change) {
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
        super.updatePos(change);
    }

    @Override
    protected boolean isNeedInterceptTouchEvent() {
        return !mAutoHintCanBeInterrupted || super.isNeedInterceptTouchEvent();
    }

    @Override
    protected void onFingerUp(boolean stayForLoading) {
        if (canPerformTwoLevelPullToRefresh() && mTwoLevelIndicator
                .crossTwoLevelRefreshLine() && mStatus == SR_STATUS_PREPARE) {
            onRelease();
            return;
        }
        super.onFingerUp(stayForLoading);
    }

    @Override
    protected boolean tryToNotifyReset() {
        boolean reset = super.tryToNotifyReset();
        if (reset) {
            mNeedFilterRefreshEvent = false;
            mAutoHintCanBeInterrupted = true;
            if (mDelayToBackToTopRunnable != null)
                removeCallbacks(mDelayToBackToTopRunnable);
        }
        return reset;
    }

    @Override
    protected void reset() {
        if (mDelayToBackToTopRunnable != null)
            removeCallbacks(mDelayToBackToTopRunnable);
        super.reset();
    }

    @Override
    protected void onRelease() {
        if (mAutomaticActionUseSmoothScroll && mDurationToStayAtHintPos > 0) {
            delayForStay();
            return;
        }
        if (canPerformTwoLevelPullToRefresh()) {
            tryToPerformRefresh();
        }
        if (mEnabledTwoLevelRefresh && isMovingHeader() && isTwoLevelRefreshing()
                && mTwoLevelIndicator.crossTwoLevelRefreshLine()) {
            if (isEnabledKeepRefreshView())
                mScrollChecker.tryToScrollTo(mTwoLevelIndicator
                                .getOffsetToKeepTwoLevelHeaderWhileLoading(),
                        mDurationOfBackToTwoLevelHeaderHeight);
            else
                mScrollChecker.tryToScrollTo(mTwoLevelIndicator
                                .getOffsetToKeepTwoLevelHeaderWhileLoading(),
                        mDurationToCloseTwoLevelHeader);
            return;
        }
        super.onRelease();
    }

    @Override
    protected void tryToPerformRefresh() {
        if (mNeedFilterRefreshEvent)
            return;
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
                mTwoLevelRefreshView.onTwoLevelRefreshBegin(this, mTwoLevelIndicator);
            }
            if (mRefreshListener != null && mRefreshListener instanceof OnRefreshListener)
                ((OnRefreshListener) mRefreshListener).onTwoLevelRefreshBegin();
            return;
        }
        super.performRefresh();
    }

    @Override
    protected void notifyUIRefreshComplete(boolean useScroll) {
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
                && mEnabledTwoLevelRefresh && canPerformRefresh() && isMovingHeader();
    }

    private void delayForStay() {
        if (mDelayToBackToTopRunnable == null)
            mDelayToBackToTopRunnable = new DelayToBackToTop(this);
        else
            mDelayToBackToTopRunnable.mLayoutWeakRf = new WeakReference<>(this);
        mAutomaticActionUseSmoothScroll = false;
        postDelayed(mDelayToBackToTopRunnable, mDurationToStayAtHintPos);
    }

    public interface OnRefreshListener extends SmoothRefreshLayout.OnRefreshListener {
        void onTwoLevelRefreshBegin();
    }

    private static class DelayToBackToTop implements Runnable {
        private WeakReference<TwoLevelSmoothRefreshLayout> mLayoutWeakRf;

        private DelayToBackToTop(TwoLevelSmoothRefreshLayout layout) {
            mLayoutWeakRf = new WeakReference<>(layout);
        }

        @Override
        public void run() {
            if (mLayoutWeakRf.get() != null) {
                if (SmoothRefreshLayout.sDebug) {
                    SRLog.i(SmoothRefreshLayout.TAG, "DelayToBackToTop: run()");
                }
                mLayoutWeakRf.get().onRelease();
            }
        }
    }
}

