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
import android.view.ViewParent;
import me.dkzwm.widget.srl.extra.IRefreshView;

/** @author dkzwm */
public class ViewCatcherUtil {
    private static Class<?> sClassOfCoordinatorLayout;
    private static Class<?> sClassOfAppBarLayout;
    private static Class<?> sClassOfViewPager;
    private static Class<?> sClassOfRecyclerView;
    private static boolean sIsCaughtAppBarLayout = false;
    private static boolean sIsCaughtViewPager = false;
    private static boolean sIsCaughtRecyclerView = false;

    public static boolean isViewPager(View view) {
        if (sIsCaughtViewPager && sClassOfViewPager == null) return false;
        sIsCaughtViewPager = true;
        if (sClassOfViewPager == null) {
            try {
                sClassOfViewPager = Class.forName("androidx.viewpager.widget.ViewPager");
            } catch (Exception e) {
                return false;
            }
        }
        return sClassOfViewPager.isAssignableFrom(view.getClass());
    }

    public static boolean isRecyclerView(View view) {
        if (sIsCaughtRecyclerView && sClassOfRecyclerView == null) return false;
        sIsCaughtRecyclerView = true;
        if (sClassOfRecyclerView == null) {
            try {
                sClassOfRecyclerView = Class.forName("androidx.recyclerview.widget.RecyclerView");
            } catch (Exception e) {
                return false;
            }
        }
        return sClassOfRecyclerView.isAssignableFrom(view.getClass());
    }

    public static View catchAppBarLayout(ViewGroup group) {
        if (sIsCaughtAppBarLayout
                && (sClassOfCoordinatorLayout == null || sClassOfAppBarLayout == null)) return null;
        sIsCaughtAppBarLayout = true;
        try {
            if (sClassOfCoordinatorLayout == null) {
                sClassOfCoordinatorLayout =
                        Class.forName("androidx.coordinatorlayout.widget.CoordinatorLayout");
            }
            if (sClassOfAppBarLayout == null) {
                sClassOfAppBarLayout =
                        Class.forName("com.google.android.material.appbar.AppBarLayout");
            }
        } catch (Exception e) {
            return null;
        }
        while (true) {
            ViewParent parent = group.getParent();
            if (parent instanceof ViewGroup) {
                group = (ViewGroup) parent;
                if (group.getId() == android.R.id.content) {
                    break;
                }
                continue;
            }
            break;
        }
        return findAppBarLayout(group, sClassOfCoordinatorLayout, sClassOfAppBarLayout);
    }

    private static View findAppBarLayout(
            ViewGroup group, Class<?> classOfCoordinatorLayout, Class<?> classOfAppBarLayout) {
        if (group instanceof IRefreshView) {
            return null;
        }
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
