package me.dkzwm.widget.srl.indicator;

/**
 * Created by dkzwm on 2017/6/12.
 *
 * @author dkzwm
 */

public class DefaultTwoLevelIndicator extends DefaultIndicator implements ITwoLevelIndicator {
    private int mOffsetToHintTwoLevelRefresh = 0;
    private int mOffsetToTwoLevelRefresh = 0;
    private int mTwoLevelRefreshCompleteY;
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
    public boolean crossTwoLevelCompletePos() {
        return mCurrentPos >= mTwoLevelRefreshCompleteY;
    }

    @Override
    public void onTwoLevelRefreshComplete() {
        mTwoLevelRefreshCompleteY = mCurrentPos;
    }

    @Override
    public void setRatioOfHeaderToHintTwoLevel(float ratio) {
        mRatioOfHeaderHeightToHintTwoLevelRefresh = ratio;
        mOffsetToHintTwoLevelRefresh = (int) (mHeaderHeight * ratio);
    }

    @Override
    public void setRatioOfHeaderToTwoLevel(float ratio) {
        if (mRatioOfHeaderHeightToHintTwoLevelRefresh >= mRatioOfHeaderHeightToTwoLevelRefresh) {
            throw new RuntimeException("If RatioOfHeaderHeightToTwoLevelRefresh less than " +
                    "RatioOfHeaderHeightToHintTwoLevelRefresh, Two level refresh will never be " +
                    "trigger!");
        }
        mRatioOfHeaderHeightToTwoLevelRefresh = ratio;
        mOffsetToTwoLevelRefresh = (int) (mHeaderHeight * ratio);
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
