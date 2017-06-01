package me.dkzwm.smoothrefreshlayout.gesture;

import android.view.MotionEvent;

/**
 * Created by dkzwm on 2017/5/24.
 *
 * @author dkzwm
 */
public interface OnGestureListener {
    void onDown(MotionEvent ev);

    void onFling(MotionEvent last, MotionEvent current, float vx, float vy);
}
