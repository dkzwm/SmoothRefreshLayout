package me.dkzwm.smoothrefreshlayout.extra.header;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;

import me.dkzwm.smoothrefreshlayout.SmoothRefreshLayout;
import me.dkzwm.smoothrefreshlayout.drawable.MaterialProgressDrawable;
import me.dkzwm.smoothrefreshlayout.extra.IRefreshView;
import me.dkzwm.smoothrefreshlayout.indicator.IIndicator;

/**
 * @author dkzwm
 */
public class MaterialHeader extends View implements IRefreshView {
    private MaterialProgressDrawable mDrawable;
    private float mScale = 1f;
    private int mCachedDuration = -1;
    private ValueAnimator mAnimator;
    private SmoothRefreshLayout mRefreshLayout;
    private SmoothRefreshLayout.OnHookUIRefreshCompleteCallBack mHookUIRefreshCompleteCallBack
            = new SmoothRefreshLayout.OnHookUIRefreshCompleteCallBack() {
        @Override
        public void onHook(final SmoothRefreshLayout.RefreshCompleteHook hook) {
            if (mRefreshLayout != null && mRefreshLayout.isRefreshing()) {
                mAnimator.setDuration(180);
                mAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mAnimator.removeListener(this);
                        hook.onHookComplete();
                    }
                });
                mAnimator.start();
            } else {
                hook.onHookComplete();
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
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mScale = (float) animation.getAnimatedValue();
                mDrawable.setAlpha((int) (255 * mScale));
                invalidate();
            }
        });
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        resetDrawable();
        cancelAnimator();
    }

    public void release() {
        if (mRefreshLayout != null) {
            if (mRefreshLayout.equalsOnHookHeaderRefreshCompleteCallback(mHookUIRefreshCompleteCallBack))
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
        int height = mDrawable.getIntrinsicHeight() + getPaddingTop() + getPaddingBottom();
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final int size = mDrawable.getIntrinsicHeight();
        mDrawable.setBounds(0, 0, size, size);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final int saveCount = canvas.save();
        Rect rect = mDrawable.getBounds();
        int l = getPaddingLeft() + (getMeasuredWidth() - mDrawable.getIntrinsicWidth()) / 2;
        canvas.translate(l, getPaddingTop());
        canvas.scale(mScale, mScale, rect.exactCenterX(), rect.exactCenterY());
        mDrawable.draw(canvas);
        canvas.restoreToCount(saveCount);
    }

    public void doHookUIRefreshComplete(SmoothRefreshLayout layout) {
        mRefreshLayout = layout;
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
    public void onFingerUp(SmoothRefreshLayout layout, IIndicator indicator) {
    }

    @Override
    public void onReset(SmoothRefreshLayout layout) {
        resetDrawable();
        if (layout.equalsOnHookHeaderRefreshCompleteCallback(mHookUIRefreshCompleteCallBack)
                && mCachedDuration > 0) {
            layout.setDurationToCloseHeader(mCachedDuration);
        }
        mCachedDuration = -1;
    }

    @Override
    public void onRefreshPrepare(SmoothRefreshLayout layout) {
        resetDrawable();
        cancelAnimator();
    }

    @Override
    public void onRefreshBegin(SmoothRefreshLayout layout, IIndicator indicator) {
        mDrawable.setAlpha(255);
        mDrawable.start();
    }

    @Override
    public void onRefreshComplete(SmoothRefreshLayout layout) {
        if (layout.equalsOnHookHeaderRefreshCompleteCallback(mHookUIRefreshCompleteCallBack)) {
            int duration = layout.getDurationToCloseHeader();
            if (duration > 0)
                mCachedDuration = duration;
            layout.setDurationToCloseHeader(0);
            return;
        }
        long duration = layout.getDurationToCloseHeader();
        mAnimator.setDuration(duration);
        mAnimator.start();
    }

    @Override
    public void onRefreshPositionChanged(SmoothRefreshLayout layout, byte status, IIndicator indicator) {
        float percent = Math.min(1, indicator.getCurrentPercentOfHeader());
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

    private void resetDrawable() {
        mDrawable.setAlpha(255);
        mDrawable.stop();
        mScale = 1;
    }

    private void cancelAnimator() {
        if (mAnimator.isRunning())
            mAnimator.cancel();
    }
}
