package me.dkzwm.widget.srl.utils;

import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.HorizontalScrollView;

/**
 * Created by dkzwm on 2017/9/13.
 *
 * @author dkzwm
 */

public class BoundaryUtil {
    private BoundaryUtil() {
    }

    public static boolean isInsideHorizontalView(float rawX, float rawY, @NonNull View view) {
        boolean isHorizontalView = isHorizontalView(view);
        if (isHorizontalView) {
            return isInsideView(rawX, rawY, view);
        } else if (view instanceof ViewGroup)
            return isInsideViewGroup(rawX, rawY, (ViewGroup) view);
        return false;
    }

    public static boolean isInsideView(float rawX, float rawY, @NonNull View view) {
        int[] point = new int[2];
        view.getLocationOnScreen(point);
        return rawX > point[0] && rawX < point[0] + view.getWidth()
                && rawY > point[1] && rawY < point[1] + view.getHeight();
    }

    private static boolean isInsideViewGroup(float rawX, float rawY,
                                             @NonNull ViewGroup group) {
        final int size = group.getChildCount();
        for (int i = 0; i < size; i++) {
            View child = group.getChildAt(i);
            if (child.getVisibility() != View.VISIBLE)
                continue;
            boolean isHorizontalView = isHorizontalView(child);
            if (isHorizontalView) {
                boolean isInside = isInsideView(rawX, rawY, child);
                if (isInside)
                    return true;
            } else {
                if (child instanceof ViewGroup) {
                    return isInsideViewGroup(rawX, rawY, (ViewGroup) child);
                }
            }
        }
        return false;
    }

    private static boolean isHorizontalView(View view) {
        if (view instanceof ViewPager || view instanceof HorizontalScrollView
                || view instanceof WebView) {
            return true;
        }
        try {
            if (view instanceof RecyclerView) {
                RecyclerView recyclerView = (RecyclerView) view;
                RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
                if (manager != null) {
                    if (manager instanceof LinearLayoutManager) {
                        LinearLayoutManager linearManager = ((LinearLayoutManager) manager);
                        if (linearManager.getOrientation() == LinearLayoutManager.HORIZONTAL)
                            return true;
                    } else if (manager instanceof StaggeredGridLayoutManager) {
                        StaggeredGridLayoutManager gridLayoutManager = (StaggeredGridLayoutManager) manager;
                        if (gridLayoutManager.getOrientation() == StaggeredGridLayoutManager.HORIZONTAL)
                            return true;
                    }
                }
            }
        } catch (NoClassDefFoundError e) {
            e.printStackTrace();
        }
        return false;
    }
}
