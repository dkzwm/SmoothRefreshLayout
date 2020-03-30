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
import me.dkzwm.widget.srl.SmoothRefreshLayout;
import me.dkzwm.widget.srl.extra.IRefreshView;

/** @author dkzwm */
public class ViewCatcherUtil {
    private static final String CLASS_NAME_OF_VIEWPAGER = "androidx.viewpager.widget.ViewPager";
    private static final String CLASS_NAME_OF_COORDINATORLAYOUT =
            "androidx.coordinatorlayout.widget.CoordinatorLayout";
    private static final String CLASS_NAME_OF_RECYCLERVIEW =
            "androidx.recyclerview.widget.RecyclerView";
    private static final String CLASS_NAME_OF_APPBARLAYOUT =
            "com.google.android.material.appbar.AppBarLayout";
    private static Class<?> sClassOfCoordinatorLayout;
    private static Class<?> sClassOfAppBarLayout;
    private static Class<?> sClassOfViewPager;
    private static Class<?> sClassOfRecyclerView;
    private static boolean sIsCaughtAppBarLayout = false;
    private static boolean sIsCaughtCoordinatorLayout = false;
    private static boolean sIsCaughtViewPager = false;
    private static boolean sIsCaughtRecyclerView = false;

    public static boolean isViewPager(View view) {
        if (sIsCaughtViewPager && sClassOfViewPager == null) return false;
        sIsCaughtViewPager = true;
        if (sClassOfViewPager == null) {
            try {
                sClassOfViewPager = Class.forName(CLASS_NAME_OF_VIEWPAGER);
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
                sClassOfRecyclerView = Class.forName(CLASS_NAME_OF_RECYCLERVIEW);
            } catch (Exception e) {
                return false;
            }
        }
        return sClassOfRecyclerView.isAssignableFrom(view.getClass());
    }

    public static boolean isCoordinatorLayout(View view) {
        if (view == null) {
            return false;
        }
        if (sIsCaughtCoordinatorLayout && sClassOfCoordinatorLayout == null) {
            return false;
        }
        sIsCaughtCoordinatorLayout = true;
        if (sClassOfCoordinatorLayout == null) {
            try {
                sClassOfCoordinatorLayout = Class.forName(CLASS_NAME_OF_COORDINATORLAYOUT);
            } catch (Exception e) {
                return false;
            }
        }
        return sClassOfCoordinatorLayout.isAssignableFrom(view.getClass());
    }

    public static View catchAppBarLayout(final SmoothRefreshLayout group) {
        if ((sIsCaughtCoordinatorLayout || sIsCaughtAppBarLayout)
                && (sClassOfCoordinatorLayout == null || sClassOfAppBarLayout == null)) {
            return null;
        }
        sIsCaughtCoordinatorLayout = true;
        if (sClassOfCoordinatorLayout == null) {
            try {
                sClassOfCoordinatorLayout = Class.forName(CLASS_NAME_OF_COORDINATORLAYOUT);
            } catch (Exception e) {
                return null;
            }
        }
        sIsCaughtAppBarLayout = true;
        if (sClassOfAppBarLayout == null) {
            try {
                sClassOfAppBarLayout = Class.forName(CLASS_NAME_OF_APPBARLAYOUT);
            } catch (Exception e) {
                return null;
            }
        }
        ViewGroup coordinatorLayout = findChildCoordinatorLayout(group);
        if (coordinatorLayout == null) {
            coordinatorLayout = findParentCoordinatorLayout(group);
        }
        if (coordinatorLayout == null) {
            return null;
        }
        final int count = coordinatorLayout.getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = coordinatorLayout.getChildAt(i);
            if (sClassOfAppBarLayout.isAssignableFrom(child.getClass())) {
                return child;
            }
        }
        return null;
    }

    private static ViewGroup findChildCoordinatorLayout(ViewGroup group) {
        if (sClassOfCoordinatorLayout.isAssignableFrom(group.getClass())) {
            return group;
        }
        final int count = group.getChildCount();
        for (int i = 0; i < count; i++) {
            final View view = group.getChildAt(i);
            if (view instanceof ViewGroup) {
                if (ScrollCompat.isScrollingView(view)) {
                    continue;
                }
                if (view instanceof IRefreshView) {
                    continue;
                }
                ViewGroup layout = findChildCoordinatorLayout((ViewGroup) view);
                if (layout != null) {
                    return layout;
                }
            }
        }
        return null;
    }

    private static ViewGroup findParentCoordinatorLayout(ViewGroup group) {
        ViewParent parent = group.getParent();
        while (parent instanceof ViewGroup) {
            group = (ViewGroup) parent;
            if (group.getId() == android.R.id.content) {
                return null;
            }
            if (ScrollCompat.isScrollingView(group)) {
                return null;
            }
            if (sClassOfCoordinatorLayout.isAssignableFrom(group.getClass())) {
                return group;
            }
            parent = group.getParent();
        }
        return null;
    }
}
