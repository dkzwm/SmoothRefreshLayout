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

import android.support.annotation.NonNull;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.ScrollView;

/**
 * Created by dkzwm on 2017/10/23.
 *
 * @author dkzwm
 */
public class HorizontalBoundaryUtil {
    public static boolean isInsideVerticalView(float rawX, float rawY, @NonNull View view) {
        boolean isHorizontalView = isVerticalView(view);
        if (isHorizontalView) {
            return BoundaryUtil.isInsideView(rawX, rawY, view);
        } else if (view instanceof ViewGroup)
            return isInsideViewGroup(rawX, rawY, (ViewGroup) view);
        return false;
    }

    private static boolean isInsideViewGroup(float rawX, float rawY, @NonNull ViewGroup group) {
        final int size = group.getChildCount();
        for (int i = 0; i < size; i++) {
            View child = group.getChildAt(i);
            if (child.getVisibility() != View.VISIBLE) continue;
            boolean isHorizontalView = isVerticalView(child);
            if (isHorizontalView) {
                boolean isInside = BoundaryUtil.isInsideView(rawX, rawY, child);
                if (isInside) return true;
            } else {
                if (child instanceof ViewGroup) {
                    return isInsideViewGroup(rawX, rawY, (ViewGroup) child);
                }
            }
        }
        return false;
    }

    private static boolean isVerticalView(View view) {
        if (view instanceof AbsListView
                || view instanceof ScrollView
                || view instanceof NestedScrollView
                || view instanceof WebView) {
            return true;
        } else if (ScrollCompat.isRecyclerView(view)) {
            RecyclerView recyclerView = (RecyclerView) view;
            RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
            if (manager != null) {
                if (manager instanceof LinearLayoutManager) {
                    LinearLayoutManager linearManager = ((LinearLayoutManager) manager);
                    return linearManager.getOrientation() == LinearLayoutManager.VERTICAL;
                } else if (manager instanceof StaggeredGridLayoutManager) {
                    StaggeredGridLayoutManager gridLayoutManager =
                            (StaggeredGridLayoutManager) manager;
                    return gridLayoutManager.getOrientation()
                            == StaggeredGridLayoutManager.VERTICAL;
                }
            }
        }
        return false;
    }
}
