package me.dkzwm.widget.srl.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.ViewTreeObserver;
import android.view.animation.Interpolator;
import android.widget.AbsListView;
import android.widget.Scroller;

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
    private static Field sOnScrollChangedListenersField;
    private static Method sOnScrollChangedListenersRemoveMethod;
    private static Method sOnScrollChangedListenersSizeMethod;
    private static Field sScrollerInterpolatorField;


    /**
     * Safely remove the onScrollChangedListener from target ViewTreeObserver
     */
    public static void safelyRemoveListeners(ViewTreeObserver observer,
                                             ViewTreeObserver.OnScrollChangedListener listener) {
        try {
            if (sOnScrollChangedListenersField == null) {
                sOnScrollChangedListenersField = ViewTreeObserver.class.getDeclaredField
                        ("mOnScrollChangedListeners");
                if (sOnScrollChangedListenersField != null)
                    sOnScrollChangedListenersField.setAccessible(true);
            }
            if (sOnScrollChangedListenersField != null) {
                Object object = sOnScrollChangedListenersField.get(observer);
                if (object != null) {
                    if (sOnScrollChangedListenersRemoveMethod == null) {
                        sOnScrollChangedListenersRemoveMethod = object.getClass().getDeclaredMethod
                                ("remove", Object.class);
                        if (sOnScrollChangedListenersRemoveMethod != null)
                            sOnScrollChangedListenersRemoveMethod.setAccessible(true);
                    }
                    if (sOnScrollChangedListenersRemoveMethod != null) {
                        sOnScrollChangedListenersRemoveMethod.invoke(object, listener);
                    }
                    if (sOnScrollChangedListenersSizeMethod == null) {
                        sOnScrollChangedListenersSizeMethod = object.getClass().getDeclaredMethod
                                ("size");
                        if (sOnScrollChangedListenersSizeMethod != null)
                            sOnScrollChangedListenersSizeMethod.setAccessible(true);
                    }
                    if (sOnScrollChangedListenersSizeMethod != null) {
                        object = sOnScrollChangedListenersSizeMethod.invoke(object);
                        if (object != null && object instanceof Integer) {
                            int size = (int) object;
                            if (size == 0) {
                                sOnScrollChangedListenersField.set(observer, null);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            //ignore exception
        }
    }

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

    public static Scroller setScrollerInterpolatorOrReCreateScroller(Context context,
                                                                     Scroller scroller,
                                                                     Interpolator interpolator) {
        try {
            if (sScrollerInterpolatorField == null) {
                sScrollerInterpolatorField = Scroller.class.getDeclaredField("mInterpolator");
                if (sScrollerInterpolatorField != null)
                    sScrollerInterpolatorField.setAccessible(true);
            }
            if (sScrollerInterpolatorField == null) {
                return new Scroller(context, interpolator);
            } else {
                sScrollerInterpolatorField.set(scroller, interpolator);
                return scroller;
            }
        } catch (Exception e) {
            return new Scroller(context, interpolator);
        }
    }
}
