package me.dkzwm.smoothrefreshlayout.extra.footer;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import com.pnikosis.materialishprogress.ProgressWheel;

import me.dkzwm.smoothrefreshlayout.SmoothRefreshLayout;
import me.dkzwm.smoothrefreshlayout.extra.IRefreshView;
import me.dkzwm.smoothrefreshlayout.indicator.IIndicator;
import me.dkzwm.smoothrefreshlayout.utils.PixelUtl;

/**
 * @author dkzwm
 */
public class MaterialFooter extends FrameLayout implements IRefreshView {
    protected ProgressWheel mProgress;
    protected int mDefaultHeight;

    public MaterialFooter(Context context) {
        this(context, null);
    }

    public MaterialFooter(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MaterialFooter(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mProgress = new ProgressWheel(context);
        mProgress.setBarColor(Color.BLUE);
        mProgress.setCircleRadius(PixelUtl.dp2px(context, 20));
        mProgress.setBarWidth(PixelUtl.dp2px(context, 3));
        mDefaultHeight = PixelUtl.dp2px(context, 64);
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, mDefaultHeight);
        layoutParams.gravity = Gravity.CENTER;
        addView(mProgress, layoutParams);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mProgress.stopSpinning();
    }

    @Override
    public int getType() {
        return TYPE_FOOTER;
    }

    @Override
    public int getStyle() {
        return STYLE_DEFAULT;
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
        mProgress.stopSpinning();
    }

    @Override
    public void onRefreshPrepare(SmoothRefreshLayout layout) {

    }

    @Override
    public void onFingerUp(SmoothRefreshLayout layout, IIndicator indicator) {

    }

    @Override
    public void onRefreshBegin(SmoothRefreshLayout layout, IIndicator indicator) {
        mProgress.spin();
    }

    @Override
    public void onRefreshComplete(SmoothRefreshLayout layout) {
        mProgress.stopSpinning();
    }

    @Override
    public void onRefreshPositionChanged(SmoothRefreshLayout layout, byte status, IIndicator indicator) {
        float percent = Math.min(1f, indicator.getCurrentPercentOfFooter());
        if (status == SmoothRefreshLayout.SR_STATUS_PREPARE) {
            if (mProgress.isSpinning()) {
                mProgress.stopSpinning();
            }
            mProgress.setInstantProgress(percent);
            invalidate();
        }
    }
}
