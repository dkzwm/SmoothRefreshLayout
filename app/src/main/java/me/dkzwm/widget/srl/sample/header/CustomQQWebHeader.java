package me.dkzwm.widget.srl.sample.header;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import me.dkzwm.widget.srl.SmoothRefreshLayout;
import me.dkzwm.widget.srl.extra.IRefreshView;
import me.dkzwm.widget.srl.indicator.IIndicator;
import me.dkzwm.widget.srl.sample.R;

/**
 * Created by dkzwm on 2017/6/21.
 *
 * @author dkzwm
 */
public class CustomQQWebHeader extends FrameLayout implements IRefreshView {

    public CustomQQWebHeader(@NonNull Context context) {
        this(context, null);
    }

    public CustomQQWebHeader(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomQQWebHeader(
            @NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.layout_custom_qq_web_header, this);
    }

    @Override
    public int getType() {
        return TYPE_HEADER;
    }

    @Override
    public int getStyle() {
        return STYLE_PIN;
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
    public void onRefreshBegin(SmoothRefreshLayout layout, IIndicator indicator) {}

    @Override
    public void onRefreshComplete(SmoothRefreshLayout layout, boolean isSuccessful) {}

    @Override
    public void onRefreshPositionChanged(
            SmoothRefreshLayout layout, byte status, IIndicator indicator) {}

    @Override
    public void onPureScrollPositionChanged(
            SmoothRefreshLayout layout, byte status, IIndicator indicator) {}
}
