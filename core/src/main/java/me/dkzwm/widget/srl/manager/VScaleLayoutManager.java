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

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import me.dkzwm.widget.srl.SmoothRefreshLayout;
import me.dkzwm.widget.srl.annotation.Orientation;
import me.dkzwm.widget.srl.config.Constants;
import me.dkzwm.widget.srl.extra.IRefreshView;
import me.dkzwm.widget.srl.indicator.IIndicator;
import me.dkzwm.widget.srl.util.ScrollCompat;
import me.dkzwm.widget.srl.util.ViewCatcherUtil;

public class VScaleLayoutManager extends SmoothRefreshLayout.LayoutManager {
    protected float mMaxScaleFactor = 1.2f;

    @Override
    @Orientation
    public int getOrientation() {
        return SmoothRefreshLayout.LayoutManager.VERTICAL;
    }

    @Override
    public void setLayout(SmoothRefreshLayout layout) {
        super.setLayout(layout);
        setHeaderHeight(100000);
        setFooterHeight(100000);
    }

    @Override
    public boolean isNeedFilterOverTop(float delta) {
        return mLayout.getScrollMode() != Constants.SCROLLER_MODE_SPRING_BACK
                && mLayout.getScrollMode() != Constants.SCROLLER_MODE_FLING_BACK;
    }

    @Override
    public void measureHeader(
            @NonNull IRefreshView<IIndicator> header, int widthMeasureSpec, int heightMeasureSpec) {
        int childMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.EXACTLY);
        header.getView().measure(childMeasureSpec, childMeasureSpec);
    }

    @Override
    public void measureFooter(
            @NonNull IRefreshView<IIndicator> footer, int widthMeasureSpec, int heightMeasureSpec) {
        int childMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.EXACTLY);
        footer.getView().measure(childMeasureSpec, childMeasureSpec);
    }

    @Override
    public void layoutHeaderView(@NonNull IRefreshView<IIndicator> header) {
        header.getView().layout(0, 0, 0, 0);
        if (SmoothRefreshLayout.sDebug) {
            Log.d(TAG, String.format("onLayout(): header: %s %s %s %s", 0, 0, 0, 0));
        }
    }

    @Override
    public void layoutFooterView(@NonNull IRefreshView<IIndicator> footer) {
        footer.getView().layout(0, 0, 0, 0);
        if (SmoothRefreshLayout.sDebug) {
            Log.d(TAG, String.format("onLayout(): footer: %s %s %s %s", 0, 0, 0, 0));
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
    }

    @Override
    public void layoutStickyHeaderView(@NonNull View stickyHeader) {
        stickyHeader.layout(0, 0, 0, 0);
        if (SmoothRefreshLayout.sDebug) {
            Log.d(TAG, String.format("onLayout(): stickyHeader: %s %s %s %s", 0, 0, 0, 0));
        }
    }

    @Override
    public void layoutStickyFooterView(@NonNull View stickyFooter) {
        stickyFooter.layout(0, 0, 0, 0);
        if (SmoothRefreshLayout.sDebug) {
            Log.d(TAG, String.format("onLayout(): stickyFooter: %s %s %s %s", 0, 0, 0, 0));
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
        float calculatedScale = calculateScale();
        if (calculatedScale <= mMaxScaleFactor && content != null) {
            if (mLayout.isMovingHeader()) {
                if (ScrollCompat.canScaleInternal(content)) {
                    View view = ((ViewGroup) content).getChildAt(0);
                    view.setPivotY(0);
                    view.setScaleY(calculatedScale);
                } else {
                    content.setPivotY(0);
                    content.setScaleY(calculatedScale);
                }
            } else if (mLayout.isMovingFooter()) {
                View targetView = mLayout.getScrollTargetView();
                if (targetView != null) {
                    if (targetView != content) {
                        ViewParent parent = targetView.getParent();
                        if (parent instanceof View) {
                            View parentView = (View) parent;
                            if (ViewCatcherUtil.isViewPager(parentView)) {
                                targetView = parentView;
                            }
                        }
                    }
                    if (ScrollCompat.canScaleInternal(targetView)) {
                        View view = ((ViewGroup) targetView).getChildAt(0);
                        view.setPivotY(view.getHeight());
                        view.setScaleY(calculatedScale);
                    } else {
                        targetView.setPivotY(mLayout.getHeight());
                        targetView.setScaleY(calculatedScale);
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void resetLayout(
            @Nullable IRefreshView<IIndicator> header,
            @Nullable IRefreshView<IIndicator> footer,
            @Nullable View stickyHeader,
            @Nullable View stickyFooter,
            @Nullable View content) {
        View targetView = mLayout.getScrollTargetView();
        if (targetView != null) {
            if (targetView != content) {
                ViewParent parent = targetView.getParent();
                if (parent instanceof View) {
                    View parentView = (View) parent;
                    if (ViewCatcherUtil.isViewPager(parentView)) {
                        targetView = parentView;
                    }
                }
            }
            if (ScrollCompat.canScaleInternal(targetView)) {
                View view = ((ViewGroup) targetView).getChildAt(0);
                view.setPivotY(0);
                view.setScaleY(1);
            } else {
                targetView.setPivotY(0);
                targetView.setScaleY(1);
            }
        }
    }

    protected float calculateScale() {
        final int pos = mLayout.getIndicator().getCurrentPos();
        if (pos >= 0) {
            return 1 + (float) Math.min(.2f, Math.pow(pos, .72f) / 1000f);
        } else {
            return 1 - (float) Math.min(.2f, Math.pow(-pos, .72f) / 1000f);
        }
    }
}
