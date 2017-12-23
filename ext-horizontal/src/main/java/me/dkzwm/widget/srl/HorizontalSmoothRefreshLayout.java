package me.dkzwm.widget.srl;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.Arrays;

import me.dkzwm.widget.srl.extra.IRefreshView;
import me.dkzwm.widget.srl.indicator.HorizontalDefaultIndicator;
import me.dkzwm.widget.srl.indicator.IIndicator;
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

    @Override
    final public int getSupportScrollAxis() {
        return ViewCompat.SCROLL_AXIS_HORIZONTAL;
    }

    @Override
    protected void createIndicator() {
        mIndicator = new HorizontalDefaultIndicator();
    }

    @Override
    protected void measureHeader(View child, LayoutParams lp, int widthMeasureSpec, int heightMeasureSpec) {
        if (isDisabledRefresh() || isEnabledHideHeaderView())
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
            mIndicator.setHeaderHeight(child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin);
        } else {
            if (height <= 0 && height != LayoutParams.MATCH_PARENT) {
                throw new IllegalArgumentException("If header view type is " +
                        "STYLE_SCALE or STYLE_FOLLOW_SCALE, you must set a accurate height");
            } else {
                if (height == LayoutParams.MATCH_PARENT) {
                    int specSize = MeasureSpec.getSize(heightMeasureSpec);
                    height = Math.max(0, specSize - (getPaddingLeft() + getPaddingRight()
                            + lp.leftMargin + lp.rightMargin));
                    mIndicator.setHeaderHeight(height);
                } else {
                    mIndicator.setHeaderHeight(height + lp.leftMargin + lp.rightMargin);
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
        if (isDisabledLoadMore() || isEnabledHideFooterView())
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
            mIndicator.setFooterHeight(child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin);
        } else {
            if (height <= 0 && height != LayoutParams.MATCH_PARENT) {
                throw new IllegalArgumentException("If footer view type is " +
                        "STYLE_SCALE or STYLE_FOLLOW_SCALE, you must set a accurate height");
            } else {
                if (height == LayoutParams.MATCH_PARENT) {
                    int specSize = MeasureSpec.getSize(heightMeasureSpec);
                    height = Math.max(0, specSize - (getPaddingLeft() + getPaddingRight()
                            + lp.leftMargin + lp.rightMargin));
                    mIndicator.setFooterHeight(height);
                } else {
                    mIndicator.setFooterHeight(height + lp.leftMargin + lp.rightMargin);
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
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int count = getChildCount();
        if (count == 0)
            return;
        checkViewsZAxisNeedReset();
        final int paddingLeft = getPaddingLeft();
        final int paddingTop = getPaddingTop();
        final int parentRight = r - l - getPaddingRight();
        final int parentBottom = b - t - getPaddingBottom();
        int offsetHeader = 0;
        int offsetFooter = 0;
        if (isMovingHeader()) {
            offsetHeader = mIndicator.getCurrentPos();
        } else if (isMovingFooter()) {
            offsetFooter = mIndicator.getCurrentPos();
        }
        int contentRight = 0;
        boolean pin = (mScrollTargetView != null && !isMovingHeader()) || isEnabledPinContentView();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() == GONE)
                continue;
            if (mHeaderView != null && child == mHeaderView.getView()) {
                layoutHeaderView(child, offsetHeader);
            } else if (mTargetView != null && child == mTargetView
                    || (mPreviousState != STATE_NONE && mChangeStateAnimator != null
                    && mChangeStateAnimator.isRunning() && getView(mPreviousState) == child)) {
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                final int top = paddingTop + lp.topMargin;
                final int bottom = top + child.getMeasuredHeight();
                int left, right;
                if (isMovingHeader()) {
                    left = paddingLeft + lp.leftMargin + (pin ? 0 : offsetHeader);
                    right = left + child.getMeasuredWidth();
                    child.layout(left, top, right, bottom);
                } else if (isMovingFooter()) {
                    left = paddingLeft + lp.leftMargin - (pin ? 0 : offsetFooter);
                    right = left + child.getMeasuredWidth();
                    child.layout(left, top, right, bottom);
                } else {
                    left = paddingLeft + lp.leftMargin;
                    right = left + child.getMeasuredWidth();
                    child.layout(left, top, right, bottom);
                }
                if (sDebug) {
                    SRLog.d(TAG, "onLayout(): content: %s %s %s %s", left, top, right, bottom);
                }
                contentRight = right + lp.rightMargin;
            } else if (mFooterView == null || mFooterView.getView() != child) {
                layoutOtherViewUseGravity(child, parentRight, parentBottom);
            }
        }
        if (mFooterView != null && mFooterView.getView().getVisibility() != GONE) {
            layoutFooterView(mFooterView.getView(), offsetFooter, pin, contentRight);
        }
        tryToPerformAutoRefresh();
    }

    @Override
    protected void layoutHeaderView(View child, int offsetHeader) {
        if (isDisabledRefresh() || isEnabledHideHeaderView() || child.getMeasuredWidth() == 0) {
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
        if (isDisabledLoadMore() || isEnabledHideFooterView() || child.getMeasuredWidth() == 0) {
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
    protected void drawRefreshViewBackground(Canvas canvas) {
        if (mBackgroundPaint != null && !isEnabledPinContentView() && !mIndicator.isInStartPosition()) {
            if (!isDisabledRefresh() && isMovingHeader() && mHeaderBackgroundColor != -1) {
                mBackgroundPaint.setColor(mHeaderBackgroundColor);
                final int right = Math.min(getPaddingLeft() + mIndicator.getCurrentPos(),
                        getWidth() - getPaddingLeft());
                canvas.drawRect(getPaddingLeft(), getPaddingTop(), right, getHeight() -
                        getPaddingBottom(), mBackgroundPaint);
            } else if (!isDisabledLoadMore() && isMovingFooter() && mFooterBackgroundColor != -1) {
                mBackgroundPaint.setColor(mFooterBackgroundColor);
                final int left = Math.max(getWidth() - getPaddingRight() - mIndicator
                        .getCurrentPos(), getPaddingLeft());
                canvas.drawRect(left, getPaddingTop(), getWidth() - getPaddingRight(),
                        getHeight() - getPaddingBottom(), mBackgroundPaint);
            }
        }
    }

    @Override
    protected boolean processDispatchTouchEvent(MotionEvent ev) {
        final int action = ev.getAction() & MotionEvent.ACTION_MASK;
        switch (action) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_DOWN:
                return super.processDispatchTouchEvent(ev);
            case MotionEvent.ACTION_MOVE:
                if (sDebug) {
                    SRLog.d(TAG, "processDispatchTouchEvent(): action: %s", action);
                }
                if (!mIndicator.hasTouched()) {
                    return dispatchTouchEventSuper(ev);
                }
                final int index = ev.findPointerIndex(mTouchPointerId);
                if (index < 0) {
                    Log.e(TAG, "Error processing scroll; pointer index for id " +
                            mTouchPointerId + " not found. Did any MotionEvents get skipped?");
                    return dispatchTouchEventSuper(ev);
                }
                mLastMoveEvent = ev;
                if (tryToFilterTouchEventInDispatchTouchEvent(ev))
                    return true;
                tryToResetMovingStatus();
                mIndicator.onFingerMove(ev.getX(index), ev.getY(index));
                float offsetX, offsetY;
                final float[] pressDownPoint = mIndicator.getFingerDownPoint();
                offsetX = ev.getX(index) - pressDownPoint[0];
                offsetY = ev.getY(index) - pressDownPoint[1];
                if (isDisabledWhenAnotherDirectionMove() && mIsFingerInsideAnotherDirectionView) {
                    if (!mDealAnotherDirectionMove) {
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
                    }
                } else {
                    if (Math.abs(offsetX) < mTouchSlop
                            && Math.abs(offsetY) < mTouchSlop) {
                        return dispatchTouchEventSuper(ev);
                    }
                }
                if (mPreventForAnotherDirection) {
                    return dispatchTouchEventSuper(ev);
                }
                final boolean canNotChildScrollRight = !isChildNotYetInEdgeCannotMoveFooter();
                final boolean canNotChildScrollLeft = !isChildNotYetInEdgeCannotMoveHeader();
                offsetX = mIndicator.getOffset();
                int current = mIndicator.getCurrentPos();
                boolean movingRight = offsetX > 0;
                if (isMovingFooter() && isFooterInProcessing() && mStatus == SR_STATUS_COMPLETE
                        && mIndicator.hasLeftStartPosition() && !canNotChildScrollRight) {
                    mScrollChecker.tryToScrollTo(IIndicator.START_POS, 0);
                    return dispatchTouchEventSuper(ev);
                }
                float maxHeaderDistance = mIndicator.getCanMoveTheMaxDistanceOfHeader();
                if (movingRight && isMovingHeader() && !mIndicator.isInStartPosition()
                        && maxHeaderDistance > 0) {
                    if (current >= maxHeaderDistance) {
                        updateAnotherDirectionPos();
                        return dispatchTouchEventSuper(ev);
                    } else if (current + offsetX > maxHeaderDistance) {
                        moveHeaderPos(maxHeaderDistance - current);
                        return true;
                    }
                }
                float maxFooterDistance = mIndicator.getCanMoveTheMaxDistanceOfFooter();
                if (!movingRight && isMovingFooter() && !mIndicator.isInStartPosition()
                        && maxFooterDistance > 0) {
                    if (current >= maxFooterDistance) {
                        updateAnotherDirectionPos();
                        return dispatchTouchEventSuper(ev);
                    } else if (current - offsetX > maxFooterDistance) {
                        moveFooterPos(current - maxFooterDistance);
                        return true;
                    }
                }
                boolean canMoveLeft = isMovingHeader() && mIndicator.hasLeftStartPosition();
                boolean canMoveRight = isMovingFooter() && mIndicator.hasLeftStartPosition();
                boolean canHeaderMoveRight = canNotChildScrollLeft && !isDisabledRefresh();
                boolean canFooterMoveLeft = canNotChildScrollRight && !isDisabledLoadMore();
                if (!canMoveLeft && !canMoveRight) {
                    if ((movingRight && !canHeaderMoveRight) || (!movingRight && !canFooterMoveLeft)) {
                        if (isLoadingMore() && mIndicator.hasLeftStartPosition()) {
                            moveFooterPos(offsetX);
                            return true;
                        } else if (isRefreshing() && mIndicator.hasLeftStartPosition()) {
                            moveHeaderPos(offsetX);
                            return true;
                        } else if (isAutoRefresh() && !mAutoRefreshBeenSendTouchEvent) {
                            // When the Auto-Refresh is in progress, the content view can not
                            // continue to move up when the content view returns to the left
                            // 当自动刷新正在进行时，移动内容视图返回到最左侧后无法继续向左移动
                            makeNewTouchDownEvent(ev);
                            mAutoRefreshBeenSendTouchEvent = true;
                        }
                        return dispatchTouchEventSuper(ev);
                    }
                    // should show up header
                    if (movingRight) {
                        if (isDisabledRefresh())
                            return dispatchTouchEventSuper(ev);
                        moveHeaderPos(offsetX);
                        return true;
                    }
                    if (isDisabledLoadMore())
                        return dispatchTouchEventSuper(ev);
                    moveFooterPos(offsetX);
                    return true;
                }
                if (canMoveLeft) {
                    if (isDisabledRefresh())
                        return dispatchTouchEventSuper(ev);
                    if ((!canHeaderMoveRight && movingRight)) {
                        sendDownEvent();
                        return dispatchTouchEventSuper(ev);
                    }
                    moveHeaderPos(offsetX);
                    return true;
                }
                if (isDisabledLoadMore())
                    return dispatchTouchEventSuper(ev);
                if ((!canFooterMoveLeft && !movingRight)) {
                    sendDownEvent();
                    return dispatchTouchEventSuper(ev);
                }
                moveFooterPos(offsetX);
                return true;
        }
        return dispatchTouchEventSuper(ev);
    }

    /**
     * Set a callback to override {@link SmoothRefreshLayout#isChildNotYetInEdgeCannotMoveFooter()} method. Non-null
     * callback will return the value provided by the callback and ignore all internal logic.<br/>
     * <p>
     * 设置{@link SmoothRefreshLayout#isChildNotYetInEdgeCannotMoveFooter()}的重载回调，用来检测内容视图是否在最右侧
     *
     * @param callback Callback that should be called when isChildNotYetInEdgeCannotMoveFooter() is called.
     */
    @Override
    public void setOnChildNotYetInEdgeCannotMoveFooterCallBack(OnChildNotYetInEdgeCannotMoveFooterCallBack callback) {
        super.setOnChildNotYetInEdgeCannotMoveFooterCallBack(callback);
    }

    /**
     * Set a callback to override {@link SmoothRefreshLayout#isChildNotYetInEdgeCannotMoveHeader()} method. Non-null
     * callback will return the value provided by the callback and ignore all internal logic.<br/>
     * <p>
     * 设置{@link SmoothRefreshLayout#isChildNotYetInEdgeCannotMoveHeader()}的重载回调，用来检测内容视图是否在最左边
     *
     * @param callback Callback that should be called when isChildNotYetInEdgeCannotMoveHeader() is called.
     */
    @Override
    public void setOnChildNotYetInEdgeCannotMoveHeaderCallBack(OnChildNotYetInEdgeCannotMoveHeaderCallBack callback) {
        super.setOnChildNotYetInEdgeCannotMoveHeaderCallBack(callback);
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
    protected boolean offsetChild(int change, boolean isMovingHeader, boolean isMovingFooter) {
        boolean needRequestLayout = false;
        if (mHeaderView != null && !isDisabledRefresh() && isMovingHeader
                && !isEnabledHideHeaderView()) {
            final int type = mHeaderView.getStyle();
            switch (type) {
                case IRefreshView.STYLE_DEFAULT:
                    mHeaderView.getView().offsetLeftAndRight(change);
                    break;
                case IRefreshView.STYLE_SCALE:
                    needRequestLayout = true;
                    break;
                case IRefreshView.STYLE_PIN:
                    break;
                case IRefreshView.STYLE_FOLLOW_PIN:
                    if (mIndicator.getCurrentPos() <= mIndicator.getHeaderHeight())
                        mHeaderView.getView().offsetLeftAndRight(change);
                    break;
                case IRefreshView.STYLE_FOLLOW_SCALE:
                case IRefreshView.STYLE_FOLLOW_CENTER:
                    if (mIndicator.getCurrentPos() > mIndicator.getHeaderHeight())
                        needRequestLayout = true;
                    else
                        mHeaderView.getView().offsetLeftAndRight(change);
                    break;
            }
            if (isHeaderInProcessing())
                mHeaderView.onRefreshPositionChanged(this, mStatus, mIndicator);
            else
                mHeaderView.onPureScrollPositionChanged(this, mStatus, mIndicator);
        } else if (mFooterView != null && !isDisabledLoadMore() && isMovingFooter
                && !isEnabledHideFooterView()) {
            final int type = mFooterView.getStyle();
            switch (type) {
                case IRefreshView.STYLE_DEFAULT:
                    mFooterView.getView().offsetLeftAndRight(change);
                    break;
                case IRefreshView.STYLE_SCALE:
                    needRequestLayout = true;
                    break;
                case IRefreshView.STYLE_PIN:
                    break;
                case IRefreshView.STYLE_FOLLOW_PIN:
                    if (mIndicator.getCurrentPos() <= mIndicator.getFooterHeight())
                        mFooterView.getView().offsetLeftAndRight(change);
                    break;
                case IRefreshView.STYLE_FOLLOW_SCALE:
                case IRefreshView.STYLE_FOLLOW_CENTER:
                    if (mIndicator.getCurrentPos() > mIndicator.getFooterHeight())
                        needRequestLayout = true;
                    else
                        mFooterView.getView().offsetLeftAndRight(change);
                    break;
            }
            if (isFooterInProcessing())
                mFooterView.onRefreshPositionChanged(this, mStatus, mIndicator);
            else
                mFooterView.onPureScrollPositionChanged(this, mStatus, mIndicator);
        }
        if (!isEnabledPinContentView()) {
            if (mScrollTargetView != null && isMovingFooter && !isDisabledLoadMore()) {
                mScrollTargetView.offsetLeftAndRight(change);
            } else {
                mTargetView.offsetLeftAndRight(change);
            }
        }
        return needRequestLayout;
    }

    @Override
    public boolean isChildNotYetInEdgeCannotMoveHeader() {
        if (mInEdgeCanMoveHeaderCallBack != null)
            return mInEdgeCanMoveHeaderCallBack.isChildNotYetInEdgeCannotMoveHeader(this,
                    mTargetView, mHeaderView);
        return HorizontalScrollCompat.canChildScrollLeft(mTargetView);
    }

    @Override
    public boolean isChildNotYetInEdgeCannotMoveFooter() {
        if (mInEdgeCanMoveFooterCallBack != null)
            return mInEdgeCanMoveFooterCallBack.isChildNotYetInEdgeCannotMoveFooter(this,
                    mTargetView, mFooterView);
        return HorizontalScrollCompat.canChildScrollRight(mTargetView);
    }

    @Override
    public void onNestedPreScroll(@NonNull View target, int dx, int dy, @NonNull int[] consumed) {
        if (sDebug) {
            SRLog.d(TAG, "onNestedPreScroll(): dx: %s, dy: %s, consumed: %s",
                    dx, dy, Arrays.toString(consumed));
        }
        if (isNeedFilterTouchEvent()) {
            consumed[0] = dx;
            onNestedPreScroll(dx, dy, consumed);
            return;
        }
        if (!mIndicator.hasTouched()) {
            if (sDebug) {
                SRLog.w(TAG, "onNestedPreScroll(): There was an exception in touch event handling，" +
                        "This method should be performed after the onNestedScrollAccepted() " +
                        "method is called");
            }
            onNestedPreScroll(dx, dy, consumed);
            return;
        }
        if (dx > 0 && !isDisabledRefresh() && !isChildNotYetInEdgeCannotMoveHeader()
                && !(isEnabledPinRefreshViewWhileLoading() && (isRefreshing() || isLoadingMore())
                && mIndicator.isOverOffsetToKeepHeaderWhileLoading())) {
            if (!mIndicator.isInStartPosition() && isMovingHeader()) {
                mIndicator.onFingerMove(mIndicator.getLastMovePoint()[0] - dx,
                        mIndicator.getLastMovePoint()[1] - dy);
                moveHeaderPos(mIndicator.getOffset());
                consumed[0] = dx;
            } else {
                mIndicator.onFingerMove(mIndicator.getLastMovePoint()[0],
                        mIndicator.getLastMovePoint()[1] - dy);
            }
        }
        if (dx < 0 && !isDisabledLoadMore() && !isChildNotYetInEdgeCannotMoveFooter()
                && !(isEnabledPinRefreshViewWhileLoading() && (isRefreshing() || isLoadingMore())
                && mIndicator.isOverOffsetToKeepFooterWhileLoading())) {
            if (!mIndicator.isInStartPosition() && isMovingFooter()) {
                mIndicator.onFingerMove(mIndicator.getLastMovePoint()[0] - dx,
                        mIndicator.getLastMovePoint()[1] - dy);
                moveFooterPos(mIndicator.getOffset());
                consumed[0] = dx;
            } else {
                mIndicator.onFingerMove(mIndicator.getLastMovePoint()[0],
                        mIndicator.getLastMovePoint()[1] - dy);
            }
        }
        if (dx == 0) {
            mIndicator.onFingerMove(mIndicator.getLastMovePoint()[0],
                    mIndicator.getLastMovePoint()[1] - dy);
            updateAnotherDirectionPos();
        } else if (isMovingFooter() && isFooterInProcessing() && mStatus == SR_STATUS_COMPLETE
                && mIndicator.hasLeftStartPosition() && isChildNotYetInEdgeCannotMoveFooter()) {
            mScrollChecker.tryToScrollTo(IIndicator.START_POS, 0);
            consumed[0] = dx;
        }
        tryToResetMovingStatus();
        onNestedPreScroll(dx, dy, consumed);
    }

    @Override
    public void onNestedScroll(final View target, final int dxConsumed, final int dyConsumed,
                               final int dxUnconsumed, final int dyUnconsumed) {
        if (sDebug) {
            SRLog.d(TAG, "onNestedScroll(): dxConsumed: %s, dyConsumed: %s, dxUnconsumed: %s" +
                    " dyUnconsumed: %s", dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
        }
        if (isNeedFilterTouchEvent())
            return;
        // Dispatch up to the nested parent first
        dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, mParentOffsetInWindow);
        if (!mIndicator.hasTouched()) {
            if (sDebug) {
                SRLog.w(TAG, "onNestedScroll(): There was an exception in touch event handling，" +
                        "This method should be performed after the onNestedScrollAccepted() " +
                        "method is called");
            }
            return;
        }
        final int dx = dxUnconsumed + mParentOffsetInWindow[1];
        if (dx < 0 && !isDisabledRefresh() && !isChildNotYetInEdgeCannotMoveHeader()
                && !(isEnabledPinRefreshViewWhileLoading() && (isRefreshing() || isLoadingMore())
                && mIndicator.isOverOffsetToKeepHeaderWhileLoading())) {
            float distance = mIndicator.getCanMoveTheMaxDistanceOfHeader();
            if (distance > 0 && mIndicator.getCurrentPos() >= distance)
                return;
            mIndicator.onFingerMove(mIndicator.getLastMovePoint()[0] - dx,
                    mIndicator.getLastMovePoint()[1]);
            if (distance > 0 && (mIndicator.getCurrentPos() + mIndicator.getOffset() > distance))
                moveHeaderPos(distance - mIndicator.getCurrentPos());
            else
                moveHeaderPos(mIndicator.getOffset());
        } else if (dx > 0 && !isDisabledLoadMore() && !isChildNotYetInEdgeCannotMoveFooter()
                && !(isEnabledPinRefreshViewWhileLoading() && (isRefreshing() || isLoadingMore())
                && mIndicator.isOverOffsetToKeepFooterWhileLoading())) {
            float distance = mIndicator.getCanMoveTheMaxDistanceOfFooter();
            if (distance > 0 && mIndicator.getCurrentPos() > distance)
                return;
            mIndicator.onFingerMove(mIndicator.getLastMovePoint()[0] - dx,
                    mIndicator.getLastMovePoint()[1]);
            if (distance > 0 && (mIndicator.getCurrentPos() - mIndicator.getOffset() > distance))
                moveFooterPos(mIndicator.getCurrentPos() - distance);
            else
                moveFooterPos(mIndicator.getOffset());
        }
        tryToResetMovingStatus();
    }

    @Override
    public boolean onFling(float vx, float vy) {
        if ((isDisabledLoadMore() && isDisabledRefresh())
                || (!isAutoRefresh() && (isNeedInterceptTouchEvent() ||
                isCanNotAbortOverScrolling())))
            return mNestedScrollInProgress && dispatchNestedPreFling(-vx, -vy);
        if ((!isChildNotYetInEdgeCannotMoveHeader() && vx > 0) ||
                (!isChildNotYetInEdgeCannotMoveFooter() && vx < 0))
            return mNestedScrollInProgress && dispatchNestedPreFling(-vx, -vy);
        if (!mIndicator.isInStartPosition()) {
            if (!isEnabledPinRefreshViewWhileLoading()) {
                if (Math.abs(vx) > mMinimumFlingVelocity * 2) {
                    mDelayedNestedFling = true;
                    mOverScrollChecker.preFling(vx);
                }
                return true;
            }
        } else {
            if (isEnabledOverScroll()) {
                if (!isEnabledPinRefreshViewWhileLoading()
                        || ((vx >= 0 || !isDisabledLoadMore())
                        && (vx <= 0 || !isDisabledRefresh()))) {
                    mOverScrollChecker.fling(vx);
                }
            }
            mDelayedNestedFling = true;
            if (mDelayedScrollChecker == null)
                mDelayedScrollChecker = new DelayedScrollChecker();
            mDelayedScrollChecker.updateVelocity((int) vx);
        }
        return mNestedScrollInProgress && dispatchNestedPreFling(-vx, -vy);
    }

    protected boolean isFingerInsideAnotherDirectionView(final float x, final float y) {
        if (mFingerInsideAnotherDirectionViewCallback != null)
            return mFingerInsideAnotherDirectionViewCallback.isFingerInside(x, y, mTargetView);
        return HorizontalBoundaryUtil.isFingerInsideVerticalView(x, y, mTargetView);
    }

    @Override
    protected void compatLoadMoreScroll(float delta) {
        if (mLoadMoreScrollCallback == null) {
            if (mScrollTargetView != null) {
                if (HorizontalScrollCompat.canChildScrollRight(mScrollTargetView))
                    HorizontalScrollCompat.scrollCompat(mScrollTargetView, delta);
            } else {
                if (HorizontalScrollCompat.canChildScrollRight(mTargetView))
                    HorizontalScrollCompat.scrollCompat(mTargetView, delta);
            }
        } else {
            mLoadMoreScrollCallback.onScroll(mTargetView, delta);
        }
    }

    @Override
    protected void dispatchNestedFling(int v) {
        if (mScrollTargetView != null)
            HorizontalScrollCompat.flingCompat(mScrollTargetView, -v);
        else
            HorizontalScrollCompat.flingCompat(mTargetView, -v);
    }

    @Override
    protected float tryToFilterMovePos(float deltaX) {
        if (isMovingHeader() && !isDisabledRefresh() && mHeaderView != null) {
            int style = mHeaderView.getStyle();
            if (style != IRefreshView.STYLE_DEFAULT && style != IRefreshView.STYLE_FOLLOW_CENTER) {
                final int maxWidth = getWidth() - getPaddingLeft() - getPaddingRight();
                if (mIndicator.getCurrentPos() + Math.round(deltaX) > maxWidth) {
                    return maxWidth - mIndicator.getCurrentPos();
                }
            }
        } else if (isMovingFooter() && !isDisabledLoadMore() && mFooterView != null) {
            int style = mFooterView.getStyle();
            if (style != IRefreshView.STYLE_DEFAULT && style != IRefreshView.STYLE_FOLLOW_CENTER) {
                final int maxWidth = getWidth() - getPaddingLeft() - getPaddingRight();
                if (mIndicator.getCurrentPos() + Math.round(deltaX) > maxWidth) {
                    return maxWidth - mIndicator.getCurrentPos();
                }
            }
        }
        return deltaX;
    }
}
