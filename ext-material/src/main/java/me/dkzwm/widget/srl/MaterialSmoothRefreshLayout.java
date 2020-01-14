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
import android.graphics.Color;
import android.util.AttributeSet;
import me.dkzwm.widget.srl.config.Constants;
import me.dkzwm.widget.srl.extra.footer.MaterialFooter;
import me.dkzwm.widget.srl.extra.header.MaterialHeader;
import me.dkzwm.widget.srl.indicator.IIndicator;
import me.dkzwm.widget.srl.util.PixelUtl;

/** @author dkzwm */
public class MaterialSmoothRefreshLayout extends SmoothRefreshLayout {
    protected OnUIPositionChangedListener mOnUIPositionChangedListener =
            new OnUIPositionChangedListener() {
                int mLastMovingStatus = Constants.MOVING_CONTENT;

                @Override
                public void onChanged(byte status, IIndicator indicator) {
                    int movingStatus = indicator.getMovingStatus();
                    if (movingStatus == Constants.MOVING_HEADER) {
                        if (movingStatus != mLastMovingStatus) {
                            setEnablePinRefreshViewWhileLoading(true);
                        }
                    } else {
                        if (movingStatus != mLastMovingStatus) {
                            setEnablePinContentView(false);
                        }
                    }
                    mLastMovingStatus = movingStatus;
                }
            };

    public MaterialSmoothRefreshLayout(Context context) {
        super(context);
    }

    public MaterialSmoothRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MaterialSmoothRefreshLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super.init(context, attrs, defStyleAttr, defStyleRes);
        MaterialHeader header = new MaterialHeader(context);
        header.setColorSchemeColors(new int[] {Color.RED, Color.BLUE, Color.GREEN, Color.BLACK});
        header.setPadding(0, PixelUtl.dp2px(context, 25), 0, PixelUtl.dp2px(context, 20));
        setHeaderView(header);
        MaterialFooter footer = new MaterialFooter(context);
        setFooterView(footer);
    }

    /**
     * Quickly set to material style. Before you change the configuration, you must know which
     * parameters have been configured
     */
    public void materialStyle() {
        setRatioOfFooterToRefresh(.95f);
        setMaxMoveRatioOfFooter(1f);
        setRatioToKeep(1f);
        setMaxMoveRatioOfHeader(1.5f);
        setEnablePinContentView(true);
        setEnableKeepRefreshView(true);
        if (mHeaderView instanceof MaterialHeader)
            ((MaterialHeader) mHeaderView).doHookUIRefreshComplete(this);
        if (!isDisabledLoadMore()) {
            addOnUIPositionChangedListener(mOnUIPositionChangedListener);
        }
    }
}
