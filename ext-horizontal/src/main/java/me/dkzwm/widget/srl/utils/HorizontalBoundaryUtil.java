package me.dkzwm.widget.srl.utils;

import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

/**
 * Created by dkzwm on 2017/10/23.
 *
 * @author dkzwm
 */
public class HorizontalBoundaryUtil {
    private HorizontalBoundaryUtil() {
    }

    public static boolean isFingerInsideVerticalView(float rawX, float rawY, @NonNull View view) {
        boolean isHorizontalView = isVerticalView(view);
        if (isHorizontalView) {
            return BoundaryUtil.isInsideView(rawX, rawY, view);
        } else if (view instanceof ViewGroup)
            return isInsideViewGroup(rawX, rawY, (ViewGroup) view);
        return false;
    }

    private static boolean isInsideViewGroup(float rawX, float rawY,
                                             @NonNull ViewGroup group) {
        final int size = group.getChildCount();
        for (int i = 0; i < size; i++) {
            View child = group.getChildAt(i);
            if (child.getVisibility() != View.VISIBLE)
                continue;
            boolean isHorizontalView = isVerticalView(child);
            if (isHorizontalView) {
                boolean isInside = BoundaryUtil.isInsideView(rawX, rawY, child);
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

    private static boolean isVerticalView(View view) {
        if (view instanceof AbsListView || view instanceof ScrollView
                || view instanceof NestedScrollView
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
                        if (linearManager.getOrientation() == RecyclerView.VERTICAL)
                            return true;
                    } else if (manager instanceof StaggeredGridLayoutManager) {
                        StaggeredGridLayoutManager gridLayoutManager = (StaggeredGridLayoutManager) manager;
                        if (gridLayoutManager.getOrientation() == RecyclerView.VERTICAL)
                            return true;
                    }
                }
            }
        } catch (NoClassDefFoundError ignored) {
            //ignored
        }
        return false;
    }

}
