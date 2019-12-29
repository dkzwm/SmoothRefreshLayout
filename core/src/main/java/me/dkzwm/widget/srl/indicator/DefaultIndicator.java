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
package me.dkzwm.widget.srl.indicator;

import android.util.Log;
import androidx.annotation.NonNull;
import me.dkzwm.widget.srl.annotation.MovingStatus;
import me.dkzwm.widget.srl.config.Constants;

/** @author dkzwm */
public class DefaultIndicator implements IIndicator, IIndicatorSetter {
    final float[] mLastMovePoint = new float[] {0f, 0f};
    final float[] mFingerDownPoint = new float[] {0f, 0f};
    IOffsetCalculator mOffsetCalculator;
    float mRawOffsetX;
    float mRawOffsetY;
    int mCurrentPos = 0;
    int mLastPos = 0;
    int mHeaderHeight = -1;
    int mFooterHeight = -1;
    int mPressedPos = 0;
    float mOffset;
    boolean mTouched = false;
    @MovingStatus private int mStatus = Constants.MOVING_CONTENT;
    private float mResistanceHeader = DEFAULT_RESISTANCE;
    private float mResistanceFooter = DEFAULT_RESISTANCE;
    private int mOffsetToRefresh = 0;
    private int mOffsetToKeepHeader = 0;
    private int mOffsetToLoadMore = 0;
    private int mOffsetToKeepFooter = 0;
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
        return mCurrentPos != mPressedPos;
    }

    @Override
    public void setResistanceOfHeader(float resistance) {
        mResistanceHeader = resistance;
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
        mTouched = false;
        mPressedPos = 0;
    }

    @Override
    public void setRatioToRefresh(float ratio) {
        setRatioOfHeaderToRefresh(ratio);
        setRatioOfFooterToRefresh(ratio);
    }

    @Override
    public void setRatioOfHeaderToRefresh(float ratio) {
        mRatioOfHeaderHeightToRefresh = ratio;
        mOffsetToRefresh = (int) (mHeaderHeight * ratio);
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
        float offsetX = (x - mLastMovePoint[0]);
        float offsetY = (y - mLastMovePoint[1]);
        processOnMove(offsetY);
        mRawOffsetX = offsetX;
        mRawOffsetY = offsetY;
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
        mOffsetToKeepHeader = (int) (mOffsetRatioToKeepHeaderWhileLoading * mHeaderHeight);
    }

    @Override
    public int getFooterHeight() {
        return mFooterHeight;
    }

    @Override
    public void setFooterHeight(int height) {
        mFooterHeight = height;
        mOffsetToLoadMore = (int) (mRatioOfFooterHeightToLoadMore * mFooterHeight);
        mOffsetToKeepFooter = (int) (mOffsetRatioToKeepFooterWhileLoading * mFooterHeight);
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
        return mLastPos != START_POS && mCurrentPos == START_POS;
    }

    @Override
    public boolean isJustReturnedOffsetToRefresh() {
        return mLastPos > mOffsetToRefresh
                && mLastPos > mCurrentPos
                && mCurrentPos <= mOffsetToRefresh;
    }

    @Override
    public boolean isJustReturnedOffsetToLoadMore() {
        return mLastPos > mOffsetToLoadMore
                && mLastPos > mCurrentPos
                && mCurrentPos <= mOffsetToLoadMore;
    }

    @Override
    public boolean isOverOffsetToKeepHeaderWhileLoading() {
        return mHeaderHeight >= 0 && mCurrentPos >= mOffsetToKeepHeader;
    }

    @Override
    public boolean isOverOffsetToRefresh() {
        return mCurrentPos >= mOffsetToRefresh;
    }

    @Override
    public boolean isOverOffsetToKeepFooterWhileLoading() {
        return mFooterHeight >= 0 && mCurrentPos >= mOffsetToKeepFooter;
    }

    @Override
    public boolean isOverOffsetToLoadMore() {
        return mCurrentPos >= mOffsetToLoadMore;
    }

    @Override
    public int getOffsetToKeepHeaderWhileLoading() {
        return mOffsetToKeepHeader;
    }

    @Override
    public int getOffsetToKeepFooterWhileLoading() {
        return mOffsetToKeepFooter;
    }

    @Override
    public void setRatioToKeepFooter(float ratio) {
        mOffsetRatioToKeepFooterWhileLoading = ratio;
        mOffsetToKeepFooter = (int) (mOffsetRatioToKeepFooterWhileLoading * mFooterHeight);
    }

    @Override
    public void setRatioToKeepHeader(float ratio) {
        mOffsetRatioToKeepHeaderWhileLoading = ratio;
        mOffsetToKeepHeader = (int) (mOffsetRatioToKeepHeaderWhileLoading * mHeaderHeight);
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
    public void checkConfig() {
        if (mCanMoveTheMaxRatioOfHeaderHeight > 0
                && mCanMoveTheMaxRatioOfHeaderHeight < mRatioOfHeaderHeightToRefresh) {
            Log.e(
                    getClass().getSimpleName(),
                    "If the max can move ratio of header less than "
                            + "the triggered refresh ratio of header, refresh will be never trigger!");
        }
        if (mCanMoveTheMaxRatioOfFooterHeight > 0
                && mCanMoveTheMaxRatioOfFooterHeight < mRatioOfFooterHeightToLoadMore) {
            Log.e(
                    getClass().getSimpleName(),
                    "If the max can move ratio of footer less than "
                            + "the triggered load more ratio of footer, load more will be never trigger!");
        }
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
        mCanMoveTheMaxRatioOfHeaderHeight = ratio;
    }

    @Override
    public void setMaxMoveRatioOfFooter(float ratio) {
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

    @Override
    public float getRawOffset() {
        return mRawOffsetY;
    }

    @Override
    public float[] getRawOffsets() {
        return new float[] {mRawOffsetX, mRawOffsetY};
    }
}
