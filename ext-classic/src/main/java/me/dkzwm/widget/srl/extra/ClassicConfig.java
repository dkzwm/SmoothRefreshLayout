package me.dkzwm.widget.srl.extra;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import me.dkzwm.widget.srl.ext.classic.R;
import me.dkzwm.widget.srl.utils.PixelUtl;

/**
 * Created by dkzwm on 2017/5/31.
 *
 * @author dkzwm
 */
public class ClassicConfig {
    private static final String SP_NAME = "sr_classic_last_update_time";
    private static final SimpleDateFormat sDataFormat = new SimpleDateFormat
            ("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    private ClassicConfig() {
    }

    public static void updateTime(@NonNull Context context, String key, long time) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SP_NAME, 0);
        if (!TextUtils.isEmpty(key)) {
            sharedPreferences.edit().putLong(key, time).apply();
        }
    }

    static String getLastUpdateTime(@NonNull Context context, long mLastUpdateTime, @NonNull String key) {
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

    static void createClassicViews(RelativeLayout layout) {
        TextView textViewTitle = new TextView(layout.getContext());
        textViewTitle.setId(me.dkzwm.widget.srl.ext.classic.R.id.sr_classic_title);
        textViewTitle.setTextSize(12);
        textViewTitle.setTextColor(Color.parseColor("#333333"));
        TextView textViewLastUpdate = new TextView(layout.getContext());
        textViewLastUpdate.setId(me.dkzwm.widget.srl.ext.classic.R.id.sr_classic_last_update);
        textViewLastUpdate.setTextSize(10);
        textViewLastUpdate.setTextColor(Color.parseColor("#969696"));
        textViewLastUpdate.setVisibility(View.GONE);
        LinearLayout textContainer = new LinearLayout(layout.getContext());
        textContainer.setOrientation(LinearLayout.VERTICAL);
        textContainer.setGravity(Gravity.CENTER_HORIZONTAL);
        textContainer.setId(me.dkzwm.widget.srl.ext.classic.R.id.sr_classic_text_container);
        LinearLayout.LayoutParams textViewTitleLP = new LinearLayout.LayoutParams(ViewGroup
                .LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        textContainer.addView(textViewTitle, textViewTitleLP);
        LinearLayout.LayoutParams textViewLastUpdateLP = new LinearLayout.LayoutParams(ViewGroup
                .LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        textContainer.addView(textViewLastUpdate, textViewLastUpdateLP);
        RelativeLayout.LayoutParams textContainerLP = new RelativeLayout.LayoutParams(ViewGroup
                .LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        textContainerLP.addRule(RelativeLayout.CENTER_IN_PARENT);
        layout.addView(textContainer, textContainerLP);
        ImageView imageViewArrow = new ImageView(layout.getContext());
        imageViewArrow.setId(me.dkzwm.widget.srl.ext.classic.R.id.sr_classic_arrow);
        RelativeLayout.LayoutParams imageViewArrowLP = new RelativeLayout.LayoutParams(ViewGroup
                .LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final int dp6 = PixelUtl.dp2px(layout.getContext(), 6);
        imageViewArrowLP.setMargins(dp6, dp6, dp6, dp6);
        imageViewArrowLP.addRule(RelativeLayout.LEFT_OF, me.dkzwm.widget.srl.ext.classic.R.id
                .sr_classic_text_container);
        imageViewArrowLP.addRule(RelativeLayout.CENTER_VERTICAL);
        layout.addView(imageViewArrow, imageViewArrowLP);
        ProgressBar progressBar = new ProgressBar(layout.getContext(), null, android.R.attr
                .progressBarStyleSmallInverse);
        progressBar.setId(me.dkzwm.widget.srl.ext.classic.R.id.sr_classic_progress);
        RelativeLayout.LayoutParams progressBarLP = new RelativeLayout.LayoutParams(ViewGroup
                .LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        progressBarLP.setMargins(dp6, dp6, dp6, dp6);
        progressBarLP.addRule(RelativeLayout.LEFT_OF, me.dkzwm.widget.srl.ext.classic.R.id
                .sr_classic_text_container);
        progressBarLP.addRule(RelativeLayout.CENTER_VERTICAL);
        layout.addView(progressBar, progressBarLP);
    }
}
