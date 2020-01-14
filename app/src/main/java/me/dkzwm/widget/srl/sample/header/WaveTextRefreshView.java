package me.dkzwm.widget.srl.sample.header;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import me.dkzwm.widget.srl.SmoothRefreshLayout;
import me.dkzwm.widget.srl.extra.IRefreshView;
import me.dkzwm.widget.srl.indicator.IIndicator;
import me.dkzwm.widget.srl.util.PixelUtl;

/**
 * Created by dkzwm on 2017/8/9.
 *
 * @author dkzwm
 */
public class WaveTextRefreshView extends View implements IRefreshView {
    protected byte mStatus = SmoothRefreshLayout.SR_STATUS_INIT;
    private int mType = TYPE_HEADER;
    private float mOffsetY = 0;
    private float mProgress = 1f;
    private float mOffsetX;
    private String mText = "SmoothRefreshLayout";
    private TextPaint mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private Paint mWavePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Path mTextPath = new Path();
    private Rect mTextRect = new Rect();
    private RectF mTextRectF = new RectF();
    private Path mWavePath = new Path();
    private Path mMovingPath = new Path();
    private float mAmplitude;
    private float mWaveLength;
    private float mIncrementalY = 1f;
    private float mIncrementalX = 1f;
    private float mTextSpacing = 5f;

    public WaveTextRefreshView(Context context) {
        this(context, null);
    }

    public WaveTextRefreshView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WaveTextRefreshView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mTextPaint.setTextSize(
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_SP,
                        24,
                        context.getResources().getDisplayMetrics()));
        mTextPaint.setTypeface(Typeface.DEFAULT);
        mTextPaint.setFakeBoldText(true);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setColor(Color.GRAY);
        mWavePaint.setColor(Color.GREEN);
        mWavePaint.setStyle(Paint.Style.FILL);
        mAmplitude = PixelUtl.dp2px(context, 3);
        mWaveLength = PixelUtl.dp2px(context, 12);
        final int dip20 = PixelUtl.dp2px(context, 20);
        setPadding(0, dip20, 0, dip20);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            setLayerType(LAYER_TYPE_SOFTWARE, null);
        }
    }

    public void setIncrementalY(float incrementalY) {
        mIncrementalY = incrementalY;
    }

    public void setIncrementalX(float incrementalX) {
        mIncrementalX = incrementalX;
    }

    public void setTextSpacing(float textSpacing) {
        mTextSpacing = textSpacing;
    }

    public void setText(String text) {
        mText = text;
        requestLayout();
    }

    public void setTextTypeface(Typeface typeface) {
        mTextPaint.setTypeface(typeface);
        requestLayout();
    }

    public void setTextPaint(TextPaint paint) {
        mTextPaint = paint;
        requestLayout();
    }

    public void setTextSize(float size) {
        setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
    }

    public void setTextSize(int unit, float size) {
        mTextPaint.setTextSize(
                (TypedValue.applyDimension(
                        unit, size, getContext().getResources().getDisplayMetrics())));
    }

    public void setTextColor(@ColorInt int color) {
        mTextPaint.setColor(color);
    }

    public void setWaveColor(@ColorInt int color) {
        mWavePaint.setColor(color);
    }

    public void setAmplitude(float amplitude) {
        mAmplitude = amplitude;
        requestLayout();
    }

    public void setWaveLength(float waveLength) {
        mWaveLength = waveLength;
        requestLayout();
    }

    public void setTypeface(Typeface typeface) {
        mTextPaint.setTypeface(typeface);
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mTextPaint.getTextBounds(mText, 0, mText.length(), mTextRect);
        heightMeasureSpec =
                MeasureSpec.makeMeasureSpec(
                        mTextRect.height() + getPaddingTop() + getPaddingBottom(),
                        MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mWavePath.reset();
        mWavePath.moveTo(0, mAmplitude);
        int waveCount = (int) (w % mWaveLength == 0 ? w / mWaveLength + 1 : w / mWaveLength + 2);
        for (int i = 0; i < waveCount; i++) {
            mWavePath.quadTo(
                    mWaveLength / 4 + i * mWaveLength,
                    -mAmplitude,
                    mWaveLength / 2 + i * mWaveLength,
                    mAmplitude);
            mWavePath.quadTo(
                    mWaveLength / 4 * 3 + i * mWaveLength,
                    mAmplitude * 2,
                    mWaveLength + i * mWaveLength,
                    mAmplitude);
        }
        mWavePath.lineTo(mWaveLength * waveCount, mAmplitude * 2 + mTextRect.height());
        mWavePath.lineTo(0, mAmplitude * 2 + mTextRect.height());
        mWavePath.offset(0, -mAmplitude * 2);
        mTextPath.reset();
        float offsetX = 0;
        final Path tempPath = new Path();
        final Paint.FontMetrics tempFontMetrics = new Paint.FontMetrics();
        mTextPaint.getFontMetrics(tempFontMetrics);
        for (int i = 0; i < mText.length(); i++) {
            String str = mText.substring(i, i + 1);
            mTextPaint.getTextPath(str, 0, 1, 0, 0, tempPath);
            mTextPath.addPath(tempPath, offsetX, 0);
            offsetX += mTextSpacing;
            offsetX += mTextPaint.measureText(str, 0, 1);
        }
        tempPath.reset();
        mTextPath.computeBounds(mTextRectF, true);
        mTextPath.offset(w / 2 - mTextRectF.width() / 2, getPaddingTop() - mTextRectF.top);
        mOffsetX = 0;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mStatus == SmoothRefreshLayout.SR_STATUS_COMPLETE) {
            mOffsetX = 0;
            mOffsetY = (mTextRect.height() + mAmplitude * 2 + getPaddingTop());
        }
        mMovingPath.set(mWavePath);
        mMovingPath.offset(mOffsetX, mOffsetY);
        canvas.drawPath(mTextPath, mTextPaint);
        canvas.save();
        canvas.clipPath(mTextPath);
        canvas.drawPath(mMovingPath, mWavePaint);
        canvas.restore();
        mOffsetX -= mIncrementalX;
        if (mOffsetX <= -mWaveLength) mOffsetX = 0f;
        if (mStatus == SmoothRefreshLayout.SR_STATUS_REFRESHING
                || mStatus == SmoothRefreshLayout.SR_STATUS_LOADING_MORE) {
            mOffsetY -= mIncrementalY;
            if (mOffsetY <= getPaddingTop())
                mOffsetY = (mTextRect.height() + mAmplitude * 2 + getPaddingTop());
            mProgress = 1 - mOffsetY / (mTextRect.height() + mAmplitude * 2);
        }
        if (mStatus == SmoothRefreshLayout.SR_STATUS_COMPLETE) return;
        invalidate();
    }

    @Override
    public int getType() {
        return mType;
    }

    public void setType(@RefreshViewType int type) {
        mType = type;
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
    public void onFingerUp(SmoothRefreshLayout layout, IIndicator indicator) {}

    @Override
    public void onReset(SmoothRefreshLayout layout) {
        mStatus = SmoothRefreshLayout.SR_STATUS_INIT;
        mProgress = 0;
    }

    @Override
    public void onRefreshPrepare(SmoothRefreshLayout layout) {
        mStatus = SmoothRefreshLayout.SR_STATUS_PREPARE;
    }

    @Override
    public void onRefreshBegin(SmoothRefreshLayout layout, IIndicator indicator) {
        if (layout.isRefreshing()) mStatus = SmoothRefreshLayout.SR_STATUS_REFRESHING;
        else mStatus = SmoothRefreshLayout.SR_STATUS_LOADING_MORE;
    }

    @Override
    public void onRefreshComplete(SmoothRefreshLayout layout, boolean isSuccessful) {
        mStatus = SmoothRefreshLayout.SR_STATUS_COMPLETE;
    }

    @Override
    public void onRefreshPositionChanged(
            SmoothRefreshLayout layout, byte status, IIndicator indicator) {
        if (status == SmoothRefreshLayout.SR_STATUS_PREPARE) {
            mProgress = Math.min(1, indicator.getCurrentPercentOfRefreshOffset());
            mProgress = mProgress * mProgress * mProgress;
            mOffsetY = (mTextRectF.height() + mAmplitude * 2) * (1 - mProgress) + getPaddingTop();
            invalidate();
        }
    }

    @Override
    public void onPureScrollPositionChanged(
            SmoothRefreshLayout layout, byte status, IIndicator indicator) {
        mProgress = Math.min(1, indicator.getCurrentPercentOfRefreshOffset());
        mProgress = mProgress * mProgress * mProgress;
        mOffsetY = (mTextRectF.height() + mAmplitude * 2) * (1 - mProgress) + getPaddingTop();
        invalidate();
    }
}
