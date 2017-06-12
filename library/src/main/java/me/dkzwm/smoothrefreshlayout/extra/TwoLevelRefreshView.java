package me.dkzwm.smoothrefreshlayout.extra;

import me.dkzwm.smoothrefreshlayout.TwoLevelSmoothRefreshLayout;
import me.dkzwm.smoothrefreshlayout.indicator.IIndicator;
import me.dkzwm.smoothrefreshlayout.indicator.ITwoLevelIndicator;

/**
 * Created by dkzwm on 2017/6/12.
 *
 * @author dkzwm
 */
public interface TwoLevelRefreshView extends IRefreshView {

    void onTwoLevelRefreshBegin(TwoLevelSmoothRefreshLayout layout, IIndicator indicator,
                                ITwoLevelIndicator twoLevelIndicator);
}
