package me.dkzwm.widget.srl.extra;

import android.support.annotation.NonNull;

/**
 * Created by dkzwm on 2017/10/11.
 *
 * @author dkzwm
 */
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
