//package me.dkzwm.widget.srl.gesture;
//
//import android.content.Context;
//import android.view.MotionEvent;
//import android.view.VelocityTracker;
//import android.view.ViewConfiguration;
//
///**
// * <p>This is a Part of the code copy</p>
// * Created by dkzwm on 2017/5/19.
// *
// * @author dkzwm
// * @see android.support.v4.view.GestureDetectorCompat
// */
//public class GestureDetector  {
//    private final OnGestureListener mGestureListener;
//
//
//    public GestureDetector(Context context, OnGestureListener listener) {
//        mGestureListener = listener;
//
//    }
//
//    public void onTouchEvent(MotionEvent ev) {
//        final int action = ev.getAction();
//        // Determine focal point
//
//        switch (action & MotionEvent.ACTION_MASK) {
//            case MotionEvent.ACTION_DOWN:
//
//                break;
//            case MotionEvent.ACTION_POINTER_UP:
//
//                break;
//            case MotionEvent.ACTION_UP:
//
//                break;
//            case MotionEvent.ACTION_CANCEL:
//                if (mVelocityTracker != null) {
//                    mVelocityTracker.recycle();
//                    mVelocityTracker = null;
//                }
//                break;
//            default:
//                if (mVelocityTracker != null)
//                    mVelocityTracker.addMovement(ev);
//                break;
//        }
//    }
//}
