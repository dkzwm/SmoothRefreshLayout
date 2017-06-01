package me.dkzwm.smoothrefreshlayout.utils;

import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ScrollView;

/**
 * Created by dkzwm on 2017/5/27.
 *
 * @author dkzwm
 */
public class ScrollCompat {
    public static boolean canChildScrollDown(View view) {
        if (view instanceof AbsListView) {
            final AbsListView absListView = (AbsListView) view;
            return absListView.getChildCount() == 0
                    || (absListView.getChildCount() > 0 && (absListView.getLastVisiblePosition()
                    < absListView.getChildCount() - 1)
                    || absListView.getChildAt(absListView.getChildCount() - 1)
                    .getBottom() > absListView.getBottom());
        } else if (view instanceof ScrollView) {
            final ScrollView scrollView = (ScrollView) view;
            return scrollView.getChildCount() == 0
                    || (scrollView.getChildCount() != 0
                    && scrollView.getScrollY() < scrollView.getChildAt(0)
                    .getHeight() - scrollView.getHeight());
        } else if (view instanceof RecyclerView) {
            final RecyclerView recyclerView = (RecyclerView) view;
            return recyclerView.getChildCount() == 0
                    || ViewCompat.canScrollVertically(view, 1);
        }
        return ViewCompat.canScrollVertically(view, 1);
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


}
