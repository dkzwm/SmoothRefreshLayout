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

import android.view.View;
import android.view.ViewGroup;

/**
 * Created by dkzwm on 2017/12/18. 视图搜寻工具
 *
 * @author dkzwm
 */
public class ViewCatcherUtil {
    public static View catchAppBarLayout(ViewGroup group) {
        try {
            Class<?> classOfCoordinatorLayout =
                    Class.forName("androidx.coordinatorlayout.widget.CoordinatorLayout");
            Class<?> classOfAppBarLayout =
                    Class.forName("com.google.android.material.appbar.AppBarLayout");
            return findAppBarLayout(group, classOfCoordinatorLayout, classOfAppBarLayout);
        } catch (Exception e) {
            return null;
        }
    }

    private static View findAppBarLayout(
            ViewGroup group, Class<?> classOfCoordinatorLayout, Class<?> classOfAppBarLayout) {
        if (classOfCoordinatorLayout.isAssignableFrom(group.getClass())) {
            final int count = group.getChildCount();
            for (int i = 0; i < count; i++) {
                final View subView = group.getChildAt(i);
                if (classOfAppBarLayout.isAssignableFrom(subView.getClass())) {
                    return subView;
                }
            }
            return null;
        }
        final int count = group.getChildCount();
        for (int i = 0; i < count; i++) {
            final View view = group.getChildAt(i);
            if (view instanceof ViewGroup) {
                View layout =
                        findAppBarLayout(
                                (ViewGroup) view, classOfCoordinatorLayout, classOfAppBarLayout);
                if (layout != null) return layout;
            }
        }
        return null;
    }
}
