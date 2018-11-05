package me.dkzwm.widget.srl.indicator;

import android.util.Log;

/**
 * Created by dkzwm on 2017/6/12.
 *
 * @author dkzwm
 */

public class DefaultTwoLevelIndicator extends DefaultIndicator implements ITwoLevelIndicator
        , ITwoLevelIndicatorSetter {
    private int mOffsetToHintTwoLevelRefresh = 0;
    private int mOffsetToTwoLevelRefresh = 0;
    private float mOffsetRatioToKeepTwoLevelHeaderWhileLoading = 1f;
    private float mRatioOfHeaderHeightToHintTwoLevelRefresh = 1.5f;
    private float mRatioOfHeaderHeightToTwoLevelRefresh = 2.0f;

    @Override
    public void setHeaderHeight(int height) {
        super.setHeaderHeight(height);
        mOffsetToHintTwoLevelRefresh = (int) (mHeaderHeight *
                mRatioOfHeaderHeightToHintTwoLevelRefresh);
        mOffsetToTwoLevelRefresh = (int) (mHeaderHeight *
                mRatioOfHeaderHeightToTwoLevelRefresh);
    }

    @Override
    public void setRatioOfHeaderToHintTwoLevel(float ratio) {
        mRatioOfHeaderHeightToHintTwoLevelRefresh = ratio;
        mOffsetToHintTwoLevelRefresh = (int) (mHeaderHeight * ratio);
    }

    @Override
    public void setRatioOfHeaderToTwoLevel(float ratio) {
        mRatioOfHeaderHeightToTwoLevelRefresh = ratio;
        mOffsetToTwoLevelRefresh = (int) (mHeaderHeight * ratio);
    }

    @Override
    public void checkConfig() {
        super.checkConfig();
        if (mRatioOfHeaderHeightToHintTwoLevelRefresh >= mRatioOfHeaderHeightToTwoLevelRefresh) {
            Log.w(getClass().getSimpleName(), "If the height ratio of the Two-Level refresh is " +
                    "less than the height ratio of the triggered Two-Level hint, the Two-Level " +
                    "refresh will never be triggered!");
        }
    }

    @Override
    public void setRatioToKeepTwoLevelHeader(float ratio) {
        mOffsetRatioToKeepTwoLevelHeaderWhileLoading = ratio;
    }

    @Override
    public int getOffsetToKeepTwoLevelHeader() {
        return (int) (mOffsetRatioToKeepTwoLevelHeaderWhileLoading * mHeaderHeight);
    }

    @Override
    public int getOffsetToTwoLevelRefresh() {
        return mOffsetToTwoLevelRefresh;
    }

    @Override
    public int getOffsetToHintTwoLevelRefresh() {
        return mOffsetToHintTwoLevelRefresh;
    }

    @Override
    public boolean crossTwoLevelRefreshLine() {
        return mCurrentPos >= mOffsetToTwoLevelRefresh;
    }

    @Override
    public boolean crossTwoLevelHintLine() {
        return mCurrentPos >= mOffsetToHintTwoLevelRefresh;
    }

}
