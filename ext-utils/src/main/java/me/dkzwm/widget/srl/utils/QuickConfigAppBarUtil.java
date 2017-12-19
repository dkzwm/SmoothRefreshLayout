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
public class QuickConfigAppBarUtil implements ILifecycleObserver, AppBarLayout.OnOffsetChangedListener
        , SmoothRefreshLayout.OnChildNotYetInEdgeCannotMoveHeaderCallBack
        , SmoothRefreshLayout.OnChildNotYetInEdgeCannotMoveFooterCallBack {
    private int mMinOffset;
    private int mOffset = -1;
    private boolean mFullyExpanded;

    @Override
    public void onAttached(SmoothRefreshLayout layout) {
        CoordinatorLayout coordinatorLayout = findCoordinatorLayout(layout);
        if (coordinatorLayout == null)
            return;
        AppBarLayout appBarLayout = findAppBarLayout(coordinatorLayout);
        if (appBarLayout == null)
            return;
        appBarLayout.addOnOffsetChangedListener(this);
        layout.setOnChildNotYetInEdgeCannotMoveHeaderCallBack(this);
        layout.setOnChildNotYetInEdgeCannotMoveFooterCallBack(this);
    }

    @Override
    public void onDetached(SmoothRefreshLayout layout) {
        layout.setOnChildNotYetInEdgeCannotMoveFooterCallBack(null);
        layout.setOnChildNotYetInEdgeCannotMoveHeaderCallBack(null);
        CoordinatorLayout coordinatorLayout = findCoordinatorLayout(layout);
        if (coordinatorLayout == null)
            return;
        AppBarLayout appBarLayout = findAppBarLayout(coordinatorLayout);
        if (appBarLayout == null)
            return;
        appBarLayout.removeOnOffsetChangedListener(this);
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        mOffset = verticalOffset;
        mFullyExpanded = (appBarLayout.getHeight() - appBarLayout.getBottom()) == 0;
        mMinOffset = Math.min(mOffset, mMinOffset);
    }

    private CoordinatorLayout findCoordinatorLayout(ViewGroup group) {
        CoordinatorLayout layout = null;
        for (int i = 0; i < group.getChildCount(); i++) {
            if (group.getChildAt(i) instanceof CoordinatorLayout) {
                layout = (CoordinatorLayout) group.getChildAt(i);
                break;
            }
        }
        return layout;
    }

    private AppBarLayout findAppBarLayout(ViewGroup group) {
        AppBarLayout layout = null;
        for (int i = 0; i < group.getChildCount(); i++) {
            if (group.getChildAt(i) instanceof AppBarLayout) {
                layout = (AppBarLayout) group.getChildAt(i);
                break;
            }
        }
        return layout;
    }

    @Override
    public boolean isChildNotYetInEdgeCannotMoveHeader(SmoothRefreshLayout parent,
                                                       @Nullable View child,
                                                       @Nullable IRefreshView header) {
        View targetView = parent.getLoadMoreScrollTargetView();
        if (targetView == null)
            throw new IllegalArgumentException("You must set target view first!");
        return !mFullyExpanded || ScrollCompat.canChildScrollUp(targetView);
    }

    @Override
    public boolean isChildNotYetInEdgeCannotMoveFooter(SmoothRefreshLayout parent,
                                                       @Nullable View child,
                                                       @Nullable IRefreshView footer) {
        View targetView = parent.getLoadMoreScrollTargetView();
        if (targetView == null)
            throw new IllegalArgumentException("You must set target view first!");
        return mMinOffset != mOffset || ScrollCompat.canChildScrollDown(targetView);
    }
}
