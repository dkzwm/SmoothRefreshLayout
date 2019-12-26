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
import android.view.View;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.ListView;
import android.widget.ScrollView;
import androidx.core.view.ScrollingView;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

/** @author dkzwm */
public class ScrollCompat {
    public static boolean canAutoLoadMore(View view) {
        if (view instanceof AbsListView) {
            AbsListView listView = (AbsListView) view;
            final int lastVisiblePosition = listView.getLastVisiblePosition();
            final Adapter adapter = listView.getAdapter();
            return adapter != null
                    && adapter.getCount() > 0
                    && lastVisiblePosition >= 0
                    && lastVisiblePosition >= adapter.getCount() - 1;
        } else if (ViewCatcherUtil.isRecyclerView(view)) {
            RecyclerView recyclerView = (RecyclerView) view;
            RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
            if (manager == null) {
                return false;
            }
            int lastVisiblePosition = -1;
            if (manager instanceof LinearLayoutManager) {
                LinearLayoutManager linearManager = ((LinearLayoutManager) manager);
                if (linearManager.getOrientation() == RecyclerView.HORIZONTAL) {
                    return false;
                }
                lastVisiblePosition = linearManager.findLastVisibleItemPosition();
            } else if (manager instanceof StaggeredGridLayoutManager) {
                StaggeredGridLayoutManager gridLayoutManager = (StaggeredGridLayoutManager) manager;
                if (gridLayoutManager.getOrientation() == RecyclerView.HORIZONTAL) {
                    return false;
                }
                int[] lastPositions = new int[gridLayoutManager.getSpanCount()];
                gridLayoutManager.findLastVisibleItemPositions(lastPositions);
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
        if (view instanceof AbsListView) {
            AbsListView listView = (AbsListView) view;
            final int lastVisiblePosition = listView.getLastVisiblePosition();
            final Adapter adapter = listView.getAdapter();
            return adapter != null && lastVisiblePosition == 0;
        } else if (ViewCatcherUtil.isRecyclerView(view)) {
            RecyclerView recyclerView = (RecyclerView) view;
            RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
            if (manager == null) {
                return false;
            }
            int firstVisiblePosition = -1;
            if (manager instanceof LinearLayoutManager) {
                LinearLayoutManager linearManager = ((LinearLayoutManager) manager);
                if (linearManager.getOrientation() == RecyclerView.HORIZONTAL) {
                    return false;
                }
                firstVisiblePosition = linearManager.findFirstVisibleItemPosition();
            } else if (manager instanceof StaggeredGridLayoutManager) {
                StaggeredGridLayoutManager gridLayoutManager = (StaggeredGridLayoutManager) manager;
                if (gridLayoutManager.getOrientation() == RecyclerView.HORIZONTAL) {
                    return false;
                }
                int[] firstPositions = new int[gridLayoutManager.getSpanCount()];
                gridLayoutManager.findFirstVisibleItemPositions(firstPositions);
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

    public static boolean scrollCompat(View view, float deltaY) {
        if (view != null) {
            if (view instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) view;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    absListView.scrollListBy((int) deltaY);
                } else if (absListView instanceof ListView) {
                    // {@link android.support.v4.widget.ListViewCompat#scrollListBy(ListView,
                    // int)}
                    final ListView listView = (ListView) absListView;
                    final int firstPosition = listView.getFirstVisiblePosition();
                    if (firstPosition == ListView.INVALID_POSITION) {
                        return false;
                    }
                    final View firstView = listView.getChildAt(0);
                    if (firstView == null) {
                        return false;
                    }
                    final int newTop = (int) (firstView.getTop() - deltaY);
                    listView.setSelectionFromTop(firstPosition, newTop);
                } else {
                    absListView.smoothScrollBy((int) deltaY, 0);
                }
                return true;
            } else if (view instanceof WebView
                    || view instanceof ScrollView
                    || view instanceof NestedScrollView) {
                view.scrollBy(0, (int) deltaY);
                return true;
            } else if (ViewCatcherUtil.isRecyclerView(view)) {
                RecyclerView recyclerView = (RecyclerView) view;
                if (!(recyclerView.getOnFlingListener() instanceof PagerSnapHelper)) {
                    // Fix the problem of adding new data to RecyclerView while in Fling state,
                    // the new items will continue to Fling
                    if (recyclerView.getScrollState() == RecyclerView.SCROLL_STATE_SETTLING) {
                        recyclerView.stopScroll();
                    }
                    view.scrollBy(0, (int) deltaY);
                }
                return true;
            }
        }
        return false;
    }

    public static boolean canScaleInternal(View view) {
        return view instanceof ScrollView && ((ScrollView) view).getChildCount() > 0
                || view instanceof NestedScrollView
                        && ((NestedScrollView) view).getChildCount() > 0;
    }

    public static boolean isScrollingView(View view) {
        if (view instanceof AbsListView || view instanceof ScrollView || view instanceof WebView) {
            return true;
        }
        if (ViewCatcherUtil.isRecyclerView(view)) {
            RecyclerView recyclerView = (RecyclerView) view;
            RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
            if (manager != null) {
                if (manager instanceof LinearLayoutManager) {
                    LinearLayoutManager linearManager = ((LinearLayoutManager) manager);
                    return linearManager.getOrientation() == RecyclerView.VERTICAL;
                } else if (manager instanceof StaggeredGridLayoutManager) {
                    StaggeredGridLayoutManager gridLayoutManager =
                            (StaggeredGridLayoutManager) manager;
                    return gridLayoutManager.getOrientation() == RecyclerView.VERTICAL;
                }
            }
            return true;
        } else {
            return view instanceof ScrollingView;
        }
    }

    public static void flingCompat(View view, int velocityY) {
        if (view instanceof ScrollView) {
            ScrollView scrollView = (ScrollView) view;
            scrollView.fling(velocityY);
        } else if (view instanceof WebView) {
            WebView webView = (WebView) view;
            webView.flingScroll(0, velocityY);
        } else if (view instanceof NestedScrollView) {
            NestedScrollView scrollView = (NestedScrollView) view;
            scrollView.fling(velocityY);
        } else if (view instanceof AbsListView) {
            AbsListView listView = (AbsListView) view;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                listView.fling(velocityY);
            }
        } else if (ViewCatcherUtil.isRecyclerView(view)) {
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.fling(0, velocityY);
        }
    }

    public static void stopFling(View view) {
        if (view instanceof ScrollView) {
            ScrollView scrollView = (ScrollView) view;
            scrollView.smoothScrollBy(0, 0);
        } else if (view instanceof WebView) {
            WebView webView = (WebView) view;
            webView.flingScroll(0, 0);
        } else if (view instanceof NestedScrollView) {
            NestedScrollView scrollView = (NestedScrollView) view;
            scrollView.smoothScrollBy(0, 0);
        } else if (view instanceof AbsListView) {
            AbsListView listView = (AbsListView) view;
            listView.smoothScrollBy(0, 0);
        } else if (ViewCatcherUtil.isRecyclerView(view)) {
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.stopScroll();
        }
    }
}
