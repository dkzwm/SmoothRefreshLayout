package me.dkzwm.widget.srl.extra;

import android.view.View;

/**
 * Created by dkzwm on 2017/10/11.
 *
 * @author dkzwm
 */
public class LastUpdateTimeUpdater implements Runnable {
    private boolean mRunning = false;
    private ITimeUpdater mUpdater;
    private View mRefreshView;

    public LastUpdateTimeUpdater(ITimeUpdater updater, View refreshView) {
        mUpdater = updater;
        mRefreshView = refreshView;
    }

    public void start() {
        mRunning = true;
        if (mRefreshView != null)
            mRefreshView.post(this);
    }

    public void stop() {
        mRunning = false;
        if (mRefreshView != null)
            mRefreshView.removeCallbacks(this);
    }

    @Override
    public void run() {
        if (mUpdater != null && mRefreshView != null) {
            mUpdater.tryUpdateLastUpdateTime();
            mRefreshView.removeCallbacks(this);
            if (mRunning) {
                mRefreshView.postDelayed(this, 1000);
            }
        }
    }

    public interface ITimeUpdater {
        void tryUpdateLastUpdateTime();
    }
}
