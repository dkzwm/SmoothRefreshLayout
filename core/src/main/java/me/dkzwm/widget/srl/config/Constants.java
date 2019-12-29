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
package me.dkzwm.widget.srl.config;

/** @author dkzwm */
public interface Constants {
    int MODE_DEFAULT = 0;
    int MODE_SCALE = 1;

    int ACTION_NOTIFY = 0;
    int ACTION_AT_ONCE = 1;
    int ACTION_NOTHING = 2;

    int MOVING_CONTENT = 0;
    int MOVING_FOOTER = 1;
    int MOVING_HEADER = 2;

    byte SCROLLER_MODE_NONE = -1;
    byte SCROLLER_MODE_PRE_FLING = 0;
    byte SCROLLER_MODE_CALC_FLING = 1;
    byte SCROLLER_MODE_FLING = 2;
    byte SCROLLER_MODE_FLING_BACK = 3;
    byte SCROLLER_MODE_SPRING = 4;
    byte SCROLLER_MODE_SPRING_BACK = 5;
}
