package me.dkzwm.widget.srl.indicator;

/**
 * Created by dkzwm on 2017/6/12.
 *
 * @author dkzwm
 */
public interface ITwoLevelIndicator extends IIndicator {
    boolean crossTwoLevelCompletePos();

    void onTwoLevelRefreshComplete();

    void setRatioOfHeaderToHintTwoLevel(float ratio);

    void setRatioOfHeaderToTwoLevel(float ratio);

    void setRatioToKeepTwoLevelHeader(float ratio);

    int getOffsetToKeepTwoLevelHeader();

    int getOffsetToTwoLevelRefresh();

    int getOffsetToHintTwoLevelRefresh();

    boolean crossTwoLevelRefreshLine();

    boolean crossTwoLevelHintLine();

}
