package me.dkzwm.smoothrefreshlayout.indicator;

import me.dkzwm.smoothrefreshlayout.exception.SRUIRuntimeException;

/**
 * Created by dkzwm on 2017/6/12.
 *
 * @author dkzwm
 */

public class DefaultTwoLevelIndicator extends DefaultIndicator implements ITwoLevelIndicator {
    private int mOffsetToHintTwoLevelRefresh = 0;
    private int mOffsetToTwoLevelRefresh = 0;
    private int mTwoLevelRefreshCompleteY;
    private float mRatioOfHeaderHeightToHintTwoLevelRefresh = 1.5f;
    private float mRatioOfHeaderHeightToTwoLevelRefresh = 2.0f;

    @Override
    public boolean crossTwoLevelCompletePos() {
        return mCurrentPos >= mTwoLevelRefreshCompleteY;
    }

    @Override
    public void onTwoLevelRefreshComplete() {
        mTwoLevelRefreshCompleteY = mCurrentPos;
    }

    @Override
    public void setRatioOfHeaderHeightToHintTwoLevelRefresh(float ratio) {
        mRatioOfHeaderHeightToHintTwoLevelRefresh = ratio;
        mOffsetToHintTwoLevelRefresh = (int) (mHeaderHeight * ratio);
    }

    @Override
    public void setRatioOfHeaderHeightToTwoLevelRefresh(float ratio) {
        if (mRatioOfHeaderHeightToHintTwoLevelRefresh >= mRatioOfHeaderHeightToTwoLevelRefresh) {
            throw new SRUIRuntimeException("If RatioOfHeaderHeightToTwoLevelRefresh less than " +
                    "RatioOfHeaderHeightToHintTwoLevelRefresh, Two level refresh will never be " +
                    "trigger!");
        }
        mRatioOfHeaderHeightToTwoLevelRefresh = ratio;
        mOffsetToTwoLevelRefresh = (int) (mHeaderHeight * ratio);
    }

    @Override
    public float getRatioOfHeaderHeightToHintTwoLevelRefresh() {
        return mRatioOfHeaderHeightToHintTwoLevelRefresh;
    }

    @Override
    public float getRatioOfHeaderHeightToTwoLevelRefresh() {
        return mRatioOfHeaderHeightToTwoLevelRefresh;
    }

    @Override
    public int getOffsetToTwoLevelRefresh() {
        return mOffsetToTwoLevelRefresh == 0
                ? Math.round(mHeaderHeight * mRatioOfHeaderHeightToTwoLevelRefresh)
                : mHeaderHeight;
    }

    @Override
    public int getOffsetToHintTwoLevelRefresh() {
        return mOffsetToHintTwoLevelRefresh == 0
                ? Math.round(mHeaderHeight * mRatioOfHeaderHeightToHintTwoLevelRefresh)
                : mHeaderHeight;
    }

    @Override
    public boolean crossTwoLevelRefreshLine() {
        return mCurrentPos >= getOffsetToTwoLevelRefresh();
    }

    @Override
    public boolean crossTwoLevelHintLine() {
        return mCurrentPos >= getOffsetToHintTwoLevelRefresh();
    }


}
