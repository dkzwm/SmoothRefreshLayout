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
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import me.dkzwm.widget.srl.SmoothRefreshLayout;
import me.dkzwm.widget.srl.annotation.Orientation;
import me.dkzwm.widget.srl.extra.IRefreshView;
import me.dkzwm.widget.srl.indicator.IIndicator;

public class HRefreshLayoutManager extends VRefreshLayoutManager {
    @Override
    @Orientation
    public int getOrientation() {
        return SmoothRefreshLayout.LayoutManager.HORIZONTAL;
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
        int size = header.getCustomHeight();
        if (header.getStyle() == IRefreshView.STYLE_DEFAULT
                || header.getStyle() == IRefreshView.STYLE_PIN
                || header.getStyle() == IRefreshView.STYLE_FOLLOW_CENTER
                || header.getStyle() == IRefreshView.STYLE_FOLLOW_PIN) {
            if (size <= 0) {
                if (size == SmoothRefreshLayout.LayoutParams.MATCH_PARENT) {
                    lp.height = SmoothRefreshLayout.LayoutParams.MATCH_PARENT;
                }
            } else {
                lp.width = size;
            }
            measureChildWithMargins(child, widthMeasureSpec, heightMeasureSpec);
            setHeaderHeight(child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin);
        } else {
            if (size <= 0 && size != SmoothRefreshLayout.LayoutParams.MATCH_PARENT) {
                throw new IllegalArgumentException(
                        "If header view type is "
                                + "STYLE_SCALE or STYLE_FOLLOW_SCALE, you must set a accurate height");
            } else {
                if (size == SmoothRefreshLayout.LayoutParams.MATCH_PARENT) {
                    int specSize = View.MeasureSpec.getSize(heightMeasureSpec);
                    size =
                            Math.max(
                                    0,
                                    specSize
                                            - (mLayout.getPaddingLeft()
                                                    + mLayout.getPaddingRight()
                                                    + lp.leftMargin
                                                    + lp.rightMargin));
                    setHeaderHeight(size);
                } else {
                    setHeaderHeight(size + lp.leftMargin + lp.rightMargin);
                }
            }
            if (header.getStyle() == IRefreshView.STYLE_FOLLOW_SCALE) {
                if (indicator.getCurrentPos() <= indicator.getHeaderHeight()) {
                    lp.width = size;
                    measureChildWithMargins(child, widthMeasureSpec, heightMeasureSpec);
                    return;
                }
            }
            final int childHeightMeasureSpec =
                    ViewGroup.getChildMeasureSpec(
                            heightMeasureSpec,
                            mLayout.getPaddingTop()
                                    + mLayout.getPaddingBottom()
                                    + lp.topMargin
                                    + lp.bottomMargin,
                            lp.height);
            final int childWidthMeasureSpec;
            if (mLayout.isMovingHeader()) {
                final int maxWidth =
                        View.MeasureSpec.getSize(widthMeasureSpec)
                                - mLayout.getPaddingLeft()
                                - mLayout.getPaddingRight()
                                - lp.leftMargin
                                - lp.rightMargin;
                int realWidth =
                        Math.min(
                                indicator.getCurrentPos() - lp.leftMargin - lp.rightMargin,
                                maxWidth);
                childWidthMeasureSpec =
                        View.MeasureSpec.makeMeasureSpec(
                                Math.max(realWidth, 0), View.MeasureSpec.EXACTLY);
            } else {
                childWidthMeasureSpec =
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
        int size = footer.getCustomHeight();
        if (footer.getStyle() == IRefreshView.STYLE_DEFAULT
                || footer.getStyle() == IRefreshView.STYLE_PIN
                || footer.getStyle() == IRefreshView.STYLE_FOLLOW_CENTER
                || footer.getStyle() == IRefreshView.STYLE_FOLLOW_PIN) {
            if (size <= 0) {
                if (size == SmoothRefreshLayout.LayoutParams.MATCH_PARENT) {
                    lp.width = SmoothRefreshLayout.LayoutParams.MATCH_PARENT;
                }
            } else {
                lp.width = size;
            }
            measureChildWithMargins(child, widthMeasureSpec, heightMeasureSpec);
            setFooterHeight(child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin);
        } else {
            if (size <= 0 && size != SmoothRefreshLayout.LayoutParams.MATCH_PARENT) {
                throw new IllegalArgumentException(
                        "If footer view type is "
                                + "STYLE_SCALE or STYLE_FOLLOW_SCALE, you must set a accurate height");
            } else {
                if (size == SmoothRefreshLayout.LayoutParams.MATCH_PARENT) {
                    int specSize = View.MeasureSpec.getSize(heightMeasureSpec);
                    size =
                            Math.max(
                                    0,
                                    specSize
                                            - (mLayout.getPaddingLeft()
                                                    + mLayout.getPaddingRight()
                                                    + lp.leftMargin
                                                    + lp.rightMargin));
                    setFooterHeight(size);
                } else {
                    setFooterHeight(size + lp.leftMargin + lp.rightMargin);
                }
            }
            if (footer.getStyle() == IRefreshView.STYLE_FOLLOW_SCALE) {
                if (indicator.getCurrentPos() <= indicator.getFooterHeight()) {
                    lp.width = size;
                    measureChildWithMargins(child, widthMeasureSpec, heightMeasureSpec);
                    return;
                }
            }
            final int childHeightMeasureSpec =
                    ViewGroup.getChildMeasureSpec(
                            heightMeasureSpec,
                            mLayout.getPaddingTop()
                                    + mLayout.getPaddingBottom()
                                    + lp.topMargin
                                    + lp.bottomMargin,
                            lp.height);
            final int childWidthMeasureSpec;
            if (mLayout.isMovingFooter()) {
                final int maxWidth =
                        View.MeasureSpec.getSize(widthMeasureSpec)
                                - mLayout.getPaddingLeft()
                                - mLayout.getPaddingRight()
                                - lp.leftMargin
                                - lp.rightMargin;
                int realWidth =
                        Math.min(
                                indicator.getCurrentPos() - lp.leftMargin - lp.rightMargin,
                                maxWidth);
                childWidthMeasureSpec =
                        View.MeasureSpec.makeMeasureSpec(
                                Math.max(realWidth, 0), View.MeasureSpec.EXACTLY);
            } else {
                childWidthMeasureSpec =
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.EXACTLY);
            }
            child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
        }
    }

    @Override
    public void layoutHeaderView(@NonNull IRefreshView<IIndicator> header) {
        final View child = header.getView();
        if (mLayout.isDisabledRefresh() || child.getMeasuredWidth() == 0) {
            child.layout(0, 0, 0, 0);
            if (SmoothRefreshLayout.sDebug) {
                Log.d(TAG, String.format("onLayout(): header: %s %s %s %s", 0, 0, 0, 0));
            }
            return;
        }
        final SmoothRefreshLayout.LayoutParams lp =
                (SmoothRefreshLayout.LayoutParams) child.getLayoutParams();
        final IIndicator indicator = mLayout.getIndicator();
        int left = 0, right, top, bottom;
        switch (header.getStyle()) {
            case IRefreshView.STYLE_DEFAULT:
                if (mLayout.isMovingHeader()) {
                    child.setTranslationX(indicator.getCurrentPos());
                } else {
                    child.setTranslationX(0);
                }
                left = mLayout.getPaddingLeft() - child.getMeasuredWidth() - lp.rightMargin;
                break;
            case IRefreshView.STYLE_SCALE:
            case IRefreshView.STYLE_PIN:
                child.setTranslationX(0);
                left = mLayout.getPaddingLeft() + lp.leftMargin;
                break;
            case IRefreshView.STYLE_FOLLOW_SCALE:
                if (mLayout.isMovingHeader()) {
                    if (indicator.getCurrentPos() <= indicator.getHeaderHeight()) {
                        left = mLayout.getPaddingLeft() - child.getMeasuredWidth() - lp.rightMargin;
                        child.setTranslationX(indicator.getCurrentPos());
                    } else {
                        left = mLayout.getPaddingLeft() + lp.leftMargin;
                        child.setTranslationX(0);
                    }
                } else {
                    left = mLayout.getPaddingLeft() - child.getMeasuredWidth() - lp.rightMargin;
                    child.setTranslationX(0);
                }
                break;
            case IRefreshView.STYLE_FOLLOW_PIN:
                if (mLayout.isMovingHeader()) {
                    child.setTranslationX(
                            Math.min(indicator.getCurrentPos(), indicator.getHeaderHeight()));
                } else {
                    child.setTranslationX(0);
                }
                left = mLayout.getPaddingLeft() - child.getMeasuredWidth() - lp.rightMargin;
                break;
            case IRefreshView.STYLE_FOLLOW_CENTER:
                if (mLayout.isMovingHeader()) {
                    if (indicator.getCurrentPos() <= indicator.getHeaderHeight()) {
                        left = mLayout.getPaddingLeft() - child.getMeasuredWidth() - lp.rightMargin;
                        child.setTranslationX(indicator.getCurrentPos());
                    } else {
                        left =
                                (int)
                                        (mLayout.getPaddingLeft()
                                                + lp.leftMargin
                                                + (indicator.getCurrentPos()
                                                                - indicator.getHeaderHeight())
                                                        / 2f);
                        child.setTranslationX(0);
                    }
                } else {
                    left = mLayout.getPaddingLeft() - child.getMeasuredWidth() - lp.rightMargin;
                    child.setTranslationX(0);
                }
                break;
        }
        if (mLayout.isInEditMode()) {
            left = left + child.getMeasuredWidth();
        }
        top = mLayout.getPaddingTop() + lp.topMargin;
        right = left + child.getMeasuredWidth();
        bottom = top + child.getMeasuredHeight();
        child.layout(left, top, right, bottom);
        if (SmoothRefreshLayout.sDebug) {
            Log.d(TAG, String.format("onLayout(): header: %s %s %s %s", left, top, right, bottom));
        }
    }

    @Override
    public void layoutFooterView(@NonNull IRefreshView<IIndicator> footer) {
        final View child = footer.getView();
        if (mLayout.isDisabledLoadMore() || child.getMeasuredWidth() == 0) {
            child.layout(0, 0, 0, 0);
            if (SmoothRefreshLayout.sDebug) {
                Log.d(TAG, String.format("onLayout(): footer: %s %s %s %s", 0, 0, 0, 0));
            }
            return;
        }
        final SmoothRefreshLayout.LayoutParams lp =
                (SmoothRefreshLayout.LayoutParams) child.getLayoutParams();
        final IIndicator indicator = mLayout.getIndicator();
        int left = 0, right, top, bottom;
        int translationX = 0;
        switch (footer.getStyle()) {
            case IRefreshView.STYLE_DEFAULT:
                if (mLayout.isMovingFooter()) {
                    translationX = -indicator.getCurrentPos();
                }
                left = lp.leftMargin + mContentEnd;
                break;
            case IRefreshView.STYLE_SCALE:
                left =
                        lp.leftMargin
                                + mContentEnd
                                - (mLayout.isMovingFooter() ? indicator.getCurrentPos() : 0);
                break;
            case IRefreshView.STYLE_PIN:
                left = mContentEnd - lp.rightMargin - child.getMeasuredWidth();
                break;
            case IRefreshView.STYLE_FOLLOW_PIN:
                if (mLayout.isMovingFooter()) {
                    translationX =
                            -Math.min(indicator.getCurrentPos(), indicator.getFooterHeight());
                }
                left = lp.leftMargin + mContentEnd;
                break;
            case IRefreshView.STYLE_FOLLOW_SCALE:
                if (mLayout.isMovingFooter()) {
                    if (indicator.getCurrentPos() <= indicator.getFooterHeight()) {
                        left = lp.leftMargin + mContentEnd;
                        translationX = -indicator.getCurrentPos();
                    } else {
                        left = lp.leftMargin + mContentEnd - child.getMeasuredWidth();
                    }
                } else {
                    left = lp.leftMargin + mContentEnd;
                }
                break;
            case IRefreshView.STYLE_FOLLOW_CENTER:
                {
                    if (mLayout.isMovingFooter()) {
                        if (indicator.getCurrentPos() <= indicator.getFooterHeight()) {
                            left = lp.leftMargin + mContentEnd;
                            translationX = -indicator.getCurrentPos();
                        } else {
                            left =
                                    (int)
                                            (lp.leftMargin
                                                    + mContentEnd
                                                    - indicator.getCurrentPos()
                                                    + (indicator.getCurrentPos()
                                                                    - indicator.getFooterHeight())
                                                            / 2f);
                        }
                    } else {
                        left = lp.leftMargin + mContentEnd;
                    }
                    break;
                }
        }
        if (mLayout.isMovingHeader()
                && left < mLayout.getMeasuredWidth()
                && footer.getStyle() != IRefreshView.STYLE_SCALE) {
            translationX = indicator.getCurrentPos();
        }
        child.setTranslationX(translationX);
        if (mLayout.isInEditMode()) {
            left = left - child.getMeasuredWidth();
        }
        right = left + child.getMeasuredWidth();
        top = mLayout.getPaddingTop() + lp.topMargin;
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
        mContentEnd = right + lp.rightMargin;
    }

    @Override
    public void layoutStickyFooterView(@NonNull View stickyFooter) {
        final SmoothRefreshLayout.LayoutParams lp =
                (SmoothRefreshLayout.LayoutParams) stickyFooter.getLayoutParams();
        final int top = mLayout.getPaddingTop() + lp.topMargin;
        final int bottom = top + stickyFooter.getMeasuredHeight();
        final int right = mContentEnd - lp.rightMargin;
        final int left = right - stickyFooter.getMeasuredWidth();
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
            IRefreshView<IIndicator> header,
            IRefreshView<IIndicator> footer,
            View stickyHeader,
            View stickyFooter,
            View content,
            int change) {
        boolean needRequestLayout = false;
        final IIndicator indicator = mLayout.getIndicator();
        if (header != null
                && !mLayout.isDisabledRefresh()
                && mLayout.isMovingHeader()
                && header.getView().getVisibility() == View.VISIBLE) {
            switch (header.getStyle()) {
                case IRefreshView.STYLE_DEFAULT:
                    header.getView().setTranslationX(indicator.getCurrentPos());
                    break;
                case IRefreshView.STYLE_PIN:
                    header.getView().setTranslationX(0);
                    break;
                case IRefreshView.STYLE_FOLLOW_PIN:
                    header.getView()
                            .setTranslationX(
                                    Math.min(
                                            indicator.getCurrentPos(),
                                            indicator.getHeaderHeight()));
                    break;
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
        } else if (footer != null
                && !mLayout.isDisabledLoadMore()
                && mLayout.isMovingFooter()
                && footer.getView().getVisibility() == View.VISIBLE) {
            switch (footer.getStyle()) {
                case IRefreshView.STYLE_DEFAULT:
                    footer.getView().setTranslationX(-indicator.getCurrentPos());
                    break;
                case IRefreshView.STYLE_PIN:
                    footer.getView().setTranslationX(0);
                    break;
                case IRefreshView.STYLE_FOLLOW_PIN:
                    footer.getView()
                            .setTranslationX(
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
                stickyHeader.setTranslationX(indicator.getCurrentPos());
            } else if (mLayout.isMovingFooter() && stickyFooter != null) {
                stickyFooter.setTranslationX(-indicator.getCurrentPos());
            }
            if (content != null) {
                if (mLayout.isMovingHeader()) {
                    content.setTranslationX(indicator.getCurrentPos());
                    if (footer != null && footer.getView().getLeft() < mLayout.getMeasuredWidth()) {
                        footer.getView().setTranslationY(indicator.getCurrentPos());
                    }
                } else if (mLayout.isMovingFooter()) {
                    View targetView = mLayout.getScrollTargetView();
                    if (targetView != null) {
                        targetView.setTranslationX(-indicator.getCurrentPos());
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
        if (content != null) {
            View targetView = mLayout.getScrollTargetView();
            if (targetView != null) {
                targetView.setTranslationX(0);
            }
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

    @Override
    protected void drawHeaderBackground(Canvas canvas) {
        final int right =
                Math.min(
                        mLayout.getPaddingLeft() + mLayout.getIndicator().getCurrentPos(),
                        mLayout.getWidth() - mLayout.getPaddingLeft());
        canvas.drawRect(
                mLayout.getPaddingLeft(),
                mLayout.getPaddingTop(),
                right,
                mLayout.getHeight() - mLayout.getPaddingBottom(),
                mBackgroundPaint);
    }

    @Override
    protected void drawFooterBackground(Canvas canvas) {
        final int left;
        final int right = mContentEnd;
        left = right - mLayout.getIndicator().getCurrentPos();
        canvas.drawRect(
                left,
                mLayout.getPaddingTop(),
                right,
                mLayout.getHeight() - mLayout.getPaddingBottom(),
                mBackgroundPaint);
    }
}
