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

/** @author dkzwm */
public class DefaultTwoLevelIndicator extends DefaultIndicator
        implements ITwoLevelIndicator, ITwoLevelIndicatorSetter {
    private int mOffsetToHintTwoLevelRefresh = 0;
    private int mOffsetToTwoLevelRefresh = 0;
    private float mOffsetRatioToKeepTwoLevelHeaderWhileLoading = 1f;
    private float mRatioOfHeaderHeightToHintTwoLevelRefresh = 1.5f;
    private float mRatioOfHeaderHeightToTwoLevelRefresh = 2.0f;

    @Override
    public void setHeaderHeight(int height) {
        super.setHeaderHeight(height);
        mOffsetToHintTwoLevelRefresh =
                (int) (mHeaderHeight * mRatioOfHeaderHeightToHintTwoLevelRefresh);
        mOffsetToTwoLevelRefresh = (int) (mHeaderHeight * mRatioOfHeaderHeightToTwoLevelRefresh);
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
            Log.e(
                    getClass().getSimpleName(),
                    "If the height ratio of the Two-Level refresh is "
                            + "less than the height ratio of the triggered Two-Level hint, the Two-Level "
                            + "refresh will never be triggered!");
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

    @Override
    public float getCurrentPercentOfTwoLevelRefreshOffset() {
        return mHeaderHeight <= 0 ? 0 : mCurrentPos * 1f / mOffsetToTwoLevelRefresh;
    }
}
