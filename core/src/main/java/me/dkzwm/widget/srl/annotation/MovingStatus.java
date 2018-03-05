package me.dkzwm.widget.srl.annotation;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import me.dkzwm.widget.srl.config.Constants;

/**
 * Created by dkzwm on 2018/3/5.
 *
 * @author dkzwm
 */

@IntDef({Constants.MOVING_CONTENT, Constants.MOVING_FOOTER, Constants.MOVING_HEADER})
@Retention(RetentionPolicy.SOURCE)
public @interface MovingStatus {
}