package me.dkzwm.widget.srl.sample.header;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.List;
import me.dkzwm.widget.srl.SmoothRefreshLayout;
import me.dkzwm.widget.srl.extra.IRefreshView;
import me.dkzwm.widget.srl.indicator.IIndicator;
import me.dkzwm.widget.srl.sample.animation.StoreHouseBarItemAnimation;
import me.dkzwm.widget.srl.sample.utils.StoreHousePath;
import me.dkzwm.widget.srl.util.PixelUtl;

/**
 * Part of the code comes from @see <a
 * href="https://github.com/liaohuqiu/android-Ultra-Pull-To-Refresh">Modify by dkzwm
 */
public class StoreHouseHeader extends View implements IRefreshView {
    protected List<StoreHouseBarItemAnimation> mAnimations = new ArrayList<>();
    protected int mLineWidth;
    protected float mScale = .5f;
    protected int mDropHeight = -1;
    protected float mProgress = 0;
    protected int mDrawZoneWidth = 0;
    protected int mDrawZoneHeight = 0;
    protected int mOffsetX = 0;
    protected int mOffsetY = 0;
    private int mHorizontalRandomness = -1;
    private int mLoadingAniDuration = 1000;
    private int mLoadingAniSegDuration = 1000;
    private int mLoadingAniItemDuration = 400;
    private float mTopOffset = 25;
    private float mBottomOffset = 25;
    private AniController mAniController = new AniController();
    private int mTextColor = Color.WHITE;
    private me.dkzwm.widget.srl.extra.RefreshViewStyle mStyle;
    private Matrix mMatrix = new Matrix();

    public StoreHouseHeader(Context context) {
        this(context, null);
    }

    public StoreHouseHeader(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StoreHouseHeader(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mStyle = new me.dkzwm.widget.srl.extra.RefreshViewStyle(context, attrs, defStyleAttr, 0);
        mLineWidth = PixelUtl.dp2px(context, 1);
        setBackgroundColor(Color.DKGRAY);
        mHorizontalRandomness = context.getResources().getDisplayMetrics().widthPixels / 2;
    }

    private void setProgress(float progress) {
        mProgress = progress;
    }

    public int getLoadingAniDuration() {
        return mLoadingAniDuration;
    }

    public void setLoadingAniDuration(int duration) {
        mLoadingAniDuration = duration;
        mLoadingAniSegDuration = duration;
    }

    public void setLineWidth(int width) {
        mLineWidth = width;
        for (int i = 0; i < mAnimations.size(); i++) {
            mAnimations.get(i).setLineWidth(width);
        }
    }

    public void setTextColor(int color) {
        mTextColor = color;
        for (int i = 0; i < mAnimations.size(); i++) {
            mAnimations.get(i).setColor(color);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mAniController.stop();
        mAnimations.clear();
    }

    public int getTopOffset() {
        return getPaddingTop() + PixelUtl.dp2px(getContext(), mTopOffset);
    }

    public void setTopOffset(float offsetInDip) {
        mTopOffset = offsetInDip;
    }

    private int getBottomOffset() {
        return getPaddingBottom() + PixelUtl.dp2px(getContext(), mBottomOffset);
    }

    public void setBottomOffset(float offsetInDip) {
        mBottomOffset = offsetInDip;
    }

    public void initPathWithString(String str) {
        initPathWithString(str, 62, 86);
    }

    public void initPathWithString(String str, float fontWidthInPixel, float fontHeightInPixel) {
        ArrayList<float[]> pointList =
                StoreHousePath.parsePath(str, fontWidthInPixel, fontHeightInPixel, 22);
        initPathWithPointList(pointList);
    }

    public void initPathWithStringRes(int id) {
        String[] points = getResources().getStringArray(id);
        ArrayList<float[]> pointList = new ArrayList<>(points.length);
        for (String point : points) {
            String[] x = point.split(",");
            float[] f = new float[4];
            for (int j = 0; j < 4; j++) {
                f[j] = Float.parseFloat(x[j]);
            }
            pointList.add(f);
        }
        initPathWithPointList(pointList);
    }

    public float getScale() {
        return mScale;
    }

    public void setScale(float scale) {
        mScale = scale;
    }

    public void initPathWithPointList(ArrayList<float[]> pointList) {
        float drawWidth = 0;
        float drawHeight = 0;
        boolean shouldLayout = mAnimations.size() > 0;
        mAnimations.clear();
        for (int i = 0; i < pointList.size(); i++) {
            float[] line = pointList.get(i);
            float[] startPoint = new float[] {line[0] * mScale, line[1] * mScale};
            float[] endPoint = new float[] {line[2] * mScale, line[3] * mScale};
            drawWidth = Math.max(drawWidth, startPoint[0]);
            drawWidth = Math.max(drawWidth, endPoint[0]);
            drawHeight = Math.max(drawHeight, startPoint[1]);
            drawHeight = Math.max(drawHeight, endPoint[1]);
            StoreHouseBarItemAnimation item =
                    new StoreHouseBarItemAnimation(i, startPoint, endPoint, mTextColor, mLineWidth);
            item.resetPos(mHorizontalRandomness);
            mAnimations.add(item);
        }
        mDrawZoneWidth = (int) Math.ceil(drawWidth);
        mDrawZoneHeight = (int) Math.ceil(drawHeight);
        if (shouldLayout) {
            requestLayout();
        }
    }

    private void beginLoading() {
        mAniController.start();
        invalidate();
    }

    private void loadFinish() {
        mAniController.stop();
    }

    @Override
    public void onDraw(Canvas canvas) {
        float progress = mProgress;
        int c1 = canvas.save();
        int len = mAnimations.size();
        for (int i = 0; i < len; i++) {
            canvas.save();
            StoreHouseBarItemAnimation storeHouseBarItem = mAnimations.get(i);
            float offsetX = mOffsetX + storeHouseBarItem.getMiddlePoint()[0];
            float offsetY = mOffsetY + storeHouseBarItem.getMiddlePoint()[1];

            if (mAniController.mRunning) {
                storeHouseBarItem.getTransformation(getDrawingTime(), null);
                canvas.translate(offsetX, offsetY);
            } else {
                if (progress == 0) {
                    storeHouseBarItem.resetPos(mHorizontalRandomness);
                    continue;
                }
                float internalAnimationFactor = 0.7f;
                float startPadding = (1 - internalAnimationFactor) * i / len;
                float endPadding = 1 - internalAnimationFactor - startPadding;
                // onHookComplete
                float barDarkAlpha = 0.5f;
                if (progress == 1 || progress >= 1 - endPadding) {
                    canvas.translate(offsetX, offsetY);
                    storeHouseBarItem.setAlpha(barDarkAlpha);
                } else {
                    float realProgress;
                    if (progress <= startPadding) {
                        realProgress = 0;
                    } else {
                        realProgress =
                                Math.min(1, (progress - startPadding) / internalAnimationFactor);
                    }
                    offsetX += storeHouseBarItem.getTranslationX() * (1 - realProgress);
                    offsetY += -mDropHeight * (1 - realProgress);
                    mMatrix.reset();
                    mMatrix.postRotate(360 * realProgress);
                    mMatrix.postScale(realProgress, realProgress);
                    mMatrix.postTranslate(offsetX, offsetY);
                    storeHouseBarItem.setAlpha(barDarkAlpha * realProgress);
                    canvas.concat(mMatrix);
                }
            }
            storeHouseBarItem.onDraw(canvas);
            canvas.restore();
        }
        canvas.restoreToCount(c1);
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
        return mDrawZoneHeight + getTopOffset() + getBottomOffset();
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
        loadFinish();
        for (int i = 0; i < mAnimations.size(); i++) {
            mAnimations.get(i).resetPos(mHorizontalRandomness);
        }
    }

    @Override
    public void onRefreshPrepare(SmoothRefreshLayout layout) {}

    @Override
    public void onRefreshBegin(SmoothRefreshLayout layout, IIndicator indicator) {
        beginLoading();
    }

    @Override
    public void onRefreshComplete(SmoothRefreshLayout layout, boolean isSuccessful) {
        loadFinish();
    }

    @Override
    public void onRefreshPositionChanged(
            SmoothRefreshLayout layout, byte status, IIndicator indicator) {
        calculate(indicator);
        if (status == SmoothRefreshLayout.SR_STATUS_PREPARE
                || status == SmoothRefreshLayout.SR_STATUS_COMPLETE) {
            float currentPercent = Math.min(1f, indicator.getCurrentPercentOfRefreshOffset());
            setProgress(currentPercent);
            invalidate();
        }
    }

    @Override
    public void onPureScrollPositionChanged(
            SmoothRefreshLayout layout, byte status, IIndicator indicator) {
        calculate(indicator);
        float currentPercent = Math.min(1f, indicator.getCurrentPercentOfRefreshOffset());
        setProgress(currentPercent);
        invalidate();
    }

    private void calculate(IIndicator indicator) {
        mOffsetX = (getWidth() - mDrawZoneWidth) / 2;
        if (mStyle.mStyle != STYLE_SCALE && mStyle.mStyle != STYLE_FOLLOW_SCALE) {
            mOffsetY = getTopOffset();
        } else {
            if (mStyle.mStyle == STYLE_FOLLOW_SCALE
                    && indicator.getCurrentPos() <= getCustomHeight()) {
                mOffsetY = getTopOffset();
            } else {
                mOffsetY = (int) (getTopOffset() + (getHeight() - getCustomHeight()) / 2f);
            }
        }
        mDropHeight = getBottomOffset();
    }

    private class AniController implements Runnable {
        private int mTick = 0;
        private int mCountPerSeg = 0;
        private int mSegCount = 0;
        private int mInterval = 0;
        private boolean mRunning = false;

        private void start() {
            mRunning = true;
            mTick = 0;
            mInterval =
                    StoreHouseHeader.this.mLoadingAniDuration
                            / StoreHouseHeader.this.mAnimations.size();
            mCountPerSeg = StoreHouseHeader.this.mLoadingAniSegDuration / mInterval;
            mSegCount = StoreHouseHeader.this.mAnimations.size() / mCountPerSeg + 1;
            StoreHouseHeader.this.post(this);
        }

        @Override
        public void run() {
            StoreHouseHeader.this.invalidate();
            int pos = mTick % mCountPerSeg;
            for (int i = 0; i < mSegCount; i++) {
                int index = i * mCountPerSeg + pos;
                if (index > mTick) {
                    continue;
                }
                index = index % StoreHouseHeader.this.mAnimations.size();
                StoreHouseBarItemAnimation item = StoreHouseHeader.this.mAnimations.get(index);
                item.setFillAfter(false);
                item.setFillEnabled(true);
                item.setFillBefore(false);
                item.setDuration(StoreHouseHeader.this.mLoadingAniItemDuration);
                float fromAlpha = 1.0f;
                float toAlpha = 0.5f;
                item.start(fromAlpha, toAlpha);
            }
            mTick++;
            if (mRunning) {
                StoreHouseHeader.this.postDelayed(this, mInterval);
            }
        }

        private void stop() {
            mRunning = false;
            StoreHouseHeader.this.removeCallbacks(this);
        }
    }
}
