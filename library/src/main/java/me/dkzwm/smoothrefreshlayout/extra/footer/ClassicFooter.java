package me.dkzwm.smoothrefreshlayout.extra.footer;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.TextView;

import me.dkzwm.smoothrefreshlayout.R;
import me.dkzwm.smoothrefreshlayout.SmoothRefreshLayout;
import me.dkzwm.smoothrefreshlayout.extra.ClassicConfig;
import me.dkzwm.smoothrefreshlayout.extra.IRefreshView;
import me.dkzwm.smoothrefreshlayout.indicator.IIndicator;

/**
 * @author dkzwm
 */
public class ClassicFooter extends FrameLayout implements IRefreshView {
    protected RotateAnimation mFlipAnimation;
    protected RotateAnimation mReverseFlipAnimation;
    protected TextView mLastUpdateTextView;
    protected TextView mTitleTextView;
    protected View mRotateView;
    protected View mProgressBar;
    protected String mLastUpdateTimeKey;
    protected boolean mShouldShowLastUpdate;
    protected long mLastUpdateTime = -1;
    protected int mRotateAniTime = 200;
    @RefreshViewStyle
    protected int mStyle = STYLE_DEFAULT;
    private LastUpdateTimeUpdater mLastUpdateTimeUpdater;
    private boolean mNoMoreDataChangedView = false;

    public ClassicFooter(Context context) {
        this(context, null);
    }

    public ClassicFooter(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClassicFooter(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (attrs != null) {
            final TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.IRefreshView, 0, 0);
            @RefreshViewStyle
            int style = arr.getInt(R.styleable.IRefreshView_sr_style, mStyle);
            mStyle = style;
            arr.recycle();
        }
        mFlipAnimation = new RotateAnimation(0, -180, RotateAnimation.RELATIVE_TO_SELF,
                0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        mFlipAnimation.setInterpolator(new LinearInterpolator());
        mFlipAnimation.setDuration(mRotateAniTime);
        mFlipAnimation.setFillAfter(true);

        mReverseFlipAnimation = new RotateAnimation(-180, 0, RotateAnimation.RELATIVE_TO_SELF,
                0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        mReverseFlipAnimation.setInterpolator(new LinearInterpolator());
        mReverseFlipAnimation.setDuration(mRotateAniTime);
        mReverseFlipAnimation.setFillAfter(true);
        View header = LayoutInflater.from(getContext()).inflate(R.layout.sr_classic_footer, this);
        mRotateView = header.findViewById(R.id.view_footer_rotate);
        mTitleTextView = (TextView) header.findViewById(R.id.textView_footer_title);
        mLastUpdateTextView = (TextView) header.findViewById(R.id.textView_footer_last_update);
        mProgressBar = header.findViewById(R.id.progressBar_footer);
        mLastUpdateTimeUpdater = new LastUpdateTimeUpdater();
        mRotateView.clearAnimation();
        mRotateView.setVisibility(INVISIBLE);
        mProgressBar.setVisibility(INVISIBLE);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mLastUpdateTimeUpdater != null) {
            mLastUpdateTimeUpdater.stop();
        }
        mFlipAnimation.cancel();
        mReverseFlipAnimation.cancel();
        removeCallbacks(mLastUpdateTimeUpdater);
        if (getHandler() != null)
            getHandler().removeCallbacksAndMessages(null);
    }

    public void setRotateAniTime(int time) {
        if (time == mRotateAniTime || time == 0) {
            return;
        }
        mRotateAniTime = time;
        mFlipAnimation.setDuration(mRotateAniTime);
        mReverseFlipAnimation.setDuration(mRotateAniTime);
    }

    public void setLastUpdateTimeKey(String key) {
        if (TextUtils.isEmpty(key)) {
            return;
        }
        mLastUpdateTimeKey = key;
    }

    @Override
    public int getType() {
        return TYPE_FOOTER;
    }

    @Override
    public int getStyle() {
        return mStyle;
    }

    public void setStyle(@RefreshViewStyle int style) {
        mStyle = style;
        requestLayout();
    }

    @Override
    public int getCustomHeight() {
        return getResources().getDimensionPixelOffset(R.dimen.sr_header_default_height);
    }

    @NonNull
    @Override
    public View getView() {
        return this;
    }

    @Override
    public void onReset(SmoothRefreshLayout frame) {
        mRotateView.clearAnimation();
        mRotateView.setVisibility(INVISIBLE);
        mProgressBar.setVisibility(INVISIBLE);
        mShouldShowLastUpdate = true;
        mNoMoreDataChangedView = false;
        mLastUpdateTimeUpdater.stop();
        tryUpdateLastUpdateTime();
    }

    @Override
    public void onRefreshPrepare(SmoothRefreshLayout frame) {
        mShouldShowLastUpdate = true;
        mNoMoreDataChangedView = false;
        tryUpdateLastUpdateTime();
        mLastUpdateTimeUpdater.start();
        mProgressBar.setVisibility(INVISIBLE);
        mRotateView.setVisibility(VISIBLE);
        mTitleTextView.setVisibility(VISIBLE);
        if (frame.isEnabledLoadMoreNoMoreData()) {
            mTitleTextView.setText(R.string.sr_no_more_data);
        } else if (frame.isEnabledPullToRefresh() && !frame.isDisabledPerformLoadMore()) {
            mTitleTextView.setText(R.string.sr_pull_up_to_load);
        } else {
            mTitleTextView.setText(R.string.sr_pull_up);
        }
    }

    @Override
    public void onFingerUp(SmoothRefreshLayout layout, IIndicator indicator) {

    }

    @Override
    public void onRefreshBegin(SmoothRefreshLayout frame, IIndicator indicator) {
        mShouldShowLastUpdate = false;
        mRotateView.clearAnimation();
        mRotateView.setVisibility(INVISIBLE);
        mProgressBar.setVisibility(VISIBLE);
        mTitleTextView.setVisibility(VISIBLE);
        mTitleTextView.setText(R.string.sr_loading);
        tryUpdateLastUpdateTime();
        mLastUpdateTimeUpdater.stop();
    }

    @Override
    public void onRefreshComplete(SmoothRefreshLayout frame, boolean isSuccessful) {
        mRotateView.clearAnimation();
        mRotateView.setVisibility(INVISIBLE);
        mProgressBar.setVisibility(INVISIBLE);
        mTitleTextView.setVisibility(VISIBLE);
        if (frame.isRefreshSuccessful()) {
            mTitleTextView.setText(R.string.sr_load_complete);
            mLastUpdateTime = System.currentTimeMillis();
            ClassicConfig.updateTime(getContext(), mLastUpdateTimeKey, mLastUpdateTime);
        } else {
            mTitleTextView.setText(R.string.sr_load_failed);
        }
    }

    private void tryUpdateLastUpdateTime() {
        if (TextUtils.isEmpty(mLastUpdateTimeKey) || !mShouldShowLastUpdate) {
            mLastUpdateTextView.setVisibility(GONE);
        } else {
            String time = ClassicConfig.getLastUpdateTime(getContext(), mLastUpdateTime, mLastUpdateTimeKey);
            if (TextUtils.isEmpty(time)) {
                mLastUpdateTextView.setVisibility(GONE);
            } else {
                mLastUpdateTextView.setVisibility(VISIBLE);
                mLastUpdateTextView.setText(time);
            }
        }
    }

    @Override
    public void onRefreshPositionChanged(SmoothRefreshLayout frame, byte status, IIndicator indicator) {
        final int mOffsetToRefresh = indicator.getOffsetToLoadMore();
        final int currentPos = indicator.getCurrentPosY();
        final int lastPos = indicator.getLastPosY();

        if (frame.isEnabledLoadMoreNoMoreData() && !mNoMoreDataChangedView) {
            mTitleTextView.setVisibility(VISIBLE);
            mLastUpdateTextView.setVisibility(GONE);
            mProgressBar.setVisibility(INVISIBLE);
            mLastUpdateTimeUpdater.stop();
            mRotateView.clearAnimation();
            mRotateView.setVisibility(GONE);
            mTitleTextView.setText(R.string.sr_no_more_data);
            mNoMoreDataChangedView = true;
            return;
        }
        mNoMoreDataChangedView = false;
        if (currentPos < mOffsetToRefresh && lastPos >= mOffsetToRefresh) {
            if (indicator.hasTouched() && status == SmoothRefreshLayout.SR_STATUS_PREPARE) {
                mTitleTextView.setVisibility(VISIBLE);
                if (frame.isEnabledPullToRefresh() && !frame.isDisabledPerformLoadMore()) {
                    mTitleTextView.setText(R.string.sr_pull_up_to_load);
                } else {
                    mTitleTextView.setText(R.string.sr_pull_up);
                }
                if (mRotateView != null) {
                    mRotateView.clearAnimation();
                    mRotateView.startAnimation(mReverseFlipAnimation);
                }
            }
        } else if (currentPos > mOffsetToRefresh && lastPos <= mOffsetToRefresh) {
            if (indicator.hasTouched() && status == SmoothRefreshLayout.SR_STATUS_PREPARE) {
                if (!frame.isEnabledPullToRefresh() && !frame.isDisabledPerformLoadMore()) {
                    mTitleTextView.setVisibility(VISIBLE);
                    mTitleTextView.setText(R.string.sr_release_to_load);
                }
                if (mRotateView != null) {
                    mRotateView.clearAnimation();
                    mRotateView.startAnimation(mFlipAnimation);
                }
            }
        }
    }

    private class LastUpdateTimeUpdater implements Runnable {

        private boolean mRunning = false;

        private void start() {
            if (TextUtils.isEmpty(mLastUpdateTimeKey)) {
                return;
            }
            mRunning = true;
            run();
        }

        private void stop() {
            mRunning = false;
            removeCallbacks(this);
        }

        @Override
        public void run() {
            tryUpdateLastUpdateTime();
            if (mRunning) {
                postDelayed(this, 1000);
            }
        }
    }
}
