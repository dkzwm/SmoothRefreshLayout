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

import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.Nullable;
import me.dkzwm.widget.srl.SmoothRefreshLayout;
import me.dkzwm.widget.srl.annotation.Orientation;
import me.dkzwm.widget.srl.extra.IRefreshView;
import me.dkzwm.widget.srl.indicator.IIndicator;
import me.dkzwm.widget.srl.util.HorizontalScrollCompat;
import me.dkzwm.widget.srl.util.ScrollCompat;

public class HScaleLayoutManager extends VScaleLayoutManager {
    @Override
    @Orientation
    public int getOrientation() {
        return SmoothRefreshLayout.LayoutManager.HORIZONTAL;
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
                if (HorizontalScrollCompat.canScaleInternal(content)) {
                    View view = ((ViewGroup) content).getChildAt(0);
                    view.setPivotX(0);
                    view.setScaleX(calculatedScale);
                } else {
                    content.setPivotX(0);
                    content.setScaleX(calculatedScale);
                }
            } else if (mLayout.isMovingFooter()) {
                final View targetView = mLayout.getScrollTargetView();
                if (targetView != null) {
                    if (HorizontalScrollCompat.canScaleInternal(targetView)) {
                        View view = ((ViewGroup) targetView).getChildAt(0);
                        view.setPivotX(view.getWidth());
                        view.setScaleX(calculatedScale);
                    } else {
                        targetView.setPivotX(mLayout.getWidth());
                        targetView.setScaleX(calculatedScale);
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
            if (ScrollCompat.canScaleInternal(targetView)) {
                View view = ((ViewGroup) targetView).getChildAt(0);
                view.setPivotX(0);
                view.setScaleX(1);
            } else {
                targetView.setPivotX(0);
                targetView.setScaleX(1);
            }
        }
    }
}
