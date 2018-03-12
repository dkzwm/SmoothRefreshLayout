package me.dkzwm.widget.srl.indicator;

import android.support.annotation.NonNull;

import me.dkzwm.widget.srl.annotation.MovingStatus;

/**
 * Created by dkzwm on 2017/5/22.
 *
 * @author dkzwm
 */
public interface IIndicator {
    float DEFAULT_RATIO_TO_REFRESH = 1.1f;
    float DEFAULT_MAX_MOVE_RATIO = 0f;
    float DEFAULT_OFFSET_RATIO_TO_KEEP_REFRESH_WHILE_LOADING = 1;
    float DEFAULT_RESISTANCE = 1.65f;
    int START_POS = 0;

    @MovingStatus
    int getMovingStatus();

    void setMovingStatus(@MovingStatus int direction);

    int getCurrentPos();

    void setCurrentPos(int current);

    boolean hasTouched();

    boolean hasMoved();

    float getResistanceOfPullDown();

    void setResistanceOfHeader(float resistance);

    float getResistanceOfPullUp();

    void setResistanceOfFooter(float resistance);

    void setResistance(float resistance);

    void onRefreshComplete();

    void setRatioToRefresh(float ratio);

    float getRatioOfHeaderToRefresh();

    void setRatioOfHeaderToRefresh(float ratio);

    float getRatioOfFooterToRefresh();

    void setRatioOfFooterToRefresh(float ratio);

    int getOffsetToRefresh();

    int getOffsetToLoadMore();

    void onFingerDown();

    void onFingerDown(float x, float y);

    void onFingerMove(float x, float y);

    void onFingerUp();

    float getOffset();

    int getLastPos();

    int getHeaderHeight();

    void setHeaderHeight(int height);

    int getFooterHeight();

    void setFooterHeight(int height);

    void convert(IIndicator indicator);

    boolean hasLeftStartPosition();

    boolean hasJustLeftStartPosition();

    boolean hasJustBackToStartPosition();

    boolean isOverOffsetToRefresh();

    boolean isOverOffsetToLoadMore();

    boolean hasMovedAfterPressedDown();

    boolean isInStartPosition();

    boolean crossRefreshLineFromTopToBottom();

    boolean crossRefreshLineFromBottomToTop();

    boolean isOverOffsetToKeepHeaderWhileLoading();

    boolean isOverOffsetToKeepFooterWhileLoading();

    int getOffsetToKeepHeaderWhileLoading();

    int getOffsetToKeepFooterWhileLoading();

    void setRatioToKeepFooter(float ratio);

    void setRatioToKeepHeader(float ratio);

    boolean isAlreadyHere(int to);

    boolean willOverTop(int to);

    void setMaxMoveRatio(float ratio);

    void setMaxMoveRatioOfHeader(float ratio);

    void setMaxMoveRatioOfFooter(float ratio);

    float getCanMoveTheMaxDistanceOfHeader();

    float getCanMoveTheMaxDistanceOfFooter();

    @NonNull
    float[] getFingerDownPoint();

    @NonNull
    float[] getLastMovePoint();

    float getCurrentPercentOfRefreshOffset();

    float getCurrentPercentOfLoadMoreOffset();

    void setOffsetCalculator(IOffsetCalculator calculator);

    /**
     * Created by dkzwm on 2017/10/24.
     *
     * @author dkzwm
     */
    interface IOffsetCalculator {
        float calculate(@MovingStatus int status, int currentPos, float offset);
    }
}
