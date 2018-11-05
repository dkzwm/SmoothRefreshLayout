package me.dkzwm.widget.srl.config;

/**
 * Created by dkzwm on 2018/2/27.
 *
 * @author dkzwm
 */
public interface Constants {
    int MODE_DEFAULT = 0;
    int MODE_SCALE = 1;

    int ACTION_NOTIFY = 0;
    int ACTION_AT_ONCE = 1;
    int ACTION_NOTHING = 2;

    int MOVING_CONTENT = 0;
    int MOVING_FOOTER = 1;
    int MOVING_HEADER = 2;

    byte SCROLLER_MODE_NONE = -1;
    byte SCROLLER_MODE_PRE_FLING = 0;
    byte SCROLLER_MODE_FLING = 1;
    byte SCROLLER_MODE_FLING_BACK = 2;
    byte SCROLLER_MODE_SPRING = 3;
    byte SCROLLER_MODE_SPRING_BACK = 4;
}
