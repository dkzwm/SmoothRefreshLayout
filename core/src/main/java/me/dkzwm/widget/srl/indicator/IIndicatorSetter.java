package me.dkzwm.widget.srl.indicator;

import me.dkzwm.widget.srl.annotation.MovingStatus;

/**
 * Created by dkzwm on 2018/3/21.
 *
 * @author dkzwm
 */
public interface IIndicatorSetter {

    void setRatioToKeepFooter(float ratio);

    void setRatioToKeepHeader(float ratio);

    void setFooterHeight(int height);

    void setHeaderHeight(int height);

    void onFingerDown();

    void onFingerDown(float x, float y);

    void onFingerMove(float x, float y);

    void onFingerUp();

    void setRatioOfFooterToRefresh(float ratio);

    void setRatioOfHeaderToRefresh(float ratio);

    void setRatioToRefresh(float ratio);

    void onRefreshComplete();

    void setResistanceOfFooter(float resistance);

    void setResistance(float resistance);

    void setResistanceOfHeader(float resistance);

    void setCurrentPos(int current);

    void setMovingStatus(@MovingStatus int direction);

    void setMaxMoveRatio(float ratio);

    void setMaxMoveRatioOfHeader(float ratio);

    void setMaxMoveRatioOfFooter(float ratio);

    void setOffsetCalculator(IIndicator.IOffsetCalculator calculator);
}
