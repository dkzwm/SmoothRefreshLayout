package me.dkzwm.smoothrefreshlayout.extra.header;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;

import me.dkzwm.smoothrefreshlayout.SmoothRefreshLayout;
import me.dkzwm.smoothrefreshlayout.animation.StoreHouseBarItemAnimation;
import me.dkzwm.smoothrefreshlayout.extra.IRefreshView;
import me.dkzwm.smoothrefreshlayout.indicator.IIndicator;
import me.dkzwm.smoothrefreshlayout.utils.PixelUtl;
import me.dkzwm.smoothrefreshlayout.utils.StoreHousePath;

/**
 * Part of the code comes from @see <a href="https://github.com/liaohuqiu/android-Ultra-Pull-To-Refresh">
 * Modify by dkzwm
 */
public class StoreHouseHeader extends View implements IRefreshView {

    private ArrayList<StoreHouseBarItemAnimation> mAnimations = new ArrayList<>();
    private ArrayList<Matrix> mMatrices = new ArrayList<>();

    private int mLineWidth = -1;
    private float mScale = .5f;
    private int mDropHeight = -1;
    private float mInternalAnimationFactor = 0.7f;
    private int mHorizontalRandomness = -1;
    private float mProgress = 0;
    private int mDrawZoneWidth = 0;
    private int mDrawZoneHeight = 0;
    private int mOffsetX = 0;
    private int mOffsetY = 0;
    private float mBarDarkAlpha = 0.5f;
    private float mFromAlpha = 1.0f;
    private float mToAlpha = 0.5f;

    private int mLoadingAniDuration = 1000;
    private int mLoadingAniSegDuration = 1000;
    private int mLoadingAniItemDuration = 400;
    private float mTopOffset = 10;
    private float mBottomOffset = 10;

    private AniController mAniController = new AniController();
    private int mTextColor = Color.WHITE;

    public StoreHouseHeader(Context context) {
        this(context, null);
    }

    public StoreHouseHeader(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StoreHouseHeader(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mLineWidth = PixelUtl.dp2px(context, 1);
        mDropHeight = PixelUtl.dp2px(context, 40);
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

    public void setDropHeight(int height) {
        mDropHeight = height;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (getHandler() != null)
            getHandler().removeCallbacksAndMessages(null);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = getPaddingTop() + PixelUtl.dp2px(getContext(), mTopOffset)
                + mDrawZoneHeight + getPaddingBottom() + PixelUtl.dp2px(getContext(), mBottomOffset);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        mOffsetX = (getWidth() - mDrawZoneWidth) / 2;
        mOffsetY = getPaddingTop() + PixelUtl.dp2px(getContext(), mTopOffset);
        mDropHeight = getPaddingBottom() + PixelUtl.dp2px(getContext(), mBottomOffset);
    }

    public float getTopOffset() {
        return mTopOffset;
    }

    public void setTopOffset(float offsetInDip) {
        mTopOffset = offsetInDip;
    }

    private float getBottomOffset() {
        return mBottomOffset;
    }

    public void setBottomOffset(float offsetInDip) {
        mBottomOffset = offsetInDip;
    }

    public void initPathWithString(String str) {
        initPathWithString(str, 62, 86);
    }

    public void initPathWithString(String str, float fontWidthInPixel, float fontHeightInPixel) {
        ArrayList<float[]> pointList = StoreHousePath.parsePath(str, fontWidthInPixel,
                fontHeightInPixel, 22);
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
        mMatrices.clear();
        for (int i = 0; i < pointList.size(); i++) {
            float[] line = pointList.get(i);
            float[] startPoint = new float[]{line[0] * mScale, line[1] * mScale};
            float[] endPoint = new float[]{line[2] * mScale, line[3] * mScale};
            drawWidth = Math.max(drawWidth, startPoint[0]);
            drawWidth = Math.max(drawWidth, endPoint[0]);
            drawHeight = Math.max(drawHeight, startPoint[1]);
            drawHeight = Math.max(drawHeight, endPoint[1]);
            StoreHouseBarItemAnimation item = new StoreHouseBarItemAnimation(i, startPoint, endPoint,
                    mTextColor, mLineWidth);
            item.resetPos(mHorizontalRandomness);
            mAnimations.add(item);
            mMatrices.add(new Matrix());
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
                float startPadding = (1 - mInternalAnimationFactor) * i / len;
                float endPadding = 1 - mInternalAnimationFactor - startPadding;
                // onHookComplete
                if (progress == 1 || progress >= 1 - endPadding) {
                    canvas.translate(offsetX, offsetY);
                    storeHouseBarItem.setAlpha(mBarDarkAlpha);
                } else {
                    float realProgress;
                    if (progress <= startPadding) {
                        realProgress = 0;
                    } else {
                        realProgress = Math.min(1, (progress - startPadding) / mInternalAnimationFactor);
                    }
                    offsetX += storeHouseBarItem.getTranslationX() * (1 - realProgress);
                    offsetY += -mDropHeight * (1 - realProgress);
                    Matrix matrix = mMatrices.get(i);
                    matrix.reset();
                    matrix.postRotate(360 * realProgress);
                    matrix.postScale(realProgress, realProgress);
                    matrix.postTranslate(offsetX, offsetY);
                    storeHouseBarItem.setAlpha(mBarDarkAlpha * realProgress);
                    canvas.concat(matrix);
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
        loadFinish();
        for (int i = 0; i < mAnimations.size(); i++) {
            mAnimations.get(i).resetPos(mHorizontalRandomness);
        }
    }

    @Override
    public void onRefreshPrepare(SmoothRefreshLayout layout) {

    }

    @Override
    public void onRefreshBegin(SmoothRefreshLayout layout, IIndicator indicator) {
        beginLoading();
    }

    @Override
    public void onRefreshComplete(SmoothRefreshLayout layout) {
        loadFinish();
    }

    @Override
    public void onRefreshPositionChanged(SmoothRefreshLayout layout, byte status, IIndicator indicator) {
        if (status == SmoothRefreshLayout.SR_STATUS_PREPARE
                || status == SmoothRefreshLayout.SR_STATUS_COMPLETE) {
            float currentPercent = Math.min(1f, indicator.getCurrentPercentOfHeader());
            setProgress(currentPercent);
            invalidate();
        }
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
            mInterval = mLoadingAniDuration / mAnimations.size();
            mCountPerSeg = mLoadingAniSegDuration / mInterval;
            mSegCount = mAnimations.size() / mCountPerSeg + 1;
            post(this);
        }

        @Override
        public void run() {
            invalidate();
            int pos = mTick % mCountPerSeg;
            for (int i = 0; i < mSegCount; i++) {
                int index = i * mCountPerSeg + pos;
                if (index > mTick) {
                    continue;
                }
                index = index % mAnimations.size();
                StoreHouseBarItemAnimation item = mAnimations.get(index);
                item.setFillAfter(false);
                item.setFillEnabled(true);
                item.setFillBefore(false);
                item.setDuration(mLoadingAniItemDuration);
                item.start(mFromAlpha, mToAlpha);
            }
            mTick++;
            if (mRunning) {
                postDelayed(this, mInterval);
            }
        }

        private void stop() {
            mRunning = false;
            removeCallbacks(this);
        }
    }
}