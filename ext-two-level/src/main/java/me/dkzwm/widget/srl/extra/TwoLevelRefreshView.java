package me.dkzwm.widget.srl.extra;

import me.dkzwm.widget.srl.TwoLevelSmoothRefreshLayout;
import me.dkzwm.widget.srl.indicator.IIndicator;
import me.dkzwm.widget.srl.indicator.ITwoLevelIndicator;

/**
 * Created by dkzwm on 2017/6/12.
 *
 * @author dkzwm
 */
public interface TwoLevelRefreshView<T extends ITwoLevelIndicator> extends IRefreshView<T> {
    void onTwoLevelRefreshBegin(TwoLevelSmoothRefreshLayout layout, T twoLevelIndicator);
}
