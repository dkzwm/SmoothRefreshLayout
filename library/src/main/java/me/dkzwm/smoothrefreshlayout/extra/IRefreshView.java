package me.dkzwm.smoothrefreshlayout.extra;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.view.View;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import me.dkzwm.smoothrefreshlayout.SmoothRefreshLayout;
import me.dkzwm.smoothrefreshlayout.indicator.IIndicator;

/**
 * Created by dkzwm on 2017/5/18.
 *
 * @author dkzwm
 */
public interface IRefreshView {
    byte TYPE_HEADER = 0;
    byte TYPE_FOOTER = 1;

    /**
     * Get the view type
     *
     * @return type {@link #TYPE_HEADER}, {@link #TYPE_FOOTER}.
     */
    @RefreshViewType
    int getType();

    /**
     * Get the target view
     *
     * @return view:The returned view must be the view that will be added to the Layout
     */
    @NonNull
    View getView();

    void onFingerUp(SmoothRefreshLayout layout, IIndicator indicator);

    void onReset(SmoothRefreshLayout layout);

    void onRefreshPrepare(SmoothRefreshLayout layout);

    void onRefreshBegin(SmoothRefreshLayout layout, IIndicator indicator);

    void onRefreshComplete(SmoothRefreshLayout layout);

    void onRefreshPositionChanged(SmoothRefreshLayout layout, byte status, IIndicator indicator);

    @IntDef({IRefreshView.TYPE_HEADER, IRefreshView.TYPE_FOOTER})
    @Retention(RetentionPolicy.CLASS)
    @interface RefreshViewType {
    }
}
