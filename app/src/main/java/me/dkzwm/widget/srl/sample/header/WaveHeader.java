package me.dkzwm.widget.srl.sample.header;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.SystemClock;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import me.dkzwm.widget.srl.SmoothRefreshLayout;
import me.dkzwm.widget.srl.extra.IRefreshView;
import me.dkzwm.widget.srl.indicator.IIndicator;
import me.dkzwm.widget.srl.util.PixelUtl;

/**
 * Created by dkzwm on 2017/7/10.
 *
 * @author dkzwm
 */
public class WaveHeader extends View implements IRefreshView {
    private static final Interpolator sBounceInterpolator = new BounceInterpolator();
    protected Paint mWavePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    protected Paint mBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    protected Paint mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    protected RectF mProgressBounds = new RectF();
    protected Path mPath = new Path();
    protected String mText;
    protected byte mStatus = SmoothRefreshLayout.SR_STATUS_INIT;
    protected float[] mLastPoint = new float[] {0, 0};
    protected int mDefaultHeight;
    protected float mMaxY = 0;
    protected float mProgress = 0f;
    protected int mCurrentPosY = 0;
    protected int mCircleRadius;
    private boolean mFromFront = true;
    private double mGrowingTime = 0;
    private float mBarExtraLength = 0;
    private long mLastDrawProgressTime = 0;
    private int mBarWidth = 4;
    private int mDip2;
    private me.dkzwm.widget.srl.extra.RefreshViewStyle mStyle;

    public WaveHeader(Context context) {
        this(context, null);
    }

    public WaveHeader(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WaveHeader(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mStyle = new me.dkzwm.widget.srl.extra.RefreshViewStyle(context, attrs, defStyleAttr, 0);
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        mWavePaint.setStyle(Paint.Style.FILL);
        mWavePaint.setColor(Color.BLUE);
        mWavePaint.setDither(true);
        mBarPaint.setColor(Color.WHITE);
        mBarPaint.setStyle(Paint.Style.STROKE);
        mBarPaint.setStrokeWidth(mBarWidth);
        mBarPaint.setDither(true);
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14, metrics));
        mTextPaint.setDither(true);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        setWillNotDraw(false);
        mDip2 = PixelUtl.dp2px(context, 2);
        mCircleRadius = mDip2 * 6;
        mDefaultHeight = metrics.heightPixels / 2;
    }

    public void setDefaultHeight(int height) {
        mDefaultHeight = height;
        requestLayout();
    }

    public void setWaveColor(@ColorInt int color) {
        mWavePaint.setColor(color);
        invalidate();
    }

    public void setTextColor(@ColorInt int color) {
        mTextPaint.setColor(color);
        invalidate();
    }

    public void setTextSize(float size) {
        mTextPaint.setTextSize(size);
        invalidate();
    }

    public void setProgressBarWidth(int width) {
        mBarWidth = width;
        mBarPaint.setStrokeWidth(mBarWidth);
        invalidate();
    }

    public void setProgressBarColor(@ColorInt int color) {
        mBarPaint.setColor(color);
        invalidate();
    }

    @Override
    public int getType() {
        return TYPE_HEADER;
    }

    @Override
    public int getStyle() {
        return mStyle.mStyle;
    }

    public void setStyle(@RefreshViewStyle int style) {
        if (mStyle.mStyle != style) {
            mStyle.mStyle = style;
            requestLayout();
        }
    }

    @Override
    public int getCustomHeight() {
        return mDefaultHeight;
    }

    public void setCustomHeight(int height) {
        mDefaultHeight = height;
        requestLayout();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPath.reset();
        mPath.moveTo(0, 0);
        mPath.quadTo(mLastPoint[0], mLastPoint[1] * 2, getWidth(), 0);
        mPath.lineTo(0, 0);
        canvas.drawPath(mPath, mWavePaint);
        if (mStatus == SmoothRefreshLayout.SR_STATUS_REFRESHING) {
            drawProgress(canvas);
        } else {
            if (mStatus == SmoothRefreshLayout.SR_STATUS_COMPLETE && !TextUtils.isEmpty(mText)) {
                drawText(canvas);
            }
        }
    }

    private void drawProgress(Canvas canvas) {
        canvas.save();
        canvas.restore();
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
            mGrowingTime -= barSpinCycleTime;
            mFromFront = !mFromFront;
        }
        float distance =
                (float) Math.cos((mGrowingTime / barSpinCycleTime + 1) * Math.PI) / 2f + 0.5f;
        int barMaxLength = 270;
        float destLength = (barMaxLength - barLength);

        if (mFromFront) {
            mBarExtraLength = distance * destLength;
        } else {
            float newLength = destLength * (1 - distance);
            mProgress += (mBarExtraLength - newLength);
            mBarExtraLength = newLength;
        }
        mProgress += deltaNormalized;
        if (mProgress > 360) {
            mProgress -= 360f;
        }
        mLastDrawProgressTime = SystemClock.uptimeMillis();

        float startAngle = mProgress - 90;
        float sweepAngle = barLength + mBarExtraLength;
        canvas.drawArc(mProgressBounds, startAngle, sweepAngle, false, mBarPaint);
        canvas.save();
        ViewCompat.postInvalidateOnAnimation(this);
    }

    private void drawText(Canvas canvas) {
        canvas.save();
        canvas.restore();
        float textCenterY =
                mCurrentPosY + ((mTextPaint.descent() + mTextPaint.ascent()) / 2f) - mDip2 * 5;
        canvas.drawText(mText, getWidth() / 2f, textCenterY, mTextPaint);
        canvas.save();
    }

    @NonNull
    @Override
    public View getView() {
        return this;
    }

    @Override
    public void onFingerUp(SmoothRefreshLayout layout, IIndicator indicator) {}

    @Override
    public void onReset(SmoothRefreshLayout layout) {
        layout.resetScrollerInterpolator();
        mStatus = SmoothRefreshLayout.SR_STATUS_INIT;
        reset();
        invalidate();
    }

    @Override
    public void onRefreshPrepare(SmoothRefreshLayout layout) {
        layout.resetScrollerInterpolator();
        mStatus = SmoothRefreshLayout.SR_STATUS_PREPARE;
        reset();
        invalidate();
    }

    @Override
    public void onRefreshBegin(SmoothRefreshLayout layout, IIndicator indicator) {
        mStatus = SmoothRefreshLayout.SR_STATUS_REFRESHING;
        layout.resetScrollerInterpolator();
        updateProgressBounds();
        invalidate();
    }

    @Override
    public void onRefreshComplete(SmoothRefreshLayout layout, boolean isSuccessful) {
        mStatus = SmoothRefreshLayout.SR_STATUS_COMPLETE;
        if (layout.isRefreshSuccessful()) {
            mText =
                    getContext()
                            .getString(
                                    me.dkzwm.widget.srl.ext.classic.R.string.sr_refresh_complete);
        } else {
            mText =
                    getContext()
                            .getString(me.dkzwm.widget.srl.ext.classic.R.string.sr_refresh_failed);
        }
        layout.resetScrollerInterpolator();
        invalidate();
    }

    @Override
    public void onRefreshPositionChanged(
            SmoothRefreshLayout layout, byte status, IIndicator indicator) {
        mCurrentPosY = indicator.getCurrentPos();
        mMaxY = Math.max(mCurrentPosY, mMaxY);
        final int width = getWidth();
        final float[] lastMovePoint = indicator.getLastMovePoint();
        final int offsetToKeepHeader;
        if (layout.isEnabledKeepRefreshView()) {
            offsetToKeepHeader = indicator.getOffsetToKeepHeaderWhileLoading();
        } else {
            offsetToKeepHeader = 0;
        }
        if (status == SmoothRefreshLayout.SR_STATUS_PREPARE) {
            if (offsetToKeepHeader > 0 && mCurrentPosY > offsetToKeepHeader) {
                layout.setSpringBackInterpolator(sBounceInterpolator);
            }
            float x = lastMovePoint[0];
            float percent;
            if (!layout.isAutoRefresh()) {
                if (mMaxY > offsetToKeepHeader) {
                    if (mCurrentPosY > offsetToKeepHeader) {
                        percent =
                                (mCurrentPosY - offsetToKeepHeader) / (mMaxY - offsetToKeepHeader);
                    } else {
                        percent = 0;
                    }
                    if (x > width) {
                        x = x - (x - width / 2f) * (1 - percent);
                    } else {
                        x = x + (width / 2f - x) * (1 - percent);
                    }
                } else {
                    percent = mCurrentPosY / mMaxY;
                    if (x > width) {
                        x = x - (x - width / 2f) * (1 - percent);
                    } else {
                        x = x + (width / 2f - x) * (1 - percent);
                    }
                }
            } else {
                x = width / 2f;
            }
            mLastPoint[0] = x;
            mLastPoint[1] = mCurrentPosY;
        } else if (status == SmoothRefreshLayout.SR_STATUS_REFRESHING) {
            updateProgressBounds();
        } else if (status == SmoothRefreshLayout.SR_STATUS_COMPLETE) {
            mLastPoint[0] = width / 2f;
            mLastPoint[1] = mCurrentPosY;
        }
        invalidate();
    }

    @Override
    public void onPureScrollPositionChanged(
            SmoothRefreshLayout layout, byte status, IIndicator indicator) {
        final int width = getWidth();
        if (indicator.hasTouched()) {
            mLastPoint = new float[] {indicator.getLastMovePoint()[0], mCurrentPosY};
        } else mLastPoint[0] = width / 2f;
        mLastPoint[1] = mCurrentPosY;
        invalidate();
    }

    private void updateProgressBounds() {
        final int width = getWidth();
        mProgressBounds.setEmpty();
        mProgressBounds.set(
                width / 2f - mCircleRadius - mBarWidth,
                mCurrentPosY - mCircleRadius * 2 - mDip2 * 5 - mBarWidth * 2,
                width / 2f + mCircleRadius + mBarWidth,
                mCurrentPosY - mDip2 * 5);
        mLastPoint[0] = width / 2f;
        mLastPoint[1] = mCurrentPosY;
    }

    private void reset() {
        mMaxY = 0;
        mProgress = 0;
        mLastDrawProgressTime = 0;
        mBarExtraLength = 0;
        mGrowingTime = 0;
        mCurrentPosY = 0;
        mLastPoint[0] = 0;
        mLastPoint[1] = 0;
        mPath.reset();
    }
}
