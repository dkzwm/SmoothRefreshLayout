package me.dkzwm.widget.srl.utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;

/**
 * Created by dkzwm on 2017/5/31.
 *
 * @author dkzwm
 */
public class PixelUtl {
    private PixelUtl() {
    }

    public static int dp2px(Context context, float offSet) {
        Resources r;
        if (context == null)
            r = Resources.getSystem();
        else
            r = context.getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                offSet, r.getDisplayMetrics()));
    }

}
