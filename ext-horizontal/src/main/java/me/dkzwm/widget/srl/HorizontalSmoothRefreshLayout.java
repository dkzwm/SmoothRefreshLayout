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
        int height = mHeaderView.getCustomHeight();
        if (mHeaderView.getStyle() == IRefreshView.STYLE_DEFAULT
                || mHeaderView.getStyle() == IRefreshView.STYLE_PIN
                || mHeaderView.getStyle() == IRefreshView.STYLE_FOLLOW_CENTER
                || mHeaderView.getStyle() == IRefreshView.STYLE_FOLLOW_PIN) {
            if (height <= 0) {
                if (height == LayoutParams.MATCH_PARENT) lp.height = LayoutParams.MATCH_PARENT;
            } else lp.width = height;
            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
            mIndicatorSetter.setHeaderHeight(child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin);
        } else {
            if (height <= 0 && height != LayoutParams.MATCH_PARENT) {
                throw new IllegalArgumentException("If header view type is " +
                        "STYLE_SCALE or STYLE_FOLLOW_SCALE, you must set a accurate height");
            } else {
                if (height == LayoutParams.MATCH_PARENT) {
                    int specSize = MeasureSpec.getSize(heightMeasureSpec);
                    height = Math.max(0, specSize - (getPaddingLeft() + getPaddingRight()
                            + lp.leftMargin + lp.rightMargin));
                    mIndicatorSetter.setHeaderHeight(height);
                } else {
                    mIndicatorSetter.setHeaderHeight(height + lp.leftMargin + lp.rightMargin);
                }
            }
            if (mHeaderView.getStyle() == IRefreshView.STYLE_FOLLOW_SCALE) {
                if (mIndicator.getCurrentPos() <= mIndicator.getHeaderHeight()) {
                    lp.width = height;
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
        int height = mFooterView.getCustomHeight();
        if (mFooterView.getStyle() == IRefreshView.STYLE_DEFAULT
                || mFooterView.getStyle() == IRefreshView.STYLE_PIN
                || mFooterView.getStyle() == IRefreshView.STYLE_FOLLOW_CENTER
                || mFooterView.getStyle() == IRefreshView.STYLE_FOLLOW_PIN) {
            if (height <= 0) {
                if (height == LayoutParams.MATCH_PARENT) lp.width = LayoutParams.MATCH_PARENT;
            } else lp.width = height;
            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
            mIndicatorSetter.setFooterHeight(child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin);
        } else {
            if (height <= 0 && height != LayoutParams.MATCH_PARENT) {
                throw new IllegalArgumentException("If footer view type is " +
                        "STYLE_SCALE or STYLE_FOLLOW_SCALE, you must set a accurate height");
            } else {
                if (height == LayoutParams.MATCH_PARENT) {
                    int specSize = MeasureSpec.getSize(heightMeasureSpec);
                    height = Math.max(0, specSize - (getPaddingLeft() + getPaddingRight()
                            + lp.leftMargin + lp.rightMargin));
                    mIndicatorSetter.setFooterHeight(height);
                } else {
                    mIndicatorSetter.setFooterHeight(height + lp.leftMargin + lp.rightMargin);
                }
            }
            if (mFooterView.getStyle() == IRefreshView.STYLE_FOLLOW_SCALE) {
                if (mIndicator.getCurrentPos() <= mIndicator.getFooterHeight()) {
                    lp.width = height;
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
        int contentRight = 0;
        final LayoutParams lp = (LayoutParams) child.getLayoutParams();
        final int top = getPaddingTop() + lp.topMargin;
        final int bottom = top + child.getMeasuredHeight();
        int left, right;
        if (mMode == Constants.MODE_DEFAULT && isMovingHeader()) {
            left = getPaddingLeft() + lp.leftMargin + (pin ? 0 : offsetHeader);
            right = left + child.getMeasuredWidth();
            child.layout(left, top, right, bottom);
        } else if (mMode == Constants.MODE_DEFAULT && isMovingFooter()
                && mStickyHeaderView != child) {
            left = getPaddingLeft() + lp.leftMargin - (pin ? 0 : offsetFooter);
            right = left + child.getMeasuredWidth();
            child.layout(left, top, right, bottom);
        } else {
            left = getPaddingLeft() + lp.leftMargin;
            right = left + child.getMeasuredWidth();
            child.layout(left, top, right, bottom);
        }
        if (sDebug) {
            SRLog.d(TAG, "onLayout(): content: %s %s %s %s", left, top, right, bottom);
        }
        if (mTargetView == child) contentRight = right + lp.rightMargin;
        return contentRight;
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
        right = left + child.getMeasuredWidth();
        bottom = top + child.getMeasuredHeight();
        child.layout(left, top, right, bottom);
        if (sDebug) {
            SRLog.d(TAG, "onLayout(): footer: %s %s %s %s", left, top, right, bottom);
        }
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
        final int left = Math.max(getWidth() - getPaddingRight() - mIndicator
                .getCurrentPos(), getPaddingLeft());
        canvas.drawRect(left, getPaddingTop(), getWidth() - getPaddingRight(),
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
                        ViewCompat.offsetLeftAndRight(mHeaderView.getView(),change);
                        break;
                    case IRefreshView.STYLE_SCALE:
                        needRequestLayout = true;
                        break;
                    case IRefreshView.STYLE_PIN:
                        break;
                    case IRefreshView.STYLE_FOLLOW_PIN:
                        if (mIndicator.getCurrentPos() <= mIndicator.getHeaderHeight())
                            ViewCompat.offsetLeftAndRight(mHeaderView.getView(),change);
                        break;
                    case IRefreshView.STYLE_FOLLOW_SCALE:
                    case IRefreshView.STYLE_FOLLOW_CENTER:
                        if (mIndicator.getCurrentPos() > mIndicator.getHeaderHeight())
                            needRequestLayout = true;
                        else
                            ViewCompat.offsetLeftAndRight(mHeaderView.getView(),change);
                        break;
                }
                if (!isEnabledPinContentView() && mStickyHeaderView != null)
                    ViewCompat.offsetLeftAndRight(mStickyHeaderView,change);
                if (isHeaderInProcessing())
                    mHeaderView.onRefreshPositionChanged(this, mStatus, mIndicator);
                else
                    mHeaderView.onPureScrollPositionChanged(this, mStatus, mIndicator);
            } else if (mFooterView != null && !isDisabledLoadMore() && isMovingFooter
                    && mFooterView.getView().getVisibility() == VISIBLE) {
                final int type = mFooterView.getStyle();
                switch (type) {
                    case IRefreshView.STYLE_DEFAULT:
                        ViewCompat.offsetLeftAndRight(mFooterView.getView(),change);
                        break;
                    case IRefreshView.STYLE_SCALE:
                        needRequestLayout = true;
                        break;
                    case IRefreshView.STYLE_PIN:
                        break;
                    case IRefreshView.STYLE_FOLLOW_PIN:
                        if (mIndicator.getCurrentPos() <= mIndicator.getFooterHeight())
                            ViewCompat.offsetLeftAndRight(mFooterView.getView(),change);
                        break;
                    case IRefreshView.STYLE_FOLLOW_SCALE:
                    case IRefreshView.STYLE_FOLLOW_CENTER:
                        if (mIndicator.getCurrentPos() > mIndicator.getFooterHeight())
                            needRequestLayout = true;
                        else
                            ViewCompat.offsetLeftAndRight(mFooterView.getView(),change);
                        break;
                }
                if (isFooterInProcessing())
                    mFooterView.onRefreshPositionChanged(this, mStatus, mIndicator);
                else
                    mFooterView.onPureScrollPositionChanged(this, mStatus, mIndicator);
            }
            if (!isEnabledPinContentView()) {
                if (mScrollTargetView != null && isMovingFooter) {
                    ViewCompat.offsetLeftAndRight(mScrollTargetView,change);
                } else if (mAutoFoundScrollTargetView != null && isMovingFooter) {
                    ViewCompat.offsetLeftAndRight(mAutoFoundScrollTargetView,change);
                } else {
                    if (mTargetView != null)
                        ViewCompat.offsetLeftAndRight(mTargetView,change);
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
    public boolean isNotYetInEdgeCannotMoveHeader() {
        if (mInEdgeCanMoveHeaderCallBack != null)
            return mInEdgeCanMoveHeaderCallBack.isNotYetInEdgeCannotMoveHeader(this,
                    mTargetView, mHeaderView);
        if (mScrollTargetView != null)
            return HorizontalScrollCompat.canChildScrollLeft(mScrollTargetView);
        if (mAutoFoundScrollTargetView != null)
            return HorizontalScrollCompat.canChildScrollLeft(mAutoFoundScrollTargetView);
        return HorizontalScrollCompat.canChildScrollLeft(mTargetView);
    }

    @Override
    public boolean isNotYetInEdgeCannotMoveFooter() {
        if (mInEdgeCanMoveFooterCallBack != null)
            return mInEdgeCanMoveFooterCallBack.isNotYetInEdgeCannotMoveFooter(this,
                    mTargetView, mFooterView);
        if (mScrollTargetView != null)
            return HorizontalScrollCompat.canChildScrollRight(mScrollTargetView);
        if (mAutoFoundScrollTargetView != null)
            return HorizontalScrollCompat.canChildScrollRight(mAutoFoundScrollTargetView);
        return HorizontalScrollCompat.canChildScrollRight(mTargetView);
    }

    protected boolean isInsideAnotherDirectionView(final float x, final float y) {
        if (mInsideAnotherDirectionViewCallback != null)
            return mInsideAnotherDirectionViewCallback.isInside(x, y, mTargetView);
        return HorizontalBoundaryUtil.isFingerInsideVerticalView(x, y, mTargetView);
    }

    @Override
    protected void compatLoadMoreScroll(float delta) {
        if (mLoadMoreScrollCallback == null) {
            if (mScrollTargetView != null)
                HorizontalScrollCompat.scrollCompat(mScrollTargetView, delta);
            if (mAutoFoundScrollTargetView != null) {
                HorizontalScrollCompat.scrollCompat(mAutoFoundScrollTargetView, delta);
            } else if (mTargetView != null)
                HorizontalScrollCompat.scrollCompat(mTargetView, delta);
        } else {
            mLoadMoreScrollCallback.onScroll(mTargetView, delta);
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
    protected boolean isScrollingView(View target) {
        return HorizontalScrollCompat.isScrollingView(target);
    }
}
