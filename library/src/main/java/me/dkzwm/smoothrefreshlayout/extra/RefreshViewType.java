package me.dkzwm.smoothrefreshlayout.extra;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by dkzwm on 2017/5/23.
 *
 * @author dkzwm
 */
@IntDef({IRefreshView.TYPE_HEADER, IRefreshView.TYPE_FOOTER})
@Retention(RetentionPolicy.SOURCE)
@interface RefreshViewType {
}
