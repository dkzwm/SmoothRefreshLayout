package me.dkzwm.widget.srl.utils;

import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.view.View;
import android.view.ViewGroup;

import me.dkzwm.widget.srl.ILifecycleObserver;
import me.dkzwm.widget.srl.SmoothRefreshLayout;
import me.dkzwm.widget.srl.extra.IRefreshView;

/**
 * Created by dkzwm on 2017/12/18.
 *
 * @author dkzwm
 */
public class AppBarUtil implements ILifecycleObserver, AppBarLayout.OnOffsetChangedListener
        , SmoothRefreshLayout.OnHeaderEdgeDetectCallBack
        , SmoothRefreshLayout.OnFooterEdgeDetectCallBack {
    private boolean mFullyExpanded;
    private boolean mFullCollapsed;
    private boolean mFound = false;

    @Override
    public void onAttached(SmoothRefreshLayout layout) {
        try {
            AppBarLayout appBarLayout = findAppBarLayout(layout);
            if (appBarLayout == null)
                return;
            appBarLayout.addOnOffsetChangedListener(this);
            mFound = true;
        } catch (Exception e) {
            //ignored
            mFound = false;
        }
    }

    @Override
    public void onDetached(SmoothRefreshLayout layout) {
        try {
            AppBarLayout appBarLayout = findAppBarLayout(layout);
            if (appBarLayout == null)
                return;
            appBarLayout.removeOnOffsetChangedListener(this);
        } catch (Exception e) {
            //ignored
        }
        mFound = false;
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        mFullyExpanded = verticalOffset >= 0;
        mFullCollapsed = appBarLayout.getTotalScrollRange() + verticalOffset <= 0;
    }

    private AppBarLayout findAppBarLayout(ViewGroup group) {
        AppBarLayout bar = null;
        final int count = group.getChildCount();
        for (int i = 0; i < count; i++) {
            final View view = group.getChildAt(i);
            if (view instanceof CoordinatorLayout) {
                CoordinatorLayout layout = (CoordinatorLayout) view;
                final int subCount = layout.getChildCount();
                for (int j = 0; j < subCount; j++) {
                    final View subView = layout.getChildAt(j);
                    if (subView instanceof AppBarLayout) {
                        bar = (AppBarLayout) subView;
                        break;
                    }
                }
            }
        }
        return bar;
    }

    @Override
    public boolean isNotYetInEdgeCannotMoveHeader(SmoothRefreshLayout parent,
                                                  @Nullable View child,
                                                  @Nullable IRefreshView header) {
        View targetView = parent.getScrollTargetView();
        if (targetView == null)
            targetView = child;
        return !mFullyExpanded || ScrollCompat.canChildScrollUp(targetView);
    }

    @Override
    public boolean isNotYetInEdgeCannotMoveFooter(SmoothRefreshLayout parent,
                                                  @Nullable View child,
                                                  @Nullable IRefreshView footer) {
        View targetView = parent.getScrollTargetView();
        if (targetView == null)
            targetView = child;
        return !mFullCollapsed || ScrollCompat.canChildScrollDown(targetView);
    }

    public boolean hasFound() {
        return mFound;
    }
}
