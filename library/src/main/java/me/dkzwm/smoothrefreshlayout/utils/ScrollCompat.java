package me.dkzwm.smoothrefreshlayout.utils;

import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AbsListView;
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
