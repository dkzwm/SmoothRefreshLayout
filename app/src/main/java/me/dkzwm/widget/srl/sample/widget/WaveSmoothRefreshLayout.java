package me.dkzwm.widget.srl.sample.widget;

import android.content.Context;
import android.util.AttributeSet;

import me.dkzwm.widget.srl.SmoothRefreshLayout;
import me.dkzwm.widget.srl.sample.header.WaveHeader;

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
        setHeaderView(mWaveHeader);
        setEnableHeaderDrawerStyle(true);
        setEnableKeepRefreshView(true);
        setCanMoveTheMaxRatioOfHeaderHeight(.4f);
        setRatioOfHeaderHeightToRefresh(.22f);
        setOffsetRatioToKeepHeaderWhileLoading(.22f);
        setDurationToCloseHeader(380);
        setDurationOfBackToKeepHeaderPosition(850);
    }

    public WaveHeader getDefaultHeader() {
        return mWaveHeader;
    }

}
