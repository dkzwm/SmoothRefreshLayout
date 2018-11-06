package me.dkzwm.widget.srl;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import me.dkzwm.widget.srl.config.Constants;
import me.dkzwm.widget.srl.extra.IRefreshView;
import me.dkzwm.widget.srl.indicator.DefaultIndicator;
import me.dkzwm.widget.srl.indicator.HorizontalDefaultIndicator;
import me.dkzwm.widget.srl.utils.HorizontalBoundaryUtil;
import me.dkzwm.widget.srl.utils.HorizontalScrollCompat;
import me.dkzwm.widget.srl.utils.SRLog;

/**
 * Created by dkzwm on 2017/10/20.
 * <p>
 * Support Horizontal refresh feature;<br/>
 * </p>
 *
 * @author dkzwm
 */
public class HorizontalSmoothRefreshLayout extends SmoothRefreshLayout {

    public HorizontalSmoothRefreshLayout(Context context) {
        super(context);
    }

    public HorizontalSmoothRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HorizontalSmoothRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public HorizontalSmoothRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    final public int getSupportScrollAxis() {
        return ViewCompat.SCROLL_AXIS_HORIZONTAL;
    }

    @Override
    protected void createIndicator() {
        DefaultIndicator indicator = new HorizontalDefaultIndicator();
        mIndicator = indicator;
        mIndicatorSetter = indicator;
    }

    @Override
    protected void measureHeader(View child, LayoutParams lp, int widthMeasureSpec, int heightMeasureSpec) {
        if (isDisabledRefresh())
            return;
        int size = mHeaderView.getCustomHeight();
        if (mHeaderView.getStyle() == IRefreshView.STYLE_DEFAULT
                || mHeaderView.getStyle() == IRefreshView.STYLE_PIN
                || mHeaderView.getStyle() == IRefreshView.STYLE_FOLLOW_CENTER
                || mHeaderView.getStyle() == IRefreshView.STYLE_FOLLOW_PIN) {
            if (size <= 0) {
                if (size == LayoutParams.MATCH_PARENT) lp.height = LayoutParams.MATCH_PARENT;
            } else lp.width = size;
            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
            mIndicatorSetter.setHeaderHeight(child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin);
        } else {
            if (size <= 0 && size != LayoutParams.MATCH_PARENT) {
                throw new IllegalArgumentException("If header view type is " +
                        "STYLE_SCALE or STYLE_FOLLOW_SCALE, you must set a accurate height");
            } else {
                if (size == LayoutParams.MATCH_PARENT) {
                    int specSize = MeasureSpec.getSize(heightMeasureSpec);
                    size = Math.max(0, specSize - (getPaddingLeft() + getPaddingRight()
                            + lp.leftMargin + lp.rightMargin));
                    mIndicatorSetter.setHeaderHeight(size);
                } else {
                    mIndicatorSetter.setHeaderHeight(size + lp.leftMargin + lp.rightMargin);
                }
            }
            if (mHeaderView.getStyle() == IRefreshView.STYLE_FOLLOW_SCALE) {
                if (mIndicator.getCurrentPos() <= mIndicator.getHeaderHeight()) {
                    lp.width = size;
                    measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
                    return;
                }
            }
            final int childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec,
                    getPaddingTop() + getPaddingBottom() + lp.topMargin + lp.bottomMargin, lp
                            .height);
            final int childWidthMeasureSpec;
            if (isMovingHeader()) {
                final int maxWidth = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft()
                        - getPaddingRight() - lp.leftMargin - lp.rightMargin;
                int realWidth = Math.min(mIndicator.getCurrentPos() - lp.topMargin - lp
                        .rightMargin, maxWidth);
                childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(realWidth > 0 ? realWidth : 0,
                        MeasureSpec.EXACTLY);
            } else {
                childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.EXACTLY);
            }
            child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
        }
    }

    @Override
    protected void measureFooter(View child, LayoutParams lp, int widthMeasureSpec, int heightMeasureSpec) {
        if (isDisabledLoadMore())
            return;
        int size = mFooterView.getCustomHeight();
        if (mFooterView.getStyle() == IRefreshView.STYLE_DEFAULT
                || mFooterView.getStyle() == IRefreshView.STYLE_PIN
                || mFooterView.getStyle() == IRefreshView.STYLE_FOLLOW_CENTER
                || mFooterView.getStyle() == IRefreshView.STYLE_FOLLOW_PIN) {
            if (size <= 0) {
                if (size == LayoutParams.MATCH_PARENT) lp.width = LayoutParams.MATCH_PARENT;
            } else lp.width = size;
            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
            mIndicatorSetter.setFooterHeight(child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin);
        } else {
            if (size <= 0 && size != LayoutParams.MATCH_PARENT) {
                throw new IllegalArgumentException("If footer view type is " +
                        "STYLE_SCALE or STYLE_FOLLOW_SCALE, you must set a accurate height");
            } else {
                if (size == LayoutParams.MATCH_PARENT) {
                    int specSize = MeasureSpec.getSize(heightMeasureSpec);
                    size = Math.max(0, specSize - (getPaddingLeft() + getPaddingRight()
                            + lp.leftMargin + lp.rightMargin));
                    mIndicatorSetter.setFooterHeight(size);
                } else {
                    mIndicatorSetter.setFooterHeight(size + lp.leftMargin + lp.rightMargin);
                }
            }
            if (mFooterView.getStyle() == IRefreshView.STYLE_FOLLOW_SCALE) {
                if (mIndicator.getCurrentPos() <= mIndicator.getFooterHeight()) {
                    lp.width = size;
                    measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
                    return;
                }
            }
            final int childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec,
                    getPaddingTop() + getPaddingBottom() + lp.topMargin + lp.bottomMargin, lp.height);
            final int childWidthMeasureSpec;
            if (isMovingFooter()) {
                final int maxWidth = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft()
                        - getPaddingRight() - lp.leftMargin - lp.rightMargin;
                int realWidth = Math.min(mIndicator.getCurrentPos() - lp.topMargin - lp
                        .rightMargin, maxWidth);
                childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(realWidth > 0 ? realWidth : 0,
                        MeasureSpec.EXACTLY);
            } else {
                childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.EXACTLY);
            }
            child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
        }
    }

    @Override
    protected int layoutContentView(View child, boolean pin, int offsetHeader, int offsetFooter) {
        final LayoutParams lp = (LayoutParams) child.getLayoutParams();
        final int top = getPaddingTop() + lp.topMargin;
        final int bottom = top + child.getMeasuredHeight();
        int left, right;
        if (isMovingHeader()) {
            left = getPaddingLeft() + lp.leftMargin + (pin ? 0 : offsetHeader);
            right = left + child.getMeasuredWidth();
            child.layout(left, top, right, bottom);
        } else if (isMovingFooter()) {
            left = getPaddingLeft() + lp.leftMargin - (pin ? 0 : offsetFooter);
            right = left + child.getMeasuredWidth();
            child.layout(left, top, right, bottom);
        } else {
            left = getPaddingLeft() + lp.leftMargin;
            right = left + child.getMeasuredWidth();
            child.layout(left, top, right, bottom);
        }
        if (sDebug) SRLog.d(TAG, "onLayout(): content: %s %s %s %s", left, top, right, bottom);
        return right;
    }

    @Override
    protected void layoutHeaderView(View child, int offsetHeader) {
        if (mMode != Constants.MODE_DEFAULT || isDisabledRefresh()
                || child.getMeasuredWidth() == 0) {
            child.layout(0, 0, 0, 0);
            if (sDebug) {
                SRLog.d(TAG, "onLayout(): header: %s %s %s %s", 0, 0, 0, 0);
            }
            return;
        }
        final LayoutParams lp = (LayoutParams) child.getLayoutParams();
        @IRefreshView.RefreshViewStyle final int type = mHeaderView.getStyle();
        int left = 0, right, top, bottom;
        switch (type) {
            case IRefreshView.STYLE_DEFAULT:
                int offset = offsetHeader - child.getMeasuredWidth();
                left = getPaddingLeft() + offset - lp.rightMargin;
                break;
            case IRefreshView.STYLE_PIN:
            case IRefreshView.STYLE_SCALE:
                left = getPaddingLeft() + lp.leftMargin;
                break;
            case IRefreshView.STYLE_FOLLOW_PIN:
            case IRefreshView.STYLE_FOLLOW_SCALE:
                if (offsetHeader <= mIndicator.getHeaderHeight()) {
                    left = getPaddingLeft() + offsetHeader - child.getMeasuredWidth() - lp
                            .rightMargin;
                } else {
                    left = getPaddingLeft() + lp.leftMargin;
                }
                break;
            case IRefreshView.STYLE_FOLLOW_CENTER:
                if (offsetHeader <= mIndicator.getHeaderHeight()) {
                    left = getPaddingLeft() + offsetHeader - child.getMeasuredWidth() - lp
                            .rightMargin;
                } else {
                    left = getPaddingLeft() + lp.leftMargin + (offsetHeader - mIndicator
                            .getHeaderHeight()) / 2;
                }
                break;
        }
        top = getPaddingTop() + lp.topMargin;
        if (isInEditMode())
            left = left + child.getMeasuredWidth();
        right = left + child.getMeasuredWidth();
        bottom = top + child.getMeasuredHeight();
        child.layout(left, top, right, bottom);
        if (sDebug) {
            SRLog.d(TAG, "onLayout(): header: %s %s %s %s", left, top, right, bottom);
        }
    }

    @Override
    protected void layoutFooterView(View child, int offsetFooter, boolean pin, int contentRight) {
        if (mMode != Constants.MODE_DEFAULT || isDisabledLoadMore()
                || child.getMeasuredWidth() == 0) {
            child.layout(0, 0, 0, 0);
            if (sDebug) {
                SRLog.d(TAG, "onLayout(): footer: %s %s %s %s", 0, 0, 0, 0);
            }
            return;
        }
        final LayoutParams lp = (LayoutParams) child.getLayoutParams();
        @IRefreshView.RefreshViewStyle final int type = mFooterView.getStyle();
        int left = 0, right, top, bottom;
        switch (type) {
            case IRefreshView.STYLE_DEFAULT:
            case IRefreshView.STYLE_SCALE:
                left = lp.leftMargin + contentRight - (pin ? offsetFooter : 0);
                break;
            case IRefreshView.STYLE_PIN:
                left = getMeasuredWidth() - child.getMeasuredWidth() - lp.rightMargin
                        - getPaddingRight();
                break;
            case IRefreshView.STYLE_FOLLOW_PIN:
            case IRefreshView.STYLE_FOLLOW_SCALE:
                if (offsetFooter <= mIndicator.getFooterHeight()) {
                    left = lp.leftMargin + contentRight - (pin ? offsetFooter : 0);
                } else {
                    left = getMeasuredWidth() - child.getMeasuredWidth() - lp.rightMargin
                            - getPaddingRight();
                }
                break;
            case IRefreshView.STYLE_FOLLOW_CENTER:
                if (offsetFooter <= mIndicator.getFooterHeight()) {
                    left = lp.leftMargin + contentRight - (pin ? offsetFooter : 0);
                } else {
                    left = lp.leftMargin + contentRight - (pin ? offsetFooter : 0)
                            + (offsetFooter - mIndicator.getFooterHeight()) / 2;
                }
                break;
        }
        top = getPaddingTop() + lp.topMargin;
        if (isInEditMode())
            left = left - child.getMeasuredWidth();
        right = left + child.getMeasuredWidth();
        bottom = top + child.getMeasuredHeight();
        child.layout(left, top, right, bottom);
        if (sDebug) {
            SRLog.d(TAG, "onLayout(): footer: %s %s %s %s", left, top, right, bottom);
        }
    }

    @Override
    protected void layoutStickyHeader(boolean pin, int offsetHeader) {
        final LayoutParams lp = (LayoutParams) mStickyHeaderView.getLayoutParams();
        final int top = getPaddingTop() + lp.topMargin;
        final int bottom = top + mStickyHeaderView.getMeasuredHeight();
        int left, right;
        if (isMovingHeader()) {
            left = getPaddingLeft() + lp.leftMargin + (pin ? 0 : offsetHeader);
        } else {
            left = getPaddingLeft() + lp.leftMargin;
        }
        right = left + mStickyHeaderView.getMeasuredWidth();
        mStickyHeaderView.layout(left, top, right, bottom);
        if (sDebug) SRLog.d(TAG, "onLayout(): stickyHeader: %s %s %s %s", left, top, right, bottom);
    }

    @Override
    protected void layoutStickyFooter(int contentRight, int offsetFooterY) {
        if (!isMovingFooter()) contentRight = getMeasuredWidth();
        final LayoutParams lp = (LayoutParams) mStickyFooterView.getLayoutParams();
        final int top = getPaddingTop() + lp.topMargin;
        final int bottom = top + mStickyFooterView.getMeasuredHeight();
        final int right = contentRight - lp.bottomMargin;
        final int left = right - mStickyFooterView.getMeasuredWidth();
        mStickyFooterView.layout(left, top, right, bottom);
        if (sDebug) SRLog.d(TAG, "onLayout(): stickyFooter: %s %s %s %s", left, top, right, bottom);
    }

    @Override
    protected void drawHeaderBackground(Canvas canvas) {
        final int right = Math.min(getPaddingLeft() + mIndicator.getCurrentPos(),
                getWidth() - getPaddingLeft());
        canvas.drawRect(getPaddingLeft(), getPaddingTop(), right, getHeight() -
                getPaddingBottom(), mBackgroundPaint);
    }

    @Override
    protected void drawFooterBackground(Canvas canvas) {
        final int left, right;
        if (mTargetView != null) {
            final LayoutParams lp = (LayoutParams) mTargetView.getLayoutParams();
            right = getPaddingLeft() + lp.leftMargin + mTargetView.getMeasuredWidth() + lp
                    .rightMargin;
            left = right - mIndicator.getCurrentPos();
        } else {
            left = Math.max(getWidth() - getPaddingRight() - mIndicator
                    .getCurrentPos(), getPaddingLeft());
            right = getWidth() - getPaddingRight();
        }
        canvas.drawRect(left, getPaddingTop(), right,
                getHeight() - getPaddingBottom(), mBackgroundPaint);
    }

    @Override
    protected void tryToDealAnotherDirectionMove(float offsetX, float offsetY) {
        if (isDisabledWhenAnotherDirectionMove() && mIsFingerInsideAnotherDirectionView) {
            if ((Math.abs(offsetY) >= mTouchSlop
                    && Math.abs(offsetY) > Math.abs(offsetX))) {
                mPreventForAnotherDirection = true;
                mDealAnotherDirectionMove = true;
            } else if (Math.abs(offsetX) < mTouchSlop
                    && Math.abs(offsetY) < mTouchSlop) {
                mDealAnotherDirectionMove = false;
                mPreventForAnotherDirection = true;
            } else {
                mDealAnotherDirectionMove = true;
                mPreventForAnotherDirection = false;
            }
        } else {
            mPreventForAnotherDirection = Math.abs(offsetX) < mTouchSlop
                    && Math.abs(offsetY) < mTouchSlop;
            if (!mPreventForAnotherDirection)
                mDealAnotherDirectionMove = true;
        }
    }

    /**
     * Set a callback to override {@link SmoothRefreshLayout#isNotYetInEdgeCannotMoveFooter()} method. Non-null
     * callback will return the value provided by the callback and ignore all internal logic.<br/>
     * <p>
     * 设置{@link SmoothRefreshLayout#isNotYetInEdgeCannotMoveFooter()}的重载回调，用来检测内容视图是否在最右侧
     *
     * @param callback Callback that should be called when isChildNotYetInEdgeCannotMoveFooter() is called.
     */
    @Override
    public void setOnFooterEdgeDetectCallBack(OnFooterEdgeDetectCallBack callback) {
        super.setOnFooterEdgeDetectCallBack(callback);
    }

    /**
     * Set a callback to override {@link SmoothRefreshLayout#isNotYetInEdgeCannotMoveHeader()} method. Non-null
     * callback will return the value provided by the callback and ignore all internal logic.<br/>
     * <p>
     * 设置{@link SmoothRefreshLayout#isNotYetInEdgeCannotMoveHeader()}的重载回调，用来检测内容视图是否在最左边
     *
     * @param callback Callback that should be called when isChildNotYetInEdgeCannotMoveHeader() is called.
     */
    @Override
    public void setOnHeaderEdgeDetectCallBack(OnHeaderEdgeDetectCallBack callback) {
        super.setOnHeaderEdgeDetectCallBack(callback);
    }

    @Override
    protected void addFreshViewLayoutParams(View view) {
        ViewGroup.LayoutParams lp = view.getLayoutParams();
        if (lp == null) {
            lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
            view.setLayoutParams(lp);
        }
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
    }

    @Override
    protected void resetViewScale(View targetView) {
        if (HorizontalScrollCompat.canScaleInternal(targetView)) {
            View view = ((ViewGroup) targetView).getChildAt(0);
            view.setPivotX(0);
            view.setPivotY(0);
            view.setScaleX(1);
            view.setScaleY(1);
        }
    }

    @Override
    protected boolean offsetChild(int change, boolean isMovingHeader, boolean isMovingFooter) {
        boolean needRequestLayout = false;
        if (mMode == Constants.MODE_DEFAULT) {
            if (mHeaderView != null && !isDisabledRefresh() && isMovingHeader
                    && mHeaderView.getView().getVisibility() == VISIBLE) {
                final int type = mHeaderView.getStyle();
                switch (type) {
                    case IRefreshView.STYLE_DEFAULT:
                        ViewCompat.offsetLeftAndRight(mHeaderView.getView(), change);
                        break;
                    case IRefreshView.STYLE_SCALE:
                        needRequestLayout = true;
                        break;
                    case IRefreshView.STYLE_PIN:
                        break;
                    case IRefreshView.STYLE_FOLLOW_PIN:
                        if (mIndicator.getCurrentPos() <= mIndicator.getHeaderHeight())
                            ViewCompat.offsetLeftAndRight(mHeaderView.getView(), change);
                        break;
                    case IRefreshView.STYLE_FOLLOW_SCALE:
                    case IRefreshView.STYLE_FOLLOW_CENTER:
                        if (mIndicator.getCurrentPos() > mIndicator.getHeaderHeight())
                            needRequestLayout = true;
                        else
                            ViewCompat.offsetLeftAndRight(mHeaderView.getView(), change);
                        break;
                }
                if (isHeaderInProcessing())
                    mHeaderView.onRefreshPositionChanged(this, mStatus, mIndicator);
                else
                    mHeaderView.onPureScrollPositionChanged(this, mStatus, mIndicator);
            } else if (mFooterView != null && !isDisabledLoadMore() && isMovingFooter
                    && mFooterView.getView().getVisibility() == VISIBLE) {
                final int type = mFooterView.getStyle();
                switch (type) {
                    case IRefreshView.STYLE_DEFAULT:
                        ViewCompat.offsetLeftAndRight(mFooterView.getView(), change);
                        break;
                    case IRefreshView.STYLE_SCALE:
                        needRequestLayout = true;
                        break;
                    case IRefreshView.STYLE_PIN:
                        break;
                    case IRefreshView.STYLE_FOLLOW_PIN:
                        if (mIndicator.getCurrentPos() <= mIndicator.getFooterHeight())
                            ViewCompat.offsetLeftAndRight(mFooterView.getView(), change);
                        break;
                    case IRefreshView.STYLE_FOLLOW_SCALE:
                    case IRefreshView.STYLE_FOLLOW_CENTER:
                        if (mIndicator.getCurrentPos() > mIndicator.getFooterHeight())
                            needRequestLayout = true;
                        else
                            ViewCompat.offsetLeftAndRight(mFooterView.getView(), change);
                        break;
                }
                if (isFooterInProcessing())
                    mFooterView.onRefreshPositionChanged(this, mStatus, mIndicator);
                else
                    mFooterView.onPureScrollPositionChanged(this, mStatus, mIndicator);
            }
            if (!isEnabledPinContentView()) {
                if (isMovingHeader && mStickyHeaderView != null)
                    ViewCompat.offsetLeftAndRight(mStickyHeaderView, change);
                if (isMovingFooter && mStickyFooterView != null)
                    ViewCompat.offsetLeftAndRight(mStickyFooterView, change);
                if (mScrollTargetView != null && isMovingFooter) {
                    mScrollTargetView.setTranslationX(-mIndicator.getCurrentPos());
                } else if (mAutoFoundScrollTargetView != null && isMovingFooter) {
                    mAutoFoundScrollTargetView.setTranslationX(-mIndicator.getCurrentPos());
                } else {
                    if (mTargetView != null)
                        ViewCompat.offsetLeftAndRight(mTargetView, change);
                }
            }
        } else {
            if (mTargetView != null) {
                if (isMovingHeader) {
                    if (HorizontalScrollCompat.canScaleInternal(mTargetView)) {
                        View view = ((ViewGroup) mTargetView).getChildAt(0);
                        view.setPivotX(0);
                        view.setScaleX(calculateScale());
                    } else {
                        mTargetView.setPivotX(0);
                        mTargetView.setScaleX(calculateScale());
                    }
                } else if (isMovingFooter) {
                    final View targetView;
                    if (mScrollTargetView != null) {
                        targetView = mScrollTargetView;
                    } else if (mAutoFoundScrollTargetView != null) {
                        targetView = mAutoFoundScrollTargetView;
                    } else {
                        targetView = mTargetView;
                    }
                    if (HorizontalScrollCompat.canScaleInternal(targetView)) {
                        View view = ((ViewGroup) targetView).getChildAt(0);
                        view.setPivotX(view.getWidth());
                        view.setScaleX(calculateScale());
                    } else {
                        targetView.setPivotX(getWidth());
                        targetView.setScaleX(calculateScale());
                    }
                }
            }
        }
        return needRequestLayout;
    }

    @Override
    public boolean isNotYetInEdgeCannotMoveHeader(View view) {
        if (mInEdgeCanMoveHeaderCallBack != null)
            return mInEdgeCanMoveHeaderCallBack.isNotYetInEdgeCannotMoveHeader(this,
                    view, mHeaderView);
        return HorizontalScrollCompat.canChildScrollLeft(view);
    }

    @Override
    public boolean isNotYetInEdgeCannotMoveFooter(View view) {
        if (mInEdgeCanMoveFooterCallBack != null)
            return mInEdgeCanMoveFooterCallBack.isNotYetInEdgeCannotMoveFooter(this,
                    view, mFooterView);
        return HorizontalScrollCompat.canChildScrollRight(view);
    }

    protected boolean isInsideAnotherDirectionView(final float x, final float y) {
        if (mInsideAnotherDirectionViewCallback != null)
            return mInsideAnotherDirectionViewCallback.isInside(x, y, mTargetView);
        return HorizontalBoundaryUtil.isFingerInsideVerticalView(x, y, mTargetView);
    }

    @Override
    protected void compatLoadMoreScroll(View view, float delta) {
        if (mLoadMoreScrollCallback == null) {
            HorizontalScrollCompat.scrollCompat(view, delta);
        } else {
            mLoadMoreScrollCallback.onScroll(view, delta);
        }
    }

    @Override
    protected void dispatchNestedFling(int velocity) {
        if (sDebug) SRLog.d(TAG, "dispatchNestedFling() : %s", velocity);
        if (mScrollTargetView != null)
            HorizontalScrollCompat.flingCompat(mScrollTargetView, -velocity);
        else if (mAutoFoundScrollTargetView != null) {
            HorizontalScrollCompat.flingCompat(mAutoFoundScrollTargetView, -velocity);
        } else
            HorizontalScrollCompat.flingCompat(mTargetView, -velocity);
    }

    @Override
    protected boolean canAutoLoadMore(View view) {
        if (mAutoLoadMoreCallBack != null)
            mAutoLoadMoreCallBack.canAutoLoadMore(this, view);
        return HorizontalScrollCompat.canAutoLoadMore(view);
    }

    @Override
    protected boolean canAutoRefresh(View view) {
        if (mAutoRefreshCallBack != null)
            mAutoRefreshCallBack.canAutoRefresh(this, view);
        return HorizontalScrollCompat.canAutoRefresh(view);
    }

    @Override
    protected boolean isScrollingView(View target) {
        return HorizontalScrollCompat.isScrollingView(target);
    }
}
