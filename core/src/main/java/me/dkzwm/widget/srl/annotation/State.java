package me.dkzwm.widget.srl.annotation;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import me.dkzwm.widget.srl.config.Constants;

/**
 * Created by dkzwm1 on 2018/2/27.
 */
@Retention(RetentionPolicy.SOURCE)
@IntDef(
        {
                Constants.STATE_NONE,
                Constants.STATE_CONTENT,
                Constants.STATE_ERROR,
                Constants.STATE_EMPTY,
                Constants.STATE_CUSTOM
        }
)
public @interface State {
}