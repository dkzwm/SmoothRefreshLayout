package me.dkzwm.smoothrefreshlayout.sample;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

/**
 * Created by dkzwm on 2017/6/28.
 *
 * @author dkzwm
 */

public class DemoApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        LeakCanary.install(this);
    }
}
