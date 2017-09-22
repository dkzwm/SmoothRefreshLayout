package me.dkzwm.widget.srl.extra.header;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import me.dkzwm.widget.srl.R;
import me.dkzwm.widget.srl.SmoothRefreshLayout;
import me.dkzwm.widget.srl.extra.ClassicConfig;
import me.dkzwm.widget.srl.extra.IRefreshView;
import me.dkzwm.widget.srl.indicator.IIndicator;

/**
 * @author dkzwm
 */
public class ClassicHeader extends FrameLayout implements IRefreshView {
    protected RotateAnimation mFlipAnimation;
    protected RotateAnimation mReverseFlipAnimation;
    protected TextView mTitleTextView;
    protected TextView mLastUpdateTextView;
    protected ImageView mRotateView;
    protected ProgressBar mProgressBar;
    protected String mLastUpdateTimeKey;
    protected boolean mShouldShowLastUpdate;
    protected long mLastUpdateTime = -1;
    protected int mRotateAniTime = 200;
    @RefreshViewStyle
    protected int mStyle = STYLE_DEFAULT;
    private LastUpdateTimeUpdater mLastUpdateTimeUpdater;


    public ClassicHeader(Context context) {
        this(context, null);
    }

    public ClassicHeader(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClassicHeader(Context context, AttributeSet attrs, int defStyle) {
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
        View header = LayoutInflater.from(getContext()).inflate(R.layout.sr_classic_header, this);
        mRotateView = (ImageView) header.findViewById(R.id.view_header_rotate);
        mTitleTextView = (TextView) header.findViewById(R.id.textView_header_title);
        mLastUpdateTextView = (TextView) header.findViewById(R.id.textView_header_last_update);
        mProgressBar = (ProgressBar) header.findViewById(R.id.progressBar_header);
        mLastUpdateTimeUpdater = new LastUpdateTimeUpdater();
        mRotateView.clearAnimation();
        mRotateView.setVisibility(INVISIBLE);
        mProgressBar.setVisibility(INVISIBLE);
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mLastUpdateTimeUpdater.stop();
        mFlipAnimation.cancel();
        mReverseFlipAnimation.cancel();
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


    public void setTitleTextColor(@ColorInt int color) {
        mTitleTextView.setTextColor(color);
    }

    public void setLastUpdateTextColor(@ColorInt int color) {
        mLastUpdateTextView.setTextColor(color);
    }

    public void setLastUpdateTimeKey(String key) {
        if (TextUtils.isEmpty(key)) {
            return;
        }
        mLastUpdateTimeKey = key;
    }

    @Override
    public int getType() {
        return TYPE_HEADER;
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
        mLastUpdateTimeUpdater.stop();
        mShouldShowLastUpdate = true;
        tryUpdateLastUpdateTime();
    }

    @Override
    public void onRefreshPrepare(SmoothRefreshLayout frame) {
        mShouldShowLastUpdate = true;
        tryUpdateLastUpdateTime();
        mLastUpdateTimeUpdater.start();
        mProgressBar.setVisibility(INVISIBLE);
        mRotateView.setVisibility(VISIBLE);
        mTitleTextView.setVisibility(VISIBLE);
        if (frame.isEnabledPullToRefresh()) {
            mTitleTextView.setText(R.string.sr_pull_down_to_refresh);
        } else {
            mTitleTextView.setText(R.string.sr_pull_down);
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
        mTitleTextView.setText(R.string.sr_refreshing);
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
            mTitleTextView.setText(R.string.sr_refresh_complete);
            mLastUpdateTime = System.currentTimeMillis();
            ClassicConfig.updateTime(getContext(), mLastUpdateTimeKey, mLastUpdateTime);
        } else
            mTitleTextView.setText(R.string.sr_refresh_failed);
    }


    @Override
    public void onRefreshPositionChanged(SmoothRefreshLayout frame, byte status, IIndicator indicator) {
        final int mOffsetToRefresh = indicator.getOffsetToRefresh();
        final int currentPos = indicator.getCurrentPosY();
        final int lastPos = indicator.getLastPosY();

        if (currentPos < mOffsetToRefresh && lastPos >= mOffsetToRefresh) {
            if (indicator.hasTouched() && status == SmoothRefreshLayout.SR_STATUS_PREPARE) {
                mTitleTextView.setVisibility(VISIBLE);
                if (frame.isEnabledPullToRefresh()) {
                    mTitleTextView.setText(R.string.sr_pull_down_to_refresh);
                } else {
                    mTitleTextView.setText(R.string.sr_pull_down);
                }
                mRotateView.setVisibility(VISIBLE);
                mRotateView.clearAnimation();
                mRotateView.startAnimation(mReverseFlipAnimation);
            }
        } else if (currentPos > mOffsetToRefresh && lastPos <= mOffsetToRefresh) {
            if (indicator.hasTouched() && status == SmoothRefreshLayout.SR_STATUS_PREPARE) {
                mTitleTextView.setVisibility(VISIBLE);
                if (!frame.isEnabledPullToRefresh()) {
                    mTitleTextView.setText(R.string.sr_release_to_refresh);
                }
                mRotateView.setVisibility(VISIBLE);
                mRotateView.clearAnimation();
                mRotateView.startAnimation(mFlipAnimation);
            }
        }
    }

    @Override
    public void onPureScrollPositionChanged(SmoothRefreshLayout layout, byte status, IIndicator indicator) {
        if (indicator.hasJustLeftStartPosition()) {
            mShouldShowLastUpdate = false;
            tryUpdateLastUpdateTime();
            mLastUpdateTimeUpdater.stop();
            mProgressBar.setVisibility(GONE);
            mRotateView.setVisibility(GONE);
            mTitleTextView.setVisibility(GONE);
            if (layout.isEnabledPullToRefresh()) {
                mTitleTextView.setText(R.string.sr_pull_down_to_refresh);
            } else {
                mTitleTextView.setText(R.string.sr_pull_down);
            }
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

    private class LastUpdateTimeUpdater implements Runnable {

        private boolean mRunning = false;

        private void start() {
            if (TextUtils.isEmpty(mLastUpdateTimeKey)) {
                return;
            }
            mRunning = true;
            ClassicHeader.this.post(this);
        }

        private void stop() {
            mRunning = false;
            ClassicHeader.this.removeCallbacks(this);
        }

        @Override
        public void run() {
            ClassicHeader.this.tryUpdateLastUpdateTime();
            if (mRunning) {
                ClassicHeader.this.postDelayed(this, 1000);
            }
        }
    }
}
