package me.dkzwm.smoothrefreshlayout.gesture;

import android.view.MotionEvent;

/**
 * Created by dkzwm on 2017/5/19.
 *
 * @author dkzwm
 */
public interface IGestureDetector {
    void onTouchEvent(MotionEvent ev);

    void onDetached();

    float getFriction();
}
