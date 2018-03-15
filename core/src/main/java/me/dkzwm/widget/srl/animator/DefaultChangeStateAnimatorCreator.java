package me.dkzwm.widget.srl.animator;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.support.annotation.NonNull;
import android.view.View;

/**
 * Created by dkzwm on 2018/3/15.
 *
 * @author dkzwm
 */
public class DefaultChangeStateAnimatorCreator implements IChangeStateAnimatorCreator {
    @NonNull
    @Override
    public ValueAnimator create(final View previous, final View current) {
        ValueAnimator animator = ObjectAnimator.ofFloat(1.0f, 0.0f).setDuration(250L);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                current.setVisibility(View.VISIBLE);
                current.setAlpha(0);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                previous.setVisibility(View.GONE);
                previous.setAlpha(1);
                current.setAlpha(1);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                previous.setVisibility(View.GONE);
                previous.setAlpha(1);
            }
        });
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                previous.setAlpha(value);
                current.setAlpha(1f - value);
            }
        });
        return animator;
    }

}
