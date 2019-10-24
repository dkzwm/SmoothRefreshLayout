/*
 * MIT License
 *
 * Copyright (c) 2017 dkzwm
 * Copyright (c) 2015 liaohuqiu.net
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package me.dkzwm.widget.srl.util;

import android.widget.AbsListView;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/** @author dkzwm */
public class SRReflectUtil {
    private static Class sFlingRunnableClass;
    private static Field sFlingRunnableField;
    private static Method sReportScrollStateChangeMethod;
    private static Method sFlingRunnableStartMethod;
    private static Constructor sFlingRunnableConstructor;
    private static boolean sFound = false;

    @SuppressWarnings("unchecked")
    public static void compatOlderAbsListViewFling(AbsListView view, int velocityY) {
        if (sFound) {
            return;
        }
        if (sFlingRunnableClass == null) {
            Class<?>[] clazz = AbsListView.class.getDeclaredClasses();
            for (Class c : clazz) {
                if (c.getCanonicalName() != null
                        && c.getCanonicalName().endsWith("FlingRunnable")) {
                    sFlingRunnableClass = c;
                    break;
                }
            }
        }
        sFound = true;
        if (sFlingRunnableClass == null) {
            return;
        }
        try {
            if (sFlingRunnableField == null) {
                sFlingRunnableField = AbsListView.class.getDeclaredField("mFlingRunnable");
                sFlingRunnableField.setAccessible(true);
            }
            if (sFlingRunnableField == null) {
                return;
            }
            Object obj = sFlingRunnableField.get(view);
            if (obj == null) {
                if (sFlingRunnableConstructor == null) {
                    sFlingRunnableConstructor =
                            sFlingRunnableClass.getDeclaredConstructor(AbsListView.class);
                    sFlingRunnableConstructor.setAccessible(true);
                }
                if (sFlingRunnableConstructor == null) {
                    return;
                }
                obj = sFlingRunnableConstructor.newInstance(view);
            }
            sFlingRunnableField.set(view, obj);
            if (sReportScrollStateChangeMethod == null) {
                sReportScrollStateChangeMethod =
                        AbsListView.class.getDeclaredMethod("reportScrollStateChange", int.class);
                sReportScrollStateChangeMethod.setAccessible(true);
            }
            if (sReportScrollStateChangeMethod == null) {
                return;
            }
            sReportScrollStateChangeMethod.invoke(
                    view, AbsListView.OnScrollListener.SCROLL_STATE_FLING);
            if (sFlingRunnableStartMethod == null) {
                sFlingRunnableStartMethod =
                        sFlingRunnableClass.getDeclaredMethod("start", int.class);
                sFlingRunnableStartMethod.setAccessible(true);
            }
            if (sFlingRunnableStartMethod == null) {
                return;
            }
            sFlingRunnableStartMethod.invoke(obj, velocityY);
        } catch (Exception e) {
            // ignore exception
        }
    }
}
