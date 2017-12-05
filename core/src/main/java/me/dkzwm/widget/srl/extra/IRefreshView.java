package me.dkzwm.widget.srl.extra;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.view.View;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import me.dkzwm.widget.srl.SmoothRefreshLayout;
import me.dkzwm.widget.srl.indicator.IIndicator;

/**
 * @author dkzwm
 */
public interface IRefreshView<T extends IIndicator> {

    byte TYPE_HEADER = 0;
    byte TYPE_FOOTER = 1;

    byte STYLE_DEFAULT = 0;
    byte STYLE_SCALE = 1;
    //desc start
    //added in version 1.4.8
    byte STYLE_PIN = 2;
    byte STYLE_FOLLOW_SCALE = 3;
    byte STYLE_FOLLOW_PIN = 4;
    byte STYLE_FOLLOW_CENTER = 5;
    //desc end

    /**
     * Get the view type.
     *
     * @return type {@link #TYPE_HEADER}, {@link #TYPE_FOOTER}.
     */
    @RefreshViewType
    int getType();

    /**
     * Get the view style. If return {@link #STYLE_SCALE} SmoothRefreshLayout will dynamically
     * change the height, so the performance will be reduced. If return {@link #STYLE_FOLLOW_SCALE}
     * , when the moved position large than the view height, SmoothRefreshLayout will dynamically
     * change the height, so the performance will be reduced.
     * <p>
     * Since 1.4.8 add {@link #STYLE_PIN}, {@link #STYLE_FOLLOW_SCALE}, {@link #STYLE_FOLLOW_PIN},
     * {@link #STYLE_FOLLOW_CENTER}
     *
     * @return style {@link #STYLE_DEFAULT}, {@link #STYLE_SCALE}, {@link #STYLE_PIN},
     * {@link #STYLE_FOLLOW_SCALE}, {@link #STYLE_FOLLOW_PIN}, {@link #STYLE_FOLLOW_CENTER}.
     */
    @RefreshViewStyle
    int getStyle();

    /**
     * Get the custom height,  When the return style is {@link #STYLE_SCALE} or
     * {@link #STYLE_FOLLOW_SCALE} , you must return a accurate height<br/>
     * <p>
     * Since version 1.6.1, If you want the height equal to the srl height, you can return `-1`
     * {@link android.view.ViewGroup.LayoutParams#MATCH_PARENT}
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
    void onFingerUp(SmoothRefreshLayout layout, T indicator);

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
    void onRefreshBegin(SmoothRefreshLayout layout, T indicator);

    /**
     * This method will be triggered when the frame is refresh completed.
     *
     * @param layout       The layout {@link SmoothRefreshLayout}
     * @param isSuccessful The layout refresh state
     */
    void onRefreshComplete(SmoothRefreshLayout layout, boolean isSuccessful);

    /**
     * This method will be triggered when the position of the refresh view changes.
     *
     * @param layout    The layout {@link SmoothRefreshLayout}
     * @param status    Current status @see{@link SmoothRefreshLayout#SR_STATUS_INIT},
     *                  {@link SmoothRefreshLayout#SR_STATUS_PREPARE},
     *                  {@link SmoothRefreshLayout#SR_STATUS_REFRESHING},
     *                  {@link SmoothRefreshLayout#SR_STATUS_LOADING_MORE},
     *                  {@link SmoothRefreshLayout#SR_STATUS_COMPLETE}.
     * @param indicator The indicator {@link IIndicator}
     */
    void onRefreshPositionChanged(SmoothRefreshLayout layout, byte status, T indicator);

    /**
     * Before the transaction of the refresh view has not yet been processed completed。
     * This method will be triggered when the position of the other refresh view changes.<br/>
     * <p>
     * Since version 1.4.6
     *
     * @param layout    The layout {@link SmoothRefreshLayout}
     * @param status    Current status @see{@link SmoothRefreshLayout#SR_STATUS_INIT},
     *                  {@link SmoothRefreshLayout#SR_STATUS_PREPARE},
     *                  {@link SmoothRefreshLayout#SR_STATUS_REFRESHING},
     *                  {@link SmoothRefreshLayout#SR_STATUS_LOADING_MORE},
     *                  {@link SmoothRefreshLayout#SR_STATUS_COMPLETE}.
     * @param indicator The indicator {@link IIndicator}
     */
    void onPureScrollPositionChanged(SmoothRefreshLayout layout, byte status, T indicator);

    @IntDef({TYPE_HEADER, TYPE_FOOTER})
    @Retention(RetentionPolicy.SOURCE)
    public @interface RefreshViewType {
    }

    @IntDef({STYLE_DEFAULT, STYLE_SCALE, STYLE_PIN, STYLE_FOLLOW_SCALE, STYLE_FOLLOW_PIN,
            STYLE_FOLLOW_CENTER})
    @Retention(RetentionPolicy.SOURCE)
    public @interface RefreshViewStyle {
    }
}
