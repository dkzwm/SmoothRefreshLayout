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
package me.dkzwm.widget.srl.util;

import android.view.View;
import androidx.annotation.Nullable;
import com.google.android.material.appbar.AppBarLayout;
import me.dkzwm.widget.srl.SmoothRefreshLayout;
import me.dkzwm.widget.srl.extra.IRefreshView;

/** @author dkzwm */
public class AppBarLayoutUtil
        implements SmoothRefreshLayout.OnHeaderEdgeDetectCallBack,
                SmoothRefreshLayout.OnFooterEdgeDetectCallBack {
    private AppBarLayout mAppBarLayout;
    private boolean mFullyExpanded;
    private boolean mFullyCollapsed;
    private AppBarLayout.OnOffsetChangedListener mListener =
            new AppBarLayout.OnOffsetChangedListener() {
                @Override
                public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                    mFullyExpanded = verticalOffset >= 0;
                    mFullyCollapsed = appBarLayout.getTotalScrollRange() + verticalOffset <= 0;
                }
            };

    public AppBarLayoutUtil(View view) {
        if (view instanceof AppBarLayout) {
            mAppBarLayout = (AppBarLayout) view;
            mAppBarLayout.addOnOffsetChangedListener(mListener);
        }
    }

    public void detach() {
        if (mAppBarLayout != null) {
            mAppBarLayout.removeOnOffsetChangedListener(mListener);
            mAppBarLayout = null;
        }
    }

    @Override
    public boolean isNotYetInEdgeCannotMoveHeader(
            SmoothRefreshLayout parent, @Nullable View child, @Nullable IRefreshView header) {
        if (child == null) {
            if (parent.isVerticalOrientation()) {
                return !mFullyExpanded;
            } else {
                return true;
            }
        } else {
            if (parent.isVerticalOrientation()) {
                return !mFullyExpanded || child.canScrollVertically(-1);
            } else {
                return child.canScrollHorizontally(-1);
            }
        }
    }

    @Override
    public boolean isNotYetInEdgeCannotMoveFooter(
            SmoothRefreshLayout parent, @Nullable View child, @Nullable IRefreshView footer) {
        if (child == null) {
            if (parent.isVerticalOrientation()) {
                return !mFullyCollapsed;
            } else {
                return true;
            }
        } else {
            if (parent.isVerticalOrientation()) {
                return !mFullyCollapsed || child.canScrollVertically(1);
            } else {
                return child.canScrollHorizontally(1);
            }
        }
    }
}
