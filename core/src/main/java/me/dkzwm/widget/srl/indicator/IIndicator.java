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

import androidx.annotation.NonNull;
import me.dkzwm.widget.srl.annotation.MovingStatus;

/** @author dkzwm */
public interface IIndicator {
    float DEFAULT_RATIO_TO_REFRESH = 1f;
    float DEFAULT_MAX_MOVE_RATIO = 0f;
    float DEFAULT_RATIO_TO_KEEP = 1;
    float DEFAULT_RESISTANCE = 1.65f;
    int START_POS = 0;

    @MovingStatus
    int getMovingStatus();

    int getCurrentPos();

    boolean hasTouched();

    boolean hasMoved();

    int getOffsetToRefresh();

    int getOffsetToLoadMore();

    float getOffset();

    float getRawOffset();

    float[] getRawOffsets();

    int getLastPos();

    int getHeaderHeight();

    int getFooterHeight();

    boolean hasLeftStartPosition();

    boolean hasJustLeftStartPosition();

    boolean hasJustBackToStartPosition();

    boolean isJustReturnedOffsetToRefresh();

    boolean isJustReturnedOffsetToLoadMore();

    boolean isOverOffsetToKeepHeaderWhileLoading();

    boolean isOverOffsetToRefresh();

    boolean isOverOffsetToKeepFooterWhileLoading();

    boolean isOverOffsetToLoadMore();

    int getOffsetToKeepHeaderWhileLoading();

    int getOffsetToKeepFooterWhileLoading();

    boolean isAlreadyHere(int to);

    float getCanMoveTheMaxDistanceOfHeader();

    float getCanMoveTheMaxDistanceOfFooter();

    @NonNull
    float[] getFingerDownPoint();

    @NonNull
    float[] getLastMovePoint();

    float getCurrentPercentOfRefreshOffset();

    float getCurrentPercentOfLoadMoreOffset();

    void checkConfig();

    /**
     * Created by dkzwm on 2017/10/24.
     *
     * @author dkzwm
     */
    interface IOffsetCalculator {
        float calculate(@MovingStatus int status, int currentPos, float offset);
    }
}
