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
package me.dkzwm.widget.srl.manager;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import me.dkzwm.widget.srl.SmoothRefreshLayout;
import me.dkzwm.widget.srl.annotation.Orientation;
import me.dkzwm.widget.srl.extra.IRefreshView;
import me.dkzwm.widget.srl.indicator.IIndicator;
import me.dkzwm.widget.srl.util.ViewCatcherUtil;

public class VRefreshLayoutManager extends SmoothRefreshLayout.LayoutManager {
    protected int mContentEnd;
    protected Paint mBackgroundPaint;
    protected int mHeaderBackgroundColor = Color.TRANSPARENT;
    protected int mFooterBackgroundColor = Color.TRANSPARENT;

    @Override
    public void setLayout(SmoothRefreshLayout layout) {
        super.setLayout(layout);
        if (layout != null) {
            layout.setWillNotDraw(mBackgroundPaint == null);
        }
    }

    @Override
    @Orientation
    public int getOrientation() {
        return SmoothRefreshLayout.LayoutManager.VERTICAL;
    }

    @Override
    public void measureHeader(
            @NonNull IRefreshView<IIndicator> header, int widthMeasureSpec, int heightMeasureSpec) {
        if (mLayout.isDisabledRefresh()) {
            return;
        }
        final View child = header.getView();
        final IIndicator indicator = mLayout.getIndicator();
        final SmoothRefreshLayout.LayoutParams lp =
                (SmoothRefreshLayout.LayoutParams) header.getView().getLayoutParams();
        int height = header.getCustomHeight();
        if (header.getStyle() == IRefreshView.STYLE_DEFAULT
                || header.getStyle() == IRefreshView.STYLE_PIN
                || header.getStyle() == IRefreshView.STYLE_FOLLOW_CENTER
                || header.getStyle() == IRefreshView.STYLE_FOLLOW_PIN) {
            if (height <= 0) {
                if (height == SmoothRefreshLayout.LayoutParams.MATCH_PARENT) {
                    lp.height = SmoothRefreshLayout.LayoutParams.MATCH_PARENT;
                }
            } else {
                lp.height = height;
            }
            measureChildWithMargins(child, widthMeasureSpec, heightMeasureSpec);
            setHeaderHeight(child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin);
        } else {
            if (height <= 0 && height != SmoothRefreshLayout.LayoutParams.MATCH_PARENT) {
                throw new IllegalArgumentException(
                        "If header view type is STYLE_SCALE or STYLE_FOLLOW_SCALE, you must set a accurate height");
            } else {
                if (height == SmoothRefreshLayout.LayoutParams.MATCH_PARENT) {
                    int specSize = View.MeasureSpec.getSize(heightMeasureSpec);
                    height =
                            Math.max(
                                    0,
                                    specSize
                                            - (mLayout.getPaddingTop()
                                                    + mLayout.getPaddingBottom()
                                                    + lp.topMargin
                                                    + lp.bottomMargin));
                    setHeaderHeight(height);
                } else {
                    setHeaderHeight(height + lp.topMargin + lp.bottomMargin);
                }
            }
            if (header.getStyle() == IRefreshView.STYLE_FOLLOW_SCALE) {
                if (indicator.getCurrentPos() <= indicator.getHeaderHeight()) {
                    lp.height = height;
                    measureChildWithMargins(child, widthMeasureSpec, heightMeasureSpec);
                    return;
                }
            }
            final int childWidthMeasureSpec =
                    ViewGroup.getChildMeasureSpec(
                            widthMeasureSpec,
                            mLayout.getPaddingLeft()
                                    + mLayout.getPaddingRight()
                                    + lp.leftMargin
                                    + lp.rightMargin,
                            lp.width);
            final int childHeightMeasureSpec;
            if (mLayout.isMovingHeader()) {
                final int maxHeight =
                        View.MeasureSpec.getSize(heightMeasureSpec)
                                - mLayout.getPaddingTop()
                                - mLayout.getPaddingBottom()
                                - lp.topMargin
                                - lp.bottomMargin;
                int realHeight =
                        Math.min(
                                indicator.getCurrentPos() - lp.topMargin - lp.bottomMargin,
                                maxHeight);
                childHeightMeasureSpec =
                        View.MeasureSpec.makeMeasureSpec(
                                Math.max(realHeight, 0), View.MeasureSpec.EXACTLY);
            } else {
                childHeightMeasureSpec =
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.EXACTLY);
            }
            child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
        }
    }

    @Override
    public void measureFooter(
            @NonNull IRefreshView<IIndicator> footer, int widthMeasureSpec, int heightMeasureSpec) {
        if (mLayout.isDisabledLoadMore()) {
            return;
        }
        final View child = footer.getView();
        final IIndicator indicator = mLayout.getIndicator();
        final SmoothRefreshLayout.LayoutParams lp =
                (SmoothRefreshLayout.LayoutParams) footer.getView().getLayoutParams();
        int height = footer.getCustomHeight();
        if (footer.getStyle() == IRefreshView.STYLE_DEFAULT
                || footer.getStyle() == IRefreshView.STYLE_PIN
                || footer.getStyle() == IRefreshView.STYLE_FOLLOW_CENTER
                || footer.getStyle() == IRefreshView.STYLE_FOLLOW_PIN) {
            if (height <= 0) {
                if (height == SmoothRefreshLayout.LayoutParams.MATCH_PARENT) {
                    lp.height = SmoothRefreshLayout.LayoutParams.MATCH_PARENT;
                }
            } else {
                lp.height = height;
            }
            measureChildWithMargins(child, widthMeasureSpec, heightMeasureSpec);
            setFooterHeight(child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin);
        } else {
            if (height <= 0 && height != SmoothRefreshLayout.LayoutParams.MATCH_PARENT) {
                throw new IllegalArgumentException(
                        "If footer view type is STYLE_SCALE or STYLE_FOLLOW_SCALE, you must set a accurate height");
            } else {
                if (height == SmoothRefreshLayout.LayoutParams.MATCH_PARENT) {
                    int specSize = View.MeasureSpec.getSize(heightMeasureSpec);
                    height =
                            Math.max(
                                    0,
                                    specSize
                                            - (mLayout.getPaddingTop()
                                                    + mLayout.getPaddingBottom()
                                                    + lp.topMargin
                                                    + lp.bottomMargin));
                    setFooterHeight(height);
                } else {
                    setFooterHeight(height + lp.topMargin + lp.bottomMargin);
                }
            }
            if (footer.getStyle() == IRefreshView.STYLE_FOLLOW_SCALE) {
                if (indicator.getCurrentPos() <= indicator.getFooterHeight()) {
                    lp.height = height;
                    measureChildWithMargins(child, widthMeasureSpec, heightMeasureSpec);
                    return;
                }
            }
            final int childWidthMeasureSpec =
                    ViewGroup.getChildMeasureSpec(
                            widthMeasureSpec,
                            mLayout.getPaddingLeft()
                                    + mLayout.getPaddingRight()
                                    + lp.leftMargin
                                    + lp.rightMargin,
                            lp.width);
            final int childHeightMeasureSpec;
            if (mLayout.isMovingFooter()) {
                final int maxHeight =
                        View.MeasureSpec.getSize(heightMeasureSpec)
                                - mLayout.getPaddingTop()
                                - mLayout.getPaddingBottom()
                                - lp.topMargin
                                - lp.bottomMargin;
                int realHeight =
                        Math.min(
                                indicator.getCurrentPos() - lp.topMargin - lp.bottomMargin,
                                maxHeight);
                childHeightMeasureSpec =
                        View.MeasureSpec.makeMeasureSpec(
                                Math.max(realHeight, 0), View.MeasureSpec.EXACTLY);
            } else {
                childHeightMeasureSpec =
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.EXACTLY);
            }
            child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
        }
    }

    @Override
    public void layoutHeaderView(@NonNull IRefreshView<IIndicator> header) {
        final View child = header.getView();
        if (mLayout.isDisabledRefresh() || child.getMeasuredHeight() == 0) {
            child.layout(0, 0, 0, 0);
            if (SmoothRefreshLayout.sDebug) {
                Log.d(TAG, String.format("onLayout(): header: %s %s %s %s", 0, 0, 0, 0));
            }
            return;
        }
        final SmoothRefreshLayout.LayoutParams lp =
                (SmoothRefreshLayout.LayoutParams) child.getLayoutParams();
        final IIndicator indicator = mLayout.getIndicator();
        int left, right, top = 0, bottom;
        switch (header.getStyle()) {
            case IRefreshView.STYLE_DEFAULT:
                if (mLayout.isMovingHeader()) {
                    child.setTranslationY(indicator.getCurrentPos());
                } else {
                    child.setTranslationY(0);
                }
                top = mLayout.getPaddingTop() - child.getMeasuredHeight() - lp.bottomMargin;
                break;
            case IRefreshView.STYLE_SCALE:
            case IRefreshView.STYLE_PIN:
                child.setTranslationY(0);
                top = mLayout.getPaddingTop() + lp.topMargin;
                break;
            case IRefreshView.STYLE_FOLLOW_SCALE:
                if (mLayout.isMovingHeader()) {
                    if (indicator.getCurrentPos() <= indicator.getHeaderHeight()) {
                        top = mLayout.getPaddingTop() - child.getMeasuredHeight() - lp.bottomMargin;
                        child.setTranslationY(indicator.getCurrentPos());
                    } else {
                        top = mLayout.getPaddingTop() + lp.topMargin;
                        child.setTranslationY(0);
                    }
                } else {
                    top = mLayout.getPaddingTop() - child.getMeasuredHeight() - lp.bottomMargin;
                    child.setTranslationY(0);
                }
                break;
            case IRefreshView.STYLE_FOLLOW_PIN:
                if (mLayout.isMovingHeader()) {
                    child.setTranslationY(
                            Math.min(indicator.getCurrentPos(), indicator.getHeaderHeight()));
                } else {
                    child.setTranslationY(0);
                }
                top = mLayout.getPaddingTop() - child.getMeasuredHeight() - lp.bottomMargin;
                break;
            case IRefreshView.STYLE_FOLLOW_CENTER:
                if (mLayout.isMovingHeader()) {
                    if (indicator.getCurrentPos() <= indicator.getHeaderHeight()) {
                        top = mLayout.getPaddingTop() - child.getMeasuredHeight() - lp.bottomMargin;
                        child.setTranslationY(indicator.getCurrentPos());
                    } else {
                        top =
                                (int)
                                        (mLayout.getPaddingTop()
                                                + lp.topMargin
                                                + (indicator.getCurrentPos()
                                                                - indicator.getHeaderHeight())
                                                        / 2f);
                        child.setTranslationY(0);
                    }
                } else {
                    top = mLayout.getPaddingTop() - child.getMeasuredHeight() - lp.bottomMargin;
                    child.setTranslationY(0);
                }
                break;
        }
        left = mLayout.getPaddingLeft() + lp.leftMargin;
        right = left + child.getMeasuredWidth();
        if (mLayout.isInEditMode()) top = top + child.getMeasuredHeight();
        bottom = top + child.getMeasuredHeight();
        child.layout(left, top, right, bottom);
        if (SmoothRefreshLayout.sDebug) {
            Log.d(TAG, String.format("onLayout(): header: %s %s %s %s", left, top, right, bottom));
        }
    }

    @Override
    public void layoutFooterView(@NonNull IRefreshView<IIndicator> footer) {
        final View child = footer.getView();
        if (mLayout.isDisabledLoadMore() || child.getMeasuredHeight() == 0) {
            child.layout(0, 0, 0, 0);
            if (SmoothRefreshLayout.sDebug) {
                Log.d(TAG, String.format("onLayout(): footer: %s %s %s %s", 0, 0, 0, 0));
            }
            return;
        }
        final SmoothRefreshLayout.LayoutParams lp =
                (SmoothRefreshLayout.LayoutParams) child.getLayoutParams();
        final IIndicator indicator = mLayout.getIndicator();
        int left, right, top = 0, bottom;
        int translationY = 0;
        switch (footer.getStyle()) {
            case IRefreshView.STYLE_DEFAULT:
                if (mLayout.isMovingFooter()) {
                    translationY = -indicator.getCurrentPos();
                }
                top = lp.topMargin + mContentEnd;
                break;
            case IRefreshView.STYLE_SCALE:
                top =
                        lp.topMargin
                                + mContentEnd
                                - (mLayout.isMovingFooter() ? indicator.getCurrentPos() : 0);
                break;
            case IRefreshView.STYLE_PIN:
                top = mContentEnd - lp.bottomMargin - child.getMeasuredHeight();
                break;
            case IRefreshView.STYLE_FOLLOW_PIN:
                top = lp.topMargin + mContentEnd;
                if (mLayout.isMovingFooter()) {
                    translationY =
                            -Math.min(indicator.getCurrentPos(), indicator.getFooterHeight());
                }
                break;
            case IRefreshView.STYLE_FOLLOW_SCALE:
                if (mLayout.isMovingFooter()) {
                    if (indicator.getCurrentPos() <= indicator.getFooterHeight()) {
                        top = lp.topMargin + mContentEnd;
                        translationY = -indicator.getCurrentPos();
                    } else {
                        top = lp.topMargin + mContentEnd - child.getMeasuredHeight();
                    }
                } else {
                    top = lp.topMargin + mContentEnd;
                }
                break;
            case IRefreshView.STYLE_FOLLOW_CENTER:
                {
                    if (mLayout.isMovingFooter()) {
                        if (indicator.getCurrentPos() <= indicator.getFooterHeight()) {
                            top = lp.topMargin + mContentEnd;
                            translationY = -indicator.getCurrentPos();
                        } else {
                            top =
                                    (int)
                                            (lp.topMargin
                                                    + mContentEnd
                                                    - indicator.getCurrentPos()
                                                    + (indicator.getCurrentPos()
                                                                    - indicator.getFooterHeight())
                                                            / 2f);
                        }
                    } else {
                        top = lp.topMargin + mContentEnd;
                    }
                    break;
                }
        }
        if (mLayout.isMovingHeader()
                && top < mLayout.getMeasuredHeight()
                && footer.getStyle() != IRefreshView.STYLE_SCALE) {
            translationY = indicator.getCurrentPos();
        }
        child.setTranslationY(translationY);
        left = mLayout.getPaddingLeft() + lp.leftMargin;
        right = left + child.getMeasuredWidth();
        if (mLayout.isInEditMode()) top = top - child.getMeasuredHeight();
        bottom = top + child.getMeasuredHeight();
        child.layout(left, top, right, bottom);
        if (SmoothRefreshLayout.sDebug) {
            Log.d(TAG, String.format("onLayout(): footer: %s %s %s %s", left, top, right, bottom));
        }
    }

    @Override
    public void layoutContentView(@NonNull View content) {
        final SmoothRefreshLayout.LayoutParams lp =
                (SmoothRefreshLayout.LayoutParams) content.getLayoutParams();
        final int left = mLayout.getPaddingLeft() + lp.leftMargin;
        final int right = left + content.getMeasuredWidth();
        final int top = mLayout.getPaddingTop() + lp.topMargin;
        final int bottom = top + content.getMeasuredHeight();
        content.layout(left, top, right, bottom);
        if (SmoothRefreshLayout.sDebug) {
            Log.d(TAG, String.format("onLayout(): content: %s %s %s %s", left, top, right, bottom));
        }
        mContentEnd = bottom + lp.bottomMargin;
    }

    @Override
    public void layoutStickyHeaderView(@NonNull View stickyHeader) {
        final SmoothRefreshLayout.LayoutParams lp =
                (SmoothRefreshLayout.LayoutParams) stickyHeader.getLayoutParams();
        final int left = mLayout.getPaddingLeft() + lp.leftMargin;
        final int right = left + stickyHeader.getMeasuredWidth();
        final int top = mLayout.getPaddingTop() + lp.topMargin;
        final int bottom = top + stickyHeader.getMeasuredHeight();
        stickyHeader.layout(left, top, right, bottom);
        if (SmoothRefreshLayout.sDebug) {
            Log.d(
                    TAG,
                    String.format(
                            "onLayout(): stickyHeader: %s %s %s %s", left, top, right, bottom));
        }
    }

    @Override
    public void layoutStickyFooterView(@NonNull View stickyFooter) {
        final SmoothRefreshLayout.LayoutParams lp =
                (SmoothRefreshLayout.LayoutParams) stickyFooter.getLayoutParams();
        final int left = mLayout.getPaddingLeft() + lp.leftMargin;
        final int right = left + stickyFooter.getMeasuredWidth();
        final int bottom = mContentEnd - lp.bottomMargin;
        final int top = bottom - stickyFooter.getMeasuredHeight();
        stickyFooter.layout(left, top, right, bottom);
        if (SmoothRefreshLayout.sDebug) {
            Log.d(
                    TAG,
                    String.format(
                            "onLayout(): stickyFooter: %s %s %s %s", left, top, right, bottom));
        }
    }

    @Override
    public boolean offsetChild(
            @Nullable IRefreshView<IIndicator> header,
            @Nullable IRefreshView<IIndicator> footer,
            @Nullable View stickyHeader,
            @Nullable View stickyFooter,
            @Nullable View content,
            int change) {
        boolean needRequestLayout = false;
        final IIndicator indicator = mLayout.getIndicator();
        if (mLayout.isMovingHeader()
                && !mLayout.isDisabledRefresh()
                && header != null
                && header.getView().getVisibility() == View.VISIBLE) {
            switch (header.getStyle()) {
                case IRefreshView.STYLE_DEFAULT:
                    header.getView().setTranslationY(indicator.getCurrentPos());
                    break;
                case IRefreshView.STYLE_PIN:
                    header.getView().setTranslationY(0);
                    break;
                case IRefreshView.STYLE_FOLLOW_PIN:
                    header.getView()
                            .setTranslationY(
                                    Math.min(
                                            indicator.getCurrentPos(),
                                            indicator.getHeaderHeight()));
                    break;
                case IRefreshView.STYLE_SCALE:
                case IRefreshView.STYLE_FOLLOW_SCALE:
                case IRefreshView.STYLE_FOLLOW_CENTER:
                    if (ViewCompat.isInLayout(mLayout)) {
                        break;
                    }
                    if (mMeasureMatchParentChildren) {
                        needRequestLayout = !ViewCompat.isInLayout(mLayout);
                    } else {
                        measureHeader(header, mOldWidthMeasureSpec, mOldHeightMeasureSpec);
                        layoutHeaderView(header);
                    }
                    break;
            }
            if (mLayout.isHeaderInProcessing()) {
                header.onRefreshPositionChanged(mLayout, getRefreshStatus(), indicator);
            } else {
                header.onPureScrollPositionChanged(mLayout, getRefreshStatus(), indicator);
            }
        } else if (mLayout.isMovingFooter()
                && footer != null
                && !mLayout.isDisabledLoadMore()
                && footer.getView().getVisibility() == View.VISIBLE) {
            switch (footer.getStyle()) {
                case IRefreshView.STYLE_DEFAULT:
                    footer.getView().setTranslationY(-indicator.getCurrentPos());
                    break;
                case IRefreshView.STYLE_PIN:
                    footer.getView().setTranslationY(0);
                    break;
                case IRefreshView.STYLE_FOLLOW_PIN:
                    footer.getView()
                            .setTranslationY(
                                    -Math.min(
                                            indicator.getCurrentPos(),
                                            indicator.getFooterHeight()));
                    break;
                case IRefreshView.STYLE_SCALE:
                case IRefreshView.STYLE_FOLLOW_SCALE:
                case IRefreshView.STYLE_FOLLOW_CENTER:
                    if (ViewCompat.isInLayout(mLayout)) {
                        break;
                    }
                    if (mMeasureMatchParentChildren) {
                        needRequestLayout = !ViewCompat.isInLayout(mLayout);
                    } else {
                        measureFooter(footer, mOldWidthMeasureSpec, mOldHeightMeasureSpec);
                        layoutFooterView(footer);
                    }
                    break;
            }
            if (mLayout.isFooterInProcessing()) {
                footer.onRefreshPositionChanged(mLayout, getRefreshStatus(), indicator);
            } else {
                footer.onPureScrollPositionChanged(mLayout, getRefreshStatus(), indicator);
            }
        }
        if (!mLayout.isEnabledPinContentView()) {
            if (mLayout.isMovingHeader() && stickyHeader != null) {
                stickyHeader.setTranslationY(indicator.getCurrentPos());
            } else if (mLayout.isMovingFooter() && stickyFooter != null) {
                stickyFooter.setTranslationY(-indicator.getCurrentPos());
            }
            if (content != null) {
                if (mLayout.isMovingFooter()) {
                    View targetView = mLayout.getScrollTargetView();
                    if (targetView != null && targetView != content) {
                        ViewParent parent = targetView.getParent();
                        if (parent instanceof View) {
                            View parentView = (View) parent;
                            if (ViewCatcherUtil.isViewPager(parentView)) {
                                targetView = parentView;
                            }
                        }
                    }
                    if (targetView != null) {
                        targetView.setTranslationY(-indicator.getCurrentPos());
                    }
                } else if (mLayout.isMovingHeader()) {
                    content.setTranslationY(indicator.getCurrentPos());
                    if (footer != null
                            && footer.getView().getVisibility() == View.VISIBLE
                            && !mLayout.isDisabledLoadMore()
                            && footer.getStyle() != IRefreshView.STYLE_SCALE
                            && footer.getView().getTop() < mLayout.getMeasuredHeight()) {
                        footer.getView().setTranslationY(indicator.getCurrentPos());
                    }
                }
            }
        }
        if (mBackgroundPaint != null) {
            mLayout.invalidate();
        }
        return needRequestLayout;
    }

    @Override
    public void resetLayout(
            @Nullable IRefreshView<IIndicator> header,
            @Nullable IRefreshView<IIndicator> footer,
            @Nullable View stickyHeader,
            @Nullable View stickyFooter,
            @Nullable View content) {
        View targetView = mLayout.getScrollTargetView();
        if (targetView != null && targetView != content) {
            ViewParent parent = targetView.getParent();
            if (parent instanceof View) {
                View parentView = (View) parent;
                if (ViewCatcherUtil.isViewPager(parentView)) {
                    targetView = parentView;
                }
            }
        }
        if (targetView != null) {
            targetView.setTranslationY(0);
        }
        if (header != null) {
            layoutHeaderView(header);
        }
        if (footer != null) {
            layoutFooterView(footer);
        }
        if (stickyHeader != null) {
            layoutStickyHeaderView(stickyHeader);
        }
        if (stickyFooter != null) {
            layoutStickyFooterView(stickyFooter);
        }
    }

    public void setHeaderBackgroundColor(@ColorInt int color) {
        mHeaderBackgroundColor = color;
        preparePaint();
    }

    public void setFooterBackgroundColor(@ColorInt int color) {
        mFooterBackgroundColor = color;
        preparePaint();
    }

    private void preparePaint() {
        boolean hasColor =
                (mHeaderBackgroundColor != Color.TRANSPARENT
                        || mFooterBackgroundColor != Color.TRANSPARENT);
        if (mBackgroundPaint == null && hasColor) {
            mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mBackgroundPaint.setStyle(Paint.Style.FILL);
            if (mLayout != null) {
                mLayout.setWillNotDraw(false);
            }
        } else if (!hasColor) {
            if (mBackgroundPaint != null) mBackgroundPaint.reset();
            mBackgroundPaint = null;
            if (mLayout != null) {
                mLayout.setWillNotDraw(true);
            }
        }
    }

    @Override
    public void onLayoutDraw(Canvas canvas) {
        if (mBackgroundPaint != null
                && !mLayout.isEnabledPinContentView()
                && !mLayout.getIndicator().isAlreadyHere(IIndicator.START_POS)) {
            if (!mLayout.isDisabledRefresh()
                    && mLayout.isMovingHeader()
                    && mHeaderBackgroundColor != Color.TRANSPARENT) {
                mBackgroundPaint.setColor(mHeaderBackgroundColor);
                drawHeaderBackground(canvas);
            } else if (!mLayout.isDisabledLoadMore()
                    && mLayout.isMovingFooter()
                    && mFooterBackgroundColor != Color.TRANSPARENT) {
                mBackgroundPaint.setColor(mFooterBackgroundColor);
                drawFooterBackground(canvas);
            }
        }
    }

    protected void drawHeaderBackground(Canvas canvas) {
        final int bottom =
                Math.min(
                        mLayout.getPaddingTop() + mLayout.getIndicator().getCurrentPos(),
                        mLayout.getHeight() - mLayout.getPaddingTop());
        canvas.drawRect(
                mLayout.getPaddingLeft(),
                mLayout.getPaddingTop(),
                mLayout.getWidth() - mLayout.getPaddingRight(),
                bottom,
                mBackgroundPaint);
    }

    protected void drawFooterBackground(Canvas canvas) {
        final int top;
        final int bottom = mContentEnd;
        top = bottom - mLayout.getIndicator().getCurrentPos();
        canvas.drawRect(
                mLayout.getPaddingLeft(),
                top,
                mLayout.getWidth() - mLayout.getPaddingRight(),
                bottom,
                mBackgroundPaint);
    }
}
