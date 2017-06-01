package me.dkzwm.smoothrefreshlayout.extra;

import android.support.annotation.NonNull;
import android.view.View;

import me.dkzwm.smoothrefreshlayout.SmoothRefreshLayout;
import me.dkzwm.smoothrefreshlayout.indicator.IIndicator;

/**
 * Created by dkzwm on 2017/5/18.
 *
 * @author dkzwm
 */
public interface IRefreshView {
    int TYPE_HEADER = 0;
    int TYPE_FOOTER = 1;

    @RefreshViewType
    int getType();

    @NonNull
    View getView();

    void onFingerUp(SmoothRefreshLayout layout, IIndicator indicator);

    void onReset(SmoothRefreshLayout layout);

    void onRefreshPrepare(SmoothRefreshLayout layout);

    void onRefreshBegin(SmoothRefreshLayout layout, IIndicator indicator);

    void onRefreshComplete(SmoothRefreshLayout layout);

    void onRefreshPositionChanged(SmoothRefreshLayout layout, byte status, IIndicator indicator);

}
