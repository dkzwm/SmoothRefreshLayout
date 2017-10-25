package me.dkzwm.widget.srl.utils;

import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.webkit.WebView;
import android.widget.HorizontalScrollView;

/**
 * Created by dkzwm on 2017/10/25.
 *
 * @author dkzwm
 */

public class HorizontalScrollCompat {
    private HorizontalScrollCompat() {

    }


    public static boolean canChildScrollLeft(View view) {
        if (Build.VERSION.SDK_INT < 26)
            return ViewCompat.canScrollHorizontally(view, -1);
        else
            return view.canScrollHorizontally(-1);
    }

    public static boolean canChildScrollRight(View view) {
        if (Build.VERSION.SDK_INT < 26)
            return ViewCompat.canScrollHorizontally(view, 1);
        else
            return view.canScrollHorizontally(1);
    }


    public static boolean scrollCompat(View view, float deltaY) {
        if (view != null) {
            if ((view instanceof WebView)
                    || (view instanceof HorizontalScrollView)) {
                view.scrollBy((int) deltaY, 0);
            } else {
                try {
                    if (view instanceof RecyclerView) {
                        view.scrollBy((int) deltaY, 0);
                        return true;
                    }
                } catch (NoClassDefFoundError e) {
                    //ignore exception
                }
            }
        }
        return false;
    }
}
