package me.dkzwm.smoothrefreshlayout.extra;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by dkzwm1 on 2017/5/23.
 */


@IntDef({IExtraView.TYPE_HEADER, IExtraView.TYPE_FOOTER})
@Retention(RetentionPolicy.SOURCE)
public @interface ExtraViewType {
}
