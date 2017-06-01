package me.dkzwm.smoothrefreshlayout.extra;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import me.dkzwm.smoothrefreshlayout.R;

/**
 * Created by dkzwm on 2017/5/31.
 *
 * @author dkzwm
 */
public class ClassicConfig {
    private static final String SP_NAME = "sr_classic_last_update_time";
    private static SimpleDateFormat sDataFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
            Locale.getDefault());

    public static String getLastUpdateTime(@NonNull Context context,
                                           long mLastUpdateTime,
                                           @NonNull String key) {
        if (mLastUpdateTime == -1 && !TextUtils.isEmpty(key)) {
            mLastUpdateTime = context.getSharedPreferences(SP_NAME, 0).getLong(key, -1);
        }
        if (mLastUpdateTime == -1) {
            return null;
        }
        long diffTime = new Date().getTime() - mLastUpdateTime;
        int seconds = (int) (diffTime / 1000);
        if (diffTime < 0) {
            return null;
        }
        if (seconds <= 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(context.getString(R.string.sr_last_update));

        if (seconds < 60) {
            sb.append(seconds);
            sb.append(context.getString(R.string.sr_seconds_ago));
        } else {
            int minutes = (seconds / 60);
            if (minutes > 60) {
                int hours = minutes / 60;
                if (hours > 24) {
                    Date date = new Date(mLastUpdateTime);
                    sb.append(sDataFormat.format(date));
                } else {
                    sb.append(hours);
                    sb.append(context.getString(R.string.sr_hours_ago));
                }

            } else {
                sb.append(minutes);
                sb.append(context.getString(R.string.sr_minutes_ago));
            }
        }
        return sb.toString();
    }

    public static void updateTime(@NonNull Context context, String key, long time) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SP_NAME, 0);
        if (!TextUtils.isEmpty(key)) {
            sharedPreferences.edit().putLong(key, time).apply();
        }
    }
}
