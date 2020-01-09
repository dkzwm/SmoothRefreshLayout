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
package me.dkzwm.widget.srl.extra.footer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import me.dkzwm.widget.srl.SmoothRefreshLayout;
import me.dkzwm.widget.srl.extra.IRefreshView;
import me.dkzwm.widget.srl.indicator.IIndicator;
import me.dkzwm.widget.srl.util.PixelUtl;

/** @author dkzwm */
public class MaterialFooter<T extends IIndicator> extends View implements IRefreshView<T> {
    protected int mStyle = STYLE_DEFAULT;
    protected int mDefaultHeightInDP = 64;
    private int[] mColors = new int[] {Color.RED, Color.BLUE, Color.GREEN, Color.BLACK};
    private Paint mBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private RectF mProgressBounds = new RectF();
    private float mProgress = 0f;
    private int mCircleRadius;
    private int mColorIndex = 0;
    private boolean mFromFront = true;
    private boolean mGrowing = false;
    private double mGrowingTime = 0;
    private float mBarExtraLength = 0;
    private long mLastDrawProgressTime = 0;
    private int mBarWidth;
    private boolean mMustInvalidate;
    private boolean mIsSpinning = false;

    public MaterialFooter(Context context) {
        this(context, null);
    }

    public MaterialFooter(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MaterialFooter(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mBarWidth = PixelUtl.dp2px(context, 3);
        mCircleRadius = mBarWidth * 4;
        mBarPaint.setStyle(Paint.Style.STROKE);
        mBarPaint.setDither(true);
        mBarPaint.setStrokeWidth(mBarWidth);
    }

    public void setDefaultHeightInDP(@IntRange(from = 0) int defaultHeightInDP) {
        mDefaultHeightInDP = defaultHeightInDP;
        requestLayout();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mProgressBounds.set(
                w / 2f - mCircleRadius - mBarWidth,
                h / 2f - mCircleRadius - mBarWidth,
                w / 2f + mCircleRadius + mBarWidth,
                h / 2f + mCircleRadius + mBarWidth);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!mMustInvalidate) mColorIndex = 0;
        if (mIsSpinning) {
            long deltaTime;
            if (mLastDrawProgressTime <= 0) {
                deltaTime = 0;
            } else {
                deltaTime = (SystemClock.uptimeMillis() - mLastDrawProgressTime);
            }
            float spinSpeed = 180.0f;
            float deltaNormalized = deltaTime * spinSpeed / 1000.0f;
            int barLength = 16;
            mGrowingTime += deltaTime;
            double barSpinCycleTime = 600;
            if (mGrowingTime > barSpinCycleTime) {
                mGrowingTime = mGrowingTime % barSpinCycleTime;
                mFromFront = !mFromFront;
            }
            float distance =
                    (float) Math.cos((mGrowingTime / barSpinCycleTime + 1) * Math.PI) / 2 + 0.5f;
            int barMaxLength = 270;
            float destLength = (barMaxLength - barLength);
            float barExtraLength;
            if (mFromFront) {
                barExtraLength = distance * destLength;
            } else {
                float newLength = destLength * (1 - distance);
                mProgress += (mBarExtraLength - newLength);
                barExtraLength = newLength;
            }
            mProgress += deltaNormalized;
            if (mProgress > 360) {
                mProgress -= 360f;
            }
            mLastDrawProgressTime = SystemClock.uptimeMillis();
            if (mBarExtraLength < destLength / 2 && barExtraLength < destLength / 2) {
                if ((barExtraLength > mBarExtraLength && !mGrowing)
                        || (barExtraLength < mBarExtraLength && mGrowing)) {
                    mBarPaint.setColor(mColors[mColorIndex % mColors.length]);
                    mColorIndex++;
                }
            }
            mGrowing = barExtraLength > mBarExtraLength;
            mBarExtraLength = barExtraLength;
            float startAngle = mProgress - 90;
            float sweepAngle = barLength + mBarExtraLength;
            canvas.drawArc(mProgressBounds, startAngle, sweepAngle, false, mBarPaint);
            canvas.save();
        } else {
            canvas.drawArc(mProgressBounds, 270, mProgress * 360, false, mBarPaint);
        }
        if (mMustInvalidate) ViewCompat.postInvalidateOnAnimation(this);
    }

    public void setProgressBarWidth(int width) {
        mBarWidth = width;
        mBarPaint.setStrokeWidth(mBarWidth);
        if (mStyle == STYLE_SCALE) requestLayout();
        else invalidate();
    }

    public void setProgressBarColors(@NonNull int[] colors) {
        mColors = colors;
        invalidate();
    }

    public void setProgressBarRadius(int radius) {
        mCircleRadius = radius;
        if (mStyle == STYLE_SCALE) requestLayout();
        else invalidate();
    }

    @Override
    public int getType() {
        return TYPE_FOOTER;
    }

    @Override
    public int getStyle() {
        return mStyle;
    }

    public void setStyle(@RefreshViewStyle int style) {
        mStyle = style;
        requestLayout();
    }

    @Override
    public int getCustomHeight() {
        return PixelUtl.dp2px(getContext(), mDefaultHeightInDP);
    }

    @NonNull
    @Override
    public View getView() {
        return this;
    }

    @Override
    public void onReset(SmoothRefreshLayout layout) {
        reset();
    }

    @Override
    public void onRefreshPrepare(SmoothRefreshLayout layout) {
        reset();
    }

    @Override
    public void onFingerUp(SmoothRefreshLayout layout, T indicator) {}

    @Override
    public void onRefreshBegin(SmoothRefreshLayout layout, T indicator) {
        mProgress = 1f;
        mIsSpinning = true;
        mMustInvalidate = true;
        mColorIndex = 0;
        invalidate();
    }

    @Override
    public void onRefreshComplete(SmoothRefreshLayout layout, boolean isSuccessful) {
        mMustInvalidate = false;
        mProgress = 1f;
        mIsSpinning = false;
        invalidate();
    }

    @Override
    public void onRefreshPositionChanged(SmoothRefreshLayout layout, byte status, T indicator) {
        float percent = Math.min(1f, indicator.getCurrentPercentOfLoadMoreOffset());
        if (status == SmoothRefreshLayout.SR_STATUS_PREPARE) {
            mIsSpinning = false;
            mMustInvalidate = false;
            mProgress = percent;
            invalidate();
        }
    }

    @Override
    public void onPureScrollPositionChanged(SmoothRefreshLayout layout, byte status, T indicator) {
        if (indicator.hasJustLeftStartPosition()) {
            mIsSpinning = false;
            mMustInvalidate = false;
            mProgress = 1;
            invalidate();
        }
    }

    private void reset() {
        mMustInvalidate = false;
        mLastDrawProgressTime = 0;
        mGrowingTime = 0;
        mFromFront = true;
        mBarExtraLength = 0;
        mProgress = 0f;
        mColorIndex = 0;
        mIsSpinning = false;
        mBarPaint.setColor(mColors[0]);
        invalidate();
    }
}
