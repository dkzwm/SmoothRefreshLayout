/*
 * MIT License
 *
 * Copyright (c) 2017 dkzwm
 * Copyright (c) 2015 liaohuqiu.net
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package me.dkzwm.widget.srl;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Build;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Scroller;
import androidx.annotation.CallSuper;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.core.view.NestedScrollingChild3;
import androidx.core.view.NestedScrollingChildHelper;
import androidx.core.view.NestedScrollingParent3;
import androidx.core.view.NestedScrollingParentHelper;
import androidx.core.view.ViewCompat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import me.dkzwm.widget.srl.annotation.Action;
import me.dkzwm.widget.srl.annotation.Mode;
import me.dkzwm.widget.srl.annotation.Orientation;
import me.dkzwm.widget.srl.config.Constants;
import me.dkzwm.widget.srl.extra.IRefreshView;
import me.dkzwm.widget.srl.indicator.DefaultIndicator;
import me.dkzwm.widget.srl.indicator.IIndicator;
import me.dkzwm.widget.srl.indicator.IIndicatorSetter;
import me.dkzwm.widget.srl.manager.VRefreshLayoutManager;
import me.dkzwm.widget.srl.manager.VScaleLayoutManager;
import me.dkzwm.widget.srl.util.AppBarLayoutUtil;
import me.dkzwm.widget.srl.util.ScrollCompat;
import me.dkzwm.widget.srl.util.ViewCatcherUtil;

/** @author dkzwm */
public class SmoothRefreshLayout extends ViewGroup
        implements NestedScrollingParent3, NestedScrollingChild3 {
    // status
    public static final byte SR_STATUS_INIT = 1;
    public static final byte SR_STATUS_PREPARE = 2;
    public static final byte SR_STATUS_REFRESHING = 3;
    public static final byte SR_STATUS_LOADING_MORE = 4;
    public static final byte SR_STATUS_COMPLETE = 5;
    // fresh view status
    public static final byte SR_VIEW_STATUS_INIT = 21;
    public static final byte SR_VIEW_STATUS_HEADER_IN_PROCESSING = 22;
    public static final byte SR_VIEW_STATUS_FOOTER_IN_PROCESSING = 23;

    protected static final Interpolator SPRING_INTERPOLATOR =
            new Interpolator() {
                public float getInterpolation(float input) {
                    --input;
                    return input * input * input * input * input + 1.0F;
                }
            };
    protected static final Interpolator FLING_INTERPOLATOR = new DecelerateInterpolator(.95f);
    protected static final Interpolator SPRING_BACK_INTERPOLATOR = new DecelerateInterpolator(1.6f);
    protected static final int FLAG_AUTO_REFRESH = 0x01;
    protected static final int FLAG_ENABLE_OVER_SCROLL = 0x01 << 2;
    protected static final int FLAG_ENABLE_KEEP_REFRESH_VIEW = 0x01 << 3;
    protected static final int FLAG_ENABLE_PIN_CONTENT_VIEW = 0x01 << 4;
    protected static final int FLAG_ENABLE_PULL_TO_REFRESH = 0x01 << 5;
    protected static final int FLAG_ENABLE_PIN_REFRESH_VIEW_WHILE_LOADING = 0x01 << 6;
    protected static final int FLAG_ENABLE_HEADER_DRAWER_STYLE = 0x01 << 7;
    protected static final int FLAG_ENABLE_FOOTER_DRAWER_STYLE = 0x01 << 8;
    protected static final int FLAG_DISABLE_PERFORM_LOAD_MORE = 0x01 << 9;
    protected static final int FLAG_ENABLE_NO_MORE_DATA = 0x01 << 10;
    protected static final int FLAG_DISABLE_LOAD_MORE = 0x01 << 11;
    protected static final int FLAG_DISABLE_PERFORM_REFRESH = 0x01 << 12;
    protected static final int FLAG_DISABLE_REFRESH = 0x01 << 13;
    protected static final int FLAG_ENABLE_AUTO_PERFORM_LOAD_MORE = 0x01 << 14;
    protected static final int FLAG_ENABLE_AUTO_PERFORM_REFRESH = 0x01 << 15;
    protected static final int FLAG_ENABLE_INTERCEPT_EVENT_WHILE_LOADING = 0x01 << 16;
    protected static final int FLAG_DISABLE_WHEN_ANOTHER_DIRECTION_MOVE = 0x01 << 17;
    protected static final int FLAG_ENABLE_NO_MORE_DATA_NO_BACK = 0x01 << 18;
    protected static final int FLAG_DISABLE_LOAD_MORE_WHEN_CONTENT_NOT_FULL = 0x01 << 19;
    protected static final int FLAG_ENABLE_COMPAT_SYNC_SCROLL = 0x01 << 20;
    protected static final int FLAG_ENABLE_PERFORM_FRESH_WHEN_FLING = 0x01 << 21;
    protected static final int FLAG_ENABLE_OLD_TOUCH_HANDLING = 0x01 << 22;
    protected static final int FLAG_BEEN_SET_NO_MORE_DATA = 0x01 << 23;
    protected static final int MASK_DISABLE_PERFORM_LOAD_MORE = 0x07 << 9;
    protected static final int MASK_DISABLE_PERFORM_REFRESH = 0x03 << 12;
    public static boolean sDebug = false;
    private static int sId = 0;
    private static IRefreshViewCreator sCreator;
    protected final String TAG = "SmoothRefreshLayout-" + sId++;
    protected final int[] mParentScrollConsumed = new int[2];
    protected final int[] mParentOffsetInWindow = new int[2];
    private final List<View> mCachedViews = new ArrayList<>();
    protected IRefreshView<IIndicator> mHeaderView;
    protected IRefreshView<IIndicator> mFooterView;
    protected IIndicator mIndicator;
    protected IIndicatorSetter mIndicatorSetter;
    protected OnRefreshListener mRefreshListener;
    protected boolean mAutomaticActionUseSmoothScroll = false;
    protected boolean mAutomaticActionTriggered = true;
    protected boolean mIsSpringBackCanNotBeInterrupted = false;
    protected boolean mDealAnotherDirectionMove = false;
    protected boolean mPreventForAnotherDirection = false;
    protected boolean mIsInterceptTouchEventInOnceTouch = false;
    protected boolean mIsLastOverScrollCanNotAbort = false;
    protected boolean mNestedScrolling = false;
    protected boolean mNestedTouchScrolling = false;
    protected byte mStatus = SR_STATUS_INIT;
    protected byte mViewStatus = SR_VIEW_STATUS_INIT;
    protected long mLoadingStartTime = 0;
    protected int mAutomaticAction = Constants.ACTION_NOTIFY;
    protected int mLastNestedType = ViewCompat.TYPE_NON_TOUCH;
    protected int mDurationToCloseHeader = 350;
    protected int mDurationToCloseFooter = 350;
    protected int mDurationOfBackToHeaderHeight = 200;
    protected int mDurationOfBackToFooterHeight = 200;
    protected int mDurationOfFlingBack = 550;
    protected int mContentResId = View.NO_ID;
    protected int mStickyHeaderResId = View.NO_ID;
    protected int mStickyFooterResId = View.NO_ID;
    protected int mTouchSlop;
    protected int mTouchPointerId;
    protected int mMinimumFlingVelocity;
    protected int mMaximumFlingVelocity;
    protected View mTargetView;
    protected View mScrollTargetView;
    protected View mAutoFoundScrollTargetView;
    protected View mStickyHeaderView;
    protected View mStickyFooterView;
    protected LayoutManager mLayoutManager;
    protected ScrollChecker mScrollChecker;
    protected VelocityTracker mVelocityTracker;
    protected MotionEvent mLastMoveEvent;
    protected OnHeaderEdgeDetectCallBack mInEdgeCanMoveHeaderCallBack;
    protected OnFooterEdgeDetectCallBack mInEdgeCanMoveFooterCallBack;
    protected OnSyncScrollCallback mSyncScrollCallback;
    protected OnPerformAutoLoadMoreCallBack mAutoLoadMoreCallBack;
    protected OnPerformAutoRefreshCallBack mAutoRefreshCallBack;
    protected OnCalculateBounceCallback mCalculateBounceCallback;
    protected int mFlag =
            FLAG_DISABLE_LOAD_MORE
                    | FLAG_ENABLE_KEEP_REFRESH_VIEW
                    | FLAG_ENABLE_COMPAT_SYNC_SCROLL
                    | FLAG_ENABLE_OLD_TOUCH_HANDLING
                    | FLAG_ENABLE_PERFORM_FRESH_WHEN_FLING;
    private NestedScrollingParentHelper mNestedScrollingParentHelper =
            new NestedScrollingParentHelper(this);
    private NestedScrollingChildHelper mNestedScrollingChildHelper;
    private Interpolator mSpringInterpolator;
    private Interpolator mSpringBackInterpolator;
    private ArrayList<OnUIPositionChangedListener> mUIPositionChangedListeners;
    private ArrayList<OnStatusChangedListener> mStatusChangedListeners;
    private ArrayList<ILifecycleObserver> mLifecycleObservers;
    private DelayToDispatchNestedFling mDelayToDispatchNestedFling;
    private DelayToRefreshComplete mDelayToRefreshComplete;
    private DelayToPerformAutoRefresh mDelayToPerformAutoRefresh;
    private RefreshCompleteHook mHeaderRefreshCompleteHook;
    private RefreshCompleteHook mFooterRefreshCompleteHook;
    private AppBarLayoutUtil mAppBarLayoutUtil;
    private Matrix mCachedMatrix = new Matrix();
    private boolean mIsLastRefreshSuccessful = true;
    private boolean mViewsZAxisNeedReset = true;
    private boolean mNeedFilterScrollEvent = false;
    private boolean mHasSendCancelEvent = false;
    private boolean mHasSendDownEvent = false;
    private boolean mLastEventIsActionDown = false;
    private float[] mCachedFloatPoint = new float[2];
    private int[] mCachedIntPoint = new int[2];
    private float mOffsetConsumed = 0f;
    private float mOffsetTotal = 0f;
    private int mMaxOverScrollDuration = 350;
    private int mMinOverScrollDuration = 100;
    private int mOffsetRemaining = 0;

    public SmoothRefreshLayout(Context context) {
        super(context);
        init(context, null, 0, 0);
    }

    public SmoothRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    public SmoothRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    /**
     * Set the static refresh view creator, if the refresh view is null and the frame be needed the
     * refresh view,frame will use this creator to create refresh view.
     *
     * <p>设置默认的刷新视图构造器，当刷新视图为null且需要使用刷新视图时，Frame会使用该构造器构造刷新视图
     *
     * @param creator The static refresh view creator
     */
    public static void setDefaultCreator(IRefreshViewCreator creator) {
        sCreator = creator;
    }

    @CallSuper
    protected void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        sId++;
        createIndicator();
        if (mIndicator == null || mIndicatorSetter == null) {
            throw new IllegalArgumentException(
                    "You must create a IIndicator, current indicator is null");
        }
        ViewConfiguration viewConfiguration = ViewConfiguration.get(getContext());
        mTouchSlop = viewConfiguration.getScaledTouchSlop();
        mMaximumFlingVelocity = viewConfiguration.getScaledMaximumFlingVelocity();
        mMinimumFlingVelocity = viewConfiguration.getScaledMinimumFlingVelocity();
        createScrollerChecker();
        mSpringInterpolator = SPRING_INTERPOLATOR;
        mSpringBackInterpolator = SPRING_BACK_INTERPOLATOR;
        mDelayToPerformAutoRefresh = new DelayToPerformAutoRefresh();
        TypedArray arr =
                context.obtainStyledAttributes(
                        attrs, R.styleable.SmoothRefreshLayout, defStyleAttr, defStyleRes);
        int mode = Constants.MODE_DEFAULT;
        if (arr != null) {
            try {
                mContentResId =
                        arr.getResourceId(
                                R.styleable.SmoothRefreshLayout_sr_content, mContentResId);
                float resistance =
                        arr.getFloat(
                                R.styleable.SmoothRefreshLayout_sr_resistance,
                                IIndicator.DEFAULT_RESISTANCE);
                mIndicatorSetter.setResistance(resistance);
                mIndicatorSetter.setResistanceOfHeader(
                        arr.getFloat(
                                R.styleable.SmoothRefreshLayout_sr_resistanceOfHeader, resistance));
                mIndicatorSetter.setResistanceOfFooter(
                        arr.getFloat(
                                R.styleable.SmoothRefreshLayout_sr_resistanceOfFooter, resistance));
                mDurationOfBackToHeaderHeight =
                        arr.getInt(
                                R.styleable.SmoothRefreshLayout_sr_backToKeepDuration,
                                mDurationOfBackToHeaderHeight);
                mDurationOfBackToFooterHeight =
                        arr.getInt(
                                R.styleable.SmoothRefreshLayout_sr_backToKeepDuration,
                                mDurationOfBackToFooterHeight);
                mDurationOfBackToHeaderHeight =
                        arr.getInt(
                                R.styleable.SmoothRefreshLayout_sr_backToKeepHeaderDuration,
                                mDurationOfBackToHeaderHeight);
                mDurationOfBackToFooterHeight =
                        arr.getInt(
                                R.styleable.SmoothRefreshLayout_sr_backToKeepFooterDuration,
                                mDurationOfBackToFooterHeight);
                mDurationToCloseHeader =
                        arr.getInt(
                                R.styleable.SmoothRefreshLayout_sr_closeDuration,
                                mDurationToCloseHeader);
                mDurationToCloseFooter =
                        arr.getInt(
                                R.styleable.SmoothRefreshLayout_sr_closeDuration,
                                mDurationToCloseFooter);
                mDurationToCloseHeader =
                        arr.getInt(
                                R.styleable.SmoothRefreshLayout_sr_closeHeaderDuration,
                                mDurationToCloseHeader);
                mDurationToCloseFooter =
                        arr.getInt(
                                R.styleable.SmoothRefreshLayout_sr_closeFooterDuration,
                                mDurationToCloseFooter);
                float ratio =
                        arr.getFloat(
                                R.styleable.SmoothRefreshLayout_sr_ratioToRefresh,
                                IIndicator.DEFAULT_RATIO_TO_REFRESH);
                mIndicatorSetter.setRatioToRefresh(ratio);
                mIndicatorSetter.setRatioOfHeaderToRefresh(
                        arr.getFloat(
                                R.styleable.SmoothRefreshLayout_sr_ratioOfHeaderToRefresh, ratio));
                mIndicatorSetter.setRatioOfFooterToRefresh(
                        arr.getFloat(
                                R.styleable.SmoothRefreshLayout_sr_ratioOfFooterToRefresh, ratio));
                ratio =
                        arr.getFloat(
                                R.styleable.SmoothRefreshLayout_sr_ratioToKeep,
                                IIndicator.DEFAULT_RATIO_TO_REFRESH);
                mIndicatorSetter.setRatioToKeepHeader(ratio);
                mIndicatorSetter.setRatioToKeepFooter(ratio);
                mIndicatorSetter.setRatioToKeepHeader(
                        arr.getFloat(R.styleable.SmoothRefreshLayout_sr_ratioToKeepHeader, ratio));
                mIndicatorSetter.setRatioToKeepFooter(
                        arr.getFloat(R.styleable.SmoothRefreshLayout_sr_ratioToKeepFooter, ratio));
                ratio =
                        arr.getFloat(
                                R.styleable.SmoothRefreshLayout_sr_maxMoveRatio,
                                IIndicator.DEFAULT_MAX_MOVE_RATIO);
                mIndicatorSetter.setMaxMoveRatio(ratio);
                mIndicatorSetter.setMaxMoveRatioOfHeader(
                        arr.getFloat(
                                R.styleable.SmoothRefreshLayout_sr_maxMoveRatioOfHeader, ratio));
                mIndicatorSetter.setMaxMoveRatioOfFooter(
                        arr.getFloat(
                                R.styleable.SmoothRefreshLayout_sr_maxMoveRatioOfFooter, ratio));
                mStickyHeaderResId =
                        arr.getResourceId(R.styleable.SmoothRefreshLayout_sr_stickyHeader, NO_ID);
                mStickyFooterResId =
                        arr.getResourceId(R.styleable.SmoothRefreshLayout_sr_stickyFooter, NO_ID);
                setEnableKeepRefreshView(
                        arr.getBoolean(R.styleable.SmoothRefreshLayout_sr_enableKeep, true));
                setEnablePinContentView(
                        arr.getBoolean(R.styleable.SmoothRefreshLayout_sr_enablePinContent, false));
                setEnableOverScroll(
                        arr.getBoolean(R.styleable.SmoothRefreshLayout_sr_enableOverScroll, true));
                setEnablePullToRefresh(
                        arr.getBoolean(
                                R.styleable.SmoothRefreshLayout_sr_enablePullToRefresh, false));
                setDisableRefresh(
                        !arr.getBoolean(R.styleable.SmoothRefreshLayout_sr_enableRefresh, true));
                setDisableLoadMore(
                        !arr.getBoolean(R.styleable.SmoothRefreshLayout_sr_enableLoadMore, false));
                mode = arr.getInt(R.styleable.SmoothRefreshLayout_sr_mode, 0);
                setEnabled(arr.getBoolean(R.styleable.SmoothRefreshLayout_android_enabled, true));
            } finally {
                arr.recycle();
            }
        }
        setMode(mode);
        if (mNestedScrollingChildHelper == null) {
            setNestedScrollingEnabled(true);
        }
    }

    protected void createIndicator() {
        DefaultIndicator indicator = new DefaultIndicator();
        mIndicator = indicator;
        mIndicatorSetter = indicator;
    }

    protected void createScrollerChecker() {
        mScrollChecker = new ScrollChecker();
    }

    public final IIndicator getIndicator() {
        return mIndicator;
    }

    @Override
    @SuppressWarnings("unchecked")
    @CallSuper
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (params == null) {
            params = generateDefaultLayoutParams();
        } else {
            params = generateLayoutParams(params);
        }
        if (child instanceof IRefreshView) {
            IRefreshView<IIndicator> view = (IRefreshView<IIndicator>) child;
            switch (view.getType()) {
                case IRefreshView.TYPE_HEADER:
                    if (mHeaderView != null)
                        throw new IllegalArgumentException(
                                "Unsupported operation, HeaderView only can be add once !!");
                    mHeaderView = view;
                    break;
                case IRefreshView.TYPE_FOOTER:
                    if (mFooterView != null)
                        throw new IllegalArgumentException(
                                "Unsupported operation, FooterView only can be add once !!");
                    mFooterView = view;
                    break;
            }
        }
        super.addView(child, index, params);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (!enabled) {
            reset();
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        checkViewsZAxisNeedReset();
    }

    @Override
    protected void onDetachedFromWindow() {
        final List<ILifecycleObserver> observers = mLifecycleObservers;
        if (observers != null) {
            for (ILifecycleObserver observer : observers) {
                observer.onDetached(this);
            }
        }
        if (mAppBarLayoutUtil != null) {
            if (mInEdgeCanMoveHeaderCallBack == mAppBarLayoutUtil) {
                mInEdgeCanMoveHeaderCallBack = null;
            }
            if (mInEdgeCanMoveFooterCallBack == mAppBarLayoutUtil) {
                mInEdgeCanMoveFooterCallBack = null;
            }
            mAppBarLayoutUtil.detach();
        }
        mAppBarLayoutUtil = null;
        reset();
        if (mHeaderRefreshCompleteHook != null) {
            mHeaderRefreshCompleteHook.mLayout = null;
        }
        if (mFooterRefreshCompleteHook != null) {
            mFooterRefreshCompleteHook.mLayout = null;
        }
        if (mDelayToDispatchNestedFling != null) {
            mDelayToDispatchNestedFling.mLayout = null;
        }
        if (mDelayToRefreshComplete != null) {
            mDelayToRefreshComplete.mLayout = null;
        }
        mDelayToPerformAutoRefresh.mLayout = null;
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
        }
        mVelocityTracker = null;
        if (sDebug) {
            Log.d(TAG, "onDetachedFromWindow()");
        }
        super.onDetachedFromWindow();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (sDebug) {
            Log.d(TAG, "onAttachedToWindow()");
        }
        final List<ILifecycleObserver> observers = mLifecycleObservers;
        if (observers != null) {
            for (ILifecycleObserver observer : observers) {
                observer.onAttached(this);
            }
        }
        if (isVerticalOrientation()) {
            View view = ViewCatcherUtil.catchAppBarLayout(this);
            if (view != null) {
                mAppBarLayoutUtil = new AppBarLayoutUtil(view);
                if (mInEdgeCanMoveHeaderCallBack == null) {
                    mInEdgeCanMoveHeaderCallBack = mAppBarLayoutUtil;
                }
                if (mInEdgeCanMoveFooterCallBack == null) {
                    mInEdgeCanMoveFooterCallBack = mAppBarLayoutUtil;
                }
            }
        }
        mDelayToPerformAutoRefresh.mLayout = this;
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int count = getChildCount();
        if (count == 0) {
            return;
        }
        ensureTargetView();
        int maxHeight = 0;
        int maxWidth = 0;
        int childState = 0;
        mCachedViews.clear();
        final boolean measureMatchParentChildren =
                MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY
                        || MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY;
        mLayoutManager.mMeasureMatchParentChildren = measureMatchParentChildren;
        mLayoutManager.mOldWidthMeasureSpec = widthMeasureSpec;
        mLayoutManager.mOldHeightMeasureSpec = heightMeasureSpec;
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }
            final LayoutParams lp = (LayoutParams) child.getLayoutParams();
            if (mHeaderView != null && child == mHeaderView.getView()) {
                mLayoutManager.measureHeader(mHeaderView, widthMeasureSpec, heightMeasureSpec);
            } else if (mFooterView != null && child == mFooterView.getView()) {
                mLayoutManager.measureFooter(mFooterView, widthMeasureSpec, heightMeasureSpec);
            } else {
                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
                if (measureMatchParentChildren
                        && (lp.width == LayoutParams.MATCH_PARENT
                                || lp.height == LayoutParams.MATCH_PARENT)) {
                    mCachedViews.add(child);
                }
            }
            maxWidth =
                    Math.max(maxWidth, child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin);
            maxHeight =
                    Math.max(maxHeight, child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin);
            childState = combineMeasuredStates(childState, child.getMeasuredState());
        }
        maxWidth += getPaddingLeft() + getPaddingRight();
        maxHeight += getPaddingTop() + getPaddingBottom();
        maxHeight = Math.max(maxHeight, getSuggestedMinimumHeight());
        maxWidth = Math.max(maxWidth, getSuggestedMinimumWidth());
        setMeasuredDimension(
                resolveSizeAndState(maxWidth, widthMeasureSpec, childState),
                resolveSizeAndState(
                        maxHeight, heightMeasureSpec, childState << MEASURED_HEIGHT_STATE_SHIFT));
        count = mCachedViews.size();
        if (count > 1) {
            for (int i = 0; i < count; i++) {
                final View child = mCachedViews.get(i);
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                final int[] spec = measureChildAgain(lp, widthMeasureSpec, heightMeasureSpec);
                child.measure(spec[0], spec[1]);
            }
        }
        mCachedViews.clear();
        if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY
                || MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY) {
            if (mHeaderView != null && mHeaderView.getView().getVisibility() != GONE) {
                final View child = mHeaderView.getView();
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                final int[] spec = measureChildAgain(lp, widthMeasureSpec, heightMeasureSpec);
                mLayoutManager.measureHeader(mHeaderView, spec[0], spec[1]);
            }
            if (mFooterView != null && mFooterView.getView().getVisibility() != GONE) {
                final View child = mFooterView.getView();
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                final int[] spec = measureChildAgain(lp, widthMeasureSpec, heightMeasureSpec);
                mLayoutManager.measureFooter(mFooterView, spec[0], spec[1]);
            }
        }
    }

    private int[] measureChildAgain(LayoutParams lp, int widthMeasureSpec, int heightMeasureSpec) {
        if (lp.width == LayoutParams.MATCH_PARENT) {
            final int width =
                    Math.max(
                            0,
                            getMeasuredWidth()
                                    - getPaddingLeft()
                                    - getPaddingRight()
                                    - lp.leftMargin
                                    - lp.rightMargin);
            mCachedIntPoint[0] = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
        } else {
            mCachedIntPoint[0] =
                    getChildMeasureSpec(
                            widthMeasureSpec,
                            getPaddingLeft() + getPaddingRight() + lp.leftMargin + lp.rightMargin,
                            lp.width);
        }
        if (lp.height == LayoutParams.MATCH_PARENT) {
            final int height =
                    Math.max(
                            0,
                            getMeasuredHeight()
                                    - getPaddingTop()
                                    - getPaddingBottom()
                                    - lp.topMargin
                                    - lp.bottomMargin);
            mCachedIntPoint[1] = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        } else {
            mCachedIntPoint[1] =
                    getChildMeasureSpec(
                            heightMeasureSpec,
                            getPaddingTop() + getPaddingBottom() + lp.topMargin + lp.bottomMargin,
                            lp.height);
        }
        return mCachedIntPoint;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int count = getChildCount();
        if (count == 0) {
            return;
        }
        mIndicator.checkConfig();
        final int parentRight = r - l - getPaddingRight();
        final int parentBottom = b - t - getPaddingBottom();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }
            if (mHeaderView != null && child == mHeaderView.getView()) {
                mLayoutManager.layoutHeaderView(mHeaderView);
            } else if (mTargetView != null && child == mTargetView) {
                mLayoutManager.layoutContentView(child);
            } else if (mStickyHeaderView != null && child == mStickyHeaderView) {
                mLayoutManager.layoutStickyHeaderView(child);
            } else if ((mFooterView == null || mFooterView.getView() != child)
                    && (mStickyFooterView == null || mStickyFooterView != child)) {
                layoutOtherView(child, parentRight, parentBottom);
            }
        }
        if (mFooterView != null && mFooterView.getView().getVisibility() != GONE) {
            mLayoutManager.layoutFooterView(mFooterView);
        }
        if (mStickyFooterView != null && mStickyFooterView.getVisibility() != GONE) {
            mLayoutManager.layoutStickyFooterView(mStickyFooterView);
        }
        if (!mAutomaticActionTriggered) {
            removeCallbacks(mDelayToPerformAutoRefresh);
            postDelayed(mDelayToPerformAutoRefresh, 90);
        }
    }

    protected void layoutOtherView(View child, int parentRight, int parentBottom) {
        final int width = child.getMeasuredWidth();
        final int height = child.getMeasuredHeight();
        int childLeft, childTop;
        final LayoutParams lp = (LayoutParams) child.getLayoutParams();
        final int gravity = lp.gravity;
        final int layoutDirection = ViewCompat.getLayoutDirection(this);
        final int absoluteGravity = GravityCompat.getAbsoluteGravity(gravity, layoutDirection);
        final int verticalGravity = gravity & Gravity.VERTICAL_GRAVITY_MASK;
        switch (absoluteGravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
            case Gravity.CENTER_HORIZONTAL:
                childLeft =
                        (int)
                                (getPaddingLeft()
                                        + (parentRight - getPaddingLeft() - width) / 2f
                                        + lp.leftMargin
                                        - lp.rightMargin);
                break;
            case Gravity.RIGHT:
                childLeft = parentRight - width - lp.rightMargin;
                break;
            default:
                childLeft = getPaddingLeft() + lp.leftMargin;
        }
        switch (verticalGravity) {
            case Gravity.CENTER_VERTICAL:
                childTop =
                        (int)
                                (getPaddingTop()
                                        + (parentBottom - getPaddingTop() - height) / 2f
                                        + lp.topMargin
                                        - lp.bottomMargin);
                break;
            case Gravity.BOTTOM:
                childTop = parentBottom - height - lp.bottomMargin;
                break;
            default:
                childTop = getPaddingTop() + lp.topMargin;
        }
        child.layout(childLeft, childTop, childLeft + width, childTop + height);
        if (sDebug) {
            Log.d(
                    TAG,
                    String.format(
                            "onLayout(): child: %d %d %d %d",
                            childLeft, childTop, childLeft + width, childTop + height));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mLayoutManager.onLayoutDraw(canvas);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        mLastEventIsActionDown = ev.getActionMasked() == MotionEvent.ACTION_DOWN;
        if (!isEnabled()
                || mTargetView == null
                || ((isDisabledLoadMore() && isDisabledRefresh()))
                || (isEnabledPinRefreshViewWhileLoading()
                        && ((isRefreshing() && isMovingHeader())
                                || (isLoadingMore() && isMovingFooter())))
                || mNestedTouchScrolling) {
            return super.dispatchTouchEvent(ev);
        }
        return processDispatchTouchEvent(ev);
    }

    protected final boolean dispatchTouchEventSuper(MotionEvent ev) {
        if (!isEnabledOldTouchHandling()) {
            final int index = ev.findPointerIndex(mTouchPointerId);
            if (index < 0) {
                return super.dispatchTouchEvent(ev);
            }
            if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
                mOffsetConsumed = 0;
                mOffsetTotal = 0;
                mOffsetRemaining = mTouchSlop * 3;
            } else {
                if (!mIndicator.isAlreadyHere(IIndicator.START_POS)
                        && mIndicator.getRawOffset() != 0) {
                    if (mOffsetRemaining > 0) {
                        mOffsetRemaining -= mTouchSlop;
                        if (isMovingHeader()) {
                            mOffsetTotal -= mOffsetRemaining;
                        } else if (isMovingFooter()) {
                            mOffsetTotal += mOffsetRemaining;
                        }
                    }
                    mOffsetConsumed +=
                            mIndicator.getRawOffset() < 0
                                    ? mIndicator.getLastPos() - mIndicator.getCurrentPos()
                                    : mIndicator.getCurrentPos() - mIndicator.getLastPos();
                    mOffsetTotal += mIndicator.getRawOffset();
                }
                if (isVerticalOrientation()) {
                    ev.offsetLocation(0, mOffsetConsumed - mOffsetTotal);
                } else {
                    ev.offsetLocation(mOffsetConsumed - mOffsetTotal, 0);
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    public void addLifecycleObserver(@NonNull ILifecycleObserver observer) {
        if (mLifecycleObservers == null) {
            mLifecycleObservers = new ArrayList<>();
            mLifecycleObservers.add(observer);
        } else if (!mLifecycleObservers.contains(observer)) {
            mLifecycleObservers.add(observer);
        }
    }

    public void removeLifecycleObserver(@NonNull ILifecycleObserver observer) {
        if (mLifecycleObservers != null) {
            mLifecycleObservers.remove(observer);
        }
    }

    @Nullable
    public View getScrollTargetView() {
        if (mScrollTargetView != null) {
            return mScrollTargetView;
        } else if (mAutoFoundScrollTargetView != null) {
            return mAutoFoundScrollTargetView;
        } else {
            return mTargetView;
        }
    }

    /**
     * Set loadMore scroll target view,For example the content view is a FrameLayout,with a listView
     * in it.You can call this method,set the listView as load more scroll target view. Load more
     * compat will try to make it smooth scrolling.
     *
     * <p>设置加载更多时需要做滑动处理的视图。 例如在SmoothRefreshLayout中有一个CoordinatorLayout,
     * CoordinatorLayout中有AppbarLayout、RecyclerView等，加载更多时希望被移动的视图为RecyclerVieW
     * 而不是CoordinatorLayout,那么设置RecyclerView为TargetView即可
     *
     * @param view Target view
     */
    public void setScrollTargetView(View view) {
        mScrollTargetView = view;
    }

    public LayoutManager getLayoutManager() {
        return mLayoutManager;
    }

    /**
     * Set custom LayoutManager
     *
     * <p>设置自定义布局管理器
     *
     * @param layoutManager The custom LayoutManager
     */
    public void setLayoutManager(@NonNull LayoutManager layoutManager) {
        if (mLayoutManager != layoutManager) {
            if (mLayoutManager != null) {
                if (mLayoutManager.getOrientation() != layoutManager.getOrientation()) {
                    reset();
                    requestLayout();
                }
                mLayoutManager.setLayout(null);
            }
            mLayoutManager = layoutManager;
            mLayoutManager.setLayout(this);
        }
    }

    /**
     * Set the layout mode
     *
     * <p>设置模式，默认为刷新模式，可配置为拉伸模式
     *
     * @param mode The layout mode. {@link Constants#MODE_DEFAULT}, {@link Constants#MODE_SCALE}
     */
    public void setMode(@Mode int mode) {
        if (mode == Constants.MODE_DEFAULT) {
            if (mLayoutManager instanceof VRefreshLayoutManager) {
                return;
            }
            setLayoutManager(new VRefreshLayoutManager());
        } else {
            if (mLayoutManager instanceof VScaleLayoutManager) {
                return;
            }
            setLayoutManager(new VScaleLayoutManager());
        }
    }

    /**
     * Whether to enable the synchronous scroll when load more completed.
     *
     * <p>当加载更多完成时是否启用同步滚动。
     *
     * @param enable enable
     */
    public void setEnableCompatSyncScroll(boolean enable) {
        if (enable) {
            mFlag = mFlag | FLAG_ENABLE_COMPAT_SYNC_SCROLL;
        } else {
            mFlag = mFlag & ~FLAG_ENABLE_COMPAT_SYNC_SCROLL;
        }
    }

    /**
     * Set the custom offset calculator.
     *
     * <p>设置自定义偏移计算器
     *
     * @param calculator Offset calculator
     */
    public void setIndicatorOffsetCalculator(IIndicator.IOffsetCalculator calculator) {
        mIndicatorSetter.setOffsetCalculator(calculator);
    }

    /**
     * Set the listener to be notified when a refresh is triggered.
     *
     * <p>设置刷新监听回调
     *
     * @param listener Listener
     */
    public <T extends OnRefreshListener> void setOnRefreshListener(T listener) {
        mRefreshListener = listener;
    }

    /**
     * Add a listener to listen the views position change event.
     *
     * <p>设置UI位置变化回调
     *
     * @param listener Listener
     */
    public void addOnUIPositionChangedListener(@NonNull OnUIPositionChangedListener listener) {
        if (mUIPositionChangedListeners == null) {
            mUIPositionChangedListeners = new ArrayList<>();
            mUIPositionChangedListeners.add(listener);
        } else if (!mUIPositionChangedListeners.contains(listener)) {
            mUIPositionChangedListeners.add(listener);
        }
    }

    /**
     * remove the listener.
     *
     * <p>移除UI位置变化监听器
     *
     * @param listener Listener
     */
    public void removeOnUIPositionChangedListener(@NonNull OnUIPositionChangedListener listener) {
        if (mUIPositionChangedListeners != null) {
            mUIPositionChangedListeners.remove(listener);
        }
    }

    /**
     * Add a listener when status changed.
     *
     * <p>添加个状态改变监听
     *
     * @param listener Listener that should be called when status changed.
     */
    public void addOnStatusChangedListener(@NonNull OnStatusChangedListener listener) {
        if (mStatusChangedListeners == null) {
            mStatusChangedListeners = new ArrayList<>();
            mStatusChangedListeners.add(listener);
        } else if (!mStatusChangedListeners.contains(listener)) {
            mStatusChangedListeners.add(listener);
        }
    }

    /**
     * remove the listener.
     *
     * <p>移除状态改变监听器
     *
     * @param listener Listener
     */
    public void removeOnStatusChangedListener(@NonNull OnStatusChangedListener listener) {
        if (mStatusChangedListeners != null) {
            mStatusChangedListeners.remove(listener);
        }
    }

    /**
     * Set a sync scrolling callback when refresh competed.
     *
     * <p>设置同步滚动回调，可使用该属性对内容视图做滑动处理。例如内容视图是ListView，完成加载更多时，
     * 需要将加载出的数据显示出来，那么设置该回调，每次Footer回滚时拿到滚动的数值对ListView做向上滚动处理，将数据展示处理
     *
     * @param callback a sync scrolling callback when refresh competed.
     */
    public void setOnSyncScrollCallback(OnSyncScrollCallback callback) {
        mSyncScrollCallback = callback;
    }

    /**
     * Set a callback to override {@link SmoothRefreshLayout#isNotYetInEdgeCannotMoveHeader()}
     * method. Non-null callback will return the value provided by the callback and ignore all
     * internal logic.
     *
     * <p>设置{@link SmoothRefreshLayout#isNotYetInEdgeCannotMoveHeader()}的重载回调，用来检测内容视图是否在顶部
     *
     * @param callback Callback that should be called when isChildNotYetInEdgeCannotMoveHeader() is
     *     called.
     */
    public void setOnHeaderEdgeDetectCallBack(OnHeaderEdgeDetectCallBack callback) {
        mInEdgeCanMoveHeaderCallBack = callback;
    }

    /**
     * Set a callback to override {@link SmoothRefreshLayout#isNotYetInEdgeCannotMoveFooter()}
     * method. Non-null callback will return the value provided by the callback and ignore all
     * internal logic.
     *
     * <p>设置{@link SmoothRefreshLayout#isNotYetInEdgeCannotMoveFooter()}的重载回调，用来检测内容视图是否在底部
     *
     * @param callback Callback that should be called when isChildNotYetInEdgeCannotMoveFooter() is
     *     called.
     */
    public void setOnFooterEdgeDetectCallBack(OnFooterEdgeDetectCallBack callback) {
        mInEdgeCanMoveFooterCallBack = callback;
    }

    /**
     * Set a callback to make sure you need to customize the specified trigger the auto load more
     * rule.
     *
     * <p>设置自动加载更多的触发条件回调，可自定义具体的触发自动加载更多的条件
     *
     * @param callBack Customize the specified triggered rule
     */
    public void setOnPerformAutoLoadMoreCallBack(OnPerformAutoLoadMoreCallBack callBack) {
        mAutoLoadMoreCallBack = callBack;
    }

    /**
     * Set a callback to make sure you need to customize the specified trigger the auto refresh
     * rule.
     *
     * <p>设置滚到到顶自动刷新的触发条件回调，可自定义具体的触发自动刷新的条件
     *
     * @param callBack Customize the specified triggered rule
     */
    public void setOnPerformAutoRefreshCallBack(OnPerformAutoRefreshCallBack callBack) {
        mAutoRefreshCallBack = callBack;
    }

    public void setOnCalculateBounceCallback(OnCalculateBounceCallback callBack) {
        mCalculateBounceCallback = callBack;
    }

    /**
     * Set a hook callback when the refresh complete event be triggered. Only can be called on
     * refreshing.
     *
     * <p>设置一个头部视图刷新完成前的Hook回调
     *
     * @param callback Callback that should be called when refreshComplete() is called.
     */
    public void setOnHookHeaderRefreshCompleteCallback(OnHookUIRefreshCompleteCallBack callback) {
        if (mHeaderRefreshCompleteHook == null)
            mHeaderRefreshCompleteHook = new RefreshCompleteHook();
        mHeaderRefreshCompleteHook.mCallBack = callback;
    }

    /**
     * Set a hook callback when the refresh complete event be triggered. Only can be called on
     * loading more.
     *
     * <p>设置一个尾部视图刷新完成前的Hook回调
     *
     * @param callback Callback that should be called when refreshComplete() is called.
     */
    public void setOnHookFooterRefreshCompleteCallback(OnHookUIRefreshCompleteCallBack callback) {
        if (mFooterRefreshCompleteHook == null)
            mFooterRefreshCompleteHook = new RefreshCompleteHook();
        mFooterRefreshCompleteHook.mCallBack = callback;
    }

    /**
     * Whether it is refreshing state.
     *
     * <p>是否在刷新中
     *
     * @return Refreshing
     */
    public boolean isRefreshing() {
        return mStatus == SR_STATUS_REFRESHING;
    }

    /**
     * Whether it is loading more state.
     *
     * <p>是否在加载更多种
     *
     * @return Loading
     */
    public boolean isLoadingMore() {
        return mStatus == SR_STATUS_LOADING_MORE;
    }

    /**
     * Whether it is refresh successful.
     *
     * <p>是否刷新成功
     *
     * @return Is
     */
    public boolean isRefreshSuccessful() {
        return mIsLastRefreshSuccessful;
    }

    /**
     * Perform refresh complete, to reset the state to {@link SmoothRefreshLayout#SR_STATUS_INIT}
     * and set the last refresh operation successfully.
     *
     * <p>完成刷新，刷新状态为成功
     */
    public final void refreshComplete() {
        refreshComplete(true);
    }

    /**
     * Perform refresh complete, to reset the state to {@link SmoothRefreshLayout#SR_STATUS_INIT}.
     *
     * <p>完成刷新，刷新状态`isSuccessful`
     *
     * @param isSuccessful Set the last refresh operation status
     */
    public final void refreshComplete(boolean isSuccessful) {
        refreshComplete(isSuccessful, 0);
    }

    /**
     * Perform refresh complete, delay to reset the state to {@link
     * SmoothRefreshLayout#SR_STATUS_INIT} and set the last refresh operation successfully.
     *
     * <p>完成刷新，延迟`delayDurationToChangeState`时间
     *
     * @param delayDurationToChangeState Delay to change the state to {@link
     *     SmoothRefreshLayout#SR_STATUS_COMPLETE}
     */
    public final void refreshComplete(long delayDurationToChangeState) {
        refreshComplete(true, delayDurationToChangeState);
    }

    /**
     * Perform refresh complete, delay to reset the state to {@link
     * SmoothRefreshLayout#SR_STATUS_INIT} and set the last refresh operation.
     *
     * <p>完成刷新，刷新状态`isSuccessful`，延迟`delayDurationToChangeState`时间
     *
     * @param delayDurationToChangeState Delay to change the state to {@link
     *     SmoothRefreshLayout#SR_STATUS_INIT}
     * @param isSuccessful Set the last refresh operation
     */
    public final void refreshComplete(boolean isSuccessful, long delayDurationToChangeState) {
        if (sDebug) {
            Log.d(
                    TAG,
                    String.format(
                            "refreshComplete(): isSuccessful: %b, "
                                    + "delayDurationToChangeState: %d",
                            isSuccessful, delayDurationToChangeState));
        }
        mIsLastRefreshSuccessful = isSuccessful;
        if (!isRefreshing() && !isLoadingMore()) {
            return;
        }
        if (delayDurationToChangeState <= 0) {
            performRefreshComplete(true, false, true);
        } else {
            if (isRefreshing() && mHeaderView != null) {
                mHeaderView.onRefreshComplete(this, isSuccessful);
            } else if (isLoadingMore() && mFooterView != null) {
                mFooterView.onRefreshComplete(this, isSuccessful);
            }
            if (mDelayToRefreshComplete == null) {
                mDelayToRefreshComplete = new DelayToRefreshComplete();
            }
            mDelayToRefreshComplete.mLayout = this;
            mDelayToRefreshComplete.mNotifyViews = false;
            postDelayed(mDelayToRefreshComplete, delayDurationToChangeState);
        }
    }

    /**
     * Get the Header height, after the measurement is completed, the height will have value.
     *
     * <p>获取Header的高度，在布局计算完成前无法得到准确的值
     *
     * @return Height default is -1
     */
    public int getHeaderHeight() {
        return mIndicator.getHeaderHeight();
    }

    /**
     * Get the Footer height, after the measurement is completed, the height will have value.
     *
     * <p>获取Footer的高度，在布局计算完成前无法得到准确的值
     *
     * @return Height default is -1
     */
    public int getFooterHeight() {
        return mIndicator.getFooterHeight();
    }

    /**
     * Perform auto refresh at once.
     *
     * <p>自动刷新并立即触发刷新回调
     */
    public boolean autoRefresh() {
        return autoRefresh(Constants.ACTION_NOTIFY, true);
    }

    /**
     * If @param atOnce has been set to true. Auto perform refresh at once.
     *
     * <p>自动刷新，`atOnce`立即触发刷新回调
     *
     * @param atOnce Auto refresh at once
     */
    public boolean autoRefresh(boolean atOnce) {
        return autoRefresh(atOnce ? Constants.ACTION_AT_ONCE : Constants.ACTION_NOTIFY, true);
    }

    /**
     * If @param atOnce has been set to true. Auto perform refresh at once. If @param smooth has
     * been set to true. Auto perform refresh will using smooth scrolling.
     *
     * <p>自动刷新，`atOnce`立即触发刷新回调，`smoothScroll`滚动到触发位置
     *
     * @param atOnce Auto refresh at once
     * @param smoothScroll Auto refresh use smooth scrolling
     */
    public boolean autoRefresh(boolean atOnce, boolean smoothScroll) {
        return autoRefresh(
                atOnce ? Constants.ACTION_AT_ONCE : Constants.ACTION_NOTIFY, smoothScroll);
    }

    /**
     * The @param action can be used to specify the action to trigger refresh. If the `action` been
     * set to `SR_ACTION_NOTHING`, we will not notify the refresh listener when in refreshing. If
     * the `action` been set to `SR_ACTION_AT_ONCE`, we will notify the refresh listener at once. If
     * the `action` been set to `SR_ACTION_NOTIFY`, we will notify the refresh listener when in
     * refreshing be later If @param smooth has been set to true. Auto perform refresh will using
     * smooth scrolling.
     *
     * <p>自动刷新，`action`触发刷新的动作，`smoothScroll`滚动到触发位置
     *
     * @param action Auto refresh use action .{@link Constants#ACTION_NOTIFY}, {@link
     *     Constants#ACTION_AT_ONCE}, {@link Constants#ACTION_NOTHING}
     * @param smoothScroll Auto refresh use smooth scrolling
     */
    public boolean autoRefresh(@Action int action, boolean smoothScroll) {
        if (mStatus != SR_STATUS_INIT || isDisabledPerformRefresh()) {
            return false;
        }
        if (sDebug) {
            Log.d(
                    TAG,
                    String.format(
                            "autoRefresh(): action: %d, smoothScroll: %b", action, smoothScroll));
        }
        final byte old = mStatus;
        mStatus = SR_STATUS_PREPARE;
        notifyStatusChanged(old, mStatus);
        if (mHeaderView != null) {
            mHeaderView.onRefreshPrepare(this);
        }
        mIndicatorSetter.setMovingStatus(Constants.MOVING_HEADER);
        mViewStatus = SR_VIEW_STATUS_HEADER_IN_PROCESSING;
        mAutomaticActionUseSmoothScroll = smoothScroll;
        mAutomaticAction = action;
        if (mIndicator.getHeaderHeight() <= 0) {
            mAutomaticActionTriggered = false;
        } else {
            scrollToTriggeredAutomatic(true);
        }
        return true;
    }

    /**
     * Trigger refresh action directly
     *
     * <p>强制直接触发刷新
     */
    public boolean forceRefresh() {
        if (mIndicator.getHeaderHeight() <= 0 || isDisabledPerformRefresh()) {
            return false;
        }
        removeCallbacks(mDelayToRefreshComplete);
        triggeredRefresh(true);
        return true;
    }

    /**
     * Perform auto load more at once.
     *
     * <p>自动加载更多，并立即触发刷新回调
     */
    public boolean autoLoadMore() {
        return autoLoadMore(Constants.ACTION_NOTIFY, true);
    }

    /**
     * If @param atOnce has been set to true. Auto perform load more at once.
     *
     * <p>自动加载更多，`atOnce`立即触发刷新回调
     *
     * @param atOnce Auto load more at once
     */
    public boolean autoLoadMore(boolean atOnce) {
        return autoLoadMore(atOnce ? Constants.ACTION_AT_ONCE : Constants.ACTION_NOTIFY, true);
    }

    /**
     * If @param atOnce has been set to true. Auto perform load more at once. If @param smooth has
     * been set to true. Auto perform load more will using smooth scrolling.
     *
     * <p>自动加载更多，`atOnce`立即触发刷新回调，`smoothScroll`滚动到触发位置
     *
     * @param atOnce Auto load more at once
     * @param smoothScroll Auto load more use smooth scrolling
     */
    public boolean autoLoadMore(boolean atOnce, boolean smoothScroll) {
        return autoLoadMore(
                atOnce ? Constants.ACTION_AT_ONCE : Constants.ACTION_NOTIFY, smoothScroll);
    }

    /**
     * The @param action can be used to specify the action to trigger refresh. If the `action` been
     * set to `SR_ACTION_NOTHING`, we will not notify the refresh listener when in refreshing. If
     * the `action` been set to `SR_ACTION_AT_ONCE`, we will notify the refresh listener at once. If
     * the `action` been set to `SR_ACTION_NOTIFY`, we will notify the refresh listener when in
     * refreshing be later If @param smooth has been set to true. Auto perform load more will using
     * smooth scrolling.
     *
     * <p>自动加载更多，`action`触发加载更多的动作，`smoothScroll`滚动到触发位置
     *
     * @param action Auto load more use action.{@link Constants#ACTION_NOTIFY}, {@link
     *     Constants#ACTION_AT_ONCE}, {@link Constants#ACTION_NOTHING}
     * @param smoothScroll Auto load more use smooth scrolling
     */
    public boolean autoLoadMore(@Action int action, boolean smoothScroll) {
        if (mStatus != SR_STATUS_INIT || isDisabledPerformLoadMore()) {
            return false;
        }
        if (sDebug) {
            Log.d(
                    TAG,
                    String.format(
                            "autoLoadMore(): action: %d, smoothScroll: %b", action, smoothScroll));
        }
        final byte old = mStatus;
        mStatus = SR_STATUS_PREPARE;
        notifyStatusChanged(old, mStatus);
        if (mFooterView != null) {
            mFooterView.onRefreshPrepare(this);
        }
        mIndicatorSetter.setMovingStatus(Constants.MOVING_FOOTER);
        mViewStatus = SR_VIEW_STATUS_FOOTER_IN_PROCESSING;
        mAutomaticActionUseSmoothScroll = smoothScroll;
        if (mIndicator.getFooterHeight() <= 0) {
            mAutomaticActionTriggered = false;
        } else {
            scrollToTriggeredAutomatic(false);
        }
        return true;
    }

    /**
     * Trigger load more action directly
     *
     * <p>强制直接触发加载更多
     */
    public boolean forceLoadMore() {
        if (mIndicator.getFooterHeight() <= 0 || isDisabledPerformLoadMore()) {
            return false;
        }
        removeCallbacks(mDelayToRefreshComplete);
        triggeredLoadMore(true);
        return true;
    }

    /**
     * Set the resistance while you are moving.
     *
     * <p>移动刷新视图时候的移动阻尼
     *
     * @param resistance Resistance
     */
    public void setResistance(float resistance) {
        mIndicatorSetter.setResistance(resistance);
    }

    /**
     * Set the resistance while you are moving Footer.
     *
     * <p>移动Footer视图时候的移动阻尼
     *
     * @param resistance Resistance
     */
    public void setResistanceOfFooter(float resistance) {
        mIndicatorSetter.setResistanceOfFooter(resistance);
    }

    /**
     * Set the resistance while you are moving Header.
     *
     * <p>移动Header视图时候的移动阻尼
     *
     * @param resistance Resistance
     */
    public void setResistanceOfHeader(float resistance) {
        mIndicatorSetter.setResistanceOfHeader(resistance);
    }

    /**
     * Set the height ratio of the trigger refresh.
     *
     * <p>设置触发刷新时的位置占刷新视图的高度比
     *
     * @param ratio Height ratio
     */
    public void setRatioToRefresh(float ratio) {
        mIndicatorSetter.setRatioToRefresh(ratio);
    }

    /**
     * Set the Header height ratio of the trigger refresh.
     *
     * <p>设置触发下拉刷新时的位置占Header视图的高度比
     *
     * @param ratio Height ratio
     */
    public void setRatioOfHeaderToRefresh(float ratio) {
        mIndicatorSetter.setRatioOfHeaderToRefresh(ratio);
    }

    /**
     * Set the Footer height ratio of the trigger refresh.
     *
     * <p>设置触发加载更多时的位置占Footer视图的高度比
     *
     * @param ratio Height ratio
     */
    public void setRatioOfFooterToRefresh(float ratio) {
        mIndicatorSetter.setRatioOfFooterToRefresh(ratio);
    }

    /**
     * Set the offset of keep view in refreshing occupies the height ratio of the refresh view.
     *
     * <p>刷新中保持视图位置占刷新视图的高度比（默认:`1f`）,该属性的值必须小于等于触发刷新高度比才会有效果， 当开启了{@link
     * SmoothRefreshLayout#isEnabledKeepRefreshView}后，该属性会生效
     *
     * @param ratio Height ratio
     */
    public void setRatioToKeep(float ratio) {
        mIndicatorSetter.setRatioToKeepHeader(ratio);
        mIndicatorSetter.setRatioToKeepFooter(ratio);
    }

    /**
     * Set the offset of keep Header in refreshing occupies the height ratio of the Header.
     *
     * <p>刷新中保持视图位置占Header视图的高度比（默认:`1f`）,该属性的值必须小于等于触发刷新高度比才会有效果
     *
     * @param ratio Height ratio
     */
    public void setRatioToKeepHeader(float ratio) {
        mIndicatorSetter.setRatioToKeepHeader(ratio);
    }

    /**
     * Set the offset of keep Footer in refreshing occupies the height ratio of the Footer.
     *
     * <p>刷新中保持视图位置占Header视图的高度比（默认:`1f`）,该属性的值必须小于等于触发刷新高度比才会有效果
     *
     * @param ratio Height ratio
     */
    public void setRatioToKeepFooter(float ratio) {
        mIndicatorSetter.setRatioToKeepFooter(ratio);
    }

    /**
     * Set the max duration for Cross-Boundary-Rebound(OverScroll).
     *
     * <p>设置越界回弹效果弹出时的最大持续时长（默认:`350`）
     *
     * @param duration Duration
     */
    public void setMaxOverScrollDuration(int duration) {
        mMaxOverScrollDuration = duration;
    }

    /**
     * Set the min duration for Cross-Boundary-Rebound(OverScroll).
     *
     * <p>设置越界回弹效果弹出时的最小持续时长（默认:`100`）
     *
     * @param duration Duration
     */
    public void setMinOverScrollDuration(int duration) {
        mMinOverScrollDuration = duration;
    }

    /**
     * Set the duration for Fling back.
     *
     * <p>设置越界回弹效果回弹时的持续时长（默认:`550`）
     *
     * @param duration Duration
     */
    public void setFlingBackDuration(int duration) {
        mDurationOfFlingBack = duration;
    }

    /**
     * Set the duration of return back to the start position.
     *
     * <p>设置刷新完成回滚到起始位置的时间
     *
     * @param duration Millis
     */
    public void setDurationToClose(int duration) {
        mDurationToCloseHeader = duration;
        mDurationToCloseFooter = duration;
    }

    /**
     * Set the duration of Header return to the start position.
     *
     * <p>设置Header刷新完成回滚到起始位置的时间
     *
     * @param duration Millis
     */
    public void setDurationToCloseHeader(int duration) {
        mDurationToCloseHeader = duration;
    }

    /**
     * Set the duration of Footer return to the start position.
     *
     * <p>设置Footer刷新完成回滚到起始位置的时间
     *
     * @param duration Millis
     */
    public void setDurationToCloseFooter(int duration) {
        mDurationToCloseFooter = duration;
    }

    /**
     * Set the duration of return to the keep refresh view position.
     *
     * <p>设置回滚到保持刷新视图位置的时间
     *
     * @param duration Millis
     */
    public void setDurationOfBackToKeep(int duration) {
        mDurationOfBackToHeaderHeight = duration;
        mDurationOfBackToFooterHeight = duration;
    }

    /**
     * Set the duration of return to the keep refresh view position when Header moves.
     *
     * <p>设置回滚到保持Header视图位置的时间
     *
     * @param duration Millis
     */
    public void setDurationOfBackToKeepHeader(int duration) {
        this.mDurationOfBackToHeaderHeight = duration;
    }

    /**
     * Set the duration of return to the keep refresh view position when Footer moves.
     *
     * <p>设置回顾到保持Footer视图位置的时间
     *
     * @param duration Millis
     */
    public void setDurationOfBackToKeepFooter(int duration) {
        this.mDurationOfBackToFooterHeight = duration;
    }

    /**
     * Set the max can move offset occupies the height ratio of the refresh view.
     *
     * <p>设置最大移动距离占刷新视图的高度比
     *
     * @param ratio The max ratio of refresh view
     */
    public void setMaxMoveRatio(float ratio) {
        mIndicatorSetter.setMaxMoveRatio(ratio);
    }

    /**
     * Set the max can move offset occupies the height ratio of the Header.
     *
     * <p>设置最大移动距离占Header视图的高度比
     *
     * @param ratio The max ratio of Header view
     */
    public void setMaxMoveRatioOfHeader(float ratio) {
        mIndicatorSetter.setMaxMoveRatioOfHeader(ratio);
    }

    /**
     * Set the max can move offset occupies the height ratio of the Footer.
     *
     * <p>设置最大移动距离占Footer视图的高度比
     *
     * @param ratio The max ratio of Footer view
     */
    public void setMaxMoveRatioOfFooter(float ratio) {
        mIndicatorSetter.setMaxMoveRatioOfFooter(ratio);
    }

    /**
     * The flag has set to autoRefresh.
     *
     * <p>是否处于自动刷新刷新
     *
     * @return Enabled
     */
    public boolean isAutoRefresh() {
        return (mFlag & FLAG_AUTO_REFRESH) > 0;
    }

    /**
     * The flag has set enabled overScroll.
     *
     * <p>是否已经开启越界回弹
     *
     * @return Enabled
     */
    public boolean isEnabledOverScroll() {
        return (mFlag & FLAG_ENABLE_OVER_SCROLL) > 0;
    }

    /**
     * If @param enable has been set to true. Will supports over scroll.
     *
     * <p>设置开始越界回弹
     *
     * @param enable Enable
     */
    public void setEnableOverScroll(boolean enable) {
        if (enable) {
            mFlag = mFlag | FLAG_ENABLE_OVER_SCROLL;
        } else {
            mFlag = mFlag & ~FLAG_ENABLE_OVER_SCROLL;
        }
    }

    /**
     * The flag has set enabled to intercept the touch event while loading.
     *
     * <p>是否已经开启刷新中拦截消耗触摸事件
     *
     * @return Enabled
     */
    public boolean isEnabledInterceptEventWhileLoading() {
        return (mFlag & FLAG_ENABLE_INTERCEPT_EVENT_WHILE_LOADING) > 0;
    }

    /**
     * If @param enable has been set to true. Will intercept the touch event while loading.
     *
     * <p>开启刷新中拦截消耗触摸事件
     *
     * @param enable Enable
     */
    public void setEnableInterceptEventWhileLoading(boolean enable) {
        if (enable) {
            mFlag = mFlag | FLAG_ENABLE_INTERCEPT_EVENT_WHILE_LOADING;
        } else {
            mFlag = mFlag & ~FLAG_ENABLE_INTERCEPT_EVENT_WHILE_LOADING;
        }
    }

    /**
     * The flag has been set to pull to refresh.
     *
     * <p>是否已经开启拉动刷新，下拉或者上拉到触发刷新位置即立即触发刷新
     *
     * @return Enabled
     */
    public boolean isEnabledPullToRefresh() {
        return (mFlag & FLAG_ENABLE_PULL_TO_REFRESH) > 0;
    }

    /**
     * If @param enable has been set to true. When the current pos >= refresh offsets perform
     * refresh.
     *
     * <p>设置开启拉动刷新,下拉或者上拉到触发刷新位置即立即触发刷新
     *
     * @param enable Pull to refresh
     */
    public void setEnablePullToRefresh(boolean enable) {
        if (enable) {
            mFlag = mFlag | FLAG_ENABLE_PULL_TO_REFRESH;
        } else {
            mFlag = mFlag & ~FLAG_ENABLE_PULL_TO_REFRESH;
        }
    }

    /**
     * The flag has been set to enabled Header drawerStyle.
     *
     * <p>是否已经开启Header的抽屉效果，即Header在Content下面
     *
     * @return Enabled
     */
    public boolean isEnabledHeaderDrawerStyle() {
        return (mFlag & FLAG_ENABLE_HEADER_DRAWER_STYLE) > 0;
    }

    /**
     * If @param enable has been set to true.Enable Header drawerStyle.
     *
     * <p>设置开启Header的抽屉效果，即Header在Content下面,由于该效果需要改变层级关系，所以需要重新布局
     *
     * @param enable enable Header drawerStyle
     */
    public void setEnableHeaderDrawerStyle(boolean enable) {
        if (enable) {
            mFlag = mFlag | FLAG_ENABLE_HEADER_DRAWER_STYLE;
        } else {
            mFlag = mFlag & ~FLAG_ENABLE_HEADER_DRAWER_STYLE;
        }
        mViewsZAxisNeedReset = true;
        checkViewsZAxisNeedReset();
    }

    /**
     * The flag has been set to enabled Footer drawerStyle.
     *
     * <p>是否已经开启Footer的抽屉效果，即Footer在Content下面
     *
     * @return Enabled
     */
    public boolean isEnabledFooterDrawerStyle() {
        return (mFlag & FLAG_ENABLE_FOOTER_DRAWER_STYLE) > 0;
    }

    /**
     * If @param enable has been set to true.Enable Footer drawerStyle.
     *
     * <p>设置开启Footer的抽屉效果，即Footer在Content下面,由于该效果需要改变层级关系，所以需要重新布局
     *
     * @param enable enable Footer drawerStyle
     */
    public void setEnableFooterDrawerStyle(boolean enable) {
        if (enable) {
            mFlag = mFlag | FLAG_ENABLE_FOOTER_DRAWER_STYLE;
        } else {
            mFlag = mFlag & ~FLAG_ENABLE_FOOTER_DRAWER_STYLE;
        }
        mViewsZAxisNeedReset = true;
        checkViewsZAxisNeedReset();
    }

    /**
     * The flag has been set to disabled perform refresh.
     *
     * <p>是否已经关闭触发下拉刷新
     *
     * @return Disabled
     */
    public boolean isDisabledPerformRefresh() {
        return (mFlag & MASK_DISABLE_PERFORM_REFRESH) > 0;
    }

    /**
     * If @param disable has been set to true. Will never perform refresh.
     *
     * <p>设置是否关闭触发下拉刷新
     *
     * @param disable Disable perform refresh
     */
    public void setDisablePerformRefresh(boolean disable) {
        if (disable) {
            mFlag = mFlag | FLAG_DISABLE_PERFORM_REFRESH;
            reset();
        } else {
            mFlag = mFlag & ~FLAG_DISABLE_PERFORM_REFRESH;
        }
    }

    /**
     * The flag has been set to disabled refresh.
     *
     * <p>是否已经关闭刷新
     *
     * @return Disabled
     */
    public boolean isDisabledRefresh() {
        return (mFlag & FLAG_DISABLE_REFRESH) > 0;
    }

    /**
     * If @param disable has been set to true.Will disable refresh.
     *
     * <p>设置是否关闭刷新
     *
     * @param disable Disable refresh
     */
    public void setDisableRefresh(boolean disable) {
        if (disable) {
            mFlag = mFlag | FLAG_DISABLE_REFRESH;
            reset();
        } else {
            mFlag = mFlag & ~FLAG_DISABLE_REFRESH;
        }
    }

    /**
     * The flag has been set to disabled perform load more.
     *
     * <p>是否已经关闭触发加载更多
     *
     * @return Disabled
     */
    public boolean isDisabledPerformLoadMore() {
        return (mFlag & MASK_DISABLE_PERFORM_LOAD_MORE) > 0;
    }

    /**
     * If @param disable has been set to true.Will never perform load more.
     *
     * <p>设置是否关闭触发加载更多
     *
     * @param disable Disable perform load more
     */
    public void setDisablePerformLoadMore(boolean disable) {
        if (disable) {
            mFlag = mFlag | FLAG_DISABLE_PERFORM_LOAD_MORE;
            reset();
        } else {
            mFlag = mFlag & ~FLAG_DISABLE_PERFORM_LOAD_MORE;
        }
    }

    /**
     * The flag has been set to disabled load more.
     *
     * <p>是否已经关闭加载更多
     *
     * @return Disabled
     */
    public boolean isDisabledLoadMore() {
        return (mFlag & FLAG_DISABLE_LOAD_MORE) > 0;
    }

    /**
     * If @param disable has been set to true. Will disable load more.
     *
     * <p>设置关闭加载更多
     *
     * @param disable Disable load more
     */
    public void setDisableLoadMore(boolean disable) {
        if (disable) {
            mFlag = mFlag | FLAG_DISABLE_LOAD_MORE;
            reset();
        } else {
            mFlag = mFlag & ~FLAG_DISABLE_LOAD_MORE;
        }
    }

    /**
     * The flag has been set to enabled the old touch handling logic.
     *
     * <p>是否已经启用老版本的事件处理逻辑
     *
     * @return Enabled
     */
    public boolean isEnabledOldTouchHandling() {
        return (mFlag & FLAG_ENABLE_OLD_TOUCH_HANDLING) > 0;
    }

    /**
     * If @param enable has been set to true. Frame will use the old version of the touch event
     * handling logic.
     *
     * <p>设置开启老版本的事件处理逻辑，老版本的逻辑会导致部分场景下体验下降，例如拉出刷新视图再收回视图，
     * 当刷新视图回到顶部后缓慢滑动会导致内容视图触发按下效果，视觉上产生割裂，整体性较差但兼容性最好。
     * 新版本的逻辑将一直向下传递触摸事件，不会产生割裂效果，但同时由于需要避免触发长按事件，取巧性的利用了偏移进行规避，
     * 可能导致极个别情况下兼容没有老版本的逻辑好，可按需切换。切莫在处理事件处理过程中切换！
     *
     * @param enable Enable old touch handling
     */
    public void setEnableOldTouchHandling(boolean enable) {
        if (mIndicator.hasTouched()) {
            Log.e(TAG, "This method cannot be called during touch event handling");
            return;
        }
        if (enable) {
            mFlag = mFlag | FLAG_ENABLE_OLD_TOUCH_HANDLING;
        } else {
            mFlag = mFlag & ~FLAG_ENABLE_OLD_TOUCH_HANDLING;
        }
    }

    /**
     * The flag has been set to disabled when horizontal move.
     *
     * <p>是否已经设置响应其它方向滑动
     *
     * @return Disabled
     */
    public boolean isDisabledWhenAnotherDirectionMove() {
        return (mFlag & FLAG_DISABLE_WHEN_ANOTHER_DIRECTION_MOVE) > 0;
    }

    /**
     * Set whether to filter the horizontal moves.
     *
     * <p>设置响应其它方向滑动，当内容视图含有需要响应其它方向滑动的子视图时，需要设置该属性，否则子视图无法响应其它方向的滑动
     *
     * @param disable Enable
     */
    public void setDisableWhenAnotherDirectionMove(boolean disable) {
        if (disable) {
            mFlag = mFlag | FLAG_DISABLE_WHEN_ANOTHER_DIRECTION_MOVE;
        } else {
            mFlag = mFlag & ~FLAG_DISABLE_WHEN_ANOTHER_DIRECTION_MOVE;
        }
    }

    /**
     * The flag has been set to enabled load more has no more data.
     *
     * <p>是否已经开启加载更多完成已无更多数据，自定义Footer可根据该属性判断是否显示无更多数据的提示
     *
     * @return Enabled
     */
    public boolean isEnabledNoMoreData() {
        return (mFlag & FLAG_ENABLE_NO_MORE_DATA) > 0;
    }

    /**
     * If @param enable has been set to true. The Footer will show no more data and will never
     * trigger load more.
     *
     * <p>设置开启加载更多完成已无更多数据，当该属性设置为`true`时，将不再触发加载更多
     *
     * @param enable Enable no more data
     */
    public void setEnableNoMoreData(boolean enable) {
        mFlag = mFlag | FLAG_BEEN_SET_NO_MORE_DATA;
        if (enable) {
            mFlag = mFlag | FLAG_ENABLE_NO_MORE_DATA;
        } else {
            mFlag = mFlag & ~(FLAG_ENABLE_NO_MORE_DATA | FLAG_ENABLE_NO_MORE_DATA_NO_BACK);
        }
    }

    /**
     * The flag has been set to enabled. Load more will be disabled when the content is not full.
     *
     * <p>是否已经设置了内容视图未满屏时关闭加载更多
     *
     * @return Disabled
     */
    public boolean isDisabledLoadMoreWhenContentNotFull() {
        return (mFlag & FLAG_DISABLE_LOAD_MORE_WHEN_CONTENT_NOT_FULL) > 0;
    }

    /**
     * If @param disable has been set to true.Load more will be disabled when the content is not
     * full.
     *
     * <p>设置当内容视图未满屏时关闭加载更多
     *
     * @param disable Disable load more when the content is not full
     */
    public void setDisableLoadMoreWhenContentNotFull(boolean disable) {
        if (disable) {
            mFlag = mFlag | FLAG_DISABLE_LOAD_MORE_WHEN_CONTENT_NOT_FULL;
        } else {
            mFlag = mFlag & ~FLAG_DISABLE_LOAD_MORE_WHEN_CONTENT_NOT_FULL;
        }
    }

    /**
     * The flag has been set to enabled when Footer has no more data and no spring back.
     *
     * <p>是否已经开启加载更多完成已无更多数据且不需要回滚动作
     *
     * @return Enabled
     */
    public boolean isEnabledNoMoreDataAndNoSpringBack() {
        return (mFlag & FLAG_ENABLE_NO_MORE_DATA_NO_BACK) > 0;
    }

    /**
     * If @param enable has been set to true. When there is no more data and no spring back.
     *
     * <p>设置开启加载更多完成已无更多数据且不需要回滚动作，当该属性设置为`true`时，将不再触发加载更多
     *
     * @param enable Enable no more data
     */
    public void setEnableNoMoreDataAndNoSpringBack(boolean enable) {
        mFlag = mFlag | FLAG_BEEN_SET_NO_MORE_DATA;
        if (enable) {
            mFlag = mFlag | FLAG_ENABLE_NO_MORE_DATA | FLAG_ENABLE_NO_MORE_DATA_NO_BACK;
        } else {
            mFlag = mFlag & ~(FLAG_ENABLE_NO_MORE_DATA | FLAG_ENABLE_NO_MORE_DATA_NO_BACK);
        }
    }

    /**
     * The flag has been set to keep refresh view while loading.
     *
     * <p>是否已经开启保持刷新视图
     *
     * @return Enabled
     */
    public boolean isEnabledKeepRefreshView() {
        return (mFlag & FLAG_ENABLE_KEEP_REFRESH_VIEW) > 0;
    }

    /**
     * If @param enable has been set to true.When the current pos> = keep refresh view pos, it rolls
     * back to the keep refresh view pos to perform refresh and remains until the refresh completed.
     *
     * <p>开启刷新中保持刷新视图位置
     *
     * @param enable Keep refresh view
     */
    public void setEnableKeepRefreshView(boolean enable) {

        if (enable) {
            mFlag = mFlag | FLAG_ENABLE_KEEP_REFRESH_VIEW;
        } else {
            mFlag =
                    mFlag
                            & ~(FLAG_ENABLE_KEEP_REFRESH_VIEW
                                    | FLAG_ENABLE_PIN_REFRESH_VIEW_WHILE_LOADING);
        }
    }

    /**
     * The flag has been set to perform load more when the content view scrolling to bottom.
     *
     * <p>是否已经开启到底部自动加载更多
     *
     * @return Enabled
     */
    public boolean isEnabledAutoLoadMore() {
        return (mFlag & FLAG_ENABLE_AUTO_PERFORM_LOAD_MORE) > 0;
    }

    /**
     * If @param enable has been set to true.When the content view scrolling to bottom, it will be
     * perform load more.
     *
     * <p>开启到底自动加载更多
     *
     * @param enable Enable
     */
    public void setEnableAutoLoadMore(boolean enable) {
        if (enable) {
            mFlag = mFlag | FLAG_ENABLE_AUTO_PERFORM_LOAD_MORE;
        } else {
            mFlag = mFlag & ~FLAG_ENABLE_AUTO_PERFORM_LOAD_MORE;
        }
    }

    /**
     * The flag has been set to perform refresh when the content view scrolling to top.
     *
     * <p>是否已经开启到顶自动刷新
     *
     * @return Enabled
     */
    public boolean isEnabledAutoRefresh() {
        return (mFlag & FLAG_ENABLE_AUTO_PERFORM_REFRESH) > 0;
    }

    /**
     * If @param enable has been set to true.When the content view scrolling to top, it will be
     * perform refresh.
     *
     * <p>开启到顶自动刷新
     *
     * @param enable Enable
     */
    public void setEnableAutoRefresh(boolean enable) {
        if (enable) {
            mFlag = mFlag | FLAG_ENABLE_AUTO_PERFORM_REFRESH;
        } else {
            mFlag = mFlag & ~FLAG_ENABLE_AUTO_PERFORM_REFRESH;
        }
    }

    /**
     * The flag has been set to pinned refresh view while loading.
     *
     * <p>是否已经开启刷新过程中固定刷新视图且不响应触摸移动
     *
     * @return Enabled
     */
    public boolean isEnabledPinRefreshViewWhileLoading() {
        return (mFlag & FLAG_ENABLE_PIN_REFRESH_VIEW_WHILE_LOADING) > 0;
    }

    /**
     * If @param enable has been set to true.The refresh view will pinned at the keep refresh
     * position.
     *
     * @param enable Pin content view
     */
    public void setEnablePinRefreshViewWhileLoading(boolean enable) {
        if (enable) {
            mFlag =
                    mFlag
                            | FLAG_ENABLE_PIN_CONTENT_VIEW
                            | FLAG_ENABLE_PIN_REFRESH_VIEW_WHILE_LOADING
                            | FLAG_ENABLE_KEEP_REFRESH_VIEW;
        } else {
            mFlag = mFlag & ~FLAG_ENABLE_PIN_REFRESH_VIEW_WHILE_LOADING;
        }
    }

    /**
     * The flag has been set to pinned content view while loading.
     *
     * <p>是否已经开启了固定内容视图
     *
     * @return Enabled
     */
    public boolean isEnabledPinContentView() {
        return (mFlag & FLAG_ENABLE_PIN_CONTENT_VIEW) > 0;
    }

    /**
     * If @param enable has been set to true. The content view will be pinned in the start pos.
     *
     * <p>设置开启固定内容视图
     *
     * @param enable Pin content view
     */
    public void setEnablePinContentView(boolean enable) {
        if (enable) {
            mFlag = mFlag | FLAG_ENABLE_PIN_CONTENT_VIEW;
        } else {
            mFlag =
                    mFlag
                            & ~(FLAG_ENABLE_PIN_CONTENT_VIEW
                                    | FLAG_ENABLE_PIN_REFRESH_VIEW_WHILE_LOADING);
        }
    }

    /**
     * The flag has been set to perform refresh when Fling.
     *
     * <p>是否已经开启了当收回刷新视图的手势被触发且当前位置大于触发刷新的位置时，将可以触发刷新同时将不存在Fling效果的功能,
     *
     * @return Enabled
     */
    public boolean isEnabledPerformFreshWhenFling() {
        return (mFlag & FLAG_ENABLE_PERFORM_FRESH_WHEN_FLING) > 0;
    }

    /**
     * If @param enable has been set to true. When the gesture of retracting the refresh view is
     * triggered and the current offset is greater than the trigger refresh offset, the fresh can be
     * performed without the Fling effect.
     *
     * <p>当收回刷新视图的手势被触发且当前位置大于触发刷新的位置时，将可以触发刷新同时将不存在Fling效果
     *
     * @param enable enable perform refresh when fling
     */
    public void setEnablePerformFreshWhenFling(boolean enable) {
        if (enable) {
            mFlag = mFlag | FLAG_ENABLE_PERFORM_FRESH_WHEN_FLING;
        } else {
            mFlag = mFlag & ~FLAG_ENABLE_PERFORM_FRESH_WHEN_FLING;
        }
    }

    @Nullable
    public IRefreshView<IIndicator> getFooterView() {
        // Use the static default creator to create the Footer view
        if (!isDisabledLoadMore() && mFooterView == null && sCreator != null) {
            final IRefreshView<IIndicator> footer = sCreator.createFooter(this);
            if (footer != null) {
                setFooterView(footer);
            }
        }
        return mFooterView;
    }

    /**
     * Set the Footer view.
     *
     * <p>设置Footer视图
     *
     * @param footer Footer view
     */
    public void setFooterView(@NonNull IRefreshView footer) {
        if (footer.getType() != IRefreshView.TYPE_FOOTER) {
            throw new IllegalArgumentException("Wrong type, FooterView type must be TYPE_FOOTER");
        }
        if (footer == mFooterView) {
            return;
        }
        if (mFooterView != null) {
            removeView(mFooterView.getView());
            mFooterView = null;
        }
        final View view = footer.getView();
        mViewsZAxisNeedReset = true;
        addView(view, -1, view.getLayoutParams());
    }

    @Nullable
    public IRefreshView<IIndicator> getHeaderView() {
        // Use the static default creator to create the Header view
        if (!isDisabledRefresh() && mHeaderView == null && sCreator != null) {
            final IRefreshView<IIndicator> header = sCreator.createHeader(this);
            if (header != null) {
                setHeaderView(header);
            }
        }
        return mHeaderView;
    }

    /**
     * Set the Header view.
     *
     * <p>设置Header视图
     *
     * @param header Header view
     */
    public void setHeaderView(@NonNull IRefreshView header) {
        if (header.getType() != IRefreshView.TYPE_HEADER) {
            throw new IllegalArgumentException("Wrong type, HeaderView type must be TYPE_HEADER");
        }
        if (header == mHeaderView) {
            return;
        }
        if (mHeaderView != null) {
            removeView(mHeaderView.getView());
            mHeaderView = null;
        }
        final View view = header.getView();
        mViewsZAxisNeedReset = true;
        addView(view, -1, view.getLayoutParams());
    }

    /**
     * Set the content view.
     *
     * <p>设置内容视图
     *
     * @param content Content view
     */
    public void setContentView(View content) {
        if (mTargetView == content) {
            return;
        }
        mContentResId = View.NO_ID;
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child == content) {
                mTargetView = content;
                return;
            }
        }
        if (mTargetView != null) {
            removeView(mTargetView);
        }
        ViewGroup.LayoutParams lp = content.getLayoutParams();
        if (lp == null) {
            lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        }
        mTargetView = content;
        mViewsZAxisNeedReset = true;
        addView(content, lp);
    }

    /**
     * Set the content view resource id.
     *
     * <p>设置内容视图
     *
     * @param id Content view resource id
     */
    public void setContentResId(@IdRes int id) {
        if (id != mContentResId) {
            mContentResId = id;
            mTargetView = null;
            ensureTargetView();
        }
    }

    /**
     * Reset scroller interpolator.
     *
     * <p>重置Scroller的插值器
     */
    public void resetScrollerInterpolator() {
        if (mSpringInterpolator != SPRING_INTERPOLATOR) {
            setSpringInterpolator(SPRING_INTERPOLATOR);
        }
        if (mSpringBackInterpolator != SPRING_BACK_INTERPOLATOR) {
            setSpringBackInterpolator(SPRING_BACK_INTERPOLATOR);
        }
    }

    /**
     * Set the scroller default interpolator.
     *
     * <p>设置Scroller的默认插值器
     *
     * @param interpolator Scroller interpolator
     */
    public void setSpringInterpolator(@NonNull Interpolator interpolator) {
        if (mSpringInterpolator != interpolator) {
            mSpringInterpolator = interpolator;
            if (mScrollChecker.isSpring()) {
                mScrollChecker.setInterpolator(interpolator);
            }
        }
    }

    /**
     * Set the scroller spring back interpolator.
     *
     * @param interpolator Scroller interpolator
     */
    public void setSpringBackInterpolator(@NonNull Interpolator interpolator) {
        if (mSpringBackInterpolator != interpolator) {
            mSpringBackInterpolator = interpolator;
            if (mScrollChecker.isSpringBack() || mScrollChecker.isFlingBack()) {
                mScrollChecker.setInterpolator(interpolator);
            }
        }
    }

    /**
     * Get the ScrollChecker current mode.
     *
     * @return the mode {@link Constants#SCROLLER_MODE_NONE}, {@link
     *     Constants#SCROLLER_MODE_PRE_FLING}, {@link Constants#SCROLLER_MODE_FLING}, {@link
     *     Constants#SCROLLER_MODE_CALC_FLING}, {@link Constants#SCROLLER_MODE_FLING_BACK}, {@link
     *     Constants#SCROLLER_MODE_SPRING}, {@link Constants#SCROLLER_MODE_SPRING_BACK}.
     */
    public byte getScrollMode() {
        return mScrollChecker.mMode;
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams lp) {
        return lp instanceof LayoutParams;
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams lp) {
        if (lp instanceof LayoutParams) {
            return lp;
        } else if (lp instanceof MarginLayoutParams) {
            return new LayoutParams((MarginLayoutParams) lp);
        } else {
            return new LayoutParams(lp);
        }
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    /**
     * Set the pinned header view resource id
     *
     * @param resId Resource id
     */
    public void setStickyHeaderResId(@IdRes int resId) {
        if (mStickyHeaderResId != resId) {
            mStickyHeaderResId = resId;
            mStickyHeaderView = null;
            ensureTargetView();
        }
    }

    /**
     * Set the pinned footer view resource id
     *
     * @param resId Resource id
     */
    public void setStickyFooterResId(@IdRes int resId) {
        if (mStickyFooterResId != resId) {
            mStickyFooterResId = resId;
            mStickyFooterView = null;
            ensureTargetView();
        }
    }

    public boolean onFling(float vx, final float vy, boolean nested) {
        if (sDebug) {
            Log.d(
                    TAG,
                    String.format(
                            "onFling() velocityX: %f, velocityY: %f, nested: %b", vx, vy, nested));
        }
        if ((isNeedInterceptTouchEvent() || isCanNotAbortOverScrolling())) {
            return true;
        }
        if (mPreventForAnotherDirection) {
            return nested && dispatchNestedPreFling(-vx, -vy);
        }
        float realVelocity = isVerticalOrientation() ? vy : vx;
        if (!mIndicator.isAlreadyHere(IIndicator.START_POS)) {
            if (!isEnabledPinContentView()) {
                if (Math.abs(realVelocity) > 2000) {
                    if ((realVelocity > 0 && isMovingHeader())
                            || (realVelocity < 0 && isMovingFooter())) {
                        if (isEnabledOverScroll()) {
                            if (isDisabledLoadMoreWhenContentNotFull()
                                    && !isNotYetInEdgeCannotMoveFooter()
                                    && !isNotYetInEdgeCannotMoveHeader()) {
                                return true;
                            }
                            boolean invert = realVelocity < 0;
                            realVelocity = (float) Math.pow(Math.abs(realVelocity), .5f);
                            mScrollChecker.startPreFling(invert ? -realVelocity : realVelocity);
                        }
                    } else {
                        if (mScrollChecker.getFinalY(realVelocity) > mIndicator.getCurrentPos()) {
                            if (!isEnabledPerformFreshWhenFling()) {
                                mScrollChecker.startPreFling(realVelocity);
                            } else if (isMovingHeader()
                                    && (isDisabledPerformRefresh()
                                            || mIndicator.getCurrentPos()
                                                    < mIndicator.getOffsetToRefresh())) {
                                mScrollChecker.startPreFling(realVelocity);
                            } else if (isMovingFooter()
                                    && (isDisabledPerformLoadMore()
                                            || mIndicator.getCurrentPos()
                                                    < mIndicator.getOffsetToLoadMore())) {
                                mScrollChecker.startPreFling(realVelocity);
                            }
                        }
                    }
                }
                return true;
            }
            if (nested) {
                return dispatchNestedPreFling(-vx, -vy);
            } else {
                return false;
            }
        } else {
            tryToResetMovingStatus();
            if (isEnabledOverScroll()
                    && (!isEnabledPinContentView()
                            || ((realVelocity >= 0 || !isDisabledLoadMore())
                                    && (realVelocity <= 0 || !isDisabledRefresh())))) {
                if (isDisabledLoadMoreWhenContentNotFull()
                        && realVelocity < 0
                        && !isNotYetInEdgeCannotMoveHeader()
                        && !isNotYetInEdgeCannotMoveFooter()) {
                    return nested && dispatchNestedPreFling(-vx, -vy);
                }
                mScrollChecker.startFling(realVelocity);
                if (!nested && isEnabledOldTouchHandling()) {
                    if (mDelayToDispatchNestedFling == null)
                        mDelayToDispatchNestedFling = new DelayToDispatchNestedFling();
                    mDelayToDispatchNestedFling.mLayout = this;
                    mDelayToDispatchNestedFling.mVelocity = (int) realVelocity;
                    ViewCompat.postOnAnimation(this, mDelayToDispatchNestedFling);
                    invalidate();
                    return true;
                }
            }
            invalidate();
        }
        return nested && dispatchNestedPreFling(-vx, -vy);
    }

    @Override
    public boolean onStartNestedScroll(
            @NonNull View child, @NonNull View target, int nestedScrollAxes) {
        return onStartNestedScroll(child, target, nestedScrollAxes, ViewCompat.TYPE_TOUCH);
    }

    @Override
    public boolean onStartNestedScroll(
            @NonNull View child, @NonNull View target, int axes, int type) {
        if (sDebug) {
            Log.d(TAG, String.format("onStartNestedScroll(): axes: %d, type: %d", axes, type));
        }
        return isEnabled()
                && isNestedScrollingEnabled()
                && mTargetView != null
                && (axes & getNestedScrollAxes()) != 0
                && !(type == ViewCompat.TYPE_NON_TOUCH && !isEnabledOverScroll());
    }

    @Override
    public void onNestedScrollAccepted(@NonNull View child, @NonNull View target, int axes) {
        onNestedScrollAccepted(child, target, axes, ViewCompat.TYPE_TOUCH);
    }

    @Override
    public void onNestedScrollAccepted(
            @NonNull View child, @NonNull View target, int axes, int type) {
        if (sDebug) {
            Log.d(TAG, String.format("onNestedScrollAccepted(): axes: %d, type: %d", axes, type));
        }
        // Reset the counter of how much leftover scroll needs to be consumed.
        mNestedScrollingParentHelper.onNestedScrollAccepted(child, target, axes, type);
        // Dispatch up to the nested parent
        startNestedScroll(axes & getNestedScrollAxes(), type);
        mNestedTouchScrolling = type == ViewCompat.TYPE_TOUCH;
        mLastNestedType = type;
        mNestedScrolling = true;
    }

    @Override
    public int getNestedScrollAxes() {
        return mLayoutManager == null
                ? ViewCompat.SCROLL_AXIS_NONE
                : mLayoutManager.getOrientation() == LayoutManager.VERTICAL
                        ? ViewCompat.SCROLL_AXIS_VERTICAL
                        : ViewCompat.SCROLL_AXIS_HORIZONTAL;
    }

    @Override
    public void onNestedPreScroll(@NonNull View target, int dx, int dy, @NonNull int[] consumed) {
        onNestedPreScroll(target, dx, dy, consumed, ViewCompat.TYPE_TOUCH);
    }

    @Override
    public void onNestedPreScroll(
            @NonNull View target, int dx, int dy, @NonNull int[] consumed, int type) {
        final boolean isVerticalOrientation = isVerticalOrientation();
        if (type == ViewCompat.TYPE_TOUCH) {
            if (tryToFilterTouchEvent(null)) {
                if (isVerticalOrientation) {
                    consumed[1] = dy;
                } else {
                    consumed[0] = dx;
                }
            } else {
                mScrollChecker.stop();
                final int distance = isVerticalOrientation ? dy : dx;
                if (distance > 0
                        && !isDisabledRefresh()
                        && !(isEnabledPinRefreshViewWhileLoading() && isRefreshing())
                        && !isNotYetInEdgeCannotMoveHeader()) {
                    if (!mIndicator.isAlreadyHere(IIndicator.START_POS) && isMovingHeader()) {
                        mIndicatorSetter.onFingerMove(
                                mIndicator.getLastMovePoint()[0] - dx,
                                mIndicator.getLastMovePoint()[1] - dy);
                        moveHeaderPos(mIndicator.getOffset());
                        if (isVerticalOrientation) {
                            consumed[1] = dy;
                        } else {
                            consumed[0] = dx;
                        }
                    } else {
                        if (isVerticalOrientation) {
                            mIndicatorSetter.onFingerMove(
                                    mIndicator.getLastMovePoint()[0] - dx,
                                    mIndicator.getLastMovePoint()[1]);
                        } else {
                            mIndicatorSetter.onFingerMove(
                                    mIndicator.getLastMovePoint()[0],
                                    mIndicator.getLastMovePoint()[1] - dy);
                        }
                    }
                } else if (distance < 0
                        && !isDisabledLoadMore()
                        && !(isEnabledPinRefreshViewWhileLoading() && isLoadingMore())
                        && !isNotYetInEdgeCannotMoveFooter()) {
                    if (!mIndicator.isAlreadyHere(IIndicator.START_POS) && isMovingFooter()) {
                        mIndicatorSetter.onFingerMove(
                                mIndicator.getLastMovePoint()[0] - dx,
                                mIndicator.getLastMovePoint()[1] - dy);
                        moveFooterPos(mIndicator.getOffset());
                        if (isVerticalOrientation) {
                            consumed[1] = dy;
                        } else {
                            consumed[0] = dx;
                        }
                    } else {
                        if (isVerticalOrientation) {
                            mIndicatorSetter.onFingerMove(
                                    mIndicator.getLastMovePoint()[0] - dx,
                                    mIndicator.getLastMovePoint()[1]);
                        } else {
                            mIndicatorSetter.onFingerMove(
                                    mIndicator.getLastMovePoint()[0],
                                    mIndicator.getLastMovePoint()[1] - dy);
                        }
                    }
                }
            }
            tryToResetMovingStatus();
        }
        // Now let our nested parent consume the leftovers
        final int[] parentConsumed = mParentScrollConsumed;
        parentConsumed[0] = 0;
        parentConsumed[1] = 0;
        if (dispatchNestedPreScroll(
                dx - consumed[0], dy - consumed[1], parentConsumed, null, type)) {
            consumed[0] += parentConsumed[0];
            consumed[1] += parentConsumed[1];
        } else if (type == ViewCompat.TYPE_NON_TOUCH) {
            if (!isMovingContent() && !isEnabledPinContentView()) {
                if (isVerticalOrientation) {
                    parentConsumed[1] = dy;
                } else {
                    parentConsumed[0] = dx;
                }
                consumed[0] += parentConsumed[0];
                consumed[1] += parentConsumed[1];
            }
        }
        if (sDebug) {
            Log.d(
                    TAG,
                    String.format(
                            "onNestedPreScroll(): dx: %d, dy: %d, consumed: %s, type: %d",
                            dx, dy, Arrays.toString(consumed), type));
        }
    }

    @Override
    public void onNestedScroll(
            @NonNull View target,
            int dxConsumed,
            int dyConsumed,
            int dxUnconsumed,
            int dyUnconsumed) {
        onNestedScroll(
                target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, ViewCompat.TYPE_TOUCH);
    }

    @Override
    public void onNestedScroll(
            @NonNull View target,
            int dxConsumed,
            int dyConsumed,
            int dxUnconsumed,
            int dyUnconsumed,
            int type) {
        mCachedIntPoint[0] = 0;
        mCachedIntPoint[1] = 0;
        onNestedScroll(
                target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type, mCachedIntPoint);
    }

    @Override
    public void onNestedScroll(
            @NonNull View target,
            int dxConsumed,
            int dyConsumed,
            int dxUnconsumed,
            int dyUnconsumed,
            int type,
            @NonNull int[] consumed) {
        // Dispatch up to the nested parent first
        dispatchNestedScroll(
                dxConsumed,
                dyConsumed,
                dxUnconsumed,
                dyUnconsumed,
                mParentOffsetInWindow,
                type,
                consumed);
        if (sDebug) {
            Log.d(
                    TAG,
                    String.format(
                            "onNestedScroll(): dxConsumed: %d, dyConsumed: %d, dxUnconsumed: %d"
                                    + " dyUnconsumed: %d, type: %d, consumed: %s",
                            dxConsumed,
                            dyConsumed,
                            dxUnconsumed,
                            dyUnconsumed,
                            type,
                            Arrays.toString(consumed)));
        }
        final boolean isVerticalOrientation = isVerticalOrientation();
        if (isVerticalOrientation) {
            if (dyUnconsumed == 0 || consumed[1] == dyUnconsumed) {
                onNestedScrollChanged(true);
                return;
            }
        } else {
            if (dxUnconsumed == 0 || consumed[0] == dxUnconsumed) {
                onNestedScrollChanged(true);
                return;
            }
        }
        if (type == ViewCompat.TYPE_TOUCH) {
            if (tryToFilterTouchEvent(null)) {
                return;
            }
            final int dx = dxUnconsumed + mParentOffsetInWindow[0] - consumed[0];
            final int dy = dyUnconsumed + mParentOffsetInWindow[1] - consumed[1];
            final int distance = isVerticalOrientation ? dy : dx;
            if (distance < 0
                    && !isDisabledRefresh()
                    && !isNotYetInEdgeCannotMoveHeader()
                    && !(isEnabledPinRefreshViewWhileLoading() && isRefreshing())) {
                mIndicatorSetter.onFingerMove(
                        mIndicator.getLastMovePoint()[0] - dx,
                        mIndicator.getLastMovePoint()[1] - dy);
                moveHeaderPos(mIndicator.getOffset());
                if (isVerticalOrientation) {
                    consumed[1] += dy;
                } else {
                    consumed[0] += dx;
                }
            } else if (distance > 0
                    && !isDisabledLoadMore()
                    && !isNotYetInEdgeCannotMoveFooter()
                    && !(isDisabledLoadMoreWhenContentNotFull()
                            && !isNotYetInEdgeCannotMoveHeader()
                            && mIndicator.isAlreadyHere(IIndicator.START_POS))
                    && !(isEnabledPinRefreshViewWhileLoading() && isLoadingMore())) {
                mIndicatorSetter.onFingerMove(
                        mIndicator.getLastMovePoint()[0] - dx,
                        mIndicator.getLastMovePoint()[1] - dy);
                moveFooterPos(mIndicator.getOffset());
                if (isVerticalOrientation) {
                    consumed[1] += dy;
                } else {
                    consumed[0] += dx;
                }
            }
            tryToResetMovingStatus();
        }
        if (dxConsumed != 0 || dyConsumed != 0 || consumed[0] != 0 || consumed[1] != 0) {
            onNestedScrollChanged(true);
        }
    }

    @Override
    public void onStopNestedScroll(@NonNull View target) {
        onStopNestedScroll(target, ViewCompat.TYPE_TOUCH);
    }

    @Override
    public void onStopNestedScroll(@NonNull View target, int type) {
        if (sDebug) {
            Log.d(TAG, String.format("onStopNestedScroll() type: %d", type));
        }
        mNestedScrollingParentHelper.onStopNestedScroll(target, type);
        if (mLastNestedType == type) {
            mNestedScrolling = false;
        }
        mNestedTouchScrolling = false;
        mIsInterceptTouchEventInOnceTouch = isNeedInterceptTouchEvent();
        mIsLastOverScrollCanNotAbort = isCanNotAbortOverScrolling();
        // Dispatch up our nested parent
        getScrollingChildHelper().stopNestedScroll(type);
        if (!isAutoRefresh() && type == ViewCompat.TYPE_TOUCH && !mLastEventIsActionDown) {
            mIndicatorSetter.onFingerUp();
            onFingerUp();
        }
        onNestedScrollChanged(true);
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return getScrollingChildHelper().isNestedScrollingEnabled();
    }

    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        getScrollingChildHelper().setNestedScrollingEnabled(enabled);
    }

    @Override
    public boolean startNestedScroll(int axes) {
        return getScrollingChildHelper().startNestedScroll(axes);
    }

    @Override
    public boolean startNestedScroll(int axes, int type) {
        return getScrollingChildHelper().startNestedScroll(axes, type);
    }

    @Override
    public void stopNestedScroll() {
        stopNestedScroll(ViewCompat.TYPE_TOUCH);
    }

    @Override
    public void stopNestedScroll(int type) {
        if (sDebug) {
            Log.d(TAG, String.format("stopNestedScroll() type: %d", type));
        }
        final View targetView = getScrollTargetView();
        if (targetView != null) {
            ViewCompat.stopNestedScroll(targetView, type);
        }
        getScrollingChildHelper().stopNestedScroll(type);
    }

    @Override
    public boolean hasNestedScrollingParent() {
        return getScrollingChildHelper().hasNestedScrollingParent();
    }

    @Override
    public boolean hasNestedScrollingParent(int type) {
        return getScrollingChildHelper().hasNestedScrollingParent(type);
    }

    @Override
    public boolean dispatchNestedScroll(
            int dxConsumed,
            int dyConsumed,
            int dxUnconsumed,
            int dyUnconsumed,
            int[] offsetInWindow) {
        return getScrollingChildHelper()
                .dispatchNestedScroll(
                        dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedScroll(
            int dxConsumed,
            int dyConsumed,
            int dxUnconsumed,
            int dyUnconsumed,
            @Nullable int[] offsetInWindow,
            int type) {
        return getScrollingChildHelper()
                .dispatchNestedScroll(
                        dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow, type);
    }

    @Override
    public void dispatchNestedScroll(
            int dxConsumed,
            int dyConsumed,
            int dxUnconsumed,
            int dyUnconsumed,
            @Nullable int[] offsetInWindow,
            int type,
            @NonNull int[] consumed) {
        getScrollingChildHelper()
                .dispatchNestedScroll(
                        dxConsumed,
                        dyConsumed,
                        dxUnconsumed,
                        dyUnconsumed,
                        offsetInWindow,
                        type,
                        consumed);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        return getScrollingChildHelper().dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedPreScroll(
            int dx, int dy, @Nullable int[] consumed, @Nullable int[] offsetInWindow, int type) {
        return getScrollingChildHelper()
                .dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow, type);
    }

    @Override
    public boolean onNestedPreFling(@NonNull View target, float velocityX, float velocityY) {
        return onFling(-velocityX, -velocityY, true);
    }

    @Override
    public boolean onNestedFling(
            @NonNull View target, float velocityX, float velocityY, boolean consumed) {
        return dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return getScrollingChildHelper().dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return getScrollingChildHelper().dispatchNestedPreFling(velocityX, velocityY);
    }

    @Override
    public void computeScroll() {
        if (mNestedScrolling || !isMovingContent()) {
            return;
        }
        onNestedScrollChanged(true);
    }

    @Override
    public boolean canScrollVertically(int direction) {
        if (isVerticalOrientation()) {
            if (mAppBarLayoutUtil == null || mAppBarLayoutUtil != mInEdgeCanMoveHeaderCallBack) {
                if (direction < 0) {
                    return super.canScrollVertically(direction) || isNotYetInEdgeCannotMoveHeader();
                } else {
                    return super.canScrollVertically(direction) || isNotYetInEdgeCannotMoveFooter();
                }
            }
        }
        return super.canScrollVertically(direction);
    }

    @Override
    public boolean canScrollHorizontally(int direction) {
        if (!isVerticalOrientation()) {
            if (direction < 0) {
                return super.canScrollHorizontally(direction) || isNotYetInEdgeCannotMoveHeader();
            } else {
                return super.canScrollHorizontally(direction) || isNotYetInEdgeCannotMoveFooter();
            }
        }
        return super.canScrollHorizontally(direction);
    }

    public void onNestedScrollChanged(boolean compute) {
        if (mNeedFilterScrollEvent) {
            return;
        }
        tryScrollToPerformAutoRefresh();
        if (compute) {
            mScrollChecker.computeScrollOffset();
        }
    }

    public boolean isVerticalOrientation() {
        return mLayoutManager == null || mLayoutManager.getOrientation() == LayoutManager.VERTICAL;
    }

    /** Check the Z-Axis relationships of the views need to be rearranged */
    protected void checkViewsZAxisNeedReset() {
        final int count = getChildCount();
        if (mViewsZAxisNeedReset && count > 0 && (mHeaderView != null || mFooterView != null)) {
            mCachedViews.clear();
            if (mHeaderView != null && !isEnabledHeaderDrawerStyle()) {
                mCachedViews.add(mHeaderView.getView());
            }
            if (mFooterView != null && !isEnabledFooterDrawerStyle()) {
                mCachedViews.add(mFooterView.getView());
            }
            for (int i = count - 1; i >= 0; i--) {
                View view = getChildAt(i);
                if (!(view instanceof IRefreshView)) {
                    mCachedViews.add(view);
                }
            }
            final int viewCount = mCachedViews.size();
            if (viewCount > 0) {
                for (int i = viewCount - 1; i >= 0; i--) {
                    bringChildToFront(mCachedViews.get(i));
                }
            }
            mCachedViews.clear();
        }
        mViewsZAxisNeedReset = false;
    }

    protected void reset() {
        if (mStatus != SR_STATUS_INIT) {
            if (isRefreshing() || isLoadingMore()) {
                notifyUIRefreshComplete(false, false, true);
            }
            if (mHeaderView != null) {
                mHeaderView.onReset(this);
            }
            if (mFooterView != null) {
                mFooterView.onReset(this);
            }
            if (!mIndicator.isAlreadyHere(IIndicator.START_POS)) {
                mScrollChecker.scrollTo(IIndicator.START_POS, 0);
            }
            mScrollChecker.stop();
            mScrollChecker.setInterpolator(mSpringInterpolator);
            final byte old = mStatus;
            mStatus = SR_STATUS_INIT;
            notifyStatusChanged(old, mStatus);
            mAutomaticActionTriggered = true;
            mLayoutManager.resetLayout(
                    mHeaderView, mFooterView, mStickyHeaderView, mStickyFooterView, mTargetView);
            removeCallbacks(mDelayToRefreshComplete);
            removeCallbacks(mDelayToDispatchNestedFling);
            removeCallbacks(mDelayToPerformAutoRefresh);
            if (sDebug) {
                Log.d(TAG, "reset()");
            }
        }
    }

    protected void tryToPerformAutoRefresh() {
        if (!mAutomaticActionTriggered) {
            if (sDebug) Log.d(TAG, "tryToPerformAutoRefresh()");
            if (isHeaderInProcessing() && isMovingHeader()) {
                if (mHeaderView == null || mIndicator.getHeaderHeight() <= 0) {
                    return;
                }
                scrollToTriggeredAutomatic(true);
            } else if (isFooterInProcessing() && isMovingFooter()) {
                if (mFooterView == null || mIndicator.getFooterHeight() <= 0) {
                    return;
                }
                scrollToTriggeredAutomatic(false);
            }
        }
    }

    private void ensureTargetView() {
        boolean ensureStickyHeader = mStickyHeaderView == null && mStickyHeaderResId != NO_ID;
        boolean ensureStickyFooter = mStickyFooterView == null && mStickyFooterResId != NO_ID;
        boolean ensureTarget = mTargetView == null && mContentResId != NO_ID;
        final int count = getChildCount();
        if (ensureStickyHeader || ensureStickyFooter || ensureTarget) {
            for (int i = count - 1; i >= 0; i--) {
                View child = getChildAt(i);
                if (ensureStickyHeader && child.getId() == mStickyHeaderResId) {
                    mStickyHeaderView = child;
                    ensureStickyHeader = false;
                } else if (ensureStickyFooter && child.getId() == mStickyFooterResId) {
                    mStickyFooterView = child;
                    ensureStickyFooter = false;
                } else if (ensureTarget) {
                    if (mContentResId == child.getId()) {
                        mTargetView = child;
                        View view = ensureScrollTargetView(child, true, 0, 0);
                        if (view != null && view != child) {
                            mAutoFoundScrollTargetView = view;
                        }
                        ensureTarget = false;
                    } else if (child instanceof ViewGroup) {
                        final View view =
                                foundViewInViewGroupById((ViewGroup) child, mContentResId);
                        if (view != null) {
                            mTargetView = child;
                            mScrollTargetView = view;
                            ensureTarget = false;
                        }
                    }
                } else if (!ensureStickyHeader && !ensureStickyFooter) {
                    break;
                }
            }
        }
        if (mTargetView == null) {
            for (int i = count - 1; i >= 0; i--) {
                View child = getChildAt(i);
                if (child.getVisibility() == VISIBLE
                        && !(child instanceof IRefreshView)
                        && child != mStickyHeaderView
                        && child != mStickyFooterView) {
                    View view = ensureScrollTargetView(child, true, 0, 0);
                    if (view != null) {
                        mTargetView = child;
                        if (view != child) {
                            mAutoFoundScrollTargetView = view;
                        }
                        break;
                    } else {
                        mTargetView = child;
                        break;
                    }
                }
            }
        } else if (mTargetView.getParent() == null) {
            mTargetView = null;
            ensureTargetView();
            mLayoutManager.offsetChild(
                    mHeaderView, mFooterView, mStickyHeaderView, mStickyFooterView, mTargetView, 0);
            return;
        }
        mHeaderView = getHeaderView();
        mFooterView = getFooterView();
    }

    /**
     * Returns true if a child view contains the specified point when transformed into its
     * coordinate space.
     *
     * @see ViewGroup source code
     */
    protected boolean isTransformedTouchPointInView(float x, float y, ViewGroup group, View child) {
        if (child.getVisibility() != VISIBLE
                || child.getAnimation() != null
                || child instanceof IRefreshView) {
            return false;
        }
        mCachedFloatPoint[0] = x;
        mCachedFloatPoint[1] = y;
        transformPointToViewLocal(group, mCachedFloatPoint, child);
        final boolean isInView =
                mCachedFloatPoint[0] >= 0
                        && mCachedFloatPoint[1] >= 0
                        && mCachedFloatPoint[0] < child.getWidth()
                        && mCachedFloatPoint[1] < child.getHeight();
        if (isInView) {
            mCachedFloatPoint[0] = mCachedFloatPoint[0] - x;
            mCachedFloatPoint[1] = mCachedFloatPoint[1] - y;
        }
        return isInView;
    }

    public void transformPointToViewLocal(ViewGroup group, float[] point, View child) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1
                && Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
            group.transformPointToViewLocal(point, child);
        } else {
            // When the system version is lower than LOLLIPOP_MR1, the system source code has no
            // transformPointToViewLocal method. We need to be compatible with it.
            // When the system version is larger than R, the transformPointToViewLocal method has
            // been unavailable. We need to be compatible with it.
            point[0] += group.getScrollX() - child.getLeft();
            point[1] += group.getScrollY() - child.getTop();
            Matrix matrix = child.getMatrix();
            if (!matrix.isIdentity()) {
                mCachedMatrix.reset();
                if (matrix.invert(mCachedMatrix)) {
                    mCachedMatrix.mapPoints(point);
                }
            }
        }
    }

    protected View ensureScrollTargetView(View target, boolean noTransform, float x, float y) {
        if (target instanceof IRefreshView
                || target.getVisibility() != VISIBLE
                || target.getAnimation() != null) {
            return null;
        }
        if (isScrollingView(target)) {
            return target;
        }
        if (target instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) target;
            final int count = group.getChildCount();
            for (int i = count - 1; i >= 0; i--) {
                View child = group.getChildAt(i);
                if (noTransform || isTransformedTouchPointInView(x, y, group, child)) {
                    View view =
                            ensureScrollTargetView(
                                    child,
                                    noTransform,
                                    x + mCachedFloatPoint[0],
                                    y + mCachedFloatPoint[1]);
                    if (view != null) {
                        return view;
                    }
                }
            }
        }
        return null;
    }

    protected boolean isWrappedByScrollingView(ViewParent parent) {
        if (parent instanceof View) {
            if (isScrollingView((View) parent)) {
                return true;
            }
            return isWrappedByScrollingView(parent.getParent());
        }
        return false;
    }

    protected boolean isScrollingView(View view) {
        return ScrollCompat.isScrollingView(view);
    }

    protected boolean processDispatchTouchEvent(MotionEvent ev) {
        final int action = ev.getAction() & MotionEvent.ACTION_MASK;
        if (sDebug) {
            Log.d(TAG, String.format("processDispatchTouchEvent(): action: %d", action));
        }
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);
        final boolean oldTouchHanding = isEnabledOldTouchHandling();
        switch (action) {
            case MotionEvent.ACTION_UP:
                final int pointerId = ev.getPointerId(0);
                mVelocityTracker.computeCurrentVelocity(1000, mMaximumFlingVelocity);
                float vy = mVelocityTracker.getYVelocity(pointerId);
                float vx = mVelocityTracker.getXVelocity(pointerId);
                if (Math.abs(vx) >= mMinimumFlingVelocity
                        || Math.abs(vy) >= mMinimumFlingVelocity) {
                    final boolean handler = onFling(vx, vy, false);
                    final View targetView = getScrollTargetView();
                    if (handler
                            && !ViewCatcherUtil.isCoordinatorLayout(mTargetView)
                            && targetView != null
                            && !ViewCatcherUtil.isViewPager(targetView)
                            && !(targetView.getParent() instanceof View
                                    && ViewCatcherUtil.isViewPager(
                                            (View) targetView.getParent()))) {
                        ev.setAction(MotionEvent.ACTION_CANCEL);
                    }
                }
            case MotionEvent.ACTION_CANCEL:
                mIndicatorSetter.onFingerUp();
                mPreventForAnotherDirection = false;
                mDealAnotherDirectionMove = false;
                if (isNeedFilterTouchEvent()) {
                    mIsInterceptTouchEventInOnceTouch = false;
                    if (mIsLastOverScrollCanNotAbort
                            && mIndicator.isAlreadyHere(IIndicator.START_POS)) {
                        mScrollChecker.stop();
                    }
                    mIsLastOverScrollCanNotAbort = false;
                } else {
                    mIsInterceptTouchEventInOnceTouch = false;
                    mIsLastOverScrollCanNotAbort = false;
                    if (mIndicator.hasLeftStartPosition()) {
                        onFingerUp();
                    } else {
                        notifyFingerUp();
                    }
                }
                mHasSendCancelEvent = false;
                mVelocityTracker.clear();
                break;
            case MotionEvent.ACTION_POINTER_UP:
                final int pointerIndex =
                        (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK)
                                >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                if (ev.getPointerId(pointerIndex) == mTouchPointerId) {
                    // Pick a new pointer to pick up the slack.
                    final int newIndex = pointerIndex == 0 ? 1 : 0;
                    mTouchPointerId = ev.getPointerId(newIndex);
                    mIndicatorSetter.onFingerMove(ev.getX(newIndex), ev.getY(newIndex));
                }
                final int count = ev.getPointerCount();
                mVelocityTracker.computeCurrentVelocity(1000, mMaximumFlingVelocity);
                final int upIndex = ev.getActionIndex();
                final int id1 = ev.getPointerId(upIndex);
                final float x1 = mVelocityTracker.getXVelocity(id1);
                final float y1 = mVelocityTracker.getYVelocity(id1);
                for (int i = 0; i < count; i++) {
                    if (i == upIndex) continue;
                    final int id2 = ev.getPointerId(i);
                    final float x = x1 * mVelocityTracker.getXVelocity(id2);
                    final float y = y1 * mVelocityTracker.getYVelocity(id2);
                    final float dot = x + y;
                    if (dot < 0) {
                        mVelocityTracker.clear();
                        break;
                    }
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                mTouchPointerId = ev.getPointerId(ev.getActionIndex());
                mIndicatorSetter.onFingerMove(
                        ev.getX(ev.getActionIndex()), ev.getY(ev.getActionIndex()));
                break;
            case MotionEvent.ACTION_DOWN:
                mIndicatorSetter.onFingerUp();
                mTouchPointerId = ev.getPointerId(0);
                mIndicatorSetter.onFingerDown(ev.getX(), ev.getY());
                mIsInterceptTouchEventInOnceTouch = isNeedInterceptTouchEvent();
                mIsLastOverScrollCanNotAbort = isCanNotAbortOverScrolling();
                if (!isNeedFilterTouchEvent()) {
                    mScrollChecker.stop();
                }
                mHasSendDownEvent = false;
                mPreventForAnotherDirection = false;
                if (mScrollTargetView == null) {
                    View view = ensureScrollTargetView(this, false, ev.getX(), ev.getY());
                    if (view != null && mTargetView != view && mAutoFoundScrollTargetView != view) {
                        mAutoFoundScrollTargetView = view;
                    }
                } else {
                    mAutoFoundScrollTargetView = null;
                }
                removeCallbacks(mDelayToDispatchNestedFling);
                dispatchTouchEventSuper(ev);
                return true;
            case MotionEvent.ACTION_MOVE:
                final int index = ev.findPointerIndex(mTouchPointerId);
                if (index < 0) {
                    Log.e(
                            TAG,
                            String.format(
                                    "Error processing scroll; pointer index for id %d not found. Did any MotionEvents get skipped?",
                                    mTouchPointerId));
                    return super.dispatchTouchEvent(ev);
                }
                if (!mIndicator.hasTouched()) {
                    mIndicatorSetter.onFingerDown(ev.getX(index), ev.getY(index));
                }
                mLastMoveEvent = ev;
                if (tryToFilterTouchEvent(ev)) {
                    return true;
                }
                tryToResetMovingStatus();
                if (!mDealAnotherDirectionMove) {
                    final float[] pressDownPoint = mIndicator.getFingerDownPoint();
                    final float offsetX = ev.getX(index) - pressDownPoint[0];
                    final float offsetY = ev.getY(index) - pressDownPoint[1];
                    tryToDealAnotherDirectionMove(offsetX, offsetY);
                    if (mDealAnotherDirectionMove && oldTouchHanding) {
                        mIndicatorSetter.onFingerDown(
                                ev.getX(index) - offsetX / 10, ev.getY(index) - offsetY / 10);
                    }
                    final ViewParent parent = getParent();
                    if (!isWrappedByScrollingView(parent)) {
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                }
                final boolean canNotChildScrollDown = !isNotYetInEdgeCannotMoveFooter();
                final boolean canNotChildScrollUp = !isNotYetInEdgeCannotMoveHeader();
                if (mPreventForAnotherDirection) {
                    if (mDealAnotherDirectionMove && isMovingHeader() && !canNotChildScrollUp) {
                        mPreventForAnotherDirection = false;
                    } else if (mDealAnotherDirectionMove
                            && isMovingFooter()
                            && !canNotChildScrollDown) {
                        mPreventForAnotherDirection = false;
                    } else {
                        return super.dispatchTouchEvent(ev);
                    }
                }
                mIndicatorSetter.onFingerMove(ev.getX(index), ev.getY(index));
                final float offset = mIndicator.getOffset();
                boolean movingDown = offset > 0;
                if (!movingDown
                        && isDisabledLoadMoreWhenContentNotFull()
                        && mIndicator.isAlreadyHere(IIndicator.START_POS)
                        && canNotChildScrollDown
                        && canNotChildScrollUp) {
                    return dispatchTouchEventSuper(ev);
                }
                boolean canMoveUp = isMovingHeader() && mIndicator.hasLeftStartPosition();
                boolean canMoveDown = isMovingFooter() && mIndicator.hasLeftStartPosition();
                boolean canHeaderMoveDown = canNotChildScrollUp && !isDisabledRefresh();
                boolean canFooterMoveUp = canNotChildScrollDown && !isDisabledLoadMore();
                if (!canMoveUp && !canMoveDown) {
                    if ((movingDown && !canHeaderMoveDown) || (!movingDown && !canFooterMoveUp)) {
                        if (isLoadingMore() && mIndicator.hasLeftStartPosition()) {
                            moveFooterPos(offset);
                            if (oldTouchHanding) {
                                return true;
                            }
                        } else if (isRefreshing() && mIndicator.hasLeftStartPosition()) {
                            moveHeaderPos(offset);
                            if (oldTouchHanding) {
                                return true;
                            }
                        }
                    } else if (movingDown) {
                        if (!isDisabledRefresh()) {
                            moveHeaderPos(offset);
                            if (oldTouchHanding) {
                                return true;
                            }
                        }
                    } else if (!isDisabledLoadMore()) {
                        moveFooterPos(offset);
                        if (oldTouchHanding) {
                            return true;
                        }
                    }
                } else if (canMoveUp) {
                    if (isDisabledRefresh()) {
                        return dispatchTouchEventSuper(ev);
                    }
                    if ((!canHeaderMoveDown && movingDown)) {
                        if (oldTouchHanding) {
                            sendDownEvent(ev);
                            return true;
                        }
                        return dispatchTouchEventSuper(ev);
                    }
                    moveHeaderPos(offset);
                    if (oldTouchHanding) {
                        return true;
                    }
                } else {
                    if (isDisabledLoadMore()) {
                        return dispatchTouchEventSuper(ev);
                    }
                    if ((!canFooterMoveUp && !movingDown)) {
                        if (oldTouchHanding) {
                            sendDownEvent(ev);
                            return true;
                        }
                        return dispatchTouchEventSuper(ev);
                    }
                    moveFooterPos(offset);
                    if (oldTouchHanding) {
                        return true;
                    }
                }
        }
        return dispatchTouchEventSuper(ev);
    }

    protected void tryToDealAnotherDirectionMove(float offsetX, float offsetY) {
        if (isDisabledWhenAnotherDirectionMove()) {
            if ((Math.abs(offsetX) >= mTouchSlop && Math.abs(offsetX) > Math.abs(offsetY))) {
                mPreventForAnotherDirection = true;
                mDealAnotherDirectionMove = true;
            } else if (Math.abs(offsetX) < mTouchSlop && Math.abs(offsetY) < mTouchSlop) {
                mDealAnotherDirectionMove = false;
                mPreventForAnotherDirection = true;
            } else {
                mDealAnotherDirectionMove = true;
                mPreventForAnotherDirection = false;
            }
        } else {
            mPreventForAnotherDirection =
                    Math.abs(offsetX) < mTouchSlop && Math.abs(offsetY) < mTouchSlop;
            if (!mPreventForAnotherDirection) {
                mDealAnotherDirectionMove = true;
            }
        }
    }

    protected boolean tryToFilterTouchEvent(MotionEvent ev) {
        if (mIsInterceptTouchEventInOnceTouch) {
            if ((!isAutoRefresh()
                            && mIndicator.isAlreadyHere(IIndicator.START_POS)
                            && !mScrollChecker.mIsScrolling)
                    || (isAutoRefresh() && (isRefreshing() || isLoadingMore()))) {
                mScrollChecker.stop();
                if (ev != null) {
                    makeNewTouchDownEvent(ev);
                }
                mIsInterceptTouchEventInOnceTouch = false;
            }
            return true;
        }
        if (mIsLastOverScrollCanNotAbort) {
            if (mIndicator.isAlreadyHere(IIndicator.START_POS) && !mScrollChecker.mIsScrolling) {
                if (ev != null) {
                    makeNewTouchDownEvent(ev);
                }
                mIsLastOverScrollCanNotAbort = false;
            }
            return true;
        }
        if (mIsSpringBackCanNotBeInterrupted) {
            if (isEnabledNoMoreDataAndNoSpringBack()) {
                mIsSpringBackCanNotBeInterrupted = false;
                return false;
            }
            if (mIndicator.isAlreadyHere(IIndicator.START_POS) && !mScrollChecker.mIsScrolling) {
                if (ev != null) {
                    makeNewTouchDownEvent(ev);
                }
                mIsSpringBackCanNotBeInterrupted = false;
            }
            return true;
        }
        return false;
    }

    private NestedScrollingChildHelper getScrollingChildHelper() {
        if (mNestedScrollingChildHelper == null) {
            mNestedScrollingChildHelper = new NestedScrollingChildHelper(this);
        }
        return mNestedScrollingChildHelper;
    }

    private void scrollToTriggeredAutomatic(boolean isRefresh) {
        if (sDebug) {
            Log.d(TAG, "scrollToTriggeredAutomatic()");
        }
        switch (mAutomaticAction) {
            case Constants.ACTION_NOTHING:
                if (isRefresh) {
                    triggeredRefresh(false);
                } else {
                    triggeredLoadMore(false);
                }
                break;
            case Constants.ACTION_NOTIFY:
                mFlag |= FLAG_AUTO_REFRESH;
                break;
            case Constants.ACTION_AT_ONCE:
                if (isRefresh) {
                    triggeredRefresh(true);
                } else {
                    triggeredLoadMore(true);
                }
                break;
        }
        int offset;
        if (isRefresh) {
            if (isEnabledKeepRefreshView()) {
                final int offsetToKeepHeaderWhileLoading =
                        mIndicator.getOffsetToKeepHeaderWhileLoading();
                final int offsetToRefresh = mIndicator.getOffsetToRefresh();
                offset = Math.max(offsetToKeepHeaderWhileLoading, offsetToRefresh);
            } else {
                offset = mIndicator.getOffsetToRefresh();
            }
        } else {
            if (isEnabledKeepRefreshView()) {
                final int offsetToKeepFooterWhileLoading =
                        mIndicator.getOffsetToKeepFooterWhileLoading();
                final int offsetToLoadMore = mIndicator.getOffsetToLoadMore();
                offset = Math.max(offsetToKeepFooterWhileLoading, offsetToLoadMore);
            } else {
                offset = mIndicator.getOffsetToLoadMore();
            }
        }
        mAutomaticActionTriggered = true;
        mScrollChecker.scrollTo(
                offset,
                mAutomaticActionUseSmoothScroll
                        ? isRefresh ? mDurationToCloseHeader : mDurationToCloseFooter
                        : 0);
    }

    protected boolean isNeedInterceptTouchEvent() {
        return (isEnabledInterceptEventWhileLoading() && (isRefreshing() || isLoadingMore()))
                || mAutomaticActionUseSmoothScroll;
    }

    protected boolean isNeedFilterTouchEvent() {
        return mIsLastOverScrollCanNotAbort
                || mIsSpringBackCanNotBeInterrupted
                || mIsInterceptTouchEventInOnceTouch;
    }

    protected boolean isCanNotAbortOverScrolling() {
        return ((mScrollChecker.isFling()
                        || mScrollChecker.isFlingBack()
                        || mScrollChecker.isPreFling())
                && (((isMovingHeader() && isDisabledRefresh()))
                        || (isMovingFooter() && isDisabledLoadMore())));
    }

    public boolean isNotYetInEdgeCannotMoveHeader() {
        final View targetView = getScrollTargetView();
        if (mInEdgeCanMoveHeaderCallBack != null) {
            return mInEdgeCanMoveHeaderCallBack.isNotYetInEdgeCannotMoveHeader(
                    this, targetView, mHeaderView);
        }
        return targetView != null && targetView.canScrollVertically(-1);
    }

    public boolean isNotYetInEdgeCannotMoveFooter() {
        final View targetView = getScrollTargetView();
        if (mInEdgeCanMoveFooterCallBack != null) {
            return mInEdgeCanMoveFooterCallBack.isNotYetInEdgeCannotMoveFooter(
                    this, targetView, mFooterView);
        }
        return targetView != null && targetView.canScrollVertically(1);
    }

    protected void makeNewTouchDownEvent(MotionEvent ev) {
        if (sDebug) {
            Log.d(TAG, "makeNewTouchDownEvent()");
        }
        sendCancelEvent(ev);
        sendDownEvent(ev);
        mOffsetConsumed = 0;
        mOffsetTotal = 0;
        mOffsetRemaining = mTouchSlop * 3;
        mIndicatorSetter.onFingerUp();
        mIndicatorSetter.onFingerDown(ev.getX(), ev.getY());
    }

    protected void sendCancelEvent(MotionEvent event) {
        if (mHasSendCancelEvent || (event == null && mLastMoveEvent == null)) {
            return;
        }
        if (sDebug) {
            Log.d(TAG, "sendCancelEvent()");
        }
        final MotionEvent last = event == null ? mLastMoveEvent : event;
        final long now = SystemClock.uptimeMillis();
        final MotionEvent ev =
                MotionEvent.obtain(
                        now, now, MotionEvent.ACTION_CANCEL, last.getX(), last.getY(), 0);
        ev.setSource(InputDevice.SOURCE_TOUCHSCREEN);
        mHasSendCancelEvent = true;
        mHasSendDownEvent = false;
        super.dispatchTouchEvent(ev);
        ev.recycle();
    }

    protected void sendDownEvent(MotionEvent event) {
        if (mHasSendDownEvent || (event == null && mLastMoveEvent == null)) {
            return;
        }
        if (sDebug) {
            Log.d(TAG, "sendDownEvent()");
        }
        final MotionEvent last = event == null ? mLastMoveEvent : event;
        final long now = SystemClock.uptimeMillis();
        final float[] rawOffsets = mIndicator.getRawOffsets();
        final MotionEvent downEv =
                MotionEvent.obtain(
                        now,
                        now,
                        MotionEvent.ACTION_DOWN,
                        last.getX() - rawOffsets[0],
                        last.getY() - rawOffsets[1],
                        0);
        downEv.setSource(InputDevice.SOURCE_TOUCHSCREEN);
        super.dispatchTouchEvent(downEv);
        downEv.recycle();
        final MotionEvent moveEv =
                MotionEvent.obtain(now, now, MotionEvent.ACTION_MOVE, last.getX(), last.getY(), 0);
        moveEv.setSource(InputDevice.SOURCE_TOUCHSCREEN);
        mHasSendCancelEvent = false;
        mHasSendDownEvent = true;
        super.dispatchTouchEvent(moveEv);
        moveEv.recycle();
    }

    protected void notifyFingerUp() {
        if (isHeaderInProcessing() && mHeaderView != null && !isDisabledRefresh()) {
            mHeaderView.onFingerUp(this, mIndicator);
        } else if (isFooterInProcessing() && mFooterView != null && !isDisabledLoadMore()) {
            mFooterView.onFingerUp(this, mIndicator);
        }
    }

    protected void onFingerUp() {
        if (sDebug) {
            Log.d(TAG, "onFingerUp()");
        }
        notifyFingerUp();
        if (!mScrollChecker.isPreFling()) {
            if (isEnabledKeepRefreshView() && mStatus != SR_STATUS_COMPLETE) {
                if (isHeaderInProcessing()
                        && mHeaderView != null
                        && !isDisabledPerformRefresh()
                        && isMovingHeader()
                        && mIndicator.isOverOffsetToRefresh()) {
                    if (!mIndicator.isAlreadyHere(mIndicator.getOffsetToKeepHeaderWhileLoading())) {
                        mScrollChecker.scrollTo(
                                mIndicator.getOffsetToKeepHeaderWhileLoading(),
                                mDurationOfBackToHeaderHeight);
                        return;
                    }
                } else if (isFooterInProcessing()
                        && mFooterView != null
                        && !isDisabledPerformLoadMore()
                        && isMovingFooter()
                        && mIndicator.isOverOffsetToLoadMore()) {
                    if (!mIndicator.isAlreadyHere(mIndicator.getOffsetToKeepFooterWhileLoading())) {
                        mScrollChecker.scrollTo(
                                mIndicator.getOffsetToKeepFooterWhileLoading(),
                                mDurationOfBackToFooterHeight);
                        return;
                    }
                }
            }
            onRelease();
        }
    }

    protected void onRelease() {
        if (sDebug) {
            Log.d(TAG, "onRelease()");
        }
        if ((isMovingFooter() && isEnabledNoMoreDataAndNoSpringBack())) {
            mScrollChecker.stop();
            return;
        }
        tryToPerformRefresh();
        if (mStatus == SR_STATUS_COMPLETE) {
            notifyUIRefreshComplete(true, false, false);
            return;
        } else if (isEnabledKeepRefreshView()) {
            if (isHeaderInProcessing() && mHeaderView != null && !isDisabledPerformRefresh()) {
                if (isRefreshing()
                        && isMovingHeader()
                        && mIndicator.isAlreadyHere(
                                mIndicator.getOffsetToKeepHeaderWhileLoading())) {
                    return;
                } else if (isMovingHeader() && mIndicator.isOverOffsetToKeepHeaderWhileLoading()) {
                    mScrollChecker.scrollTo(
                            mIndicator.getOffsetToKeepHeaderWhileLoading(),
                            mDurationOfBackToHeaderHeight);
                    return;
                } else if (isRefreshing() && !isMovingFooter()) {
                    return;
                }
            } else if (isFooterInProcessing()
                    && mFooterView != null
                    && !isDisabledPerformLoadMore()) {
                if (isLoadingMore()
                        && isMovingFooter()
                        && mIndicator.isAlreadyHere(
                                mIndicator.getOffsetToKeepFooterWhileLoading())) {
                    return;
                } else if (isMovingFooter() && mIndicator.isOverOffsetToKeepFooterWhileLoading()) {
                    mScrollChecker.scrollTo(
                            mIndicator.getOffsetToKeepFooterWhileLoading(),
                            mDurationOfBackToFooterHeight);
                    return;
                } else if (isLoadingMore() && !isMovingHeader()) {
                    return;
                }
            }
        }
        tryScrollBackToTop();
    }

    protected void tryScrollBackToTop() {
        // Use the current percentage duration of the current position to scroll back to the top
        if (mScrollChecker.isFlingBack()) {
            tryScrollBackToTop(mDurationOfFlingBack);
        } else if (isMovingHeader()) {
            tryScrollBackToTop(mDurationToCloseHeader);
        } else if (isMovingFooter()) {
            tryScrollBackToTop(mDurationToCloseFooter);
        } else {
            tryToNotifyReset();
        }
    }

    protected void tryScrollBackToTop(int duration) {
        if (sDebug) {
            Log.d(TAG, String.format("tryScrollBackToTop(): duration: %d", duration));
        }
        if (mIndicator.hasLeftStartPosition()
                && (!mIndicator.hasTouched() || !mIndicator.hasMoved())) {
            mScrollChecker.scrollTo(IIndicator.START_POS, duration);
            return;
        }
        if (isNeedFilterTouchEvent() && mIndicator.hasLeftStartPosition()) {
            mScrollChecker.scrollTo(IIndicator.START_POS, duration);
            return;
        }
        tryToNotifyReset();
    }

    protected void moveHeaderPos(float delta) {
        if (sDebug) {
            Log.d(TAG, String.format("moveHeaderPos(): delta: %f", delta));
        }
        mNeedFilterScrollEvent = false;
        if (!mNestedScrolling
                && !mHasSendCancelEvent
                && isEnabledOldTouchHandling()
                && mIndicator.hasTouched()
                && !mIndicator.isAlreadyHere(IIndicator.START_POS)) sendCancelEvent(null);
        mIndicatorSetter.setMovingStatus(Constants.MOVING_HEADER);
        if (mHeaderView != null) {
            if (delta > 0) {
                final float maxHeaderDistance = mIndicator.getCanMoveTheMaxDistanceOfHeader();
                final int current = mIndicator.getCurrentPos();
                final boolean isFling = mScrollChecker.isFling() || mScrollChecker.isPreFling();
                if (maxHeaderDistance > 0) {
                    if (current >= maxHeaderDistance) {
                        if (!mScrollChecker.mIsScrolling || isFling) {
                            updateAnotherDirectionPos();
                            return;
                        }
                    } else if (current + delta > maxHeaderDistance) {
                        if (!mScrollChecker.mIsScrolling || isFling) {
                            delta = maxHeaderDistance - current;
                            if (isFling) {
                                mScrollChecker.mScroller.forceFinished(true);
                            }
                        }
                    }
                }
            } else {
                // check if it is needed to compatible scroll
                if ((mFlag & FLAG_ENABLE_COMPAT_SYNC_SCROLL) > 0
                        && !isEnabledPinContentView()
                        && mIsLastRefreshSuccessful
                        && mStatus == SR_STATUS_COMPLETE
                        && isNotYetInEdgeCannotMoveHeader()) {
                    if (sDebug) {
                        Log.d(
                                TAG,
                                String.format(
                                        "moveHeaderPos(): compatible scroll delta: %f", delta));
                    }
                    mNeedFilterScrollEvent = true;
                    tryToCompatSyncScroll(getScrollTargetView(), delta);
                }
            }
        }
        movePos(delta);
    }

    protected void moveFooterPos(float delta) {
        if (sDebug) {
            Log.d(TAG, String.format("moveFooterPos(): delta: %f", delta));
        }
        mNeedFilterScrollEvent = false;
        if (!mNestedScrolling
                && !mHasSendCancelEvent
                && isEnabledOldTouchHandling()
                && mIndicator.hasTouched()
                && !mIndicator.isAlreadyHere(IIndicator.START_POS)) sendCancelEvent(null);
        mIndicatorSetter.setMovingStatus(Constants.MOVING_FOOTER);
        if (mFooterView != null) {
            if (delta < 0) {
                final float maxFooterDistance = mIndicator.getCanMoveTheMaxDistanceOfFooter();
                final int current = mIndicator.getCurrentPos();
                final boolean isFling = mScrollChecker.isFling() || mScrollChecker.isPreFling();
                if (maxFooterDistance > 0) {
                    if (current >= maxFooterDistance) {
                        if (!mScrollChecker.mIsScrolling || isFling) {
                            updateAnotherDirectionPos();
                            return;
                        }
                    } else if (current - delta > maxFooterDistance) {
                        if (!mScrollChecker.mIsScrolling || isFling) {
                            delta = current - maxFooterDistance;
                            if (isFling) {
                                mScrollChecker.mScroller.forceFinished(true);
                            }
                        }
                    }
                }
            } else {
                // check if it is needed to compatible scroll
                if ((mFlag & FLAG_ENABLE_COMPAT_SYNC_SCROLL) > 0
                        && !isEnabledPinContentView()
                        && mIsLastRefreshSuccessful
                        && mStatus == SR_STATUS_COMPLETE
                        && isNotYetInEdgeCannotMoveFooter()) {
                    if (sDebug) {
                        Log.d(
                                TAG,
                                String.format(
                                        "moveFooterPos(): compatible scroll delta: %f", delta));
                    }
                    mNeedFilterScrollEvent = true;
                    tryToCompatSyncScroll(getScrollTargetView(), delta);
                }
            }
        }
        movePos(-delta);
    }

    protected void tryToCompatSyncScroll(View view, float delta) {
        if (mSyncScrollCallback != null) {
            mSyncScrollCallback.onScroll(view, delta);
        } else {
            if (!ScrollCompat.scrollCompat(view, delta)) {
                Log.w(TAG, "tryToCompatSyncScroll(): scrollCompat failed!");
            }
        }
    }

    protected void movePos(float delta) {
        if (delta == 0f) {
            if (sDebug) {
                Log.d(TAG, "movePos(): delta is zero");
            }
            return;
        }
        int to = (int) (mIndicator.getCurrentPos() + delta);
        // over top
        if (to < IIndicator.START_POS && mLayoutManager.isNeedFilterOverTop(delta)) {
            to = IIndicator.START_POS;
            if (sDebug) {
                Log.d(TAG, "movePos(): over top");
            }
        }
        mIndicatorSetter.setCurrentPos(to);
        int change = to - mIndicator.getLastPos();
        updatePos(isMovingFooter() ? -change : change);
    }

    /**
     * Update view's Y position
     *
     * @param change The changed value
     */
    protected void updatePos(int change) {
        if (sDebug) {
            Log.d(
                    TAG,
                    String.format(
                            "updatePos(): change: %d, current: %d last: %d",
                            change, mIndicator.getCurrentPos(), mIndicator.getLastPos()));
        }
        // leave initiated position or just refresh complete
        if ((mIndicator.hasJustLeftStartPosition() || mViewStatus == SR_VIEW_STATUS_INIT)
                && mStatus == SR_STATUS_INIT) {
            final byte old = mStatus;
            mStatus = SR_STATUS_PREPARE;
            notifyStatusChanged(old, mStatus);
            if (isMovingHeader()) {
                mViewStatus = SR_VIEW_STATUS_HEADER_IN_PROCESSING;
                if (mHeaderView != null) {
                    mHeaderView.onRefreshPrepare(this);
                }
            } else if (isMovingFooter()) {
                mViewStatus = SR_VIEW_STATUS_FOOTER_IN_PROCESSING;
                if (mFooterView != null) {
                    mFooterView.onRefreshPrepare(this);
                }
            }
        }
        tryToPerformRefreshWhenMoved();
        notifyUIPositionChanged();
        boolean needRequestLayout =
                mLayoutManager.offsetChild(
                        mHeaderView,
                        mFooterView,
                        mStickyHeaderView,
                        mStickyFooterView,
                        mTargetView,
                        change);
        // back to initiated position
        if (!(isAutoRefresh() && mStatus != SR_STATUS_COMPLETE)
                && mIndicator.hasJustBackToStartPosition()) {
            tryToNotifyReset();
            if (isEnabledOldTouchHandling()) {
                if (mIndicator.hasTouched() && !mNestedScrolling && !mHasSendDownEvent) {
                    sendDownEvent(null);
                }
            }
        }
        if (needRequestLayout) {
            requestLayout();
        } else if (mIndicator.isAlreadyHere(IIndicator.START_POS)) {
            invalidate();
        }
    }

    protected void tryToPerformRefreshWhenMoved() {
        // try to perform refresh
        if (mStatus == SR_STATUS_PREPARE && !isAutoRefresh()) {
            // reach fresh height while moving from top to bottom or reach load more height while
            // moving from bottom to top
            if (isHeaderInProcessing() && isMovingHeader() && !isDisabledPerformRefresh()) {
                if (isEnabledPullToRefresh() && mIndicator.isOverOffsetToRefresh()) {
                    triggeredRefresh(true);
                } else if (isEnabledPerformFreshWhenFling()
                        && !mIndicator.hasTouched()
                        && !(mScrollChecker.isPreFling() || mScrollChecker.isFling())
                        && mIndicator.isJustReturnedOffsetToRefresh()) {
                    mScrollChecker.stop();
                    triggeredRefresh(true);
                }
            } else if (isFooterInProcessing() && isMovingFooter() && !isDisabledPerformLoadMore()) {
                if (isEnabledPullToRefresh() && mIndicator.isOverOffsetToLoadMore()) {
                    triggeredLoadMore(true);
                } else if (isEnabledPerformFreshWhenFling()
                        && !mIndicator.hasTouched()
                        && !(mScrollChecker.isPreFling() || mScrollChecker.isFling())
                        && mIndicator.isJustReturnedOffsetToLoadMore()) {
                    mScrollChecker.stop();
                    triggeredLoadMore(true);
                }
            }
        }
    }

    /** We need to notify the X pos changed */
    protected void updateAnotherDirectionPos() {
        if (mHeaderView != null
                && !isDisabledRefresh()
                && isMovingHeader()
                && mHeaderView.getView().getVisibility() == VISIBLE) {
            if (isHeaderInProcessing()) {
                mHeaderView.onRefreshPositionChanged(this, mStatus, mIndicator);
            } else {
                mHeaderView.onPureScrollPositionChanged(this, mStatus, mIndicator);
            }
        } else if (mFooterView != null
                && !isDisabledLoadMore()
                && isMovingFooter()
                && mFooterView.getView().getVisibility() == VISIBLE) {
            if (isFooterInProcessing()) {
                mFooterView.onRefreshPositionChanged(this, mStatus, mIndicator);
            } else {
                mFooterView.onPureScrollPositionChanged(this, mStatus, mIndicator);
            }
        }
    }

    public boolean isMovingHeader() {
        return mIndicator.getMovingStatus() == Constants.MOVING_HEADER;
    }

    public boolean isMovingContent() {
        return mIndicator.getMovingStatus() == Constants.MOVING_CONTENT;
    }

    public boolean isMovingFooter() {
        return mIndicator.getMovingStatus() == Constants.MOVING_FOOTER;
    }

    public boolean isHeaderInProcessing() {
        return mViewStatus == SR_VIEW_STATUS_HEADER_IN_PROCESSING;
    }

    public boolean isFooterInProcessing() {
        return mViewStatus == SR_VIEW_STATUS_FOOTER_IN_PROCESSING;
    }

    protected void tryToDispatchNestedFling() {
        if (mScrollChecker.isPreFling() && mIndicator.isAlreadyHere(IIndicator.START_POS)) {
            if (sDebug) {
                Log.d(TAG, "tryToDispatchNestedFling()");
            }
            final int velocity = (int) (mScrollChecker.getCurrVelocity() + 0.5f);
            mIndicatorSetter.setMovingStatus(Constants.MOVING_CONTENT);
            if (isEnabledOverScroll()
                    && !(isDisabledLoadMoreWhenContentNotFull()
                            && !isNotYetInEdgeCannotMoveHeader()
                            && !isNotYetInEdgeCannotMoveFooter())) {
                mScrollChecker.startFling(velocity);
            } else {
                mScrollChecker.stop();
            }
            dispatchNestedFling(velocity);
            postInvalidateDelayed(30);
        }
    }

    protected boolean tryToNotifyReset() {
        if ((mStatus == SR_STATUS_COMPLETE || mStatus == SR_STATUS_PREPARE)
                && mIndicator.isAlreadyHere(IIndicator.START_POS)) {
            if (sDebug) {
                Log.d(TAG, "tryToNotifyReset()");
            }
            if (mHeaderView != null) {
                mHeaderView.onReset(this);
            }
            if (mFooterView != null) {
                mFooterView.onReset(this);
            }
            final byte old = mStatus;
            mStatus = SR_STATUS_INIT;
            notifyStatusChanged(old, mStatus);
            mViewStatus = SR_VIEW_STATUS_INIT;
            mAutomaticActionTriggered = true;
            mNeedFilterScrollEvent = false;
            tryToResetMovingStatus();
            if (!mIndicator.hasTouched()) {
                mIsSpringBackCanNotBeInterrupted = false;
            }
            if (mScrollChecker.isSpringBack()
                    || mScrollChecker.isSpring()
                    || mScrollChecker.isFlingBack()) {
                mScrollChecker.stop();
            }
            mLayoutManager.resetLayout(
                    mHeaderView, mFooterView, mStickyHeaderView, mStickyFooterView, mTargetView);
            if (getParent() != null) {
                getParent().requestDisallowInterceptTouchEvent(false);
            }
            return true;
        }
        return false;
    }

    protected void performRefreshComplete(
            boolean hook, boolean immediatelyNoScrolling, boolean notifyViews) {
        if (sDebug) {
            Log.d(
                    TAG,
                    String.format(
                            "performRefreshComplete(): hook: %b, immediatelyNoScrolling: %b, notifyViews: %b",
                            hook, immediatelyNoScrolling, notifyViews));
        }
        if (isRefreshing()
                && hook
                && mHeaderRefreshCompleteHook != null
                && mHeaderRefreshCompleteHook.mCallBack != null) {
            mHeaderRefreshCompleteHook.mLayout = this;
            mHeaderRefreshCompleteHook.mNotifyViews = notifyViews;
            mHeaderRefreshCompleteHook.doHook();
            return;
        }
        if (isLoadingMore()
                && hook
                && mFooterRefreshCompleteHook != null
                && mFooterRefreshCompleteHook.mCallBack != null) {
            mFooterRefreshCompleteHook.mLayout = this;
            mFooterRefreshCompleteHook.mNotifyViews = notifyViews;
            mFooterRefreshCompleteHook.doHook();
            return;
        }
        if ((mFlag & FLAG_BEEN_SET_NO_MORE_DATA) <= 0) {
            if (mIsLastRefreshSuccessful) {
                mFlag = mFlag & ~(FLAG_ENABLE_NO_MORE_DATA | FLAG_ENABLE_NO_MORE_DATA_NO_BACK);
            }
        } else {
            mFlag = mFlag & ~FLAG_BEEN_SET_NO_MORE_DATA;
        }
        final byte old = mStatus;
        mStatus = SR_STATUS_COMPLETE;
        notifyStatusChanged(old, mStatus);
        notifyUIRefreshComplete(
                !(isMovingFooter() && isEnabledNoMoreDataAndNoSpringBack()),
                immediatelyNoScrolling,
                notifyViews);
    }

    protected void notifyUIRefreshComplete(
            boolean useScroll, boolean immediatelyNoScrolling, boolean notifyViews) {
        if (sDebug) {
            Log.d(
                    TAG,
                    String.format(
                            "notifyUIRefreshComplete(): useScroll: %b, immediatelyNoScrolling: %b, notifyViews: %b",
                            useScroll, immediatelyNoScrolling, notifyViews));
        }
        mIsSpringBackCanNotBeInterrupted = true;
        if (notifyViews) {
            if (isHeaderInProcessing() && mHeaderView != null) {
                mHeaderView.onRefreshComplete(this, mIsLastRefreshSuccessful);
            } else if (isFooterInProcessing() && mFooterView != null) {
                mFooterView.onRefreshComplete(this, mIsLastRefreshSuccessful);
            }
        }
        if (useScroll) {
            if (mScrollChecker.isFlingBack()) {
                mScrollChecker.stop();
            }
            if (immediatelyNoScrolling) {
                tryScrollBackToTop(0);
            } else {
                tryScrollBackToTop();
            }
        }
    }

    /** try to perform refresh or loading , if performed return true */
    protected void tryToPerformRefresh() {
        // status not be prepare or over scrolling or moving content go to break;
        if (mStatus != SR_STATUS_PREPARE || isMovingContent()) {
            return;
        }
        if (sDebug) {
            Log.d(TAG, "tryToPerformRefresh()");
        }
        final boolean isEnabledKeep = isEnabledKeepRefreshView();
        if (isHeaderInProcessing() && !isDisabledPerformRefresh() && mHeaderView != null) {
            if ((isEnabledKeep && mIndicator.isAlreadyHere(mIndicator.getOffsetToRefresh())
                    || mIndicator.isAlreadyHere(mIndicator.getOffsetToKeepHeaderWhileLoading()))) {
                triggeredRefresh(true);
                return;
            }
        }
        if (isFooterInProcessing() && !isDisabledPerformLoadMore() && mFooterView != null) {
            if ((isEnabledKeep && mIndicator.isAlreadyHere(mIndicator.getOffsetToLoadMore())
                    || mIndicator.isAlreadyHere(mIndicator.getOffsetToKeepFooterWhileLoading()))) {
                triggeredLoadMore(true);
            }
        }
    }

    protected void tryScrollToPerformAutoRefresh() {
        if (isMovingContent() && (mStatus == SR_STATUS_INIT || mStatus == SR_STATUS_PREPARE)) {
            if ((isEnabledAutoLoadMore() && !isDisabledPerformLoadMore())
                    || (isEnabledAutoRefresh() && !isDisabledPerformRefresh())) {
                if (sDebug) {
                    Log.d(TAG, "tryScrollToPerformAutoRefresh()");
                }
                final View targetView = getScrollTargetView();
                if (targetView != null) {
                    if (isEnabledAutoLoadMore() && canAutoLoadMore(targetView)) {
                        if (!isDisabledLoadMoreWhenContentNotFull()
                                || isNotYetInEdgeCannotMoveHeader()
                                || isNotYetInEdgeCannotMoveFooter()) {
                            triggeredLoadMore(true);
                        }
                    } else if (isEnabledAutoRefresh() && canAutoRefresh(targetView)) {
                        triggeredRefresh(true);
                    }
                }
            }
        }
    }

    protected boolean canAutoLoadMore(View view) {
        if (mAutoLoadMoreCallBack != null) {
            return mAutoLoadMoreCallBack.canAutoLoadMore(this, view);
        }
        return ScrollCompat.canAutoLoadMore(view);
    }

    protected boolean canAutoRefresh(View view) {
        if (mAutoRefreshCallBack != null) {
            return mAutoRefreshCallBack.canAutoRefresh(this, view);
        }
        return ScrollCompat.canAutoRefresh(view);
    }

    protected void triggeredRefresh(boolean notify) {
        if (sDebug) {
            Log.d(TAG, "triggeredRefresh()");
        }
        byte old = mStatus;
        if (old != SR_STATUS_PREPARE) {
            notifyStatusChanged(old, SR_STATUS_PREPARE);
            old = SR_STATUS_PREPARE;
            if (mHeaderView != null) {
                mHeaderView.onRefreshPrepare(this);
            }
        }
        mStatus = SR_STATUS_REFRESHING;
        notifyStatusChanged(old, mStatus);
        mViewStatus = SR_VIEW_STATUS_HEADER_IN_PROCESSING;
        mFlag &= ~FLAG_AUTO_REFRESH;
        mIsSpringBackCanNotBeInterrupted = false;
        performRefresh(notify);
    }

    protected void triggeredLoadMore(boolean notify) {
        if (sDebug) {
            Log.d(TAG, "triggeredLoadMore()");
        }
        byte old = mStatus;
        if (old != SR_STATUS_PREPARE) {
            notifyStatusChanged(old, SR_STATUS_PREPARE);
            old = SR_STATUS_PREPARE;
            if (mFooterView != null) {
                mFooterView.onRefreshPrepare(this);
            }
        }
        mStatus = SR_STATUS_LOADING_MORE;
        notifyStatusChanged(old, mStatus);
        mViewStatus = SR_VIEW_STATUS_FOOTER_IN_PROCESSING;
        mFlag &= ~FLAG_AUTO_REFRESH;
        mIsSpringBackCanNotBeInterrupted = false;
        performRefresh(notify);
    }

    protected void tryToResetMovingStatus() {
        if (mIndicator.isAlreadyHere(IIndicator.START_POS) && !isMovingContent()) {
            mIndicatorSetter.setMovingStatus(Constants.MOVING_CONTENT);
            notifyUIPositionChanged();
        }
    }

    protected void performRefresh(boolean notify) {
        // loading start milliseconds since boot
        mLoadingStartTime = SystemClock.uptimeMillis();
        if (sDebug) {
            Log.d(TAG, String.format("onRefreshBegin systemTime: %d", mLoadingStartTime));
        }
        if (isRefreshing()) {
            if (mHeaderView != null) {
                mHeaderView.onRefreshBegin(this, mIndicator);
            }
        } else if (isLoadingMore()) {
            if (mFooterView != null) {
                mFooterView.onRefreshBegin(this, mIndicator);
            }
        }
        if (notify && mRefreshListener != null) {
            if (isRefreshing()) {
                mRefreshListener.onRefreshing();
            } else {
                mRefreshListener.onLoadingMore();
            }
        }
    }

    protected void dispatchNestedFling(int velocity) {
        if (sDebug) {
            Log.d(TAG, String.format("dispatchNestedFling() : velocity: %d", velocity));
        }
        final View targetView = getScrollTargetView();
        ScrollCompat.flingCompat(targetView, -velocity);
    }

    private void notifyUIPositionChanged() {
        final List<OnUIPositionChangedListener> listeners = mUIPositionChangedListeners;
        if (listeners != null) {
            for (OnUIPositionChangedListener listener : listeners) {
                listener.onChanged(mStatus, mIndicator);
            }
        }
    }

    protected void notifyStatusChanged(byte old, byte now) {
        final List<OnStatusChangedListener> listeners = mStatusChangedListeners;
        if (listeners != null) {
            for (OnStatusChangedListener listener : listeners) {
                listener.onStatusChanged(old, now);
            }
        }
    }

    private View foundViewInViewGroupById(ViewGroup group, int id) {
        final int size = group.getChildCount();
        for (int i = 0; i < size; i++) {
            View view = group.getChildAt(i);
            if (view.getId() == id) {
                return view;
            } else if (view instanceof ViewGroup) {
                final View found = foundViewInViewGroupById((ViewGroup) view, id);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    /**
     * Classes that wish to override {@link SmoothRefreshLayout#isNotYetInEdgeCannotMoveHeader()}
     * method behavior
     */
    public interface OnHeaderEdgeDetectCallBack {
        /**
         * Callback that will be called when {@link
         * SmoothRefreshLayout#isNotYetInEdgeCannotMoveHeader()} method is called to allow the
         * implementer to override its behavior.
         *
         * @param parent SmoothRefreshLayout that this callback is overriding.
         * @param child The child view.
         * @param header The Header view.
         * @return Whether it is possible for the child view of parent layout to scroll up.
         */
        boolean isNotYetInEdgeCannotMoveHeader(
                SmoothRefreshLayout parent, @Nullable View child, @Nullable IRefreshView header);
    }

    /**
     * Classes that wish to override {@link SmoothRefreshLayout#isNotYetInEdgeCannotMoveFooter()}
     * method behavior
     */
    public interface OnFooterEdgeDetectCallBack {
        /**
         * Callback that will be called when {@link
         * SmoothRefreshLayout#isNotYetInEdgeCannotMoveFooter()} method is called to allow the
         * implementer to override its behavior.
         *
         * @param parent SmoothRefreshLayout that this callback is overriding.
         * @param child The child view.
         * @param footer The Footer view.
         * @return Whether it is possible for the child view of parent layout to scroll down.
         */
        boolean isNotYetInEdgeCannotMoveFooter(
                SmoothRefreshLayout parent, @Nullable View child, @Nullable IRefreshView footer);
    }

    /** Classes that wish to be notified when the swipe gesture correctly triggers a refresh */
    public interface OnRefreshListener {
        /** Called when a refresh is triggered. */
        void onRefreshing();

        /** Called when a load more is triggered. */
        void onLoadingMore();
    }

    /** Classes that wish to be notified when the views position changes */
    public interface OnUIPositionChangedListener {
        /**
         * UI position changed
         *
         * @param status {@link #SR_STATUS_INIT}, {@link #SR_STATUS_PREPARE}, {@link
         *     #SR_STATUS_REFRESHING},{@link #SR_STATUS_LOADING_MORE},{@link #SR_STATUS_COMPLETE}.
         * @param indicator @see {@link IIndicator}
         */
        void onChanged(byte status, IIndicator indicator);
    }

    /** Classes that wish to be called when refresh completed spring back to start position */
    public interface OnSyncScrollCallback {
        /**
         * Called when refresh completed spring back to start position, each move triggers a
         * callback once
         *
         * @param content The content view
         * @param delta The scroll distance in current axis
         */
        void onScroll(View content, float delta);
    }

    public interface OnHookUIRefreshCompleteCallBack {
        void onHook(RefreshCompleteHook hook);
    }

    /**
     * Classes that wish to be called when {@link
     * SmoothRefreshLayout#setEnableAutoLoadMore(boolean)} has been set true and {@link
     * SmoothRefreshLayout#isDisabledLoadMore()} not be true and sure you need to customize the
     * specified trigger rule
     */
    public interface OnPerformAutoLoadMoreCallBack {
        /**
         * Whether need trigger auto load more
         *
         * @param parent The frame
         * @param child the child view
         * @return whether need trigger
         */
        boolean canAutoLoadMore(SmoothRefreshLayout parent, @Nullable View child);
    }

    /**
     * Classes that wish to be called when {@link SmoothRefreshLayout#setEnableAutoRefresh(boolean)}
     * has been set true and {@link SmoothRefreshLayout#isDisabledRefresh()} not be true and sure
     * you need to customize the specified trigger rule
     */
    public interface OnPerformAutoRefreshCallBack {
        /**
         * Whether need trigger auto refresh
         *
         * @param parent The frame
         * @param child the child view
         * @return whether need trigger
         */
        boolean canAutoRefresh(SmoothRefreshLayout parent, @Nullable View child);
    }

    /** Classes that wish to be notified when the status changed */
    public interface OnStatusChangedListener {
        /**
         * Status changed
         *
         * @param old the old status, as follows {@link #SR_STATUS_INIT}, {@link
         *     #SR_STATUS_PREPARE}, {@link #SR_STATUS_REFRESHING},{@link #SR_STATUS_LOADING_MORE},
         *     {@link #SR_STATUS_COMPLETE}}
         * @param now the current status, as follows {@link #SR_STATUS_INIT}, {@link
         *     #SR_STATUS_PREPARE}, {@link #SR_STATUS_REFRESHING},{@link #SR_STATUS_LOADING_MORE},
         *     {@link #SR_STATUS_COMPLETE}}
         */
        void onStatusChanged(byte old, byte now);
    }

    /** Classes that wish to override the calculate bounce duration and distance method */
    public interface OnCalculateBounceCallback {
        int onCalculateDistance(float velocity);

        int onCalculateDuration(float velocity);
    }

    public static class LayoutParams extends MarginLayoutParams {
        public int gravity = Gravity.TOP | Gravity.START;

        @SuppressWarnings("WeakerAccess")
        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            final TypedArray a =
                    c.obtainStyledAttributes(attrs, R.styleable.SmoothRefreshLayout_Layout);
            gravity =
                    a.getInt(
                            R.styleable.SmoothRefreshLayout_Layout_android_layout_gravity, gravity);
            a.recycle();
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        @SuppressWarnings("unused")
        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

        @SuppressWarnings("WeakerAccess")
        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }
    }

    public static class RefreshCompleteHook {
        private SmoothRefreshLayout mLayout;
        private OnHookUIRefreshCompleteCallBack mCallBack;
        private boolean mNotifyViews;

        public void onHookComplete(boolean immediatelyNoScrolling) {
            if (mLayout != null) {
                if (sDebug) {
                    Log.d(
                            mLayout.TAG,
                            String.format(
                                    "RefreshCompleteHook: onHookComplete(): immediatelyNoScrolling: %b",
                                    immediatelyNoScrolling));
                }
                mLayout.performRefreshComplete(false, immediatelyNoScrolling, mNotifyViews);
            }
        }

        private void doHook() {
            if (mCallBack != null) {
                if (sDebug) {
                    Log.d(mLayout.TAG, "RefreshCompleteHook: doHook()");
                }
                mCallBack.onHook(this);
            }
        }
    }

    /** Delayed completion of loading */
    private static class DelayToRefreshComplete implements Runnable {
        private SmoothRefreshLayout mLayout;
        private boolean mNotifyViews;

        @Override
        public void run() {
            if (mLayout != null) {
                if (sDebug) {
                    Log.d(mLayout.TAG, "DelayToRefreshComplete: run()");
                }
                mLayout.performRefreshComplete(true, false, mNotifyViews);
            }
        }
    }

    /** Delayed to dispatch nested fling */
    private static class DelayToDispatchNestedFling implements Runnable {
        private SmoothRefreshLayout mLayout;
        private int mVelocity;

        @Override
        public void run() {
            if (mLayout != null) {
                if (sDebug) {
                    Log.d(mLayout.TAG, "DelayToDispatchNestedFling: run()");
                }
                mLayout.dispatchNestedFling(mVelocity);
            }
        }
    }

    /** Delayed to perform auto refresh */
    private static class DelayToPerformAutoRefresh implements Runnable {
        private SmoothRefreshLayout mLayout;

        @Override
        public void run() {
            if (mLayout != null) {
                if (sDebug) {
                    Log.d(mLayout.TAG, "DelayToPerformAutoRefresh: run()");
                }
                mLayout.tryToPerformAutoRefresh();
            }
        }
    }

    public abstract static class LayoutManager {
        public static final int HORIZONTAL = 0;
        public static final int VERTICAL = 1;
        protected final String TAG = getClass().getSimpleName() + "-" + SmoothRefreshLayout.sId++;
        protected SmoothRefreshLayout mLayout;
        protected boolean mMeasureMatchParentChildren;
        protected int mOldWidthMeasureSpec;
        protected int mOldHeightMeasureSpec;

        @Orientation
        public abstract int getOrientation();

        @CallSuper
        public void setLayout(SmoothRefreshLayout layout) {
            mLayout = layout;
        }

        public void onLayoutDraw(Canvas canvas) {}

        public boolean isNeedFilterOverTop(float delta) {
            return true;
        }

        public abstract void measureHeader(
                @NonNull IRefreshView<IIndicator> header,
                int widthMeasureSpec,
                int heightMeasureSpec);

        public abstract void measureFooter(
                @NonNull IRefreshView<IIndicator> footer,
                int widthMeasureSpec,
                int heightMeasureSpec);

        public abstract void layoutHeaderView(@NonNull IRefreshView<IIndicator> header);

        public abstract void layoutFooterView(@NonNull IRefreshView<IIndicator> footer);

        public abstract void layoutContentView(@NonNull View content);

        public abstract void layoutStickyHeaderView(@NonNull View stickyHeader);

        public abstract void layoutStickyFooterView(@NonNull View stickyFooter);

        public abstract boolean offsetChild(
                @Nullable IRefreshView<IIndicator> header,
                @Nullable IRefreshView<IIndicator> footer,
                @Nullable View stickyHeader,
                @Nullable View stickyFooter,
                @Nullable View content,
                int change);

        public void resetLayout(
                @Nullable IRefreshView<IIndicator> header,
                @Nullable IRefreshView<IIndicator> footer,
                @Nullable View stickyHeader,
                @Nullable View stickyFooter,
                @Nullable View content) {}

        protected void setHeaderHeight(int height) {
            if (mLayout != null) {
                mLayout.mIndicatorSetter.setHeaderHeight(height);
            }
        }

        protected void setFooterHeight(int height) {
            if (mLayout != null) {
                mLayout.mIndicatorSetter.setFooterHeight(height);
            }
        }

        protected byte getRefreshStatus() {
            return mLayout == null ? SmoothRefreshLayout.SR_STATUS_INIT : mLayout.mStatus;
        }

        protected final void measureChildWithMargins(
                View child, int parentWidthMeasureSpec, int parentHeightMeasureSpec) {
            final ViewGroup.MarginLayoutParams lp =
                    (ViewGroup.MarginLayoutParams) child.getLayoutParams();
            final int childWidthMeasureSpec =
                    ViewGroup.getChildMeasureSpec(
                            parentWidthMeasureSpec,
                            mLayout.getPaddingLeft()
                                    + mLayout.getPaddingRight()
                                    + lp.leftMargin
                                    + lp.rightMargin,
                            lp.width);
            final int childHeightMeasureSpec =
                    ViewGroup.getChildMeasureSpec(
                            parentHeightMeasureSpec,
                            mLayout.getPaddingTop()
                                    + mLayout.getPaddingBottom()
                                    + lp.topMargin
                                    + lp.bottomMargin,
                            lp.height);
            child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
        }
    }

    class ScrollChecker implements Runnable {
        private static final float GRAVITY_EARTH = 9.80665f;
        final int mMaxDistance;
        private final float mPhysical;
        Scroller[] mCachedScroller;
        Scroller mScroller;
        Scroller mCalcScroller;
        Interpolator mInterpolator;
        float mLastY;
        float mLastStart;
        float mLastTo;
        int mDuration;
        byte mMode = Constants.SCROLLER_MODE_NONE;
        float mVelocity;
        boolean mIsScrolling = false;
        private int[] mCachedPair = new int[2];

        ScrollChecker() {
            DisplayMetrics dm = getResources().getDisplayMetrics();
            mMaxDistance = (int) (dm.heightPixels / 8f);
            mPhysical = GRAVITY_EARTH * 39.37f * dm.density * 160f * 0.84f;
            mCalcScroller = new Scroller(getContext());
            mInterpolator = SPRING_INTERPOLATOR;
            mCachedScroller =
                    new Scroller[] {
                        new Scroller(getContext(), SPRING_INTERPOLATOR),
                        new Scroller(getContext(), SPRING_BACK_INTERPOLATOR),
                        new Scroller(getContext(), FLING_INTERPOLATOR)
                    };
            mScroller = mCachedScroller[0];
        }

        @Override
        public void run() {
            if (mMode == Constants.SCROLLER_MODE_NONE || isCalcFling()) {
                return;
            }
            boolean finished = !mScroller.computeScrollOffset() && mScroller.getCurrY() == mLastY;
            int curY = mScroller.getCurrY();
            float deltaY = curY - mLastY;
            if (sDebug) {
                Log.d(
                        TAG,
                        String.format(
                                "ScrollChecker: run(): finished: %b, mode: %d, start: %f, to: %f,"
                                        + " curPos: %d, curY:%d, last: %f, delta: %f",
                                finished,
                                mMode,
                                mLastStart,
                                mLastTo,
                                mIndicator.getCurrentPos(),
                                curY,
                                mLastY,
                                deltaY));
            }
            if (!finished) {
                mLastY = curY;
                if (isMovingHeader()) {
                    moveHeaderPos(deltaY);
                } else if (isMovingFooter()) {
                    if (isPreFling()) {
                        moveFooterPos(deltaY);
                    } else {
                        moveFooterPos(-deltaY);
                    }
                }
                ViewCompat.postOnAnimation(SmoothRefreshLayout.this, this);
                tryToDispatchNestedFling();
            } else {
                switch (mMode) {
                    case Constants.SCROLLER_MODE_SPRING:
                    case Constants.SCROLLER_MODE_FLING_BACK:
                    case Constants.SCROLLER_MODE_SPRING_BACK:
                        stop();
                        if (!mIndicator.isAlreadyHere(IIndicator.START_POS)) {
                            onRelease();
                        }
                        break;
                    case Constants.SCROLLER_MODE_PRE_FLING:
                    case Constants.SCROLLER_MODE_FLING:
                        stop();
                        mMode = Constants.SCROLLER_MODE_FLING_BACK;
                        if (isEnabledPerformFreshWhenFling()
                                || isRefreshing()
                                || isLoadingMore()
                                || (isEnabledAutoLoadMore() && isMovingFooter())
                                || (isEnabledAutoRefresh() && isMovingHeader())) {
                            onRelease();
                        } else {
                            tryScrollBackToTop();
                        }
                        break;
                }
            }
        }

        boolean isPreFling() {
            return mMode == Constants.SCROLLER_MODE_PRE_FLING;
        }

        boolean isFling() {
            return mMode == Constants.SCROLLER_MODE_FLING;
        }

        boolean isFlingBack() {
            return mMode == Constants.SCROLLER_MODE_FLING_BACK;
        }

        boolean isSpringBack() {
            return mMode == Constants.SCROLLER_MODE_SPRING_BACK;
        }

        boolean isSpring() {
            return mMode == Constants.SCROLLER_MODE_SPRING;
        }

        boolean isCalcFling() {
            return mMode == Constants.SCROLLER_MODE_CALC_FLING;
        }

        float getCurrVelocity() {
            final int originalSymbol = mVelocity > 0 ? 1 : -1;
            float v = mScroller.getCurrVelocity() * originalSymbol;
            if (sDebug) {
                Log.d(TAG, String.format("ScrollChecker: getCurrVelocity(): v: %f", v));
            }
            return v;
        }

        int getFinalY(float v) {
            mCalcScroller.fling(
                    0,
                    0,
                    0,
                    (int) v,
                    Integer.MIN_VALUE,
                    Integer.MAX_VALUE,
                    Integer.MIN_VALUE,
                    Integer.MAX_VALUE);
            final int y = Math.abs(mCalcScroller.getFinalY());
            if (sDebug) {
                Log.d(
                        TAG,
                        String.format(
                                "ScrollChecker: getFinalY(): v: %s, finalY: %d, currentY: %d",
                                v, y, mIndicator.getCurrentPos()));
            }
            mCalcScroller.abortAnimation();
            return y;
        }

        void startPreFling(float v) {
            stop();
            mMode = Constants.SCROLLER_MODE_PRE_FLING;
            setInterpolator(FLING_INTERPOLATOR);
            mVelocity = v;
            mScroller.fling(
                    0,
                    0,
                    0,
                    (int) v,
                    Integer.MIN_VALUE,
                    Integer.MAX_VALUE,
                    Integer.MIN_VALUE,
                    Integer.MAX_VALUE);
            if (sDebug) {
                Log.d(TAG, String.format("ScrollChecker: startPreFling(): v: %s", v));
            }
            ViewCompat.postOnAnimation(SmoothRefreshLayout.this, this);
        }

        void startFling(float v) {
            stop();
            mMode = Constants.SCROLLER_MODE_CALC_FLING;
            setInterpolator(FLING_INTERPOLATOR);
            mVelocity = v;
            mScroller.fling(
                    0,
                    0,
                    0,
                    (int) v,
                    Integer.MIN_VALUE,
                    Integer.MAX_VALUE,
                    Integer.MIN_VALUE,
                    Integer.MAX_VALUE);
            if (sDebug) {
                Log.d(TAG, String.format("ScrollChecker: startFling(): v: %s", v));
            }
        }

        void scrollTo(int to, int duration) {
            final int curPos = mIndicator.getCurrentPos();
            if (to > curPos) {
                stop();
                setInterpolator(mSpringInterpolator);
                mMode = Constants.SCROLLER_MODE_SPRING;
            } else if (to < curPos) {
                if (!mScrollChecker.isFlingBack()) {
                    stop();
                    mMode = Constants.SCROLLER_MODE_SPRING_BACK;
                }
                setInterpolator(mSpringBackInterpolator);
            } else {
                mMode = Constants.SCROLLER_MODE_NONE;
                return;
            }
            mLastStart = curPos;
            mLastTo = to;
            if (sDebug) {
                Log.d(
                        TAG,
                        String.format(
                                "ScrollChecker: scrollTo(): to: %d, duration: %d", to, duration));
            }
            int distance = (int) (mLastTo - mLastStart);
            mLastY = 0;
            mDuration = duration;
            mIsScrolling = true;
            mScroller.startScroll(0, 0, 0, distance, duration);
            removeCallbacks(this);
            if (duration <= 0) {
                run();
            } else {
                ViewCompat.postOnAnimation(SmoothRefreshLayout.this, this);
            }
        }

        void computeScrollOffset() {
            if (mScroller.computeScrollOffset()) {
                if (sDebug) {
                    Log.d(TAG, "ScrollChecker: computeScrollOffset()");
                }
                if (isCalcFling()) {
                    if (mVelocity > 0
                            && mIndicator.isAlreadyHere(IIndicator.START_POS)
                            && !isNotYetInEdgeCannotMoveHeader()) {
                        final float velocity = Math.abs(getCurrVelocity());
                        stop();
                        mIndicatorSetter.setMovingStatus(Constants.MOVING_HEADER);
                        final int[] result = computeScroll(velocity);
                        if (getHeaderHeight() > 0 && (isRefreshing() || isEnabledAutoRefresh())) {
                            startBounce(
                                    Math.min(result[0] * 3, getHeaderHeight()),
                                    Math.min(
                                            Math.max(result[1] / 2 * 5, mMinOverScrollDuration),
                                            mMaxOverScrollDuration));
                        } else {
                            startBounce(result[0], result[1]);
                        }
                        return;
                    } else if (mVelocity < 0
                            && mIndicator.isAlreadyHere(IIndicator.START_POS)
                            && !isNotYetInEdgeCannotMoveFooter()) {
                        final float velocity = Math.abs(getCurrVelocity());
                        stop();
                        mIndicatorSetter.setMovingStatus(Constants.MOVING_FOOTER);
                        final int[] result = computeScroll(velocity);
                        if (getFooterHeight() > 0
                                && (isLoadingMore()
                                        || isEnabledAutoLoadMore()
                                        || isEnabledNoMoreData())) {
                            startBounce(
                                    Math.min(result[0] * 3, getFooterHeight()),
                                    Math.min(
                                            Math.max(result[1] / 2 * 5, mMinOverScrollDuration),
                                            mMaxOverScrollDuration));
                        } else {
                            startBounce(result[0], result[1]);
                        }
                        return;
                    }
                }
                invalidate();
            }
        }

        int[] computeScroll(float velocity) {
            int distance, duration;
            if (mCalculateBounceCallback != null) {
                distance = mCalculateBounceCallback.onCalculateDistance(velocity);
                duration = mCalculateBounceCallback.onCalculateDuration(velocity);
                mCachedPair[0] = Math.max(distance, mTouchSlop);
            } else {
                // Multiply by a given empirical value
                velocity = velocity * .535f;
                float deceleration =
                        (float)
                                Math.log(
                                        Math.abs(velocity / 4.5f)
                                                / (ViewConfiguration.getScrollFriction()
                                                        * mPhysical));
                float ratio = (float) ((Math.exp(-Math.log10(velocity) / 1.2d)) * 2f);
                distance =
                        (int)
                                ((ViewConfiguration.getScrollFriction()
                                                * mPhysical
                                                * Math.exp(deceleration))
                                        * ratio);
                duration = (int) (1000f * ratio);
                mCachedPair[0] = Math.max(Math.min(distance, mMaxDistance), mTouchSlop);
            }
            mCachedPair[1] =
                    Math.min(Math.max(duration, mMinOverScrollDuration), mMaxOverScrollDuration);
            return mCachedPair;
        }

        void startBounce(int to, int duration) {
            mMode = Constants.SCROLLER_MODE_FLING;
            setInterpolator(SPRING_INTERPOLATOR);
            mLastStart = mIndicator.getCurrentPos();
            mLastTo = to;
            if (sDebug) {
                Log.d(
                        TAG,
                        String.format(
                                "ScrollChecker: startBounce(): to: %d, duration: %d",
                                to, duration));
            }
            int distance = (int) (mLastTo - mLastStart);
            mLastY = 0;
            mDuration = duration;
            mIsScrolling = true;
            mScroller.startScroll(0, 0, 0, distance, duration);
            removeCallbacks(this);
            ViewCompat.postOnAnimation(SmoothRefreshLayout.this, this);
        }

        void setInterpolator(Interpolator interpolator) {
            if (mInterpolator == interpolator) {
                return;
            }
            if (sDebug) {
                Log.d(
                        TAG,
                        String.format(
                                "ScrollChecker: updateInterpolator(): interpolator: %s",
                                interpolator == null
                                        ? "null"
                                        : interpolator.getClass().getSimpleName()));
            }
            mInterpolator = interpolator;
            if (!mScroller.isFinished()) {
                switch (mMode) {
                    case Constants.SCROLLER_MODE_SPRING:
                    case Constants.SCROLLER_MODE_FLING_BACK:
                    case Constants.SCROLLER_MODE_SPRING_BACK:
                        mLastStart = mIndicator.getCurrentPos();
                        int distance = (int) (mLastTo - mLastStart);
                        int passed = mScroller.timePassed();
                        mScroller = makeOrGetScroller(interpolator);
                        mScroller.startScroll(0, 0, 0, distance, mDuration - passed);
                        removeCallbacks(this);
                        ViewCompat.postOnAnimation(SmoothRefreshLayout.this, this);
                        break;
                    case Constants.SCROLLER_MODE_PRE_FLING:
                    case Constants.SCROLLER_MODE_CALC_FLING:
                        final float currentVelocity = getCurrVelocity();
                        mScroller = makeOrGetScroller(interpolator);
                        if (isCalcFling()) {
                            startFling(currentVelocity);
                        } else {
                            startPreFling(currentVelocity);
                        }
                        break;
                    default:
                        mScroller = makeOrGetScroller(interpolator);
                        break;
                }
            } else {
                mScroller = makeOrGetScroller(interpolator);
            }
        }

        void stop() {
            if (mMode != Constants.SCROLLER_MODE_NONE) {
                if (sDebug) {
                    Log.d(TAG, "ScrollChecker: stop()");
                }
                if (mNestedScrolling && isCalcFling()) {
                    mMode = Constants.SCROLLER_MODE_NONE;
                    stopNestedScroll(ViewCompat.TYPE_NON_TOUCH);
                } else {
                    mMode = Constants.SCROLLER_MODE_NONE;
                }
                mAutomaticActionUseSmoothScroll = false;
                mIsScrolling = false;
                mScroller.forceFinished(true);
                mDuration = 0;
                mLastY = 0;
                mLastTo = -1;
                mLastStart = 0;
                removeCallbacks(this);
            }
        }

        private Scroller makeOrGetScroller(Interpolator interpolator) {
            if (interpolator == SPRING_INTERPOLATOR) {
                return mCachedScroller[0];
            } else if (interpolator == SPRING_BACK_INTERPOLATOR) {
                return mCachedScroller[1];
            } else if (interpolator == FLING_INTERPOLATOR) {
                return mCachedScroller[2];
            } else {
                return new Scroller(getContext(), interpolator);
            }
        }
    }
}
