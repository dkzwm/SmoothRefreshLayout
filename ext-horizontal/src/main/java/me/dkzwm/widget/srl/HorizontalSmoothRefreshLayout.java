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
import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import me.dkzwm.widget.srl.config.Constants;
import me.dkzwm.widget.srl.extra.IRefreshView;
import me.dkzwm.widget.srl.indicator.DefaultIndicator;
import me.dkzwm.widget.srl.indicator.HorizontalDefaultIndicator;
import me.dkzwm.widget.srl.util.HorizontalBoundaryUtil;
import me.dkzwm.widget.srl.util.HorizontalScrollCompat;

/**
 * Created by dkzwm on 2017/10/20.
 *
 * <p>Support Horizontal refresh feature;<br>
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
    public final int getSupportScrollAxis() {
        return ViewCompat.SCROLL_AXIS_HORIZONTAL;
    }

    @Override
    protected void createIndicator() {
        DefaultIndicator indicator = new HorizontalDefaultIndicator();
        mIndicator = indicator;
        mIndicatorSetter = indicator;
    }

    @Override
    protected void measureHeader(
            View child, LayoutParams lp, int widthMeasureSpec, int heightMeasureSpec) {
        if (isDisabledRefresh()) return;
        int size = mHeaderView.getCustomHeight();
        if (mHeaderView.getStyle() == IRefreshView.STYLE_DEFAULT
                || mHeaderView.getStyle() == IRefreshView.STYLE_PIN
                || mHeaderView.getStyle() == IRefreshView.STYLE_FOLLOW_CENTER
                || mHeaderView.getStyle() == IRefreshView.STYLE_FOLLOW_PIN) {
            if (size <= 0) {
                if (size == LayoutParams.MATCH_PARENT) lp.height = LayoutParams.MATCH_PARENT;
            } else lp.width = size;
            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
            mIndicatorSetter.setHeaderHeight(
                    child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin);
        } else {
            if (size <= 0 && size != LayoutParams.MATCH_PARENT) {
                throw new IllegalArgumentException(
                        "If header view type is "
                                + "STYLE_SCALE or STYLE_FOLLOW_SCALE, you must set a accurate height");
            } else {
                if (size == LayoutParams.MATCH_PARENT) {
                    int specSize = MeasureSpec.getSize(heightMeasureSpec);
                    size =
                            Math.max(
                                    0,
                                    specSize
                                            - (getPaddingLeft()
                                                    + getPaddingRight()
                                                    + lp.leftMargin
                                                    + lp.rightMargin));
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
            final int childHeightMeasureSpec =
                    getChildMeasureSpec(
                            heightMeasureSpec,
                            getPaddingTop() + getPaddingBottom() + lp.topMargin + lp.bottomMargin,
                            lp.height);
            final int childWidthMeasureSpec;
            if (isMovingHeader()) {
                final int maxWidth =
                        MeasureSpec.getSize(widthMeasureSpec)
                                - getPaddingLeft()
                                - getPaddingRight()
                                - lp.leftMargin
                                - lp.rightMargin;
                int realWidth =
                        Math.min(
                                mIndicator.getCurrentPos() - lp.leftMargin - lp.rightMargin,
                                maxWidth);
                childWidthMeasureSpec =
                        MeasureSpec.makeMeasureSpec(
                                realWidth > 0 ? realWidth : 0, MeasureSpec.EXACTLY);
            } else {
                childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.EXACTLY);
            }
            child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
        }
    }

    @Override
    protected void measureFooter(
            View child, LayoutParams lp, int widthMeasureSpec, int heightMeasureSpec) {
        if (isDisabledLoadMore()) return;
        int size = mFooterView.getCustomHeight();
        if (mFooterView.getStyle() == IRefreshView.STYLE_DEFAULT
                || mFooterView.getStyle() == IRefreshView.STYLE_PIN
                || mFooterView.getStyle() == IRefreshView.STYLE_FOLLOW_CENTER
                || mFooterView.getStyle() == IRefreshView.STYLE_FOLLOW_PIN) {
            if (size <= 0) {
                if (size == LayoutParams.MATCH_PARENT) lp.width = LayoutParams.MATCH_PARENT;
            } else lp.width = size;
            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
            mIndicatorSetter.setFooterHeight(
                    child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin);
        } else {
            if (size <= 0 && size != LayoutParams.MATCH_PARENT) {
                throw new IllegalArgumentException(
                        "If footer view type is "
                                + "STYLE_SCALE or STYLE_FOLLOW_SCALE, you must set a accurate height");
            } else {
                if (size == LayoutParams.MATCH_PARENT) {
                    int specSize = MeasureSpec.getSize(heightMeasureSpec);
                    size =
                            Math.max(
                                    0,
                                    specSize
                                            - (getPaddingLeft()
                                                    + getPaddingRight()
                                                    + lp.leftMargin
                                                    + lp.rightMargin));
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
            final int childHeightMeasureSpec =
                    getChildMeasureSpec(
                            heightMeasureSpec,
                            getPaddingTop() + getPaddingBottom() + lp.topMargin + lp.bottomMargin,
                            lp.height);
            final int childWidthMeasureSpec;
            if (isMovingFooter()) {
                final int maxWidth =
                        MeasureSpec.getSize(widthMeasureSpec)
                                - getPaddingLeft()
                                - getPaddingRight()
                                - lp.leftMargin
                                - lp.rightMargin;
                int realWidth =
                        Math.min(
                                mIndicator.getCurrentPos() - lp.leftMargin - lp.rightMargin,
                                maxWidth);
                childWidthMeasureSpec =
                        MeasureSpec.makeMeasureSpec(
                                realWidth > 0 ? realWidth : 0, MeasureSpec.EXACTLY);
            } else {
                childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.EXACTLY);
            }
            child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
        }
    }

    @Override
    protected void layoutHeaderView(View child) {
        if (mMode != Constants.MODE_DEFAULT
                || isDisabledRefresh()
                || child.getMeasuredWidth() == 0) {
            child.layout(0, 0, 0, 0);
            if (sDebug) Log.d(TAG, String.format("onLayout(): header: %s %s %s %s", 0, 0, 0, 0));
            return;
        }
        final LayoutParams lp = (LayoutParams) child.getLayoutParams();
        @IRefreshView.RefreshViewStyle final int type = mHeaderView.getStyle();
        int left = 0, right, top, bottom;
        switch (type) {
            case IRefreshView.STYLE_DEFAULT:
                if (isMovingHeader()) {
                    child.setTranslationX(mIndicator.getCurrentPos());
                } else {
                    child.setTranslationX(0);
                }
                left = getPaddingLeft() - child.getMeasuredWidth() - lp.rightMargin;
                break;
            case IRefreshView.STYLE_SCALE:
            case IRefreshView.STYLE_PIN:
                child.setTranslationX(0);
                left = getPaddingLeft() + lp.leftMargin;
                break;
            case IRefreshView.STYLE_FOLLOW_SCALE:
                child.setTranslationX(0);
                if (isMovingHeader()) {
                    if (mIndicator.getCurrentPos() <= mIndicator.getHeaderHeight()) {
                        left =
                                getPaddingLeft()
                                        - child.getMeasuredWidth()
                                        + mIndicator.getCurrentPos()
                                        - lp.rightMargin;
                    } else {
                        left = getPaddingLeft() + lp.leftMargin;
                    }
                } else {
                    left = getPaddingLeft() - child.getMeasuredWidth() - lp.rightMargin;
                }
                break;
            case IRefreshView.STYLE_FOLLOW_PIN:
                if (isMovingHeader()) {
                    if (mIndicator.getCurrentPos() <= mIndicator.getHeaderHeight()) {
                        child.setTranslationX(mIndicator.getCurrentPos());
                    } else {
                        child.setTranslationX(mIndicator.getHeaderHeight());
                    }
                } else {
                    child.setTranslationX(0);
                }
                left = getPaddingLeft() - child.getMeasuredWidth() - lp.rightMargin;
                break;
            case IRefreshView.STYLE_FOLLOW_CENTER:
                child.setTranslationX(0);
                if (isMovingHeader()) {
                    if (mIndicator.getCurrentPos() <= mIndicator.getHeaderHeight()) {
                        left =
                                getPaddingLeft()
                                        + mIndicator.getCurrentPos()
                                        - child.getMeasuredWidth()
                                        - lp.rightMargin;
                    } else {
                        left =
                                (int)
                                        (getPaddingLeft()
                                                + lp.leftMargin
                                                + (mIndicator.getCurrentPos()
                                                                - mIndicator.getHeaderHeight())
                                                        / 2f);
                    }
                } else {
                    left = getPaddingLeft() - child.getMeasuredWidth() - lp.rightMargin;
                }
                break;
        }
        if (isInEditMode()) left = left + child.getMeasuredWidth();
        top = getPaddingTop() + lp.topMargin;
        right = left + child.getMeasuredWidth();
        bottom = top + child.getMeasuredHeight();
        child.layout(left, top, right, bottom);
        if (sDebug)
            Log.d(TAG, String.format("onLayout(): header: %s %s %s %s", left, top, right, bottom));
    }

    @Override
    protected int layoutContentView(View child) {
        final LayoutParams lp = (LayoutParams) child.getLayoutParams();
        final int left = getPaddingLeft() + lp.leftMargin;
        final int right = left + child.getMeasuredWidth();
        final int top = getPaddingTop() + lp.topMargin;
        final int bottom = top + child.getMeasuredHeight();
        child.layout(left, top, right, bottom);
        if (sDebug)
            Log.d(TAG, String.format("onLayout(): content: %s %s %s %s", left, top, right, bottom));
        return right + lp.rightMargin;
    }

    @Override
    protected void layoutFooterView(View child, int contentRight) {
        if (mMode != Constants.MODE_DEFAULT
                || isDisabledLoadMore()
                || child.getMeasuredWidth() == 0) {
            child.layout(0, 0, 0, 0);
            if (sDebug) Log.d(TAG, String.format("onLayout(): footer: %s %s %s %s", 0, 0, 0, 0));
            return;
        }
        final LayoutParams lp = (LayoutParams) child.getLayoutParams();
        @IRefreshView.RefreshViewStyle final int type = mFooterView.getStyle();
        int left = 0, right, top, bottom;
        switch (type) {
            case IRefreshView.STYLE_DEFAULT:
                if (isMovingFooter()) {
                    child.setTranslationX(-mIndicator.getCurrentPos());
                } else {
                    child.setTranslationX(0);
                }
                left = lp.leftMargin + contentRight;
                break;
            case IRefreshView.STYLE_SCALE:
                child.setTranslationX(0);
                left =
                        lp.leftMargin
                                + contentRight
                                - (isMovingFooter() ? mIndicator.getCurrentPos() : 0);
                break;
            case IRefreshView.STYLE_PIN:
                child.setTranslationX(0);
                left = contentRight - lp.rightMargin - child.getMeasuredWidth();
                break;
            case IRefreshView.STYLE_FOLLOW_PIN:
                if (isMovingFooter()) {
                    if (mIndicator.getCurrentPos() <= mIndicator.getFooterHeight()) {
                        child.setTranslationX(-mIndicator.getCurrentPos());
                    } else {
                        child.setTranslationX(-mIndicator.getFooterHeight());
                    }
                } else {
                    child.setTranslationX(0);
                }
                left = lp.leftMargin + contentRight;
                break;
            case IRefreshView.STYLE_FOLLOW_SCALE:
                child.setTranslationX(0);
                if (isMovingFooter()) {
                    if (mIndicator.getCurrentPos() <= mIndicator.getFooterHeight()) {
                        left = lp.leftMargin + contentRight - mIndicator.getCurrentPos();
                    } else {
                        left = lp.leftMargin + contentRight - child.getMeasuredWidth();
                    }
                } else {
                    left = lp.leftMargin + contentRight;
                }
                break;
            case IRefreshView.STYLE_FOLLOW_CENTER:
                {
                    child.setTranslationX(0);
                    if (isMovingFooter()) {
                        if (mIndicator.getCurrentPos() <= mIndicator.getFooterHeight()) {
                            left = lp.leftMargin + contentRight - mIndicator.getCurrentPos();
                        } else {
                            left =
                                    (int)
                                            (lp.leftMargin
                                                    + contentRight
                                                    - mIndicator.getCurrentPos()
                                                    + (mIndicator.getCurrentPos()
                                                                    - mIndicator.getFooterHeight())
                                                            / 2f);
                        }
                    } else {
                        left = lp.leftMargin + contentRight;
                    }
                    break;
                }
        }
        if (isInEditMode()) left = left - child.getMeasuredWidth();
        right = left + child.getMeasuredWidth();
        top = getPaddingTop() + lp.topMargin;
        bottom = top + child.getMeasuredHeight();
        child.layout(left, top, right, bottom);
        if (sDebug)
            Log.d(TAG, String.format("onLayout(): footer: %s %s %s %s", left, top, right, bottom));
    }

    @Override
    protected void layoutStickyFooterView(@NonNull View child, int contentRight) {
        final LayoutParams lp = (LayoutParams) child.getLayoutParams();
        final int top = getPaddingTop() + lp.topMargin;
        final int bottom = top + child.getMeasuredHeight();
        final int right = contentRight - lp.rightMargin;
        final int left = right - child.getMeasuredWidth();
        child.layout(left, top, right, bottom);
        if (sDebug)
            Log.d(
                    TAG,
                    String.format(
                            "onLayout(): stickyFooter: %s %s %s %s", left, top, right, bottom));
    }

    @Override
    protected void drawHeaderBackground(Canvas canvas) {
        final int right =
                Math.min(
                        getPaddingLeft() + mIndicator.getCurrentPos(),
                        getWidth() - getPaddingLeft());
        canvas.drawRect(
                getPaddingLeft(),
                getPaddingTop(),
                right,
                getHeight() - getPaddingBottom(),
                mBackgroundPaint);
    }

    @Override
    protected void drawFooterBackground(Canvas canvas) {
        final int left, right;
        if (mTargetView != null) {
            final LayoutParams lp = (LayoutParams) mTargetView.getLayoutParams();
            right =
                    getPaddingLeft()
                            + lp.leftMargin
                            + mTargetView.getMeasuredWidth()
                            + lp.rightMargin;
            left = right - mIndicator.getCurrentPos();
        } else {
            left =
                    Math.max(
                            getWidth() - getPaddingRight() - mIndicator.getCurrentPos(),
                            getPaddingLeft());
            right = getWidth() - getPaddingRight();
        }
        canvas.drawRect(
                left, getPaddingTop(), right, getHeight() - getPaddingBottom(), mBackgroundPaint);
    }

    @Override
    protected void tryToDealAnotherDirectionMove(float offsetX, float offsetY) {
        if (isDisabledWhenAnotherDirectionMove() && mIsFingerInsideAnotherDirectionView) {
            if ((Math.abs(offsetY) >= mTouchSlop && Math.abs(offsetY) > Math.abs(offsetX))) {
                mPreventForAnotherDirection = true;
                mDealAnotherDirectionMove = true;
            } else if (Math.abs(offsetX) < mTouchSlop && Math.abs(offsetY) < mTouchSlop) {
                mDealAnotherDirectionMove = false;
                mPreventForAnotherDirection = true;
            } else {
                mDealAnotherDirectionMove = true;
                mPreventForAnotherDirection = false;
            }
        } else {
            mPreventForAnotherDirection =
                    Math.abs(offsetX) < mTouchSlop && Math.abs(offsetY) < mTouchSlop;
            if (!mPreventForAnotherDirection) mDealAnotherDirectionMove = true;
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
            view.setScaleX(1);
        } else {
            targetView.setPivotX(0);
            targetView.setScaleX(1);
        }
    }

    @Override
    protected boolean offsetChild(int change, boolean isMovingHeader, boolean isMovingFooter) {
        boolean needRequestLayout = false;
        if (mMode == Constants.MODE_DEFAULT) {
            if (mHeaderView != null
                    && !isDisabledRefresh()
                    && isMovingHeader
                    && mHeaderView.getView().getVisibility() == VISIBLE) {
                final int type = mHeaderView.getStyle();
                switch (type) {
                    case IRefreshView.STYLE_DEFAULT:
                        mHeaderView.getView().setTranslationX(mIndicator.getCurrentPos());
                        break;
                    case IRefreshView.STYLE_SCALE:
                        if (MeasureSpec.getMode(mCachedWidthMeasureSpec) != MeasureSpec.EXACTLY
                                || MeasureSpec.getMode(mCachedHeightMeasureSpec)
                                        != MeasureSpec.EXACTLY) {
                            needRequestLayout = !ViewCompat.isInLayout(this);
                        } else {
                            final View child = mHeaderView.getView();
                            final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                            measureHeader(
                                    child, lp, mCachedWidthMeasureSpec, mCachedHeightMeasureSpec);
                            layoutHeaderView(child);
                        }
                        break;
                    case IRefreshView.STYLE_PIN:
                        mHeaderView.getView().setTranslationX(0);
                        break;
                    case IRefreshView.STYLE_FOLLOW_PIN:
                        if (mIndicator.getCurrentPos() <= mIndicator.getHeaderHeight())
                            mHeaderView.getView().setTranslationX(mIndicator.getCurrentPos());
                        else mHeaderView.getView().setTranslationX(mIndicator.getHeaderHeight());
                        break;
                    case IRefreshView.STYLE_FOLLOW_SCALE:
                    case IRefreshView.STYLE_FOLLOW_CENTER:
                        if (ViewCompat.isInLayout(this)) break;
                        if (mIndicator.getCurrentPos() > mIndicator.getHeaderHeight()) {
                            if (MeasureSpec.getMode(mCachedWidthMeasureSpec) != MeasureSpec.EXACTLY
                                    || MeasureSpec.getMode(mCachedHeightMeasureSpec)
                                            != MeasureSpec.EXACTLY) {
                                needRequestLayout = !ViewCompat.isInLayout(this);
                            } else {
                                final View child = mHeaderView.getView();
                                final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                                measureHeader(
                                        child,
                                        lp,
                                        mCachedWidthMeasureSpec,
                                        mCachedHeightMeasureSpec);
                                layoutHeaderView(child);
                            }
                        } else {
                            ViewCompat.offsetLeftAndRight(mHeaderView.getView(), change);
                        }
                        break;
                }
                if (isHeaderInProcessing())
                    mHeaderView.onRefreshPositionChanged(this, mStatus, mIndicator);
                else mHeaderView.onPureScrollPositionChanged(this, mStatus, mIndicator);
            } else if (mFooterView != null
                    && !isDisabledLoadMore()
                    && isMovingFooter
                    && mFooterView.getView().getVisibility() == VISIBLE) {
                final int type = mFooterView.getStyle();
                switch (type) {
                    case IRefreshView.STYLE_DEFAULT:
                        mFooterView.getView().setTranslationX(-mIndicator.getCurrentPos());
                        break;
                    case IRefreshView.STYLE_SCALE:
                        if (MeasureSpec.getMode(mCachedWidthMeasureSpec) != MeasureSpec.EXACTLY
                                || MeasureSpec.getMode(mCachedHeightMeasureSpec)
                                        != MeasureSpec.EXACTLY) {
                            needRequestLayout = !ViewCompat.isInLayout(this);
                        } else {
                            final View child = mFooterView.getView();
                            final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                            measureFooter(
                                    child, lp, mCachedWidthMeasureSpec, mCachedHeightMeasureSpec);
                            final int right;
                            if (mTargetView != null) {
                                final LayoutParams lpTarget =
                                        (LayoutParams) mTargetView.getLayoutParams();
                                right = mTargetView.getRight() + lpTarget.rightMargin;
                            } else {
                                right = 0;
                            }
                            layoutFooterView(child, right);
                        }
                        break;
                    case IRefreshView.STYLE_PIN:
                        mFooterView.getView().setTranslationX(0);
                        break;
                    case IRefreshView.STYLE_FOLLOW_PIN:
                        if (mIndicator.getCurrentPos() <= mIndicator.getFooterHeight())
                            mFooterView.getView().setTranslationX(-mIndicator.getCurrentPos());
                        else mFooterView.getView().setTranslationX(-mIndicator.getFooterHeight());
                        break;
                    case IRefreshView.STYLE_FOLLOW_SCALE:
                    case IRefreshView.STYLE_FOLLOW_CENTER:
                        if (ViewCompat.isInLayout(this)) break;
                        if (mIndicator.getCurrentPos() > mIndicator.getFooterHeight()) {
                            if (MeasureSpec.getMode(mCachedWidthMeasureSpec) != MeasureSpec.EXACTLY
                                    || MeasureSpec.getMode(mCachedHeightMeasureSpec)
                                            != MeasureSpec.EXACTLY) {
                                needRequestLayout = !ViewCompat.isInLayout(this);
                            } else {
                                final View child = mFooterView.getView();
                                final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                                measureFooter(
                                        child,
                                        lp,
                                        mCachedWidthMeasureSpec,
                                        mCachedHeightMeasureSpec);
                                final int right;
                                if (mTargetView != null) {
                                    final LayoutParams lpTarget =
                                            (LayoutParams) mTargetView.getLayoutParams();
                                    right = mTargetView.getRight() + lpTarget.rightMargin;
                                } else {
                                    right = 0;
                                }
                                layoutFooterView(child, right);
                            }
                        } else {
                            ViewCompat.offsetLeftAndRight(mFooterView.getView(), change);
                        }
                        break;
                }
                if (isFooterInProcessing())
                    mFooterView.onRefreshPositionChanged(this, mStatus, mIndicator);
                else mFooterView.onPureScrollPositionChanged(this, mStatus, mIndicator);
            }
            if (!isEnabledPinContentView()) {
                if (isMovingHeader && mStickyHeaderView != null)
                    mStickyHeaderView.setTranslationX(mIndicator.getCurrentPos());
                if (isMovingFooter && mStickyFooterView != null)
                    mStickyFooterView.setTranslationX(-mIndicator.getCurrentPos());
                if (mScrollTargetView != null && isMovingFooter)
                    mScrollTargetView.setTranslationX(-mIndicator.getCurrentPos());
                else if (mAutoFoundScrollTargetView != null && isMovingFooter)
                    mAutoFoundScrollTargetView.setTranslationX(-mIndicator.getCurrentPos());
                else if (mTargetView != null) {
                    if (isMovingHeader) mTargetView.setTranslationX(mIndicator.getCurrentPos());
                    else if (isMovingFooter)
                        mTargetView.setTranslationX(-mIndicator.getCurrentPos());
                }
            }
        } else if (mTargetView != null) {
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
                if (mScrollTargetView != null) targetView = mScrollTargetView;
                else if (mAutoFoundScrollTargetView != null)
                    targetView = mAutoFoundScrollTargetView;
                else targetView = mTargetView;
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
        return needRequestLayout;
    }

    @Override
    public boolean isNotYetInEdgeCannotMoveHeader(View view) {
        if (mInEdgeCanMoveHeaderCallBack != null)
            return mInEdgeCanMoveHeaderCallBack.isNotYetInEdgeCannotMoveHeader(
                    this, view, mHeaderView);
        return HorizontalScrollCompat.canChildScrollLeft(view);
    }

    @Override
    public boolean isNotYetInEdgeCannotMoveFooter(View view) {
        if (mInEdgeCanMoveFooterCallBack != null)
            return mInEdgeCanMoveFooterCallBack.isNotYetInEdgeCannotMoveFooter(
                    this, view, mFooterView);
        return HorizontalScrollCompat.canChildScrollRight(view);
    }

    protected boolean isInsideAnotherDirectionView(final float x, final float y) {
        if (mInsideAnotherDirectionViewCallback != null)
            return mInsideAnotherDirectionViewCallback.isInside(x, y, mTargetView);
        return HorizontalBoundaryUtil.isInsideVerticalView(x, y, mTargetView);
    }

    @Override
    protected void compatLoadMoreScroll(View view, float delta) {
        if (mLoadMoreScrollCallback == null) HorizontalScrollCompat.scrollCompat(view, delta);
        else mLoadMoreScrollCallback.onScroll(view, delta);
    }

    @Override
    protected void dispatchNestedFling(int velocity) {
        if (sDebug) Log.d(TAG, String.format("dispatchNestedFling() : %s", velocity));
        if (mScrollTargetView != null)
            HorizontalScrollCompat.flingCompat(mScrollTargetView, -velocity);
        else if (mAutoFoundScrollTargetView != null)
            HorizontalScrollCompat.flingCompat(mAutoFoundScrollTargetView, -velocity);
        else if (mTargetView != null) HorizontalScrollCompat.flingCompat(mTargetView, -velocity);
    }

    @Override
    protected boolean canAutoLoadMore(View view) {
        if (mAutoLoadMoreCallBack != null) mAutoLoadMoreCallBack.canAutoLoadMore(this, view);
        return HorizontalScrollCompat.canAutoLoadMore(view);
    }

    @Override
    protected boolean canAutoRefresh(View view) {
        if (mAutoRefreshCallBack != null) mAutoRefreshCallBack.canAutoRefresh(this, view);
        return HorizontalScrollCompat.canAutoRefresh(view);
    }

    @Override
    protected boolean isScrollingView(View target) {
        return HorizontalScrollCompat.isScrollingView(target);
    }
}
