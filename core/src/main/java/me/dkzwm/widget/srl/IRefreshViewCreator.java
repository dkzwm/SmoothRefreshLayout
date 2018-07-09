package me.dkzwm.widget.srl;

import me.dkzwm.widget.srl.extra.IRefreshView;
import me.dkzwm.widget.srl.indicator.IIndicator;

/**
 * Created by dkzwm on 2017/7/24.
 *
 * @author dkzwm
 */
public interface IRefreshViewCreator {
    IRefreshView<IIndicator> createHeader(SmoothRefreshLayout layout);

    IRefreshView<IIndicator> createFooter(SmoothRefreshLayout layout);
}
