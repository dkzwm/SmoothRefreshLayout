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

    byte STYLE_DEFAULT = 0;
    byte STYLE_SCALE = 1;

    /**
     * Get the view's type.
     *
     * @return type {@link #TYPE_HEADER}, {@link #TYPE_FOOTER}.
     */
    @RefreshViewType
    int getType();

    /**
     * Get the view's style. If return {@link #STYLE_SCALE} SmoothRefreshLayout will dynamically
     * change the height, so the performance will be reduced.
     *
     * @return style {@link #STYLE_DEFAULT}, {@link #STYLE_SCALE}.
     */
    @RefreshViewStyle
    int getStyle();

    /**
     * Get the custom height, If style is {@link #STYLE_SCALE} should return a custom height.
     *
     * @return Custom height
     */
    int getCustomHeight();

    /**
     * Get the target view.
     *
     * @return The returned view must be the view that will be added to the Layout
     */
    @NonNull
    View getView();

    /**
     * This method will be triggered when the touched finger is lifted.
     *
     * @param layout    The layout {@link SmoothRefreshLayout}
     * @param indicator The indicator {@link IIndicator}
     */
    void onFingerUp(SmoothRefreshLayout layout, IIndicator indicator);

    /**
     * This method will be triggered when the refresh state is reset to
     * {@link SmoothRefreshLayout#SR_STATUS_INIT}.
     *
     * @param layout The layout {@link SmoothRefreshLayout}
     */
    void onReset(SmoothRefreshLayout layout);

    /**
     * This method will be triggered when the frame is ready to refreshing.
     *
     * @param layout The layout {@link SmoothRefreshLayout}
     */
    void onRefreshPrepare(SmoothRefreshLayout layout);

    /**
     * This method will be triggered when the frame begin to refresh.
     *
     * @param layout    The layout {@link SmoothRefreshLayout}
     * @param indicator The indicator {@link IIndicator}
     */
    void onRefreshBegin(SmoothRefreshLayout layout, IIndicator indicator);

    /**
     * This method will be triggered when the frame is refresh completed.
     *
     * @param layout The layout {@link SmoothRefreshLayout}
     */
    void onRefreshComplete(SmoothRefreshLayout layout);

    /**
     * This method will be triggered when the position of the refresh view changes.
     *
     * @param layout The layout {@link SmoothRefreshLayout}
     */
    void onRefreshPositionChanged(SmoothRefreshLayout layout, byte status, IIndicator indicator);

    @IntDef({IRefreshView.TYPE_HEADER, IRefreshView.TYPE_FOOTER})
    @Retention(RetentionPolicy.SOURCE)
    @interface RefreshViewType {
    }

    @IntDef({IRefreshView.STYLE_DEFAULT, IRefreshView.STYLE_SCALE})
    @Retention(RetentionPolicy.SOURCE)
    @interface RefreshViewStyle {
    }
}
