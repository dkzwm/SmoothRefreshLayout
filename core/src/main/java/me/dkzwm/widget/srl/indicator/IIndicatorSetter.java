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

import me.dkzwm.widget.srl.annotation.MovingStatus;

/** @author dkzwm */
public interface IIndicatorSetter {

    void setRatioToKeepFooter(float ratio);

    void setRatioToKeepHeader(float ratio);

    void setFooterHeight(int height);

    void setHeaderHeight(int height);

    void onFingerDown();

    void onFingerDown(float x, float y);

    void onFingerMove(float x, float y);

    void onFingerUp();

    void setRatioOfFooterToRefresh(float ratio);

    void setRatioOfHeaderToRefresh(float ratio);

    void setRatioToRefresh(float ratio);

    void setResistanceOfFooter(float resistance);

    void setResistance(float resistance);

    void setResistanceOfHeader(float resistance);

    void setCurrentPos(int current);

    void setMovingStatus(@MovingStatus int direction);

    void setMaxMoveRatio(float ratio);

    void setMaxMoveRatioOfHeader(float ratio);

    void setMaxMoveRatioOfFooter(float ratio);

    void setOffsetCalculator(IIndicator.IOffsetCalculator calculator);
}
