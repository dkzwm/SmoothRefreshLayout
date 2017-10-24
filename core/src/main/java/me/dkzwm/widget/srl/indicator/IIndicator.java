package me.dkzwm.widget.srl.indicator;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by dkzwm on 2017/5/22.
 *
 * @author dkzwm
 */
public interface IIndicator {
    float DEFAULT_RATIO_OF_REFRESH_VIEW_HEIGHT_TO_REFRESH = 1.1f;
    float DEFAULT_CAN_MOVE_THE_MAX_RATIO_OF_REFRESH_VIEW_HEIGHT = 0f;
    float DEFAULT_OFFSET_RATIO_TO_KEEP_REFRESH_WHILE_LOADING = 1;
    float DEFAULT_RESISTANCE = 1.65f;
    int START_POS = 0;
    int MOVING_CONTENT = 0;
    int MOVING_FOOTER = 1;
    int MOVING_HEADER = 2;

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

    void setRatioOfRefreshViewHeightToRefresh(float ratio);

    float getRatioOfHeaderHeightToRefresh();

    void setRatioOfHeaderHeightToRefresh(float ratio);

    float getRatioOfFooterHeightToRefresh();

    void setRatioOfFooterHeightToRefresh(float ratio);

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

    boolean hasJustReachedHeaderHeightFromTopToBottom();

    boolean hasJustReachedFooterHeightFromBottomToTop();

    boolean isOverOffsetToKeepHeaderWhileLoading();

    boolean isOverOffsetToKeepFooterWhileLoading();

    boolean isInKeepFooterWhileLoadingPos();

    boolean isInKeepHeaderWhileLoadingPos();

    int getOffsetToKeepHeaderWhileLoading();

    int getOffsetToKeepFooterWhileLoading();

    void setOffsetRatioToKeepFooterWhileLoading(float ratio);

    void setOffsetRatioToKeepHeaderWhileLoading(float ratio);

    boolean isAlreadyHere(int to);

    boolean willOverTop(int to);

    void setCanMoveTheMaxRatioOfRefreshViewHeight(float ratio);

    float getCanMoveTheMaxRatioOfHeaderHeight();

    void setCanMoveTheMaxRatioOfHeaderHeight(float ratio);

    float getCanMoveTheMaxRatioOfFooterHeight();

    void setCanMoveTheMaxRatioOfFooterHeight(float ratio);

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
    public interface IOffsetCalculator {
        float calculate(@MovingStatus int status, int currentPos, float offset);
    }

    @IntDef({MOVING_CONTENT, MOVING_FOOTER, MOVING_HEADER})
    @Retention(RetentionPolicy.SOURCE)
    public @interface MovingStatus {
    }
}
