package me.dkzwm.widget.srl.sample;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

import me.dkzwm.widget.srl.IRefreshViewCreator;
import me.dkzwm.widget.srl.SmoothRefreshLayout;
import me.dkzwm.widget.srl.extra.footer.ClassicFooter;
import me.dkzwm.widget.srl.extra.header.ClassicHeader;

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
