package me.dkzwm.smoothrefreshlayout.sample;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

import me.dkzwm.smoothrefreshlayout.IRefreshViewCreator;
import me.dkzwm.smoothrefreshlayout.SmoothRefreshLayout;
import me.dkzwm.smoothrefreshlayout.extra.footer.ClassicFooter;
import me.dkzwm.smoothrefreshlayout.extra.header.ClassicHeader;

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
        SmoothRefreshLayout.setDefaultCreator(new IRefreshViewCreator() {
            @Override
            public void createHeader(SmoothRefreshLayout layout) {
                ClassicHeader header = new ClassicHeader(layout.getContext());
                header.setLastUpdateTimeKey("header_last_update_time");
                layout.setHeaderView(header);
            }

            @Override
            public void createFooter(SmoothRefreshLayout layout) {
                ClassicFooter footer = new ClassicFooter(layout.getContext());
                footer.setLastUpdateTimeKey("footer_last_update_time");
                layout.setFooterView(footer);
            }
        });
    }
}
