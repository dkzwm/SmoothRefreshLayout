package me.dkzwm.smoothrefreshlayout;

import android.content.Context;
import android.util.AttributeSet;

import me.dkzwm.smoothrefreshlayout.extra.header.WaveHeader;

/**
 * Wave smooth refresh layout
 *
 * @author dkzwm
 */
public class WaveSmoothRefreshLayout extends SmoothRefreshLayout {
    private WaveHeader mWaveHeader;

    public WaveSmoothRefreshLayout(Context context) {
        this(context, null);
    }

    public WaveSmoothRefreshLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WaveSmoothRefreshLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mWaveHeader = new WaveHeader(context);
        setDisableRefresh(false);
        setHeaderView(mWaveHeader);
        setEnableHeaderDrawerStyle(true);
        setEnableKeepRefreshView(true);
        setCanMoveTheMaxRatioOfHeaderHeight(.4f);
        setRatioOfHeaderHeightToRefresh(.22f);
        setOffsetRatioToKeepHeaderWhileLoading(.22f);
        setDurationToCloseHeader(1000);
        setDurationOfBackToKeepHeaderPosition(1000);
    }

    public WaveHeader getDefaultHeader() {
        return mWaveHeader;
    }

}
