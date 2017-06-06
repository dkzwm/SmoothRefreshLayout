package me.dkzwm.smoothrefreshlayout.animation;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import java.util.Random;

/**
 * Created by srain on 11/6/14.
 * Modify by dkzwm on 6/6/17
 *
 * @author srain;dkzwm
 */
public class StoreHouseBarItemAnimation extends Animation {
    private final  float[] mMiddlePoint = new float[2];
    private final float[] mStartPoint = new float[2];
    private final float[] mEndPoint = new float[2];
    private final int mIndex;
    private final Paint mPaint;
    private float mFromAlpha = 1.0f;
    private float mToAlpha = 0.4f;
    private float mTranslationX;


    public StoreHouseBarItemAnimation(int index,
                                      float[] start,
                                      float[] end,
                                      int color,
                                      int lineWidth) {
        mIndex = index;

        mMiddlePoint[0] = (start[0] + end[0]) / 2;
        mMiddlePoint[1] = (start[1] + end[1]) / 2;
        mStartPoint[0] = start[0] - mMiddlePoint[0];
        mStartPoint[1] = start[1] - mMiddlePoint[1];
        mEndPoint[0] = end[0] - mMiddlePoint[0];
        mEndPoint[1] = end[1] - mMiddlePoint[1];
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(color);
        mPaint.setStrokeWidth(lineWidth);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
    }

    public float[] getMiddlePoint() {
        return mMiddlePoint;
    }

    public int getIndex() {
        return mIndex;
    }

    public float getTranslationX() {
        return mTranslationX;
    }

    public void setLineWidth(int width) {
        mPaint.setStrokeWidth(width);
    }

    public void setColor(int color) {
        mPaint.setColor(color);
    }

    public void resetPos(int horizontalRandomness) {
        Random random = new Random();
        mTranslationX = -random.nextInt(horizontalRandomness) + horizontalRandomness;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        float alpha = mFromAlpha;
        alpha = alpha + ((mToAlpha - alpha) * interpolatedTime);
        setAlpha(alpha);
    }

    public void start(float fromAlpha, float toAlpha) {
        mFromAlpha = fromAlpha;
        mToAlpha = toAlpha;
        super.start();
    }

    public void setAlpha(float alpha) {
        mPaint.setAlpha(Math.round(alpha * 255));
    }

    public void onDraw(Canvas canvas) {
        canvas.drawLine(mStartPoint[0], mStartPoint[1], mEndPoint[0], mEndPoint[1], mPaint);
    }
}