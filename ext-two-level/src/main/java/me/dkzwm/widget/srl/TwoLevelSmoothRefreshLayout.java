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
package me.dkzwm.widget.srl;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.CallSuper;
import me.dkzwm.widget.srl.annotation.Action;
import me.dkzwm.widget.srl.config.Constants;
import me.dkzwm.widget.srl.ext.twolevel.R;
import me.dkzwm.widget.srl.extra.TwoLevelRefreshView;
import me.dkzwm.widget.srl.indicator.DefaultTwoLevelIndicator;
import me.dkzwm.widget.srl.indicator.IIndicator;
import me.dkzwm.widget.srl.indicator.ITwoLevelIndicator;
import me.dkzwm.widget.srl.indicator.ITwoLevelIndicatorSetter;

/** @author dkzwm */
public class TwoLevelSmoothRefreshLayout extends SmoothRefreshLayout {
    private static final byte FLAG_DISABLE_TWO_LEVEL_REFRESH = 0x01 << 1;
    private static final byte FLAG_TRIGGER_TWO_LEVEL_REFRESH = 0x01 << 2;
    private static final byte FLAG_TRIGGERED_TWO_LEVEL_REFRESH = 0x01 << 3;
    private int mSubFlag = 0;
    private TwoLevelRefreshView<ITwoLevelIndicator> mTwoLevelRefreshView;
    private ITwoLevelIndicator mTwoLevelIndicator;
    private ITwoLevelIndicatorSetter mTwoLevelIndicatorSetter;
    private boolean mNeedFilterRefreshEvent = false;
    private boolean mAutoHintCanBeInterrupted = true;
    private int mDurationOfBackToTwoLevel = 500;
    private int mDurationToCloseTwoLevel = 500;
    private int mDurationToStayAtHint = 0;
    private DelayToBackToTop mDelayToBackToTopRunnable;

    public TwoLevelSmoothRefreshLayout(Context context) {
        super(context);
    }

    public TwoLevelSmoothRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TwoLevelSmoothRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    protected void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super.init(context, attrs, defStyleAttr, defStyleRes);
        TypedArray arr =
                context.obtainStyledAttributes(
                        attrs, R.styleable.TwoLevelSmoothRefreshLayout, defStyleAttr, defStyleRes);
        if (arr != null) {
            try {
                setDisableTwoLevelRefresh(
                        !arr.getBoolean(
                                R.styleable.TwoLevelSmoothRefreshLayout_sr_enableTwoLevelRefresh,
                                false));
                mDurationOfBackToTwoLevel =
                        arr.getInt(
                                R.styleable.TwoLevelSmoothRefreshLayout_sr_backToKeep2Duration,
                                mDurationOfBackToTwoLevel);
                mDurationToCloseTwoLevel =
                        arr.getInt(
                                R.styleable.TwoLevelSmoothRefreshLayout_sr_closeHeader2Duration,
                                mDurationToCloseTwoLevel);
            } finally {
                arr.recycle();
            }
        }
    }

    @Override
    protected void createIndicator() {
        DefaultTwoLevelIndicator indicator = new DefaultTwoLevelIndicator();
        mIndicator = indicator;
        mIndicatorSetter = indicator;
        mTwoLevelIndicator = indicator;
        mTwoLevelIndicatorSetter = indicator;
    }

    @Override
    @SuppressWarnings("unchecked")
    @CallSuper
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        if (child instanceof TwoLevelRefreshView) {
            mTwoLevelRefreshView = (TwoLevelRefreshView<ITwoLevelIndicator>) child;
        }
    }

    /**
     * Set the height ratio of Header to trigger Two-Level refresh hint<br>
     *
     * <p>设置触发二级刷新提示时的位置占Header视图的高度比
     *
     * @param ratio Height ratio
     */
    public void setRatioOfHeaderToHintTwoLevel(float ratio) {
        mTwoLevelIndicatorSetter.setRatioOfHeaderToHintTwoLevel(ratio);
    }

    /**
     * Set the height ratio of Header to trigger Two-Level refresh<br>
     *
     * <p>设置触发二级刷新时的位置占Header视图的高度比
     *
     * @param ratio Height ratio
     */
    public void setRatioOfHeaderToTwoLevel(float ratio) {
        mTwoLevelIndicatorSetter.setRatioOfHeaderToTwoLevel(ratio);
    }

    /**
     * The offset of keep Header in Two-Level refreshing occupies the height ratio of the Header<br>
     *
     * <p>二级刷新中保持视图位置占Header视图的高度比（默认:`1f`）
     *
     * @param ratio Height ratio
     */
    public void setRatioToKeepTwoLevelHeader(float ratio) {
        mTwoLevelIndicatorSetter.setRatioToKeepTwoLevelHeader(ratio);
    }

    /**
     * The flag has been set to disabled Two-Level refresh<br>
     *
     * <p>是否已经关闭二级刷新
     *
     * @return Disabled
     */
    public boolean isDisabledTwoLevelRefresh() {
        return isDisabledRefresh() || (mSubFlag & FLAG_DISABLE_TWO_LEVEL_REFRESH) > 0;
    }

    /**
     * If @param disable has been set to true.Will disable Two-Level refresh<br>
     *
     * <p>设置是否关闭二级刷新
     *
     * @param disable Disable refresh
     */
    public void setDisableTwoLevelRefresh(boolean disable) {
        if (disable) {
            mSubFlag = mSubFlag | FLAG_DISABLE_TWO_LEVEL_REFRESH;
            reset();
        } else {
            mSubFlag = mSubFlag & ~FLAG_DISABLE_TWO_LEVEL_REFRESH;
        }
    }

    /**
     * Set the duration of to keep Two-Level refresh view position when Header moves<br>
     *
     * <p>设置回滚到保持二级刷新Header视图位置的时间
     *
     * @param duration Millis
     */
    public void setDurationOfBackToKeepTwoLevel(int duration) {
        mDurationOfBackToTwoLevel = duration;
    }

    /**
     * Set the duration for closing the Two-Level refresh<br>
     *
     * <p>设置二级刷新Header刷新完成回滚到起始位置的时间
     *
     * @param duration Millis
     */
    public void setDurationToCloseTwoLevel(int duration) {
        mDurationToCloseTwoLevel = duration;
    }

    /**
     * Whether it is being Two-Level refreshed<br>
     *
     * <p>是否在二级刷新中
     *
     * @return Refreshing
     */
    public boolean isTwoLevelRefreshing() {
        return super.isRefreshing() && (mSubFlag & FLAG_TRIGGER_TWO_LEVEL_REFRESH) > 0;
    }

    /**
     * Auto trigger Two-Level refresh hint use smooth scrolling.<br>
     *
     * <p>自动触发二级刷新提示并滚动到触发提示位置后回滚回起始位置
     */
    public boolean autoTwoLevelRefreshHint() {
        return autoTwoLevelRefreshHint(true, 0, true);
    }

    /**
     * Auto trigger Two-Level refresh hint use smooth scrolling.<br>
     *
     * <p>自动触发二级刷新提示并是否滚动到触发提示位置, `smoothScroll`是否滚动到触发位置
     */
    public boolean autoTwoLevelRefreshHint(boolean smoothScroll) {
        return autoTwoLevelRefreshHint(smoothScroll, 0, true);
    }

    /**
     * Auto trigger Two-Level refresh hint use smooth scrolling.<br>
     *
     * <p>自动触发二级刷新提示并滚动到触发提示位置, `stayDuration`停留多长时间
     */
    public boolean autoTwoLevelRefreshHint(int stayDuration) {
        return autoTwoLevelRefreshHint(true, stayDuration, true);
    }

    /**
     * If @param smoothScroll has been set to true. Auto perform Two-Level refresh hint use smooth
     * scrolling.<br>
     *
     * <p>自动触发二级刷新提示，`smoothScroll`是否滚动到触发位置，`stayDuration`停留多长时间
     *
     * @param smoothScroll Auto Two-Level refresh hint use smooth scrolling
     * @param stayDuration The header moved to the position of the hint, and then how long to stay.
     */
    public boolean autoTwoLevelRefreshHint(boolean smoothScroll, int stayDuration) {
        return autoTwoLevelRefreshHint(smoothScroll, stayDuration, true);
    }

    /**
     * If @param smoothScroll has been set to true. Auto perform Two-Level refresh hint use smooth
     * scrolling.<br>
     *
     * <p>自动触发二级刷新提示，`smoothScroll`是否滚动到触发位置，`stayDuration`停留多长时间, `canBeInterrupted`是否能被触摸打断
     *
     * @param smoothScroll Auto Two-Level refresh hint use smooth scrolling
     * @param stayDuration The header moved to the position of the hint, and then how long to stay.
     * @param canBeInterrupted The Two-Level refresh hint can be interrupted by touch handling.
     */
    public boolean autoTwoLevelRefreshHint(
            boolean smoothScroll, int stayDuration, boolean canBeInterrupted) {
        if (mStatus != SR_STATUS_INIT && !isDisabledTwoLevelRefresh()) {
            return false;
        }
        if (sDebug) {
            Log.d(TAG, String.format("autoTwoLevelRefreshHint(): smoothScroll: %b", smoothScroll));
        }
        mStatus = SR_STATUS_PREPARE;
        mNeedFilterRefreshEvent = true;
        mDurationToStayAtHint = stayDuration;
        if (mHeaderView != null) {
            mHeaderView.onRefreshPrepare(this);
        }
        mIndicatorSetter.setMovingStatus(Constants.MOVING_HEADER);
        mViewStatus = SR_VIEW_STATUS_HEADER_IN_PROCESSING;
        mAutomaticActionUseSmoothScroll = smoothScroll;
        mAutoHintCanBeInterrupted = canBeInterrupted;
        int offsetToRefreshHint = mTwoLevelIndicator.getOffsetToHintTwoLevelRefresh();
        if (offsetToRefreshHint <= 0) {
            mAutomaticActionTriggered = false;
        } else {
            mAutomaticActionTriggered = true;
            mScrollChecker.scrollTo(
                    offsetToRefreshHint,
                    mAutomaticActionUseSmoothScroll ? mDurationToCloseHeader : 0);
        }
        return true;
    }

    /**
     * Perform auto Two-Level refresh at once.
     *
     * <p>自动二级刷新并立即触发刷新回调
     */
    public boolean autoTwoLevelRefresh() {
        return autoTwoLevelRefresh(Constants.ACTION_NOTIFY, true);
    }

    /**
     * If @param atOnce has been set to true. Auto perform Two-Level refresh at once.
     *
     * <p>自动刷新，`atOnce`立即触发二级刷新回调
     *
     * @param atOnce Auto Two-Level refresh at once
     */
    public boolean autoTwoLevelRefresh(boolean atOnce) {
        return autoTwoLevelRefresh(
                atOnce ? Constants.ACTION_AT_ONCE : Constants.ACTION_NOTIFY, true);
    }

    /**
     * If @param atOnce has been set to true. Auto perform Two-Level refresh at once. If @param
     * smooth has been set to true. Auto perform Two-Level refresh will using smooth scrolling.
     *
     * <p>自动刷新，`atOnce`立即触发刷新回调，`smooth`滚动到触发位置
     *
     * @param atOnce Auto Two-Level refresh at once
     * @param smoothScroll Auto Two-Level refresh use smooth scrolling
     */
    public boolean autoTwoLevelRefresh(boolean atOnce, boolean smoothScroll) {
        return autoTwoLevelRefresh(
                atOnce ? Constants.ACTION_AT_ONCE : Constants.ACTION_NOTIFY, smoothScroll);
    }

    /**
     * The @param action can be used to specify the action to trigger Two-Level refresh. If
     * the`action` been set to `SR_ACTION_NOTHING`, we will not notify the Two-Level refresh
     * listener when in refreshing. If the `action` been set to `SR_ACTION_AT_ONCE`, we will notify
     * the Two-Level refresh listener at once. If the `action` been set to `SR_ACTION_NOTIFY`, we
     * will notify the Two-Level refresh listener when in Two-Level refreshing be later If @param
     * smooth has been set to true. Auto perform Two-Level refresh will using smooth scrolling.
     *
     * <p>自动刷新，`action`触发刷新的动作，`smooth`滚动到触发位置
     *
     * @param action Auto Two-Level refresh use action.{@link Constants#ACTION_NOTIFY}, {@link
     *     Constants#ACTION_AT_ONCE},{@link Constants#ACTION_NOTHING}
     * @param smoothScroll Auto Two-Level refresh use smooth scrolling
     */
    public boolean autoTwoLevelRefresh(@Action int action, boolean smoothScroll) {
        if (mStatus != SR_STATUS_INIT || mNeedFilterRefreshEvent || isDisabledPerformRefresh()) {
            return false;
        }
        if (sDebug) {
            Log.d(
                    TAG,
                    String.format(
                            "autoTwoLevelRefresh(): action: %d, smoothScroll: %b",
                            action, smoothScroll));
        }
        final byte old = mStatus;
        mStatus = SR_STATUS_PREPARE;
        notifyStatusChanged(old, mStatus);
        if (mHeaderView != null) mHeaderView.onRefreshPrepare(this);
        mIndicatorSetter.setMovingStatus(Constants.MOVING_HEADER);
        mViewStatus = SR_VIEW_STATUS_HEADER_IN_PROCESSING;
        mSubFlag |= FLAG_TRIGGERED_TWO_LEVEL_REFRESH;
        mAutomaticActionUseSmoothScroll = smoothScroll;
        mAutomaticAction = action;
        if (mIndicator.getHeaderHeight() <= 0) {
            mAutomaticActionTriggered = false;
        } else {
            scrollToTriggeredTwoLevelAutomatic();
        }
        return true;
    }

    @Override
    public boolean autoRefresh(@Action int action, boolean smoothScroll) {
        if (mNeedFilterRefreshEvent) {
            return false;
        }
        return super.autoRefresh(action, smoothScroll);
    }

    @Override
    public boolean autoLoadMore(@Action int action, boolean smoothScroll) {
        if (mNeedFilterRefreshEvent) {
            return false;
        }
        return super.autoLoadMore(action, smoothScroll);
    }

    @Override
    protected boolean processDispatchTouchEvent(MotionEvent ev) {
        if (mAutoHintCanBeInterrupted) {
            mNeedFilterRefreshEvent = false;
            mDurationToStayAtHint = 0;
            final int action = ev.getAction() & MotionEvent.ACTION_MASK;
            if (action == MotionEvent.ACTION_DOWN) removeCallbacks(mDelayToBackToTopRunnable);
        }
        return super.processDispatchTouchEvent(ev);
    }

    @Override
    protected void tryToPerformAutoRefresh() {
        if (!mAutomaticActionTriggered
                && mStatus == SR_STATUS_PREPARE
                && isHeaderInProcessing()
                && isMovingHeader()) {
            if (mTwoLevelRefreshView == null || mTwoLevelIndicator.getHeaderHeight() <= 0) return;
            if (mNeedFilterRefreshEvent) {
                int offsetToRefreshHint = mTwoLevelIndicator.getOffsetToHintTwoLevelRefresh();
                if (offsetToRefreshHint > 0) {
                    mAutomaticActionTriggered = true;
                    mScrollChecker.scrollTo(
                            offsetToRefreshHint,
                            mAutomaticActionUseSmoothScroll ? mDurationToCloseHeader : 0);
                    return;
                } else {
                    mNeedFilterRefreshEvent = false;
                }
            } else if ((mSubFlag & FLAG_TRIGGERED_TWO_LEVEL_REFRESH) > 0) {
                scrollToTriggeredTwoLevelAutomatic();
                return;
            }
        }
        super.tryToPerformAutoRefresh();
    }

    @Override
    protected boolean isNeedInterceptTouchEvent() {
        return !mAutoHintCanBeInterrupted || super.isNeedInterceptTouchEvent();
    }

    @Override
    protected void onFingerUp() {
        if (canPerformTwoLevelRefresh()
                && mTwoLevelIndicator.crossTwoLevelRefreshLine()
                && mStatus == SR_STATUS_PREPARE) {
            onRelease();
            return;
        }
        super.onFingerUp();
    }

    @Override
    protected boolean tryToNotifyReset() {
        boolean reset = super.tryToNotifyReset();
        if (reset) {
            mNeedFilterRefreshEvent = false;
            mAutoHintCanBeInterrupted = true;
            mDurationToStayAtHint = 0;
            if (mDelayToBackToTopRunnable != null) {
                mDelayToBackToTopRunnable.mLayout = null;
                removeCallbacks(mDelayToBackToTopRunnable);
            }
        }
        return reset;
    }

    @Override
    protected void reset() {
        if (mDelayToBackToTopRunnable != null) {
            mDelayToBackToTopRunnable.mLayout = null;
            removeCallbacks(mDelayToBackToTopRunnable);
        }
        super.reset();
    }

    @Override
    protected void onRelease() {
        if (mDurationToStayAtHint > 0) {
            mAutomaticActionUseSmoothScroll = false;
            delayForStay();
            return;
        }
        tryToPerformRefresh();
        if (canPerformTwoLevelRefresh()
                && isTwoLevelRefreshing()
                && mTwoLevelIndicator.crossTwoLevelRefreshLine()) {
            if (isEnabledKeepRefreshView()) {
                mScrollChecker.scrollTo(
                        mTwoLevelIndicator.getOffsetToKeepTwoLevelHeader(),
                        mDurationOfBackToTwoLevel);
            } else {
                mScrollChecker.scrollTo(IIndicator.START_POS, mDurationToCloseTwoLevel);
            }
            return;
        }
        super.onRelease();
    }

    @Override
    protected void tryToPerformRefresh() {
        if (mNeedFilterRefreshEvent) {
            return;
        }
        if (canPerformTwoLevelRefresh()
                && mStatus == SR_STATUS_PREPARE
                && mTwoLevelIndicator.crossTwoLevelRefreshLine()) {
            mSubFlag |= FLAG_TRIGGER_TWO_LEVEL_REFRESH;
            triggeredRefresh(true);
            return;
        }
        super.tryToPerformRefresh();
    }

    @Override
    protected void tryToPerformRefreshWhenMoved() {
        if (mNeedFilterRefreshEvent) {
            return;
        }
        super.tryToPerformRefreshWhenMoved();
    }

    @Override
    protected void performRefresh(boolean notify) {
        if (isTwoLevelRefreshing()) {
            mLoadingStartTime = SystemClock.uptimeMillis();
            if (sDebug) {
                Log.d(TAG, String.format("onRefreshBegin systemTime: %s", mLoadingStartTime));
            }
            if (mTwoLevelRefreshView != null) {
                mTwoLevelRefreshView.onTwoLevelRefreshBegin(this, mTwoLevelIndicator);
            }
            if (mRefreshListener instanceof OnRefreshListener) {
                ((OnRefreshListener) mRefreshListener).onTwoLevelRefreshing();
            }
            return;
        }
        super.performRefresh(notify);
    }

    @Override
    protected void notifyUIRefreshComplete(
            boolean useScroll, boolean immediatelyNoScrolling, boolean notifyViews) {
        if ((mSubFlag & FLAG_TRIGGER_TWO_LEVEL_REFRESH) > 0) {
            mSubFlag &= ~FLAG_TRIGGER_TWO_LEVEL_REFRESH;
            super.notifyUIRefreshComplete(false, immediatelyNoScrolling, notifyViews);
            if (useScroll) {
                if (immediatelyNoScrolling) {
                    tryScrollBackToTop(0);
                } else {
                    tryScrollBackToTop(mDurationToCloseTwoLevel);
                }
            }
            return;
        }
        super.notifyUIRefreshComplete(true, immediatelyNoScrolling, notifyViews);
    }

    private void scrollToTriggeredTwoLevelAutomatic() {
        switch (mAutomaticAction) {
            case Constants.ACTION_NOTHING:
                triggeredRefresh(false);
                break;
            case Constants.ACTION_NOTIFY:
                mFlag |= FLAG_AUTO_REFRESH;
                break;
            case Constants.ACTION_AT_ONCE:
                triggeredRefresh(true);
                break;
        }
        final int offsetToRefresh = mTwoLevelIndicator.getOffsetToTwoLevelRefresh();
        final int offset;
        if (isEnabledKeepRefreshView()) {
            final int offsetToKeepHeaderWhileLoading =
                    mTwoLevelIndicator.getOffsetToKeepTwoLevelHeader();
            offset =
                    (offsetToKeepHeaderWhileLoading >= offsetToRefresh)
                            ? offsetToKeepHeaderWhileLoading
                            : offsetToRefresh;
        } else {
            offset = offsetToRefresh;
        }
        mAutomaticActionTriggered = true;
        mSubFlag &= ~FLAG_TRIGGERED_TWO_LEVEL_REFRESH;
        if (offset > offsetToRefresh) {
            mScrollChecker.scrollTo(
                    offset, mAutomaticActionUseSmoothScroll ? mDurationOfBackToTwoLevel : 0);
        } else {
            mScrollChecker.scrollTo(
                    offset, mAutomaticActionUseSmoothScroll ? mDurationToCloseTwoLevel : 0);
        }
    }

    private boolean canPerformTwoLevelRefresh() {
        return mTwoLevelRefreshView != null && !isDisabledTwoLevelRefresh() && isMovingHeader();
    }

    private void delayForStay() {
        if (mDelayToBackToTopRunnable == null) {
            mDelayToBackToTopRunnable = new DelayToBackToTop();
        }
        mDelayToBackToTopRunnable.mLayout = this;
        postDelayed(mDelayToBackToTopRunnable, mDurationToStayAtHint);
    }

    public interface OnRefreshListener extends SmoothRefreshLayout.OnRefreshListener {
        void onTwoLevelRefreshing();
    }

    private static class DelayToBackToTop implements Runnable {
        private TwoLevelSmoothRefreshLayout mLayout;

        @Override
        public void run() {
            if (mLayout != null) {
                if (SmoothRefreshLayout.sDebug) {
                    Log.d(mLayout.TAG, "DelayToBackToTop: run()");
                }
                mLayout.mScrollChecker.scrollTo(
                        IIndicator.START_POS, mLayout.mDurationToCloseHeader);
            }
        }
    }
}
