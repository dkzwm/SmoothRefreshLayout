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
import android.util.AttributeSet;
import android.util.Log;
import android.view.animation.Interpolator;
import androidx.core.view.ViewCompat;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.FlingAnimation;
import androidx.dynamicanimation.animation.FloatPropertyCompat;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;
import me.dkzwm.widget.srl.config.Constants;
import me.dkzwm.widget.srl.indicator.IIndicator;

/** @author dkzwm */
public class DynamicReboundSmoothRefreshLayout extends SmoothRefreshLayout {

    public DynamicReboundSmoothRefreshLayout(Context context) {
        super(context);
    }

    public DynamicReboundSmoothRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DynamicReboundSmoothRefreshLayout(
            Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void createScrollerChecker() {
        mScrollChecker = new DynamicReboundScrollChecker();
    }

    @Override
    public void setMaxOverScrollDuration(int duration) {
        if (sDebug) {
            Log.e(TAG, "Calling this method will have no effect");
        }
    }

    @Override
    public void setMinOverScrollDuration(int duration) {
        if (sDebug) {
            Log.e(TAG, "Calling this method will have no effect");
        }
    }

    @Override
    public void setOnCalculateBounceCallback(OnCalculateBounceCallback callBack) {
        if (sDebug) {
            Log.e(TAG, "Calling this method will have no effect");
        }
    }

    public void setFrictionFactor(float factor) {
        if (mScrollChecker instanceof DynamicReboundScrollChecker) {
            ((DynamicReboundScrollChecker) mScrollChecker).mFrictionFactor = factor;
        } else {
            if (sDebug) {
                Log.e(TAG, "Calling this method will have no effect");
            }
        }
    }

    public void setStiffness(float stiffness) {
        if (mScrollChecker instanceof DynamicReboundScrollChecker) {
            ((DynamicReboundScrollChecker) mScrollChecker).mStiffness = stiffness;
        } else {
            if (sDebug) {
                Log.e(TAG, "Calling this method will have no effect");
            }
        }
    }

    public void setDampingRatio(float dampingRatio) {
        if (mScrollChecker instanceof DynamicReboundScrollChecker) {
            ((DynamicReboundScrollChecker) mScrollChecker).mDampingRatio = dampingRatio;
        } else {
            if (sDebug) {
                Log.e(TAG, "Calling this method will have no effect");
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        if (mScrollChecker instanceof DynamicReboundScrollChecker) {
            ((DynamicReboundScrollChecker) mScrollChecker).cleanAnimation();
        }
        super.onDetachedFromWindow();
    }

    private static class ReboundProperty extends FloatPropertyCompat {
        private float mValue;

        private ReboundProperty() {
            super("Rebound");
        }

        @Override
        public float getValue(Object object) {
            return mValue;
        }

        @Override
        public void setValue(Object object, float value) {
            mValue = value;
        }
    }

    class DynamicReboundScrollChecker extends ScrollChecker
            implements DynamicAnimation.OnAnimationUpdateListener,
                    DynamicAnimation.OnAnimationEndListener {
        private FlingAnimation mFlingAnimation;
        private SpringAnimation mSpringAnimation;
        private float mFrictionFactor = 3.5f;
        private float mStiffness = SpringForce.STIFFNESS_LOW;
        private float mDampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY;
        private ReboundProperty mReboundProperty = new ReboundProperty();

        @Override
        public void run() {
            if (isAnimationRunning()) {
                return;
            }
            super.run();
        }

        @Override
        public void onAnimationEnd(
                DynamicAnimation animation, boolean canceled, float value, float velocity) {
            if (sDebug) {
                Log.d(
                        TAG,
                        String.format(
                                "ScrollChecker: onAnimationEnd(): mode: %d, curPos: %d, canceled: %b",
                                mMode, mIndicator.getCurrentPos(), canceled));
            }
            if (!canceled) {
                switch (mMode) {
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
                }
            }
        }

        @Override
        public void onAnimationUpdate(DynamicAnimation animation, float value, float velocity) {
            float deltaY = value - mLastY;
            if (sDebug) {
                Log.d(
                        TAG,
                        String.format(
                                "ScrollChecker: onAnimationUpdate(): mode: %d, curPos: %d, curY: %f, last: %f, delta: %f",
                                mMode, mIndicator.getCurrentPos(), value, mLastY, deltaY));
            }
            mLastY = value;
            if (isMovingHeader()) {
                moveHeaderPos(deltaY);
            } else if (isMovingFooter()) {
                if (isPreFling()) {
                    moveFooterPos(deltaY);
                } else {
                    moveFooterPos(-deltaY);
                }
            }
            tryToDispatchNestedFling();
        }

        @Override
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
                        startBounce(velocity, false);
                        return;
                    } else if (mVelocity < 0
                            && mIndicator.isAlreadyHere(IIndicator.START_POS)
                            && !isNotYetInEdgeCannotMoveFooter()) {
                        final float velocity = Math.abs(getCurrVelocity());
                        stop();
                        mIndicatorSetter.setMovingStatus(Constants.MOVING_FOOTER);
                        startBounce(velocity, false);
                        return;
                    }
                }
                invalidate();
            }
        }

        @Override
        void startPreFling(float velocity) {
            if (isMovingFooter()) {
                if (velocity > 0) {
                    super.startPreFling(velocity);
                    return;
                }
            } else if (isMovingHeader()) {
                if (velocity < 0) {
                    super.startPreFling(velocity);
                    return;
                }
            }
            stop();
            mVelocity = velocity;
            if (sDebug) {
                Log.d(TAG, String.format("ScrollChecker: startPreFling(): v: %s", velocity));
            }
            startBounce(velocity, true);
        }

        @Override
        void scrollTo(int to, int duration) {
            if (to < mIndicator.getCurrentPos()) {
                if (mScrollChecker.isFlingBack()) {
                    startFlingBack(to);
                    return;
                }
            }
            super.scrollTo(to, duration);
        }

        @Override
        void setInterpolator(Interpolator interpolator) {
            if (isAnimationRunning()) {
                return;
            }
            super.setInterpolator(interpolator);
        }

        @Override
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
                if (mFlingAnimation != null) {
                    mFlingAnimation.cancel();
                }
                if (mSpringAnimation != null) {
                    mSpringAnimation.cancel();
                }
                mDuration = 0;
                mLastY = 0;
                mLastTo = -1;
                mLastStart = 0;
                removeCallbacks(this);
            }
        }

        void cleanAnimation() {
            if (mFlingAnimation != null) {
                mFlingAnimation.cancel();
                mFlingAnimation.removeUpdateListener(this);
                mFlingAnimation.removeEndListener(this);
                mFlingAnimation = null;
            }
            if (mSpringAnimation != null) {
                mSpringAnimation.cancel();
                mSpringAnimation.removeUpdateListener(this);
                mSpringAnimation.removeEndListener(this);
                mSpringAnimation = null;
            }
        }

        void startBounce(float velocity, boolean preFling) {
            if (sDebug) {
                Log.d(
                        TAG,
                        String.format(
                                "ScrollChecker: startBounce(): velocity: %f, preFling: %b",
                                velocity, preFling));
            }
            mMode = preFling ? Constants.SCROLLER_MODE_PRE_FLING : Constants.SCROLLER_MODE_FLING;
            mLastStart = mIndicator.getCurrentPos();
            mIsScrolling = true;
            removeCallbacks(this);
            if (mFlingAnimation == null) {
                mFlingAnimation = new FlingAnimation(this, mReboundProperty);
                mFlingAnimation.addUpdateListener(this);
                mFlingAnimation.addEndListener(this);
            } else {
                mReboundProperty.mValue = 0;
            }
            mFlingAnimation.setStartVelocity(velocity);
            mFlingAnimation.setFriction((float) Math.pow(Math.abs(velocity), 1 / mFrictionFactor));
            mFlingAnimation.start();
        }

        void startFlingBack(int to) {
            if (sDebug) {
                Log.d(TAG, String.format("ScrollChecker: startFlingBack(): to: %d", to));
            }
            stop();
            mMode = Constants.SCROLLER_MODE_FLING_BACK;
            if (mSpringAnimation == null) {
                mSpringAnimation = new SpringAnimation(this, mReboundProperty, to);
                mSpringAnimation.addUpdateListener(this);
                mSpringAnimation.addEndListener(this);
            }
            mLastY = mIndicator.getCurrentPos();
            mSpringAnimation.setStartValue(mLastY);
            mSpringAnimation
                    .getSpring()
                    .setStiffness(mStiffness)
                    .setDampingRatio(mDampingRatio)
                    .setFinalPosition(to);
            mSpringAnimation.start();
        }

        private boolean isAnimationRunning() {
            if ((mSpringAnimation != null && mSpringAnimation.isRunning())
                    || (mFlingAnimation != null && mFlingAnimation.isRunning())) {
                if (sDebug) {
                    Log.e(TAG, "Calling this method will have no effect");
                }
                return true;
            }
            return false;
        }
    }
}
