package me.dkzwm.widget.srl;

/**
 * Created by dkzwm on 2017/7/24.
 *
 * @author dkzwm
 */
public interface IRefreshViewCreator {
    void createHeader(SmoothRefreshLayout layout);

    void createFooter(SmoothRefreshLayout layout);
}
