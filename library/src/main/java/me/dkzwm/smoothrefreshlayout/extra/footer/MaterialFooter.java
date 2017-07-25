package me.dkzwm.smoothrefreshlayout.extra.footer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.SystemClock;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;

import me.dkzwm.smoothrefreshlayout.R;
import me.dkzwm.smoothrefreshlayout.SmoothRefreshLayout;
import me.dkzwm.smoothrefreshlayout.extra.IRefreshView;
import me.dkzwm.smoothrefreshlayout.indicator.IIndicator;
import me.dkzwm.smoothrefreshlayout.utils.PixelUtl;

/**
 * @author dkzwm
 */
public class MaterialFooter extends View implements IRefreshView {
    protected Paint mBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    protected RectF mProgressBounds = new RectF();
    protected int mDefaultHeight;
    protected float mProgress = 0f;
    protected int mStyle = STYLE_DEFAULT;
    protected int mCircleRadius;
    private boolean mFromFront = true;
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
        mDefaultHeight = context.getResources()
                .getDimensionPixelOffset(R.dimen.sr_footer_default_height);
        mBarWidth = PixelUtl.dp2px(context, 3);
        mCircleRadius = mBarWidth * 4;
        mBarPaint.setColor(Color.BLUE);
        mBarPaint.setStyle(Paint.Style.STROKE);
        mBarPaint.setDither(true);
        mBarPaint.setStrokeWidth(mBarWidth);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (getStyle() == STYLE_DEFAULT) {
            int height = mDefaultHeight + getPaddingTop() + getPaddingBottom();
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        canvas.restore();
        mProgressBounds.setEmpty();
        mProgressBounds.set(getWidth() / 2 - mCircleRadius - mBarWidth,
                getHeight() / 2 - mCircleRadius - mBarWidth,
                getWidth() / 2 + mCircleRadius + mBarWidth,
                getHeight() / 2 + mCircleRadius + mBarWidth);
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
                mGrowingTime -= barSpinCycleTime;
                mFromFront = !mFromFront;
            }
            float distance = (float) Math.cos((mGrowingTime / barSpinCycleTime + 1)
                    * Math.PI) / 2 + 0.5f;
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
        } else {
            canvas.drawArc(mProgressBounds, 270, mProgress * 360, false, mBarPaint);
        }
        if (mMustInvalidate)
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

    public void setProgressBarRadius(int radius) {
        mCircleRadius = radius;
        invalidate();
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
        return mDefaultHeight;
    }

    @NonNull
    @Override
    public View getView() {
        return this;
    }

    @Override
    public void onReset(SmoothRefreshLayout layout) {
        mMustInvalidate = false;
        mProgress = 0;
        mIsSpinning = false;
        invalidate();
    }

    @Override
    public void onRefreshPrepare(SmoothRefreshLayout layout) {

    }

    @Override
    public void onFingerUp(SmoothRefreshLayout layout, IIndicator indicator) {

    }

    @Override
    public void onRefreshBegin(SmoothRefreshLayout layout, IIndicator indicator) {
        mProgress = 1f;
        mIsSpinning = true;
        mMustInvalidate = true;
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
    public void onRefreshPositionChanged(SmoothRefreshLayout layout, byte status, IIndicator indicator) {
        float percent = Math.min(1f, indicator.getCurrentPercentOfFooter());
        if (status == SmoothRefreshLayout.SR_STATUS_PREPARE) {
            mIsSpinning = false;
            mMustInvalidate = false;
            mProgress = percent;
            invalidate();
        }
    }
}
