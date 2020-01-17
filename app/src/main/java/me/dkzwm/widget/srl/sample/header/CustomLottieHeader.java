package me.dkzwm.widget.srl.sample.header;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.RenderMode;
import me.dkzwm.widget.srl.SmoothRefreshLayout;
import me.dkzwm.widget.srl.extra.IRefreshView;
import me.dkzwm.widget.srl.indicator.IIndicator;

public class CustomLottieHeader extends LottieAnimationView implements IRefreshView<IIndicator> {

    public CustomLottieHeader(@NonNull Context context) {
        this(context, null);
    }

    public CustomLottieHeader(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomLottieHeader(
            @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
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
    public void onFingerUp(SmoothRefreshLayout layout, IIndicator indicator) {}

    @Override
    public void onReset(SmoothRefreshLayout layout) {}

    @Override
    public void onRefreshPrepare(SmoothRefreshLayout layout) {}

    @Override
    public void onRefreshBegin(SmoothRefreshLayout layout, IIndicator indicator) {
        setRenderMode(RenderMode.HARDWARE);
        setRepeatMode(LottieDrawable.RESTART);
        setRepeatCount(LottieDrawable.INFINITE);
        playAnimation();
    }

    @Override
    public void onRefreshComplete(SmoothRefreshLayout layout, boolean isSuccessful) {
        pauseAnimation();
    }

    @Override
    public void onRefreshPositionChanged(
            SmoothRefreshLayout layout, byte status, IIndicator indicator) {
        if (status == SmoothRefreshLayout.SR_STATUS_PREPARE) {
            float progress = Math.min(1, indicator.getCurrentPercentOfRefreshOffset() % 1f);
            setProgress(progress);
        }
    }

    @Override
    public void onPureScrollPositionChanged(
            SmoothRefreshLayout layout, byte status, IIndicator indicator) {}
}
