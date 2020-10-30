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
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import me.dkzwm.widget.srl.config.Constants;
import me.dkzwm.widget.srl.indicator.DefaultIndicator;
import me.dkzwm.widget.srl.indicator.HorizontalDefaultIndicator;
import me.dkzwm.widget.srl.manager.HRefreshLayoutManager;
import me.dkzwm.widget.srl.manager.HScaleLayoutManager;
import me.dkzwm.widget.srl.util.HorizontalScrollCompat;

/** @author dkzwm */
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
    protected void createIndicator() {
        DefaultIndicator indicator = new HorizontalDefaultIndicator();
        mIndicator = indicator;
        mIndicatorSetter = indicator;
    }

    @Override
    public void setMode(int mode) {
        if (mode == Constants.MODE_DEFAULT) {
            if (mLayoutManager instanceof HRefreshLayoutManager) {
                return;
            }
            setLayoutManager(new HRefreshLayoutManager());
        } else {
            if (mLayoutManager instanceof HScaleLayoutManager) {
                return;
            }
            setLayoutManager(new HScaleLayoutManager());
        }
    }

    @Override
    protected void tryToDealAnotherDirectionMove(float offsetX, float offsetY) {
        if (isDisabledWhenAnotherDirectionMove()) {
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
            if (!mPreventForAnotherDirection) {
                mDealAnotherDirectionMove = true;
            }
        }
    }

    @Override
    public boolean isNotYetInEdgeCannotMoveHeader() {
        View targetView = getScrollTargetView();
        if (mInEdgeCanMoveHeaderCallBack != null) {
            return mInEdgeCanMoveHeaderCallBack.isNotYetInEdgeCannotMoveHeader(
                    this, targetView, mHeaderView);
        }
        return targetView != null && targetView.canScrollHorizontally(-1);
    }

    @Override
    public boolean isNotYetInEdgeCannotMoveFooter() {
        final View targetView = getScrollTargetView();
        if (mInEdgeCanMoveFooterCallBack != null) {
            return mInEdgeCanMoveFooterCallBack.isNotYetInEdgeCannotMoveFooter(
                    this, targetView, mHeaderView);
        }
        return targetView != null && targetView.canScrollHorizontally(1);
    }

    @Override
    protected void tryToCompatSyncScroll(View view, float delta) {
        if (mSyncScrollCallback == null) {
            HorizontalScrollCompat.scrollCompat(view, delta);
        } else {
            mSyncScrollCallback.onScroll(view, delta);
        }
    }

    @Override
    protected void dispatchNestedFling(int velocity) {
        if (sDebug) {
            Log.d(TAG, String.format("dispatchNestedFling() : %s", velocity));
        }
        final View targetView = getScrollTargetView();
        HorizontalScrollCompat.flingCompat(targetView, -velocity);
    }

    @Override
    protected boolean canAutoLoadMore(View view) {
        if (mAutoLoadMoreCallBack != null) {
            mAutoLoadMoreCallBack.canAutoLoadMore(this, view);
        }
        return HorizontalScrollCompat.canAutoLoadMore(view);
    }

    @Override
    protected boolean canAutoRefresh(View view) {
        if (mAutoRefreshCallBack != null) {
            mAutoRefreshCallBack.canAutoRefresh(this, view);
        }
        return HorizontalScrollCompat.canAutoRefresh(view);
    }

    @Override
    protected boolean isScrollingView(View target) {
        return HorizontalScrollCompat.isScrollingView(target);
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
    }
}
