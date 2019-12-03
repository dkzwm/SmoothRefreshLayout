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
    private static final String sClassNameOfViewPager = "androidx.viewpager.widget.ViewPager";
    private static final String sClassNameOfCoordinatorLayout =
            "androidx.coordinatorlayout.widget.CoordinatorLayout";
    private static final String sClassNameOfRecyclerView =
            "androidx.recyclerview.widget.RecyclerView";
    private static final String sClassNameOfAppBarLayout =
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
                sClassOfViewPager = Class.forName(sClassNameOfViewPager);
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
                sClassOfRecyclerView = Class.forName(sClassNameOfRecyclerView);
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
                sClassOfCoordinatorLayout = Class.forName(sClassNameOfCoordinatorLayout);
            } catch (Exception e) {
                return false;
            }
        }
        return sClassOfCoordinatorLayout.isAssignableFrom(view.getClass());
    }

    public static View catchAppBarLayout(final ViewGroup group) {
        if ((sIsCaughtCoordinatorLayout || sIsCaughtAppBarLayout)
                && (sClassOfCoordinatorLayout == null || sClassOfAppBarLayout == null)) {
            return null;
        }
        sIsCaughtCoordinatorLayout = true;
        if (sClassOfCoordinatorLayout == null) {
            try {
                sClassOfCoordinatorLayout = Class.forName(sClassNameOfCoordinatorLayout);
            } catch (Exception e) {
                return null;
            }
        }
        sIsCaughtAppBarLayout = true;
        if (sClassOfAppBarLayout == null) {
            try {
                sClassOfAppBarLayout = Class.forName(sClassNameOfAppBarLayout);
            } catch (Exception e) {
                return null;
            }
        }
        ViewGroup ignoredViewGroup = null;
        ViewGroup findViewGroup = group;
        while (true) {
            View view = findAppBarLayout(findViewGroup, ignoredViewGroup);
            if (view != null) {
                return view;
            }
            ignoredViewGroup = findViewGroup;
            ViewParent parent = findViewGroup.getParent();
            if (parent instanceof ViewGroup) {
                findViewGroup = (ViewGroup) parent;
                if (findViewGroup.getId() == android.R.id.content) {
                    return null;
                }
                if (isViewPager(findViewGroup) || isRecyclerView(findViewGroup)) {
                    return null;
                }
                continue;
            }
            return null;
        }
    }

    private static View findAppBarLayout(ViewGroup group, ViewGroup ignoredGroup) {
        if (ignoredGroup != null) {
            if (ignoredGroup == group) {
                return null;
            }
        } else if (group instanceof IRefreshView) {
            return null;
        }
        if (sClassOfCoordinatorLayout.isAssignableFrom(group.getClass())) {
            final int count = group.getChildCount();
            for (int i = 0; i < count; i++) {
                final View subView = group.getChildAt(i);
                if (sClassOfAppBarLayout.isAssignableFrom(subView.getClass())) {
                    return subView;
                }
            }
            return null;
        }
        final int count = group.getChildCount();
        for (int i = 0; i < count; i++) {
            final View view = group.getChildAt(i);
            if (view instanceof ViewGroup) {
                View layout = findAppBarLayout((ViewGroup) view, ignoredGroup);
                if (layout != null) {
                    return layout;
                }
            }
        }
        return null;
    }
}
