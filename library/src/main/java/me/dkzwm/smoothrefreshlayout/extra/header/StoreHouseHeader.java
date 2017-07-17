package me.dkzwm.smoothrefreshlayout.extra.header;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;

import me.dkzwm.smoothrefreshlayout.R;
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
    @RefreshViewStyle
    protected int mStyle = STYLE_DEFAULT;
    protected int mCurrentPosY;
    protected ArrayList<StoreHouseBarItemAnimation> mAnimations = new ArrayList<>();
    protected ArrayList<Matrix> mMatrices = new ArrayList<>();
    protected int mLineWidth = -1;
    protected float mScale = .5f;
    protected int mDropHeight = -1;
    protected float mProgress = 0;
    protected int mDrawZoneWidth = 0;
    protected int mDrawZoneHeight = 0;
    protected int mOffsetX = 0;
    protected int mOffsetY = 0;
    private float mInternalAnimationFactor = 0.7f;
    private int mHorizontalRandomness = -1;
    private float mBarDarkAlpha = 0.5f;
    private float mFromAlpha = 1.0f;
    private float mToAlpha = 0.5f;
    private int mLoadingAniDuration = 1000;
    private int mLoadingAniSegDuration = 1000;
    private int mLoadingAniItemDuration = 400;
    private float mTopOffset = 25;
    private float mBottomOffset = 25;
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
        if (attrs != null) {
            final TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.IRefreshView, 0, 0);
            @RefreshViewStyle
            int style = arr.getInt(R.styleable.IRefreshView_sr_style, mStyle);
            mStyle = style;
            arr.recycle();
        }
        mLineWidth = PixelUtl.dp2px(context, 1);
        mDropHeight = PixelUtl.dp2px(context, 40);
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

    public void setDropHeight(int height) {
        mDropHeight = height;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (getHandler() != null)
            getHandler().removeCallbacksAndMessages(null);
        mAnimations.clear();
        mMatrices.clear();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mStyle == STYLE_DEFAULT) {
            int height = getPaddingTop() + PixelUtl.dp2px(getContext(), mTopOffset)
                    + mDrawZoneHeight + getPaddingBottom() + PixelUtl.dp2px(getContext(), mBottomOffset);
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        mOffsetX = (getWidth() - mDrawZoneWidth) / 2;
        if (mStyle == STYLE_DEFAULT)
            mOffsetY = getPaddingTop() + PixelUtl.dp2px(getContext(), mTopOffset);
        else {
            mOffsetY = (mCurrentPosY - mDrawZoneHeight) / 2;
        }
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
        return mStyle == STYLE_SCALE ? mDrawZoneHeight
                + PixelUtl.dp2px(getContext(), mTopOffset)
                + PixelUtl.dp2px(getContext(), mBottomOffset) : 0;
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
        mCurrentPosY = indicator.getCurrentPosY();
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