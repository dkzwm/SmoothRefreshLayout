package me.dkzwm.widget.srl.indicator;

import android.support.annotation.NonNull;

import me.dkzwm.widget.srl.annotation.MovingStatus;

/**
 * Created by dkzwm on 2017/5/22.
 *
 * @author dkzwm
 */
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
