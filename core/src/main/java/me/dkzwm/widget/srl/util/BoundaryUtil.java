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
import android.webkit.WebView;
import android.widget.HorizontalScrollView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.viewpager.widget.ViewPager;

/**
 * Created by dkzwm on 2017/9/13.
 *
 * @author dkzwm
 */
public class BoundaryUtil {
    private static final int[] sPoint = new int[2];

    public static boolean isInsideHorizontalView(float rawX, float rawY, @NonNull View view) {
        boolean isHorizontalView = isHorizontalView(view);
        if (isHorizontalView) {
            return isInsideView(rawX, rawY, view);
        } else if (view instanceof ViewGroup)
            return isInsideViewGroup(rawX, rawY, (ViewGroup) view);
        return false;
    }

    public static boolean isInsideView(float rawX, float rawY, @NonNull View view) {
        view.getLocationOnScreen(sPoint);
        return rawX > sPoint[0]
                && rawX < sPoint[0] + view.getWidth()
                && rawY > sPoint[1]
                && rawY < sPoint[1] + view.getHeight();
    }

    private static boolean isInsideViewGroup(float rawX, float rawY, @NonNull ViewGroup group) {
        final int size = group.getChildCount();
        for (int i = 0; i < size; i++) {
            View child = group.getChildAt(i);
            if (child.getVisibility() != View.VISIBLE) continue;
            boolean isHorizontalView = isHorizontalView(child);
            if (isHorizontalView) {
                boolean isInside = isInsideView(rawX, rawY, child);
                if (isInside) return true;
            } else {
                if (child instanceof ViewGroup) {
                    return isInsideViewGroup(rawX, rawY, (ViewGroup) child);
                }
            }
        }
        return false;
    }

    private static boolean isHorizontalView(View view) {
        if (view instanceof ViewPager
                || view instanceof HorizontalScrollView
                || view instanceof WebView) {
            return true;
        } else if (ScrollCompat.isRecyclerView(view)) {
            RecyclerView recyclerView = (RecyclerView) view;
            RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
            if (manager != null) {
                if (manager instanceof LinearLayoutManager) {
                    LinearLayoutManager linearManager = ((LinearLayoutManager) manager);
                    return linearManager.getOrientation() == LinearLayoutManager.HORIZONTAL;
                } else if (manager instanceof StaggeredGridLayoutManager) {
                    StaggeredGridLayoutManager gridLayoutManager =
                            (StaggeredGridLayoutManager) manager;
                    return gridLayoutManager.getOrientation()
                            == StaggeredGridLayoutManager.HORIZONTAL;
                }
            }
        }
        return false;
    }
}
