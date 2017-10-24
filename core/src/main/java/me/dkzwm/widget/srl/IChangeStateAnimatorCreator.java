package me.dkzwm.widget.srl;

import android.animation.ValueAnimator;
import android.support.annotation.NonNull;
import android.view.View;

/**
 * Created by dkzwm on 2017/10/10.
 *
 * @author dkzwm
 */

public interface IChangeStateAnimatorCreator {
    @NonNull
    ValueAnimator create(final View previous, final View current);
}
