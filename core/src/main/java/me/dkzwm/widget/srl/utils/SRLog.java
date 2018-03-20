package me.dkzwm.widget.srl.utils;

import android.util.Log;

/**
 * Created by dkzwm on 2017/5/22.
 *
 * @author dkzwm
 */
public class SRLog {

    private SRLog() {
    }

    public static void d(String tag, String msg) {
        Log.d(tag, msg);
    }

    public static void d(String tag, String msg, Object... args) {
        if (args.length > 0) {
            msg = String.format(msg, args);
        }
        Log.d(tag, msg);
    }
}
