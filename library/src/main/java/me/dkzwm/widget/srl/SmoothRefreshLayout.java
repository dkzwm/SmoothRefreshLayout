package me.dkzwm.widget.srl;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.SystemClock;
import android.support.annotation.IntDef;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MotionEventCompat;
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
import android.widget.AbsListView;
import android.widget.OverScroller;
import android.widget.Scroller;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.dkzwm.widget.srl.extra.IRefreshView;
import me.dkzwm.widget.srl.extra.header.MaterialHeader;
import me.dkzwm.widget.srl.gesture.GestureDetector;
import me.dkzwm.widget.srl.gesture.IGestureDetector;
import me.dkzwm.widget.srl.gesture.OnGestureListener;
import me.dkzwm.widget.srl.indicator.DefaultIndicator;
import me.dkzwm.widget.srl.indicator.IIndicator;
import me.dkzwm.widget.srl.utils.SRLog;
import me.dkzwm.widget.srl.utils.ScrollCompat;

import static android.view.View.MeasureSpec.makeMeasureSpec;

/**
 * Created by dkzwm on 2017/5/18.
 * <p>
 * Part of the code comes from @see <a href="https://github.com/liaohuqiu/android-Ultra-Pull-To-Refresh">
 * android-Ultra-Pull-To-Refresh</a><br/>
 * 部分代码实现来自 @see <a href="https://github.com/liaohuqiu">LiaoHuQiu</a> 的UltraPullToRefresh项目</p>
 * Support NestedScroll feature;<br/>
 * Support OverScroll feature;<br/>
 * Support Refresh and LoadMore feature;<br/>
 * Support AutoRefresh feature;<br/>
 * Support scroll to bottom to auto load more feature;<br/>
 * Support MultiState feature;<br/>
 *
 * @author dkzwm
 */
public class SmoothRefreshLayout extends ViewGroup implements OnGestureListener, NestedScrollingChild,
        NestedScrollingParent, ViewTreeObserver.OnScrollChangedListener {
    //state
    public static final byte STATE_CONTENT = 0;
    public static final byte STATE_ERROR = 1;
    public static final byte STATE_EMPTY = 2;
    public static final byte STATE_CUSTOM = 3;
    //status
    public static final byte SR_STATUS_INIT = 1;
    public static final byte SR_STATUS_PREPARE = 2;
    public static final byte SR_STATUS_REFRESHING = 3;
    public static final byte SR_STATUS_LOADING_MORE = 4;
    public static final byte SR_STATUS_COMPLETE = 5;
    //local
    private static final String TAG = "SmoothRefreshLayout";
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
    private static final int FLAG_ENABLE_LOAD_MORE_NO_MORE_DATA = 0x01 << 11;
    private static final int FLAG_DISABLE_LOAD_MORE = 0x01 << 12;
    private static final int FLAG_DISABLE_PERFORM_REFRESH = 0x01 << 13;
    private static final int FLAG_DISABLE_REFRESH = 0x01 << 14;
    private static final int FLAG_ENABLE_WHEN_SCROLLING_TO_BOTTOM_TO_PERFORM_LOAD_MORE = 0x01 << 15;
    private static final int FLAG_ENABLE_INTERCEPT_EVENT_WHILE_LOADING = 0x01 << 16;
    private static final int FLAG_DISABLE_WHEN_HORIZONTAL_MOVE = 0x01 << 17;
    private static final int FLAG_ENABLE_HIDE_HEADER_VIEW = 0x01 << 18;
    private static final int FLAG_ENABLE_HIDE_FOOTER_VIEW = 0x01 << 19;
    private static final byte MASK_AUTO_REFRESH = 0x03;
    private static final int MASK_DISABLE_PERFORM_LOAD_MORE = 0x05 << 10;
    private static final int MASK_DISABLE_PERFORM_REFRESH = 0x03 << 13;
    private static final int[] LAYOUT_ATTRS = new int[]{
            android.R.attr.enabled
    };
    private static boolean sDebug = false;
    private static IRefreshViewCreator sCreator;
    private final List<View> mCachedViews = new ArrayList<>(1);
    private final NestedScrollingParentHelper mNestedScrollingParentHelper;
    private final NestedScrollingChildHelper mNestedScrollingChildHelper;
    private final int[] mParentScrollConsumed = new int[2];
    private final int[] mParentOffsetInWindow = new int[2];
    @State
    protected int mState = STATE_CONTENT;
    protected int mPreviousState = -1;
    protected IRefreshView mHeaderView;
    protected IRefreshView mFooterView;
    protected IIndicator mIndicator;
    protected OnRefreshListener mRefreshListener;
    protected OnStateChangedListener mStateChangedListener;
    protected byte mStatus = SR_STATUS_INIT;
    protected boolean mTriggeredAutoRefresh = true;
    protected boolean mTriggeredAutoLoadMore = true;
    protected boolean mAutoRefreshUseSmoothScroll = false;
    protected boolean mNeedNotifyRefreshComplete = true;
    protected boolean mDelayedRefreshComplete = false;
    protected long mLoadingMinTime = 500;
    protected long mLoadingStartTime = 0;
    protected int mDurationToCloseHeader = 500;
    protected int mDurationToCloseFooter = 500;
    protected View mTargetView;
    protected View mContentView;
    protected View mEmptyView;
    protected View mErrorView;
    protected View mCustomView;
    protected View mLoadMoreScrollTargetView;
    protected LayoutInflater mInflater;
    protected int mContentResId = View.NO_ID;
    protected int mErrorLayoutResId = View.NO_ID;
    protected int mEmptyLayoutResId = View.NO_ID;
    protected int mCustomLayoutResId = View.NO_ID;
    private int mFlag = FLAG_DISABLE_LOAD_MORE;
    private Interpolator mDefaultSpringInterpolator;
    private IGestureDetector mGestureDetector;
    private OnChildScrollUpCallback mScrollUpCallback;
    private OnChildScrollDownCallback mScrollDownCallback;
    private OnLoadMoreScrollCallback mLoadMoreScrollCallback;
    private OnPerformAutoLoadMoreCallBack mAutoLoadMoreCallBack;
    private List<OnUIPositionChangedListener> mUIPositionChangedListeners;
    private ScrollChecker mScrollChecker;
    private OverScrollChecker mOverScrollChecker;
    private MotionEvent mLastMoveEvent;
    private DelayToRefreshComplete mDelayToRefreshComplete;
    private RefreshCompleteHook mHeaderRefreshCompleteHook;
    private RefreshCompleteHook mFooterRefreshCompleteHook;
    private ViewTreeObserver mTargetViewTreeObserver;
    private ValueAnimator mChangeStateAnimator;
    private boolean mHasSendCancelEvent = false;
    private boolean mHasSendDownEvent = false;
    private boolean mDealHorizontalMove = false;
    private boolean mPreventForHorizontal = false;
    private boolean mIsLastRefreshSuccessful = true;
    private boolean mNestedScrollInProgress = false;
    private boolean mViewsZAxisNeedReset = true;
    private boolean mNestedFling = false;
    private boolean mNeedScrollCompat = false;
    private float mOverScrollDistanceRatio = 0.8f;
    private int mDurationOfBackToHeaderHeight = 200;
    private int mDurationOfBackToFooterHeight = 200;
    private int mTouchSlop;
    private int mTotalRefreshingUnconsumed;
    private int mTotalRefreshingConsumed;
    private int mTotalLoadMoreUnconsumed;
    private int mTotalLoadMoreConsumed;
    private int mTouchPointerId;

    public SmoothRefreshLayout(Context context) {
        this(context, null);
    }

    public SmoothRefreshLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SmoothRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mIndicator = new DefaultIndicator();
        mInflater = LayoutInflater.from(context);
        TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.SmoothRefreshLayout, 0, 0);
        if (arr != null) {
            mContentResId = arr.getResourceId(R.styleable.SmoothRefreshLayout_sr_content, mContentResId);
            float resistance = arr.getFloat(R.styleable
                    .SmoothRefreshLayout_sr_resistance, IIndicator.DEFAULT_RESISTANCE);
            mIndicator.setResistance(resistance);
            mIndicator.setResistanceOfHeader(arr.getFloat(R.styleable
                    .SmoothRefreshLayout_sr_resistance_of_header, resistance));
            mIndicator.setResistanceOfFooter(arr.getFloat(R.styleable
                    .SmoothRefreshLayout_sr_resistance_of_footer, resistance));

            mDurationOfBackToHeaderHeight = arr.getInt(R.styleable
                            .SmoothRefreshLayout_sr_duration_of_back_to_keep_refresh_pos,
                    mDurationOfBackToHeaderHeight);
            mDurationOfBackToFooterHeight = arr.getInt(R.styleable
                            .SmoothRefreshLayout_sr_duration_of_back_to_keep_refresh_pos,
                    mDurationOfBackToFooterHeight);
            mDurationOfBackToHeaderHeight = arr.getInt(R.styleable
                            .SmoothRefreshLayout_sr_duration_of_back_to_keep_header_pos,
                    mDurationOfBackToHeaderHeight);
            mDurationOfBackToFooterHeight = arr.getInt(R.styleable
                            .SmoothRefreshLayout_sr_duration_of_back_to_keep_footer_pos,
                    mDurationOfBackToFooterHeight);

            mDurationToCloseHeader = arr.getInt(R.styleable
                            .SmoothRefreshLayout_sr_duration_to_close_of_refresh,
                    mDurationToCloseHeader);
            mDurationToCloseFooter = arr.getInt(R.styleable
                            .SmoothRefreshLayout_sr_duration_to_close_of_refresh,
                    mDurationToCloseFooter);
            mDurationToCloseHeader = arr.getInt(R.styleable
                    .SmoothRefreshLayout_sr_duration_to_close_of_header, mDurationToCloseHeader);
            mDurationToCloseFooter = arr.getInt(R.styleable
                    .SmoothRefreshLayout_sr_duration_to_close_of_footer, mDurationToCloseFooter);

            //ratio
            float ratio = arr.getFloat(R.styleable.
                    SmoothRefreshLayout_sr_ratio_of_refresh_height_to_refresh, IIndicator
                    .DEFAULT_RATIO_OF_REFRESH_VIEW_HEIGHT_TO_REFRESH);
            mIndicator.setRatioOfRefreshViewHeightToRefresh(ratio);
            mIndicator.setRatioOfHeaderHeightToRefresh(arr.getFloat(R.styleable
                    .SmoothRefreshLayout_sr_ratio_of_header_height_to_refresh, ratio));
            mIndicator.setRatioOfFooterHeightToRefresh(arr.getFloat(R.styleable
                    .SmoothRefreshLayout_sr_ratio_of_footer_height_to_refresh, ratio));

            ratio = arr.getFloat(R.styleable.
                    SmoothRefreshLayout_sr_offset_ratio_to_keep_refresh_while_Loading, IIndicator
                    .DEFAULT_RATIO_OF_REFRESH_VIEW_HEIGHT_TO_REFRESH);
            mIndicator.setOffsetRatioToKeepHeaderWhileLoading(ratio);
            mIndicator.setOffsetRatioToKeepFooterWhileLoading(ratio);
            mIndicator.setOffsetRatioToKeepHeaderWhileLoading(arr.getFloat(R.styleable
                    .SmoothRefreshLayout_sr_offset_ratio_to_keep_header_while_Loading, ratio));
            mIndicator.setOffsetRatioToKeepFooterWhileLoading(arr.getFloat(R.styleable
                    .SmoothRefreshLayout_sr_offset_ratio_to_keep_footer_while_Loading, ratio));

            //max move ratio of height
            ratio = arr.getFloat(R.styleable.
                    SmoothRefreshLayout_sr_can_move_the_max_ratio_of_refresh_height, IIndicator
                    .DEFAULT_CAN_MOVE_THE_MAX_RATIO_OF_REFRESH_VIEW_HEIGHT);
            mIndicator.setCanMoveTheMaxRatioOfRefreshViewHeight(ratio);
            mIndicator.setCanMoveTheMaxRatioOfHeaderHeight(arr.getFloat(R.styleable
                    .SmoothRefreshLayout_sr_can_move_the_max_ratio_of_header_height, ratio));
            mIndicator.setCanMoveTheMaxRatioOfFooterHeight(arr.getFloat(R.styleable
                    .SmoothRefreshLayout_sr_can_move_the_max_ratio_of_footer_height, ratio));

            setEnableKeepRefreshView(arr.getBoolean(R.styleable
                    .SmoothRefreshLayout_sr_enable_keep_refresh_view, true));
            setEnablePinContentView(arr.getBoolean(R.styleable
                    .SmoothRefreshLayout_sr_enable_pin_content, false));
            setEnableOverScroll(arr.getBoolean(R.styleable
                    .SmoothRefreshLayout_sr_enable_over_scroll, true));
            setEnablePullToRefresh(arr.getBoolean(R.styleable
                    .SmoothRefreshLayout_sr_enable_pull_to_refresh, false));

            setDisableRefresh(!arr.getBoolean(R.styleable.SmoothRefreshLayout_sr_enable_refresh,
                    true));
            setDisableLoadMore(!arr.getBoolean(R.styleable
                    .SmoothRefreshLayout_sr_enable_load_more, false));

            mErrorLayoutResId = arr.getResourceId(R.styleable.SmoothRefreshLayout_sr_error_layout,
                    NO_ID);
            mEmptyLayoutResId = arr.getResourceId(R.styleable.SmoothRefreshLayout_sr_empty_layout,
                    NO_ID);
            mCustomLayoutResId = arr.getResourceId(R.styleable.SmoothRefreshLayout_sr_custom_layout,
                    NO_ID);
            @State
            int state = arr.getInt(R.styleable.SmoothRefreshLayout_sr_state, STATE_CONTENT);
            mState = state;
            arr.recycle();
            arr = context.obtainStyledAttributes(attrs, LAYOUT_ATTRS, 0, 0);
            setEnabled(arr.getBoolean(0, true));
            arr.recycle();
        } else {
            setEnablePullToRefresh(true);
            setEnableKeepRefreshView(true);
        }
        final ViewConfiguration conf = ViewConfiguration.get(getContext());
        mTouchSlop = conf.getScaledTouchSlop();
        mGestureDetector = new GestureDetector(context, this);
        mScrollChecker = new ScrollChecker(this);
        mOverScrollChecker = new OverScrollChecker(this);
        mDefaultSpringInterpolator = new DecelerateInterpolator();
        //supports nested scroll
        mNestedScrollingChildHelper = new NestedScrollingChildHelper(this);
        mNestedScrollingParentHelper = new NestedScrollingParentHelper(this);
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
     * needed the refresh view,frame will use this creator to create refresh view<br/>
     * <p>
     * 设置默认的刷新视图构造器，当刷新视图为null且需要使用刷新视图时，Frame会使用该构造器构造刷新视图
     *
     * @param creator The static refresh view creator
     */
    public static void setDefaultCreator(IRefreshViewCreator creator) {
        sCreator = creator;
    }

    @Override
    final public void addView(View child) {
        addView(child, -1);
    }

    @Override
    final public void addView(View child, int index) {
        if (child == null) {
            throw new IllegalArgumentException("Cannot add a null child view to a ViewGroup");
        }
        ViewGroup.LayoutParams params = child.getLayoutParams();
        if (params == null) {
            params = generateDefaultLayoutParams();
            if (params == null) {
                throw new IllegalArgumentException("generateDefaultLayoutParams() cannot return null");
            }
        }
        addView(child, index, params);
    }

    @Override
    final public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        ensureFreshView(child);
    }

    @Override
    final public void addView(View child, ViewGroup.LayoutParams params) {
        addView(child, -1, params);
    }

    @Override
    final public void addView(View child, int width, int height) {
        final ViewGroup.LayoutParams params = generateDefaultLayoutParams();
        if (params == null) {
            throw new IllegalArgumentException("generateDefaultLayoutParams() cannot return null");
        }
        params.width = width;
        params.height = height;
        addView(child, -1, params);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (!enabled)
            reset();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        destroy();
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int count = getChildCount();
        if (count == 0)
            return;
        ensureTargetView();
        final boolean measureMatchParentChildren =
                MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY ||
                        MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY;
        int maxHeight = 0;
        int maxWidth = 0;
        int childState = 0;
        final int paddingLeft = getPaddingLeft();
        final int paddingRight = getPaddingRight();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() == GONE)
                continue;
            MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
            if (mHeaderView != null && child == mHeaderView) {
                if (isDisabledRefresh() || isEnabledHideHeaderView())
                    continue;
                if (mHeaderView.getStyle() == IRefreshView.STYLE_DEFAULT) {
                    measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
                } else {
                    final int childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec,
                            paddingLeft + paddingRight + lp.leftMargin + lp.rightMargin, lp.width);
                    final int heightSpec;
                    if (isMovingHeader()) {
                        heightSpec = makeMeasureSpec(mIndicator.getCurrentPosY()
                                + lp.topMargin, MeasureSpec.EXACTLY);
                    } else {
                        heightSpec = makeMeasureSpec(0, MeasureSpec.EXACTLY);
                    }
                    child.measure(childWidthMeasureSpec, heightSpec);
                }
                if (mHeaderView.getCustomHeight() > 0) {
                    mIndicator.setHeaderHeight(mHeaderView.getCustomHeight());
                } else {
                    if (mHeaderView.getStyle() == IRefreshView.STYLE_SCALE)
                        throw new IllegalArgumentException("If header view's type is " +
                                "STYLE_SCALE, you must set a accurate height");
                    mIndicator.setHeaderHeight(child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin);
                }
            } else if (mFooterView != null && child == mFooterView) {
                if (isDisabledLoadMore() || isEnabledHideFooterView())
                    continue;
                if (mFooterView.getStyle() == IRefreshView.STYLE_DEFAULT) {
                    measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
                } else {
                    final int childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec,
                            paddingLeft + paddingRight + lp.leftMargin + lp.rightMargin, lp.width);
                    final int heightSpec;
                    if (isMovingFooter()) {
                        heightSpec = makeMeasureSpec(mIndicator.getCurrentPosY()
                                + lp.topMargin + lp.bottomMargin, MeasureSpec.EXACTLY);
                    } else {
                        heightSpec = makeMeasureSpec(0, MeasureSpec.EXACTLY);
                    }
                    child.measure(childWidthMeasureSpec, heightSpec);
                }
                if (mFooterView.getCustomHeight() > 0) {
                    mIndicator.setFooterHeight(mFooterView.getCustomHeight());
                } else {
                    if (mFooterView.getStyle() == IRefreshView.STYLE_SCALE)
                        throw new IllegalArgumentException("If footer view's type is " +
                                "STYLE_SCALE, you must set a accurate height");
                    mIndicator.setFooterHeight(child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin);
                }
            } else if (mTargetView != null && mTargetView == child
                    || (mPreviousState != -1 && mChangeStateAnimator != null
                    && mChangeStateAnimator.isRunning() && getView(mPreviousState) == child)) {
                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
            } else if (measureMatchParentChildren) {
                final int childWidthMeasureSpec;
                final int measuredWidth = getMeasuredWidth();
                final int measuredHeight = getMeasuredHeight();
                if (lp.width == LayoutParams.MATCH_PARENT) {
                    final int width = Math.max(0, measuredWidth - getPaddingLeft() - getPaddingRight()
                            - lp.leftMargin - lp.rightMargin);
                    childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(
                            width, MeasureSpec.EXACTLY);
                } else {
                    childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec,
                            getPaddingLeft() + getPaddingRight() + lp.leftMargin + lp.rightMargin,
                            lp.width);
                }
                final int childHeightMeasureSpec;
                if (lp.height == LayoutParams.MATCH_PARENT) {
                    final int height = Math.max(0, measuredHeight - getPaddingTop() - getPaddingRight()
                            - lp.topMargin - lp.bottomMargin);
                    childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
                } else {
                    childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec,
                            getPaddingTop() + getPaddingBottom() + lp.topMargin + lp.bottomMargin,
                            lp.height);
                }
                child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
            } else {
                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
            }
            maxWidth = Math.max(maxWidth,
                    child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin);
            maxHeight = Math.max(maxHeight,
                    child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin);
            childState = combineMeasuredStates(childState, child.getMeasuredState());
        }
        maxWidth += paddingLeft + paddingRight;
        maxHeight += getPaddingTop() + getPaddingBottom();
        maxHeight = Math.max(maxHeight, getSuggestedMinimumHeight());
        maxWidth = Math.max(maxWidth, getSuggestedMinimumWidth());
        setMeasuredDimension(resolveSizeAndState(maxWidth, widthMeasureSpec, childState),
                resolveSizeAndState(maxHeight, heightMeasureSpec,
                        childState << MEASURED_HEIGHT_STATE_SHIFT));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int count = getChildCount();
        if (count == 0)
            return;
        checkViewsZAxisNeedReset();
        final int paddingLeft = getPaddingLeft();
        final int paddingTop = getPaddingTop();
        final int paddingRight = r - l - getPaddingRight();
        final int paddingBottom = b - t - getPaddingBottom();
        int offsetHeaderY = 0;
        int offsetFooterY = 0;
        if (isMovingHeader()) {
            offsetHeaderY = mIndicator.getCurrentPosY();
        } else if (isMovingFooter()) {
            offsetFooterY = mIndicator.getCurrentPosY();
        }
        int contentBottom = 0;
        boolean pin = (mLoadMoreScrollTargetView != null && !isMovingHeader())
                || isEnabledPinContentView();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() == GONE)
                continue;
            final LayoutParams lp = (LayoutParams) child.getLayoutParams();
            if (mHeaderView != null && child == mHeaderView) {
                if (isDisabledRefresh() || isEnabledHideHeaderView()) {
                    child.layout(0, 0, child.getMeasuredWidth(), 0);
                    if (sDebug) {
                        SRLog.d(TAG, "onLayout(): header: %s %s %s %s", 0, 0,
                                child.getMeasuredWidth(), 0);
                    }
                    continue;
                }
                final int left = paddingLeft + lp.leftMargin;
                final int offset = (isEnabledHeaderDrawerStyle()
                        || mHeaderView.getStyle() == IRefreshView.STYLE_SCALE
                        ? 0 : offsetHeaderY - mIndicator.getHeaderHeight());
                final int top = paddingTop + lp.topMargin + offset;
                final int right = left + child.getMeasuredWidth();
                final int bottom = top + child.getMeasuredHeight();
                child.layout(left, top, right, bottom);
                if (sDebug) {
                    SRLog.d(TAG, "onLayout(): header: %s %s %s %s", left, top, right, bottom);
                }
            } else if (mTargetView != null && child == mTargetView
                    || (mPreviousState != -1 && mChangeStateAnimator != null
                    && mChangeStateAnimator.isRunning() && getView(mPreviousState) == child)) {
                final int left = paddingLeft + lp.leftMargin;
                final int right = left + child.getMeasuredWidth();
                int top, bottom;
                if (isMovingHeader()) {
                    if (lp.bottomMargin != 0) {
                        top = paddingTop + lp.topMargin + (pin ? 0 : offsetHeaderY);
                        bottom = top + child.getMeasuredHeight() - (pin ? 0 : offsetHeaderY);
                    } else {
                        top = paddingTop + lp.topMargin + (pin ? 0 : offsetHeaderY);
                        bottom = top + child.getMeasuredHeight();
                    }
                    child.layout(left, top, right, bottom);
                    // If content view is moving and the bottom margin is not zero. we need
                    // scroll to the top to fix margin not working
                    // 如果内容视图在移动中且BottomMargin不为0，那么需要对内部视图做滑动处理使视图的Margin起作用
                    if (!pin && offsetHeaderY != 0 && lp.bottomMargin != 0 && mNeedScrollCompat) {
                        final int deltaY = offsetHeaderY - mIndicator.getLastPosY();
                        if (deltaY != 0 && !(mIndicator.hasTouched() && deltaY < 0)
                                && ScrollCompat.canChildScrollUp(child)) {
                            ScrollCompat.scrollCompat(child, -deltaY);
                            if (sDebug) {
                                SRLog.d(TAG, "onLayout(): do scroll compat top deltaY: %s", -deltaY);
                            }
                        }
                    }
                } else if (isMovingFooter()) {
                    if (lp.topMargin != 0) {
                        top = paddingTop + lp.topMargin;
                        bottom = top + child.getMeasuredHeight() - (pin ? 0 : offsetFooterY);
                    } else {
                        top = paddingTop + lp.topMargin - (pin ? 0 : offsetFooterY);
                        bottom = top + child.getMeasuredHeight();
                    }
                    child.layout(left, top, right, bottom);
                    // If content view is moving and the top margin is not zero. we need scroll to
                    // the bottom to fix margin not working
                    // 如果内容视图在移动中且TopMargin不为0，那么需要对内部视图做滑动处理使视图的Margin起作用
                    if (!pin && offsetFooterY != 0 && lp.topMargin != 0 && mNeedScrollCompat) {
                        final int deltaY = offsetFooterY - mIndicator.getLastPosY();
                        if (deltaY != 0 && !(mIndicator.hasTouched() && deltaY < 0)
                                && ScrollCompat.canChildScrollDown(child)) {
                            ScrollCompat.scrollCompat(child, deltaY);
                            if (sDebug) {
                                SRLog.d(TAG, "onLayout(): do scroll compat bottom deltaY: %s", deltaY);
                            }
                        }
                    }
                } else {
                    top = paddingTop + lp.topMargin;
                    bottom = top + child.getMeasuredHeight();
                    child.layout(left, top, right, bottom);
                }
                if (sDebug) {
                    SRLog.d(TAG, "onLayout(): content: %s %s %s %s", left, top, right, bottom);
                }
                if (isEnabledFooterDrawerStyle())
                    contentBottom = paddingTop + lp.topMargin + child.getMeasuredHeight();
                else
                    contentBottom = bottom + lp.bottomMargin;
            } else if (mFooterView == null || mFooterView != child) {
                final int width = child.getMeasuredWidth();
                final int height = child.getMeasuredHeight();
                int childLeft, childTop;
                int gravity = lp.mGravity;
                if (gravity == -1) {
                    gravity = Gravity.TOP | Gravity.START;
                }
                final int layoutDirection = ViewCompat.getLayoutDirection(this);
                final int absoluteGravity = GravityCompat.getAbsoluteGravity(gravity, layoutDirection);
                final int verticalGravity = gravity & Gravity.VERTICAL_GRAVITY_MASK;
                switch (absoluteGravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
                    case Gravity.CENTER_HORIZONTAL:
                        childLeft = paddingLeft + (paddingRight - paddingLeft - width) / 2
                                + lp.leftMargin - lp.rightMargin;
                        break;
                    case Gravity.RIGHT:
                        childLeft = paddingRight - width - lp.rightMargin;
                        break;
                    default:
                        childLeft = paddingLeft + lp.leftMargin;
                }
                switch (verticalGravity) {
                    case Gravity.CENTER_VERTICAL:
                        childTop = paddingTop + (paddingBottom - paddingTop - height) / 2
                                + lp.topMargin - lp.bottomMargin;
                        break;
                    case Gravity.BOTTOM:
                        childTop = paddingBottom - height - lp.bottomMargin;
                        break;
                    default:
                        childTop = paddingTop + lp.topMargin;
                }
                child.layout(childLeft, childTop, childLeft + width, childTop + height);
                if (sDebug) {
                    SRLog.d(TAG, "onLayout(): child: %s %s %s %s", childLeft, childTop, childLeft
                            + width, childTop + height);
                }
            }
        }
        if (mFooterView != null && mFooterView instanceof View
                && mFooterView.getView().getVisibility() != GONE) {
            View child = (View) mFooterView;
            if (isDisabledLoadMore() || isEnabledHideFooterView()) {
                child.layout(0, 0, child.getMeasuredWidth(), 0);
                if (sDebug) {
                    SRLog.d(TAG, "onLayout(): footer: %s %s %s %s", 0, 0,
                            child.getMeasuredWidth(), 0);
                }
            } else {
                MarginLayoutParams lp = (MarginLayoutParams) ((View) mFooterView).getLayoutParams();
                final int left = paddingLeft + lp.leftMargin;
                final int offset = (isEnabledFooterDrawerStyle()
                        ? -mIndicator.getFooterHeight()
                        : -(pin ? offsetFooterY : 0));
                final int top = lp.topMargin + contentBottom + offset;
                final int right = left + child.getMeasuredWidth();
                final int bottom = top + child.getMeasuredHeight();
                ((View) mFooterView).layout(left, top, right, bottom);
                if (sDebug) {
                    SRLog.d(TAG, "onLayout(): footer: %s %s %s %s", left, top, right, bottom);
                }
            }
        }
        if (mNeedScrollCompat)
            mNeedScrollCompat = false;
        tryToPerformAutoRefresh();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ensureTargetView();
        if (mState != STATE_CONTENT) {
            ensureContentView();
            if (mContentView != null)
                mContentView.setVisibility(GONE);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (!isEnabled() || mTargetView == null || (isDisabledLoadMore() && isDisabledRefresh())
                || (isEnabledPinRefreshViewWhileLoading() && (isRefreshing() || isLoadingMore()))
                || mNestedScrollInProgress) {
            return super.dispatchTouchEvent(ev);
        }
        return processDispatchTouchEvent(ev);
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean b) {
        if (!((android.os.Build.VERSION.SDK_INT < 21 && mTargetView instanceof AbsListView)
                || (mTargetView != null && !ViewCompat.isNestedScrollingEnabled(mTargetView)))) {
            super.requestDisallowInterceptTouchEvent(b);
        }
    }

    /**
     * Set loadMore scroll target view<br/>
     * For example the content view is a FrameLayout,with a listView in it.<br/>
     * You can call this method,set the listView as load more scroll target view.<br/>
     * Load more compat will try to make it smooth scrolling<br/>
     * <p>
     * 设置加载更多时需要做滑动处理的视图。<br/>
     * 例如在SmoothRefreshLayout中有一个CoordinatorLayout,
     * CoordinatorLayout中有AppbarLayout、RecyclerView等，加载更多时希望被移动的视图为RecyclerVieW
     * 而不是CoordinatorLayout,那么设置RecyclerView为TargetView即可
     *
     * @param view Target view
     */
    @SuppressWarnings({"unused"})
    public void setLoadMoreScrollTargetView(View view) {
        mLoadMoreScrollTargetView = view;
    }

    /**
     * Set the listener to be notified when a refresh is triggered.<br/>
     * <p>
     * 设置刷新监听回调
     *
     * @param listener Listener
     */
    public <T extends OnRefreshListener> void setOnRefreshListener(T listener) {
        mRefreshListener = listener;
    }

    /**
     * Set the listener to be notified when the state changed<br/>
     * <p>
     * 设置状态改变回调
     *
     * @param listener Listener
     */
    public void setOnStateChangedListener(OnStateChangedListener listener) {
        mStateChangedListener = listener;
    }

    /**
     * Add a listener to listen the views position change event<br/>
     * <p>
     * 设置UI位置变化回调
     *
     * @param listener Listener
     */
    public void addOnUIPositionChangedListener(OnUIPositionChangedListener listener) {
        if (mUIPositionChangedListeners == null)
            mUIPositionChangedListeners = new ArrayList<>();
        mUIPositionChangedListeners.add(listener);
    }

    /**
     * remove the listener<br/>
     * <p>
     * 移除UI位置变化回调
     *
     * @param listener Listener
     */
    public void removeOnUIPositionChangedListener(OnUIPositionChangedListener listener) {
        if (mUIPositionChangedListeners != null && !mUIPositionChangedListeners.isEmpty())
            mUIPositionChangedListeners.remove(listener);
    }

    public void clearOnUIPositionChangedListeners() {
        if (mUIPositionChangedListeners != null)
            mUIPositionChangedListeners.clear();
    }

    /**
     * Set a scrolling callback when loading more.<br/>
     * <p>
     * 设置当加载更多时滚动回调，可使用该属性对内部视图做滑动处理。例如内部视图是ListView，完成加载更多时，
     * 需要将加载出的数据显示出来，那么设置该回调，每次Footer
     * 回滚时拿到滚动的数值对ListView做向上滚动处理，将数据展示处理
     *
     * @param callback Callback that should be called when scrolling on loading more.
     */
    public void setOnLoadMoreScrollCallback(OnLoadMoreScrollCallback callback) {
        mLoadMoreScrollCallback = callback;
    }

    /**
     * Set a callback to override {@link SmoothRefreshLayout#canChildScrollUp()} method. Non-null
     * callback will return the value provided by the callback and ignore all internal logic.<br/>
     * <p>
     * 设置{@link SmoothRefreshLayout#canChildScrollUp()}的重载回调，用来检测内容视图是否在顶部
     *
     * @param callback Callback that should be called when canChildScrollUp() is called.
     */
    public void setOnChildScrollUpCallback(OnChildScrollUpCallback callback) {
        mScrollUpCallback = callback;
    }

    /**
     * Set a callback to override {@link SmoothRefreshLayout#canChildScrollDown()} method. Non-null
     * callback will return the value provided by the callback and ignore all internal logic.<br/>
     * <p>
     * 设置{@link SmoothRefreshLayout#canChildScrollDown()}的重载回调，用来检测内容视图是否在底部
     *
     * @param callback Callback that should be called when canChildScrollDown() is called.
     */
    public void setOnChildScrollDownCallback(OnChildScrollDownCallback callback) {
        mScrollDownCallback = callback;
    }

    /**
     * Set a callback to make sure you need to customize the specified trigger the auto load more
     * rule <br/>
     * <p>
     * 设置自动加载更多的触发条件回调，可自定义具体的触发自动加载更多的条件
     *
     * @param callBack Customize the specified triggered rule
     */
    public void setOnPerformAutoLoadMoreCallBack(OnPerformAutoLoadMoreCallBack callBack) {
        mAutoLoadMoreCallBack = callBack;
    }

    /**
     * Set a hook callback when the refresh complete event be triggered. Only can be called on
     * refreshing<br/>
     * <p>
     * 设置一个头部视图刷新完成前的Hook回调
     *
     * @param callback Callback that should be called when refreshComplete() is called.
     */
    public void setOnHookHeaderRefreshCompleteCallback(OnHookUIRefreshCompleteCallBack callback) {
        if (mHeaderRefreshCompleteHook == null)
            mHeaderRefreshCompleteHook = new RefreshCompleteHook();
        mHeaderRefreshCompleteHook.mLayoutWeakRf = new WeakReference<>(this);
        mHeaderRefreshCompleteHook.setHookCallBack(callback);
    }

    /**
     * Set a hook callback when the refresh complete event be triggered. Only can be called on
     * loading more<br/>
     * <p>
     * 设置一个尾部视图刷新完成前的Hook回调
     *
     * @param callback Callback that should be called when refreshComplete() is called.
     */
    public void setOnHookFooterRefreshCompleteCallback(OnHookUIRefreshCompleteCallBack callback) {
        if (mFooterRefreshCompleteHook == null)
            mFooterRefreshCompleteHook = new RefreshCompleteHook();
        mFooterRefreshCompleteHook.mLayoutWeakRf = new WeakReference<>(this);
        mFooterRefreshCompleteHook.setHookCallBack(callback);
    }

    public boolean equalsOnHookHeaderRefreshCompleteCallback(OnHookUIRefreshCompleteCallBack callBack) {
        return mHeaderRefreshCompleteHook != null && mHeaderRefreshCompleteHook.mCallBack == callBack;
    }

    public boolean equalsOnHookFooterRefreshCompleteCallback(OnHookUIRefreshCompleteCallBack callBack) {
        return mFooterRefreshCompleteHook != null && mFooterRefreshCompleteHook.mCallBack == callBack;
    }

    /**
     * Whether it is being refreshed<br/>
     * <p>
     * 是否在刷新中
     *
     * @return Refreshing
     */
    public boolean isRefreshing() {
        return mStatus == SR_STATUS_REFRESHING;
    }

    /**
     * Whether it is being refreshed<br/>
     * <p>
     * 是否在加载更多种
     *
     * @return Loading
     */
    public boolean isLoadingMore() {
        return mStatus == SR_STATUS_LOADING_MORE;
    }

    /**
     * Whether it is in start position<br/>
     * <p>
     * 是否在起始位置
     *
     * @return Is
     */
    public boolean isInStartPosition() {
        return mIndicator.isInStartPosition();
    }

    /**
     * Whether it is refresh successful<br/>
     * <p>
     * 是否刷新成功
     *
     * @return Is
     */
    public boolean isRefreshSuccessful() {
        return mIsLastRefreshSuccessful;
    }

    /**
     * Perform refresh complete, to reset the state to {@link SmoothRefreshLayout#SR_STATUS_INIT},
     * and set the last refresh operation successfully<br/>
     * <p>
     * 完成刷新，刷新状态为成功
     */
    final public void refreshComplete() {
        refreshComplete(true);
    }

    /**
     * Perform refresh complete, to reset the state to
     * {@link SmoothRefreshLayout#SR_STATUS_INIT}<br/>
     * <p>
     * 完成刷新，刷新状态`isSuccessful`
     *
     * @param isSuccessful Set the last refresh operation status
     */
    final public void refreshComplete(boolean isSuccessful) {
        refreshComplete(isSuccessful, 0);
    }

    /**
     * Perform refresh complete, delay to reset the state to
     * {@link SmoothRefreshLayout#SR_STATUS_INIT} and set the last refresh operation
     * successfully<br/>
     * <p>
     * 完成刷新，延迟`delayDurationToChangeState`时间
     *
     * @param delayDurationToChangeState Delay to change the state to
     *                                   {@link SmoothRefreshLayout#SR_STATUS_INIT}
     */
    final public void refreshComplete(long delayDurationToChangeState) {
        refreshComplete(true, delayDurationToChangeState);
    }

    /**
     * Perform refresh complete, delay to reset the state to
     * {@link SmoothRefreshLayout#SR_STATUS_INIT} and set the last refresh operation<br/>
     * <p>
     * 完成刷新，刷新状态`isSuccessful`，延迟`delayDurationToChangeState`时间
     *
     * @param delayDurationToChangeState Delay to change the state to
     *                                   {@link SmoothRefreshLayout#SR_STATUS_INIT}
     * @param isSuccessful               Set the last refresh operation
     */
    final public void refreshComplete(boolean isSuccessful, long delayDurationToChangeState) {
        if (sDebug) {
            SRLog.d(TAG, "refreshComplete(): isSuccessful: " + isSuccessful);
        }
        mIsLastRefreshSuccessful = isSuccessful;
        if (!isRefreshing() && !isLoadingMore())
            return;
        if (delayDurationToChangeState <= 0) {
            long delay = mLoadingMinTime - (SystemClock.uptimeMillis() - mLoadingStartTime);
            if (delay <= 0) {
                performRefreshComplete(true);
            } else {
                if (mDelayToRefreshComplete == null)
                    mDelayToRefreshComplete = new DelayToRefreshComplete(this);
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
            long delay = mLoadingMinTime - (SystemClock.uptimeMillis() - mLoadingStartTime);
            if (delayDurationToChangeState < delay)
                delayDurationToChangeState = delay;
            if (mDelayToRefreshComplete == null)
                mDelayToRefreshComplete = new DelayToRefreshComplete(this);
            postDelayed(mDelayToRefreshComplete, delayDurationToChangeState);
        }
    }

    /**
     * Set the loading min time<br/>
     * <p>
     * 设置加载过程的最小持续时间
     *
     * @param time Millis
     */
    public void setLoadingMinTime(long time) {
        mLoadingMinTime = time;
    }

    /**
     * Get the header height,
     * After the measurement is completed, the height will have value<br/>
     * <p>
     * 获取Header的高度，在布局计算完成前无法得到准确的值
     *
     * @return Height default is -1
     */
    public int getHeaderHeight() {
        return mIndicator.getHeaderHeight();
    }

    /**
     * Get the footer height,
     * After the measurement is completed, the height will have value<br/>
     * <p>
     * 获取Footer的高度，在布局计算完成前无法得到准确的值
     *
     * @return Height default is -1
     */
    public int getFooterHeight() {
        return mIndicator.getFooterHeight();
    }

    /**
     * Perform auto refresh at once<br/>
     * <p>
     * 自动刷新并立即触发刷新回调
     */
    public void autoRefresh() {
        autoRefresh(true);
    }

    /**
     * If @param atOnce has been set to true. Auto perform refresh at once.<br/>
     * <p>
     * 自动刷新，`atOnce`立即触发刷新回调
     *
     * @param atOnce Auto refresh at once
     */
    public void autoRefresh(boolean atOnce) {
        autoRefresh(atOnce, true);
    }

    /**
     * If @param atOnce has been set to true. Auto perform refresh at once.
     * If @param smooth has been set to true. Auto perform refresh will using smooth scrolling.<br/>
     * <p>
     * 自动刷新，`atOnce`立即触发刷新回调，`smooth`滚动到触发位置
     *
     * @param atOnce Auto refresh at once
     * @param smooth Auto refresh use smooth scrolling
     */
    public void autoRefresh(boolean atOnce, boolean smooth) {
        if (mStatus != SR_STATUS_INIT) {
            return;
        }
        if (!mTriggeredAutoLoadMore)
            throw new IllegalArgumentException("Can not trigger refresh and load at" +
                    " the same time");
        if (sDebug) {
            SRLog.d(TAG, "autoRefresh(): atOnce:", atOnce);
        }
        mFlag |= atOnce ? FLAG_AUTO_REFRESH_AT_ONCE : FLAG_AUTO_REFRESH_BUT_LATER;
        mStatus = SR_STATUS_PREPARE;
        if (mHeaderView != null)
            mHeaderView.onRefreshPrepare(this);
        mIndicator.setMovingStatus(IIndicator.MOVING_HEADER);
        mAutoRefreshUseSmoothScroll = smooth;
        int offsetToRefresh = mIndicator.getOffsetToRefresh();
        if (offsetToRefresh <= 0) {
            mTriggeredAutoRefresh = false;
        } else {
            mTriggeredAutoRefresh = true;
            mScrollChecker.tryToScrollTo(offsetToRefresh, smooth ? mDurationToCloseHeader : 0);
        }
        if (atOnce) {
            mStatus = SR_STATUS_REFRESHING;
            mDelayedRefreshComplete = false;
            performRefresh();
        }
    }

    /**
     * Perform auto load more at once<br/>
     * <p>
     * 自动加载更多，并立即触发刷新回调
     */
    public void autoLoadMore() {
        autoLoadMore(true);
    }

    /**
     * If @param atOnce has been set to true. Auto perform load more at once.<br/>
     * <p>
     * 自动加载更多，`atOnce`立即触发刷新回调
     *
     * @param atOnce Auto load more at once
     */
    public void autoLoadMore(boolean atOnce) {
        autoLoadMore(atOnce, true);
    }

    /**
     * If @param atOnce has been set to true. Auto perform load more at once.
     * If @param smooth has been set to true. Auto perform load more will using smooth scrolling
     * .<br/>
     * <p>
     * 自动加载更多，`atOnce`立即触发刷新回调，`smooth`滚动到触发位置
     *
     * @param atOnce Auto load more at once
     * @param smooth Auto load more use smooth scrolling
     */
    public void autoLoadMore(boolean atOnce, boolean smooth) {
        if (mStatus != SR_STATUS_INIT) {
            return;
        }
        if (!mTriggeredAutoRefresh)
            throw new IllegalArgumentException("Can not trigger refresh and load at" +
                    " the same time");
        if (sDebug) {
            SRLog.d(TAG, "autoLoadMore(): atOnce:", atOnce);
        }
        mFlag |= atOnce ? FLAG_AUTO_REFRESH_AT_ONCE : FLAG_AUTO_REFRESH_BUT_LATER;
        mStatus = SR_STATUS_PREPARE;
        if (mFooterView != null)
            mFooterView.onRefreshPrepare(this);
        mIndicator.setMovingStatus(IIndicator.MOVING_FOOTER);
        mAutoRefreshUseSmoothScroll = smooth;
        int offsetToLoadMore = mIndicator.getOffsetToLoadMore();
        if (offsetToLoadMore <= 0) {
            mTriggeredAutoLoadMore = false;
        } else {
            mTriggeredAutoLoadMore = true;
            mScrollChecker.tryToScrollTo(offsetToLoadMore, smooth ? mDurationToCloseFooter : 0);
        }
        if (atOnce) {
            mStatus = SR_STATUS_LOADING_MORE;
            mDelayedRefreshComplete = false;
            performRefresh();
        }
    }

    /**
     * The resistance while you are moving<br/>
     * <p>
     * 移动刷新视图时候的移动阻尼
     *
     * @param resistance Resistance
     */
    @SuppressWarnings({"unused"})
    public void setResistance(float resistance) {
        mIndicator.setResistance(resistance);
    }

    /**
     * The resistance while you are moving footer<br/>
     * <p>
     * 移动Footer视图时候的移动阻尼
     *
     * @param resistance Resistance
     */
    @SuppressWarnings({"unused"})
    public void setResistanceOfFooter(float resistance) {
        mIndicator.setResistanceOfFooter(resistance);
    }

    /**
     * The resistance while you are moving header<br/>
     * <p>
     * 移动Header视图时候的移动阻尼
     *
     * @param resistance Resistance
     */
    @SuppressWarnings({"unused"})
    public void setResistanceOfHeader(float resistance) {
        mIndicator.setResistanceOfHeader(resistance);
    }

    /**
     * the height ratio of the trigger refresh<br/>
     * <p>
     * 设置触发刷新时的位置占刷新视图的高度比
     *
     * @param ratio Height ratio
     */
    @SuppressWarnings({"unused"})
    public void setRatioOfRefreshViewHeightToRefresh(float ratio) {
        mIndicator.setRatioOfRefreshViewHeightToRefresh(ratio);
    }

    /**
     * the height ratio of the trigger refresh<br/>
     * <p>
     * 设置触发下拉刷新时的位置占Header视图的高度比
     *
     * @param ratio Height ratio
     */
    @SuppressWarnings({"unused"})
    public void setRatioOfHeaderHeightToRefresh(float ratio) {
        mIndicator.setRatioOfHeaderHeightToRefresh(ratio);
    }

    @SuppressWarnings({"unused"})
    public float getRatioOfHeaderHeightToRefresh(float ratio) {
        return mIndicator.getRatioOfHeaderHeightToRefresh();
    }

    @SuppressWarnings({"unused"})
    public float getRatioOfFooterHeightToRefresh() {
        return mIndicator.getRatioOfFooterHeightToRefresh();
    }

    /**
     * The height ratio of the trigger refresh<br/>
     * <p>
     * 设置触发加载更多时的位置占Footer视图的高度比
     *
     * @param ratio Height ratio
     */
    @SuppressWarnings({"unused"})
    public void setRatioOfFooterHeightToRefresh(float ratio) {
        mIndicator.setRatioOfFooterHeightToRefresh(ratio);
    }

    /**
     * Set in the refresh to keep the refresh view's position of the ratio of the view's height<br/>
     * <p>
     * 刷新中保持视图位置占刷新视图的高度比（默认:`1f`）,该属性的值必须小于等于触发刷新高度比才会有效果，
     * 当开启了{@link SmoothRefreshLayout#isEnabledKeepRefreshView}后，该属性会生效
     *
     * @param ratio Height ratio
     */
    public void setOffsetRatioToKeepRefreshViewWhileLoading(float ratio) {
        mIndicator.setOffsetRatioToKeepHeaderWhileLoading(ratio);
        mIndicator.setOffsetRatioToKeepFooterWhileLoading(ratio);
    }

    /**
     * Set in the refresh to keep the header view's position of the ratio of the view's height<br/>
     * <p>
     * 刷新中保持视图位置占Header视图的高度比（默认:`1f`）,该属性的值必须小于等于触发刷新高度比才会有效果
     *
     * @param ratio Height ratio
     */
    public void setOffsetRatioToKeepHeaderWhileLoading(float ratio) {
        mIndicator.setOffsetRatioToKeepHeaderWhileLoading(ratio);
    }

    /**
     * Set in the refresh to keep the footer view's position of the ratio of the view's height<br/>
     * <p>
     * 刷新中保持视图位置占Header视图的高度比（默认:`1f`）,该属性的值必须小于等于触发刷新高度比才会有效果
     *
     * @param ratio Height ratio
     */
    public void setOffsetRatioToKeepFooterWhileLoading(float ratio) {
        mIndicator.setOffsetRatioToKeepFooterWhileLoading(ratio);
    }

    public void setOverScrollDistanceRatio(float ratio) {
        mOverScrollDistanceRatio = ratio;
    }

    /**
     * The duration of return back to the start position <br/>
     * <p>
     * 设置刷新完成回滚到起始位置的时间
     *
     * @param duration Millis
     */
    @SuppressWarnings({"unused"})
    public void setDurationToClose(int duration) {
        mDurationToCloseHeader = duration;
        mDurationToCloseFooter = duration;
    }

    /**
     * Get the  duration of header return back to the start position
     *
     * @return mDuration
     */
    @SuppressWarnings({"unused"})
    public int getDurationToCloseHeader() {
        return mDurationToCloseHeader;
    }

    /**
     * The duration of header return back to the start position<br/>
     * <p>
     * 设置Header刷新完成回滚到起始位置的时间
     *
     * @param duration Millis
     */
    @SuppressWarnings({"unused"})
    public void setDurationToCloseHeader(int duration) {
        mDurationToCloseHeader = duration;
    }

    /**
     * Get the  duration of footer return back to the start position<br/>
     * <p>
     * 设置Footer刷新完成回滚到起始位置的时间
     *
     * @return mDuration
     */
    @SuppressWarnings({"unused"})
    public int getDurationToCloseFooter() {
        return mDurationToCloseFooter;
    }

    /**
     * The duration of footer return back to the start position
     *
     * @param duration Millis
     */
    @SuppressWarnings({"unused"})
    public void setDurationToCloseFooter(int duration) {
        mDurationToCloseFooter = duration;
    }

    /**
     * The duration of refresh view to return back to the keep refresh view position<br/>
     * <p>
     * 设置回滚到保持刷新视图位置的时间
     *
     * @param duration Millis
     */
    @SuppressWarnings({"unused"})
    public void setDurationOfBackToKeepRefreshViewPosition(int duration) {
        mDurationOfBackToHeaderHeight = duration;
        mDurationOfBackToFooterHeight = duration;
    }

    /**
     * Get the duration of header return back to the keep header position<br/>
     * <p>
     * 得到回滚到保持Header视图位置的时间
     *
     * @return Duration
     */
    @SuppressWarnings({"unused"})
    public int getDurationOfBackToKeepHeaderPosition() {
        return mDurationOfBackToHeaderHeight;
    }

    /**
     * The duration of header return back to the keep header position<br/>
     * <p>
     * 设置回滚到保持Header视图位置的时间
     *
     * @param duration Millis
     */
    @SuppressWarnings({"unused"})
    public void setDurationOfBackToKeepHeaderPosition(int duration) {
        this.mDurationOfBackToHeaderHeight = duration;
    }

    /**
     * Get the duration of footer return back to the keep footer position<br/>
     * <p>
     * 得到回滚到保持Footer视图位置的时间
     *
     * @return mDuration
     */
    @SuppressWarnings({"unused"})
    public int getDurationOfBackToKeepFooterPosition() {
        return mDurationOfBackToFooterHeight;
    }

    /**
     * The duration of footer return back to the keep footer position<br/>
     * <p>
     * 设置回顾到保持Footer视图位置的时间
     *
     * @param duration Millis
     */
    @SuppressWarnings({"unused"})
    public void setDurationOfBackToKeepFooterPosition(int duration) {
        this.mDurationOfBackToFooterHeight = duration;
    }

    /**
     * The max ratio of height for the refresh view when the finger moves<br/>
     * <p>
     * 设置最大移动距离占刷新视图的高度比
     *
     * @param ratio The max ratio of refresh view
     */
    @SuppressWarnings({"unused"})
    public void setCanMoveTheMaxRatioOfRefreshViewHeight(float ratio) {
        mIndicator.setCanMoveTheMaxRatioOfRefreshViewHeight(ratio);
    }

    @SuppressWarnings({"unused"})
    public float getCanMoveTheMaxRatioOfHeaderHeight() {
        return mIndicator.getCanMoveTheMaxRatioOfHeaderHeight();
    }

    /**
     * The max ratio of height for the header view when the finger moves<br/>
     * <p>
     * 最大移动距离占Header视图的高度比
     *
     * @param ratio The max ratio of header view
     */
    @SuppressWarnings({"unused"})
    public void setCanMoveTheMaxRatioOfHeaderHeight(float ratio) {
        mIndicator.setCanMoveTheMaxRatioOfHeaderHeight(ratio);
    }

    @SuppressWarnings({"unused"})
    public float getCanMoveTheMaxRatioOfFooterHeight() {
        return mIndicator.getCanMoveTheMaxRatioOfFooterHeight();
    }

    /**
     * The max ratio of height for the footer view when the finger moves<br/>
     * <p>
     * 最大移动距离占Footer视图的高度比
     *
     * @param ratio The max ratio of footer view
     */
    @SuppressWarnings({"unused"})
    public void setCanMoveTheMaxRatioOfFooterHeight(float ratio) {
        mIndicator.setCanMoveTheMaxRatioOfFooterHeight(ratio);
    }

    /**
     * The flag has set to autoRefresh<br/>
     * <p>
     * 是否处于自动刷新刷新
     *
     * @return Enabled
     */
    public boolean isAutoRefresh() {
        return (mFlag & MASK_AUTO_REFRESH) > 0;
    }

    /**
     * If enable has been set to true. The user can perform next PTR at once.<br/>
     * <p>
     * 是否已经开启完成刷新后即可立即触发刷新
     *
     * @return Is enable
     */
    public boolean isEnabledNextPtrAtOnce() {
        return (mFlag & FLAG_ENABLE_NEXT_AT_ONCE) > 0;
    }

    /**
     * If @param enable has been set to true. The user can perform next PTR at once.<br/>
     * <p>
     * 设置开启完成刷新后即可立即触发刷新
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
     * The flag has set enabled overScroll<br/>
     * <p>
     * 是否已经开启越界回弹
     *
     * @return Enabled
     */
    public boolean isEnabledOverScroll() {
        return (mFlag & FLAG_ENABLE_OVER_SCROLL) > 0;
    }

    /**
     * If @param enable has been set to true. Will supports over scroll.<br/>
     * <p>
     * 设置开始越界回弹
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
     * The flag has set enabled to intercept the touch event while loading<br/>
     * <p>
     * 是否已经开启刷新中拦截消耗触摸事件
     *
     * @return Enabled
     */
    public boolean isEnabledInterceptEventWhileLoading() {
        return (mFlag & FLAG_ENABLE_INTERCEPT_EVENT_WHILE_LOADING) > 0;
    }

    /**
     * If @param enable has been set to true. Will intercept the touch event while loading<br/>
     * <p>
     * 开启刷新中拦截消耗触摸事件
     *
     * @param enable Enable
     */
    public void setEnabledInterceptEventWhileLoading(boolean enable) {
        if (enable) {
            mFlag = mFlag | FLAG_ENABLE_INTERCEPT_EVENT_WHILE_LOADING;
        } else {
            mFlag = mFlag & ~FLAG_ENABLE_INTERCEPT_EVENT_WHILE_LOADING;
        }
    }

    /**
     * The flag has been set to pull to refresh<br/>
     * <p>
     * 是否已经开启拉动刷新，下拉或者上拉到触发刷新位置即立即触发刷新
     *
     * @return Enabled
     */
    public boolean isEnabledPullToRefresh() {
        return (mFlag & FLAG_ENABLE_PULL_TO_REFRESH) > 0;
    }

    /**
     * If @param enable has been set to true. When the current pos >= refresh offsets, perform
     * refresh<br/>
     * <p>
     * 设置开启拉动刷新,下拉或者上拉到触发刷新位置即立即触发刷新
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
     * The flag has been set to enabled header drawerStyle<br/>
     * <p>
     * 是否已经开启Header的抽屉效果，即Header在Content下面且Header固定在顶部
     *
     * @return Enabled
     */
    public boolean isEnabledHeaderDrawerStyle() {
        return (mFlag & FLAG_ENABLE_HEADER_DRAWER_STYLE) > 0;
    }

    /**
     * If @param enable has been set to true.Enable header drawerStyle<br/>
     * <p>
     * 设置开启Header的抽屉效果，即Header在Content下面且Header固定在顶部.
     * 由于该效果需要改变层级关系，所以需要重新布局
     *
     * @param enable enable header drawerStyle
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
     * The flag has been set to enabled footer drawerStyle<br/>
     * <p>
     * 是否已经开启Footer的抽屉效果，即Footer在Content下面且Footer固定在底部
     *
     * @return Enabled
     */
    public boolean isEnabledFooterDrawerStyle() {
        return (mFlag & FLAG_ENABLE_FOOTER_DRAWER_STYLE) > 0;
    }

    /**
     * If @param enable has been set to true.Enable footer drawerStyle<br/>
     * <p>
     * 设置开启Footer的抽屉效果，即Footer在Content下面且Footer固定在底部.
     * 由于该效果需要改变层级关系，所以需要重新布局
     *
     * @param enable enable footer drawerStyle
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
     * The flag has been set to disabled perform refresh<br/>
     * <p>
     * 是否已经关闭触发下拉刷新
     *
     * @return Disabled
     */
    public boolean isDisabledPerformRefresh() {
        return (mFlag & MASK_DISABLE_PERFORM_REFRESH) > 0;
    }

    /**
     * If @param disable has been set to true.Will never perform refresh<br/>
     * <p>
     * 设置是否关闭触发下拉刷新
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
     * The flag has been set to disabled refresh<br/>
     * <p>
     * 是否已经关闭下拉刷新
     *
     * @return Disabled
     */
    public boolean isDisabledRefresh() {
        return (mFlag & FLAG_DISABLE_REFRESH) > 0;
    }

    /**
     * If @param disable has been set to true.Will disable refresh<br/>
     * <p>
     * 设置是否关闭下拉刷新
     *
     * @param disable Disable refresh
     */
    public void setDisableRefresh(boolean disable) {
        if (disable) {
            mFlag = mFlag | FLAG_DISABLE_REFRESH;
        } else {
            mFlag = mFlag & ~FLAG_DISABLE_REFRESH;
        }
        requestLayout();
    }


    /**
     * The flag has been set to disabled perform load more<br/>
     * <p>
     * 是否已经关闭触发加载更多
     *
     * @return Disabled
     */
    public boolean isDisabledPerformLoadMore() {
        return (mFlag & MASK_DISABLE_PERFORM_LOAD_MORE) > 0;
    }

    /**
     * If @param disable has been set to true.Will never perform load more<br/>
     * <p>
     * 设置是否关闭触发加载更多
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
     * The flag has been set to disabled load more<br/>
     * <p>
     * 是否已经关闭加载更多
     *
     * @return Disabled
     */
    public boolean isDisabledLoadMore() {
        return (mFlag & FLAG_DISABLE_LOAD_MORE) > 0;
    }

    /**
     * If @param disable has been set to true.Will disable load more<br/>
     * <p>
     * 设置关闭加载更多
     *
     * @param disable Disable load more
     */
    public void setDisableLoadMore(boolean disable) {
        if (disable) {
            mFlag = mFlag | FLAG_DISABLE_LOAD_MORE;
        } else {
            mFlag = mFlag & ~FLAG_DISABLE_LOAD_MORE;
        }
        requestLayout();
    }

    /**
     * The flag has been set to hided header view<br/>
     * <p>
     * 是否已经开启不显示Header
     *
     * @return hided
     */
    public boolean isEnabledHideHeaderView() {
        return (mFlag & FLAG_ENABLE_HIDE_HEADER_VIEW) > 0;
    }

    /**
     * If @param enable has been set to true.Will hide the header<br/>
     * <p>
     * 设置是否开启不显示Header
     *
     * @param enable Enable hide the header
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
     * The flag has been set to hided footer view<br/>
     * <p>
     * 是否已经开启不显示Footer
     *
     * @return hided
     */
    public boolean isEnabledHideFooterView() {
        return (mFlag & FLAG_ENABLE_HIDE_FOOTER_VIEW) > 0;
    }

    /**
     * If @param enable has been set to true.Will hide the footer<br/>
     * <p>
     * 设置是否开启不显示Footer
     *
     * @param enable Enable hide the footer
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
     * The flag has been set to disabled when horizontal move<br/>
     * <p>
     * 是否已经设置不响应横向滑动
     *
     * @return Disabled
     */
    public boolean isDisabledWhenHorizontalMove() {
        return (mFlag & FLAG_DISABLE_WHEN_HORIZONTAL_MOVE) > 0;
    }

    /**
     * Set whether to filter the horizontal move<br/>
     * <p>
     * 设置不响应横向滑动，当内部视图含有需要响应横向滑动的子视图时，需要设置该属性，否则自视图无法响应横向滑动
     *
     * @param disable Enable
     */
    public void setDisableWhenHorizontalMove(boolean disable) {
        if (disable) {
            mFlag = mFlag | FLAG_DISABLE_WHEN_HORIZONTAL_MOVE;
        } else {
            mFlag = mFlag & ~FLAG_DISABLE_WHEN_HORIZONTAL_MOVE;
        }
    }

    /**
     * The flag has been set to enabled load more has no more data<br/>
     * <p>
     * 是否已经开启加载更多完成已无更多数据，自定义Footer可根据该属性判断是否显示无更多数据的提示
     *
     * @return Enabled
     */
    public boolean isEnabledLoadMoreNoMoreData() {
        return (mFlag & FLAG_ENABLE_LOAD_MORE_NO_MORE_DATA) > 0;
    }

    /**
     * If @param enable has been set to true. The footer will show no more data and will never
     * trigger load more<br/>
     * <p>
     * 设置开启加载更多完成已无更多数据，当该属性设置为`true`时，将不再触发加载更多。
     *
     * @param enable Enable no more data
     */
    public void setEnableLoadMoreNoMoreData(boolean enable) {
        if (enable) {
            mFlag = mFlag | FLAG_ENABLE_LOAD_MORE_NO_MORE_DATA;
        } else {
            mFlag = mFlag & ~FLAG_ENABLE_LOAD_MORE_NO_MORE_DATA;
        }
    }

    /**
     * The flag has been set to keep refresh view while loading<br/>
     * <p>
     * 是否已经开启保持刷新视图
     *
     * @return Enabled
     */
    public boolean isEnabledKeepRefreshView() {
        return (mFlag & FLAG_ENABLE_KEEP_REFRESH_VIEW) > 0;
    }

    /**
     * If @param enable has been set to true.When the current pos> = keep refresh view pos,
     * it rolls back to the keep refresh view pos to perform refresh and remains until the refresh
     * completed<br/>
     * <p>
     * 开启刷新中保持刷新视图位置
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
     * The flag has been set to perform load more when the content view scrolling to bottom<br/>
     * <p>
     * 是否已经开启到底部自动加载更多
     *
     * @return Enabled
     */
    public boolean isEnabledScrollToBottomAutoLoadMore() {
        return (mFlag & FLAG_ENABLE_WHEN_SCROLLING_TO_BOTTOM_TO_PERFORM_LOAD_MORE) > 0;
    }

    /**
     * If @param enable has been set to true.When the content view scrolling to bottom,
     * It will be perform load more<br/>
     * <p>
     * 开启到底自动加载更多
     *
     * @param enable Enable
     */
    public void setEnableScrollToBottomAutoLoadMore(boolean enable) {
        if (enable) {
            mFlag = mFlag | FLAG_ENABLE_WHEN_SCROLLING_TO_BOTTOM_TO_PERFORM_LOAD_MORE;
        } else {
            mFlag = mFlag & ~FLAG_ENABLE_WHEN_SCROLLING_TO_BOTTOM_TO_PERFORM_LOAD_MORE;
        }
    }

    /**
     * The flag has been set to pinned refresh view while loading<br/>
     * <p>
     * 是否已经开启刷新过程中固定刷新视图且不响应触摸移动
     *
     * @return Enabled
     */
    public boolean isEnabledPinRefreshViewWhileLoading() {
        return (mFlag & FLAG_ENABLE_PIN_REFRESH_VIEW_WHILE_LOADING) > 0;
    }

    /**
     * If @param enable has been set to true.The refresh view will pinned at the refresh offset
     * position<br/>
     * <p>
     * 设置开启刷新过程中固定刷新视图且不响应触摸移动，该属性只有在{@link SmoothRefreshLayout#setEnablePinContentView(boolean)}
     * 和{@link SmoothRefreshLayout#setEnableKeepRefreshView(boolean)}2个属性都为`true`时才能生效
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
     * The flag has been set to pinned content view while loading<br/>
     * <p>
     * 是否已经开启了固定内容视图
     *
     * @return Enabled
     */
    public boolean isEnabledPinContentView() {
        return (mFlag & FLAG_ENABLE_PIN_CONTENT_VIEW) > 0;
    }

    /**
     * If @param enable has been set to true.The content view will be pinned in the start pos
     * unless overScroll flag has been set and in overScrolling<br/>
     * <p>
     * 设置开启固定内容视图
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
     * Set the footer view<br/>
     * <p>
     * 设置Footer
     *
     * @param footer Footer view
     */
    public void setFooterView(@NonNull IRefreshView footer) {
        if (mFooterView != null) {
            removeView(mFooterView.getView());
            mFooterView = null;
        }
        if (footer.getType() != IRefreshView.TYPE_FOOTER)
            throw new IllegalArgumentException("Wrong type,FooterView's type must be " +
                    "TYPE_FOOTER");
        View view = footer.getView();
        ViewGroup.LayoutParams lp = view.getLayoutParams();
        if (lp == null) {
            lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            view.setLayoutParams(lp);
        }
        mViewsZAxisNeedReset = true;
        addView(view);
    }

    /**
     * Set the header view<br/>
     * <p>
     * 设置Header
     *
     * @param header Header view
     */
    public void setHeaderView(@NonNull IRefreshView header) {
        if (mHeaderView != null) {
            if (mHeaderView instanceof MaterialHeader) {
                ((MaterialHeader) mHeaderView).release();
            }
            removeView(mHeaderView.getView());
            mHeaderView = null;
        }
        if (header.getType() != IRefreshView.TYPE_HEADER)
            throw new IllegalArgumentException("Wrong type,HeaderView's type must be " +
                    "TYPE_HEADER");
        View view = header.getView();
        ViewGroup.LayoutParams lp = view.getLayoutParams();
        if (lp == null) {
            lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            view.setLayoutParams(lp);
        }
        mViewsZAxisNeedReset = true;
        addView(view);
    }

    /**
     * Set the content view<br/>
     * <p>
     * 设置内容视图，`state`内容视图状态，`content`状态对应的视图
     *
     * @param state   The state of content view
     * @param content Content view
     */
    public void setContentView(@State int state, @NonNull View content) {
        switch (state) {
            case STATE_CONTENT:
                if (mContentView != null) {
                    removeView(mContentView);
                }
                mContentResId = View.NO_ID;
                mContentView = content;
                break;
            case STATE_EMPTY:
                if (mEmptyView != null) {
                    removeView(mEmptyView);
                }
                mEmptyLayoutResId = View.NO_ID;
                mEmptyView = content;
                break;
            case STATE_ERROR:
                if (mErrorView != null) {
                    removeView(mErrorView);
                }
                mErrorLayoutResId = View.NO_ID;
                mErrorView = content;
                break;
            case STATE_CUSTOM:
                if (mCustomView != null) {
                    removeView(mCustomView);
                }
                mCustomLayoutResId = View.NO_ID;
                mCustomView = content;
                break;
        }
        ViewGroup.LayoutParams lp = content.getLayoutParams();
        if (lp == null) {
            lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            content.setLayoutParams(lp);
        }
        if (mState != state) {
            content.setVisibility(GONE);
        }
        mViewsZAxisNeedReset = true;
        addView(content);
    }

    /**
     * Update scroller's interpolator<br/>
     * <p>
     * 设置Scroller的插值器
     *
     * @param interpolator Scroller's interpolator
     */
    public void updateScrollerInterpolator(Interpolator interpolator) {
        mScrollChecker.updateInterpolator(interpolator);
    }

    /**
     * Reset scroller's interpolator<br/>
     * <p>
     * 重置Scroller的插值器
     */
    public void resetScrollerInterpolator() {
        mScrollChecker.updateInterpolator(mDefaultSpringInterpolator);
    }

    /**
     * Is in over scrolling
     *
     * @return Is
     */
    public boolean isOverScrolling() {
        return mOverScrollChecker.mScrolling;
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
     * Returns the {@link View} associated with the {@link SmoothRefreshLayout.State}<br/>
     * <p>
     * 得到状态对应的内容视图
     *
     * @param state The view
     */
    public View getView(@State int state) {
        switch (state) {
            case STATE_CONTENT:
                ensureContentView();
                return mContentView;
            case STATE_ERROR:
                ensureErrorView();
                return mErrorView;
            case STATE_EMPTY:
                ensureEmptyView();
                return mEmptyView;
            case STATE_CUSTOM:
            default:
                ensureCustomView();
                return mCustomView;
        }
    }

    /**
     * Returns the current state<br/>
     * <p>
     * 获取当前的状态
     *
     * @return Current state
     */
    @State
    public int getState() {
        return mState;
    }

    /**
     * Set the current state<br/>
     * <p>
     * 设置当前的状态
     *
     * @param state Current state
     */
    public void setState(@State int state) {
        setState(state, false);
    }

    /**
     * Set the current state<br/>
     * <p>
     * 设置当前的状态，`state`状态，`animate`动画过渡
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
            if (animate) {
                mChangeStateAnimator = ObjectAnimator.ofFloat(1.0f, 0.0f).setDuration(250L);
                mChangeStateAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        currentView.setVisibility(View.VISIBLE);
                        currentView.setAlpha(0);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        previousView.setVisibility(View.GONE);
                        previousView.setAlpha(1);
                        currentView.setAlpha(1);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        previousView.setVisibility(View.GONE);
                        previousView.setAlpha(1);
                    }
                });
                mChangeStateAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float value = (float) animation.getAnimatedValue();
                        previousView.setAlpha(value);
                        currentView.setAlpha(1f - value);
                    }
                });
                mChangeStateAnimator.start();
            } else {
                previousView.setVisibility(GONE);
                currentView.setVisibility(VISIBLE);
            }
            mPreviousState = mState;
            mState = state;
            mTargetView = currentView;
            if (mStateChangedListener != null)
                mStateChangedListener.onStateChanged(state);
        }
    }

    // NestedScrollingParent

    @Override
    public void onFling(float vx, float vy) {
        if (!isEnabledOverScroll() || (isDisabledLoadMore() && isDisabledRefresh())
                || needInterceptTouchEvent() || mNestedScrollInProgress)
            return;
        if ((!canChildScrollUp() && vy > 0) || (!canChildScrollDown() && vy < 0)) {
            return;
        }
        if ((isRefreshing() && vy < 0) || (isLoadingMore() && vy > 0))
            return;
        if (isEnabledPinContentView()
                && ((isDisabledLoadMore() && vy < 0) || isDisabledRefresh() && vy > 0))
            return;
        if (isEnabledScrollToBottomAutoLoadMore() && !isDisabledPerformLoadMore() && vy < 0)
            vy = vy * 2;
        mOverScrollChecker.fling(vy, mGestureDetector.getFriction());
    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        if (sDebug) {
            SRLog.d(TAG, "onStartNestedScroll(): nestedScrollAxes: %s", nestedScrollAxes);
        }
        return isEnabled() && isNestedScrollingEnabled()
                && (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int axes) {
        if (sDebug) {
            SRLog.d(TAG, "onNestedScrollAccepted(): axes: %s", axes);
        }
        // Reset the counter of how much leftover scroll needs to be consumed.
        mNestedScrollingParentHelper.onNestedScrollAccepted(child, target, axes);
        mIndicator.onFingerDown();
        // Dispatch up to the nested parent
        startNestedScroll(axes & ViewCompat.SCROLL_AXIS_VERTICAL);
        mTotalRefreshingUnconsumed = 0;
        mTotalLoadMoreUnconsumed = 0;
        mNestedScrollInProgress = true;
        if (!needInterceptTouchEvent())
            mScrollChecker.abortIfWorking();
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        if (sDebug) {
            SRLog.d(TAG, "onNestedPreScroll(): dx: %s, dy: %s, consumed: %s",
                    dx, dy, Arrays.toString(consumed));
        }
        if (needInterceptTouchEvent()) {
            consumed[1] = dy;
            onNestedPreScroll(dx, dy, consumed);
            return;
        }
        if (mScrollChecker.mIsRunning) {
            onNestedPreScroll(dx, dy, consumed);
            return;
        }
        if (!mIndicator.hasTouched()) {
            if (sDebug) {
                SRLog.w(TAG, "onNestedPreScroll(): There was an exception in touch event handling，" +
                        "This method should be performed after the onNestedScrollAccepted() " +
                        "method is called");
            }
            onNestedPreScroll(dx, dy, consumed);
            return;
        }
        if (dy > 0 && mTotalRefreshingUnconsumed >= 0
                && !isDisabledRefresh()
                && !(isEnabledPinRefreshViewWhileLoading() && isRefreshing())
                && !mOverScrollChecker.mScrolling
                && (isMovingHeader() || isMovingContent() && mTotalRefreshingUnconsumed == 0)) {
            if (mTotalRefreshingUnconsumed == 0 && mStatus == SR_STATUS_REFRESHING
                    && mTotalRefreshingConsumed / mIndicator.getResistanceOfPullUp()
                    < mIndicator.getHeaderHeight()) {
                mTotalRefreshingConsumed += dy;
                mIndicator.onFingerMove(mIndicator.getLastMovePoint()[0] - dx,
                        mIndicator.getLastMovePoint()[1] - dy);
                moveHeaderPos(mIndicator.getOffsetY());
                consumed[1] = dy;
            } else if (mTotalRefreshingUnconsumed != 0) {
                mTotalRefreshingUnconsumed -= dy;

                if (mTotalRefreshingUnconsumed <= 0) {//over
                    mTotalRefreshingUnconsumed = 0;
                }
                mIndicator.onFingerMove(mIndicator.getLastMovePoint()[0] - dx,
                        mIndicator.getLastMovePoint()[1] - dy);
                moveHeaderPos(mIndicator.getOffsetY());
                consumed[1] = dy;
            } else if (!mIndicator.isInStartPosition()) {
                mIndicator.onFingerMove(mIndicator.getLastMovePoint()[0] - dx,
                        mIndicator.getLastMovePoint()[1] - dy);
                moveHeaderPos(mIndicator.getOffsetY());
                consumed[1] = dy;
            } else {
                mIndicator.onFingerMove(mIndicator.getLastMovePoint()[0] - dx,
                        mIndicator.getLastMovePoint()[1]);
            }
        }
        if (dy < 0 && mTotalLoadMoreUnconsumed >= 0
                && !isDisabledLoadMore()
                && !(isEnabledPinRefreshViewWhileLoading() && isLoadingMore())
                && !mOverScrollChecker.mScrolling) {
            if (mStatus == SR_STATUS_LOADING_MORE && mTotalLoadMoreUnconsumed == 0
                    && mTotalLoadMoreConsumed / mIndicator.getResistanceOfPullDown()
                    < mIndicator.getFooterHeight()
                    && (isMovingFooter() || isMovingContent())) {
                mTotalLoadMoreConsumed += Math.abs(dy);
                mIndicator.onFingerMove(mIndicator.getLastMovePoint()[0] - dx,
                        mIndicator.getLastMovePoint()[1] - dy);
                moveFooterPos(mIndicator.getOffsetY());
                consumed[1] = dy;
            } else if (mTotalLoadMoreUnconsumed != 0) {
                mTotalLoadMoreUnconsumed += dy;
                if (mTotalLoadMoreUnconsumed <= 0) {//over
                    mTotalLoadMoreUnconsumed = 0;
                }
                mIndicator.onFingerMove(mIndicator.getLastMovePoint()[0] - dx,
                        mIndicator.getLastMovePoint()[1] - dy);
                moveFooterPos(mIndicator.getOffsetY());
                consumed[1] = dy;
            } else if (!mIndicator.isInStartPosition() && isMovingFooter()) {
                mIndicator.onFingerMove(mIndicator.getLastMovePoint()[0] - dx,
                        mIndicator.getLastMovePoint()[1] - dy);
                moveFooterPos(mIndicator.getOffsetY());
                consumed[1] = dy;
            } else {
                mIndicator.onFingerMove(mIndicator.getLastMovePoint()[0] - dx,
                        mIndicator.getLastMovePoint()[1]);
            }
        }
        if (dy == 0) {
            mIndicator.onFingerMove(mIndicator.getLastMovePoint()[0] - dx,
                    mIndicator.getLastMovePoint()[1]);
            updateXPos();
        }
        if (isMovingFooter() && mIndicator.hasLeftStartPosition()
                && mStatus == SR_STATUS_COMPLETE) {
            mScrollChecker.tryToScrollTo(IIndicator.DEFAULT_START_POS, 0);
        }
        onNestedPreScroll(dx, dy, consumed);
    }

    private void onNestedPreScroll(int dx, int dy, int[] consumed) {
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
            mIndicator.onFingerUp();
        }
        mGestureDetector.onDetached();
        mNestedScrollInProgress = false;
        mNestedFling = false;
        mTotalRefreshingUnconsumed = 0;
        mTotalRefreshingConsumed = 0;
        mTotalLoadMoreUnconsumed = 0;
        mTotalLoadMoreConsumed = 0;
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
        if (needInterceptTouchEvent())
            return;
        // Dispatch up to the nested parent first
        dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed,
                mParentOffsetInWindow);
        if (mScrollChecker.mIsRunning)
            return;
        if (!mIndicator.hasTouched()) {
            if (sDebug) {
                SRLog.w(TAG, "onNestedScroll(): There was an exception in touch event handling，" +
                        "This method should be performed after the onNestedScrollAccepted() " +
                        "method is called");
            }
            return;
        }
        final int dy = dyUnconsumed + mParentOffsetInWindow[1];
        if (dy < 0 && !isDisabledRefresh() && !canChildScrollUp()
                && (isMovingHeader() || isMovingContent())
                && !(isEnabledPinRefreshViewWhileLoading() && isRefreshing())) {
            float distance = mIndicator.getCanMoveTheMaxDistanceOfHeader();
            if (distance > 0 && mIndicator.getCurrentPosY() >= distance)
                return;
            mTotalRefreshingUnconsumed += Math.abs(dy);
            mIndicator.onFingerMove(mIndicator.getLastMovePoint()[0],
                    mIndicator.getLastMovePoint()[1] - dy);
            if (distance > 0 && (mIndicator.getCurrentPosY() + mIndicator.getOffsetY() > distance))
                moveHeaderPos(distance - mIndicator.getCurrentPosY());
            else
                moveHeaderPos(mIndicator.getOffsetY());
        } else if (dy > 0 && !isDisabledLoadMore() && !canChildScrollDown()
                && (isMovingFooter() || mTotalLoadMoreUnconsumed == 0 && isMovingContent())
                && !(isEnabledPinRefreshViewWhileLoading() && isLoadingMore())) {
            float distance = mIndicator.getCanMoveTheMaxDistanceOfFooter();
            if (distance > 0 && mIndicator.getCurrentPosY() > distance)
                return;
            mTotalLoadMoreUnconsumed += dy;
            mIndicator.onFingerMove(mIndicator.getLastMovePoint()[0],
                    mIndicator.getLastMovePoint()[1] - dy);
            if (distance > 0 && (mIndicator.getCurrentPosY() - mIndicator.getOffsetY() > distance))
                moveFooterPos(mIndicator.getCurrentPosY() - distance);
            else
                moveFooterPos(mIndicator.getOffsetY());
        }
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return mNestedScrollingChildHelper.isNestedScrollingEnabled();
    }

    // NestedScrollingChild
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
        mNestedFling = false;
        if (sDebug) {
            SRLog.d(TAG, "onNestedPreFling(): velocityX: %s, velocityY: %s", velocityX, velocityY);
        }
        if (needInterceptTouchEvent())
            return true;
        //When the content view can not scroll up, ignore the fling to scroll down.
        if ((!canChildScrollUp() && velocityY < 0) ||
                //When the content view can not scroll down, ignore the fling to scroll up.
                (!canChildScrollDown() && velocityY > 0))
            return dispatchNestedPreFling(velocityX, velocityY);
        if (isEnabledOverScroll()) {
            mNestedFling = Math.abs(velocityY) > 500;
            //IF is fling to scroll down and is loading more, ignore the fling.
            //Or is fling to scroll up and is refreshing, ignore the fling.
            if ((velocityY > 0 && isRefreshing()) || (velocityY < 0 && isLoadingMore()))
                return dispatchNestedPreFling(velocityX, velocityY);
            float vy = -velocityY;
            if (isEnabledPinContentView()
                    && ((isDisabledLoadMore() && vy < 0) || (isDisabledRefresh() && vy > 0)))
                return dispatchNestedPreFling(velocityX, velocityY);
            if (sDebug) {
                SRLog.d(TAG, "onNestedPreFling(): newVelocityY: %s", vy);
            }
            if (isEnabledScrollToBottomAutoLoadMore() && !isDisabledPerformLoadMore() && vy < 0)
                vy = vy * 4;
            mOverScrollChecker.fling(vy, mGestureDetector.getFriction());
        }
        return dispatchNestedPreFling(velocityX, velocityY);
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
        if (isEnabledScrollToBottomAutoLoadMore() && !isDisabledPerformLoadMore()
                && (mStatus == SR_STATUS_INIT || mStatus == SR_STATUS_PREPARE)) {
            if ((mAutoLoadMoreCallBack == null && ScrollCompat.canAutoLoadMore(mTargetView))
                    || (mAutoLoadMoreCallBack != null && mAutoLoadMoreCallBack
                    .canAutoLoadMore(this, mTargetView))) {
                mStatus = SR_STATUS_LOADING_MORE;
                mDelayedRefreshComplete = false;
                performRefresh();
            }
        }
        mOverScrollChecker.computeScrollOffset();
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
                    if (view != mHeaderView && view != mFooterView)
                        mCachedViews.add(view);
                }
            } else if (isEnabledHeaderDrawer) {
                for (int i = count - 1; i >= 0; i--) {
                    View view = getChildAt(i);
                    if (view != mHeaderView)
                        mCachedViews.add(view);
                }
            } else if (isEnabledFooterDrawer) {
                for (int i = count - 1; i >= 0; i--) {
                    View view = getChildAt(i);
                    if (view != mFooterView)
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
        if (mUIPositionChangedListeners != null)
            mUIPositionChangedListeners.clear();
        if (sDebug) {
            SRLog.i(TAG, "destroy()");
        }
    }

    protected void reset() {
        if (!mIndicator.isInStartPosition()) {
            mScrollChecker.tryToScrollTo(IIndicator.DEFAULT_START_POS, 0);
        }
        if (!tryToNotifyReset()) {
            mScrollChecker.destroy();
            mOverScrollChecker.destroy();
        }
        mGestureDetector.onDetached();
        removeCallbacks(mScrollChecker);
        removeCallbacks(mOverScrollChecker);
        if (mDelayToRefreshComplete != null)
            removeCallbacks(mDelayToRefreshComplete);
        if (mHeaderRefreshCompleteHook != null)
            mHeaderRefreshCompleteHook.mLayoutWeakRf.clear();
        if (mFooterRefreshCompleteHook != null)
            mFooterRefreshCompleteHook.mLayoutWeakRf.clear();
        if (mChangeStateAnimator != null && mChangeStateAnimator.isRunning())
            mChangeStateAnimator.cancel();
        if (getHandler() != null)
            getHandler().removeCallbacksAndMessages(null);
        if (sDebug) {
            SRLog.i(TAG, "reset()");
        }
    }

    protected void tryToPerformAutoRefresh() {
        if (isAutoRefresh()) {
            if (sDebug) {
                SRLog.d(TAG, "tryToPerformAutoRefresh()");
            }
            if (!mTriggeredAutoRefresh) {
                if (mHeaderView == null || mIndicator.getHeaderHeight() <= 0)
                    return;
                mTriggeredAutoRefresh = true;
                mIndicator.setMovingStatus(IIndicator.MOVING_HEADER);
                mScrollChecker.tryToScrollTo(mIndicator.getOffsetToRefresh(),
                        mAutoRefreshUseSmoothScroll ? mDurationToCloseHeader : 0);
            } else if (!mTriggeredAutoLoadMore) {
                if (mFooterView == null || mIndicator.getFooterHeight() <= 0)
                    return;
                mTriggeredAutoLoadMore = true;
                mIndicator.setMovingStatus(IIndicator.MOVING_FOOTER);
                mScrollChecker.tryToScrollTo(mIndicator.getOffsetToLoadMore(),
                        mAutoRefreshUseSmoothScroll ? mDurationToCloseFooter : 0);
            }
        }
    }

    protected void ensureFreshView(View child) {
        if (child instanceof IRefreshView) {
            IRefreshView view = (IRefreshView) child;
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
                    if (!(child instanceof IRefreshView) && mContentResId == child.getId())
                        mContentView = child;
                }
            } else {
                for (int i = getChildCount() - 1; i >= 0; i--) {
                    View child = getChildAt(i);
                    if ((mEmptyView != null && child == mEmptyView)
                            || (mErrorView != null && child == mErrorView)
                            || (mCustomView != null && child == mCustomView))
                        continue;
                    if (child.getVisibility() != GONE && !(child instanceof IRefreshView))
                        mContentView = child;
                }
            }
        }
    }

    private void ensureErrorView() {
        if (mErrorView == null && mErrorLayoutResId != NO_ID) {
            mErrorView = mInflater.inflate(mErrorLayoutResId, null, false);
            generateStateViewLayoutParams(mErrorView);
            addView(mErrorView);
        } else if (mErrorView == null)
            throw new IllegalArgumentException("Error view must be not null");
    }

    private void ensureEmptyView() {
        if (mEmptyView == null && mEmptyLayoutResId != NO_ID) {
            mEmptyView = mInflater.inflate(mEmptyLayoutResId, null, false);
            generateStateViewLayoutParams(mEmptyView);
            addView(mEmptyView);
        } else if (mEmptyView == null)
            throw new IllegalArgumentException("Empty view must be not null");
    }


    private void ensureCustomView() {
        if (mCustomView == null && mCustomLayoutResId != NO_ID) {
            mCustomView = mInflater.inflate(mCustomLayoutResId, null, false);
            generateStateViewLayoutParams(mCustomView);
            addView(mCustomView);
        } else if (mCustomView == null)
            throw new IllegalArgumentException("Custom view must be not null");
    }


    private void generateStateViewLayoutParams(View view) {
        ViewGroup.LayoutParams lp = view.getLayoutParams();
        if (lp == null) {
            lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            view.setLayoutParams(lp);
        }
    }

    private void ensureTargetView() {
        if (mTargetView == null) {
            switch (mState) {
                case STATE_CONTENT:
                    ensureContentView();
                    mTargetView = mContentView;
                    break;
                case STATE_EMPTY:
                    ensureEmptyView();
                    mTargetView = mEmptyView;
                    break;
                case STATE_ERROR:
                    ensureErrorView();
                    mTargetView = mErrorView;
                    break;
                case STATE_CUSTOM:
                default:
                    ensureCustomView();
                    mTargetView = mCustomView;
                    break;
            }
        }
        if (mTargetView == null) {
            throw new RuntimeException("The content view is empty." +
                    " Do you forget to added it in the XML layout file or add it in code ?");
        }
        ViewTreeObserver observer = mTargetView.getViewTreeObserver();
        if (observer != mTargetViewTreeObserver && observer.isAlive()) {
            if (mTargetViewTreeObserver != null && mTargetViewTreeObserver.isAlive())
                mTargetViewTreeObserver.removeOnScrollChangedListener(this);
            mTargetViewTreeObserver = observer;
            mTargetViewTreeObserver.addOnScrollChangedListener(this);
        }
        //Use the static default creator to create the header view
        if (!isDisabledRefresh() && mHeaderView == null && sCreator != null) {
            sCreator.createHeader(this);
        }
        //Use the static default creator to create the footer view
        if (!isDisabledLoadMore() && mFooterView == null && sCreator != null) {
            sCreator.createFooter(this);
        }
    }

    protected boolean processDispatchTouchEvent(MotionEvent ev) {
        mGestureDetector.onTouchEvent(ev);
        final int action = MotionEventCompat.getActionMasked(ev);
        if (sDebug) {
            SRLog.d(TAG, "processDispatchTouchEvent(): action: %s", action);
        }
        switch (action) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mPreventForHorizontal = false;
                mDealHorizontalMove = false;
                mIndicator.onFingerUp();
                if (needInterceptTouchEvent()) {
                    float offsetX, offsetY;
                    float[] pressDownPoint = mIndicator.getFingerDownPoint();
                    offsetX = ev.getX() - pressDownPoint[0];
                    offsetY = ev.getY() - pressDownPoint[1];
                    if ((Math.abs(offsetX) > mTouchSlop || Math.abs(offsetY) > mTouchSlop)) {
                        sendCancelEvent();
                        return true;
                    } else {
                        return super.dispatchTouchEvent(ev);
                    }
                } else {
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
                final int actionIndex = MotionEventCompat.getActionIndex(ev);
                if (ev.getPointerId(actionIndex) == mTouchPointerId) {
                    // Pick a new pointer to pick up the slack.
                    final int newIndex = actionIndex == 0 ? 1 : 0;
                    mTouchPointerId = ev.getPointerId(newIndex);
                    mIndicator.onFingerMove(ev.getX(newIndex), ev.getY(newIndex));
                }
                break;
            case MotionEvent.ACTION_DOWN:
                mTouchPointerId = ev.getPointerId(0);
                mIndicator.onFingerDown(ev.getX(), ev.getY());
                if (!needInterceptTouchEvent()) {
                    mHasSendCancelEvent = false;
                    mHasSendDownEvent = true;
                    boolean movingFooter = isMovingFooter();
                    boolean hasLeftStartPosition = mIndicator.hasLeftStartPosition();
                    if ((!movingFooter && hasLeftStartPosition) || mStatus != SR_STATUS_COMPLETE) {
                        mScrollChecker.abortIfWorking();
                    } else {
                        mScrollChecker.tryToScrollTo(IIndicator.DEFAULT_START_POS, 0);
                    }
                } else {
                    mHasSendCancelEvent = true;
                    mHasSendDownEvent = false;
                }
                mPreventForHorizontal = false;
                mDealHorizontalMove = false;
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
                mIndicator.onFingerMove(ev.getX(index), ev.getY(index));
                float offsetX, offsetY;
                float[] pressDownPoint = mIndicator.getFingerDownPoint();
                offsetX = ev.getX(index) - pressDownPoint[0];
                offsetY = ev.getY(index) - pressDownPoint[1];
                if (isDisabledWhenHorizontalMove()) {
                    boolean needProcess = (mIndicator.isInStartPosition()
                            || (isRefreshing() && !isEnabledPullToRefresh()
                            && mIndicator.isInKeepHeaderWhileLoadingPos())
                            || (isLoadingMore() && !isEnabledPullToRefresh()
                            && mIndicator.isInKeepFooterWhileLoadingPos()));
                    if (!mDealHorizontalMove && needProcess) {
                        if ((Math.abs(offsetX) >= mTouchSlop
                                && Math.abs(offsetX * 1.1f) > Math.abs(offsetY))) {
                            mPreventForHorizontal = true;
                            mDealHorizontalMove = true;
                        } else if (Math.abs(offsetX) < mTouchSlop
                                && Math.abs(offsetY) < mTouchSlop) {
                            mDealHorizontalMove = false;
                            mPreventForHorizontal = true;
                        } else {
                            mDealHorizontalMove = true;
                            mPreventForHorizontal = false;
                        }
                    }
                } else {
                    if (Math.abs(offsetX) < mTouchSlop
                            && Math.abs(offsetY) < mTouchSlop) {
                        return super.dispatchTouchEvent(ev);
                    }
                }
                if (mPreventForHorizontal) {
                    return super.dispatchTouchEvent(ev);
                }
                if (needInterceptTouchEvent()) {
                    mHasSendDownEvent = !(Math.abs(offsetX) > mTouchSlop
                            || Math.abs(offsetY) > mTouchSlop);
                    if (mOverScrollChecker.mScrolling && !mScrollChecker.mIsRunning) {
                        if (mIndicator.isInStartPosition()) {
                            mOverScrollChecker.destroy();
                        } else {
                            mScrollChecker.tryToScrollTo(IIndicator.DEFAULT_START_POS, 0);
                        }
                    }
                    return true;
                } else if (!mHasSendDownEvent) {
                    sendCancelEvent();
                    sendDownEvent();
                }
                if (isMovingFooter() && mIndicator.hasLeftStartPosition()
                        && mStatus == SR_STATUS_COMPLETE) {
                    mScrollChecker.tryToScrollTo(IIndicator.DEFAULT_START_POS, 0);
                    return super.dispatchTouchEvent(ev);
                }
                offsetY = mIndicator.getOffsetY();
                int currentY = mIndicator.getCurrentPosY();
                boolean movingDown = offsetY > 0;
                float maxHeaderDistance = mIndicator.getCanMoveTheMaxDistanceOfHeader();
                if (movingDown && isMovingHeader() && !mIndicator.isInStartPosition()
                        && maxHeaderDistance > 0) {
                    if (currentY >= maxHeaderDistance) {
                        updateXPos();
                        return super.dispatchTouchEvent(ev);
                    } else if (currentY + offsetY > maxHeaderDistance) {
                        moveHeaderPos(maxHeaderDistance - currentY);
                        return true;
                    }
                }
                float maxFooterDistance = mIndicator.getCanMoveTheMaxDistanceOfFooter();
                if (!movingDown && isMovingFooter() && !mIndicator.isInStartPosition()
                        && maxFooterDistance > 0) {
                    if (currentY >= maxFooterDistance) {
                        updateXPos();
                        return super.dispatchTouchEvent(ev);
                    } else if (currentY - offsetY > maxFooterDistance) {
                        moveFooterPos(currentY - maxFooterDistance);
                        return true;
                    }
                }
                boolean canMoveUp = isMovingHeader() && mIndicator.hasLeftStartPosition();
                boolean canMoveDown = isMovingFooter() && mIndicator.hasLeftStartPosition();
                boolean canHeaderMoveDown = !canChildScrollUp() && !isDisabledRefresh();
                boolean canFooterMoveUp = !canChildScrollDown() && !isDisabledLoadMore();
                if (!canMoveUp && !canMoveDown) {
                    if (movingDown && !canHeaderMoveDown) {
                        if (isLoadingMore() && mIndicator.hasLeftStartPosition()) {
                            moveFooterPos(offsetY);
                            return true;
                        } else if (isRefreshing() && mIndicator.hasLeftStartPosition()) {
                            moveHeaderPos(offsetY);
                            return true;
                        }
                        return super.dispatchTouchEvent(ev);
                    }
                    if (!movingDown && !canFooterMoveUp) {
                        if (isLoadingMore() && mIndicator.hasLeftStartPosition()) {
                            moveFooterPos(offsetY);
                            return true;
                        } else if (isRefreshing() && mIndicator.hasLeftStartPosition()) {
                            moveHeaderPos(offsetY);
                            return true;
                        }
                        return super.dispatchTouchEvent(ev);
                    }
                    // should show up header
                    if (movingDown) {
                        if (isLoadingMore() || isDisabledRefresh())
                            return super.dispatchTouchEvent(ev);
                        moveHeaderPos(offsetY);
                        return true;
                    }
                    if (isRefreshing() || isDisabledLoadMore())
                        return super.dispatchTouchEvent(ev);
                    moveFooterPos(offsetY);
                    return true;
                }
                if (canMoveUp) {
                    if (isLoadingMore() || isDisabledRefresh() || !canHeaderMoveDown)
                        return super.dispatchTouchEvent(ev);
                    moveHeaderPos(offsetY);
                    return true;
                }
                if (mStatus == SR_STATUS_COMPLETE) {
                    return super.dispatchTouchEvent(ev);
                } else {
                    if (isRefreshing() || isDisabledLoadMore() || !canFooterMoveUp)
                        return super.dispatchTouchEvent(ev);
                    moveFooterPos(offsetY);
                    return true;
                }
        }
        return super.dispatchTouchEvent(ev);
    }

    protected boolean needInterceptTouchEvent() {
        return (isEnabledInterceptEventWhileLoading() && (isRefreshing() || isLoadingMore()))
                || mChangeStateAnimator != null && mChangeStateAnimator.isRunning()
                || (isAutoRefresh() && mScrollChecker.mIsRunning)
                || (mOverScrollChecker.mScrolling
                && (((isMovingHeader() && isDisabledRefresh()))
                || (isMovingFooter() && isDisabledLoadMore())));
    }

    protected boolean canChildScrollUp() {
        if (mScrollUpCallback != null)
            return mScrollUpCallback.canChildScrollUp(this, mTargetView, mHeaderView);
        return ScrollCompat.canChildScrollUp(mTargetView);
    }

    protected boolean canChildScrollDown() {
        if (mScrollDownCallback != null)
            return mScrollDownCallback.canChildScrollDown(this, mTargetView, mFooterView);
        return ScrollCompat.canChildScrollDown(mTargetView);
    }

    private void sendCancelEvent() {
        if (sDebug) {
            SRLog.i(TAG, "sendCancelEvent()");
        }
        if (mLastMoveEvent == null) return;
        mHasSendCancelEvent = true;
        final MotionEvent last = mLastMoveEvent;
        MotionEvent e = MotionEvent.obtain(last.getDownTime(), last.getEventTime() +
                        ViewConfiguration.getLongPressTimeout(),
                MotionEvent.ACTION_CANCEL, last.getX(), last.getY(), last.getMetaState());
        super.dispatchTouchEvent(e);
    }

    private void sendDownEvent() {
        if (sDebug) {
            SRLog.i(TAG, "sendDownEvent()");
        }
        if (mLastMoveEvent == null) return;
        mHasSendDownEvent = true;
        final MotionEvent last = mLastMoveEvent;
        MotionEvent e = MotionEvent.obtain(last.getDownTime(), last.getEventTime(),
                MotionEvent.ACTION_DOWN, last.getX(), last.getY(), last.getMetaState());
        super.dispatchTouchEvent(e);
    }

    private void notifyFingerUp() {
        if (sDebug) {
            SRLog.i(TAG, "notifyFingerUp()");
        }
        if (isMovingHeader() && mHeaderView != null && !isDisabledRefresh()) {
            mHeaderView.onFingerUp(this, mIndicator);
        } else if (isMovingFooter() && mFooterView != null && !isDisabledLoadMore()) {
            mFooterView.onFingerUp(this, mIndicator);
        }
    }

    protected void onFingerUp(boolean stayForLoading) {
        if (sDebug) {
            SRLog.d(TAG, "onFingerUp(): stayForLoading: %s", stayForLoading);
        }
        notifyFingerUp();
        if (mOverScrollChecker.mScrolling)
            return;
        if (!stayForLoading && isEnabledKeepRefreshView() && mStatus != SR_STATUS_COMPLETE
                && !isRefreshing() && !isLoadingMore()) {
            if (isMovingHeader() && !isDisabledRefresh() && mIndicator
                    .isOverOffsetToKeepHeaderWhileLoading()) {
                if (mIndicator.isAlreadyHere(mIndicator.getOffsetToKeepHeaderWhileLoading())
                        || isDisabledPerformRefresh()) {
                    onRelease(0);
                    return;
                }
                mScrollChecker.tryToScrollTo(mIndicator.getOffsetToKeepHeaderWhileLoading(),
                        mDurationOfBackToHeaderHeight);
            } else if (isMovingFooter() && !isDisabledLoadMore() && mIndicator
                    .isOverOffsetToKeepFooterWhileLoading()) {
                if (mIndicator.isAlreadyHere(mIndicator.getOffsetToKeepFooterWhileLoading())
                        || isDisabledPerformLoadMore()) {
                    onRelease(0);
                    return;
                }
                mScrollChecker.tryToScrollTo(mIndicator.getOffsetToKeepFooterWhileLoading(),
                        mDurationOfBackToFooterHeight);
            } else {
                onRelease(0);
            }
        } else {
            onRelease(0);
        }
    }

    protected void onRelease(int duration) {
        if (sDebug) {
            SRLog.d(TAG, "onRelease(): duration: %s", duration);
        }
        tryToPerformRefresh();
        if (mStatus == SR_STATUS_REFRESHING || mStatus == SR_STATUS_LOADING_MORE) {
            if (isEnabledKeepRefreshView()) {
                if (isRefreshing() && mIndicator.isOverOffsetToKeepHeaderWhileLoading()) {
                    mScrollChecker.tryToScrollTo(mIndicator.getOffsetToKeepHeaderWhileLoading(),
                            mDurationOfBackToHeaderHeight);
                } else if (isLoadingMore() && mIndicator.isOverOffsetToKeepFooterWhileLoading()) {
                    mScrollChecker.tryToScrollTo(mIndicator.getOffsetToKeepFooterWhileLoading(),
                            mDurationOfBackToFooterHeight);
                } else if (mNestedScrollInProgress && mNestedFling) {
                    tryScrollBackToTopByPercentDuration(duration);
                }
            } else {
                tryScrollBackToTopByPercentDuration(duration);
            }
        } else if (mStatus == SR_STATUS_COMPLETE) {
            notifyUIRefreshComplete(true);
        } else {
            tryScrollBackToTopByPercentDuration(duration);
        }
    }

    protected void tryScrollBackToTopByPercentDuration(int duration) {
        //Use the current percentage duration of the current position to scroll back to the top
        float percent;
        if (isMovingHeader()) {
            percent = mIndicator.getCurrentPercentOfHeader();
            percent = percent > 1 || percent <= 0 ? 1 : percent;
            tryScrollBackToTop(duration > 0 ? duration : Math.round(mDurationToCloseHeader * percent));
        } else if (isMovingFooter()) {
            percent = mIndicator.getCurrentPercentOfFooter();
            percent = percent > 1 || percent <= 0 ? 1 : percent;
            tryScrollBackToTop(duration > 0 ? duration : Math.round(mDurationToCloseFooter * percent));
        } else {
            tryScrollBackToTop(duration);
        }
    }

    protected void tryScrollBackToTop(int duration) {
        if (sDebug) {
            SRLog.d(TAG, "tryScrollBackToTop(): duration: %s", duration);
        }
        if (!mIndicator.hasTouched() && mIndicator.hasLeftStartPosition()) {
            mScrollChecker.tryToScrollTo(IIndicator.DEFAULT_START_POS, duration);
            return;
        }
        if (needInterceptTouchEvent() && mIndicator.hasLeftStartPosition()) {
            mScrollChecker.tryToScrollTo(IIndicator.DEFAULT_START_POS, duration);
            return;
        }
        if (isMovingFooter() && mStatus == SR_STATUS_COMPLETE
                && mIndicator.hasJustBackToStartPosition()) {
            mScrollChecker.tryToScrollTo(IIndicator.DEFAULT_START_POS, duration);
        }
    }

    protected void notifyUIRefreshComplete(boolean scroll) {
        if (sDebug) {
            SRLog.i(TAG, "notifyUIRefreshComplete()");
        }
        mIndicator.onRefreshComplete();
        if (mNeedNotifyRefreshComplete) {
            if (isMovingHeader() && mHeaderView != null) {
                mHeaderView.onRefreshComplete(this, mIsLastRefreshSuccessful);
            } else if (isMovingFooter() && mFooterView != null) {
                mFooterView.onRefreshComplete(this, mIsLastRefreshSuccessful);
            }
            if (mRefreshListener != null) {
                mRefreshListener.onRefreshComplete(mIsLastRefreshSuccessful);
            }
            mNeedNotifyRefreshComplete = false;
        } else if (mDelayedRefreshComplete) {
            if (mRefreshListener != null) {
                mRefreshListener.onRefreshComplete(mIsLastRefreshSuccessful);
            }
        }
        if (scroll)
            tryScrollBackToTopByPercentDuration(0);
        tryToNotifyReset();
    }

    protected void moveHeaderPos(float deltaY) {
        if (sDebug) {
            SRLog.d(TAG, "moveHeaderPos(): deltaY: %s", deltaY);
        }
        mIndicator.setMovingStatus(IIndicator.MOVING_HEADER);
        // to keep the consistence with refresh, need to converse the deltaY
        movePos(deltaY);
    }

    protected void moveFooterPos(float deltaY) {
        if (sDebug) {
            SRLog.d(TAG, "moveFooterPos(): deltaY: %s", deltaY);
        }
        mIndicator.setMovingStatus(IIndicator.MOVING_FOOTER);
        //check if it is needed to compatible scroll
        if (!isEnabledPinContentView() && mIsLastRefreshSuccessful
                && (mStatus == SR_STATUS_COMPLETE
                || (isEnabledNextPtrAtOnce() && mStatus == SR_STATUS_PREPARE
                && !mOverScrollChecker.mScrolling
                && !mIndicator.hasTouched()))) {
            if (sDebug) {
                SRLog.d(TAG, "moveFooterPos(): compatible scroll deltaY: %s", deltaY);
            }
            if (mLoadMoreScrollCallback == null) {
                if (mLoadMoreScrollTargetView != null)
                    ScrollCompat.scrollCompat(mLoadMoreScrollTargetView, deltaY);
                else
                    ScrollCompat.scrollCompat(mTargetView, deltaY);
            } else {
                if (!mLoadMoreScrollCallback.onScroll(mTargetView, deltaY))
                    ScrollCompat.scrollCompat(mTargetView, deltaY);
            }
        }
        // to keep the consistence with refresh, need to converse the deltaY
        movePos(-deltaY);
    }

    protected void movePos(float deltaY) {
        // has reached the top
        if (deltaY < 0 && mIndicator.isInStartPosition()) {
            if (sDebug) {
                SRLog.d(TAG, "movePos(): has reached the top");
            }
            return;
        }
        int to = mIndicator.getCurrentPosY() + (int) deltaY;
        // over top
        if (mIndicator.willOverTop(to)) {
            to = IIndicator.DEFAULT_START_POS;
            if (sDebug) {
                SRLog.d(TAG, "movePos(): over top");
            }
        }
        mIndicator.setCurrentPos(to);
        int change = to - mIndicator.getLastPosY();
        if (isMovingHeader())
            updateYPos(change);
        else if (isMovingFooter())
            updateYPos(-change);
    }

    /**
     * Update view's Y position
     *
     * @param change The changed value
     */
    protected void updateYPos(int change) {
        // once moved, cancel event will be sent to child
        if (mIndicator.hasTouched() && !mHasSendCancelEvent
                && mIndicator.hasMovedAfterPressedDown() && !mNestedScrollInProgress) {
            sendCancelEvent();
        }
        // leave initiated position or just refresh complete
        if ((mIndicator.hasJustLeftStartPosition() && mStatus == SR_STATUS_INIT)
                || (mStatus == SR_STATUS_COMPLETE && isEnabledNextPtrAtOnce()
                && ((isMovingHeader() && change > 0) || (isMovingFooter() && change < 0)))) {
            mStatus = SR_STATUS_PREPARE;
            int status = mIndicator.getMovingStatus();
            switch (status) {
                case IIndicator.MOVING_FOOTER:
                    if (mFooterView != null)
                        mFooterView.onRefreshPrepare(this);
                    break;
                case IIndicator.MOVING_HEADER:
                    if (mHeaderView != null)
                        mHeaderView.onRefreshPrepare(this);
                    break;
                case IIndicator.MOVING_CONTENT:
                    break;
            }
        }

        // back to initiated position
        if (!(isAutoRefresh() && mStatus != SR_STATUS_COMPLETE)
                && mIndicator.hasJustBackToStartPosition()) {
            tryToNotifyReset();
            // recover event to children
            if (mIndicator.hasTouched() && !mNestedScrollInProgress) {
                sendDownEvent();
            }
        }

        // try to perform refresh
        if (!mOverScrollChecker.mScrolling && mStatus == SR_STATUS_PREPARE) {
            // reach fresh height while moving from top to bottom or reach load more height while
            // moving from bottom to top
            if (mIndicator.hasTouched() && !isAutoRefresh() && isEnabledPullToRefresh()) {
                if ((isMovingHeader() && mIndicator.crossRefreshLineFromTopToBottom())
                        || (isMovingFooter() && mIndicator.crossRefreshLineFromBottomToTop()))
                    tryToPerformRefresh();
            }
            // reach header height while auto refresh or reach footer height while auto refresh
            if (!isRefreshing() && !isLoadingMore() && performAutoRefreshButLater()) {
                if ((isMovingHeader() && mIndicator.hasJustReachedHeaderHeightFromTopToBottom())
                        || (isMovingFooter() && mIndicator.hasJustReachedFooterHeightFromBottomToTop()))
                    tryToPerformRefresh();
            }
        }
        if (sDebug) {
            SRLog.d(TAG, "updateYPos(): change: %s, current: %s last: %s",
                    change, mIndicator.getCurrentPosY(), mIndicator.getLastPosY());
        }
        if (mUIPositionChangedListeners != null && !mUIPositionChangedListeners.isEmpty()) {
            for (OnUIPositionChangedListener listener : mUIPositionChangedListeners) {
                listener.onChanged(mStatus, mIndicator);
            }
        }
        final MarginLayoutParams lp = (MarginLayoutParams) mTargetView.getLayoutParams();
        if (mHeaderView != null && !isDisabledRefresh() && isMovingHeader()
                && !isEnabledHideHeaderView()) {
            if (!isEnabledHeaderDrawerStyle() && lp.bottomMargin == 0)
                mHeaderView.getView().offsetTopAndBottom(change);
            mHeaderView.onRefreshPositionChanged(this, mStatus, mIndicator);
        } else if (mFooterView != null && !isDisabledLoadMore() && isMovingFooter()
                && !isEnabledHideFooterView()) {
            if (!isEnabledFooterDrawerStyle() && lp.topMargin == 0)
                mFooterView.getView().offsetTopAndBottom(change);
            mFooterView.onRefreshPositionChanged(this, mStatus, mIndicator);
        }
        if (!isEnabledPinContentView()) {
            if (mLoadMoreScrollTargetView != null && isMovingFooter() && !isDisabledLoadMore()) {
                mLoadMoreScrollTargetView.offsetTopAndBottom(change);
            } else {
                mTargetView.offsetTopAndBottom(change);
            }
        }
        invalidate();
        mNeedScrollCompat = false;
        boolean needRequestLayout = false;
        //check if the margin is zero, we need relayout to change the content height
        if (isMovingHeader()) {
            if (lp.bottomMargin != 0) {
                mNeedScrollCompat = true;
            } else if ((mHeaderView != null && mHeaderView.getStyle() == IRefreshView.STYLE_SCALE)) {
                needRequestLayout = true;
            }
        } else if (isMovingFooter()) {
            if (lp.topMargin != 0) {
                mNeedScrollCompat = true;
            } else if ((mFooterView != null && mFooterView.getStyle() == IRefreshView.STYLE_SCALE)) {
                needRequestLayout = true;
            }
        }
        //check need perform load more
        if (mStatus == SR_STATUS_PREPARE && !isDisabledLoadMore()
                && change < 0 && isMovingFooter() && !canChildScrollDown()
                && !isDisabledPerformLoadMore()
                && isEnabledScrollToBottomAutoLoadMore()) {
            mStatus = SR_STATUS_LOADING_MORE;
            mDelayedRefreshComplete = false;
            performRefresh();
        }
        if (mNeedScrollCompat || needRequestLayout
                || (!mOverScrollChecker.mScrolling && mIndicator.isInStartPosition())) {
            if (sDebug) {
                SRLog.d(TAG, "movePos(): need relayout");
            }
            requestLayout();
        }
    }

    /**
     * We need to notify the X pos changed
     */
    protected void updateXPos() {
        if (mHeaderView != null && !isDisabledRefresh() && isMovingHeader()
                && !isEnabledHideHeaderView()) {
            mHeaderView.onRefreshPositionChanged(this, mStatus, mIndicator);
        } else if (mFooterView != null && !isDisabledLoadMore() && isMovingFooter()
                && !isEnabledHideFooterView()) {
            mFooterView.onRefreshPositionChanged(this, mStatus, mIndicator);
        }
    }

    protected boolean isMovingHeader() {
        return mIndicator.getMovingStatus() == IIndicator.MOVING_HEADER || isRefreshing();
    }

    protected boolean isMovingContent() {
        return mIndicator.getMovingStatus() == IIndicator.MOVING_CONTENT;
    }

    protected boolean isMovingFooter() {
        return mIndicator.getMovingStatus() == IIndicator.MOVING_FOOTER || isLoadingMore();
    }

    protected void tryToScrollTo(int to, int duration) {
        mScrollChecker.tryToScrollTo(to, duration);
    }

    /**
     * Check in over scrolling needs to scroll back to the start position
     *
     * @return Needs
     */
    private boolean needScrollBackToTop() {
        if (mOverScrollChecker.mClamped && !mIndicator.isInStartPosition()) {
            if (sDebug) {
                SRLog.i(TAG, "needScrollBackToTop()");
            }
            onRelease(mOverScrollChecker.mDuration);
            mOverScrollChecker.setClamped(false);
            return true;
        } else {
            return false;
        }
    }

    protected boolean tryToNotifyReset() {
        if (sDebug) {
            SRLog.i(TAG, "tryToNotifyReset()");
        }
        if ((mStatus == SR_STATUS_COMPLETE || mStatus == SR_STATUS_PREPARE)
                && mIndicator.isInStartPosition()) {
            if (mHeaderView != null)
                mHeaderView.onReset(this);
            if (mFooterView != null)
                mFooterView.onReset(this);
            mIndicator.setMovingStatus(IIndicator.MOVING_CONTENT);
            mStatus = SR_STATUS_INIT;
            mNeedNotifyRefreshComplete = true;
            mDelayedRefreshComplete = false;
            mOverScrollChecker.destroy();
            mFlag = mFlag & ~MASK_AUTO_REFRESH;
            return true;
        }
        return false;
    }

    protected void performRefreshComplete(boolean hook) {
        if (sDebug) {
            SRLog.i(TAG, "performRefreshComplete()");
        }
        if (isRefreshing() && hook && mHeaderRefreshCompleteHook != null
                && mHeaderRefreshCompleteHook.mCallBack != null) {
            mHeaderRefreshCompleteHook.mLayoutWeakRf = new WeakReference<>(this);
            mHeaderRefreshCompleteHook.doHook();
            return;
        }
        if (isLoadingMore() && hook && mFooterRefreshCompleteHook != null
                && mFooterRefreshCompleteHook.mCallBack != null) {
            mFooterRefreshCompleteHook.mLayoutWeakRf = new WeakReference<>(this);
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
        if (mStatus != SR_STATUS_PREPARE || !canPerformRefresh()) {
            return;
        }
        if (sDebug) {
            SRLog.i(TAG, "tryToPerformRefresh()");
        }
        if (isMovingHeader() && !isDisabledRefresh() && !isDisabledPerformRefresh()
                && ((mIndicator.isOverOffsetToKeepHeaderWhileLoading() && isAutoRefresh())
                || (isEnabledKeepRefreshView() && mIndicator.isOverOffsetToKeepHeaderWhileLoading())
                || mIndicator.isOverOffsetToRefresh())) {
            mStatus = SR_STATUS_REFRESHING;
            mDelayedRefreshComplete = false;
            performRefresh();
            return;
        }
        if (isMovingFooter() && !isDisabledLoadMore() && !isDisabledPerformLoadMore()
                && ((mIndicator.isOverOffsetToKeepFooterWhileLoading() && isAutoRefresh())
                || (isEnabledKeepRefreshView() && mIndicator.isOverOffsetToKeepFooterWhileLoading())
                || mIndicator.isOverOffsetToLoadMore())) {
            mStatus = SR_STATUS_LOADING_MORE;
            mDelayedRefreshComplete = false;
            performRefresh();
        }
    }

    protected boolean canPerformRefresh() {
        return !(mOverScrollChecker.mClamped || mOverScrollChecker.mScrolling
                || isMovingContent());
    }

    /**
     * try check auto refresh later flag
     *
     * @return Performed
     */
    protected boolean performAutoRefreshButLater() {
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
        if (mRefreshListener != null)
            mRefreshListener.onRefreshBegin(isRefreshing());
    }

    /**
     * If need auto refresh , make a release event
     */
    protected void onPtrScrollAbort() {
        if (mIndicator.hasLeftStartPosition() && isAutoRefresh()) {
            if (sDebug) {
                SRLog.i(TAG, "onPtrScrollAbort()");
            }
            onFingerUp(true);
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({STATE_CONTENT, STATE_ERROR, STATE_EMPTY, STATE_CUSTOM})
    @interface State {
    }

    /**
     * Classes that wish to override {@link SmoothRefreshLayout#canChildScrollUp()} method
     * behavior should implement this interface.
     */
    public interface OnChildScrollUpCallback {
        /**
         * Callback that will be called when {@link SmoothRefreshLayout#canChildScrollUp()} method
         * is called to allow the implementer to override its behavior.
         *
         * @param parent SmoothRefreshLayout that this callback is overriding.
         * @param child  The child view.
         * @param header The header view.
         * @return Whether it is possible for the child view of parent layout to scroll up.
         */
        boolean canChildScrollUp(SmoothRefreshLayout parent, @Nullable View child,
                                 @Nullable IRefreshView header);
    }

    /**
     * Classes that wish to override {@link SmoothRefreshLayout#canChildScrollDown()} method
     * behavior should implement this interface.
     */
    public interface OnChildScrollDownCallback {
        /**
         * Callback that will be called when {@link SmoothRefreshLayout#canChildScrollDown()} method
         * is called to allow the implementer to override its behavior.
         *
         * @param parent SmoothRefreshLayout that this callback is overriding.
         * @param child  The child view.
         * @param footer The footer view.
         * @return Whether it is possible for the child view of parent layout to scroll down.
         */
        boolean canChildScrollDown(SmoothRefreshLayout parent, @Nullable View child,
                                   @Nullable IRefreshView footer);
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
         * @param state The {@link State} that was changed
         */
        void onStateChanged(@State int state);
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
         * @param deltaY  The scroll distance in Y-axis
         * @return Whether it has been processed
         */
        boolean onScroll(View content, float deltaY);
    }

    public interface OnHookUIRefreshCompleteCallBack {
        @MainThread
        void onHook(RefreshCompleteHook hook);
    }

    /**
     * Classes that wish to be called when {@link SmoothRefreshLayout#setEnableScrollToBottomAutoLoadMore(boolean)}
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

    @SuppressWarnings("unused")
    public static class LayoutParams extends MarginLayoutParams {
        public static final int UNSPECIFIED_GRAVITY = -1;
        private int mGravity = UNSPECIFIED_GRAVITY;

        @SuppressWarnings("unused")
        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            final TypedArray a = c.obtainStyledAttributes(attrs, new int[]{android.R.attr.layout_gravity});
            mGravity = a.getInt(0, UNSPECIFIED_GRAVITY);
            a.recycle();
        }

        public LayoutParams(int width, int height, int gravity) {
            super(width, height);
            this.mGravity = gravity;
        }

        @SuppressWarnings({"unused"})
        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

        @SuppressWarnings("unused")
        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        @SuppressWarnings("unused")
        public LayoutParams(LayoutParams source) {
            super(source);
            mGravity = source.mGravity;
        }

        public int getGravity() {
            return mGravity;
        }
    }

    public static class RefreshCompleteHook {
        private WeakReference<SmoothRefreshLayout> mLayoutWeakRf;
        private OnHookUIRefreshCompleteCallBack mCallBack;

        public void onHookComplete() {
            if (mLayoutWeakRf.get() != null) {
                if (sDebug) {
                    SRLog.i(TAG, "RefreshCompleteHook: onHookComplete()");
                }
                mLayoutWeakRf.get().performRefreshComplete(false);
            }
        }

        private void setHookCallBack(OnHookUIRefreshCompleteCallBack callBack) {
            mCallBack = callBack;
        }

        private void doHook() {
            if (mCallBack != null) {
                if (sDebug) {
                    SRLog.i(TAG, "RefreshCompleteHook: doHook()");
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
                if (sDebug) {
                    SRLog.i(TAG, "DelayToRefreshComplete: run()");
                }
                mLayoutWeakRf.get().performRefreshComplete(true);
            }
        }
    }

    /**
     * Support over Scroll feature
     * The Over Scroll checker
     */
    private class OverScrollChecker implements Runnable {
        private final int mDisplayHeight;
        private OverScroller mOverScroller;
        private int mDuration = 0;
        private boolean mScrolling = false;
        private boolean mClamped = false;
        private boolean mFling = false;

        private OverScrollChecker(SmoothRefreshLayout layout) {
            DisplayMetrics dm = layout.getContext().getResources().getDisplayMetrics();
            mDisplayHeight = dm.heightPixels;
            mOverScroller = new OverScroller(layout.getContext());
        }

        private void fling(float vy, float friction) {
            if (sDebug) {
                SRLog.d(TAG, "OverScrollChecker: fling(): vy: %s", vy);
            }
            destroy();
            mFling = true;
            mOverScroller.setFriction(friction);
            mOverScroller.fling(0, 0, 0, (int) vy, Integer.MIN_VALUE, Integer.MAX_VALUE,
                    Integer.MIN_VALUE, Integer.MAX_VALUE);
        }

        private void reset() {
            mFling = false;
            if (sDebug) {
                SRLog.i(TAG, "OverScrollChecker: reset()");
            }
            removeCallbacks(this);
            mOverScroller.forceFinished(true);
        }

        private void setClamped(boolean clamped) {
            mClamped = clamped;
            if (sDebug) {
                SRLog.d(TAG, "OverScrollChecker: setClamped(): clamped: %s", clamped);
            }
        }

        private void destroy() {
            reset();
            mScrolling = false;
            mClamped = false;
            mDuration = 0;
            if (sDebug) {
                SRLog.i(TAG, "OverScrollChecker: destroy()");
            }
        }

        private void abortIfWorking() {
            if (sDebug) {
                SRLog.d(TAG, "OverScrollChecker: abortIfWorking(): scrolling: %s", mScrolling);
            }
            if (mScrolling) {
                destroy();
            }
            mFling = false;
            mOverScroller.forceFinished(true);
        }

        private void computeScrollOffset() {
            if (mFling && mOverScroller.computeScrollOffset()) {
                if (sDebug) {
                    SRLog.d(TAG, "OverScrollChecker: computeScrollOffset(): fling: %s, " +
                            "finished: %s", mFling, mOverScroller.isFinished());
                }
                mFling = true;
                removeCallbacks(this);
                postDelayed(this, 20);
            } else {
                mFling = false;
            }
        }

        @Override
        public void run() {
            if (!mFling)
                return;
            removeCallbacks(this);
            if (!mOverScroller.isFinished()) {
                int currY = mOverScroller.getCurrY();
                if (currY > 0 && !canChildScrollUp() && !isLoadingMore()
                        && !(isEnabledPinRefreshViewWhileLoading() && isRefreshing())) {
                    int distance = Math.round((mOverScroller.getFinalY() - currY)
                            * mOverScrollDistanceRatio);
                    mDuration = Math.abs(distance * 25);
                    mDuration = mDuration > 500 ? 500 : mDuration;
                    float maxHeaderDistance = mIndicator.getCanMoveTheMaxDistanceOfHeader();
                    int to = Math.round(distance);
                    final int maxDistance = mDisplayHeight / 6;
                    to = to > maxDistance ? maxDistance : to;
                    if (maxHeaderDistance > 0 && to > maxHeaderDistance) {
                        to = Math.round(maxHeaderDistance);
                    }
                    if (sDebug) {
                        SRLog.d(TAG, "OverScrollChecker: run(): to: %s, duration: %s", to,
                                mDuration);
                    }
                    mIndicator.setMovingStatus(IIndicator.MOVING_HEADER);
                    mScrollChecker.tryToScrollTo(to, mDuration);
                    mClamped = true;
                    mScrolling = true;
                    reset();
                    return;
                } else if (currY < 0 && !canChildScrollDown() && !isRefreshing()
                        && !(isEnabledPinRefreshViewWhileLoading() && isLoadingMore())) {
                    int distance = Math.round((currY - mOverScroller.getFinalY())
                            * mOverScrollDistanceRatio);
                    mDuration = Math.abs(distance * 25);
                    mDuration = mDuration > 500 ? 500 : mDuration;
                    float maxFooterDistance = mIndicator.getCanMoveTheMaxDistanceOfFooter();
                    int to = Math.abs(distance);
                    final int maxDistance = mDisplayHeight / 6;
                    to = to > maxDistance ? maxDistance : to;
                    if (maxFooterDistance > 0 && to > maxFooterDistance) {
                        to = Math.round(maxFooterDistance);
                    }
                    mClamped = true;
                    if (isEnabledScrollToBottomAutoLoadMore() && !isDisabledPerformLoadMore()) {
                        int offsetToKeepFooterWhileLoading = mIndicator
                                .getOffsetToKeepFooterWhileLoading();
                        if (to > offsetToKeepFooterWhileLoading) {
                            to = offsetToKeepFooterWhileLoading;
                        }
                        mDuration = Math.max(mDuration, getDurationToCloseFooter());
                        mClamped = false;
                    }
                    if (sDebug) {
                        SRLog.d(TAG, "OverScrollChecker: run(): to: %s, duration: %s", -to,
                                mDuration);
                    }
                    mIndicator.setMovingStatus(IIndicator.MOVING_FOOTER);
                    mScrollChecker.tryToScrollTo(to, mDuration);
                    mScrolling = true;
                    reset();
                    return;
                }
            }
            mScrolling = false;
            mClamped = false;
        }
    }

    private class ScrollChecker implements Runnable {
        private int mLastY;
        private int mLastStart;
        private int mLastTo;
        private boolean mIsRunning = false;
        private Scroller mScroller;

        private ScrollChecker(SmoothRefreshLayout layout) {
            mScroller = new Scroller(layout.getContext(), layout.mDefaultSpringInterpolator);
        }

        public void run() {
            boolean finished = !mScroller.computeScrollOffset() || mScroller.isFinished();
            int curY = mScroller.getCurrY();
            int deltaY = curY - mLastY;
            if (sDebug) {
                SRLog.d(TAG,
                        "ScrollChecker: run(): finished: %s, start: %s, to: %s, currentPos: %s, " +
                                "currentY:%s, last: %s, delta: %s",
                        finished, mLastStart, mLastTo, mIndicator.getCurrentPosY(), curY,
                        mLastY, deltaY);
            }
            if (!finished) {
                mLastY = curY;
                if (isMovingHeader()) {
                    moveHeaderPos(deltaY);
                } else if (isMovingFooter()) {
                    moveFooterPos(-deltaY);
                }
                post(this);
            } else {
                if (!needScrollBackToTop()) {
                    checkInStartPosition();
                    reset();
                    onRelease(0);
                }
            }
        }

        private void updateInterpolator(Interpolator interpolator) {
            if (mIsRunning) {
                int timePassed = mScroller.timePassed();
                int duration = mScroller.getDuration();
                destroy();
                mLastStart = mIndicator.getCurrentPosY();
                int distance = mLastTo - mLastStart;
                mScroller = new Scroller(getContext(), interpolator);
                mScroller.startScroll(0, 0, 0, distance, duration - timePassed);
                post(this);
            } else {
                destroy();
                mScroller = new Scroller(getContext(), interpolator);
            }
        }

        private void checkInStartPosition() {
            //It should have scrolled to the specified location, but it has not scrolled
            if (mLastTo == IIndicator.DEFAULT_START_POS
                    && !mIndicator.isInStartPosition()) {
                int currentPos = mIndicator.getCurrentPosY();
                int deltaY = IIndicator.DEFAULT_START_POS - currentPos;
                if (sDebug) {
                    SRLog.d(TAG, "ScrollChecker: checkInStartPosition(): deltaY: %s", deltaY);
                }
                if (isMovingHeader()) {
                    moveHeaderPos(deltaY);
                } else if (isMovingFooter()) {
                    moveFooterPos(-deltaY);
                }
            }
        }

        private void reset() {
            if (sDebug) {
                SRLog.i(TAG, "ScrollChecker: reset()");
            }
            mIsRunning = false;
            mLastY = 0;
            mOverScrollChecker.abortIfWorking();
            removeCallbacks(this);
        }

        private void destroy() {
            if (sDebug) {
                SRLog.i(TAG, "ScrollChecker: destroy()");
            }
            reset();
            if (!mScroller.isFinished()) {
                mScroller.forceFinished(true);
            }
        }

        private void abortIfWorking() {
            if (sDebug) {
                SRLog.i(TAG, "ScrollChecker: abortIfWorking()");
            }
            if (mIsRunning) {
                if (!mScroller.isFinished()) {
                    mScroller.forceFinished(true);
                }
                onPtrScrollAbort();
                reset();
            }
        }

        private void tryToScrollTo(int to, int duration) {
            if (sDebug) {
                SRLog.i(TAG, "ScrollChecker: tryToScrollTo()");
            }
            if (mIndicator.isAlreadyHere(to)) {
                return;
            }
            mLastStart = mIndicator.getCurrentPosY();
            mLastTo = to;
            int distance = to - mLastStart;
            removeCallbacks(this);
            mLastY = 0;
            if (sDebug) {
                SRLog.d(TAG, "ScrollChecker: tryToScrollTo(): start: %s, to:%s, duration:%s",
                        mLastStart, to, duration);
            }
            if (!mScroller.isFinished()) {
                mScroller.forceFinished(true);
            }
            if (duration > 0) {
                mScroller.startScroll(0, 0, 0, distance, duration);
                post(this);
                mIsRunning = true;
            } else {
                if (isMovingHeader()) {
                    moveHeaderPos(distance);
                } else if (isMovingFooter()) {
                    moveFooterPos(-distance);
                }
                mIsRunning = false;
            }
        }
    }
}
