package me.dkzwm.widget.srl.utils;

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
    private HorizontalScrollCompat() {

    }


    public static boolean canChildScrollLeft(View view) {
        if (Build.VERSION.SDK_INT < 26)
            return ViewCompat.canScrollHorizontally(view, -1);
        else
            return view.canScrollHorizontally(-1);
    }

    public static boolean canChildScrollRight(View view) {
        if (Build.VERSION.SDK_INT < 26)
            return ViewCompat.canScrollHorizontally(view, 1);
        else
            return view.canScrollHorizontally(1);
    }


    public static boolean scrollCompat(View view, float deltaY) {
        try {
            if (view != null) {
                if ((view instanceof WebView)
                        || (view instanceof HorizontalScrollView)) {
                    view.scrollBy((int) deltaY, 0);
                } else {
                    if (view instanceof RecyclerView) {
                        view.scrollBy((int) deltaY, 0);
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            //ignored
        }
        return false;
    }

    public static void flingCompat(View view, int velocityX) {
        if (view instanceof WebView) {
            ((WebView) view).flingScroll(velocityX, 0);
        } else if (view instanceof RecyclerView) {
            ((RecyclerView) view).fling(velocityX, 0);
        } else if (view instanceof HorizontalScrollView) {
            ((HorizontalScrollView) view).fling(velocityX);
        }
    }

    public static boolean canScaleInternal(View view) {
        return view instanceof HorizontalScrollView && ((HorizontalScrollView) view).getChildCount() > 0;
    }

    public static boolean isScrollingView(View view) {
        boolean isScrollingView = (view instanceof HorizontalScrollView
                || view instanceof WebView
                || view instanceof ViewPager);
        if (isScrollingView)
            return true;
        else {
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
            } catch (Exception e) {
                //ignored
            }
        }
        return false;
    }

}
