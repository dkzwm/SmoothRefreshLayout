package me.dkzwm.smoothrefreshlayout.extra.header;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import me.dkzwm.smoothrefreshlayout.R;
import me.dkzwm.smoothrefreshlayout.SmoothRefreshLayout;
import me.dkzwm.smoothrefreshlayout.extra.IRefreshView;
import me.dkzwm.smoothrefreshlayout.indicator.IIndicator;
import me.dkzwm.smoothrefreshlayout.utils.PixelUtl;
import me.dkzwm.smoothrefreshlayout.view.ProgressWheel;

/**
 * Created by dkzwm on 2017/7/10.
 *
 * @author dkzwm
 */

public class WaveHeader extends ViewGroup implements IRefreshView {
    private byte mStatus = SmoothRefreshLayout.SR_STATUS_INIT;
    private float[] mLastPoint = new float[]{0, 0};
    private float mFingerUpY = 0;
    private int mCurrentPosY = 0;
    private int mDefaultHeight;
    private Paint mPaint;
    private Path mPath;
    private ProgressWheel mWheel;
    private TextView mTextView;

    public WaveHeader(Context context) {
        this(context, null);
    }

    public WaveHeader(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WaveHeader(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPath = new Path();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(ContextCompat.getColor(context, R.color.colorPrimary));
        mPaint.setDither(true);
        setWillNotDraw(false);
        mWheel = new ProgressWheel(context);
        mWheel.setBarColor(Color.WHITE);
        final int dip2 = PixelUtl.dp2px(context, 2);
        mWheel.setBarWidth(dip2);
        mWheel.setRimWidth(dip2);
        mWheel.setCircleRadius(dip2 * 14);
        mWheel.setPadding(0, 0, 0, dip2 * 3);
        mTextView = new TextView(context);
        mTextView.setTextColor(Color.WHITE);
        mTextView.setTextSize(14);
        mTextView.setPadding(0, dip2, 0, dip2 * 3);
        addView(mWheel, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        addView(mTextView, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        mDefaultHeight = PixelUtl.getDisplayHeight(context) / 2;
    }

    public void setWaveColor(@ColorInt int color) {
        mPaint.setColor(color);
        invalidate();
    }

    public void setTextColor(@ColorInt int color) {
        mTextView.setTextColor(color);
    }

    public void setTextSize(float size) {
        mTextView.setTextSize(size);
    }

    public void setProgressBarWidth(int width) {
        mWheel.setBarWidth(width);
    }

    public void setProgressBarColor(@ColorInt int color) {
        mWheel.setBarColor(color);
    }

    public void setProgressRimWidth(int width) {
        mWheel.setRimWidth(width);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (mWheel.getVisibility() != GONE) {
            mWheel.layout(r / 2 - mWheel.getMeasuredWidth() / 2,
                    mCurrentPosY - mWheel.getMeasuredHeight(),
                    r / 2 + mWheel.getMeasuredWidth() / 2,
                    mCurrentPosY);
        } else if (mTextView.getVisibility() != GONE) {
            mTextView.layout(r / 2 - mTextView.getMeasuredWidth() / 2,
                    mCurrentPosY - mTextView.getMeasuredHeight(),
                    r / 2 + mTextView.getMeasuredWidth() / 2,
                    mCurrentPosY);
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = mDefaultHeight + getPaddingTop() + getPaddingBottom();
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        if (mWheel.getVisibility() != GONE)
            measureChild(mWheel, widthMeasureSpec, heightMeasureSpec);
        else if (mTextView.getVisibility() != GONE)
            measureChild(mTextView, widthMeasureSpec, heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }


    @Override
    public int getType() {
        return TYPE_HEADER;
    }

    @Override
    public void draw(Canvas canvas) {
        mPath.reset();
        mPath.moveTo(0, 0);
        mPath.quadTo(mLastPoint[0], mLastPoint[1] * 2, getWidth(), 0);
        mPath.lineTo(0, 0);
        canvas.drawPath(mPath, mPaint);
        super.draw(canvas);
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
        mWheel.stopSpinning();
        mWheel.setVisibility(GONE);
        mTextView.setVisibility(GONE);
    }

    @Override
    public void onRefreshPrepare(SmoothRefreshLayout layout) {
        mStatus = SmoothRefreshLayout.SR_STATUS_PREPARE;
        mFingerUpY = 0;
        mCurrentPosY = 0;
        mWheel.stopSpinning();
        mWheel.setVisibility(GONE);
        mTextView.setVisibility(GONE);
        if (layout.isEnabledPullToRefresh()) {
            mTextView.setText(me.dkzwm.smoothrefreshlayout.R.string.sr_pull_down_to_refresh);
        } else {
            mTextView.setText(me.dkzwm.smoothrefreshlayout.R.string.sr_pull_down);
        }
    }

    @Override
    public void onRefreshBegin(SmoothRefreshLayout layout, IIndicator indicator) {
        mStatus = SmoothRefreshLayout.SR_STATUS_REFRESHING;
        mWheel.setVisibility(VISIBLE);
        mWheel.spin();
    }

    @Override
    public void onRefreshComplete(SmoothRefreshLayout layout) {
        mStatus = SmoothRefreshLayout.SR_STATUS_COMPLETE;
        if (layout.isRefreshSuccessful()) {
            mTextView.setText(me.dkzwm.smoothrefreshlayout.R.string.sr_refresh_complete);
        } else {
            mTextView.setText(me.dkzwm.smoothrefreshlayout.R.string.sr_refresh_failed);
        }
        mTextView.setVisibility(VISIBLE);
        mWheel.setVisibility(GONE);
        layout.updateScrollerInterpolator(new DecelerateInterpolator());
    }

    @Override
    public void onRefreshPositionChanged(SmoothRefreshLayout layout, byte status, IIndicator indicator) {
        mStatus = status;
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
                            percent = (mCurrentPosY - offsetToKeepHeader) / (mFingerUpY - offsetToKeepHeader);
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
            invalidate();
        } else if (status == SmoothRefreshLayout.SR_STATUS_REFRESHING) {
            mLastPoint[0] = width / 2;
            mLastPoint[1] = mCurrentPosY;
            requestLayout();
        } else if (status == SmoothRefreshLayout.SR_STATUS_COMPLETE) {
            mLastPoint[0] = width / 2;
            mLastPoint[1] = mCurrentPosY;
            requestLayout();
        }
    }
}
