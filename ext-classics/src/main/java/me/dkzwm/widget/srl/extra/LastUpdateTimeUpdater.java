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
package me.dkzwm.widget.srl.extra;

import androidx.annotation.NonNull;

/** @author dkzwm */
public class LastUpdateTimeUpdater implements Runnable {
    private AbsClassicRefreshView mRefreshView;
    private ITimeUpdater mUpdater;
    private boolean mRunning = false;

    LastUpdateTimeUpdater(AbsClassicRefreshView refreshView) {
        mRefreshView = refreshView;
        mUpdater = refreshView;
    }

    void setTimeUpdater(@NonNull ITimeUpdater updater) {
        mUpdater = updater;
    }

    public void start() {
        mRunning = true;
        if (mRefreshView != null) mRefreshView.post(this);
    }

    public boolean isRunning() {
        return mRunning;
    }

    public void stop() {
        mRunning = false;
        if (mRefreshView != null) mRefreshView.removeCallbacks(this);
    }

    @Override
    public void run() {
        if (mUpdater != null && mRefreshView != null) {
            if (mUpdater.canUpdate()) {
                mUpdater.updateTime(mRefreshView);
            }
            mRefreshView.removeCallbacks(this);
            if (mRunning) {
                mRefreshView.postDelayed(this, 1000);
            }
        }
    }

    public interface ITimeUpdater {
        boolean canUpdate();

        void updateTime(AbsClassicRefreshView view);
    }
}
