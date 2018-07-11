package me.dkzwm.widget.srl.utils;

import android.os.Build;
import android.support.v4.view.ScrollingView;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.view.ViewParent;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.ScrollView;

/**
 * Created by dkzwm on 2017/5/27.
 *
 * @author dkzwm
 */
public class ScrollCompat {

    private ScrollCompat() {
    }

    public static boolean canChildScrollDown(View view) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            if (view instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) view;
                return absListView.getChildCount() > 0
                        && (absListView.getLastVisiblePosition() < absListView.getAdapter().getCount() - 1)
                        || absListView.getChildAt(absListView.getChildCount() - 1).getBottom()
                        > absListView.getHeight() - absListView.getPaddingBottom();
            } else if (view instanceof ScrollView) {
                final ScrollView scrollView = (ScrollView) view;
                return scrollView.getChildCount() != 0
                        && scrollView.getScrollY() < scrollView.getChildAt(0).getHeight()
                        - scrollView.getHeight();
            } else {
                return ViewCompat.canScrollVertically(view, 1);
            }
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return ViewCompat.canScrollVertically(view, 1);
        } else {
            return view.canScrollVertically(1);
        }
    }

    public static boolean canAutoLoadMore(View view) {
        if (view instanceof AbsListView) {
            AbsListView listView = (AbsListView) view;
            final int lastVisiblePosition = listView.getLastVisiblePosition();
            final Adapter adapter = listView.getAdapter();
            return adapter != null && adapter.getCount() > 0 && lastVisiblePosition >= 0
                    && lastVisiblePosition >= adapter.getCount() - 1;
        } else if (isRecyclerView(view)) {
            RecyclerView recyclerView = (RecyclerView) view;
            RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
            if (manager == null)
                return false;
            int lastVisiblePosition = 0;
            if (manager instanceof LinearLayoutManager) {
                LinearLayoutManager linearManager = ((LinearLayoutManager) manager);
                if (linearManager.getOrientation() == LinearLayoutManager.HORIZONTAL)
                    return false;
                lastVisiblePosition = linearManager.findLastVisibleItemPosition();
            } else if (manager instanceof StaggeredGridLayoutManager) {
                StaggeredGridLayoutManager gridLayoutManager = (StaggeredGridLayoutManager) manager;
                if (gridLayoutManager.getOrientation() == LinearLayoutManager.HORIZONTAL)
                    return false;
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
            return adapter != null && adapter.getItemCount() > 0 && lastVisiblePosition >= 0
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
        } else if (isRecyclerView(view)) {
            RecyclerView recyclerView = (RecyclerView) view;
            RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
            if (manager == null)
                return false;
            int firstVisiblePosition = -1;
            if (manager instanceof LinearLayoutManager) {
                LinearLayoutManager linearManager = ((LinearLayoutManager) manager);
                if (linearManager.getOrientation() == LinearLayoutManager.HORIZONTAL)
                    return false;
                firstVisiblePosition = linearManager.findFirstVisibleItemPosition();
            } else if (manager instanceof StaggeredGridLayoutManager) {
                StaggeredGridLayoutManager gridLayoutManager = (StaggeredGridLayoutManager) manager;
                if (gridLayoutManager.getOrientation() == LinearLayoutManager.HORIZONTAL)
                    return false;
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

    public static boolean canChildScrollUp(View view) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            if (view instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) view;
                return absListView.getChildCount() > 0
                        && (absListView.getFirstVisiblePosition() > 0
                        || absListView.getChildAt(0).getTop() < absListView.getPaddingTop());
            } else {
                return ViewCompat.canScrollVertically(view, -1)
                        || view.getScrollY() > 0;
            }
        } else {
            return view.canScrollVertically(-1);
        }
    }

    public static boolean scrollCompat(View view, float deltaY) {
        if (view != null) {
            try {
                if (view instanceof AbsListView) {
                    final AbsListView listView = (AbsListView) view;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        listView.scrollListBy((int) deltaY);
                    } else {
                        SRReflectUtil.compatOlderAbsListViewScrollListBy((AbsListView) view,
                                (int) deltaY);
                    }
                    return true;
                } else if (view instanceof WebView
                        || view instanceof ScrollView
                        || view instanceof NestedScrollView
                        || isRecyclerView(view)) {
                    view.scrollBy(0, (int) deltaY);
                    return true;
                }
            } catch (Exception ignored) {
                return false;
            }
        }
        return false;
    }

    public static boolean canScaleInternal(View view) {
        return view instanceof ScrollView && ((ScrollView) view).getChildCount() > 0
                || view instanceof NestedScrollView && ((NestedScrollView) view).getChildCount() > 0;
    }

    public static boolean isScrollingView(View view) {
        return (view instanceof AbsListView
                || view instanceof ScrollView
                || view instanceof ScrollingView
                || view instanceof WebView);
    }

    public static boolean isViewPager(ViewParent parent) {
        return parent != null && parent instanceof ViewPager;
    }

    public static boolean isRecyclerView(View view) {
        try {
            return view != null && view instanceof RecyclerView;
        } catch (NoClassDefFoundError ignored) {
            return false;
        }
    }

    public static void flingCompat(View view, int velocityY) {
        try {
            if (view instanceof ScrollView) {
                ((ScrollView) view).fling(velocityY);
            } else if (view instanceof WebView) {
                ((WebView) view).flingScroll(0, velocityY);
            } else if (isRecyclerView(view)) {
                ((RecyclerView) view).fling(0, velocityY);
            } else if (view instanceof NestedScrollView) {
                ((NestedScrollView) view).fling(velocityY);
            } else if (view instanceof AbsListView) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ((AbsListView) view).fling(velocityY);
                } else {
                    SRReflectUtil.compatOlderAbsListViewFling((AbsListView) view, velocityY);
                }
            }
        } catch (Exception e) {
            //ignored
        }
    }
}
