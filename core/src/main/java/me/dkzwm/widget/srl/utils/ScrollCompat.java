package me.dkzwm.widget.srl.utils;

import android.annotation.SuppressLint;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.ScrollView;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by dkzwm on 2017/5/27.
 *
 * @author dkzwm
 */
public class ScrollCompat {

    private ScrollCompat() {
    }

    public static boolean canChildScrollDown(View view) {
        if (view instanceof AbsListView) {
            final AbsListView absListView = (AbsListView) view;
            if (Build.VERSION.SDK_INT < 14) {
                return absListView.getChildCount() == 0
                        || absListView.getAdapter() == null
                        || (absListView.getChildCount() > 0
                        && (absListView.getLastVisiblePosition() < absListView.getAdapter().getCount() - 1)
                        || absListView.getChildAt(absListView.getChildCount() - 1).getBottom()
                        > absListView.getHeight() - absListView.getPaddingBottom());
            } else {
                if (Build.VERSION.SDK_INT < 26)
                    return absListView.getChildCount() == 0 ||
                            ViewCompat.canScrollVertically(view, 1);
                else
                    return absListView.getChildCount() == 0 ||
                            view.canScrollVertically(1);
            }
        } else if (view instanceof ScrollView) {
            final ScrollView scrollView = (ScrollView) view;
            if (Build.VERSION.SDK_INT < 14) {
                return scrollView.getChildCount() == 0
                        || (scrollView.getChildCount() != 0
                        && scrollView.getScrollY() < scrollView.getChildAt(0).getHeight()
                        - scrollView.getHeight());
            } else {
                if (Build.VERSION.SDK_INT < 26)
                    return scrollView.getChildCount() == 0 ||
                            ViewCompat.canScrollVertically(view, 1);
                else
                    return scrollView.getChildCount() == 0 ||
                            view.canScrollVertically(1);
            }
        } else {
            try {
                if (view instanceof RecyclerView) {
                    final RecyclerView recyclerView = (RecyclerView) view;
                    if (Build.VERSION.SDK_INT < 26)
                        return recyclerView.getChildCount() == 0 ||
                                ViewCompat.canScrollVertically(view, 1);
                    else
                        return recyclerView.getChildCount() == 0 ||
                                view.canScrollVertically(1);
                }
            } catch (NoClassDefFoundError e) {
                e.printStackTrace();
            }
            if (Build.VERSION.SDK_INT < 26)
                return ViewCompat.canScrollVertically(view, 1);
            else
                return view.canScrollVertically(1);
        }
    }

    public static boolean canAutoLoadMore(View view) {
        if (view instanceof AbsListView) {
            AbsListView listView = (AbsListView) view;
            final int lastVisiblePosition = listView.getLastVisiblePosition();
            final Adapter adapter = listView.getAdapter();
            return adapter != null && lastVisiblePosition > 0
                    && lastVisiblePosition >= adapter.getCount() - 1;
        } else {
            try {
                if (view instanceof RecyclerView) {
                    RecyclerView recyclerView = (RecyclerView) view;
                    RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
                    if (manager == null)
                        return false;
                    int lastVisiblePosition = 0;
                    if (manager instanceof LinearLayoutManager) {
                        LinearLayoutManager linearManager = ((LinearLayoutManager) manager);
                        lastVisiblePosition = linearManager.findLastVisibleItemPosition();
                    } else if (manager instanceof StaggeredGridLayoutManager) {
                        StaggeredGridLayoutManager gridLayoutManager = (StaggeredGridLayoutManager) manager;
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
                    return adapter != null && lastVisiblePosition > 0
                            && lastVisiblePosition >= adapter.getItemCount() - 1;
                }
            } catch (NoClassDefFoundError e) {
                e.printStackTrace();
            }
            return false;
        }
    }

    public static boolean canAutoRefresh(View view) {
        if (view instanceof AbsListView) {
            AbsListView listView = (AbsListView) view;
            final int lastVisiblePosition = listView.getLastVisiblePosition();
            final Adapter adapter = listView.getAdapter();
            return adapter != null && lastVisiblePosition == 0;
        } else {
            try {
                if (view instanceof RecyclerView) {
                    RecyclerView recyclerView = (RecyclerView) view;
                    RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
                    if (manager == null)
                        return false;
                    int firstVisiblePosition = -1;
                    if (manager instanceof LinearLayoutManager) {
                        LinearLayoutManager linearManager = ((LinearLayoutManager) manager);
                        firstVisiblePosition = linearManager.findFirstVisibleItemPosition();
                    } else if (manager instanceof StaggeredGridLayoutManager) {
                        StaggeredGridLayoutManager gridLayoutManager = (StaggeredGridLayoutManager) manager;
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
            } catch (NoClassDefFoundError e) {
                e.printStackTrace();
            }
            return false;
        }
    }

    public static boolean canChildScrollUp(View view) {
        if (Build.VERSION.SDK_INT < 14) {
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
            if (Build.VERSION.SDK_INT < 26)
                return ViewCompat.canScrollVertically(view, -1);
            else
                return view.canScrollVertically(-1);
        }
    }

    public static boolean scrollCompat(View view, float deltaY) {
        if (view != null) {
            if (view instanceof AbsListView) {
                final AbsListView listView = (AbsListView) view;
                if (Build.VERSION.SDK_INT >= 19) {
                    listView.scrollListBy((int) deltaY);
                    return true;
                } else {
                    try {
                        @SuppressLint("PrivateApi")
                        Method method = AbsListView.class.getDeclaredMethod("trackMotionScroll",
                                int.class, int.class);
                        if (method != null) {
                            method.setAccessible(true);
                            method.invoke(listView, -(int) deltaY, -(int) deltaY);
                        }
                    } catch (Exception e) {
                        return false;
                    }
                }
                return true;
            } else if ((view instanceof WebView)
                    || (view instanceof ScrollView)
                    || (view instanceof NestedScrollView)) {
                view.scrollBy(0, (int) deltaY);
            } else {
                try {
                    if (view instanceof RecyclerView) {
                        view.scrollBy(0, (int) deltaY);
                        return true;
                    }
                } catch (NoClassDefFoundError e) {
                    //ignore exception
                }
            }
        }
        return false;
    }

    public static void flingCompat(View view, int velocityY) {
        if (view instanceof ScrollView) {
            ((ScrollView) view).fling(velocityY);
        } else if (view instanceof WebView) {
            ((WebView) view).flingScroll(0, velocityY);
        } else if (view instanceof RecyclerView) {
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
    }
}
