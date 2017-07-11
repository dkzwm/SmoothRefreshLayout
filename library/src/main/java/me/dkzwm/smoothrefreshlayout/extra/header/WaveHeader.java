package me.dkzwm.smoothrefreshlayout.extra.header;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;

import me.dkzwm.smoothrefreshlayout.R;
import me.dkzwm.smoothrefreshlayout.SmoothRefreshLayout;
import me.dkzwm.smoothrefreshlayout.extra.IRefreshView;
import me.dkzwm.smoothrefreshlayout.indicator.IIndicator;
import me.dkzwm.smoothrefreshlayout.utils.PixelUtl;

/**
 * Created by dkzwm on 2017/7/10.
 *
 * @author dkzwm
 */

public class WaveHeader extends View implements IRefreshView {
    private byte mStatus = SmoothRefreshLayout.SR_STATUS_INIT;
    private float[] mLastPoint = new float[]{0, 0};
    private float mProgress = 0.0f;
    private float mFingerUpY = 0;
    private int mCurrentPosY = 0;
    private int mCircleRadius;
    private int mDefaultHeight;
    private int mBarWidth = 4;
    private int mDip2;
    private Paint mWavePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private RectF mProgressBounds = new RectF();
    private Path mPath = new Path();
    private String mText;

    public WaveHeader(Context context) {
        this(context, null);
    }

    public WaveHeader(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WaveHeader(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        mWavePaint.setStyle(Paint.Style.FILL);
        mWavePaint.setColor(ContextCompat.getColor(context, R.color.colorPrimary));
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
        mDefaultHeight = metrics.heightPixels;
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
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = mDefaultHeight + getPaddingTop() + getPaddingBottom();
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }


    @Override
    public int getType() {
        return TYPE_HEADER;
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
            if (mStatus == SmoothRefreshLayout.SR_STATUS_COMPLETE
                    && !TextUtils.isEmpty(mText)) {
                drawText(canvas);
            }
            mProgress = 0;
        }
    }

    private void drawProgress(Canvas canvas) {
        canvas.save();
        canvas.restore();
        mProgress += 6.5f;
        if (mProgress > 360) {
            mProgress -= 360f;
        }
        float from = mProgress - 90;
        canvas.drawArc(mProgressBounds, from, 300, false, mBarPaint);
        canvas.save();
        invalidate();
    }

    private void drawText(Canvas canvas) {
        canvas.save();
        canvas.restore();
        float textCenterY = mCurrentPosY + ((mTextPaint.descent() + mTextPaint.ascent()) / 2)
                - mDip2 * 5;
        canvas.drawText(mText, getWidth() / 2, textCenterY, mTextPaint);
        canvas.save();
    }


    @NonNull
    @Override
    public View getView() {
        return this;
    }

    @Override
    public void onFingerUp(SmoothRefreshLayout layout, IIndicator indicator) {
        mFingerUpY = indicator.getCurrentPosY();
        if (layout.isEnabledKeepRefreshView()) {
            final int offsetToKeepHeader = indicator.getOffsetToKeepHeaderWhileLoading();
            if (mFingerUpY > offsetToKeepHeader) {
                layout.updateScrollerInterpolator(new BounceInterpolator());
            }
        }
        invalidate();
    }

    @Override
    public void onReset(SmoothRefreshLayout layout) {
        mStatus = SmoothRefreshLayout.SR_STATUS_INIT;
        mFingerUpY = 0;
        mCurrentPosY = 0;
        mLastPoint[0] = 0;
        mLastPoint[1] = 0;
        mPath.reset();
        invalidate();
    }

    @Override
    public void onRefreshPrepare(SmoothRefreshLayout layout) {
        mStatus = SmoothRefreshLayout.SR_STATUS_PREPARE;
        mFingerUpY = 0;
        mCurrentPosY = 0;
        invalidate();
    }

    @Override
    public void onRefreshBegin(SmoothRefreshLayout layout, IIndicator indicator) {
        mStatus = SmoothRefreshLayout.SR_STATUS_REFRESHING;
        invalidate();
    }

    @Override
    public void onRefreshComplete(SmoothRefreshLayout layout) {
        mStatus = SmoothRefreshLayout.SR_STATUS_COMPLETE;
        if (layout.isRefreshSuccessful()) {
            mText = getContext().getString(me.dkzwm.smoothrefreshlayout.R.string.sr_refresh_complete);
        } else {
            mText = getContext().getString(me.dkzwm.smoothrefreshlayout.R.string.sr_refresh_failed);
        }
        layout.updateScrollerInterpolator(new DecelerateInterpolator());
        invalidate();
    }

    @Override
    public void onRefreshPositionChanged(SmoothRefreshLayout layout, byte status, IIndicator indicator) {
        mCurrentPosY = indicator.getCurrentPosY();
        final int width = getWidth();
        final float[] lastMovePoint = indicator.getLastMovePoint();
        final int offsetToKeepHeader;
        if (layout.isEnabledKeepRefreshView()) {
            offsetToKeepHeader = indicator.getOffsetToKeepHeaderWhileLoading();
        } else {
            offsetToKeepHeader = 0;
        }
        if (status == SmoothRefreshLayout.SR_STATUS_PREPARE) {
            if (indicator.hasTouched()) {
                mLastPoint = new float[]{lastMovePoint[0], mCurrentPosY};
            } else if (layout.isOverScrolling()) {
                mLastPoint = new float[]{width / 2, mCurrentPosY};
            } else {
                float x = lastMovePoint[0];
                float percent;
                if (mFingerUpY > 0) {
                    if (mFingerUpY > offsetToKeepHeader) {
                        if (mCurrentPosY > offsetToKeepHeader) {
                            percent = (mCurrentPosY - offsetToKeepHeader)
                                    / (mFingerUpY - offsetToKeepHeader);
                        } else {
                            percent = 0;
                        }
                        if (x > width) {
                            x = x - (x - width / 2) * (1 - percent);
                        } else {
                            x = x + (width / 2 - x) * (1 - percent);
                        }
                    } else {
                        percent = mCurrentPosY / mFingerUpY;
                        if (x > width) {
                            x = x - (x - width / 2) * (1 - percent);
                        } else {
                            x = x + (width / 2 - x) * (1 - percent);
                        }
                    }
                } else {
                    x = width / 2;
                }
                mLastPoint[0] = x;
                mLastPoint[1] = mCurrentPosY;
            }
        } else if (status == SmoothRefreshLayout.SR_STATUS_REFRESHING) {
            mProgressBounds.setEmpty();
            mProgressBounds = new RectF(width / 2 - mCircleRadius - mBarWidth,
                    mCurrentPosY - mCircleRadius * 2 - mDip2 * 5 - mBarWidth * 2,
                    width / 2 + mCircleRadius + mBarWidth,
                    mCurrentPosY - mDip2 * 5);
            mLastPoint[0] = width / 2;
            mLastPoint[1] = mCurrentPosY;
        } else if (status == SmoothRefreshLayout.SR_STATUS_COMPLETE) {
            mLastPoint[0] = width / 2;
            mLastPoint[1] = mCurrentPosY;
        }
        invalidate();
    }
}
