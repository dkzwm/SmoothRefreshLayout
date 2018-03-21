package me.dkzwm.widget.srl;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.SystemClock;
import android.support.annotation.ColorInt;
import android.support.annotation.FloatRange;
import android.support.annotation.IdRes;
import android.support.annotation.IntRange;
import android.support.annotation.LayoutRes;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Scroller;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.dkzwm.widget.srl.animator.DefaultChangeStateAnimatorCreator;
import me.dkzwm.widget.srl.animator.IChangeStateAnimatorCreator;
import me.dkzwm.widget.srl.annotation.Action;
import me.dkzwm.widget.srl.annotation.Mode;
import me.dkzwm.widget.srl.annotation.State;
import me.dkzwm.widget.srl.config.Constants;
import me.dkzwm.widget.srl.extra.IRefreshView;
import me.dkzwm.widget.srl.gesture.GestureDetector;
import me.dkzwm.widget.srl.gesture.OnGestureListener;
import me.dkzwm.widget.srl.indicator.DefaultIndicator;
import me.dkzwm.widget.srl.indicator.IIndicator;
import me.dkzwm.widget.srl.indicator.IIndicatorSetter;
import me.dkzwm.widget.srl.utils.BoundaryUtil;
import me.dkzwm.widget.srl.utils.SRLog;
import me.dkzwm.widget.srl.utils.SRReflectUtil;
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
public class SmoothRefreshLayout extends ViewGroup implements OnGestureListener, NestedScrollingChild,
        NestedScrollingParent, ViewTreeObserver.OnScrollChangedListener {
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

    protected static final String TAG = "SmoothRefreshLayout";
    protected static final Interpolator sQuinticInterpolator = new Interpolator() {
        @Override
        public float getInterpolation(float t) {
            t -= 1.0f;
            return t * t * t * t * t + 1.0f;
        }
    };
    protected static final Interpolator sLinearInterpolator = new LinearInterpolator();
    private static final byte FLAG_AUTO_REFRESH_AT_ONCE = 0x01;
    private static final byte FLAG_AUTO_REFRESH_BUT_LATER = 0x01 << 1;
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
    private static final int FLAG_ENABLE_HIDE_HEADER_VIEW = 0x01 << 19;
    private static final int FLAG_ENABLE_HIDE_FOOTER_VIEW = 0x01 << 20;
    private static final int FLAG_ENABLE_CHECK_FINGER_INSIDE = 0x01 << 21;
    private static final int FLAG_ENABLE_NO_MORE_DATA_NO_BACK = 0x01 << 22;
    private static final int FLAG_ENABLE_SMOOTH_ROLLBACK_WHEN_COMPLETED = 0x01 << 23;
    private static final int FLAG_DISABLE_LOAD_MORE_WHEN_CONTENT_NOT_FULL = 0x01 << 24;
    private static final byte MASK_AUTO_REFRESH = 0x03;
    private static final int MASK_DISABLE_PERFORM_LOAD_MORE = 0x07 << 10;
    private static final int MASK_DISABLE_PERFORM_REFRESH = 0x03 << 13;
    private static final int[] LAYOUT_ATTRS = new int[]{
            android.R.attr.enabled
    };
    protected static boolean sDebug = false;
    private static IRefreshViewCreator sCreator;
    protected final int[] mParentScrollConsumed = new int[2];
    protected final int[] mParentOffsetInWindow = new int[2];
    private final NestedScrollingParentHelper mNestedScrollingParentHelper;
    private final NestedScrollingChildHelper mNestedScrollingChildHelper;
    private final List<View> mCachedViews = new ArrayList<>(1);
    @Mode
    protected int mMode = Constants.MODE_DEFAULT;
    @State
    protected int mState = Constants.STATE_CONTENT;
    @State
    protected int mPreviousState = Constants.STATE_NONE;
    protected IRefreshView<IIndicator> mHeaderView;
    protected IRefreshView<IIndicator> mFooterView;
    protected IIndicator mIndicator;
    protected IIndicatorSetter mIndicatorSetter;
    protected OnRefreshListener mRefreshListener;
    protected OnStateChangedListener mStateChangedListener;
    protected byte mStatus = SR_STATUS_INIT;
    protected byte mViewStatus = SR_VIEW_STATUS_INIT;
    protected boolean mNeedNotifyRefreshComplete = true;
    protected boolean mDelayedRefreshComplete = false;
    protected boolean mAutomaticActionUseSmoothScroll = false;
    protected boolean mAutomaticActionInScrolling = false;
    protected boolean mAutomaticActionTriggered = true;
    protected boolean mDelayedNestedFling = false;
    protected boolean mIsSpringBackCanNotBeInterrupted = false;
    protected boolean mHasSendCancelEvent = false;
    protected boolean mHasSendDownEvent = false;
    protected boolean mDealAnotherDirectionMove = false;
    protected boolean mPreventForAnotherDirection = false;
    protected boolean mBeenSendTouchEvent = false;
    protected boolean mIsInterceptTouchEventInOnceTouch = false;
    protected boolean mIsLastOverScrollCanNotAbort = false;
    protected boolean mIsFingerInsideAnotherDirectionView = false;
    protected boolean mNestedScrollInProgress = false;
    protected boolean mNeedNotifyRefreshListener = true;
    protected long mLoadingMinTime = 500;
    protected long mLoadingStartTime = 0;
    protected int mDurationToCloseHeader = 500;
    protected int mDurationToCloseFooter = 500;
    protected View mTargetView;
    protected View mContentView;
    protected View mEmptyView;
    protected View mErrorView;
    protected View mCustomView;
    protected View mScrollTargetView;
    protected View mStickyHeaderView;
    protected LayoutInflater mInflater;
    protected int mContentResId = View.NO_ID;
    protected int mErrorLayoutResId = View.NO_ID;
    protected int mEmptyLayoutResId = View.NO_ID;
    protected int mCustomLayoutResId = View.NO_ID;
    protected int mStickyHeaderResId = View.NO_ID;
    protected ScrollChecker mScrollChecker;
    protected OverScrollChecker mOverScrollChecker;
    protected DelayedScrollChecker mDelayedScrollChecker;
    protected int mTouchSlop;
    protected int mTouchPointerId;
    protected int mHeaderBackgroundColor = -1;
    protected int mFooterBackgroundColor = -2;
    protected int mMinimumFlingVelocity;
    protected Paint mBackgroundPaint;
    protected MotionEvent mLastMoveEvent;
    protected OnHeaderEdgeDetectCallBack mInEdgeCanMoveHeaderCallBack;
    protected OnFooterEdgeDetectCallBack mInEdgeCanMoveFooterCallBack;
    protected OnInsideAnotherDirectionViewCallback mFingerInsideAnotherDirectionViewCallback;
    protected OnLoadMoreScrollCallback mLoadMoreScrollCallback;
    protected ValueAnimator mChangeStateAnimator;
    private int mFlag = FLAG_DISABLE_LOAD_MORE;
    private ILifecycleObserver mLifecycleObserver;
    private Interpolator mSpringInterpolator;
    private Interpolator mOverScrollInterpolator;
    private IChangeStateAnimatorCreator mAnimatorCreator;
    private OnPerformAutoLoadMoreCallBack mAutoLoadMoreCallBack;
    private List<OnUIPositionChangedListener> mUIPositionChangedListeners;
    private GestureDetector mGestureDetector;
    private DelayToRefreshComplete mDelayToRefreshComplete;
    private RefreshCompleteHook mHeaderRefreshCompleteHook;
    private RefreshCompleteHook mFooterRefreshCompleteHook;
    private ViewTreeObserver mTargetViewTreeObserver;
    private boolean mIsLastRefreshSuccessful = true;
    private boolean mViewsZAxisNeedReset = true;
    private boolean mNeedFilterScrollEvent = false;
    private boolean mCompatLoadMoreScroll = true;
    private int mMaxOverScrollDuration = 500;
    private int mMinOverScrollDuration = 150;
    private int mDurationOfBackToHeaderHeight = 200;
    private int mDurationOfBackToFooterHeight = 200;

    public SmoothRefreshLayout(Context context) {
        this(context, null);
    }

    public SmoothRefreshLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SmoothRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        createIndicator();
        if (mIndicator == null || mIndicatorSetter == null)
            throw new IllegalArgumentException("You must create a IIndicator, current indicator is null");
        mInflater = LayoutInflater.from(context);
        TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.SmoothRefreshLayout,
                0, 0);
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

            mErrorLayoutResId = arr.getResourceId(R.styleable.SmoothRefreshLayout_sr_errorLayout,
                    NO_ID);
            mEmptyLayoutResId = arr.getResourceId(R.styleable.SmoothRefreshLayout_sr_emptyLayout,
                    NO_ID);
            mCustomLayoutResId = arr.getResourceId(R.styleable.SmoothRefreshLayout_sr_customLayout,
                    NO_ID);
            mStickyHeaderResId = arr.getResourceId(R.styleable.SmoothRefreshLayout_sr_stickyHeader,
                    NO_ID);

            mHeaderBackgroundColor = arr.getColor(R.styleable
                    .SmoothRefreshLayout_sr_headerBackgroundColor, -1);
            mFooterBackgroundColor = arr.getColor(R.styleable
                    .SmoothRefreshLayout_sr_footerBackgroundColor, -1);
            if (mHeaderBackgroundColor != -1 || mFooterBackgroundColor != -1) {
                preparePaint();
            }
            @State
            int state = arr.getInt(R.styleable.SmoothRefreshLayout_sr_state, Constants
                    .STATE_CONTENT);
            mState = state;
            @Mode
            int mode = arr.getInt(R.styleable.SmoothRefreshLayout_sr_mode, Constants.MODE_DEFAULT);
            mMode = mode;
            arr.recycle();
            arr = context.obtainStyledAttributes(attrs, LAYOUT_ATTRS, 0, 0);
            setEnabled(arr.getBoolean(0, true));
            arr.recycle();
        } else {
            setEnablePullToRefresh(true);
            setEnableKeepRefreshView(true);
        }
        ViewConfiguration viewConfiguration = ViewConfiguration.get(getContext());
        mTouchSlop = viewConfiguration.getScaledTouchSlop();
        mMinimumFlingVelocity = viewConfiguration.getScaledMinimumFlingVelocity();
        mGestureDetector = new GestureDetector(context, this);
        mScrollChecker = new ScrollChecker();
        mOverScrollChecker = new OverScrollChecker();
        mSpringInterpolator = sQuinticInterpolator;
        mOverScrollInterpolator = new DecelerateInterpolator(1.18f);
        //Nested scrolling
        mNestedScrollingChildHelper = new NestedScrollingChildHelper(this);
        mNestedScrollingParentHelper = new NestedScrollingParentHelper(this);
        mAnimatorCreator = new DefaultChangeStateAnimatorCreator();
        setNestedScrollingEnabled(true);
    }

    public static void debug(boolean debug) {
        sDebug = debug;
    }

    public static boolean isDebug() {
        return sDebug;
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
        if (mLifecycleObserver != null)
            mLifecycleObserver.onDetached(this);
        super.onDetachedFromWindow();
        destroy();
    }

    @Override
    protected void onAttachedToWindow() {
        if (mLifecycleObserver != null)
            mLifecycleObserver.onAttached(this);
        super.onAttachedToWindow();
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
        if (isDisabledRefresh() || isEnabledHideHeaderView())
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
        if (isDisabledLoadMore() || isEnabledHideFooterView())
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
        final int parentRight = r - l - getPaddingRight();
        final int parentBottom = b - t - getPaddingBottom();
        int offsetHeaderY = 0;
        int offsetFooterY = 0;
        if (isMovingHeader())
            offsetHeaderY = mIndicator.getCurrentPos();
        else if (isMovingFooter())
            offsetFooterY = mIndicator.getCurrentPos();
        int contentBottom = 0;
        boolean pin = (mScrollTargetView != null && !isMovingHeader()) || isEnabledPinContentView();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() == GONE) continue;
            if (mHeaderView != null && child == mHeaderView.getView()) {
                layoutHeaderView(child, offsetHeaderY);
            } else if (mTargetView != null && child == mTargetView
                    || (mPreviousState != Constants.STATE_NONE && mChangeStateAnimator != null
                    && mChangeStateAnimator.isRunning() && getView(mPreviousState) == child)
                    || (mStickyHeaderView != null && child == mStickyHeaderView)) {
                int bottom = layoutContentView(child, pin, offsetHeaderY, offsetFooterY);
                if (bottom != 0) contentBottom = bottom;
            } else if (mFooterView == null || mFooterView.getView() != child) {
                layoutOtherView(child, parentRight, parentBottom);
            }
        }
        if (mFooterView != null && mFooterView.getView().getVisibility() != GONE) {
            layoutFooterView(mFooterView.getView(), offsetFooterY, pin, contentBottom);
        }
        tryToPerformAutoRefresh();
    }

    protected int layoutContentView(View child, boolean pin, int offsetHeader, int offsetFooter) {
        int contentBottom = 0;
        final LayoutParams lp = (LayoutParams) child.getLayoutParams();
        final int left = getPaddingLeft() + lp.leftMargin;
        final int right = left + child.getMeasuredWidth();
        int top, bottom;
        if (mMode == Constants.MODE_DEFAULT && isMovingHeader()) {
            top = getPaddingTop() + lp.topMargin + (pin ? 0 : offsetHeader);
            bottom = top + child.getMeasuredHeight();
            child.layout(left, top, right, bottom);
        } else if (mMode == Constants.MODE_DEFAULT && isMovingFooter()
                && mStickyHeaderView != child) {
            top = getPaddingTop() + lp.topMargin - (pin ? 0 : offsetFooter);
            bottom = top + child.getMeasuredHeight();
            child.layout(left, top, right, bottom);
        } else {
            top = getPaddingTop() + lp.topMargin;
            bottom = top + child.getMeasuredHeight();
            child.layout(left, top, right, bottom);
        }
        if (sDebug) {
            SRLog.d(TAG, "onLayout(): content: %s %s %s %s", left, top, right, bottom);
        }
        if (mTargetView == child) contentBottom = bottom + lp.bottomMargin;
        return contentBottom;
    }

    protected void layoutHeaderView(View child, int offsetHeader) {
        if (mMode != Constants.MODE_DEFAULT || isDisabledRefresh() || isEnabledHideHeaderView() || child
                .getMeasuredHeight() == 0) {
            child.layout(0, 0, 0, 0);
            if (sDebug) {
                SRLog.d(TAG, "onLayout(): header: %s %s %s %s", 0, 0, 0, 0);
            }
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
        bottom = top + child.getMeasuredHeight();
        child.layout(left, top, right, bottom);
        if (sDebug) {
            SRLog.d(TAG, "onLayout(): header: %s %s %s %s", left, top, right, bottom);
        }
    }

    protected void layoutFooterView(View child, int offsetFooter, boolean pin, int contentBottom) {
        if (mMode != Constants.MODE_DEFAULT || isDisabledLoadMore() || isEnabledHideFooterView() || child
                .getMeasuredHeight() == 0) {
            child.layout(0, 0, 0, 0);
            if (sDebug) {
                SRLog.d(TAG, "onLayout(): footer: %s %s %s %s", 0, 0, 0, 0);
            }
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
        bottom = top + child.getMeasuredHeight();
        child.layout(left, top, right, bottom);
        if (sDebug) {
            SRLog.d(TAG, "onLayout(): footer: %s %s %s %s", left, top, right, bottom);
        }
    }

    @SuppressLint({"RtlHardcpded", "RtlHardcoded"})
    protected void layoutOtherView(View child, int parentRight, int parentBottom) {
        final int width = child.getMeasuredWidth();
        final int height = child.getMeasuredHeight();
        int childLeft, childTop;
        final LayoutParams lp = (LayoutParams) child.getLayoutParams();
        int gravity = lp.mGravity;
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
        if (sDebug) {
            SRLog.d(TAG, "onLayout(): child: %s %s %s %s", childLeft, childTop, childLeft
                    + width, childTop + height);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ensureTargetView();
        if (mState != Constants.STATE_CONTENT) {
            ensureContentView();
            if (mContentView != null)
                mContentView.setVisibility(GONE);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (!isEnabled() || mTargetView == null
                || (isEnabledPinRefreshViewWhileLoading() && ((isRefreshing() && isMovingHeader())
                || (isLoadingMore() && isMovingFooter())))
                || mNestedScrollInProgress || (isDisabledLoadMore() && isDisabledRefresh())) {
            return super.dispatchTouchEvent(ev);
        }
        mGestureDetector.onTouchEvent(ev);
        return processDispatchTouchEvent(ev);
    }

    protected boolean dispatchTouchEventSuper(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (mMode == Constants.MODE_DEFAULT) {
            if (mBackgroundPaint != null && !isEnabledPinContentView() && !mIndicator.isInStartPosition()) {
                if (!isDisabledRefresh() && isMovingHeader() && mHeaderBackgroundColor != -1) {
                    mBackgroundPaint.setColor(mHeaderBackgroundColor);
                    drawHeaderBackground(canvas);
                } else if (!isDisabledLoadMore() && isMovingFooter() && mFooterBackgroundColor != -1) {
                    mBackgroundPaint.setColor(mFooterBackgroundColor);
                    drawFooterBackground(canvas);
                }
            }
        }
        super.dispatchDraw(canvas);
    }

    protected void drawHeaderBackground(Canvas canvas) {
        final int bottom = Math.min(getPaddingTop() + mIndicator.getCurrentPos(),
                getHeight() - getPaddingTop());
        canvas.drawRect(getPaddingLeft(), getPaddingTop(), getWidth() - getPaddingRight(),
                bottom, mBackgroundPaint);
    }

    protected void drawFooterBackground(Canvas canvas) {
        final int top = Math.max(getHeight() - getPaddingBottom() - mIndicator
                .getCurrentPos(), getPaddingTop());
        canvas.drawRect(getPaddingLeft(), top, getWidth() - getPaddingRight(),
                getHeight() - getPaddingBottom(), mBackgroundPaint);
    }

    @ViewCompat.ScrollAxis
    public int getSupportScrollAxis() {
        return ViewCompat.SCROLL_AXIS_VERTICAL;
    }

    public void setLifecycleObserver(ILifecycleObserver observer) {
        mLifecycleObserver = observer;
    }

    @Nullable
    public View getLoadMoreScrollTargetView() {
        return mScrollTargetView;
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
    public void setLoadMoreScrollTargetView(@NonNull View view) {
        mScrollTargetView = view;
    }

    /**
     * Whether to enable the synchronous scroll when load more completed.
     * <p>当加载更多完成时是否启用同步滚动。</p>
     *
     * @param enable enable
     */
    public void setEnableCompatLoadMoreScroll(boolean enable) {
        mCompatLoadMoreScroll = enable;
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
     * Set the listener to be notified when the state changed.
     * <p>设置状态改变回调</p>
     *
     * @param listener Listener
     */
    public void setOnStateChangedListener(OnStateChangedListener listener) {
        mStateChangedListener = listener;
    }

    /**
     * Add a listener to listen the views position change event.
     * <p>设置UI位置变化回调</p>
     *
     * @param listener Listener
     */
    public void addOnUIPositionChangedListener(OnUIPositionChangedListener listener) {
        if (mUIPositionChangedListeners == null)
            mUIPositionChangedListeners = new ArrayList<>();
        mUIPositionChangedListeners.add(listener);
    }

    /**
     * remove the listener.
     * <p>移除UI位置变化回调</p>
     *
     * @param listener Listener
     */
    public void removeOnUIPositionChangedListener(OnUIPositionChangedListener listener) {
        if (mUIPositionChangedListeners != null && !mUIPositionChangedListeners.isEmpty())
            mUIPositionChangedListeners.remove(listener);
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
     * Set a callback to make sure you need to customize the specified trigger the auto load more rule.
     * <p>设置自动加载更多的触发条件回调，可自定义具体的触发自动加载更多的条件</p>
     *
     * @param callBack Customize the specified triggered rule
     */
    public void setOnPerformAutoLoadMoreCallBack(OnPerformAutoLoadMoreCallBack callBack) {
        mAutoLoadMoreCallBack = callBack;
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
        mHeaderRefreshCompleteHook.mLayout = this;
        mHeaderRefreshCompleteHook.setHookCallBack(callback);
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
        mFooterRefreshCompleteHook.mLayout = this;
        mFooterRefreshCompleteHook.setHookCallBack(callback);
    }

    public boolean equalsOnHookHeaderRefreshCompleteCallback(OnHookUIRefreshCompleteCallBack callBack) {
        return mHeaderRefreshCompleteHook != null && mHeaderRefreshCompleteHook.mCallBack == callBack;
    }

    public boolean equalsOnHookFooterRefreshCompleteCallback(OnHookUIRefreshCompleteCallBack callBack) {
        return mFooterRefreshCompleteHook != null && mFooterRefreshCompleteHook.mCallBack == callBack;
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
        mFingerInsideAnotherDirectionViewCallback = callback;
    }

    /**
     * Set the change state animator creator.
     * <p>设置切换状态时使用的动画的构造器</p>
     *
     * @param creator The change state animator creator
     */
    public void setChangeStateAnimatorCreator(@NonNull IChangeStateAnimatorCreator creator) {
        mAnimatorCreator = creator;
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
        return mIndicator.isInStartPosition();
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
        if (sDebug) {
            SRLog.d(TAG, "refreshComplete(): isSuccessful: %s", isSuccessful);
        }
        mIsLastRefreshSuccessful = isSuccessful;
        if (!isRefreshing() && !isLoadingMore())
            return;
        mIsSpringBackCanNotBeInterrupted = isEnabledSmoothRollbackWhenCompleted();
        long delay = mLoadingMinTime - (SystemClock.uptimeMillis() - mLoadingStartTime);
        if (delayDurationToChangeState <= 0) {
            if (delay <= 0) {
                performRefreshComplete(true);
            } else {
                if (mDelayToRefreshComplete == null)
                    mDelayToRefreshComplete = new DelayToRefreshComplete(this);
                else
                    mDelayToRefreshComplete.mLayoutWeakRf = new WeakReference<>(this);
                postDelayed(mDelayToRefreshComplete, delay);
            }
        } else {
            if (isRefreshing() && mHeaderView != null) {
                mHeaderView.onRefreshComplete(this, isSuccessful);
                mNeedNotifyRefreshComplete = false;
            } else if (isLoadingMore() && mFooterView != null) {
                mFooterView.onRefreshComplete(this, isSuccessful);
                mNeedNotifyRefreshComplete = false;
            }
            mDelayedRefreshComplete = true;
            if (delayDurationToChangeState < delay)
                delayDurationToChangeState = delay;
            if (mDelayToRefreshComplete == null)
                mDelayToRefreshComplete = new DelayToRefreshComplete(this);
            else
                mDelayToRefreshComplete.mLayoutWeakRf = new WeakReference<>(this);
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
        if (mStatus != SR_STATUS_INIT && mMode != Constants.MODE_DEFAULT) {
            return;
        }
        if (sDebug) {
            SRLog.d(TAG, "autoRefresh(): action: %s, smoothScroll: %s", action, smoothScroll);
        }
        mStatus = SR_STATUS_PREPARE;
        if (mHeaderView != null)
            mHeaderView.onRefreshPrepare(this);
        mIndicatorSetter.setMovingStatus(Constants.MOVING_HEADER);
        mViewStatus = SR_VIEW_STATUS_HEADER_IN_PROCESSING;
        mAutomaticActionUseSmoothScroll = smoothScroll;
        switch (action) {
            case Constants.ACTION_NOTHING:
                mFlag |= FLAG_AUTO_REFRESH_AT_ONCE;
                mNeedNotifyRefreshListener = false;
                triggeredRefresh(false);
                break;
            case Constants.ACTION_NOTIFY:
                mFlag |= FLAG_AUTO_REFRESH_BUT_LATER;
                mNeedNotifyRefreshListener = true;
                break;
            case Constants.ACTION_AT_ONCE:
                mFlag |= FLAG_AUTO_REFRESH_AT_ONCE;
                mNeedNotifyRefreshListener = true;
                triggeredRefresh(true);
                break;
        }
        int offsetToRefresh = mIndicator.getOffsetToRefresh();
        if (offsetToRefresh <= 0) {
            mAutomaticActionInScrolling = false;
            mAutomaticActionTriggered = false;
        } else {
            mAutomaticActionTriggered = true;
            mScrollChecker.tryToScrollTo(offsetToRefresh, smoothScroll ? mDurationToCloseHeader : 0);
            mAutomaticActionInScrolling = smoothScroll;
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
        if (mStatus != SR_STATUS_INIT && mMode != Constants.MODE_DEFAULT) {
            return;
        }
        if (sDebug) {
            SRLog.d(TAG, "autoLoadMore(): action: %s, smoothScroll: %s", action, smoothScroll);
        }
        mStatus = SR_STATUS_PREPARE;
        if (mFooterView != null)
            mFooterView.onRefreshPrepare(this);
        mIndicatorSetter.setMovingStatus(Constants.MOVING_FOOTER);
        mViewStatus = SR_VIEW_STATUS_FOOTER_IN_PROCESSING;
        mAutomaticActionUseSmoothScroll = smoothScroll;
        switch (action) {
            case Constants.ACTION_NOTHING:
                mFlag |= FLAG_AUTO_REFRESH_AT_ONCE;
                mNeedNotifyRefreshListener = false;
                triggeredLoadMore(false);
                break;
            case Constants.ACTION_NOTIFY:
                mFlag |= FLAG_AUTO_REFRESH_BUT_LATER;
                mNeedNotifyRefreshListener = true;
                break;
            case Constants.ACTION_AT_ONCE:
                mFlag |= FLAG_AUTO_REFRESH_AT_ONCE;
                mNeedNotifyRefreshListener = true;
                triggeredLoadMore(true);
                break;
        }
        int offsetToLoadMore = mIndicator.getOffsetToLoadMore();
        if (offsetToLoadMore <= 0) {
            mAutomaticActionInScrolling = false;
            mAutomaticActionTriggered = false;
        } else {
            mAutomaticActionTriggered = true;
            mScrollChecker.tryToScrollTo(offsetToLoadMore, smoothScroll ? mDurationToCloseFooter : 0);
            mAutomaticActionInScrolling = smoothScroll;
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
     * <p>设置越界回弹效果的最大持续时长（默认:`500`）</p>
     *
     * @param duration Duration
     */
    public void setMaxOverScrollDuration(@IntRange(from = 0, to = Integer.MAX_VALUE) int duration) {
        mMaxOverScrollDuration = duration;
    }

    /**
     * Set the min duration for Cross-Boundary-Rebound(OverScroll).
     * <p>设置越界回弹效果的最小持续时长（默认:`150`）</p>
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
     * Get the duration of return to the keep refresh view position when Header moves.
     *
     * @return Duration
     */
    public int getDurationOfBackToKeepHeader() {
        return mDurationOfBackToHeaderHeight;
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
     * Get the duration of return to the keep refresh view position when Footer moves.
     *
     * @return mDuration
     */
    public int getDurationOfBackToKeepFooter() {
        return mDurationOfBackToFooterHeight;
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
     * <p>最大移动距离占Footer视图的高度比</p>
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
        return (mFlag & MASK_AUTO_REFRESH) > 0;
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
     * The flag has been set to hided Header view.
     * <p>是否已经开启不显示Header</p>
     *
     * @return hided
     */
    public boolean isEnabledHideHeaderView() {
        return (mFlag & FLAG_ENABLE_HIDE_HEADER_VIEW) > 0;
    }

    /**
     * If @param enable has been set to true.Will hide the Header.
     * <p>设置是否开启不显示Header</p>
     *
     * @param enable Enable hide the Header
     */
    public void setEnableHideHeaderView(boolean enable) {
        if (enable) {
            mFlag = mFlag | FLAG_ENABLE_HIDE_HEADER_VIEW;
        } else {
            mFlag = mFlag & ~FLAG_ENABLE_HIDE_HEADER_VIEW;
        }
        requestLayout();
    }

    /**
     * The flag has been set to hided Footer view.
     * <p>是否已经开启不显示Footer</p>
     *
     * @return hided
     */
    public boolean isEnabledHideFooterView() {
        return (mFlag & FLAG_ENABLE_HIDE_FOOTER_VIEW) > 0;
    }

    /**
     * If @param enable has been set to true.Will hide the Footer.
     * <p>设置是否开启不显示Footer</p>
     *
     * @param enable Enable hide the Footer
     */
    public void setEnableHideFooterView(boolean enable) {
        if (enable) {
            mFlag = mFlag | FLAG_ENABLE_HIDE_FOOTER_VIEW;
        } else {
            mFlag = mFlag & ~FLAG_ENABLE_HIDE_FOOTER_VIEW;
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
     * <p>设置内容视图，`state`内容视图状态，`content`状态对应的视图</p>
     *
     * @param state   The state of content view
     * @param content Content view
     */
    public void setContentView(@State int state, @NonNull View content) {
        switch (state) {
            case Constants.STATE_NONE:
                throw new IllegalArgumentException("STATE_NONE can not be used, It only can be " +
                        "used as an initial value");
            case Constants.STATE_CONTENT:
                if (mContentView != null) {
                    removeView(mContentView);
                }
                mContentResId = View.NO_ID;
                mContentView = content;
                break;
            case Constants.STATE_EMPTY:
                if (mEmptyView != null) {
                    removeView(mEmptyView);
                }
                mEmptyLayoutResId = View.NO_ID;
                mEmptyView = content;
                break;
            case Constants.STATE_ERROR:
                if (mErrorView != null) {
                    removeView(mErrorView);
                }
                mErrorLayoutResId = View.NO_ID;
                mErrorView = content;
                break;
            case Constants.STATE_CUSTOM:
            default:
                if (mCustomView != null) {
                    removeView(mCustomView);
                }
                mCustomLayoutResId = View.NO_ID;
                mCustomView = content;
                break;
        }
        addStateViewLayoutParams(content);
        if (mState != state) {
            content.setVisibility(GONE);
        }
        mViewsZAxisNeedReset = true;
        addView(content);
    }

    /**
     * Update scroller interpolator.
     * <p>设置Scroller的插值器</p>
     *
     * @param interpolator Scroller interpolator
     */
    public void updateScrollerInterpolator(Interpolator interpolator) {
        mScrollChecker.updateInterpolator(interpolator);
    }

    /**
     * Reset scroller interpolator.
     * <p>重置Scroller的插值器</p>
     */
    public void resetScrollerInterpolator() {
        mScrollChecker.updateInterpolator(mSpringInterpolator);
    }

    /**
     * Set the scroller default interpolator.
     * <p>设置Scroller的默认插值器</p>
     *
     * @param interpolator Scroller interpolator
     */
    public void setSpringInterpolator(Interpolator interpolator) {
        mSpringInterpolator = interpolator;
        mScrollChecker.updateInterpolator(interpolator);
    }

    /**
     * Set the scroller interpolator when in cross boundary rebound.
     * <p>设置触发越界回弹时候Scroller的插值器</p>
     *
     * @param interpolator Scroller interpolator
     */
    public void setOverScrollInterpolator(Interpolator interpolator) {
        mOverScrollInterpolator = interpolator;
    }

    /**
     * Is in over scrolling
     *
     * @return Is
     */
    public boolean isOverScrolling() {
        return mOverScrollChecker.mIsScrolling;
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p != null && p instanceof LayoutParams;
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
     * Returns the {@link View} associated with the {@link State}.
     * <p>得到状态对应的内容视图</p>
     *
     * @param state The view
     */
    public View getView(@State int state) {
        switch (state) {
            case Constants.STATE_NONE:
                throw new IllegalArgumentException("STATE_NONE can not be used, It only can be " +
                        "used as an initial value");
            case Constants.STATE_CONTENT:
                ensureContentView();
                return mContentView;
            case Constants.STATE_ERROR:
                ensureErrorView();
                return mErrorView;
            case Constants.STATE_EMPTY:
                ensureEmptyView();
                return mEmptyView;
            case Constants.STATE_CUSTOM:
            default:
                ensureCustomView();
                return mCustomView;
        }
    }

    /**
     * Set the pinned header view resource id
     *
     * @param resId Resource id
     */
    public void setStickyHeaderResId(@IdRes int resId) {
        if (mStickyHeaderResId != resId) {
            mStickyHeaderResId = resId;
            requestLayout();
        }
    }

    /**
     * Set layout resource id when the state is Empty state
     *
     * @param resId Resource id
     */
    public void setEmptyLayoutResId(@LayoutRes int resId) {
        if (mEmptyLayoutResId != resId) {
            if (mEmptyLayoutResId != NO_ID && mEmptyView != null) {
                removeViewInLayout(mEmptyView);
                mEmptyView = null;
            }
            mEmptyLayoutResId = resId;
            if (mState == Constants.STATE_EMPTY)
                ensureEmptyView();
        }
    }

    /**
     * Set layout resource id when the state is Error state
     *
     * @param resId Resource id
     */
    public void setErrorLayoutResId(@LayoutRes int resId) {
        if (mErrorLayoutResId != resId) {
            if (mErrorLayoutResId != NO_ID && mErrorView != null) {
                removeViewInLayout(mErrorView);
                mErrorView = null;
            }
            mErrorLayoutResId = resId;
            if (mState == Constants.STATE_ERROR)
                ensureErrorView();
        }
    }

    /**
     * Set layout resource id when the state is Custom state
     *
     * @param resId Resource id
     */
    public void setCustomLayoutResId(@LayoutRes int resId) {
        if (mCustomLayoutResId != resId) {
            if (mCustomLayoutResId != NO_ID && mCustomView != null) {
                removeViewInLayout(mCustomView);
                mErrorView = null;
            }
            mCustomLayoutResId = resId;
            if (mState == Constants.STATE_CUSTOM)
                ensureCustomView();
        }
    }

    public void setMode(@Mode int mode) {
        mMode = mode;
        requestLayout();
    }

    /**
     * Returns the current state.
     * <p>获取当前的状态</p>
     *
     * @return Current state
     */
    @State
    public int getState() {
        return mState;
    }

    /**
     * Set the current state.
     * <p>设置当前的状态</p>
     *
     * @param state Current state
     */
    public void setState(@State int state) {
        setState(state, false);
    }

    /**
     * Set the current state.
     * <p>设置当前的状态，`state`状态，`animate`动画过渡</p>
     *
     * @param state   Current state
     * @param animate Use animation
     */
    public void setState(@State final int state, final boolean animate) {
        if (state != mState) {
            if (mChangeStateAnimator != null && mChangeStateAnimator.isRunning()) {
                mChangeStateAnimator.cancel();
                mChangeStateAnimator = null;
            }
            final View previousView = getView(mState);
            final View currentView = getView(state);
            if (animate && previousView != null && currentView != null) {
                mChangeStateAnimator = mAnimatorCreator.create(previousView, currentView);
                mChangeStateAnimator.start();
            } else {
                if (previousView != null)
                    previousView.setVisibility(GONE);
                if (currentView != null)
                    currentView.setVisibility(VISIBLE);
            }
            mPreviousState = mState;
            mState = state;
            mTargetView = currentView;
            if (mStateChangedListener != null)
                mStateChangedListener.onStateChanged(mPreviousState, mState);
        }
    }

    @Override
    public boolean onFling(float vx, final float vy) {
        final float realVelocity = getRealVelocity(vx, vy);
        if ((isDisabledLoadMore() && isDisabledRefresh())
                || (!isAutoRefresh() && (isNeedInterceptTouchEvent() ||
                isCanNotAbortOverScrolling()))
                || (!isNotYetInEdgeCannotMoveHeader() && realVelocity > 0)
                || (!isNotYetInEdgeCannotMoveFooter() && realVelocity < 0)) {
            return mNestedScrollInProgress && dispatchNestedPreFling(-vx, -vy);
        }
        if (!mIndicator.isInStartPosition()) {
            if (!isEnabledPinRefreshViewWhileLoading()) {
                if (Math.abs(realVelocity) > mMinimumFlingVelocity * 2) {
                    mDelayedNestedFling = true;
                    mOverScrollChecker.preFling(realVelocity);
                }
                return true;
            }
        } else {
            if (isEnabledOverScroll() && (!isEnabledPinRefreshViewWhileLoading()
                    || ((realVelocity >= 0 || !isDisabledLoadMore())
                    && (realVelocity <= 0 || !isDisabledRefresh())))) {
                mOverScrollChecker.fling(realVelocity);
            }
            mDelayedNestedFling = true;
            if (mDelayedScrollChecker == null)
                mDelayedScrollChecker = new DelayedScrollChecker();
            mDelayedScrollChecker.updateVelocity((int) realVelocity);
        }
        return mNestedScrollInProgress && dispatchNestedPreFling(-vx, -vy);
    }

    protected float getRealVelocity(float vx, float vy) {
        return vy;
    }

    @Override
    public boolean onStartNestedScroll(@NonNull View child, @NonNull View target, int nestedScrollAxes) {
        if (sDebug) {
            SRLog.d(TAG, "onStartNestedScroll(): nestedScrollAxes: %s", nestedScrollAxes);
        }
        return isEnabled() && isNestedScrollingEnabled() && mTargetView != null
                && (nestedScrollAxes & getSupportScrollAxis()) != 0;
    }

    @Override
    public void onNestedScrollAccepted(@NonNull View child, @NonNull View target, int axes) {
        if (sDebug) {
            SRLog.d(TAG, "onNestedScrollAccepted(): axes: %s", axes);
        }
        // Reset the counter of how much leftover scroll needs to be consumed.
        mNestedScrollingParentHelper.onNestedScrollAccepted(child, target, axes);
        mIndicatorSetter.onFingerDown();
        // Dispatch up to the nested parent
        startNestedScroll(axes & getSupportScrollAxis());
        mNestedScrollInProgress = true;
        if (!isNeedFilterTouchEvent()) {
            mScrollChecker.abortIfWorking();
            mOverScrollChecker.abortIfWorking();
        }
    }

    @Override
    public void onNestedPreScroll(@NonNull View target, int dx, int dy, @NonNull int[] consumed) {
        if (sDebug) {
            SRLog.d(TAG, "onNestedPreScroll(): dx: %s, dy: %s, consumed: %s",
                    dx, dy, Arrays.toString(consumed));
        }
        if (isNeedFilterTouchEvent()) {
            consumed[1] = dy;
        } else if (!mIndicator.hasTouched()) {
            if (sDebug) {
                SRLog.d(TAG, "onNestedPreScroll(): There was an exception in touch event " +
                        "handling，This method should be performed after the " +
                        "onNestedScrollAccepted() method is called");
            }
        } else {
            if (dy > 0 && !isDisabledRefresh() && !isNotYetInEdgeCannotMoveHeader()
                    && !(isEnabledPinRefreshViewWhileLoading() && isRefreshing()
                    && mIndicator.isOverOffsetToKeepHeaderWhileLoading())) {
                if (!mIndicator.isInStartPosition() && isMovingHeader()) {
                    mIndicatorSetter.onFingerMove(mIndicator.getLastMovePoint()[0] - dx,
                            mIndicator.getLastMovePoint()[1] - dy);
                    moveHeaderPos(mIndicator.getOffset());
                    consumed[1] = dy;
                } else {
                    mIndicatorSetter.onFingerMove(mIndicator.getLastMovePoint()[0] - dx,
                            mIndicator.getLastMovePoint()[1]);
                }
            }
            if (dy < 0 && !isDisabledLoadMore() && !isNotYetInEdgeCannotMoveFooter()
                    && !(isEnabledPinRefreshViewWhileLoading() && isLoadingMore()
                    && mIndicator.isOverOffsetToKeepFooterWhileLoading())) {
                if (!mIndicator.isInStartPosition() && isMovingFooter()) {
                    mIndicatorSetter.onFingerMove(mIndicator.getLastMovePoint()[0] - dx,
                            mIndicator.getLastMovePoint()[1] - dy);
                    moveFooterPos(mIndicator.getOffset());
                    consumed[1] = dy;
                } else {
                    mIndicatorSetter.onFingerMove(mIndicator.getLastMovePoint()[0] - dx,
                            mIndicator.getLastMovePoint()[1]);
                }
            }
            if (dy == 0) {
                mIndicatorSetter.onFingerMove(mIndicator.getLastMovePoint()[0] - dx,
                        mIndicator.getLastMovePoint()[1]);
                updateAnotherDirectionPos();
            } else if (isMovingFooter() && isFooterInProcessing() && mStatus == SR_STATUS_COMPLETE
                    && mIndicator.hasLeftStartPosition() && isNotYetInEdgeCannotMoveFooter()) {
                mScrollChecker.tryToScrollTo(IIndicator.START_POS, 0);
                consumed[1] = dy;
            }
            tryToResetMovingStatus();
        }
        // Now let our nested parent consume the leftovers
        final int[] parentConsumed = mParentScrollConsumed;
        if (dispatchNestedPreScroll(dx - consumed[0], dy - consumed[1], parentConsumed, null)) {
            consumed[0] += parentConsumed[0];
            consumed[1] += parentConsumed[1];
        }
    }

    @Override
    public int getNestedScrollAxes() {
        return mNestedScrollingParentHelper.getNestedScrollAxes();
    }

    @Override
    public void onStopNestedScroll(View target) {
        if (sDebug) {
            SRLog.d(TAG, "onStopNestedScroll()");
        }
        mNestedScrollingParentHelper.onStopNestedScroll(target);
        if (mNestedScrollInProgress) {
            mIndicatorSetter.onFingerUp();
        }
        mNestedScrollInProgress = false;
        mIsInterceptTouchEventInOnceTouch = isNeedInterceptTouchEvent();
        mIsLastOverScrollCanNotAbort = isCanNotAbortOverScrolling();
        // Dispatch up our nested parent
        stopNestedScroll();
        if (isAutoRefresh() && mScrollChecker.mIsRunning)
            return;
        if (mIndicator.hasLeftStartPosition()) {
            onFingerUp(false);
        } else {
            notifyFingerUp();
        }
    }

    @Override
    public void onNestedScroll(final View target, final int dxConsumed, final int dyConsumed,
                               final int dxUnconsumed, final int dyUnconsumed) {
        if (sDebug) {
            SRLog.d(TAG, "onNestedScroll(): dxConsumed: %s, dyConsumed: %s, dxUnconsumed: %s" +
                    " dyUnconsumed: %s", dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
        }
        if (isNeedFilterTouchEvent())
            return;
        // Dispatch up to the nested parent first
        dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, mParentOffsetInWindow);
        if (!mIndicator.hasTouched()) {
            if (sDebug) {
                SRLog.d(TAG, "onNestedScroll(): There was an exception in touch event handling，" +
                        "This method should be performed after the onNestedScrollAccepted() " +
                        "method is called");
            }
            return;
        }
        final int dy = dyUnconsumed + mParentOffsetInWindow[1];
        final boolean canNotChildScrollDown = !isNotYetInEdgeCannotMoveFooter();
        final boolean canNotChildScrollUp = !isNotYetInEdgeCannotMoveHeader();
        if (dy < 0 && !isDisabledRefresh() && canNotChildScrollUp
                && !(isEnabledPinRefreshViewWhileLoading() && isRefreshing()
                && mIndicator.isOverOffsetToKeepHeaderWhileLoading())) {
            float distance = mIndicator.getCanMoveTheMaxDistanceOfHeader();
            if (distance > 0 && mIndicator.getCurrentPos() >= distance)
                return;
            mIndicatorSetter.onFingerMove(mIndicator.getLastMovePoint()[0],
                    mIndicator.getLastMovePoint()[1] - dy);
            if (distance > 0 && (mIndicator.getCurrentPos() + mIndicator.getOffset() > distance))
                moveHeaderPos(distance - mIndicator.getCurrentPos());
            else
                moveHeaderPos(mIndicator.getOffset());
        } else if (dy > 0 && !isDisabledLoadMore() && canNotChildScrollDown
                && !(isDisabledLoadMoreWhenContentNotFull() && canNotChildScrollUp
                && mIndicator.isInStartPosition())
                && !(isEnabledPinRefreshViewWhileLoading() && isLoadingMore()
                && mIndicator.isOverOffsetToKeepFooterWhileLoading())) {
            float distance = mIndicator.getCanMoveTheMaxDistanceOfFooter();
            if (distance > 0 && mIndicator.getCurrentPos() > distance)
                return;
            mIndicatorSetter.onFingerMove(mIndicator.getLastMovePoint()[0],
                    mIndicator.getLastMovePoint()[1] - dy);
            if (distance > 0 && (mIndicator.getCurrentPos() - mIndicator.getOffset() > distance))
                moveFooterPos(mIndicator.getCurrentPos() - distance);
            else
                moveFooterPos(mIndicator.getOffset());
        }
        tryToResetMovingStatus();
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
    public void stopNestedScroll() {
        mNestedScrollingChildHelper.stopNestedScroll();
    }

    @Override
    public boolean hasNestedScrollingParent() {
        return mNestedScrollingChildHelper.hasNestedScrollingParent();
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed,
                                        int dyUnconsumed, int[] offsetInWindow) {
        return mNestedScrollingChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed,
                dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed,
                                           int[] offsetInWindow) {
        return mNestedScrollingChildHelper.dispatchNestedPreScroll(
                dx, dy, consumed, offsetInWindow);
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX,
                                    float velocityY) {
        return onFling(-velocityX, -velocityY);
    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY,
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
    public void onScrollChanged() {
        if (mNeedFilterScrollEvent) {
            mNeedFilterScrollEvent = false;
            return;
        }
        mDelayedNestedFling = false;
        if (mDelayedScrollChecker != null) {
            mDelayedScrollChecker.abortIfWorking();
        }
        checkAnotherDirectionViewUnInterceptedEvent();
        tryToPerformScrollToBottomToLoadMore();
        tryToPerformScrollToTopToRefresh();
        mOverScrollChecker.computeScrollOffset();
    }

    private void checkAnotherDirectionViewUnInterceptedEvent() {
        if (mIndicator.hasTouched() && mIndicator.hasMoved() && mPreventForAnotherDirection
                && isDisabledWhenAnotherDirectionMove() && mIsFingerInsideAnotherDirectionView) {
            if (isMovingHeader() && isNotYetInEdgeCannotMoveHeader()) {
                mPreventForAnotherDirection = false;
            } else if (isMovingFooter() && isNotYetInEdgeCannotMoveFooter()) {
                mPreventForAnotherDirection = false;
            }
        }
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
            if (sDebug) {
                SRLog.d(TAG, "checkViewsZAxisNeedReset()");
            }
        }
        mViewsZAxisNeedReset = false;
    }

    protected void destroy() {
        reset();
        mPreviousState = Constants.STATE_NONE;
        if (mHeaderRefreshCompleteHook != null)
            mHeaderRefreshCompleteHook.mLayout = null;
        mHeaderRefreshCompleteHook = null;
        if (mFooterRefreshCompleteHook != null)
            mFooterRefreshCompleteHook.mLayout = null;
        mFooterRefreshCompleteHook = null;
        if (mUIPositionChangedListeners != null)
            mUIPositionChangedListeners.clear();
        if (sDebug) {
            SRLog.d(TAG, "destroy()");
        }
    }

    protected void reset() {
        if (!mIndicator.isInStartPosition()) {
            mScrollChecker.tryToScrollTo(IIndicator.START_POS, 0);
        }
        if (mChangeStateAnimator != null && mChangeStateAnimator.isRunning())
            mChangeStateAnimator.cancel();
        mChangeStateAnimator = null;
        if (isRefreshing() || isLoadingMore()) {
            mStatus = SR_STATUS_COMPLETE;
            notifyUIRefreshComplete(false);
        }
        mScrollChecker.destroy();
        if (mDelayToRefreshComplete != null)
            removeCallbacks(mDelayToRefreshComplete);
        mDelayToRefreshComplete = null;
        mDelayedNestedFling = false;
        if (mDelayedScrollChecker != null)
            mDelayedScrollChecker.abortIfWorking();
        if (sDebug) {
            SRLog.d(TAG, "reset()");
        }
    }

    protected void tryToPerformAutoRefresh() {
        if (isAutoRefresh() && !mAutomaticActionTriggered) {
            if (sDebug) {
                SRLog.d(TAG, "tryToPerformAutoRefresh()");
            }
            if (isHeaderInProcessing()) {
                if (mHeaderView == null || mIndicator.getHeaderHeight() <= 0)
                    return;
                mAutomaticActionTriggered = true;
                mScrollChecker.tryToScrollTo(mIndicator.getOffsetToRefresh(),
                        mAutomaticActionUseSmoothScroll ? mDurationToCloseHeader : 0);
                mAutomaticActionInScrolling = mAutomaticActionUseSmoothScroll;
            } else if (isFooterInProcessing()) {
                if (mFooterView == null || mIndicator.getFooterHeight() <= 0)
                    return;
                mAutomaticActionTriggered = true;
                mScrollChecker.tryToScrollTo(mIndicator.getOffsetToLoadMore(),
                        mAutomaticActionUseSmoothScroll ? mDurationToCloseFooter : 0);
                mAutomaticActionInScrolling = mAutomaticActionUseSmoothScroll;
            }
        }
    }

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

    protected void ensureContentView() {
        if (mContentView == null) {
            if (mContentResId != View.NO_ID) {
                for (int i = getChildCount() - 1; i >= 0; i--) {
                    View child = getChildAt(i);
                    if (!(child instanceof IRefreshView) && mContentResId == child.getId()) {
                        mContentView = child;
                        break;
                    }
                }
            } else {
                for (int i = getChildCount() - 1; i >= 0; i--) {
                    View child = getChildAt(i);
                    if ((mEmptyView != null && child == mEmptyView)
                            || (mErrorView != null && child == mErrorView)
                            || (mCustomView != null && child == mCustomView))
                        continue;
                    if (child.getVisibility() != GONE && !(child instanceof IRefreshView)) {
                        mContentView = child;
                        break;
                    }
                }
            }
        }
        if (mStickyHeaderView == null && mStickyHeaderResId != NO_ID) {
            mStickyHeaderView = findViewById(mStickyHeaderResId);
        }
    }

    private void ensureErrorView() {
        if (mErrorView == null && mErrorLayoutResId != NO_ID) {
            mErrorView = mInflater.inflate(mErrorLayoutResId, null, false);
            addStateViewLayoutParams(mErrorView);
            addView(mErrorView);
        } else if (mErrorView == null)
            throw new IllegalArgumentException("Error view must be not null");
    }

    private void ensureEmptyView() {
        if (mEmptyView == null && mEmptyLayoutResId != NO_ID) {
            mEmptyView = mInflater.inflate(mEmptyLayoutResId, null, false);
            addStateViewLayoutParams(mEmptyView);
            addView(mEmptyView);
        } else if (mEmptyView == null)
            throw new IllegalArgumentException("Empty view must be not null");
    }

    private void ensureCustomView() {
        if (mCustomView == null && mCustomLayoutResId != NO_ID) {
            mCustomView = mInflater.inflate(mCustomLayoutResId, null, false);
            addStateViewLayoutParams(mCustomView);
            addView(mCustomView);
        } else if (mCustomView == null)
            throw new IllegalArgumentException("Custom view must be not null");
    }

    protected void addFreshViewLayoutParams(View view) {
        ViewGroup.LayoutParams lp = view.getLayoutParams();
        if (lp == null) {
            lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            view.setLayoutParams(lp);
        }
    }

    protected void addStateViewLayoutParams(View view) {
        ViewGroup.LayoutParams lp = view.getLayoutParams();
        if (lp == null) {
            lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            view.setLayoutParams(lp);
        }
    }

    private void ensureTargetView() {
        if (mTargetView == null) {
            switch (mState) {
                case Constants.STATE_NONE:
                    break;
                case Constants.STATE_CONTENT:
                    ensureContentView();
                    mTargetView = mContentView;
                    break;
                case Constants.STATE_EMPTY:
                    ensureEmptyView();
                    mTargetView = mEmptyView;
                    break;
                case Constants.STATE_ERROR:
                    ensureErrorView();
                    mTargetView = mErrorView;
                    break;
                case Constants.STATE_CUSTOM:
                    ensureCustomView();
                    mTargetView = mCustomView;
                    break;
            }
            if (mTargetView != null && isEnabledOverScroll())
                mTargetView.setOverScrollMode(OVER_SCROLL_NEVER);
        }
        if (mTargetView != null) {
            ViewTreeObserver observer;
            if (mScrollTargetView == null) {
                observer = mTargetView.getViewTreeObserver();
            } else {
                observer = mScrollTargetView.getViewTreeObserver();
                if (isEnabledOverScroll())
                    mScrollTargetView.setOverScrollMode(OVER_SCROLL_NEVER);
            }
            if (observer != mTargetViewTreeObserver && observer.isAlive()) {
                safelyRemoveListeners();
                mTargetViewTreeObserver = observer;
                mTargetViewTreeObserver.addOnScrollChangedListener(this);
            }
        }
        //Use the static default creator to create the Header view
        if (!isDisabledRefresh() && !isEnabledHideHeaderView() && mHeaderView == null &&
                sCreator != null && mMode == Constants.MODE_DEFAULT) {
            sCreator.createHeader(this);
        }
        //Use the static default creator to create the Footer view
        if (!isDisabledLoadMore() && !isEnabledHideFooterView() && mFooterView == null &&
                sCreator != null && mMode == Constants.MODE_DEFAULT) {
            sCreator.createFooter(this);
        }
    }

    protected boolean processDispatchTouchEvent(MotionEvent ev) {
        final int action = ev.getAction() & MotionEvent.ACTION_MASK;
        if (sDebug) {
            SRLog.d(TAG, "processDispatchTouchEvent(): action: %s", action);
        }
        switch (action) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mPreventForAnotherDirection = false;
                mDealAnotherDirectionMove = false;
                mIsFingerInsideAnotherDirectionView = false;
                mBeenSendTouchEvent = false;
                mIndicatorSetter.onFingerUp();
                if (isNeedFilterTouchEvent()) {
                    mIsInterceptTouchEventInOnceTouch = false;
                    if (mIsLastOverScrollCanNotAbort && mIndicator.isInStartPosition()) {
                        mOverScrollChecker.abortIfWorking();
                    }
                    mIsLastOverScrollCanNotAbort = false;
                    float offsetX, offsetY;
                    float[] pressDownPoint = mIndicator.getFingerDownPoint();
                    offsetX = ev.getX() - pressDownPoint[0];
                    offsetY = ev.getY() - pressDownPoint[1];
                    if (Math.abs(offsetX) > mTouchSlop || Math.abs(offsetY) > mTouchSlop) {
                        sendCancelEvent();
                        return true;
                    } else {
                        return super.dispatchTouchEvent(ev);
                    }
                } else {
                    mIsInterceptTouchEventInOnceTouch = false;
                    mIsLastOverScrollCanNotAbort = false;
                    if (mIndicator.hasLeftStartPosition()) {
                        onFingerUp(false);
                        if (mIndicator.hasMovedAfterPressedDown()) {
                            sendCancelEvent();
                            return true;
                        }
                        return super.dispatchTouchEvent(ev);
                    } else {
                        notifyFingerUp();
                        return super.dispatchTouchEvent(ev);
                    }
                }
            case MotionEvent.ACTION_POINTER_UP:
                final int actionIndex = ev.getActionIndex();
                if (ev.getPointerId(actionIndex) == mTouchPointerId) {
                    // Pick a new pointer to pick up the slack.
                    final int newIndex = actionIndex == 0 ? 1 : 0;
                    mTouchPointerId = ev.getPointerId(newIndex);
                    mIndicatorSetter.onFingerMove(ev.getX(newIndex), ev.getY(newIndex));
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                mTouchPointerId = ev.getPointerId(ev.getActionIndex());
                mIndicatorSetter.onFingerMove(ev.getX(ev.getActionIndex()), ev.getY(ev.getActionIndex()));
                break;
            case MotionEvent.ACTION_DOWN:
                mIndicatorSetter.onFingerUp();
                mHasSendDownEvent = false;
                mTouchPointerId = ev.getPointerId(0);
                mIndicatorSetter.onFingerDown(ev.getX(), ev.getY());
                mIsFingerInsideAnotherDirectionView = isDisabledWhenAnotherDirectionMove()
                        && (!isEnableCheckInsideAnotherDirectionView()
                        || isInsideAnotherDirectionView(ev.getRawX(), ev.getRawY()));
                mIsInterceptTouchEventInOnceTouch = isNeedInterceptTouchEvent();
                mIsLastOverScrollCanNotAbort = isCanNotAbortOverScrolling();
                if (!isNeedFilterTouchEvent()) {
                    mScrollChecker.abortIfWorking();
                    mOverScrollChecker.abortIfWorking();
                }
                mDelayedNestedFling = false;
                if (mDelayedScrollChecker != null)
                    mDelayedScrollChecker.abortIfWorking();
                mHasSendCancelEvent = false;
                mPreventForAnotherDirection = false;
                super.dispatchTouchEvent(ev);
                return true;
            case MotionEvent.ACTION_MOVE:
                if (!mIndicator.hasTouched()) {
                    return super.dispatchTouchEvent(ev);
                }
                final int index = ev.findPointerIndex(mTouchPointerId);
                if (index < 0) {
                    Log.e(TAG, "Error processing scroll; pointer index for id " +
                            mTouchPointerId + " not found. Did any MotionEvents get skipped?");
                    return super.dispatchTouchEvent(ev);
                }
                mLastMoveEvent = ev;
                if (tryToFilterTouchEventInDispatchTouchEvent(ev))
                    return true;
                tryToResetMovingStatus();
                mIndicatorSetter.onFingerMove(ev.getX(index), ev.getY(index));
                float offsetX, offsetY;
                final float[] pressDownPoint = mIndicator.getFingerDownPoint();
                offsetX = ev.getX(index) - pressDownPoint[0];
                offsetY = ev.getY(index) - pressDownPoint[1];
                final boolean canNotChildScrollDown = !isNotYetInEdgeCannotMoveFooter();
                final boolean canNotChildScrollUp = !isNotYetInEdgeCannotMoveHeader();
                if (isDisabledWhenAnotherDirectionMove() && mIsFingerInsideAnotherDirectionView) {
                    if (!mDealAnotherDirectionMove) {
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
                    }
                } else {
                    if (Math.abs(offsetX) < mTouchSlop
                            && Math.abs(offsetY) < mTouchSlop) {
                        return super.dispatchTouchEvent(ev);
                    }
                }
                if (mPreventForAnotherDirection) {
                    return super.dispatchTouchEvent(ev);
                }
                offsetY = mIndicator.getOffset();
                int currentY = mIndicator.getCurrentPos();
                boolean movingDown = offsetY > 0;
                if (isMovingFooter() && isFooterInProcessing() && mStatus == SR_STATUS_COMPLETE
                        && mIndicator.hasLeftStartPosition() && !canNotChildScrollDown) {
                    mScrollChecker.tryToScrollTo(IIndicator.START_POS, 0);
                    return super.dispatchTouchEvent(ev);
                }
                if (movingDown) {
                    final float maxHeaderDistance = mIndicator.getCanMoveTheMaxDistanceOfHeader();
                    if (isMovingHeader() && !mIndicator.isInStartPosition()
                            && maxHeaderDistance > 0) {
                        if (currentY >= maxHeaderDistance) {
                            updateAnotherDirectionPos();
                            return super.dispatchTouchEvent(ev);
                        } else if (currentY + offsetY > maxHeaderDistance) {
                            moveHeaderPos(maxHeaderDistance - currentY);
                            return true;
                        }
                    }
                } else {
                    final float maxFooterDistance = mIndicator.getCanMoveTheMaxDistanceOfFooter();
                    if (isMovingFooter() && !mIndicator.isInStartPosition()
                            && maxFooterDistance > 0) {
                        if (currentY >= maxFooterDistance) {
                            updateAnotherDirectionPos();
                            return super.dispatchTouchEvent(ev);
                        } else if (currentY - offsetY > maxFooterDistance) {
                            moveFooterPos(currentY - maxFooterDistance);
                            return true;
                        }
                    } else if (isDisabledLoadMoreWhenContentNotFull() && mIndicator
                            .isInStartPosition() && canNotChildScrollDown && canNotChildScrollUp) {
                        return true;
                    }
                }
                boolean canMoveUp = isMovingHeader() && mIndicator.hasLeftStartPosition();
                boolean canMoveDown = isMovingFooter() && mIndicator.hasLeftStartPosition();
                boolean canHeaderMoveDown = canNotChildScrollUp && !isDisabledRefresh();
                boolean canFooterMoveUp = canNotChildScrollDown && !isDisabledLoadMore();
                if (!canMoveUp && !canMoveDown) {
                    if ((movingDown && !canHeaderMoveDown) || (!movingDown && !canFooterMoveUp)) {
                        if (isLoadingMore() && mIndicator.hasLeftStartPosition()) {
                            moveFooterPos(offsetY);
                            return true;
                        } else if (isRefreshing() && mIndicator.hasLeftStartPosition()) {
                            moveHeaderPos(offsetY);
                            return true;
                        } else if ((isAutoRefresh() || mIndicator.getCurrentPos() < IIndicator.START_POS)
                                && !mBeenSendTouchEvent) {
                            makeNewTouchDownEvent(ev);
                            mBeenSendTouchEvent = true;
                        }
                        return super.dispatchTouchEvent(ev);
                    }
                    // should show up Header
                    if (movingDown) {
                        if (isDisabledRefresh())
                            return super.dispatchTouchEvent(ev);
                        moveHeaderPos(offsetY);
                        return true;
                    }
                    if (isDisabledLoadMore())
                        return super.dispatchTouchEvent(ev);
                    moveFooterPos(offsetY);
                    return true;
                }
                if (canMoveUp) {
                    if (isDisabledRefresh())
                        return super.dispatchTouchEvent(ev);
                    if ((!canHeaderMoveDown && movingDown)) {
                        sendDownEvent();
                        return super.dispatchTouchEvent(ev);
                    }
                    moveHeaderPos(offsetY);
                    return true;
                }
                if (isDisabledLoadMore())
                    return super.dispatchTouchEvent(ev);
                if ((!canFooterMoveUp && !movingDown)) {
                    sendDownEvent();
                    return super.dispatchTouchEvent(ev);
                }
                moveFooterPos(offsetY);
                return true;
        }
        return super.dispatchTouchEvent(ev);
    }


    protected boolean tryToFilterTouchEventInDispatchTouchEvent(MotionEvent ev) {
        if (mIsInterceptTouchEventInOnceTouch) {
            mOverScrollChecker.abortIfWorking();
            if (mIndicator.isInStartPosition() && !mScrollChecker.mIsRunning) {
                makeNewTouchDownEvent(ev);
                mIsInterceptTouchEventInOnceTouch = false;
            }
            return true;
        }
        if (mIsLastOverScrollCanNotAbort) {
            if (mIndicator.isInStartPosition() && !mOverScrollChecker.mIsScrolling) {
                makeNewTouchDownEvent(ev);
                mIsLastOverScrollCanNotAbort = false;
            }
            return true;
        }
        if (mIsSpringBackCanNotBeInterrupted) {
            if (mIndicator.isInStartPosition() && !mScrollChecker.mIsRunning
                    && !mOverScrollChecker.mIsScrolling) {
                makeNewTouchDownEvent(ev);
                mIsSpringBackCanNotBeInterrupted = false;
            }
            return true;
        }
        return false;
    }

    protected void preparePaint() {
        if (mBackgroundPaint == null) {
            mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mBackgroundPaint.setStyle(Paint.Style.FILL);
        }
    }

    protected boolean isNeedInterceptTouchEvent() {
        return (isEnabledInterceptEventWhileLoading() && (isRefreshing() || isLoadingMore()))
                || mChangeStateAnimator != null && mChangeStateAnimator.isRunning()
                || (isAutoRefresh() && mAutomaticActionInScrolling);
    }

    protected boolean isNeedFilterTouchEvent() {
        return mIsLastOverScrollCanNotAbort || mIsSpringBackCanNotBeInterrupted
                || mIsInterceptTouchEventInOnceTouch;
    }

    protected boolean isCanNotAbortOverScrolling() {
        return (mOverScrollChecker.mIsScrolling
                && (((isMovingHeader() && isDisabledRefresh()))
                || (isMovingFooter() && isDisabledLoadMore())));
    }

    public boolean isNotYetInEdgeCannotMoveHeader() {
        if (mInEdgeCanMoveHeaderCallBack != null)
            return mInEdgeCanMoveHeaderCallBack.isNotYetInEdgeCannotMoveHeader(this,
                    mTargetView, mHeaderView);
        return ScrollCompat.canChildScrollUp(mTargetView);
    }

    public boolean isNotYetInEdgeCannotMoveFooter() {
        if (mInEdgeCanMoveFooterCallBack != null)
            return mInEdgeCanMoveFooterCallBack.isNotYetInEdgeCannotMoveFooter(this,
                    mTargetView, mFooterView);
        return ScrollCompat.canChildScrollDown(mTargetView);
    }

    protected boolean isInsideAnotherDirectionView(final float x, final float y) {
        if (mFingerInsideAnotherDirectionViewCallback != null)
            return mFingerInsideAnotherDirectionViewCallback.isInside(x, y, mTargetView);
        return BoundaryUtil.isInsideHorizontalView(x, y, mTargetView);
    }


    protected void makeNewTouchDownEvent(MotionEvent ev) {
        sendCancelEvent();
        sendDownEvent();
        mIndicatorSetter.onFingerUp();
        mIndicatorSetter.onFingerDown(ev.getX(), ev.getY());
    }

    protected void sendCancelEvent() {
        if (mHasSendCancelEvent || mLastMoveEvent == null) return;
        if (sDebug) {
            SRLog.d(TAG, "sendCancelEvent()");
        }
        final MotionEvent last = mLastMoveEvent;
        MotionEvent ev = MotionEvent.obtain(last.getDownTime(), last.getEventTime() +
                        ViewConfiguration.getLongPressTimeout(),
                MotionEvent.ACTION_CANCEL, last.getX(), last.getY(), last.getMetaState());
        mHasSendCancelEvent = true;
        mHasSendDownEvent = false;
        super.dispatchTouchEvent(ev);
    }

    protected void sendDownEvent() {
        if (mHasSendDownEvent || mLastMoveEvent == null) return;
        if (sDebug) {
            SRLog.d(TAG, "sendDownEvent()");
        }
        final MotionEvent last = mLastMoveEvent;
        MotionEvent ev = MotionEvent.obtain(last.getDownTime(), last.getEventTime(),
                MotionEvent.ACTION_DOWN, last.getX(), last.getY(), last.getMetaState());
        mHasSendCancelEvent = false;
        mHasSendDownEvent = true;
        super.dispatchTouchEvent(ev);
    }

    protected void notifyFingerUp() {
        if (mHeaderView != null && isHeaderInProcessing() && !isDisabledRefresh()) {
            mHeaderView.onFingerUp(this, mIndicator);
        } else if (mFooterView != null && isFooterInProcessing() && !isDisabledLoadMore()) {
            mFooterView.onFingerUp(this, mIndicator);
        }
    }

    protected void onFingerUp(boolean stayForLoading) {
        if (sDebug) {
            SRLog.d(TAG, "onFingerUp(): stayForLoading: %s", stayForLoading);
        }
        notifyFingerUp();
        if (!stayForLoading && isEnabledKeepRefreshView() && mStatus != SR_STATUS_COMPLETE
                && !isRefreshing() && !isLoadingMore() && mMode == Constants.MODE_DEFAULT) {
            if (isHeaderInProcessing() && !isDisabledRefresh()
                    && mIndicator.isOverOffsetToKeepHeaderWhileLoading()) {
                if (!mIndicator.isAlreadyHere(mIndicator.getOffsetToKeepHeaderWhileLoading())
                        && !isDisabledPerformRefresh()) {
                    mScrollChecker.tryToScrollTo(mIndicator.getOffsetToKeepHeaderWhileLoading(),
                            mDurationOfBackToHeaderHeight);
                    return;
                }
            } else if (isFooterInProcessing() && !isDisabledLoadMore()
                    && mIndicator.isOverOffsetToKeepFooterWhileLoading()) {
                if (!mIndicator.isAlreadyHere(mIndicator.getOffsetToKeepFooterWhileLoading())
                        && !isDisabledPerformLoadMore()) {
                    mScrollChecker.tryToScrollTo(mIndicator.getOffsetToKeepFooterWhileLoading(),
                            mDurationOfBackToFooterHeight);
                    return;
                }
            }
        }
        onRelease(false);
    }

    protected void onRelease(boolean springBack) {
        mAutomaticActionInScrolling = false;
        if (canInterceptRelease() || (isEnabledNoMoreData() && isMovingFooter()
                && isEnabledNoSpringBackWhenNoMoreData()))
            return;
        tryToPerformRefresh();
        if (mStatus == SR_STATUS_REFRESHING || mStatus == SR_STATUS_LOADING_MORE) {
            if (isEnabledKeepRefreshView()) {
                if (isHeaderInProcessing()) {
                    if (isMovingHeader() && mIndicator.isOverOffsetToKeepHeaderWhileLoading()) {
                        mScrollChecker.tryToScrollTo(mIndicator.getOffsetToKeepHeaderWhileLoading(),
                                springBack ? mOverScrollChecker.mDuration :
                                        mDurationOfBackToHeaderHeight);
                        return;
                    } else if (!isMovingFooter()) {
                        return;
                    }
                } else if (isFooterInProcessing()) {
                    if (isMovingFooter() && mIndicator.isOverOffsetToKeepFooterWhileLoading()) {
                        mScrollChecker.tryToScrollTo(mIndicator.getOffsetToKeepFooterWhileLoading(),
                                springBack ? mOverScrollChecker.mDuration :
                                        mDurationOfBackToFooterHeight);
                        return;
                    } else if (!isMovingHeader()) {
                        return;
                    }
                }
            }
        } else if (mStatus == SR_STATUS_COMPLETE) {
            notifyUIRefreshComplete(true);
            return;
        }
        if (springBack)
            tryScrollBackToTop(mOverScrollChecker.mDuration);
        else
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
            tryScrollBackToTop(0);
        }
    }

    protected void tryScrollBackToTop(int duration) {
        if (sDebug) {
            SRLog.d(TAG, "tryScrollBackToTop(): duration: %s", duration);
        }
        if (mIndicator.hasLeftStartPosition() && (!mIndicator.hasTouched() || !mIndicator.hasMoved())) {
            mScrollChecker.tryToScrollTo(IIndicator.START_POS, duration);
            return;
        }
        if (isNeedFilterTouchEvent() && mIndicator.hasLeftStartPosition()) {
            mScrollChecker.tryToScrollTo(IIndicator.START_POS, duration);
            return;
        }
        if (isMovingFooter() && mStatus == SR_STATUS_COMPLETE
                && mIndicator.hasJustBackToStartPosition()) {
            mScrollChecker.tryToScrollTo(IIndicator.START_POS, duration);
            return;
        }
        tryToNotifyReset();
    }

    protected void notifyUIRefreshComplete(boolean useScroll) {
        mIndicatorSetter.onRefreshComplete();
        if (mNeedNotifyRefreshComplete) {
            if (isHeaderInProcessing() && mHeaderView != null) {
                mHeaderView.onRefreshComplete(this, mIsLastRefreshSuccessful);
            } else if (isFooterInProcessing() && mFooterView != null) {
                mFooterView.onRefreshComplete(this, mIsLastRefreshSuccessful);
            }
            if (mNeedNotifyRefreshListener && mRefreshListener != null) {
                mRefreshListener.onRefreshComplete(mIsLastRefreshSuccessful);
            }
            mNeedNotifyRefreshComplete = false;
        } else if (mDelayedRefreshComplete && mNeedNotifyRefreshListener && mRefreshListener != null) {
            mRefreshListener.onRefreshComplete(mIsLastRefreshSuccessful);
        }
        if (useScroll) tryScrollBackToTopByPercentDuration();
        tryToNotifyReset();
    }

    protected void moveHeaderPos(float delta) {
        if (sDebug) {
            SRLog.d(TAG, "moveHeaderPos(): delta: %s", delta);
        }
        mIndicatorSetter.setMovingStatus(Constants.MOVING_HEADER);
        // to keep the consistence with refresh, need to converse the deltaY
        movePos(delta);
    }

    protected void moveFooterPos(float delta) {
        if (sDebug) {
            SRLog.d(TAG, "moveFooterPos(): delta: %s", delta);
        }
        mIndicatorSetter.setMovingStatus(Constants.MOVING_FOOTER);
        //check if it is needed to compatible scroll
        if (mCompatLoadMoreScroll && !isEnabledPinContentView() && mIsLastRefreshSuccessful
                && (!mIndicator.hasTouched() || mNestedScrollInProgress
                || isEnabledSmoothRollbackWhenCompleted())
                && mStatus == SR_STATUS_COMPLETE && !mOverScrollChecker.mIsScrolling) {
            if (sDebug) {
                SRLog.d(TAG, "moveFooterPos(): compatible scroll delta: %s", delta);
            }
            mNeedFilterScrollEvent = true;
            compatLoadMoreScroll(delta);
        }
        movePos(-delta);
    }

    protected void compatLoadMoreScroll(float delta) {
        if (mLoadMoreScrollCallback == null) {
            if (mScrollTargetView != null) {
                try {
                    ScrollCompat.scrollCompat(mScrollTargetView, delta);
                } catch (Exception ignored) {//ignored
                }
            } else if (mTargetView != null)
                try {
                    ScrollCompat.scrollCompat(mTargetView, delta);
                } catch (Exception ignored) {//ignored
                }
        } else {
            mLoadMoreScrollCallback.onScroll(mTargetView, delta);
        }
    }

    protected void movePos(float delta) {
        if (delta == 0f) {
            if (sDebug) {
                SRLog.d(TAG, "movePos(): delta is zero");
            }
            return;
        }
        if (delta > 0) {
            delta = tryToFilterMovePos(delta);
            if (delta <= 0)
                return;
        }
        if (delta > 0 && mMode == Constants.MODE_SCALE && calculateScale() >= 1.2f) {
            return;
        }
        int to = mIndicator.getCurrentPos() + Math.round(delta);
        // over top
        if ((mMode == Constants.MODE_DEFAULT || mOverScrollChecker.mIsFling)
                && to < IIndicator.START_POS) {
            to = IIndicator.START_POS;
            if (sDebug) {
                SRLog.d(TAG, "movePos(): over top");
            }
        }
        mBeenSendTouchEvent = false;
        mIndicatorSetter.setCurrentPos(to);
        int change = to - mIndicator.getLastPos();
        if (getParent() != null && !mNestedScrollInProgress && mIndicator.hasTouched()
                && mIndicator.hasJustLeftStartPosition())
            getParent().requestDisallowInterceptTouchEvent(true);
        if (isMovingHeader())
            updatePos(change);
        else if (isMovingFooter())
            updatePos(-change);
    }


    protected float tryToFilterMovePos(float deltaY) {
        if (isMovingHeader() && !isDisabledRefresh() && mHeaderView != null) {
            int style = mHeaderView.getStyle();
            if (style != IRefreshView.STYLE_DEFAULT && style != IRefreshView.STYLE_FOLLOW_CENTER) {
                final int maxHeight = getHeight() - getPaddingTop() - getPaddingBottom();
                if (mIndicator.getCurrentPos() + Math.round(deltaY) > maxHeight) {
                    return maxHeight - mIndicator.getCurrentPos();
                }
            }
        } else if (isMovingFooter() && !isDisabledLoadMore() && mFooterView != null) {
            int style = mFooterView.getStyle();
            if (style != IRefreshView.STYLE_DEFAULT && style != IRefreshView.STYLE_FOLLOW_CENTER) {
                final int maxHeight = getHeight() - getPaddingTop() - getPaddingBottom();
                if (mIndicator.getCurrentPos() + Math.round(deltaY) > maxHeight) {
                    return maxHeight - mIndicator.getCurrentPos();
                }
            }
        }
        return deltaY;
    }

    /**
     * Update view's Y position
     *
     * @param change The changed value
     */
    protected void updatePos(int change) {
        // once moved, cancel event will be sent to child
        if (mIndicator.hasTouched() && !mNestedScrollInProgress
                && mIndicator.hasMovedAfterPressedDown()) {
            sendCancelEvent();
        }
        final boolean isMovingHeader = isMovingHeader();
        final boolean isMovingFooter = isMovingFooter();
        // leave initiated position or just refresh complete
        if (mMode == Constants.MODE_DEFAULT && ((mIndicator.hasJustLeftStartPosition()
                || mViewStatus == SR_VIEW_STATUS_INIT) && mStatus == SR_STATUS_INIT)
                || (mStatus == SR_STATUS_COMPLETE && isEnabledNextPtrAtOnce()
                && ((isHeaderInProcessing() && isMovingHeader && change > 0)
                || (isFooterInProcessing() && isMovingFooter && change < 0)))) {
            mStatus = SR_STATUS_PREPARE;
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
        tryToDispatchNestedFling();
        // back to initiated position
        if (!(isAutoRefresh() && mStatus != SR_STATUS_COMPLETE)
                && mIndicator.hasJustBackToStartPosition()) {
            tryToNotifyReset();
            // recover event to children
            if (mIndicator.hasTouched() && !mNestedScrollInProgress && mHasSendCancelEvent) {
                sendDownEvent();
            }
        }
        tryToPerformRefreshWhenMoved();
        if (sDebug) {
            SRLog.d(TAG, "updatePos(): change: %s, current: %s last: %s",
                    change, mIndicator.getCurrentPos(), mIndicator.getLastPos());
        }
        notifyUIPositionChanged();
        boolean needRequestLayout = offsetChild(change, isMovingHeader, isMovingFooter);
        if (needRequestLayout || (!mOverScrollChecker.mIsScrolling && mIndicator
                .isInStartPosition())) {
            requestLayout();
        } else {
            invalidate();
        }
    }

    protected boolean offsetChild(int change, boolean isMovingHeader, boolean isMovingFooter) {
        boolean needRequestLayout = false;
        if (mMode == Constants.MODE_DEFAULT) {
            if (mHeaderView != null && !isDisabledRefresh() && isMovingHeader
                    && !isEnabledHideHeaderView()) {
                final int type = mHeaderView.getStyle();
                switch (type) {
                    case IRefreshView.STYLE_DEFAULT:
                        mHeaderView.getView().offsetTopAndBottom(change);
                        break;
                    case IRefreshView.STYLE_SCALE:
                        needRequestLayout = true;
                        break;
                    case IRefreshView.STYLE_PIN:
                        break;
                    case IRefreshView.STYLE_FOLLOW_PIN:
                        if (mIndicator.getCurrentPos() <= mIndicator.getHeaderHeight())
                            mHeaderView.getView().offsetTopAndBottom(change);
                        break;
                    case IRefreshView.STYLE_FOLLOW_SCALE:
                    case IRefreshView.STYLE_FOLLOW_CENTER:
                        if (mIndicator.getCurrentPos() > mIndicator.getHeaderHeight())
                            needRequestLayout = true;
                        else
                            mHeaderView.getView().offsetTopAndBottom(change);
                        break;
                }
                if (!isEnabledPinContentView() && mStickyHeaderView != null)
                    mStickyHeaderView.offsetTopAndBottom(change);
                if (isHeaderInProcessing())
                    mHeaderView.onRefreshPositionChanged(this, mStatus, mIndicator);
                else
                    mHeaderView.onPureScrollPositionChanged(this, mStatus, mIndicator);
            } else if (mFooterView != null && !isDisabledLoadMore() && isMovingFooter
                    && !isEnabledHideFooterView()) {
                final int type = mFooterView.getStyle();
                switch (type) {
                    case IRefreshView.STYLE_DEFAULT:
                        mFooterView.getView().offsetTopAndBottom(change);
                        break;
                    case IRefreshView.STYLE_SCALE:
                        needRequestLayout = true;
                        break;
                    case IRefreshView.STYLE_PIN:
                        break;
                    case IRefreshView.STYLE_FOLLOW_PIN:
                        if (mIndicator.getCurrentPos() <= mIndicator.getFooterHeight())
                            mFooterView.getView().offsetTopAndBottom(change);
                        break;
                    case IRefreshView.STYLE_FOLLOW_SCALE:
                    case IRefreshView.STYLE_FOLLOW_CENTER:
                        if (mIndicator.getCurrentPos() > mIndicator.getFooterHeight())
                            needRequestLayout = true;
                        else
                            mFooterView.getView().offsetTopAndBottom(change);
                        break;
                }
                if (isFooterInProcessing())
                    mFooterView.onRefreshPositionChanged(this, mStatus, mIndicator);
                else
                    mFooterView.onPureScrollPositionChanged(this, mStatus, mIndicator);
            }
            if (!isEnabledPinContentView()) {
                if (mScrollTargetView != null && isMovingFooter && !isDisabledLoadMore()) {
                    mScrollTargetView.setTranslationY(-mIndicator.getCurrentPos());
                } else {
                    if (mTargetView != null) mTargetView.offsetTopAndBottom(change);
                }
            }
        } else {
            if (mTargetView != null) {
                if (isMovingHeader) {
                    mTargetView.setPivotY(0);
                    mTargetView.setScaleY(calculateScale());
                } else if (isMovingFooter) {
                    if (mScrollTargetView != null) {
                        mScrollTargetView.setPivotY(getHeight());
                        mScrollTargetView.setScaleY(calculateScale());
                    } else {
                        mTargetView.setPivotY(getHeight());
                        mTargetView.setScaleY(calculateScale());
                    }
                } else {
                    mTargetView.setPivotY(0);
                    mTargetView.setScaleY(1);
                    if (mScrollTargetView != null) {
                        mScrollTargetView.setPivotY(0);
                        mScrollTargetView.setScaleY(1);
                    }
                }
                mTargetView.setScaleX(1);
                mTargetView.setPivotX(0);
                if (mScrollTargetView != null) {
                    mScrollTargetView.setScaleX(0);
                    mScrollTargetView.setPivotX(1);
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
        if (mMode == Constants.MODE_DEFAULT && !mOverScrollChecker.mIsScrolling && mStatus == SR_STATUS_PREPARE) {
            // reach fresh height while moving from top to bottom or reach load more height while
            // moving from bottom to top
            if (mIndicator.hasTouched() && !isAutoRefresh() && isEnabledPullToRefresh()
                    && ((isHeaderInProcessing() && isMovingHeader() && mIndicator
                    .crossRefreshLineFromTopToBottom())
                    || (isFooterInProcessing() && isMovingFooter() && mIndicator
                    .crossRefreshLineFromBottomToTop()))) {
                tryToPerformRefresh();
            }
            // reach Header height while auto refresh or reach Footer height while auto refresh
            if (!isRefreshing() && !isLoadingMore() && isPerformAutoRefreshButLater()
                    && ((isHeaderInProcessing() && isMovingHeader())
                    || (isFooterInProcessing() && isMovingFooter()))) {
                tryToPerformRefresh();
            }
        }
    }

    /**
     * We need to notify the X pos changed
     */
    protected void updateAnotherDirectionPos() {
        if (mMode == Constants.MODE_DEFAULT) {
            if (mHeaderView != null && !isDisabledRefresh() && isMovingHeader()
                    && !isEnabledHideHeaderView()) {
                if (isHeaderInProcessing())
                    mHeaderView.onRefreshPositionChanged(this, mStatus, mIndicator);
                else
                    mHeaderView.onPureScrollPositionChanged(this, mStatus, mIndicator);
            } else if (mFooterView != null && !isDisabledLoadMore() && isMovingFooter()
                    && !isEnabledHideFooterView()) {
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

    /**
     * Check in over scrolling needs to scroll back to the start position
     *
     * @return Is
     */
    private boolean canSpringBack() {
        if (mOverScrollChecker.mIsClamped && !mIndicator.isInStartPosition()) {
            mScrollChecker.updateInterpolator(mSpringInterpolator);
            mOverScrollChecker.mIsClamped = false;
            onRelease(true);
            return true;
        } else {
            return false;
        }
    }

    private void tryToDispatchNestedFling() {
        if (mDelayedNestedFling && mIndicator.isInStartPosition()) {
            mDelayedNestedFling = false;
            mScrollChecker.abortIfWorking();
            if (mDelayedScrollChecker != null)
                mDelayedScrollChecker.abortIfWorking();
            int v = (int) mOverScrollChecker.calculateVelocity();
            dispatchNestedFling(v);
        }
    }

    protected boolean tryToNotifyReset() {
        if (sDebug) {
            SRLog.d(TAG, "tryToNotifyReset()");
        }
        if ((mStatus == SR_STATUS_COMPLETE || mStatus == SR_STATUS_PREPARE)
                && mIndicator.isInStartPosition()) {
            if (mHeaderView != null)
                mHeaderView.onReset(this);
            if (mFooterView != null)
                mFooterView.onReset(this);
            mStatus = SR_STATUS_INIT;
            mViewStatus = SR_VIEW_STATUS_INIT;
            mIsSpringBackCanNotBeInterrupted = false;
            mNeedNotifyRefreshComplete = true;
            mDelayedRefreshComplete = false;
            mScrollChecker.destroy();
            mFlag = mFlag & ~MASK_AUTO_REFRESH;
            mAutomaticActionTriggered = false;
            tryToResetMovingStatus();
            resetScrollerInterpolator();
            if (mMode == Constants.MODE_SCALE && mTargetView != null) {
                mTargetView.setPivotX(0);
                mTargetView.setPivotY(0);
                mTargetView.setScaleX(1);
                mTargetView.setScaleY(1);
                if (mScrollTargetView != null) {
                    mScrollTargetView.setPivotX(0);
                    mScrollTargetView.setPivotY(0);
                    mScrollTargetView.setScaleX(1);
                    mScrollTargetView.setScaleY(1);
                }
            }
            if (getParent() != null)
                getParent().requestDisallowInterceptTouchEvent(false);
            return true;
        }
        return false;
    }

    protected void performRefreshComplete(boolean hook) {
        if (isRefreshing() && hook && mHeaderRefreshCompleteHook != null
                && mHeaderRefreshCompleteHook.mCallBack != null) {
            mHeaderRefreshCompleteHook.mLayout = this;
            mHeaderRefreshCompleteHook.doHook();
            return;
        }
        if (isLoadingMore() && hook && mFooterRefreshCompleteHook != null
                && mFooterRefreshCompleteHook.mCallBack != null) {
            mFooterRefreshCompleteHook.mLayout = this;
            mFooterRefreshCompleteHook.doHook();
            return;
        }
        mStatus = SR_STATUS_COMPLETE;
        notifyUIRefreshComplete(true);
    }

    /**
     * try to perform refresh or loading , if performed return true
     */
    protected void tryToPerformRefresh() {
        // status not be prepare or over scrolling or moving content go to break;
        if (mMode != Constants.MODE_DEFAULT || mStatus != SR_STATUS_PREPARE || !canPerformRefresh()) {
            return;
        }
        if (sDebug) {
            SRLog.d(TAG, "tryToPerformRefresh()");
        }
        if (isHeaderInProcessing() && !isDisabledRefresh() && !isDisabledPerformRefresh()
                && ((mIndicator.isOverOffsetToKeepHeaderWhileLoading() && isAutoRefresh())
                || (isEnabledKeepRefreshView() && mIndicator.isOverOffsetToKeepHeaderWhileLoading())
                || mIndicator.isOverOffsetToRefresh())) {
            triggeredRefresh(true);
            return;
        }
        if (isFooterInProcessing() && !isDisabledLoadMore() && !isDisabledPerformLoadMore()
                && ((mIndicator.isOverOffsetToKeepFooterWhileLoading() && isAutoRefresh())
                || (isEnabledKeepRefreshView() && mIndicator.isOverOffsetToKeepFooterWhileLoading())
                || mIndicator.isOverOffsetToLoadMore())) {
            triggeredLoadMore(true);
        }
    }

    protected void tryToPerformScrollToBottomToLoadMore() {
        if (isEnabledAutoLoadMore() && !isDisabledPerformLoadMore() && mMode ==
                Constants.MODE_DEFAULT && (mStatus == SR_STATUS_INIT || mStatus == SR_STATUS_PREPARE)) {
            if (mAutoLoadMoreCallBack != null && mAutoLoadMoreCallBack.canAutoLoadMore(this,
                    mTargetView)) {
                triggeredLoadMore(true);
            } else if (mAutoLoadMoreCallBack == null) {
                if (isMovingFooter() && mScrollTargetView != null
                        && ScrollCompat.canAutoLoadMore(mScrollTargetView)) {
                    triggeredLoadMore(true);
                } else if (ScrollCompat.canAutoLoadMore(mTargetView)) {
                    triggeredLoadMore(true);
                }
            }
        }
    }

    protected void tryToPerformScrollToTopToRefresh() {
        if (isEnabledAutoRefresh() && !isDisabledPerformRefresh() && mMode == Constants.MODE_DEFAULT
                && (mStatus == SR_STATUS_INIT || mStatus == SR_STATUS_PREPARE)
                && ScrollCompat.canAutoRefresh(mTargetView)) {
            triggeredRefresh(true);
        }
    }

    protected void triggeredRefresh(boolean notify) {
        mNeedNotifyRefreshListener = notify;
        mStatus = SR_STATUS_REFRESHING;
        mViewStatus = SR_VIEW_STATUS_HEADER_IN_PROCESSING;
        mDelayedRefreshComplete = false;
        mIsSpringBackCanNotBeInterrupted = false;
        performRefresh();
    }

    protected void triggeredLoadMore(boolean notify) {
        mNeedNotifyRefreshListener = notify;
        mStatus = SR_STATUS_LOADING_MORE;
        mViewStatus = SR_VIEW_STATUS_FOOTER_IN_PROCESSING;
        mDelayedRefreshComplete = false;
        mIsSpringBackCanNotBeInterrupted = false;
        performRefresh();
    }

    protected void tryToResetMovingStatus() {
        if (mIndicator.isInStartPosition() && !isMovingContent()
                && mStatus == SR_STATUS_INIT) {
            mIndicatorSetter.setMovingStatus(Constants.MOVING_CONTENT);
            notifyUIPositionChanged();
        }
    }

    protected boolean canPerformRefresh() {
        return !(mOverScrollChecker.mIsClamped || mOverScrollChecker.mIsScrolling
                || isMovingContent());
    }

    /**
     * Try check auto refresh later flag
     *
     * @return Is
     */
    protected boolean isPerformAutoRefreshButLater() {
        return (mFlag & MASK_AUTO_REFRESH) == FLAG_AUTO_REFRESH_BUT_LATER;
    }

    protected void performRefresh() {
        //loading start milliseconds since boot
        mLoadingStartTime = SystemClock.uptimeMillis();
        mNeedNotifyRefreshComplete = true;
        if (sDebug) {
            SRLog.d(TAG, "onRefreshBegin systemTime: %s", mLoadingStartTime);
        }
        if (isRefreshing()) {
            if (mHeaderView != null)
                mHeaderView.onRefreshBegin(this, mIndicator);
        } else if (isLoadingMore()) {
            if (mFooterView != null)
                mFooterView.onRefreshBegin(this, mIndicator);
        }
        if (mNeedNotifyRefreshListener && mRefreshListener != null)
            mRefreshListener.onRefreshBegin(isRefreshing());
    }

    protected void dispatchNestedFling(int velocity) {
        if (mScrollTargetView != null)
            ScrollCompat.flingCompat(mScrollTargetView, -velocity);
        else
            ScrollCompat.flingCompat(mTargetView, -velocity);
    }

    private void notifyUIPositionChanged() {
        if (mUIPositionChangedListeners != null && !mUIPositionChangedListeners.isEmpty()) {
            for (OnUIPositionChangedListener listener : mUIPositionChangedListeners) {
                listener.onChanged(mStatus, mIndicator);
            }
        }
    }

    /**
     * Safely remove the onScrollChangedListener from target ViewTreeObserver
     */
    private void safelyRemoveListeners() {
        if (mTargetViewTreeObserver != null) {
            if (mTargetViewTreeObserver.isAlive())
                mTargetViewTreeObserver.removeOnScrollChangedListener(this);
            else {
                SRReflectUtil.safelyRemoveListeners(mTargetViewTreeObserver, this);
            }
        }
    }

    private boolean canInterceptRelease() {
        return mOverScrollChecker.mMode == OverScrollChecker.MODE_PRE_FLING
                && mOverScrollChecker.mIsScrolling;
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
         * Called when a swipe gesture triggers a refresh.
         *
         * @param isRefresh Refresh is true , load more is false
         */
        void onRefreshBegin(boolean isRefresh);

        /**
         * Called when refresh completed.
         *
         * @param isSuccessful refresh state
         */
        void onRefreshComplete(boolean isSuccessful);
    }

    /**
     * Classes that wish to be notified when the state changes should implement this interface.
     */
    public interface OnStateChangedListener {
        /**
         * Called when the state changes
         *
         * @param previous The previous state @see {@link State}
         * @param current  The current state @see {@link State}
         */
        void onStateChanged(@State int previous, @State int current);
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

    public static class LayoutParams extends MarginLayoutParams {
        private static final int[] LAYOUT_ATTRS = new int[]{android.R.attr.layout_gravity};
        private int mGravity = Gravity.TOP | Gravity.START;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            final TypedArray a = c.obtainStyledAttributes(attrs, LAYOUT_ATTRS);
            mGravity = a.getInt(0, mGravity);
            a.recycle();
        }

        public LayoutParams(int width, int height, int gravity) {
            super(width, height);
            this.mGravity = gravity;
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(LayoutParams source) {
            super(source);
            mGravity = source.mGravity;
        }

        public int getGravity() {
            return mGravity;
        }
    }

    public static class RefreshCompleteHook {
        private SmoothRefreshLayout mLayout;
        private OnHookUIRefreshCompleteCallBack mCallBack;

        public void onHookComplete() {
            if (mLayout != null) {
                if (SmoothRefreshLayout.sDebug) {
                    SRLog.d(SmoothRefreshLayout.TAG, "RefreshCompleteHook: onHookComplete()");
                }
                mLayout.performRefreshComplete(false);
            }
        }

        private void setHookCallBack(OnHookUIRefreshCompleteCallBack callBack) {
            mCallBack = callBack;
        }

        private void doHook() {
            if (mCallBack != null) {
                if (SmoothRefreshLayout.sDebug) {
                    SRLog.d(SmoothRefreshLayout.TAG, "RefreshCompleteHook: doHook()");
                }
                mCallBack.onHook(this);
            }
        }
    }

    /**
     * Delayed completion of loading
     */
    private static class DelayToRefreshComplete implements Runnable {
        private WeakReference<SmoothRefreshLayout> mLayoutWeakRf;

        private DelayToRefreshComplete(SmoothRefreshLayout layout) {
            mLayoutWeakRf = new WeakReference<>(layout);
        }

        @Override
        public void run() {
            if (mLayoutWeakRf.get() != null) {
                if (SmoothRefreshLayout.sDebug) {
                    SRLog.d(SmoothRefreshLayout.TAG, "DelayToRefreshComplete: run()");
                }
                mLayoutWeakRf.get().performRefreshComplete(true);
            }
        }
    }

    class DelayedScrollChecker implements Runnable {
        private int mVelocity;
        private boolean mRunning = false;

        void abortIfWorking() {
            if (mRunning) {
                SmoothRefreshLayout.this.removeCallbacks(this);
                mRunning = false;
            }
        }

        void updateVelocity(int velocity) {
            mVelocity = velocity;
            abortIfWorking();
            mRunning = true;
            SmoothRefreshLayout.this.postDelayed(this, 25);
        }

        @Override
        public void run() {
            if (mRunning) {
                SmoothRefreshLayout.this.dispatchNestedFling(mVelocity);
                mRunning = false;
            }
        }
    }

    /**
     * Support over Scroll feature
     * The Over Scroll checker
     */
    class OverScrollChecker implements Runnable {
        private static final byte MODE_NONE = -1;
        private static final byte MODE_PRE_FLING = 0;
        private static final byte MODE_FLING = 1;
        final int mMaxDistance;
        Scroller mScroller;
        int mDuration = 0;
        int mLastY = 0;
        float mVelocity = -1;
        boolean mIsScrolling = false;
        boolean mIsClamped = false;
        boolean mIsFling = false;
        private byte mMode = MODE_NONE;

        OverScrollChecker() {
            DisplayMetrics dm = SmoothRefreshLayout.this.getContext().getResources()
                    .getDisplayMetrics();
            mMaxDistance = dm.heightPixels / 8;
            mScroller = new Scroller(SmoothRefreshLayout.this.getContext(),
                    SmoothRefreshLayout.sLinearInterpolator, false);
        }

        void fling(float v) {
            destroy();
            mMode = MODE_FLING;
            mIsFling = true;
            mVelocity = v;
            mScroller.fling(0, 0, 0, (int) v, Integer.MIN_VALUE, Integer.MAX_VALUE,
                    Integer.MIN_VALUE, Integer.MAX_VALUE);
            final int finalY = mScroller.getFinalY();
            final int duration = mScroller.getDuration();
            if (SmoothRefreshLayout.sDebug) {
                SRLog.d(SmoothRefreshLayout.TAG, "OverScrollChecker: fling(): v: %s, finalY: %s," +
                        " duration: %s", v, finalY, duration);
            }
            mScroller.startScroll(0, 0, 0, finalY, duration);
        }

        void preFling(float v) {
            destroy();
            mMode = MODE_PRE_FLING;
            mIsFling = true;
            mVelocity = v;
            mScroller.fling(0, 0, 0, (int) v, Integer.MIN_VALUE, Integer.MAX_VALUE,
                    Integer.MIN_VALUE, Integer.MAX_VALUE);
            final int finalY = mScroller.getFinalY();
            final int duration = mScroller.getDuration();
            if (SmoothRefreshLayout.sDebug) {
                SRLog.d(SmoothRefreshLayout.TAG, "OverScrollChecker: preFling(): v: %s, finalY: %s," +
                        " duration: %s", v, finalY, duration);
            }
            mScroller.startScroll(0, 0, 0, finalY, duration);
            run();
        }

        float calculateVelocity() {
            final float percent = (mScroller.getDuration() - mScroller.timePassed())
                    / (float) mScroller.getDuration();
            return mVelocity * percent * percent / 2;
        }

        private void reset() {
            mIsFling = false;
            mMode = MODE_NONE;
            SmoothRefreshLayout.this.removeCallbacks(this);
            mScroller.forceFinished(true);
        }

        void destroy() {
            reset();
            mIsScrolling = false;
            mIsClamped = false;
            mDuration = 0;
            mLastY = 0;
            SmoothRefreshLayout.this.resetScrollerInterpolator();
        }

        void abortIfWorking() {
            if (mIsScrolling) {
                destroy();
            }
        }

        void computeScrollOffset() {
            if (mMode == MODE_FLING && mIsFling && mScroller.computeScrollOffset()) {
                SmoothRefreshLayout.this.removeCallbacks(this);
                SmoothRefreshLayout.this.postDelayed(this, 25);
            } else {
                mIsFling = false;
            }
        }

        @Override
        public void run() {
            if (!mIsFling)
                return;
            if (mMode == MODE_FLING) {
                checkFling();
            } else {
                checkPreFling();
            }
        }

        private void checkPreFling() {
            boolean finished = !mScroller.computeScrollOffset() || mScroller.isFinished();
            final int currY = mScroller.getCurrY();
            int deltaY = currY - mLastY;
            if (SmoothRefreshLayout.sDebug) {
                SRLog.d(SmoothRefreshLayout.TAG,
                        "OverScrollChecker: checkPreFling(): finished: %s, currentPos: %s, " +
                                "currentY:%s, lastY: %s, delta: %s",
                        finished, SmoothRefreshLayout.this.mIndicator.getCurrentPos(), currY,
                        mLastY, deltaY);
            }
            if (!finished) {
                mLastY = currY;
                mIsScrolling = true;
                if (SmoothRefreshLayout.this.isMovingHeader()) {
                    SmoothRefreshLayout.this.moveHeaderPos(deltaY);
                } else if (SmoothRefreshLayout.this.isMovingFooter()) {
                    SmoothRefreshLayout.this.moveFooterPos(deltaY);
                }
                ViewCompat.postOnAnimation(SmoothRefreshLayout.this, this);
            } else {
                destroy();
                SmoothRefreshLayout.this.mDelayedNestedFling = false;
                SmoothRefreshLayout.this.onRelease(false);
            }
        }

        private void checkFling() {
            if (!mScroller.isFinished()) {
                final int currY = mScroller.getCurrY();
                if (currY > 0 && SmoothRefreshLayout.this.isInStartPosition()
                        && !SmoothRefreshLayout.this.isNotYetInEdgeCannotMoveHeader()
                        && !SmoothRefreshLayout.this.mScrollChecker.mIsRunning) {
                    int to = calculate(true);
                    if (SmoothRefreshLayout.this.isEnabledAutoRefresh()
                            && !SmoothRefreshLayout.this.isDisabledPerformRefresh()) {
                        int offsetToKeepHeaderWhileLoading = SmoothRefreshLayout.this.mIndicator
                                .getOffsetToKeepHeaderWhileLoading();
                        if (to > offsetToKeepHeaderWhileLoading) {
                            to = offsetToKeepHeaderWhileLoading;
                        }
                        mDuration = Math.max(mDuration, SmoothRefreshLayout.this
                                .getDurationToCloseFooter());
                        mIsClamped = false;
                    }
                    if (SmoothRefreshLayout.sDebug) {
                        SRLog.d(SmoothRefreshLayout.TAG, "OverScrollChecker: checkFling(): to: %s, duration: %s",
                                to, mDuration);
                    }
                    SmoothRefreshLayout.this.mIndicatorSetter.setMovingStatus(Constants.MOVING_HEADER);
                    SmoothRefreshLayout.this.updateScrollerInterpolator(SmoothRefreshLayout
                            .this.mOverScrollInterpolator);
                    SmoothRefreshLayout.this.mScrollChecker.tryToScrollTo(to, mDuration);
                    //Add a buffer value
                    mDuration = (int) (mDuration * 1.25f);
                    mIsScrolling = true;
                    reset();
                    return;
                } else if (currY < 0 && SmoothRefreshLayout.this.isInStartPosition()
                        && !SmoothRefreshLayout.this.isNotYetInEdgeCannotMoveFooter()
                        && !SmoothRefreshLayout.this.mScrollChecker.mIsRunning) {
                    int to = calculate(false);
                    if (SmoothRefreshLayout.this.isEnabledAutoLoadMore()
                            && !SmoothRefreshLayout.this.isDisabledPerformLoadMore()) {
                        int offsetToKeepFooterWhileLoading = SmoothRefreshLayout.this.mIndicator
                                .getOffsetToKeepFooterWhileLoading();
                        if (to > offsetToKeepFooterWhileLoading) {
                            to = offsetToKeepFooterWhileLoading;
                        }
                        mDuration = Math.max(mDuration, SmoothRefreshLayout.this
                                .getDurationToCloseFooter());
                        mIsClamped = false;
                    }
                    if (SmoothRefreshLayout.sDebug) {
                        SRLog.d(SmoothRefreshLayout.TAG, "OverScrollChecker: checkFling(): to: %s, duration: %s",
                                -to, mDuration);
                    }
                    SmoothRefreshLayout.this.mIndicatorSetter.setMovingStatus(Constants.MOVING_FOOTER);
                    SmoothRefreshLayout.this.updateScrollerInterpolator(SmoothRefreshLayout
                            .this.mOverScrollInterpolator);
                    SmoothRefreshLayout.this.mScrollChecker.tryToScrollTo(to, mDuration);
                    //Add a buffer value
                    mDuration = (int) (mDuration * 1.25f);
                    mIsScrolling = true;
                    reset();
                    return;
                }
            }
            mIsScrolling = false;
            mIsClamped = false;
        }

        private int calculate(boolean isMovingHeader) {
            mDuration = Math.max(mScroller.getDuration() - mScroller.timePassed(),
                    SmoothRefreshLayout.this.mMinOverScrollDuration);
            mDuration = Math.min(mDuration, SmoothRefreshLayout.this.mMaxOverScrollDuration);
            final int optimizedDistance = Math.min((int) Math.pow(Math.abs(calculateVelocity()),
                    .58f), mMaxDistance);
            final float maxViewDistance;
            final int viewHeight;
            if (isMovingHeader) {
                maxViewDistance = SmoothRefreshLayout.this.mIndicator
                        .getCanMoveTheMaxDistanceOfHeader();
                viewHeight = SmoothRefreshLayout.this.getHeaderHeight();
            } else {
                maxViewDistance = SmoothRefreshLayout.this.mIndicator
                        .getCanMoveTheMaxDistanceOfFooter();
                viewHeight = SmoothRefreshLayout.this.getFooterHeight();
            }
            int to = viewHeight > 0 ? Math.min(viewHeight, optimizedDistance) : optimizedDistance;
            if (maxViewDistance > 0 && to > maxViewDistance) {
                to = Math.round(maxViewDistance);
            }
            to = Math.max(to, SmoothRefreshLayout.this.mTouchSlop);
            if (SmoothRefreshLayout.sDebug) {
                SRLog.d(SmoothRefreshLayout.TAG, "OverScrollChecker: calculate(): " +
                                "isMovingHeader: %s, duration: %s, optimizedDistance: %s, " +
                                "maxViewDistance: %s, viewHeight: %s, to: %s",
                        isMovingHeader, mDuration, optimizedDistance, maxViewDistance,
                        viewHeight, to);
            }
            mIsClamped = true;
            return to;
        }
    }

    class ScrollChecker implements Runnable {
        int mLastY;
        int mLastStart;
        int mLastTo;
        boolean mIsRunning = false;
        Scroller mScroller;
        Interpolator mInterpolator;

        ScrollChecker() {
            mInterpolator = SmoothRefreshLayout.this.mSpringInterpolator;
            mScroller = new Scroller(SmoothRefreshLayout.this.getContext(), mInterpolator);
        }

        @Override
        public void run() {
            boolean finished = !mScroller.computeScrollOffset() || mScroller.isFinished();
            int curY = mScroller.getCurrY();
            int deltaY = curY - mLastY;
            if (SmoothRefreshLayout.sDebug) {
                SRLog.d(SmoothRefreshLayout.TAG,
                        "ScrollChecker: run(): finished: %s, start: %s, to: %s, currentPos: %s, " +
                                "currentY:%s, last: %s, delta: %s",
                        finished, mLastStart, mLastTo, SmoothRefreshLayout.this.mIndicator
                                .getCurrentPos(), curY, mLastY, deltaY);
            }
            if (!finished) {
                mLastY = curY;
                if (SmoothRefreshLayout.this.isMovingHeader()) {
                    SmoothRefreshLayout.this.moveHeaderPos(deltaY);
                } else if (SmoothRefreshLayout.this.isMovingFooter()) {
                    SmoothRefreshLayout.this.moveFooterPos(-deltaY);
                }
                ViewCompat.postOnAnimation(SmoothRefreshLayout.this, this);
            } else {
                if (!SmoothRefreshLayout.this.canSpringBack()) {
                    checkInStartPosition();
                    reset(true);
                    SmoothRefreshLayout.this.onRelease(false);
                }
            }
        }

        void updateInterpolator(Interpolator interpolator) {
            if (mInterpolator == interpolator)
                return;
            mInterpolator = interpolator;
            if (mIsRunning) {
                int timePassed = mScroller.timePassed();
                int duration = mScroller.getDuration();
                reset(false);
                mLastStart = mIndicator.getCurrentPos();
                int distance = mLastTo - mLastStart;
                mScroller = new Scroller(getContext(), interpolator);
                mScroller.startScroll(0, 0, 0, distance, duration - timePassed);
                ViewCompat.postOnAnimation(SmoothRefreshLayout.this, this);
            } else {
                reset(false);
                mScroller = SRReflectUtil.setScrollerInterpolatorOrReCreateScroller
                        (SmoothRefreshLayout.this.getContext(), mScroller, interpolator);
            }
        }

        private void checkInStartPosition() {
            //It should have scrolled to the specified location, but it has not scrolled
            if (mLastTo == IIndicator.START_POS
                    && !SmoothRefreshLayout.this.mIndicator.isInStartPosition()) {
                int currentPos = SmoothRefreshLayout.this.mIndicator.getCurrentPos();
                int deltaY = IIndicator.START_POS - currentPos;
                if (SmoothRefreshLayout.this.isMovingHeader()) {
                    SmoothRefreshLayout.this.moveHeaderPos(deltaY);
                } else if (SmoothRefreshLayout.this.isMovingFooter()) {
                    SmoothRefreshLayout.this.moveFooterPos(-deltaY);
                }
            }
        }

        private void reset(boolean stopOverScrollCheck) {
            mIsRunning = false;
            mLastY = 0;
            if (stopOverScrollCheck)
                mOverScrollChecker.abortIfWorking();
            SmoothRefreshLayout.this.removeCallbacks(this);
        }

        void destroy() {
            reset(true);
            mScroller.forceFinished(true);
        }

        void abortIfWorking() {
            if (mIsRunning) {
                mScroller.forceFinished(true);
                reset(true);
            }
        }

        void tryToScrollTo(int to, int duration) {
            mLastStart = SmoothRefreshLayout.this.mIndicator.getCurrentPos();
            if (SmoothRefreshLayout.this.mIndicator.isAlreadyHere(to)) {
                SmoothRefreshLayout.this.mOverScrollChecker.abortIfWorking();
                return;
            }
            mLastTo = to;
            int distance = to - mLastStart;
            mLastY = 0;
            if (SmoothRefreshLayout.sDebug) {
                SRLog.d(SmoothRefreshLayout.TAG, "ScrollChecker: tryToScrollTo(): start: %s, " +
                        "to:%s, duration:%s", mLastStart, to, duration);
            }
            if (duration > 0) {
                mScroller.startScroll(0, 0, 0, distance, duration);
                SmoothRefreshLayout.this.removeCallbacks(this);
                SmoothRefreshLayout.this.post(this);
                mIsRunning = true;
            } else {
                if (SmoothRefreshLayout.this.isMovingHeader()) {
                    SmoothRefreshLayout.this.moveHeaderPos(distance);
                } else if (SmoothRefreshLayout.this.isMovingFooter()) {
                    SmoothRefreshLayout.this.moveFooterPos(-distance);
                }
                destroy();
            }
        }
    }
}