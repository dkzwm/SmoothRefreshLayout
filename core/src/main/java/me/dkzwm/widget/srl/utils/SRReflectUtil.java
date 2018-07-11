package me.dkzwm.widget.srl.utils;

import android.annotation.SuppressLint;
import android.widget.AbsListView;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by dkzwm on 2017/12/19.
 *
 * @author dkzwm
 */
public class SRReflectUtil {
    private static Class sFlingRunnableClass;
    private static Field sFlingRunnableField;
    private static Method sReportScrollStateChangeMethod;
    private static Method sFlingRunnableStartMethod;
    private static Constructor sFlingRunnableConstructor;
    private static Method sTrackMotionScrollMethod;

    @SuppressLint("PrivateApi")
    @SuppressWarnings("unchecked")
    static void compatOlderAbsListViewFling(AbsListView view, int velocityY) {
        if (sFlingRunnableClass == null) {
            Class<?>[] clazz = AbsListView.class.getDeclaredClasses();
            for (Class c : clazz) {
                if (c.getCanonicalName().endsWith("FlingRunnable")) {
                    sFlingRunnableClass = c;
                    break;
                }
            }
        }
        if (sFlingRunnableClass == null)
            return;
        try {
            if (sFlingRunnableField == null) {
                sFlingRunnableField = AbsListView.class.getDeclaredField("mFlingRunnable");
                if (sFlingRunnableField != null)
                    sFlingRunnableField.setAccessible(true);
            }
            if (sFlingRunnableField == null)
                return;
            Object obj = sFlingRunnableField.get(view);
            if (obj == null) {
                if (sFlingRunnableConstructor == null) {
                    sFlingRunnableConstructor = sFlingRunnableClass.getDeclaredConstructor
                            (AbsListView.class);
                    if (sFlingRunnableConstructor != null)
                        sFlingRunnableConstructor.setAccessible(true);
                }
                if (sFlingRunnableConstructor == null)
                    return;
                obj = sFlingRunnableConstructor.newInstance(view);
            }
            sFlingRunnableField.set(view, obj);
            if (sReportScrollStateChangeMethod == null) {
                sReportScrollStateChangeMethod = AbsListView.class.getDeclaredMethod
                        ("reportScrollStateChange", int.class);
                if (sReportScrollStateChangeMethod != null)
                    sReportScrollStateChangeMethod.setAccessible(true);
            }
            if (sReportScrollStateChangeMethod == null)
                return;
            sReportScrollStateChangeMethod.invoke(view, AbsListView.OnScrollListener
                    .SCROLL_STATE_FLING);
            if (sFlingRunnableStartMethod == null) {
                sFlingRunnableStartMethod = sFlingRunnableClass.getDeclaredMethod("start",
                        int.class);
                if (sFlingRunnableStartMethod != null)
                    sFlingRunnableStartMethod.setAccessible(true);
            }
            if (sFlingRunnableStartMethod == null)
                return;
            sFlingRunnableStartMethod.invoke(obj, velocityY);
        } catch (Exception e) {
            //ignore exception
        }
    }

    @SuppressLint("PrivateApi")
    static void compatOlderAbsListViewScrollListBy(AbsListView view, int delta) {
        try {
            if (sTrackMotionScrollMethod == null) {
                sTrackMotionScrollMethod = AbsListView.class.getDeclaredMethod("trackMotionScroll",
                        int.class, int.class);
                sTrackMotionScrollMethod.setAccessible(true);
            }
            if (sTrackMotionScrollMethod != null) {
                sTrackMotionScrollMethod.invoke(view, -delta, -delta);
            }
        } catch (Exception e) {
            //ignore exception
        }
    }
}
