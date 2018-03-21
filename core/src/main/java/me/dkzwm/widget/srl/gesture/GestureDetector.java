package me.dkzwm.widget.srl.gesture;

import android.content.Context;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;

/**
 * <p>This is a Part of the code copy</p>
 * Created by dkzwm on 2017/5/19.
 *
 * @author dkzwm
 * @see android.support.v4.view.GestureDetectorCompat
 */
public class GestureDetector  {
    private final OnGestureListener mGestureListener;
    private final int mMaximumFlingVelocity;
    private final int mMinimumFlingVelocity;
    private VelocityTracker mVelocityTracker;

    public GestureDetector(Context context, OnGestureListener listener) {
        mGestureListener = listener;
        ViewConfiguration configuration = ViewConfiguration.get(context);
        mMaximumFlingVelocity = configuration.getScaledMaximumFlingVelocity();
        mMinimumFlingVelocity = configuration.getScaledMinimumFlingVelocity();
    }

    public void onTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        // Determine focal point
        final int count = ev.getPointerCount();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                if (mVelocityTracker != null)
                    mVelocityTracker.recycle();
                mVelocityTracker = VelocityTracker.obtain();
                mVelocityTracker.addMovement(ev);
                break;
            case MotionEvent.ACTION_POINTER_UP:
                if (mVelocityTracker != null) {
                    mVelocityTracker.addMovement(ev);
                    // Check the dot product of current velocities.
                    // If the pointer that left was opposing another velocity vector, clear.
                    mVelocityTracker.computeCurrentVelocity(1000, mMaximumFlingVelocity);
                    final int upIndex = ev.getActionIndex();
                    final int id1 = ev.getPointerId(upIndex);
                    final float x1 = mVelocityTracker.getXVelocity(id1);
                    final float y1 = mVelocityTracker.getYVelocity(id1);
                    for (int i = 0; i < count; i++) {
                        if (i == upIndex) continue;

                        final int id2 = ev.getPointerId(i);
                        final float x = x1 * mVelocityTracker.getXVelocity(id2);
                        final float y = y1 * mVelocityTracker.getYVelocity(id2);

                        final float dot = x + y;
                        if (dot < 0) {
                            mVelocityTracker.clear();
                            break;
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mVelocityTracker != null) {
                    mVelocityTracker.addMovement(ev);
                    final int pointerId = ev.getPointerId(0);
                    mVelocityTracker.computeCurrentVelocity(1000, mMaximumFlingVelocity);
                    float vy = mVelocityTracker.getYVelocity(pointerId);
                    float vx = mVelocityTracker.getXVelocity(pointerId);
                    if (Math.abs(vx) >= mMinimumFlingVelocity
                            || Math.abs(vy) >= mMinimumFlingVelocity) {
                        mGestureListener.onFling(vx, vy);
                    }
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                break;
            default:
                if (mVelocityTracker != null)
                    mVelocityTracker.addMovement(ev);
                break;
        }
    }
}
