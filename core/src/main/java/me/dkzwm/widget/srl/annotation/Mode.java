package me.dkzwm.widget.srl.annotation;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import me.dkzwm.widget.srl.config.Constants;


@Retention(RetentionPolicy.SOURCE)
@IntDef(
        {
                Constants.MODE_DEFAULT,
                Constants.MODE_SCALE
        }
)
public @interface Mode {
}
