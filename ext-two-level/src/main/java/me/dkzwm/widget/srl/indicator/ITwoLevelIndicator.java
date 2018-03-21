package me.dkzwm.widget.srl.indicator;

/**
 * Created by dkzwm on 2017/6/12.
 *
 * @author dkzwm
 */
public interface ITwoLevelIndicator extends IIndicator {
    boolean crossTwoLevelCompletePos();

    int getOffsetToKeepTwoLevelHeader();

    int getOffsetToTwoLevelRefresh();

    int getOffsetToHintTwoLevelRefresh();

    boolean crossTwoLevelRefreshLine();

    boolean crossTwoLevelHintLine();

}
