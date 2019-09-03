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

import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.webkit.WebView;
import android.widget.HorizontalScrollView;

/**
 * Created by dkzwm on 2017/10/25.
 *
 * @author dkzwm
 */
public class HorizontalScrollCompat {
    public static boolean canChildScrollLeft(View view) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            return ViewCompat.canScrollHorizontally(view, -1);
        else return view.canScrollHorizontally(-1);
    }

    public static boolean canChildScrollRight(View view) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            return ViewCompat.canScrollHorizontally(view, 1);
        else return view.canScrollHorizontally(1);
    }

    public static boolean scrollCompat(View view, float deltaY) {
        if (view != null) {
            try {
                if (view instanceof WebView
                        || view instanceof HorizontalScrollView
                        || ScrollCompat.isRecyclerView(view)) {
                    view.scrollBy((int) deltaY, 0);
                    return true;
                }
            } catch (Exception e) {
                // ignored
            }
        }
        return false;
    }

    public static void flingCompat(View view, int velocityX) {
        if (view instanceof WebView) {
            ((WebView) view).flingScroll(velocityX, 0);
        } else if (ScrollCompat.isRecyclerView(view)) {
            ((RecyclerView) view).fling(velocityX, 0);
        } else if (view instanceof HorizontalScrollView) {
            ((HorizontalScrollView) view).fling(velocityX);
        }
    }

    public static boolean canScaleInternal(View view) {
        return view instanceof HorizontalScrollView
                && ((HorizontalScrollView) view).getChildCount() > 0;
    }

    public static boolean isScrollingView(View view) {
        if (view instanceof HorizontalScrollView
                || view instanceof WebView
                || view instanceof ViewPager) return true;
        else if (ScrollCompat.isRecyclerView(view)) {
            RecyclerView recyclerView = (RecyclerView) view;
            RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
            if (manager != null) {
                if (manager instanceof LinearLayoutManager) {
                    LinearLayoutManager linearManager = ((LinearLayoutManager) manager);
                    return linearManager.getOrientation() == RecyclerView.HORIZONTAL;
                } else if (manager instanceof StaggeredGridLayoutManager) {
                    StaggeredGridLayoutManager gridLayoutManager =
                            (StaggeredGridLayoutManager) manager;
                    return gridLayoutManager.getOrientation() == RecyclerView.HORIZONTAL;
                }
            }
        }
        return false;
    }

    public static boolean canAutoLoadMore(View view) {
        if (ScrollCompat.isRecyclerView(view)) {
            RecyclerView recyclerView = (RecyclerView) view;
            RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
            if (manager == null) return false;
            int lastVisiblePosition = 0;
            if (manager instanceof LinearLayoutManager) {
                LinearLayoutManager linearManager = ((LinearLayoutManager) manager);
                if (linearManager.getOrientation() != RecyclerView.HORIZONTAL) return false;
                lastVisiblePosition = linearManager.findLastVisibleItemPosition();
            } else if (manager instanceof StaggeredGridLayoutManager) {
                StaggeredGridLayoutManager gridLayoutManager = (StaggeredGridLayoutManager) manager;
                if (gridLayoutManager.getOrientation() != RecyclerView.HORIZONTAL) return false;
                int[] lastPositions = new int[gridLayoutManager.getSpanCount()];
                gridLayoutManager.findLastVisibleItemPositions(lastPositions);
                lastVisiblePosition = lastPositions[0];
                for (int value : lastPositions) {
                    if (value > lastVisiblePosition) {
                        lastVisiblePosition = value;
                    }
                }
            }
            RecyclerView.Adapter adapter = recyclerView.getAdapter();
            return adapter != null
                    && adapter.getItemCount() > 0
                    && lastVisiblePosition >= 0
                    && lastVisiblePosition >= adapter.getItemCount() - 1;
        }
        return false;
    }

    public static boolean canAutoRefresh(View view) {
        if (ScrollCompat.isRecyclerView(view)) {
            RecyclerView recyclerView = (RecyclerView) view;
            RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
            if (manager == null) return false;
            int firstVisiblePosition = -1;
            if (manager instanceof LinearLayoutManager) {
                LinearLayoutManager linearManager = ((LinearLayoutManager) manager);
                if (linearManager.getOrientation() != RecyclerView.HORIZONTAL) return false;
                firstVisiblePosition = linearManager.findFirstVisibleItemPosition();
            } else if (manager instanceof StaggeredGridLayoutManager) {
                StaggeredGridLayoutManager gridLayoutManager = (StaggeredGridLayoutManager) manager;
                if (gridLayoutManager.getOrientation() != RecyclerView.HORIZONTAL) return false;
                int[] firstPositions = new int[gridLayoutManager.getSpanCount()];
                gridLayoutManager.findFirstVisibleItemPositions(firstPositions);
                firstVisiblePosition = firstPositions[0];
                for (int value : firstPositions) {
                    if (value == 0) {
                        firstVisiblePosition = 0;
                        break;
                    }
                }
            }
            RecyclerView.Adapter adapter = recyclerView.getAdapter();
            return adapter != null && firstVisiblePosition == 0;
        }
        return false;
    }
}
