package me.dkzwm.widget.srl;

/**
 * Created by dkzwm on 2017/12/18.
 *
 * @author dkzwm
 */
public interface ILifecycleObserver {
    void onAttached(SmoothRefreshLayout layout);

    void onDetached(SmoothRefreshLayout layout);
}
