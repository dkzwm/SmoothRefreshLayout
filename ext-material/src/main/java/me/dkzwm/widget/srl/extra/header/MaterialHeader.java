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
package me.dkzwm.widget.srl.extra.header;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.NonNull;
import me.dkzwm.widget.srl.SmoothRefreshLayout;
import me.dkzwm.widget.srl.drawable.MaterialProgressDrawable;
import me.dkzwm.widget.srl.extra.IRefreshView;
import me.dkzwm.widget.srl.indicator.IIndicator;

/** @author dkzwm */
public class MaterialHeader<T extends IIndicator> extends View implements IRefreshView<T> {
    protected MaterialProgressDrawable mDrawable;
    protected float mScale = 1f;
    private ValueAnimator mAnimator;
    private SmoothRefreshLayout mRefreshLayout;
    private boolean mHasHook = false;
    private SmoothRefreshLayout.OnHookUIRefreshCompleteCallBack mHookUIRefreshCompleteCallBack =
            new SmoothRefreshLayout.OnHookUIRefreshCompleteCallBack() {
                @Override
                public void onHook(final SmoothRefreshLayout.RefreshCompleteHook hook) {
                    if (mRefreshLayout != null && mRefreshLayout.isRefreshing()) {
                        mAnimator.setDuration(300);
                        mAnimator.addListener(
                                new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        mAnimator.removeListener(this);
                                        hook.onHookComplete(true);
                                    }
                                });
                        mAnimator.start();
                    } else {
                        hook.onHookComplete(true);
                    }
                }
            };

    public MaterialHeader(Context context) {
        this(context, null);
    }

    public MaterialHeader(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MaterialHeader(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mDrawable = new MaterialProgressDrawable(getContext(), this);
        mDrawable.setBackgroundColor(Color.WHITE);
        mDrawable.setCallback(this);
        mAnimator = ValueAnimator.ofFloat(1, 0);
        mAnimator.setRepeatCount(0);
        mAnimator.setRepeatMode(ValueAnimator.RESTART);
        mAnimator.addUpdateListener(
                new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        mScale = (float) animation.getAnimatedValue();
                        mDrawable.setAlpha((int) (255 * mScale));
                        invalidate();
                    }
                });
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mHasHook) {
            if (mRefreshLayout != null) {
                mRefreshLayout.setOnHookHeaderRefreshCompleteCallback(
                        mHookUIRefreshCompleteCallBack);
            } else if (getParent() instanceof SmoothRefreshLayout) {
                mRefreshLayout = (SmoothRefreshLayout) getParent();
                mRefreshLayout.setOnHookHeaderRefreshCompleteCallback(
                        mHookUIRefreshCompleteCallBack);
            } else {
                mHasHook = false;
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        resetDrawable();
        cancelAnimator();
        if (mRefreshLayout != null && mHasHook) {
            mRefreshLayout.setOnHookHeaderRefreshCompleteCallback(null);
        }
    }

    @Override
    public void invalidateDrawable(Drawable dr) {
        if (dr == mDrawable) {
            invalidate();
        } else {
            super.invalidateDrawable(dr);
        }
    }

    public void setColorSchemeColors(int[] colors) {
        mDrawable.setColorSchemeColors(colors);
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mRefreshLayout == null) {
            if (getParent() instanceof SmoothRefreshLayout)
                mRefreshLayout = (SmoothRefreshLayout) getParent();
            if (mRefreshLayout == null) {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                return;
            }
        }
        if (mRefreshLayout.isVerticalOrientation()) {
            int height = mDrawable.getIntrinsicHeight() + getPaddingTop() + getPaddingBottom();
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        } else {
            int width = mDrawable.getIntrinsicWidth() + getPaddingLeft() + getPaddingRight();
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final int size = mDrawable.getIntrinsicHeight();
        mDrawable.setBounds(0, 0, size, size);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mRefreshLayout == null) return;
        final int saveCount = canvas.save();
        if (mRefreshLayout.isVerticalOrientation()) {
            int l = getPaddingLeft() + (getMeasuredWidth() - mDrawable.getIntrinsicWidth()) / 2;
            canvas.translate(l, getPaddingTop());
        } else {
            int top = getPaddingTop() + (getMeasuredHeight() - mDrawable.getIntrinsicWidth()) / 2;
            canvas.translate(getPaddingLeft(), top);
        }
        Rect rect = mDrawable.getBounds();
        canvas.scale(mScale, mScale, rect.exactCenterX(), rect.exactCenterY());
        mDrawable.draw(canvas);
        canvas.restoreToCount(saveCount);
    }

    public void doHookUIRefreshComplete(SmoothRefreshLayout layout) {
        mRefreshLayout = layout;
        mHasHook = true;
        layout.setOnHookHeaderRefreshCompleteCallback(mHookUIRefreshCompleteCallBack);
    }

    @Override
    public int getType() {
        return TYPE_HEADER;
    }

    @Override
    public int getStyle() {
        return STYLE_DEFAULT;
    }

    @Override
    public int getCustomHeight() {
        return 0;
    }

    @NonNull
    @Override
    public View getView() {
        return this;
    }

    @Override
    public void onFingerUp(SmoothRefreshLayout layout, T indicator) {}

    @Override
    public void onReset(SmoothRefreshLayout layout) {
        resetDrawable();
        cancelAnimator();
    }

    @Override
    public void onRefreshPrepare(SmoothRefreshLayout layout) {
        resetDrawable();
        cancelAnimator();
    }

    @Override
    public void onRefreshBegin(SmoothRefreshLayout layout, T indicator) {
        mDrawable.setAlpha(255);
        mDrawable.start();
        invalidate();
    }

    @Override
    public void onRefreshComplete(SmoothRefreshLayout layout, boolean isSuccessful) {}

    @Override
    public void onRefreshPositionChanged(SmoothRefreshLayout layout, byte status, T indicator) {
        float percent = Math.min(1, indicator.getCurrentPercentOfRefreshOffset());
        float alphaPercent = Math.min(1, percent * percent * percent);
        if (status == SmoothRefreshLayout.SR_STATUS_PREPARE) {
            mDrawable.setAlpha((int) (255 * alphaPercent));
            mDrawable.showArrow(true);
            float strokeStart = ((percent) * .8f);
            mDrawable.setStartEndTrim(0f, Math.min(0.8f, strokeStart));
            mDrawable.setArrowScale(percent);
            float rotation = (-0.25f + .4f * percent + percent * 2) * .5f;
            mDrawable.setProgressRotation(rotation);
            invalidate();
        }
    }

    @Override
    public void onPureScrollPositionChanged(SmoothRefreshLayout layout, byte status, T indicator) {
        if (indicator.hasJustLeftStartPosition()) {
            mDrawable.setAlpha(255);
            mDrawable.setStartEndTrim(0f, 0.8f);
            mDrawable.showArrow(true);
            mDrawable.setArrowScale(1);
            invalidate();
        }
    }

    private void resetDrawable() {
        mDrawable.setAlpha(255);
        mDrawable.stop();
        mScale = 1;
    }

    private void cancelAnimator() {
        if (mAnimator.isRunning()) mAnimator.cancel();
    }
}
