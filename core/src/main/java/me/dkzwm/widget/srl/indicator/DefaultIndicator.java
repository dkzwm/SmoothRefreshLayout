package me.dkzwm.widget.srl.indicator;

import android.support.annotation.NonNull;

import me.dkzwm.widget.srl.annotation.MovingStatus;
import me.dkzwm.widget.srl.config.Constants;

/**
 * @author dkzwm
 */
public class DefaultIndicator implements IIndicator, IIndicatorSetter {
    protected final float[] mLastMovePoint = new float[]{0f, 0f};
    protected final float[] mFingerDownPoint = new float[]{0f, 0f};
    protected IOffsetCalculator mOffsetCalculator;
    protected int mCurrentPos = 0;
    protected int mLastPos = 0;
    protected int mHeaderHeight = -1;
    protected int mFooterHeight = -1;
    protected int mPressedPos = 0;
    protected int mRefreshCompleteY = 0;
    protected float mOffset;
    protected boolean mTouched = false;
    protected boolean mMoved = false;
    @MovingStatus
    protected int mStatus = Constants.MOVING_CONTENT;
    protected float mResistanceHeader = DEFAULT_RESISTANCE;
    protected float mResistanceFooter = DEFAULT_RESISTANCE;
    private int mOffsetToRefresh = 1;
    private int mOffsetToLoadMore = 1;
    private float mOffsetRatioToKeepHeaderWhileLoading = DEFAULT_RATIO_TO_KEEP;
    private float mOffsetRatioToKeepFooterWhileLoading = DEFAULT_RATIO_TO_KEEP;
    private float mRatioOfHeaderHeightToRefresh = DEFAULT_RATIO_TO_REFRESH;
    private float mRatioOfFooterHeightToLoadMore = DEFAULT_RATIO_TO_REFRESH;
    private float mCanMoveTheMaxRatioOfHeaderHeight = DEFAULT_MAX_MOVE_RATIO;
    private float mCanMoveTheMaxRatioOfFooterHeight = DEFAULT_MAX_MOVE_RATIO;

    @Override
    public boolean hasTouched() {
        return mTouched;
    }

    @Override
    public boolean hasMoved() {
        return mMoved;
    }

    @Override
    public float getResistanceOfHeader() {
        return mResistanceHeader;
    }

    @Override
    public void setResistanceOfHeader(float resistance) {
        mResistanceHeader = resistance;
    }

    @Override
    public float getResistanceOfFooter() {
        return mResistanceFooter;
    }

    @Override
    public void setResistanceOfFooter(float resistance) {
        mResistanceFooter = resistance;
    }

    @Override
    public void setResistance(float resistance) {
        mResistanceHeader = resistance;
        mResistanceFooter = resistance;
    }

    @Override
    public void onFingerUp() {
        mLastPos = 0;
        mTouched = false;
        mMoved = false;
    }

    @Override
    public void onRefreshComplete() {
        mRefreshCompleteY = mCurrentPos;
    }

    @Override
    public void setRatioToRefresh(float ratio) {
        mRatioOfHeaderHeightToRefresh = ratio;
        mRatioOfFooterHeightToLoadMore = ratio;
        mOffsetToRefresh = (int) (mHeaderHeight * ratio);
        mOffsetToLoadMore = (int) (mFooterHeight * ratio);
    }

    @Override
    public float getRatioOfHeaderToRefresh() {
        return mRatioOfHeaderHeightToRefresh;
    }

    @Override
    public void setRatioOfHeaderToRefresh(float ratio) {
        mRatioOfHeaderHeightToRefresh = ratio;
        mOffsetToRefresh = (int) (mHeaderHeight * ratio);
    }

    @Override
    public float getRatioOfFooterToRefresh() {
        return mRatioOfFooterHeightToLoadMore;
    }

    @Override
    public void setRatioOfFooterToRefresh(float ratio) {
        mRatioOfFooterHeightToLoadMore = ratio;
        mOffsetToLoadMore = (int) (mFooterHeight * ratio);
    }

    @Override
    public int getOffsetToRefresh() {
        return mOffsetToRefresh;
    }

    @Override
    public int getOffsetToLoadMore() {
        return mOffsetToLoadMore;
    }

    @Override
    public void onFingerDown() {
        mTouched = true;
    }

    @Override
    public void onFingerDown(float x, float y) {
        mTouched = true;
        mPressedPos = mCurrentPos;
        mLastMovePoint[0] = x;
        mLastMovePoint[1] = y;
        mFingerDownPoint[0] = x;
        mFingerDownPoint[1] = y;
    }

    @Override
    public void onFingerMove(float x, float y) {
        mMoved = true;
        float offsetY = (y - mLastMovePoint[1]);
        processOnMove(offsetY);
        mLastMovePoint[0] = x;
        mLastMovePoint[1] = y;
    }

    @Override
    public float getOffset() {
        return mOffset;
    }

    @Override
    public int getLastPos() {
        return mLastPos;
    }

    @Override
    public int getMovingStatus() {
        return mStatus;
    }

    @Override
    public void setMovingStatus(@MovingStatus int status) {
        mStatus = status;
    }

    @Override
    public int getCurrentPos() {
        return mCurrentPos;
    }

    @Override
    public void setCurrentPos(int current) {
        mLastPos = mCurrentPos;
        mCurrentPos = current;
    }

    @Override
    public int getHeaderHeight() {
        return mHeaderHeight;
    }

    @Override
    public void setHeaderHeight(int height) {
        mHeaderHeight = height;
        mOffsetToRefresh = (int) (mRatioOfHeaderHeightToRefresh * mHeaderHeight);
    }

    @Override
    public int getFooterHeight() {
        return mFooterHeight;
    }

    @Override
    public void setFooterHeight(int height) {
        mFooterHeight = height;
        mOffsetToLoadMore = (int) (mRatioOfFooterHeightToLoadMore * mFooterHeight);
    }

    @Override
    public void convert(IIndicator indicator) {
        mCurrentPos = indicator.getCurrentPos();
        mLastPos = indicator.getLastPos();
        mHeaderHeight = indicator.getHeaderHeight();
        mFooterHeight = indicator.getFooterHeight();
        mRatioOfHeaderHeightToRefresh = indicator.getRatioOfHeaderToRefresh();
        mRatioOfFooterHeightToLoadMore = indicator.getRatioOfFooterToRefresh();
        mOffsetToRefresh = indicator.getOffsetToRefresh();
        mOffsetToLoadMore = indicator.getOffsetToLoadMore();
        mResistanceHeader = indicator.getResistanceOfHeader();
        mResistanceFooter = indicator.getResistanceOfFooter();
    }

    @Override
    public boolean hasLeftStartPosition() {
        return mCurrentPos > START_POS;
    }

    @Override
    public boolean hasJustLeftStartPosition() {
        return mLastPos == START_POS && hasLeftStartPosition();
    }

    @Override
    public boolean hasJustBackToStartPosition() {
        return mLastPos != START_POS && isInStartPosition();
    }

    @Override
    public boolean isOverOffsetToRefresh() {
        return mCurrentPos >= getOffsetToRefresh();
    }

    @Override
    public boolean isOverOffsetToLoadMore() {
        return mCurrentPos >= getOffsetToLoadMore();
    }

    @Override
    public boolean hasMovedAfterPressedDown() {
        return mCurrentPos != mPressedPos;
    }

    @Override
    public boolean isInStartPosition() {
        return mCurrentPos == START_POS;
    }

    @Override
    public boolean crossRefreshLineFromTopToBottom() {
        return mLastPos < getOffsetToRefresh() && mCurrentPos >= getOffsetToRefresh();
    }

    @Override
    public boolean crossRefreshLineFromBottomToTop() {
        return mLastPos < getOffsetToLoadMore() && mCurrentPos >= getOffsetToLoadMore();
    }

    @Override
    public boolean isOverOffsetToKeepHeaderWhileLoading() {
        return mCurrentPos >= getOffsetToKeepHeaderWhileLoading();
    }

    @Override
    public boolean isOverOffsetToKeepFooterWhileLoading() {
        return mCurrentPos >= getOffsetToKeepFooterWhileLoading();
    }

    @Override
    public int getOffsetToKeepHeaderWhileLoading() {
        return (int) (mOffsetRatioToKeepHeaderWhileLoading * mHeaderHeight);
    }

    @Override
    public int getOffsetToKeepFooterWhileLoading() {
        return (int) (mOffsetRatioToKeepFooterWhileLoading * mFooterHeight);
    }

    @Override
    public void setRatioToKeepFooter(float ratio) {
        mOffsetRatioToKeepFooterWhileLoading = ratio;
    }

    @Override
    public void setRatioToKeepHeader(float ratio) {
        mOffsetRatioToKeepHeaderWhileLoading = ratio;
    }

    @Override
    public boolean isAlreadyHere(int to) {
        return mCurrentPos == to;
    }

    @Override
    public float getCurrentPercentOfRefreshOffset() {
        return mHeaderHeight <= 0 ? 0 : mCurrentPos * 1f / mOffsetToRefresh;
    }

    @Override
    public float getCurrentPercentOfLoadMoreOffset() {
        return mFooterHeight <= 0 ? 0 : mCurrentPos * 1f / mOffsetToLoadMore;
    }

    @Override
    public void setOffsetCalculator(IOffsetCalculator calculator) {
        mOffsetCalculator = calculator;
    }

    @Override
    public void setMaxMoveRatio(float ratio) {
        setMaxMoveRatioOfHeader(ratio);
        setMaxMoveRatioOfFooter(ratio);
    }

    @Override
    public void setMaxMoveRatioOfHeader(float ratio) {
        if (mCanMoveTheMaxRatioOfHeaderHeight > 0
                && mCanMoveTheMaxRatioOfHeaderHeight < mRatioOfHeaderHeightToRefresh)
            throw new RuntimeException("If mCanMoveTheMaxRatioOfHeaderHeight less than " +
                    "RatioOfHeaderHeightToRefresh, refresh will be never trigger!");
        mCanMoveTheMaxRatioOfHeaderHeight = ratio;
    }

    @Override
    public void setMaxMoveRatioOfFooter(float ratio) {
        if (mCanMoveTheMaxRatioOfFooterHeight > 0
                && mCanMoveTheMaxRatioOfFooterHeight < mRatioOfFooterHeightToLoadMore)
            throw new RuntimeException("If MaxRatioOfFooterWhenFingerMoves less than " +
                    "RatioOfFooterHeightToLoadMore, load more will be never trigger!");
        mCanMoveTheMaxRatioOfFooterHeight = ratio;
    }

    @Override
    public float getCanMoveTheMaxDistanceOfHeader() {
        return mCanMoveTheMaxRatioOfHeaderHeight * mHeaderHeight;
    }

    @Override
    public float getCanMoveTheMaxDistanceOfFooter() {
        return mCanMoveTheMaxRatioOfFooterHeight * mFooterHeight;
    }

    @Override
    @NonNull
    public float[] getFingerDownPoint() {
        return mFingerDownPoint;
    }

    @NonNull
    @Override
    public float[] getLastMovePoint() {
        return mLastMovePoint;
    }

    protected void processOnMove(float offset) {
        if (mOffsetCalculator != null) {
            mOffset = mOffsetCalculator.calculate(mStatus, mCurrentPos, offset);
        } else {
            if (mStatus == Constants.MOVING_HEADER) {
                mOffset = offset / mResistanceHeader;
            } else if (mStatus == Constants.MOVING_FOOTER) {
                mOffset = offset / mResistanceFooter;
            } else {
                if (offset > 0) {
                    mOffset = offset / mResistanceHeader;
                } else if (offset < 0) {
                    mOffset = offset / mResistanceFooter;
                } else {
                    mOffset = offset;
                }
            }
        }
    }
}
