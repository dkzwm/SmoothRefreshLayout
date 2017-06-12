package me.dkzwm.smoothrefreshlayout.indicator;

/**
 * Created by dkzwm on 2017/6/12.
 *
 * @author dkzwm
 */
public interface ITwoLevelIndicator {
    boolean crossTwoLevelCompletePos();

    void onTwoLevelRefreshComplete();

    void setRatioOfHeaderHeightToHintTwoLevelRefresh(float ratio);

    void setRatioOfHeaderHeightToTwoLevelRefresh(float ratio);

    float getRatioOfHeaderHeightToHintTwoLevelRefresh();

    float getRatioOfHeaderHeightToTwoLevelRefresh();

    int getOffsetToTwoLevelRefresh();

    int getOffsetToHintTwoLevelRefresh();

    boolean crossTwoLevelRefreshLine();

    boolean crossTwoLevelHintLine();

}
