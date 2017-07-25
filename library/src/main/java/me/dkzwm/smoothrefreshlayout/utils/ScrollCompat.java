package me.dkzwm.smoothrefreshlayout.utils;

import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.ScrollView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by dkzwm on 2017/5/27.
 *
 * @author dkzwm
 */
public class ScrollCompat {

    public static boolean canChildScrollDown(View view) {
        if (view instanceof AbsListView) {
            final AbsListView absListView = (AbsListView) view;
            if (android.os.Build.VERSION.SDK_INT < 14) {
                return absListView.getChildCount() == 0
                        || absListView.getAdapter() == null
                        || (absListView.getChildCount() > 0
                        && (absListView.getLastVisiblePosition() < absListView.getAdapter().getCount() - 1)
                        || absListView.getChildAt(absListView.getChildCount() - 1).getBottom()
                        > absListView.getHeight() - absListView.getPaddingBottom());
            } else {
                return absListView.getChildCount() == 0 || ViewCompat.canScrollVertically(view, 1);
            }
        } else if (view instanceof ScrollView) {
            final ScrollView scrollView = (ScrollView) view;
            if (android.os.Build.VERSION.SDK_INT < 14) {
                return scrollView.getChildCount() == 0
                        || (scrollView.getChildCount() != 0
                        && scrollView.getScrollY() < scrollView.getChildAt(0).getHeight()
                        - scrollView.getHeight());
            } else {
                return scrollView.getChildCount() == 0 || ViewCompat.canScrollVertically(view, 1);
            }
        } else if (view instanceof RecyclerView) {
            final RecyclerView recyclerView = (RecyclerView) view;
            return recyclerView.getChildCount() == 0 || ViewCompat.canScrollVertically(view, 1);
        } else {
            return ViewCompat.canScrollVertically(view, 1);
        }
    }


    public static boolean canAutoLoadMore(View view) {
        if (view instanceof AbsListView) {
            AbsListView listView = (AbsListView) view;
            final int lastVisiblePosition = listView.getLastVisiblePosition();
            final Adapter adapter = listView.getAdapter();
            return adapter != null && lastVisiblePosition > 0
                    && lastVisiblePosition == adapter.getCount() - 1;
        } else if (view instanceof RecyclerView) {
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
        return false;
    }

    public static boolean canChildScrollUp(View view) {
        if (android.os.Build.VERSION.SDK_INT < 14) {
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
            return ViewCompat.canScrollVertically(view, -1);
        }
    }


    public static boolean scrollCompat(View view, float deltaY) {
        if (view != null) {
            if (view instanceof AbsListView) {
                final AbsListView listView = (AbsListView) view;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    listView.scrollListBy(Math.round(deltaY));
                    return true;
                } else {
                    try {
                        Method method = AbsListView.class.getDeclaredMethod("trackMotionScroll",
                                int.class, int.class);
                        if (method != null) {
                            method.setAccessible(true);
                            method.invoke(listView, -Math.round(deltaY), -Math.round(deltaY));
                        }
                    } catch (NoSuchMethodException e) {
                        return false;
                    } catch (IllegalAccessException e) {
                        return false;
                    } catch (InvocationTargetException e) {
                        return false;
                    }
                }
                return true;
            } else if (view instanceof RecyclerView) {
                final RecyclerView recyclerView = (RecyclerView) view;
                recyclerView.scrollBy(0, Math.round(deltaY));
                return true;
            } else {
                view.scrollBy(0, Math.round(deltaY));
                return true;
            }
        }
        return false;
    }


}
