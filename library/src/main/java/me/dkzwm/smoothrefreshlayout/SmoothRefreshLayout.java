package me.dkzwm.smoothrefreshlayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.support.annotation.IntDef;
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
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.Scroller;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.dkzwm.smoothrefreshlayout.exception.SRUIRuntimeException;
import me.dkzwm.smoothrefreshlayout.exception.SRUnsupportedOperationException;
import me.dkzwm.smoothrefreshlayout.extra.IRefreshView;
import me.dkzwm.smoothrefreshlayout.extra.header.MaterialHeader;
import me.dkzwm.smoothrefreshlayout.gesture.GestureDetector;
import me.dkzwm.smoothrefreshlayout.gesture.IGestureDetector;
import me.dkzwm.smoothrefreshlayout.gesture.OnGestureListener;
import me.dkzwm.smoothrefreshlayout.indicator.DefaultIndicator;
import me.dkzwm.smoothrefreshlayout.indicator.IIndicator;
import me.dkzwm.smoothrefreshlayout.utils.SRLog;
import me.dkzwm.smoothrefreshlayout.utils.ScrollCompat;

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
 *
 * @author dkzwm
 */
public class SmoothRefreshLayout extends ViewGroup implements OnGestureListener, NestedScrollingChild, NestedScrollingParent {
    //mode
    public static final int MODE_NONE = 0;
    public static final int MODE_REFRESH = 1;
    public static final int MODE_LOAD_MORE = 2;
    public static final int MODE_OVER_SCROLL = 3;
    public static final int MODE_BOTH = 4;
    //status
    public static final byte SR_STATUS_INIT = 1;
    public static final byte SR_STATUS_PREPARE = 2;
    public static final byte SR_STATUS_REFRESHING = 3;
    public static final byte SR_STATUS_LOADING_MORE = 4;
    public static final byte SR_STATUS_COMPLETE = 5;
    //local
    private static final String TAG = "SmoothRefreshLayout";
    private static final int OVER_SCROLL_MIN_VX = 20;
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
    private static final int FLAG_DISABLE_PERFORM_REFRESH = 0x01 << 10;
    private static final int FLAG_DISABLE_PERFORM_LOAD_MORE = 0x01 << 11;
    private static final int FLAG_DISABLE_REFRESH = 0x01 << 12;
    private static final int FLAG_DISABLE_LOAD_MORE = 0x01 << 13;
    private static final int FLAG_ENABLE_WHEN_SCROLLING_TO_BOTTOM_TO_PERFORM_LOAD_MORE = 0x01 << 14;
    private static final byte MASK_AUTO_REFRESH = 0x03;
    private static final int[] LAYOUT_ATTRS = new int[]{
            android.R.attr.enabled
    };
    private static boolean sDebug = false;
    private final List<View> mCachedViews = new ArrayList<>(1);
    private final NestedScrollingParentHelper mNestedScrollingParentHelper;
    private final NestedScrollingChildHelper mNestedScrollingChildHelper;
    private final int[] mParentScrollConsumed = new int[2];
    private final int[] mParentOffsetInWindow = new int[2];
    @Mode
    protected int mMode = MODE_NONE;
    protected IRefreshView mHeaderView;
    protected IRefreshView mFooterView;
    protected IIndicator mIndicator;
    protected OnRefreshListener mRefreshListener;
    protected byte mStatus = SR_STATUS_INIT;
    protected boolean mTriggeredAutoRefresh = true;
    protected boolean mTriggeredAutoLoadMore = true;
    protected boolean mNeedNotifyRefreshComplete = true;
    protected long mLoadingMinTime = 500;
    protected long mLoadingStartTime = 0;
    protected int mDurationToCloseHeader = 500;
    protected int mDurationToCloseFooter = 500;
    private int mFlag = 0x00;
    private IGestureDetector mGestureDetector;
    private OnChildScrollUpCallback mScrollUpCallback;
    private OnChildScrollDownCallback mScrollDownCallback;
    private OnLoadMoreScrollCallback mLoadMoreScrollCallback;
    private OnUIPositionChangedListener mUIPositionChangedListener;
    private View mContentView;
    private View mLoadMoreScrollTargetView;
    private MotionEvent mLastMoveEvent;
    private ScrollChecker mScrollChecker;
    private OverScrollChecker mOverScrollChecker;
    private DelayToRefreshComplete mDelayToRefreshComplete;
    private RefreshCompleteHook mRefreshCompleteHook;
    private boolean mHasSendCancelEvent = false;
    private boolean mDealHorizontalMove = false;
    private boolean mPreventForHorizontal = false;
    private boolean mIsLastRefreshSuccessful = true;
    private boolean mNestedScrollInProgress = false;
    private boolean mViewsZTreeNeedReset = true;
    private boolean mNestedFling = false;
    private boolean mNeedScrollCompat = false;
    private int mTouchSlop;
    private int mDurationOfBackToHeaderHeight = 200;
    private int mDurationOfBackToFooterHeight = 200;
    private int mContentResId = View.NO_ID;
    private int mTotalRefreshingUnconsumed;
    private int mTotalRefreshingConsumed;
    private int mTotalLoadMoreUnconsumed;
    private int mTotalLoadMoreConsumed;

    public SmoothRefreshLayout(Context context) {
        this(context, null);
    }

    public SmoothRefreshLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SmoothRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mIndicator = new DefaultIndicator();
        TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.SmoothRefreshLayout, 0, 0);
        if (arr != null) {
            mContentResId = arr.getResourceId(R.styleable.SmoothRefreshLayout_sr_content, mContentResId);
            float resistance = arr.getFloat(R.styleable
                    .SmoothRefreshLayout_sr_resistance, IIndicator.DEFAULT_RESISTANCE);
            mIndicator.setResistance(resistance);
            mIndicator.setResistanceOfPullDown(arr.getFloat(R.styleable
                    .SmoothRefreshLayout_sr_resistance_of_pull_down, mIndicator.getResistanceOfPullDown()));
            mIndicator.setResistanceOfPullUp(arr.getFloat(R.styleable
                    .SmoothRefreshLayout_sr_resistance_of_pull_up, mIndicator.getResistanceOfPullUp()));

            mDurationOfBackToHeaderHeight = arr.getInt(R.styleable
                            .SmoothRefreshLayout_sr_duration_of_back_to_refresh_height,
                    mDurationOfBackToHeaderHeight);
            mDurationOfBackToFooterHeight = arr.getInt(R.styleable
                            .SmoothRefreshLayout_sr_duration_of_back_to_refresh_height,
                    mDurationOfBackToFooterHeight);
            mDurationOfBackToHeaderHeight = arr.getInt(R.styleable
                            .SmoothRefreshLayout_sr_duration_of_back_to_header_height,
                    mDurationOfBackToHeaderHeight);
            mDurationOfBackToFooterHeight = arr.getInt(R.styleable
                            .SmoothRefreshLayout_sr_duration_of_back_to_footer_height,
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
            ratio = arr.getFloat(R.styleable
                    .SmoothRefreshLayout_sr_ratio_of_header_height_to_refresh, mIndicator
                    .getRatioOfHeaderHeightToRefresh());
            mIndicator.setRatioOfHeaderHeightToRefresh(ratio);
            ratio = arr.getFloat(R.styleable
                    .SmoothRefreshLayout_sr_ratio_of_footer_height_to_refresh, mIndicator
                    .getRatioOfFooterHeightToRefresh());
            mIndicator.setRatioOfFooterHeightToRefresh(ratio);

            //max move ratio of height
            ratio = arr.getFloat(R.styleable.
                    SmoothRefreshLayout_sr_can_move_the_max_ratio_of_refresh_height, IIndicator
                    .DEFAULT_CAN_MOVE_THE_MAX_RATIO_OF_REFRESH_VIEW_HEIGHT);
            mIndicator.setCanMoveTheMaxRatioOfRefreshHeight(ratio);
            ratio = arr.getFloat(R.styleable
                    .SmoothRefreshLayout_sr_can_move_the_max_ratio_of_header_height, mIndicator
                    .getCanMoveTheMaxRatioOfHeaderHeight());
            mIndicator.setCanMoveTheMaxRatioOfHeaderHeight(ratio);
            ratio = arr.getFloat(R.styleable
                    .SmoothRefreshLayout_sr_can_move_the_max_ratio_of_footer_height, mIndicator
                    .getCanMoveTheMaxRatioOfFooterHeight());
            mIndicator.setCanMoveTheMaxRatioOfFooterHeight(ratio);

            setEnableKeepRefreshView(arr.getBoolean(R.styleable
                    .SmoothRefreshLayout_sr_enable_keep_refresh_view, true));
            setEnablePinContentView(arr.getBoolean(R.styleable
                    .SmoothRefreshLayout_sr_enable_pin_content, false));
            setEnableOverScroll(arr.getBoolean(R.styleable
                    .SmoothRefreshLayout_sr_enable_over_scroll, true));
            setEnablePullToRefresh(arr.getBoolean(R.styleable
                    .SmoothRefreshLayout_sr_enable_pull_to_refresh, false));
            @Mode
            int mode = arr.getInt(R.styleable.SmoothRefreshLayout_sr_mode, MODE_NONE);
            mMode = mode;
            if (mMode == MODE_OVER_SCROLL) {
                setEnableOverScroll(true);
            }
            arr.recycle();
        } else {
            setEnablePullToRefresh(true);
        }
        final ViewConfiguration conf = ViewConfiguration.get(getContext());
        mTouchSlop = conf.getScaledTouchSlop();
        mGestureDetector = new GestureDetector(context, this);
        mScrollChecker = new ScrollChecker(this);
        mOverScrollChecker = new OverScrollChecker(this);
        setWillNotDraw(false);
        ViewCompat.setChildrenDrawingOrderEnabled(this, true);
        //supports nested scroll
        mNestedScrollingChildHelper = new NestedScrollingChildHelper(this);
        mNestedScrollingParentHelper = new NestedScrollingParentHelper(this);
        setNestedScrollingEnabled(true);

        final TypedArray a = context.obtainStyledAttributes(attrs, LAYOUT_ATTRS);
        setEnabled(a.getBoolean(0, true));
        a.recycle();
    }

    public static void debug(boolean debug) {
        sDebug = debug;
    }

    public static boolean isDebug() {
        return sDebug;
    }

    @Override
    final public void addView(View child) {
        addView(child, -1);
    }

    @Override
    final public void addView(View child, int index) {
        if (child == null) {
            throw new SRUnsupportedOperationException("Cannot add a null child view to a ViewGroup");
        }
        ViewGroup.LayoutParams params = child.getLayoutParams();
        if (params == null) {
            params = generateDefaultLayoutParams();
            if (params == null) {
                throw new SRUnsupportedOperationException("generateDefaultLayoutParams() cannot return null");
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
            throw new SRUnsupportedOperationException("generateDefaultLayoutParams() cannot return null");
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
        reset();
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mContentView == null) {
            ensureContent();
        }
        if (mContentView == null) {
            throw new SRUIRuntimeException("The content view is empty." +
                    " Do you forget to added it in the XML layout file or add it in code ?");
        }
        mCachedViews.clear();
        int count = getChildCount();
        final boolean measureMatchParentChildren =
                MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY ||
                        MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY;
        int maxHeight = 0;
        int maxWidth = 0;
        int childState = 0;
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (mHeaderView != null && child == mHeaderView) {
                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
                MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
                mIndicator.setHeaderHeight(child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin);
                continue;
            }
            if (mFooterView != null && child == mFooterView) {
                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
                MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
                mIndicator.setFooterHeight(child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin);
                continue;
            }
            if (mContentView != null && mContentView == child) {
                measureContentView(mContentView, widthMeasureSpec, heightMeasureSpec);
                continue;
            }
            if (child.getVisibility() != GONE) {
                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                maxWidth = Math.max(maxWidth,
                        child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin);
                maxHeight = Math.max(maxHeight,
                        child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin);
                childState = combineMeasuredStates(childState, child.getMeasuredState());
                if (measureMatchParentChildren) {
                    if (lp.width == LayoutParams.MATCH_PARENT ||
                            lp.height == LayoutParams.MATCH_PARENT) {
                        mCachedViews.add(child);
                    }
                }
            }
        }
        maxWidth += getPaddingLeft() + getPaddingRight();
        maxHeight += getPaddingTop() + getPaddingBottom();
        maxHeight = Math.max(maxHeight, getSuggestedMinimumHeight());
        maxWidth = Math.max(maxWidth, getSuggestedMinimumWidth());
        final Drawable drawable;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            drawable = getForeground();
            if (drawable != null) {
                maxHeight = Math.max(maxHeight, drawable.getMinimumHeight());
                maxWidth = Math.max(maxWidth, drawable.getMinimumWidth());
            }
        }
        setMeasuredDimension(resolveSizeAndState(maxWidth, widthMeasureSpec, childState),
                resolveSizeAndState(maxHeight, heightMeasureSpec,
                        childState << MEASURED_HEIGHT_STATE_SHIFT));
        measureOtherViews(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int count = getChildCount();
        if (count == 0)
            return;
        checkViewsZTreeNeedReset();
        final int paddingLeft = getPaddingLeft();
        final int paddingTop = getPaddingTop();
        final int paddingRight = r - l - getPaddingRight();
        final int paddingBottom = b - t - getPaddingBottom();
        int offsetHeaderY = 0;
        int offsetFooterY = 0;
        if ((isMovingHeader() || isRefreshing())) {
            offsetHeaderY = mIndicator.getCurrentPosY();
        } else if ((isMovingFooter() || isLoadingMore())) {
            offsetFooterY = mIndicator.getCurrentPosY();
        }
        int contentBottom = 0;
        boolean pin = isEnabledPinContentView();
        if (mLoadMoreScrollTargetView != null && !isMovingHeader())
            pin = true;
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (mHeaderView != null && child == mHeaderView) {
                if ((mMode == MODE_REFRESH || mMode == MODE_BOTH)) {
                    MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
                    final int left = paddingLeft + lp.leftMargin;
                    final int top = paddingTop + lp.topMargin +
                            (isEnabledHeaderDrawerStyle() ? 0 : offsetHeaderY - mIndicator.getHeaderHeight());
                    final int right = left + child.getMeasuredWidth();
                    final int bottom = top + child.getMeasuredHeight();
                    child.layout(left, top, right, bottom);
                    if (sDebug) {
                        SRLog.d(TAG, "onLayout(): header: %s %s %s %s", left, top, right, bottom);
                    }
                }
                continue;
            }
            if (mContentView != null && child == mContentView) {
                MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
                final int left = paddingLeft + lp.leftMargin;
                final int right = left + child.getMeasuredWidth();
                int top, bottom;
                if (isMovingHeader() || isRefreshing()) {
                    top = paddingTop + lp.topMargin + (pin ? 0 : offsetHeaderY);
                    bottom = top + child.getMeasuredHeight();
                    child.layout(left, top, right, bottom);
                } else if (isMovingFooter() || isLoadingMore()) {
                    if (lp.topMargin == 0) {
                        top = paddingTop + lp.topMargin - (pin ? 0 : offsetFooterY);
                        bottom = top + child.getMeasuredHeight();
                    } else {
                        top = paddingTop + lp.topMargin;
                        bottom = top + child.getMeasuredHeight() - (pin ? 0 : offsetFooterY);
                    }
                    child.layout(left, top, right, bottom);
                    //If content view is moving and top margin is not zero.we need scroll to the
                    // bottom to fix margin not working
                    if (offsetFooterY != 0 && lp.topMargin != 0 && mNeedScrollCompat) {
                        final int deltaY = offsetFooterY - mIndicator.getLastPosY();
                        if (!(mIndicator.hasTouched() && deltaY < 0)) {
                            ScrollCompat.scrollCompat(mContentView, deltaY);
                            if (sDebug) {
                                SRLog.d(TAG, "onLayout(): do scroll compat deltaY: %s", deltaY);
                            }
                        }
                        mNeedScrollCompat = false;
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
                continue;
            }
            if (mFooterView != null && mFooterView == child) {
                continue;
            }
            if (child.getVisibility() != GONE) {
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();
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
                && (mMode == MODE_BOTH || mMode == MODE_LOAD_MORE)) {
            MarginLayoutParams lp = (MarginLayoutParams) ((View) mFooterView).getLayoutParams();
            final int left = paddingLeft + lp.leftMargin;
            final int top = lp.topMargin + contentBottom +
                    (isEnabledFooterDrawerStyle() ? -mIndicator.getFooterHeight()
                            : -(pin ? offsetFooterY : 0));
            final int right = left + ((View) mFooterView).getMeasuredWidth();
            final int bottom = top + ((View) mFooterView).getMeasuredHeight();
            ((View) mFooterView).layout(left, top, right, bottom);
            if (sDebug) {
                SRLog.d(TAG, "onLayout(): footer: %s %s %s %s", left, top, right, bottom);
            }
        }
        tryToPerformAutoRefresh();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (!isEnabled() || mContentView == null || mMode == MODE_NONE
                || (isEnabledPinRefreshViewWhileLoading() && (isRefreshing() || isLoadingMore()))
                || mNestedScrollInProgress) {
            return super.dispatchTouchEvent(ev);
        }
        return processDispatchTouchEvent(ev);
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean b) {
        if (!((android.os.Build.VERSION.SDK_INT < 21 && mContentView instanceof AbsListView)
                || (mContentView != null && !ViewCompat.isNestedScrollingEnabled(mContentView)))) {
            super.requestDisallowInterceptTouchEvent(b);
        }
    }

    /**
     * Set loadMore scroll target view<br/>
     * For example the content view is a FrameLayout,with a listView in it.<br/>
     * You can call this method,set the listView as load more scroll target view.<br/>
     * Load more compat will try to make it smooth scrolling
     *
     * @param view Target view
     */
    @SuppressWarnings({"unused"})
    public void setLoadMoreScrollTargetView(View view) {
        if (mMode == MODE_NONE || mMode == MODE_REFRESH || mMode == MODE_OVER_SCROLL)
            throw new SRUnsupportedOperationException("Set load more scroll target view ,the mode" +
                    " must be MODE_LOAD_MORE or MODE_BOTH");
        mLoadMoreScrollTargetView = view;
    }

    /**
     * Set the listener to be notified when a refresh is triggered.
     *
     * @param listener Listener
     */
    public <T extends OnRefreshListener> void setOnRefreshListener(T listener) {
        mRefreshListener = listener;
    }

    public void setOnUIPositionChangedListener(OnUIPositionChangedListener listener) {
        mUIPositionChangedListener = listener;
    }

    /**
     * Set a scrolling callback when loading more.
     *
     * @param callback Callback that should be called when scrolling on loading more.
     */
    public void setOnLoadMoreScrollCallback(OnLoadMoreScrollCallback callback) {
        mLoadMoreScrollCallback = callback;
    }

    /**
     * Set a callback to override {@link SmoothRefreshLayout#canChildScrollUp()} method. Non-null
     * callback will return the value provided by the callback and ignore all internal logic.
     *
     * @param callback Callback that should be called when canChildScrollUp() is called.
     */
    public void setOnChildScrollUpCallback(OnChildScrollUpCallback callback) {
        mScrollUpCallback = callback;
    }

    /**
     * Set a callback to override {@link SmoothRefreshLayout#canChildScrollDown()} method. Non-null
     * callback will return the value provided by the callback and ignore all internal logic.
     *
     * @param callback Callback that should be called when canChildScrollDown() is called.
     */
    public void setOnChildScrollDownCallback(OnChildScrollDownCallback callback) {
        mScrollDownCallback = callback;
    }

    public void setOnHookUIRefreshCompleteCallback(OnHookUIRefreshCompleteCallBack callback) {
        if (mRefreshCompleteHook == null)
            mRefreshCompleteHook = new RefreshCompleteHook(this);
        mRefreshCompleteHook.setHookCallBack(callback);
    }

    public boolean equalsOnHookUIRefreshCompleteCallback(OnHookUIRefreshCompleteCallBack callBack) {
        return mRefreshCompleteHook != null && mRefreshCompleteHook.mCallBack == callBack;
    }

    /**
     * Whether it is being refreshed
     *
     * @return Refreshing
     */
    public boolean isRefreshing() {
        return mStatus == SR_STATUS_REFRESHING;
    }

    /**
     * Whether it is being refreshed
     *
     * @return Loading
     */
    public boolean isLoadingMore() {
        return mStatus == SR_STATUS_LOADING_MORE;
    }

    public boolean isInStartPosition() {
        return mIndicator.isInStartPosition();
    }

    public boolean isRefreshSuccessful() {
        return mIsLastRefreshSuccessful;
    }

    /**
     * Perform refresh complete ,to reset
     */
    final public void refreshComplete() {
        refreshComplete(true);
    }

    final public void refreshComplete(boolean isSuccessful) {
        if (sDebug) {
            SRLog.d(TAG, "refreshComplete(): isSuccessful: " + isSuccessful);
        }
//        mNeedScrollToBottom = false;
        mIsLastRefreshSuccessful = isSuccessful;
        long delay = mLoadingMinTime - (SystemClock.uptimeMillis() - mLoadingStartTime);
        if (delay <= 0) {
            performRefreshComplete(true);
        } else {
            if (mDelayToRefreshComplete == null)
                mDelayToRefreshComplete = new DelayToRefreshComplete(this);
            postDelayed(mDelayToRefreshComplete, delay);
        }
    }

    /**
     * Set the loading min time
     *
     * @param time Millis
     */
    public void setLoadingMinTime(long time) {
        mLoadingMinTime = time;
    }

    /**
     * Get the header height,
     * After the measurement is completed, the height will have value
     *
     * @return Height default is -1
     */
    public int getHeaderHeight() {
        return mIndicator.getHeaderHeight();
    }

    /**
     * Get the footer height,
     * After the measurement is completed, the height will have value
     *
     * @return Height default is -1
     */
    public int getFooterHeight() {
        return mIndicator.getFooterHeight();
    }

    /**
     * If enable has been set to true. The user can perform next PTR at once.
     *
     * @return Is enable
     */
    public boolean isEnabledNextPtrAtOnce() {
        return (mFlag & FLAG_ENABLE_NEXT_AT_ONCE) > 0;
    }

    /**
     * If @param enable has been set to true. The user can perform next PTR at once.
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
     * Perform auto refresh at once
     */
    public void autoRefresh() {
        autoRefresh(true);
    }

    /**
     * If @param atOnce has been set to true. Auto perform refresh at once.
     *
     * @param atOnce Auto refresh at once
     */
    public void autoRefresh(boolean atOnce) {
        if (mMode != MODE_REFRESH && mMode != MODE_BOTH)
            throw new SRUnsupportedOperationException("Perform auto refresh , the mode" +
                    "must be MODE_REFRESH or MODE_BOTH");
        if (mStatus != SR_STATUS_INIT) {
            return;
        }
        if (!mTriggeredAutoLoadMore)
            throw new SRUnsupportedOperationException("Can not trigger refresh and load at" +
                    " the same time");
        if (sDebug) {
            SRLog.d(TAG, "autoRefresh(): atOnce:", atOnce);
        }
        mFlag |= atOnce ? FLAG_AUTO_REFRESH_AT_ONCE : FLAG_AUTO_REFRESH_BUT_LATER;
        mStatus = SR_STATUS_PREPARE;
        if (mHeaderView != null)
            mHeaderView.onRefreshPrepare(this);
        if (mFooterView != null)
            mFooterView.onRefreshPrepare(this);
        mIndicator.setMovingStatus(IIndicator.MOVING_HEADER);
        int offsetToRefresh = mIndicator.getOffsetToRefresh();
        if (offsetToRefresh <= 0) {
            mTriggeredAutoRefresh = false;
        } else {
            mTriggeredAutoRefresh = true;
            mScrollChecker.tryToScrollTo(offsetToRefresh, mDurationToCloseHeader);
        }
        if (atOnce) {
            mStatus = SR_STATUS_REFRESHING;
            performRefresh();
        }
    }

    /**
     * Perform auto load more at once
     */
    public void autoLoadMore() {
        autoLoadMore(true);
    }

    /**
     * If @param atOnce has been set to true. Auto perform load more at once.
     *
     * @param atOnce Auto load more at once
     */
    public void autoLoadMore(boolean atOnce) {
        if (mMode != MODE_LOAD_MORE && mMode != MODE_BOTH)
            throw new SRUnsupportedOperationException("Perform auto load more , the mode" +
                    "must be MODE_LOAD_MORE or MODE_BOTH");
        if (mStatus != SR_STATUS_INIT) {
            return;
        }
        if (!mTriggeredAutoRefresh)
            throw new SRUnsupportedOperationException("Can not trigger refresh and load at" +
                    " the same time");
        if (sDebug) {
            SRLog.d(TAG, "autoLoadMore(): atOnce:", atOnce);
        }
        mFlag |= atOnce ? FLAG_AUTO_REFRESH_AT_ONCE : FLAG_AUTO_REFRESH_BUT_LATER;
        mStatus = SR_STATUS_PREPARE;
        if (mHeaderView != null)
            mHeaderView.onRefreshPrepare(this);
        if (mFooterView != null)
            mFooterView.onRefreshPrepare(this);
        mIndicator.setMovingStatus(IIndicator.MOVING_FOOTER);
        int offsetToLoadMore = mIndicator.getOffsetToLoadMore();
        if (offsetToLoadMore <= 0) {
            mTriggeredAutoLoadMore = false;
        } else {
            mTriggeredAutoLoadMore = true;
            mScrollChecker.tryToScrollTo(offsetToLoadMore, mDurationToCloseFooter);
        }
        if (atOnce) {
            mStatus = SR_STATUS_LOADING_MORE;
            performRefresh();
        }
    }

    /**
     * The resistance while you are moving
     *
     * @param resistance Height ratio
     */
    @SuppressWarnings({"unused"})
    public void setResistance(float resistance) {
        mIndicator.setResistance(resistance);
    }

    /**
     * The resistance while you are pulling up
     *
     * @param resistance Height ratio
     */
    @SuppressWarnings({"unused"})
    public void setResistanceOfPullUp(float resistance) {
        mIndicator.setResistanceOfPullUp(resistance);
    }

    /**
     * The resistance while you are pulling down
     *
     * @param resistance Height ratio
     */
    @SuppressWarnings({"unused"})
    public void setResistanceOfPullDown(float resistance) {
        mIndicator.setResistanceOfPullDown(resistance);
    }

    /**
     * the height ratio of the trigger refresh
     *
     * @param ratio Height ratio
     */
    @SuppressWarnings({"unused"})
    public void setRatioOfRefreshViewHeightToRefresh(float ratio) {
        mIndicator.setRatioOfRefreshViewHeightToRefresh(ratio);
    }

    /**
     * the height ratio of the trigger refresh
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
     * The height ratio of the trigger refresh
     *
     * @param ratio Height ratio
     */
    @SuppressWarnings({"unused"})
    public void setRatioOfFooterHeightToRefresh(float ratio) {
        mIndicator.setRatioOfFooterHeightToRefresh(ratio);
    }

    public void setOffsetRatioToKeepHeaderWhileLoading(float ratio) {
        mIndicator.setOffsetRatioToKeepHeaderWhileLoading(ratio);
    }

    public void setOffsetRatioToKeepFooterWhileLoading(float ratio) {
        mIndicator.setOffsetRatioToKeepFooterWhileLoading(ratio);
    }

    /**
     * The duration of return back to the start position
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
     * @return Duration
     */
    @SuppressWarnings({"unused"})
    public int getDurationToCloseHeader() {
        return mDurationToCloseHeader;
    }

    /**
     * The duration of header return back to the start position
     *
     * @param duration Millis
     */
    @SuppressWarnings({"unused"})
    public void setDurationToCloseHeader(int duration) {
        mDurationToCloseHeader = duration;
    }

    /**
     * Get the  duration of footer return back to the start position
     *
     * @return Duration
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
     * The duration of refresh view to return back to the refresh or loading position
     *
     * @param duration Millis
     */
    @SuppressWarnings({"unused"})
    public void setDurationOfBackToRefreshViewHeight(int duration) {
        mDurationOfBackToHeaderHeight = duration;
        mDurationOfBackToFooterHeight = duration;
    }

    /**
     * Get the duration of header return back to the refresh position
     *
     * @return Duration
     */
    @SuppressWarnings({"unused"})
    public int getDurationOfBackToHeaderHeight() {
        return mDurationOfBackToHeaderHeight;
    }

    /**
     * The duration of header return back to the refresh position
     *
     * @param duration Millis
     */
    @SuppressWarnings({"unused"})
    public void setDurationOfBackToHeaderHeight(int duration) {
        this.mDurationOfBackToHeaderHeight = duration;
    }

    /**
     * Get the duration of footer return back to the loading position
     *
     * @return Duration
     */
    @SuppressWarnings({"unused"})
    public int getDurationOfBackToFooterHeight() {
        return mDurationOfBackToFooterHeight;
    }

    /**
     * The duration of footer return back to the loading position
     *
     * @param duration Millis
     */
    @SuppressWarnings({"unused"})
    public void setDurationOfBackToFooterHeight(int duration) {
        this.mDurationOfBackToFooterHeight = duration;
    }

    /**
     * The max ratio of height for the refresh view when the finger moves
     *
     * @param ratio The max ratio of refresh view
     */
    @SuppressWarnings({"unused"})
    public void setCanMoveTheMaxRatioOfRefreshHeight(float ratio) {
        mIndicator.setCanMoveTheMaxRatioOfRefreshHeight(ratio);
    }

    @SuppressWarnings({"unused"})
    public float getCanMoveTheMaxRatioOfHeaderHeight() {
        return mIndicator.getCanMoveTheMaxRatioOfHeaderHeight();
    }

    /**
     * The max ratio of height for the header view when the finger moves
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
     * The max ratio of height for the footer view when the finger moves
     *
     * @param ratio The max ratio of footer view
     */
    @SuppressWarnings({"unused"})
    public void setCanMoveTheMaxRatioOfFooterHeight(float ratio) {
        mIndicator.setCanMoveTheMaxRatioOfFooterHeight(ratio);
    }

    /**
     * The flag has set to autoRefresh
     *
     * @return Enabled
     */
    public boolean isAutoRefresh() {
        return (mFlag & MASK_AUTO_REFRESH) > 0;
    }

    /**
     * The flag has set enabled overScroll
     *
     * @return Enabled
     */
    public boolean isEnabledOverScroll() {
        return (mFlag & FLAG_ENABLE_OVER_SCROLL) > 0;
    }

    /**
     * If @param enable has been set to true. Will supports over scroll.
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
     * The flag has been set to pull to refresh
     *
     * @return Enabled
     */
    public boolean isEnabledPullToRefresh() {
        return (mFlag & FLAG_ENABLE_PULL_TO_REFRESH) > 0;
    }

    /**
     * If @param enable has been set to true. When the current pos >= refresh offsets, perform refresh
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
     * The flag has been set to enabled header drawerStyle
     *
     * @return Enabled
     */
    public boolean isEnabledHeaderDrawerStyle() {
        return (mFlag & FLAG_ENABLE_HEADER_DRAWER_STYLE) > 0;
    }

    /**
     * If @param enable has been set to true.Enable header drawerStyle
     *
     * @param enable enable header drawerStyle
     */
    public void setEnableHeaderDrawerStyle(boolean enable) {
        if (enable) {
            mFlag = mFlag | FLAG_ENABLE_HEADER_DRAWER_STYLE;
        } else {
            mFlag = mFlag & ~FLAG_ENABLE_HEADER_DRAWER_STYLE;
        }
        mViewsZTreeNeedReset = true;
    }

    /**
     * The flag has been set to enabled footer drawerStyle
     *
     * @return Enabled
     */
    public boolean isEnabledFooterDrawerStyle() {
        return (mFlag & FLAG_ENABLE_FOOTER_DRAWER_STYLE) > 0;
    }

    /**
     * If @param enable has been set to true.Enable footer drawerStyle
     *
     * @param enable enable footer drawerStyle
     */
    public void setEnableFooterDrawerStyle(boolean enable) {
        if (enable) {
            mFlag = mFlag | FLAG_ENABLE_FOOTER_DRAWER_STYLE;
        } else {
            mFlag = mFlag & ~FLAG_ENABLE_FOOTER_DRAWER_STYLE;
        }
        mViewsZTreeNeedReset = true;
    }

    /**
     * The flag has been set to disabled perform refresh
     *
     * @return Disabled
     */
    public boolean isDisabledPerformRefresh() {
        return (mFlag & FLAG_DISABLE_PERFORM_REFRESH) > 0;
    }

    /**
     * If @param disable has been set to true.Will never perform refresh
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
     * The flag has been set to disabled refresh
     *
     * @return Disabled
     */
    public boolean isDisabledRefresh() {
        return (mFlag & FLAG_DISABLE_REFRESH) > 0;
    }

    /**
     * If @param disable has been set to true.Will disable refresh
     *
     * @param disable Disable refresh
     */
    public void setDisableRefresh(boolean disable) {
        if (disable) {
            mFlag = mFlag | FLAG_DISABLE_REFRESH;
        } else {
            mFlag = mFlag & ~FLAG_DISABLE_REFRESH;
        }
    }

    /**
     * The flag has been set to disabled perform load more
     *
     * @return Disabled
     */
    public boolean isDisabledPerformLoadMore() {
        return (mFlag & FLAG_DISABLE_PERFORM_LOAD_MORE) > 0;
    }

    /**
     * If @param disable has been set to true.Will never perform load more
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
     * The flag has been set to disabled load more
     *
     * @return Disabled
     */
    public boolean isDisabledLoadMore() {
        return (mFlag & FLAG_DISABLE_LOAD_MORE) > 0;
    }

    /**
     * If @param disable has been set to true.Will disable load more
     *
     * @param disable Disable load more
     */
    public void setDisableLoadMore(boolean disable) {
        if (disable) {
            mFlag = mFlag | FLAG_DISABLE_LOAD_MORE;
        } else {
            mFlag = mFlag & ~FLAG_DISABLE_LOAD_MORE;
        }
    }

    /**
     * The flag has been set to keep refresh view while loading
     *
     * @return Enabled
     */
    public boolean isEnabledKeepRefreshView() {
        return (mFlag & FLAG_ENABLE_KEEP_REFRESH_VIEW) > 0;
    }

    /**
     * If @param enable has been set to true.When the current pos> = refresh view height,
     * it rolls back to the refresh view height to perform refresh and remains until the refresh
     * completed
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
     * The flag has been set to perform load more when the content view scrolling to bottom
     *
     * @return Enabled
     */
    public boolean isEnabledWhenScrollingToBottomToPerformLoadMore() {
        return (mFlag & FLAG_ENABLE_WHEN_SCROLLING_TO_BOTTOM_TO_PERFORM_LOAD_MORE) > 0;
    }

    /**
     * If @param enable has been set to true.When the content view scrolling to bottom,
     * It will be perform load more
     *
     * @param enable Enable
     */
    public void setEnableWhenScrollingToBottomToPerformLoadMore(boolean enable) {
        if (enable) {
            mFlag = mFlag | FLAG_ENABLE_WHEN_SCROLLING_TO_BOTTOM_TO_PERFORM_LOAD_MORE;
        } else {
            mFlag = mFlag & ~FLAG_ENABLE_WHEN_SCROLLING_TO_BOTTOM_TO_PERFORM_LOAD_MORE;
        }
    }

    /**
     * The flag has been set to pinned refresh view while loading
     *
     * @return Enabled
     */
    public boolean isEnabledPinRefreshViewWhileLoading() {
        return (mFlag & FLAG_ENABLE_PIN_REFRESH_VIEW_WHILE_LOADING) > 0;
    }

    /**
     * If @param enable has been set to true.The refresh view will pinned at the refresh offset position
     *
     * @param enable Pin content view
     */
    public void setEnablePinRefreshViewWhileLoading(boolean enable) {
        if (enable) {
            if (isEnabledPinContentView() && isEnabledKeepRefreshView()) {
                mFlag = mFlag | FLAG_ENABLE_PIN_REFRESH_VIEW_WHILE_LOADING;
            } else {
                throw new SRUnsupportedOperationException("This method can only be enabled if setEnablePinContentView" +
                        " and setEnableKeepRefreshView are set be true");
            }
        } else {
            mFlag = mFlag & ~FLAG_ENABLE_PIN_REFRESH_VIEW_WHILE_LOADING;
        }
    }

    /**
     * The flag has been set to pinned content view while loading
     *
     * @return Enabled
     */
    public boolean isEnabledPinContentView() {
        return (mFlag & FLAG_ENABLE_PIN_CONTENT_VIEW) > 0;
    }

    /**
     * If @param enable has been set to true.The content view will be pinned in the start pos
     * unless overScroll flag has been set and in overScrolling
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
     * Set the load more view
     *
     * @param footer Footer view
     */
    public void setFooterView(@NonNull IRefreshView footer) {
        if (mFooterView != null) {
            removeView(mFooterView.getView());
            mFooterView = null;
        }
        if (footer.getType() != IRefreshView.TYPE_FOOTER)
            throw new SRUnsupportedOperationException("Wrong type,FooterView's type must be " +
                    "TYPE_FOOTER");
        if (mMode != MODE_LOAD_MORE && mMode != MODE_BOTH)
            throw new SRUnsupportedOperationException("You can set the FooterView only if the " +
                    "mode is MODE_BOTH or MODE_LOAD_MORE !");
        View view = footer.getView();
        ViewGroup.LayoutParams lp = view.getLayoutParams();
        if (lp == null) {
            lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            view.setLayoutParams(lp);
        }
        mViewsZTreeNeedReset = true;
        addView(view);
    }

    /**
     * Set the refresh view
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
            throw new SRUnsupportedOperationException("Wrong type,HeaderView's type must be " +
                    "TYPE_HEADER");
        if (mMode != MODE_REFRESH && mMode != MODE_BOTH)
            throw new SRUnsupportedOperationException("You can set the HeaderView only if the " +
                    "mode is MODE_BOTH or MODE_REFRESH !");
        View view = header.getView();
        ViewGroup.LayoutParams lp = view.getLayoutParams();
        if (lp == null) {
            lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            view.setLayoutParams(lp);
        }
        mViewsZTreeNeedReset = true;
        addView(view);
    }

    /**
     * Set the content view
     *
     * @param content Content view
     */
    public void setContentView(@NonNull View content) {
        if (mContentView != null) {
            removeView(content);
            mContentResId = View.NO_ID;
            mContentView = null;
        }
        ViewGroup.LayoutParams lp = content.getLayoutParams();
        if (lp == null) {
            lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            content.setLayoutParams(lp);
        }
        mViewsZTreeNeedReset = true;
        addView(content);
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
     * Get SR mode
     *
     * @return Mode SR mode
     */
    @SmoothRefreshLayout.Mode
    @SuppressWarnings({"unused"})
    public int getMode() {
        return mMode;
    }

    /**
     * Set SR mode
     *
     * @param mode SR mode
     */
    public void setMode(@Mode int mode) {
        mMode = mode;
        tryToNotifyReset();
    }

    // NestedScrollingParent

    @Override
    public void onFling(float lastScrollY, float vx, float vy) {
        if (!isEnabledOverScroll() || mMode == MODE_NONE
                || isRefreshing() || isLoadingMore() || mNestedScrollInProgress)
            return;
        if ((!canChildScrollUp() && vy > 0) || (!canChildScrollDown() && vy < 0))
            return;
        mOverScrollChecker.updateVelocityY(vy / 5, lastScrollY > 0 ? -1 : 1);
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
        mScrollChecker.abortIfWorking();
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        if (sDebug) {
            SRLog.d(TAG, "onNestedPreScroll(): dx: %s, dy: %s, consumed: %s",
                    dx, dy, Arrays.toString(consumed));
        }
        if (dy > 0 && mTotalRefreshingUnconsumed >= 0
                && !isDisabledRefresh()
                && !(isEnabledPinRefreshViewWhileLoading() && isRefreshing())
                && !mScrollChecker.mIsRunning
                && !mOverScrollChecker.isScrolling()
                && (mMode == MODE_BOTH || mMode == MODE_REFRESH)
                && (isMovingHeader() || isMovingContent() && mTotalRefreshingUnconsumed == 0)) {
            if (mTotalRefreshingUnconsumed == 0 && mStatus == SR_STATUS_REFRESHING
                    && mTotalRefreshingConsumed / mIndicator.getResistanceOfPullUp()
                    < mIndicator.getHeaderHeight()) {
                mTotalRefreshingConsumed += dy;
                mIndicator.onFingerMove(mIndicator.getLastMovePoint()[0],
                        mIndicator.getLastMovePoint()[1] - dy);
                moveHeaderPos(mIndicator.getOffsetY());
                consumed[1] = dy;
            } else if (mTotalRefreshingUnconsumed != 0) {
                mTotalRefreshingUnconsumed -= dy;

                if (mTotalRefreshingUnconsumed <= 0) {//over
                    mTotalRefreshingUnconsumed = 0;
                }
                mIndicator.onFingerMove(mIndicator.getLastMovePoint()[0],
                        mIndicator.getLastMovePoint()[1] - dy);
                moveHeaderPos(mIndicator.getOffsetY());
                consumed[1] = dy;
            } else if (!mIndicator.isInStartPosition()) {
                mIndicator.onFingerMove(mIndicator.getLastMovePoint()[0],
                        mIndicator.getLastMovePoint()[1] - dy);
                moveHeaderPos(mIndicator.getOffsetY());
                consumed[1] = dy;
            }
        }
        if (dy < 0 && (mMode == MODE_BOTH || mMode == MODE_LOAD_MORE)
                && !isDisabledLoadMore()
                && !(isEnabledPinRefreshViewWhileLoading() && isLoadingMore())
                && !mScrollChecker.mIsRunning
                && !mOverScrollChecker.isScrolling()
                && mTotalLoadMoreUnconsumed >= 0) {
            if (mStatus == SR_STATUS_LOADING_MORE && mTotalLoadMoreUnconsumed == 0
                    && mTotalLoadMoreConsumed / mIndicator.getResistanceOfPullDown()
                    < mIndicator.getFooterHeight()
                    && (isMovingFooter() || isMovingContent())) {
                mTotalLoadMoreConsumed += Math.abs(dy);
                mIndicator.onFingerMove(mIndicator.getLastMovePoint()[0],
                        mIndicator.getLastMovePoint()[1] + Math.abs(dy));
                moveFooterPos(mIndicator.getOffsetY());
                consumed[1] = dy;
            } else if (mTotalLoadMoreUnconsumed != 0) {
                mTotalLoadMoreUnconsumed += dy;
                if (mTotalLoadMoreUnconsumed <= 0) {//over
                    mTotalLoadMoreUnconsumed = 0;
                }
                mIndicator.onFingerMove(mIndicator.getLastMovePoint()[0],
                        mIndicator.getLastMovePoint()[1] + Math.abs(dy));
                moveFooterPos(mIndicator.getOffsetY());
                consumed[1] = dy;
            } else if (!mIndicator.isInStartPosition() && isMovingFooter()) {
                mIndicator.onFingerMove(mIndicator.getLastMovePoint()[0],
                        mIndicator.getLastMovePoint()[1] + Math.abs(dy));
                moveFooterPos(mIndicator.getOffsetY());
                consumed[1] = dy;
            }
        }
        if (isMovingFooter() && mIndicator.hasLeftStartPosition()
                && mStatus == SR_STATUS_COMPLETE) {
            mScrollChecker.tryToScrollTo(IIndicator.DEFAULT_START_POS, 0);
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
            mIndicator.onFingerUp();
        }
        if (mIndicator.hasLeftStartPosition()) {
            onFingerUp(false);
        } else {
            notifyFingerUp();
        }
        mNestedScrollInProgress = false;
        mNestedFling = false;
        mTotalRefreshingUnconsumed = 0;
        mTotalRefreshingConsumed = 0;
        mTotalLoadMoreUnconsumed = 0;
        mTotalLoadMoreConsumed = 0;
        // Dispatch up our nested parent
        stopNestedScroll();
    }

    @Override
    public void onNestedScroll(final View target, final int dxConsumed, final int dyConsumed,
                               final int dxUnconsumed, final int dyUnconsumed) {
        if (sDebug) {
            SRLog.d(TAG, "onNestedScroll(): dxConsumed: %s, dyConsumed: %s, dxUnconsumed: %s" +
                    " dyUnconsumed: %s", dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
        }
        // Dispatch up to the nested parent first
        dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed,
                mParentOffsetInWindow);
        if (mScrollChecker.mIsRunning)
            return;
        final int dy = dyUnconsumed + mParentOffsetInWindow[1];
        if (dy < 0 && !isDisabledRefresh() && !canChildScrollUp()
                && (isMovingHeader() || isMovingContent())
                && !(isEnabledPinRefreshViewWhileLoading() && isRefreshing())) {
            float distance = mIndicator.getCanMoveTheMaxDistanceOfHeader();
            if (distance > 0 && mIndicator.getCurrentPosY() >= distance)
                return;
            mTotalRefreshingUnconsumed += Math.abs(dy);
            mIndicator.onFingerMove(mIndicator.getLastMovePoint()[0],
                    mIndicator.getLastMovePoint()[1] + Math.abs(dy));
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
            Log.d(getClass().getSimpleName(), "-----------" + mIndicator.getCurrentPosY() + " "
                    + mIndicator.getOffsetY());
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
            float newVelocityY = velocityY * mGestureDetector.getFriction();
            mOverScrollChecker.updateVelocityY(-newVelocityY / 5, newVelocityY > 0 ? -1 : 1);
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

    private void measureOtherViews(int widthMeasureSpec, int heightMeasureSpec) {
        final int measuredWidth = getMeasuredWidth();
        final int measuredHeight = getMeasuredHeight();
        final int count = mCachedViews.size();
        if (count > 1) {
            for (int i = 0; i < count; i++) {
                final View child = mCachedViews.get(i);
                final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();

                final int childWidthMeasureSpec;
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
            }
        }
        mCachedViews.clear();
    }

    private void measureContentView(View child,
                                    int parentWidthMeasureSpec,
                                    int parentHeightMeasureSpec) {
        final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
        final int childWidthMeasureSpec = getChildMeasureSpec(parentWidthMeasureSpec,
                getPaddingLeft() + getPaddingRight() + lp.leftMargin + lp.rightMargin, lp.width);
        final int currentPosY = mIndicator.getCurrentPosY();
        final int childHeightMeasureSpec;
        if ((isMovingHeader() || isRefreshing()) && lp.bottomMargin != 0 && lp.topMargin != 0
                && currentPosY > 0) {
            childHeightMeasureSpec = getChildMeasureSpec(parentHeightMeasureSpec,
                    getPaddingTop() + getPaddingBottom() + lp.topMargin + lp.bottomMargin
                            + currentPosY, lp.height);
        } else {
            childHeightMeasureSpec = getChildMeasureSpec(parentHeightMeasureSpec,
                    getPaddingTop() + getPaddingBottom() + lp.topMargin + lp.bottomMargin, lp.height);
        }
        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }

    private void checkViewsZTreeNeedReset() {
        final int count = getChildCount();
        if (mContentView == null)
            return;
        mCachedViews.clear();
        if (mViewsZTreeNeedReset && count > 0) {
            if (isEnabledHeaderDrawerStyle() && isEnabledFooterDrawerStyle()) {
                for (int i = count - 1; i >= 0; i--) {
                    View view = getChildAt(i);
                    if (view != mHeaderView && view != mFooterView)
                        mCachedViews.add(view);
                }
            } else if (isEnabledHeaderDrawerStyle()) {
                for (int i = count - 1; i >= 0; i--) {
                    View view = getChildAt(i);
                    if (view != mHeaderView)
                        mCachedViews.add(view);
                }
            } else if (isEnabledFooterDrawerStyle()) {
                for (int i = count - 1; i >= 0; i--) {
                    View view = getChildAt(i);
                    if (view != mFooterView)
                        mCachedViews.add(view);
                }
            } else {
                for (int i = count - 1; i >= 0; i--) {
                    View view = getChildAt(i);
                    if (view != mContentView)
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
                SRLog.d(TAG, "checkViewsZTreeNeedReset()");
            }
        }
        mViewsZTreeNeedReset = false;
    }

    private void reset() {
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
        if (mRefreshCompleteHook != null)
            mRefreshCompleteHook.mLayoutWeakRf.clear();
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
                mTriggeredAutoRefresh = true;
                mIndicator.setMovingStatus(IIndicator.MOVING_HEADER);
                mScrollChecker.tryToScrollTo(mIndicator.getOffsetToRefresh(), mDurationToCloseHeader);
            } else if (!mTriggeredAutoLoadMore) {
                mTriggeredAutoLoadMore = true;
                mIndicator.setMovingStatus(IIndicator.MOVING_FOOTER);
                mScrollChecker.tryToScrollTo(mIndicator.getOffsetToLoadMore(), mDurationToCloseFooter);
            }
        }
    }

    protected void ensureFreshView(View child) {
        if (child instanceof IRefreshView) {
            IRefreshView view = (IRefreshView) child;
            switch (view.getType()) {
                case IRefreshView.TYPE_HEADER:
                    if (mHeaderView != null)
                        throw new SRUnsupportedOperationException("Unsupported operation , " +
                                "HeaderView only can be add once !!");
                    mHeaderView = view;
                    break;
                case IRefreshView.TYPE_FOOTER:
                    if (mFooterView != null)
                        throw new SRUnsupportedOperationException("Unsupported operation , " +
                                "FooterView only can be add once !!");
                    mFooterView = view;
                    break;
            }
        }
    }

    private void ensureContent() {
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
                    if (!(child instanceof IRefreshView))
                        mContentView = child;
                }
            }
        }
    }

    private boolean processDispatchTouchEvent(MotionEvent ev) {
        mGestureDetector.onTouchEvent(ev);
        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mPreventForHorizontal = false;
                mDealHorizontalMove = false;
                mIndicator.onFingerUp();
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
            case MotionEvent.ACTION_DOWN:
                mHasSendCancelEvent = false;
                mIndicator.onFingerDown(ev.getX(), ev.getY());
                boolean movingFooter = isMovingFooter();
                boolean hasLeftStartPosition = mIndicator.hasLeftStartPosition();
                if ((!movingFooter && hasLeftStartPosition) || mStatus != SR_STATUS_COMPLETE) {
                    mScrollChecker.abortIfWorking();
                } else {
                    mScrollChecker.tryToScrollTo(IIndicator.DEFAULT_START_POS, 0);
                }
                mPreventForHorizontal = false;
                mDealHorizontalMove = false;
                super.dispatchTouchEvent(ev);
                return true;
            case MotionEvent.ACTION_MOVE:
                if (!mIndicator.hasTouched()) {
                    return super.dispatchTouchEvent(ev);
                }
                mLastMoveEvent = ev;
                mIndicator.onFingerMove(ev.getX(), ev.getY());
                float offsetX, offsetY;
                float[] pressDownPoint = mIndicator.getFingerDownPoint();
                offsetX = ev.getX() - pressDownPoint[0];
                offsetY = ev.getY() - pressDownPoint[1];
                if (!mDealHorizontalMove) {
                    if ((Math.abs(offsetX) >= mTouchSlop
                            && Math.abs(offsetX) > Math.abs(offsetY))) {
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
                if (mPreventForHorizontal) {
                    return super.dispatchTouchEvent(ev);
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
                if (movingDown && isMovingHeader() && maxHeaderDistance > 0) {
                    if (currentY >= maxHeaderDistance)
                        return super.dispatchTouchEvent(ev);
                    else if (currentY + offsetY > maxHeaderDistance) {
                        moveHeaderPos(maxHeaderDistance - currentY);
                        return true;
                    }
                }
                float maxFooterDistance = mIndicator.getCanMoveTheMaxDistanceOfFooter();
                if (!movingDown && isMovingFooter() && maxFooterDistance > 0) {
                    if (currentY >= maxFooterDistance)
                        return super.dispatchTouchEvent(ev);
                    else if (currentY - offsetY > maxFooterDistance) {
                        moveFooterPos(-(maxFooterDistance - currentY));
                        return true;
                    }
                }
                boolean canMoveUp = isMovingHeader() && mIndicator.hasLeftStartPosition()
                        && (mMode != MODE_LOAD_MORE);
                boolean canMoveDown = isMovingFooter() && mIndicator.hasLeftStartPosition()
                        && (mMode != MODE_REFRESH);
                boolean canHeaderMoveDown = !canChildScrollUp() && mMode != MODE_LOAD_MORE;
                boolean canFooterMoveUp = !canChildScrollDown() && mMode != MODE_REFRESH;
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

    private boolean canChildScrollUp() {
        if (mScrollUpCallback != null)
            return mScrollUpCallback.canChildScrollUp(this, mContentView, mHeaderView);
        return ScrollCompat.canChildScrollUp(mContentView);
    }

    private boolean canChildScrollDown() {
        if (mScrollDownCallback != null)
            return mScrollDownCallback.canChildScrollDown(this, mContentView, mFooterView);
        return ScrollCompat.canChildScrollDown(mContentView);
    }

    private void clearAutoRefreshFlag() {
        // remove auto fresh flag
        mFlag = mFlag & ~MASK_AUTO_REFRESH;
        if (sDebug) {
            SRLog.i(TAG, "clearAutoRefreshFlag()");
        }
    }

    private void sendCancelEvent() {
        if (sDebug) {
            SRLog.i(TAG, "sendCancelEvent()");
        }
        if (mLastMoveEvent == null) return;
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
        final MotionEvent last = mLastMoveEvent;
        MotionEvent e = MotionEvent.obtain(last.getDownTime(), last.getEventTime(),
                MotionEvent.ACTION_DOWN, last.getX(), last.getY(), last.getMetaState());
        super.dispatchTouchEvent(e);
    }

    private void notifyFingerUp() {
        if (sDebug) {
            SRLog.i(TAG, "notifyFingerUp()");
        }
        if (isMovingHeader() && mHeaderView != null && needCheckPos()) {
            mHeaderView.onFingerUp(this, mIndicator);
        } else if (isMovingFooter() && mFooterView != null && needCheckPos()) {
            mFooterView.onFingerUp(this, mIndicator);
        }
    }

    protected void onFingerUp(boolean stayForLoading) {
        if (sDebug) {
            SRLog.d(TAG, "onFingerUp(): stayForLoading: %s", stayForLoading);
        }
        notifyFingerUp();
        if (!stayForLoading && isEnabledKeepRefreshView()
                && needCheckPos() && mStatus != SR_STATUS_COMPLETE
                && !isRefreshing() && !isLoadingMore()) {
            if (isMovingHeader() && mIndicator.isOverOffsetToKeepHeaderWhileLoading()) {
                if (mIndicator.isAlreadyHere(mIndicator.getOffsetToKeepHeaderWhileLoading())
                        || isDisabledPerformRefresh()) {
                    onRelease(0);
                    return;
                }
                mScrollChecker.tryToScrollTo(mIndicator.getOffsetToKeepHeaderWhileLoading(),
                        mDurationOfBackToHeaderHeight);
            } else if (isMovingFooter() && mIndicator.isOverOffsetToKeepFooterWhileLoading()) {
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
        if (needCheckPos()) {
            tryToPerformRefresh();
        }
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
            notifyUIRefreshComplete();
        } else {
            tryScrollBackToTopByPercentDuration(duration);
        }
    }

    private void tryScrollBackToTopByPercentDuration(int duration) {
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

    protected void tryScrollBackToHeaderHeight() {
        mScrollChecker.tryToScrollTo(mIndicator.getOffsetToKeepHeaderWhileLoading(),
                mDurationOfBackToHeaderHeight);
    }

    protected void tryScrollBackToTop(int duration) {
        if (sDebug) {
            SRLog.d(TAG, "tryScrollBackToTop(): duration: %s", duration);
        }
        if (!mIndicator.hasTouched() && mIndicator.hasLeftStartPosition()) {
            mScrollChecker.tryToScrollTo(IIndicator.DEFAULT_START_POS, duration);
            return;
        }
        if (isMovingFooter() && mStatus == SR_STATUS_COMPLETE
                && mIndicator.hasJustBackToStartPosition()) {
            mScrollChecker.tryToScrollTo(IIndicator.DEFAULT_START_POS, duration);
        }
    }

    protected void notifyUIRefreshComplete() {
        if (sDebug) {
            SRLog.i(TAG, "notifyUIRefreshComplete()");
        }
        mIndicator.onRefreshComplete();
        if (mNeedNotifyRefreshComplete) {
            if (isMovingHeader() && mHeaderView != null) {
                mHeaderView.onRefreshComplete(this);
            } else if (isMovingFooter() && mFooterView != null) {
                mFooterView.onRefreshComplete(this);
            }
            if (mRefreshListener != null) {
                mRefreshListener.onRefreshComplete();
            }
            mNeedNotifyRefreshComplete = false;
        }
        tryScrollBackToTopByPercentDuration(0);
        tryToNotifyReset();
    }

    private void moveHeaderPos(float deltaY) {
        if (sDebug) {
            SRLog.d(TAG, "moveHeaderPos(): deltaY: %s", deltaY);
        }
        mIndicator.setMovingStatus(IIndicator.MOVING_HEADER);
        // to keep the consistence with refresh, need to converse the deltaY
        movePos(deltaY);
    }

    private void moveFooterPos(float deltaY) {
        if (sDebug) {
            SRLog.d(TAG, "moveFooterPos(): deltaY: %s", deltaY);
        }
        mIndicator.setMovingStatus(IIndicator.MOVING_FOOTER);
        //check if it is needed to compatible scroll
        if (!isEnabledPinContentView() && mIsLastRefreshSuccessful
                && (mStatus == SR_STATUS_COMPLETE
                || (isEnabledNextPtrAtOnce() && mStatus == SR_STATUS_PREPARE
                && !mOverScrollChecker.isScrolling()
                && !mIndicator.hasTouched()))) {
            if (sDebug) {
                SRLog.d(TAG, "moveFooterPos(): compatible scroll deltaY: %s", deltaY);
            }
            if (mLoadMoreScrollCallback == null) {
                if (mLoadMoreScrollTargetView != null)
                    ScrollCompat.scrollCompat(mLoadMoreScrollTargetView, deltaY);
                else
                    ScrollCompat.scrollCompat(mContentView, deltaY);
            } else {
                if (!mLoadMoreScrollCallback.onScroll(mContentView, deltaY))
                    ScrollCompat.scrollCompat(mContentView, deltaY);
            }
            Log.d(getClass().getSimpleName(), "-------------------Y:" + deltaY);
        }
        // to keep the consistence with refresh, need to converse the deltaY
        movePos(-deltaY);
    }

    private void movePos(float deltaY) {
        // has reached the top
        if (isMovingHeader() && deltaY < 0 && mIndicator.isInStartPosition()) {
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
        if (isRefreshing() || isMovingHeader())
            updatePos(change);
        else if (isLoadingMore() || isMovingFooter())
            updatePos(-change);
    }

    protected void tryToSendCancelEventToChild() {
        if (mIndicator.hasTouched() && !mHasSendCancelEvent
                && mIndicator.hasMovedAfterPressedDown() && !mNestedScrollInProgress) {
            mHasSendCancelEvent = true;
            sendCancelEvent();
        }
    }

    protected void updatePos(int change) {
        // once moved, cancel event will be sent to child
        tryToSendCancelEventToChild();
        // leave initiated position or just refresh complete
        if (needCheckPos() && ((mIndicator.hasJustLeftStartPosition() && mStatus == SR_STATUS_INIT)
                || (mIndicator.crossCompletePos() && mStatus == SR_STATUS_COMPLETE && isEnabledNextPtrAtOnce()))) {
            mStatus = SR_STATUS_PREPARE;
            @IIndicator.MovingStatus
            int status = mIndicator.getMovingStatus();
            switch (status) {
                case IIndicator.MOVING_CONTENT:
                    if (mHeaderView != null)
                        mHeaderView.onRefreshPrepare(this);
                    if (mFooterView != null)
                        mFooterView.onRefreshPrepare(this);
                    break;
                case IIndicator.MOVING_FOOTER:
                    if (mFooterView != null)
                        mFooterView.onRefreshPrepare(this);
                    break;
                case IIndicator.MOVING_HEADER:
                    if (mHeaderView != null)
                        mHeaderView.onRefreshPrepare(this);
                    break;
            }
        }

        // back to initiated position
        if (mIndicator.hasJustBackToStartPosition()) {
            tryToNotifyReset();
            // recover event to children
            if (mIndicator.hasTouched() && !mNestedScrollInProgress) {
                sendDownEvent();
            }
        }

        // try to perform refresh
        if (needCheckPos() && !mOverScrollChecker.isScrolling() && mStatus == SR_STATUS_PREPARE) {
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
            SRLog.d(TAG, "updatePos(): change: %s, current: %s last: %s",
                    change, mIndicator.getCurrentPosY(), mIndicator.getLastPosY());
        }
        if (mUIPositionChangedListener != null) {
            mUIPositionChangedListener.onChanged(mStatus, mIndicator);
        }
        final MarginLayoutParams lp = (MarginLayoutParams) mContentView.getLayoutParams();
        //check mode
        switch (mMode) {
            case MODE_NONE:
                //no moving
                invalidate();
                break;
            case MODE_REFRESH:
                if (mHeaderView != null && !isDisabledRefresh() && (isRefreshing() || isMovingHeader())) {
                    if (!isEnabledHeaderDrawerStyle() && lp.bottomMargin == 0)
                        mHeaderView.getView().offsetTopAndBottom(change);
                    mHeaderView.onRefreshPositionChanged(this, mStatus, mIndicator);
                }
                if (!isEnabledPinContentView()) {
                    mContentView.offsetTopAndBottom(change);
                    //check if the margin is zero, we need relayout to change the content height
                    if (isMovingHeader() || isRefreshing()) {
                        if (lp.bottomMargin != 0) {
                            mNeedScrollCompat = true;
                            requestLayout();
                        }
                    } else {
                        mNeedScrollCompat = false;
                    }
                }
                invalidate();
                break;
            case MODE_LOAD_MORE:
                if (mFooterView != null && !isDisabledLoadMore() && (isLoadingMore() || isMovingFooter())) {
                    if (!isEnabledFooterDrawerStyle() && lp.topMargin == 0)
                        mFooterView.getView().offsetTopAndBottom(change);
                    mFooterView.onRefreshPositionChanged(this, mStatus, mIndicator);
                }
                if (!isEnabledPinContentView()) {
                    if (mLoadMoreScrollTargetView != null && isMovingFooter())
                        mLoadMoreScrollTargetView.offsetTopAndBottom(change);
                    else
                        mContentView.offsetTopAndBottom(change);
                    //check if the margin is zero, we need relayout to change the content height
                    if (isMovingFooter() || isLoadingMore()) {
                        if (lp.topMargin != 0) {
                            mNeedScrollCompat = true;
                            requestLayout();
                        }
                    } else {
                        mNeedScrollCompat = false;
                    }
                }
                invalidate();
                break;
            case MODE_BOTH:
            case MODE_OVER_SCROLL:
                if (mHeaderView != null && mMode == MODE_BOTH && !isDisabledRefresh()
                        && (isRefreshing() || isMovingHeader())) {
                    if (!isEnabledHeaderDrawerStyle() && lp.bottomMargin == 0)
                        mHeaderView.getView().offsetTopAndBottom(change);
                    mHeaderView.onRefreshPositionChanged(this, mStatus, mIndicator);
                } else if (mFooterView != null && mMode == MODE_BOTH && !isDisabledLoadMore()
                        && (isLoadingMore() || isMovingFooter())) {
                    if (!isEnabledFooterDrawerStyle() && lp.topMargin == 0)
                        mFooterView.getView().offsetTopAndBottom(change);
                    mFooterView.onRefreshPositionChanged(this, mStatus, mIndicator);
                }
                if (!isEnabledPinContentView()) {
                    if (mLoadMoreScrollTargetView != null && isMovingFooter() && mMode == MODE_BOTH) {
                        mLoadMoreScrollTargetView.offsetTopAndBottom(change);
                    } else {
                        mContentView.offsetTopAndBottom(change);
                    }
                    //check if the margin is zero, we need relayout to change the content height
                    if (isMovingHeader() || isRefreshing()) {
                        if (lp.bottomMargin != 0) {
                            mNeedScrollCompat = true;
                            requestLayout();
                        }
                    } else if (isMovingFooter() || isLoadingMore()) {
                        if (lp.topMargin != 0) {
                            mNeedScrollCompat = true;
                            requestLayout();
                        }
                    } else {
                        mNeedScrollCompat = false;
                    }
                }
                invalidate();
                break;
        }
        //check need perform load more
        if (mStatus == SR_STATUS_PREPARE && change < 0 && isMovingFooter() && !canChildScrollDown()
                && !isDisabledLoadMore() && !isDisabledPerformLoadMore()
                && isEnabledWhenScrollingToBottomToPerformLoadMore()) {
            mStatus = SR_STATUS_LOADING_MORE;
            performRefresh();
        }
        if (!mOverScrollChecker.isScrolling() && mIndicator.isInStartPosition() && !mNeedScrollCompat) {
            if (sDebug) {
                SRLog.d(TAG, "movePos(): need relayout");
            }
            requestLayout();
        }
    }

    /**
     * When moving, only the specified mode needs to check the position
     *
     * @return Need check position
     */
    protected boolean needCheckPos() {
        return mMode == MODE_REFRESH || mMode == MODE_LOAD_MORE || mMode == MODE_BOTH;
    }

    protected boolean isMovingHeader() {
        return mIndicator.getMovingStatus() == IIndicator.MOVING_HEADER;
    }

    protected boolean isMovingContent() {
        return mIndicator.getMovingStatus() == IIndicator.MOVING_CONTENT;
    }

    protected boolean isMovingFooter() {
        return mIndicator.getMovingStatus() == IIndicator.MOVING_FOOTER;
    }

    /**
     * Check in over scrolling needs to scroll back to the start position
     *
     * @return Needs
     */
    private boolean needScrollBackToTop() {
        if (mOverScrollChecker.hasClamped() && !mIndicator.isInStartPosition()) {
            if (sDebug) {
                SRLog.i(TAG, "needScrollBackToTop()");
            }
            onRelease(mOverScrollChecker.getLastDuration());
            mOverScrollChecker.setClamped(false);
            return true;
        } else {
            return false;
        }
    }

    private boolean tryToNotifyReset() {
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
            mOverScrollChecker.destroy();
            clearAutoRefreshFlag();
            return true;
        }
        return false;
    }

    private void performRefreshComplete(boolean hook) {
        if (sDebug) {
            SRLog.i(TAG, "performRefreshComplete()");
        }
        if ((isRefreshing() || isLoadingMore()) && hook && mRefreshCompleteHook != null
                && !mRefreshCompleteHook.isEmpty()) {
            mRefreshCompleteHook.setLayout(this);
            mRefreshCompleteHook.doHook();
            return;
        }
        mStatus = SR_STATUS_COMPLETE;
        notifyUIRefreshComplete();
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
        //check mode
        switch (mMode) {
            case MODE_LOAD_MORE:
                if (isMovingHeader())
                    return;
                break;
            case MODE_REFRESH:
                if (isMovingFooter())
                    return;
                break;
            case MODE_NONE:
                return;
            case MODE_OVER_SCROLL:
                return;
            case MODE_BOTH:
                break;
        }
        if (isMovingHeader()
                && ((mIndicator.isOverOffsetToKeepHeaderWhileLoading() && isAutoRefresh())
                || (isEnabledKeepRefreshView() && !isDisabledPerformRefresh()
                && mIndicator.isOverOffsetToKeepHeaderWhileLoading())
                || (mIndicator.isOverOffsetToRefresh() && !isDisabledPerformRefresh()))) {
            mStatus = SR_STATUS_REFRESHING;
            performRefresh();
            return;
        }
        if (isMovingFooter()
                && ((mIndicator.isOverOffsetToKeepFooterWhileLoading() && isAutoRefresh())
                || (isEnabledKeepRefreshView() && !isDisabledPerformLoadMore()
                && mIndicator.isOverOffsetToKeepFooterWhileLoading())
                || (mIndicator.isOverOffsetToLoadMore() && !isDisabledPerformLoadMore()))) {
            mStatus = SR_STATUS_LOADING_MORE;
            performRefresh();
        }
    }

    protected boolean canPerformRefresh() {
        return !(mOverScrollChecker.hasClamped() || mOverScrollChecker.isScrolling()
                || isMovingContent());
    }

    /**
     * try check auto refresh later flag
     *
     * @return Performed
     */
    private boolean performAutoRefreshButLater() {
        return (mFlag & MASK_AUTO_REFRESH) == FLAG_AUTO_REFRESH_BUT_LATER;
    }

    protected void performRefresh() {
        //loading start milliseconds since boot
        mLoadingStartTime = SystemClock.uptimeMillis();
        mNeedNotifyRefreshComplete = true;
        if (sDebug) {
            SRLog.d(TAG, "onRefreshBegin systemTime: %s", mLoadingStartTime);
        }
        onRefresh();
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

    protected void onRefresh() {
        //ignore
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

    @IntDef({MODE_NONE, MODE_REFRESH, MODE_LOAD_MORE, MODE_OVER_SCROLL, MODE_BOTH})
    @Retention(RetentionPolicy.SOURCE)
    @interface Mode {
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
         */
        void onRefreshComplete();
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

    public interface OnLoadMoreScrollCallback {
        boolean onScroll(View content, float deltaY);
    }

    public interface OnHookUIRefreshCompleteCallBack {
        @MainThread
        void onHook(RefreshCompleteHook hook);
    }

    @SuppressWarnings("unused")
    public static class LayoutParams extends MarginLayoutParams {
        public static final int UNSPECIFIED_GRAVITY = -1;
        private int mGravity = UNSPECIFIED_GRAVITY;

        @SuppressWarnings("unused")
        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            final TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.SmoothRefreshLayout);
            mGravity = a.getInt(R.styleable.SmoothRefreshLayout_sr_layout_gravity, UNSPECIFIED_GRAVITY);
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

    private static class ScrollChecker implements Runnable {
        private int mLastY;
        private int mLastStart;
        private int mLastTo;
        private Scroller mScroller;
        private boolean mIsRunning = false;
        private WeakReference<SmoothRefreshLayout> mLayoutWeakRf;

        private ScrollChecker(SmoothRefreshLayout layout) {
            mLayoutWeakRf = new WeakReference<>(layout);
            mScroller = new Scroller(layout.getContext(), new DecelerateInterpolator());
        }

        public void run() {
            if (mLayoutWeakRf.get() == null)
                return;
            SmoothRefreshLayout layout = mLayoutWeakRf.get();
            boolean finished = !mScroller.computeScrollOffset() || mScroller.isFinished();
            int curY = mScroller.getCurrY();
            int deltaY = curY - mLastY;
            if (sDebug) {
                SRLog.d(TAG,
                        "ScrollChecker: run(): finished: %s, start: %s, to: %s, currentPos: %s, " +
                                "currentY:%s, last: %s, delta: %s",
                        finished, mLastStart, mLastTo, layout.mIndicator.getCurrentPosY(), curY,
                        mLastY, deltaY);
            }
            if (!finished) {
                mLastY = curY;
                if (layout.isRefreshing() || layout.isMovingHeader()) {
                    layout.moveHeaderPos(deltaY);
                } else if (layout.isLoadingMore() || layout.isMovingFooter()) {
                    layout.moveFooterPos(-deltaY);
                }
                layout.post(this);
            } else {
                if (!layout.needScrollBackToTop()) {
                    checkInStartPosition();
                    reset();
                    layout.onRelease(0);
                }
            }
        }

        private void checkInStartPosition() {
            if (mLayoutWeakRf.get() == null)
                return;
            SmoothRefreshLayout layout = mLayoutWeakRf.get();
            //It should have scrolled to the specified location, but it has not scrolled
            if (mLastTo == IIndicator.DEFAULT_START_POS
                    && !layout.mIndicator.isInStartPosition()) {
                int currentPos = layout.mIndicator.getCurrentPosY();
                int deltaY = IIndicator.DEFAULT_START_POS - currentPos;
                if (sDebug) {
                    SRLog.d(TAG, "ScrollChecker: checkInStartPosition(): deltaY: %s", deltaY);
                }
                if (layout.isRefreshing() || layout.isMovingHeader()) {
                    layout.moveHeaderPos(deltaY);
                } else if (layout.isLoadingMore() || layout.isMovingFooter()) {
                    layout.moveFooterPos(-deltaY);
                }
            }
        }

        private void reset() {
            if (sDebug) {
                SRLog.i(TAG, "ScrollChecker: reset()");
            }
            mIsRunning = false;
            mLastY = 0;
            SmoothRefreshLayout layout = mLayoutWeakRf.get();
            if (layout != null) {
                layout.mOverScrollChecker.abortIfWorking();
                layout.removeCallbacks(this);
            }
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
                if (mLayoutWeakRf.get() != null) {
                    mLayoutWeakRf.get().onPtrScrollAbort();
                }
                reset();
            }
        }

        private void tryToScrollTo(int to, int duration) {
            if (mLayoutWeakRf.get() == null)
                return;
            if (sDebug) {
                SRLog.i(TAG, "ScrollChecker: tryToScrollTo()");
            }
            SmoothRefreshLayout layout = mLayoutWeakRf.get();
            if (layout.mIndicator.isAlreadyHere(to)) {
                return;
            }
            mLastStart = layout.mIndicator.getCurrentPosY();
            mLastTo = to;
            int distance = to - mLastStart;
            layout.removeCallbacks(this);
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
                layout.post(this);
                mIsRunning = true;
            } else {
                if (layout.isMovingHeader()) {
                    layout.moveHeaderPos(distance);
                } else if (layout.isMovingFooter()) {
                    layout.moveFooterPos(-distance);
                }
                mIsRunning = false;
            }
        }
    }

    public static class RefreshCompleteHook {
        private WeakReference<SmoothRefreshLayout> mLayoutWeakRf;
        private OnHookUIRefreshCompleteCallBack mCallBack;

        private RefreshCompleteHook(SmoothRefreshLayout layout) {
            mLayoutWeakRf = new WeakReference<>(layout);
        }

        public void onHookComplete() {
            if (mLayoutWeakRf.get() != null) {
                if (sDebug) {
                    SRLog.i(TAG, "RefreshCompleteHook: onHookComplete()");
                }
                mLayoutWeakRf.get().performRefreshComplete(false);
            }
        }

        private void setHookCallBack(@NonNull OnHookUIRefreshCompleteCallBack callBack) {
            mCallBack = callBack;
        }

        private void setLayout(SmoothRefreshLayout layout) {
            if (mLayoutWeakRf.get() == null)
                mLayoutWeakRf = new WeakReference<>(layout);
        }

        private boolean isEmpty() {
            return null == mCallBack;
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
    private static class OverScrollChecker implements Runnable {
        private float mVelocityY;
        private int mTimes = 0;
        private int mDirection = 0;
        private int mLastDuration = 0;
        private boolean mScrolling = false;
        private boolean mClamped = false;
        private WeakReference<SmoothRefreshLayout> mLayoutWeakRf;

        private OverScrollChecker(SmoothRefreshLayout layout) {
            mLayoutWeakRf = new WeakReference<>(layout);
        }

        private void updateVelocityY(float vy, int direction) {
            if (mLayoutWeakRf.get() == null)
                return;
            if (sDebug) {
                SRLog.d(TAG, "OverScrollChecker: updateVelocityY(): vy: %s, direction: %s", vy,
                        direction);
            }
            SmoothRefreshLayout layout = mLayoutWeakRf.get();
            layout.removeCallbacks(this);
            mVelocityY = vy;
            mDirection = direction;
            layout.post(this);
        }

        private void reset() {
            mTimes = 0;
            mDirection = 0;
            mVelocityY = 0;
            if (sDebug) {
                SRLog.i(TAG, "OverScrollChecker: reset():");
            }
            if (mLayoutWeakRf.get() == null)
                return;
            SmoothRefreshLayout layout = mLayoutWeakRf.get();
            layout.removeCallbacks(this);
        }

        private boolean isScrolling() {
            return mScrolling;
        }

        private boolean hasClamped() {
            return mClamped;
        }

        private void setClamped(boolean clamped) {
            mClamped = clamped;
            if (sDebug) {
                SRLog.d(TAG, "OverScrollChecker: setClamped(): clamped: %s", clamped);
            }
        }

        private int getLastDuration() {
            return mLastDuration;
        }

        private void destroy() {
            reset();
            mScrolling = false;
            mClamped = false;
            mLastDuration = 0;
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
            mTimes = 0;
        }

        @Override
        public void run() {
            if (mLayoutWeakRf.get() == null)
                return;
            SmoothRefreshLayout layout = mLayoutWeakRf.get();
            layout.removeCallbacks(this);
            if (Math.abs(mVelocityY) <= OVER_SCROLL_MIN_VX || mScrolling
                    || mDirection == 0 || mTimes > 110) {
                mTimes = 0;
                return;
            }
            mScrolling = false;
            mClamped = false;
            if (mDirection > 0) {
                mVelocityY -= Math.abs(mVelocityY) * ViewConfiguration.getScrollFriction();
            } else {
                mVelocityY += Math.abs(mVelocityY) * ViewConfiguration.getScrollFriction();
            }
            if (sDebug) {
                SRLog.d(TAG, "OverScrollChecker: run(): vy: %s, direction: %s", mVelocityY, mDirection);
            }
            if (mDirection > 0) {
                if (!layout.canChildScrollUp() && !layout.isLoadingMore() && !(layout
                        .isEnabledPinRefreshViewWhileLoading() && layout.isRefreshing())) {
                    mLastDuration = Math.abs(Math.round(mVelocityY * .28f));
                    mLastDuration = mLastDuration > 350 ? 350 : mLastDuration;
                    float maxHeaderDistance = layout.mIndicator.getCanMoveTheMaxDistanceOfHeader();
                    int to = Math.round(mVelocityY * .1f);
                    if (maxHeaderDistance > 0 && to > maxHeaderDistance) {
                        to = Math.round(maxHeaderDistance);
                    }
                    if (sDebug) {
                        SRLog.d(TAG, "OverScrollChecker: run(): to: %s, duration: %s", to,
                                mLastDuration);
                    }
                    layout.mIndicator.setMovingStatus(IIndicator.MOVING_HEADER);
                    layout.mScrollChecker.tryToScrollTo(to, mLastDuration);
                    mClamped = true;
                    mScrolling = true;
                    reset();
                    return;
                }
            } else {
                if (!layout.canChildScrollDown() && !layout.isRefreshing() && !(layout
                        .isEnabledPinRefreshViewWhileLoading() && layout.isLoadingMore())) {
                    mLastDuration = Math.abs(Math.round(mVelocityY * .28f));
                    mLastDuration = mLastDuration > 350 ? 350 : mLastDuration;
                    float maxFooterDistance = layout.mIndicator.getCanMoveTheMaxDistanceOfFooter();
                    int to = Math.abs(Math.round(mVelocityY * .1f));
                    if (maxFooterDistance > 0 && to > maxFooterDistance) {
                        to = Math.round(maxFooterDistance);
                    }
                    if (sDebug) {
                        SRLog.d(TAG, "OverScrollChecker: run(): to: %s, duration: %s", -to,
                                mLastDuration);
                    }
                    layout.mIndicator.setMovingStatus(IIndicator.MOVING_FOOTER);
                    layout.mScrollChecker.tryToScrollTo(to, mLastDuration);
                    mScrolling = true;
                    mClamped = true;
                    reset();
                    return;
                }
            }
            mTimes++;
            layout.postDelayed(this, 10);
        }
    }

}
