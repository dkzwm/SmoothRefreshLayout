package me.dkzwm.widget.srl;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Scroller;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.annotation.IdRes;
import androidx.annotation.IntRange;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.core.view.NestedScrollingChild2;
import androidx.core.view.NestedScrollingChildHelper;
import androidx.core.view.NestedScrollingParent2;
import androidx.core.view.NestedScrollingParentHelper;
import androidx.core.view.ViewCompat;
import me.dkzwm.widget.srl.animation.ViscousFluidInterpolator;
import me.dkzwm.widget.srl.annotation.Action;
import me.dkzwm.widget.srl.annotation.Mode;
import me.dkzwm.widget.srl.config.Constants;
import me.dkzwm.widget.srl.extra.IRefreshView;
import me.dkzwm.widget.srl.indicator.DefaultIndicator;
import me.dkzwm.widget.srl.indicator.IIndicator;
import me.dkzwm.widget.srl.indicator.IIndicatorSetter;
import me.dkzwm.widget.srl.utils.BoundaryUtil;
import me.dkzwm.widget.srl.utils.SRLog;
import me.dkzwm.widget.srl.utils.ScrollCompat;


/**
 * Created by dkzwm on 2017/5/18.
 * <p>Part of the code comes from @see <a href="https://github.com/liaohuqiu/android-Ultra-Pull-To-Refresh">
 * android-Ultra-Pull-To-Refresh</a><br/>
 * 部分代码实现来自 @see <a href="https://github.com/liaohuqiu">LiaoHuQiu</a> 的UltraPullToRefresh项目</p>
 * Support NestedScroll feature;<br/>
 * Support OverScroll feature;<br/>
 * Support Refresh and LoadMore feature;<br/>
 * Support AutoRefresh feature;<br/>
 * Support AutoLoadMore feature;<br/>
 * Support MultiState feature;<br/></p>
 *
 * @author dkzwm
 */
public class SmoothRefreshLayout extends ViewGroup implements NestedScrollingChild2, NestedScrollingParent2 {
    //status
    public static final byte SR_STATUS_INIT = 1;
    public static final byte SR_STATUS_PREPARE = 2;
    public static final byte SR_STATUS_REFRESHING = 3;
    public static final byte SR_STATUS_LOADING_MORE = 4;
    public static final byte SR_STATUS_COMPLETE = 5;
    //fresh view status
    public static final byte SR_VIEW_STATUS_INIT = 21;
    public static final byte SR_VIEW_STATUS_HEADER_IN_PROCESSING = 22;
    public static final byte SR_VIEW_STATUS_FOOTER_IN_PROCESSING = 23;
    protected static final Interpolator sSpringInterpolator = new ViscousFluidInterpolator();
    protected static final Interpolator sFlingInterpolator = new DecelerateInterpolator(1.2f);
    private static final byte FLAG_AUTO_REFRESH = 0x01;
    private static final byte FLAG_ENABLE_NEXT_AT_ONCE = 0x01 << 2;
    private static final byte FLAG_ENABLE_OVER_SCROLL = 0x01 << 3;
    private static final byte FLAG_ENABLE_KEEP_REFRESH_VIEW = 0x01 << 4;
    private static final byte FLAG_ENABLE_PIN_CONTENT_VIEW = 0x01 << 5;
    private static final byte FLAG_ENABLE_PULL_TO_REFRESH = 0x01 << 6;
    private static final int FLAG_ENABLE_PIN_REFRESH_VIEW_WHILE_LOADING = 0x01 << 7;
    private static final int FLAG_ENABLE_HEADER_DRAWER_STYLE = 0x01 << 8;
    private static final int FLAG_ENABLE_FOOTER_DRAWER_STYLE = 0x01 << 9;
    private static final int FLAG_DISABLE_PERFORM_LOAD_MORE = 0x01 << 10;
    private static final int FLAG_ENABLE_NO_MORE_DATA = 0x01 << 11;
    private static final int FLAG_DISABLE_LOAD_MORE = 0x01 << 12;
    private static final int FLAG_DISABLE_PERFORM_REFRESH = 0x01 << 13;
    private static final int FLAG_DISABLE_REFRESH = 0x01 << 14;
    private static final int FLAG_ENABLE_AUTO_PERFORM_LOAD_MORE = 0x01 << 15;
    private static final int FLAG_ENABLE_AUTO_PERFORM_REFRESH = 0x01 << 16;
    private static final int FLAG_ENABLE_INTERCEPT_EVENT_WHILE_LOADING = 0x01 << 17;
    private static final int FLAG_DISABLE_WHEN_ANOTHER_DIRECTION_MOVE = 0x01 << 18;
    private static final int FLAG_ENABLE_CHECK_FINGER_INSIDE = 0x01 << 19;
    private static final int FLAG_ENABLE_NO_MORE_DATA_NO_BACK = 0x01 << 20;
    private static final int FLAG_ENABLE_SMOOTH_ROLLBACK_WHEN_COMPLETED = 0x01 << 21;
    private static final int FLAG_DISABLE_LOAD_MORE_WHEN_CONTENT_NOT_FULL = 0x01 << 22;
    private static final int FLAG_ENABLE_COMPAT_SYNC_SCROLL = 0x01 << 23;
    private static final int FLAG_ENABLE_DYNAMIC_ENSURE_TARGET_VIEW = 0x01 << 24;
    private static final int FLAG_ENABLE_PERFORM_FRESH_WHEN_FLING = 0x01 << 25;
    private static final int MASK_DISABLE_PERFORM_LOAD_MORE = 0x07 << 10;
    private static final int MASK_DISABLE_PERFORM_REFRESH = 0x03 << 13;
    private static final int[] LAYOUT_ATTRS = new int[]{
            android.R.attr.enabled
    };
    public static boolean sDebug = true;
    private static int sId = 0;
    private static IRefreshViewCreator sCreator;
    protected final String TAG = "SmoothRefreshLayout-" + sId++;
    protected final int[] mParentScrollConsumed = new int[2];
    protected final int[] mParentOffsetInWindow = new int[2];
    private final List<View> mCachedViews = new ArrayList<>(1);
    @Mode
    protected int mMode = Constants.MODE_DEFAULT;
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
    protected boolean mIsFingerInsideAnotherDirectionView = false;
    protected boolean mNestedScrollInProgress = false;
    protected float mFlingBackFactor = 1.1f;
    protected byte mStatus = SR_STATUS_INIT;
    protected byte mViewStatus = SR_VIEW_STATUS_INIT;
    protected long mLoadingMinTime = 500;
    protected long mLoadingStartTime = 0;
    protected int mAutomaticAction = Constants.ACTION_NOTIFY;
    protected int mLastNestedType = ViewCompat.TYPE_NON_TOUCH;
    protected int mDurationToCloseHeader = 350;
    protected int mDurationToCloseFooter = 350;
    protected int mDurationOfBackToHeaderHeight = 200;
    protected int mDurationOfBackToFooterHeight = 200;
    protected int mMinFlingBackDuration = 200;
    protected int mContentResId = View.NO_ID;
    protected int mStickyHeaderResId = View.NO_ID;
    protected int mStickyFooterResId = View.NO_ID;
    protected int mTouchSlop;
    protected int mTouchPointerId;
    protected int mHeaderBackgroundColor = -1;
    protected int mFooterBackgroundColor = -2;
    protected int mMinimumFlingVelocity;
    protected int mMaximumFlingVelocity;
    protected View mTargetView;
    protected View mScrollTargetView;
    protected View mAutoFoundScrollTargetView;
    protected View mStickyHeaderView;
    protected View mStickyFooterView;
    protected LayoutInflater mInflater;
    protected ScrollChecker mScrollChecker;
    protected VelocityTracker mVelocityTracker;
    protected Paint mBackgroundPaint;
    protected MotionEvent mLastMoveEvent;
    protected OnHeaderEdgeDetectCallBack mInEdgeCanMoveHeaderCallBack;
    protected OnFooterEdgeDetectCallBack mInEdgeCanMoveFooterCallBack;
    protected OnInsideAnotherDirectionViewCallback mInsideAnotherDirectionViewCallback;
    protected OnLoadMoreScrollCallback mLoadMoreScrollCallback;
    protected OnPerformAutoLoadMoreCallBack mAutoLoadMoreCallBack;
    protected OnPerformAutoRefreshCallBack mAutoRefreshCallBack;
    private NestedScrollingParentHelper mNestedScrollingParentHelper;
    private NestedScrollingChildHelper mNestedScrollingChildHelper;
    private Interpolator mSpringInterpolator;
    private Interpolator mSpringBackInterpolator;
    private ArrayList<OnUIPositionChangedListener> mUIPositionChangedListeners;
    private ArrayList<OnNestedScrollChangedListener> mNestedScrollChangedListeners;
    private ArrayList<OnStatusChangedListener> mStatusChangedListeners;
    private ArrayList<ILifecycleObserver> mLifecycleObservers;
    private DelayToRefreshComplete mDelayToRefreshComplete;
    private RefreshCompleteHook mHeaderRefreshCompleteHook;
    private RefreshCompleteHook mFooterRefreshCompleteHook;
    private boolean mIsLastRefreshSuccessful = true;
    private boolean mViewsZAxisNeedReset = true;
    private boolean mNeedFilterScrollEvent = false;
    private float[] mCachedPoint = null;
    private float mOffsetConsumed = 0f;
    private float mOffsetTotal = 0f;
    private int mFlag = FLAG_DISABLE_LOAD_MORE | FLAG_ENABLE_COMPAT_SYNC_SCROLL;
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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SmoothRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    /**
     * Set the static refresh view creator, if the refresh view is null and the frame be
     * needed the refresh view,frame will use this creator to create refresh view.
     * <p>设置默认的刷新视图构造器，当刷新视图为null且需要使用刷新视图时，Frame会使用该构造器构造刷新视图</p>
     *
     * @param creator The static refresh view creator
     */
    public static void setDefaultCreator(IRefreshViewCreator creator) {
        sCreator = creator;
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        sId++;
        createIndicator();
        if (mIndicator == null || mIndicatorSetter == null)
            throw new IllegalArgumentException("You must create a IIndicator, current indicator is null");
        setWillNotDraw(false);
        mInflater = LayoutInflater.from(context);
        TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.SmoothRefreshLayout,
                defStyleAttr, defStyleRes);
        if (arr != null) {
            mContentResId = arr.getResourceId(R.styleable.SmoothRefreshLayout_sr_content, mContentResId);
            float resistance = arr.getFloat(R.styleable
                    .SmoothRefreshLayout_sr_resistance, IIndicator.DEFAULT_RESISTANCE);
            mIndicatorSetter.setResistance(resistance);
            mIndicatorSetter.setResistanceOfHeader(arr.getFloat(R.styleable
                    .SmoothRefreshLayout_sr_resistanceOfHeader, resistance));
            mIndicatorSetter.setResistanceOfFooter(arr.getFloat(R.styleable
                    .SmoothRefreshLayout_sr_resistanceOfFooter, resistance));
            mDurationOfBackToHeaderHeight = arr.getInt(R.styleable
                    .SmoothRefreshLayout_sr_backToKeepDuration, mDurationOfBackToHeaderHeight);
            mDurationOfBackToFooterHeight = arr.getInt(R.styleable
                    .SmoothRefreshLayout_sr_backToKeepDuration, mDurationOfBackToFooterHeight);
            mDurationOfBackToHeaderHeight = arr.getInt(R.styleable
                    .SmoothRefreshLayout_sr_backToKeepHeaderDuration, mDurationOfBackToHeaderHeight);
            mDurationOfBackToFooterHeight = arr.getInt(R.styleable
                    .SmoothRefreshLayout_sr_backToKeepFooterDuration, mDurationOfBackToFooterHeight);
            mDurationToCloseHeader = arr.getInt(R.styleable
                    .SmoothRefreshLayout_sr_closeDuration, mDurationToCloseHeader);
            mDurationToCloseFooter = arr.getInt(R.styleable
                    .SmoothRefreshLayout_sr_closeDuration, mDurationToCloseFooter);
            mDurationToCloseHeader = arr.getInt(R.styleable
                    .SmoothRefreshLayout_sr_closeHeaderDuration, mDurationToCloseHeader);
            mDurationToCloseFooter = arr.getInt(R.styleable
                    .SmoothRefreshLayout_sr_closeFooterDuration, mDurationToCloseFooter);
            float ratio = arr.getFloat(R.styleable.
                    SmoothRefreshLayout_sr_ratioToRefresh, IIndicator.DEFAULT_RATIO_TO_REFRESH);
            mIndicatorSetter.setRatioToRefresh(ratio);
            mIndicatorSetter.setRatioOfHeaderToRefresh(arr.getFloat(R.styleable
                    .SmoothRefreshLayout_sr_ratioOfHeaderToRefresh, ratio));
            mIndicatorSetter.setRatioOfFooterToRefresh(arr.getFloat(R.styleable
                    .SmoothRefreshLayout_sr_ratioOfFooterToRefresh, ratio));
            ratio = arr.getFloat(R.styleable.
                    SmoothRefreshLayout_sr_ratioToKeep, IIndicator.DEFAULT_RATIO_TO_REFRESH);
            mIndicatorSetter.setRatioToKeepHeader(ratio);
            mIndicatorSetter.setRatioToKeepFooter(ratio);
            mIndicatorSetter.setRatioToKeepHeader(arr.getFloat(R.styleable
                    .SmoothRefreshLayout_sr_ratioToKeepHeader, ratio));
            mIndicatorSetter.setRatioToKeepFooter(arr.getFloat(R.styleable
                    .SmoothRefreshLayout_sr_ratioToKeepFooter, ratio));
            ratio = arr.getFloat(R.styleable.
                    SmoothRefreshLayout_sr_maxMoveRatio, IIndicator.DEFAULT_MAX_MOVE_RATIO);
            mIndicatorSetter.setMaxMoveRatio(ratio);
            mIndicatorSetter.setMaxMoveRatioOfHeader(arr.getFloat(R.styleable
                    .SmoothRefreshLayout_sr_maxMoveRatioOfHeader, ratio));
            mIndicatorSetter.setMaxMoveRatioOfFooter(arr.getFloat(R.styleable
                    .SmoothRefreshLayout_sr_maxMoveRatioOfFooter, ratio));
            setEnableKeepRefreshView(arr.getBoolean(R.styleable
                    .SmoothRefreshLayout_sr_enableKeep, true));
            setEnablePinContentView(arr.getBoolean(R.styleable
                    .SmoothRefreshLayout_sr_enablePinContent, false));
            setEnableOverScroll(arr.getBoolean(R.styleable
                    .SmoothRefreshLayout_sr_enableOverScroll, true));
            setEnablePullToRefresh(arr.getBoolean(R.styleable
                    .SmoothRefreshLayout_sr_enablePullToRefresh, false));
            setDisableRefresh(!arr.getBoolean(R.styleable.SmoothRefreshLayout_sr_enableRefresh,
                    true));
            setDisableLoadMore(!arr.getBoolean(R.styleable
                    .SmoothRefreshLayout_sr_enableLoadMore, false));
            mStickyHeaderResId = arr.getResourceId(R.styleable.SmoothRefreshLayout_sr_stickyHeader,
                    NO_ID);
            mStickyFooterResId = arr.getResourceId(R.styleable.SmoothRefreshLayout_sr_stickyFooter,
                    NO_ID);
            mHeaderBackgroundColor = arr.getColor(R.styleable
                    .SmoothRefreshLayout_sr_headerBackgroundColor, -1);
            mFooterBackgroundColor = arr.getColor(R.styleable
                    .SmoothRefreshLayout_sr_footerBackgroundColor, -1);
            if (mHeaderBackgroundColor != -1 || mFooterBackgroundColor != -1)
                preparePaint();
            @Mode
            int mode = arr.getInt(R.styleable.SmoothRefreshLayout_sr_mode, Constants.MODE_DEFAULT);
            mMode = mode;
            arr.recycle();
            arr = context.obtainStyledAttributes(attrs, LAYOUT_ATTRS, defStyleAttr, defStyleRes);
            setEnabled(arr.getBoolean(0, true));
            arr.recycle();
        } else {
            setEnablePullToRefresh(true);
            setEnableKeepRefreshView(true);
        }
        ViewConfiguration viewConfiguration = ViewConfiguration.get(getContext());
        mTouchSlop = viewConfiguration.getScaledTouchSlop();
        mMaximumFlingVelocity = viewConfiguration.getScaledMaximumFlingVelocity();
        mMinimumFlingVelocity = viewConfiguration.getScaledMinimumFlingVelocity();
        mScrollChecker = new ScrollChecker();
        mSpringInterpolator = sSpringInterpolator;
        mSpringBackInterpolator = mSpringInterpolator;
        mNestedScrollingChildHelper = new NestedScrollingChildHelper(this);
        mNestedScrollingParentHelper = new NestedScrollingParentHelper(this);
        setNestedScrollingEnabled(true);
    }

    protected void createIndicator() {
        DefaultIndicator indicator = new DefaultIndicator();
        mIndicator = indicator;
        mIndicatorSetter = indicator;
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (params == null)
            params = generateDefaultLayoutParams();
        else if (!checkLayoutParams(params))
            params = generateLayoutParams(params);
        super.addView(child, index, params);
        ensureFreshView(child);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (!enabled)
            reset();
    }

    @Override
    protected void onDetachedFromWindow() {
        if (mLifecycleObservers != null && !mLifecycleObservers.isEmpty()) {
            final List<ILifecycleObserver> observers = mLifecycleObservers;
            for (ILifecycleObserver observer : observers) {
                observer.onDetached(this);
            }
        }
        super.onDetachedFromWindow();
        destroy();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mLifecycleObservers != null && !mLifecycleObservers.isEmpty()) {
            final List<ILifecycleObserver> observers = mLifecycleObservers;
            for (ILifecycleObserver observer : observers) {
                observer.onAttached(this);
            }
        }
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int count = getChildCount();
        if (count == 0)
            return;
        ensureTargetView();
        int maxHeight = 0;
        int maxWidth = 0;
        int childState = 0;
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() == GONE)
                continue;
            final LayoutParams lp = (LayoutParams) child.getLayoutParams();
            if (mHeaderView != null && child == mHeaderView.getView()) {
                measureHeader(child, lp, widthMeasureSpec, heightMeasureSpec);
            } else if (mFooterView != null && child == mFooterView.getView()) {
                measureFooter(child, lp, widthMeasureSpec, heightMeasureSpec);
            } else {
                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
            }
            maxWidth = Math.max(maxWidth, child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin);
            maxHeight = Math.max(maxHeight, child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin);
            childState = combineMeasuredStates(childState, child.getMeasuredState());
        }
        maxWidth += getPaddingLeft() + getPaddingRight();
        maxHeight += getPaddingTop() + getPaddingBottom();
        maxHeight = Math.max(maxHeight, getSuggestedMinimumHeight());
        maxWidth = Math.max(maxWidth, getSuggestedMinimumWidth());
        setMeasuredDimension(resolveSizeAndState(maxWidth, widthMeasureSpec, childState),
                resolveSizeAndState(maxHeight, heightMeasureSpec, childState <<
                        MEASURED_HEIGHT_STATE_SHIFT));
    }

    protected void measureHeader(View child, LayoutParams lp, int widthMeasureSpec, int heightMeasureSpec) {
        if (isDisabledRefresh())
            return;
        int height = mHeaderView.getCustomHeight();
        if (mHeaderView.getStyle() == IRefreshView.STYLE_DEFAULT
                || mHeaderView.getStyle() == IRefreshView.STYLE_PIN
                || mHeaderView.getStyle() == IRefreshView.STYLE_FOLLOW_CENTER
                || mHeaderView.getStyle() == IRefreshView.STYLE_FOLLOW_PIN) {
            if (height <= 0) {
                if (height == LayoutParams.MATCH_PARENT) lp.height = LayoutParams.MATCH_PARENT;
            } else lp.height = height;
            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
            mIndicatorSetter.setHeaderHeight(child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin);
        } else {
            if (height <= 0 && height != LayoutParams.MATCH_PARENT) {
                throw new IllegalArgumentException("If header view type is " +
                        "STYLE_SCALE or STYLE_FOLLOW_SCALE, you must set a accurate height");
            } else {
                if (height == LayoutParams.MATCH_PARENT) {
                    int specSize = MeasureSpec.getSize(heightMeasureSpec);
                    height = Math.max(0, specSize - (getPaddingTop() + getPaddingBottom()
                            + lp.topMargin + lp.bottomMargin));
                    mIndicatorSetter.setHeaderHeight(height);
                } else {
                    mIndicatorSetter.setHeaderHeight(height + lp.topMargin + lp.bottomMargin);
                }
            }
            if (mHeaderView.getStyle() == IRefreshView.STYLE_FOLLOW_SCALE) {
                if (mIndicator.getCurrentPos() <= mIndicator.getHeaderHeight()) {
                    lp.height = height;
                    measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
                    return;
                }
            }
            final int childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec,
                    getPaddingLeft() + getPaddingRight() + lp.leftMargin + lp.rightMargin, lp
                            .width);
            final int childHeightMeasureSpec;
            if (isMovingHeader()) {
                final int maxHeight = MeasureSpec.getSize(heightMeasureSpec) - getPaddingTop()
                        - getPaddingBottom() - lp.topMargin - lp.bottomMargin;
                int realHeight = Math.min(mIndicator.getCurrentPos() - lp.topMargin - lp.bottomMargin,
                        maxHeight);
                childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(realHeight > 0 ? realHeight : 0,
                        MeasureSpec.EXACTLY);
            } else {
                childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.EXACTLY);
            }
            child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
        }
    }

    protected void measureFooter(View child, LayoutParams lp, int widthMeasureSpec, int heightMeasureSpec) {
        if (isDisabledLoadMore())
            return;
        int height = mFooterView.getCustomHeight();
        if (mFooterView.getStyle() == IRefreshView.STYLE_DEFAULT
                || mFooterView.getStyle() == IRefreshView.STYLE_PIN
                || mFooterView.getStyle() == IRefreshView.STYLE_FOLLOW_CENTER
                || mFooterView.getStyle() == IRefreshView.STYLE_FOLLOW_PIN) {
            if (height <= 0) {
                if (height == LayoutParams.MATCH_PARENT) lp.height = LayoutParams.MATCH_PARENT;
            } else lp.height = height;
            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
            mIndicatorSetter.setFooterHeight(child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin);
        } else {
            if (height <= 0 && height != LayoutParams.MATCH_PARENT) {
                throw new IllegalArgumentException("If footer view type is " +
                        "STYLE_SCALE or STYLE_FOLLOW_SCALE, you must set a accurate height");
            } else {
                if (height == LayoutParams.MATCH_PARENT) {
                    int specSize = MeasureSpec.getSize(heightMeasureSpec);
                    height = Math.max(0, specSize - (getPaddingTop() + getPaddingBottom()
                            + lp.topMargin + lp.bottomMargin));
                    mIndicatorSetter.setFooterHeight(height);
                } else {
                    mIndicatorSetter.setFooterHeight(height + lp.topMargin + lp.bottomMargin);
                }
            }
            if (mFooterView.getStyle() == IRefreshView.STYLE_FOLLOW_SCALE) {
                if (mIndicator.getCurrentPos() <= mIndicator.getFooterHeight()) {
                    lp.height = height;
                    measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
                    return;
                }
            }
            final int childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec,
                    getPaddingLeft() + getPaddingRight() + lp.leftMargin + lp.rightMargin, lp
                            .width);
            final int childHeightMeasureSpec;
            if (isMovingFooter()) {
                final int maxHeight = MeasureSpec.getSize(heightMeasureSpec) - getPaddingTop()
                        - getPaddingBottom() - lp.topMargin - lp.bottomMargin;
                int realHeight = Math.min(mIndicator.getCurrentPos() - lp.topMargin - lp
                        .bottomMargin, maxHeight);
                childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(realHeight > 0 ? realHeight : 0,
                        MeasureSpec.EXACTLY);
            } else {
                childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.EXACTLY);
            }
            child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int count = getChildCount();
        if (count == 0)
            return;
        checkViewsZAxisNeedReset();
        mIndicator.checkConfig();
        final int parentRight = r - l - getPaddingRight();
        final int parentBottom = b - t - getPaddingBottom();
        int offsetHeaderY = 0;
        int offsetFooterY = 0;
        if (isMovingHeader())
            offsetHeaderY = mIndicator.getCurrentPos();
        else if (isMovingFooter())
            offsetFooterY = mIndicator.getCurrentPos();
        int contentBottom = 0;
        boolean pin = mMode == Constants.MODE_SCALE
                || ((mScrollTargetView != null || mAutoFoundScrollTargetView != null) && !isMovingHeader())
                || isEnabledPinContentView();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() == GONE) continue;
            if (mHeaderView != null && child == mHeaderView.getView()) {
                layoutHeaderView(child, offsetHeaderY);
            } else if (mTargetView != null && child == mTargetView) {
                contentBottom = layoutContentView(child, pin, offsetHeaderY, offsetFooterY);
            } else if ((mFooterView == null || mFooterView.getView() != child)
                    && (mStickyFooterView == null || mStickyFooterView != child)
                    && (mStickyHeaderView == null || mStickyHeaderView != child)) {
                layoutOtherView(child, parentRight, parentBottom);
            }
        }
        if (mFooterView != null && mFooterView.getView().getVisibility() != GONE) {
            layoutFooterView(mFooterView.getView(), offsetFooterY, pin, contentBottom);
        }
        if (mStickyHeaderView != null && mStickyHeaderView.getVisibility() != GONE) {
            layoutStickyHeader(pin, offsetHeaderY);
        }
        if (mStickyFooterView != null && mStickyFooterView.getVisibility() != GONE) {
            layoutStickyFooter(contentBottom, offsetFooterY);
        }
        tryToPerformAutoRefresh();
    }

    protected int layoutContentView(View child, boolean pin, int offsetHeader, int offsetFooter) {
        final LayoutParams lp = (LayoutParams) child.getLayoutParams();
        final int left = getPaddingLeft() + lp.leftMargin;
        final int right = left + child.getMeasuredWidth();
        int top, bottom;
        if (isMovingHeader()) {
            top = getPaddingTop() + lp.topMargin + (pin ? 0 : offsetHeader);
            bottom = top + child.getMeasuredHeight();
            child.layout(left, top, right, bottom);
        } else if (isMovingFooter()) {
            top = getPaddingTop() + lp.topMargin - (pin ? 0 : offsetFooter);
            bottom = top + child.getMeasuredHeight();
            child.layout(left, top, right, bottom);
        } else {
            top = getPaddingTop() + lp.topMargin;
            bottom = top + child.getMeasuredHeight();
            child.layout(left, top, right, bottom);
        }
        if (sDebug) SRLog.d(TAG, "onLayout(): content: %s %s %s %s", left, top, right, bottom);
        return bottom + lp.bottomMargin;
    }

    protected void layoutHeaderView(View child, int offsetHeader) {
        if (mMode != Constants.MODE_DEFAULT || isDisabledRefresh()
                || child.getMeasuredHeight() == 0) {
            child.layout(0, 0, 0, 0);
            if (sDebug) SRLog.d(TAG, "onLayout(): header: %s %s %s %s", 0, 0, 0, 0);
            return;
        }
        final LayoutParams lp = (LayoutParams) child.getLayoutParams();
        @IRefreshView.RefreshViewStyle final int type = mHeaderView.getStyle();
        int left, right, top = 0, bottom;
        switch (type) {
            case IRefreshView.STYLE_DEFAULT:
                int offset = offsetHeader - child.getMeasuredHeight();
                top = getPaddingTop() + offset - lp.bottomMargin;
                break;
            case IRefreshView.STYLE_PIN:
            case IRefreshView.STYLE_SCALE:
                top = getPaddingTop() + lp.topMargin;
                break;
            case IRefreshView.STYLE_FOLLOW_PIN:
            case IRefreshView.STYLE_FOLLOW_SCALE:
                if (offsetHeader <= mIndicator.getHeaderHeight()) {
                    top = getPaddingTop() + offsetHeader - child.getMeasuredHeight() - lp
                            .bottomMargin;
                } else {
                    top = getPaddingTop() + lp.topMargin;
                }
                break;
            case IRefreshView.STYLE_FOLLOW_CENTER:
                if (offsetHeader <= mIndicator.getHeaderHeight()) {
                    top = getPaddingTop() + offsetHeader - child.getMeasuredHeight() - lp
                            .bottomMargin;
                } else {
                    top = getPaddingTop() + lp.topMargin + (offsetHeader - mIndicator
                            .getHeaderHeight()) / 2;
                }
                break;
        }
        left = getPaddingLeft() + lp.leftMargin;
        right = left + child.getMeasuredWidth();
        if (isInEditMode())
            top = top + child.getMeasuredHeight();
        bottom = top + child.getMeasuredHeight();
        child.layout(left, top, right, bottom);
        if (sDebug) SRLog.d(TAG, "onLayout(): header: %s %s %s %s", left, top, right, bottom);
    }

    protected void layoutFooterView(View child, int offsetFooter, boolean pin, int contentBottom) {
        if (mMode != Constants.MODE_DEFAULT || isDisabledLoadMore()
                || child.getMeasuredHeight() == 0) {
            child.layout(0, 0, 0, 0);
            if (sDebug) SRLog.d(TAG, "onLayout(): footer: %s %s %s %s", 0, 0, 0, 0);
            return;
        }
        final LayoutParams lp = (LayoutParams) child.getLayoutParams();
        @IRefreshView.RefreshViewStyle final int type = mFooterView.getStyle();
        int left, right, top = 0, bottom;
        switch (type) {
            case IRefreshView.STYLE_DEFAULT:
            case IRefreshView.STYLE_SCALE:
                top = lp.topMargin + contentBottom - (pin ? offsetFooter : 0);
                break;
            case IRefreshView.STYLE_PIN:
                top = getMeasuredHeight() - child.getMeasuredHeight() - lp.bottomMargin
                        - getPaddingBottom();
                break;
            case IRefreshView.STYLE_FOLLOW_PIN:
            case IRefreshView.STYLE_FOLLOW_SCALE:
                if (offsetFooter <= mIndicator.getFooterHeight()) {
                    top = lp.topMargin + contentBottom - (pin ? offsetFooter : 0);
                } else {
                    top = getMeasuredHeight() - child.getMeasuredHeight() - lp.bottomMargin
                            - getPaddingBottom();
                }
                break;
            case IRefreshView.STYLE_FOLLOW_CENTER:
                if (offsetFooter <= mIndicator.getFooterHeight()) {
                    top = lp.topMargin + contentBottom - (pin ? offsetFooter : 0);
                } else {
                    top = lp.topMargin + contentBottom - (pin ? offsetFooter : 0)
                            + (offsetFooter - mIndicator.getFooterHeight()) / 2;
                }
                break;
        }
        left = getPaddingLeft() + lp.leftMargin;
        right = left + child.getMeasuredWidth();
        if (isInEditMode())
            top = top - child.getMeasuredHeight();
        bottom = top + child.getMeasuredHeight();
        child.layout(left, top, right, bottom);
        if (sDebug) SRLog.d(TAG, "onLayout(): footer: %s %s %s %s", left, top, right, bottom);
    }

    protected void layoutStickyHeader(boolean pin, int offsetHeader) {
        final LayoutParams lp = (LayoutParams) mStickyHeaderView.getLayoutParams();
        final int left = getPaddingLeft() + lp.leftMargin;
        final int right = left + mStickyHeaderView.getMeasuredWidth();
        final int top;
        if (isMovingHeader()) {
            top = getPaddingTop() + lp.topMargin + (pin ? 0 : offsetHeader);
        } else {
            top = getPaddingTop() + lp.topMargin;
        }
        final int bottom = top + mStickyHeaderView.getMeasuredHeight();
        mStickyHeaderView.layout(left, top, right, bottom);
        if (sDebug) SRLog.d(TAG, "onLayout(): stickyHeader: %s %s %s %s", left, top, right, bottom);
    }

    protected void layoutStickyFooter(int contentBottom, int offsetFooterY) {
        if (!isMovingFooter()) contentBottom = getMeasuredHeight();
        final LayoutParams lp = (LayoutParams) mStickyFooterView.getLayoutParams();
        final int left = getPaddingLeft() + lp.leftMargin;
        final int right = left + mStickyFooterView.getMeasuredWidth();
        final int bottom = contentBottom - lp.bottomMargin;
        final int top = bottom - mStickyFooterView.getMeasuredHeight();
        mStickyFooterView.layout(left, top, right, bottom);
        if (sDebug) SRLog.d(TAG, "onLayout(): stickyFooter: %s %s %s %s", left, top, right, bottom);
    }

    @SuppressLint({"RtlHardcpded", "RtlHardcoded"})
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
                childLeft = getPaddingLeft() + (parentRight - getPaddingLeft() - width) / 2
                        + lp.leftMargin - lp.rightMargin;
                break;
            case Gravity.RIGHT:
                childLeft = parentRight - width - lp.rightMargin;
                break;
            default:
                childLeft = getPaddingLeft() + lp.leftMargin;
        }
        switch (verticalGravity) {
            case Gravity.CENTER_VERTICAL:
                childTop = getPaddingTop() + (parentBottom - getPaddingTop() - height) / 2
                        + lp.topMargin - lp.bottomMargin;
                break;
            case Gravity.BOTTOM:
                childTop = parentBottom - height - lp.bottomMargin;
                break;
            default:
                childTop = getPaddingTop() + lp.topMargin;
        }
        child.layout(childLeft, childTop, childLeft + width, childTop + height);
        if (sDebug)
            SRLog.d(TAG, "onLayout(): child: %s %s %s %s", childLeft, childTop, childLeft
                    + width, childTop + height);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (!isEnabled() || mTargetView == null || ((isDisabledLoadMore() && isDisabledRefresh()))
                || (isEnabledPinRefreshViewWhileLoading() && ((isRefreshing() && isMovingHeader())
                || (isLoadingMore() && isMovingFooter()))) || mNestedScrollInProgress) {
            return super.dispatchTouchEvent(ev);
        }
        return processDispatchTouchEvent(ev);
    }

    protected final boolean dispatchTouchEventSuper(MotionEvent ev) {
        final int index = ev.findPointerIndex(mTouchPointerId);
        if (index < 0)
            return super.dispatchTouchEvent(ev);
        if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
            mOffsetConsumed = 0;
            mOffsetTotal = 0;
            mOffsetRemaining = mTouchSlop * 3;
        } else {
            if (!mIndicator.isAlreadyHere(IIndicator.START_POS) && mIndicator.getRawOffset() != 0) {
                if (mOffsetRemaining > 0) {
                    mOffsetRemaining -= mTouchSlop;
                    if (isMovingHeader())
                        mOffsetTotal -= mOffsetRemaining;
                    else if (isMovingFooter())
                        mOffsetTotal += mOffsetRemaining;
                }
                mOffsetConsumed += mIndicator.getRawOffset() < 0
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
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mMode == Constants.MODE_DEFAULT) {
            if (mBackgroundPaint != null && !isEnabledPinContentView() && !mIndicator.isAlreadyHere(IIndicator.START_POS)) {
                if (!isDisabledRefresh() && isMovingHeader() && mHeaderBackgroundColor != -1) {
                    mBackgroundPaint.setColor(mHeaderBackgroundColor);
                    drawHeaderBackground(canvas);
                } else if (!isDisabledLoadMore() && isMovingFooter() && mFooterBackgroundColor != -1) {
                    mBackgroundPaint.setColor(mFooterBackgroundColor);
                    drawFooterBackground(canvas);
                }
            }
        }
    }

    protected void drawHeaderBackground(Canvas canvas) {
        final int bottom = Math.min(getPaddingTop() + mIndicator.getCurrentPos(),
                getHeight() - getPaddingTop());
        canvas.drawRect(getPaddingLeft(), getPaddingTop(), getWidth() - getPaddingRight(),
                bottom, mBackgroundPaint);
    }

    protected void drawFooterBackground(Canvas canvas) {
        final int top;
        final int bottom;
        if (mTargetView != null) {
            final LayoutParams lp = (LayoutParams) mTargetView.getLayoutParams();
            bottom = getPaddingTop() + lp.topMargin + mTargetView.getMeasuredHeight() + lp
                    .bottomMargin;
            top = bottom - mIndicator.getCurrentPos();
        } else {
            top = Math.max(getHeight() - getPaddingBottom() - mIndicator.getCurrentPos(),
                    getPaddingTop());
            bottom = getHeight() - getPaddingBottom();
        }
        canvas.drawRect(getPaddingLeft(), top, getWidth() - getPaddingRight(),
                bottom, mBackgroundPaint);
    }

    @ViewCompat.ScrollAxis
    public int getSupportScrollAxis() {
        return ViewCompat.SCROLL_AXIS_VERTICAL;
    }

    public void addLifecycleObserver(ILifecycleObserver observer) {
        if (mLifecycleObservers == null) {
            mLifecycleObservers = new ArrayList<>();
            mLifecycleObservers.add(observer);
        } else if (!mLifecycleObservers.contains(observer)) {
            mLifecycleObservers.add(observer);
        }
    }

    public void removeLifecycleObserver(ILifecycleObserver observer) {
        if (mLifecycleObservers != null && !mLifecycleObservers.isEmpty())
            mLifecycleObservers.remove(observer);
    }

    @Nullable
    public View getScrollTargetView() {
        if (mScrollTargetView != null)
            return mScrollTargetView;
        else if (mAutoFoundScrollTargetView != null)
            return mAutoFoundScrollTargetView;
        else return null;
    }

    /**
     * Set loadMore scroll target view,For example the content view is a FrameLayout,with a
     * listView in it.You can call this method,set the listView as load more scroll target view.
     * Load more compat will try to make it smooth scrolling.
     * <p>设置加载更多时需要做滑动处理的视图。<br/>
     * 例如在SmoothRefreshLayout中有一个CoordinatorLayout,
     * CoordinatorLayout中有AppbarLayout、RecyclerView等，加载更多时希望被移动的视图为RecyclerVieW
     * 而不是CoordinatorLayout,那么设置RecyclerView为TargetView即可</p>
     *
     * @param view Target view
     */
    public void setScrollTargetView(@NonNull View view) {
        mScrollTargetView = view;
    }

    /**
     * Whether to enable the synchronous scroll when load more completed.
     * <p>当加载更多完成时是否启用同步滚动。</p>
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
     * Set the background color of the height of the Header view.
     * <p>设置移动Header时Header区域的背景颜色</p>
     *
     * @param headerBackgroundColor Color
     */
    public void setHeaderBackgroundColor(@ColorInt int headerBackgroundColor) {
        mHeaderBackgroundColor = headerBackgroundColor;
        preparePaint();
    }

    /**
     * Set the background color of the height of the Footer view.
     * <p>设置移动Footer时Footer区域的背景颜色</p>
     *
     * @param footerBackgroundColor Color
     */
    public void setFooterBackgroundColor(int footerBackgroundColor) {
        mFooterBackgroundColor = footerBackgroundColor;
        preparePaint();
    }

    /**
     * Set the custom offset calculator.
     * <p>设置自定义偏移计算器</p>
     *
     * @param calculator Offset calculator
     */
    public void setIndicatorOffsetCalculator(IIndicator.IOffsetCalculator calculator) {
        mIndicatorSetter.setOffsetCalculator(calculator);
    }

    /**
     * Set the listener to be notified when a refresh is triggered.
     * <p>设置刷新监听回调</p>
     *
     * @param listener Listener
     */
    public <T extends OnRefreshListener> void setOnRefreshListener(T listener) {
        mRefreshListener = listener;
    }

    /**
     * Add a listener to listen the views position change event.
     * <p>设置UI位置变化回调</p>
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
     * <p>移除UI位置变化监听器</p>
     *
     * @param listener Listener
     */
    public void removeOnUIPositionChangedListener(@NonNull OnUIPositionChangedListener listener) {
        if (mUIPositionChangedListeners != null && !mUIPositionChangedListeners.isEmpty())
            mUIPositionChangedListeners.remove(listener);
    }

    /**
     * Add a listener to listen for scroll events in this view and all internal views.
     * <p>添加监听器以监听此视图和所有内部视图中的滚动事件</p>
     *
     * @param listener Listener
     */
    public void addOnNestedScrollChangedListener(@NonNull OnNestedScrollChangedListener listener) {
        if (mNestedScrollChangedListeners == null) {
            mNestedScrollChangedListeners = new ArrayList<>();
            mNestedScrollChangedListeners.add(listener);
        } else if (!mNestedScrollChangedListeners.contains(listener)) {
            mNestedScrollChangedListeners.add(listener);
        }
    }

    /**
     * remove the listener.
     * <p>移除滚动变化监听器</p>
     *
     * @param listener Listener
     */
    public void removeOnNestedScrollChangedListener(@NonNull OnNestedScrollChangedListener listener) {
        if (mNestedScrollChangedListeners != null && !mNestedScrollChangedListeners.isEmpty())
            mNestedScrollChangedListeners.remove(listener);
    }

    /**
     * Add a listener when status changed.
     * <p>添加个状态改变监听</p>
     *
     * @param listener Listener that should be called when status changed.
     */
    public void addOnStatusChangedListener(OnStatusChangedListener listener) {
        if (mStatusChangedListeners == null) {
            mStatusChangedListeners = new ArrayList<>();
            mStatusChangedListeners.add(listener);
        } else if (!mStatusChangedListeners.contains(listener)) {
            mStatusChangedListeners.add(listener);
        }
    }

    /**
     * remove the listener.
     * <p>移除状态改变监听器</p>
     *
     * @param listener Listener
     */
    public void removeOnStatusChangedListener(@NonNull OnStatusChangedListener listener) {
        if (mStatusChangedListeners != null && !mStatusChangedListeners.isEmpty())
            mStatusChangedListeners.remove(listener);
    }

    /**
     * Set a scrolling callback when loading more.
     * <p>设置当加载更多时滚动回调，可使用该属性对内部视图做滑动处理。例如内部视图是ListView，完成加载更多时，
     * 需要将加载出的数据显示出来，那么设置该回调，每次Footer回滚时拿到滚动的数值对ListView做向上滚动处理，将数据展示处理</p>
     *
     * @param callback Callback that should be called when scrolling on loading more.
     */
    public void setOnLoadMoreScrollCallback(OnLoadMoreScrollCallback callback) {
        mLoadMoreScrollCallback = callback;
    }

    /**
     * Set a callback to override
     * {@link SmoothRefreshLayout#isNotYetInEdgeCannotMoveHeader()} method. Non-null
     * callback will return the value provided by the callback and ignore all internal logic.
     * <p>设置{@link SmoothRefreshLayout#isNotYetInEdgeCannotMoveHeader()}的重载回调，用来检测内容视图是否在顶部</p>
     *
     * @param callback Callback that should be called when isChildNotYetInEdgeCannotMoveHeader() is called.
     */
    public void setOnHeaderEdgeDetectCallBack(OnHeaderEdgeDetectCallBack callback) {
        mInEdgeCanMoveHeaderCallBack = callback;
    }

    /**
     * Set a callback to override
     * {@link SmoothRefreshLayout#isNotYetInEdgeCannotMoveFooter()} method. Non-null
     * callback will return the value provided by the callback and ignore all internal logic.
     * <p>设置{@link SmoothRefreshLayout#isNotYetInEdgeCannotMoveFooter()}的重载回调，用来检测内容视图是否在底部</p>
     *
     * @param callback Callback that should be called when isChildNotYetInEdgeCannotMoveFooter() is called.
     */
    public void setOnFooterEdgeDetectCallBack(OnFooterEdgeDetectCallBack callback) {
        mInEdgeCanMoveFooterCallBack = callback;
    }

    /**
     * Set a callback to make sure you need to customize the specified trigger the auto load more
     * rule.
     * <p>设置自动加载更多的触发条件回调，可自定义具体的触发自动加载更多的条件</p>
     *
     * @param callBack Customize the specified triggered rule
     */
    public void setOnPerformAutoLoadMoreCallBack(OnPerformAutoLoadMoreCallBack callBack) {
        mAutoLoadMoreCallBack = callBack;
    }

    /**
     * Set a callback to make sure you need to customize the specified trigger the auto refresh
     * rule.
     * <p>设置滚到到顶自动刷新的触发条件回调，可自定义具体的触发自动刷新的条件</p>
     *
     * @param callBack Customize the specified triggered rule
     */
    public void setOnPerformAutoRefreshCallBack(OnPerformAutoRefreshCallBack callBack) {
        mAutoRefreshCallBack = callBack;
    }

    /**
     * Set a hook callback when the refresh complete event be triggered. Only can be called on
     * refreshing.
     * <p>设置一个头部视图刷新完成前的Hook回调</p>
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
     * <p>设置一个尾部视图刷新完成前的Hook回调</p>
     *
     * @param callback Callback that should be called when refreshComplete() is called.
     */
    public void setOnHookFooterRefreshCompleteCallback(OnHookUIRefreshCompleteCallBack callback) {
        if (mFooterRefreshCompleteHook == null)
            mFooterRefreshCompleteHook = new RefreshCompleteHook();
        mFooterRefreshCompleteHook.mCallBack = callback;
    }

    /**
     * Set a callback to override
     * {@link SmoothRefreshLayout#isInsideAnotherDirectionView(float, float)}} method.
     * Non-null callback will return the value provided by the callback and ignore all internal
     * logic.
     * <p>设置{@link SmoothRefreshLayout#isInsideAnotherDirectionView(float, float)}的重载回调，
     * 用来检查手指按下的点是否位于水平视图内部</p>
     *
     * @param callback Callback that should be called when isFingerInsideAnotherDirectionView(float,
     *                 float) is called.
     */
    public void setOnInsideAnotherDirectionViewCallback(OnInsideAnotherDirectionViewCallback callback) {
        mInsideAnotherDirectionViewCallback = callback;
    }

    /**
     * Whether it is refreshing state.
     * <p>是否在刷新中</p>
     *
     * @return Refreshing
     */
    public boolean isRefreshing() {
        return mStatus == SR_STATUS_REFRESHING;
    }

    /**
     * Whether it is loading more state.
     * <p>是否在加载更多种</p>
     *
     * @return Loading
     */
    public boolean isLoadingMore() {
        return mStatus == SR_STATUS_LOADING_MORE;
    }

    /**
     * Whether it is in start position.
     * <p>是否在起始位置</p>
     *
     * @return Is
     */
    public boolean isInStartPosition() {
        return mIndicator.isAlreadyHere(IIndicator.START_POS);
    }

    /**
     * Whether it is refresh successful.
     * <p>是否刷新成功</p>
     *
     * @return Is
     */
    public boolean isRefreshSuccessful() {
        return mIsLastRefreshSuccessful;
    }

    /**
     * Perform refresh complete, to reset the state to {@link SmoothRefreshLayout#SR_STATUS_INIT}
     * and set the last refresh operation successfully.
     * <p>完成刷新，刷新状态为成功</p>
     */
    final public void refreshComplete() {
        refreshComplete(true);
    }

    /**
     * Perform refresh complete, to reset the state to {@link SmoothRefreshLayout#SR_STATUS_INIT}.
     * <p>完成刷新，刷新状态`isSuccessful`</p>
     *
     * @param isSuccessful Set the last refresh operation status
     */
    final public void refreshComplete(boolean isSuccessful) {
        refreshComplete(isSuccessful, 0);
    }

    /**
     * Perform refresh complete, delay to reset the state to
     * {@link SmoothRefreshLayout#SR_STATUS_INIT} and set the last refresh operation successfully.
     * <p>完成刷新，延迟`delayDurationToChangeState`时间</p>
     *
     * @param delayDurationToChangeState Delay to change the state to
     *                                   {@link SmoothRefreshLayout#SR_STATUS_COMPLETE}
     */
    final public void refreshComplete(long delayDurationToChangeState) {
        refreshComplete(true, delayDurationToChangeState);
    }

    /**
     * Perform refresh complete, delay to reset the state to
     * {@link SmoothRefreshLayout#SR_STATUS_INIT} and set the last refresh operation.
     * <p>完成刷新，刷新状态`isSuccessful`，延迟`delayDurationToChangeState`时间</p>
     *
     * @param delayDurationToChangeState Delay to change the state to
     *                                   {@link SmoothRefreshLayout#SR_STATUS_INIT}
     * @param isSuccessful               Set the last refresh operation
     */
    final public void refreshComplete(boolean isSuccessful, long delayDurationToChangeState) {
        if (sDebug)
            SRLog.d(TAG, "refreshComplete(): isSuccessful: %s", isSuccessful);
        mIsLastRefreshSuccessful = isSuccessful;
        if (!isRefreshing() && !isLoadingMore())
            return;
        long delay = mLoadingMinTime - (SystemClock.uptimeMillis() - mLoadingStartTime);
        if (delayDurationToChangeState <= 0) {
            if (delay <= 0) {
                performRefreshComplete(true, true);
            } else {
                if (mDelayToRefreshComplete == null)
                    mDelayToRefreshComplete = new DelayToRefreshComplete();
                mDelayToRefreshComplete.mLayoutWeakRf = new WeakReference<>(this);
                mDelayToRefreshComplete.mNotifyViews = true;
                postDelayed(mDelayToRefreshComplete, delay);
            }
        } else {
            if (isRefreshing() && mHeaderView != null) {
                mHeaderView.onRefreshComplete(this, isSuccessful);
            } else if (isLoadingMore() && mFooterView != null) {
                mFooterView.onRefreshComplete(this, isSuccessful);
            }
            if (delayDurationToChangeState < delay)
                delayDurationToChangeState = delay;
            if (mDelayToRefreshComplete == null)
                mDelayToRefreshComplete = new DelayToRefreshComplete();
            mDelayToRefreshComplete.mLayoutWeakRf = new WeakReference<>(this);
            mDelayToRefreshComplete.mNotifyViews = false;
            postDelayed(mDelayToRefreshComplete, delayDurationToChangeState);
        }
    }

    /**
     * Set the loading min time.
     * <p>设置加载过程的最小持续时间</p>
     *
     * @param time Millis
     */
    public void setLoadingMinTime(long time) {
        mLoadingMinTime = time;
    }

    /**
     * Get the Header height, after the measurement is completed, the height will have value.
     * <p>获取Header的高度，在布局计算完成前无法得到准确的值</p>
     *
     * @return Height default is -1
     */
    public int getHeaderHeight() {
        return mIndicator.getHeaderHeight();
    }

    /**
     * Get the Footer height, after the measurement is completed, the height will have value.
     * <p>获取Footer的高度，在布局计算完成前无法得到准确的值</p>
     *
     * @return Height default is -1
     */
    public int getFooterHeight() {
        return mIndicator.getFooterHeight();
    }

    /**
     * Perform auto refresh at once.
     * <p>自动刷新并立即触发刷新回调</p>
     */
    public void autoRefresh() {
        autoRefresh(Constants.ACTION_NOTIFY, true);
    }

    /**
     * If @param atOnce has been set to true. Auto perform refresh at once.
     * <p>自动刷新，`atOnce`立即触发刷新回调</p>
     *
     * @param atOnce Auto refresh at once
     */
    @Deprecated
    public void autoRefresh(boolean atOnce) {
        autoRefresh(atOnce ? Constants.ACTION_AT_ONCE : Constants.ACTION_NOTIFY, true);
    }

    /**
     * If @param atOnce has been set to true. Auto perform refresh at once.
     * If @param smooth has been set to true. Auto perform refresh will using smooth scrolling.
     * <p>自动刷新，`atOnce`立即触发刷新回调，`smooth`滚动到触发位置</p>
     *
     * @param atOnce       Auto refresh at once
     * @param smoothScroll Auto refresh use smooth scrolling
     */
    @Deprecated
    public void autoRefresh(boolean atOnce, boolean smoothScroll) {
        autoRefresh(atOnce ? Constants.ACTION_AT_ONCE : Constants.ACTION_NOTIFY, smoothScroll);
    }

    /**
     * The @param action can be used to specify the action to trigger refresh. If the `action` been
     * set to `SR_ACTION_NOTHING`, we will not notify the refresh listener when in refreshing. If
     * the `action` been set to `SR_ACTION_AT_ONCE`, we will notify the refresh listener at once. If
     * the `action` been set to `SR_ACTION_NOTIFY`, we will notify the refresh listener when in
     * refreshing be later
     * If @param smooth has been set to true. Auto perform refresh will using smooth scrolling.
     * listener
     * <p>自动刷新，`action`触发刷新的动作，`smooth`滚动到触发位置</p>
     *
     * @param action       Auto refresh use action.{@link Constants#ACTION_NOTIFY},
     *                     {@link Constants#ACTION_AT_ONCE},{@link Constants#ACTION_NOTHING}
     * @param smoothScroll Auto refresh use smooth scrolling
     */
    public void autoRefresh(@Action int action, boolean smoothScroll) {
        if (mStatus != SR_STATUS_INIT || mMode != Constants.MODE_DEFAULT)
            return;
        if (sDebug)
            SRLog.d(TAG, "autoRefresh(): action: %s, smoothScroll: %s", action, smoothScroll);
        final byte old = mStatus;
        mStatus = SR_STATUS_PREPARE;
        notifyStatusChanged(old, mStatus);
        if (mHeaderView != null)
            mHeaderView.onRefreshPrepare(this);
        mIndicatorSetter.setMovingStatus(Constants.MOVING_HEADER);
        mViewStatus = SR_VIEW_STATUS_HEADER_IN_PROCESSING;
        mAutomaticActionUseSmoothScroll = smoothScroll;
        mAutomaticAction = action;
        if (mIndicator.getHeaderHeight() <= 0) {
            mAutomaticActionTriggered = false;
        } else {
            scrollToTriggeredAutomatic(true);
        }
    }

    /**
     * Perform auto load more at once.
     * <p>自动加载更多，并立即触发刷新回调</p>
     */
    public void autoLoadMore() {
        autoLoadMore(Constants.ACTION_NOTIFY, true);
    }

    /**
     * If @param atOnce has been set to true. Auto perform load more at once.
     * <p>自动加载更多，`atOnce`立即触发刷新回调</p>
     *
     * @param atOnce Auto load more at once
     */
    @Deprecated
    public void autoLoadMore(boolean atOnce) {
        autoLoadMore(atOnce ? Constants.ACTION_AT_ONCE : Constants.ACTION_NOTIFY, true);
    }

    /**
     * If @param atOnce has been set to true. Auto perform load more at once.
     * If @param smooth has been set to true. Auto perform load more will using smooth scrolling.
     * <p>自动加载更多，`atOnce`立即触发刷新回调，`smooth`滚动到触发位置</p>
     *
     * @param atOnce       Auto load more at once
     * @param smoothScroll Auto load more use smooth scrolling
     */
    @Deprecated
    public void autoLoadMore(boolean atOnce, boolean smoothScroll) {
        autoLoadMore(atOnce ? Constants.ACTION_AT_ONCE : Constants.ACTION_NOTIFY,
                smoothScroll);
    }

    /**
     * The @param action can be used to specify the action to trigger refresh. If the `action` been
     * set to `SR_ACTION_NOTHING`, we will not notify the refresh listener when in refreshing. If
     * the `action` been set to `SR_ACTION_AT_ONCE`, we will notify the refresh listener at once. If
     * the `action` been set to `SR_ACTION_NOTIFY`, we will notify the refresh listener when in
     * refreshing be later
     * If @param smooth has been set to true. Auto perform load more will using smooth scrolling.
     * <p>自动加载更多，`action`触发加载更多的动作，`smooth`滚动到触发位置</p>
     *
     * @param action       Auto load more use action.{@link Constants#ACTION_NOTIFY},
     *                     {@link Constants#ACTION_AT_ONCE},{@link Constants#ACTION_NOTHING}
     * @param smoothScroll Auto load more use smooth scrolling
     */
    public void autoLoadMore(@Action int action, boolean smoothScroll) {
        if (mStatus != SR_STATUS_INIT || mMode != Constants.MODE_DEFAULT)
            return;
        if (sDebug)
            SRLog.d(TAG, "autoLoadMore(): action: %s, smoothScroll: %s", action, smoothScroll);
        final byte old = mStatus;
        mStatus = SR_STATUS_PREPARE;
        notifyStatusChanged(old, mStatus);
        if (mFooterView != null)
            mFooterView.onRefreshPrepare(this);
        mIndicatorSetter.setMovingStatus(Constants.MOVING_FOOTER);
        mViewStatus = SR_VIEW_STATUS_FOOTER_IN_PROCESSING;
        mAutomaticActionUseSmoothScroll = smoothScroll;
        if (mIndicator.getFooterHeight() <= 0) {
            mAutomaticActionTriggered = false;
        } else {
            scrollToTriggeredAutomatic(false);
        }
    }

    /**
     * Set the resistance while you are moving.
     * <p>移动刷新视图时候的移动阻尼</p>
     *
     * @param resistance Resistance
     */
    public void setResistance(@FloatRange(from = 0, to = Float.MAX_VALUE) float resistance) {
        mIndicatorSetter.setResistance(resistance);
    }

    /**
     * Set the resistance while you are moving Footer.
     * <p>移动Footer视图时候的移动阻尼</p>
     *
     * @param resistance Resistance
     */
    public void setResistanceOfFooter(@FloatRange(from = 0, to = Float.MAX_VALUE) float resistance) {
        mIndicatorSetter.setResistanceOfFooter(resistance);
    }

    /**
     * Set the resistance while you are moving Header.
     * <p>移动Header视图时候的移动阻尼</p>
     *
     * @param resistance Resistance
     */
    public void setResistanceOfHeader(@FloatRange(from = 0, to = Float.MAX_VALUE) float resistance) {
        mIndicatorSetter.setResistanceOfHeader(resistance);
    }

    /**
     * Set the height ratio of the trigger refresh.
     * <p>设置触发刷新时的位置占刷新视图的高度比</p>
     *
     * @param ratio Height ratio
     */
    public void setRatioToRefresh(@FloatRange(from = 0, to = Float.MAX_VALUE) float ratio) {
        mIndicatorSetter.setRatioToRefresh(ratio);
    }

    /**
     * Set the Header height ratio of the trigger refresh.
     * <p>设置触发下拉刷新时的位置占Header视图的高度比</p>
     *
     * @param ratio Height ratio
     */
    public void setRatioOfHeaderToRefresh(@FloatRange(from = 0, to = Float.MAX_VALUE) float ratio) {
        mIndicatorSetter.setRatioOfHeaderToRefresh(ratio);
    }

    /**
     * Set the Footer height ratio of the trigger refresh.
     * <p>设置触发加载更多时的位置占Footer视图的高度比</p>
     *
     * @param ratio Height ratio
     */
    public void setRatioOfFooterToRefresh(@FloatRange(from = 0, to = Float.MAX_VALUE) float ratio) {
        mIndicatorSetter.setRatioOfFooterToRefresh(ratio);
    }

    /**
     * Set the offset of keep view in refreshing occupies the height ratio of the refresh view.
     * <p>刷新中保持视图位置占刷新视图的高度比（默认:`1f`）,该属性的值必须小于等于触发刷新高度比才会有效果，
     * 当开启了{@link SmoothRefreshLayout#isEnabledKeepRefreshView}后，该属性会生效</p>
     *
     * @param ratio Height ratio
     */
    public void setRatioToKeep(@FloatRange(from = 0, to = Float.MAX_VALUE) float ratio) {
        mIndicatorSetter.setRatioToKeepHeader(ratio);
        mIndicatorSetter.setRatioToKeepFooter(ratio);
    }

    /**
     * Set the offset of keep Header in refreshing occupies the height ratio of the Header.
     * <p>刷新中保持视图位置占Header视图的高度比（默认:`1f`）,该属性的值必须小于等于触发刷新高度比才会有效果</p>
     *
     * @param ratio Height ratio
     */
    public void setRatioToKeepHeader(@FloatRange(from = 0, to = Float.MAX_VALUE) float ratio) {
        mIndicatorSetter.setRatioToKeepHeader(ratio);
    }

    /**
     * Set the offset of keep Footer in refreshing occupies the height ratio of the Footer.
     * <p>刷新中保持视图位置占Header视图的高度比（默认:`1f`）,该属性的值必须小于等于触发刷新高度比才会有效果</p>
     *
     * @param ratio Height ratio
     */
    public void setRatioToKeepFooter(@FloatRange(from = 0, to = Float.MAX_VALUE) float ratio) {
        mIndicatorSetter.setRatioToKeepFooter(ratio);
    }

    /**
     * Set the max duration for Cross-Boundary-Rebound(OverScroll).
     * <p>设置越界回弹效果的最大持续时长（默认:`350`）</p>
     *
     * @param duration Duration
     */
    public void setMaxOverScrollDuration(@IntRange(from = 0, to = Integer.MAX_VALUE) int duration) {
        mMaxOverScrollDuration = duration;
    }

    /**
     * Set the min duration for Cross-Boundary-Rebound(OverScroll).
     * <p>设置越界回弹效果的最小持续时长（默认:`100`）</p>
     *
     * @param duration Duration
     */
    public void setMinOverScrollDuration(@IntRange(from = 0, to = Integer.MAX_VALUE) int duration) {
        mMinOverScrollDuration = duration;
    }

    /**
     * Set the duration of return back to the start position.
     * <p>设置刷新完成回滚到起始位置的时间</p>
     *
     * @param duration Millis
     */
    public void setDurationToClose(@IntRange(from = 0, to = Integer.MAX_VALUE) int duration) {
        mDurationToCloseHeader = duration;
        mDurationToCloseFooter = duration;
    }

    /**
     * Get the duration of Header return to the start position.
     *
     * @return mDuration
     */
    public int getDurationToCloseHeader() {
        return mDurationToCloseHeader;
    }

    /**
     * Set the duration of Header return to the start position.
     * <p>设置Header刷新完成回滚到起始位置的时间</p>
     *
     * @param duration Millis
     */
    public void setDurationToCloseHeader(@IntRange(from = 0, to = Integer.MAX_VALUE) int duration) {
        mDurationToCloseHeader = duration;
    }

    /**
     * Get the duration of Footer return to the start position.
     *
     * @return mDuration
     */
    public int getDurationToCloseFooter() {
        return mDurationToCloseFooter;
    }

    /**
     * Set the duration of Footer return to the start position.
     * <p>设置Footer刷新完成回滚到起始位置的时间</p>
     *
     * @param duration Millis
     */
    public void setDurationToCloseFooter(@IntRange(from = 0, to = Integer.MAX_VALUE) int duration) {
        mDurationToCloseFooter = duration;
    }

    /**
     * Set the duration of return to the keep refresh view position.
     * <p>设置回滚到保持刷新视图位置的时间</p>
     *
     * @param duration Millis
     */
    public void setDurationOfBackToKeep(@IntRange(from = 0, to = Integer.MAX_VALUE) int duration) {
        mDurationOfBackToHeaderHeight = duration;
        mDurationOfBackToFooterHeight = duration;
    }

    /**
     * Set the duration of return to the keep refresh view position when Header moves.
     * <p>设置回滚到保持Header视图位置的时间</p>
     *
     * @param duration Millis
     */
    public void setDurationOfBackToKeepHeader(@IntRange(from = 0, to = Integer.MAX_VALUE) int duration) {
        this.mDurationOfBackToHeaderHeight = duration;
    }

    /**
     * Set the duration of return to the keep refresh view position when Footer moves.
     * <p>设置回顾到保持Footer视图位置的时间</p>
     *
     * @param duration Millis
     */
    public void setDurationOfBackToKeepFooter(@IntRange(from = 0, to = Integer.MAX_VALUE) int duration) {
        this.mDurationOfBackToFooterHeight = duration;
    }

    /**
     * Set the max can move offset occupies the height ratio of the refresh view.
     * <p>设置最大移动距离占刷新视图的高度比</p>
     *
     * @param ratio The max ratio of refresh view
     */
    public void setMaxMoveRatio(@FloatRange(from = 0, to = Float.MAX_VALUE) float ratio) {
        mIndicatorSetter.setMaxMoveRatio(ratio);
    }

    /**
     * Set the max can move offset occupies the height ratio of the Header.
     * <p>设置最大移动距离占Header视图的高度比</p>
     *
     * @param ratio The max ratio of Header view
     */
    public void setMaxMoveRatioOfHeader(@FloatRange(from = 0, to = Float.MAX_VALUE) float ratio) {
        mIndicatorSetter.setMaxMoveRatioOfHeader(ratio);
    }

    /**
     * Set the max can move offset occupies the height ratio of the Footer.
     * <p>设置最大移动距离占Footer视图的高度比</p>
     *
     * @param ratio The max ratio of Footer view
     */
    public void setMaxMoveRatioOfFooter(@FloatRange(from = 0, to = Float.MAX_VALUE) float ratio) {
        mIndicatorSetter.setMaxMoveRatioOfFooter(ratio);
    }

    /**
     * The flag has set to autoRefresh.
     * <p>是否处于自动刷新刷新</p>
     *
     * @return Enabled
     */
    public boolean isAutoRefresh() {
        return (mFlag & FLAG_AUTO_REFRESH) > 0;
    }

    /**
     * If enable has been set to true. The user can immediately perform next refresh.
     * <p>是否已经开启完成刷新后即可立即触发刷新</p>
     *
     * @return Is enable
     */
    public boolean isEnabledNextPtrAtOnce() {
        return (mFlag & FLAG_ENABLE_NEXT_AT_ONCE) > 0;
    }

    /**
     * If @param enable has been set to true. The user can immediately perform next refresh.
     * <p>设置开启完成刷新后即可立即触发刷新</p>
     *
     * @param enable Enable
     */
    public void setEnableNextPtrAtOnce(boolean enable) {
        if (enable) {
            mFlag = mFlag | FLAG_ENABLE_NEXT_AT_ONCE;
        } else {
            mFlag = mFlag & ~FLAG_ENABLE_NEXT_AT_ONCE;
        }
    }

    /**
     * The flag has set enabled overScroll.
     * <p>是否已经开启越界回弹</p>
     *
     * @return Enabled
     */
    public boolean isEnabledOverScroll() {
        return (mFlag & FLAG_ENABLE_OVER_SCROLL) > 0;
    }

    /**
     * If @param enable has been set to true. Will supports over scroll.
     * <p>设置开始越界回弹</p>
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
     * <p>是否已经开启刷新中拦截消耗触摸事件</p>
     *
     * @return Enabled
     */
    public boolean isEnabledInterceptEventWhileLoading() {
        return (mFlag & FLAG_ENABLE_INTERCEPT_EVENT_WHILE_LOADING) > 0;
    }

    /**
     * If @param enable has been set to true. Will intercept the touch event while loading.
     * <p>开启刷新中拦截消耗触摸事件</p>
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
     * <p>是否已经开启拉动刷新，下拉或者上拉到触发刷新位置即立即触发刷新</p>
     *
     * @return Enabled
     */
    public boolean isEnabledPullToRefresh() {
        return (mFlag & FLAG_ENABLE_PULL_TO_REFRESH) > 0;
    }

    /**
     * If @param enable has been set to true. When the current pos >= refresh offsets perform
     * refresh.
     * <p>设置开启拉动刷新,下拉或者上拉到触发刷新位置即立即触发刷新</p>
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
     * The flag has been set to check whether the finger pressed point is inside horizontal view.
     * <p>是否已经开启检查按下点是否位于水平滚动视图内</p>
     *
     * @return Enabled
     */
    public boolean isEnableCheckInsideAnotherDirectionView() {
        return (mFlag & FLAG_ENABLE_CHECK_FINGER_INSIDE) > 0;
    }

    /**
     * If @param enable has been set to true. Touch event handling will be check whether the finger
     * pressed point is inside horizontal view.
     * <p>设置开启检查按下点是否位于水平滚动视图内</p>
     *
     * @param enable Pull to refresh
     */
    public void setEnableCheckInsideAnotherDirectionView(boolean enable) {
        if (enable) {
            mFlag = mFlag | FLAG_ENABLE_CHECK_FINGER_INSIDE;
        } else {
            mFlag = mFlag & ~FLAG_ENABLE_CHECK_FINGER_INSIDE;
        }
    }

    /**
     * The flag has been set to enabled Header drawerStyle.
     * <p>是否已经开启Header的抽屉效果，即Header在Content下面</p>
     *
     * @return Enabled
     */
    public boolean isEnabledHeaderDrawerStyle() {
        return (mFlag & FLAG_ENABLE_HEADER_DRAWER_STYLE) > 0;
    }

    /**
     * If @param enable has been set to true.Enable Header drawerStyle.
     * <p>设置开启Header的抽屉效果，即Header在Content下面,由于该效果需要改变层级关系，所以需要重新布局</p>
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
        requestLayout();
    }

    /**
     * The flag has been set to enabled Footer drawerStyle.
     * <p>是否已经开启Footer的抽屉效果，即Footer在Content下面</p>
     *
     * @return Enabled
     */
    public boolean isEnabledFooterDrawerStyle() {
        return (mFlag & FLAG_ENABLE_FOOTER_DRAWER_STYLE) > 0;
    }

    /**
     * If @param enable has been set to true.Enable Footer drawerStyle.
     * <p>设置开启Footer的抽屉效果，即Footer在Content下面,由于该效果需要改变层级关系，所以需要重新布局</p>
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
        requestLayout();
    }

    /**
     * The flag has been set to disabled perform refresh.
     * <p>是否已经关闭触发下拉刷新</p>
     *
     * @return Disabled
     */
    public boolean isDisabledPerformRefresh() {
        return (mFlag & MASK_DISABLE_PERFORM_REFRESH) > 0;
    }

    /**
     * If @param disable has been set to true. Will never perform refresh.
     * <p>设置是否关闭触发下拉刷新</p>
     *
     * @param disable Disable perform refresh
     */
    public void setDisablePerformRefresh(boolean disable) {
        if (disable) {
            mFlag = mFlag | FLAG_DISABLE_PERFORM_REFRESH;
        } else {
            mFlag = mFlag & ~FLAG_DISABLE_PERFORM_REFRESH;
        }
    }

    /**
     * The flag has been set to disabled refresh.
     * <p>是否已经关闭刷新</p>
     *
     * @return Disabled
     */
    public boolean isDisabledRefresh() {
        return (mFlag & FLAG_DISABLE_REFRESH) > 0;
    }

    /**
     * If @param disable has been set to true.Will disable refresh.
     * <p>设置是否关闭刷新</p>
     *
     * @param disable Disable refresh
     */
    public void setDisableRefresh(boolean disable) {
        if (disable) {
            mFlag = mFlag | FLAG_DISABLE_REFRESH;
            if (isRefreshing()) reset();
        } else {
            mFlag = mFlag & ~FLAG_DISABLE_REFRESH;
        }
        requestLayout();
    }

    /**
     * The flag has been set to disabled perform load more.
     * <p>是否已经关闭触发加载更多</p>
     *
     * @return Disabled
     */
    public boolean isDisabledPerformLoadMore() {
        return (mFlag & MASK_DISABLE_PERFORM_LOAD_MORE) > 0;
    }

    /**
     * If @param disable has been set to true.Will never perform load more.
     * <p>设置是否关闭触发加载更多</p>
     *
     * @param disable Disable perform load more
     */
    public void setDisablePerformLoadMore(boolean disable) {
        if (disable) {
            mFlag = mFlag | FLAG_DISABLE_PERFORM_LOAD_MORE;
        } else {
            mFlag = mFlag & ~FLAG_DISABLE_PERFORM_LOAD_MORE;
        }
    }

    /**
     * The flag has been set to disabled load more.
     * <p>是否已经关闭加载更多</p>
     *
     * @return Disabled
     */
    public boolean isDisabledLoadMore() {
        return (mFlag & FLAG_DISABLE_LOAD_MORE) > 0;
    }

    /**
     * If @param disable has been set to true.Will disable load more.
     * <p>设置关闭加载更多</p>
     *
     * @param disable Disable load more
     */
    public void setDisableLoadMore(boolean disable) {
        if (disable) {
            mFlag = mFlag | FLAG_DISABLE_LOAD_MORE;
            if (isLoadingMore()) reset();
        } else {
            mFlag = mFlag & ~FLAG_DISABLE_LOAD_MORE;
        }
        requestLayout();
    }

    /**
     * The flag has been set to disabled when horizontal move.
     * <p>是否已经设置不响应横向滑动</p>
     *
     * @return Disabled
     */
    public boolean isDisabledWhenAnotherDirectionMove() {
        return (mFlag & FLAG_DISABLE_WHEN_ANOTHER_DIRECTION_MOVE) > 0;
    }

    /**
     * Set whether to filter the horizontal moves.
     * <p>设置不响应横向滑动，当内部视图含有需要响应横向滑动的子视图时，需要设置该属性，否则自视图无法响应横向滑动</p>
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
     * <p>是否已经开启加载更多完成已无更多数据，自定义Footer可根据该属性判断是否显示无更多数据的提示</p>
     *
     * @return Enabled
     */
    public boolean isEnabledNoMoreData() {
        return (mFlag & FLAG_ENABLE_NO_MORE_DATA) > 0;
    }

    /**
     * If @param enable has been set to true. The Footer will show no more data and will never
     * trigger load more.
     * <p>设置开启加载更多完成已无更多数据，当该属性设置为`true`时，将不再触发加载更多</p>
     *
     * @param enable Enable no more data
     */
    public void setEnableNoMoreData(boolean enable) {
        if (enable) {
            mFlag = mFlag | FLAG_ENABLE_NO_MORE_DATA;
        } else {
            mFlag = mFlag & ~FLAG_ENABLE_NO_MORE_DATA;
        }
    }

    /**
     * The flag has been set to enabled. The scroller rollback can not be interrupted when
     * refresh completed.
     * <p>是否已经开启当刷新完成时，回滚动作不能被打断</p>
     *
     * @return Enabled
     */
    public boolean isEnabledSmoothRollbackWhenCompleted() {
        return (mFlag & FLAG_ENABLE_SMOOTH_ROLLBACK_WHEN_COMPLETED) > 0;
    }

    /**
     * If @param enable has been set to true. The rollback can not be interrupted when refresh
     * completed.
     * <p>设置开启当刷新完成时，回滚动作不能被打断</p>
     *
     * @param enable Enable no more data
     */
    public void setEnableSmoothRollbackWhenCompleted(boolean enable) {
        if (enable) {
            mFlag = mFlag | FLAG_ENABLE_SMOOTH_ROLLBACK_WHEN_COMPLETED;
        } else {
            mFlag = mFlag & ~FLAG_ENABLE_SMOOTH_ROLLBACK_WHEN_COMPLETED;
        }
    }

    /**
     * The flag has been set to enabled. Load more will be disabled when the content is not
     * full.
     * <p>是否已经设置了内容视图未满屏时关闭加载更多</p>
     *
     * @return Disabled
     */
    public boolean isDisabledLoadMoreWhenContentNotFull() {
        return (mFlag & FLAG_DISABLE_LOAD_MORE_WHEN_CONTENT_NOT_FULL) > 0;
    }

    /**
     * If @param disable has been set to true.Load more will be disabled when the content is not
     * full.
     * <p>设置当内容视图未满屏时关闭加载更多</p>
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
     * The flag has been set to enabled when Footer has no more data to no longer need spring back.
     * <p>是否已经开启加载更多完成已无更多数据且不需要回滚动作</p>
     *
     * @return Enabled
     */
    public boolean isEnabledNoSpringBackWhenNoMoreData() {
        return (mFlag & FLAG_ENABLE_NO_MORE_DATA_NO_BACK) > 0;
    }

    /**
     * If @param enable has been set to true. When there is no more data will no longer spring back.
     * <p>设置开启加载更多完成已无更多数据且不需要回滚动作，当该属性设置为`true`时，将不再触发加载更多</p>
     *
     * @param enable Enable no more data
     */
    public void setEnableNoSpringBackWhenNoMoreData(boolean enable) {
        if (enable) {
            mFlag = mFlag | FLAG_ENABLE_NO_MORE_DATA_NO_BACK;
        } else {
            mFlag = mFlag & ~FLAG_ENABLE_NO_MORE_DATA_NO_BACK;
        }
    }

    /**
     * The flag has been set to keep refresh view while loading.
     * <p>是否已经开启保持刷新视图</p>
     *
     * @return Enabled
     */
    public boolean isEnabledKeepRefreshView() {
        return (mFlag & FLAG_ENABLE_KEEP_REFRESH_VIEW) > 0;
    }

    /**
     * If @param enable has been set to true.When the current pos> = keep refresh view pos,
     * it rolls back to the keep refresh view pos to perform refresh and remains until the refresh
     * completed.
     * <p>开启刷新中保持刷新视图位置</p>
     *
     * @param enable Keep refresh view
     */
    public void setEnableKeepRefreshView(boolean enable) {
        if (enable) {
            mFlag = mFlag | FLAG_ENABLE_KEEP_REFRESH_VIEW;
        } else {
            mFlag = mFlag & ~FLAG_ENABLE_KEEP_REFRESH_VIEW;
            setEnablePinRefreshViewWhileLoading(false);
        }
    }

    /**
     * The flag has been set to perform load more when the content view scrolling to bottom.
     * <p>是否已经开启到底部自动加载更多</p>
     *
     * @return Enabled
     */
    public boolean isEnabledAutoLoadMore() {
        return (mFlag & FLAG_ENABLE_AUTO_PERFORM_LOAD_MORE) > 0;
    }

    /**
     * If @param enable has been set to true.When the content view scrolling to bottom, it will
     * be perform load more.
     * <p>开启到底自动加载更多</p>
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
     * <p>是否已经开启到顶自动刷新</p>
     *
     * @return Enabled
     */
    public boolean isEnabledAutoRefresh() {
        return (mFlag & FLAG_ENABLE_AUTO_PERFORM_REFRESH) > 0;
    }

    /**
     * If @param enable has been set to true.When the content view scrolling to top, it will be
     * perform refresh.
     * <p>开启到顶自动刷新</p>
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
     * <p>是否已经开启刷新过程中固定刷新视图且不响应触摸移动</p>
     *
     * @return Enabled
     */
    public boolean isEnabledPinRefreshViewWhileLoading() {
        return (mFlag & FLAG_ENABLE_PIN_REFRESH_VIEW_WHILE_LOADING) > 0;
    }

    /**
     * If @param enable has been set to true.The refresh view will pinned at the keep refresh
     * position.
     * <p>设置开启刷新过程中固定刷新视图且不响应触摸移动，该属性只有在
     * {@link SmoothRefreshLayout#setEnablePinContentView(boolean)}和
     * {@link SmoothRefreshLayout#setEnableKeepRefreshView(boolean)}2个属性都为`true`时才能生效</p>
     *
     * @param enable Pin content view
     */
    public void setEnablePinRefreshViewWhileLoading(boolean enable) {
        if (enable) {
            if (isEnabledPinContentView() && isEnabledKeepRefreshView()) {
                mFlag = mFlag | FLAG_ENABLE_PIN_REFRESH_VIEW_WHILE_LOADING;
            } else {
                throw new IllegalArgumentException("This method can only be enabled if setEnablePinContentView" +
                        " and setEnableKeepRefreshView are set be true");
            }
        } else {
            mFlag = mFlag & ~FLAG_ENABLE_PIN_REFRESH_VIEW_WHILE_LOADING;
        }
    }

    /**
     * The flag has been set to pinned content view while loading.
     * <p>是否已经开启了固定内容视图</p>
     *
     * @return Enabled
     */
    public boolean isEnabledPinContentView() {
        return (mFlag & FLAG_ENABLE_PIN_CONTENT_VIEW) > 0;
    }

    /**
     * If @param enable has been set to true. The content view will be pinned in the start pos.
     * <p>设置开启固定内容视图</p>
     *
     * @param enable Pin content view
     */
    public void setEnablePinContentView(boolean enable) {
        if (enable) {
            mFlag = mFlag | FLAG_ENABLE_PIN_CONTENT_VIEW;
        } else {
            mFlag = mFlag & ~FLAG_ENABLE_PIN_CONTENT_VIEW;
            setEnablePinRefreshViewWhileLoading(false);
        }
    }

    /**
     * The flag has been set to dynamic search the target view.
     * <p>是否已经开启了固定内容视图</p>
     *
     * @return Enabled
     */
    public boolean isEnabledDynamicEnsureTargetView() {
        return (mFlag & FLAG_ENABLE_DYNAMIC_ENSURE_TARGET_VIEW) > 0;
    }

    /**
     * If @param enable has been set to true. Frame will be dynamic search the target view.
     *
     * @param enable dynamic search
     */
    public void setEnableDynamicEnsureTargetView(boolean enable) {
        if (enable) {
            mFlag = mFlag | FLAG_ENABLE_DYNAMIC_ENSURE_TARGET_VIEW;
        } else {
            mFlag = mFlag & ~FLAG_ENABLE_DYNAMIC_ENSURE_TARGET_VIEW;
        }
    }

    /**
     * The flag has been set to perform refresh when Fling.
     * <p>是否已经开启了当收回刷新视图的手势被触发且当前位置大于触发刷新的位置时，将可以触发刷新同时将不存在Fling效果的功能,</p>
     *
     * @return Enabled
     */
    public boolean isEnabledPerformFreshWhenFling() {
        return (mFlag & FLAG_ENABLE_PERFORM_FRESH_WHEN_FLING) > 0;
    }

    /**
     * If @param enable has been set to true. When the gesture of retracting the refresh view is
     * triggered and the current offset is greater than the trigger refresh offset, the fresh can
     * be performed without the Fling effect.
     * <p>当收回刷新视图的手势被触发且当前位置大于触发刷新的位置时，将可以触发刷新同时将不存在Fling效果</p>
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
        //Use the static default creator to create the Footer view
        if (!isDisabledLoadMore() && mFooterView == null && sCreator != null
                && mMode == Constants.MODE_DEFAULT) {
            final IRefreshView<IIndicator> footer = sCreator.createFooter(this);
            if (footer != null) {
                setFooterView(footer);
            }
        }
        return mFooterView;
    }

    /**
     * Set the Footer view.
     * <p>设置Footer视图</p>
     *
     * @param footer Footer view
     */
    public void setFooterView(@NonNull IRefreshView footer) {
        if (mFooterView != null) {
            removeView(mFooterView.getView());
            mFooterView = null;
        }
        if (footer.getType() != IRefreshView.TYPE_FOOTER)
            throw new IllegalArgumentException("Wrong type,FooterView type must be " +
                    "TYPE_FOOTER");
        View view = footer.getView();
        addFreshViewLayoutParams(view);
        mViewsZAxisNeedReset = true;
        addView(view);
    }

    @Nullable
    public IRefreshView<IIndicator> getHeaderView() {
        //Use the static default creator to create the Header view
        if (!isDisabledRefresh() && mHeaderView == null && sCreator != null
                && mMode == Constants.MODE_DEFAULT) {
            final IRefreshView<IIndicator> header = sCreator.createHeader(this);
            if (header != null) {
                setHeaderView(header);
            }
        }
        return mHeaderView;
    }

    /**
     * Set the Header view.
     * <p>设置Header视图</p>
     *
     * @param header Header view
     */
    public void setHeaderView(@NonNull IRefreshView header) {
        if (mHeaderView != null) {
            removeView(mHeaderView.getView());
            mHeaderView = null;
        }
        if (header.getType() != IRefreshView.TYPE_HEADER)
            throw new IllegalArgumentException("Wrong type,HeaderView type must be " +
                    "TYPE_HEADER");
        View view = header.getView();
        addFreshViewLayoutParams(view);
        mViewsZAxisNeedReset = true;
        addView(view);
    }

    /**
     * Set the content view.
     * <p>设置内容视图`content`状态对应的视图</p>
     *
     * @param content Content view
     */
    public void setContentView(View content) {
        if (mTargetView != null)
            removeView(mTargetView);
        mContentResId = View.NO_ID;
        ViewGroup.LayoutParams lp = content.getLayoutParams();
        if (lp == null) {
            lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            content.setLayoutParams(lp);
        }
        mTargetView = content;
        mViewsZAxisNeedReset = true;
        addView(content);
    }

    /**
     * Reset scroller interpolator.
     * <p>重置Scroller的插值器</p>
     */
    public void resetScrollerInterpolator() {
        if (mSpringInterpolator != sSpringInterpolator) {
            setSpringInterpolator(sSpringInterpolator);
        }
    }

    /**
     * Set the scroller default interpolator.
     * <p>设置Scroller的默认插值器</p>
     *
     * @param interpolator Scroller interpolator
     */
    public void setSpringInterpolator(@NonNull Interpolator interpolator) {
        if (mSpringInterpolator == interpolator)
            return;
        mSpringInterpolator = interpolator;
        if (mScrollChecker.$Mode == Constants.SCROLLER_MODE_SPRING) {
            mScrollChecker.setInterpolator(interpolator);
        }
    }

    /**
     * Set the scroller spring back interpolator.
     *
     * @param interpolator Scroller interpolator
     */
    public void setSpringBackInterpolator(@NonNull Interpolator interpolator) {
        if (mSpringBackInterpolator == interpolator)
            return;
        mSpringBackInterpolator = interpolator;
        if (mScrollChecker.$Mode == Constants.SCROLLER_MODE_SPRING_BACK) {
            mScrollChecker.setInterpolator(interpolator);
        }
    }

    /**
     * Get the ScrollChecker current mode.
     *
     * @return the mode {@link Constants#SCROLLER_MODE_NONE},
     * {@link Constants#SCROLLER_MODE_PRE_FLING},{@link Constants#SCROLLER_MODE_FLING},
     * {@link Constants#SCROLLER_MODE_FLING_BACK},{@link Constants#SCROLLER_MODE_SPRING},
     * {@link Constants#SCROLLER_MODE_SPRING_BACK}.
     */
    public byte getScrollMode() {
        return mScrollChecker.$Mode;
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
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
            requestLayout();
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
            requestLayout();
        }
    }

    public void setMode(@Mode int mode) {
        mMode = mode;
        requestLayout();
    }

    protected boolean onFling(float vx, final float vy, boolean nested) {
        if (sDebug) SRLog.d(TAG, "onFling() vx: %s, vy: %s", vx, vy);
        if ((isNeedInterceptTouchEvent() || isCanNotAbortOverScrolling()))
            return true;
        if (mPreventForAnotherDirection) {
            return nested && dispatchNestedPreFling(-vx, -vy);
        }
        float realVelocity = isVerticalOrientation() ? vy : vx;
        final boolean canNotChildScrollDown = !isNotYetInEdgeCannotMoveFooter();
        final boolean canNotChildScrollUp = !isNotYetInEdgeCannotMoveHeader();
        if (!mIndicator.isAlreadyHere(IIndicator.START_POS)) {
            if (!isEnabledPinRefreshViewWhileLoading()) {
                if (Math.abs(realVelocity) > mMinimumFlingVelocity * 2) {
                    if ((canNotChildScrollUp && realVelocity > 0 && isMovingHeader())
                            || (canNotChildScrollDown && realVelocity < 0 && isMovingFooter())) {
                        if (isEnabledOverScroll()) {
                            if (isDisabledLoadMoreWhenContentNotFull()
                                    && canNotChildScrollDown && canNotChildScrollUp) {
                                return true;
                            }
                            boolean invert = realVelocity < 0;
                            realVelocity = (float) Math.pow(Math.abs(realVelocity), .5f);
                            mScrollChecker.startPreFling(invert ? -realVelocity : realVelocity);
                        }
                    } else {
                        if (!isEnabledPerformFreshWhenFling()) {
                            mScrollChecker.startPreFling(realVelocity);
                        } else if (isMovingHeader()
                                && mIndicator.getCurrentPos() < mIndicator.getOffsetToRefresh()) {
                            mScrollChecker.startPreFling(realVelocity);
                        } else if (isMovingFooter()
                                && mIndicator.getCurrentPos() < mIndicator.getOffsetToLoadMore()) {
                            mScrollChecker.startPreFling(realVelocity);
                        }
                    }
                }
                return true;
            }
        } else {
            if (isEnabledOverScroll() && (!isEnabledPinRefreshViewWhileLoading()
                    || ((realVelocity >= 0 || !isDisabledLoadMore())
                    && (realVelocity <= 0 || !isDisabledRefresh())))) {
                if (isDisabledLoadMoreWhenContentNotFull() && realVelocity < 0
                        && canNotChildScrollDown && canNotChildScrollUp) {
                    return nested && dispatchNestedPreFling(-vx, -vy);
                }
                mScrollChecker.startFling(realVelocity);
            }
            tryToResetMovingStatus();
            invalidate();
        }
        return nested && dispatchNestedPreFling(-vx, -vy);
    }

    @Override
    public boolean onStartNestedScroll(@NonNull View child, @NonNull View target, int nestedScrollAxes) {
        return onStartNestedScroll(child, target, nestedScrollAxes, ViewCompat.TYPE_TOUCH);
    }

    @Override
    public boolean onStartNestedScroll(@NonNull View child, @NonNull View target, int axes, int type) {
        if (sDebug) SRLog.d(TAG, "onStartNestedScroll(): axes: %s, type: %s", axes, type);
        return isEnabled() && isNestedScrollingEnabled() && mTargetView != null
                && (axes & getSupportScrollAxis()) != 0;
    }

    @Override
    public void onNestedScrollAccepted(@NonNull View child, @NonNull View target, int axes) {
        onNestedScrollAccepted(child, target, axes, ViewCompat.TYPE_TOUCH);
    }

    @Override
    public void onNestedScrollAccepted(@NonNull View child, @NonNull View target, int axes, int type) {
        if (sDebug) SRLog.d(TAG, "onNestedScrollAccepted(): axes: %s, type: %s", axes, type);
        // Reset the counter of how much leftover scroll needs to be consumed.
        mNestedScrollingParentHelper.onNestedScrollAccepted(child, target, axes, type);
        // Dispatch up to the nested parent
        startNestedScroll(axes & getSupportScrollAxis(), type);
        if (type == ViewCompat.TYPE_TOUCH)
            mIndicatorSetter.onFingerDown();
        mLastNestedType = type;
        mNestedScrollInProgress = true;
    }

    @Override
    public void onNestedPreScroll(@NonNull View target, int dx, int dy, @NonNull int[] consumed) {
        onNestedPreScroll(target, dx, dy, consumed, ViewCompat.TYPE_TOUCH);
    }

    @Override
    public void onNestedPreScroll(@NonNull View target, int dx, int dy, @NonNull int[] consumed, int type) {
        final boolean isVerticalOrientation = isVerticalOrientation();
        if (type == ViewCompat.TYPE_TOUCH) {
            if (tryToFilterTouchEvent(null)) {
                if (isVerticalOrientation) consumed[1] = dy;
                else consumed[0] = dx;
            } else {
                mScrollChecker.stop();
                final boolean canNotChildScrollDown = !isNotYetInEdgeCannotMoveFooter();
                final boolean canNotChildScrollUp = !isNotYetInEdgeCannotMoveHeader();
                final int distance = isVerticalOrientation ? dy : dx;
                if (distance > 0 && !isDisabledRefresh() && canNotChildScrollUp
                        && !(isEnabledPinRefreshViewWhileLoading() && isRefreshing()
                        && mIndicator.isOverOffsetToKeepHeaderWhileLoading())) {
                    if (!mIndicator.isAlreadyHere(IIndicator.START_POS) && isMovingHeader()) {
                        mIndicatorSetter.onFingerMove(mIndicator.getLastMovePoint()[0] - dx,
                                mIndicator.getLastMovePoint()[1] - dy);
                        moveHeaderPos(mIndicator.getOffset());
                        if (isVerticalOrientation) consumed[1] = dy;
                        else consumed[0] = dx;
                    } else {
                        if (isVerticalOrientation)
                            mIndicatorSetter.onFingerMove(mIndicator.getLastMovePoint()[0] - dx,
                                    mIndicator.getLastMovePoint()[1]);
                        else
                            mIndicatorSetter.onFingerMove(mIndicator.getLastMovePoint()[0],
                                    mIndicator.getLastMovePoint()[1] - dy);
                    }
                }
                if (distance < 0 && !isDisabledLoadMore() && canNotChildScrollDown
                        && !(isEnabledPinRefreshViewWhileLoading() && isLoadingMore()
                        && mIndicator.isOverOffsetToKeepFooterWhileLoading())) {
                    if (!mIndicator.isAlreadyHere(IIndicator.START_POS) && isMovingFooter()) {
                        mIndicatorSetter.onFingerMove(mIndicator.getLastMovePoint()[0] - dx,
                                mIndicator.getLastMovePoint()[1] - dy);
                        moveFooterPos(mIndicator.getOffset());
                        if (isVerticalOrientation) consumed[1] = dy;
                        else consumed[0] = dx;
                    } else {
                        if (isVerticalOrientation)
                            mIndicatorSetter.onFingerMove(mIndicator.getLastMovePoint()[0] - dx,
                                    mIndicator.getLastMovePoint()[1]);
                        else
                            mIndicatorSetter.onFingerMove(mIndicator.getLastMovePoint()[0],
                                    mIndicator.getLastMovePoint()[1] - dy);
                    }
                }
                if (isMovingFooter() && isFooterInProcessing() && mStatus == SR_STATUS_COMPLETE
                        && mIndicator.hasLeftStartPosition() && !canNotChildScrollDown) {
                    mScrollChecker.scrollTo(IIndicator.START_POS, 0);
                    if (isVerticalOrientation) consumed[1] = dy;
                    else consumed[0] = dx;
                }
            }
            tryToResetMovingStatus();
        }
        // Now let our nested parent consume the leftovers
        final int[] parentConsumed = mParentScrollConsumed;
        parentConsumed[0] = 0;
        parentConsumed[1] = 0;
        if (dispatchNestedPreScroll(dx - consumed[0], dy - consumed[1], parentConsumed, null, type)) {
            consumed[0] += parentConsumed[0];
            consumed[1] += parentConsumed[1];
            onNestedScrollChanged();
        } else if (type == ViewCompat.TYPE_NON_TOUCH) {
            onNestedScrollChanged();
            if (!isMovingContent() && !(isEnabledPinRefreshViewWhileLoading())) {
                if (isVerticalOrientation) parentConsumed[1] = dy;
                else parentConsumed[0] = dx;
                consumed[0] += parentConsumed[0];
                consumed[1] += parentConsumed[1];
            }
        }
        if (sDebug)
            SRLog.d(TAG, "onNestedPreScroll(): dx: %s, dy: %s, consumed: %s, type: %s",
                    dx, dy, Arrays.toString(consumed), type);
    }

    @Override
    public int getNestedScrollAxes() {
        return mNestedScrollingParentHelper.getNestedScrollAxes();
    }

    @Override
    public void onStopNestedScroll(@NonNull View target) {
        onStopNestedScroll(target, ViewCompat.TYPE_TOUCH);
    }

    @Override
    public void onStopNestedScroll(@NonNull View target, int type) {
        if (sDebug) SRLog.d(TAG, "onStopNestedScroll() type: %s", type);
        mNestedScrollingParentHelper.onStopNestedScroll(target, type);
        if (mLastNestedType == type)
            mNestedScrollInProgress = false;
        mIsInterceptTouchEventInOnceTouch = isNeedInterceptTouchEvent();
        mIsLastOverScrollCanNotAbort = isCanNotAbortOverScrolling();
        // Dispatch up our nested parent
        mNestedScrollingChildHelper.stopNestedScroll(type);
        if (!isAutoRefresh() && type == ViewCompat.TYPE_TOUCH) {
            mIndicatorSetter.onFingerUp();
            onFingerUp();
        }
    }

    @Override
    public void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, ViewCompat.TYPE_TOUCH);
    }

    @Override
    public void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {
        if (sDebug)
            SRLog.d(TAG, "onNestedScroll(): dxConsumed: %s, dyConsumed: %s, dxUnconsumed: %s" +
                    " dyUnconsumed: %s, type: %s", dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type);
        // Dispatch up to the nested parent first
        dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed,
                mParentOffsetInWindow, type);
        if (type == ViewCompat.TYPE_TOUCH) {
            if (tryToFilterTouchEvent(null))
                return;
            final int dx = dxUnconsumed + mParentOffsetInWindow[0];
            final int dy = dyUnconsumed + mParentOffsetInWindow[1];
            final boolean canNotChildScrollDown = !isNotYetInEdgeCannotMoveFooter();
            final boolean canNotChildScrollUp = !isNotYetInEdgeCannotMoveHeader();
            final boolean isVerticalOrientation = isVerticalOrientation();
            final int distance = isVerticalOrientation ? dy : dx;
            if (distance < 0 && !isDisabledRefresh() && canNotChildScrollUp
                    && !(isEnabledPinRefreshViewWhileLoading() && isRefreshing()
                    && mIndicator.isOverOffsetToKeepHeaderWhileLoading())) {
                mIndicatorSetter.onFingerMove(mIndicator.getLastMovePoint()[0] - dx,
                        mIndicator.getLastMovePoint()[1] - dy);
                moveHeaderPos(mIndicator.getOffset());
            } else if (distance > 0 && !isDisabledLoadMore() && canNotChildScrollDown
                    && !(isDisabledLoadMoreWhenContentNotFull() && canNotChildScrollUp
                    && mIndicator.isAlreadyHere(IIndicator.START_POS))
                    && !(isEnabledPinRefreshViewWhileLoading() && isLoadingMore()
                    && mIndicator.isOverOffsetToKeepFooterWhileLoading())) {
                mIndicatorSetter.onFingerMove(mIndicator.getLastMovePoint()[0] - dx,
                        mIndicator.getLastMovePoint()[1] - dy);
                moveFooterPos(mIndicator.getOffset());
            }
            tryToResetMovingStatus();
        }
        if (dxConsumed > 0 || dyConsumed > 0) {
            onNestedScrollChanged();
        }
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return mNestedScrollingChildHelper.isNestedScrollingEnabled();
    }

    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        mNestedScrollingChildHelper.setNestedScrollingEnabled(enabled);
    }

    @Override
    public boolean startNestedScroll(int axes) {
        return mNestedScrollingChildHelper.startNestedScroll(axes);
    }

    @Override
    public boolean startNestedScroll(int axes, int type) {
        return mNestedScrollingChildHelper.startNestedScroll(axes, type);
    }

    @Override
    public void stopNestedScroll() {
        stopNestedScroll(ViewCompat.TYPE_TOUCH);
    }

    @Override
    public void stopNestedScroll(int type) {
        final View targetView;
        if (mScrollTargetView != null)
            targetView = mScrollTargetView;
        else if (mAutoFoundScrollTargetView != null)
            targetView = mAutoFoundScrollTargetView;
        else targetView = mTargetView;
        if (ViewCompat.isNestedScrollingEnabled(targetView))
            ViewCompat.stopNestedScroll(targetView, type);
        else
            mNestedScrollingChildHelper.stopNestedScroll(type);
    }

    @Override
    public boolean hasNestedScrollingParent() {
        return mNestedScrollingChildHelper.hasNestedScrollingParent();
    }

    @Override
    public boolean hasNestedScrollingParent(int type) {
        return mNestedScrollingChildHelper.hasNestedScrollingParent(type);
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed,
                                        int dyUnconsumed, int[] offsetInWindow) {
        return mNestedScrollingChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed,
                dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, @Nullable int[] offsetInWindow, int type) {
        return mNestedScrollingChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed,
                dxUnconsumed, dyUnconsumed, offsetInWindow, type);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        return mNestedScrollingChildHelper.dispatchNestedPreScroll(
                dx, dy, consumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, @Nullable int[] consumed, @Nullable int[] offsetInWindow, int type) {
        return mNestedScrollingChildHelper.dispatchNestedPreScroll(
                dx, dy, consumed, offsetInWindow, type);
    }

    @Override
    public boolean onNestedPreFling(@NonNull View target, float velocityX, float velocityY) {
        if (sDebug)
            SRLog.d(TAG, "onNestedPreFling() velocityX: %s, velocityY: %s", velocityX, velocityY);
        return onFling(-velocityX, -velocityY, true);
    }

    @Override
    public boolean onNestedFling(@NonNull View target, float velocityX, float velocityY,
                                 boolean consumed) {
        return dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return mNestedScrollingChildHelper.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return mNestedScrollingChildHelper.dispatchNestedPreFling(velocityX, velocityY);
    }

    @Override
    public void computeScroll() {
        if (mNestedScrollInProgress || !isMovingContent())
            return;
        onNestedScrollChanged();
    }

    public void onNestedScrollChanged() {
        if (mNeedFilterScrollEvent) {
            mNeedFilterScrollEvent = false;
            return;
        }
        tryToPerformScrollToBottomToLoadMore();
        tryToPerformScrollToTopToRefresh();
        notifyNestedScrollChanged();
        mScrollChecker.computeScrollOffset();
    }

    private boolean isVerticalOrientation() {
        final int axis = getSupportScrollAxis();
        if (axis == ViewCompat.SCROLL_AXIS_NONE)
            throw new IllegalArgumentException("Unsupported operation , " +
                    "Support scroll axis must be SCROLL_AXIS_HORIZONTAL or SCROLL_AXIS_VERTICAL !!");
        else
            return axis == ViewCompat.SCROLL_AXIS_VERTICAL;
    }

    /**
     * Check the Z-Axis relationships of the views need to be rearranged
     */
    protected void checkViewsZAxisNeedReset() {
        final int count = getChildCount();
        if (mViewsZAxisNeedReset && count > 0) {
            mCachedViews.clear();
            final boolean isEnabledHeaderDrawer = isEnabledHeaderDrawerStyle();
            final boolean isEnabledFooterDrawer = isEnabledFooterDrawerStyle();
            if (isEnabledHeaderDrawer && isEnabledFooterDrawer) {
                for (int i = count - 1; i >= 0; i--) {
                    View view = getChildAt(i);
                    if (view != mHeaderView.getView() && view != mFooterView.getView())
                        mCachedViews.add(view);
                }
            } else if (isEnabledHeaderDrawer) {
                for (int i = count - 1; i >= 0; i--) {
                    View view = getChildAt(i);
                    if (view != mHeaderView.getView())
                        mCachedViews.add(view);
                }
            } else if (isEnabledFooterDrawer) {
                for (int i = count - 1; i >= 0; i--) {
                    View view = getChildAt(i);
                    if (view != mFooterView.getView())
                        mCachedViews.add(view);
                }
            } else {
                for (int i = count - 1; i >= 0; i--) {
                    View view = getChildAt(i);
                    if (view != mTargetView)
                        mCachedViews.add(view);
                }
            }
            final int viewCount = mCachedViews.size();
            if (viewCount > 0) {
                for (int i = viewCount - 1; i >= 0; i--) {
                    mCachedViews.get(i).bringToFront();
                }
            }
            mCachedViews.clear();
        }
        mViewsZAxisNeedReset = false;
    }

    protected void destroy() {
        reset();
        if (mHeaderRefreshCompleteHook != null)
            mHeaderRefreshCompleteHook.mLayout = null;
        if (mFooterRefreshCompleteHook != null)
            mFooterRefreshCompleteHook.mLayout = null;
        if (sDebug) SRLog.d(TAG, "destroy()");
    }

    protected void reset() {
        if (isRefreshing() || isLoadingMore())
            notifyUIRefreshComplete(false, true);
        if (!mIndicator.isAlreadyHere(IIndicator.START_POS))
            mScrollChecker.scrollTo(IIndicator.START_POS, 0);
        mScrollChecker.setInterpolator(mSpringInterpolator);
        final byte old = mStatus;
        mStatus = SR_STATUS_INIT;
        notifyStatusChanged(old, mStatus);
        mAutomaticActionTriggered = true;
        mScrollChecker.stop();
        if (mDelayToRefreshComplete != null)
            removeCallbacks(mDelayToRefreshComplete);
        if (sDebug) SRLog.d(TAG, "reset()");
    }

    protected void tryToPerformAutoRefresh() {
        if (!mAutomaticActionTriggered) {
            if (sDebug) SRLog.d(TAG, "tryToPerformAutoRefresh()");
            if (isHeaderInProcessing()) {
                if (mHeaderView == null || mIndicator.getHeaderHeight() <= 0)
                    return;
                scrollToTriggeredAutomatic(true);
            } else if (isFooterInProcessing()) {
                if (mFooterView == null || mIndicator.getFooterHeight() <= 0)
                    return;
                scrollToTriggeredAutomatic(false);
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected void ensureFreshView(View child) {
        if (child instanceof IRefreshView) {
            IRefreshView<IIndicator> view = (IRefreshView<IIndicator>) child;
            switch (view.getType()) {
                case IRefreshView.TYPE_HEADER:
                    if (mHeaderView != null)
                        throw new IllegalArgumentException("Unsupported operation , " +
                                "HeaderView only can be add once !!");
                    mHeaderView = view;
                    break;
                case IRefreshView.TYPE_FOOTER:
                    if (mFooterView != null)
                        throw new IllegalArgumentException("Unsupported operation , " +
                                "FooterView only can be add once !!");
                    mFooterView = view;
                    break;
            }
        }
    }

    protected void addFreshViewLayoutParams(View view) {
        ViewGroup.LayoutParams lp = view.getLayoutParams();
        if (lp == null) {
            lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            view.setLayoutParams(lp);
        }
    }

    private void ensureTargetView() {
        if (mTargetView == null) {
            final int count = getChildCount();
            if (mContentResId != View.NO_ID) {
                for (int i = count - 1; i >= 0; i--) {
                    View child = getChildAt(i);
                    if (mContentResId == child.getId()) {
                        mTargetView = child;
                        break;
                    } else if (child instanceof ViewGroup) {
                        final View view = foundViewInViewGroupById((ViewGroup) child, mContentResId);
                        if (view != null) {
                            mTargetView = child;
                            mScrollTargetView = view;
                            break;
                        }
                    }
                }
            } else {
                for (int i = count - 1; i >= 0; i--) {
                    View child = getChildAt(i);
                    View topTempView = null;
                    if (child.getVisibility() == VISIBLE && !(child instanceof IRefreshView)) {
                        if (isEnabledDynamicEnsureTargetView()) {
                            View view = ensureScrollTargetView(child, getLeft() + getWidth() / 2,
                                    getTop() + getHeight() / 2);
                            topTempView = child;
                            if (view != null) {
                                mTargetView = child;
                                if (view != child) {
                                    mAutoFoundScrollTargetView = view;
                                }
                                break;
                            }
                        } else {
                            mTargetView = child;
                        }
                    }
                    if (mTargetView == null)
                        mTargetView = topTempView;
                }
            }
        }
        if (mStickyHeaderView == null && mStickyHeaderResId != NO_ID)
            mStickyHeaderView = findViewById(mStickyHeaderResId);
        if (mStickyFooterView == null && mStickyFooterResId != NO_ID)
            mStickyFooterView = findViewById(mStickyFooterResId);
        mHeaderView = getHeaderView();
        mFooterView = getFooterView();
    }

    /**
     * Returns true if a child view contains the specified point when transformed
     * into its coordinate space.
     *
     * @see ViewGroup source code
     */
    private boolean isTransformedTouchPointInView(float x, float y, View group, View child) {
        if (child.getVisibility() != VISIBLE || child.getAnimation() != null) {
            return false;
        }
        if (mCachedPoint == null)
            mCachedPoint = new float[2];
        mCachedPoint[0] = x;
        mCachedPoint[1] = y;
        mCachedPoint[0] += group.getScrollX() - child.getLeft();
        mCachedPoint[1] += group.getScrollY() - child.getTop();
        final boolean isInView = mCachedPoint[0] >= 0 && mCachedPoint[1] >= 0
                && mCachedPoint[0] < (child.getWidth())
                && mCachedPoint[1] < ((child.getHeight()));
        if (isInView) {
            mCachedPoint[0] = mCachedPoint[0] - x;
            mCachedPoint[1] = mCachedPoint[1] - y;
        }
        return isInView;
    }

    protected View ensureScrollTargetView(View target, float x, float y) {
        if (target instanceof IRefreshView || target.getVisibility() != VISIBLE
                || target.getAnimation() != null)
            return null;
        if (isScrollingView(target))
            return target;
        if (target instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) target;
            final int count = group.getChildCount();
            for (int i = count - 1; i >= 0; i--) {
                View child = group.getChildAt(i);
                if (isTransformedTouchPointInView(x, y, group, child)) {
                    View view = ensureScrollTargetView(child, x + mCachedPoint[0], y +
                            mCachedPoint[1]);
                    if (view != null)
                        return view;
                }
            }
        }
        return null;
    }

    protected boolean isScrollingView(View target) {
        return ScrollCompat.isScrollingView(target);
    }

    protected boolean processDispatchTouchEvent(MotionEvent ev) {
        final int action = ev.getAction() & MotionEvent.ACTION_MASK;
        if (sDebug) SRLog.d(TAG, "processDispatchTouchEvent(): action: %s", action);
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);
        switch (action) {
            case MotionEvent.ACTION_UP:
                final int pointerId = ev.getPointerId(0);
                mVelocityTracker.computeCurrentVelocity(1000, mMaximumFlingVelocity);
                float vy = mVelocityTracker.getYVelocity(pointerId);
                float vx = mVelocityTracker.getXVelocity(pointerId);
                if (Math.abs(vx) >= mMinimumFlingVelocity
                        || Math.abs(vy) >= mMinimumFlingVelocity) {
                    boolean handler = onFling(vx, vy, false);
                    if (handler) ev.setAction(MotionEvent.ACTION_CANCEL);
                }
            case MotionEvent.ACTION_CANCEL:
                mIsFingerInsideAnotherDirectionView = false;
                mIndicatorSetter.onFingerUp();
                mPreventForAnotherDirection = false;
                mDealAnotherDirectionMove = false;
                if (isNeedFilterTouchEvent()) {
                    mIsInterceptTouchEventInOnceTouch = false;
                    if (mIsLastOverScrollCanNotAbort && mIndicator.isAlreadyHere(IIndicator.START_POS))
                        mScrollChecker.stop();
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
                if (mVelocityTracker != null)
                    mVelocityTracker.recycle();
                mVelocityTracker = null;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >>
                        MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                if (ev.getPointerId(pointerIndex) == mTouchPointerId) {
                    // Pick a new pointer to pick up the slack.
                    final int newIndex = pointerIndex == 0 ? 1 : 0;
                    mTouchPointerId = ev.getPointerId(newIndex);
                    mIndicatorSetter.onFingerMove(ev.getX(newIndex), ev.getY(newIndex));
                }
                final int count = ev.getPointerCount();
                if (mVelocityTracker != null) {
                    mVelocityTracker.addMovement(ev);
                    // Check the dot product of current velocities.
                    // If the pointer that left was opposing another velocity vector, clear.
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
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                mTouchPointerId = ev.getPointerId(ev.getActionIndex());
                mIndicatorSetter.onFingerMove(ev.getX(ev.getActionIndex()), ev.getY(ev.getActionIndex()));
                break;
            case MotionEvent.ACTION_DOWN:
                mIndicatorSetter.onFingerUp();
                mTouchPointerId = ev.getPointerId(0);
                mIndicatorSetter.onFingerDown(ev.getX(), ev.getY());
                mIsFingerInsideAnotherDirectionView = isDisabledWhenAnotherDirectionMove()
                        && (!isEnableCheckInsideAnotherDirectionView()
                        || isInsideAnotherDirectionView(ev.getRawX(), ev.getRawY()));
                mIsInterceptTouchEventInOnceTouch = isNeedInterceptTouchEvent();
                mIsLastOverScrollCanNotAbort = isCanNotAbortOverScrolling();
                if (!isNeedFilterTouchEvent())
                    mScrollChecker.stop();
                mPreventForAnotherDirection = false;
                if (mScrollTargetView == null && isEnabledDynamicEnsureTargetView()) {
                    View view = ensureScrollTargetView(this, ev.getX(), ev.getY());
                    if (view != null && mTargetView != view && mAutoFoundScrollTargetView != view) {
                        mAutoFoundScrollTargetView = view;
                    }
                } else {
                    mAutoFoundScrollTargetView = null;
                }
                dispatchTouchEventSuper(ev);
                return true;
            case MotionEvent.ACTION_MOVE:
                if (!mIndicator.hasTouched())
                    return super.dispatchTouchEvent(ev);
                final int index = ev.findPointerIndex(mTouchPointerId);
                if (index < 0) {
                    Log.e(TAG, "Error processing scroll; pointer index for id " +
                            mTouchPointerId + " not found. Did any MotionEvents get skipped?");
                    return super.dispatchTouchEvent(ev);
                }
                mLastMoveEvent = ev;
                if (tryToFilterTouchEvent(ev))
                    return true;
                tryToResetMovingStatus();
                final float[] pressDownPoint = mIndicator.getFingerDownPoint();
                final float offsetX = ev.getX(index) - pressDownPoint[0];
                final float offsetY = ev.getY(index) - pressDownPoint[1];
                if (!mDealAnotherDirectionMove)
                    tryToDealAnotherDirectionMove(offsetX, offsetY);
                if (mPreventForAnotherDirection)
                    return super.dispatchTouchEvent(ev);
                mIndicatorSetter.onFingerMove(ev.getX(index), ev.getY(index));
                final boolean canNotChildScrollDown = !isNotYetInEdgeCannotMoveFooter();
                final boolean canNotChildScrollUp = !isNotYetInEdgeCannotMoveHeader();
                final float offset = mIndicator.getOffset();
                boolean movingDown = offset > 0;
                if (isMovingFooter() && isFooterInProcessing() && mStatus == SR_STATUS_COMPLETE
                        && mIndicator.hasLeftStartPosition() && !canNotChildScrollDown) {
                    mScrollChecker.scrollTo(IIndicator.START_POS, 0);
                    return dispatchTouchEventSuper(ev);
                }
                if (!movingDown && isDisabledLoadMoreWhenContentNotFull()
                        && mIndicator.isAlreadyHere(IIndicator.START_POS)
                        && canNotChildScrollDown && canNotChildScrollUp) {
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
                        } else if (isRefreshing() && mIndicator.hasLeftStartPosition()) {
                            moveHeaderPos(offset);
                        }
                    } else if (movingDown) {
                        if (!isDisabledRefresh())
                            moveHeaderPos(offset);
                    } else if (!isDisabledLoadMore())
                        moveFooterPos(offset);
                } else if (canMoveUp) {
                    if (!isDisabledRefresh() && !(!canHeaderMoveDown && movingDown))
                        moveHeaderPos(offset);
                } else if (!isDisabledLoadMore() && !(!canFooterMoveUp && !movingDown)) {
                    moveFooterPos(offset);
                }
        }
        return dispatchTouchEventSuper(ev);
    }

    protected void tryToDealAnotherDirectionMove(float offsetX, float offsetY) {
        if (isDisabledWhenAnotherDirectionMove() && mIsFingerInsideAnotherDirectionView) {
            if ((Math.abs(offsetX) >= mTouchSlop
                    && Math.abs(offsetX) > Math.abs(offsetY))) {
                mPreventForAnotherDirection = true;
                mDealAnotherDirectionMove = true;
            } else if (Math.abs(offsetX) < mTouchSlop
                    && Math.abs(offsetY) < mTouchSlop) {
                mDealAnotherDirectionMove = false;
                mPreventForAnotherDirection = true;
            } else {
                mDealAnotherDirectionMove = true;
                mPreventForAnotherDirection = false;
            }
        } else {
            mPreventForAnotherDirection = Math.abs(offsetX) < mTouchSlop
                    && Math.abs(offsetY) < mTouchSlop;
            if (!mPreventForAnotherDirection)
                mDealAnotherDirectionMove = true;
        }
    }

    protected boolean tryToFilterTouchEvent(MotionEvent ev) {
        if (mIsInterceptTouchEventInOnceTouch) {
            if ((!isAutoRefresh() && mIndicator.isAlreadyHere(IIndicator.START_POS)
                    && !mScrollChecker.$IsScrolling)
                    || (isAutoRefresh() && (isRefreshing() || isLoadingMore()))) {
                mScrollChecker.stop();
                if (ev != null) makeNewTouchDownEvent(ev);
                mIsInterceptTouchEventInOnceTouch = false;
            }
            return true;
        }
        if (mIsLastOverScrollCanNotAbort) {
            if (mIndicator.isAlreadyHere(IIndicator.START_POS) && !mScrollChecker.$IsScrolling) {
                if (ev != null) makeNewTouchDownEvent(ev);
                mIsLastOverScrollCanNotAbort = false;
            }
            return true;
        }
        if (mIsSpringBackCanNotBeInterrupted) {
            if (isEnabledNoMoreData()) {
                mIsSpringBackCanNotBeInterrupted = false;
                return false;
            }
            if (mIndicator.isAlreadyHere(IIndicator.START_POS) && !mScrollChecker.$IsScrolling) {
                if (ev != null) makeNewTouchDownEvent(ev);
                mIsSpringBackCanNotBeInterrupted = false;
            }
            return true;
        }
        return false;
    }

    private void scrollToTriggeredAutomatic(boolean isRefresh) {
        switch (mAutomaticAction) {
            case Constants.ACTION_NOTHING:
                if (isRefresh)
                    triggeredRefresh(false);
                else
                    triggeredLoadMore(false);
                break;
            case Constants.ACTION_NOTIFY:
                mFlag |= FLAG_AUTO_REFRESH;
                break;
            case Constants.ACTION_AT_ONCE:
                if (isRefresh)
                    triggeredRefresh(true);
                else
                    triggeredLoadMore(true);
                break;
        }
        int offset;
        if (isRefresh) {
            if (isEnabledKeepRefreshView()) {
                final int offsetToKeepHeaderWhileLoading = mIndicator
                        .getOffsetToKeepHeaderWhileLoading();
                final int offsetToRefresh = mIndicator.getOffsetToRefresh();
                offset = (offsetToKeepHeaderWhileLoading >= offsetToRefresh)
                        ? offsetToKeepHeaderWhileLoading : offsetToRefresh;
            } else {
                offset = mIndicator.getOffsetToRefresh();
            }
        } else {
            if (isEnabledKeepRefreshView()) {
                final int offsetToKeepFooterWhileLoading = mIndicator
                        .getOffsetToKeepFooterWhileLoading();
                final int offsetToLoadMore = mIndicator.getOffsetToLoadMore();
                offset = (offsetToKeepFooterWhileLoading >= offsetToLoadMore)
                        ? offsetToKeepFooterWhileLoading : offsetToLoadMore;
            } else {
                offset = mIndicator.getOffsetToLoadMore();
            }
        }
        mAutomaticActionTriggered = true;
        mScrollChecker.scrollTo(offset, mAutomaticActionUseSmoothScroll ?
                isRefresh ? mDurationToCloseHeader : mDurationToCloseFooter : 0);
    }

    protected void preparePaint() {
        if (mBackgroundPaint == null) {
            mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mBackgroundPaint.setStyle(Paint.Style.FILL);
        }
    }

    protected boolean isNeedInterceptTouchEvent() {
        return (isEnabledInterceptEventWhileLoading() && (isRefreshing() || isLoadingMore()))
                || mAutomaticActionUseSmoothScroll;
    }

    protected boolean isNeedFilterTouchEvent() {
        return mIsLastOverScrollCanNotAbort || mIsSpringBackCanNotBeInterrupted
                || mIsInterceptTouchEventInOnceTouch;
    }

    protected boolean isCanNotAbortOverScrolling() {
        return (mScrollChecker.isOverScrolling()
                && (((isMovingHeader() && isDisabledRefresh()))
                || (isMovingFooter() && isDisabledLoadMore())));
    }

    public boolean isNotYetInEdgeCannotMoveHeader() {
        if (mScrollTargetView != null)
            return isNotYetInEdgeCannotMoveHeader(mScrollTargetView);
        if (mAutoFoundScrollTargetView != null)
            return isNotYetInEdgeCannotMoveHeader(mAutoFoundScrollTargetView);
        return isNotYetInEdgeCannotMoveHeader(mTargetView);
    }

    public boolean isNotYetInEdgeCannotMoveFooter() {
        if (mScrollTargetView != null)
            return isNotYetInEdgeCannotMoveFooter(mScrollTargetView);
        if (mAutoFoundScrollTargetView != null)
            return isNotYetInEdgeCannotMoveFooter(mAutoFoundScrollTargetView);
        return isNotYetInEdgeCannotMoveFooter(mTargetView);
    }

    protected boolean isNotYetInEdgeCannotMoveHeader(View view) {
        if (mInEdgeCanMoveHeaderCallBack != null)
            return mInEdgeCanMoveHeaderCallBack.isNotYetInEdgeCannotMoveHeader(this, view,
                    mHeaderView);
        return ScrollCompat.canChildScrollUp(view);
    }

    protected boolean isNotYetInEdgeCannotMoveFooter(View view) {
        if (mInEdgeCanMoveFooterCallBack != null)
            return mInEdgeCanMoveFooterCallBack.isNotYetInEdgeCannotMoveFooter(this, view,
                    mFooterView);
        return ScrollCompat.canChildScrollDown(view);
    }

    protected boolean isInsideAnotherDirectionView(final float x, final float y) {
        if (mInsideAnotherDirectionViewCallback != null)
            return mInsideAnotherDirectionViewCallback.isInside(x, y, mTargetView);
        return BoundaryUtil.isInsideHorizontalView(x, y, mTargetView);
    }

    protected void makeNewTouchDownEvent(MotionEvent ev) {
        if (sDebug) SRLog.d(TAG, "makeNewTouchDownEvent()");
        sendCancelEvent(ev);
        sendDownEvent(ev);
        mIndicatorSetter.onFingerUp();
        mIndicatorSetter.onFingerDown(ev.getX(), ev.getY());
    }

    protected void sendCancelEvent(MotionEvent event) {
        if (event == null && mLastMoveEvent == null) return;
        if (sDebug) SRLog.d(TAG, "sendCancelEvent()");
        final MotionEvent last;
        if (event == null) last = mLastMoveEvent;
        else last = event;
        final MotionEvent ev = MotionEvent.obtain(last.getDownTime(), last.getEventTime(),
                MotionEvent.ACTION_CANCEL, last.getX(), last.getY(), last.getMetaState());
        dispatchTouchEventSuper(ev);
        ev.recycle();
    }

    protected void sendDownEvent(MotionEvent event) {
        if (event == null && mLastMoveEvent == null) return;
        if (sDebug) SRLog.d(TAG, "sendDownEvent()");
        final MotionEvent last;
        if (event == null) last = mLastMoveEvent;
        else last = event;
        final MotionEvent ev = MotionEvent.obtain(last.getDownTime(), last.getEventTime(),
                MotionEvent.ACTION_DOWN, last.getX(), last.getY(), last.getMetaState());
        dispatchTouchEventSuper(ev);
        ev.recycle();
    }

    protected void notifyFingerUp() {
        if (mHeaderView != null && isHeaderInProcessing() && !isDisabledRefresh()) {
            mHeaderView.onFingerUp(this, mIndicator);
        } else if (mFooterView != null && isFooterInProcessing() && !isDisabledLoadMore()) {
            mFooterView.onFingerUp(this, mIndicator);
        }
    }

    protected void onFingerUp() {
        if (sDebug) SRLog.d(TAG, "onFingerUp()");
        notifyFingerUp();
        if (mMode == Constants.MODE_DEFAULT) {
            if (isEnabledNoMoreData())
                return;
            if (!mScrollChecker.isPreFling() && isEnabledKeepRefreshView() && mStatus != SR_STATUS_COMPLETE) {
                if (isHeaderInProcessing() && !isDisabledPerformRefresh() && isMovingHeader()
                        && mIndicator.isOverOffsetToKeepHeaderWhileLoading()) {
                    if (!mIndicator.isAlreadyHere(mIndicator.getOffsetToKeepHeaderWhileLoading())) {
                        mScrollChecker.scrollTo(mIndicator.getOffsetToKeepHeaderWhileLoading(),
                                mDurationOfBackToHeaderHeight);
                        return;
                    }
                } else if (isFooterInProcessing() && !isDisabledPerformLoadMore() && isMovingFooter()
                        && mIndicator.isOverOffsetToKeepFooterWhileLoading()) {
                    if (!mIndicator.isAlreadyHere(mIndicator.getOffsetToKeepFooterWhileLoading())) {
                        mScrollChecker.scrollTo(mIndicator.getOffsetToKeepFooterWhileLoading(),
                                mDurationOfBackToFooterHeight);
                        return;
                    }
                }
            }
        }
        if (!mScrollChecker.isPreFling()) {
            onRelease();
        }
    }

    protected void onRelease() {
        if (sDebug) SRLog.d(TAG, "onRelease()");
        if (mMode == Constants.MODE_DEFAULT) {
            if ((isEnabledNoMoreData() && isMovingFooter() && isEnabledNoSpringBackWhenNoMoreData()))
                return;
            tryToPerformRefresh();
            if (mStatus == SR_STATUS_COMPLETE) {
                notifyUIRefreshComplete(true, false);
                return;
            } else if (isEnabledKeepRefreshView()) {
                if (isHeaderInProcessing() && mHeaderView != null) {
                    if (isRefreshing() && isMovingHeader() && mIndicator.isAlreadyHere(mIndicator
                            .getOffsetToKeepHeaderWhileLoading())) {
                        return;
                    } else if (isMovingHeader() && mIndicator.isOverOffsetToKeepHeaderWhileLoading()) {
                        mScrollChecker.scrollTo(mIndicator.getOffsetToKeepHeaderWhileLoading(),
                                mDurationOfBackToHeaderHeight);
                        return;
                    } else if (isRefreshing() && !isMovingFooter()) {
                        return;
                    }
                } else if (isFooterInProcessing() && mFooterView != null) {
                    if (isLoadingMore() && isMovingFooter() && mIndicator.isAlreadyHere(mIndicator
                            .getOffsetToKeepFooterWhileLoading())) {
                        return;
                    } else if (isMovingFooter() && mIndicator.isOverOffsetToKeepFooterWhileLoading()) {
                        mScrollChecker.scrollTo(mIndicator.getOffsetToKeepFooterWhileLoading(),
                                mDurationOfBackToFooterHeight);
                        return;
                    } else if (isLoadingMore() && !isMovingHeader()) {
                        return;
                    }
                }
            }
        }
        tryScrollBackToTopByPercentDuration();
    }

    protected void tryScrollBackToTopByPercentDuration() {
        //Use the current percentage duration of the current position to scroll back to the top
        float percent;
        if (isMovingHeader()) {
            percent = mIndicator.getCurrentPercentOfRefreshOffset();
            percent = percent > 1 || percent <= 0 ? 1 : percent;
            tryScrollBackToTop(Math.round(mDurationToCloseHeader * percent));
        } else if (isMovingFooter()) {
            percent = mIndicator.getCurrentPercentOfLoadMoreOffset();
            percent = percent > 1 || percent <= 0 ? 1 : percent;
            tryScrollBackToTop(Math.round(mDurationToCloseFooter * percent));
        } else {
            tryToNotifyReset();
        }
    }

    protected void tryScrollBackToTop(int duration) {
        if (sDebug) SRLog.d(TAG, "tryScrollBackToTop(): duration: %s", duration);
        if (mIndicator.hasLeftStartPosition() && (!mIndicator.hasTouched() || !mIndicator.hasMoved())) {
            mScrollChecker.scrollTo(IIndicator.START_POS, duration);
            return;
        }
        if (isNeedFilterTouchEvent() && mIndicator.hasLeftStartPosition()) {
            mScrollChecker.scrollTo(IIndicator.START_POS, duration);
            return;
        }
        if (isMovingFooter() && mStatus == SR_STATUS_COMPLETE
                && mIndicator.hasJustBackToStartPosition()) {
            mScrollChecker.scrollTo(IIndicator.START_POS, duration);
            return;
        }
        tryToNotifyReset();
    }

    protected void notifyUIRefreshComplete(boolean useScroll, boolean notifyViews) {
        mIsSpringBackCanNotBeInterrupted = isEnabledSmoothRollbackWhenCompleted();
        if (notifyViews) {
            if (isHeaderInProcessing() && mHeaderView != null) {
                mHeaderView.onRefreshComplete(this, mIsLastRefreshSuccessful);
            } else if (isFooterInProcessing() && mFooterView != null) {
                mFooterView.onRefreshComplete(this, mIsLastRefreshSuccessful);
            }
        }
        if (useScroll) {
            tryScrollBackToTopByPercentDuration();
        }
    }

    protected void moveHeaderPos(float delta) {
        if (sDebug) SRLog.d(TAG, "moveHeaderPos(): delta: %s", delta);
        mIndicatorSetter.setMovingStatus(Constants.MOVING_HEADER);
        final float maxHeaderDistance = mIndicator.getCanMoveTheMaxDistanceOfHeader();
        final int current = mIndicator.getCurrentPos();
        final boolean isFling = mScrollChecker.isFling() || mScrollChecker.isPreFling();
        if (maxHeaderDistance > 0 && delta > 0) {
            if (current >= maxHeaderDistance) {
                if ((mIndicator.hasTouched() && !mScrollChecker.$IsScrolling)
                        || isFling) {
                    updateAnotherDirectionPos();
                    return;
                }
            } else if (current + delta > maxHeaderDistance) {
                if ((mIndicator.hasTouched() && !mScrollChecker.$IsScrolling)
                        || isFling) {
                    delta = maxHeaderDistance - current;
                    if (isFling) mScrollChecker.$Scroller.forceFinished(true);
                }
            }
        }
        movePos(delta);
    }

    protected void moveFooterPos(float delta) {
        if (sDebug) SRLog.d(TAG, "moveFooterPos(): delta: %s", delta);
        mIndicatorSetter.setMovingStatus(Constants.MOVING_FOOTER);
        if (delta < 0) {
            final float maxFooterDistance = mIndicator.getCanMoveTheMaxDistanceOfFooter();
            final int current = mIndicator.getCurrentPos();
            final boolean isFling = mScrollChecker.isFling() || mScrollChecker.isPreFling();
            if (maxFooterDistance > 0) {
                if (current >= maxFooterDistance) {
                    if ((mIndicator.hasTouched() && !mScrollChecker.$IsScrolling)
                            || isFling) {
                        updateAnotherDirectionPos();
                        return;
                    }
                } else if (current - delta > maxFooterDistance) {
                    if ((mIndicator.hasTouched() && !mScrollChecker.$IsScrolling)
                            || isFling) {
                        delta = current - maxFooterDistance;
                        if (isFling) mScrollChecker.$Scroller.forceFinished(true);
                    }
                }
            }
        } else {
            //check if it is needed to compatible scroll
            if ((mFlag & FLAG_ENABLE_COMPAT_SYNC_SCROLL) > 0
                    && !isEnabledPinContentView() && mIsLastRefreshSuccessful
                    && (!mIndicator.hasTouched() || mNestedScrollInProgress
                    || isEnabledSmoothRollbackWhenCompleted())
                    && mStatus == SR_STATUS_COMPLETE) {
                if (sDebug)
                    SRLog.d(TAG, "moveFooterPos(): compatible scroll delta: %s", delta);
                mNeedFilterScrollEvent = true;
                if (mScrollTargetView != null)
                    compatLoadMoreScroll(mScrollTargetView, delta);
                if (mAutoFoundScrollTargetView != null) {
                    compatLoadMoreScroll(mAutoFoundScrollTargetView, delta);
                } else if (mTargetView != null)
                    compatLoadMoreScroll(mTargetView, delta);
            }
        }
        movePos(-delta);
    }

    protected void compatLoadMoreScroll(View view, float delta) {
        if (mLoadMoreScrollCallback != null) {
            mLoadMoreScrollCallback.onScroll(view, delta);
        } else {
            ScrollCompat.scrollCompat(this, view, delta);
        }
    }

    protected void movePos(float delta) {
        if (delta == 0f) {
            if (sDebug) SRLog.d(TAG, "movePos(): delta is zero");
            mIndicatorSetter.setCurrentPos(mIndicator.getCurrentPos());
            return;
        }
        if (delta > 0 && mMode == Constants.MODE_SCALE && calculateScale() >= 1.2f) {
            return;
        }
        int to = mIndicator.getCurrentPos() + Math.round(delta);
        // over top
        if ((mMode == Constants.MODE_DEFAULT || mScrollChecker.isPreFling()
                || (mMode == Constants.MODE_SCALE && mIndicator.hasTouched()))
                && to < IIndicator.START_POS) {
            to = IIndicator.START_POS;
            if (sDebug) SRLog.d(TAG, "movePos(): over top");
        }
        mIndicatorSetter.setCurrentPos(to);
        int change = to - mIndicator.getLastPos();
        if (getParent() != null && !mNestedScrollInProgress && mIndicator.hasTouched())
            getParent().requestDisallowInterceptTouchEvent(true);
        if (isMovingHeader())
            updatePos(change);
        else if (isMovingFooter())
            updatePos(-change);
    }

    /**
     * Update view's Y position
     *
     * @param change The changed value
     */
    protected void updatePos(int change) {
        final boolean isMovingHeader = isMovingHeader();
        final boolean isMovingFooter = isMovingFooter();
        // leave initiated position or just refresh complete
        if (mMode == Constants.MODE_DEFAULT && ((mIndicator.hasJustLeftStartPosition()
                || mViewStatus == SR_VIEW_STATUS_INIT) && mStatus == SR_STATUS_INIT)
                || (mStatus == SR_STATUS_COMPLETE && isEnabledNextPtrAtOnce()
                && ((isHeaderInProcessing() && isMovingHeader && change > 0)
                || (isFooterInProcessing() && isMovingFooter && change < 0)))) {
            final byte old = mStatus;
            mStatus = SR_STATUS_PREPARE;
            notifyStatusChanged(old, mStatus);
            if (isMovingHeader()) {
                mViewStatus = SR_VIEW_STATUS_HEADER_IN_PROCESSING;
                if (mHeaderView != null)
                    mHeaderView.onRefreshPrepare(this);
            } else if (isMovingFooter()) {
                mViewStatus = SR_VIEW_STATUS_FOOTER_IN_PROCESSING;
                if (mFooterView != null)
                    mFooterView.onRefreshPrepare(this);
            }
        }
        // back to initiated position
        if (!(isAutoRefresh() && mStatus != SR_STATUS_COMPLETE)
                && mIndicator.hasJustBackToStartPosition()) {
            tryToNotifyReset();
        }
        tryToPerformRefreshWhenMoved();
        if (sDebug)
            SRLog.d(TAG, "updatePos(): change: %s, current: %s last: %s",
                    change, mIndicator.getCurrentPos(), mIndicator.getLastPos());
        notifyUIPositionChanged();
        boolean needRequestLayout = change != 0 && offsetChild(change, isMovingHeader,
                isMovingFooter);
        if (needRequestLayout || mIndicator.isAlreadyHere(IIndicator.START_POS)) {
            requestLayout();
        } else {
            invalidate();
        }
    }

    protected boolean offsetChild(int change, boolean isMovingHeader, boolean isMovingFooter) {
        boolean needRequestLayout = false;
        if (mMode == Constants.MODE_DEFAULT) {
            if (mHeaderView != null && !isDisabledRefresh() && isMovingHeader
                    && mHeaderView.getView().getVisibility() == VISIBLE) {
                final int type = mHeaderView.getStyle();
                switch (type) {
                    case IRefreshView.STYLE_DEFAULT:
                        ViewCompat.offsetTopAndBottom(mHeaderView.getView(), change);
                        break;
                    case IRefreshView.STYLE_SCALE:
                        needRequestLayout = true;
                        break;
                    case IRefreshView.STYLE_PIN:
                        break;
                    case IRefreshView.STYLE_FOLLOW_PIN:
                        if (mIndicator.getCurrentPos() <= mIndicator.getHeaderHeight())
                            ViewCompat.offsetTopAndBottom(mHeaderView.getView(), change);
                        break;
                    case IRefreshView.STYLE_FOLLOW_SCALE:
                    case IRefreshView.STYLE_FOLLOW_CENTER:
                        if (mIndicator.getCurrentPos() > mIndicator.getHeaderHeight())
                            needRequestLayout = true;
                        else
                            ViewCompat.offsetTopAndBottom(mHeaderView.getView(), change);
                        break;
                }
                if (isHeaderInProcessing())
                    mHeaderView.onRefreshPositionChanged(this, mStatus, mIndicator);
                else
                    mHeaderView.onPureScrollPositionChanged(this, mStatus, mIndicator);
            } else if (mFooterView != null && !isDisabledLoadMore() && isMovingFooter
                    && mFooterView.getView().getVisibility() == VISIBLE) {
                final int type = mFooterView.getStyle();
                switch (type) {
                    case IRefreshView.STYLE_DEFAULT:
                        ViewCompat.offsetTopAndBottom(mFooterView.getView(), change);
                        break;
                    case IRefreshView.STYLE_SCALE:
                        needRequestLayout = true;
                        break;
                    case IRefreshView.STYLE_PIN:
                        break;
                    case IRefreshView.STYLE_FOLLOW_PIN:
                        if (mIndicator.getCurrentPos() <= mIndicator.getFooterHeight())
                            ViewCompat.offsetTopAndBottom(mFooterView.getView(), change);
                        break;
                    case IRefreshView.STYLE_FOLLOW_SCALE:
                    case IRefreshView.STYLE_FOLLOW_CENTER:
                        if (mIndicator.getCurrentPos() > mIndicator.getFooterHeight())
                            needRequestLayout = true;
                        else
                            ViewCompat.offsetTopAndBottom(mFooterView.getView(), change);
                        break;
                }
                if (isFooterInProcessing())
                    mFooterView.onRefreshPositionChanged(this, mStatus, mIndicator);
                else
                    mFooterView.onPureScrollPositionChanged(this, mStatus, mIndicator);
            }
            if (!isEnabledPinContentView()) {
                if (isMovingHeader && mStickyHeaderView != null)
                    ViewCompat.offsetTopAndBottom(mStickyHeaderView, change);
                if (isMovingFooter && mStickyFooterView != null)
                    ViewCompat.offsetTopAndBottom(mStickyFooterView, change);
                if (mScrollTargetView != null && isMovingFooter) {
                    mScrollTargetView.setTranslationY(-mIndicator.getCurrentPos());
                } else if (mAutoFoundScrollTargetView != null && isMovingFooter) {
                    final View targetView;
                    if (ScrollCompat.isViewPager(mAutoFoundScrollTargetView.getParent())) {
                        targetView = (View) mAutoFoundScrollTargetView.getParent();
                    } else {
                        targetView = mAutoFoundScrollTargetView;
                    }
                    targetView.setTranslationY(-mIndicator.getCurrentPos());
                } else {
                    if (mTargetView != null)
                        ViewCompat.offsetTopAndBottom(mTargetView, change);
                }
            }
        } else {
            if (mTargetView != null) {
                if (isMovingHeader) {
                    if (ScrollCompat.canScaleInternal(mTargetView)) {
                        View view = ((ViewGroup) mTargetView).getChildAt(0);
                        view.setPivotY(0);
                        view.setScaleY(calculateScale());
                    } else {
                        mTargetView.setPivotY(0);
                        mTargetView.setScaleY(calculateScale());
                    }
                } else if (isMovingFooter) {
                    final View targetView;
                    if (mScrollTargetView != null) {
                        targetView = mScrollTargetView;
                    } else if (mAutoFoundScrollTargetView != null) {
                        if (ScrollCompat.isViewPager(mAutoFoundScrollTargetView.getParent())) {
                            targetView = (View) mAutoFoundScrollTargetView.getParent();
                        } else {
                            targetView = mAutoFoundScrollTargetView;
                        }
                    } else {
                        targetView = mTargetView;
                    }
                    if (ScrollCompat.canScaleInternal(targetView)) {
                        View view = ((ViewGroup) targetView).getChildAt(0);
                        view.setPivotY(view.getHeight());
                        view.setScaleY(calculateScale());
                    } else {
                        targetView.setPivotY(getHeight());
                        targetView.setScaleY(calculateScale());
                    }
                }
            }
        }
        return needRequestLayout;
    }

    protected float calculateScale() {
        if (mIndicator.getCurrentPos() >= 0)
            return 1 + (float) Math.min(.2f, Math.pow(mIndicator.getCurrentPos(), .72f) / 1000f);
        else
            return 1 - (float) Math.min(.2f, Math.pow(-mIndicator.getCurrentPos(), .72f) / 1000f);
    }

    protected void tryToPerformRefreshWhenMoved() {
        // try to perform refresh
        if (mMode == Constants.MODE_DEFAULT && mStatus == SR_STATUS_PREPARE && !isAutoRefresh()) {
            // reach fresh height while moving from top to bottom or reach load more height while
            // moving from bottom to top
            if (isHeaderInProcessing() && isMovingHeader() && !isDisabledPerformRefresh()) {
                if (isEnabledPullToRefresh() && mIndicator.isOverOffsetToRefresh()) {
                    triggeredRefresh(true);
                } else if (!mIndicator.hasTouched()
                        && !(mScrollChecker.isPreFling() || mScrollChecker.isFling())
                        && mIndicator.isJustReturnedOffsetToRefresh()) {
                    triggeredRefresh(true);
                    mScrollChecker.stop();
                }
            } else if (isFooterInProcessing() && isMovingFooter() && !isDisabledPerformLoadMore()) {
                if (isEnabledPullToRefresh() && mIndicator.isOverOffsetToLoadMore()) {
                    triggeredLoadMore(true);
                } else if (!mIndicator.hasTouched()
                        && !(mScrollChecker.isPreFling() || mScrollChecker.isFling())
                        && mIndicator.isJustReturnedOffsetToLoadMore()) {
                    triggeredLoadMore(true);
                    mScrollChecker.stop();
                }
            }
        }
    }

    /**
     * We need to notify the X pos changed
     */
    protected void updateAnotherDirectionPos() {
        if (mMode == Constants.MODE_DEFAULT) {
            if (mHeaderView != null && !isDisabledRefresh() && isMovingHeader()
                    && mHeaderView.getView().getVisibility() == VISIBLE) {
                if (isHeaderInProcessing())
                    mHeaderView.onRefreshPositionChanged(this, mStatus, mIndicator);
                else
                    mHeaderView.onPureScrollPositionChanged(this, mStatus, mIndicator);
            } else if (mFooterView != null && !isDisabledLoadMore() && isMovingFooter()
                    && mFooterView.getView().getVisibility() == VISIBLE) {
                if (isFooterInProcessing())
                    mFooterView.onRefreshPositionChanged(this, mStatus, mIndicator);
                else
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

    private void tryToDispatchNestedFling() {
        if (mScrollChecker.isPreFling() && mIndicator.isAlreadyHere(IIndicator.START_POS)) {
            final int velocity = (int) (mScrollChecker.getCurrVelocity() + 0.5f);
            mIndicatorSetter.setMovingStatus(Constants.MOVING_CONTENT);
            if (isEnabledOverScroll() && !(isDisabledLoadMoreWhenContentNotFull()
                    && !isNotYetInEdgeCannotMoveHeader()
                    && !isNotYetInEdgeCannotMoveFooter()))
                mScrollChecker.startFling(velocity);
            else
                mScrollChecker.stop();
            dispatchNestedFling(velocity);
        }
    }

    protected boolean tryToNotifyReset() {
        if ((mStatus == SR_STATUS_COMPLETE || mStatus == SR_STATUS_PREPARE)
                && mIndicator.isAlreadyHere(IIndicator.START_POS)) {
            if (sDebug) SRLog.d(TAG, "tryToNotifyReset()");
            if (mHeaderView != null)
                mHeaderView.onReset(this);
            if (mFooterView != null)
                mFooterView.onReset(this);
            final byte old = mStatus;
            mStatus = SR_STATUS_INIT;
            notifyStatusChanged(old, mStatus);
            mViewStatus = SR_VIEW_STATUS_INIT;
            if (mScrollChecker.$Scroller.isFinished()) {
                mScrollChecker.stop();
                mScrollChecker.setInterpolator(mSpringInterpolator);
            }
            mAutomaticActionTriggered = true;
            tryToResetMovingStatus();
            if (mMode == Constants.MODE_SCALE && mTargetView != null) {
                resetViewScale(mTargetView);
                if (mScrollTargetView != null) {
                    resetViewScale(mScrollTargetView);
                    mScrollTargetView.setTranslationY(0);
                    mScrollTargetView.setTranslationX(0);
                } else if (mAutoFoundScrollTargetView != null) {
                    final View targetView;
                    if (ScrollCompat.isViewPager(mAutoFoundScrollTargetView.getParent())) {
                        targetView = (View) mAutoFoundScrollTargetView.getParent();
                    } else {
                        targetView = mAutoFoundScrollTargetView;
                    }
                    resetViewScale(targetView);
                    targetView.setTranslationY(0);
                    targetView.setTranslationX(0);
                }
            }
            if (!mIndicator.hasTouched())
                mIsSpringBackCanNotBeInterrupted = false;
            if (getParent() != null)
                getParent().requestDisallowInterceptTouchEvent(false);
            return true;
        }
        return false;
    }

    protected void resetViewScale(View targetView) {
        targetView.setPivotX(0);
        targetView.setPivotY(0);
        targetView.setScaleX(1);
        targetView.setScaleY(1);
        if (isVerticalOrientation() && ScrollCompat.canScaleInternal(targetView)) {
            View view = ((ViewGroup) targetView).getChildAt(0);
            view.setPivotX(0);
            view.setPivotY(0);
            view.setScaleX(1);
            view.setScaleY(1);
        }
    }

    protected void performRefreshComplete(boolean hook, boolean notifyViews) {
        if (isRefreshing() && hook && mHeaderRefreshCompleteHook != null
                && mHeaderRefreshCompleteHook.mCallBack != null) {
            mHeaderRefreshCompleteHook.mLayout = this;
            mHeaderRefreshCompleteHook.mNotifyViews = notifyViews;
            mHeaderRefreshCompleteHook.doHook();
            return;
        }
        if (isLoadingMore() && hook && mFooterRefreshCompleteHook != null
                && mFooterRefreshCompleteHook.mCallBack != null) {
            mFooterRefreshCompleteHook.mLayout = this;
            mFooterRefreshCompleteHook.mNotifyViews = notifyViews;
            mFooterRefreshCompleteHook.doHook();
            return;
        }
        final byte old = mStatus;
        mStatus = SR_STATUS_COMPLETE;
        notifyStatusChanged(old, mStatus);
        notifyUIRefreshComplete(!isEnabledNoMoreData(), notifyViews);
    }

    /**
     * try to perform refresh or loading , if performed return true
     */
    protected void tryToPerformRefresh() {
        // status not be prepare or over scrolling or moving content go to break;
        if (mStatus != SR_STATUS_PREPARE || isMovingContent()) {
            return;
        }
        if (sDebug) SRLog.d(TAG, "tryToPerformRefresh()");
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

    protected void tryToPerformScrollToBottomToLoadMore() {
        if (isEnabledAutoLoadMore() && !isDisabledPerformLoadMore() && mMode ==
                Constants.MODE_DEFAULT && (mStatus == SR_STATUS_INIT || mStatus == SR_STATUS_PREPARE)) {
            if (mScrollTargetView != null) {
                if (canAutoLoadMore(mScrollTargetView)) {
                    if (isDisabledLoadMoreWhenContentNotFull()
                            && !isNotYetInEdgeCannotMoveHeader(mScrollTargetView)
                            && !isNotYetInEdgeCannotMoveFooter(mScrollTargetView)) {
                        return;
                    }
                    triggeredLoadMore(true);
                }
            } else if (mAutoFoundScrollTargetView != null) {
                if (canAutoLoadMore(mAutoFoundScrollTargetView)) {
                    if (isDisabledLoadMoreWhenContentNotFull()
                            && !isNotYetInEdgeCannotMoveHeader(mAutoFoundScrollTargetView)
                            && !isNotYetInEdgeCannotMoveFooter(mAutoFoundScrollTargetView)) {
                        return;
                    }
                    triggeredLoadMore(true);
                }
            } else if (canAutoLoadMore(mTargetView)) {
                if (isDisabledLoadMoreWhenContentNotFull()
                        && !isNotYetInEdgeCannotMoveHeader(mTargetView)
                        && !isNotYetInEdgeCannotMoveFooter(mTargetView)) {
                    return;
                }
                triggeredLoadMore(true);
            }
        }
    }

    protected void tryToPerformScrollToTopToRefresh() {
        if (isEnabledAutoRefresh() && !isDisabledPerformRefresh() && mMode == Constants.MODE_DEFAULT
                && (mStatus == SR_STATUS_INIT || mStatus == SR_STATUS_PREPARE)
                && ScrollCompat.canAutoRefresh(mTargetView)) {
            if (mScrollTargetView != null) {
                if (canAutoRefresh(mScrollTargetView))
                    triggeredRefresh(true);
            } else if (mAutoFoundScrollTargetView != null) {
                if (canAutoRefresh(mAutoFoundScrollTargetView))
                    triggeredRefresh(true);
            } else if (canAutoRefresh(mTargetView)) {
                triggeredRefresh(true);
            }
        }
    }

    protected boolean canAutoLoadMore(View view) {
        if (mAutoLoadMoreCallBack != null)
            return mAutoLoadMoreCallBack.canAutoLoadMore(this, view);
        return ScrollCompat.canAutoLoadMore(view);
    }

    protected boolean canAutoRefresh(View view) {
        if (mAutoRefreshCallBack != null)
            return mAutoRefreshCallBack.canAutoRefresh(this, view);
        return ScrollCompat.canAutoRefresh(view);
    }

    protected void triggeredRefresh(boolean notify) {
        if (sDebug) SRLog.d(TAG, "triggeredRefresh()");
        final byte old = mStatus;
        mStatus = SR_STATUS_REFRESHING;
        notifyStatusChanged(old, mStatus);
        mViewStatus = SR_VIEW_STATUS_HEADER_IN_PROCESSING;
        mFlag &= ~(FLAG_AUTO_REFRESH | FLAG_ENABLE_NO_MORE_DATA);
        mIsSpringBackCanNotBeInterrupted = false;
        performRefresh(notify);
    }

    protected void triggeredLoadMore(boolean notify) {
        if (sDebug) SRLog.d(TAG, "triggeredLoadMore()");
        final byte old = mStatus;
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
        //loading start milliseconds since boot
        mLoadingStartTime = SystemClock.uptimeMillis();
        if (sDebug) SRLog.d(TAG, "onRefreshBegin systemTime: %s", mLoadingStartTime);
        if (isRefreshing()) {
            if (mHeaderView != null)
                mHeaderView.onRefreshBegin(this, mIndicator);
        } else if (isLoadingMore()) {
            if (mFooterView != null)
                mFooterView.onRefreshBegin(this, mIndicator);
        }
        if (notify && mRefreshListener != null) {
            if (isRefreshing())
                mRefreshListener.onRefreshing();
            else
                mRefreshListener.onLoadingMore();
        }
    }

    protected void dispatchNestedFling(int velocity) {
        if (sDebug) SRLog.d(TAG, "dispatchNestedFling() : %s", velocity);
        if (mScrollTargetView != null)
            ScrollCompat.flingCompat(mScrollTargetView, -velocity);
        else if (mAutoFoundScrollTargetView != null) {
            ScrollCompat.flingCompat(mAutoFoundScrollTargetView, -velocity);
        } else
            ScrollCompat.flingCompat(mTargetView, -velocity);
    }

    private void notifyUIPositionChanged() {
        if (mUIPositionChangedListeners != null && !mUIPositionChangedListeners.isEmpty()) {
            final List<OnUIPositionChangedListener> listeners = mUIPositionChangedListeners;
            for (OnUIPositionChangedListener listener : listeners) {
                listener.onChanged(mStatus, mIndicator);
            }
        }
        notifyNestedScrollChanged();
    }

    private void notifyNestedScrollChanged() {
        if (mNestedScrollChangedListeners != null && !mNestedScrollChangedListeners.isEmpty()) {
            final List<OnNestedScrollChangedListener> listeners = mNestedScrollChangedListeners;
            for (OnNestedScrollChangedListener listener : listeners) {
                listener.onNestedScrollChanged();
            }
        }
    }

    private void notifyStatusChanged(byte old, byte now) {
        if (mStatusChangedListeners != null && !mStatusChangedListeners.isEmpty()) {
            final List<OnStatusChangedListener> listeners = mStatusChangedListeners;
            for (OnStatusChangedListener listener : listeners) {
                listener.onStatusChanged(old, now);
            }
        }
    }

    private View foundViewInViewGroupById(ViewGroup group, int id) {
        final int size = group.getChildCount();
        for (int i = 0; i < size; i++) {
            View view = group.getChildAt(i);
            if (view.getId() == id)
                return view;
            else if (view instanceof ViewGroup) {
                final View found = foundViewInViewGroupById((ViewGroup) view, id);
                if (found != null)
                    return found;
            }
        }
        return null;
    }


    /**
     * Classes that wish to override {@link SmoothRefreshLayout#isNotYetInEdgeCannotMoveHeader()} method
     * behavior should implement this interface.
     */
    public interface OnHeaderEdgeDetectCallBack {
        /**
         * Callback that will be called when {@link SmoothRefreshLayout#isNotYetInEdgeCannotMoveHeader()} method
         * is called to allow the implementer to override its behavior.
         *
         * @param parent SmoothRefreshLayout that this callback is overriding.
         * @param child  The child view.
         * @param header The Header view.
         * @return Whether it is possible for the child view of parent layout to scroll up.
         */
        boolean isNotYetInEdgeCannotMoveHeader(SmoothRefreshLayout parent, @Nullable View child,
                                               @Nullable IRefreshView header);
    }

    /**
     * Classes that wish to override {@link SmoothRefreshLayout#isNotYetInEdgeCannotMoveFooter()} method
     * behavior should implement this interface.
     */
    public interface OnFooterEdgeDetectCallBack {
        /**
         * Callback that will be called when {@link SmoothRefreshLayout#isNotYetInEdgeCannotMoveFooter()} method
         * is called to allow the implementer to override its behavior.
         *
         * @param parent SmoothRefreshLayout that this callback is overriding.
         * @param child  The child view.
         * @param footer The Footer view.
         * @return Whether it is possible for the child view of parent layout to scroll down.
         */
        boolean isNotYetInEdgeCannotMoveFooter(SmoothRefreshLayout parent, @Nullable View child,
                                               @Nullable IRefreshView footer);
    }

    /**
     * Classes that wish to override {@link SmoothRefreshLayout#isInsideAnotherDirectionView(float, float)}}
     * method behavior should implement this interface.
     */
    public interface OnInsideAnotherDirectionViewCallback {
        /**
         * Callback that will be called when
         * {@link SmoothRefreshLayout#isInsideAnotherDirectionView(float, float)}} method
         * is called to allow the implementer to override its behavior.
         *
         * @param x    The finger pressed x of the screen.
         * @param y    The finger pressed y of the screen.
         * @param view The target view.
         * @return Whether the finger pressed point is inside horizontal view
         */
        boolean isInside(float x, float y, View view);
    }

    /**
     * Classes that wish to be notified when the swipe gesture correctly triggers a refresh
     * should implement this interface.
     */
    public interface OnRefreshListener {
        /**
         * Called when a refresh is triggered.
         */
        void onRefreshing();

        /**
         * Called when a load more is triggered.
         */
        void onLoadingMore();
    }

    /**
     * Classes that wish to be notified when the views position changes should implement this
     * interface
     */
    public interface OnUIPositionChangedListener {
        /**
         * UI position changed
         *
         * @param status    {@link #SR_STATUS_INIT}, {@link #SR_STATUS_PREPARE},
         *                  {@link #SR_STATUS_REFRESHING},{@link #SR_STATUS_LOADING_MORE},{@link #SR_STATUS_COMPLETE}.
         * @param indicator @see {@link IIndicator}
         */
        void onChanged(byte status, IIndicator indicator);
    }

    /**
     * Classes that wish to be called when load more completed spring back to start position
     */
    public interface OnLoadMoreScrollCallback {
        /**
         * Called when load more completed spring back to start position, each move triggers a
         * callback once
         *
         * @param content The content view
         * @param delta   The scroll distance in current axis
         */
        void onScroll(View content, float delta);
    }

    public interface OnHookUIRefreshCompleteCallBack {
        @MainThread
        void onHook(RefreshCompleteHook hook);
    }

    /**
     * Classes that wish to be called when {@link SmoothRefreshLayout#setEnableAutoLoadMore(boolean)}
     * has been set true and {@link SmoothRefreshLayout#isDisabledLoadMore()} not be true and
     * sure you need to customize the specified trigger rule
     */
    public interface OnPerformAutoLoadMoreCallBack {
        /**
         * Whether need trigger auto load more
         *
         * @param parent The frame
         * @param child  the child view
         * @return whether need trigger
         */
        boolean canAutoLoadMore(SmoothRefreshLayout parent, @Nullable View child);
    }

    /**
     * Classes that wish to be called when {@link SmoothRefreshLayout#setEnableAutoRefresh(boolean)}
     * has been set true and {@link SmoothRefreshLayout#isDisabledRefresh()} not be true and
     * sure you need to customize the specified trigger rule
     */
    public interface OnPerformAutoRefreshCallBack {
        /**
         * Whether need trigger auto refresh
         *
         * @param parent The frame
         * @param child  the child view
         * @return whether need trigger
         */
        boolean canAutoRefresh(SmoothRefreshLayout parent, @Nullable View child);
    }

    /**
     * Classes that wish to be notified when the scroll events triggered in this view and all internal views
     */
    public interface OnNestedScrollChangedListener {
        /**
         * Scroll events triggered
         */
        void onNestedScrollChanged();
    }

    /**
     * Classes that wish to be notified when the status changed
     */
    public interface OnStatusChangedListener {
        /**
         * Status changed
         *
         * @param old the old status, as follows
         *            {@link #SR_STATUS_INIT}, {@link #SR_STATUS_PREPARE},
         *            {@link #SR_STATUS_REFRESHING},{@link #SR_STATUS_LOADING_MORE},
         *            {@link #SR_STATUS_COMPLETE}}
         * @param now the current status, as follows
         *            {@link #SR_STATUS_INIT}, {@link #SR_STATUS_PREPARE},
         *            {@link #SR_STATUS_REFRESHING},{@link #SR_STATUS_LOADING_MORE},
         *            {@link #SR_STATUS_COMPLETE}}
         */
        void onStatusChanged(byte old, byte now);
    }

    public static class LayoutParams extends MarginLayoutParams {
        private static final int[] LAYOUT_ATTRS = new int[]{android.R.attr.layout_gravity};
        public int gravity = Gravity.TOP | Gravity.START;

        @SuppressWarnings("WeakerAccess")
        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            final TypedArray a = c.obtainStyledAttributes(attrs, LAYOUT_ATTRS);
            gravity = a.getInt(0, gravity);
            a.recycle();
        }

        @SuppressWarnings("unused")
        public LayoutParams(int width, int height, int gravity) {
            super(width, height);
            this.gravity = gravity;
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

        @SuppressWarnings("unused")
        public LayoutParams(LayoutParams source) {
            super(source);
            gravity = source.gravity;
        }
    }

    public static class RefreshCompleteHook {
        private SmoothRefreshLayout mLayout;
        private OnHookUIRefreshCompleteCallBack mCallBack;
        private boolean mNotifyViews;

        public void onHookComplete() {
            if (mLayout != null) {
                if (SmoothRefreshLayout.sDebug)
                    SRLog.d(mLayout.TAG, "RefreshCompleteHook: onHookComplete()");
                mLayout.performRefreshComplete(false, mNotifyViews);
            }
        }

        private void doHook() {
            if (mCallBack != null) {
                if (SmoothRefreshLayout.sDebug)
                    SRLog.d(mLayout.TAG, "RefreshCompleteHook: doHook()");
                mCallBack.onHook(this);
            }
        }
    }

    /**
     * Delayed completion of loading
     */
    private static class DelayToRefreshComplete implements Runnable {
        private WeakReference<SmoothRefreshLayout> mLayoutWeakRf;
        private boolean mNotifyViews;

        @Override
        public void run() {
            if (mLayoutWeakRf.get() != null) {
                if (SmoothRefreshLayout.sDebug)
                    SRLog.d(mLayoutWeakRf.get().TAG, "DelayToRefreshComplete: run()");
                mLayoutWeakRf.get().performRefreshComplete(true, mNotifyViews);
            }
        }
    }

    class ScrollChecker implements Runnable {
        private final float $Physical;
        private final int $MaxDistance;
        Scroller $Scroller;
        Interpolator $Interpolator;
        int $LastY;
        int $LastStart;
        int $LastTo;
        int $Duration;
        byte $Mode = Constants.SCROLLER_MODE_NONE;
        float $Velocity;
        boolean $IsScrolling = false;
        private int $CalcTimes = 0;
        private int $CalcTotalTimes = 0;
        private float $CalcFactor = 0;
        private float $CalcPart = 0;

        ScrollChecker() {
            DisplayMetrics dm = getResources().getDisplayMetrics();
            $MaxDistance = (int) (dm.heightPixels / 6f);
            $Interpolator = mSpringInterpolator;
            $Physical = SensorManager.GRAVITY_EARTH * 39.37f * dm.density * 160f * 0.84f;
            $Scroller = new Scroller(getContext(), $Interpolator);
        }

        @Override
        public void run() {
            if ($Mode == Constants.SCROLLER_MODE_NONE)
                return;
            boolean finished = $Mode == Constants.SCROLLER_MODE_FLING
                    ? $CalcTotalTimes <= $CalcTimes
                    : !$Scroller.computeScrollOffset() && $Scroller.getCurrY() == $LastY;
            int curY;
            if ($Mode != Constants.SCROLLER_MODE_FLING) {
                curY = $Scroller.getCurrY();
            } else {
                curY = (int) ($LastY + $CalcPart * Math.pow($CalcFactor, $CalcTimes));
                $CalcTimes++;
            }
            int deltaY = curY - $LastY;
            if (SmoothRefreshLayout.sDebug)
                SRLog.d(TAG, "ScrollChecker: run(): finished: %s, mode: %s, start: %s, to: %s, " +
                                "curPos: %s, curY:%s, last: %s, delta: %s", finished, $Mode,
                        $LastStart, $LastTo, mIndicator.getCurrentPos(), curY, $LastY, deltaY);
            if (!finished) {
                $LastY = curY;
                if (isMovingHeader()) {
                    moveHeaderPos(deltaY);
                } else if (isMovingFooter()) {
                    if (isPreFling())
                        moveFooterPos(deltaY);
                    else
                        moveFooterPos(-deltaY);
                }
                ViewCompat.postOnAnimation(SmoothRefreshLayout.this, this);
                tryToDispatchNestedFling();
            } else {
                switch ($Mode) {
                    case Constants.SCROLLER_MODE_SPRING:
                    case Constants.SCROLLER_MODE_FLING_BACK:
                    case Constants.SCROLLER_MODE_SPRING_BACK:
                        stop();
                        if (!isInStartPosition())
                            onRelease();
                        break;
                    case Constants.SCROLLER_MODE_FLING:
                        $Mode = Constants.SCROLLER_MODE_FLING_BACK;
                        onRelease();
                        break;
                    case Constants.SCROLLER_MODE_PRE_FLING:
                        if (!isInStartPosition()) {
                            $Mode = Constants.SCROLLER_MODE_FLING_BACK;
                        } else {
                            stop();
                        }
                        onRelease();
                        break;
                }
            }
        }

        boolean isOverScrolling() {
            return $Mode == Constants.SCROLLER_MODE_FLING
                    || $Mode == Constants.SCROLLER_MODE_FLING_BACK
                    || $Mode == Constants.SCROLLER_MODE_PRE_FLING;
        }

        boolean isPreFling() {
            return $Mode == Constants.SCROLLER_MODE_PRE_FLING;
        }

        boolean isFling() {
            return $Mode == Constants.SCROLLER_MODE_FLING;
        }

        float getCurrVelocity() {
            float v;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                final int originalSymbol = $Velocity > 0 ? 1 : -1;
                v = $Scroller.getCurrVelocity() * originalSymbol;
            } else {
                final float percent = $Scroller.getCurrY() / (float) $Scroller.getFinalY();
                v = $Velocity * (1 - $Interpolator.getInterpolation(percent));
            }
            return v;
        }

        void startPreFling(float v) {
            stop();
            $Mode = Constants.SCROLLER_MODE_PRE_FLING;
            setInterpolator(sFlingInterpolator);
            $Velocity = v;
            $Scroller.fling(0, 0, 0, (int) v, Integer.MIN_VALUE, Integer.MAX_VALUE,
                    Integer.MIN_VALUE, Integer.MAX_VALUE);
            if (SmoothRefreshLayout.sDebug)
                SRLog.d(TAG, "ScrollChecker: startPreFling(): v: %s", v);
            run();
        }

        void startFling(float v) {
            stop();
            $Mode = Constants.SCROLLER_MODE_FLING;
            setInterpolator(sFlingInterpolator);
            $Velocity = v;
            $Scroller.fling(0, 0, 0, (int) v, Integer.MIN_VALUE, Integer.MAX_VALUE,
                    Integer.MIN_VALUE, Integer.MAX_VALUE);
            if (SmoothRefreshLayout.sDebug)
                SRLog.d(TAG, "ScrollChecker: startFling(): v: %s", v);
        }

        void scrollTo(int to, int duration) {
            $LastStart = mIndicator.getCurrentPos();
            if (to == IIndicator.START_POS && $Mode == Constants.SCROLLER_MODE_FLING_BACK) {
                if ($LastStart > $MaxDistance) {
                    duration = Math.max((int) (1000f * Math.sqrt(2.4f * $MaxDistance / 2000f) *
                            mFlingBackFactor), mMinFlingBackDuration);
                } else {
                    duration = Math.max((int) (1000f * Math.sqrt(2.4f * $LastStart / 2000f) *
                            mFlingBackFactor), mMinFlingBackDuration);
                }
            } else {
                stop();
            }
            $LastTo = to;
            if (to > $LastStart) {
                setInterpolator(mSpringInterpolator);
                $Mode = Constants.SCROLLER_MODE_SPRING;
            } else if (to < $LastStart) {
                setInterpolator(mSpringBackInterpolator);
                if ($Mode != Constants.SCROLLER_MODE_FLING_BACK)
                    $Mode = Constants.SCROLLER_MODE_SPRING_BACK;
            } else {
                $Mode = Constants.SCROLLER_MODE_NONE;
                return;
            }
            if (SmoothRefreshLayout.sDebug)
                SRLog.d(TAG, "ScrollChecker: scrollTo(): to:%s, duration:%s", to, duration);
            int distance = $LastTo - $LastStart;
            $LastY = 0;
            $Duration = duration;
            $IsScrolling = true;
            $Scroller.startScroll(0, 0, 0, distance, duration);
            removeCallbacks(this);
            run();
        }

        void computeScrollOffset() {
            if ($Scroller.computeScrollOffset()) {
                if (SmoothRefreshLayout.sDebug)
                    SRLog.d(TAG, "ScrollChecker: computeScrollOffset()");
                if ($Mode == Constants.SCROLLER_MODE_FLING && !$IsScrolling) {
                    $LastY = $Scroller.getCurrY();
                    if ($Velocity > 0 && isInStartPosition() && !isNotYetInEdgeCannotMoveHeader()) {
                        final float velocity = Math.abs(getCurrVelocity());
                        stop();
                        mIndicatorSetter.setMovingStatus(Constants.MOVING_HEADER);
                        final int[] result = calculate(velocity);
                        startBounce(result[0], result[1]);
                        return;
                    } else if ($Velocity < 0 && isInStartPosition() &&
                            !isNotYetInEdgeCannotMoveFooter()) {
                        final float velocity = Math.abs(getCurrVelocity());
                        stop();
                        mIndicatorSetter.setMovingStatus(Constants.MOVING_FOOTER);
                        if (isEnabledNoMoreData() && velocity > 1200 && getFooterHeight() > 0) {
                            startBounce(getFooterHeight(), mMaxOverScrollDuration);
                        } else {
                            final int[] result = calculate(velocity);
                            startBounce(result[0], result[1]);
                        }
                        return;
                    }
                }
                invalidate();
            }
        }

        int[] calculate(float velocity) {
            float deceleration = (float) Math.log(Math.abs(velocity / 3f) /
                    (ViewConfiguration.getScrollFriction() * $Physical));
            float ratio = (float) ((Math.exp(-Math.log10(velocity) / 1.2d)) * 2f);
            int to = Math.max(Math.min((int) ((ViewConfiguration.getScrollFriction() *
                    $Physical * Math.exp(deceleration)) * ratio), $MaxDistance), mTouchSlop);
            int duration = Math.min(Math.max((int) (1000f * Math.pow(Math.exp
                            (deceleration), .1f) * ratio * mFlingBackFactor)
                    , mMinOverScrollDuration), mMaxOverScrollDuration);
            return new int[]{to, duration};
        }

        void startBounce(int to, int duration) {
            $CalcTotalTimes = (int) Math.floor((duration * 60f / 1000));
            $CalcFactor = (1 - 1f / $CalcTotalTimes) * 0.92f;
            float sumPer = (float) Math.pow((2 - $CalcFactor), $CalcTotalTimes);
            $CalcPart = to / sumPer;
            $LastTo = to;
            $Mode = Constants.SCROLLER_MODE_FLING;
            $IsScrolling = true;
            run();
        }

        void setInterpolator(Interpolator interpolator) {
            if ($Interpolator == interpolator)
                return;
            if (SmoothRefreshLayout.sDebug)
                SRLog.d(TAG, "ScrollChecker: updateInterpolator(): interpolator: %s",
                        interpolator.getClass().getSimpleName());
            $Interpolator = interpolator;
            if (!$Scroller.isFinished()) {
                switch ($Mode) {
                    case Constants.SCROLLER_MODE_SPRING:
                    case Constants.SCROLLER_MODE_FLING_BACK:
                    case Constants.SCROLLER_MODE_SPRING_BACK:
                        $LastStart = mIndicator.getCurrentPos();
                        int distance = $LastTo - $LastStart;
                        int passed = $Scroller.timePassed();
                        $Scroller = new Scroller(getContext(), interpolator);
                        $Scroller.startScroll(0, 0, 0, distance, $Duration - passed);
                        run();
                        break;
                    case Constants.SCROLLER_MODE_FLING: {
                        final float currentVelocity = getCurrVelocity();
                        $Scroller = new Scroller(getContext(), interpolator);
                        startFling(currentVelocity);
                        break;
                    }
                    case Constants.SCROLLER_MODE_PRE_FLING: {
                        final float currentVelocity = getCurrVelocity();
                        $Scroller = new Scroller(getContext(), interpolator);
                        startPreFling(currentVelocity);
                        break;
                    }
                    case Constants.SCROLLER_MODE_NONE:
                        $Scroller = new Scroller(getContext(), interpolator);
                        break;
                }
            } else {
                $Scroller = new Scroller(getContext(), interpolator);
            }
        }

        void stop() {
            if ($Mode != Constants.SCROLLER_MODE_NONE) {
                if (SmoothRefreshLayout.sDebug)
                    SRLog.d(TAG, "ScrollChecker: stop()");
                $Mode = Constants.SCROLLER_MODE_NONE;
                $CalcTimes = 0;
                $CalcTotalTimes = 0;
                mAutomaticActionUseSmoothScroll = false;
                $IsScrolling = false;
                $Scroller.forceFinished(true);
                $Duration = 0;
                $LastY = 0;
                $LastTo = -1;
                removeCallbacks(this);
            }
        }
    }
}